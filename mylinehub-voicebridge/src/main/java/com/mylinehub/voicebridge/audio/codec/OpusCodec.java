/*
 * Auto-formatted + Deep-Logged:
 * File: src/main/java/com/mylinehub/voicebridge/audio/codec/OpusCodec.java
 */
package com.mylinehub.voicebridge.audio.codec;

import com.mylinehub.voicebridge.audio.AudioCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avutil.AVChannelLayout;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.ffmpeg.swresample.SwrContext;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;

import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import static org.bytedeco.ffmpeg.global.swresample.*;

/**
 * OPUS codec wrapper (FFmpeg) implementing the {@link AudioCodec} contract.
 *
 * Telephony real-time usage:
 *  - PCM16 LE mono.
 *  - 20 ms frames.
 *  - One Opus frame per RTP packet (encode enforces exact frame size).
 *
 * Concurrency:
 *  - This class is NOT thread-safe.
 *  - Use one OpusCodec instance per playout/rtp thread (per call).
 *
 * "FFmpeg-driven" goals in this file:
 *  - No manual byte sizing like nbSamples * 2
 *  - Output size computed via av_samples_get_buffer_size()
 *  - Decoder normalization via SWR (swr_alloc_set_opts2 + swr_convert_frame)
 *    so output is ALWAYS S16 packed mono @ fs, regardless of what decoder produces.
 *
 * Logging:
 *  - Controlled by static booleans below:
 *      DEEP_LOGS     : gates all non-error logs (INFO, DEBUG, WARN, TRACE).
 *      RTP_DEEP_LOGS : reserved for RTP-heavy logs (not used directly here).
 *  - ERROR logs are NEVER gated and always printed.
 */
public final class OpusCodec implements AudioCodec {

    // =====================================================================
    // Global logging switches for this file.
    // =====================================================================

    private static final boolean DEEP_LOGS = false;
    @SuppressWarnings("unused")
    private static final boolean RTP_DEEP_LOGS = false;

    private static final Logger log = LoggerFactory.getLogger(OpusCodec.class);

    /** Sample rate (Hz), e.g., 48000, 16000. */
    private final int fs;

    /** 20ms frame size in samples = fs/50. (Business rule for RTP alignment) */
    private final int frameSamples;

    // ----- FFmpeg encoder/decoder state (per instance / per call-thread) -----
    private final AVCodecContext encCtx;
    private final AVCodecContext decCtx;

    private final AVFrame encFrame;
    private final AVFrame decFrame; // raw decoded frame from opus decoder

    private final AVPacket encPacket;
    private final AVPacket decPacket;

    private final AVChannelLayout monoLayout;

    // ----- Decoder SWR normalizer (to force S16 packed mono @ fs) -----
    private SwrContext swr;
    private final AVFrame swrOutFrame; // output frame from SWR convert_frame

    // Cache last decoder input signature to know when to rebuild SWR
    private int lastInFmt = Integer.MIN_VALUE;
    private int lastInRate = Integer.MIN_VALUE;
    private boolean hasLastLayout = false;
    private final AVChannelLayout lastLayout = new AVChannelLayout();

    // Reusable pointers (avoid per-call allocations)
    private final IntPointer lineSizePtr = new IntPointer(1);

    // --- deep counters (per instance / per call-thread) ---
    private final AtomicLong encCalls = new AtomicLong();
    private final AtomicLong decCalls = new AtomicLong();
    private final AtomicLong encErrs  = new AtomicLong();
    private final AtomicLong decErrs  = new AtomicLong();
    private final AtomicLong inBytesTotal  = new AtomicLong();
    private final AtomicLong outBytesTotal = new AtomicLong();

    public OpusCodec(int fs) {
        if (fs <= 0) throw new IllegalArgumentException("Invalid Opus fs=" + fs);

        this.fs = fs;
        this.frameSamples = Math.max(1, fs / 50); // 20ms business rule

        // ------------------ COMMON MONO LAYOUT ------------------
        this.monoLayout = new AVChannelLayout();
        av_channel_layout_default(this.monoLayout, 1); // mono

        // ------------------ ENCODER SETUP ------------------
        AVCodec encoder = avcodec_find_encoder(AV_CODEC_ID_OPUS);
        if (encoder == null) {
            throw new IllegalStateException("FFmpeg opus encoder not found (AV_CODEC_ID_OPUS)");
        }

        this.encCtx = avcodec_alloc_context3(encoder);
        if (this.encCtx == null) {
            throw new IllegalStateException("Failed to alloc AVCodecContext for opus encoder");
        }

        encCtx.sample_rate(fs);
        encCtx.sample_fmt(AV_SAMPLE_FMT_S16);
        encCtx.ch_layout(monoLayout);
        encCtx.bit_rate(32_000); // telephony-ish default

        int rc = avcodec_open2(encCtx, encoder, (org.bytedeco.ffmpeg.avutil.AVDictionary) null);
        if (rc < 0) {
            throw new IllegalStateException("Cannot open opus encoder, ret=" + rc);
        }

        this.encFrame = av_frame_alloc();
        if (this.encFrame == null) {
            throw new IllegalStateException("Failed to alloc AVFrame for opus encoder");
        }
        encFrame.format(AV_SAMPLE_FMT_S16);
        encFrame.sample_rate(fs);
        encFrame.ch_layout(monoLayout);

        this.encPacket = av_packet_alloc();
        if (this.encPacket == null) {
            throw new IllegalStateException("Failed to alloc AVPacket for opus encoder");
        }

        // ------------------ DECODER SETUP ------------------
        AVCodec decoder = avcodec_find_decoder(AV_CODEC_ID_OPUS);
        if (decoder == null) {
            throw new IllegalStateException("FFmpeg opus decoder not found (AV_CODEC_ID_OPUS)");
        }

        this.decCtx = avcodec_alloc_context3(decoder);
        if (this.decCtx == null) {
            throw new IllegalStateException("Failed to alloc AVCodecContext for opus decoder");
        }

        // Decoder hints (not guarantees):
        decCtx.sample_rate(fs);
        decCtx.request_sample_fmt(AV_SAMPLE_FMT_S16);
        decCtx.ch_layout(monoLayout);

        rc = avcodec_open2(decCtx, decoder, (org.bytedeco.ffmpeg.avutil.AVDictionary) null);
        if (rc < 0) {
            throw new IllegalStateException("Cannot open opus decoder, ret=" + rc);
        }

        this.decFrame = av_frame_alloc();
        if (this.decFrame == null) {
            throw new IllegalStateException("Failed to alloc AVFrame for opus decoder");
        }

        this.decPacket = av_packet_alloc();
        if (this.decPacket == null) {
            throw new IllegalStateException("Failed to alloc AVPacket for opus decoder");
        }

        // SWR output frame
        this.swrOutFrame = av_frame_alloc();
        if (this.swrOutFrame == null) {
            throw new IllegalStateException("Failed to alloc AVFrame for SWR output");
        }

        if (DEEP_LOGS) {
            log.debug("opus_codec_init_ok fs={} frameSamples(20ms)={} bitrate={}b",
                fs, frameSamples, encCtx.bit_rate());
        }
    }

    @Override
    public String name() {
        return "opus";
    }

    @Override
    public int sampleRate() {
        return fs;
    }

    /**
     * Encode PCM16 LE mono (20ms) into ONE Opus packet using FFmpeg.
     *
     * Business rule (not FFmpeg sizing):
     *  - Input must be exactly one 20ms frame.
     *  - If not, it is trimmed/padded to preserve RTP alignment.
     */
    @Override
    public byte[] encodePcmToPayload(byte[] pcm) {
        long n = encCalls.incrementAndGet();

        if (pcm == null || pcm.length == 0) {
            if (DEEP_LOGS) log.debug("opus_encode_empty call={} fs={}", n, fs);
            return new byte[0];
        }

        // Normalize input to exactly frameSamples (20ms) – this is intentional RTP logic.
        int targetBytes = frameSamples * 2; // 2 bytes/sample for PCM16 (business invariant)
        byte[] usePcm;
        if (pcm.length != targetBytes) {
            usePcm = new byte[targetBytes];
            int copy = Math.min(pcm.length, targetBytes);
            System.arraycopy(pcm, 0, usePcm, 0, copy);

            if (DEEP_LOGS) {
                log.warn("opus_encode_adjust_frame call={} fs={} inBytes={} -> targetBytes={}",
                    n, fs, pcm.length, targetBytes);
            }
        } else {
            usePcm = pcm;
        }

        if (!ensureEncFrameCapacity(frameSamples)) {
            encErrs.incrementAndGet();
            log.error("opus_encode_frame_capacity_failed call={} fs={} samples={}", n, fs, frameSamples);
            return new byte[0];
        }

        int wr = av_frame_make_writable(encFrame);
        if (wr < 0) {
            encErrs.incrementAndGet();
            log.error("opus_encode_make_writable_failed call={} fs={} ret={}", n, fs, wr);
            return new byte[0];
        }

        BytePointer dst = encFrame.data(0);
        if (dst == null || dst.isNull()) {
            encErrs.incrementAndGet();
            log.error("opus_encode_frame_data_null call={} fs={}", n, fs);
            return new byte[0];
        }

        dst.position(0);
        dst.put(usePcm, 0, usePcm.length);

        encFrame.nb_samples(frameSamples);

        try {
            if (DEEP_LOGS) {
                log.debug("opus_encode_start call={} fs={} frameSamples={} inBytes={} effBytes={}",
                    n, fs, frameSamples, pcm.length, usePcm.length);
            }

            int ret = avcodec_send_frame(encCtx, encFrame);
            if (ret < 0) {
                encErrs.incrementAndGet();
                log.error("opus_encode_send_frame_failed call={} fs={} ret={}", n, fs, ret);
                return new byte[0];
            }

            // Receive ONE packet for this 20ms frame (Opus is typically 1 packet).
            av_packet_unref(encPacket);
            ret = avcodec_receive_packet(encCtx, encPacket);
            if (ret < 0) {
                encErrs.incrementAndGet();
                log.error("opus_encode_receive_packet_failed call={} fs={} ret={}", n, fs, ret);
                return new byte[0];
            }

            int outLen = encPacket.size();
            if (outLen <= 0 || encPacket.data() == null || encPacket.data().isNull()) {
                encErrs.incrementAndGet();
                log.error("opus_encode_bad_packet call={} fs={} outLen={}", n, fs, outLen);
                av_packet_unref(encPacket);
                return new byte[0];
            }

            byte[] payload = new byte[outLen];
            encPacket.data().position(0).get(payload, 0, outLen);
            av_packet_unref(encPacket);

            inBytesTotal.addAndGet(usePcm.length);
            outBytesTotal.addAndGet(payload.length);

            if (DEEP_LOGS) {
                log.debug("opus_encode_ok call={} fs={} outBytes={} totals_inBytes={} totals_outBytes={}",
                    n, fs, payload.length, inBytesTotal.get(), outBytesTotal.get());
                PcmStats st = analyzePcm16LE(usePcm);
                log.trace("opus_encode_pcm_stats call={} min={} max={} rms={}", n, st.min, st.max, st.rms);
            }

            return payload;
        } catch (Throwable e) {
            encErrs.incrementAndGet();
            log.error("opus_encode_failed call={} fs={} errCount={} msg={}",
                n, fs, encErrs.get(), e.toString(), e);
            return new byte[0];
        }
    }

    /**
     * Decode ONE Opus packet into PCM16 LE mono using FFmpeg, normalized by SWR:
     *  - Output ALWAYS S16 packed mono @ fs (even if decoder outputs planar/float/other layout).
     */
    @Override
    public byte[] decodePayloadToPcm(byte[] payload) {
        long n = decCalls.incrementAndGet();

        if (payload == null || payload.length == 0) {
            if (DEEP_LOGS) log.debug("opus_decode_empty call={} fs={}", n, fs);
            return new byte[0];
        }

        try {
            if (DEEP_LOGS) {
                log.debug("opus_decode_start call={} fs={} payloadBytes={}", n, fs, payload.length);
            }

            // Use FFmpeg-owned packet buffer (copy payload into it) to avoid BytePointer lifetime/ownership issues.
            av_packet_unref(decPacket);
            int rc = av_new_packet(decPacket, payload.length);
            if (rc < 0) {
                decErrs.incrementAndGet();
                log.error("opus_decode_av_new_packet_failed call={} fs={} ret={}", n, fs, rc);
                return new byte[0];
            }

            BytePointer p = decPacket.data();
            if (p == null || p.isNull()) {
                decErrs.incrementAndGet();
                log.error("opus_decode_packet_data_null call={} fs={}", n, fs);
                av_packet_unref(decPacket);
                return new byte[0];
            }

            p.position(0);
            p.put(payload, 0, payload.length);
            decPacket.size(payload.length);

            int ret = avcodec_send_packet(decCtx, decPacket);
            if (ret < 0) {
                decErrs.incrementAndGet();
                log.error("opus_decode_send_packet_failed call={} fs={} ret={}", n, fs, ret);
                av_packet_unref(decPacket);
                return new byte[0];
            }

            // Most RTP usage: one opus packet -> one decoded frame, but we still loop safely.
            byte[] lastChunk = new byte[0];

            while (true) {
                av_frame_unref(decFrame);

                ret = avcodec_receive_frame(decCtx, decFrame);
                if (ret == AVERROR_EAGAIN() || ret == AVERROR_EOF()) break;
                if (ret < 0) {
                    decErrs.incrementAndGet();
                    log.error("opus_decode_receive_frame_failed call={} fs={} ret={}", n, fs, ret);
                    break;
                }

                // Normalize to S16 packed mono @ fs using SWR convert_frame (FFmpeg handles planar/packed).
                byte[] chunk = normalizeDecodedFrameToPcm16Mono(decFrame);
                if (chunk.length > 0) {
                    lastChunk = chunk; // for RTP we typically expect one chunk; keep last if multiple
                }
            }

            av_packet_unref(decPacket);
            av_frame_unref(decFrame);

            inBytesTotal.addAndGet(payload.length);
            outBytesTotal.addAndGet(lastChunk.length);

            if (DEEP_LOGS) {
                log.debug("opus_decode_ok call={} fs={} outBytes={} totals_inBytes={} totals_outBytes={}",
                    n, fs, lastChunk.length, inBytesTotal.get(), outBytesTotal.get());
                PcmStats st = analyzePcm16LE(lastChunk);
                log.trace("opus_decode_pcm_stats call={} min={} max={} rms={}", n, st.min, st.max, st.rms);
            }

            return lastChunk;

        } catch (Throwable e) {
            decErrs.incrementAndGet();
            log.error("opus_decode_failed call={} fs={} errCount={} msg={}",
                n, fs, decErrs.get(), e.toString(), e);
            return new byte[0];
        }
    }

    // ---------------------------------------------------------------------------
    // Decoder normalization (SWR)
    // ---------------------------------------------------------------------------

    private byte[] normalizeDecodedFrameToPcm16Mono(AVFrame inFrame) {
        if (inFrame == null) return new byte[0];

        final int inFmt = inFrame.format();
        final int inRate = (inFrame.sample_rate() > 0) ? inFrame.sample_rate() : fs;
        final AVChannelLayout inLayout = inFrame.ch_layout();

        final int outFmt = AV_SAMPLE_FMT_S16;
        final int outRate = fs;

        // outLayout mono (FFmpeg struct)
        AVChannelLayout outLayout = new AVChannelLayout();
        av_channel_layout_default(outLayout, 1);

        // Rebuild SWR if input signature changed
        boolean needReinit;
        if (swr == null) {
            needReinit = true;
        } else if (inFmt != lastInFmt || inRate != lastInRate) {
            needReinit = true;
        } else if (!hasLastLayout) {
            needReinit = true;
        } else {
            needReinit = av_channel_layout_compare(lastLayout, inLayout) != 0;
        }

        if (needReinit) {
            if (swr != null) {
                swr_free(swr);
                swr = null;
            }

            PointerPointer<SwrContext> swrPtr = new PointerPointer<>(1);
            swrPtr.put(0, (SwrContext) null);

            int rc = swr_alloc_set_opts2(
                swrPtr,
                outLayout, outFmt, outRate,
                inLayout,  inFmt,  inRate,
                0, null
            );
            if (rc < 0) throw new IllegalStateException("swr_alloc_set_opts2 failed rc=" + rc);

            Pointer raw = swrPtr.get(0);
            if (raw == null) throw new IllegalStateException("swr_alloc_set_opts2 returned null SwrContext");

            swr = new SwrContext(raw);

            rc = swr_init(swr);
            if (rc < 0) {
                swr_free(swr);
                swr = null;
                throw new IllegalStateException("swr_init failed rc=" + rc);
            }

            // Cache layout signature
            if (hasLastLayout) {
                av_channel_layout_uninit(lastLayout);
            }
            int cpy = av_channel_layout_copy(lastLayout, inLayout);
            hasLastLayout = (cpy >= 0);

            lastInFmt = inFmt;
            lastInRate = inRate;
        }

        // FFmpeg-driven capacity: ask SWR how many samples it may output for this input
        int outSamplesCap = swr_get_out_samples(swr, inFrame.nb_samples());
        if (outSamplesCap <= 0) return new byte[0];

        // Prepare out frame
        av_frame_unref(swrOutFrame);
        swrOutFrame.format(outFmt);
        swrOutFrame.sample_rate(outRate);
        swrOutFrame.ch_layout(outLayout);
        swrOutFrame.nb_samples(outSamplesCap);

        int rc = av_frame_get_buffer(swrOutFrame, 0);
        if (rc < 0) return new byte[0];

        rc = av_frame_make_writable(swrOutFrame);
        if (rc < 0) return new byte[0];

        // Frame-to-frame conversion (no manual plane pointers)
        rc = swr_convert_frame(swr, swrOutFrame, inFrame);
        if (rc < 0) return new byte[0];

        // Compute byte size using FFmpeg helper (no nb_samples * 2)
        lineSizePtr.put(0, 0);
        int outBytes = av_samples_get_buffer_size(
            lineSizePtr,
            1, // mono
            swrOutFrame.nb_samples(),
            outFmt,
            1
        );
        if (outBytes <= 0) return new byte[0];

        BytePointer data0 = swrOutFrame.data(0);
        if (data0 == null || data0.isNull()) return new byte[0];

        byte[] out = new byte[outBytes];
        data0.position(0).get(out, 0, outBytes);
        return out;
    }

    // ---------------------------------------------------------------------------
    // Encoder frame capacity helper
    // ---------------------------------------------------------------------------

    private boolean ensureEncFrameCapacity(int samples) {
        if (samples <= 0) return false;

        if (encFrame.nb_samples() != samples || encFrame.data(0) == null || encFrame.data(0).isNull()) {
            av_frame_unref(encFrame);
            encFrame.nb_samples(samples);
            encFrame.format(AV_SAMPLE_FMT_S16);
            encFrame.sample_rate(fs);
            encFrame.ch_layout(monoLayout);

            int ret = av_frame_get_buffer(encFrame, 0);
            if (ret < 0) {
                log.error("opus_enc_frame_get_buffer_failed samples={} ret={}", samples, ret);
                return false;
            }
        }
        return true;
    }

    // -----------------------------
    // Helpers: PCM stats (trace)
    // -----------------------------

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

    // ---------------------------------------------------------------------------
    // AudioCodec extra hooks (decoder tail / reset)
    // ---------------------------------------------------------------------------

    /**
     * Flush any SWR buffered samples at end-of-call (if any).
     * For Opus decode, SWR *can* have a small tail depending on resampling/layout changes.
     */
    @Override
    public byte[] flushDecoderTailPcm16() {
        try {
            if (swr == null) return new byte[0];

            final int outFmt = AV_SAMPLE_FMT_S16;
            final int outRate = fs;

            AVChannelLayout outLayout = new AVChannelLayout();
            av_channel_layout_default(outLayout, 1);

            int outSamplesCap = swr_get_out_samples(swr, 0);
            if (outSamplesCap <= 0) return new byte[0];

            av_frame_unref(swrOutFrame);
            swrOutFrame.format(outFmt);
            swrOutFrame.sample_rate(outRate);
            swrOutFrame.ch_layout(outLayout);
            swrOutFrame.nb_samples(outSamplesCap);

            int rc = av_frame_get_buffer(swrOutFrame, 0);
            if (rc < 0) return new byte[0];

            rc = av_frame_make_writable(swrOutFrame);
            if (rc < 0) return new byte[0];

            // Flush: input frame is null
            rc = swr_convert_frame(swr, swrOutFrame, null);
            if (rc < 0) return new byte[0];

            lineSizePtr.put(0, 0);
            int outBytes = av_samples_get_buffer_size(lineSizePtr, 1, swrOutFrame.nb_samples(), outFmt, 1);
            if (outBytes <= 0) return new byte[0];

            BytePointer data0 = swrOutFrame.data(0);
            if (data0 == null || data0.isNull()) return new byte[0];

            byte[] out = new byte[outBytes];
            data0.position(0).get(out, 0, outBytes);
            return out;

        } catch (Throwable t) {
            log.error("opus_flush_decoder_tail_failed msg={}", t.toString(), t);
            return new byte[0];
        }
    }

    @Override
    public void resetDecoderState() {
        try {
            av_packet_unref(decPacket);
        } catch (Throwable ignore) {}
        try {
            av_frame_unref(decFrame);
        } catch (Throwable ignore) {}
        try {
            av_frame_unref(swrOutFrame);
        } catch (Throwable ignore) {}

        if (swr != null) {
            try { swr_free(swr); } catch (Throwable ignore) {}
            swr = null;
        }

        lastInFmt = Integer.MIN_VALUE;
        lastInRate = Integer.MIN_VALUE;

        if (hasLastLayout) {
            try { av_channel_layout_uninit(lastLayout); } catch (Throwable ignore) {}
            hasLastLayout = false;
        }
    }
}
