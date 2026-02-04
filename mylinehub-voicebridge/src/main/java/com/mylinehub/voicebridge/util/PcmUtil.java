package com.mylinehub.voicebridge.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * PCM16 LE <-> short[] utilities.
 *
 * Non-stateful, thread-safe. Suitable for real-time pipelines.
 */
public final class PcmUtil {

  private static final Logger log = LoggerFactory.getLogger(PcmUtil.class);

  /**
   * Per-file deep log switch:
   *  - When false: all DEBUG/TRACE/INFO logs are suppressed.
   *  - ERROR logs (if ever added) must remain ungated.
   */
  private static final boolean DEEP_LOGS = false;

  /**
   * RTP-specific deep logs placeholder for this utility.
   * (No RTP-packet-level logging here currently.)
   */
  private static final boolean RTP_DEEP_LOGS = false;

  private PcmUtil() {}

  // =====================================================================
  // Little-endian bytes -> short[]
  // =====================================================================

  /**
   * Convert PCM16 LE bytes into short[] samples.
   */
  public static short[] bytesToShortsLE(byte[] pcm) {
    if (pcm == null || pcm.length < 2) {
      if (DEEP_LOGS) log.debug("pcm_bytesToShorts: empty or null input");
      return new short[0];
    }

    if ((pcm.length & 1) != 0) {
      if (DEEP_LOGS) {
        log.warn("pcm_bytesToShorts: odd byte count={}, trimming last byte", pcm.length);
      }
      byte[] trimmed = new byte[pcm.length - 1];
      System.arraycopy(pcm, 0, trimmed, 0, trimmed.length);
      pcm = trimmed;
    }

    int n = pcm.length / 2;
    short[] out = new short[n];

    for (int i = 0, j = 0; j < n; i += 2, j++) {
      int lo = pcm[i] & 0xFF;
      int hi = pcm[i + 1]; // signed
      out[j] = (short) ((hi << 8) | lo);
    }

    if (DEEP_LOGS) {
      if (log.isDebugEnabled()) {
        log.debug("pcm_bytesToShorts: inBytes={} outSamples={}", pcm.length, n);
      }
      if (log.isTraceEnabled()) {
        dumpSamplePreview("bytesToShorts", out);
      }
    }

    return out;
  }

  // =====================================================================
  // short[] -> little-endian bytes
  // =====================================================================

  /**
   * Convert short[] (samples) into PCM16 LE bytes.
   */
  public static byte[] shortsToBytesLE(short[] samples) {
    if (samples == null || samples.length == 0) {
      if (DEEP_LOGS) log.debug("pcm_shortsToBytes: empty input");
      return new byte[0];
    }

    byte[] out = new byte[samples.length * 2];

    for (int i = 0, j = 0; j < samples.length; j++, i += 2) {
      short s = samples[j];
      out[i]     = (byte) (s & 0xFF);
      out[i + 1] = (byte) ((s >> 8) & 0xFF);
    }

    if (DEEP_LOGS) {
      if (log.isDebugEnabled()) {
        log.debug("pcm_shortsToBytes: inSamples={} outBytes={}", samples.length, out.length);
      }
      if (log.isTraceEnabled()) {
        PcmStats st = analyzePcmLE(out);
        log.trace("pcm_shortsToBytes_stats min={} max={} rms={}", st.min, st.max, st.rms);
        dumpSamplePreview("shortsToBytes", samples);
      }
    }

    return out;
  }

  // =====================================================================
  // Helpers for logging
  // =====================================================================

  /** Analyze PCM16 LE buffer for min/max/RMS. */
  private static PcmStats analyzePcmLE(byte[] pcm) {
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

    return new PcmStats(min, max, Math.sqrt(sumSq / samples));
  }

  /** Print first few samples for debugging. */
  private static void dumpSamplePreview(String tag, short[] s) {
    int preview = Math.min(10, s.length);
    StringBuilder sb = new StringBuilder();
    sb.append(tag).append(" preview=[");
    for (int i = 0; i < preview; i++) {
      sb.append(s[i]).append(i < preview - 1 ? "," : "");
    }
    sb.append("]");
    log.trace(sb.toString());
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
}
