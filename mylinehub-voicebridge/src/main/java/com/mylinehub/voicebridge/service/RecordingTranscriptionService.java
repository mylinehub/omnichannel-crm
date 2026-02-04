package com.mylinehub.voicebridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moczul.ok2curl.CurlInterceptor;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.session.CallSession;
import com.mylinehub.voicebridge.util.OkHttpLoggerUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * RecordingTranscriptionService (OkHttp)
 */
@Slf4j
@Service
public class RecordingTranscriptionService {

    private static final boolean DEEP_LOGS = true;

    // Dangerous: enable ONLY briefly
    private static final boolean LOG_RAW_API_KEY = false;

    // Print curl (Ok2Curl)
    private static final boolean ENABLE_CURL_LOGS = true;

    // Avoid reading half-finalized WAV
    private static final long FILE_STABLE_WAIT_MS = 1500;
    private static final long FILE_STABLE_POLL_MS = 150;
    private static final long FILE_STABLE_MIN_BYTES = 1024;

    // HTTP timeouts
    private static final long STT_READ_TIMEOUT_SEC = 180;
    private static final long CLEANUP_READ_TIMEOUT_SEC = 120;

    private static final String OPENAI_BASE_URL = "https://api.openai.com";
    private static final String STT_PATH = "/v1/audio/transcriptions";
    private static final String RESPONSES_PATH = "/v1/responses";

    private final ObjectMapper mapper = new ObjectMapper();
    private final OkHttpClient okHttpClient = buildOkHttpClient();

    // =========================================================
    // Result DTO (STRING ONLY, as you requested)
    // =========================================================
    public static final class TranscriptionResult {
        private final String rawOriginal;                 // STT output (any language) - EXACT
        private final String englishReadableConversation; // completion output (English + Bot/User + <<<>>>)
        private final String summary;
        
        public TranscriptionResult(String rawOriginal, String englishReadableConversation,String summary) {
            this.rawOriginal = (rawOriginal == null) ? "" : rawOriginal;
            this.englishReadableConversation = (englishReadableConversation == null) ? "" : englishReadableConversation;
            this.summary = (summary == null) ? "" : summary;;
        }

        public String getRawOriginal() { return rawOriginal; }
        public String getEnglishReadableConversation() { return englishReadableConversation; }
        public String getSummary() { return summary; }
    }

    // =========================================================
    // Main API (returns readable English string)
    // =========================================================

    /**
     * Main entry (kept name same):
     * returns English readable conversation format (Bot/User + <<<>>>).
     */
    public String transcribeWavFile(String wavPath, StasisAppConfig cfg, String channelId) {
        TranscriptionResult r = transcribeWavFileDetailed(wavPath, cfg, channelId);
        return r.getEnglishReadableConversation();
    }

    /**
     * New detailed entry:
     * - rawOriginal = STT text
     * - englishReadableConversation = ONE completion output
     */
    public TranscriptionResult transcribeWavFileDetailed(String wavPath, StasisAppConfig cfg, String channelId) {
        final long t0 = System.nanoTime();
        final String ch = nvl(channelId);

        if (isBlank(wavPath)) {
            if (DEEP_LOGS) log.info("STT-FLOW skip channel={} reason=empty_path", ch);
            return new TranscriptionResult("", "","");
        }

        try {
            // ----------------------------
            // STEP 0: file validations
            // ----------------------------
            Path p = Path.of(wavPath);

            if (DEEP_LOGS) log.info("STT-FLOW step=0_start channel={} wavPath='{}' absPath='{}'",
                    ch, wavPath, p.toAbsolutePath());

            if (!Files.exists(p)) {
                log.warn("STT-FLOW step=0_skip channel={} reason=file_missing path='{}'", ch, wavPath);
                return new TranscriptionResult("", "", "");
            }

            waitForFileStable(p, ch);

            long sizeBytes = Files.size(p);
            if (sizeBytes < FILE_STABLE_MIN_BYTES) {
                log.warn("STT-FLOW step=0_skip channel={} reason=file_too_small sizeBytes={} path='{}'",
                        ch, sizeBytes, wavPath);
                return new TranscriptionResult("", "", "");
            }

            String apiKey = pickApiKey(cfg);
            if (isBlank(apiKey)) {
                log.error("STT-FLOW step=0_skip channel={} reason=missing_api_key (cfg.ai_openai_api_key OR -Dopenai.api.key OR env OPENAI_API_KEY) path='{}'",
                        ch, wavPath);
                return new TranscriptionResult("", "", "");
            }

            if (DEEP_LOGS) {
                log.info("STT-FLOW step=0_ok channel={} fileBytes={} apiKeyLen={} apiKeyHash={}",
                        ch, sizeBytes, apiKey.length(), sha256(apiKey));
                if (LOG_RAW_API_KEY) log.warn("STT-FLOW RAW_API_KEY channel={} key={}", ch, apiKey);
            }

            // ----------------------------
            // STEP 1: STT (any language)  [CALL #1]
            // ----------------------------
            final long t1 = System.nanoTime();
            String sttModel = pickSttModel(cfg);

            if (DEEP_LOGS) {
                log.info("STT-FLOW step=1_begin channel={} endpoint={} model={} note=stt_json_only",
                        ch, (OPENAI_BASE_URL + STT_PATH), safe(sttModel));
            }

            String rawTranscript = callOpenAiTranscriptions(apiKey, sttModel, p, ch, t1);
            if (rawTranscript == null) rawTranscript = "";
            rawTranscript = rawTranscript.trim();

            if (DEEP_LOGS) {
                log.info("STT-FLOW step=1_done channel={} ms={} rawLen={}",
                        ch, msSince(t1), rawTranscript.length());
                log.info("STT-FLOW step=1_raw_full channel={} rawTranscript=\n{}",
                        ch, rawTranscript);
            }

            if (rawTranscript.isEmpty()) {
                log.warn("STT-FLOW step=1_empty channel={} -> returning_empty", ch);
                return new TranscriptionResult("", "", "");
            }

            // ----------------------------
            // STEP 2: English readable conversation  [CALL #2]
            // ----------------------------
            final long t2 = System.nanoTime();
            String model = pickCleanupModel(cfg);

            if (DEEP_LOGS) {
                log.info("STT-FLOW step=2_begin channel={} endpoint={} model={} goal=english_readable_bot_user",
                        ch, (OPENAI_BASE_URL + RESPONSES_PATH), safe(model));
            }

            String englishReadable = callOpenAiResponsesEnglishReadable(apiKey, model, rawTranscript, ch, t2);
            if (englishReadable == null) englishReadable = "";
            englishReadable = englishReadable.trim();

            if (DEEP_LOGS) {
                log.info("STT-FLOW step=2_done channel={} ms={} englishReadableLen={}",
                        ch, msSince(t2), englishReadable.length());
                log.info("STT-FLOW step=2_english_readable_full channel={} englishReadable=\n{}",
                        ch, englishReadable);
            }

            if (DEEP_LOGS) {
                log.info("STT-FLOW done channel={} totalMs={} rawLen={} englishReadableLen={}",
                        ch, msSince(t0), rawTranscript.length(), englishReadable.length());
            }
            
            final long t3 = System.nanoTime();
            String summary = callOpenAiResponsesSummaryReadable(apiKey, model, englishReadable, ch, t3);

            if (DEEP_LOGS) {
                log.info("STT-FLOW step=3_done channel={} ms={} summaryLen={}",
                        ch, msSince(t3), summary.length());
                log.info("STT-FLOW step=3_summary_full channel={} summary=\n{}",
                        ch, summary);
            }
            
            
            

            return new TranscriptionResult(rawTranscript, englishReadable,summary);

        } catch (Exception ex) {
            log.error("STT-FLOW fatal channel={} path='{}' totalMs={} msg={}",
                    ch, wavPath, msSince(t0), safe(ex.getMessage()), ex);
            return new TranscriptionResult("", "", "");
        }
    }

    // =========================================================
    // Call #1: /v1/audio/transcriptions (multipart)
    // =========================================================
    private String callOpenAiTranscriptions(String apiKey,
                                           String sttModel,
                                           Path wavPath,
                                           String channelId,
                                           long tStartNanos) throws Exception {

        MediaType wavMedia = MediaType.parse("audio/wav");
        RequestBody fileBody = RequestBody.create(wavPath.toFile(), wavMedia);

        MultipartBody form = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", sttModel)
                .addFormDataPart("file", wavPath.getFileName().toString(), fileBody)
                .addFormDataPart("temperature", "0")
                .addFormDataPart("response_format", "json")
                .addFormDataPart(
                	    "prompt",
                	    "Your job is to convert a telephone call recording into text. The audio contains two way audio (caller and AI bot). Hindi, Hinglish, or Gujarati may be spoken. "
                	  + "Short phrases like 'ha', 'available hai', 'available che', 'haan', 'nahi', 'nahi che', 'nakko' etc. are critical - transcribe them if heard. "
                	  + "Do NOT change statements into questions. For example, 'ha available' must NOT become 'Is it available?'. "
                	  + "Transcribe exactly as spoken. "
                	  + "Do NOT drop short confirmations. "
                	  + "If the audio is truly not audible, return an empty response. Do NOT invent content."
                	)

                .build();

        HttpUrl url = HttpUrl.parse(OPENAI_BASE_URL + STT_PATH);
        if (url == null) throw new IllegalArgumentException("Bad OpenAI STT URL: " + OPENAI_BASE_URL + STT_PATH);

        Request req = new Request.Builder()
                .url(url)
                .post(form)
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .build();

        if (DEEP_LOGS) {
            log.info("STT-HTTP req channel={} url={} fileName={} fileBytes={} model={}",
                    nvl(channelId),
                    url,
                    wavPath.getFileName().toString(),
                    Files.size(wavPath),
                    safe(sttModel));
        }

        OkHttpClient sttClient = okHttpClient.newBuilder()
                .readTimeout(STT_READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();

        try (Response resp = sttClient.newCall(req).execute()) {
            int code = resp.code();
            String bodyStr = "";
            ResponseBody rb = resp.body();
            if (rb != null) bodyStr = rb.string();

            if (DEEP_LOGS) {
                log.info("STT-HTTP resp channel={} status={} ms={} body_full=\n{}",
                        nvl(channelId), code, Duration.ofNanos(System.nanoTime() - tStartNanos).toMillis(), bodyStr);
            }

            if (code < 200 || code >= 300) {
                throw new IllegalStateException("STT HTTP " + code + " body=" + bodyStr);
            }

            JsonNode root = mapper.readTree(bodyStr);
            JsonNode textNode = root.get("text");
            return textNode == null ? "" : textNode.asText("");
        }
    }

    // =========================================================
    // Call #2: /v1/responses (English readable Bot/User format)
    // =========================================================
    private String callOpenAiResponsesEnglishReadable(String apiKey,
                                                     String model,
                                                     String rawTranscriptAnyLanguage,
                                                     String channelId,
                                                     long tStartNanos) throws Exception {


    	String instructions =
    		    "You are a strict translator for noisy, broken phone-call transcripts.\n" +
    		    "\n" +
    		    "TASK:\n" +
    		    "Translate the input into ENGLISH ONLY.\n" +
    		    "\n" +
    		    "CRITICAL RULES:\n" +
    		    "1) Preserve MEANING and SENTENCE TYPE exactly.\n" +
    		    "   - If input is a STATEMENT, output a statement.\n" +
    		    "   - If input is a QUESTION, output a question.\n" +
    		    "   - Do NOT convert statements into questions or questions into statements.\n" +
    		    "\n" +
    		    "2) Do NOT add new words like 'is/are/was' if the original did not imply a question.\n" +
    		    "3) Do NOT invent missing context. If speaker is unclear, do not guess.\n" +
    		    "4) Do NOT summarize. Do NOT explain. Only translation.\n" +
    		    "5) Do NOT change statement into question such as 'ha avaiable' to 'Is it available? " +
    		    "6) Keep short fragments as fragments.\n" +
    		    "7) Speaker labels OPTIONAL. If used, only: 'Agent:' and 'Caller:'.\n" +
    		    "\n" +
    		    "EXAMPLE:\n" +
    		    "Input: 'avaiable' -> Output: 'It is available.' (NOT 'Is it available?')\n";

        JsonNode reqJson = mapper.createObjectNode()
                .put("model", model)
                .put("instructions", instructions)
                .put("input", rawTranscriptAnyLanguage)
		        .put("temperature", 0.0);
        
        String jsonBody = mapper.writeValueAsString(reqJson);

        HttpUrl url = HttpUrl.parse(OPENAI_BASE_URL + RESPONSES_PATH);
        if (url == null) throw new IllegalArgumentException("Bad OpenAI Responses URL: " + OPENAI_BASE_URL + RESPONSES_PATH);

        RequestBody rb = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        Request req = new Request.Builder()
                .url(url)
                .post(rb)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        if (DEEP_LOGS) {
            log.info("CLEANUP-READABLE req channel={} url={} model={} instructions_full=\n{}",
                    nvl(channelId), url, safe(model), instructions);
            log.info("CLEANUP-READABLE req channel={} body_full=\n{}",
                    nvl(channelId), jsonBody);
        }

        OkHttpClient cleanupClient = okHttpClient.newBuilder()
                .readTimeout(CLEANUP_READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();

        try (Response resp = cleanupClient.newCall(req).execute()) {
            int code = resp.code();
            String bodyStr = "";
            ResponseBody body = resp.body();
            if (body != null) bodyStr = body.string();

            if (DEEP_LOGS) {
                log.info("CLEANUP-READABLE resp channel={} status={} ms={} body_full=\n{}",
                        nvl(channelId), code, Duration.ofNanos(System.nanoTime() - tStartNanos).toMillis(), bodyStr);
            }

            if (code < 200 || code >= 300) {
                throw new IllegalStateException("CLEANUP READABLE HTTP " + code + " body=" + bodyStr);
            }

            String out = parseResponsesOutputText(bodyStr);
            return out;
        }
    }

    // =========================================================
    // Call #2: /v1/responses (English readable Bot/User format)
    // =========================================================
    private String callOpenAiResponsesSummaryReadable(String apiKey,
                                                     String model,
                                                     String englishTranscription,
                                                     String channelId,
                                                     long tStartNanos) throws Exception {
    	
    	
    	String SUMMARY_INSTRUCTIONS =
    		    "You are analyzing a phone call transcript which may contain noise (TV sound, background loud music).\n" +
    		    "\n" +
    		    "TASK:\n" +
    		    "Write a concise ENGLISH summary of the conversation and emotion (poerson was free avaiable, angry, rude, taking abuse etc).\n" +
    		    "\n" +
    		    "RULES:\n" +
    		    "1) Output ENGLISH ONLY.\n" +
    		    "2) Write two or three line summary.\n" +
    		    "3) Include only facts present in the transcript:\n" +
    		    "   - Do not include Who called whom (agent/company vs caller) etc.\n" +
    		    "   - Only What the call was about & whats the crux of conversation\n" +
    		    "4) Do NOT invent details.\n";

    	
        JsonNode reqJson = mapper.createObjectNode()
                .put("model", model)
                .put("instructions", SUMMARY_INSTRUCTIONS)
                .put("input", englishTranscription)
		        .put("temperature", 0.0);
        
        String jsonBody = mapper.writeValueAsString(reqJson);

        HttpUrl url = HttpUrl.parse(OPENAI_BASE_URL + RESPONSES_PATH);
        if (url == null) throw new IllegalArgumentException("Bad OpenAI Responses URL: " + OPENAI_BASE_URL + RESPONSES_PATH);

        RequestBody rb = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        Request req = new Request.Builder()
                .url(url)
                .post(rb)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        if (DEEP_LOGS) {
            log.info("CLEANUP-READABLE req channel={} url={} model={} instructions_full=\n{}",
                    nvl(channelId), url, safe(model), SUMMARY_INSTRUCTIONS);
            log.info("CLEANUP-READABLE req channel={} body_full=\n{}",
                    nvl(channelId), jsonBody);
        }

        OkHttpClient cleanupClient = okHttpClient.newBuilder()
                .readTimeout(CLEANUP_READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();

        try (Response resp = cleanupClient.newCall(req).execute()) {
            int code = resp.code();
            String bodyStr = "";
            ResponseBody body = resp.body();
            if (body != null) bodyStr = body.string();

            if (DEEP_LOGS) {
                log.info("CLEANUP-READABLE resp channel={} status={} ms={} body_full=\n{}",
                        nvl(channelId), code, Duration.ofNanos(System.nanoTime() - tStartNanos).toMillis(), bodyStr);
            }

            if (code < 200 || code >= 300) {
                throw new IllegalStateException("CLEANUP READABLE HTTP " + code + " body=" + bodyStr);
            }

            String out = parseResponsesOutputText(bodyStr);
            return out;
        }
    }
   
    
    
    private String parseResponsesOutputText(String bodyStr) throws Exception {
        JsonNode root = mapper.readTree(bodyStr);
        JsonNode output = root.get("output");
        if (output == null || !output.isArray()) return "";

        for (JsonNode item : output) {
            if (!"message".equals(item.path("type").asText())) continue;
            JsonNode content = item.get("content");
            if (content == null || !content.isArray()) continue;

            for (JsonNode c : content) {
                if ("output_text".equals(c.path("type").asText())) {
                    return c.path("text").asText("");
                }
            }
        }
        return "";
    }



    // =========================================================
    // Config pickers
    // =========================================================

    /**
     * Priority:
     *  1) cfg.ai_openai_api_key
     *  2) JVM system property: -Dopenai.api.key=...
     *  3) env var: OPENAI_API_KEY
     */
    private String pickApiKey(StasisAppConfig cfg) {
        try {
            String fromCfg = (cfg != null ? cfg.getAi_openai_apiKey() : null);
            if (!isBlank(fromCfg)) return fromCfg.trim();
        } catch (Exception ignore) {}

        try {
            String fromProp = System.getProperty("openai.api.key");
            if (!isBlank(fromProp)) return fromProp.trim();
        } catch (Exception ignore) {}

        try {
            String fromEnv = System.getenv("OPENAI_API_KEY");
            if (!isBlank(fromEnv)) return fromEnv.trim();
        } catch (Exception ignore) {}

        return "";
    }

    private String pickSttModel(StasisAppConfig cfg) {
        String m = null;
        try {
            m = (cfg != null ? cfg.getAi_model_transcribe() : null);
        } catch (Exception ignore) {}

        if (isBlank(m)) return "gpt-4o-mini-transcribe";
        return m.trim();
    }

    private String pickCleanupModel(StasisAppConfig cfg) {
        String m = null;
        try {
            m = (cfg != null ? cfg.getAi_model_completion() : null);
        } catch (Exception ignore) {}

        if (isBlank(m)) return "gpt-4o-mini";
        return m.trim();
    }

    // =========================================================
    // File stability guard
    // =========================================================

    private void waitForFileStable(Path p, String channelId) {
        long deadline = System.currentTimeMillis() + FILE_STABLE_WAIT_MS;
        long lastSize = -1;

        if (DEEP_LOGS) {
            log.info("STT-FILE stable_wait_begin channel={} path='{}' waitMs={} pollMs={}",
                    nvl(channelId), p.toAbsolutePath(), FILE_STABLE_WAIT_MS, FILE_STABLE_POLL_MS);
        }

        while (System.currentTimeMillis() < deadline) {
            try {
                long s = Files.size(p);
                if (DEEP_LOGS) {
                    log.info("STT-FILE stable_wait_poll channel={} path='{}' sizeBytes={}",
                            nvl(channelId), p.toAbsolutePath(), s);
                }
                if (s > 0 && s == lastSize) {
                    if (DEEP_LOGS) {
                        log.info("STT-FILE stable_wait_ok channel={} path='{}' stableSizeBytes={}",
                                nvl(channelId), p.toAbsolutePath(), s);
                    }
                    return;
                }
                lastSize = s;
            } catch (Exception e) {
                if (DEEP_LOGS) {
                    log.info("STT-FILE stable_wait_poll_error channel={} path='{}' err={}",
                            nvl(channelId), p.toAbsolutePath(), safe(e.toString()));
                }
            }

            try {
                Thread.sleep(FILE_STABLE_POLL_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                if (DEEP_LOGS) {
                    log.info("STT-FILE stable_wait_interrupted channel={} path='{}'",
                            nvl(channelId), p.toAbsolutePath());
                }
                return;
            }
        }

        if (DEEP_LOGS) {
            try {
                log.info("STT-FILE stable_wait_timeout channel={} path='{}' sizeBytesNow={}",
                        nvl(channelId), p.toAbsolutePath(), Files.size(p));
            } catch (Exception e) {
                log.info("STT-FILE stable_wait_timeout channel={} path='{}' sizeBytesNow=unknown",
                        nvl(channelId), p.toAbsolutePath());
            }
        }
    }

    // =========================================================
    // OkHttp client
    // =========================================================

    private static OkHttpClient buildOkHttpClient() {
        OkHttpClient.Builder b = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);

        if (ENABLE_CURL_LOGS) {
            OkHttpLoggerUtils myLogger = new OkHttpLoggerUtils();
            CurlInterceptor curl = new CurlInterceptor(myLogger);
            b.addInterceptor(curl);
        }

        return b.build();
    }

    // =========================================================
    // Helpers
    // =========================================================

    private static long msSince(long t0Nanos) {
        return Duration.ofNanos(System.nanoTime() - t0Nanos).toMillis();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String safe(String s) {
        if (s == null) return "null";
        return s.replace("\r", " ").replace("\n", " ").trim();
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
}
