/*
 * Auto-formatted + DEEP LOGS: src/main/java/com/mylinehub/voicebridge/queue/OutboundQueue.java
 *
 * ARCHITECTURE (No-cutover version / same behavior as old playout path):
 * - Producers enqueue PCM chunks (variable size).
 * - Queue owns: carry merge + fixed framing (e.g. 20ms) + codec.encodePcmToPayload(frame) + stores EncodedFrame.
 * - Playout polls ONLY encoded frames via pollEncoded(). (No PCM poll API.)
 *
 * DROP POLICY:
 * - The queue NEVER drops frames because of "gap" or "cutover".
 * - Frames are dropped ONLY when:
 *     1) clear() is called (explicit), OR
 *     2) overflow policy triggers (DROP_OLDEST / DROP_NEW).
 *
 * MEMORY IMPROVEMENTS:
 * - By default, queue DOES NOT retain pcmFrame inside EncodedFrame.
 * - Enable record tap per call via setRecordTapEnabled(true).
 *
 * CONCURRENCY:
 * - enqueuePcm() and clear() are guarded by a per-instance lock
 *   so carryPcm and framing are safe even if multiple producer threads enqueue.
 */
package com.mylinehub.voicebridge.queue;

import com.mylinehub.voicebridge.audio.AudioCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class OutboundQueue {

  public enum OverflowPolicy {
    DROP_OLDEST, DROP_NEW
  }

  // -------------------------------------------------------------------------
  // Logging switches
  // -------------------------------------------------------------------------
  private static final Logger log = LoggerFactory.getLogger(OutboundQueue.class);
  private static final boolean DEEP_LOGS = true;
  private static final boolean RTP_DEEP_LOGS = false;

  // -------------------------------------------------------------------------
  // Data model requirement: keep PcmChunk (DO NOT REMOVE)
  // -------------------------------------------------------------------------
  public static final class PcmChunk {
    private final String id;
    private final byte[] pcm;
    private final int durationMs;
    private final String mark;
    private final int sampleRateHz;

    public PcmChunk(String id, byte[] pcm, int durationMs, String mark, int sampleRateHz) {
      this.id = id;
      this.pcm = pcm;
      this.durationMs = durationMs;
      this.mark = mark;
      this.sampleRateHz = sampleRateHz;
    }

    public String getId() { return id; }
    public byte[] getPcm() { return pcm; }
    public int getDurationMs() { return durationMs; }
    public String getMark() { return mark; }
    public int getSampleRateHz() { return sampleRateHz; }
  }

  // -------------------------------------------------------------------------
  // Encoded frame container (exactly one fixed frame: e.g. 20ms)
  // -------------------------------------------------------------------------
  public static final class EncodedFrame {
    private final String id;
    private final String mark;
    private final int sampleRateHz;    // codec/sample rate
    private final int frameMs;         // fixed (e.g. 20)
    private final int frameSamples;    // fixed (rate * frameMs / 1000)
    private final byte[] pcmFrame;     // OPTIONAL: may be null if record tap disabled
    private final byte[] payload;      // encoded bytes for this frame

    public EncodedFrame(String id,
                        String mark,
                        int sampleRateHz,
                        int frameMs,
                        int frameSamples,
                        byte[] pcmFrame,
                        byte[] payload) {
      this.id = id;
      this.mark = mark;
      this.sampleRateHz = sampleRateHz;
      this.frameMs = frameMs;
      this.frameSamples = frameSamples;
      this.pcmFrame = pcmFrame;
      this.payload = payload;
    }

    public String getId() { return id; }
    public String getMark() { return mark; }
    public int getSampleRateHz() { return sampleRateHz; }
    public int getFrameMs() { return frameMs; }
    public int getFrameSamples() { return frameSamples; }
    public byte[] getPcmFrame() { return pcmFrame; }
    public byte[] getPayload() { return payload; }
  }

  // -------------------------------------------------------------------------
  // Queue internals
  // -------------------------------------------------------------------------
  private final ConcurrentLinkedQueue<EncodedFrame> qEnc = new ConcurrentLinkedQueue<>();
  private final AtomicLong depthMs = new AtomicLong(0);

  private final long maxMs;
  private final OverflowPolicy policy;

  private final AudioCodec codec;
  private final int frameMs;
  private final int codecRateHz;
  private final int samplesPerFrame;
  private final int frameBytes;

  // Carry (PCM) to avoid padding artifacts across producer chunks
  private byte[] carryPcm = new byte[0];

  // Per-instance enqueue lock (per-call queue)
  private final Object enqLock = new Object();

  // If true, retain pcmFrame in EncodedFrame for outbound recording tap
  private volatile boolean recordTapEnabled = false;

  // Counters/diagnostics
  private final AtomicLong enqPcmChunks = new AtomicLong(0);
  private final AtomicLong enqFrames = new AtomicLong(0);
  private final AtomicLong pollFrames = new AtomicLong(0);
  private final AtomicLong dropNew = new AtomicLong(0);
  private final AtomicLong dropOld = new AtomicLong(0);

  private final AtomicLong lastEnqueueTimeMs = new AtomicLong(0);

  private static final long INFO_SUMMARY_EVERY_ENQ = 50;

  public OutboundQueue(long maxMs,
                       OverflowPolicy policy,
                       AudioCodec codec,
                       int frameMs) {

    if (codec == null) throw new IllegalArgumentException("codec is null");
    this.maxMs = maxMs;
    this.policy = policy;
    this.codec = codec;

    this.frameMs = Math.max(10, frameMs);
    this.codecRateHz = codec.sampleRate();
    this.samplesPerFrame = (codecRateHz * this.frameMs) / 1000;
    this.frameBytes = this.samplesPerFrame * 2;

    if (DEEP_LOGS) {
      log.info("OQ-INIT outbound_queue_init maxMs={} policy={} frameMs={} codecRateHz={} samplesPerFrame={} frameBytes={} recordTapEnabled=false (no cutover)",
          this.maxMs, this.policy, this.frameMs, this.codecRateHz, this.samplesPerFrame, this.frameBytes);
    }
  }

  // Enable/disable retaining PCM frames for recording tap (per call)
  public void setRecordTapEnabled(boolean enabled) {
    this.recordTapEnabled = enabled;
    if (DEEP_LOGS) {
      log.debug("OQ-STATE record_tap_enabled={} codecRateHz={} frameMs={}", enabled, codecRateHz, frameMs);
    }
  }

  public boolean isRecordTapEnabled() {
    return recordTapEnabled;
  }

  // =========================================================================
  // Public API: enqueue PCM chunk (queue does carry + framing + encode)
  // =========================================================================
  public boolean enqueuePcm(String id, byte[] pcm16, int sampleRateHz, String mark) {
    if (pcm16 == null || pcm16.length == 0 || sampleRateHz <= 0) {
      if (RTP_DEEP_LOGS) {
        log.debug("OQ-ENQ enqueuePcm_invalid id={} pcmBytes={} rateHz={} mark={}",
            id, (pcm16 == null ? 0 : pcm16.length), sampleRateHz, mark);
      }
      return false;
    }

    synchronized (enqLock) {

      // NOTE: No cutover logic here. Old behavior: never drop due to time gaps.
      // If you need barge-in, call clear() explicitly from your session logic.

      enqPcmChunks.incrementAndGet();

      // Carry merge (no padding)
      if (carryPcm.length > 0) {
        byte[] merged = new byte[carryPcm.length + pcm16.length];
        System.arraycopy(carryPcm, 0, merged, 0, carryPcm.length);
        System.arraycopy(pcm16, 0, merged, carryPcm.length, pcm16.length);
        pcm16 = merged;
        carryPcm = new byte[0];
      }

      // Frame + encode into qEnc
      boolean ok = frameAndEncodeIntoQueue(id, pcm16, mark);

      if (ok) {
        lastEnqueueTimeMs.set(System.currentTimeMillis());
      }

      long idx = enqPcmChunks.get();
      if (RTP_DEEP_LOGS && (idx % INFO_SUMMARY_EVERY_ENQ) == 0) {
        log.info("OQ-STATE enqueue_summary pcmChunks={} frames={} polledFrames={} depthMs={} maxMs={} dropsOld={} dropsNew={} qEncSizeApprox={} recordTapEnabled={}",
            enqPcmChunks.get(), enqFrames.get(), pollFrames.get(),
            depthMs.get(), maxMs, dropOld.get(), dropNew.get(),
            qEnc.size(), recordTapEnabled);
      }

      return ok;
    }
  }

  // =========================================================================
  // Public API: poll encoded frame (only API playout uses)
  // =========================================================================
  public EncodedFrame pollEncoded() {
    EncodedFrame f = qEnc.poll();
    if (f != null) {
      depthMs.addAndGet(-f.getFrameMs());
      pollFrames.incrementAndGet();
      return f;
    }
    return null;
  }

  public long depthMs() {
    return depthMs.get();
  }

  public void clear() {
    synchronized (enqLock) {
      qEnc.clear();
      depthMs.set(0);
      carryPcm = new byte[0];
      lastEnqueueTimeMs.set(0);
      if (RTP_DEEP_LOGS) {
        log.debug("OQ-CLEAR clear_ok depthMs=0 qEncSizeApprox=0");
      }
    }
  }

  // =========================================================================
  // Internal: frame+encode
  // =========================================================================
  private boolean frameAndEncodeIntoQueue(String id, byte[] pcmAtCodecRate, String mark) {
    if (pcmAtCodecRate == null || pcmAtCodecRate.length == 0) return true;

    int offset = 0;
    int framesMade = 0;

    while (offset + frameBytes <= pcmAtCodecRate.length) {
      // PCM frame (copy)
      byte[] framePcm = new byte[frameBytes];
      System.arraycopy(pcmAtCodecRate, offset, framePcm, 0, frameBytes);
      offset += frameBytes;

      // Encode once here (moves work out of playout thread)
      byte[] payload = codec.encodePcmToPayload(framePcm);
      if (payload == null || payload.length == 0) {
        log.warn("OQ-CODEC encode_empty_drop id={} frameIndex={} pcmBytes={} pt=?",
            id, framesMade, framePcm.length);
        continue;
      }

      // Memory improvement: keep pcmFrame only if recording tap is enabled
      byte[] pcmToStore = recordTapEnabled ? framePcm : null;

      EncodedFrame f = new EncodedFrame(
          id,
          mark,
          codecRateHz,
          frameMs,
          samplesPerFrame,
          pcmToStore,
          payload
      );

      if (!enqueueFrameWithOverflow(f)) {
        // DROP_NEW policy rejected
        return false;
      }

      framesMade++;
    }

    // remainder becomes carry (no padding)
    int rem = pcmAtCodecRate.length - offset;
    if (rem > 0) {
      carryPcm = new byte[rem];
      System.arraycopy(pcmAtCodecRate, offset, carryPcm, 0, rem);
    } else {
      carryPcm = new byte[0];
    }

    return true;
  }

  private boolean enqueueFrameWithOverflow(EncodedFrame f) {
    long after = depthMs.addAndGet(f.getFrameMs());

    if (after > maxMs) {
      if (policy == OverflowPolicy.DROP_OLDEST) {
        while (depthMs.get() > maxMs && !qEnc.isEmpty()) {
          EncodedFrame d = qEnc.poll();
          if (d == null) break;
          depthMs.addAndGet(-d.getFrameMs());
          dropOld.incrementAndGet();
        }
      } else {
        depthMs.addAndGet(-f.getFrameMs());
        dropNew.incrementAndGet();
        return false;
      }
    }

    qEnc.add(f);
    enqFrames.incrementAndGet();
    return true;
  }

  public String debugSnapshot() {
    return "OutboundQueue{depthMs=" + depthMs.get() +
        ", maxMs=" + maxMs +
        ", policy=" + policy +
        ", codecRateHz=" + codecRateHz +
        ", frameMs=" + frameMs +
        ", enqPcmChunks=" + enqPcmChunks.get() +
        ", enqFrames=" + enqFrames.get() +
        ", pollFrames=" + pollFrames.get() +
        ", dropOld=" + dropOld.get() +
        ", dropNew=" + dropNew.get() +
        ", lastEnqMs=" + lastEnqueueTimeMs.get() +
        ", qEncSizeApprox=" + qEnc.size() +
        ", recordTapEnabled=" + recordTapEnabled +
        "}";
  }
}
