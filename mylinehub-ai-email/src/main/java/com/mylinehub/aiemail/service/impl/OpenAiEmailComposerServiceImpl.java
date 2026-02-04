package com.mylinehub.aiemail.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.aiemail.service.OpenAiEmailComposerService;
import com.mylinehub.aiemail.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * OpenAI client that calls a mini model to generate email replies.
 *
 * EMAIL INSTRUCTIONS FROM DB (SystemConfig):
 *  - OPENAI_BASE_URL                : e.g. https://api.openai.com
 *  - OPENAI_MODEL                   : e.g. gpt-4o-mini
 *  - OPENAI_API_KEY                 : secret key
 *  - EMAIL_SYSTEM_PROMPT_TEMPLATE   : multi-line template with placeholders:
 *
 *      ${organization}  -> organization name
 *      ${languageCode}  -> language code from heuristic ('en', 'Romanized', 'Mixed', 'Unknown')
 *
 *  Example value:
 *  """
 *  You are an email assistant for the organization '${organization}'.
 *  - Always reply as a human representative of the company.
 *  - Use clear, polite, concise language.
 *  - Never mention that you are an AI or automated system.
 *  - Use the RAG context to answer accurately where possible.
 *  - The detected user language code is: '${languageCode}'.
 *    * If 'en' -> reply in English.
 *    * If 'Romanized' or 'Mixed' -> reply in clear Romanized Hindi (Hindi in English letters).
 *    * If 'Unknown' -> reply in simple English.
 *  At the end of every email, add:
 *  Regards,
 *  ${organization} Team
 *  """
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiEmailComposerServiceImpl implements OpenAiEmailComposerService {

    private final WebClient.Builder webClientBuilder;
    private final SystemConfigService configService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<String> composeReply(String organization,
                                     String inboundSummary,
                                     String ragContext,
                                     String languageCode) {

        String baseUrl = configService.getRequired("OPENAI_BASE_URL");
        String model   = configService.getRequired("OPENAI_MODEL");
        String apiKey  = configService.getRequired("OPENAI_API_KEY");

        // Load email-writing instructions from DB
        String template = configService.getRequired("EMAIL_SYSTEM_PROMPT_TEMPLATE");
        String systemPrompt = applyTemplate(template, organization, languageCode);

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        String userContent = (
                "INBOUND EMAIL SUMMARY (original language):\n"
                + "%s\n\n"
                + "RAG CONTEXT (in English, may be empty, optional):\n"
                + "%s\n\n"
                + "Please draft a professional email reply following the system instructions."
        ).formatted(inboundSummary, ragContext == null ? "" : ragContext);

        var requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);

        var messages = objectMapper.createArrayNode();

        var sys = objectMapper.createObjectNode();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        messages.add(sys);

        var user = objectMapper.createObjectNode();
        user.put("role", "user");
        user.put("content", userContent);
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
                .map(this::extractReplyText)
                .doOnSuccess(text -> log.info("[OpenAI] Reply generated, length={}", text != null ? text.length() : 0))
                .onErrorResume(e -> {
                    log.error("[OpenAI] Error while calling OpenAI: {}", e.getMessage(), e);
                    return Mono.just("We are currently facing a technical issue while generating an automated response. Our team will get back to you shortly.");
                });
    }

    /**
     * Replace placeholders in the system prompt template.
     * Supports:
     *   ${organization}
     *   ${languageCode}
     */
    private String applyTemplate(String template, String organization, String languageCode) {
        if (template == null) return "";
        String result = template
                .replace("${organization}", organization == null ? "" : organization)
                .replace("${languageCode}", languageCode == null ? "Unknown" : languageCode);
        return result;
    }

    /**
     * Extracts the reply content from OpenAI chat completion JSON.
     */
    private String extractReplyText(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path("message");
                JsonNode content = message.path("content");
                if (!content.isMissingNode()) {
                    return content.asText();
                }
            }
        } catch (Exception e) {
            log.error("[OpenAI] Failed to parse completion JSON", e);
        }
        return "Thank you for your email. We will review your request and get back to you shortly.";
    }
}
