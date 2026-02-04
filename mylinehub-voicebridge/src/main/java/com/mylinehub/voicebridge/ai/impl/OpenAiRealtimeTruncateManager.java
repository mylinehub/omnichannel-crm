package com.mylinehub.voicebridge.ai.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.mylinehub.voicebridge.ai.TruncateManager;

import okhttp3.WebSocket;

/**
 * OpenAiRealtimeTruncateManager
 *
 * Tracks item_id/content_index from OpenAI Realtime "output_audio.delta" events.
 * Uses playedSamples from playout to compute audio_end_ms.
 *
 * On interrupt:
 * - response.cancel
 * - conversation.item.truncate(item_id, content_index, audio_end_ms)
 */
public final class OpenAiRealtimeTruncateManager implements TruncateManager {

  private static final boolean DEEP_LOGS = true;
  private static final Logger log = LoggerFactory.getLogger(OpenAiRealtimeTruncateManager.class);

  private volatile String currentItemId;
  private volatile int currentContentIndex = 0;
  private volatile long playedSamples = 0;
  private volatile long lastPlayedLogMs = 0;

  @Override
  public void onAssistantAudioEvent(JsonNode root) {
    if (root == null) return;

    // We only track when the event includes item_id.
    // OpenAI's output_audio.delta includes item_id/content_index.
    String itemId = root.path("item_id").asText(null);
    if (itemId == null || itemId.isBlank()) return;

    int contentIndex = root.path("content_index").asInt(0);

    if (!itemId.equals(currentItemId) || contentIndex != currentContentIndex) {
      currentItemId = itemId;
      currentContentIndex = contentIndex;
      playedSamples = 0;

      if (DEEP_LOGS) {
    	  log.info("TRUNCATE item_switch item_id={} content_index={} reset_playedSamples=true",
    	      itemId, contentIndex);
    	}

    }
  }

  @Override
  public void onFramePlayed(int frameSamples) {
    if (frameSamples <= 0) return;
    if (currentItemId == null) return;
    playedSamples += frameSamples;
    
    if (DEEP_LOGS) {
    	  log.info("TRUNCATE played_progress item_id={} content_index={} playedSamples={}",
    			    currentItemId, currentContentIndex, playedSamples);
    	}
  }

  @Override
  public void interrupt(WebSocket ws, int sampleRateHz) {
    if (ws == null) return;

    // Always cancel first (you required both).
    ws.send("{\"type\":\"response.cancel\"}");

    if (currentItemId == null || sampleRateHz <= 0) { reset(); return; }

    long audioEndMs = (playedSamples * 1000L) / sampleRateHz;
    if (audioEndMs < 0) audioEndMs = 0;

    String payload =
        new StringBuilder(220)
            .append("{\"type\":\"conversation.item.truncate\"")
            .append(",\"item_id\":\"").append(escape(currentItemId)).append("\"")
            .append(",\"content_index\":").append(currentContentIndex)
            .append(",\"audio_end_ms\":").append(audioEndMs)
            .append("}")
            .toString();

    if (DEEP_LOGS) {
    	  log.info("TRUNCATE interrupt_send item_id={} content_index={} playedSamples={} audio_end_ms={} sampleRateHz={}",
    	      currentItemId, currentContentIndex, playedSamples, audioEndMs, sampleRateHz);
    	}
    
    ws.send(payload);
    
    if (DEEP_LOGS) {
    	  log.info("TRUNCATE truncate_sent_ok item_id={} content_index={}", currentItemId, currentContentIndex);
    	}

    
    reset();
  }

  @Override
  public void reset() {
	  
    currentItemId = null;
    currentContentIndex = 0;
    playedSamples = 0;
    
    if (DEEP_LOGS) {
    	  log.info("TRUNCATE reset_state");
    	}

  }

  private static String escape(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
