package com.mylinehub.aiemail.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.aiemail.dto.LanguageHeuristicResult;
import com.mylinehub.aiemail.service.LanguageHeuristicService;
import com.mylinehub.aiemail.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Calls OpenAI with a DB-stored prompt template to detect:
 *  - language
 *  - englishTranslation
 *  - various heuristic flags
 *
 * SystemConfig keys used:
 *  - OPENAI_BASE_URL
 *  - OPENAI_MODEL
 *  - OPENAI_API_KEY
 *  - LANGUAGE_HEURISTIC_PROMPT_TEMPLATE  (full instructions for JSON-only response)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageHeuristicServiceImpl implements LanguageHeuristicService {

    private final WebClient.Builder webClientBuilder;
    private final SystemConfigService configService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<LanguageHeuristicResult> analyze(String text) {

        if (text == null || text.isBlank()) {
            // No text -> default result
            return Mono.just(LanguageHeuristicResult.builder()
                    .language("Unknown")
                    .englishTranslation(null)
                    .customerStillWriting(false)
                    .noFurtherTextRequired(false)
                    .calculationRequired(false)
                    .customerMaybeAskingDemoVideo(false)
                    .build());
        }

        String baseUrl = configService.getRequired("OPENAI_BASE_URL");
        String model   = configService.getRequired("OPENAI_MODEL");
        String apiKey  = configService.getRequired("OPENAI_API_KEY");

        String systemPrompt = configService.getRequired("LANGUAGE_HEURISTIC_PROMPT_TEMPLATE");

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        // Build request for chat.completions
        var requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);

        var messages = objectMapper.createArrayNode();

        var sys = objectMapper.createObjectNode();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        messages.add(sys);

        var user = objectMapper.createObjectNode();
        user.put("role", "user");
        user.put("content", text);
        messages.add(user);

        requestBody.set("messages", messages);

        return client.post()
                .uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractResultFromJson)
                .doOnSuccess(res -> log.info("[LangHeuristic] language={} calc={} demo={}",
                        res.getLanguage(), res.getCalculationRequired(), res.getCustomerMaybeAskingDemoVideo()))
                .onErrorResume(e -> {
                    log.error("[LangHeuristic] Error while calling OpenAI: {}", e.getMessage(), e);
                    // Fallback: assume English, no translation
                    return Mono.just(LanguageHeuristicResult.builder()
                            .language("en")
                            .englishTranslation(null)
                            .customerStillWriting(false)
                            .noFurtherTextRequired(false)
                            .calculationRequired(false)
                            .customerMaybeAskingDemoVideo(false)
                            .build());
                });
    }

    /**
     * Extracts JSON content from OpenAI completion and maps to LanguageHeuristicResult.
     */
    private LanguageHeuristicResult extractResultFromJson(String completionJson) {
        try {
            JsonNode root = objectMapper.readTree(completionJson);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.size() == 0) {
                throw new IllegalStateException("No choices in completion");
            }
            JsonNode message = choices.get(0).path("message");
            JsonNode contentNode = message.path("content");
            if (contentNode.isMissingNode()) {
                throw new IllegalStateException("Missing content in completion");
            }

            String content = contentNode.asText();
            // content itself should be a JSON string per your prompt
            JsonNode json = objectMapper.readTree(content);

            LanguageHeuristicResult.LanguageHeuristicResultBuilder builder = LanguageHeuristicResult.builder();

            if (json.has("language")) {
                builder.language(json.path("language").asText());
            }
            if (json.has("englishTranslation") && !json.path("englishTranslation").isNull()) {
                builder.englishTranslation(json.path("englishTranslation").asText());
            }
            if (json.has("customerStillWriting")) {
                builder.customerStillWriting(json.path("customerStillWriting").asBoolean());
            }
            if (json.has("noFurtherTextRequired")) {
                builder.noFurtherTextRequired(json.path("noFurtherTextRequired").asBoolean());
            }
            if (json.has("calculationRequired")) {
                builder.calculationRequired(json.path("calculationRequired").asBoolean());
            }
            if (json.has("customerMaybeAskingDemoVideo")) {
                builder.customerMaybeAskingDemoVideo(json.path("customerMaybeAskingDemoVideo").asBoolean());
            }

            return builder.build();

        } catch (Exception e) {
            log.error("[LangHeuristic] Failed to parse JSON content: {}", e.getMessage(), e);
            // Fall back to English
            return LanguageHeuristicResult.builder()
                    .language("en")
                    .englishTranslation(null)
                    .customerStillWriting(false)
                    .noFurtherTextRequired(false)
                    .calculationRequired(false)
                    .customerMaybeAskingDemoVideo(false)
                    .build();
        }
    }
}
