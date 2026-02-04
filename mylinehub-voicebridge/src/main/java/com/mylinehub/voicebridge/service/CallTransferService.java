package com.mylinehub.voicebridge.service;

import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.session.CallSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Service
public class CallTransferService {

    private static final Logger log = LoggerFactory.getLogger(CallTransferService.class);

    private static final int ARI_TIMEOUT_MS = 1200;

    private final WebClient web = WebClient.builder().build();

    private String basic(String u, String pw) {
        String raw = (u == null ? "" : u) + ":" + (pw == null ? "" : pw);
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Performs BLIND transfer to dialplan:
     * - Sets ONE variable: TRANSFER_DATA_HEX (hex of "phone|lang" or "phone")
     * - Continues SAME channel into dialplan:
     *     context   = <stasis_app_name>-transfer   (dynamic)
     *     extension = DNIS
     *     priority  = 1
     *
     * Dialplan will:
     * - Dial DNIS so outbound To: user-part = DNIS
     * - Set User-to-User header using TRANSFER_DATA_HEX;encoding=hex
     */
    public void transferCallerToDialplan(CallSession s, StasisAppConfig cfg, String dnis, String phone) {

        if (s == null || cfg == null) {
            log.warn("TRANSFER-SVC drop sessNull={} cfgNull={}", (s == null), (cfg == null));
            return;
        }

        final String channelId = s.getChannelId();
        final String ariBase   = cfg.getAri_rest_baseUrl();
        final String user      = cfg.getAri_username();
        final String pass      = cfg.getAri_password();

        if (isBlank(channelId) || isBlank(ariBase) || isBlank(user)) {
            log.error("TRANSFER-SVC missing params channel={} ariBase='{}' userPresent={} dnis={} phonePresent={}",
                    channelId,
                    ariBase,
                    (user != null && !user.isBlank()),
                    dnis,
                    (phone != null && !phone.isBlank()));
            return;
        }

        final String dnisNorm  = digitsOnly(dnis);
        final String phoneNorm = (phone == null ? "" : phone.trim().replaceAll("\\s+", ""));

        // language from session (already set in AriBridgeImpl: s.putAttr("call.language", rr.language))
        String lang = "";
        try {
            String x = s.getAttr("call.language", String.class);
            if (x != null) lang = x.trim();
        } catch (Exception ignore) {}

        final String langNorm = (lang == null ? "" : lang.trim());

        if (isBlank(dnisNorm) || isBlank(phoneNorm)) {
            log.warn("TRANSFER-SVC drop invalid dnis='{}' phone='{}' channel={}", dnis, phone, channelId);
            return;
        }

        // Dynamic context: <stasis_app_name>-transfer
        final String ctx = nvl(cfg.getStasis_app_name()).trim() + "-transfer";

     // TransferData: ONLY "phone" (no pipe, no language)
        final String transferData = phoneNorm;
        
        // Hex encode TransferData
        final String transferHex = toHexAscii(transferData);

        // Logs (masked phone for safety; adjust to your needs)
        log.info("TRANSFER-SVC begin channel={} ctx={} dnis={} phone={} transferDataLen={} transferHexLen={}",
                channelId, ctx, dnisNorm, maskDigits(phoneNorm), transferData.length(), transferHex.length());


        // ---- MIN CALLS: ONE setvar ----
        boolean setOk = setVarBlocking(
                ariBase, user, pass,
                channelId,
                "TRANSFER_DATA_HEX",
                transferHex
        );

        // DNIS passed via "extension" (NOT variable)
        boolean contOk = continueBlockingJson(
                ariBase, user, pass,
                channelId,
                ctx,
                dnisNorm,
                1
        );

        log.info("TRANSFER-SVC done channel={} ctx={} dnis={} setOk={} continueOk={}",
                channelId, ctx, dnisNorm, setOk, contOk);
    }

    // ======================================================================
    // ARI helpers (blocking, with status+body logs)
    // ======================================================================

    private boolean setVarBlocking(String ariBase, String user, String pass,
                                   String channelId, String var, String value) {
        try {
            String url = ariBase + "/channels/" + channelId
                    + "/variable?variable=" + URLEncoder.encode(var, StandardCharsets.UTF_8)
                    + "&value=" + URLEncoder.encode(value, StandardCharsets.UTF_8);

            log.info("TRANSFER-SVC setvar begin channel={} var={} valueLen={}",
                    channelId, var, (value != null ? value.length() : 0));

            AriResult r = web.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, basic(user, pass))
                    .exchangeToMono(resp ->
                            resp.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .map(body -> new AriResult(resp.statusCode(), body))
                    )
                    .timeout(Duration.ofMillis(ARI_TIMEOUT_MS))
                    .onErrorResume(e -> Mono.just(new AriResult(HttpStatusCode.valueOf(0), "ERR: " + e.getMessage())))
                    .block();

            if (r == null) {
                log.warn("TRANSFER-SVC setvar null_response channel={} var={}", channelId, var);
                return false;
            }

            log.info("TRANSFER-SVC setvar done channel={} var={} http={} bodyLen={}",
                    channelId, var, r.status.value(), (r.body != null ? r.body.length() : 0));

            if (r.body != null && !r.body.isBlank()) {
                log.debug("TRANSFER-SVC setvar body channel={} var={} body={}", channelId, var, r.body);
            }

            return r.status.is2xxSuccessful();

        } catch (Exception e) {
            log.error("TRANSFER-SVC setvar error channel={} var={} msg={}", channelId, var, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Continue with JSON body: { "context": "...", "extension": "...", "priority": N }
     * IMPORTANT: field name is "extension" (not "exten")
     */
    private boolean continueBlockingJson(String ariBase, String user, String pass,
                                        String channelId, String context, String extension, int priority) {
        try {
            String url = ariBase + "/channels/" + channelId + "/continue";

            String json = "{"
                    + "\"context\":\"" + escapeJson(context) + "\","
                    + "\"extension\":\"" + escapeJson(extension) + "\","
                    + "\"priority\":" + priority
                    + "}";

            log.info("TRANSFER-SVC continue begin channel={} ctx={} extension={} pri={} body={}",
                    channelId, context, extension, priority, json);

            AriResult r = web.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, basic(user, pass))
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .bodyValue(json)
                    .exchangeToMono(resp ->
                            resp.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .map(body -> new AriResult(resp.statusCode(), body))
                    )
                    .timeout(Duration.ofMillis(ARI_TIMEOUT_MS))
                    .onErrorResume(e -> Mono.just(new AriResult(HttpStatusCode.valueOf(0), "ERR: " + e.getMessage())))
                    .block();

            if (r == null) {
                log.warn("TRANSFER-SVC continue null_response channel={}", channelId);
                return false;
            }

            log.info("TRANSFER-SVC continue done channel={} http={} bodyLen={}",
                    channelId, r.status.value(), (r.body != null ? r.body.length() : 0));

            if (r.body != null && !r.body.isBlank()) {
                log.debug("TRANSFER-SVC continue body channel={} body={}", channelId, r.body);
            }

            return r.status.is2xxSuccessful();

        } catch (Exception e) {
            log.error("TRANSFER-SVC continue error channel={} ctx={} extension={} msg={}",
                    channelId, context, extension, e.getMessage(), e);
            return false;
        }
    }

    // ======================================================================
    // Small helpers
    // ======================================================================

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String digitsOnly(String s) {
        if (s == null) return "";
        String x = s.replaceAll("\\D+", "");
        return x == null ? "" : x.trim();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * ASCII -> HEX (uppercase)
     * Example: "2269635161|en" -> "323236393633353136317C656E"
     */
    private static String toHexAscii(String s) {
        if (s == null) return "";
        byte[] b = s.getBytes(StandardCharsets.US_ASCII);
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) sb.append(String.format("%02X", x));
        return sb.toString();
    }

    private static String maskDigits(String s) {
        if (s == null) return "null";
        // keep last 2 digits visible
        String x = s.replaceAll("\\D+", "");
        if (x.length() <= 2) return "**";
        return "*".repeat(Math.max(0, x.length() - 2)) + x.substring(x.length() - 2);
    }

    private static final class AriResult {
        final HttpStatusCode status;
        final String body;

        AriResult(HttpStatusCode status, String body) {
            this.status = (status != null ? status : HttpStatusCode.valueOf(0));
            this.body = (body != null ? body : "");
        }
    }
}
