package com.mylinehub.voicebridge.barge;

import com.mylinehub.voicebridge.queue.OutboundQueue;
import com.mylinehub.voicebridge.service.DspService;
import com.mylinehub.voicebridge.session.CallSession;
import okhttp3.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BargeInController {

  private static final Logger log = LoggerFactory.getLogger(BargeInController.class);

  // Turn on while tuning:
  private static final boolean DEEP_LOGS = true;

  private final int sampleRateHz;
  private final int bytesPer10ms;

  private final int startupIgnoreMs;
  private final int cooldownMs;
  private final int confirmMs;
  private final int confirmFrames;
  private final int aiSpeakingDepthMs;
  private final int energyThreshold;

  private final DspService dspService;

  // Per-call state
  private final long createdNs;
  private final long ignoreUntilNs;
  private volatile long nextAllowedNs = 0L;

  private int consecutiveHotFrames = 0;
  private long lastEnergyLogNs = 0L;

  public BargeInController(
      int sampleRateHz,
      int startupIgnoreMs,
      int cooldownMs,
      int confirmMs,
      int aiSpeakingDepthMs,
      int energyThreshold,
      DspService dspService
  ) {
    this.sampleRateHz = sampleRateHz;
    this.startupIgnoreMs = startupIgnoreMs;
    this.cooldownMs = cooldownMs;
    this.confirmMs = confirmMs;
    this.aiSpeakingDepthMs = aiSpeakingDepthMs;
    this.energyThreshold = energyThreshold;
    this.dspService = dspService;

    int samples10ms = sampleRateHz / 100;
    this.bytesPer10ms = samples10ms * 2;

    this.confirmFrames = Math.max(1, (int) Math.ceil(confirmMs / 10.0));

    this.createdNs = System.nanoTime();
    this.ignoreUntilNs = createdNs + (startupIgnoreMs * 1_000_000L);

    if (DEEP_LOGS) {
      log.info("BARGE init rateHz={} startupIgnoreMs={} cooldownMs={} confirmMs={} confirmFrames={} aiSpeakingDepthMs={} energyThr={}",
          sampleRateHz, startupIgnoreMs, cooldownMs, confirmMs, confirmFrames, aiSpeakingDepthMs, energyThreshold);
    }
  }

  /**
   * Called from AriBridgeImpl RTP-IN thread AFTER DSP (near-end).
   * pcm16 must be PCM16LE mono at sampleRateHz.
   */
  public void onNearEndPcm(CallSession session, WebSocket ws, byte[] pcm16, long nowNs) {
    if (session == null || ws == null || pcm16 == null || pcm16.length < 2) return;

    // 1) startup gate
    if (nowNs < ignoreUntilNs) {
      if (DEEP_LOGS && (nowNs - createdNs) > 100_000_000L && (nowNs - lastEnergyLogNs) > 200_000_000L) {
        lastEnergyLogNs = nowNs;
        log.info("BARGE startup_gate channel={} remainingMs={}",
            session.getChannelId(), (ignoreUntilNs - nowNs) / 1_000_000L);
      }
      return;
    }

    // 2) cooldown gate
    if (nowNs < nextAllowedNs) return;

    // 3) AI speaking?
    if (!isAiSpeaking(session, nowNs)) {
      consecutiveHotFrames = 0;
      return;
    }

    // 4) split into 10ms and compute energy
    int off = 0;
    while (off + bytesPer10ms <= pcm16.length) {
      int rms = AudioEnergy.rmsPcm16(slice(pcm16, off, bytesPer10ms));
      off += bytesPer10ms;

      if (DEEP_LOGS && (nowNs - lastEnergyLogNs) > 1_000_000_000L) {
        lastEnergyLogNs = nowNs;
        log.info("BARGE energy channel={} rms={} thr={} depthMs={} lastAiMsAgo={}",
            session.getChannelId(),
            rms,
            energyThreshold,
            (session.getOutboundQueue() != null ? session.getOutboundQueue().depthMs() : -1),
            msAgo(session.getLastAiPcmEnqueuedNs(), nowNs));
      }

      if (rms >= energyThreshold) {
        consecutiveHotFrames++;
        if (consecutiveHotFrames >= confirmFrames) {
          triggerBarge(session, ws, nowNs);
          return; // once per pcm batch
        }
      } else {
        consecutiveHotFrames = 0;
      }
    }
  }

  private boolean isAiSpeaking(CallSession session, long nowNs) {
    OutboundQueue q = session.getOutboundQueue();
    long depth = (q != null ? q.depthMs() : 0L);
    if (depth >= aiSpeakingDepthMs) return true;

    long lastAi = session.getLastAiPcmEnqueuedNs();
    if (lastAi > 0L) {
      long ageNs = nowNs - lastAi;
      return ageNs >= 0 && ageNs <= 200_000_000L; // 200ms
    }
    return false;
  }

  private void triggerBarge(CallSession session, WebSocket ws, long nowNs) {
    OutboundQueue q = session.getOutboundQueue();
    long before = (q != null ? q.depthMs() : -1);

    if (q != null) q.clear();

    // Cancel + truncate (truncate manager sends response.cancel itself)
    if (session.getTruncateManager() != null) {
      session.getTruncateManager().interrupt(ws, session.getCodec().sampleRate());
    } else {
      ws.send("{\"type\":\"response.cancel\"}");
    }

    // Reset per-call barge state
    consecutiveHotFrames = 0;
    nextAllowedNs = nowNs + cooldownMs * 1_000_000L;

    // Recreate APM (your requirement)
    try {
      if (dspService != null) {
        dspService.recreateApmOnBarge(session);
      }
    } catch (Exception e) {
      log.warn("BARGE apm_recreate_failed channel={} msg={}", session.getChannelId(), e.getMessage());
    }

    if (DEEP_LOGS) {
      log.info("BARGE TRIGGERED channel={} depthBeforeMs={} depthAfterMs=0 cooldownMs={}",
          session.getChannelId(), before, cooldownMs);
    }
  }

  private static byte[] slice(byte[] src, int off, int len) {
    byte[] out = new byte[len];
    System.arraycopy(src, off, out, 0, len);
    return out;
  }

  private static long msAgo(long eventNs, long nowNs) {
    if (eventNs <= 0) return -1;
    return Math.max(0, (nowNs - eventNs) / 1_000_000L);
  }
}
