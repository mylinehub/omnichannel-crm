/*
 * OkHttp version + DEEP DIAGNOSTICS:
 *   src/main/java/com/mylinehub/voicebridge/service/impl/DeductAiAmountServiceImpl.java
 *
 * - Uses OkHttp (so CurlInterceptor/OkHttpLoggerUtils can print curl)
 * - Logs status + body even on 401/403/500
 * - Does NOT swallow errors (keeps your retryWhen flow working)
 */

package com.mylinehub.voicebridge.service.impl;

import com.moczul.ok2curl.CurlInterceptor;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.service.DeductAiAmountService;
import com.mylinehub.voicebridge.service.LoginService;
import com.mylinehub.voicebridge.service.StasisAppConfigService;
import com.mylinehub.voicebridge.util.OkHttpLoggerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeductAiAmountServiceImpl implements DeductAiAmountService {

    private static final boolean DEEP_LOGS = true;

    // Dangerous: enable only briefly for debugging
    private static final boolean LOG_RAW_TOKEN = false;

    // If true, prints curl for EVERY request (via CurlInterceptor)
    private static final boolean ENABLE_CURL_LOGS = true;

    private static final int LOG_BODY_MAX = 2000;

    private final LoginService loginService;
    private final StasisAppConfigService configService;

    private static final AtomicLong lastReqLog = new AtomicLong(0);
    private static final long LOG_WINDOW_MS = 60_000;

    // Build one OkHttpClient instance for this service (reused connection pool)
    // If you already have a global OkHttpClient bean, inject it instead and remove this.
    private final OkHttpClient okHttpClient = buildOkHttpClient();

    @Override
    public Mono<Boolean> deductAiAmount(String stasisAppName, String organization, long callDurationSeconds,Boolean dynamicCost,Integer callCost,String callCostMode, String linkId,String customerPhone,boolean redirectChannel,boolean ivrCall) {

        if (isBlank(organization) || callDurationSeconds <= 0) {
            if (DEEP_LOGS) {
                log.debug("[AI-DEDUCT] skip app={} org={} secs={}",
                        safe(stasisAppName), safe(organization), callDurationSeconds);
            }
            return Mono.just(false);
        }

        StasisAppConfig cfg = configService.getConfigOrNull(stasisAppName);
        if (cfg == null) {
            log.error("[AI-DEDUCT] no_config app={} org={} secs={}",
                    safe(stasisAppName), safe(organization), callDurationSeconds);
            return Mono.just(false);
        }

        String baseUrl = cfg.getMylinehub_base_url();
        String deductPath = cfg.getMylinehub_crm_deduct_ai_amount_url();

        if (isBlank(baseUrl) || isBlank(deductPath)) {
            log.error("[AI-DEDUCT] missing_base_or_path app={} baseUrl={} path={}",
                    safe(stasisAppName), safe(baseUrl), safe(deductPath));
            return Mono.just(false);
        }

        // Build full URL with query params
        HttpUrl url = buildUrl(baseUrl, deductPath, organization.trim(), callDurationSeconds,dynamicCost,callCost,callCostMode,linkId,customerPhone,redirectChannel,ivrCall);
        if (url == null) {
            log.error("[AI-DEDUCT] bad_url app={} baseUrl={} path={}",
                    safe(stasisAppName), safe(baseUrl), safe(deductPath));
            return Mono.just(false);
        }

        rateLimitedLog("[AI-DEDUCT] req app=" + safe(stasisAppName)
                + " org=" + safe(organization.trim())
                + " secs=" + callDurationSeconds
                + " url=" + safe(url.toString()));

        final long t0 = System.nanoTime();

        return loginService.getValidSystemToken(cfg)
                .doOnNext(token -> logToken("DEDUCT", stasisAppName, token))
                .flatMap(token -> {
                    Request req = new Request.Builder()
                            .url(url)
                            .post(RequestBody.create(new byte[0], null)) // POST with empty body (same as your WebClient)
                            .header("Accept", "application/json")
                            .header("Authorization", "Bearer " + token)
                            .build();

                    return callOkHttpBoolean(req, stasisAppName, organization.trim(), callDurationSeconds, t0);
                });
    }

    private Mono<Boolean> callOkHttpBoolean(Request req,
                                           String stasisAppName,
                                           String organization,
                                           long secs,
                                           long t0Nanos) {

        return Mono.create(sink -> {
            Call call = okHttpClient.newCall(req);

            // If subscriber cancels, cancel HTTP call
            sink.onCancel(call::cancel);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    long ms = Duration.ofNanos(System.nanoTime() - t0Nanos).toMillis();
                    log.error("[AI-DEDUCT] http_fail app={} org={} secs={} ms={} err={}",
                            safe(stasisAppName), safe(organization), secs, ms, safe(e.getMessage()), e);
                    sink.error(e);
                }

                @Override
                public void onResponse(Call call, Response resp) {
                    long ms = Duration.ofNanos(System.nanoTime() - t0Nanos).toMillis();
                    int code = resp.code();

                    String bodyStr = "";
                    try (ResponseBody rb = resp.body()) {
                        bodyStr = (rb != null ? rb.string() : "");
                    } catch (Exception ex) {
                        bodyStr = "[body_read_error:" + ex.getClass().getSimpleName() + "]";
                    }

                    if (code >= 200 && code < 300) {
                        if (DEEP_LOGS) {
                            log.info("[AI-DEDUCT] resp_ok app={} org={} secs={} status={} ms={} body={}",
                                    safe(stasisAppName), safe(organization), secs, code, ms,
                                    truncate(bodyStr, LOG_BODY_MAX));
                        } else {
                            log.info("[AI-DEDUCT] resp_ok app={} org={} secs={} status={} ms={}",
                                    safe(stasisAppName), safe(organization), secs, code, ms);
                        }

                        String b = bodyStr != null ? bodyStr.trim() : "";
                        if ("true".equalsIgnoreCase(b)) {
                            sink.success(true);
                            return;
                        }
                        if ("false".equalsIgnoreCase(b)) {
                            sink.success(false);
                            return;
                        }

                        sink.error(new IllegalStateException("Unexpected Boolean body: " + truncate(bodyStr, 300)));
                        return;
                    }

                    // Non-2xx: log status + body and propagate error (so retryWhen can retry)
                    log.error("[AI-DEDUCT] resp_err app={} org={} secs={} status={} ms={} body={}",
                            safe(stasisAppName), safe(organization), secs, code, ms,
                            truncate(bodyStr, LOG_BODY_MAX));

                    sink.error(new IllegalStateException("AI-DEDUCT HTTP " + code + " body=" + truncate(bodyStr, 500)));
                }
            });
        });
    }

    private static OkHttpClient buildOkHttpClient() {
        OkHttpClient.Builder b = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS);

        if (ENABLE_CURL_LOGS) {
            // Your existing curl logger
            OkHttpLoggerUtils myLogger = new OkHttpLoggerUtils();
            CurlInterceptor curl = new CurlInterceptor(myLogger);
            b.addInterceptor(curl);
        }

        return b.build();
    }

    private static HttpUrl buildUrl(String baseUrl, String path, String organization, long secs,Boolean dynamicCost,Integer callCost,String callCostMode,String linkId,String customerPhone,boolean redirectChannel,boolean ivrCall) {
        // Normalize: baseUrl + path (path may start with /)
        String full = baseUrl;
        if (full.endsWith("/") && path.startsWith("/")) full = full.substring(0, full.length() - 1);
        else if (!full.endsWith("/") && !path.startsWith("/")) full = full + "/";
        full = full + path;

        HttpUrl parsed = HttpUrl.parse(full);
        if (parsed == null) return null;

        return parsed.newBuilder()
                .addQueryParameter("organization", organization)
                .addQueryParameter("callDurationSeconds", String.valueOf(secs))
                .addQueryParameter("dynamicCost", String.valueOf(dynamicCost))
                .addQueryParameter("callCost", String.valueOf(callCost))
                .addQueryParameter("callCostMode", String.valueOf(callCostMode)) 
                .addQueryParameter("linkId", linkId) 
                .addQueryParameter("customerPhone",customerPhone)
                .addQueryParameter("redirectChannel",String.valueOf(redirectChannel))
                .addQueryParameter("ivrCall",String.valueOf(ivrCall))
                .build();
    }

    // ---------------- helpers ----------------

    private void rateLimitedLog(String msg) {
        long now = System.currentTimeMillis();
        if (now - lastReqLog.get() > LOG_WINDOW_MS) {
            lastReqLog.set(now);
            log.info(msg);
        } else {
            log.debug(msg);
        }
    }

    private void logToken(String op, String app, String token) {
        if (!DEEP_LOGS) return;

        log.info("[AI-DEDUCT] token_ok op={} app={} tokenLen={} tokenHash={}",
                safe(op), safe(app),
                token != null ? token.length() : 0,
                sha256Hex(token));

        if (LOG_RAW_TOKEN) {
            log.warn("[AI-DEDUCT] RAW_TOKEN op={} app={} token={}", safe(op), safe(app), token);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safe(String s) {
        if (s == null) return "null";
        return s.replace("\r", " ").replace("\n", " ").trim();
    }

    private static String truncate(String s, int max) {
        if (s == null) return "null";
        String v = safe(s);
        if (v.length() <= max) return v;
        return v.substring(0, max) + " ...[truncated len=" + v.length() + "]";
    }

    private static String sha256Hex(String s) {
        if (s == null) return "null";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte b : dig) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            return "sha256_err";
        }
    }
}
