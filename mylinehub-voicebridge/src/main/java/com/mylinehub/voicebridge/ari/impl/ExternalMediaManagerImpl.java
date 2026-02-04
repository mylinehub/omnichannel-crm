/*
 * File: src/main/java/com/mylinehub/voicebridge/ari/impl/ExternalMediaManagerImpl.java
 */
package com.mylinehub.voicebridge.ari.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moczul.ok2curl.CurlInterceptor;
import com.mylinehub.voicebridge.ari.ExternalMediaManager;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.util.OkHttpLoggerUtils;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Component
public class ExternalMediaManagerImpl implements ExternalMediaManager {

    private static final Logger log = LoggerFactory.getLogger(ExternalMediaManagerImpl.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json");

    private static final boolean DEEP_LOGS = false;
    private static final int TRACE_LIMIT = 512;

    private final OkHttpClient client;

    public ExternalMediaManagerImpl() {
        this.client =
                new OkHttpClient.Builder()
                        .addInterceptor(new CurlInterceptor(new OkHttpLoggerUtils()))
                        .connectTimeout(Duration.ofSeconds(5))
                        .readTimeout(Duration.ofSeconds(10))
                        .build();
    }

    // ---------------------------------------------------------------------
    // Auth + cfg helpers
    // ---------------------------------------------------------------------

    private static String nullToEmpty(String s) {
        return (s == null) ? "" : s;
    }

    private static String baseUrl(StasisAppConfig cfg) {
        String b = nullToEmpty(cfg != null ? cfg.getAri_rest_baseUrl() : "").trim();
        // remove trailing slash to keep path joining stable
        while (b.endsWith("/")) b = b.substring(0, b.length() - 1);
        return b;
    }

    private static String user(StasisAppConfig cfg) {
        return nullToEmpty(cfg != null ? cfg.getAri_username() : "");
    }

    private static String pass(StasisAppConfig cfg) {
        return nullToEmpty(cfg != null ? cfg.getAri_password() : "");
    }

    private String basic(String user, String pw) {
        String token = Base64.getEncoder()
                .encodeToString((nullToEmpty(user) + ":" + nullToEmpty(pw)).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    private static HttpUrl mustUrl(String url, String opName) {
        HttpUrl u = HttpUrl.parse(url);
        if (u == null) throw new IllegalArgumentException(opName + " invalid url=" + url);
        return u;
    }

    private static String shorten(String s) {
        if (s == null) return "null";
        if (s.length() <= TRACE_LIMIT) return s;
        return s.substring(0, TRACE_LIMIT) + "...(len=" + s.length() + ")";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ---------------------------------------------------------------------
    // Execute Request (blocking OkHttp, wrapped in Mono on boundedElastic)
    // ---------------------------------------------------------------------

    private String executeRequest(Request request, String opName) throws IOException {
        final long t0 = DEEP_LOGS ? System.nanoTime() : 0L;
        final String thread = Thread.currentThread().getName();

        if (DEEP_LOGS) {
            log.debug("{}_begin method={} url={} thread={}",
                    opName, request.method(), request.url(), thread);
        }

        try (Response resp = client.newCall(request).execute()) {
            String body = resp.body() != null ? resp.body().string() : "";
            String bodyShort = shorten(body);

            if (!resp.isSuccessful()) {
                // ERROR logs are always visible
                log.error("{}_error status={} url={} bodyShort={} thread={}",
                        opName, resp.code(), request.url(), bodyShort, thread);
                if (DEEP_LOGS) {
                    log.debug("{}_error_body_full status={} url={} bodyFull={}",
                            opName, resp.code(), request.url(), body);
                }
                throw new RuntimeException("ARI " + opName + " failed with status " + resp.code());
            }

            if (DEEP_LOGS) {
                long us = (System.nanoTime() - t0) / 1000;
                log.debug("{}_ok status={} url={} bodyShort={} durUs={} thread={}",
                        opName, resp.code(), request.url(), bodyShort, us, thread);
            }

            return body;
        }
    }

    /**
     * Same as executeRequest, but treats 404 as "already gone" and returns empty body.
     * (Useful for cleanup paths.)
     */
    private String executeRequestAllow404(Request request, String opName) throws IOException {
        final String thread = Thread.currentThread().getName();

        try (Response resp = client.newCall(request).execute()) {
            String body = resp.body() != null ? resp.body().string() : "";
            if (resp.code() == 404) {
                if (DEEP_LOGS) {
                    log.warn("{}_404_ok url={} thread={}", opName, request.url(), thread);
                }
                return "";
            }
            if (!resp.isSuccessful()) {
                log.error("{}_error status={} url={} bodyShort={} thread={}",
                        opName, resp.code(), request.url(), shorten(body), thread);
                throw new RuntimeException("ARI " + opName + " failed with status " + resp.code());
            }
            return body;
        }
    }

    // ---------------------------------------------------------------------
    // Interface methods
    // ---------------------------------------------------------------------

    @Override
    public Mono<String> createBridge(StasisAppConfig cfg, String type, String name) {
        final String base = baseUrl(cfg);
        final String t = (type == null || type.isBlank()) ? "mixing" : type.trim();
        final String n = (name == null) ? "" : name.trim();

        String url = base + "/bridges";

        // ARI accepts {"type":"mixing","name":"X"} (name optional)
        final String bodyJson = n.isEmpty()
                ? "{\"type\":\"" + escapeJson(t) + "\"}"
                : "{\"type\":\"" + escapeJson(t) + "\",\"name\":\"" + escapeJson(n) + "\"}";

        if (DEEP_LOGS) {
            log.info("ari_create_bridge_request url={} type={} name={}", url, t, n);
            log.debug("ari_create_bridge_payload {}", bodyJson);
        }

        Request req = new Request.Builder()
                .url(mustUrl(url, "ari_create_bridge"))
                .addHeader("Authorization", basic(user(cfg), pass(cfg)))
                .post(RequestBody.create(bodyJson, JSON))
                .build();

        return Mono.fromCallable(() -> executeRequest(req, "ari_create_bridge"))
                .map(body -> {
                    try {
                        JsonNode n0 = MAPPER.readTree(body);
                        String id = n0.path("id").asText(null);
                        if (id != null && !id.isEmpty()) return id;
                    } catch (Exception ex) {
                        if (DEEP_LOGS) {
                            log.warn("ari_create_bridge_parse_error msg={} bodyShort={}",
                                    ex.getMessage(), shorten(body));
                        }
                    }
                    return body;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> addChannelToBridge(StasisAppConfig cfg, String bridgeId, String channelId) {
        if (bridgeId == null || bridgeId.isBlank() || channelId == null || channelId.isBlank()) {
            return Mono.empty();
        }

        final String base = baseUrl(cfg);

        // Use HttpUrl builder so channelId is properly encoded
        HttpUrl url = mustUrl(base + "/bridges/" + bridgeId + "/addChannel", "ari_add_channel")
                .newBuilder()
                .addQueryParameter("channel", channelId)
                .build();

        if (DEEP_LOGS) {
            log.info("ari_add_channel_request url={} bridgeId={} channelId={}", url, bridgeId, channelId);
        }

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", basic(user(cfg), pass(cfg)))
                .post(RequestBody.create(new byte[0], null))
                .build();

        return Mono.fromCallable(() -> attemptAddChannel(req, bridgeId, channelId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private boolean attemptAddChannel(Request req, String bridgeId, String channelId) throws Exception {
        final int maxAttempts = 3;
        final long delayMs = 100;
        final String thread = Thread.currentThread().getName();

        if (DEEP_LOGS) {
            log.debug("ari_add_channel_attempts_begin maxAttempts={} delayMs={} bridgeId={} channelId={} thread={}",
                    maxAttempts, delayMs, bridgeId, channelId, thread);
        }

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            final long t0 = DEEP_LOGS ? System.nanoTime() : 0L;

            try (Response resp = client.newCall(req).execute()) {
                String body = resp.body() != null ? resp.body().string() : "";
                String bodyShort = shorten(body);

                // 422 retry (bridge not ready yet / race)
                if (resp.code() == 422) {
                    if (DEEP_LOGS) {
                        log.warn("ari_add_channel_retry_needed attempt={} status=422 bridgeId={} channelId={} bodyShort={} thread={}",
                                attempt, bridgeId, channelId, bodyShort, thread);
                    }
                    if (attempt < maxAttempts) {
                        Thread.sleep(delayMs);
                        continue;
                    }
                }

                if (!resp.isSuccessful()) {
                    log.error("ari_add_channel_error attempt={} status={} bridgeId={} channelId={} bodyShort={} thread={}",
                            attempt, resp.code(), bridgeId, channelId, bodyShort, thread);
                    if (DEEP_LOGS) {
                        log.debug("ari_add_channel_error_body_full attempt={} bodyFull={}", attempt, body);
                    }
                    throw new RuntimeException("ARI addChannel failed status=" + resp.code());
                }

                if (DEEP_LOGS) {
                    long durUs = (System.nanoTime() - t0) / 1000;
                    log.info("ari_add_channel_ok attempt={} bridgeId={} channelId={} status={} durUs={} thread={}",
                            attempt, bridgeId, channelId, resp.code(), durUs, thread);
                }

                return true;
            }
        }

        throw new RuntimeException("ARI addChannel failed after retries");
    }

    @Override
    public Mono<String> createSnoopInbound(StasisAppConfig cfg, String callerChannelId, String app, String snoopId) {
        if (callerChannelId == null || callerChannelId.isBlank()) {
            return Mono.error(new IllegalArgumentException("callerChannelId is blank"));
        }

        final String base = baseUrl(cfg);
        final String appVal = nullToEmpty(app).trim();
        final String snoopVal = nullToEmpty(snoopId).trim();

        // ARI: POST /channels/{callerChannelId}/snoop?app=...&spy=in&whisper=none&snoopId=...
        HttpUrl url = mustUrl(base + "/channels/" + callerChannelId + "/snoop", "ari_create_snoop")
                .newBuilder()
                .addQueryParameter("app", appVal)
                .addQueryParameter("spy", "in")
                .addQueryParameter("whisper", "none")
                .addQueryParameter("snoopId", snoopVal)
                .build();

        if (DEEP_LOGS) {
            log.info("ari_create_snoop_request url={} callerChannelId={} app={} snoopId={}",
                    url, callerChannelId, appVal, snoopVal);
        }

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", basic(user(cfg), pass(cfg)))
                .post(RequestBody.create(new byte[0], null))
                .build();

        return Mono.fromCallable(() -> executeRequest(req, "ari_create_snoop"))
                .map(body -> {
                    try {
                        JsonNode n0 = MAPPER.readTree(body);
                        String id = n0.path("id").asText(null);
                        if (id != null && !id.isEmpty()) return id;
                    } catch (Exception ex) {
                        if (DEEP_LOGS) {
                            log.warn("ari_create_snoop_parse_error msg={} bodyShort={}",
                                    ex.getMessage(), shorten(body));
                        }
                    }
                    // fallback (still stable for your call graph)
                    return !snoopVal.isEmpty() ? snoopVal : body;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> createExternalMedia(
            StasisAppConfig cfg,
            String app,
            String externalId,
            String host,
            int port,
            String codec
    ) {
        final String base = baseUrl(cfg);

        final String appVal = nullToEmpty(app).trim();
        final String externalIdVal = nullToEmpty(externalId).trim();
        final String hostVal = nullToEmpty(host).trim();

        if (hostVal.isEmpty() || port <= 0) {
            return Mono.error(new IllegalArgumentException("host/port invalid for externalMedia host=" + hostVal + " port=" + port));
        }

        final String fmt = mapCodecToAriFormat(codec); // effectively-final
        final String externalHost = hostVal + ":" + port;

        HttpUrl.Builder ub = mustUrl(base + "/channels/externalMedia", "ari_create_external_media")
                .newBuilder()
                .addQueryParameter("app", appVal)
                .addQueryParameter("external_host", externalHost)
                .addQueryParameter("format", fmt);

        // Stable channelId (optional)
        if (!externalIdVal.isEmpty()) {
            ub.addQueryParameter("channelId", externalIdVal);
        }

        HttpUrl url = ub.build();

        if (DEEP_LOGS) {
            log.info("ari_create_external_media_request url={} app={} externalId={} externalHost={} fmt={}",
                    url, appVal, externalIdVal, externalHost, fmt);
        }

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", basic(user(cfg), pass(cfg)))
                .post(RequestBody.create(new byte[0], null))
                .build();

        return Mono.fromCallable(() -> executeRequest(req, "ari_create_external_media"))
                .map(body -> parseExternalMediaIdOrFallback(body, externalIdVal))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> destroyBridge(StasisAppConfig cfg, String bridgeId) {
        if (bridgeId == null || bridgeId.isBlank()) return Mono.empty();

        final String base = baseUrl(cfg);
        String url = base + "/bridges/" + bridgeId;

        if (DEEP_LOGS) {
            log.info("ari_destroy_bridge_request url={} bridgeId={}", url, bridgeId);
        }

        Request req = new Request.Builder()
                .url(mustUrl(url, "ari_destroy_bridge"))
                .addHeader("Authorization", basic(user(cfg), pass(cfg)))
                .delete()
                .build();

        return Mono.fromCallable(() -> executeRequestAllow404(req, "ari_destroy_bridge"))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Mono<Void> hangupChannel(StasisAppConfig cfg, String channelId) {
        if (channelId == null || channelId.isBlank()) return Mono.empty();

        final String base = baseUrl(cfg);
        String url = base + "/channels/" + channelId;

        if (DEEP_LOGS) {
            log.info("ari_hangup_channel_request url={} channelId={}", url, channelId);
        }

        Request req = new Request.Builder()
                .url(mustUrl(url, "ari_hangup_channel"))
                .addHeader("Authorization", basic(user(cfg), pass(cfg)))
                .delete()
                .build();

        return Mono.fromCallable(() -> executeRequestAllow404(req, "ari_hangup_channel"))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    // ---------------------------------------------------------------------
    // NEW: Resolve the RTP peer for extMediaOut channel
    // ---------------------------------------------------------------------

    @Override
    public Mono<UnicastRtpPeer> getUnicastRtpPeer(StasisAppConfig cfg, String channelId) {
        if (channelId == null || channelId.isBlank()) return Mono.empty();

        final String base = baseUrl(cfg);
        final String url = base + "/channels/" + channelId;

        Request req = new Request.Builder()
                .url(mustUrl(url, "ari_get_channel"))
                .addHeader("Authorization", basic(user(cfg), pass(cfg)))
                .get()
                .build();

        return Mono.fromCallable(() -> executeRequest(req, "ari_get_channel"))
                .map(body -> {
                    try {
                        JsonNode root = MAPPER.readTree(body);
                        JsonNode vars = root.path("channelvars");

                        String ip = vars.path("UNICASTRTP_LOCAL_ADDRESS").asText(null);
                        String portStr = vars.path("UNICASTRTP_LOCAL_PORT").asText(null);

                        Integer port = null;
                        try {
                            if (portStr != null && !portStr.isBlank()) port = Integer.parseInt(portStr.trim());
                        } catch (Exception ignore) {}

                        if (ip == null || ip.isBlank() || port == null || port <= 0) {
                            // Always visible warning: this is key for deadlock debugging
                            log.warn("ARI UNICASTRTP vars missing/invalid channelId={} ip='{}' portStr='{}' bodyShort={}",
                                    channelId, ip, portStr, shorten(body));
                            return null;
                        }

                        if (DEEP_LOGS) {
                            log.info("ARI UNICASTRTP peer resolved channelId={} peer={}:{}", channelId, ip, port);
                        }

                        return new UnicastRtpPeer(ip, port);

                    } catch (Exception ex) {
                        log.warn("ARI UNICASTRTP parse error channelId={} msg={} bodyShort={}",
                                channelId, ex.getMessage(), shorten(body));
                        return null;
                    }
                })
                .flatMap(peer -> peer == null ? Mono.empty() : Mono.just(peer))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // ---------------------------------------------------------------------
    // Parsing + codec mapping
    // ---------------------------------------------------------------------

    private static String mapCodecToAriFormat(String codec) {
        String c = nullToEmpty(codec).trim().toLowerCase();

        // Common inputs you use: "pcmu", "pcma", "ulaw", "alaw", "opus"
        switch (c) {
            case "pcma":
            case "alaw":
                return "alaw";
            case "opus":
                return "opus";
            case "pcmu":
            case "ulaw":
            default:
                return "ulaw";
        }
    }

    private String parseExternalMediaIdOrFallback(String body, String fallbackChannelId) {
        try {
            JsonNode n = MAPPER.readTree(body);
            String id = n.path("id").asText(null);
            if (id != null && !id.isEmpty()) return id;
        } catch (Exception ex) {
            if (DEEP_LOGS) {
                log.warn("ari_create_external_media_parse_error msg={} bodyShort={}",
                        ex.getMessage(), shorten(body));
            }
        }
        // fallback: if you asked ARI to use a stable channelId, keep it stable even if parse fails
        if (fallbackChannelId != null && !fallbackChannelId.isBlank()) {
            return fallbackChannelId;
        }
        return body;
    }
}
