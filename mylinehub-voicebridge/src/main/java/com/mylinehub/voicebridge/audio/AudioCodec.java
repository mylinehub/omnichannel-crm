/*
 * Auto-formatted + DEEP-LOG GUIDES ONLY:
 * src/main/java/com/mylinehub/voicebridge/audio/AudioCodec.java
 *
 * IMPORTANT:
 * - This is still a pure interface (no behavior changes).
 * - Only documentation and OPTIONAL log helper methods added.
 * - No encoding/decoding logic altered.
 * - Fully ASCII-safe for Windows-1252 editors (STS/Eclipse/IntelliJ).
 *
 * Purpose:
 *   Provides minimal contract for codecs (PCMU, PCMA, Opus, etc.)
 *   AND common debugging helpers so implementations can produce
 *   consistent trace logs during audio troubleshooting.
 */

package com.mylinehub.voicebridge.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;

public interface AudioCodec {

    /*
     =====================================================================
      PER-FILE LOGGING SWITCHES
     =====================================================================

      DEEP_LOGS:
        - Master switch for all non-error logs emitted from the helper
          methods in this interface (encode/decode start/end, previews).
        - Set to true ONLY while debugging codec behavior.

      RTP_DEEP_LOGS:
        - Secondary switch reserved for extremely chatty RTP-packet logs.
        - Not used in this interface yet, but declared for consistency
          with other audio / RTP related classes.

      NOTE:
        - Any ERROR logs (if added by implementations) MUST remain
          always-on and MUST NOT be gated by these flags.
     */

    boolean RTP_DEEP_LOGS = false;

    /*
     =====================================================================
      HIGH-LEVEL DEBUGGING NOTES (for telemetry across all codecs)
     =====================================================================

      Implementations SHOULD emit deep logs at DEBUG or TRACE:

        [CODEC] encode_start codec=PCMU pcmBytes=320
        [CODEC] encode_done codec=PCMU payloadBytes=160 peakAbs=1234

        [CODEC] decode_start codec=PCMU payloadBytes=160
        [CODEC] decode_done codec=PCMU pcmBytes=320 peakAbs=2345

      Peak ABS value helps locate clipping, silence, corruption or drift.

      NEVER log full PCM byte arrays at INFO.
      ONLY safe at TRACE and truncated.
    */

    // -----------------------------------------------------------------
    //  Required interface methods (unchanged)
    // -----------------------------------------------------------------

    /** @return codec name such as "PCMU", "PCMA", "Opus". */
    String name();

    /** @return codec's inherent sample rate (Hz). */
    int sampleRate();

    /**
     * Encode PCM16LE mono to codec payload.
     *
     * @param pcm 16-bit little-endian PCM
     * @return codec-specific RTP payload
     */
    byte[] encodePcmToPayload(byte[] pcm);

    /**
     * Decode codec payload to PCM16LE mono.
     *
     * @param payload RTP payload (codec format)
     * @return decoded PCM bytes (16-bit LE)
     */
    byte[] decodePayloadToPcm(byte[] payload);


    // =====================================================================
    //  OPTIONAL STATIC DEBUG HELPERS (no behavior, only utilities)
    //  Implementers may call these inside encode/decode for consistent logs.
    // =====================================================================

    Logger CODEC_LOG = LoggerFactory.getLogger("AudioCodec");

    /**
     * Compute peak absolute PCM sample (diagnostic only).
     */
    static int peakAbs(byte[] pcm) {
        if (pcm == null || pcm.length < 2) return 0;
        int samples = pcm.length / 2;
        int peak = 0;
        for (int i = 0; i < samples; i++) {
            int lo = pcm[2 * i] & 0xFF;
            int hi = pcm[2 * i + 1];
            short s = (short) ((hi << 8) | lo);
            int a = Math.abs(s);
            if (a > peak) peak = a;
        }
        return peak;
    }

    /**
     * Return first N bytes in hex for TRACE-level debug.
     */
    static String hexPreview(byte[] b, int max) {
        if (b == null) return "null";
        int n = Math.min(max, b.length);
        StringBuilder sb = new StringBuilder(n * 3);
        for (int i = 0; i < n; i++) {
            int v = b[i] & 0xFF;
            if (i > 0) sb.append(' ');
            if (v < 0x10) sb.append('0');
            sb.append(Integer.toHexString(v).toUpperCase());
        }
        if (b.length > n) sb.append(" ...");
        return sb.toString();
    }

    /**
     * Safe DEBUG wrapper for encode entry.
     *
     * Controlled by DEEP_LOGS flag above. When DEEP_LOGS is false,
     * this method returns immediately and NO debug/trace logs are emitted.
     */
    static void logEncodeStart(String codec, byte[] pcm) {
        if (!RTP_DEEP_LOGS) {
            return;
        }
        CODEC_LOG.debug("[CODEC] encode_start codec={} pcmBytes={} peakAbs={}",
                codec,
                (pcm != null ? pcm.length : 0),
                peakAbs(pcm));
        CODEC_LOG.trace("[CODEC] encode_pcm_preview codec={} {}",
                codec, hexPreview(pcm, 32));
    }

    /**
     * Safe DEBUG wrapper for encode exit.
     *
     * Controlled by DEEP_LOGS flag above.
     */
    static void logEncodeDone(String codec, byte[] payload) {
        if (!RTP_DEEP_LOGS) {
            return;
        }
        CODEC_LOG.debug("[CODEC] encode_done codec={} payloadBytes={}",
                codec,
                (payload != null ? payload.length : 0));
        CODEC_LOG.trace("[CODEC] encode_payload_preview codec={} {}",
                codec, hexPreview(payload, 32));
    }

    /**
     * Safe DEBUG wrapper for decode entry.
     *
     * Controlled by DEEP_LOGS flag above.
     */
    static void logDecodeStart(String codec, byte[] payload) {
        if (!RTP_DEEP_LOGS) {
            return;
        }
        CODEC_LOG.debug("[CODEC] decode_start codec={} payloadBytes={}",
                codec,
                (payload != null ? payload.length : 0));
        CODEC_LOG.trace("[CODEC] decode_payload_preview codec={} {}",
                codec, hexPreview(payload, 32));
    }

    /**
     * Safe DEBUG wrapper for decode exit.
     *
     * Controlled by DEEP_LOGS flag above.
     */
    static void logDecodeDone(String codec, byte[] pcm) {
        if (!RTP_DEEP_LOGS) {
            return;
        }
        CODEC_LOG.debug("[CODEC] decode_done codec={} pcmBytes={} peakAbs={}",
                codec,
                (pcm != null ? pcm.length : 0),
                peakAbs(pcm));
        CODEC_LOG.trace("[CODEC] decode_pcm_preview codec={} {}",
                codec, hexPreview(pcm, 32));
    }
    
    /**
     * Flush any buffered decoder tail as PCM16LE mono bytes at codec sample rate.
     * Return empty array if nothing buffered.
     */
    byte[] flushDecoderTailPcm16();

    /**
     * Reset any per-thread/per-call decoder state (ThreadLocal state, SWR, etc).
     */
    void resetDecoderState();
}
