package com.mylinehub.voicebridge.dsp;

import dev.onvoid.webrtc.media.audio.AudioProcessing;
import dev.onvoid.webrtc.media.audio.AudioProcessingConfig;
import dev.onvoid.webrtc.media.audio.AudioProcessingStreamConfig;

import java.util.Arrays;

/**
 * WebRtcApmProcessor (per call)
 *
 * Near-end:
 * - processNearEnd(pcm8k) -> cleaned pcm8k
 *
 * Far-end reference (reverse stream):
 * - pushFarEnd(pcm8k) -> updates AEC state
 *
 * IMPORTANT:
 * - Input MUST be PCM16LE mono at a fixed sample rate (telephony: 8000).
 * - Internally splits into 10ms frames.
 */
public final class WebRtcApmProcessor implements AutoCloseable {

  private static final boolean DEEP_LOGS = true;

  private final int rateHz;
  private final AudioProcessing apm;

  private final AudioProcessingStreamConfig inCfg;
  private final AudioProcessingStreamConfig outCfg;

  private final Pcm10msFramer nearFramer;
  private final Pcm10msFramer farFramer;

  private final int bytes10ms;
  private final byte[] tmpNearOut;
  private final byte[] tmpFarOut;

  public WebRtcApmProcessor(int rateHz, boolean aecOn, boolean nsOn, boolean agcOn) {
    if (rateHz <= 0) throw new IllegalArgumentException("rateHz <= 0");

    this.rateHz = rateHz;
    this.apm = new AudioProcessing();

    this.inCfg = new AudioProcessingStreamConfig(rateHz, 1);
    this.outCfg = new AudioProcessingStreamConfig(rateHz, 1);

    this.nearFramer = new Pcm10msFramer(rateHz);
    this.farFramer = new Pcm10msFramer(rateHz);

    this.bytes10ms = nearFramer.bytesPer10ms();

    // We don't assume the exact target buffer size, but allocate safely.
    int target = apm.getTargetBufferSize(inCfg, outCfg);
    int outSize = Math.max(bytes10ms, target);

    this.tmpNearOut = new byte[outSize];
    this.tmpFarOut  = new byte[outSize];

    AudioProcessingConfig cfg = new AudioProcessingConfig();

    cfg.echoCanceller.enabled = aecOn;
    cfg.echoCanceller.enforceHighPassFiltering = true;

    cfg.noiseSuppression.enabled = nsOn;
    cfg.noiseSuppression.level = AudioProcessingConfig.NoiseSuppression.Level.MODERATE;

    cfg.gainControl.enabled = agcOn;

    apm.applyConfig(cfg);

    if (DEEP_LOGS) {
      System.out.println("WebRtcApmProcessor init rateHz=" + rateHz +
          " aecOn=" + aecOn + " nsOn=" + nsOn + " agcOn=" + agcOn +
          " bytes10ms=" + bytes10ms + " targetBuf=" + target);
    }
  }

  public int rateHz() { return rateHz; }

  /**
   * Feed far-end reference audio (the SAME PCM that is being played to the caller),
   * already at telephony rate (8k).
   *
   * delayMs is your estimate of render delay. You can pass 0 if unknown.
   */
  public void pushFarEnd(byte[] pcm16leMono, int delayMs) {
    if (pcm16leMono == null || pcm16leMono.length == 0) return;

    if (delayMs >= 0) apm.setStreamDelayMs(delayMs);

    farFramer.push(pcm16leMono, frame10 -> {
      byte[] src = (frame10.length == bytes10ms) ? frame10 : Arrays.copyOf(frame10, bytes10ms);
      // We ignore rc; if it fails, AEC just won't update for this frame.
      apm.processReverseStream(src, inCfg, outCfg, tmpFarOut);
    });
  }

  /**
   * Process near-end audio (caller).
   * Returns cleaned audio at same rate/duration.
   */
  public byte[] processNearEnd(byte[] pcm16leMono, int delayMs) {
    if (pcm16leMono == null || pcm16leMono.length == 0) return pcm16leMono;

    if (delayMs >= 0) apm.setStreamDelayMs(delayMs);

    final byte[] out = new byte[pcm16leMono.length];
    final int[] outOff = {0};

    nearFramer.push(pcm16leMono, frame10 -> {
      byte[] src = (frame10.length == bytes10ms) ? frame10 : Arrays.copyOf(frame10, bytes10ms);

      int rc = apm.processStream(src, inCfg, outCfg, tmpNearOut);
      if (rc != 0) {
        // Pass-through on failure
        System.arraycopy(src, 0, out, outOff[0], src.length);
        outOff[0] += src.length;
        return;
      }

      // Copy exactly 10ms worth
      System.arraycopy(tmpNearOut, 0, out, outOff[0], bytes10ms);
      outOff[0] += bytes10ms;
    });

    // outOff should match out.length, but keep safe:
    if (outOff[0] == out.length) return out;
    return Arrays.copyOf(out, outOff[0]);
  }

  @Override
  public void close() {
    try { apm.dispose(); } catch (Exception ignore) {}
  }
}
