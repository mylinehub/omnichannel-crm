package com.mylinehub.voicebridge.ai.impl;


import com.fasterxml.jackson.databind.JsonNode;
import com.mylinehub.voicebridge.ai.TruncateManager;

import okhttp3.WebSocket;

/**
 * GoogleNoopTruncateManager
 *
 * Placeholder implementation:
 * - We don't know the exact truncate API for your Google Live mode yet.
 * - So interrupt() only cancels playback locally (caller-side) via your queue clear logic,
 *   and this implementation does nothing on WS.
 */
public final class GoogleNoopTruncateManager implements TruncateManager {

  @Override
  public void onAssistantAudioEvent(JsonNode root) { /* no-op */ }

  @Override
  public void onFramePlayed(int frameSamples) { /* no-op */ }

  @Override
  public void interrupt(WebSocket ws, int sampleRateHz) {
    // no-op for now (or send google-specific cancel later)
  }

  @Override
  public void reset() { /* no-op */ }
}
