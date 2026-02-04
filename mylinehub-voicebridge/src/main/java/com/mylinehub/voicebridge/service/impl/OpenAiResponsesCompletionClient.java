package com.mylinehub.voicebridge.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.voicebridge.service.LlmCompletionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiResponsesCompletionClient implements LlmCompletionClient {

  private final WebClient.Builder webClientBuilder;
  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Mono<String> runJsonCompletion(String apiKey, String model, String instructions, String input) {
    if (isBlank(apiKey) || isBlank(model) || isBlank(instructions) || input == null) {
      return Mono.error(new IllegalArgumentException("Missing apiKey/model/instructions/input"));
    }

    WebClient client = webClientBuilder
        .baseUrl("https://api.openai.com")
        .build();

    // Responses API: POST /v1/responses :contentReference[oaicite:1]{index=1}
    Map<String, Object> body = new HashMap<>();
    body.put("model", model);
    body.put("instructions", instructions);
    body.put("input", input);

    // Strongly bias toward deterministic JSON
    body.put("temperature", 0);

    // (Optional) some models support structured JSON configs; keep simple here.

    return client.post()
        .uri("/v1/responses")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + apiKey)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(String.class)
        .timeout(Duration.ofSeconds(60))
        .flatMap(raw -> extractTextOutput(raw))
        .onErrorResume(e -> {
          log.error("[LLM] completion_error msg={}", e.getMessage(), e);
          return Mono.error(e);
        });
  }

  /**
   * Extract "text" from Responses API JSON.
   * We keep this defensive because output shapes can vary by model/tools.
   */
  private Mono<String> extractTextOutput(String rawJson) {
    try {
      JsonNode root = mapper.readTree(rawJson);

      // Common pattern: output[] -> content[] -> text
      JsonNode output = root.path("output");
      if (output.isArray()) {
        for (JsonNode item : output) {
          JsonNode content = item.path("content");
          if (content.isArray()) {
            for (JsonNode c : content) {
              String type = c.path("type").asText("");
              if ("output_text".equals(type) || "text".equals(type)) {
                String text = c.path("text").asText(null);
                if (!isBlank(text)) return Mono.just(text.trim());
              }
            }
          }
        }
      }

      // Fallback: sometimes there is output_text style convenience fields
      String direct = root.path("output_text").asText(null);
      if (!isBlank(direct)) return Mono.just(direct.trim());

      return Mono.just(""); // let validator handle empty
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

  private boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
