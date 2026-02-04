package com.mylinehub.voicebridge.ai;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.WebSocket;

/**
 * TruncateManager (per call)
 *
 * Purpose:
 * - Track "what assistant audio item is currently being spoken"
 * - Track "how much audio has actually been played"
 * - On interrupt: cancel + truncate so server state matches what caller heard
 *
 * IMPORTANT:
 * - This interface is mode-agnostic. Implementations differ per AI vendor.
 */
public interface TruncateManager {

  /** Called when assistant audio events arrive (vendor-specific fields inside root). */
  void onAssistantAudioEvent(JsonNode root);

  /** Called by PlayoutScheduler after an audio frame is actually sent to Asterisk. */
  void onFramePlayed(int frameSamples);

  /** Called when we interrupt the assistant (barge/stop). Must cancel + truncate (if supported). */
  void interrupt(WebSocket ws, int sampleRateHz);

  /** Reset item tracking (call end / vendor reset). */
  void reset();
}

