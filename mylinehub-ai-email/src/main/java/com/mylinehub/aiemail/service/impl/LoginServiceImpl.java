package com.mylinehub.aiemail.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.aiemail.service.LoginService;
import com.mylinehub.aiemail.service.SystemConfigService;
import com.mylinehub.aiemail.util.SystemJwtTokenHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation that calls MyLineHub login API
 * with system username/password to get a JWT token.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final WebClient.Builder webClientBuilder;
    private final SystemConfigService configService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final AtomicLong lastLoginRequestLog = new AtomicLong(0);
    private static final AtomicLong lastLoginResponseLog = new AtomicLong(0);
    private static final long LOG_WINDOW_MS = 60_000;

    @Override
    public Mono<String> getValidSystemToken() {

        long now = System.currentTimeMillis();
        if (now - lastLoginRequestLog.get() > LOG_WINDOW_MS) {
            lastLoginRequestLog.set(now);
            log.info("[SystemToken] getValidSystemToken() called...");
        }

        if (SystemJwtTokenHolder.isTokenValid()) {
            log.debug("[SystemToken] Returning cached system token (valid)");
            return Mono.just(SystemJwtTokenHolder.getToken());
        }

        log.info("[SystemToken] Cached token expired - performing new login");
        return loginAndRefreshToken();
    }

    private Mono<String> loginAndRefreshToken() {

        String baseUrl = configService.getRequired("MYLINEHUB_BASE_URL");
        String loginUrl = configService.getRequired("MYLINEHUB_LOGIN_URL");
        String username = configService.getRequired("MYLINEHUB_LOGIN_USERNAME");
        String password = configService.getRequired("MYLINEHUB_LOGIN_PASSWORD");

        log.info("[SystemLogin] Initiating system login - {}{}", baseUrl, loginUrl);

        long start = System.currentTimeMillis();

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        Map<String, String> body = Map.of(
                "username", username,
                "password", password
        );

        return client
                .post()
                .uri(loginUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {

                    long now = System.currentTimeMillis();
                    if (now - lastLoginResponseLog.get() > LOG_WINDOW_MS) {
                        lastLoginResponseLog.set(now);

                        String preview = json.length() > 200
                                ? json.substring(0, 200) + "..."
                                : json;

                        log.info("[SystemLogin] Raw login response (preview 200 chars): {}", preview);
                    }

                    return extractTokenFromLoginResponse(json);
                })
                .doOnNext(token -> {
                    SystemJwtTokenHolder.updateToken(token);
                    long duration = System.currentTimeMillis() - start;
                    log.info("[SystemLogin] SUCCESS - Token refreshed (duration={} ms)", duration);
                })
                .doOnError(e -> {
                    long duration = System.currentTimeMillis() - start;
                    log.error("[SystemLogin] FAILED after {} ms", duration, e);
                });
    }

    private String extractTokenFromLoginResponse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.path("data");
            if (data.isMissingNode() || !data.hasNonNull("token")) {
                log.error("[SystemLogin] Response JSON missing 'data.token'");
                throw new IllegalStateException("No token field in login response");
            }
            String token = data.get("token").asText();

            log.debug("[SystemLogin] Token extracted (not printing)");
            return token;

        } catch (Exception e) {
            log.error("[SystemLogin] Failed to parse login JSON", e);
            throw new RuntimeException("Failed to parse login response JSON", e);
        }
    }
}
