/*
 * Auto-formatted: src/main/java/com/mylinehub/voicebridge/audio/AlignedPcmChunker.java
 */
package com.mylinehub.voicebridge.audio;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mylinehub.voicebridge.audio.codec.MuLaw;

/**
 * Aligns PCM16 bytes into transport-friendly chunks.
 *
 * Typical use cases:
 *  - WS streaming where payload length should be a multiple of a frame size.
 *  - 20 ms frames for 8 kHz (G.711) or 48 kHz (Opus) PCM16 mono.
 *
 * Instance configuration:
 *  - ALIGN  = bytes per "frame" (e.g., 20 ms of PCM16 @ sampleRateHz)
 *  - MIN    = minimum chunk size (recommended steady-state, e.g., ~200 ms)
 *  - MAX    = hard cap per chunk (default: 100KB)
 *
 * Stateful per direction/per call.
 * Create a NEW instance per call for isolation.
 *
 * Deep DEEP_LOGS (opt-in):
 *  - Controlled purely by DEEP_DEEP_LOGS in this file (no System properties).
 */
public final class AlignedPcmChunker {

  /**
   * Per-file switches:
   *
   * DEEP_DEEP_LOGS:
   *   - Enables all internal System.out debug traces from this chunker.
   *   - This class uses System.out for diagnostics to avoid SLF4J coupling.
   *
   * RTP_DEEP_DEEP_LOGS:
   *   - Present only for API consistency; not used directly in this class.
   */
  private static final boolean DEEP_LOGS = false;
  private static final boolean RTP_DEEP_LOGS = false;

  private static final Logger log = LoggerFactory.getLogger(AlignedPcmChunker.class);
  
  // How many debug emissions per call to trace (was System property; now constant).
  private static final int TRACE_LIMIT = 4;

  // Hard cap (unchanged from your original)
  private static final int MAX = 100 * 1024;

  // Per-instance configuration
  private final String codecName;
  private final int sampleRateHz;
  private final int frameMs;
  private final int ALIGN; // bytes per frame
  private final int MIN;   // minimum chunk size in bytes (steady state ~= several frames)

  private byte[] buf = new byte[0];

  // ---------------------------------------------------------------------------
  // Constructors
  // ---------------------------------------------------------------------------

  /**
   * Backwards-compatible constructor:
   *  - Assumes 8 kHz, 20 ms frames (G.711-style).
   *  - ALIGN = 320 bytes, MIN = 3200 bytes (same as original behavior).
   */
  public AlignedPcmChunker() {
    this("pcmu-default", 8000, 20);
  }

  /**
   * Construct a chunker for a given codec/sample-rate/frame size.
   *
   * @param codecName     logical codec label (e.g., "pcmu", "pcma", "opus")
   * @param sampleRateHz  PCM sample rate in Hz (e.g., 8000, 16000, 24000, 48000)
   * @param frameMs       frame duration in milliseconds (e.g., 20)
   */
  public AlignedPcmChunker(String codecName, int sampleRateHz, int frameMs) {
    if (sampleRateHz <= 0) {
      throw new IllegalArgumentException("sampleRateHz must be > 0, got " + sampleRateHz);
    }
    if (frameMs <= 0) {
      throw new IllegalArgumentException("frameMs must be > 0, got " + frameMs);
    }

    this.codecName = (codecName != null ? codecName : "unknown");
    this.sampleRateHz = sampleRateHz;
    this.frameMs = frameMs;

    // PCM16 mono: 2 bytes per sample
    int bytesPerSample = 2;
    // approximate frame size in samples for frameMs
    // e.g., 8000 * 20 / 1000 = 160 samples -> 320 bytes
    int frameSamples = (sampleRateHz * frameMs) / 1000;
    if (frameSamples <= 0) {
      frameSamples = 1;
    }

    this.ALIGN = frameSamples * bytesPerSample;

    // Recommended MIN: ~200 ms (10 frames of 20 ms), scaled with frameMs
    // For frameMs=20 => 10 * ALIGN => 3200 bytes @ 8 kHz
    int framesForMin = Math.max(1, 200 / frameMs); // target around 200 ms
    this.MIN = framesForMin * this.ALIGN;

    if (DEEP_LOGS) {
      log.debug("[init] codec=" + this.codecName +
          " fs=" + this.sampleRateHz +
          " frameMs=" + this.frameMs +
          " ALIGN=" + this.ALIGN +
          " MIN=" + this.MIN +
          " MAX=" + MAX);
    }
  }

  /**
   * Convenience constructor when you don't care about codec name.
   */
  public AlignedPcmChunker(int sampleRateHz, int frameMs) {
    this("uplink", sampleRateHz, frameMs);
  }

  // ---------------------------------------------------------------------------
  // Public API
  // ---------------------------------------------------------------------------

  /** Append raw PCM bytes and emit aligned chunks (>=MIN) to sink. */
  public void append(byte[] in, Consumer<byte[]> sink) {
    if (in == null || in.length == 0 || sink == null) return;

    final long t0 = RTP_DEEP_LOGS ? System.nanoTime() : 0L;
    final String thread = RTP_DEEP_LOGS ? Thread.currentThread().getName() : null;

    if (RTP_DEEP_LOGS) {
      log.debug("[append] codec=" + codecName +
          " inLen=" + in.length +
          " bufLen=" + buf.length +
          " ALIGN=" + ALIGN +
          " MIN=" + MIN +
          " thread=" + thread);
    }

    // grow buffer
    byte[] next = new byte[buf.length + in.length];
    System.arraycopy(buf, 0, next, 0, buf.length);
    System.arraycopy(in, 0, next, buf.length, in.length);
    buf = next;

    emitIfPossible(sink);

    if (RTP_DEEP_LOGS) {
      long us = (System.nanoTime() - t0) / 1000;
      log.debug("[append] end codec=" + codecName +
          " newBufLen=" + buf.length +
          " thread=" + thread +
          " durUs=" + us);
    }
  }

  /** Flush steady-state chunks only (>=MIN). Remainder is kept. */
  public void flush(Consumer<byte[]> sink) {
    if (sink == null) return;

    final long t0 = RTP_DEEP_LOGS ? System.nanoTime() : 0L;
    final String thread = RTP_DEEP_LOGS ? Thread.currentThread().getName() : null;

    if (RTP_DEEP_LOGS) {
      log.debug("[flush] begin codec=" + codecName +
          " bufLen=" + buf.length +
          " thread=" + thread);
    }

    emitIfPossible(sink);

    if (RTP_DEEP_LOGS) {
      long us = (System.nanoTime() - t0) / 1000;
      log.debug("[flush] end codec=" + codecName +
          " bufLen=" + buf.length +
          " thread=" + thread +
          " durUs=" + us);
    }
  }

  /**
   * Final flush at stop/close:
   * - emit any >=MIN chunks
   * - then pad remainder to ALIGN multiple with silence and emit (even if <MIN).
   */
  public void flushFinal(Consumer<byte[]> sink) {
    if (sink == null) return;

    final long t0 = RTP_DEEP_LOGS ? System.nanoTime() : 0L;
    final String thread = RTP_DEEP_LOGS ? Thread.currentThread().getName() : null;

    if (RTP_DEEP_LOGS) {
      log.debug("[flushFinal] begin codec=" + codecName +
          " bufLen=" + buf.length +
          " thread=" + thread);
    }

    // 1) normal emission
    emitIfPossible(sink);

    if (buf.length == 0) {
      if (RTP_DEEP_LOGS) {
        long us = (System.nanoTime() - t0) / 1000;
        log.debug("[flushFinal] nothing to pad, end codec=" + codecName +
            " durUs=" + us);
      }
      return;
    }

    // 2) pad to ALIGN
    int rem = buf.length % ALIGN;
    int padLen = (rem == 0) ? 0 : (ALIGN - rem);

    if (RTP_DEEP_LOGS) {
      log.debug("[flushFinal] pad calc codec=" + codecName +
          " rem=" + rem +
          " padLen=" + padLen +
          " oldBufLen=" + buf.length);
    }

    if (padLen > 0) {
      byte[] padded = new byte[buf.length + padLen];
      System.arraycopy(buf, 0, padded, 0, buf.length);
      // last padLen bytes default 0 (silence)
      buf = padded;
    }

    // 3) Emit aligned chunks with MAX enforcement
    int offset = 0;
    int emitCount = 0;

    while (buf.length - offset > 0) {
      int available = buf.length - offset;
      int maxAligned = (MAX / ALIGN) * ALIGN;
      int takeLen = Math.min(available, maxAligned);

      if (takeLen <= 0) break;

      byte[] out = new byte[takeLen];
      System.arraycopy(buf, offset, out, 0, takeLen);
      offset += takeLen;
      sink.accept(out);

      if (RTP_DEEP_LOGS && (emitCount < TRACE_LIMIT)) {
        log.debug("[flushFinal] emit#" + emitCount +
            " codec=" + codecName +
            " aligned=" + out.length +
            " thread=" + thread);
      }
      emitCount++;
    }

    buf = new byte[0];

    if (RTP_DEEP_LOGS) {
      long us = (System.nanoTime() - t0) / 1000;
      log.debug("[flushFinal] end codec=" + codecName +
          " totalEmits=" + emitCount +
          " durUs=" + us);
    }
  }

  // ---------------------------------------------------------------------------
  // Internal helpers
  // ---------------------------------------------------------------------------

  /** Emit aligned chunks while >= MIN exists in buf. */
  private void emitIfPossible(Consumer<byte[]> sink) {
    int offset = 0;
    int emitCount = 0;

    if (RTP_DEEP_LOGS) {
      log.debug("[emitIfPossible] start codec=" + codecName +
          " bufLen=" + buf.length +
          " ALIGN=" + ALIGN +
          " MIN=" + MIN);
    }

    while (buf.length - offset >= MIN) {
      int available = buf.length - offset;

      int takeLen = (available / ALIGN) * ALIGN;
      if (takeLen <= 0) break;

      int maxAligned = (MAX / ALIGN) * ALIGN;
      if (takeLen > maxAligned) {
        takeLen = maxAligned;
      }

      byte[] out = new byte[takeLen];
      System.arraycopy(buf, offset, out, 0, takeLen);
      offset += takeLen;
      sink.accept(out);

      if (RTP_DEEP_LOGS && (emitCount < TRACE_LIMIT)) {
        log.debug("[emitIfPossible] emit#" + emitCount +
            " codec=" + codecName +
            " aligned=" + takeLen +
            " remaining=" + (buf.length - offset));
      }
      emitCount++;
    }

    if (offset > 0) {
      byte[] rem = new byte[buf.length - offset];
      System.arraycopy(buf, offset, rem, 0, rem.length);
      buf = rem;

      if (RTP_DEEP_LOGS) {
        log.debug("[emitIfPossible] compact codec=" + codecName +
            " newBufLen=" + buf.length +
            " emittedChunks=" + emitCount);
      }
    }
  }

}
