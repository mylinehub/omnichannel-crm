package com.mylinehub.voicebridge.ai;

import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.session.CallSession;
import okhttp3.WebSocket;

/**
 * Generic bot audio client contract.
 *
 * VoiceBridge uses this interface for BOTH:
 *  - OpenAI Realtime WebSocket (bot.mode=openai)
 *  - External client bot WebSocket (bot.mode=external)
 *
 * CONCURRENCY GUARANTEE:
 *  - Implementations are Spring singletons.
 *  - They MUST NOT store per-call mutable fields.
 *  - Any per-call state must be stored in CallSession.attrs.
 */
public interface BotClient {

  WebSocket connect(CallSession session,StasisAppConfig configProperties);

  void sendAudioChunk(CallSession session, WebSocket ws, byte[] pcm16);

  default void sendTranscript(WebSocket ws, String text) {
    // no-op for external bots
  }

  default void sendDtmf(CallSession session, WebSocket ws, String digits) {
    // no-op for OpenAI
  }

  default void sendMark(CallSession session, WebSocket ws, String name) {
    // no-op by default
  }

  default void sendStop(CallSession session, WebSocket ws, String reason) {
    close(ws, 1000, reason != null ? reason : "stop");
  }

  default void close(WebSocket ws, int code, String reason) {
    if (ws != null) {
      ws.close(code, reason);
    }
  }
}
