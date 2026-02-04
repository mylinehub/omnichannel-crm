package com.mylinehub.voicebridge.audio.codec;

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

import java.io.ByteArrayOutputStream;

import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import static org.bytedeco.ffmpeg.global.swresample.*;

/**
 * MuLaw codec (G.711 PCMU) using FFmpeg pcm_mulaw encoder/decoder.
 *
 * Goal:
 * - Encode: PCM16LE mono -> PCMU bytes
 * - Decode: PCMU bytes -> ALWAYS PCM16LE mono PACKED @ 8000 Hz (normalized via SWR)
 *
 * FFmpeg-driven choices:
 * - Input layout comes from decoded AVFrame (no guessing mono/stereo)
 * - SWR configured with swr_alloc_set_opts2 (AVChannelLayout aware)
 * - Resample via swr_convert_frame (no manual plane pointers)
 * - Output sizing via av_samples_get_buffer_size (no outSamples*2)
 * - Capacity via swr_get_out_samples (FFmpeg computed)
 */
public final class MuLaw {

    private static final Logger log = LoggerFactory.getLogger(MuLaw.class);
    private static final boolean RTP_DEEP_LOGS = false;

    private static final ThreadLocal<EncoderState> ENCODER =
            ThreadLocal.withInitial(EncoderState::create);

    private static final ThreadLocal<DecoderState> DECODER =
            ThreadLocal.withInitial(DecoderState::create);

    private MuLaw() {}

    /**
     * PCM16LE mono -> PCMU (1 byte/sample).
     */
    public static byte[] pcm16ToMuLaw(byte[] pcm) {
        if (pcm == null || pcm.length < 2) return new byte[0];

        // Ensure even length (PCM16)
        if ((pcm.length & 1) != 0) {
            byte[] trimmed = new byte[pcm.length - 1];
            System.arraycopy(pcm, 0, trimmed, 0, trimmed.length);
            pcm = trimmed;
        }

        final int inSamples = pcm.length / 2;
        if (inSamples <= 0) return new byte[0];

        EncoderState st = ENCODER.get();

        if (!st.ensureFrameCapacity(inSamples)) {
            log.error("MuLaw pcm16ToMuLaw ensureFrameCapacity failed samples={}", inSamples);
            return new byte[0];
        }

        // FFmpeg safety: make sure writable before writing into frame buffer
        int wr = av_frame_make_writable(st.frame);
        if (wr < 0) {
            log.error("MuLaw pcm16ToMuLaw av_frame_make_writable failed ret={}", wr);
            return new byte[0];
        }

        BytePointer dst = st.frame.data(0);
        if (dst == null || dst.isNull()) {
            log.error("MuLaw pcm16ToMuLaw frame.data(0) is null");
            return new byte[0];
        }

        dst.position(0);
        dst.put(pcm, 0, inSamples * 2);
        st.frame.nb_samples(inSamples);

        int ret = avcodec_send_frame(st.ctx, st.frame);
        if (ret < 0) {
            log.error("MuLaw pcm16ToMuLaw avcodec_send_frame failed ret={}", ret);
            return new byte[0];
        }

        // Receive all packets (usually one, but do not assume)
        ByteArrayOutputStream baos = new ByteArrayOutputStream(inSamples);

        while (true) {
            av_packet_unref(st.packet);

            ret = avcodec_receive_packet(st.ctx, st.packet);
            if (ret == AVERROR_EAGAIN() || ret == AVERROR_EOF()) break;
            if (ret < 0) {
                log.error("MuLaw pcm16ToMuLaw avcodec_receive_packet failed ret={}", ret);
                return new byte[0];
            }

            int outLen = st.packet.size();
            if (outLen > 0 && st.packet.data() != null && !st.packet.data().isNull()) {
                byte[] chunk = new byte[outLen];
                st.packet.data().position(0).get(chunk, 0, outLen);
                baos.write(chunk, 0, outLen);
            }
        }

        byte[] out = baos.toByteArray();

        // For PCMU: usually out.length == inSamples
        if (RTP_DEEP_LOGS && out.length != inSamples) {
            log.warn("MuLaw pcm16ToMuLaw unusual_len inSamples={} outLen={}", inSamples, out.length);
        }

        return out;
    }

    /**
     * PCMU bytes -> PCM16LE mono packed bytes.
     * Typical expectation for pure PCMU: out should be exactly inLen * 2 bytes.
     */
    public static byte[] muLawToPcm16(byte[] ulaw) {
        if (ulaw == null || ulaw.length == 0) return new byte[0];

        final int inLen = ulaw.length;
        final int expectedOut = inLen * 2;

        DecoderState st = DECODER.get();

        av_packet_unref(st.packet);
        av_frame_unref(st.decFrame);

        int ret = av_new_packet(st.packet, inLen);
        if (ret < 0) {
            log.error("MuLaw muLawToPcm16 av_new_packet failed ret={} inLen={}", ret, inLen);
            return new byte[0];
        }

        BytePointer p = st.packet.data();
        if (p == null || p.isNull()) {
            log.error("MuLaw muLawToPcm16 packet.data is null after av_new_packet inLen={}", inLen);
            av_packet_unref(st.packet);
            return new byte[0];
        }

        p.position(0);
        p.put(ulaw, 0, inLen);
        st.packet.size(inLen);

        ret = avcodec_send_packet(st.ctx, st.packet);
        if (ret < 0) {
            log.error("MuLaw muLawToPcm16 avcodec_send_packet failed ret={} inLen={}", ret, inLen);
            av_packet_unref(st.packet);
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(expectedOut);

        while (true) {
            ret = avcodec_receive_frame(st.ctx, st.decFrame);
            if (ret == AVERROR_EAGAIN() || ret == AVERROR_EOF()) break;
            if (ret < 0) {
                log.error("MuLaw muLawToPcm16 avcodec_receive_frame failed ret={} inLen={}", ret, inLen);
                break;
            }

            byte[] chunk = st.convertFrameToPcm16MonoPacked(st.decFrame);
            if (chunk.length > 0) baos.write(chunk, 0, chunk.length);

            av_frame_unref(st.decFrame);
        }

        byte[] out = baos.toByteArray();

        if (out.length != expectedOut) {
            log.error("MuLaw muLawToPcm16 BAD_DECODE_LEN inLen={} expectedOutLen={} actualOutLen={}",
                    inLen, expectedOut, out.length);
        }

        av_packet_unref(st.packet);
        av_frame_unref(st.decFrame);

        return out;
    }

    // -------------------------------------------------------------------------
    // Encoder state (thread-local)
    // -------------------------------------------------------------------------

    private static final class EncoderState {
        final AVCodecContext ctx;
        final AVFrame frame;
        final AVPacket packet;

        private EncoderState(AVCodecContext ctx, AVFrame frame, AVPacket packet) {
            this.ctx = ctx;
            this.frame = frame;
            this.packet = packet;
        }

        static EncoderState create() {
            AVCodec encoder = avcodec_find_encoder(AV_CODEC_ID_PCM_MULAW);
            if (encoder == null) throw new IllegalStateException("FFmpeg pcm_mulaw encoder not found");

            AVCodecContext c = avcodec_alloc_context3(encoder);
            if (c == null) throw new IllegalStateException("Failed to alloc AVCodecContext for pcm_mulaw encoder");

            c.sample_rate(8000);
            c.sample_fmt(AV_SAMPLE_FMT_S16);

            AVChannelLayout layout = new AVChannelLayout();
            av_channel_layout_default(layout, 1);
            c.ch_layout(layout);

            c.bit_rate(64000);

            int ret = avcodec_open2(c, encoder, (org.bytedeco.ffmpeg.avutil.AVDictionary) null);
            if (ret < 0) throw new IllegalStateException("Cannot open pcm_mulaw encoder, ret=" + ret);

            AVFrame f = av_frame_alloc();
            if (f == null) throw new IllegalStateException("Failed to alloc AVFrame for encoder");

            f.format(AV_SAMPLE_FMT_S16);
            f.sample_rate(8000);
            f.ch_layout(layout);

            AVPacket p = av_packet_alloc();
            if (p == null) throw new IllegalStateException("Failed to alloc AVPacket for encoder");

            return new EncoderState(c, f, p);
        }

        boolean ensureFrameCapacity(int samples) {
            if (samples <= 0) return false;

            if (frame.nb_samples() != samples || frame.data(0) == null || frame.data(0).isNull()) {
                av_frame_unref(frame);

                frame.nb_samples(samples);
                frame.format(AV_SAMPLE_FMT_S16);
                frame.sample_rate(8000);

                AVChannelLayout layout = new AVChannelLayout();
                av_channel_layout_default(layout, 1);
                frame.ch_layout(layout);

                int ret = av_frame_get_buffer(frame, 0);
                if (ret < 0) {
                    log.error("MuLaw ensureFrameCapacity av_frame_get_buffer failed samples={} ret={}", samples, ret);
                    return false;
                }
            }
            return true;
        }
    }

    // -------------------------------------------------------------------------
    // Decoder state + SWR normalizer (thread-local)
    // -------------------------------------------------------------------------

    private static final class DecoderState {
        final AVCodecContext ctx;
        final AVFrame decFrame;
        final AVFrame outFrame;
        final AVPacket packet;

        SwrContext swr;

        int lastInFmt = Integer.MIN_VALUE;
        int lastInRate = Integer.MIN_VALUE;
        boolean hasLastLayout = false;
        final AVChannelLayout lastLayout = new AVChannelLayout();

        final IntPointer lineSizePtr = new IntPointer(1);

        private DecoderState(AVCodecContext ctx, AVFrame decFrame, AVFrame outFrame, AVPacket packet) {
            this.ctx = ctx;
            this.decFrame = decFrame;
            this.outFrame = outFrame;
            this.packet = packet;
        }

        static DecoderState create() {
            AVCodec decoder = avcodec_find_decoder(AV_CODEC_ID_PCM_MULAW);
            if (decoder == null) throw new IllegalStateException("FFmpeg pcm_mulaw decoder not found");

            AVCodecContext c = avcodec_alloc_context3(decoder);
            if (c == null) throw new IllegalStateException("Failed to alloc AVCodecContext for pcm_mulaw decoder");

            c.sample_rate(8000);
            c.request_sample_fmt(AV_SAMPLE_FMT_S16); // hint only

            AVChannelLayout layout = new AVChannelLayout();
            av_channel_layout_default(layout, 1);
            c.ch_layout(layout);

            int ret = avcodec_open2(c, decoder, (org.bytedeco.ffmpeg.avutil.AVDictionary) null);
            if (ret < 0) throw new IllegalStateException("Cannot open pcm_mulaw decoder, ret=" + ret);

            AVFrame f = av_frame_alloc();
            if (f == null) throw new IllegalStateException("Failed to alloc AVFrame for decoder");

            AVFrame out = av_frame_alloc();
            if (out == null) throw new IllegalStateException("Failed to alloc AVFrame for swr output");

            AVPacket p = av_packet_alloc();
            if (p == null) throw new IllegalStateException("Failed to alloc AVPacket for decoder");

            return new DecoderState(c, f, out, p);
        }

        byte[] convertFrameToPcm16MonoPacked(AVFrame inFrame) {
            if (inFrame == null) return new byte[0];

            // Input truth from the frame
            final int inFmt = inFrame.format();
            final int inRate = (inFrame.sample_rate() > 0) ? inFrame.sample_rate() : 8000;
            final AVChannelLayout inLayout = inFrame.ch_layout();

            // Output fixed
            final int outFmt = AV_SAMPLE_FMT_S16;
            final int outRate = 8000;

            AVChannelLayout outLayout = new AVChannelLayout();
            av_channel_layout_default(outLayout, 1);

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

                // cache layout (FFmpeg helpers)
                if (hasLastLayout) {
                    av_channel_layout_uninit(lastLayout);
                }
                int cpy = av_channel_layout_copy(lastLayout, inLayout);
                hasLastLayout = (cpy >= 0);

                lastInFmt = inFmt;
                lastInRate = inRate;
            }

            // Capacity is FFmpeg computed (no hand math)
            int outSamplesCap = swr_get_out_samples(swr, inFrame.nb_samples());
            if (outSamplesCap <= 0) return new byte[0];

            av_frame_unref(outFrame);
            outFrame.format(outFmt);
            outFrame.sample_rate(outRate);
            outFrame.ch_layout(outLayout);
            outFrame.nb_samples(outSamplesCap);

            int rc = av_frame_get_buffer(outFrame, 0);
            if (rc < 0) return new byte[0];

            rc = av_frame_make_writable(outFrame);
            if (rc < 0) return new byte[0];

            // Frame-to-frame conversion (FFmpeg handles planar/packed internally)
            rc = swr_convert_frame(swr, outFrame, inFrame);
            if (rc < 0) return new byte[0];

            int producedSamples = outFrame.nb_samples();
            if (producedSamples <= 0) return new byte[0];

            lineSizePtr.put(0, 0);
            int outBytes = av_samples_get_buffer_size(lineSizePtr, 1, producedSamples, outFmt, 1);
            if (outBytes <= 0) return new byte[0];

            BytePointer data0 = outFrame.data(0);
            if (data0 == null || data0.isNull()) return new byte[0];

            byte[] out = new byte[outBytes];
            data0.position(0).get(out, 0, outBytes);
            return out;
        }

        byte[] flushSWR() {
            if (swr == null) return new byte[0];

            final int outFmt = AV_SAMPLE_FMT_S16;
            final int outRate = 8000;

            AVChannelLayout outLayout = new AVChannelLayout();
            av_channel_layout_default(outLayout, 1);

            int outSamplesCap = swr_get_out_samples(swr, 0);
            if (outSamplesCap <= 0) return new byte[0];

            av_frame_unref(outFrame);
            outFrame.format(outFmt);
            outFrame.sample_rate(outRate);
            outFrame.ch_layout(outLayout);
            outFrame.nb_samples(outSamplesCap);

            int rc = av_frame_get_buffer(outFrame, 0);
            if (rc < 0) return new byte[0];

            rc = av_frame_make_writable(outFrame);
            if (rc < 0) return new byte[0];

            // Flush: input frame is null
            rc = swr_convert_frame(swr, outFrame, null);
            if (rc < 0) return new byte[0];

            int producedSamples = outFrame.nb_samples();
            if (producedSamples <= 0) return new byte[0];

            lineSizePtr.put(0, 0);
            int outBytes = av_samples_get_buffer_size(lineSizePtr, 1, producedSamples, outFmt, 1);
            if (outBytes <= 0) return new byte[0];

            BytePointer data0 = outFrame.data(0);
            if (data0 == null || data0.isNull()) return new byte[0];

            byte[] out = new byte[outBytes];
            data0.position(0).get(out, 0, outBytes);
            return out;
        }
    }

    // -------------------------------------------------------------------------
    // Decoder lifecycle helpers (call-end only)
    // -------------------------------------------------------------------------

    /**
     * Flush remaining buffered samples from SWR at call end.
     * Call ONCE at hangup; append returned bytes to recording.
     */
    public static byte[] flushDecoder() {
        DecoderState st = DECODER.get();
        if (st == null) return new byte[0];

        try {
            return st.flushSWR();
        } catch (Throwable t) {
            log.error("MuLaw flushDecoder failed", t);
            return new byte[0];
        }
    }

    /**
     * Reset decoder state completely.
     * Call after flushDecoder() at hangup, or before reuse on same thread.
     */
    public static void resetDecoder() {
        DecoderState st = DECODER.get();
        if (st == null) return;

        if (st.swr != null) {
            swr_free(st.swr);
            st.swr = null;
        }

        st.lastInFmt = Integer.MIN_VALUE;
        st.lastInRate = Integer.MIN_VALUE;

        if (st.hasLastLayout) {
            try { av_channel_layout_uninit(st.lastLayout); } catch (Throwable ignore) {}
            st.hasLastLayout = false;
        }

        try { av_frame_unref(st.decFrame); } catch (Throwable ignore) {}
        try { av_frame_unref(st.outFrame); } catch (Throwable ignore) {}
        try { av_packet_unref(st.packet); } catch (Throwable ignore) {}
    }
}
