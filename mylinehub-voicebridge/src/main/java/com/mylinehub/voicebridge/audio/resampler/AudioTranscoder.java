/*
 * File: src/main/java/com/mylinehub/voicebridge/audio/resampler/AudioTranscoder.java
 *
 * Purpose:
 *  - Single resampling facade used everywhere in the project.
 *  - NO custom interpolation/resample logic lives here anymore.
 *  - Delegates all PCM16LE mono resampling to FFmpeg/libswresample via {@link FfmpegResampler}.
 *
 * Concurrency / multi-call safety:
 *  - FFmpeg SwrContext is NOT thread-safe.
 *  - To support many concurrent calls, each thread gets its own SwrContext per (srcHz,dstHz).
 *  - We store resamplers in a ThreadLocal Map keyed by (srcHz,dstHz).
 *
 * Lifetime:
 *  - Resamplers hold native memory and are kept for the life of the thread.
 *  - This is OK because VoiceBridge uses bounded thread pools.
 *  - If you ever create/destroy threads dynamically, call {@link #closeThreadLocal()} on thread exit.
 *
 * Logging switches (per-file, non-error only):
 *  - RTP_DEEP_LOGS:
 *      Master switch for INFO/DEBUG/TRACE logs in this class.
 *      Set to true only while debugging resampler usage / cache behavior.
 *  - RTP_RTP_DEEP_LOGS:
 *      Reserved for RTP-packet-heavy logs (not used here yet, kept for consistency).
 *
 * NOTE:
 *  - All ERROR logs MUST stay always-on and are NOT gated by these flags.
 */

package com.mylinehub.voicebridge.audio.resampler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class AudioTranscoder {

    private static final Logger log = LoggerFactory.getLogger(AudioTranscoder.class);

    /**
     * Master switch for non-error logs (INFO/DEBUG/TRACE) in this file.
     */
    private static final boolean DEEP_LOGS = false;

    /**
     * Secondary switch for very chatty RTP-related logs.
     * Not used in this class currently, but declared for consistency.
     */
    private static final boolean RTP_DEEP_LOGS = false;

    private AudioTranscoder() {}

    /**
     * ThreadLocal cache of FFmpeg resamplers.
     * Keyed by (srcHz,dstHz) to avoid re-init cost each frame.
     */
    private static final ThreadLocal<Map<Key, FfmpegResampler>> RESAMPLERS =
            ThreadLocal.withInitial(HashMap::new);

    /**
     * Resample PCM16LE mono from inSampleRate -> outSampleRate.
     *
     * @param inPcm PCM16LE mono bytes. Length must be even.
     * @param inSampleRate input sample rate (Hz)
     * @param outSampleRate output sample rate (Hz)
     * @return resampled PCM16LE mono bytes
     */
    public static byte[] toMono16LE(byte[] inPcm, int inSampleRate, int outSampleRate) {
        // Fast exits
        if (inPcm == null || inPcm.length == 0) {
            if (RTP_DEEP_LOGS) {
                log.trace("audio_transcoder_skip_empty inHz={} outHz={} pcmLen=0 thread={}",
                        inSampleRate, outSampleRate, Thread.currentThread().getName());
            }
            return new byte[0];
        }

        if (inSampleRate <= 0 || outSampleRate <= 0) {
            // ERROR: must always be visible
            log.error("audio_transcoder_invalid_rates inHz={} outHz={} pcmLen={}",
                    inSampleRate, outSampleRate, inPcm.length);
            throw new IllegalArgumentException("Invalid sample rates inSampleRate=" +
                    inSampleRate + " outSampleRate=" + outSampleRate);
        }

        final long t0 = System.nanoTime();
        final String threadName = Thread.currentThread().getName();
        final int originalLen = inPcm.length;

        if (RTP_DEEP_LOGS) {
            log.trace("audio_transcoder_in inHz={} outHz={} pcmLen={} samples={} thread={}",
                    inSampleRate, outSampleRate, originalLen, originalLen / 2, threadName);
        }

        // Defensive: keep pipeline aligned (PCM16LE must be even length)
        if ((inPcm.length & 1) != 0) {
            if (RTP_DEEP_LOGS) {
                log.warn("audio_transcoder_odd_length_trim inHz={} outHz={} pcmLen={} thread={}",
                        inSampleRate, outSampleRate, inPcm.length, threadName);
            }
            byte[] trimmed = new byte[inPcm.length - 1];
            System.arraycopy(inPcm, 0, trimmed, 0, trimmed.length);
            inPcm = trimmed;

            if (RTP_DEEP_LOGS) {
                log.trace("audio_transcoder_trim_done newPcmLen={} newSamples={} thread={}",
                        inPcm.length, inPcm.length / 2, threadName);
            }
        }

        // No-op resample
        if (inSampleRate == outSampleRate) {
            if (RTP_DEEP_LOGS) {
                long us = (System.nanoTime() - t0) / 1000;
                log.trace("audio_transcoder_noop inHz=outHz={} pcmLen={} durUs={} thread={}",
                        inSampleRate, inPcm.length, us, threadName);
            }
            return inPcm;
        }

        final Key key = new Key(inSampleRate, outSampleRate);
        final Map<Key, FfmpegResampler> map = RESAMPLERS.get();

        if (RTP_DEEP_LOGS) {
            log.trace("audio_transcoder_cache_state size={} key={}->{}, thread={}",
                    map.size(), inSampleRate, outSampleRate, threadName);
        }

        FfmpegResampler r = map.get(key);
        if (r == null) {
            if (RTP_DEEP_LOGS) {
                log.debug("audio_transcoder_cache_miss create_resampler srcHz={} dstHz={} existingKeys={} thread={}",
                        inSampleRate, outSampleRate, map.size(), threadName);
            }
            r = new FfmpegResampler(inSampleRate, outSampleRate);
            map.put(key, r);
            if (RTP_DEEP_LOGS) {
                log.info("audio_transcoder_create_resampler srcHz={} dstHz={} thread={}",
                        inSampleRate, outSampleRate, threadName);
            }
        } else {
            if (RTP_DEEP_LOGS) {
                log.trace("audio_transcoder_cache_hit srcHz={} dstHz={} thread={}",
                        inSampleRate, outSampleRate, threadName);
            }
        }

        byte[] out;
        try {
            out = r.resample(inPcm);
        } catch (Exception e) {
            // ERROR: must always be visible
            log.error("audio_transcoder_resample_error srcHz={} dstHz={} inLen={} thread={} err={}",
                    inSampleRate, outSampleRate, inPcm.length, threadName, e.toString(), e);
            // fail-safe: return original input to avoid hard audio drop
            return inPcm;
        }

        if (out == null) {
            // ERROR: must always be visible
            log.error("audio_transcoder_resample_null srcHz={} dstHz={} inLen={} thread={}",
                    inSampleRate, outSampleRate, inPcm.length, threadName);
            return inPcm;
        }

        if (RTP_DEEP_LOGS) {
            long us = (System.nanoTime() - t0) / 1000;
            log.debug("audio_transcoder_out srcHz={} dstHz={} inLen={} outLen={} inSamples={} outSamples={} durUs={} thread={}",
                    inSampleRate, outSampleRate,
                    inPcm.length, out.length,
                    inPcm.length / 2, out.length / 2,
                    us, threadName);
        } else if (RTP_DEEP_LOGS) {
            // If debug disabled but trace enabled, still log timing & sizes
            long us = (System.nanoTime() - t0) / 1000;
            log.trace("audio_transcoder_out_trace srcHz={} dstHz={} inLen={} outLen={} durUs={} thread={}",
                    inSampleRate, outSampleRate, inPcm.length, out.length, us, threadName);
        }

        return out;
    }

    /**
     * Optional cleanup for a thread (call on thread shutdown if you create threads dynamically).
     */
    public static void closeThreadLocal() {
        final String threadName = Thread.currentThread().getName();
        Map<Key, FfmpegResampler> map = RESAMPLERS.get();

        if (DEEP_LOGS) {
            log.debug("audio_transcoder_threadlocal_close_begin size={} thread={}",
                    map.size(), threadName);
        }

        int closed = 0;
        for (FfmpegResampler r : map.values()) {
            try {
                r.close();
                closed++;
            } catch (Exception e) {
                if (RTP_DEEP_LOGS) {
                    log.warn("audio_transcoder_resampler_close_error thread={} err={}",
                            threadName, e.toString());
                }
            }
        }
        map.clear();
        RESAMPLERS.remove();

        if (DEEP_LOGS) {
            log.info("audio_transcoder_threadlocal_closed closedCount={} thread={}",
                    closed, threadName);
        }
    }

    private static final class Key {
        final int srcHz;
        final int dstHz;

        Key(int srcHz, int dstHz) {
            this.srcHz = srcHz;
            this.dstHz = dstHz;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key k = (Key) o;
            return srcHz == k.srcHz && dstHz == k.dstHz;
        }

        @Override
        public int hashCode() {
            return Objects.hash(srcHz, dstHz);
        }

        @Override
        public String toString() {
            return srcHz + "->" + dstHz;
        }
    }
}
