/*
 * Auto-formatted: src/main/java/com/mylinehub/voicebridge/audio/PcMaCodec.java
 */
package com.mylinehub.voicebridge.audio.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mylinehub.voicebridge.audio.AudioCodec;

/**
 * G.711 A-law (PCMA) codec implementing the {@link AudioCodec} contract.
 *
 * <p>This codec wraps {@link ALaw} to provide a simple bridge between:
 * <ul>
 *   <li>PCM16 LE mono (used by your AI / audio pipeline)</li>
 *   <li>G.711 A-law bytes (PCMA, RTP payload type 8)</li>
 * </ul>
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>No resampling or frame aggregation; each call is stateless.</li>
 *   <li>{@code encodePcmToPayload}:
 *     <ul>
 *       <li>Input: PCM16 LE mono bytes.</li>
 *       <li>Output: one A-law byte per PCM sample.</li>
 *     </ul>
 *   </li>
 *   <li>{@code decodePayloadToPcm}:
 *     <ul>
 *       <li>Input: A-law bytes.</li>
 *       <li>Output: PCM16 LE mono bytes (2 bytes per sample).</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>Typical telephony sample rate for PCMA is 8000 Hz.</li>
 *   <li>Use {@code CodecFactory} to construct instances with the desired
 *       sample rate (usually 8000).</li>
 * </ul>
 */
public final class PcMaCodec implements AudioCodec {

  // =====================================================================
  // Global logging switch for deep inspection.
  // Set to true to enable DEBUG logs inside this codec.
  // Set to false for zero logging overhead in production.
  // =====================================================================
  private static final boolean DEEP_LOGS = false;

  private static final Logger log = LoggerFactory.getLogger(PcMaCodec.class);

  /** Sample rate in Hz (for example, 8000 for standard G.711 telephony). */
  private final int fs;

  /**
   * Construct a PCMA codec instance for the given sample rate.
   *
   * <p>In classic telephony, fs should be 8000 Hz. However, the interface
   * accepts any integer so it can align with {@link AudioCodec#sampleRate()}.</p>
   *
   * @param fs sample rate in Hz
   */
  public PcMaCodec(int fs) {
    this.fs = fs;

    if (DEEP_LOGS) {
      log.debug("[PcMaCodec] Initialized with sampleRate={} Hz", fs);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String name() {
    // Common RTP/SDP name for A-law is "PCMA".
    return "pcma";
  }

  /** {@inheritDoc} */
  @Override
  public int sampleRate() {
    return fs;
  }

  /**
   * Encode PCM16 LE mono into A-law (PCMA) payload.
   *
   * <p>This is a thin wrapper around {@link ALaw#pcm16ToALaw(byte[])}.</p>
   *
   * @param pcm PCM16 LE mono bytes
   * @return A-law encoded bytes (1 byte per PCM sample)
   */
  @Override
  public byte[] encodePcmToPayload(byte[] pcm) {
    if (pcm == null || pcm.length == 0) {
      if (DEEP_LOGS) {
        log.debug("[PcMaCodec] encodePcmToPayload: empty or null input, returning empty array");
      }
      return new byte[0];
    }

    byte[] result = ALaw.pcm16ToALaw(pcm);

    if (DEEP_LOGS) {
      log.debug(
          "[PcMaCodec] encodePcmToPayload: inputBytes={} outputBytes={}",
          pcm.length,
          result.length);
    }

    return result;
  }

  /**
   * Decode A-law (PCMA) payload into PCM16 LE mono.
   *
   * <p>This is a thin wrapper around {@link ALaw#aLawToPcm16(byte[])}.</p>
   *
   * @param payload A-law encoded bytes
   * @return PCM16 LE mono bytes (2 bytes per sample)
   */
  @Override
  public byte[] decodePayloadToPcm(byte[] payload) {
    if (payload == null || payload.length == 0) {
      if (DEEP_LOGS) {
        log.debug("[PcMaCodec] decodePayloadToPcm: empty or null payload, returning empty array");
      }
      return new byte[0];
    }

    byte[] result = ALaw.aLawToPcm16(payload);

    if (DEEP_LOGS) {
      log.debug(
          "[PcMaCodec] decodePayloadToPcm: inputBytes={} outputBytes={}",
          payload.length,
          result.length);
    }

    return result;
  }
  
  @Override
  public byte[] flushDecoderTailPcm16() {
    return new byte[0]; // PCMA has no SWR tail today
  }

  @Override
  public void resetDecoderState() {
    // no-op
  }


}
