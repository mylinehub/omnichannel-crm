package com.mylinehub.voicebridge.recording;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CallRecordingManager (timestamp-aligned + batched flushing + silence-timeout watermark)
 *
 * What this does:
 * --------------
 * Produces ONE stereo WAV that matches the *real call timeline* (ABSOLUTE),
 * including natural overlaps, pauses, and correct ordering.
 *
 * Channels:
 * - Left  = inbound (caller -> VoiceBridge)
 * - Right = outbound (AI -> VoiceBridge)
 *
 * Key behavior:
 * -------------
 * 1) During the call we do NOT force-sync channels.
 *    We store independent timestamped PCM chunks for each side.
 *
 * 2) We flush to disk in batches for performance / memory control:
 *    - Combined batching: when (inboundChunks + outboundChunks) >= BATCH_SIZE_CHUNKS,
 *      we flush the safe portion of timeline to WAV and clear flushed chunks.
 *
 * 3) Safe watermark rules:
 *    - If BOTH sides have data & are reasonably close in time:
 *         safeEndNs = min(latestInboundTs, latestOutboundTs)
 *      (classic safe watermark)
 *
 *    - If ONE side is missing OR the other side is lagging too long (silence/monologue):
 *         safeEndNs = latestPresentSideTs - safetyGapNs
 *      (your requested behavior)
 *
 *    This prevents unlimited memory growth during long monologues while still
 *    protecting ordering correctness from bursty outbound audio.
 *
 * 4) At stopAndClose():
 *    - We flush any remaining audio to WAV (final tail), preserving absolute time.
 *
 * Concurrency / blocking:
 * -----------------------
 * - The lock here is PER-CALL instance, so different calls do NOT block each other.
 * - We also do NOT perform disk I/O under the lock.
 *   We prepare the batch under lock, release the lock, then write to file.
 *
 * Threading:
 * ----------
 * - Per-call instance (owned by CallSession).
 * - writeInbound() and writeOutbound() may be called from different threads.
 * - All list mutations + flush preparation guarded by lock.
 */
public final class CallRecordingManager {

    private static final Logger log = LoggerFactory.getLogger(CallRecordingManager.class);

    /**
     * Global deep-log switch for this file.
     * - If false: all INFO/DEBUG/WARN/TRACE logs in this class are suppressed.
     * - ERROR logs are NEVER gated and are always printed.
     */
    private static final boolean DEEP_LOGS = false;

    /**
     * RTP-specific deep logs for this file.
     * This class does not log raw RTP packets, but the flag is present
     * for consistency with other RTP-heavy classes.
     */
    private static final boolean RTP_DEEP_LOGS = false;

    // ---- Chunk / batch constants ----
    private static final int CHUNK_MS = 20;              // pipeline chunking
    private static final int BATCH_SIZE_CHUNKS = 1000;  // you requested (~20s @20ms)
    private static final int LATE_CHUNK_WARN_MS = 200;  // warn if chunk arrives before already-flushed timeline

    // ---- Silence-timeout watermark (your requested scheme) ----
    /** If one side is missing/lagging for >= this, we allow flushing using latestPresentSideTs - safetyGapNs. */
    private static final int SILENCE_TIMEOUT_MS = 2000; // 2s window for monologues / bot silence
    /** Safety gap to leave unflushed behind the latest present side to avoid tiny race/backfill. */
    private static final int SAFETY_GAP_MS = 200;       // 200ms guard tail

    /** Safety cap for extremely long calls so we don't allocate insane arrays. */
    private static final int MAX_TOTAL_SAMPLES_CAP = 60_000_000; // ~2.08h @ 8kHz

    private final StereoWavFileWriter writer;

    // NEW: dedicated IO thread per call
    private final java.util.concurrent.ExecutorService ioExecutor =
            java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "rec-io-" + System.identityHashCode(CallRecordingManager.this));
                t.setDaemon(true);
                return t;
            });
    
    private volatile boolean running = false;
    private final Object lock = new Object();

    // Recording finalization barrier (so completion can safely read WAV)
    private final java.util.concurrent.atomic.AtomicBoolean closing = new java.util.concurrent.atomic.AtomicBoolean(false);
    private final java.util.concurrent.atomic.AtomicBoolean closed  = new java.util.concurrent.atomic.AtomicBoolean(false);

    public boolean isClosing() { return closing.get(); }
    public boolean isClosed()  { return closed.get(); }
    public String getPath()    { return path; }

    
    private final String path;
    private final int sampleRateHz;

    // Timestamped chunk storage (per side)
    private final List<TimedChunk> inboundChunks  = new ArrayList<>();
    private final List<TimedChunk> outboundChunks = new ArrayList<>();

    // Timeline anchors
    private long t0Ns = Long.MIN_VALUE;   // first-ever timestamp (absolute reference)
    private int  flushedEndSample = 0;   // global sample index already written to WAV

    // Counters / stats
    private final AtomicLong inboundBytesTotal    = new AtomicLong(0);
    private final AtomicLong outboundBytesTotal   = new AtomicLong(0);
    private final AtomicLong stereoFramesWritten  = new AtomicLong(0);
    private final AtomicLong inboundChunksTotal   = new AtomicLong(0);
    private final AtomicLong outboundChunksTotal  = new AtomicLong(0);
    private final AtomicLong flushCount           = new AtomicLong(0);

    public CallRecordingManager(String fullPath, int sampleRateHz) throws Exception {
        this.writer = new StereoWavFileWriter(fullPath, sampleRateHz);
        this.path = fullPath;
        this.sampleRateHz = sampleRateHz;

        if (DEEP_LOGS) {
            log.info("RECORD recording_manager_created path={} rateHz={} batchChunks={} (~{}ms) silenceTimeoutMs={} safetyGapMs={}",
                    fullPath, sampleRateHz, BATCH_SIZE_CHUNKS, (long) BATCH_SIZE_CHUNKS * CHUNK_MS,
                    SILENCE_TIMEOUT_MS, SAFETY_GAP_MS);
        }
    }

    public void start() {
        running = true;
        if (DEEP_LOGS) {
            log.info("RECORD recording_manager_started path={} rateHz={}", path, sampleRateHz);
        }
    }

    // ---------------------------------------------------------------------
    // Async wrappers (recommended from hot path)
    // ---------------------------------------------------------------------

    public void writeInboundAsync(byte[] pcm16) {
        if (!running || pcm16 == null || pcm16.length == 0) return;
        final byte[] copy = copyOf(pcm16);
        ioExecutor.execute(() -> {
            try {
                writeInbound(copy);  // existing sync logic (timestamping, batching, flush)
            } catch (Exception e) {
                if (RTP_DEEP_LOGS) {
                    log.warn("RECORD inbound_async_error path={} bytes={} msg={}",
                            path, copy.length, e.toString(), e);
                }
            }
        });
    }

    public void writeOutboundAsync(byte[] pcm16) {
        if (!running || pcm16 == null || pcm16.length == 0) return;
        final byte[] copy = copyOf(pcm16);
        ioExecutor.execute(() -> {
            try {
                writeOutbound(copy); // existing sync logic
            } catch (Exception e) {
                if (RTP_DEEP_LOGS) {
                    log.warn("RECORD outbound_async_error path={} bytes={} msg={}",
                            path, copy.length, e.toString(), e);
                }
            }
        });
    }

    // inbound => LEFT
    private void writeInbound(byte[] pcm16) throws Exception {
        if (!running || pcm16 == null || pcm16.length == 0) return;
        if ((pcm16.length & 1) != 0) pcm16 = trimOddByte(pcm16);

        final long tsNs = System.nanoTime();
        final byte[] copy = copyOf(pcm16);

        BatchToWrite batch = null;

        synchronized (lock) {
            if (t0Ns == Long.MIN_VALUE) {
                t0Ns = tsNs;
                if (RTP_DEEP_LOGS) {
                    log.info("RECORD t0_set path={} t0Ns={}", path, t0Ns);
                }
            }

            warnIfLate(tsNs, "inbound");

            inboundChunks.add(new TimedChunk(tsNs, copy));
            long totBytes  = inboundBytesTotal.addAndGet(copy.length);
            long totChunks = inboundChunksTotal.incrementAndGet();

            if (RTP_DEEP_LOGS && log.isTraceEnabled()) {
                log.trace("RECORD inbound_write path={} tsNs={} addBytes={} totalInboundBytes={} inboundChunks={} outboundChunks={} flushedEndSample={}",
                        path, tsNs, copy.length, totBytes, totChunks, outboundChunksTotal.get(), flushedEndSample);
            }

            batch = maybePrepareFlushLocked("threshold_check_after_inbound", false);
        }

        // Write outside lock
        if (batch != null) {
            writeBatchOutsideLock(batch);
        }
    }

    // outbound => RIGHT
    private void writeOutbound(byte[] pcm16) throws Exception {
        if (!running || pcm16 == null || pcm16.length == 0) return;
        if ((pcm16.length & 1) != 0) pcm16 = trimOddByte(pcm16);

        final long tsNs = System.nanoTime();
        final byte[] copy = copyOf(pcm16);

        BatchToWrite batch = null;

        synchronized (lock) {
            if (t0Ns == Long.MIN_VALUE) {
                t0Ns = tsNs;
                if (RTP_DEEP_LOGS) {
                    log.info("RECORD t0_set path={} t0Ns={}", path, t0Ns);
                }
            }

            warnIfLate(tsNs, "outbound");

            outboundChunks.add(new TimedChunk(tsNs, copy));
            long totBytes  = outboundBytesTotal.addAndGet(copy.length);
            long totChunks = outboundChunksTotal.incrementAndGet();

            if (RTP_DEEP_LOGS && log.isTraceEnabled()) {
                log.trace("RECORD outbound_write path={} tsNs={} addBytes={} totalOutboundBytes={} inboundChunks={} outboundChunks={} flushedEndSample={}",
                        path, tsNs, copy.length, totBytes, inboundChunksTotal.get(), totChunks, flushedEndSample);
            }

            batch = maybePrepareFlushLocked("threshold_check_after_outbound", false);
        }

        // Write outside lock
        if (batch != null) {
            writeBatchOutsideLock(batch);
        }
    }

    /**
     * stopAndClose():
     * - Freeze recording.
     * - Prepare final batch under lock.
     * - Write outside lock.
     * - Close writer.
     */
    public void stopAndClose() {
    	
    	// Idempotent close: only the first caller performs the close work.
    	if (!closing.compareAndSet(false, true)) {
    	    // Someone else is already closing; block until closed so caller gets the barrier.
    	    long t0 = System.nanoTime();
    	    while (!closed.get()) {
    	        try { Thread.sleep(20); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
    	        if (java.util.concurrent.TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - t0) > 6) break;
    	    }
    	    if (DEEP_LOGS) {
    	        log.info("RECORD stopAndClose barrier_return path={} waitedMs={} closedNow={}",
    	            path,
    	            java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0),
    	            closed.get());
    	    }
    	    return;
    	}
        running = false;

        // 1) Stop accepting new tasks and drain existing queued tasks
        ioExecutor.shutdown();
        try {
            // wait a bit for queued writeInbound/writeOutbound tasks to run
        	boolean ok = ioExecutor.awaitTermination(5, TimeUnit.SECONDS);
        	if (!ok) {
        	    // Force stop to avoid hanging forever, but log clearly.
        	    log.warn("RECORD ioExecutor not terminated in time; forcing shutdownNow path={}", path);
        	    ioExecutor.shutdownNow();
        	    // Wait a bit more after shutdownNow
        	    ioExecutor.awaitTermination(2, TimeUnit.SECONDS);
        	}
        } catch (InterruptedException ie) {
            ioExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        BatchToWrite finalBatch = null;

        // 2) NOW do final flush under lock (after async queue is drained)
        synchronized (lock) {
            try {
                finalBatch = prepareFlushLocked("final_flush", true);
            } catch (Exception e) {
                log.error("RECORD prepare_final_flush_error path={} msg={}", path, e.getMessage(), e);
            }
        }

        // 3) write + close
        try {
            if (finalBatch != null) {
                writeBatchOutsideLock(finalBatch);
            }
            writer.close();
        } catch (Exception e) {
            log.error("RECORD recording_close_error path={} msg={}", path, e.getMessage(), e);
            try { writer.close(); } catch (Exception ignore) {}
        } finally {
            synchronized (lock) {
                inboundChunks.clear();
                outboundChunks.clear();
            }
            
            closed.set(true);

            if (DEEP_LOGS) {
                log.info("RECORD stopAndClose finished path={} closed=true inboundBytes={} outboundBytes={} flushCount={} stereoFramesWritten={}",
                    path,
                    inboundBytesTotal.get(),
                    outboundBytesTotal.get(),
                    flushCount.get(),
                    stereoFramesWritten.get());
            }
        }
    }

    
    /**
     * Strong barrier variant used by completion pipeline:
     * when this returns, WAV is finalized and safe to read.
     */
    public void stopAndCloseBlocking() {
        stopAndClose();
    }

    
    // ---------------------------------------------------------------------
    // Flush preparation (under lock) + write (outside lock)
    // ---------------------------------------------------------------------

    /**
     * Decide if we should flush now based on combined chunk threshold.
     * Must be called under lock.
     */
    private BatchToWrite maybePrepareFlushLocked(String reason, boolean finalFlush) throws Exception {
        int totalChunksNow = inboundChunks.size() + outboundChunks.size();
        if (!finalFlush && totalChunksNow < BATCH_SIZE_CHUNKS) return null;
        return prepareFlushLocked(reason, finalFlush);
    }

    /**
     * Prepare a SAFE batch to write.
     *
     * SAFE watermark rules:
     * - Final flush:
     *     safeEndNs = max(latestInboundTs, latestOutboundTs)
     *
     * - Non-final flush:
     *     1) If both sides present and skew < SILENCE_TIMEOUT_MS:
     *           safeEndNs = min(latestInTs, latestOutTs)
     *
     *     2) Else (one side missing OR skew >= timeout):
     *           safeEndNs = latestPresentSideTs - safetyGapNs
     *        (your requested policy)
     *
     * Must be called under lock.
     */
    private BatchToWrite prepareFlushLocked(String reason, boolean finalFlush) throws Exception {
        if (t0Ns == Long.MIN_VALUE) {
            if (RTP_DEEP_LOGS) {
                log.warn("RECORD flush_skip_no_t0 path={} reason={}", path, reason);
            }
            return null;
        }

        if (inboundChunks.isEmpty() && outboundChunks.isEmpty()) {
            if (RTP_DEEP_LOGS) {
                log.debug("RECORD flush_skip_empty path={} reason={}", path, reason);
            }
            return null;
        }

        // Defensive time order
        inboundChunks.sort(Comparator.comparingLong(c -> c.tsNs));
        outboundChunks.sort(Comparator.comparingLong(c -> c.tsNs));

        long latestInTs  = inboundChunks.isEmpty()  ? Long.MIN_VALUE : inboundChunks.get(inboundChunks.size() - 1).tsNs;
        long latestOutTs = outboundChunks.isEmpty() ? Long.MIN_VALUE : outboundChunks.get(outboundChunks.size() - 1).tsNs;

        long safeEndNs;
        if (finalFlush) {
            safeEndNs = Math.max(latestInTs, latestOutTs);
        } else {
            boolean inPresent  = latestInTs  != Long.MIN_VALUE;
            boolean outPresent = latestOutTs != Long.MIN_VALUE;

            if (inPresent && outPresent) {
                long skewNs = Math.abs(latestInTs - latestOutTs);
                long skewMs = skewNs / 1_000_000L;

                if (skewMs < SILENCE_TIMEOUT_MS) {
                    // normal safe watermark
                    safeEndNs = Math.min(latestInTs, latestOutTs);
                } else {
                    // skew too large -> silence-timeout watermark (latest present minus safety gap)
                    long latestPresent = Math.max(latestInTs, latestOutTs);
                    safeEndNs = latestPresent - SAFETY_GAP_MS * 1_000_000L;

                    if (RTP_DEEP_LOGS) {
                        log.info("RECORD watermark_timeout path={} reason={} skewMs={} safeEndNs=latestPresent-safetyGap latestPresentNs={} safeEndNs={}",
                                path, reason, skewMs, latestPresent, safeEndNs);
                    }
                }
            } else {
                // one side missing -> silence-timeout watermark (latest present minus safety gap)
                long latestPresent = Math.max(latestInTs, latestOutTs);
                safeEndNs = latestPresent - SAFETY_GAP_MS * 1_000_000L;

                if (RTP_DEEP_LOGS) {
                    log.info("RECORD watermark_one_side_missing path={} reason={} latestPresentNs={} safeEndNs={}",
                            path, reason, latestPresent, safeEndNs);
                }
            }

            if (safeEndNs < t0Ns) safeEndNs = t0Ns; // safety clamp
        }

        int safeEndSampleCutoff = nsToSamples(safeEndNs - t0Ns);

        // Take & remove chunks fully inside cutoff
        List<TimedChunk> inBatch  = takeBatchUpToEndSample(inboundChunks, safeEndSampleCutoff);
        List<TimedChunk> outBatch = takeBatchUpToEndSample(outboundChunks, safeEndSampleCutoff);

        int batchEndSample = Math.max(
                computeMaxEndSample(inBatch, t0Ns),
                computeMaxEndSample(outBatch, t0Ns)
        );

        if (batchEndSample <= flushedEndSample) {
            if (RTP_DEEP_LOGS) {
                log.debug("RECORD flush_skip_no_progress path={} reason={} safeEndSampleCutoff={} flushedEndSample={}",
                        path, reason, safeEndSampleCutoff, flushedEndSample);
            }
            return null;
        }

        if (batchEndSample > MAX_TOTAL_SAMPLES_CAP) {
            if (RTP_DEEP_LOGS) {
                log.warn("RECORD flush_cap_applied path={} batchEndSample={} capSamples={}",
                        path, batchEndSample, MAX_TOTAL_SAMPLES_CAP);
            }
            batchEndSample = MAX_TOTAL_SAMPLES_CAP;
        }

        int batchLenSamples = batchEndSample - flushedEndSample;
        if (batchLenSamples <= 0) return null;

        long batchStartMs = (long) flushedEndSample * 1000L / sampleRateHz;
        long batchEndMs   = (long) batchEndSample * 1000L / sampleRateHz;

        long flushNo = flushCount.incrementAndGet();

        if (RTP_DEEP_LOGS) {
            log.info("RECORD flush_plan path={} flush#={} reason={} final={} batchStartSample={} batchEndSample={} batchLenSamples={} (~{}ms -> ~{}ms) inBatchChunks={} outBatchChunks={} remainingInboundChunks={} remainingOutboundChunks={}",
                    path, flushNo, reason, finalFlush, flushedEndSample, batchEndSample, batchLenSamples,
                    batchStartMs, batchEndMs, inBatch.size(), outBatch.size(),
                    inboundChunks.size(), outboundChunks.size());
        }

        // Allocate batch window
        short[] leftBatch  = new short[batchLenSamples];
        short[] rightBatch = new short[batchLenSamples];

        placeOnBatch(inBatch,  t0Ns, leftBatch,  flushedEndSample, "LEFT(inbound)");
        placeOnBatch(outBatch, t0Ns, rightBatch, flushedEndSample, "RIGHT(outbound)");

        // Advance flushed pointer NOW (under lock), so late chunks can be detected.
        int newFlushedEndSample = batchEndSample;
        flushedEndSample = batchEndSample;
        stereoFramesWritten.set(flushedEndSample);

        return new BatchToWrite(flushNo, reason, finalFlush, batchStartMs, batchEndMs,
                shortsToLeBytes(leftBatch), shortsToLeBytes(rightBatch), newFlushedEndSample);
    }

    /**
     * Actually write batch bytes to WAV (outside lock).
     */
    private void writeBatchOutsideLock(BatchToWrite b) throws Exception {
        writer.writeStereo(b.leftBytes, b.rightBytes);

        if (RTP_DEEP_LOGS) {
            log.info("RECORD flush_done path={} flush#={} reason={} final={} newFlushedEndSample={} (~{}ms) wroteLeftBytes={} wroteRightBytes={}",
                    path, b.flushNo, b.reason, b.finalFlush, b.newFlushedEndSample,
                    b.batchEndMs, b.leftBytes.length, b.rightBytes.length);
        }
    }

    // ---------------------------------------------------------------------
    // Timeline / batch helpers
    // ---------------------------------------------------------------------

    /**
     * Take and REMOVE from source all chunks whose END sample <= cutoff.
     * Source lists must be sorted by tsNs.
     */
    private List<TimedChunk> takeBatchUpToEndSample(List<TimedChunk> src, int cutoffEndSample) {
        List<TimedChunk> out = new ArrayList<>();
        Iterator<TimedChunk> it = src.iterator();
        while (it.hasNext()) {
            TimedChunk c = it.next();
            int startSample = nsToSamples(c.tsNs - t0Ns);
            int endSample   = startSample + c.samples;
            if (endSample <= cutoffEndSample) {
                out.add(c);
                it.remove();
            } else {
                break; // later ones will also exceed cutoff
            }
        }
        return out;
    }

    private int computeMaxEndSample(List<TimedChunk> chunks, long t0Ns) {
        int maxEnd = 0;
        for (TimedChunk c : chunks) {
            int startSample = nsToSamples(c.tsNs - t0Ns);
            int endSample   = startSample + c.samples;
            if (endSample > maxEnd) maxEnd = endSample;
        }
        return maxEnd;
    }

    /**
     * Place absolute-timeline chunks onto a batch window.
     */
    private void placeOnBatch(List<TimedChunk> chunks,
                              long t0Ns,
                              short[] batchTimeline,
                              int batchWindowStartSample,
                              String label) {

        int placedChunks = 0;
        int clippedSamples = 0;
        int skippedChunks = 0;

        for (TimedChunk c : chunks) {
            int absStartSample = nsToSamples(c.tsNs - t0Ns);
            int relStartSample = absStartSample - batchWindowStartSample;
            if (relStartSample < 0) relStartSample = 0;

            int maxWrite = Math.min(c.samples, batchTimeline.length - relStartSample);
            if (maxWrite <= 0) {
                skippedChunks++;
                continue;
            }

            byte[] b = c.pcm16;
            for (int i = 0; i < maxWrite; i++) {
                short s = leBytesToShort(b, i * 2);

                int idx = relStartSample + i;
                short prev = batchTimeline[idx];

                int mixed = prev + s;
                if (mixed > Short.MAX_VALUE) { mixed = Short.MAX_VALUE; clippedSamples++; }
                if (mixed < Short.MIN_VALUE) { mixed = Short.MIN_VALUE; clippedSamples++; }

                batchTimeline[idx] = (short) mixed;
            }

            placedChunks++;
        }

        if (DEEP_LOGS) {
            log.info("RECORD place_done path={} side={} chunksPlaced={} chunksSkipped={} clippedSamples={} batchSamples={}",
                    path, label, placedChunks, skippedChunks, clippedSamples, batchTimeline.length);
        }
    }

    /**
     * Warn if a chunk arrives earlier than already flushed timeline.
     * We cannot insert audio into already-written WAV.
     */
    private void warnIfLate(long tsNs, String side) {
        if (t0Ns == Long.MIN_VALUE) return;

        int absStartSample = nsToSamples(tsNs - t0Ns);
        if (absStartSample >= flushedEndSample) return;

        long lateSamples = (long) flushedEndSample - absStartSample;
        long lateMs = lateSamples * 1000L / sampleRateHz;

        if (lateMs >= LATE_CHUNK_WARN_MS) {
            if (DEEP_LOGS) {
                log.warn("RECORD late_chunk_detected path={} side={} lateMs={} absStartSample={} flushedEndSample={} (cannot backfill)",
                        path, side, lateMs, absStartSample, flushedEndSample);
            }
        }
    }

    /**
     * Convert nanosecond delta to sample offset using integer math.
     */
    private int nsToSamples(long deltaNs) {
        if (deltaNs <= 0) return 0;
        long samples = (deltaNs * (long) sampleRateHz) / 1_000_000_000L;
        if (samples > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) samples;
    }

    // ---------------------------------------------------------------------
    // Data types / helpers
    // ---------------------------------------------------------------------

    private static final class TimedChunk {
        final long tsNs;
        final byte[] pcm16;
        final int samples;

        TimedChunk(long tsNs, byte[] pcm16) {
            this.tsNs = tsNs;
            this.pcm16 = pcm16;
            this.samples = pcm16.length / 2;
        }
    }

    private static final class BatchToWrite {
        final long flushNo;
        final String reason;
        final boolean finalFlush;
        final long batchStartMs;
        final long batchEndMs;
        final byte[] leftBytes;
        final byte[] rightBytes;
        final int newFlushedEndSample;

        BatchToWrite(long flushNo, String reason, boolean finalFlush,
                     long batchStartMs, long batchEndMs,
                     byte[] leftBytes, byte[] rightBytes,
                     int newFlushedEndSample) {
            this.flushNo = flushNo;
            this.reason = reason;
            this.finalFlush = finalFlush;
            this.batchStartMs = batchStartMs;
            this.batchEndMs = batchEndMs;
            this.leftBytes = leftBytes;
            this.rightBytes = rightBytes;
            this.newFlushedEndSample = newFlushedEndSample;
        }
    }

    private static byte[] trimOddByte(byte[] src) {
        int len = src.length - 1;
        byte[] out = new byte[len];
        System.arraycopy(src, 0, out, 0, len);
        return out;
    }

    private static byte[] copyOf(byte[] src) {
        byte[] out = new byte[src.length];
        System.arraycopy(src, 0, out, 0, src.length);
        return out;
    }

    private static short leBytesToShort(byte[] src, int offset) {
        int lo = src[offset] & 0xFF;
        int hi = src[offset + 1];
        return (short) ((hi << 8) | lo);
    }

    private static byte[] shortsToLeBytes(short[] samples) {
        byte[] out = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            short s = samples[i];
            out[i * 2]     = (byte) (s & 0xFF);
            out[i * 2 + 1] = (byte) ((s >>> 8) & 0xFF);
        }
        return out;
    }
}
