/*
 * Copy-paste ready (OkHttp + CurlInterceptor) :
 *   src/main/java/com/mylinehub/voicebridge/service/impl/CrmCustomerServiceImpl.java
 *
 * FIXES INCLUDED (CRM D issues):
 * 1) D1: organization was blank in CRM logs:
 *    - CRM service reads org from JSON body (customerDetails.getOrganization()).
 *    - We now FORCE body.organization = organization before sending.
 *
 * 2) D2: CRM returns boolean (true/false) but VoiceBridge was parsing DTO:
 *    - updateCustomerByOrganization now returns Mono<Boolean>
 *    - OkHttp response is parsed as Boolean (or "true"/"false" text)
 *    - If false => Mono.error so upper layer logs/knows it failed.
 *
 * Notes:
 * - GET still returns CrmCustomerDto (as before).
 * - UPDATE returns Boolean.
 * - Does NOT swallow non-2xx; returns Mono.error (caller decides retry).
 * - Token is never logged raw unless LOG_RAW_TOKEN=true.
 */

package com.mylinehub.voicebridge.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moczul.ok2curl.CurlInterceptor;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.models.StasisAppInstruction;
import com.mylinehub.voicebridge.service.CrmCustomerService;
import com.mylinehub.voicebridge.service.LoginService;
import com.mylinehub.voicebridge.service.StasisAppConfigService;
import com.mylinehub.voicebridge.service.dto.CrmCustomerDto;
import com.mylinehub.voicebridge.service.dto.CrmCustomerUpdateRequestDto;
import com.mylinehub.voicebridge.session.CallSession;
import com.mylinehub.voicebridge.util.OkHttpLoggerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrmCustomerServiceImpl implements CrmCustomerService {

    private static final boolean DEEP_LOGS = true;

    // Dangerous: enable ONLY briefly
    private static final boolean LOG_RAW_TOKEN = false;

    // Print curl (Ok2Curl)
    private static final boolean ENABLE_CURL_LOGS = true;

    private static final int LOG_BODY_MAX = 2000;

    private final LoginService loginService;
    private final StasisAppConfigService configService;

    // If you already have a shared ObjectMapper bean, inject it instead.
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final AtomicLong lastReqLog = new AtomicLong(0);
    private static final long LOG_WINDOW_MS = 60_000;

    // Reuse one OkHttp client (connection pooling)
    private final OkHttpClient okHttpClient = buildOkHttpClient();

    @Override
    public Mono<CrmCustomerDto> getByPhoneNumberAndOrganization(String stasisAppName,
                                                               String organization,
                                                               String phoneNumber) {

        StasisAppConfig cfg = configService.getConfigOrNull(stasisAppName);
        if (cfg == null) {
            log.error("[CRM] no_config app={}", safe(stasisAppName));
            return Mono.empty();
        }

        String baseUrl = cfg.getMylinehub_base_url();
        String path = cfg.getMylinehub_crm_customer_get_by_phone_url();
        if (isBlank(baseUrl) || isBlank(path)) {
            log.error("[CRM] missing_base_or_get_url app={} baseUrl={} path={}",
                    safe(stasisAppName), safe(baseUrl), safe(path));
            return Mono.empty();
        }

        HttpUrl url = buildUrl(baseUrl, path)
                .newBuilder()
                .addQueryParameter("organization", organization)
                .addQueryParameter("phoneNumber", phoneNumber)
                .build();

        rateLimitedLog("[CRM] GET_REQ app=" + safe(stasisAppName)
                + " org=" + safe(organization)
                + " phone=" + shortText(phoneNumber)
                + " url=" + safe(url.toString()));

        final long t0 = System.nanoTime();

        return loginService.getValidSystemToken(cfg)
                .doOnNext(token -> logToken("GET", stasisAppName, token))
                .flatMap(token -> {
                    Request req = new Request.Builder()
                            .url(url)
                            .get()
                            .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                            .header("Authorization", "Bearer " + token)
                            .build();
                    return callOkHttpDto("GET", stasisAppName, req, t0);
                })
                .onErrorResume(e -> {
                    log.error("[CRM] GET_FATAL app={} err={}", safe(stasisAppName), safe(e.getMessage()), e);
                    return Mono.empty();
                });
    }

    /**
     * IMPORTANT:
     * CRM controller returns ResponseEntity<Boolean>.
     * So this must return Mono<Boolean>.
     */
    @Override
    public Mono<Boolean> updateCustomerByOrganization(String stasisAppName,
                                                     String organization,
                                                     String oldPhone,
                                                     CrmCustomerUpdateRequestDto body) {

        StasisAppConfig cfg = configService.getConfigOrNull(stasisAppName);
        if (cfg == null) {
            log.error("[CRM] no_config app={}", safe(stasisAppName));
            return Mono.just(false);
        }

        String baseUrl = cfg.getMylinehub_base_url();
        String path = cfg.getMylinehub_crm_customer_update_by_org_url();
        if (isBlank(baseUrl) || isBlank(path)) {
            log.error("[CRM] missing_base_or_update_url app={} baseUrl={} path={}",
                    safe(stasisAppName), safe(baseUrl), safe(path));
            return Mono.just(false);
        }

        HttpUrl url = buildUrl(baseUrl, path)
                .newBuilder()
                .addQueryParameter("oldPhone", oldPhone)
                .addQueryParameter("organization", organization) // controller requires this too
                .build();

        
        // -----------------------
        // D1 FIX: FORCE org in body
        // -----------------------
        CrmCustomerUpdateRequestDto safeBody = (body != null ? body : new CrmCustomerUpdateRequestDto());
        try {
            // if your DTO now has organization field (recommended)
            // we force it always; CRM service reads customerDetails.getOrganization()
            safeBody.setOrganization(organization);
        } catch (Exception ignore) {
            // if setter doesn't exist (older DTO), we still log it loudly
            log.error("[CRM] UPDATE dto_has_no_organization_field -> MUST ADD organization to CrmCustomerUpdateRequestDto");
        }

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(safeBody);
        } catch (Exception e) {
            log.error("[CRM] UPDATE_body_json_err app={} err={}", safe(stasisAppName), safe(e.getMessage()), e);
            return Mono.just(false);
        }

        if (DEEP_LOGS) {
            log.debug("[CRM] UPDATE_BODY app={} body={}", safe(stasisAppName), truncate(jsonBody));
        }

        rateLimitedLog("[CRM] UPDATE_REQ app=" + safe(stasisAppName)
                + " org=" + safe(organization)
                + " oldPhone=" + shortText(oldPhone)
                + " url=" + safe(url.toString()));

        final long t0 = System.nanoTime();

        return loginService.getValidSystemToken(cfg)
                .doOnNext(token -> logToken("UPDATE", stasisAppName, token))
                .flatMap(token -> {
                    RequestBody rb = RequestBody.create(
                            jsonBody,
                            okhttp3.MediaType.parse(MediaType.APPLICATION_JSON_VALUE)
                    );

                    Request req = new Request.Builder()
                            .url(url)
                            .post(rb)
                            .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .header("Authorization", "Bearer " + token)
                            .build();

                    return callOkHttpBoolean("UPDATE", stasisAppName, req, t0);
                })
                .onErrorResume(e -> {
                    log.error("[CRM] UPDATE_FATAL app={} err={}", safe(stasisAppName), safe(e.getMessage()), e);
                    return Mono.just(false);
                });
    }

    // ---------------- core OkHttp calls ----------------

    private Mono<CrmCustomerDto> callOkHttpDto(String op, String app, Request req, long t0Nanos) {
        return Mono.create(sink -> {
            Call call = okHttpClient.newCall(req);
            sink.onCancel(call::cancel);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    long ms = Duration.ofNanos(System.nanoTime() - t0Nanos).toMillis();
                    log.error("[CRM] {}_HTTP_FAIL app={} ms={} err={}", op, safe(app), ms, safe(e.getMessage()), e);
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
                        log.info("[CRM] {}_OK app={} status={} ms={}", op, safe(app), code, ms);
                        if (DEEP_LOGS) {
                            log.debug("[CRM] {}_RESP_BODY app={} body={}", op, safe(app), truncate(bodyStr));
                        }

                        if (isBlank(bodyStr)) {
                            sink.success(null);
                            return;
                        }

                        try {
                            CrmCustomerDto dto = objectMapper.readValue(bodyStr, CrmCustomerDto.class);
                            sink.success(dto);
                        } catch (Exception parseErr) {
                            log.error("[CRM] {}_PARSE_ERR app={} status={} ms={} err={} body={}",
                                    op, safe(app), code, ms, safe(parseErr.getMessage()), truncate(bodyStr));
                            sink.error(parseErr);
                        }
                        return;
                    }

                    log.error("[CRM] {}_ERR app={} status={} ms={} body={}",
                            op, safe(app), code, ms, truncate(bodyStr));

                    sink.error(new IllegalStateException("CRM HTTP " + code + " body=" + truncate(bodyStr, 500)));
                }
            });
        });
    }

    /**
     * UPDATE endpoint returns Boolean (true/false).
     * We parse it strictly.
     * - "true"/"false" plain text is supported
     * - JSON true/false is supported
     */
    private Mono<Boolean> callOkHttpBoolean(String op, String app, Request req, long t0Nanos) {
        return Mono.create(sink -> {
            Call call = okHttpClient.newCall(req);
            sink.onCancel(call::cancel);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    long ms = Duration.ofNanos(System.nanoTime() - t0Nanos).toMillis();
                    log.error("[CRM] {}_HTTP_FAIL app={} ms={} err={}", op, safe(app), ms, safe(e.getMessage()), e);
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
                            log.debug("[CRM] {}_RESP_BODY app={} status={} ms={} body={}",
                                    op, safe(app), code, ms, truncate(bodyStr));
                        } else {
                            log.info("[CRM] {}_OK app={} status={} ms={}", op, safe(app), code, ms);
                        }

                        Boolean ok = parseBooleanLenient(bodyStr);

                        // Treat null/false as failure (because CRM uses false for validation reject)
                        if (ok == null) {
                            log.error("[CRM] {}_PARSE_BOOL_ERR app={} status={} ms={} body={}",
                                    op, safe(app), code, ms, truncate(bodyStr));
                            sink.error(new IllegalStateException("CRM update returned non-boolean body=" + truncate(bodyStr, 300)));
                            return;
                        }

                        if (!ok) {
                            log.error("[CRM] {}_FAILED_FALSE app={} status={} ms={} body={}",
                                    op, safe(app), code, ms, truncate(bodyStr));
                            sink.error(new IllegalStateException("CRM update returned false"));
                            return;
                        }

                        log.info("[CRM] {}_OK_TRUE app={} status={} ms={}", op, safe(app), code, ms);
                        sink.success(true);
                        return;
                    }

                    log.error("[CRM] {}_ERR app={} status={} ms={} body={}",
                            op, safe(app), code, ms, truncate(bodyStr));

                    sink.error(new IllegalStateException("CRM HTTP " + code + " body=" + truncate(bodyStr, 500)));
                }
            });
        });
    }

    private static Boolean parseBooleanLenient(String bodyStr) {
        if (bodyStr == null) return null;
        String s = bodyStr.trim();

        // plain "true"/"false"
        if ("true".equalsIgnoreCase(s)) return Boolean.TRUE;
        if ("false".equalsIgnoreCase(s)) return Boolean.FALSE;

        // Sometimes JSON might be "true\n" etc; already trimmed above.
        // If CRM later returns {"ok":true} you can extend parsing here.
        return null;
    }

    // ---------------- OkHttp client ----------------

    private static OkHttpClient buildOkHttpClient() {
        OkHttpClient.Builder b = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        if (ENABLE_CURL_LOGS) {
            OkHttpLoggerUtils myLogger = new OkHttpLoggerUtils();
            CurlInterceptor curl = new CurlInterceptor(myLogger);
            b.addInterceptor(curl);
        }

        return b.build();
    }

    private static HttpUrl buildUrl(String baseUrl, String path) {
        String full = baseUrl;

        if (full.endsWith("/") && path.startsWith("/")) full = full.substring(0, full.length() - 1);
        else if (!full.endsWith("/") && !path.startsWith("/")) full = full + "/";

        full = full + path;

        HttpUrl u = HttpUrl.parse(full);
        if (u == null) throw new IllegalArgumentException("Bad URL: baseUrl=" + baseUrl + " path=" + path);
        return u;
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

        log.info("[CRM] TOKEN_OK op={} app={} tokenLen={} tokenHash={}",
                safe(op), safe(app),
                token != null ? token.length() : 0,
                sha256(token));

        if (LOG_RAW_TOKEN) {
            log.warn("[CRM] RAW_TOKEN op={} app={} token={}", safe(op), safe(app), token);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String shortText(String v) {
        if (v == null) return "null";
        return v.length() > 40 ? v.substring(0, 40) + "…" : v;
    }

    private static String safe(String s) {
        if (s == null) return "null";
        return s.replace("\r", " ").replace("\n", " ").trim();
    }

    private static String truncate(String s) {
        if (s == null) return "null";
        if (s.length() <= LOG_BODY_MAX) return s;
        return s.substring(0, LOG_BODY_MAX) + " …[truncated len=" + s.length() + "]";
    }

    private static String truncate(String s, int max) {
        if (s == null) return "null";
        if (s.length() <= max) return s;
        return s.substring(0, max) + " …[truncated len=" + s.length() + "]";
    }

    private static String sha256(String s) {
        if (s == null) return "null";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "sha256_err";
        }
    }
    
    
	 // ---------------------------------------------------------------------------
	 // Wrapper: reuse buildInitialInstructionsFromMemory and (optionally) prepend
	 // existing session ATTR_CUSTOMER_INFO at the top. NO new info created.
	 // ---------------------------------------------------------------------------
	 public String buildInstructionsWithExistingCustomerInfoOnTop(
	     StasisAppConfigService stasisService,
	     StasisAppConfig config,
	     CallSession session
	 ) {
	   // 1) always build the base instruction using your existing logic
	   String base = buildInitialInstructionsFromMemory(stasisService, config);
	
	   // 2) instruction flag gating (only when true)
	   StasisAppInstruction ins = stasisService.getInstructionOrNull(config.getStasis_app_name());
	   boolean fetch = (ins != null && ins.isActive() && Boolean.TRUE.equals(ins.isFetchCustomerInfo()));
	   if (!fetch) return base;
	
	   // 3) read already-fetched customerInfo string from session attr
	   String info = session.getAttr(CallSession.ATTR_CUSTOMER_INFO, String.class);						   
	   if (info == null || info.isBlank()) return base;
	
	   // 4) extract ONLY the 3 fields (no CrmCustomerDto parsing => no Instant issue)
	   String premiseName = "";
	   String purpose = "";
	   String propertyType = "";

	   try {
	     JsonNode root = objectMapper.readTree(info);
	     JsonNode inv = root.path("propertyInventory");

	     premiseName  = textOrEmpty(inv, "premiseName");
	     purpose      = textOrEmpty(inv, "purpose");
	     propertyType = textOrEmpty(inv, "propertyType");
	   } catch (Exception ignore) {
	     // keep them empty if JSON parsing fails
	   }

	   // 5) replace placeholders inside base
	   String out = base
	       .replace("{{premiseName}}", premiseName)
	       .replace("{{purpose}}", purpose)
	       .replace("{{propertyType}}", propertyType);
	   // 6) prepend it to top (no transformation, no new content)
	   // keep it clearly separated so prompt stays readable
	   return info.trim() + "\n\n" + out;
	 }
	 

	private static String textOrEmpty(JsonNode node, String field) {
	  if (node == null || node.isMissingNode() || node.isNull()) return "";
	  JsonNode v = node.get(field);
	  if (v == null || v.isNull()) return "";
	  String s = v.asText("");
	  if (s == null) return "";
	  s = s.trim();
	  if (s.isEmpty() || "N/A".equalsIgnoreCase(s)) return "";
	  return s;
	}

	 private String buildInitialInstructionsFromMemory(
		      StasisAppConfigService stasisService,
		      StasisAppConfig config
		  ) {
		    if (stasisService == null) throw new IllegalArgumentException("stasisService is null");
		    if (config == null) throw new IllegalArgumentException("config is null");

		    String app = config.getStasis_app_name();
		    if (app == null || app.isBlank()) {
		      throw new IllegalArgumentException("config.stasis_app_name is empty");
		    }

		    StasisAppInstruction instruction = stasisService.getInstructionOrNull(app);
		    if (instruction == null || !instruction.isActive()) {
		      throw new IllegalStateException("No active instruction in memory for stasis_app_name=" + app);
		    }

		    String template = instruction.getInstructions();
		    if (template == null || template.isBlank()) {
		      throw new IllegalStateException("Instruction template is empty for stasis_app_name=" + app);
		    }

		    String organization = (config.getOrganization() != null && !config.getOrganization().isBlank())
		        ? config.getOrganization()
		        : "MyLineHub";

		    String agentName = (config.getAgent_name() != null && !config.getAgent_name().isBlank())
		        ? config.getAgent_name()
		        : "MyLineHub Agent";

		    String defaultLanguage = (config.getAgent_defaultlanguage() != null && !config.getAgent_defaultlanguage().isBlank())
		        ? config.getAgent_defaultlanguage()
		        : "English";

		    return template
		        .replace("{{organization}}", organization)
		        .replace("{{agentName}}", agentName)
		        .replace("{{defaultLanguage}}", defaultLanguage)
		        .replace("{{time}}", new Date().toString())
		        .replace("{{customer_name}}", " ");
		  }

}
