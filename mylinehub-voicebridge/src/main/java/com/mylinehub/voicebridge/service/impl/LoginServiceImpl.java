package com.mylinehub.voicebridge.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.service.LoginService;
import com.mylinehub.voicebridge.service.StasisAppConfigService;
import com.mylinehub.voicebridge.util.SystemJwtTokenHolder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final WebClient.Builder webClientBuilder;
//    private final StasisAppConfigService configService;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final long LOG_WINDOW_MS = 60_000;
    private static final AtomicLong lastReqLog = new AtomicLong(0);
    private static final AtomicLong lastRespLog = new AtomicLong(0);

    @Override
    public Mono<String> getValidSystemToken(StasisAppConfig cfg){

        if (cfg == null) {
            log.error("[SystemToken] No config for app={}", cfg.getStasis_app_name());
            return Mono.empty();
        }

        long now = System.currentTimeMillis();
        if (now - lastReqLog.get() > LOG_WINDOW_MS) {
            lastReqLog.set(now);
            log.info("[SystemToken] Request token for {}", cfg.getStasis_app_name());
        }

        if (SystemJwtTokenHolder.isTokenValid(cfg.getStasis_app_name())) {
            return Mono.just(SystemJwtTokenHolder.getToken(cfg.getStasis_app_name()));
        }

        return login(cfg, cfg.getStasis_app_name());
    }

    private Mono<String> login(StasisAppConfig cfg, String stasisAppName) {

        String baseUrl = cfg.getMylinehub_base_url();
        String loginPath = cfg.getMylinehub_login_url();
        String username  = cfg.getMylinehub_login_username();
        String password  = cfg.getMylinehub_login_password();

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        Map<String, String> body = Map.of("username", username, "password", password);

        return client
            .post()
            .uri(loginPath)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String.class)
            .map(json -> extract(json, stasisAppName))
            .doOnNext(token -> {
                SystemJwtTokenHolder.updateToken(stasisAppName, token);
                log.info("[SystemLogin] SUCCESS app={}", stasisAppName);
            })
            .doOnError(e -> log.error("[SystemLogin] ERROR app={} msg={}", stasisAppName, e.getMessage(), e));
    }

    private String extract(String json, String app){
        try {
            JsonNode root = mapper.readTree(json);
            String token = root.path("data").path("token").asText(null);

            if (token == null) throw new RuntimeException("token missing");

            return token;

        } catch (Exception e) {
            log.error("[SystemLogin] Failed to parse JSON for app={} {}", app, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
