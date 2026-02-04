package com.mylinehub.aiemail.service.impl;

import com.mylinehub.aiemail.service.LoginService;
import com.mylinehub.aiemail.service.RagService;
import com.mylinehub.aiemail.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fetches RAG snippets from MyLineHub vector search.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final WebClient.Builder webClientBuilder;
    private final SystemConfigService configService;
    private final LoginService loginService;

    private static final AtomicLong lastRequestLog = new AtomicLong(0);
    private static final AtomicLong lastResponseLog = new AtomicLong(0);
    private static final long LOG_WINDOW_MS = 60_000;

    @Override
    public Mono<String> fetchContext(String organization, String text) {

        long start = System.currentTimeMillis();

        rateLimitedInfo("[RAG] fetchContext() - org=" + organization + " textPreview=" +
                shortPreview(text) + "...");

        String baseUrl = configService.getRequired("MYLINEHUB_BASE_URL");
        String path = configService.getRequired("RAG_VECTOR_STORE_URL");

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        return loginService.getValidSystemToken()
                .flatMap(token -> client
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path(path)
                                .queryParam("organization", organization)
                                .queryParam("input", text)
                                .build())
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(String[].class)
                        .doOnNext(resp -> rateLimitedInfo("[RAG] Raw response snippet: "
                                + shortPreview(Arrays.toString(resp))))
                )
                .map(snippets -> {
                    if (snippets.length == 0) {
                        log.warn("[RAG] No snippets returned for org={} inputPreview={}",
                                organization, shortPreview(text));
                        return "";
                    }

                    String join = String.join("\n", snippets);
                    log.debug("[RAG] Joined context length={} chars", join.length());
                    return "CONTEXT: " + join;
                })
                .doOnSuccess(ctx -> {
                    long duration = System.currentTimeMillis() - start;
                    log.info("[RAG] fetchContext SUCCESS ({} ms) org={} ctxSize={}",
                            duration, organization, (ctx != null ? ctx.length() : 0));
                })
                .onErrorResume(e -> {
                    long duration = System.currentTimeMillis() - start;
                    log.error("[RAG] ERROR after {} ms org={} msg={}",
                            duration, organization, e.getMessage(), e);
                    return Mono.just("");
                });
    }

    private void rateLimitedInfo(String msg) {
        long now = System.currentTimeMillis();
        if (now - lastRequestLog.get() > LOG_WINDOW_MS) {
            lastRequestLog.set(now);
            log.info(msg);
        } else {
            log.debug(msg);
        }
    }

    private String shortPreview(String value) {
        if (value == null) return "null";
        return (value.length() > 120) ? value.substring(0, 120) + "…" : value;
    }
}
