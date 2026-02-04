/*
 * File: src/main/java/com/mylinehub/voicebridge/audio/resampler/FfmpegResampler.java
 *
 * Purpose:
 *  - FFmpeg/libswresample based PCM16LE mono resampler.
 *  - Removes any custom interpolation/resample logic from the project.
 *
 * Concurrency:
 *  - SwrContext is NOT thread-safe.
 *  - One instance per thread via AudioTranscoder ThreadLocal cache.
 *
 * Dependencies:
 *  - org.bytedeco:ffmpeg-platform (JavaCPP presets).
 */

package com.mylinehub.voicebridge.audio.resampler;

import org.bytedeco.ffmpeg.swresample.SwrContext;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.cloudfoundry.servlet.CloudFoundryActuatorAutoConfiguration.IgnoredCloudFoundryPathsWebSecurityConfiguration;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;

import static org.bytedeco.ffmpeg.global.avutil.*;
import static org.bytedeco.ffmpeg.global.swresample.*;

public final class FfmpegResampler implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(FfmpegResampler.class);

    private final int srcHz;
    private final int dstHz;

    private SwrContext swr;

    // Debug counters (per instance / per thread)
    private final AtomicLong calls = new AtomicLong();
    private final AtomicLong inBytesTotal = new AtomicLong();
    private final AtomicLong outBytesTotal = new AtomicLong();
    private final AtomicLong convertErrs = new AtomicLong();

    private static boolean DEEP_LOGS = false;
    private static boolean RTP_DEEP_LOGS = false;
    
    public FfmpegResampler(int srcHz, int dstHz) {
        if (srcHz <= 0 || dstHz <= 0) {
            throw new IllegalArgumentException("Invalid sample rates srcHz=" + srcHz + " dstHz=" + dstHz);
        }
        this.srcHz = srcHz;
        this.dstHz = dstHz;
        init();
    }

    /**
     * Non-deprecated init:
     * - Replaces swr_alloc_set_opts(...) with:
     *   swr_alloc() + av_opt_set/av_opt_set_int/av_opt_set_sample_fmt
     * - Keeps the same effective configuration as your original code.
     */
    private void init() {
        // Mono layouts on both ends
        long inLayout  = AV_CH_LAYOUT_MONO;
        long outLayout = AV_CH_LAYOUT_MONO;

        if (DEEP_LOGS) {
            log.info("ffmpeg_resampler_init_start srcHz={} dstHz={} inLayout={} outLayout={} inFmt={} outFmt={}",
                    srcHz, dstHz, inLayout, outLayout, AV_SAMPLE_FMT_S16, AV_SAMPLE_FMT_S16);
        }

        swr = swr_alloc();
        if (swr == null) {
            throw new IllegalStateException("FFmpeg swr_alloc returned null");
        }

        // Set options via AVOptions � non-deprecated path
        av_opt_set(swr, "in_channel_layout",  String.valueOf(inLayout), 0);
        av_opt_set(swr, "out_channel_layout", String.valueOf(outLayout), 0);

        av_opt_set_int(swr, "in_sample_rate",  srcHz, 0);
        av_opt_set_int(swr, "out_sample_rate", dstHz, 0);

        av_opt_set_sample_fmt(swr, "in_sample_fmt",  AV_SAMPLE_FMT_S16, 0);
        av_opt_set_sample_fmt(swr, "out_sample_fmt", AV_SAMPLE_FMT_S16, 0);

        int rc = swr_init(swr);
        if (rc < 0) {
            String err = ffErr(rc);
            swr_free(swr);
            swr = null;
            throw new IllegalStateException("FFmpeg swr_init failed rc=" + rc + " err=" + err);
        }

        if (DEEP_LOGS)
        log.info("ffmpeg_resampler_init_ok srcHz={} dstHz={}", srcHz, dstHz);
    }

    /**
     * Resample PCM16LE mono bytes.
     */
    public byte[] resample(byte[] inPcm) {
        long n = calls.incrementAndGet();

        if (inPcm == null || inPcm.length == 0) {
            if (RTP_DEEP_LOGS) {
                log.debug("ffmpeg_resampler_resample_empty call={} srcHz={} dstHz={}", n, srcHz, dstHz);
            }
            return new byte[0];
        }

        int origLen = inPcm.length;

        // Ensure even byte length (16-bit alignment)
        if ((inPcm.length & 1) != 0) {
            byte[] trimmed = new byte[inPcm.length - 1];
            System.arraycopy(inPcm, 0, trimmed, 0, trimmed.length);
            inPcm = trimmed;

            if (RTP_DEEP_LOGS) {
                log.warn("ffmpeg_resampler_unaligned_input_trimmed call={} origBytes={} trimmedBytes={} srcHz={} dstHz={}",
                        n, origLen, inPcm.length, srcHz, dstHz);
            }
        }

        int inSamples = inPcm.length / 2;

        // swr_get_delay returns pending input samples delay in srcHz timebase
        long delay = swr_get_delay(swr, srcHz);

        // Estimate required output samples with rounding up.
        int outSamplesCap = (int) av_rescale_rnd(
                delay + inSamples, dstHz, srcHz, AV_ROUND_UP
        );

        if (outSamplesCap <= 0) {
            if (RTP_DEEP_LOGS) {
                log.warn("ffmpeg_resampler_bad_out_capacity call={} delay={} inSamples={} outSamplesCap={} srcHz={} dstHz={}",
                        n, delay, inSamples, outSamplesCap, srcHz, dstHz);
            }
            return new byte[0];
        }

        if (RTP_DEEP_LOGS) {
            log.debug("ffmpeg_resampler_convert_start call={} inBytes={} inSamples={} delay={} outSamplesCap={} srcHz={} dstHz={}",
                    n, inPcm.length, inSamples, delay, outSamplesCap, srcHz, dstHz);
        }

        BytePointer inBuf = null;
        BytePointer outBuf = null;
        PointerPointer<Pointer> inPtr = null;
        PointerPointer<Pointer> outPtr = null;

        try {
            inBuf = new BytePointer(inPcm); // copies array into native memory
            outBuf = new BytePointer((long) outSamplesCap * 2L);

            // Split construction + put() so Eclipse can see it's assigned and later deallocated
            inPtr = new PointerPointer<>(1);
            inPtr.put(inBuf);

            outPtr = new PointerPointer<>(1);
            outPtr.put(outBuf);

            int outSamples = swr_convert(
                    swr,
                    outPtr,
                    outSamplesCap,
                    inPtr,
                    inSamples
            );

            if (outSamples < 0) {
                convertErrs.incrementAndGet();
                String err = ffErr(outSamples);

                if(RTP_DEEP_LOGS)
                log.warn("ffmpeg_resampler_convert_failed call={} rc={} err={} inSamples={} outSamplesCap={} srcHz={} dstHz={} delay={}",
                        n, outSamples, err, inSamples, outSamplesCap, srcHz, dstHz, delay);

                return new byte[0];
            }

            int outBytes = outSamples * 2;
            byte[] outPcm = new byte[outBytes];
            outBuf.position(0).get(outPcm);

            // update totals
            inBytesTotal.addAndGet(inPcm.length);
            outBytesTotal.addAndGet(outBytes);

            if (RTP_DEEP_LOGS) {
                log.debug("ffmpeg_resampler_convert_ok call={} inSamples={} outSamples={} inBytes={} outBytes={} srcHz={} dstHz={} ratio={} totals_inBytes={} totals_outBytes={} errs={}",
                        n,
                        inSamples,
                        outSamples,
                        inPcm.length,
                        outBytes,
                        srcHz,
                        dstHz,
                        (inSamples == 0 ? 0.0 : (double) outSamples / (double) inSamples),
                        inBytesTotal.get(),
                        outBytesTotal.get(),
                        convertErrs.get()
                );
            }

            if (RTP_DEEP_LOGS) {
                PcmStats inStats = analyzePcm16LE(inPcm);
                PcmStats outStats = analyzePcm16LE(outPcm);
                log.trace("ffmpeg_resampler_pcm_stats call={} in[min={},max={},rms={}] out[min={},max={},rms={}]",
                        n,
                        inStats.min, inStats.max, inStats.rms,
                        outStats.min, outStats.max, outStats.rms
                );
            }

            return outPcm;
        } catch (Throwable t) {
            convertErrs.incrementAndGet();
            log.error("ffmpeg_resampler_exception call={} srcHz={} dstHz={} msg={}",
                    n, srcHz, dstHz, t.toString(), t);
            return new byte[0];
        } finally {
            // Deallocate native pointers
            try { if (inPtr != null) inPtr.deallocate(); } catch (Throwable ignore) {}
            try { if (outPtr != null) outPtr.deallocate(); } catch (Throwable ignore) {}
            try { if (inBuf != null) inBuf.deallocate(); } catch (Throwable ignore) {}
            try { if (outBuf != null) outBuf.deallocate(); } catch (Throwable ignore) {}
        }
    }

    @Override
    public void close() {
        if (swr != null) {
            swr_free(swr);
            swr = null;
            
            if(DEEP_LOGS)
            log.info("ffmpeg_resampler_closed srcHz={} dstHz={} calls={} totals_inBytes={} totals_outBytes={} errs={}",
                    srcHz, dstHz, calls.get(), inBytesTotal.get(), outBytesTotal.get(), convertErrs.get());
        }
    }

    // ---- helpers ----

    /** Convert FFmpeg negative error code to readable string. */
    @SuppressWarnings("resource")
    private static String ffErr(int err) {
        BytePointer errbuf = new BytePointer(256);
        try {
            av_strerror(err, errbuf, 256);
            return errbuf.getString();
        } catch (Throwable t) {
            return "unknown";
        } finally {
            try { errbuf.deallocate(); } catch (Throwable ignore) {}
        }
    }

    /** Lightweight PCM16LE stats for trace logging only. */
    private static PcmStats analyzePcm16LE(byte[] pcm) {
        if (pcm == null || pcm.length < 2) return new PcmStats(0, 0, 0);

        ByteBuffer bb = ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN);
        int samples = pcm.length / 2;

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        double sumSq = 0;

        for (int i = 0; i < samples; i++) {
            short v = bb.getShort();
            if (v < min) min = v;
            if (v > max) max = v;
            sumSq += (double) v * (double) v;
        }

        double rms = Math.sqrt(sumSq / samples);
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
