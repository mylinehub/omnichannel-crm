package com.mylinehub.voicebridge.ari.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.voicebridge.ari.ExternalMediaManager;
import com.mylinehub.voicebridge.ai.AiClientFactory;
import com.mylinehub.voicebridge.ai.BotClient;
import com.mylinehub.voicebridge.ai.TruncateManagerFactory;
import com.mylinehub.voicebridge.audio.AudioCodec;
import com.mylinehub.voicebridge.audio.CodecFactory;
import com.mylinehub.voicebridge.audio.resampler.AudioTranscoder;
import com.mylinehub.voicebridge.billing.CallBillingInfo;
import com.mylinehub.voicebridge.ivr.IvrService;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.queue.PlayoutScheduler;
import com.mylinehub.voicebridge.recording.CallRecordingManager;
import com.mylinehub.voicebridge.recording.StereoWavFileWriter;
import com.mylinehub.voicebridge.rtp.RtpPacketizer;
import com.mylinehub.voicebridge.rtp.RtpPortAllocator;
import com.mylinehub.voicebridge.rtp.RtpSymmetricEndpoint;
import com.mylinehub.voicebridge.service.CallCompletionService;
import com.mylinehub.voicebridge.service.CrmCustomerService;
import com.mylinehub.voicebridge.service.DspService;
import com.mylinehub.voicebridge.service.StasisAppConfigService;
import com.mylinehub.voicebridge.session.CallSession;
import com.mylinehub.voicebridge.session.CallSessionManager;
import lombok.RequiredArgsConstructor;
import okhttp3.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class AriBridgeImpl {

  private static final Logger log = LoggerFactory.getLogger(AriBridgeImpl.class);

  // =========================
  // FLAGS (keep at top)
  // =========================
  private static final boolean DEEP_LOGS = true;
  private static final boolean DTMF_DEEP_LOGS = false;
  private static final boolean RTP_DEEP_LOGS = false;

  // Debug decoded inbound PCM dump (turn on only when testing)
  private static final boolean PCM_DEBUG_DUMP = false;

  // CRM fetch (blocking) timeout
  private static final int CRM_FETCH_TIMEOUT_MS = 1200;

  // Pre-session / pre-ws inbound buffer (aiRate PCM)
  private static final int PRE_WS_MAX_MS = 800;     // keep ~0.8 sec
  private static final int PRE_WS_CHUNK_MS = 20;
  private static final int PRE_WS_MAX_CHUNKS = PRE_WS_MAX_MS / PRE_WS_CHUNK_MS;

  // CPU OPT: warmup only once per (fromRate,toRate) pair
  private static final ConcurrentHashMap<Long, byte[]> RESAMPLE_WARMUP_BUF = new ConcurrentHashMap<>();

  // Ensures config dump prints ONCE per channel per phase
  private final ConcurrentHashMap<String, Boolean> cfgDumpedStart = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Boolean> cfgDumpedEnd = new ConcurrentHashMap<>();
  private static final AtomicLong seq = new AtomicLong(0L);

  // Safe JSON dump (for compact storage in attr)
  private static final ObjectMapper ATTR_MAPPER = new ObjectMapper();

  // =========================
  // DEPS
  // =========================
  private final ExternalMediaManager ext;
  private final AiClientFactory aiClientFactory;
  private final CodecFactory factory;
  private final CallSessionManager sessions;
  private final RtpPortAllocator rtpPortAllocator;
  private final CallCompletionService callCompletionService;
  private final StasisAppConfigService stasisService;
  private final CrmCustomerService crmCustomerService;
  private final DspService dspService;
  private final TruncateManagerFactory truncateManagerFactory;
  private final CallerManageService callerIdResolver;
  private final IvrService ivrService;

  private final WebClient web = WebClient.builder().build();

  // Pre-ws inbound PCM buffer (aiRate)
  private final ConcurrentHashMap<String, Deque<byte[]>> preWsInbound = new ConcurrentHashMap<>();

  // Debug WAV writers for decoded inbound PCM
  private final ConcurrentHashMap<String, StereoWavFileWriter> pcmDebugWriters = new ConcurrentHashMap<>();

  private static final long IVR_DTMF_IGNORE_MS = 1200L;


  private static boolean isIvrMode(StasisAppConfig p) {
    return p != null && "ivr".equalsIgnoreCase(nvl(p.getBot_mode()).trim());
  }

  // ========================================================================
  //  Helpers
  // ========================================================================

  private static String nvl(String s) { return s == null ? "" : s; }

  private String basic(String u, String pw) {
    String token = Base64.getEncoder()
        .encodeToString((nvl(u) + ":" + nvl(pw)).getBytes(StandardCharsets.UTF_8));
    return "Basic " + token;
  }

  private static String resolveExternalHostForAsterisk(StasisAppConfig p) {
    if (p == null) return "";
    // external_host is "where Asterisk sends RTP"
    String h = nvl(p.getRtp_external_host()).trim();
    if (!h.isEmpty()) return h;

    // fallback: bind ip usually works if Asterisk is on same LAN
    h = nvl(p.getRtp_bind_ip()).trim();
    return h;
  }

  private StereoWavFileWriter debugWriterFor(String channelId, int rateHz) {
    return pcmDebugWriters.computeIfAbsent(channelId, k -> {
      try {
        String path = "/tmp/pcm_decoded_" + channelId + "_" + System.currentTimeMillis() + ".wav";
        log.warn("PCM_DEBUG creating decoded dump wav: {}", path);
        return new StereoWavFileWriter(path, rateHz);
      } catch (Exception e) {
        log.error("PCM_DEBUG cannot create wav writer: {}", e.getMessage(), e);
        return null;
      }
    });
  }

  private void closeDebugWriter(String channelId) {
    StereoWavFileWriter w = pcmDebugWriters.remove(channelId);
    if (w != null) {
      try { w.close(); } catch (Exception ignore) {}
    }
  }

  // ========================================================================
  //  CONFIG DUMP (NO ASSUMPTIONS)
  // ========================================================================
  private void logConfigOnce(String phase, String channelId, StasisAppConfig p) {
    if (!DEEP_LOGS) return;
    if (channelId == null) channelId = "null";
    if (p == null) {
      log.warn("CFG-DUMP phase={} channel={} cfgNull=true", phase, channelId);
      return;
    }

    boolean already;
    if ("START".equalsIgnoreCase(phase)) {
      already = (cfgDumpedStart.putIfAbsent(channelId, Boolean.TRUE) != null);
    } else {
      already = (cfgDumpedEnd.putIfAbsent(channelId, Boolean.TRUE) != null);
    }
    if (already) return;

    long s = seq.incrementAndGet();

    log.info("CFG-DUMP[{}] phase={} channel={} ---------- BEGIN ----------", s, phase, channelId);
    try {
      log.info("CFG-DUMP[{}] stasis_app_name='{}' org='{}' bot_mode='{}' agent='{}' defaultLang='{}'",
          s,
          nvl(p.getStasis_app_name()),
          nvl(p.getOrganization()),
          nvl(p.getBot_mode()),
          nvl(p.getAgent_name()),
          nvl(p.getAgent_defaultlanguage())
      );

      log.info("CFG-DUMP[{}] ARI restBase='{}' wsUrl='{}' user='{}'",
          s,
          nvl(p.getAri_rest_baseUrl()),
          nvl(p.getAri_ws_url()),
          nvl(p.getAri_username())
      );

      log.info("CFG-DUMP[{}] RTP codec='{}' payloadPt={} clockRateCfg={} frameMsCfg={} bindIp='{}' externalHost='{}'",
          s,
          nvl(p.getRtp_codec()),
          p.getRtp_payload_pt(),
          p.getRtp_clock_rate(),
          p.getRtp_frame_ms(),
          nvl(p.getRtp_bind_ip()),
          nvl(p.getRtp_external_host())
      );

      log.info("CFG-DUMP[{}] AI aiRateHz={} inboundChunkMs={} realtimeModel='{}' transcribeModel='{}' voice='{}'",
          s,
          p.getAi_pcm_sampleRateHz(),
          p.getAi_inbound_chunk_ms(),
          nvl(p.getAi_model_realtime()),
          nvl(p.getAi_model_transcribe()),
          nvl(p.getAi_voice())
      );

      log.info("CFG-DUMP[{}] QUEUE maxMs={} pauseMs={} wmHighPct={} wmLowPct={}",
          s,
          p.getQueue_maxMs(),
          p.getQueue_pauseMs(),
          p.getQueue_watermarkHighPercent(),
          p.getQueue_watermarkLowPercent()
      );

      log.info("CFG-DUMP[{}] RECORD enabled={} basePath='{}'",
          s,
          p.getRecording_enabled(),
          nvl(p.getRecordingLocalBasePath())
      );

      log.info("CFG-DUMP[{}] FLAGS saveCustomer={} saveCallDetails={} callMaxSeconds={}",
          s,
          p.getSavePropertyInventory(),
          p.getSave_Call_Details(),
          p.getCall_max_seconds()
      );

    } catch (Exception ex) {
      log.error("CFG-DUMP[{}] error phase={} channel={} msg={}",
          s, phase, channelId, ex.getMessage(), ex);
    }
    log.info("CFG-DUMP[{}] phase={} channel={} ---------- END ------------", s, phase, channelId);
  }

  // ========================================================================
  //  STASIS START
  // ========================================================================
  public void onStasisStart(String channelId, StasisAppConfig p) {

    // 1) definitive config dump (ONCE)
    logConfigOnce("START", channelId, p);

    if (DEEP_LOGS) log.info("ARI stasis_start enter channel={}", channelId);

    CallSession existing = sessions.get(channelId);
    if (existing != null) {
      if (DEEP_LOGS) log.info("ARI stasis_start duplicate ignored channel={} alreadyActive=true", channelId);
      return;
    }


    // 2) Answer channel
    final String answerUrl = nvl(p.getAri_rest_baseUrl()) + "/channels/" + channelId + "/answer";
    if (DEEP_LOGS) {
      log.info("USED-FOR[ARI] answer channel={} url='{}' user='{}'", channelId, answerUrl, nvl(p.getAri_username()));
    }

    web.post()
        .uri(answerUrl)
        .header(HttpHeaders.AUTHORIZATION, basic(p.getAri_username(), p.getAri_password()))
        .retrieve()
        .bodyToMono(String.class)
        .subscribe(
            r -> { if (DEEP_LOGS) log.debug("ARI answer_ok channel={} respLen={}", channelId, r != null ? r.length() : 0); },
            e -> { if (DEEP_LOGS) log.warn("ARI answer_error channel={} msg={}", channelId, e.getMessage(), e); }
        );

    

    if (isIvrMode(p)) {

    	  try {
    	    CallSession s = sessions.createIvrSession(channelId,p);
    	    s.setIvrCall(true);
    	    s.setRedirectChannel(false);
    	    // Resolve caller using same resolver path
    	    String ariCaller = null;
    	    try { ariCaller = sessions.takeCallerNumber(channelId); } catch (Exception ignore) {}

    	    CallerManageService.ResolutionResult rr = null;
    	    try { rr = callerIdResolver.resolveCaller(p, channelId, ariCaller); } catch (Exception ignore) {}

    	    String resolvedCaller = (rr != null ? nvl(rr.resolvedCaller) : "");

    	    s.setOrganization(nvl(p.getOrganization()));
    	    s.setStasisAppName(nvl(p.getStasis_app_name()));
    	    s.setCallerNumber(resolvedCaller);

    	    if (rr != null && rr.language != null && !rr.language.isBlank()) {
    	    	  s.putAttr("call.language", rr.language.trim());
    	    	}

    	    
    	    // mark as ivr
    	    s.putAttr("call.mode", "ivr");
    	    s.putAttr("ivr.recording", nvl(p.getIvr_recording_path()));

    	    // billing start
    	    CallBillingInfo b = s.getBillingInfo();
    	    if (b != null) {
    	      b.setOrganization(nvl(p.getOrganization()));
    	      b.setStartTime(Instant.now());
    	    }

    	    if (DEEP_LOGS) {
    	      log.info("IVR session_created channel={} app='{}' org='{}' ariCaller='{}' resolvedCaller='{}' source='{}'",
    	          channelId,
    	          nvl(p.getStasis_app_name()),
    	          nvl(p.getOrganization()),
    	          (ariCaller != null ? ariCaller.replaceAll("\\d","*") : ""),
    	          (resolvedCaller != null ? resolvedCaller.replaceAll("\\d","*") : ""),
    	          (rr != null ? nvl(rr.source) : "unknown"));
    	    }

    	  } catch (Exception ex) {
    	    log.error("IVR session_create_failed channel={} msg={}", channelId, ex.getMessage(), ex);
    	  }

    	  ivrService.startIvrCall(channelId, p);

    	  if (DEEP_LOGS) log.info("IVR stasis_start handled channel={} (bypassed RTP/AI)", channelId);
    	  return;
    }

    
    // 3) Allocate dual RTP ports:
    final int rtpInPort = rtpPortAllocator.allocatePort();   // extMediaIn -> VB (caller-only)
    final int rtpOutPort = rtpPortAllocator.allocatePort();  // VB -> extMediaOut (send AI) + learn PT/peer

    if (DEEP_LOGS) {
      log.info("USED-FOR[RTP] allocatePorts channel={} rtpInPort={} rtpOutPort={} bindIp='{}' externalHost='{}'",
          channelId, rtpInPort, rtpOutPort, nvl(p.getRtp_bind_ip()), nvl(p.getRtp_external_host()));
    }
    
    // 4) Auto hangup (demo)
    Integer maxSec = p.getCall_max_seconds();
    if (maxSec != null && maxSec > 0) {
      if (DEEP_LOGS) log.info("USED-FOR[DEMO] autohangup_arm channel={} maxSeconds={}", channelId, maxSec);

      Mono.delay(Duration.ofSeconds(maxSec))
          .subscribe(t -> {
            if (sessions.get(channelId) == null) {
              if (DEEP_LOGS) log.info("DEMO autohangup skip_no_session channel={}", channelId);
              return;
            }
            String hangUrl = nvl(p.getAri_rest_baseUrl()) + "/channels/" + channelId;
            if (DEEP_LOGS) log.info("DEMO autohangup firing channel={} url={}", channelId, hangUrl);

            web.delete()
                .uri(hangUrl)
                .header(HttpHeaders.AUTHORIZATION, basic(p.getAri_username(), p.getAri_password()))
                .retrieve().bodyToMono(String.class)
                .subscribe(
                    resp -> { if (DEEP_LOGS) log.info("DEMO autohangup ok channel={} respLen={}", channelId, resp != null ? resp.length() : 0); },
                    err  -> { if (DEEP_LOGS) log.warn("DEMO autohangup error channel={} msg={}", channelId, err.getMessage(), err); }
                );
          });
    } else {
      if (DEEP_LOGS) log.info("DEMO autohangup disabled channel={}", channelId);
    }

    // 5) Build ARI media graph (2 bridges):
    final String app = nvl(p.getStasis_app_name());
    final String fmtCodec = nvl(p.getRtp_codec());
    final String externalHost = resolveExternalHostForAsterisk(p);

    final String talkBridgeName = "talk-" + channelId;
    final String tapBridgeName  = "tap-" + channelId;

    final String extOutId = "extout-" + channelId;
    final String extInId  = "extin-" + channelId;
    final String snoopId  = "snoopin-" + channelId;

    if (DEEP_LOGS) {
      log.info("USED-FOR[ARI] graph_init channel={} app='{}' talkBridge='{}' tapBridge='{}' extHost='{}' rtpInPort={} rtpOutPort={} codec='{}'",
          channelId, app, talkBridgeName, tapBridgeName, externalHost, rtpInPort, rtpOutPort, fmtCodec);
    }

    Mono<String> talkBridgeMono = ext.createBridge(p, "mixing", talkBridgeName);
    Mono<String> tapBridgeMono  = ext.createBridge(p, "mixing", tapBridgeName);

    Mono.zip(talkBridgeMono, tapBridgeMono)
        .flatMap(tuple -> {
          final String talkBridgeId = tuple.getT1();
          final String tapBridgeId  = tuple.getT2();

          // extMediaOut: joins talkBridge
          Mono<String> extOutMono =
              ext.createExternalMedia(p, app, extOutId, externalHost, rtpOutPort, fmtCodec);

          // snoop inbound on caller
          Mono<String> snoopMono =
              ext.createSnoopInbound(p, channelId, app, snoopId);

          // extMediaIn: joins tapBridge
          Mono<String> extInMono =
              ext.createExternalMedia(p, app, extInId, externalHost, rtpInPort, fmtCodec);

          return Mono.zip(extOutMono, snoopMono, extInMono)
              .flatMap(t2 -> {
                final String extOutChanId = t2.getT1();
                final String snoopChanId  = t2.getT2();
                final String extInChanId  = t2.getT3();

                Mono<Void> addCallerToTalk = ext.addChannelToBridge(p, talkBridgeId, channelId);
                Mono<Void> addExtOutToTalk = ext.addChannelToBridge(p, talkBridgeId, extOutChanId);

                Mono<Void> addSnoopToTap = ext.addChannelToBridge(p, tapBridgeId, snoopChanId);
                Mono<Void> addExtInToTap = ext.addChannelToBridge(p, tapBridgeId, extInChanId);

                // Resolve Asterisk RTP peer for extMediaOut (UNICASTRTP_LOCAL_*).
                Mono<ExternalMediaManager.UnicastRtpPeer> peerMono =
                	    ext.getUnicastRtpPeer(p, extOutChanId)
                	        .doOnError(e -> log.warn("UNICASTRTP peer fetch failed channel={} extOutChanId={} msg={}",
                	            channelId, extOutChanId, e.getMessage()))
                	        .onErrorResume(e -> Mono.empty());


                Mono<GraphReady> graphMono =
                	    peerMono
                	        .map(peer -> new GraphReady(
                	            talkBridgeId, tapBridgeId,
                	            snoopChanId,
                	            extInChanId, extOutChanId,
                	            rtpInPort, rtpOutPort,
                	            peer
                	        ))
                	        .switchIfEmpty(Mono.fromCallable(() -> new GraphReady(
                	            talkBridgeId, tapBridgeId,
                	            snoopChanId,
                	            extInChanId, extOutChanId,
                	            rtpInPort, rtpOutPort,
                	            null
                	        )));

               return Mono.when(addCallerToTalk, addExtOutToTalk, addSnoopToTap, addExtInToTap)
                	    .then(graphMono);

              });
        })
        .subscribe(
            graph -> {
              if (DEEP_LOGS) {
                log.info("ARI graph_ready channel={} talkBridgeId={} tapBridgeId={} snoop={} extIn={} extOut={} rtpInPort={} rtpOutPort={} outPeer={}",
                    channelId,
                    graph.talkBridgeId, graph.tapBridgeId,
                    graph.snoopChannelId, graph.extMediaInChannelId, graph.extMediaOutChannelId,
                    graph.rtpInPort, graph.rtpOutPort,
                    (graph.extMediaOutPeer != null ? graph.extMediaOutPeer.toString() : "null"));
              }
              startRtpAndAi(channelId, p, graph);
            },
            e -> log.error("ARI external_media_flow_error channel={} msg={}", channelId, e.getMessage(), e)
        );
  }

  // ========================================================================
  //  STASIS END
  // ========================================================================
  public void onStasisEnd(String channelId, StasisAppConfig cfg) {

    if (cfg != null) logConfigOnce("END", channelId, cfg);
    else if (DEEP_LOGS) log.warn("CFG-DUMP phase=END channel={} cfgNull=true (cannot print config)", channelId);

    if (DEEP_LOGS) log.info("ARI stasis_end enter channel={}", channelId);

    CallSession s = sessions.get(channelId);
    if (s == null) {
      if (DEEP_LOGS) log.info("ARI stasis_end no_session channel={}", channelId);
      return;
    }

    Boolean transferRequested = s.getAttr("transfer.requested", Boolean.class);
    if (Boolean.TRUE.equals(transferRequested)) {
      String tdnis = s.getAttr("transfer.dnis", String.class);
      String tph  = s.getAttr("transfer.phone", String.class);
      log.warn("TRANSFER stasis_end observed channel={} dnis={} phone={}", channelId, tdnis, tph);
    }

    boolean ivrCall = isIvrMode(cfg);
    
    if (DEEP_LOGS) log.info("IVR Call ={}", ivrCall);
    
    if(ivrCall) {
    	ivrService.clearIvrState(channelId);
    }
    else {

        // DSP dispose
        try {
          if (s.getApm() != null) {
            s.getApm().close();
            s.setApm(null);
          }
        } catch (Exception ignore) {}

        
        // Mark stopping so RTP thread can flush decoder tail on that thread
        try {
          s.markStopping();
          if (DEEP_LOGS) log.info("CALL stop_marked channel={}", channelId);
        } catch (Exception ignore) {}

        if (DEEP_LOGS) {
          log.info("ARI stasis_end session_found channel={} org='{}' rtpInPort={} rtpOutPort={} inLearnedPt={} queueDepthMs={}",
              channelId,
              nvl(s.getOrganization()),
              s.getRtpInPort(),
              s.getRtpOutPort(),
              s.getRtpInLearnedPayloadType(),
              (s.getOutboundQueue() != null ? s.getOutboundQueue().depthMs() : -1)
          );
        }

        // Stop playout
        try {
          PlayoutScheduler ps = s.getPlayoutScheduler();
          if (ps != null) {
            if (DEEP_LOGS) log.info("PLAYOUT stop begin channel={}", channelId);
            ps.stop();
            if (DEEP_LOGS) log.info("PLAYOUT stop ok channel={}", channelId);
          }
        } catch (Exception e) {
          if (DEEP_LOGS) log.warn("PLAYOUT stop error channel={} msg={}", channelId, e.getMessage(), e);
        }

        // Close RTP endpoints (dual)
        try {
          RtpSymmetricEndpoint inEp = s.getRtpInEndpoint();
          if (inEp != null) {
            if (DEEP_LOGS) log.info("RTP inEndpoint_close begin channel={}", channelId);
            inEp.close();
            if (DEEP_LOGS) log.info("RTP inEndpoint_close ok channel={}", channelId);
          }
        } catch (Exception e) {
          if (DEEP_LOGS) log.warn("RTP inEndpoint_close error channel={} msg={}", channelId, e.getMessage(), e);
        }

        try {
          RtpSymmetricEndpoint outEp = s.getRtpOutEndpoint();
          if (outEp != null) {
            if (DEEP_LOGS) log.info("RTP outEndpoint_close begin channel={}", channelId);
            outEp.close();
            if (DEEP_LOGS) log.info("RTP outEndpoint_close ok channel={}", channelId);
          }
        } catch (Exception e) {
          if (DEEP_LOGS) log.warn("RTP outEndpoint_close error channel={} msg={}", channelId, e.getMessage(), e);
        }

        // Stop bot WS
        try {
          WebSocket ws = s.getAiWebSocket();
          if (ws != null) {
            BotClient ai = s.getBotClient();
            if (DEEP_LOGS) {
              log.info("AI-WS sendStop begin channel={} wsHash={} botClient={}",
                  channelId, System.identityHashCode(ws),
                  (ai != null ? ai.getClass().getSimpleName() : "null"));
            }
            if (ai != null) ai.sendStop(s, ws, "stasis_end");
            if (DEEP_LOGS) log.info("AI-WS sendStop ok channel={}", channelId);
          }
        } catch (Exception e) {
          if (DEEP_LOGS) log.warn("AI-WS sendStop error channel={} msg={}", channelId, e.getMessage(), e);
        }

        // Recording stop
        try {
          if (s.getRecordingManager() != null) {
            if (DEEP_LOGS) {
              log.info("RECORD stopAndCloseBlocking begin channel={} path='{}'",
                  channelId,
                  (s.getBillingInfo() != null ? nvl(s.getBillingInfo().getRecordingPath()) : "null"));
            }
            s.getRecordingManager().stopAndCloseBlocking();
            closeDebugWriter(channelId);
            if (DEEP_LOGS) log.info("RECORD stopAndClose ok channel={}", channelId);
          } else {
            closeDebugWriter(channelId);
          }
        } catch (Exception e) {
          if (DEEP_LOGS) log.warn("RECORD stopAndCloseBlocking error channel={} msg={}", channelId, e.getMessage(), e);
        }
    }

    // Completion
    try {
      if (DEEP_LOGS) log.info("USED-FOR[COMPLETE] finalizeAndPersist enter channel={} cfgNull={}", channelId, (cfg == null));
      callCompletionService.finalizeAndPersist(s, "stasis_end", cfg,ivrCall);
      if (DEEP_LOGS) log.info("USED-FOR[COMPLETE] finalizeAndPersist returned channel={}", channelId);
    } catch (Exception ex) {
      log.error("COMPLETE invoke_failed channel={} msg={}", channelId, ex.getMessage(), ex);
    }

    // Session removal
    try {
      if (DEEP_LOGS) log.info("SESSION removeStasisApp begin channel={}", channelId);
      sessions.removeStasisApp(channelId);
      if (DEEP_LOGS) log.info("SESSION removeStasisApp ok channel={}", channelId);
    } catch (Exception ex) {
      log.error("SESSION removeStasisApp error channel={} msg={}", channelId, ex.getMessage(), ex);
    }

    try {
      if (DEEP_LOGS) log.info("SESSION remove(begin) channel={}", channelId);
      sessions.remove(channelId);
      if (DEEP_LOGS) log.info("SESSION remove(ok) channel={}", channelId);
    } catch (Exception ex) {
      log.error("SESSION remove(error) channel={} msg={}", channelId, ex.getMessage(), ex);
    }

    if (DEEP_LOGS) log.info("ARI stasis_end exit channel={} session_removed=true", channelId);
  }

  // ========================================================================
  //  START FULL RTP + AI PIPELINE (DUAL RTP)
  // ========================================================================
  private void startRtpAndAi(String channelId, StasisAppConfig p, GraphReady graph) {
    if (DEEP_LOGS) {
      log.info("ARI startRtpAndAi enter channel={} rtpInPort={} rtpOutPort={}", channelId, graph.rtpInPort, graph.rtpOutPort);
      log.info("USED-FOR[AI-IN] inboundChunkMs={} aiRateHz={}", p.getAi_inbound_chunk_ms(), p.getAi_pcm_sampleRateHz());
      log.info("USED-FOR[RTP] bindIp='{}' frameMsCfg={}", nvl(p.getRtp_bind_ip()), p.getRtp_frame_ms());
    }

    try {
      // Codec (RTP decode + encode/packetize)
      AudioCodec codec = factory.create(p);
      int codecRate = codec.sampleRate();

      if (DEEP_LOGS) {
        log.info("USED-FOR[CODEC] codecClass={} codecRateHz={} rtpCodecCfg='{}'",
            codec.getClass().getSimpleName(), codecRate, nvl(p.getRtp_codec()));
      }

      // Warmup resampler (CPU OPT)
      Integer aiRateCfg = p.getAi_pcm_sampleRateHz();
      int aiRateWarm = (aiRateCfg != null ? aiRateCfg : codecRate);
      try {
        if (aiRateWarm != codecRate) {
          warmupResamplerOnce(codecRate, aiRateWarm);
          warmupResamplerOnce(aiRateWarm, codecRate);
          if (DEEP_LOGS) log.debug("USED-FOR[RESAMPLE] warmup_ok codecRateHz={} aiRateHz={}", codecRate, aiRateWarm);
        } else {
          if (DEEP_LOGS) log.debug("USED-FOR[RESAMPLE] warmup_skip_same_rate rateHz={}", codecRate);
        }
      } catch (Exception e) {
        if (DEEP_LOGS) log.warn("USED-FOR[RESAMPLE] warmup_fail msg={}", e.getMessage());
      }

      // Outbound RTP packetizer (VB -> Asterisk)
      Integer ptCfg = p.getRtp_payload_pt();
      int fallbackPt = (ptCfg != null ? ptCfg : 0);
      int initialSsrcOut = ThreadLocalRandom.current().nextInt();

      if (DEEP_LOGS) {
        log.info("USED-FOR[RTP-OUT] packetizer_init fallbackPt={} clockRateHz(used)={} initialSsrcOut={}",
            fallbackPt, codecRate, initialSsrcOut);
      }

      final RtpPacketizer packetizerOut = new RtpPacketizer(fallbackPt, codecRate, initialSsrcOut);

      // RTP IN endpoint: Asterisk -> VB (caller-only from extMediaIn)
      if (DEEP_LOGS) {
        log.info("USED-FOR[RTP-IN] endpoint_init bindIp='{}' localPort={}", nvl(p.getRtp_bind_ip()), graph.rtpInPort);
      }

      RtpSymmetricEndpoint rtpInEndpoint = new RtpSymmetricEndpoint(
          p.getRtp_bind_ip(),
          graph.rtpInPort,

          payload -> {
            CallSession s = sessions.get(channelId);
            WebSocket ws = (s != null ? s.getAiWebSocket() : null);

            // If call is stopping, flush decoder tail on THIS RTP thread (ThreadLocal)
            if (s != null && s.isStopping()) {
              try {
                byte[] tail = codec.flushDecoderTailPcm16();
                if (tail != null && tail.length > 0) {
                  if (s.getRecordingManager() != null) s.getRecordingManager().writeInboundAsync(tail);
                  if (RTP_DEEP_LOGS) log.info("PCMU-FLUSH(in) tail_appended channel={} tailBytes={}", channelId, tail.length);
                }
              } catch (Exception ex) {
                log.warn("PCMU-FLUSH(in) error channel={} msg={}", channelId, ex.getMessage());
              } finally {
                try { codec.resetDecoderState(); } catch (Exception ignore) {}
              }
              return;
            }

            try {
              Integer learnedPtObj = (s != null ? s.getRtpInLearnedPayloadType() : null);
              int learnedPt = (learnedPtObj != null ? learnedPtObj : -1);

              if (RTP_DEEP_LOGS) {
                log.info("RTP-IN raw channel={} payloadBytes={} inLearnedPt={} fallbackPt={} codec={} codecRateHz={} rtpClockCfg={} frameMsCfg={}",
                    channelId,
                    (payload != null ? payload.length : -1),
                    learnedPt,
                    fallbackPt,
                    codec.getClass().getSimpleName(),
                    codecRate,
                    p.getRtp_clock_rate(),
                    p.getRtp_frame_ms()
                );
              }

              byte[] pcm = codec.decodePayloadToPcm(payload);

	           // ---------------- DSP (near-end) Phase-1
	           // Must run at codecRate (telephony clock), BEFORE any resample to aiRate.
	           // ----------------
	           pcm = dspService.maybeDspNearEnd(s, pcm, codecRate);
	           
		        // Barge-in side tap (near-end, after DSP)
		        // Does NOT change AI chunking; only observes audio.
		        try {
		          if (s != null && s.getBargeController() != null && ws != null) {
		            s.getBargeController().onNearEndPcm(s, ws, pcm, System.nanoTime());
		          }
		        } catch (Exception e) {
		          // keep RTP thread safe
		          log.warn("BARGE onNearEndPcm failed channel={} msg={}", channelId, e.getMessage());
		        }

	        
              if (PCM_DEBUG_DUMP) {
                StereoWavFileWriter w = debugWriterFor(channelId, codecRate);
                if (w != null) {
                  try {
                    w.writeStereo(pcm, null); // decoded inbound LEFT, silence RIGHT
                  } catch (Exception e) {
                    log.error("PCM_DEBUG write error: {}", e.getMessage());
                  }
                }
              }

              Integer aiRateCfg2 = p.getAi_pcm_sampleRateHz();
              int aiRate = (aiRateCfg2 != null ? aiRateCfg2 : codecRate);
              byte[] pcmAi = (codecRate == aiRate) ? pcm : AudioTranscoder.toMono16LE(pcm, codecRate, aiRate);

              // no session/ws yet => prebuffer
              if (s == null || ws == null) {
                Deque<byte[]> q = preWsInbound.computeIfAbsent(channelId, k -> new ArrayDeque<>());
                if (q.size() >= PRE_WS_MAX_CHUNKS) q.pollFirst();
                q.addLast(pcmAi);
                return;
              }

              // Recording inbound tap (codecRate)
              if (s.getRecordingManager() != null) s.getRecordingManager().writeInboundAsync(pcm);

              appendInboundAndSendChunks(s, ws, pcmAi, aiRate, p.getAi_inbound_chunk_ms());

            } catch (Exception ex) {
              log.error("RTP-IN to_ai_error channel={} msg={}", channelId, ex.getMessage(), ex);
            }
          },

          learnedPt -> {
            CallSession s = sessions.get(channelId);
            if (s != null) {
              s.setRtpInLearnedPayloadType(learnedPt);
              if (DEEP_LOGS) log.info("RTP-LEARN(in) pt_apply channel={} learnedPt={}", channelId, learnedPt);
            }
          },

          learnedSsrc -> {
            CallSession s = sessions.get(channelId);
            if (s != null) {
              s.setRtpInLearnedSSrc(learnedSsrc);
              if (DEEP_LOGS) log.info("RTP-LEARN(in) ssrc_apply channel={} learnedSsrc={}", channelId, learnedSsrc);
            }
          }
      );

      InetSocketAddress outFixedPeer = null;
      if (graph.extMediaOutPeer != null
          && graph.extMediaOutPeer.ip != null
          && !graph.extMediaOutPeer.ip.isBlank()
          && graph.extMediaOutPeer.port > 0) {

        final String rawIp = graph.extMediaOutPeer.ip.trim();

        try {
          outFixedPeer = new InetSocketAddress(
              java.net.InetAddress.getByName(rawIp),
              graph.extMediaOutPeer.port
          );
        } catch (Exception e) {
          // keep fallback path: learnPeerOut=true
          outFixedPeer = null;
          log.warn("RTP-OUT fixedPeer resolve_failed channel={} ip='{}' port={} msg={}",
              channelId, rawIp, graph.extMediaOutPeer.port, e.getMessage());
        }
      }


      // RTP OUT endpoint:
      // - Used to send AI audio back to Asterisk.
      // - Learns PT from incoming packets.
      // - If UNICASTRTP_LOCAL_* available, we set FIXED PEER and disable peer learning to break deadlock.
      boolean learnPeerOut = (outFixedPeer == null);

      if (DEEP_LOGS) {
    	  log.info("RTP-OUT fixedPeer_check channel={} fixedPeer={} fixedAddrNull={} learnPeerOut={}",
    	      channelId,
    	      (outFixedPeer != null ? outFixedPeer.toString() : "null"),
    	      (outFixedPeer != null && outFixedPeer.getAddress() == null),
    	      learnPeerOut);
    	}
      
      if (DEEP_LOGS) {
        log.info("USED-FOR[RTP-OUT] endpoint_init bindIp='{}' localPort={} learnPeerOut={} fixedPeer={}",
            nvl(p.getRtp_bind_ip()),
            graph.rtpOutPort,
            learnPeerOut,
            (outFixedPeer != null ? outFixedPeer.toString() : "null"));
      }

      RtpSymmetricEndpoint rtpOutEndpoint = new RtpSymmetricEndpoint(
          p.getRtp_bind_ip(),
          graph.rtpOutPort,
          learnPeerOut,
          outFixedPeer,

          payload -> {
            // no-op (we only need peer/PT learn thread alive)
          },

          learnedPt -> {
            // Apply learned PT to packetizerOut so outbound packets match what Asterisk expects
            try { packetizerOut.setPayloadType(learnedPt); } catch (Exception ignore) {}
            if (DEEP_LOGS) {
              log.info("RTP-LEARN(out) pt_apply channel={} learnedPt={} packetizerOutPtNow={}",
                  channelId, learnedPt, packetizerOut.payloadType());
            }
          },

          learnedSsrc -> {
            if (DEEP_LOGS) log.debug("RTP-LEARN(out) ssrc_seen channel={} learnedSsrc={}", channelId, learnedSsrc);
          }
      );

      // Always visible warning if fixed peer could not be resolved (this explains silent outbound)
      if (outFixedPeer == null) {
        log.warn("RTP-OUT fixedPeer NOT available (UNICASTRTP vars missing). channel={} extOutChanId={}",
            channelId, graph.extMediaOutChannelId);
      }

      int frameMs = Math.max(10, (p.getRtp_frame_ms() != null ? p.getRtp_frame_ms() : 20));
      if (DEEP_LOGS) {
        log.info("USED-FOR[PLAYOUT] frameMs(used)={} queueMaxMs={} pauseMs={} wmHighPct={} wmLowPct={}",
            frameMs, p.getQueue_maxMs(), p.getQueue_pauseMs(),
            p.getQueue_watermarkHighPercent(), p.getQueue_watermarkLowPercent());
      }

      // Create session (single source of truth)
      CallSession s = sessions.create(
          channelId,
          p.getQueue_maxMs(),
          packetizerOut,
          rtpInEndpoint,
          rtpOutEndpoint,
          codec,
          frameMs,
          p
      );
      
      if(p.getBarinEnabled()) {
		   // Barge-in controller per call (phase: barge-in)
		   // Values are config-driven; if null in DB, pick safe defaults in code.
		   int startupIgnoreMs = 600;
		   int cooldownMs = 1200;
		   int confirmMs = 120;
		   int aiSpeakingDepthMs = 60; // AI "speaking" if >=60ms queued
		   int energyThr = (p.getBarge_in_energy_threshold() != null ? p.getBarge_in_energy_threshold() : 400);
		
		   s.setBargeController(new com.mylinehub.voicebridge.barge.BargeInController(
		       codecRate,
		       startupIgnoreMs,
		       cooldownMs,
		       confirmMs,
		       aiSpeakingDepthMs,
		       energyThr,
		       dspService
		   ));
      }

      
      dspService.initDspPerCall(s, p, codecRate);
      s.setTruncateManager(truncateManagerFactory.get(p.getBot_mode()));

      if (s.getPlayoutScheduler() != null) {
    	  s.getPlayoutScheduler().setSession(s);
    	}

      // Fill per-call variables
      s.setOrganization(p.getOrganization());
      s.setStasisAppName(p.getStasis_app_name());
      s.setCodec(codec);

      s.setTalkBridgeId(graph.talkBridgeId);
      s.setTapBridgeId(graph.tapBridgeId);
      s.setSnoopChannelId(graph.snoopChannelId);
      s.setExtMediaInChannelId(graph.extMediaInChannelId);
      s.setExtMediaOutChannelId(graph.extMediaOutChannelId);

      s.setRtpInPort(graph.rtpInPort);
      s.setRtpOutPort(graph.rtpOutPort);
      s.setRtpInLearnedPayloadType(null);

       // Caller number (CURRENT USE-CASE): ARI caller -> UUI(X_UUI) -> fallback
      try {
        String ariCaller = sessions.takeCallerNumber(channelId); // may be null/extension

        if (DEEP_LOGS) {
          log.warn("CALLER pre_resolve channel={} ariCaller='{}'",
              channelId, (ariCaller != null ? ariCaller.replaceAll("\\d", "*") : ""));
        }

        CallerManageService.ResolutionResult rr =
            callerIdResolver.resolveCaller(p, channelId, ariCaller);

        s.setCallerNumber(rr.resolvedCaller);

        if (rr != null && rr.language != null && !rr.language.isBlank()) {
        	  s.putAttr("call.language", rr.language);
        }
        
        log.warn("CALLER resolved channel={} source={} original='{}' final='{}' lang='{}'",
        	    channelId,
        	    rr.source,
        	    (rr.originalCaller != null ? rr.originalCaller.replaceAll("\\d", "*") : ""),
        	    (rr.resolvedCaller != null ? rr.resolvedCaller.replaceAll("\\d", "*") : ""),
        	    (rr.language != null ? rr.language : ""));

      } catch (Exception ex) {
        s.setCallerNumber("unknown");
        log.warn("CALLER number_set_fail channel={} msg={}", channelId, ex.getMessage());
      }


      // CRM (blocking)
      fetchAndStoreCustomerInfoBlocking(s, p);

      // Billing init
      CallBillingInfo billing = s.getBillingInfo();
      if (billing != null) {
        billing.setOrganization(p.getOrganization());
        billing.setStartTime(Instant.now());
        if (DEEP_LOGS) log.info("USED-FOR[BILLING] startTime={} org='{}' channel={}", billing.getStartTime(), nvl(p.getOrganization()), channelId);
      }

      // Recording
      if (Boolean.TRUE.equals(p.getRecording_enabled()) && billing != null) {
        String caller = (s.getCallerNumber() != null && !s.getCallerNumber().isEmpty()) ? s.getCallerNumber() : p.getOrganization();
        String fileName = caller + "_" + channelId + "_" + System.currentTimeMillis() + ".wav";
        String fullPath = nvl(p.getRecordingLocalBasePath()) + "/" + fileName;
        billing.setRecordingFileName(fileName);
        billing.setRecordingPath(fullPath);

        if (DEEP_LOGS) {
          log.info("USED-FOR[RECORD] enabled=true file='{}' fullPath='{}' sampleRateHz(used)={}",
              fileName, fullPath, codecRate);
        }

        CallRecordingManager rm = new CallRecordingManager(fullPath, codec.sampleRate());
        s.setRecordingManager(rm);
        s.getOutboundQueue().setRecordTapEnabled(true);
        rm.start();
      } else {
        if (DEEP_LOGS) log.info("USED-FOR[RECORD] enabled=false channel={}", channelId);
      }

      // AI selection + connect
      String mode = p.getBot_mode();
      BotClient ai = aiClientFactory.get(mode);
      s.setBotClient(ai);

      if (DEEP_LOGS) {
        log.info("USED-FOR[AI-WS] connect botMode='{}' resolvedClient={} channel={}",
            nvl(mode), (ai != null ? ai.getClass().getSimpleName() : "null"), channelId);
      }

      WebSocket ws = (ai != null ? ai.connect(s, p) : null);
      s.setAiWebSocket(ws);
      if (ws != null) sessions.mapWebSocket(ws, s);

      if (DEEP_LOGS) {
        log.info("AI-WS connect_ok channel={} wsHash={} mapped={}", channelId, (ws != null ? System.identityHashCode(ws) : 0), (ws != null));
      }

      // Flush pre-ws audio
      Deque<byte[]> q = preWsInbound.remove(channelId);
      if (q != null && !q.isEmpty() && ai != null && ws != null) {
        int flushed = 0;
        while (!q.isEmpty()) {
          byte[] early = q.pollFirst();
          ai.sendAudioChunk(s, ws, early);
          flushed++;
        }
        if (DEEP_LOGS) {
          log.info("USED-FOR[AI-IN] prews_flush channel={} flushedChunks={} approxMs={}",
              channelId, flushed, flushed * PRE_WS_CHUNK_MS);
        }
      }

      // Start playout
      if (s.getPlayoutScheduler() != null) {
        s.getPlayoutScheduler().start();
        if (DEEP_LOGS) log.info("PLAYOUT start_ok channel={}", channelId);
      }

    } catch (Exception e) {
      log.error("ARI start_rtp_ai_error channel={} msg={}", channelId, e.getMessage(), e);
    }
  }

  // ========================================================================
  //  PCM ACCUMULATION & SEND TO AI
  // ========================================================================
  private void appendInboundAndSendChunks(CallSession session,
                                         WebSocket ws,
                                         byte[] pcmAi,
                                         int aiRate,
                                         int chunkMs) {

    if (pcmAi == null || pcmAi.length == 0) return;

    int effectiveChunkMs = Math.max(10, chunkMs > 0 ? chunkMs : 20);
    int samplesPerChunk = (aiRate * effectiveChunkMs) / 1000;
    int bytesPerChunk = samplesPerChunk * 2;

    if (RTP_DEEP_LOGS) {
      log.debug("USED-FOR[AI-IN] chunk_calc channel={} aiRateHz={} chunkMs(used)={} bytesPerChunk={} pcmAiBytes={}",
          session.getChannelId(), aiRate, effectiveChunkMs, bytesPerChunk, pcmAi.length);
    }

    byte[] buf = session.getInboundPcmBuffer();
    int used = session.getInboundPcmBytes();

    if (buf == null) {
      buf = new byte[bytesPerChunk * 4];
      used = 0;
    }

    int required = used + pcmAi.length;
    if (required > buf.length) {
      int newSize = Math.max(required, buf.length * 2);
      byte[] bigger = new byte[newSize];
      System.arraycopy(buf, 0, bigger, 0, used);
      buf = bigger;
    }

    System.arraycopy(pcmAi, 0, buf, used, pcmAi.length);
    used += pcmAi.length;

    int offset = 0;
    while (used - offset >= bytesPerChunk) {
      byte[] chunk = new byte[bytesPerChunk];
      System.arraycopy(buf, offset, chunk, 0, bytesPerChunk);
      offset += bytesPerChunk;

      BotClient ai = session.getBotClient();
      if (ai != null) {
        ai.sendAudioChunk(session, ws, chunk);
      }
    }

    int remaining = used - offset;
    if (remaining > 0) {
      System.arraycopy(buf, offset, buf, 0, remaining);
    }

    session.setInboundPcmBuffer(buf);
    session.setInboundPcmBytes(remaining);
  }

  public void onDtmfReceived(String channelId, String digit, int durationMs) {

	  // ----------------------------
	  // IVR MODE: collect dtmf only
	  // ----------------------------
	  if (ivrService.isIvrActive(channelId)) {
	    ivrService.onIvrDtmf(channelId, digit);
	    return;
	  }

	  // ----------------------------
	  // AI MODE (existing behavior)
	  // ----------------------------
	  CallSession s = sessions.get(channelId);
	  if (s == null) {
	    if (DTMF_DEEP_LOGS) log.info("DTMF drop_no_session channel={} digit={}", channelId, digit);
	    return;
	  }

	  WebSocket ws = s.getAiWebSocket();
	  if (ws == null) {
	    if (DTMF_DEEP_LOGS) log.info("DTMF drop_no_ws channel={} digit={}", channelId, digit);
	    return;
	  }

	  BotClient ai = s.getBotClient();
	  if (DTMF_DEEP_LOGS) {
	    log.info("DTMF forward channel={} digit={} durationMs={} botClient={}",
	        channelId, digit, durationMs, (ai != null ? ai.getClass().getSimpleName() : "null"));
	  }

	  if (ai != null) {
	    ai.sendDtmf(s, ws, digit);
	  }
	}


  // ========================================================================
  //  RESAMPLE WARMUP
  // ========================================================================
  private static long warmKey(int a, int b) {
    return (((long) a) << 32) | (b & 0xffffffffL);
  }

  private static byte[] warmBuf20ms(int rateHz) {
    int samples = Math.max(1, rateHz / 50); // 20ms
    int bytes = samples * 2; // PCM16 mono
    return new byte[bytes];
  }

  private static void warmupResamplerOnce(int fromHz, int toHz) {
    if (fromHz <= 0 || toHz <= 0 || fromHz == toHz) return;
    long k = warmKey(fromHz, toHz);
    byte[] buf = RESAMPLE_WARMUP_BUF.computeIfAbsent(k, kk -> warmBuf20ms(fromHz));
    AudioTranscoder.toMono16LE(buf, fromHz, toHz);
  }

  // ========================================================================
  //  CRM CUSTOMER INFO (BLOCKING)
  // ========================================================================
  private void fetchAndStoreCustomerInfoBlocking(CallSession s, StasisAppConfig cfg) {
    if (s == null || cfg == null) return;

    final String channelId = s.getChannelId();
    final String app = cfg.getStasis_app_name();

    final String org =
        (s.getOrganization() != null && !s.getOrganization().isBlank())
            ? s.getOrganization()
            : cfg.getOrganization();

    final String caller = s.getCallerNumber();

    if (crmCustomerService == null || stasisService == null) {
      if (DEEP_LOGS) log.info("ATTR customerInfo_skip channel={} reason=service_null", channelId);
      return;
    }
    if (app == null || app.isBlank()) {
      if (DEEP_LOGS) log.info("ATTR customerInfo_skip channel={} reason=stasis_app_blank", channelId);
      return;
    }
    if (org == null || org.isBlank()) {
      if (DEEP_LOGS) log.info("ATTR customerInfo_skip channel={} reason=org_blank", channelId);
      return;
    }
    if (caller == null || caller.isBlank() || "unknown".equalsIgnoreCase(caller.trim())) {
      if (DEEP_LOGS) log.info("ATTR customerInfo_skip channel={} reason=caller_blank caller='{}'", channelId, nvl(caller));
      return;
    }

    // instruction check (fetchCustomerInfo flag)
    try {
      var ins = stasisService.getInstructionOrNull(app);
      boolean fetch = ins != null && ins.isActive() && Boolean.TRUE.equals(ins.isFetchCustomerInfo());
      if (!fetch) {
        if (DEEP_LOGS) log.info("ATTR customerInfo_skip channel={} app={} reason=fetchCustomerInfo_false", channelId, nvl(app));
        return;
      }
    } catch (Exception ex) {
      if (DEEP_LOGS) log.warn("ATTR customerInfo_skip channel={} reason=instruction_error msg={}", channelId, ex.getMessage());
      return;
    }

    try {
      long t0 = System.nanoTime();

      com.mylinehub.voicebridge.service.dto.CrmCustomerDto dto =
          crmCustomerService
              .getByPhoneNumberAndOrganization(app, org, caller)
              .timeout(Duration.ofMillis(CRM_FETCH_TIMEOUT_MS))
              .onErrorResume(e -> Mono.empty())
              .block();

      long ms = Duration.ofNanos(System.nanoTime() - t0).toMillis();

      if (dto == null) {
        if (DEEP_LOGS) log.info("ATTR customerInfo_not_found channel={} app={} org={} caller={} ms={}",
            channelId, nvl(app), nvl(org), nvl(caller), ms);
        return;
      }

      String info = toCompactCustomerInfoString(dto);
      if (info == null || info.isBlank()) {
        if (DEEP_LOGS) log.info("ATTR customerInfo_empty channel={} app={} org={} caller={} ms={}",
            channelId, nvl(app), nvl(org), nvl(caller), ms);
        return;
      }

      s.putAttr(CallSession.ATTR_CUSTOMER_INFO, info);

      if (DEEP_LOGS) {
        log.info("ATTR customerInfo_set channel={} app={} org={} caller={} ms={} len={}",
            channelId, nvl(app), nvl(org), nvl(caller), ms, info.length());
      }

    } catch (Exception ex) {
      if (DEEP_LOGS) {
        log.warn("ATTR customerInfo_error channel={} app={} org={} caller={} msg={}",
            channelId, nvl(app), nvl(org), nvl(caller), ex.getMessage());
      }
    }
  }

  private String toCompactCustomerInfoString(com.mylinehub.voicebridge.service.dto.CrmCustomerDto dto) {
    if (dto == null) return null;
    try {
      return ATTR_MAPPER.writeValueAsString(dto);
    } catch (Exception e) {
      return String.valueOf(dto);
    }
  }

  // ========================================================================
  //  Small internal DTO
  // ========================================================================
  private static final class GraphReady {
    final String talkBridgeId;
    final String tapBridgeId;
    final String snoopChannelId;
    final String extMediaInChannelId;
    final String extMediaOutChannelId;
    final int rtpInPort;
    final int rtpOutPort;
    final ExternalMediaManager.UnicastRtpPeer extMediaOutPeer;

    private GraphReady(String talkBridgeId,
                       String tapBridgeId,
                       String snoopChannelId,
                       String extMediaInChannelId,
                       String extMediaOutChannelId,
                       int rtpInPort,
                       int rtpOutPort,
                       ExternalMediaManager.UnicastRtpPeer extMediaOutPeer) {
      this.talkBridgeId = talkBridgeId;
      this.tapBridgeId = tapBridgeId;
      this.snoopChannelId = snoopChannelId;
      this.extMediaInChannelId = extMediaInChannelId;
      this.extMediaOutChannelId = extMediaOutChannelId;
      this.rtpInPort = rtpInPort;
      this.rtpOutPort = rtpOutPort;
      this.extMediaOutPeer = extMediaOutPeer;
    }
  }
}
