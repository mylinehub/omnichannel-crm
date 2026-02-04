/*
 * Auto-formatted + Deep-Logged:
 * File: src/main/java/com/mylinehub/voicebridge/ai/impl/ExternalBotWsClientImpl.java
 *
 * PURPOSE
 * -------
 * External client bot WebSocket integration (Exotel Voicebot compatible).
 *
 * Streams PCM16 audio between:
 *   Asterisk/RTP  <->  VoiceBridge  <->  Client Bot WS (Exotel schema)
 *
 * CONCURRENCY GUARANTEE
 * ---------------------
 * This bean is a Spring singleton => MUST NOT store per-call mutable state.
 *
 * KEY AUDIO RULES (THIS FILE)
 * ---------------------------
 * Uplink (Asterisk -> Bot):
 *   - Align by RTP clock (packetizer.clockRate()) + rtp_frame_ms
 *
 * Downlink (Bot -> Asterisk):
 *   - Align by Bot sample rate (props.ai_pcm_sampleRateHz) + rtp_frame_ms
 *   - Then resample ONCE to RTP clock before enqueue
 */

package com.mylinehub.voicebridge.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moczul.ok2curl.CurlInterceptor;
import com.mylinehub.voicebridge.ai.BotClient;
import com.mylinehub.voicebridge.audio.AlignedPcmChunker;
import com.mylinehub.voicebridge.audio.resampler.AudioTranscoder;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.queue.OutboundQueue;
import com.mylinehub.voicebridge.service.CallTransferService;
import com.mylinehub.voicebridge.session.CallSession;
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

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class ExternalBotWsClientImpl implements BotClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalBotWsClientImpl.class);

    // =====================================================================
    // Per-file logging switches
    // =====================================================================
    private static final boolean DEEP_LOGS = true;
    private static final boolean RTP_DEEP_LOGS = false;
    private static final boolean CLEAN_UP_DEEP_LOGS = false;
    private static final boolean STOP_DEEP_LOGS = false;
    private static final boolean MARK_DEEP_LOGS = false;
    private static final boolean CLEAR_DEEP_LOGS = false;
    private static final boolean DTMF_DEEP_LOGS = false;

    
    private final OkHttpClient client;
    private final CallTransferService transferService;

    /** Shared JSON mapper. Thread-safe for reads. */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Per-call state registry keyed by WebSocket identityHashCode.
     * Entries removed on stop/close/failure.
     */
    private final ConcurrentHashMap<Integer, PerCallState> states = new ConcurrentHashMap<>();

    /** Holder for ALL per-call mutable state. */
    private static final class PerCallState {
        final CallSession session;
        final OutboundQueue targetQueue;

        final String streamSid;
        final long startedAtMs;

        /** BOT PCM rate (SINGLE source of truth) */
        final int sampleRateHz;

        /** RTP clock rate (packetizer.clockRate(), DB fallback) */
        final int rtpClockRateHz;

        final AtomicLong seqOut   = new AtomicLong(0);
        final AtomicLong seqBotRx = new AtomicLong(0);

        final AlignedPcmChunker uplinkChunker;   // align by RTP clock
        final AlignedPcmChunker downlinkChunker; // align by bot sample rate

        final StasisAppConfig configProperties;

        // deep counters
        final AtomicLong txFrames = new AtomicLong();
        final AtomicLong rxFrames = new AtomicLong();
        final AtomicLong txBytes  = new AtomicLong();
        final AtomicLong rxBytes  = new AtomicLong();
        final AtomicLong resampleErrs = new AtomicLong();
        final AtomicLong queueDrops   = new AtomicLong();

        PerCallState(CallSession session, int botSampleRateHz, int rtpClockRateHz, StasisAppConfig props) {
            this.session = session;
            this.targetQueue = session != null ? session.getOutboundQueue() : null;
            this.sampleRateHz = botSampleRateHz;
            this.rtpClockRateHz = rtpClockRateHz;
            this.configProperties = props;

            this.streamSid = "stream_" + UUID.randomUUID()
                    .toString().replace("-", "")
                    .substring(0, 12);
            this.startedAtMs = System.currentTimeMillis();

            // UPLINK: align frames by RTP clock
            this.uplinkChunker = new AlignedPcmChunker(this.rtpClockRateHz, props.getRtp_frame_ms());

            // DOWNLINK: align frames by bot sample rate
            this.downlinkChunker = new AlignedPcmChunker(this.sampleRateHz, props.getRtp_frame_ms());
        }
    }

    public ExternalBotWsClientImpl(CallTransferService transferService) {

    	this.transferService = transferService;
        OkHttpLoggerUtils logger = new OkHttpLoggerUtils();
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new CurlInterceptor(logger))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        if (DEEP_LOGS) {
            log.info("ExternalBotWsClientImpl initialized (mode=external)");
        }
    }

    // =========================================================================
    // CONNECT (per-call)
    // =========================================================================

    private static void appendQ(StringBuilder qs, String k, String v) {
        if (qs.length() > 0) qs.append("&");
        qs.append(URLEncoder.encode(k, StandardCharsets.UTF_8));
        qs.append("=");
        qs.append(URLEncoder.encode(v, StandardCharsets.UTF_8));
    }

    private static int safeInt(Integer v, int def) {
        if (v == null) return def;
        if (v <= 0) return def;
        return v;
    }

    private static int safeInt(int v, int def) {
        return (v > 0) ? v : def;
    }

    @Override
    public WebSocket connect(CallSession session, StasisAppConfig props) {

        String baseUrl = props.getBot_external_ws_url();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("bot.external.ws_url must be configured");
        }

        // SINGLE source of truth for bot PCM rate: DB (props)
        // (you said sampleRateHz == props.getAi_pcm_sampleRateHz() are same)
        final int botRateHz = safeInt(props.getAi_pcm_sampleRateHz(), 24000);

        // RTP clock rate: prefer packetizer.clockRate(), fallback to DB
        int rtpClock = -1;
        try {
            if (session != null && session.getPacketizerOut() != null) {
                rtpClock = session.getPacketizerOut().clockRate();
            }
        } catch (Exception ignore) {}
        rtpClock = safeInt(rtpClock, safeInt(props.getRtp_clock_rate(), 8000));

        PerCallState state = new PerCallState(session, botRateHz, rtpClock, props);

        String mode = props.getBot_external_mode();
        String lang = "";
        try {
          if (session != null) {
            String v = session.getAttr("call.language", String.class);
            if (v != null) lang = v.trim();
          }
        } catch (Exception ignore) {}

        if (lang.isBlank()) {
          lang = props.getBot_external_lang();
        }

        if (DEEP_LOGS) {
            log.info("bot_ws_connect_enter channel={} org={} botRateHz={} rtpClockHz={} frameMs={} mode={} lang={}",
                    session != null ? session.getChannelId() : "null",
                    session != null ? session.getOrganization() : "null",
                    botRateHz,
                    rtpClock,
                    props.getRtp_frame_ms(),
                    mode, lang);
        }

        URI uri;
        try {
            uri = URI.create(baseUrl);
        } catch (Exception e) {
            throw new IllegalStateException("bot.external.ws_url is invalid: " + baseUrl, e);
        }

        StringBuilder qs = new StringBuilder();
        // Exotel-style bots usually want THEIR media rate here (bot PCM rate)
        appendQ(qs, "sample-rate", String.valueOf(botRateHz));
        if (mode != null && !mode.isBlank()) appendQ(qs, "mode", mode);
        if (lang != null && !lang.isBlank()) appendQ(qs, "lang", lang);

        // preserve existing query if any
        String existingQ = uri.getQuery();
        String finalQuery =
                (existingQ == null || existingQ.isBlank())
                        ? qs.toString()
                        : existingQ + (qs.length() > 0 ? "&" + qs : "");

        URI finalUri;
        try {
            finalUri = new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    finalQuery,
                    uri.getFragment()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build bot WS url from: " + baseUrl, e);
        }

        String finalWsUrl = finalUri.toString();
        if (DEEP_LOGS) {
            log.info("bot_ws_connect_final_url={}", safe(finalWsUrl));
        }

        Request.Builder rb = new Request.Builder().url(finalWsUrl);

        if (props.getBot_external_basic_user() != null
                && props.getBot_external_basic_pass() != null) {

            String raw = props.getBot_external_basic_user() + ":" + props.getBot_external_basic_pass();
            rb.addHeader("Authorization", "Basic " +
                    Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8)));
            if (DEEP_LOGS) {
                log.info("bot_ws_connect_auth=basic enabled=true userPresent=true");
            }
        } else {
            if (DEEP_LOGS) {
                log.info("bot_ws_connect_auth=basic enabled=false");
            }
        }

        WebSocketListener listener = new BotWsListener(state);

        WebSocket ws;
        try {
            ws = client.newWebSocket(rb.build(), listener);
        } catch (Exception e) {
            // ERROR logs are NEVER gated
            log.error("bot_ws_connect_error url={} msg={}", safe(finalWsUrl), e.getMessage(), e);
            throw e;
        }

        int key = System.identityHashCode(ws);
        states.put(key, state);

        // Store ws in CallSession as single source of truth
        if (session != null) {
            session.setAiWebSocket(ws);
        }

        if (DEEP_LOGS) {
            log.info("bot_ws_connect_ok wsHash={} streamSid={}", key, state.streamSid);
        }
        return ws;
    }

    // =========================================================================
    // SEND TO BOT (called by AriBridgeImpl)
    // =========================================================================

    @Override
    public void sendAudioChunk(CallSession session, WebSocket ws, byte[] pcm16) {
        if (ws == null || pcm16 == null || pcm16.length == 0) return;

        PerCallState state = stateFor(ws, "sendAudioChunk");
        if (state == null) return;

        // UPLINK: align by RTP clock (NOT bot clock)
        state.uplinkChunker.append(pcm16, aligned ->
                sendAlignedMedia(ws, aligned, state));
    }

    @Override
    public void sendDtmf(CallSession session, WebSocket ws, String digit) {
        if (ws == null || digit == null || digit.isBlank()) return;

        PerCallState state = stateFor(ws, "sendDtmf");
        if (state == null) return;

        try {
            long seq = state.seqOut.incrementAndGet();

            ObjectNode root = mapper.createObjectNode();
            root.put("event", "dtmf");
            root.put("sequence_number", seq);
            root.put("stream_sid", state.streamSid);

            ObjectNode dtmf = root.putObject("dtmf");
            dtmf.put("digit", digit);
            dtmf.put("duration", 600);

            boolean ok = ws.send(root.toString());
            if (DTMF_DEEP_LOGS) {
                log.info("bot_ws_send_dtmf wsHash={} digit={} seq={} ok={}",
                        System.identityHashCode(ws), digit, seq, ok);
            }

        } catch (Exception e) {
            if (DEEP_LOGS) {
                log.warn("bot_ws_send_dtmf_error wsHash={} msg={}",
                        System.identityHashCode(ws), e.getMessage(), e);
            }
        }
    }

    @Override
    public void sendMark(CallSession session, WebSocket ws, String name) {
        if (ws == null || name == null || name.isBlank()) return;

        PerCallState state = stateFor(ws, "sendMark");
        if (state == null) return;

        try {
            long seq = state.seqOut.incrementAndGet();

            ObjectNode root = mapper.createObjectNode();
            root.put("event", "mark");
            root.put("sequence_number", seq);
            root.put("stream_sid", state.streamSid);
            root.putObject("mark").put("name", name);

            boolean ok = ws.send(root.toString());
            if (DEEP_LOGS) {
                log.info("bot_ws_send_mark wsHash={} name={} seq={} ok={}",
                        System.identityHashCode(ws), name, seq, ok);
            }

        } catch (Exception e) {
            if (DEEP_LOGS) {
                log.warn("bot_ws_send_mark_error wsHash={} msg={}",
                        System.identityHashCode(ws), e.getMessage(), e);
            }
        }
    }

    @Override
    public void sendStop(CallSession session, WebSocket ws, String reason) {
        if (ws == null) return;

        PerCallState state = stateFor(ws, "sendStop");
        if (state == null) {
            try { ws.close(1000, reason); } catch (Exception ignore) {}
            return;
        }

        try {
            // flush any remaining uplink audio to bot
            state.uplinkChunker.flushFinal(aligned ->
                    sendAlignedMedia(ws, aligned, state));

            long seq = state.seqOut.incrementAndGet();

            ObjectNode root = mapper.createObjectNode();
            root.put("event", "stop");
            root.put("sequence_number", seq);
            root.put("stream_sid", state.streamSid);

            ObjectNode stop = root.putObject("stop");
            stop.put("call_sid", state.session != null ? state.session.getChannelId() : "");
            stop.put("account_sid", state.session != null ? state.session.getOrganization() : "");
            stop.put("reason", reason != null ? reason : "callended");

            boolean ok = ws.send(root.toString());
            if (DEEP_LOGS) {
                log.info("bot_ws_send_stop wsHash={} streamSid={} seq={} ok={} reason={}",
                        System.identityHashCode(ws), state.streamSid, seq, ok, reason);
            }

        } catch (Exception e) {
            if (DEEP_LOGS) {
                log.warn("bot_ws_send_stop_error wsHash={} msg={}",
                        System.identityHashCode(ws), e.getMessage(), e);
            }
        } finally {
            cleanup(ws, "sendStop");
            try { ws.close(1000, reason); } catch (Exception ignore) {}
        }
    }

    // =========================================================================
    // LISTENER (per-call)
    // =========================================================================

    private class BotWsListener extends WebSocketListener {

        private final PerCallState state;

        BotWsListener(PerCallState state) {
            this.state = state;
        }

        @Override
        public void onOpen(WebSocket ws, Response response) {
            if (DEEP_LOGS) {
                log.info("bot_ws_open wsHash={} streamSid={} status={}",
                        System.identityHashCode(ws),
                        state.streamSid,
                        response != null ? response.code() : -1);
            }

            sendConnected(ws);
            sendStart(ws, state);
        }

        @Override
        public void onMessage(WebSocket ws, String text) {
            if (text == null || text.isBlank()) return;

            try {
                JsonNode node = mapper.readTree(text);
                String event = node.path("event").asText("");

                long seq = node.path("sequence_number").asLong(0);
                if (seq > 0) state.seqBotRx.set(seq);

                switch (event) {
                    case "media" -> handleBotMedia(node, state);
                    case "clear" -> handleBotClear(state);
                    case "mark"  -> handleBotMark(node, state);
                    case "stop"  -> handleBotStop(ws, node, state);
                    case "transfer" -> handleBotTransfer(node, ws, state);
                    default      -> {
                        if (DEEP_LOGS) {
                            log.debug("bot_ws_unhandled wsHash={} event={} raw={}",
                                    System.identityHashCode(ws), event, text);
                        }
                    }
                }
            } catch (Exception e) {
                if (DEEP_LOGS) {
                    log.warn("bot_ws_parse_error wsHash={} msg={}",
                            System.identityHashCode(ws), e.getMessage(), e);
                }
            }
        }

        @Override public void onMessage(WebSocket ws, ByteString bytes) { /* Exotel doesn't send binary */ }

        @Override
        public void onFailure(WebSocket ws, Throwable t, Response resp) {
            // ERROR => always visible
            log.error("bot_ws_failure wsHash={} status={} err={}",
                    System.identityHashCode(ws),
                    resp != null ? resp.code() : -1,
                    t != null ? t.getMessage() : "null", t);
            cleanup(ws, "onFailure");
        }

        @Override
        public void onClosing(WebSocket ws, int code, String reason) {
            if (DEEP_LOGS) {
                log.warn("bot_ws_closing wsHash={} code={} reason={}",
                        System.identityHashCode(ws), code, reason);
            }
        }

        @Override
        public void onClosed(WebSocket ws, int code, String reason) {
            if (DEEP_LOGS) {
                log.warn("bot_ws_closed wsHash={} code={} reason={}",
                        System.identityHashCode(ws), code, reason);
            }

            try {
                state.downlinkChunker.flushFinal(aligned ->
                        enqueueAlignedChunk(aligned, state));
            } catch (Exception ignore) {}

            cleanup(ws, "onClosed");
        }
    }

    // =========================================================================
    // PLATFORM -> BOT
    // =========================================================================

    private void sendConnected(WebSocket ws) {
        try {
            ObjectNode root = mapper.createObjectNode();
            root.put("event", "connected");
            boolean ok = ws.send(root.toString());
            if (DEEP_LOGS) {
                log.debug("bot_ws_send_connected wsHash={} ok={}", System.identityHashCode(ws), ok);
            }
        } catch (Exception e) {
            if (DEEP_LOGS) {
                log.warn("bot_ws_send_connected_error wsHash={} msg={}",
                        System.identityHashCode(ws), e.getMessage(), e);
            }
        }
    }

    private void sendStart(WebSocket ws, PerCallState state) {
        try {
            StasisAppConfig props = state.configProperties;

            long seq = state.seqOut.incrementAndGet();

            ObjectNode root = mapper.createObjectNode();
            root.put("event", "start");
            root.put("sequence_number", seq);
            root.put("stream_sid", state.streamSid);

            ObjectNode start = root.putObject("start");
            start.put("stream_sid", state.streamSid);
            start.put("call_sid", state.session != null ? state.session.getChannelId() : "");
            start.put("account_sid", state.session != null ? state.session.getOrganization() : "");
            start.put("from", state.session != null ? safeStr(state.session.getCallerNumber()) : "");
            start.put("to",   state.session != null ? safeStr(state.session.getStasisAppName()) : "");

            ObjectNode custom = start.putObject("custom_parameters");
            custom.put("org", state.session != null ? state.session.getOrganization() : "");
            custom.put("sample-rate", String.valueOf(state.sampleRateHz));

            String mode = props.getBot_external_mode();
            if (mode != null && !mode.isBlank()) custom.put("mode", mode);

            String lang = "";
            try {
              if (state.session != null) {
                String v = state.session.getAttr("call.language", String.class);
                if (v != null) lang = v.trim();
              }
            } catch (Exception ignore) {}

            if (lang.isBlank()) {
              lang = props.getBot_external_lang(); // fallback config lang
            }

            if (lang != null && !lang.isBlank()) {
              custom.put("lang", lang);
            }


            ObjectNode fmt = start.putObject("media_format");
            fmt.put("encoding", "pcm16");
            fmt.put("sample_rate", state.sampleRateHz);
            fmt.put("bit_rate", state.sampleRateHz * 16);

            if (DEEP_LOGS) {
            	log.info("bot_start_event_start ={} ",root.toString());
            }
            
            boolean ok = ws.send(root.toString());

            if (DEEP_LOGS) {
                log.info("bot_ws_send_start wsHash={} streamSid={} seq={} botSr={} ok={}",
                        System.identityHashCode(ws), state.streamSid, seq, state.sampleRateHz, ok);
            }

        } catch (Exception e) {
            if (DEEP_LOGS) {
                log.warn("bot_ws_send_start_error wsHash={} msg={}",
                        System.identityHashCode(ws), e.getMessage(), e);
            }
        }
    }

    private void sendAlignedMedia(WebSocket ws, byte[] aligned, PerCallState state) {
        if (aligned == null || aligned.length == 0) return;

        long seq = state.seqOut.incrementAndGet();
        long ts = System.currentTimeMillis() - state.startedAtMs;

        try {
            String b64 = Base64.getEncoder().encodeToString(aligned);

            ObjectNode root = mapper.createObjectNode();
            root.put("event", "media");
            root.put("sequence_number", seq);
            root.put("stream_sid", state.streamSid);

            ObjectNode media = root.putObject("media");
            media.put("chunk", seq);
            media.put("timestamp", ts);
            media.put("payload", b64);

            boolean ok = ws.send(root.toString());

            state.txFrames.incrementAndGet();
            state.txBytes.addAndGet(aligned.length);

            if (RTP_DEEP_LOGS) {
                log.trace("bot_ws_send_media wsHash={} seq={} bytes={} tsMs={} ok={} txFrames={} txBytes={}",
                        System.identityHashCode(ws), seq, aligned.length, ts, ok,
                        state.txFrames.get(), state.txBytes.get());
            }

        } catch (Exception e) {
            if (DEEP_LOGS) {
                log.warn("bot_ws_send_media_error wsHash={} msg={}",
                        System.identityHashCode(ws), e.getMessage(), e);
            }
        }
    }

    // =========================================================================
    // BOT -> PLATFORM
    // =========================================================================

    private void handleBotMedia(JsonNode node, PerCallState state) {
        try {
            String payload = node.path("media").path("payload").asText("");
            if (payload == null || payload.isBlank()) return;

            byte[] pcm = Base64.getDecoder().decode(payload);

            state.rxFrames.incrementAndGet();
            state.rxBytes.addAndGet(pcm.length);

            if (RTP_DEEP_LOGS) {
                log.trace("bot_ws_rx_media streamSid={} seq={} bytes={} rxFrames={} rxBytes={}",
                        state.streamSid, state.seqBotRx.get(), pcm.length,
                        state.rxFrames.get(), state.rxBytes.get());
            }

            // DOWNLINK: align by BOT sample rate
            state.downlinkChunker.append(pcm, aligned ->
                    enqueueAlignedChunk(aligned, state));

        } catch (Exception e) {
            if (DEEP_LOGS) {
                log.warn("bot_media_decode_error streamSid={} msg={}",
                        state.streamSid, e.getMessage(), e);
            }
        }
    }

    /** IMPORTANT: resample here BEFORE enqueue. */
    private void enqueueAlignedChunk(byte[] alignedPcm, PerCallState state) {
        if (alignedPcm == null || alignedPcm.length == 0) return;

        if (state.targetQueue == null) {
            if (RTP_DEEP_LOGS) {
                log.warn("bot_media_drop targetQueue=null streamSid={}", state.streamSid);
            }
            return;
        }
        

        try {
        	  Boolean transferRequested = (state.session != null)
        	      ? state.session.getAttr("transfer.requested", Boolean.class)
        	      : null;

        	  if (Boolean.TRUE.equals(transferRequested)) {

        	    Long reqAt = state.session.getAttr("transfer.requestEpochMs", Long.class);
        	    long now = System.currentTimeMillis();
        	    if (reqAt == null) reqAt = now;

        	    // allow bot audio for first 3 seconds after transfer request
        	    if (now - reqAt >= 3000) {
        	      if (DEEP_LOGS) {
        	        long d = -1;
        	        try { d = state.targetQueue.depthMs(); } catch (Exception ignore) {}
        	        log.info("TRANSFER audio_drop_after_grace channel={} queueDepthMs={} pcmBytes={} waitedMs={}",
        	            state.session.getChannelId(), d, alignedPcm.length, (now - reqAt));
        	      }
        	      return;
        	    }
        	  }
        	} catch (Exception ignore) {}


        // BOT sends at botRateHz
        int botRateHz = state.sampleRateHz;

        // RTP codec clock rate
        int targetRateHz = state.rtpClockRateHz;

        byte[] pcmTarget = alignedPcm;

        // RESAMPLE HERE (FFmpeg via AudioTranscoder)
        if (botRateHz != targetRateHz) {
            try {
                pcmTarget = AudioTranscoder.toMono16LE(alignedPcm, botRateHz, targetRateHz);

                if (RTP_DEEP_LOGS) {
                    log.debug("externalbot_resample {} -> {} inBytes={} outBytes={} streamSid={}",
                            botRateHz, targetRateHz,
                            alignedPcm.length, pcmTarget.length, state.streamSid);
                }

            } catch (Exception e) {
                state.resampleErrs.incrementAndGet();
                // ERROR => always visible
                log.error("externalbot_resample_error {} -> {} streamSid={} errs={} msg={}",
                        botRateHz, targetRateHz,
                        state.streamSid, state.resampleErrs.get(), e.getMessage(), e);
                return; // noise prevention
            }
        }

        if (!state.targetQueue.enqueuePcm("bot-" + state.seqBotRx.get(),pcmTarget,targetRateHz,null)) {
            state.queueDrops.incrementAndGet();
            if (RTP_DEEP_LOGS) {
                log.warn("bot_outbound_queue_overflow streamSid={} depthMs={} drops={}",
                        state.streamSid, state.targetQueue.depthMs(), state.queueDrops.get());
            }
        }
    }

    private void handleBotClear(PerCallState state) {
        if (state.targetQueue != null) {
            state.targetQueue.clear();
            if (CLEAR_DEEP_LOGS) {
                log.info("bot_clear_applied streamSid={}", state.streamSid);
            }
        }
    }

    private void handleBotMark(JsonNode node, PerCallState state) {
        String name = node.path("mark").path("name").asText("");
        if (MARK_DEEP_LOGS) {
            log.info("bot_mark_received streamSid={} name={}", state.streamSid, name);
        }
    }

    private void handleBotStop(WebSocket ws, JsonNode node, PerCallState state) {
        String reason = node.path("stop").path("reason").asText("botstop");
        if (STOP_DEEP_LOGS) {
            log.info("bot_stop_received wsHash={} reason={} streamSid={}",
                    System.identityHashCode(ws), reason, state.streamSid);
        }

        try {
            state.downlinkChunker.flushFinal(aligned ->
                    enqueueAlignedChunk(aligned, state));
        } catch (Exception ignore) {}

        cleanup(ws, "botStop");
        try { ws.close(1000, reason); } catch (Exception ignore) {}
    }

    private void handleBotTransfer(JsonNode node, WebSocket ws, PerCallState state) {
	    try {
	        final String channelId = (state != null && state.session != null) ? state.session.getChannelId() : "";
	
	        if (DEEP_LOGS) {
	            log.info("TRANSFER raw wsHash={} channel={} json={}",
	                    (ws != null ? System.identityHashCode(ws) : 0),
	                    channelId,
	                    (node != null ? node.toString() : "null"));
	        }
	
	        if (node == null || state == null || state.session == null) {
	            log.warn("TRANSFER drop_missing_state wsHash={} channel={}",
	                    (ws != null ? System.identityHashCode(ws) : 0), channelId);
	            return;
	        }
	
	        String streamSid = node.path("stream_sid").asText("");
	        String callSid   = node.path("call_sid").asText("");
	        String dnis      = node.path("payload").path("dnis").asText("");
	        String phone     = node.path("payload").path("phone").asText("");
	
	        Boolean already = state.session.getAttr("transfer.requested", Boolean.class);
	        if (Boolean.TRUE.equals(already)) {
	            log.warn("TRANSFER duplicate_ignored channel={} streamSid={} callSid={}", channelId, streamSid, callSid);
	            return;
	        }
	
	        dnis  = (dnis == null ? "" : dnis.trim());
	        phone = (phone == null ? "" : phone.trim());
	
	        if (dnis.isBlank() || phone.isBlank()) {
	            log.warn("TRANSFER drop_invalid channel={} streamSid={} callSid={} dnis='{}' phone='{}'",
	                    channelId, streamSid, callSid, dnis, phone);
	            return;
	        }
	
	        // normalize
	        phone = phone.replaceAll("\\s+", "");
	        dnis  = dnis.replaceAll("\\D+", "");
	
	        if (dnis.isBlank()) {
	            log.warn("TRANSFER drop_bad_dnis channel={} rawDnis='{}'",
	                    channelId, node.path("payload").path("dnis").asText(""));
	            return;
	        }
	
	        // Store attrs (ONLY). PlayoutScheduler will execute the transfer later.
	        state.session.putAttr("transfer.stream_sid", streamSid);
	        state.session.putAttr("transfer.call_sid", callSid);
	        state.session.putAttr("transfer.dnis", dnis);
	        state.session.putAttr("transfer.phone", phone);
	        state.session.putAttr("transfer.requested", Boolean.TRUE);
	        state.session.putAttr("transfer.completed", Boolean.FALSE);
	        state.session.putAttr("transfer.triggeredBy", "bot");
	        state.session.putAttr("transfer.requestEpochMs", System.currentTimeMillis());
	
	        long depthNow = -1;
	        try { depthNow = (state.targetQueue != null ? state.targetQueue.depthMs() : -1); } catch (Exception ignore) {}
	
	        log.info("TRANSFER IN channel={} wsHash={} streamSid={} callSid={} dnis={} phone={} queueDepthMs={} result=marked_requested_only",
	                channelId,
	                (ws != null ? System.identityHashCode(ws) : 0),
	                streamSid,
	                callSid,
	                dnis,
	                phone,
	                depthNow);
	
	    } catch (Exception e) {
	        log.error("TRANSFER error wsHash={} msg={}",
	                ws != null ? System.identityHashCode(ws) : 0,
	                e.getMessage(), e);
	    }
	}

    // =========================================================================
    // INTERNAL HELPERS
    // =========================================================================

    private PerCallState stateFor(WebSocket ws, String usage) {
        int key = System.identityHashCode(ws);
        PerCallState state = states.get(key);
        if (state == null) {
            if (DEEP_LOGS) {
                log.warn("bot_state_missing wsHash={} usage={} (dropping)", key, usage);
            }
        }
        return state;
    }

    private void cleanup(WebSocket ws, String where) {
        if (ws == null) return;
        int key = System.identityHashCode(ws);
        PerCallState removed = states.remove(key);

        if (removed != null) {
            if (CLEAN_UP_DEEP_LOGS) {
                log.info("bot_state_cleanup wsHash={} where={} removed=true streamSid={} txFrames={} rxFrames={} txBytes={} rxBytes={} resampleErrs={} drops={}",
                        key, where, removed.streamSid,
                        removed.txFrames.get(), removed.rxFrames.get(),
                        removed.txBytes.get(), removed.rxBytes.get(),
                        removed.resampleErrs.get(), removed.queueDrops.get());
            }
        } else {
            if (CLEAN_UP_DEEP_LOGS) {
                log.info("bot_state_cleanup wsHash={} where={} removed=false", key, where);
            }
        }
    }

    private static String safe(String url) {
        if (url == null) return "null";
        int idx = url.indexOf('?');
        return idx >= 0 ? url.substring(0, idx) + "?..." : url;
    }
    
   private static String safeStr(String s) { return s == null ? "" : s; }
}
