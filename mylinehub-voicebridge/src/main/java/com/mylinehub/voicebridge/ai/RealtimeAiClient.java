package com.mylinehub.voicebridge.ai;

/**
 * Backward-compat wrapper: OpenAI realtime client is a BotClient.
 * 
 * Keep this interface so the rest of your codebase stays stable.
 */
public interface RealtimeAiClient extends BotClient {
  // no extra methods; BotClient already defines everything needed.
}
