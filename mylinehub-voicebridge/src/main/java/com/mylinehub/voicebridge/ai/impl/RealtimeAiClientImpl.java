/*
 * Auto-formatted + FIXED: src/main/java/com/mylinehub/voicebridge/ai/impl/RealtimeAiClientImpl.java
 */
package com.mylinehub.voicebridge.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moczul.ok2curl.CurlInterceptor;
import com.mylinehub.voicebridge.ai.RealtimeAiClient;
import com.mylinehub.voicebridge.audio.AlignedPcmChunker;
import com.mylinehub.voicebridge.audio.resampler.AudioTranscoder;
import com.mylinehub.voicebridge.billing.CallBillingInfo;
import com.mylinehub.voicebridge.models.StasisAppConfig;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of {@link RealtimeAiClient} that connects to the OpenAI
 * Realtime WebSocket endpoint for bidirectional audio and text streaming.
 *
 * CONCURRENCY NOTE:
 *  - Spring singleton bean.
 *  - NO per-call mutable fields here.
 *  - All per-call state is created in connect() and captured by the WS listener.
 */
@Component
public class RealtimeAiClientImpl implements RealtimeAiClient {

  private static final Logger log = LoggerFactory.getLogger(RealtimeAiClientImpl.class);

  // =====================================================================
  // Per-file logging switches
  // =====================================================================
  private static final boolean DEEP_LOGS = false;
  private static final boolean RTP_DEEP_LOGS = false;
  private static final boolean RAG_DEEP_LOGS = false;
  private static final boolean BARGEIN_DEEP_LOGS = false;
  private static final boolean DEEP_LOGS_PAYLOAD = true;
  
  private final OkHttpClient client;
  private final RagService ragService;
  private final CallSessionManager sessions;
  private final StasisAppConfigService stasisService;
  private final CrmCustomerServiceImpl crmCustomerService;
  
  //Add near top of class (fields), once:
  private static final Base64.Encoder B64 = Base64.getEncoder();
  private static final Base64.Decoder B64D = Base64.getDecoder();

  /** Shared JSON mapper for WS text frames. Thread-safe for reads. */
  private final ObjectMapper mapper = new ObjectMapper();
  
  /**
   * Per-call mutable state holder.
   * One instance per connect(), captured inside that WS listener.
   */
  class PerCallState {
    final CallSession session;
    final OutboundQueue targetQueue;

    // AI output sample rate for this call (e.g., 24000)
    final int aiSampleRateHz;

    // Target RTP/codec clock rate for this call (e.g., 8000 for PCMU)
    final int targetRateHz;

    // Sequence generator for AI audio chunk IDs (per-call)
    final AtomicLong chunkSeq = new AtomicLong(0L);

    // Align OpenAI audio deltas before resample/enqueue
    final AlignedPcmChunker downlinkChunker;

    final StasisAppConfig configProperties;

    // -------------------------
    // Barge-in gating (per-call)
    // -------------------------
    volatile long wsOpenedNs = 0L;
    volatile long bargeIgnoreUntilNs = 0L;      // first 3s gate
    volatile long nextBargeAllowedNs = 0L;      // cooldown gate


    PerCallState(CallSession s, StasisAppConfig configProperties) {
      this.session = s;
      this.configProperties = configProperties;
      this.targetQueue = (s != null ? s.getOutboundQueue() : null);
      this.aiSampleRateHz = configProperties.getAi_pcm_sampleRateHz();
      this.downlinkChunker = new AlignedPcmChunker(this.aiSampleRateHz, configProperties.getRtp_frame_ms());

      int tr =
          (s != null && s.getPacketizerOut() != null)
              ? s.getPacketizerOut().clockRate()
              : this.aiSampleRateHz;
      this.targetRateHz = tr;

      if (DEEP_LOGS) {
        log.info("ai_percall_state_init channel={} aiRateHz={} targetRateHz={}",
            (s != null ? s.getChannelId() : "null"),
            this.aiSampleRateHz,
            this.targetRateHz);
      }
    }
  }

  public RealtimeAiClientImpl(CallSessionManager sessions,
                              RagService ragService,
                              StasisAppConfigService stasisService,
                              CrmCustomerServiceImpl crmCustomerService) {
    this.ragService = ragService;
    this.sessions = sessions;
    this.stasisService = stasisService;
    this.crmCustomerService = crmCustomerService;

    OkHttpLoggerUtils myLogger = new OkHttpLoggerUtils();
    CurlInterceptor curlInterceptor = new CurlInterceptor(myLogger);

    this.client = new OkHttpClient.Builder()
        .addInterceptor(curlInterceptor)
        .connectTimeout(Duration.ofSeconds(10))
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build();

    if (DEEP_LOGS) {
      log.info("RealtimeAiClientImpl initialized (mode=openai)");
    }
  }

  /**
   * Opens WS to OpenAI Realtime.
   */
  @Override
  public WebSocket connect(CallSession session, StasisAppConfig props) {
    PerCallState state = new PerCallState(session, props);
    Request request = buildWebSocketRequest(props.getAi_openai_apiKey(), props);
    WebSocketListener listener = createWebSocketListener(state, props);

    if (DEEP_LOGS) {
      log.info("ai_ws_connect_enter mode=openai channel={}",
          session != null ? session.getChannelId() : "null");
    }

    return client.newWebSocket(request, listener);
  }

  private Request buildWebSocketRequest(String bearerToken, StasisAppConfig props) {
    String wsUrl = props.getAi_realtime_ws_url();
    if (wsUrl == null || wsUrl.isBlank()) {
      wsUrl = "wss://api.openai.com/v1/realtime?model=" + props.getAi_model_realtime();
      if (DEEP_LOGS) {
        log.warn("realtime_ws_url_not_configured_using_default url={}", wsUrl);
      }
    } else {
      if (DEEP_LOGS) {
        log.debug("realtime_ws_url_configured url={}", wsUrl);
      }
    }

    return new Request.Builder()
        .url(wsUrl)
        .addHeader("Authorization", "Bearer " + bearerToken)
        .addHeader("OpenAI-Beta", "realtime=v1")
        .build();
  }

  // ---------------------------------------------------------------------------
  // Debug helper: log outgoing WS JSON
  // ---------------------------------------------------------------------------
  private void logWsJsonPayload(String usage, String payload) {
    if (!RTP_DEEP_LOGS) return;
    int len = (payload != null ? payload.length() : 0);
    String preview = len > 300 ? payload.substring(0, 300) + "..." : payload;
    log.debug("AI WS SEND [{}] len={} preview={}", usage, len, preview);
  }

  // ---------------------------------------------------------------------------
  // Internal helpers
  // ---------------------------------------------------------------------------
  private WebSocketListener createWebSocketListener(PerCallState state, StasisAppConfig props) {
    return new WebSocketListener() {

      final String persona =crmCustomerService.buildInstructionsWithExistingCustomerInfoOnTop(stasisService, props, state.session);

      @Override
      public void onOpen(WebSocket ws, Response response) {
        if (DEEP_LOGS) {
          log.info("AI WebSocket connection opened wsHash={} status={}",
              System.identityHashCode(ws),
              response != null ? response.code() : -1);
        }

        if (state.session != null) {
          // IMPORTANT: do NOT mark aiReady=true here; wait for "session.created"
          state.session.setAiReady(false);

          sessions.mapWebSocket(ws, state.session);


          sendSessionUpdate(ws, state);
          sendInitialGreeting(ws);
        }
      }

      @Override
      public void onMessage(WebSocket ws, String text) {
        handleTextMessage(ws, text, state, persona);
      }

      @Override
      public void onMessage(WebSocket ws, ByteString bytes) {
        handleAudioMessage(bytes, state);
      }

      @Override
      public void onClosing(WebSocket ws, int code, String reason) {
        CallSession s = state.session;
        String lastJsonSent = (s != null ? s.getLastJsonSent() : "n/a");
        String lastJsonRecv = (s != null ? s.getLastJsonReceived() : "n/a");

        log.error("WS CLOSING wsHash={} code={} reason={} lastSent={} lastRecv={}",
            System.identityHashCode(ws), code, reason, lastJsonSent, lastJsonRecv);

        ws.close(code, reason);
      }

      @Override
      public void onClosed(WebSocket ws, int code, String reason) {
        if (DEEP_LOGS) {
          log.info("AI WebSocket closed wsHash={} code={} reason={}",
              System.identityHashCode(ws), code, reason);
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

        log.error(
            "AI WebSocket failure wsHash={} status={} bodyPreview={} errClass={} errMsg={}",
            System.identityHashCode(ws),
            status,
            bodyPreview,
            t != null ? t.getClass().getSimpleName() : "n/a",
            t != null ? t.getMessage() : "n/a",
            t
        );
      }
    };
  }

  
  // ---------------------------------------------------------------------------
  // Send session.update over WebSocket using existing config properties
  // ---------------------------------------------------------------------------
  private void sendSessionUpdate(WebSocket ws, PerCallState state) {

    StasisAppConfig props = state.configProperties;

    String model = props.getAi_model_realtime();
    if (model == null || model.isBlank()) {
      model = "gpt-4o-mini-realtime-preview";
      if (DEEP_LOGS) {
        log.warn("realtime_model_not_configured_using_default model={}", model);
      }
    } else if (DEEP_LOGS) {
      log.info("realtime_model_configured model={}", model);
    }

    String voice =
        (props.getAi_voice() != null && !props.getAi_voice().isBlank())
            ? props.getAi_voice()
            : "alloy";

    String inputFmt =
        (props.getAi_voice_input() != null && !props.getAi_voice_input().isBlank())
            ? props.getAi_voice_input()
            : "pcm16";

    String outputFmt =
        (props.getAi_voice_output() != null && !props.getAi_voice_output().isBlank())
            ? props.getAi_voice_output()
            : "pcm16";

    String transcribeModel =
        (props.getAi_model_transcribe() != null && !props.getAi_model_transcribe().isBlank())
            ? props.getAi_model_transcribe()
            : "gpt-4o-mini-transcribe";

    Double cfgTemp = props.getAi_temperature();
    double temperature = (cfgTemp != null) ? cfgTemp : 0.6d;
    if (temperature < 0.6d) {
      if (DEEP_LOGS) {
        log.warn("ai_temperature_too_low_clamping value={} to min=0.6", temperature);
      }
      temperature = 0.6d;
    }

    try {

      ObjectNode root = mapper.createObjectNode();
      root.put("type", "session.update");

      ObjectNode sessionNode = root.putObject("session");
      sessionNode.put("model", model);

      sessionNode.putArray("modalities")
          .add("text")
          .add("audio");

      sessionNode.put("voice", voice);
      sessionNode.put("input_audio_format", inputFmt);
      sessionNode.put("output_audio_format", outputFmt);

      
      if(state.configProperties.getDo_rag()) {
	      ObjectNode inputTranscription = sessionNode.putObject("input_audio_transcription");
	      inputTranscription.put("model", transcribeModel);
	      inputTranscription.put("language", "en");
      }
      
      ObjectNode turnDetection = sessionNode.putObject("turn_detection");
      turnDetection.put("type", "server_vad");
      turnDetection.put("threshold", 0.5);
      turnDetection.put("silence_duration_ms", 400);
      turnDetection.put("create_response", true);

      sessionNode.put("temperature", temperature);

      final String persona =crmCustomerService.buildInstructionsWithExistingCustomerInfoOnTop(stasisService, props, state.session);
      sessionNode.put("instructions", persona);

      String json = root.toString();

      if (DEEP_LOGS_PAYLOAD) {
        log.info("realtime_session_update_payload jsonSize={} bytes", json.getBytes().length);
        if (log.isDebugEnabled()) {
          log.debug("realtime_session_update_payload_json={}", json);
        }
      }

      logWsJsonPayload("session.update", json);
      ws.send(json);

    } catch (Exception e) {
      log.error("realtime_session_update_exception msg={} class={}", e.getMessage(), e.getClass(), e);
    }
  }

  // ---------------------------------------------------------------------------
  // Initial greeting: calls response.create
  // ---------------------------------------------------------------------------
  private void sendInitialGreeting(WebSocket ws) {
    sendResponseCreate(ws);
  }

  /**
   * Explicit response.create request to Realtime.
   */
  public void sendResponseCreate(WebSocket ws) {
    if (!isWebSocketValid(ws, "response.create")) return;

    try {
      ObjectNode root = mapper.createObjectNode();
      root.put("type", "response.create");

      String payload = root.toString();
//      CallSession s = sessions.findByWebSocket(ws);
//      if (s != null) s.setLastJsonSent(payload);

      if (DEEP_LOGS) {
        String preview = payload.length() > 2000 ? payload.substring(0, 2000) + "..." : payload;
        log.info("realtime_response_create_payload={}", preview);
      }

      logWsJsonPayload("response.create", payload);

      boolean sent = ws.send(payload);
      if (!sent) {
        log.error("realtime_response_create_send_failed ws.send returned false");
      }

    } catch (Exception e) {
      log.error("realtime_response_create_error msg={} class={}", e.getMessage(), e.getClass(), e);
    }
  }

  private void handleSessionCreated(JsonNode eventNode, PerCallState state) {
    JsonNode sessionNode = eventNode.path("session");
    String sessionId = sessionNode.path("id").asText(null);

    CallSession s = state.session;
    if (s != null) {
      if (sessionId != null) {
        s.setRealTimeAPISessionId(sessionId);
      }
      // IMPORTANT: mark ready only after session.created
      s.setAiReady(true);
    }

    if (DEEP_LOGS) {
      log.info("realtime_session_created id={} channel={}",
          sessionId,
          s != null ? s.getChannelId() : "null");
    }
  }

  /**
   * Handle all incoming JSON text events from the Realtime WebSocket.
   */
  private void handleTextMessage(WebSocket ws, String text, PerCallState state, String persona) {

    CallSession s = state.session;
    
    
    int len = (text != null ? text.length() : 0);

    if (s != null && text != null) {
      s.setLastJsonReceived(text);
    }

    if (RTP_DEEP_LOGS) {
      String preview = len > 300 ? text.substring(0, 300) + "..." : text;
      log.debug("AI WS text message (len={}): {}", len, preview);
    }

    if (len > 0) {
      CallSession sessionForMetrics = sessions.findByWebSocket(ws);
      if (sessionForMetrics != null && sessionForMetrics.getBillingInfo() != null) {
        CallBillingInfo billing = sessionForMetrics.getBillingInfo();
        billing.setTotalAiCharactersReceived(
            billing.getTotalAiCharactersReceived() + len);
        billing.setTotalApproxTokens(
            billing.getTotalApproxTokens() + len / 4L);
      }
    }

    try {
      JsonNode root = mapper.readTree(text);
      String type = root.path("type").asText("");

     // STT is always enabled (used for barge + transcript). do_rag only controls RAG fetch.
      if (DEEP_LOGS && !Boolean.TRUE.equals(state.configProperties.getDo_rag()) && type.contains("transcription")) {
        log.debug("REALTIME STT EVENT (expected) channel={} type={} do_rag=false",
            s != null ? s.getChannelId() : "null", type);
      }


      if (RTP_DEEP_LOGS) {
        log.trace("AI WS event type={}", type);
      }

//      if ("input_audio_buffer.speech_started".equals(type)) {
//        handleSpeechStarted(ws, state);
//        return;
//      }
      
      if ("input_audio_buffer.speech_started".equals(type)) {
    	  if (BARGEIN_DEEP_LOGS) {
    	    log.info("BARGE candidate speech_started channel={}", (s != null ? s.getChannelId() : "null"));
    	  }
    	  return;
    	}


      if ("session.created".equals(type)) {
        handleSessionCreated(root, state);
      }

      // 0) Errors
      if ("error".equals(type) || type.endsWith(".error")) {
        JsonNode err = root.path("error");
        log.error("realtime_error_event type={} code={} param={} eventId={} msg={}",
            type,
            err.path("code").asText("n/a"),
            err.path("param").asText(""),
            root.path("event_id").asText(""),
            err.path("message").asText(""));
        return;
      }

      // 0b) Response completed
      if ("response.completed".equals(type)) {
        String status = root.path("response").path("status").asText("");
        if ("failed".equalsIgnoreCase(status)) {
          JsonNode err = root.path("response").path("error");
          log.error("realtime_response_failed status={} code={} eventId={} msg={}",
              status,
              err.path("code").asText("n/a"),
              root.path("event_id").asText(""),
              err.path("message").asText(""));
        }

        if (s != null && s.getAiFirstAudioNs() > 0 && s.getAiLastAudioNs() > s.getAiFirstAudioNs()) {
          long totalBytes = s.getAiDecodedBytes();
          long firstNs = s.getAiFirstAudioNs();
          long lastNs = s.getAiLastAudioNs();
          double durationSec = (lastNs - firstNs) / 1_000_000_000.0;
          double approxHz = (totalBytes / 2.0) / durationSec;
          if (DEEP_LOGS) {
            log.info("ai_audio_stats bytes={} durationSec={} approxSampleRateHz={}",
                totalBytes, durationSec, approxHz);
          }
        } else {
          if (DEEP_LOGS) {
            log.info("ai_audio_stats bytes={} durationSec={} approxSampleRateHz={}",
                (s != null ? s.getAiDecodedBytes() : 0L), 0.0, 0.0);
          }
        }

        try {
          state.downlinkChunker.flushFinal(aligned -> enqueueAlignedAiChunk(aligned, state));
        } catch (Exception ignore) {}

        // do not return; other fields might still come
      }

      // 1) Caller transcription deltas (STT)
      if ("conversation.item.input_audio_transcription.delta".equals(type) ||
          "conversation.item.input_audio_transcription.completed".equals(type) ||
          "input_audio_transcription.delta".equals(type) ||
          "input_audio_transcription.completed".equals(type)) {

        String delta = root.path("delta").asText("");
        if (delta == null || delta.isEmpty()) {
          delta = root.path("text").asText("");
        }

        if (delta != null && !delta.isEmpty()) {
          CallSession session = sessions.findByWebSocket(ws);
          if (session != null) {

            // ALWAYS store English transcript (independent of RAG)
            storeEnglishTranscript(session, delta, type);

            // RAG optional
            if (session.isRagEnabled() && !session.isRagTemporarilyDisabled()) {
              handleCallerTranscriptDelta(
                  session,
                  ws,
                  state.configProperties.getStasis_app_name(),
                  delta,
                  persona
              );
            }
          } else {
            if (RTP_DEEP_LOGS) {
              log.warn("realtime_stt_delta_no_session websocketHash={}", System.identityHashCode(ws));
            }
          }
        } else {
          if (RAG_DEEP_LOGS) {
            log.debug("rag_delta_empty_text type={} rawLen={}", type, len);
          }
        }
        return;
      }

      // 2) AI output AUDIO events (base64 PCM16)
      if ("response.output_audio.delta".equals(type) ||
          "output_audio.delta".equals(type) ||
          "response.audio.delta".equals(type) ||
          "audio.delta".equals(type)) {

        String b64 = root.path("delta").asText("");
        
        if (s != null && s.getTruncateManager() != null) {
        	  s.getTruncateManager().onAssistantAudioEvent(root);
        	}

        
        if (RTP_DEEP_LOGS) {
        	  String itemId = root.path("item_id").asText("");
        	  int idx = root.path("content_index").asInt(0);
        	  log.debug("TRUNCATE hook_output_audio_delta channel={} item_id={} content_index={}",
        	      (s != null ? s.getChannelId() : "null"), itemId, idx);
        	}

        
        if (b64 == null || b64.isEmpty()) {
          if (RTP_DEEP_LOGS) {
            log.trace("{} with empty delta payload", type);
          }
        } else {
          try {
        	byte[] pcm16 = B64D.decode(b64);
        	
            if (s != null) {
              long now = System.nanoTime();
              if (s.getAiFirstAudioNs() == 0L) s.setAiFirstAudioNs(now);
              s.setAiLastAudioNs(now);
              s.setAiDecodedBytes(s.getAiDecodedBytes() + pcm16.length);
            }

            if (RTP_DEEP_LOGS) {
              log.debug("AI audio delta received eventType={} decodedBytes={}", type, pcm16.length);
            }

            state.downlinkChunker.append(pcm16, aligned -> enqueueAlignedAiChunk(aligned, state));

          } catch (IllegalArgumentException e) {
            if (DEEP_LOGS) {
              log.warn("Failed to Base64-decode {} len={} msg={}",
                  type, b64.length(), e.getMessage());
            }
          }
        }
        return;
      }

      // 3) Assistant text replies (ignored)
      if ("response.output_text.delta".equals(type) || "output_text.delta".equals(type)) {
        if (RTP_DEEP_LOGS) {
          log.debug("Ignoring assistant text reply event type={} (audio-only UX)", type);
        }
        return;
      }

    } catch (Exception e) {
      if (DEEP_LOGS) {
        log.warn("Failed to parse AI WS text message as JSON; rawLen={}", len, e);
      }
    }
  }
  

  private void handleSpeechStarted(WebSocket ws, PerCallState state) {

	  if (state == null) return;
	  CallSession session = state.session;
	  if (session == null) session = sessions.findByWebSocket(ws);
	  if (session == null) return;

	  if (BARGEIN_DEEP_LOGS) {
		  log.info("BARGE speech_started channel={} depthBeforeMs={}",
		      session.getChannelId(),
		      (session.getOutboundQueue() != null ? session.getOutboundQueue().depthMs() : -1));
		}

	  OutboundQueue q = session.getOutboundQueue();
	  if (q == null || q.depthMs() <= 0) return;
	  if (q != null && q.depthMs() > 0) {
	    q.clear();
	  }

	  // Cancel + truncate (OpenAI)
	  if (session.getTruncateManager() != null) {
	    session.getTruncateManager().interrupt(ws, state.targetRateHz);
	  } else {
		  sendResponseCancel(ws);
	  }

	  if (BARGEIN_DEEP_LOGS) {
		  log.info("BARGE interrupt_done channel={} depthAfterMs={}",
		      session.getChannelId(),
		      (q != null ? q.depthMs() : -1));
		}
	  
	}


  private void sendResponseCancel(WebSocket ws) {
    if (!isWebSocketValid(ws, "response.cancel")) return;

    try {
      ObjectNode root = mapper.createObjectNode();
      root.put("type", "response.cancel");

      String payload = root.toString();
//      CallSession s = sessions.findByWebSocket(ws);
//      if (s != null) {
//        s.setLastJsonSent(payload);
//      }

      logWsJsonPayload("response.cancel", payload);
      boolean ok = ws.send(payload);
      if (!ok) {
        log.error("realtime_response_cancel_send_failed ws.send returned false");
      }

      if (BARGEIN_DEEP_LOGS) {
        log.info("BARGE response_cancel_sent wsHash={}", System.identityHashCode(ws));
      }
    } catch (Exception e) {
      log.error("realtime_response_cancel_error msg={} class={}", e.getMessage(), e.getClass(), e);
    }
  }

  private void handleAudioMessage(ByteString bytes, PerCallState state) {
    int size = (bytes != null ? bytes.size() : 0);

    if (RTP_DEEP_LOGS) {
      log.debug("AI WS binary audio frame received, {} bytes", size);
    }
    if (size <= 0) return;

    if (state.targetQueue == null) {
      if (RTP_DEEP_LOGS) {
        log.warn("AI binary audio received but targetQueue is null; dropping {} bytes", size);
      }
      return;
    }

    state.downlinkChunker.append(bytes.toByteArray(), aligned -> enqueueAlignedAiChunk(aligned, state));
  }

  /**
   * Align -> resample (once) -> enqueue at TARGET rate (per-call).
   */
  private void enqueueAlignedAiChunk(byte[] alignedPcmAi, PerCallState state) {
    if (alignedPcmAi == null || alignedPcmAi.length == 0) return;
    if (state == null || state.session == null) return;
    if (state.targetQueue == null) {
      if (DEEP_LOGS) {
        log.warn("enqueueAlignedAiChunk targetQueue=null, dropping {} bytes", alignedPcmAi.length);
      }
      return;
    }

    CallSession session = state.session;
    OutboundQueue q = state.targetQueue;

    byte[] pcmTarget = alignedPcmAi;

    if (state.aiSampleRateHz != state.targetRateHz) {
      try {
        pcmTarget = AudioTranscoder.toMono16LE(alignedPcmAi, state.aiSampleRateHz, state.targetRateHz);

        if (RTP_DEEP_LOGS) {
          log.debug("ai_resample_aligned {} -> {} inBytes={} outBytes={} channel={}",
              state.aiSampleRateHz, state.targetRateHz,
              alignedPcmAi.length, pcmTarget.length,
              session.getChannelId());
        }
      } catch (Exception e) {
        log.error("ai_resample_aligned_error {} -> {} channel={} msg={}",
            state.aiSampleRateHz, state.targetRateHz,
            session.getChannelId(), e.getMessage(), e);
        return;
      }
    }

    int samples = pcmTarget.length / 2;
    int durationMs = (int) ((samples * 1000L) / state.targetRateHz);

	 // ---------------- DSP (far-end reference) Phase-1
	 // Feed the SAME PCM that will be played to the caller into AEC reverse stream.
	 // Without this, AEC cannot cancel echo.
	 // ----------------
	 try {
	   if (session != null && session.getApm() != null) {
	     // Strict: feed only if pcmTarget rate matches APM rate (8k only)
	     if (state.targetRateHz == session.getApm().rateHz()) {
	       int delayMs = 0;
	       try {
	         delayMs = (int) Math.max(0, Math.min(800, q.depthMs()));
	       } catch (Exception ignore) {}
	       
	       session.getApm().pushFarEnd(pcmTarget, delayMs);
	     }
	   }
	 } catch (Exception e) {
	   if (DEEP_LOGS) {
	     log.warn("DSP far_end_push_failed channel={} msg={}",
	         (session != null ? session.getChannelId() : "null"), e.getMessage());
	   }
	 }

    String id = "ai-" + state.chunkSeq.incrementAndGet();

    if (!q.enqueuePcm(id,pcmTarget,state.targetRateHz,null)) {
      if (RTP_DEEP_LOGS) {
        log.warn("ai_outbound_queue_overflow id={} bytes={} depthMs={} channel={}",
            id, pcmTarget.length, q.depthMs(), session.getChannelId());
      }
    }
    else {
    	session.setLastAiPcmEnqueuedNs(System.nanoTime());
    }
  }

  private boolean isWebSocketValid(WebSocket ws, String usage) {
    if (ws == null) {
      if (DEEP_LOGS) {
        log.warn("Attempted to use null WebSocket for usage={}", usage);
      }
      return false;
    }
    return true;
  }

  // ---------------------------------------------------------------------------
  // RAG / Transcript handling (uses CallSession.ragTriggerTranscript + ragWordCount)
  // ---------------------------------------------------------------------------
  private void handleCallerTranscriptDelta(CallSession session,
                                          WebSocket ws,
                                          String stasisAppName,
                                          String newText,
                                          String persona) {

    if (session == null) return;
    if (!session.isRagEnabled()) return;
    if (session.isRagTemporarilyDisabled()) return;
    if (newText == null || newText.isBlank()) return;

    String cleaned = newText.replaceAll("[^\\p{L}\\p{N}\\s]", " ");
    cleaned = cleaned.trim();
    if (cleaned.isEmpty()) return;

    // Update RAG-only transcript + ragWordCount INSIDE CallSession
    int before = session.getRagWordCount();
    session.appendRagTriggerText(cleaned);
    int wordCount = session.getRagWordCount();
    if (wordCount <= before) return;

    int threshold = session.getNextRagThreshold();
    if (threshold <= 0) threshold = 5; // first trigger = 5

    while (wordCount >= threshold) {

      int queryWindow = (threshold <= 20 ? threshold : 25);
      String query = lastNWords(session.getRagTriggerTranscriptText(), queryWindow);

      triggerRagAndSendContext(session, ws, stasisAppName, query, persona);

      threshold = nextRagThreshold(threshold);
    }

    session.setNextRagThreshold(threshold);
  }

  /**
   * RAG thresholds:
   * 5 => 10 => 15 => 22 => 30 => 38 => 46 => 54 => 62 => 70 => ...
   * After 46, always +8.
   */
  private int nextRagThreshold(int current) {
    if (current < 5)  return 5;
    if (current < 10) return 10;
    if (current < 15) return 15;
    if (current < 22) return 22;
    if (current < 30) return 30;
    if (current < 38) return 38;
    if (current < 46) return 46;
    return current + 8;
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
    String result = sb.toString();

    if (RTP_DEEP_LOGS) {
      String preview = result.length() > 120 ? result.substring(0, 120) + "..." : result;
      log.debug("lastNWords returning n={} words, len={}, preview=\"{}\"",
          n, result.length(), preview);
    }

    return result;
  }

  /**
   * Append a new RAG context chunk into the per-call rolling buffer.
   */
  private void appendRagContext(CallSession session, String contextChunk) {
    if (contextChunk == null || contextChunk.isBlank()) {
      return;
    }

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
            session.getChannelId(), session.isRagEnabled(), session.isRagTemporarilyDisabled());
      }
      return;
    }

    String organization = session.getOrganization();

    if (RAG_DEEP_LOGS) {
      String qPreview = (queryText != null && queryText.length() > 120)
          ? queryText.substring(0, 120) + "..."
          : queryText;

      log.info("rag_trigger channel={} org={} queryLen={} preview=\"{}\"",
          session.getChannelId(), organization,
          (queryText != null ? queryText.length() : 0),
          qPreview);
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
              String cPreview = (clen > 160 ? context.substring(0, 160) + "..." : context);
              log.info("rag_context_received channel={} len={} preview=\"{}\"",
                  session.getChannelId(), clen, cPreview);
            }

            appendRagContext(session, context);

            String allContext = session.getRagContextBuffer().toString();

            String combined =
                persona +
                "\n\nINTERNAL KNOWLEDGE CONTEXT (never read aloud, never mention this text):\n" +
                allContext;

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
              session.getChannelId(), errors, error.getMessage(), error);

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

  private void storeEnglishTranscript(CallSession session, String text, String sourceTag) {
    if (session == null) return;
    if (text == null || text.isBlank()) return;

    session.appendCallerTranscriptEn(text);

    if (DEEP_LOGS) {
      String full = session.getCallerTranscriptEnText();
      log.info("USER-TRANSCRIPT stored channel={} source={} deltaLen={} totalChars={} transcriptWords={}",
          session.getChannelId(), sourceTag, text.length(),
          (full != null ? full.length() : 0),
          countWords(full)
      );
    }
  }

  private static int countWords(String s) {
    if (s == null) return 0;
    String t = s.trim();
    if (t.isEmpty()) return 0;
    return t.split("\\s+").length;
  }

  @Override
  public void sendAudioChunk(CallSession session, WebSocket ws, byte[] pcm16) {

    if (!isWebSocketValid(ws, "audio")) return;
    if (session == null) return;

    if (!session.isAiReady()) {
      if (RTP_DEEP_LOGS) {
        log.debug("AI_SEND_AUDIO_BLOCKED channel={} reason=ai_not_ready", session.getChannelId());
      }
      return;
    }

    if (pcm16 == null || pcm16.length == 0) return;

    try {
      String b64 = B64.encodeToString(pcm16);
      String payload = new StringBuilder(b64.length() + 64)
    		    .append("{\"type\":\"input_audio_buffer.append\",\"audio\":\"")
    		    .append(b64)
    		    .append("\"}")
    		    .toString();

//      session.setLastJsonSent(payload);

      boolean ok = ws.send(payload);
      if (!ok) {
        log.error("AI_SEND_AUDIO_FAILED ws.send returned false channel={}", session.getChannelId());
      }

      if (RTP_DEEP_LOGS) {
        log.trace("AI_SEND_AUDIO channel={} bytes={} b64Len={}",
            session.getChannelId(), pcm16.length, b64.length());
      }
    } catch (Exception e) {
      log.error("AI_SEND_AUDIO_FAILED channel={} msg={}", session.getChannelId(), e.getMessage(), e);
    }
  }


  @Override
  public void sendTranscript(WebSocket ws, String text) {

    if (!isWebSocketValid(ws, "transcript")) return;
    if (text == null || text.isBlank()) return;

    try {
      ObjectNode root = mapper.createObjectNode();
      root.put("type", "session.update");

      ObjectNode session = root.putObject("session");
      session.put("instructions", text);

      String payload = root.toString();
      CallSession s = sessions.findByWebSocket(ws);
//      if (s != null) s.setLastJsonSent(payload);

      logWsJsonPayload("sendTranscript : session.update\\instructions", payload);
      ws.send(payload);

      if (RTP_DEEP_LOGS) {
        String preview = payload.length() > 2000 ? payload.substring(0, 2000) + "..." : payload;
        log.debug("realtime_session_update_audio_config_payload={}", preview);
        log.debug("realtime_session_update_payload_ready sizeBytes={}", payload.getBytes().length);
      }

      if (s != null && s.getBillingInfo() != null) {
        CallBillingInfo b = s.getBillingInfo();
        b.setTotalAiCharactersSent(b.getTotalAiCharactersSent() + text.length());
        b.setTotalApproxTokens(b.getTotalApproxTokens() + text.length() / 4);
      }

    } catch (Exception e) {
      log.error("sendTranscript_json_build_error msg={}", e.getMessage(), e);
    }
  }
  
}
