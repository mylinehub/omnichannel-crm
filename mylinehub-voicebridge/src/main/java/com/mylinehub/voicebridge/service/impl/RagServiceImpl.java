package com.mylinehub.voicebridge.service.impl;

import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.service.LoginService;
import com.mylinehub.voicebridge.service.RagService;
import com.mylinehub.voicebridge.service.StasisAppConfigService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final WebClient.Builder webClientBuilder;
    private final LoginService loginService;
    private final StasisAppConfigService configService;

    private static final AtomicLong lastReqLog = new AtomicLong(0);
    private static final long LOG_WINDOW_MS = 60_000;

    @Override
    public Mono<String> fetchContext(String stasisAppName, String organization, String text) {

        StasisAppConfig cfg = configService.getConfigOrNull(stasisAppName);
        if (cfg == null) {
            log.error("[RAG] No config for stasisApp={}", stasisAppName);
            return Mono.just("");
        }

        String baseUrl = cfg.getMylinehub_base_url();
        String searchPath = cfg.getRag_vector_store_url();

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        rateLimitedLog("[RAG] fetchContext app=" + stasisAppName +
                " org=" + organization + " input=" + shortString(text));

        return loginService.getValidSystemToken(cfg)
            .flatMap(token -> client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(searchPath)
                        .queryParam("organization", organization)
                        .queryParam("input", text)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(String[].class)
            )
            .map(snippets -> {
                if (snippets.length == 0) return "";
                return "CONTEXT: " + String.join("\n", snippets);
            })
            .onErrorResume(e -> {
                log.error("[RAG] Error app={} msg={}", stasisAppName, e.getMessage(), e);
                return Mono.just("");
            });
    }

    private void rateLimitedLog(String msg) {
        long now = System.currentTimeMillis();
        if (now - lastReqLog.get() > LOG_WINDOW_MS) {
            lastReqLog.set(now);
            log.info(msg);
        } else {
            log.debug(msg);
        }
    }

    private String shortString(String v) {
        if (v == null) return "null";
        return v.length() > 120 ? v.substring(0, 120) + "v" : v;
    }
}
