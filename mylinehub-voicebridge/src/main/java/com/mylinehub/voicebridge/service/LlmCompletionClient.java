package com.mylinehub.voicebridge.service;

import reactor.core.publisher.Mono;

public interface LlmCompletionClient {
  Mono<String> runJsonCompletion(String apiKey, String model, String instructions, String input);
}
