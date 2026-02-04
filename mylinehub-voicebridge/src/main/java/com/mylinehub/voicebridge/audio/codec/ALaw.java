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
 * A-law codec (G.711 PCMA) using FFmpeg pcm_alaw encoder/decoder.
 *
 * API (unchanged):
 *  - pcm16ToALaw(byte[]) : PCM16LE mono -> PCMA
 *  - aLawToPcm16(byte[]) : PCMA -> PCM16LE mono packed @ 8000 (normalized via SWR)
 *
 * FFmpeg-driven rules:
 *  - Packet memory uses av_new_packet + copy (safe ownership)
 *  - Receive loops for packets/frames (no "assume one")
 *  - Output byte sizing uses av_samples_get_buffer_size (no nbSamples*2 math)
 *  - Normalize output via SWR using swr_alloc_set_opts2 + swr_convert_frame (AVChannelLayout aware)
 *  - Fast-path: if decoded frame already matches target, copy directly (optional performance win)
 */
public final class ALaw {

    private static final Logger log = LoggerFactory.getLogger(ALaw.class);
    private static final boolean RTP_DEEP_LOGS = false;

    private static final ThreadLocal<EncoderState> ENCODER =
            ThreadLocal.withInitial(EncoderState::create);

    private static final ThreadLocal<DecoderState> DECODER =
            ThreadLocal.withInitial(DecoderState::create);

    private ALaw() {}

    /**
     * PCM16LE mono bytes -> PCMA bytes.
     */
    public static byte[] pcm16ToALaw(byte[] pcm) {
        if (pcm == null || pcm.length < 2) return new byte[0];

        if ((pcm.length & 1) != 0) {
            byte[] trimmed = new byte[pcm.length - 1];
            System.arraycopy(pcm, 0, trimmed, 0, trimmed.length);
            pcm = trimmed;
        }

        final int inSamples = pcm.length / 2;
        if (inSamples <= 0) return new byte[0];

        EncoderState st = ENCODER.get();

        if (!st.ensureFrameCapacity(inSamples)) {
            log.error("ALaw pcm16ToALaw ensureFrameCapacity failed samples={}", inSamples);
            return new byte[0];
        }

        int wr = av_frame_make_writable(st.frame);
        if (wr < 0) {
            log.error("ALaw pcm16ToALaw av_frame_make_writable failed ret={}", wr);
            return new byte[0];
        }

        BytePointer dst = st.frame.data(0);
        if (dst == null || dst.isNull()) {
            log.error("ALaw pcm16ToALaw frame.data(0) is null");
            return new byte[0];
        }

        dst.position(0);
        dst.put(pcm, 0, inSamples * 2);
        st.frame.nb_samples(inSamples);

        int ret = avcodec_send_frame(st.ctx, st.frame);
        if (ret < 0) {
            log.error("ALaw pcm16ToALaw avcodec_send_frame failed ret={}", ret);
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(inSamples);

        while (true) {
            av_packet_unref(st.packet);

            ret = avcodec_receive_packet(st.ctx, st.packet);
            if (ret == AVERROR_EAGAIN() || ret == AVERROR_EOF()) break;
            if (ret < 0) {
                log.error("ALaw pcm16ToALaw avcodec_receive_packet failed ret={}", ret);
                return new byte[0];
            }

            int outLen = st.packet.size();
            if (outLen > 0 && st.packet.data() != null && !st.packet.data().isNull()) {
                byte[] chunk = new byte[outLen];
                st.packet.data().position(0).get(chunk, 0, outLen);
                baos.write(chunk, 0, outLen);
            }
        }

        return baos.toByteArray();
    }

    /**
     * PCMA bytes -> PCM16LE mono packed @ 8000.
     */
    public static byte[] aLawToPcm16(byte[] alaw) {
        if (alaw == null || alaw.length == 0) return new byte[0];

        final int inLen = alaw.length;

        DecoderState st = DECODER.get();

        av_packet_unref(st.packet);
        av_frame_unref(st.decFrame);

        int ret = av_new_packet(st.packet, inLen);
        if (ret < 0) {
            log.error("ALaw aLawToPcm16 av_new_packet failed ret={} inLen={}", ret, inLen);
            return new byte[0];
        }

        BytePointer p = st.packet.data();
        if (p == null || p.isNull()) {
            log.error("ALaw aLawToPcm16 packet.data is null after av_new_packet inLen={}", inLen);
            av_packet_unref(st.packet);
            return new byte[0];
        }

        p.position(0);
        p.put(alaw, 0, inLen);
        st.packet.size(inLen);

        ret = avcodec_send_packet(st.ctx, st.packet);
        if (ret < 0) {
            log.error("ALaw aLawToPcm16 avcodec_send_packet failed ret={} inLen={}", ret, inLen);
            av_packet_unref(st.packet);
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(inLen * 2);

        while (true) {
            ret = avcodec_receive_frame(st.ctx, st.decFrame);
            if (ret == AVERROR_EAGAIN() || ret == AVERROR_EOF()) break;
            if (ret < 0) {
                log.error("ALaw aLawToPcm16 avcodec_receive_frame failed ret={} inLen={}", ret, inLen);
                break;
            }

            byte[] chunk = st.normalizeToPcm16Mono8000(st.decFrame);
            if (chunk.length > 0) baos.write(chunk, 0, chunk.length);

            av_frame_unref(st.decFrame);
        }

        av_packet_unref(st.packet);
        av_frame_unref(st.decFrame);

        return baos.toByteArray();
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
            AVCodec encoder = avcodec_find_encoder(AV_CODEC_ID_PCM_ALAW);
            if (encoder == null) throw new IllegalStateException("FFmpeg pcm_alaw encoder not found");

            AVCodecContext c = avcodec_alloc_context3(encoder);
            if (c == null) throw new IllegalStateException("Failed to alloc AVCodecContext for pcm_alaw encoder");

            c.sample_rate(8000);
            c.sample_fmt(AV_SAMPLE_FMT_S16);

            AVChannelLayout layout = new AVChannelLayout();
            av_channel_layout_default(layout, 1);
            c.ch_layout(layout);

            c.bit_rate(64000);

            int ret = avcodec_open2(c, encoder, (org.bytedeco.ffmpeg.avutil.AVDictionary) null);
            if (ret < 0) throw new IllegalStateException("Cannot open pcm_alaw encoder, ret=" + ret);

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
                    log.error("ALaw ensureFrameCapacity av_frame_get_buffer failed samples={} ret={}", samples, ret);
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
            AVCodec decoder = avcodec_find_decoder(AV_CODEC_ID_PCM_ALAW);
            if (decoder == null) throw new IllegalStateException("FFmpeg pcm_alaw decoder not found");

            AVCodecContext c = avcodec_alloc_context3(decoder);
            if (c == null) throw new IllegalStateException("Failed to alloc AVCodecContext for pcm_alaw decoder");

            c.sample_rate(8000);
            c.request_sample_fmt(AV_SAMPLE_FMT_S16); // hint only

            AVChannelLayout layout = new AVChannelLayout();
            av_channel_layout_default(layout, 1);
            c.ch_layout(layout);

            int ret = avcodec_open2(c, decoder, (org.bytedeco.ffmpeg.avutil.AVDictionary) null);
            if (ret < 0) throw new IllegalStateException("Cannot open pcm_alaw decoder, ret=" + ret);

            AVFrame f = av_frame_alloc();
            if (f == null) throw new IllegalStateException("Failed to alloc AVFrame for decoder");

            AVFrame out = av_frame_alloc();
            if (out == null) throw new IllegalStateException("Failed to alloc AVFrame for swr output");

            AVPacket p = av_packet_alloc();
            if (p == null) throw new IllegalStateException("Failed to alloc AVPacket for decoder");

            return new DecoderState(c, f, out, p);
        }

        byte[] normalizeToPcm16Mono8000(AVFrame inFrame) {
            if (inFrame == null) return new byte[0];

            // Target output
            final int outFmt = AV_SAMPLE_FMT_S16;
            final int outRate = 8000;

            AVChannelLayout outLayout = new AVChannelLayout();
            av_channel_layout_default(outLayout, 1);

            // Input truth
            final int inFmt = inFrame.format();
            final int inRate = (inFrame.sample_rate() > 0) ? inFrame.sample_rate() : 8000;
            final AVChannelLayout inLayout = inFrame.ch_layout();

            // Optional fast-path: if already correct, copy using FFmpeg size helper
            if (inFmt == outFmt && inRate == outRate && inLayout.nb_channels() == 1) {
                IntPointer ls = lineSizePtr;
                ls.put(0, 0);
                int outBytes = av_samples_get_buffer_size(ls, 1, inFrame.nb_samples(), inFmt, 1);
                if (outBytes > 0) {
                    BytePointer d0 = inFrame.data(0);
                    if (d0 != null && !d0.isNull()) {
                        byte[] out = new byte[outBytes];
                        d0.position(0).get(out, 0, outBytes);
                        return out;
                    }
                }
                // If fast-path copy fails for any reason, fall back to SWR below.
            }

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

                if (hasLastLayout) {
                    av_channel_layout_uninit(lastLayout);
                }
                int cpy = av_channel_layout_copy(lastLayout, inLayout);
                hasLastLayout = (cpy >= 0);

                lastInFmt = inFmt;
                lastInRate = inRate;
            }

            // FFmpeg computed capacity
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
    }
}
