/*
 * Auto-formatted + DEEP LOGS ONLY: src/main/java/com/mylinehub/voicebridge/rtp/RtpSymmetricEndpoint.java
 *
 * IMPORTANT:
 * - No logic changes from your current file.
 * - Only added detailed diagnostic logs and a few log-only helpers/fields.
 * - All comments and logs are ASCII-only (safe for Windows-1252).
 */
package com.mylinehub.voicebridge.rtp;

/*
 ASCII Sequence: RTP media path (symmetric RTP)

   Asterisk(ext-media)               VoiceBridge (RTP endpoint)
            |                                     |
            | RTP: payload (pcmu/opus) ---------->|  // listen, learn peer + PT + SSRC
            |                                     |
            |<------------------------------------|  RTP: AI audio frames
            |                                     |
 Notes:
  - First inbound packet "learns" remote IP:port (symmetric RTP).
  - First inbound packet also "learns" PT and SSRC.
  - This class is per-call and MUST NOT be singleton.
  - It does NOT store CallSession; instead caller provides callbacks.
*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class RtpSymmetricEndpoint implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(RtpSymmetricEndpoint.class);

    /**
     * Per-file log switches.
     *
     * DEEP_LOGS:
     *  - Gates general endpoint lifecycle / control logs (bind, start, peer learn, periodic summaries).
     *
     * RTP_DEEP_LOGS:
     *  - Gates per-packet RTP IN/OUT logs and hex dumps (very chatty).
     *
     * ERROR logs:
     *  - Intentionally NOT gated (always visible).
     */
    private static final boolean DEEP_LOGS = false;
    private static final boolean RTP_DEEP_LOGS = false;

    // Additional periodic debug summaries (logs only)
    private static final long DEBUG_EVERY_N_IN_PACKETS  = 200;
    private static final long DEBUG_EVERY_N_OUT_PACKETS = 200;

    private final DatagramSocket sock;
    private volatile InetAddress peerA;
    private volatile int peerP;

    // simple counters for debugging
    private volatile long inboundPackets = 0;
    private volatile long outboundPackets = 0;
    private volatile long inboundBytes = 0;
    private volatile long outboundBytes = 0;

    // log-only header trackers
    private volatile int learnedPtValue = -1;
    private volatile int learnedSsrcValue = 0;
    private volatile int lastSeq = -1;
    private volatile long lastTs = -1;
    private volatile long lastArrivalNs = 0;

    // callbacks provided by caller (AriBridge/CallSession creation)
    private final Consumer<byte[]> payloadCallback;
    private final IntConsumer onLearnedPt;
    private final IntConsumer onLearnedSsrc;

    private final boolean learnPeer;
    private volatile InetSocketAddress fixedPeer; // optional fixed destination

    
    public RtpSymmetricEndpoint(
            String ip,
            int port,
            Consumer<byte[]> payloadCallback,
            IntConsumer onLearnedPt,
            IntConsumer onLearnedSsrc) throws Exception {

        this(ip, port, true, null, payloadCallback, onLearnedPt, onLearnedSsrc);
    }

    
    // NEW: fixed-peer mode constructor
    public RtpSymmetricEndpoint(
            String ip,
            int port,
            boolean learnPeer,
            InetSocketAddress fixedPeer,
            Consumer<byte[]> payloadCallback,
            IntConsumer onLearnedPt,
            IntConsumer onLearnedSsrc) throws Exception {

        this.payloadCallback = payloadCallback;
        this.onLearnedPt = onLearnedPt;
        this.onLearnedSsrc = onLearnedSsrc;

        this.learnPeer = learnPeer;
        this.fixedPeer = fixedPeer;

        this.sock = new DatagramSocket(new InetSocketAddress(InetAddress.getByName(ip), port));
        this.sock.setReuseAddress(true);

        Thread t = new Thread(this::listen, "rtp-listen-" + port);
        t.setDaemon(true);
        t.start();
    }

    // NEW: allow setting peer after creation (useful when extMediaOut tells you ip:port later)
    public void setFixedPeer(InetSocketAddress peer) {
        this.fixedPeer = peer;
        if (peer != null) {
            this.peerA = peer.getAddress();
            this.peerP = peer.getPort();
        }
    }

    private void listen() {
        byte[] buf = new byte[2048];
        DatagramPacket pkt = new DatagramPacket(buf, buf.length);

        boolean learnedPeer = false;
        boolean learnedPt = false;
        boolean learnedSsrc = false;

        if (DEEP_LOGS) {
            log.info("[RTP-EP] listen_loop_enter local={}", sock.getLocalSocketAddress());
        }

        while (!sock.isClosed()) {
            try {
                sock.receive(pkt);
                inboundPackets++;

                int len = pkt.getLength();
                inboundBytes += len;

                if (len < 12) {
                    if (RTP_DEEP_LOGS) {
                        log.debug("[RTP-IN] drop_too_small len={} from={} totalInPackets={}",
                                len, pkt.getSocketAddress(), inboundPackets);
                    }
                    continue;
                }

                byte[] data = pkt.getData();
                int off = pkt.getOffset();
                InetAddress fromA = pkt.getAddress();
                int fromP = pkt.getPort();

             // Learn remote IP:port only if learnPeer=true
                if (learnPeer) {
                    if (!learnedPeer) {
                        peerA = fromA;
                        peerP = fromP;
                        learnedPeer = true;
                        if (RTP_DEEP_LOGS) {
                            log.info("[RTP-IN] rtp_learn_peer addr={} port={} firstPacketLen={}", peerA, peerP, len);
                        }
                    } else {
                        if (peerA != null && (!peerA.equals(fromA) || peerP != fromP)) {
                            if (RTP_DEEP_LOGS) {
                                log.warn("[RTP-IN] peer_changed oldAddr={} oldPort={} newAddr={} newPort={}",
                                        peerA, peerP, fromA, fromP);
                            }
                            peerA = fromA;
                            peerP = fromP;
                        }
                    }
                } else {
                    // fixed-peer mode: do not override peer from incoming packets
                    // still allow receiving if packets arrive; but don't "learn"
                }

                // RTP header byte 1: M + PT
                int b1 = data[off + 1] & 0xFF;
                int pt = b1 & 0x7F;

                if (!learnedPt) {
                    learnedPt = true;
                    learnedPtValue = pt;
                    if (onLearnedPt != null) onLearnedPt.accept(pt);
                    if (DEEP_LOGS) {
                        log.info("[RTP-IN] rtp_learn_pt pt={} from={} totalInPackets={}", pt, pkt.getSocketAddress(), inboundPackets);
                    }
                } else {
                    if (learnedPtValue != pt) {
                        if (DEEP_LOGS) {
                            log.warn("[RTP-IN] pt_drift learnedPt={} nowPt={} seq?={}",
                                    learnedPtValue, pt, peekSeq(data, off));
                        }
                    }
                }

                // SSRC at bytes 8..11 (big endian)
                int ssrc =
                        ((data[off + 8]  & 0xFF) << 24) |
                        ((data[off + 9]  & 0xFF) << 16) |
                        ((data[off + 10] & 0xFF) << 8)  |
                         (data[off + 11] & 0xFF);

                if (!learnedSsrc) {
                    learnedSsrc = true;
                    learnedSsrcValue = ssrc;
                    if (onLearnedSsrc != null) onLearnedSsrc.accept(ssrc);
                    if (RTP_DEEP_LOGS) {
                        log.info("[RTP-IN] rtp_learn_ssrc ssrc={} pt={} from={}", ssrc, pt, pkt.getSocketAddress());
                    }
                } else {
                    if (learnedSsrcValue != ssrc) {
                        if (RTP_DEEP_LOGS) {
                            log.warn("[RTP-IN] ssrc_drift learnedSsrc={} nowSsrc={} pt={} seq={}",
                                    learnedSsrcValue, ssrc, pt, peekSeq(data, off));
                        }
                    }
                }

                // Strip RTP 12-byte header
                int payloadLen = len - 12;
                if (payloadLen <= 0) {
                    if (RTP_DEEP_LOGS) {
                        log.debug("[RTP-IN] drop_no_payload len={} pt={} ssrc={} seq={}",
                                len, pt, ssrc, peekSeq(data, off));
                    }
                    continue;
                }

                byte[] payload = new byte[payloadLen];
                System.arraycopy(data, off + 12, payload, 0, payloadLen);

                // Log-only header reads for debugging
                int seq = peekSeq(data, off);
                long tsVal = peekTs(data, off);

                if (RTP_DEEP_LOGS) {
                    log.debug("[RTP-IN] rtp_in seq={} ts={} pt={} ssrc={} payloadLen={} from={}",
                            seq, tsVal, pt, ssrc, payloadLen, pkt.getSocketAddress());
                }

                // Periodic debug summary
                if (RTP_DEEP_LOGS && (inboundPackets % DEBUG_EVERY_N_IN_PACKETS) == 0) {
                    long nowNs = System.nanoTime();
                    long gapMs = (lastArrivalNs == 0) ? 0 : (nowNs - lastArrivalNs) / 1_000_000L;
                    log.debug("[RTP-IN] rtp_in_periodic count={} bytes={} lastSeq={} seqNow={} lastTs={} tsNow={} pt={} ssrc={} interArrivalMs~={}",
                            inboundPackets, inboundBytes, lastSeq, seq, lastTs, tsVal, pt, ssrc, gapMs);
                }

                // TRACE: header + small hex preview
                if (RTP_DEEP_LOGS) {
                    log.trace("[RTP-HDR-IN] vpxcc={} mptByte={} pt={} seq={} ts={} ssrc={} payloadLen={} totalLen={}",
                            (data[off] & 0xFF), (data[off + 1] & 0xFF), pt, seq, tsVal, ssrc, payloadLen, len);
                    log.trace("[RTP-HEX-IN] packet_hex_preview from={} {}",
                            pkt.getSocketAddress(), hexPreview(data, off, len, 32));
                }

                lastSeq = seq;
                lastTs = tsVal;
                lastArrivalNs = System.nanoTime();

                // Hand payload to caller (ARI -> codec decode -> AI)
                if (payloadCallback != null) payloadCallback.accept(payload);

            } catch (Exception e) {
                if (!sock.isClosed()) {
                    // ERROR: always visible
                    log.error("[RTP-IN] rtp_recv_error inPackets={} inBytes={} peer={} ",
                            inboundPackets, inboundBytes,
                            (peerA != null ? (peerA + ":" + peerP) : "unknown"),
                            e);
                }
            }
        }

        if (DEEP_LOGS) {
            log.info("[RTP-EP] listen_loop_exit local={} inPackets={} outPackets={}",
                    sock.getLocalSocketAddress(), inboundPackets, outboundPackets);
        }
    }

    /**
     * Sends an RTP packet to the learned peer (no-op until peer learned).
     */
    public void send(byte[] rtp) {

        InetAddress destA = null;
        int destP = 0;

        try {
        	
        	 InetSocketAddress fixed = this.fixedPeer;
             if (fixed != null) {
                 destA = fixed.getAddress();
                 destP = fixed.getPort();
             } else {
                 destA = peerA;
                 destP = peerP;
             }

             if (destA == null) {
                 if (RTP_DEEP_LOGS) log.warn("[RTP-OUT] rtp_send_drop_peer_unknown");
                 return;
             }
             if (rtp == null || rtp.length == 0) {
                 if (RTP_DEEP_LOGS) log.warn("[RTP-OUT] rtp_send_drop_empty");
                 return;
             }

             sock.send(new DatagramPacket(rtp, rtp.length, destA, destP));
             outboundPackets++;
             outboundBytes += rtp.length;
             

            if (RTP_DEEP_LOGS) {
                int outSeq = peekSeq(rtp, 0);
                long outTs = peekTs(rtp, 0);
                int outPt = (rtp.length >= 2) ? (rtp[1] & 0x7F) : -1;
                log.debug("[RTP-OUT] rtp_send seq={} ts={} pt={} bytes={} to={}:{} totalOutPackets={}",
                        outSeq, outTs, outPt, rtp.length, destA, destP, outboundPackets);
            }

            if (RTP_DEEP_LOGS && (outboundPackets % DEBUG_EVERY_N_OUT_PACKETS) == 0) {
                log.debug("[RTP-OUT] rtp_out_periodic count={} bytes={} to={}:{} learnedPt={} learnedSsrc={}",
                        outboundPackets, outboundBytes, destA,  destP, learnedPtValue, learnedSsrcValue);
            }

            if (RTP_DEEP_LOGS) {
                log.trace("[RTP-HEX-OUT] packet_hex_preview to={}:{} {}",
                		destA, destP, hexPreview(rtp, 0, rtp.length, 32));
            }

        } catch (Exception e) {
            if (!sock.isClosed()) {
                // ERROR: always visible
            	log.error("[RTP-OUT] rtp_send_error outPackets={} outBytes={} to={}:{} learnedPt={} learnedSsrc={} msg={}",
            	        outboundPackets,
            	        outboundBytes,
            	        (destA != null ? destA.getHostAddress() : "null"),
            	        destP,
            	        learnedPtValue,
            	        learnedSsrcValue,
            	        e.toString(),
            	        e);

            }
        }
    }

    @Override
    public void close() {
        try {
            sock.close();
        } catch (Exception ignore) {
            // ignore
        }
        if (DEEP_LOGS) {
            log.info("[RTP-EP] rtp_socket_closed local={} peer={} inPackets={} inBytes={} outPackets={} outBytes={} learnedPt={} learnedSsrc={}",
                    sock.getLocalSocketAddress(),
                    (peerA != null ? (peerA + ":" + peerP) : "unknown"),
                    inboundPackets, inboundBytes,
                    outboundPackets, outboundBytes,
                    learnedPtValue, learnedSsrcValue);
        }
    }

    // ---------------------------------------------------------------------
    // Log-only helpers (no behavior change)
    // ---------------------------------------------------------------------

    private static int peekSeq(byte[] data, int off) {
        if (data == null || data.length < off + 4) return -1;
        return ((data[off + 2] & 0xFF) << 8) | (data[off + 3] & 0xFF);
    }

    private static long peekTs(byte[] data, int off) {
        if (data == null || data.length < off + 8) return -1;
        return ((long)(data[off + 4] & 0xFF) << 24) |
               ((long)(data[off + 5] & 0xFF) << 16) |
               ((long)(data[off + 6] & 0xFF) << 8)  |
                (long)(data[off + 7] & 0xFF);
    }
    
    public int localPort() {
        return sock.getLocalPort();
    }

    /**
     * Peer is ready if we have a fixedPeer OR we learned peerA:peerP.
     * This is used by PlayoutScheduler to avoid consuming audio before RTP OUT can send.
     */
    public boolean isPeerReady() {
        InetSocketAddress fixed = this.fixedPeer;
        if (fixed != null) {
            return fixed.getAddress() != null && fixed.getPort() > 0;
        }
        return (peerA != null && peerP > 0);
    }

    /** Optional: for debugging */
    public String peerDebug() {
        InetSocketAddress fixed = this.fixedPeer;
        if (fixed != null) return "fixedPeer=" + fixed;
        if (peerA != null && peerP > 0) return "learnedPeer=" + peerA + ":" + peerP;
        return "peer=NOT_READY";
    }

    /**
     * Hex preview of first N bytes for TRACE debugging.
     * Example: "80 00 12 34 ..."
     */
    private static String hexPreview(byte[] data, int off, int len, int maxBytes) {
        if (data == null) return "null";
        int safeLen = Math.min(len, data.length - off);
        int n = Math.min(maxBytes, safeLen);
        StringBuilder sb = new StringBuilder(n * 3);
        for (int i = 0; i < n; i++) {
            int v = data[off + i] & 0xFF;
            if (i > 0) sb.append(' ');
            if (v < 0x10) sb.append('0');
            sb.append(Integer.toHexString(v).toUpperCase());
        }
        if (safeLen > n) sb.append(" ...");
        return sb.toString();
    }
}
