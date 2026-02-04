package com.mylinehub.voicebridge.dsp;

import java.util.Arrays;

/**
 * Pcm10msFramer
 *
 * What it does:
 * - Accepts arbitrary PCM16LE mono bytes.
 * - Outputs exact 10ms frames (rateHz/100 samples, *2 bytes).
 * - Keeps remainder as carry.
 *
 * Why:
 * - WebRTC APM requires 10ms frames for processStream/processReverseStream.
 */
public final class Pcm10msFramer {

  private static final boolean DEEP_LOGS = true;

  public interface FrameConsumer {
    void onFrame10ms(byte[] frame10ms);
  }

  private final int rateHz;
  private final int bytesPer10ms;
  private byte[] carry = new byte[0];

  public Pcm10msFramer(int rateHz) {
    if (rateHz <= 0) throw new IllegalArgumentException("rateHz <= 0");
    this.rateHz = rateHz;

    int samples10ms = rateHz / 100;
    this.bytesPer10ms = samples10ms * 2;

    if (bytesPer10ms <= 0) throw new IllegalArgumentException("bytesPer10ms <= 0");
    if (DEEP_LOGS) {
      System.out.println("Pcm10msFramer init rateHz=" + rateHz + " bytesPer10ms=" + bytesPer10ms);
    }
  }

  public int rateHz() { return rateHz; }
  public int bytesPer10ms() { return bytesPer10ms; }

  public void reset() { carry = new byte[0]; }

  public void push(byte[] pcm16leMono, FrameConsumer consumer) {
    if (pcm16leMono == null || pcm16leMono.length == 0 || consumer == null) return;

    byte[] data;
    if (carry.length > 0) {
      data = new byte[carry.length + pcm16leMono.length];
      System.arraycopy(carry, 0, data, 0, carry.length);
      System.arraycopy(pcm16leMono, 0, data, carry.length, pcm16leMono.length);
      carry = new byte[0];
    } else {
      data = pcm16leMono;
    }

    int off = 0;
    while (off + bytesPer10ms <= data.length) {
      byte[] frame = new byte[bytesPer10ms];
      System.arraycopy(data, off, frame, 0, bytesPer10ms);
      off += bytesPer10ms;
      consumer.onFrame10ms(frame);
    }

    int rem = data.length - off;
    if (rem > 0) {
      carry = Arrays.copyOfRange(data, off, data.length);
    } else {
      carry = new byte[0];
    }
  }
}
