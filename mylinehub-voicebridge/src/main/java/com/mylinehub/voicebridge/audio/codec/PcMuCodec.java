/*
 * Auto-formatted + DEEP LOGS:
 *   src/main/java/com/mylinehub/voicebridge/audio/codec/PcMuCodec.java
 */
package com.mylinehub.voicebridge.audio.codec;

import com.mylinehub.voicebridge.audio.AudioCodec;
import com.mylinehub.voicebridge.audio.CodecFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PCMU (G.711 u-law) codec implementation of {@link AudioCodec}.
 *
 * Stateless wrapper around {@link MuLaw}.
 * Asterisk/FreePBX interoperable at 8000 Hz.
 *
 * Deep logs (opt-in):
 *  - Enable: -Dvoicebridge.pcmu.logs=true
 *  - Trace first N calls: -Dvoicebridge.pcmu.traceLimit=8
 */
public final class PcMuCodec implements AudioCodec {

  private static final Logger log = LoggerFactory.getLogger(PcMuCodec.class);

  // Opt-in only
  private static final boolean DEEP_LOGS = false;;

  // How many codec invocations to trace (per JVM)
  private static final int TRACE_LIMIT =
      Integer.parseInt(System.getProperty("voicebridge.pcmu.traceLimit", "0"));

  private static final java.util.concurrent.atomic.AtomicLong ENC_CALLS = new java.util.concurrent.atomic.AtomicLong();
  private static final java.util.concurrent.atomic.AtomicLong DEC_CALLS = new java.util.concurrent.atomic.AtomicLong();

  /** {@inheritDoc} */
  @Override
  public String name() {
    return "pcmu";
  }

  /** {@inheritDoc} */
  @Override
  public int sampleRate() {
    return 8000;
  }

  /**
   * Encodes 16-bit little-endian PCM into u-law bytes.
   *
   * @param pcm PCM16 LE mono bytes
   * @return u-law encoded bytes (G.711)
   */
  @Override
  public byte[] encodePcmToPayload(byte[] pcm) {
    long n = ENC_CALLS.incrementAndGet();
    long t0 = DEEP_LOGS ? System.nanoTime() : 0L;

    if (DEEP_LOGS && n <= TRACE_LIMIT) {
      log.debug("CODEC pcMU encode_begin call={} pcmBytes={} thread={}",
          n, safeLen(pcm), Thread.currentThread().getName());
    }

    byte[] ulaw = MuLaw.pcm16ToMuLaw(pcm);

    if (DEEP_LOGS && n <= TRACE_LIMIT) {
      long us = (System.nanoTime() - t0) / 1000;
      log.debug("CODEC pcMU encode_ok call={} pcmBytes={} ulawBytes={} durUs={}",
          n, safeLen(pcm), safeLen(ulaw), us);
    }

    return ulaw;
  }

  /**
   * Decodes u-law bytes into 16-bit little-endian PCM.
   *
   * @param payload u-law encoded bytes (G.711)
   * @return PCM16 LE mono bytes
   */
  @Override
  public byte[] decodePayloadToPcm(byte[] payload) {
    long n = DEC_CALLS.incrementAndGet();
    long t0 = DEEP_LOGS ? System.nanoTime() : 0L;

    if (DEEP_LOGS && n <= TRACE_LIMIT) {
      log.debug("CODEC pcMU decode_begin call={} ulawBytes={} thread={}",
          n, safeLen(payload), Thread.currentThread().getName());
    }

    byte[] pcm = MuLaw.muLawToPcm16(payload);

    if (DEEP_LOGS && n <= TRACE_LIMIT) {
      long us = (System.nanoTime() - t0) / 1000;
      log.debug("CODEC pcMU decode_ok call={} ulawBytes={} pcmBytes={} durUs={}",
          n, safeLen(payload), safeLen(pcm), us);
    }

    return pcm;
  }

  private static int safeLen(byte[] b) {
    return b == null ? 0 : b.length;
  }
  
  @Override
  public byte[] flushDecoderTailPcm16() {
    return MuLaw.flushDecoder(); // returns PCM16@8k if anything is buffered
  }

  @Override
  public void resetDecoderState() {
    MuLaw.resetDecoder();
  }

}
