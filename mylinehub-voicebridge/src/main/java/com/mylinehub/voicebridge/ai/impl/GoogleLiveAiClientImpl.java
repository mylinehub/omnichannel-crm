/*
 * Auto-formatted + FIXED (DB is single source of truth) + LOGS:
 * File: src/main/java/com/mylinehub/voicebridge/ai/impl/GoogleLiveAiClientImpl.java
 *
 * CHANGES IN THIS VERSION
 * -----------------------
 * 1) Removed CallSessionSupport (no more aiSampleRateHz(channelId) ambiguity).
 * 2) AI sample-rate is taken ONLY from StasisAppConfig (DB single source of truth):
 *      - aiSampleRateHz = configProperties.getAi_pcm_sampleRateHz()
 *      - uplink mime rate uses the SAME config rate (also from DB).
 * 3) RAG transcript logic updated to use new CallSession helpers:
 *      - appendCallerTranscriptEn(...)
 *      - appendRagTriggerText(...)
 *      - ragWordCount + ragTriggerTranscriptText for window counting
 * 4) Stored StasisAppConfig into CallSession attrs at connect() time so sendAudioChunk()
 *    can still pick correct mime rate without changing interface signatures.
 * 5) Added optional saving of AI output transcript (outputTranscription.text) into CallSession attrs
 *    so completion/summaries can include it without adding new fields to CallSession.
 *
 * IMPORTANT
 * ---------
 * - Downlink chunker alignment uses AI sample rate (DB)
 * - Target resample uses RTP packetizer clockRate()
 */

package com.mylinehub.voicebridge.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moczul.ok2curl.CurlInterceptor;
import com.mylinehub.voicebridge.ai.RealtimeAiClient;
import com.mylinehub.voicebridge.audio.AlignedPcmChunker;
import com.mylinehub.voicebridge.audio.resampler.AudioTranscoder;
import com.mylinehub.voicebridge.billing.CallBillingInfo;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.models.StasisAppInstruction;
import com.mylinehub.voicebridge.queue.OutboundQueue;
import com.mylinehub.voicebridge.service.RagService;
import com.mylinehub.voicebridge.service.StasisAppConfigService;
import com.mylinehub.voicebridge.service.impl.CrmCustomerServiceImpl;
import com.mylinehub.voicebridge.session.CallSession;
import com.mylinehub.voicebridge.session.CallSessionManager;
import com.mylinehub.voicebridge.util.OkHttpLoggerUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class GoogleLiveAiClientImpl implements RealtimeAiClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleLiveAiClientImpl.class);

    // =====================================================================
    // Per-file logging switches
    // =====================================================================
    private static final boolean DEEP_LOGS         = true;
    private static final boolean RTP_DEEP_LOGS     = false;
    private static final boolean RAG_DEEP_LOGS     = false;
    private static final boolean BARGEIN_DEEP_LOGS = false;

    // Store per-call props in CallSession attrs (DB single source of truth)
    private static final String ATTR_STASIS_PROPS = "stasis.props";

    // Optional: store AI output transcript (no new CallSession fields)
    private static final String ATTR_AI_TRANSCRIPT_EN = "ai.transcript.en";
    private static final String ATTR_AI_TRANSCRIPT_MAXCHARS = "ai.transcript.maxChars";

    private final OkHttpClient client;
    private final RagService ragService;
    private final CallSessionManager sessions;
    private final StasisAppConfigService stasisService;
    private final CrmCustomerServiceImpl crmCustomerService;

    private final ObjectMapper mapper = new ObjectMapper();

    public GoogleLiveAiClientImpl(CallSessionManager sessions,
                                  RagService ragService,
                                  StasisAppConfigService stasisService,
                                  CrmCustomerServiceImpl crmCustomerService) {

        this.sessions = sessions;
        this.ragService = ragService;
        this.stasisService = stasisService;
        this.crmCustomerService=crmCustomerService;

        OkHttpLoggerUtils myLogger = new OkHttpLoggerUtils();
        CurlInterceptor curlInterceptor = new CurlInterceptor(myLogger);

        this.client = new OkHttpClient.Builder()
                .addInterceptor(curlInterceptor)
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        if (DEEP_LOGS) {
            log.info("GoogleLiveAiClientImpl initialized (mode=google)");
        }
    }

    // =====================================================================
    // Per-call state
    // =====================================================================

    class PerCallState {
        final CallSession session;
        final OutboundQueue targetQueue;

        // AI output/input sample rate (DB single source of truth)
        final int aiSampleRateHz;

        // RTP codec clock rate (packetizer clock)
        final int targetRateHz;

        final AtomicLong chunkSeq = new AtomicLong(0L);
        final AlignedPcmChunker downlinkChunker;

        final StasisAppConfig configProperties;

        PerCallState(CallSession s, StasisAppConfig configProperties) {
            this.session = s;
            this.configProperties = configProperties;
            this.targetQueue = (s != null ? s.getOutboundQueue() : null);

            int sr = (configProperties != null ? configProperties.getAi_pcm_sampleRateHz() : 0);
            if (sr <= 0) sr = 24000; // safe fallback for Google Live downlink
            this.aiSampleRateHz = sr;

            int frameMs = (this.configProperties != null ? this.configProperties.getRtp_frame_ms() : 20);
            this.downlinkChunker = new AlignedPcmChunker(this.aiSampleRateHz, frameMs);

            int tr = (s != null && s.getPacketizerOut() != null)
                    ? s.getPacketizerOut().clockRate()
                    : this.aiSampleRateHz;
            this.targetRateHz = tr;

            if (DEEP_LOGS) {
                log.info("google_ai_percall_state_init channel={} aiRateHz={} targetRateHz={} frameMs={}",
                        (s != null ? s.getChannelId() : "null"),
                        this.aiSampleRateHz,
                        this.targetRateHz,
                        frameMs);
            }
        }
    }

    // =====================================================================
    // RealtimeAiClient: connect
    // =====================================================================

    @Override
    public WebSocket connect(CallSession session, StasisAppConfig configProperties) {

        if (session != null) {
            // keep config in per-call session (DB single source of truth)
            session.putAttr(ATTR_STASIS_PROPS, configProperties);

            // optional: init ai transcript max chars
            if (session.getAttr(ATTR_AI_TRANSCRIPT_MAXCHARS, Integer.class) == null) {
                session.putAttr(ATTR_AI_TRANSCRIPT_MAXCHARS, Integer.valueOf(60000));
            }
        }

        PerCallState state = new PerCallState(session, configProperties);
        Request request = buildWebSocketRequest(configProperties);
        WebSocketListener listener = createWebSocketListener(state);

        if (DEEP_LOGS) {
            log.info("google_ai_ws_connect_enter channel={} stasisApp={} org={} aiRateHz={}",
                    session != null ? session.getChannelId() : "null",
                    configProperties != null ? configProperties.getStasis_app_name() : "null",
                    session != null ? session.getOrganization() : "null",
                    state.aiSampleRateHz);
        }

        WebSocket ws = client.newWebSocket(request, listener);

        // Single source of truth: store WS on session
        if (session != null) {
            session.setAiWebSocket(ws);
        }

        if (DEEP_LOGS) {
            log.info("google_ai_ws_connect_ok channel={} wsHash={}",
                    session != null ? session.getChannelId() : "null",
                    System.identityHashCode(ws));
        }

        return ws;
    }

    private Request buildWebSocketRequest(StasisAppConfig props) {
        // Google Live API WebSocket endpoint
        String baseUrl = props != null ? props.getAi_google_live_ws_url() : null;
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "wss://generativelanguage.googleapis.com/ws/"
                    + "google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent";
            if (DEEP_LOGS) {
                log.warn("google_live_ws_url_not_configured_using_default url={}", baseUrl);
            }
        }

        String apiKey = props != null ? props.getAi_google_live_apiKey() : null;
        if (apiKey == null || apiKey.isBlank()) {
            log.error("google_live_api_key_missing: please set ai.google.live.apiKey in DB config");
        }

        String fullUrl = baseUrl + "?key=" + apiKey;

        if (DEEP_LOGS) {
            log.info("google_live_ws_url_final={}", safe(fullUrl));
        }

        return new Request.Builder()
                .url(fullUrl)
                .build();
    }

    // =====================================================================
    // WebSocket listener
    // =====================================================================

    private WebSocketListener createWebSocketListener(PerCallState state) {
        return new WebSocketListener() {

            @Override
            public void onOpen(WebSocket ws, Response response) {
                if (DEEP_LOGS) {
                    log.info("google_ws_open wsHash={} status={} channel={}",
                            System.identityHashCode(ws),
                            response != null ? response.code() : -1,
                            state.session != null ? state.session.getChannelId() : "null");
                }

                CallSession s = state.session;
                if (s != null) {
                    s.setAiReady(false); // becomes ready after setupComplete
                    sessions.mapWebSocket(ws, s);
                    sendSetup(ws, state);
                }
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                handleServerMessage(ws, text, state);
            }

            @Override
            public void onMessage(WebSocket ws, ByteString bytes) {
                // Google Live API uses JSON messages; binary frames are not expected.
                if (RTP_DEEP_LOGS) {
                    log.debug("google_live_binary_frame_ignored wsHash={} size={}",
                            System.identityHashCode(ws), bytes != null ? bytes.size() : -1);
                }
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                CallSession s = state.session;
                String lastJsonSent = (s != null ? s.getLastJsonSent() : "n/a");
                String lastJsonRecv = (s != null ? s.getLastJsonReceived() : "n/a");

                log.error("google_ws_closing wsHash={} code={} reason={} channel={} lastSent={} lastRecv={}",
                        System.identityHashCode(ws), code, reason,
                        s != null ? s.getChannelId() : "null",
                        preview(lastJsonSent, 240),
                        preview(lastJsonRecv, 240));

                try { ws.close(code, reason); } catch (Exception ignore) {}
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                if (DEEP_LOGS) {
                    log.warn("google_ws_closed wsHash={} code={} reason={} channel={}",
                            System.identityHashCode(ws), code, reason,
                            state.session != null ? state.session.getChannelId() : "null");
                }
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                String status = (response != null ? String.valueOf(response.code()) : "n/a");
                String bodyPreview = null;

                if (response != null && response.body() != null) {
                    try {
                        String body = response.body().string();
                        if (body != null) {
                            bodyPreview = body.length() > 500 ? body.substring(0, 500) + "..." : body;
                        }
                    } catch (Exception e) {
                        bodyPreview = "FAILED_TO_READ_BODY: " + e.getMessage();
                    }
                }

                log.error("google_ws_failure wsHash={} status={} bodyPreview={} errClass={} errMsg={} channel={}",
                        System.identityHashCode(ws),
                        status,
                        bodyPreview,
                        (t != null ? t.getClass().getSimpleName() : "n/a"),
                        (t != null ? t.getMessage() : "n/a"),
                        state.session != null ? state.session.getChannelId() : "null",
                        t);
            }
        };
    }

    // =====================================================================
    // Setup (first message to Google Live)
    // =====================================================================

    private void sendSetup(WebSocket ws, PerCallState state) {
        if (!isWebSocketValid(ws, "setup")) return;

        try {
            StasisAppConfig props = state.configProperties;

            ObjectNode root = mapper.createObjectNode();
            ObjectNode setup = root.putObject("setup");

            // Model: "models/{modelName}" e.g. models/gemini-2.0-flash-live-001
            String modelName = props != null ? props.getAi_google_live_model() : null;
            if (modelName == null || modelName.isBlank()) {
                modelName = "gemini-2.0-flash-live-001";
                if (DEEP_LOGS) {
                    log.warn("google_live_model_not_configured_using_default model={}", modelName);
                }
            }
            if (!modelName.startsWith("models/")) {
                modelName = "models/" + modelName;
            }
            setup.put("model", modelName);

            // generationConfig
            ObjectNode genCfg = setup.putObject("generationConfig");

            // Audio-only output
            ArrayNode respMods = genCfg.putArray("responseModalities");
            respMods.add("AUDIO");

            Double cfgTemp = props != null ? props.getAi_google_live_temperature() : null;
            double temperature = (cfgTemp != null ? cfgTemp : 0.7d);
            genCfg.put("temperature", temperature);

            // Speech config
            ObjectNode speechCfg = genCfg.putObject("speechConfig");
            ObjectNode voiceCfg = speechCfg.putObject("voiceConfig");
            voiceCfg.put("speakingRate", 0.85);

            String primaryLang = props != null ? props.getAi_google_primary_language() : null;
            if (primaryLang != null && !primaryLang.isBlank()) {
                voiceCfg.put("languageCode", primaryLang);
            }

            // System instructions (persona + product rules)
            ObjectNode sysInstr = setup.putObject("systemInstruction");
            ArrayNode parts = sysInstr.putArray("parts");
            String persona = personaNow(state);
            parts.addObject().put("text", persona);

            // Enable input and output transcriptions
            setup.putObject("inputAudioTranscription");
            setup.putObject("outputAudioTranscription");

            // Realtime input config: let Google handle VAD + barge-in
            ObjectNode rtCfg = setup.putObject("realtimeInputConfig");
            ObjectNode autoDet = rtCfg.putObject("automaticActivityDetection");
            autoDet.put("disabled", false);

            String payload = root.toString();
            CallSession s = state.session;
            if (s != null) {
                s.setLastJsonSent(payload);
            }

            if (DEEP_LOGS) {
                log.info("google_live_setup_send channel={} wsHash={} bytes={} model={} aiRateHz={}",
                        s != null ? s.getChannelId() : "null",
                        System.identityHashCode(ws),
                        payload.getBytes().length,
                        modelName,
                        state.aiSampleRateHz);
                if (log.isDebugEnabled()) {
                    log.debug("google_live_setup_payload={}", payload);
                }
            }

            boolean ok = ws.send(payload);
            if (!ok) {
                log.error("google_live_setup_send_failed ws.send returned false");
            }

        } catch (Exception e) {
            log.error("google_live_setup_exception msg={} class={}", e.getMessage(), e.getClass(), e);
        }
    }

    // =====================================================================
    // Handling server messages (JSON)
    // =====================================================================

    private void handleServerMessage(WebSocket ws, String text, PerCallState state) {
        CallSession session = state.session;
        int len = (text != null ? text.length() : 0);

        if (session != null && text != null) {
            session.setLastJsonReceived(text);
        }

        if (RTP_DEEP_LOGS) {
            String preview = (len > 300 ? text.substring(0, 300) + "..." : text);
            log.debug("google_ws_msg wsHash={} channel={} len={} preview={}",
                    System.identityHashCode(ws),
                    session != null ? session.getChannelId() : "null",
                    len,
                    preview);
        }

        try {
            JsonNode root = mapper.readTree(text);

            // 1) setupComplete -> mark AI ready, send initial greeting
            if (root.has("setupComplete")) {
                if (session != null) {
                    session.setAiReady(true);
                    if (DEEP_LOGS) {
                        log.info("google_live_setup_complete channel={} wsHash={}",
                                session.getChannelId(), System.identityHashCode(ws));
                    }
                }
                sendInitialGreeting(ws);
                return;
            }

            // 2) serverContent: includes audio, inputTranscription, outputTranscription, modelTurn
            JsonNode serverContent = root.path("serverContent");
            if (!serverContent.isMissingNode() && !serverContent.isNull()) {

                // 2a) Input transcription -> RAG
                JsonNode inputTx = serverContent.path("inputTranscription");
                if (!inputTx.isMissingNode() && !inputTx.isNull()) {
                    String txt = inputTx.path("text").asText(null);
                    if (txt != null && !txt.isBlank()) {
                        CallSession s = (session != null ? session : sessions.findByWebSocket(ws));
                        if (s != null) {
                            if (RTP_DEEP_LOGS) {
                                log.debug("google_input_tx channel={} len={} txtPreview={}",
                                        s.getChannelId(), txt.length(), preview(txt, 120));
                            }
                            String persona = personaNow(state);
                            handleCallerTranscriptDelta(
                                    s,
                                    ws,
                                    state.configProperties != null ? state.configProperties.getStasis_app_name() : null,
                                    txt,
                                    persona
                            );
                        }
                    }
                }

                // 2a.1) Output transcription (AI text) -> store in CallSession attrs (optional)
                JsonNode outputTx = serverContent.path("outputTranscription");
                if (!outputTx.isMissingNode() && !outputTx.isNull()) {
                    String outTxt = outputTx.path("text").asText(null);
                    if (outTxt != null && !outTxt.isBlank()) {
                        CallSession s = (session != null ? session : sessions.findByWebSocket(ws));
                        if (s != null) {
                            appendAiTranscript(s, outTxt);
                            if (RTP_DEEP_LOGS) {
                                log.debug("google_output_tx_saved channel={} len={} preview={}",
                                        s.getChannelId(), outTxt.length(), preview(outTxt, 120));
                            }
                        }
                    }
                }

                // 2b) Output audio via modelTurn.parts[].inlineData
                JsonNode modelTurn = serverContent.path("modelTurn");
                if (!modelTurn.isMissingNode() && !modelTurn.isNull()) {
                    JsonNode parts = modelTurn.path("parts");
                    if (parts.isArray()) {
                        for (JsonNode part : parts) {
                            JsonNode inline = part.path("inlineData");
                            if (!inline.isMissingNode() && !inline.isNull()) {
                                String mime = inline.path("mimeType").asText("");
                                String b64 = inline.path("data").asText("");
                                if (b64 != null && !b64.isEmpty()) {
                                    try {
                                        byte[] pcmAi = Base64.getDecoder().decode(b64);

                                        CallSession s = session;
                                        if (s == null) {
                                            s = sessions.findByWebSocket(ws);
                                        }
                                        if (s != null) {
                                            long now = System.nanoTime();
                                            if (s.getAiFirstAudioNs() == 0L) s.setAiFirstAudioNs(now);
                                            s.setAiLastAudioNs(now);
                                            s.setAiDecodedBytes(s.getAiDecodedBytes() + pcmAi.length);
                                        }

                                        if (RTP_DEEP_LOGS) {
                                            log.debug("google_audio_delta wsHash={} mime={} decodedBytes={} aiRateHz={} targetRateHz={}",
                                                    System.identityHashCode(ws), mime, pcmAi.length,
                                                    state.aiSampleRateHz, state.targetRateHz);
                                        }

                                        state.downlinkChunker.append(
                                                pcmAi,
                                                aligned -> enqueueAlignedAiChunk(aligned, state)
                                        );

                                    } catch (IllegalArgumentException e) {
                                        if (DEEP_LOGS) {
                                            log.warn("google_live_audio_b64_decode_failed len={} msg={}",
                                                    b64.length(), e.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2c) Usage metadata -> billing (tokens)
                JsonNode usage = root.path("usageMetadata");
                if (!usage.isMissingNode() && !usage.isNull()) {
                    CallSession s = (session != null ? session : sessions.findByWebSocket(ws));
                    if (s != null && s.getBillingInfo() != null) {
                        CallBillingInfo b = s.getBillingInfo();
                        long promptTokens = usage.path("promptTokenCount").asLong(0L);
                        long responseTokens = usage.path("responseTokenCount").asLong(0L);
                        b.setTotalApproxTokens(b.getTotalApproxTokens() + promptTokens + responseTokens);

                        if (RTP_DEEP_LOGS) {
                            log.debug("google_usage channel={} prompt={} resp={} totalApprox={}",
                                    s.getChannelId(), promptTokens, responseTokens, b.getTotalApproxTokens());
                        }
                    }
                }
            }

        } catch (Exception e) {
            if (DEEP_LOGS) {
                log.warn("google_live_ws_parse_error wsHash={} channel={} rawLen={}",
                        System.identityHashCode(ws),
                        session != null ? session.getChannelId() : "null",
                        len,
                        e);
            }
        }
    }

    // =====================================================================
    // Initial greeting: ask Gemini to start talking
    // =====================================================================

    private void sendInitialGreeting(WebSocket ws) {
        if (!isWebSocketValid(ws, "initialGreeting")) return;

        try {
            ObjectNode root = mapper.createObjectNode();
            ObjectNode clientContent = root.putObject("clientContent");

            ArrayNode turns = clientContent.putArray("turns");
            ObjectNode turn = turns.addObject();
            turn.put("role", "user");

            ArrayNode parts = turn.putArray("parts");
            parts.addObject().put("text",
                    "Start the phone conversation now with your standard MyLineHub greeting and ask for the caller's requirement.");

            clientContent.put("turnComplete", true);

            String payload = root.toString();
            CallSession s = sessions.findByWebSocket(ws);
            if (s != null) {
                s.setLastJsonSent(payload);
            }

            if (DEEP_LOGS) {
                log.info("google_live_initial_greeting_send wsHash={} channel={} bytes={}",
                        System.identityHashCode(ws),
                        s != null ? s.getChannelId() : "null",
                        payload.getBytes().length);
                if (log.isDebugEnabled()) {
                    log.debug("google_live_initial_greeting_payload={}", payload);
                }
            }

            boolean ok = ws.send(payload);
            if (!ok) {
                log.error("google_live_initial_greeting_send_failed ws.send returned false");
            }

        } catch (Exception e) {
            log.error("google_live_initial_greeting_error msg={} class={}", e.getMessage(), e.getClass(), e);
        }
    }

    // =====================================================================
    // RealtimeAiClient: sendAudioChunk (uplink audio)
    // =====================================================================

    @Override
    public void sendAudioChunk(CallSession session, WebSocket ws, byte[] pcm16) {
        if (!isWebSocketValid(ws, "audio")) return;
        if (session == null) return;

        if (!session.isAiReady()) {
            if (RTP_DEEP_LOGS) {
                log.debug("GOOGLE_AI_SEND_AUDIO_BLOCKED channel={} reason=ai_not_ready",
                        session.getChannelId());
            }
            return;
        }

        if (pcm16 == null || pcm16.length == 0) return;

        // DB single source of truth: pull props from CallSession attrs
        StasisAppConfig props = session.getAttr(ATTR_STASIS_PROPS, StasisAppConfig.class);

        int inputRate = (props != null && props.getAi_pcm_sampleRateHz() > 0)
                ? props.getAi_pcm_sampleRateHz()
                : 24000; // safe fallback

        try {
            ObjectNode root = mapper.createObjectNode();
            ObjectNode rt = root.putObject("realtimeInput");

            // Google Live expects Blob with data + MIME type
            ObjectNode audio = rt.putObject("audio");

            String mime = "audio/pcm;rate=" + inputRate;

            audio.put("mimeType", mime);
            audio.put("data", Base64.getEncoder().encodeToString(pcm16));

            String payload = root.toString();

            if (RTP_DEEP_LOGS) {
                log.trace("google_send_audio_pre wsHash={} channel={} bytes={} mime={}",
                        System.identityHashCode(ws), session.getChannelId(), pcm16.length, mime);
            }

            boolean ok = ws.send(payload);

            if (!ok) {
                log.error("google_live_audio_send_failed ws.send returned false channel={} wsHash={}",
                        session.getChannelId(), System.identityHashCode(ws));
            } else if (RTP_DEEP_LOGS) {
                log.trace("google_send_audio_ok wsHash={} channel={} bytes={} rate={}",
                        System.identityHashCode(ws), session.getChannelId(), pcm16.length, inputRate);
            }

        } catch (Exception e) {
            log.error("google_live_audio_send_error channel={} msg={} class={}",
                    session.getChannelId(), e.getMessage(), e.getClass(), e);
        }
    }

    // =====================================================================
    // RealtimeAiClient: sendTranscript (used for RAG context injection)
    // =====================================================================

    @Override
    public void sendTranscript(WebSocket ws, String text) {
        if (!isWebSocketValid(ws, "transcript")) return;
        if (text == null || text.isBlank()) return;

        try {
            ObjectNode root = mapper.createObjectNode();
            ObjectNode clientContent = root.putObject("clientContent");

            ArrayNode turns = clientContent.putArray("turns");
            ObjectNode turn = turns.addObject();
            turn.put("role", "user");

            ArrayNode parts = turn.putArray("parts");
            parts.addObject().put("text", text);

            // context-only, do not force response
            clientContent.put("turnComplete", false);

            String payload = root.toString();

            CallSession s = sessions.findByWebSocket(ws);
            if (s != null) {
                s.setLastJsonSent(payload);
            }

            if (RAG_DEEP_LOGS) {
                log.debug("google_live_rag_session_update_send wsHash={} bytes={} preview={}",
                        System.identityHashCode(ws),
                        payload.getBytes().length,
                        preview(payload, 160));
            }

            ws.send(payload);

            if (s != null && s.getBillingInfo() != null) {
                CallBillingInfo b = s.getBillingInfo();
                b.setTotalAiCharactersSent(b.getTotalAiCharactersSent() + text.length());
                b.setTotalApproxTokens(b.getTotalApproxTokens() + text.length() / 4);
            }

        } catch (Exception e) {
            log.error("google_live_sendTranscript_error msg={}", e.getMessage(), e);
        }
    }

    // =====================================================================
    // Downlink: align -> resample -> enqueue
    // =====================================================================

    private void enqueueAlignedAiChunk(byte[] alignedPcmAi, PerCallState state) {
        if (alignedPcmAi == null || alignedPcmAi.length == 0) return;
        if (state == null || state.session == null) return;
        if (state.targetQueue == null) {
            if (DEEP_LOGS) {
                log.warn("google_live_enqueueAlignedAiChunk targetQueue=null channel={} droppingBytes={}",
                        state.session != null ? state.session.getChannelId() : "null",
                        alignedPcmAi.length);
            }
            return;
        }

        CallSession session = state.session;
        OutboundQueue q = state.targetQueue;

        byte[] pcmTarget = alignedPcmAi;

        // Resample once: AI rate (DB) -> RTP clock (packetizer.clockRate)
        if (state.aiSampleRateHz != state.targetRateHz) {
            try {
                pcmTarget = AudioTranscoder.toMono16LE(
                        alignedPcmAi, state.aiSampleRateHz, state.targetRateHz);

                if (RTP_DEEP_LOGS) {
                    log.debug("google_ai_resample_aligned channel={} {} -> {} inBytes={} outBytes={}",
                            session.getChannelId(),
                            state.aiSampleRateHz, state.targetRateHz,
                            alignedPcmAi.length, pcmTarget.length);
                }
            } catch (Exception e) {
                log.error("google_ai_resample_aligned_error channel={} {} -> {} msg={}",
                        session.getChannelId(),
                        state.aiSampleRateHz, state.targetRateHz,
                        e.getMessage(), e);
                return;
            }
        }

        int samples = pcmTarget.length / 2;
        int durationMs = (int) ((samples * 1000L) / state.targetRateHz);

        String id = "google-ai-" + state.chunkSeq.incrementAndGet();



        boolean ok = q.enqueuePcm(id,pcmTarget,state.targetRateHz,null);
        if (!ok) {
            if (RTP_DEEP_LOGS) {
                log.warn("google_ai_outbound_queue_overflow channel={} id={} bytes={} depthMs={}",
                        session.getChannelId(), id, pcmTarget.length, q.depthMs());
            }
        } else if (RTP_DEEP_LOGS) {
            log.trace("google_ai_enqueue_ok channel={} id={} bytes={} durMs={} depthMs={}",
                    session.getChannelId(), id, pcmTarget.length, durationMs, q.depthMs());
        }
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private boolean isWebSocketValid(WebSocket ws, String usage) {
        if (ws == null) {
            if (DEEP_LOGS) {
                log.warn("Attempted to use null WebSocket for usage={}", usage);
            }
            return false;
        }
        return true;
    }


    // =====================================================================
    // RAG / transcript helpers (UPDATED for new CallSession)
    // =====================================================================

    private void handleCallerTranscriptDelta(CallSession session,
                                             WebSocket ws,
                                             String stasisAppName,
                                             String newText,
                                             String persona) {

        if (session == null) return;
        if (!session.isRagEnabled()) return;
        if (session.isRagTemporarilyDisabled()) return;
        if (newText == null || newText.isBlank()) return;

        // store real text + cleaned text using new helpers
        session.appendCallerTranscriptEn(newText);

        String cleaned = newText.replaceAll("[^\\p{L}\\p{N}\\s]", " ");
        session.appendRagTriggerText(cleaned);

        String[] words = cleaned.trim().isEmpty()
                ? new String[0]
                : cleaned.trim().split("\\s+");

        if (words.length == 0) return;

        int wordCountBefore = session.getRagWordCount();
        int wordCount = wordCountBefore;

        int threshold = session.getNextRagThreshold();
        if (threshold <= 0) threshold = 5;

        if (RAG_DEEP_LOGS) {
            log.debug("rag_delta channel={} stasisApp={} addWords={} ragWordCountBefore={} nextThreshold={}",
                    session.getChannelId(),
                    stasisAppName,
                    words.length,
                    wordCountBefore,
                    threshold);
        }

        for (String word : words) {
            if (word.isBlank()) continue;
            wordCount++;

            while (wordCount >= threshold) {

                int queryWindow = (threshold <= 20 ? threshold : 25);

                // IMPORTANT: use ragTriggerTranscriptText (cleaned window buffer)
                String query = lastNWords(session.getRagTriggerTranscriptText(), queryWindow);

                if (RAG_DEEP_LOGS) {
                    log.info("rag_trigger_ready channel={} threshold={} queryWindow={} queryPreview={}",
                            session.getChannelId(),
                            threshold,
                            queryWindow,
                            preview(query, 140));
                }

                triggerRagAndSendContext(session, ws, stasisAppName, query, persona);

                threshold = nextRagThreshold(threshold);
            }
        }

        session.setRagWordCount(wordCount);
        session.setNextRagThreshold(threshold);

        if (RAG_DEEP_LOGS) {
            log.debug("rag_delta_done channel={} ragWordCountAfter={} nextThreshold={}",
                    session.getChannelId(), wordCount, threshold);
        }
    }

    private int nextRagThreshold(int current) {
        if (current < 5)  return 5;
        if (current < 12) return 12;
        if (current < 20) return 20;
        if (current < 30) return 30;
        if (current < 40) return 40;
        if (current < 60) return 60;
        return current + 20;
    }

    private String lastNWords(String text, int n) {
        if (text == null) return "";
        String[] all = text.trim().split("\\s+");

        if (all.length <= n) {
            if (RTP_DEEP_LOGS) {
                log.trace("lastNWords returning full text, words={}", all.length);
            }
            return text.trim();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = all.length - n; i < all.length; i++) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(all[i]);
        }
        return sb.toString();
    }

    private void appendRagContext(CallSession session, String contextChunk) {
        if (contextChunk == null || contextChunk.isBlank()) return;

        StringBuilder buf = session.getRagContextBuffer();
        if (buf.length() > 0) {
            buf.append("\n\n---\n\n");
        }
        buf.append(contextChunk);

        int maxChars = session.getRagContextMaxChars();
        if (maxChars > 0 && buf.length() > maxChars) {
            int toDelete = buf.length() - maxChars;
            buf.delete(0, toDelete);
            if (RAG_DEEP_LOGS) {
                log.debug("rag_context_trimmed channel={} deletedChars={} finalLen={}",
                        session.getChannelId(), toDelete, buf.length());
            }
        }

        if (RAG_DEEP_LOGS) {
            log.debug("rag_context_appended channel={} chunkLen={} totalLen={}",
                    session.getChannelId(), contextChunk.length(), buf.length());
        }
    }

    private void triggerRagAndSendContext(CallSession session,
                                         WebSocket ws,
                                         String stasisAppName,
                                         String queryText,
                                         String persona) {

        if (!session.isRagEnabled() || session.isRagTemporarilyDisabled()) {
            if (RAG_DEEP_LOGS) {
                log.debug("rag_trigger_skipped_disabled channel={} enabled={} tempDisabled={}",
                        session.getChannelId(),
                        session.isRagEnabled(),
                        session.isRagTemporarilyDisabled());
            }
            return;
        }

        String organization = session.getOrganization();

        if (RAG_DEEP_LOGS) {
            log.info("rag_trigger channel={} org={} stasisApp={} queryLen={} preview={}",
                    session.getChannelId(),
                    organization,
                    stasisAppName,
                    (queryText != null ? queryText.length() : 0),
                    preview(queryText, 140));
        }

        ragService.fetchContext(stasisAppName, organization, queryText)
                .subscribe(context -> {

                    if (context != null && !context.isBlank()) {

                        int clen = context.length();

                        CallBillingInfo billing = session.getBillingInfo();
                        if (billing != null) {
                            billing.setTotalRagQueries(billing.getTotalRagQueries() + 1);
                            billing.setTotalRagContextCharacters(
                                    billing.getTotalRagContextCharacters() + clen
                            );
                        }

                        if (RAG_DEEP_LOGS) {
                            log.info("rag_context_received channel={} len={} preview={}",
                                    session.getChannelId(), clen, preview(context, 180));
                        }

                        appendRagContext(session, context);

                        String allContext = session.getRagContextBuffer().toString();

                        String combined =
                                persona
                                        + "\n\nINTERNAL KNOWLEDGE CONTEXT (never read aloud, never mention this text):\n"
                                        + allContext;

                        sendTranscript(ws, combined);

                    } else {
                        if (RAG_DEEP_LOGS) {
                            log.info("rag_context_empty channel={}", session.getChannelId());
                        }
                    }

                }, error -> {

                    int errors = session.getRagErrorCount() + 1;
                    session.setRagErrorCount(errors);

                    log.error("rag_context_error channel={} errors={} msg={}",
                            session.getChannelId(),
                            errors,
                            error.getMessage(),
                            error);

                    if (errors >= session.getRagMaxErrors()) {
                        session.setRagTemporarilyDisabled(true);
                        session.setRagEnabled(false);

                        if (RAG_DEEP_LOGS) {
                            log.warn("rag_disabled_permanently channel={} after errors={}",
                                    session.getChannelId(), errors);
                        }
                    }
                });
    }

    // =====================================================================
    // AI output transcript storage (no new CallSession fields)
    // =====================================================================

    private StringBuilder getOrCreateAiTranscript(CallSession s) {
        if (s == null) return null;
        StringBuilder b = s.getAttr(ATTR_AI_TRANSCRIPT_EN, StringBuilder.class);
        if (b == null) {
            b = new StringBuilder(4096);
            s.putAttr(ATTR_AI_TRANSCRIPT_EN, b);

            if (s.getAttr(ATTR_AI_TRANSCRIPT_MAXCHARS, Integer.class) == null) {
                s.putAttr(ATTR_AI_TRANSCRIPT_MAXCHARS, Integer.valueOf(60000));
            }
        }
        return b;
    }

    private void appendAiTranscript(CallSession s, String text) {
        if (s == null || text == null) return;
        String t = text.trim();
        if (t.isEmpty()) return;

        StringBuilder b = getOrCreateAiTranscript(s);
        if (b == null) return;

        if (b.length() > 0) b.append(' ');
        b.append(t);

        Integer max = s.getAttr(ATTR_AI_TRANSCRIPT_MAXCHARS, Integer.class);
        int maxChars = (max != null && max.intValue() > 0) ? max.intValue() : 60000;
        if (b.length() > maxChars) {
            int toDelete = b.length() - maxChars;
            b.delete(0, toDelete);
        }
    }

    private String personaNow(PerCallState state) {
        // Always rebuild from session attrs (includes ATTR_CUSTOMER_INFO if already fetched)
        return crmCustomerService.buildInstructionsWithExistingCustomerInfoOnTop(
            stasisService,
            state.configProperties,
            state.session
        );
    }

    
    // =====================================================================
    // Small utils
    // =====================================================================

    private static String preview(String s, int max) {
        if (s == null) return "null";
        String t = s.replace("\n", " ").replace("\r", " ").trim();
        if (t.length() <= max) return t;
        return t.substring(0, max) + "...";
    }

    private static String safe(String url) {
        if (url == null) return "null";
        int idx = url.indexOf('?');
        return idx >= 0 ? url.substring(0, idx) + "?..." : url;
    }
}
