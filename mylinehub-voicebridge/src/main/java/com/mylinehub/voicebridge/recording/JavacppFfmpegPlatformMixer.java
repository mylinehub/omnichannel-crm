// ============================================================
// FILE: src/main/java/com/mylinehub/voicebridge/recording/JavacppFfmpegFilterMixToMono.java
// ============================================================
package com.mylinehub.voicebridge.recording;

import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVCodecParameters;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avfilter.AVFilter;
import org.bytedeco.ffmpeg.avfilter.AVFilterContext;
import org.bytedeco.ffmpeg.avfilter.AVFilterGraph;
import org.bytedeco.ffmpeg.avfilter.AVFilterInOut;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.PointerPointer;

import java.nio.file.Path;

import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avfilter.*;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avutil.*;

/**
 * JavacppFfmpegFilterMixToMono
 *
 *
 * Inputs:  inWav (mono PCM16LE) + outWav (mono PCM16LE), same sample rate
 * Output:  dstMonoWav (mono PCM16LE)
 *
 * Mix graph:
 *   [in0][in1]amix=inputs=2:weights=0.5 0.5:normalize=0,
 *   aformat=sample_fmts=s16:channel_layouts=mono:sample_rates=SR
 */
public final class JavacppFfmpegPlatformMixer {

    private JavacppFfmpegPlatformMixer() {}

    public static void mixToMono(Path inWav, Path outWav, Path dstMonoWav) throws Exception {
        av_log_set_level(AV_LOG_ERROR);

        Input A = openWavInput(inWav);
        Input B = openWavInput(outWav);

        // Basic validation (keep it strict)
        if (A.ch != 1 || B.ch != 1) throw new IllegalArgumentException("Inputs must be mono");
        if (A.sampleRate != B.sampleRate) throw new IllegalArgumentException("Inputs must have same sample rate");

        final int sr = A.sampleRate;

        // Output WAV (PCM S16LE mono)
        Output out = openWavOutput(dstMonoWav, sr, 1);

        // Filter graph
        FilterGraph fg = buildMixGraph(sr, A.sampleFmt, B.sampleFmt);

        // Decode frames and feed into graph
        AVPacket pktA = av_packet_alloc();
        AVPacket pktB = av_packet_alloc();
        AVFrame  frA  = av_frame_alloc();
        AVFrame  frB  = av_frame_alloc();
        AVFrame  frOut = av_frame_alloc();

        boolean eofA = false;
        boolean eofB = false;

        // We set per-input PTS in "samples" units to help stable amix timing.
        // (Not audio math; only timestamps)
        long ptsSamplesA = 0;
        long ptsSamplesB = 0;

        while (true) {
            boolean progressed = false;

            if (!eofA) {
                int r = av_read_frame(A.fmt, pktA);
                if (r < 0) {
                    eofA = true;
                    // flush decoder A
                    avcodec_send_packet(A.dec, null);
                } else {
                    if (pktA.stream_index() == A.streamIndex) {
                        avcodec_send_packet(A.dec, pktA);
                    }
                    av_packet_unref(pktA);
                }

                while (true) {
                    int rr = avcodec_receive_frame(A.dec, frA);
                    if (rr == 0) {
                        progressed = true;
                        // set pts as "sample index"
                        frA.pts(ptsSamplesA);
                        ptsSamplesA += frA.nb_samples();

                        // push to buffersrc #0
                        int pr = av_buffersrc_add_frame_flags(fg.src0, frA, AV_BUFFERSRC_FLAG_KEEP_REF);
                        if (pr < 0) throw new RuntimeException("buffersrc add (in0) failed: " + pr);
                        av_frame_unref(frA);

                        // pull any available output
                        drainSinkToEncoder(fg.sink, frOut, out);
                    } else if (rr == AVERROR_EAGAIN() || rr == AVERROR_EOF) {
                        break;
                    } else {
                        throw new RuntimeException("decode A failed: " + rr);
                    }
                }
            }

            if (!eofB) {
                int r = av_read_frame(B.fmt, pktB);
                if (r < 0) {
                    eofB = true;
                    // flush decoder B
                    avcodec_send_packet(B.dec, null);
                } else {
                    if (pktB.stream_index() == B.streamIndex) {
                        avcodec_send_packet(B.dec, pktB);
                    }
                    av_packet_unref(pktB);
                }

                while (true) {
                    int rr = avcodec_receive_frame(B.dec, frB);
                    if (rr == 0) {
                        progressed = true;
                        frB.pts(ptsSamplesB);
                        ptsSamplesB += frB.nb_samples();

                        // push to buffersrc #1
                        int pr = av_buffersrc_add_frame_flags(fg.src1, frB, AV_BUFFERSRC_FLAG_KEEP_REF);
                        if (pr < 0) throw new RuntimeException("buffersrc add (in1) failed: " + pr);
                        av_frame_unref(frB);

                        // pull any available output
                        drainSinkToEncoder(fg.sink, frOut, out);
                    } else if (rr == AVERROR_EAGAIN() || rr == AVERROR_EOF) {
                        break;
                    } else {
                        throw new RuntimeException("decode B failed: " + rr);
                    }
                }
            }

            if (eofA && eofB) break;
            if (!progressed && (eofA || eofB)) {
                // If one side ended and we didn't progress, continue looping; other side may still produce.
            }
        }

        // Signal EOF to both filter inputs
        av_buffersrc_add_frame_flags(fg.src0, null, 0);
        av_buffersrc_add_frame_flags(fg.src1, null, 0);

        // Drain everything remaining
        drainSinkToEncoderFully(fg.sink, frOut, out);

        // Flush encoder
        flushEncoder(out);

        // Close output + cleanup
        writeTrailerAndClose(out);
        freeGraph(fg);

        closeInput(A);
        closeInput(B);

        av_packet_free(pktA);
        av_packet_free(pktB);
        av_frame_free(frA);
        av_frame_free(frB);
        av_frame_free(frOut);
    }

    // ============================================================
    // Filter graph: abuffer (x2) -> amix -> aformat -> abuffersink
    // ============================================================

    private static final class FilterGraph {
        AVFilterGraph graph;
        AVFilterContext src0;
        AVFilterContext src1;
        AVFilterContext sink;
    }

    private static FilterGraph buildMixGraph(int sr, int sampleFmtA, int sampleFmtB) {
        // NOTE: WAV PCM typically decodes to s16. But we still build graph robustly by
        // forcing output to s16 mono @ sr (aformat after amix).

        AVFilterGraph graph = avfilter_graph_alloc();
        if (graph == null) throw new RuntimeException("avfilter_graph_alloc failed");

        AVFilter abuffer = avfilter_get_by_name("abuffer");
        AVFilter abuffersink = avfilter_get_by_name("abuffersink");
        if (abuffer == null || abuffersink == null) throw new RuntimeException("filters not found (abuffer/abuffersink)");

        // Build abuffer args:
        // time_base=1/sr:sample_rate=sr:sample_fmt=s16:channel_layout=mono
        // We accept sampleFmt from decoder if it's s16; if not, still pass it in; aformat after amix will fix output.
        String args0 = "time_base=1/" + sr +
                ":sample_rate=" + sr +
                ":sample_fmt=" + sampleFmtName(sampleFmtA) +
                ":channel_layout=mono";

        String args1 = "time_base=1/" + sr +
                ":sample_rate=" + sr +
                ":sample_fmt=" + sampleFmtName(sampleFmtB) +
                ":channel_layout=mono";

        AVFilterContext src0 = new AVFilterContext();
        AVFilterContext src1 = new AVFilterContext();
        AVFilterContext sink = new AVFilterContext();

        if (avfilter_graph_create_filter(src0, abuffer, "in0", args0, null, graph) < 0)
            throw new RuntimeException("create abuffer in0 failed");
        if (avfilter_graph_create_filter(src1, abuffer, "in1", args1, null, graph) < 0)
            throw new RuntimeException("create abuffer in1 failed");
        if (avfilter_graph_create_filter(sink, abuffersink, "out", null, null, graph) < 0)
            throw new RuntimeException("create abuffersink failed");

        // Filter chain:
        // [in0][in1]amix=inputs=2:weights=0.5 0.5:normalize=0,
        // aformat=sample_fmts=s16:channel_layouts=mono:sample_rates=sr
        String filterDesc =
                "amix=inputs=2:weights=0.5 0.5:normalize=0," +
                "aformat=sample_fmts=s16:channel_layouts=mono:sample_rates=" + sr;

        AVFilterInOut outputs = avfilter_inout_alloc();
        AVFilterInOut inputs  = avfilter_inout_alloc();
        if (outputs == null || inputs == null) throw new RuntimeException("inout alloc failed");

        // outputs describe what is coming OUT of our source pads
        outputs.name(av_strdup(new BytePointer("in0")));
        outputs.filter_ctx(src0);
        outputs.pad_idx(0);
        outputs.next(null);

        AVFilterInOut outputs2 = avfilter_inout_alloc();
        outputs2.name(av_strdup(new BytePointer("in1")));
        outputs2.filter_ctx(src1);
        outputs2.pad_idx(0);
        outputs2.next(null);

        // Chain outputs list: outputs -> outputs2
        outputs.next(outputs2);

        // inputs describe what is going INTO our sink pad
        inputs.name(av_strdup(new BytePointer("out")));
        inputs.filter_ctx(sink);
        inputs.pad_idx(0);
        inputs.next(null);

        int pr = avfilter_graph_parse_ptr(graph, filterDesc, inputs, outputs, null);
        if (pr < 0) throw new RuntimeException("graph_parse failed: " + pr);

        int cr = avfilter_graph_config(graph, null);
        if (cr < 0) throw new RuntimeException("graph_config failed: " + cr);

        // Free inout structures (graph holds references)
        avfilter_inout_free(inputs);
        avfilter_inout_free(outputs);

        FilterGraph fg = new FilterGraph();
        fg.graph = graph;
        fg.src0 = src0;
        fg.src1 = src1;
        fg.sink = sink;
        return fg;
    }

    private static void freeGraph(FilterGraph fg) {
        try { avfilter_graph_free(fg.graph); } catch (Exception ignore) {}
    }

    private static String sampleFmtName(int sampleFmt) {
        // Use FFmpeg’s name helper
        BytePointer p = av_get_sample_fmt_name(sampleFmt);
        if (p == null) {
            // fallback to s16 (safe)
            return "s16";
        }
        return p.getString();
    }

    // ============================================================
    // Drain sink -> encode
    // ============================================================

    private static void drainSinkToEncoder(AVFilterContext sink, AVFrame outFrame, Output out) throws Exception {
        while (true) {
            int r = av_buffersink_get_frame(sink, outFrame);
            if (r == 0) {
                writeAudioFrame(out, outFrame);
                av_frame_unref(outFrame);
            } else if (r == AVERROR_EAGAIN() || r == AVERROR_EOF) {
                break;
            } else {
                throw new RuntimeException("buffersink_get_frame failed: " + r);
            }
        }
    }

    private static void drainSinkToEncoderFully(AVFilterContext sink, AVFrame outFrame, Output out) throws Exception {
        while (true) {
            int r = av_buffersink_get_frame(sink, outFrame);
            if (r == 0) {
                writeAudioFrame(out, outFrame);
                av_frame_unref(outFrame);
            } else if (r == AVERROR_EOF) {
                break;
            } else if (r == AVERROR_EAGAIN()) {
                // continue until EOF after EOF signal pushed
                continue;
            } else {
                throw new RuntimeException("buffersink_get_frame failed: " + r);
            }
        }
    }

    // ============================================================
    // WAV input (decode)
    // ============================================================

    private static final class Input {
        AVFormatContext fmt;
        AVCodecContext dec;
        int streamIndex;
        int sampleRate;
        int ch;
        int sampleFmt;
    }

    private static Input openWavInput(Path wav) {
        AVFormatContext fmt = avformat_alloc_context();
        if (avformat_open_input(fmt, wav.toAbsolutePath().toString(), null, (AVDictionary) null) < 0)
            throw new RuntimeException("open_input failed: " + wav);

        if (avformat_find_stream_info(fmt, (PointerPointer<?>) null) < 0)
            throw new RuntimeException("find_stream_info failed: " + wav);

        int si = av_find_best_stream(fmt, AVMEDIA_TYPE_AUDIO, -1, -1, (AVCodec) null, 0);
        if (si < 0) throw new RuntimeException("no audio stream: " + wav);

        AVStream st = fmt.streams(si);
        AVCodecParameters par = st.codecpar();
        AVCodec codec = avcodec_find_decoder(par.codec_id());
        if (codec == null) throw new RuntimeException("decoder not found: " + wav);

        AVCodecContext dec = avcodec_alloc_context3(codec);
        if (avcodec_parameters_to_context(dec, par) < 0)
            throw new RuntimeException("parameters_to_context failed: " + wav);
        if (avcodec_open2(dec, codec, (AVDictionary) null) < 0)
            throw new RuntimeException("open decoder failed: " + wav);

        Input in = new Input();
        in.fmt = fmt;
        in.dec = dec;
        in.streamIndex = si;
        in.sampleRate = dec.sample_rate();
        in.ch = dec.ch_layout().nb_channels();
        in.sampleFmt = dec.sample_fmt();
        return in;
    }

    private static void closeInput(Input in) {
        try { avcodec_free_context(in.dec); } catch (Exception ignore) {}
        try { avformat_close_input(in.fmt); } catch (Exception ignore) {}
    }

    // ============================================================
    // WAV output (encode/mux)
    // ============================================================

    private static final class Output {
        AVFormatContext fmt;
        AVCodecContext enc;
        AVStream st;
    }

    private static Output openWavOutput(Path dst, int sampleRate, int channels) {
        AVFormatContext outFmt = new AVFormatContext(null);
        if (avformat_alloc_output_context2(outFmt, null, null, dst.toAbsolutePath().toString()) < 0)
            throw new RuntimeException("alloc_output_context failed: " + dst);

        AVCodec codec = avcodec_find_encoder(AV_CODEC_ID_PCM_S16LE);
        if (codec == null) throw new RuntimeException("PCM encoder not found");

        AVStream st = avformat_new_stream(outFmt, codec);
        if (st == null) throw new RuntimeException("new_stream failed: " + dst);

        AVCodecContext enc = avcodec_alloc_context3(codec);
        enc.sample_rate(sampleRate);
        enc.ch_layout().nb_channels(channels);
        enc.sample_fmt(AV_SAMPLE_FMT_S16);
        enc.time_base().num(1);
        enc.time_base().den(sampleRate);

        if ((outFmt.oformat().flags() & AVFMT_GLOBALHEADER) != 0) {
            enc.flags(enc.flags() | AV_CODEC_FLAG_GLOBAL_HEADER);
        }

        if (avcodec_open2(enc, codec, (AVDictionary) null) < 0)
            throw new RuntimeException("open encoder failed");

        if (avcodec_parameters_from_context(st.codecpar(), enc) < 0)
            throw new RuntimeException("parameters_from_context failed");

        if ((outFmt.oformat().flags() & AVFMT_NOFILE) == 0) {
            if (avio_open(outFmt.pb(), dst.toAbsolutePath().toString(), AVIO_FLAG_WRITE) < 0)
                throw new RuntimeException("avio_open failed: " + dst);
        }

        if (avformat_write_header(outFmt, (AVDictionary) null) < 0)
            throw new RuntimeException("write_header failed: " + dst);

        Output out = new Output();
        out.fmt = outFmt;
        out.enc = enc;
        out.st = st;
        return out;
    }

    private static void writeTrailerAndClose(Output out) {
        try { av_write_trailer(out.fmt); } catch (Exception ignore) {}
        try {
            if ((out.fmt.oformat().flags() & AVFMT_NOFILE) == 0) avio_closep(out.fmt.pb());
        } catch (Exception ignore) {}
        try { avcodec_free_context(out.enc); } catch (Exception ignore) {}
        try { avformat_free_context(out.fmt); } catch (Exception ignore) {}
    }

    private static void writeAudioFrame(Output out, AVFrame frame) throws Exception {
        AVPacket pkt = av_packet_alloc();
        if (pkt == null) throw new RuntimeException("av_packet_alloc failed");

        if (avcodec_send_frame(out.enc, frame) < 0)
            throw new RuntimeException("send_frame failed");

        while (true) {
            int r = avcodec_receive_packet(out.enc, pkt);
            if (r == 0) {
                pkt.stream_index(out.st.index());
                av_packet_rescale_ts(pkt, out.enc.time_base(), out.st.time_base());
                if (av_interleaved_write_frame(out.fmt, pkt) < 0) {
                    av_packet_unref(pkt);
                    av_packet_free(pkt);
                    throw new RuntimeException("write_frame failed");
                }
                av_packet_unref(pkt);
            } else if (r == AVERROR_EAGAIN() || r == AVERROR_EOF) {
                break;
            } else {
                break;
            }
        }

        av_packet_free(pkt);
    }

    private static void flushEncoder(Output out) throws Exception {
        AVPacket pkt = av_packet_alloc();
        if (pkt == null) return;

        avcodec_send_frame(out.enc, null);
        while (true) {
            int r = avcodec_receive_packet(out.enc, pkt);
            if (r == 0) {
                pkt.stream_index(out.st.index());
                av_packet_rescale_ts(pkt, out.enc.time_base(), out.st.time_base());
                av_interleaved_write_frame(out.fmt, pkt);
                av_packet_unref(pkt);
            } else {
                break;
            }
        }
        av_packet_free(pkt);
    }
}
