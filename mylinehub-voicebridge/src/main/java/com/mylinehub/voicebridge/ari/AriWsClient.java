/*
 * Auto-formatted + FIXED + DEEP LOGS: src/main/java/com/mylinehub/voicebridge/ari/AriWsClient.java
 *
 * Notes:
 * - No logic changes. Only added detailed logs and a few small helper methods for logging.
 * - All comments are ASCII only (Windows-1252 safe).
 * - Logs are grouped by simple tags inside the message text:
 *   ARI-WS, ARI-EVENT, ARI-FILTER, ARI-DTMF, ARI-RECONNECT
 */
package com.mylinehub.voicebridge.ari;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moczul.ok2curl.CurlInterceptor;
import com.mylinehub.voicebridge.util.OkHttpLoggerUtils;
import com.mylinehub.voicebridge.ari.impl.AriBridgeImpl;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.session.CallSessionManager;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AriWsClient {

  private static final Logger log = LoggerFactory.getLogger(AriWsClient.class);
  private static final long RECONNECT_DELAY_SECONDS = 2L;

  // =====================================================================
  // Per-file logging switches
  // ---------------------------------------------------------------------
  // DEEP_LOGS:
  //   - Gates ALL non-error logs (INFO/DEBUG/TRACE/WARN) in THIS file.
  //   - Turn on to see full ARI WS traffic and lifecycle.
  //
  // RTP_DEEP_LOGS:
  //   - Reserved for highly chatty per-packet logs (none here yet).
  //
  // ERROR logs:
  //   - NEVER gated. log.error(...) is always emitted.
  // =====================================================================
  private static final boolean DEEP_LOGS = false;

  private final StasisAppConfig cfg;
  private final String stasisAppName;
  private final ApplicationContext applicationContext;
  private final OkHttpClient client;
  private final ScheduledExecutorService reconnectScheduler;
  private final CallSessionManager sessions;
  private volatile WebSocket currentWs;
  private volatile long lastConnectAttemptMs = 0L;

  /** Shared mapper for ARI event JSON. */
  private final ObjectMapper mapper = new ObjectMapper();

  public AriWsClient(CallSessionManager sessions, StasisAppConfig cfg, ApplicationContext applicationContext) {
	  this.cfg = cfg;
	  this.stasisAppName = cfg.getStasis_app_name();   // <<< IMPORTANT: initialize final field
	  this.sessions = sessions;
	  this.applicationContext=applicationContext;
	  OkHttpLoggerUtils myLogger = new OkHttpLoggerUtils(); // Your custom logger
	  CurlInterceptor curlInterceptor = new CurlInterceptor(myLogger);

	  this.client = new OkHttpClient.Builder()
	      .addInterceptor(curlInterceptor)
	      .connectTimeout(Duration.ofSeconds(10))
	      .readTimeout(0, TimeUnit.MILLISECONDS)
	      .pingInterval(Duration.ofSeconds(20))
	      .build();

	  this.reconnectScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
	    Thread t = new Thread(r, "ari-ws-reconnect-" + stasisAppName);
	    t.setDaemon(true);
	    return t;
	  });

	  if (DEEP_LOGS) {
	    log.info("ARI-WS[{}] ctor_ok wsUrl={} appName={} user={}",
	        stasisAppName, cfg.getAri_ws_url(),
	        cfg.getStasis_app_name(), cfg.getAri_username());
	  }
	}


  //  NEW: resolve AriBridgeImpl lazily (only when first event arrives)
  private AriBridgeImpl bridge() {
    return applicationContext.getBean(AriBridgeImpl.class);
  }

  public void start() {
    if (DEEP_LOGS) {
      log.info("ARI-WS start_enter reconnectDelaySec={} thread={}",
          RECONNECT_DELAY_SECONDS, Thread.currentThread().getName());
    }
    connect();
  }

  /**
   * Single place that actually opens the ARI WebSocket. Can be called at startup
   * and again later from onFailure() when we want to reconnect.
   */
  private void connect() {
    String url = cfg.getAri_ws_url();
    if (DEEP_LOGS) {
      log.info("ARI-WS connect_begin url={} user={} appName={}",
          url, cfg.getAri_username(), cfg.getStasis_app_name());
    }
    
    long now = System.currentTimeMillis();
    if (now - lastConnectAttemptMs < 1000) {
      log.warn("ARI-RECONNECT connect_skipped_throttle url={}", url);
      return;
    }
    lastConnectAttemptMs = now;

    try {
      WebSocket old = currentWs;
      if (old != null) {
        log.warn("ARI-RECONNECT closing_previous_ws oldWsHash={}", System.identityHashCode(old));
        old.close(1000, "reconnect");
      }
    } catch (Exception e) {
      log.warn("ARI-RECONNECT closing_previous_ws_error msg={}", e.getMessage());
    }

    Request req = new Request.Builder()
        .url(url)
        .addHeader(
            "Authorization",
            Credentials.basic(cfg.getAri_username(), cfg.getAri_password()))
        .build();

    client.newWebSocket(req, new WebSocketListener() {

      @Override
      public void onOpen(WebSocket webSocket, Response response) {
    	  
    	currentWs = webSocket;
        if (DEEP_LOGS) {
          log.info("ARI-WS onOpen status={} protocol={} thread={} wsHash={}",
              response != null ? response.code() : "n/a",
              response != null ? response.protocol() : "n/a",
              Thread.currentThread().getName(),
              System.identityHashCode(webSocket));
        }
      }

      @Override
      public void onMessage(WebSocket ws, String text) {
        // Raw event at INFO so you can enable via DEEP_LOGS in prod troubleshooting
        if (DEEP_LOGS) {
          log.info("ARI-EVENT raw wsHash={} len={} text={}",
              System.identityHashCode(ws),
              text != null ? text.length() : 0,
              text);
        }

        try {
          JsonNode root = mapper.readTree(text);
          String type = root.path("type").asText();

          JsonNode chNode = root.path("channel");

          String channelId        = chNode.path("id").asText();
          String channelName      = chNode.path("name").asText();
          String channelState     = chNode.path("state").asText();
          String callerNumber     = chNode.path("caller").path("number").asText();
          String callerName       = chNode.path("caller").path("name").asText();
          String connectedNumber  = chNode.path("connected").path("number").asText();
          String connectedName    = chNode.path("connected").path("name").asText();
          String language         = chNode.path("language").asText();
          String context          = chNode.path("dialplan").path("context").asText();
          String exten            = chNode.path("dialplan").path("exten").asText();
          int priority            = chNode.path("dialplan").path("priority").asInt();
          JsonNode args           = root.path("args");

          // Pretty DEBUG dump (kept, with extra one-line summary too)
          if (DEEP_LOGS) {
            log.debug(
                "\n=============== ARI EVENT ===============\n" +
                "Type              : {}\n" +
                "Channel ID        : {}\n" +
                "Channel Name      : {}\n" +
                "State             : {}\n" +
                "Caller Number     : {}\n" +
                "Caller Name       : {}\n" +
                "Connected Number  : {}\n" +
                "Connected Name    : {}\n" +
                "Language          : {}\n" +
                "Context           : {}\n" +
                "Exten             : {}\n" +
                "Priority          : {}\n" +
                "Args              : {}\n" +
                "=========================================",
                type,
                channelId,
                channelName,
                channelState,
                callerNumber,
                callerName,
                connectedNumber,
                connectedName,
                language,
                context,
                exten,
                priority,
                args != null ? args.toString() : "[]"
            );

            log.debug("ARI-EVENT parsed type={} chId={} chName={} state={} caller={} connected={} ctx={} exten={} prio={} argsLen={}",
                type, channelId, channelName, channelState,
                callerNumber, connectedNumber, context, exten, priority,
                args != null ? args.size() : 0);
          }

          // ---------------------------
          // StasisStart
          // ---------------------------
          if ("StasisStart".equals(type)) {
            if (DEEP_LOGS) {
              log.info("ARI-EVENT StasisStart received chId={} chName={} state={} caller={} connected={}",
                  channelId, channelName, channelState, callerNumber, connectedNumber);
            }

            // Only handle primary inbound call leg (PJSIP), ignore UnicastRTP and others
            if (channelName == null || !channelName.startsWith("PJSIP/")) {
              if (DEEP_LOGS) {
                log.info("ARI-FILTER StasisStart ignored nonPjsip chId={} chName={} reason=not_PJSIP",
                    channelId, channelName);
              }
              return;
            }

            // Guard: sometimes Asterisk sends empty channelId for weird events
            if (channelId == null || channelId.isBlank()) {
              if (DEEP_LOGS) {
                log.warn("ARI-FILTER StasisStart ignored empty_channelId chName={}", channelName);
              }
              return;
            }

            if (DEEP_LOGS) {
              log.info("ARI-EVENT StasisStart dispatching to bridge chId={}", channelId);
            }
            
            // Store mapping channelId -> stasis_app_name (which ARI app this call belongs to)
            sessions.putStasisApp(channelId, stasisAppName);
            if (DEEP_LOGS) {
              log.info("SESSION[{}] stasisApp_store chId={} app={}",
                  stasisAppName, channelId, stasisAppName);
            }

            
            if (callerNumber != null && !callerNumber.isBlank()) {
            	  sessions.putCallerNumber(channelId, callerNumber);
            	  if (DEEP_LOGS) {
            	    log.info("SESSION[{}] callerNumber_store chId={} caller={}",
            	        stasisAppName, channelId, callerNumber);
            	  }
            }
            
            bridge().onStasisStart(channelId,cfg);
            if (DEEP_LOGS) {
              log.info("ARI-EVENT StasisStart dispatched ok chId={}", channelId);
            }
            return;

          // ---------------------------
          // StasisEnd
          // ---------------------------
          } else if ("StasisEnd".equals(type)) {
            if (DEEP_LOGS) {
              log.info("ARI-EVENT StasisEnd received chId={} chName={} state={}",
                  channelId, channelName, channelState);
            }

            // Only end sessions for the primary PJSIP leg; UnicastRTP cleanup is handled implicitly
            if (channelName == null || !channelName.startsWith("PJSIP/")) {
              if (DEEP_LOGS) {
                log.info("ARI-FILTER StasisEnd ignored nonPjsip chId={} chName={} reason=not_PJSIP",
                    channelId, channelName);
              }
              return;
            }

            if (channelId == null || channelId.isBlank()) {
              if (DEEP_LOGS) {
                log.warn("ARI-FILTER StasisEnd ignored empty_channelId chName={}", channelName);
              }
              return;
            }

            if (DEEP_LOGS) {
              log.info("ARI-EVENT StasisEnd dispatching to bridge chId={}", channelId);
            }
            
            // REMOVE the stored mapping
            if (DEEP_LOGS) {
                log.info("SESSION[{}] stasisApp_remove chId={} app={}",
                    stasisAppName, channelId, stasisAppName);
            }
            
            bridge().onStasisEnd(channelId,cfg);
            if (DEEP_LOGS) {
              log.info("ARI-EVENT StasisEnd dispatched ok chId={}", channelId);
            }
            return;
          }

          // ---------------------------
          // DTMF
          // ---------------------------
          if ("ChannelDtmfReceived".equals(type)) {
            String digit = root.path("digit").asText("");
            int durationMs = root.path("duration_ms").asInt(0);

            if (DEEP_LOGS) {
              log.info("ARI-DTMF received chId={} chName={} digit={} durationMs={}",
                  channelId, channelName, digit, durationMs);
            }

            if (digit != null && !digit.isBlank()) {
            	 bridge().onDtmfReceived(channelId, digit, durationMs);
              if (DEEP_LOGS) {
                log.info("ARI-DTMF forwarded to bridge chId={} digit={}", channelId, digit);
              }
            } else {
              if (DEEP_LOGS) {
                log.info("ARI-DTMF ignored blank digit chId={}", channelId);
              }
            }
            return;
          }

          // ---------------------------
          // Other events
          // ---------------------------
          if (DEEP_LOGS) {
            log.debug("ARI-EVENT ignored type={} chId={} chName={}", type, channelId, channelName);
          }

        } catch (Exception e) {
          if (DEEP_LOGS) {
            log.warn("ARI-WS parse_error wsHash={} msg={} rawText={}",
                System.identityHashCode(ws), e.getMessage(), text, e);
          }
        }
      }

      @Override
      public void onFailure(WebSocket ws, Throwable t, Response r) {
    	if (currentWs == ws) currentWs = null;
        String status = (r != null ? String.valueOf(r.code()) : "n/a");
        String reason = (t != null ? t.getMessage() : "null");

        // ERROR => always printed
        log.error("ARI-RECONNECT onFailure wsHash={} status={} reason={} schedulingReconnectSec={}",
            System.identityHashCode(ws), status, reason, RECONNECT_DELAY_SECONDS, t);

        try {
          // ensure this socket is closed before we open a new one
          if (DEEP_LOGS) {
            log.warn("ARI-RECONNECT closing_failed_socket wsHash={}", System.identityHashCode(ws));
          }
          ws.close(1001, "Reconnecting");
        } catch (Exception ignore) {
          if (DEEP_LOGS) {
            log.warn("ARI-RECONNECT close_failed_socket_error wsHash={} msg={}",
                System.identityHashCode(ws), ignore.getMessage());
          }
        }

        // Retry ARI connection after delay
        reconnectScheduler.schedule(
            AriWsClient.this::connect,
            RECONNECT_DELAY_SECONDS,
            TimeUnit.SECONDS
        );

        if (DEEP_LOGS) {
          log.info("ARI-RECONNECT scheduled wsHash={} delaySec={}",
              System.identityHashCode(ws), RECONNECT_DELAY_SECONDS);
        }
      }

      @Override
      public void onClosing(WebSocket ws, int code, String reason) {
        log.warn("ARI-WS onClosing wsHash={} code={} reason={}",
            System.identityHashCode(ws), code, reason);
      }

      @Override
      public void onClosed(WebSocket ws, int code, String reason) {
        log.warn("ARI-WS onClosed wsHash={} code={} reason={}",
            System.identityHashCode(ws), code, reason);

        if (currentWs == ws) currentWs = null;

        log.error("ARI-RECONNECT onClosed wsHash={} schedulingReconnectSec={}",
            System.identityHashCode(ws), RECONNECT_DELAY_SECONDS);

        reconnectScheduler.schedule(AriWsClient.this::connect, RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
      }

      
    });

    if (DEEP_LOGS) {
      log.info("ARI-WS connect_ws_created url={} (listener attached)", url);
    }
  }


  public void shutdown() {
    if (DEEP_LOGS) {
      log.info("ARI-WS shutdown_enter thread={}", Thread.currentThread().getName());
    }

    try {
      reconnectScheduler.shutdownNow();
      if (DEEP_LOGS) {
        log.info("ARI-WS shutdown_scheduler_ok");
      }
    } catch (Exception e) {
      if (DEEP_LOGS) {
        log.warn("ARI-WS shutdown_scheduler_error msg={}", e.getMessage(), e);
      }
    }

    try {
      client.dispatcher().executorService().shutdown();
      client.connectionPool().evictAll();
      if (DEEP_LOGS) {
        log.info("ARI-WS shutdown_okhttp_ok");
      }
    } catch (Exception e) {
      if (DEEP_LOGS) {
        log.warn("ARI-WS shutdown_okhttp_error msg={}", e.getMessage(), e);
      }
    }

    if (DEEP_LOGS) {
      log.info("ARI-WS shutdown_exit");
    }
  }
}
