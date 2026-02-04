/*
 * Auto-formatted + DEEP LOGS ONLY: src/main/java/com/mylinehub/voicebridge/rtp/RtpPacketizer.java
 *
 * IMPORTANT:
 * - No logic changes from your current file.
 * - Only added detailed diagnostic logs and a few private log helpers.
 * - All comments and logs are ASCII-only (safe for Windows-1252).
 */
package com.mylinehub.voicebridge.rtp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public class RtpPacketizer {

    private static final Logger log = LoggerFactory.getLogger(RtpPacketizer.class);

    /**
     * Per-file log switches.
     *
     * DEEP_LOGS:
     *  - Gates non-RTP initialization / control logs (INFO/DEBUG/TRACE).
     *
     * RTP_DEEP_LOGS:
     *  - Gates very chatty RTP-per-packet logs (e.g., rtp_out, periodic summaries, hex dumps).
     *
     * ERROR logs:
     *  - Intentionally NOT gated (always visible as per requirement).
     */
    private static final boolean DEEP_LOGS = false;
    private static final boolean RTP_DEEP_LOGS = false;

    private final AtomicInteger pt = new AtomicInteger(); // payload type
    private final int clk;   // clock rate
    private final int ssrc;  // fixed SSRC per call

    private final AtomicInteger seq = new AtomicInteger(1);
    private final AtomicInteger ts  = new AtomicInteger(0);
    private final AtomicInteger packetCount = new AtomicInteger(0);

    // Extra periodic debug (no behavior change)
    private static final int DEBUG_EVERY_N_PACKETS = 200;

    public RtpPacketizer(int initialPt, int clk, int ssrc) {
        this.pt.set(initialPt);
        this.clk  = clk;
        this.ssrc = ssrc;

        if (DEEP_LOGS) {
            log.info("[RTP-PKT] rtp_packetizer_init pt={} clk={} ssrc={} seqStart={} tsStart={}",
                    initialPt, clk, ssrc, seq.get(), ts.get());
        }
    }

    public int payloadType() {
        return pt.get();
    }

    public int clockRate() {
        return clk;
    }

    public int ssrc() {
        return ssrc;
    }

    public void setPayloadType(int newPt) {
        int old = this.pt.getAndSet(newPt);
        if (RTP_DEEP_LOGS) {
            log.info("[RTP-PKT] rtp_packetizer_pt_update oldPt={} newPt={} ssrc={}", old, newPt, ssrc);
        }
    }

    /**
     * Packetize encoded payload into RTP frame.
     *
     * @param payload encoded audio (pcmu/opus)
     * @param samples number of PCM samples represented in this payload
     * @return RTP packet bytes
     */
    public byte[] packetize(byte[] payload, int samples) {
        if (payload == null) {
            // ERROR: must always be visible
            log.error("[RTP-PKT] packetize_payload_null ssrc={} pt={} clk={} samples={}",
                    ssrc, pt.get(), clk, samples);
            throw new IllegalArgumentException("RTP payload cannot be null");
        }

        if (samples <= 0 && DEEP_LOGS) {
            log.debug("[RTP-PKT] packetize_samples_nonpositive ssrc={} pt={} clk={} samples={} payloadBytes={}",
                    ssrc, pt.get(), clk, samples, payload.length);
        }

        int currentPt  = pt.get();
        int currentSeq = seq.get();
        int currentTs  = ts.get();

        if (RTP_DEEP_LOGS) {
            log.trace("[RTP-PKT] packetize_enter ssrc={} pt={} clk={} seq={} ts={} samples={} payloadBytes={}",
                    ssrc, currentPt, clk, currentSeq, currentTs, samples, payload.length);
        }

        ByteBuffer b = ByteBuffer.allocate(12 + payload.length);

        // Header
        b.put((byte) 0x80); // V=2
        b.put((byte) (currentPt & 0x7F)); // M=0 + PT
        b.putShort((short) seq.getAndIncrement());
        b.putInt(ts.getAndAdd(samples));
        b.putInt(ssrc);

        // Payload
        b.put(payload);

        byte[] packet = b.array();
        int count = packetCount.incrementAndGet();

        // Per-packet INFO log: extremely chatty -> gate with RTP_DEEP_LOGS.
        if (RTP_DEEP_LOGS) {
            log.info("[RTP-PKT] rtp_out seq={} ts={} pt={} clk={} payloadBytes={} packetBytes={} totalPackets={} ssrc={}",
                    currentSeq, currentTs, currentPt, clk, payload.length, packet.length, count, ssrc);
        }

        // Periodic debug summary even when not logging every packet
        if (RTP_DEEP_LOGS && (count % DEBUG_EVERY_N_PACKETS) == 0) {
            int nextSeq = seq.get();
            int nextTs  = ts.get();
            log.debug("[RTP-PKT] rtp_out_periodic ssrc={} pt={} clk={} lastSeq={} lastTs={} nextSeq={} nextTs={} samplesLast={} payloadBytesLast={} packetBytesLast={} totalPackets={}",
                    ssrc, currentPt, clk,
                    currentSeq, currentTs,
                    nextSeq, nextTs,
                    samples, payload.length, packet.length, count);
        }

        // Trace header bytes and small hex preview at TRACE only
        if (RTP_DEEP_LOGS) {
            log.trace("[RTP-HDR] header_fields ssrc={} pt={} seq={} ts={} samples={} payloadBytes={} packetBytes={}",
                    ssrc, currentPt, currentSeq, currentTs, samples, payload.length, packet.length);

            log.trace("[RTP-HEX] packet_hex_preview ssrc={} {}",
                    ssrc, hexPreview(packet, 32));
        }

        return packet;
    }

    // ---------------------------------------------------------------------
    // Log-only helpers (no behavior change)
    // ---------------------------------------------------------------------

    /**
     * Hex preview of first N bytes for TRACE debugging.
     * Example: "80 00 12 34 ..."
     */
    private static String hexPreview(byte[] data, int maxBytes) {
        if (data == null) return "null";
        int n = Math.min(maxBytes, data.length);
        StringBuilder sb = new StringBuilder(n * 3);
        for (int i = 0; i < n; i++) {
            int v = data[i] & 0xFF;
            if (i > 0) sb.append(' ');
            if (v < 0x10) sb.append('0');
            sb.append(Integer.toHexString(v).toUpperCase());
        }
        if (data.length > n) sb.append(" ...");
        return sb.toString();
    }
}
