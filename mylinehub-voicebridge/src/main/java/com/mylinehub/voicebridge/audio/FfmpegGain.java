/*
 * Auto-formatted + DEEP LOGS:
 * File: src/main/java/com/mylinehub/voicebridge/audio/FfmpegGain.java
 *
 * Purpose:
 *  - Analyze PCM16LE mono (RMS).
 *  - If level is "too low", apply gain using FFmpeg libswresample.
 *  - Keep sample rate, format, channels unchanged.
 *
 * Tuning via system properties:
 *  - -Dvoicebridge.gain.deepLogs=true
 *  - -Dvoicebridge.gain.minRms=1200        (below this we consider audio "low")
 *  - -Dvoicebridge.gain.targetRms=4000     (we try to bring RMS up to ~this)
 *  - -Dvoicebridge.gain.maxFactor=3.0      (hard cap on gain factor)
 */

package com.mylinehub.voicebridge.audio;

import org.bytedeco.ffmpeg.swresample.SwrContext;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.PointerPointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.bytedeco.ffmpeg.global.avutil.AV_CH_LAYOUT_MONO;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_S16;
import static org.bytedeco.ffmpeg.global.avutil.av_opt_set_double;
import static org.bytedeco.ffmpeg.global.avutil.av_opt_set_int;
import static org.bytedeco.ffmpeg.global.avutil.av_opt_set_sample_fmt;
import static org.bytedeco.ffmpeg.global.swresample.swr_alloc;
import static org.bytedeco.ffmpeg.global.swresample.swr_convert;
import static org.bytedeco.ffmpeg.global.swresample.swr_free;
import static org.bytedeco.ffmpeg.global.swresample.swr_init;

public final class FfmpegGain {

    // Single master switch for deep logs (hot path friendly).
    private static final boolean RTP_DEEP_LOGS = false;

    private static final Logger log = LoggerFactory.getLogger(FfmpegGain.class);

    // Below this RMS we call it "low" and consider boosting.
    private static final int MIN_RMS =
        Integer.parseInt(System.getProperty("voicebridge.gain.minRms", "1200"));

    // Target RMS we try to approach when boosting.
    private static final int TARGET_RMS =
        Integer.parseInt(System.getProperty("voicebridge.gain.targetRms", "4000"));

    // Hard maximum gain factor (e.g. 3.0 = +9.5 dB).
    private static final double MAX_GAIN =
        Double.parseDouble(System.getProperty("voicebridge.gain.maxFactor", "3.0"));

    private FfmpegGain() {
        // utility
    }

    /**
     * Auto-gain: analyze RMS and only boost if it is low.
     *
     * @param pcm16      PCM16LE mono samples (little-endian)
     * @param sampleRate sample rate in Hz (8000 / 16000 / 24000 / 48000)
     * @return possibly gain-adjusted PCM16LE, same sampleRate/channels
     */
    public static byte[] autoGainIfLow(byte[] pcm16, int sampleRate) {
        if (pcm16 == null || pcm16.length < 2) {
            return pcm16;
        }

        PcmStats stats = analyzePcm16LE(pcm16);

        if (RTP_DEEP_LOGS) {
            log.debug("autoGainIfLow analyze: rms={} min={} max={} minRms={} targetRms={} maxGain={}",
                    stats.rms, stats.min, stats.max, MIN_RMS, TARGET_RMS, MAX_GAIN);
        }

        // If already above threshold, do NOTHING.
        if (stats.rms >= MIN_RMS) {
            if (RTP_DEEP_LOGS) {
                log.debug("autoGainIfLow: RMS={} >= minRms={}, no gain applied", stats.rms, MIN_RMS);
            }
            return pcm16;
        }

        // Compute theoretical gain to hit target RMS.
        double rawGain = (stats.rms <= 0.0) ? MAX_GAIN : (TARGET_RMS / stats.rms);
        double gain = Math.min(rawGain, MAX_GAIN);

        // If gain is tiny, skip.
        if (gain <= 1.01) {
            if (RTP_DEEP_LOGS) {
                log.debug("autoGainIfLow: computedGain={} ~1.0, skipping", gain);
            }
            return pcm16;
        }

        if (RTP_DEEP_LOGS) {
            log.info("autoGainIfLow: applying gain={} (raw={} rms={} -> targetRms={})",
                    gain, rawGain, stats.rms, TARGET_RMS);
        }

        return applyGainWithFfmpeg(pcm16, sampleRate, gain);
    }

    // =============================================================================
    // Internal: apply gain using FFmpeg libswresample
    // =============================================================================

    private static byte[] applyGainWithFfmpeg(byte[] pcm16, int sampleRate, double gain) {
        if (pcm16 == null || pcm16.length == 0) return pcm16;

        int inSamples = pcm16.length / 2; // PCM16 mono: 2 bytes per sample
        if (inSamples <= 0) return pcm16;

        // New-style API: swr_alloc() + av_opt_set_* (instead of deprecated swr_alloc_set_opts)
        SwrContext swr = swr_alloc();
        if (swr == null) {
            if (RTP_DEEP_LOGS) {
                log.warn("applyGainWithFfmpeg: swr_alloc returned null, returning original audio");
            }
            return pcm16;
        }

        // Configure in/out formats: mono, S16, same sample rate
        av_opt_set_int(swr, "in_channel_layout", AV_CH_LAYOUT_MONO, 0);
        av_opt_set_int(swr, "out_channel_layout", AV_CH_LAYOUT_MONO, 0);
        av_opt_set_int(swr, "in_sample_rate", sampleRate, 0);
        av_opt_set_int(swr, "out_sample_rate", sampleRate, 0);
        av_opt_set_sample_fmt(swr, "in_sample_fmt", AV_SAMPLE_FMT_S16, 0);
        av_opt_set_sample_fmt(swr, "out_sample_fmt", AV_SAMPLE_FMT_S16, 0);

        // Volume filter via swresample "volume" option
        av_opt_set_double(swr, "volume", gain, 0);

        if (swr_init(swr) < 0) {
            if (RTP_DEEP_LOGS) {
                log.warn("applyGainWithFfmpeg: swr_init failed, returning original audio");
            }
            swr_free(swr);
            return pcm16;
        }

        BytePointer inPtr = new BytePointer(pcm16.length);
        inPtr.position(0);
        inPtr.put(pcm16);

        BytePointer outPtr = new BytePointer(pcm16.length);

        // Use generic PointerPointer<BytePointer> to avoid raw-type warnings
        PointerPointer<BytePointer> outPP = new PointerPointer<>(1);
        outPP.put(0, outPtr);

        PointerPointer<BytePointer> inPP = new PointerPointer<>(1);
        inPP.put(0, inPtr);

        int outSamples = swr_convert(
                swr,
                outPP, inSamples,   // dst, dst samples capacity
                inPP, inSamples     // src, src samples
        );

        byte[] out;
        if (outSamples <= 0) {
            if (RTP_DEEP_LOGS) {
                log.warn("applyGainWithFfmpeg: swr_convert returned outSamples={} <= 0, returning original", outSamples);
            }
            out = pcm16;
        } else {
            int outBytes = outSamples * 2; // mono, 2 bytes per sample
            out = new byte[outBytes];
            outPtr.position(0);
            outPtr.get(out, 0, outBytes);
        }

        swr_free(swr);
        inPtr.deallocate();
        outPtr.deallocate();

        return out;
    }

    // =============================================================================
    // Internal: PCM16 stats
    // =============================================================================

    private static PcmStats analyzePcm16LE(byte[] pcm) {
        if (pcm == null || pcm.length < 2) {
            return new PcmStats(0, 0, 0.0);
        }

        ByteBuffer bb = ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN);
        int samples = pcm.length / 2;

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        double sumSq = 0.0;

        for (int i = 0; i < samples; i++) {
            short v = bb.getShort();
            if (v < min) min = v;
            if (v > max) max = v;
            sumSq += (double) v * (double) v;
        }

        double rms = Math.sqrt(sumSq / Math.max(1, samples));
        return new PcmStats(min, max, rms);
    }

    private static final class PcmStats {
        final int min;
        final int max;
        final double rms;

        PcmStats(int min, int max, double rms) {
            this.min = min;
            this.max = max;
            this.rms = rms;
        }
    }
}
