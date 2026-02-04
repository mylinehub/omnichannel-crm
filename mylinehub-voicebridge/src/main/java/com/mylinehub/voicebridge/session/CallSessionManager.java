/*
 * Deep-log edition:
 * src/main/java/com/mylinehub/voicebridge/session/CallSessionManager.java
 *
 * IMPORTANT:
 *  - No logic modifications.
 *  - Only log gating via DEEP_LOGS (and RTP_DEEP_LOGS placeholder).
 *  - ASCII-safe (Windows-1252 compatible).
 */

package com.mylinehub.voicebridge.session;

import java.util.concurrent.ConcurrentHashMap;

import okhttp3.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mylinehub.voicebridge.audio.AudioCodec;
import com.mylinehub.voicebridge.rtp.RtpPacketizer;
import com.mylinehub.voicebridge.rtp.RtpSymmetricEndpoint;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.service.CallTransferService;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class CallSessionManager {


  private static final Logger log = LoggerFactory.getLogger(CallSessionManager.class);
  private final CallTransferService transferService;

  /**
   * Per-file deep log switch:
   *  - If false: all non-error logs (INFO/DEBUG/WARN/TRACE) in this class are suppressed.
   *  - ERROR logs are NEVER gated and always printed.
   */
  private static final boolean DEEP_LOGS = false;

  /**
   * RTP-specific deep logs (placeholder for RTP-heavy logging in this class).
   * There are no RTP-packet logs here now, but this is provided for consistency.
   */
  private static final boolean RTP_DEEP_LOGS = false;

  private final ConcurrentHashMap<String,String> callerNumbers = new ConcurrentHashMap<>();

  private final ConcurrentHashMap<String, CallSession> map = new ConcurrentHashMap<>();

  //inside CallSessionManager
	
  //---------------------------------------------------------
  //Stasis app mapping (channelId -> stasis_app_name)
  //---------------------------------------------------------
  private final ConcurrentHashMap<String, String> stasisAppByChannel = new ConcurrentHashMap<>();


  /**
   * Create and register a fully-wired CallSession.
   */
  public CallSession create(
          String id,
          long maxQueueMs,
          RtpPacketizer packetizerOut,
          RtpSymmetricEndpoint rtpInEndpoint,
          RtpSymmetricEndpoint rtpOutEndpoint,
          AudioCodec codec,
          int frameMs,
          StasisAppConfig props) {

    long t0 = (DEEP_LOGS ? System.nanoTime() : 0L);
    String thread = Thread.currentThread().getName();

    if (DEEP_LOGS) {
      log.debug("call_session_create_begin id={} frameMs={} maxQueueMs={} thread={} mapSize={}",
          id, frameMs, maxQueueMs, thread, map.size());
    }

    if (id == null || id.isEmpty()) {
      log.error("call_session_create_fail_empty_id thread={}", thread);
      throw new IllegalArgumentException("channelId is empty");
    }
    if (packetizerOut == null || rtpInEndpoint == null || rtpOutEndpoint == null || codec == null) {
      log.error("call_session_create_fail_null_components id={} thread={}", id, thread);
      throw new IllegalArgumentException(
              "create() requires packetizer, rtpInEndpoint, rtpOutEndpoint, codec");
    }

    CallSession s = new CallSession(id, maxQueueMs, packetizerOut, rtpInEndpoint,rtpOutEndpoint, codec, frameMs,transferService,props);
    map.put(id, s);

    if (DEEP_LOGS) {
    	  log.info("call_session_rtp_wired id={} inLocalPort={} outLocalPort={}",
    	      id,
    	      rtpInEndpoint.localPort(),
    	      rtpOutEndpoint.localPort());
    	}


    long durUs = (DEEP_LOGS ? (System.nanoTime() - t0) / 1000 : 0);

    if (DEEP_LOGS) {
      log.info("call_session_create_ok id={} pt={} clk={} ssrc={} frameMs={} queueMs={} thread={} durUs={}",
              id,
              packetizerOut.payloadType(),
              packetizerOut.clockRate(),
              packetizerOut.ssrc(),
              frameMs,
              maxQueueMs,
              thread,
              durUs);
    }

    if (DEEP_LOGS) {
      log.debug("call_session_create_after id={} activeSessions={} thread={}",
          id, map.size(), thread);
    }

    return s;
  }
  
  
  // ---------------------------------------------------------
  // IVR session creation (NO RTP / NO AI / NO queue)
  // ---------------------------------------------------------
  public CallSession createIvrSession(String id,StasisAppConfig props) {
    String thread = Thread.currentThread().getName();

    if (id == null || id.isEmpty()) {
      log.error("call_session_createIvr_fail_empty_id thread={}", thread);
      throw new IllegalArgumentException("channelId is empty");
    }

    long t0 = (DEEP_LOGS ? System.nanoTime() : 0L);

    CallSession s = new CallSession(id,transferService,props); // uses IVR-only constructor
    map.put(id, s);

    if (DEEP_LOGS) {
      long durUs = (System.nanoTime() - t0) / 1000;
      log.info("call_session_createIvr_ok id={} thread={} durUs={} activeSessions={}",
          id, thread, durUs, map.size());
    }

    return s;
  }

  
  public void putStasisApp(String channelId, String stasisAppName) {
	  if (channelId == null || stasisAppName == null) {
	    return;
	  }
	  stasisAppByChannel.put(channelId, stasisAppName);
  }

  public String getStasisApp(String channelId) {
	  if (channelId == null) {
	    return null;
	  }
	  return stasisAppByChannel.get(channelId);
  }

  public void removeStasisApp(String channelId) {
	  if (channelId != null) {
	    stasisAppByChannel.remove(channelId);
	  }
  }

  
  public void putCallerNumber(String channelId, String number) {
    callerNumbers.put(channelId, number);
  }

  public String takeCallerNumber(String channelId) {
    return callerNumbers.remove(channelId);
  }

  /** Lookup session by channelId. */
  public CallSession get(String id) {
    CallSession s = map.get(id);

    if (DEEP_LOGS) {
      log.debug("call_session_get id={} exists={} activeSessions={} thread={}",
          id, (s != null), map.size(), Thread.currentThread().getName());
    }

    return s;
  }

  /** Remove CallSession. */
  public void remove(String id) {
    long t0 = (DEEP_LOGS ? System.nanoTime() : 0L);
    String thread = Thread.currentThread().getName();

    CallSession removed = map.remove(id);

    if (removed != null) {
      long durUs = (System.nanoTime() - t0) / 1000;

      if (DEEP_LOGS) {
        log.info("call_session_removed id={} activeSessions={} thread={} durUs={}",
            id, map.size(), thread, durUs);
      }

    } else if (DEEP_LOGS) {
      log.debug("call_session_remove_nop id={} not_found thread={}", id, thread);
    }
  }

  /**
   * Find CallSession by AI WebSocket.
   */
  public CallSession findByWebSocket(WebSocket ws) {
    if (ws == null) return null;
    int hash = System.identityHashCode(ws);

    if (DEEP_LOGS) {
      log.debug("call_session_find_ws_begin wsHash={} activeSessions={} thread={}",
          hash, map.size(), Thread.currentThread().getName());
    }

    for (CallSession session : map.values()) {
      if (ws.equals(session.getAiWebSocket())) {

        if (DEEP_LOGS) {
          log.debug("call_session_find_ws_hit id={} wsHash={} thread={}",
              session.getChannelId(), hash, Thread.currentThread().getName());
        }

        return session;
      }
    }

    if (DEEP_LOGS) {
      log.debug("call_session_find_ws_miss wsHash={} activeSessions={} thread={}",
          hash, map.size(), Thread.currentThread().getName());
    }

    return null;
  }

  /**
   * Attach AI WebSocket to a session.
   */
  public void mapWebSocket(WebSocket ws, CallSession session) {
    if (ws == null || session == null) return;

    long t0 = (DEEP_LOGS ? System.nanoTime() : 0L);
    String id = session.getChannelId();
    int hash = System.identityHashCode(ws);
    String thread = Thread.currentThread().getName();

    session.setAiWebSocket(ws);

    long durUs = (System.nanoTime() - t0) / 1000;

    if (DEEP_LOGS) {
      log.info("call_session_ws_mapped id={} wsHash={} thread={} durUs={}",
              id, hash, thread, durUs);
    }

    if (DEEP_LOGS) {
      log.debug("call_session_ws_map_details id={} activeSessions={} thread={}",
          id, map.size(), thread);
    }
  }

  
}
