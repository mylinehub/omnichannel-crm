package com.mylinehub.voicebridge.barge;

public final class AudioEnergy {

  private AudioEnergy() {}

  // Returns RMS of PCM16LE mono frame.
  public static int rmsPcm16(byte[] pcm16le) {
    if (pcm16le == null || pcm16le.length < 2) return 0;

    long sumSq = 0;
    int samples = pcm16le.length / 2;

    for (int i = 0; i < samples; i++) {
      int lo = pcm16le[i * 2] & 0xff;
      int hi = pcm16le[i * 2 + 1]; // signed
      short s = (short) ((hi << 8) | lo);
      int v = s;
      sumSq += (long) v * v;
    }

    double mean = (samples > 0) ? (sumSq / (double) samples) : 0.0;
    return (int) Math.sqrt(mean);
  }
}
