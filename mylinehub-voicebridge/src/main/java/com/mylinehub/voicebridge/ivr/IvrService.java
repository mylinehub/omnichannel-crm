package com.mylinehub.voicebridge.ivr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.voicebridge.models.IvrDtmfRule;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.session.CallSession;
import com.mylinehub.voicebridge.session.CallSessionManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class IvrService {

  private static final Logger log = LoggerFactory.getLogger(IvrService.class);

  // =========================
  // DEEP LOGS TOGGLE (TOP)
  // =========================
  private static final boolean DEEP_LOGS = true;

  // Ignore first dtmf for IVR (fast)
  private static final long IVR_DTMF_IGNORE_MS = 1200L;

  // Keep small; IVR uses ARI REST only
  private static final int ARI_TIMEOUT_MS = 800;

  private final WebClient web = WebClient.builder().build();
  private final CallSessionManager sessions;
  private final ObjectMapper mapper;

  // ---------------------------------------------------
  // IVR STATE (no CallSession needed)
  // ---------------------------------------------------
  private final ConcurrentHashMap<String, Long> ignoreUntilMsByChannel = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, String> dtmfSoFarByChannel = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, StasisAppConfig> cfgByChannel = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Integer> rrIndexByChannel = new ConcurrentHashMap<>();

  // One-shot latch: once we execute close/redirect, ignore further DTMF to avoid double actions
  private final ConcurrentHashMap<String, Boolean> actionFiredByChannel = new ConcurrentHashMap<>();

  private static String nvl(String s) { return s == null ? "" : s; }

  private static String basic(String u, String pw) {
    String raw = nvl(u) + ":" + nvl(pw);
    return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  private static String maskDigits(String s) {
    if (s == null) return "";
    return s.replaceAll("\\d", "*");
  }

  private boolean isIvrMode(StasisAppConfig p) {
    return p != null && "ivr".equalsIgnoreCase(nvl(p.getBot_mode()).trim());
  }

  public void startIvrCall(String channelId, StasisAppConfig cfg) {
    if (!isIvrMode(cfg)) return;
    if (channelId == null || channelId.isBlank()) return;

    if (DEEP_LOGS) {
        String rulesJson = nvl(cfg.getIvr_dtmf_rules_json()).trim();
        log.info("IVR rules_loaded channel={} rulesJsonLen={}", channelId, rulesJson.length());
    }
    
    // reset per-call state
    rrIndexByChannel.remove(channelId);
    actionFiredByChannel.remove(channelId);

    cfgByChannel.put(channelId, cfg);

    long now = System.currentTimeMillis();
    ignoreUntilMsByChannel.put(channelId, now + IVR_DTMF_IGNORE_MS);
    dtmfSoFarByChannel.remove(channelId);

    if (DEEP_LOGS) {
      log.info("IVR start channel={} ignoreUntilMs={} recording='{}' redirectTrunk='{}' redirectProtocol='{}'",
          channelId,
          (now + IVR_DTMF_IGNORE_MS),
          nvl(cfg.getIvr_recording_path()),
          nvl(cfg.getRedirectTrunk()),
          nvl(cfg.getRedirectProtocol()));
    }

    playRecordingOnChannel(cfg, channelId, cfg.getIvr_recording_path());
  }

  public String getIvrDtmfSoFar(String channelId) {
    return dtmfSoFarByChannel.get(channelId);
  }

  public boolean isIvrActive(String channelId) {
    return channelId != null && ignoreUntilMsByChannel.containsKey(channelId);
  }

  /**
   * Called from AriBridgeImpl.onStasisEnd() for IVR calls.
   * Copies DTMF into CallSession attrs so CallCompletionService / history can persist it.
   */
  public void clearIvrState(String channelId) {
    if (channelId == null) return;

    // Snapshot before clearing
    String dtmf = dtmfSoFarByChannel.get(channelId);

    try {
      CallSession s = sessions.get(channelId);
      if (s != null) {
        // Always write call.mode=ivr even if dtmf empty (you said you still want to save it)
        s.putAttr("call.mode", "ivr");
        if (dtmf != null) s.putAttr("ivr.dtmf", dtmf);
        if (DEEP_LOGS) {
          log.info("IVR clear -> copied_to_session channel={} dtmf='{}'",
              channelId, nvl(dtmf));
        }
      } else {
        if (DEEP_LOGS) {
          log.info("IVR clear -> no_session channel={} dtmf='{}'", channelId, nvl(dtmf));
        }
      }
    } catch (Exception e) {
      log.warn("IVR clear copy_to_session_failed channel={} msg={}", channelId, e.getMessage());
    }

    // Clear maps
    ignoreUntilMsByChannel.remove(channelId);
    dtmfSoFarByChannel.remove(channelId);
    cfgByChannel.remove(channelId);
    rrIndexByChannel.remove(channelId);
    actionFiredByChannel.remove(channelId);

    if (DEEP_LOGS) log.info("IVR clear done channel={}", channelId);
  }

  private void stopAllPlaybacks(StasisAppConfig cfg, String channelId) {
	    if (cfg == null || channelId == null || channelId.isBlank()) return;

	    String url = nvl(cfg.getAri_rest_baseUrl()) + "/channels/" + channelId + "/play";
	    if (DEEP_LOGS) log.info("IVR playback_stop_all begin channel={} url='{}'", channelId, url);

	    web.delete()
	        .uri(url)
	        .header(HttpHeaders.AUTHORIZATION, basic(cfg.getAri_username(), cfg.getAri_password()))
	        .retrieve()
	        .bodyToMono(String.class)
	        .timeout(Duration.ofMillis(ARI_TIMEOUT_MS))
	        .onErrorResume(e -> {
	          log.warn("IVR playback_stop_all error channel={} msg={}", channelId, e.getMessage());
	          return Mono.empty();
	        })
	        .subscribe(resp -> {
	          if (DEEP_LOGS) log.info("IVR playback_stop_all ok channel={} respLen={}", channelId, (resp != null ? resp.length() : 0));
	        });
	  }

  /**
   * Plays an Asterisk sound file: "sound:<name>"
   * Example: ivr_recording_path = "custom/my_ivr"
   */
  public void playRecordingOnChannel(StasisAppConfig cfg, String channelId, String recordingPath) {
    String rec = nvl(recordingPath).trim();
    if (rec.isEmpty()) {
      if (DEEP_LOGS) log.warn("IVR play skip_empty_recording channel={}", channelId);
      return;
    }

    String url = nvl(cfg.getAri_rest_baseUrl()) + "/channels/" + channelId + "/play?media=sound:" + rec;

    if (DEEP_LOGS) log.info("IVR play begin channel={} media='sound:{}' url='{}'", channelId, rec, url);

    web.post()
        .uri(url)
        .header(HttpHeaders.AUTHORIZATION, basic(cfg.getAri_username(), cfg.getAri_password()))
        .retrieve()
        .bodyToMono(String.class)
        .timeout(Duration.ofMillis(ARI_TIMEOUT_MS))
        .onErrorResume(e -> {
          log.warn("IVR play error channel={} msg={}", channelId, e.getMessage());
          return Mono.empty();
        })
        .subscribe(resp -> {
          if (DEEP_LOGS) log.info("IVR play ok channel={} respLen={}", channelId, (resp != null ? resp.length() : 0));
        });
  }

  /**
   * Called from AriBridgeImpl.onDtmfReceived() only when IVR is active.
   * - collects DTMF
   * - applies cfg.ivr_dtmf_rules_json: supports actions "close" and "redirect"
   */
  public void onIvrDtmf(String channelId, String digit) {
    if (channelId == null || channelId.isBlank()) return;
    if (digit == null || digit.isBlank()) return;

    // If channel is not active in IVR, ignore
    Long ignoreUntil = ignoreUntilMsByChannel.get(channelId);
    if (ignoreUntil == null) return;

    // If action already fired, ignore any further dtmf
    if (Boolean.TRUE.equals(actionFiredByChannel.get(channelId))) {
      if (DEEP_LOGS) log.info("IVR dtmf_ignored_action_already_fired channel={} digit={}", channelId, digit);
      return;
    }

    long now = System.currentTimeMillis();
    if (now < ignoreUntil) {
      if (DEEP_LOGS) {
        log.info("IVR dtmf_ignored channel={} digit={} nowMs={} ignoreUntilMs={}",
            channelId, digit, now, ignoreUntil);
      }
      return;
    }

    // append with ":" delimiter
    dtmfSoFarByChannel.compute(channelId, (k, old) -> {
      if (old == null || old.isEmpty()) return digit;
      return old + ":" + digit;
    });

    String soFar = dtmfSoFarByChannel.get(channelId);
    if (DEEP_LOGS) log.info("IVR dtmf_accepted channel={} digit={} soFar='{}'", channelId, digit, soFar);

    StasisAppConfig cfg = cfgByChannel.get(channelId);
    if (cfg == null) {
      if (DEEP_LOGS) log.warn("IVR dtmf_drop_no_cfg channel={} soFar='{}'", channelId, soFar);
      return;
    }

    IvrDtmfRule matched = findMatchingRule(channelId, cfg, soFar);
    if (matched == null) {
      if (DEEP_LOGS) log.info("IVR rule_not_matched channel={} soFar='{}'", channelId, soFar);
      return;
    }

    String action = nvl(matched.getAction()).trim().toLowerCase();
    if (DEEP_LOGS) {
      log.info("IVR rule_matched channel={} soFar='{}' action='{}' interest='{}' redirectCount={}",
          channelId,
          soFar,
          action,
          nvl(matched.getInterest()),
          (matched.getRedirectNumberList() != null ? matched.getRedirectNumberList().size() : 0));
    }

    if ("close".equals(action)) {
    	  actionFiredByChannel.put(channelId, Boolean.TRUE);
    	  closeWithThankYouThenHangup(cfg, channelId);
    	  return;
    }


    if ("redirect".equals(action)) {
      String target = pickRedirectNumberRoundRobin(channelId, matched);
      if (target == null || target.isBlank()) {
        log.warn("IVR redirect_no_target channel={} soFar='{}'", channelId, soFar);
        return;
      }
      actionFiredByChannel.put(channelId, Boolean.TRUE);
      redirectChannel(cfg, channelId, target);
      return;
    }

    log.warn("IVR unknown_action channel={} action='{}' soFar='{}'", channelId, action, soFar);
  }

  private void closeWithThankYouThenHangup(StasisAppConfig cfg, String channelId) {
	    if (cfg == null) return;

	    String thankyou = nvl(cfg.getThankyou_recording_path()).trim();
	    Integer lenObj = cfg.getThankyou_recording_length_seconds();
	    int lenSec = (lenObj != null ? lenObj : 0);

	    // If no thankyou configured, hangup immediately.
	    if (thankyou.isEmpty() || lenSec <= 0) {
	      if (DEEP_LOGS) {
	        log.info("IVR close -> no_thankyou_or_len -> hangup_immediate channel={} thankyou='{}' lenSec={}",
	            channelId, thankyou, lenSec);
	      }
	      hangupChannel(cfg, channelId);
	      return;
	    }

	    // Stop any current playback (main menu etc)
	    stopAllPlaybacks(cfg, channelId);

	    if (DEEP_LOGS) {
	      log.info("IVR close -> thankyou_play_then_hangup channel={} thankyou='{}' lenSec={}",
	          channelId, thankyou, lenSec);
	    }

	    // Play thankyou
	    playRecordingOnChannel(cfg, channelId, thankyou);

	    // Hangup after lenSec (+ small safety buffer)
	    long delayMs = (lenSec * 1000L) + 250L;
	    Mono.delay(Duration.ofMillis(delayMs))
	        .subscribe(t -> hangupChannel(cfg, channelId),
	            e -> hangupChannel(cfg, channelId));
	  }

  private void hangupChannel(StasisAppConfig cfg, String channelId) {
    String url = nvl(cfg.getAri_rest_baseUrl()) + "/channels/" + channelId;

    if (DEEP_LOGS) log.info("IVR close -> hangup channel={} url='{}'", channelId, url);

    web.delete()
        .uri(url)
        .header(HttpHeaders.AUTHORIZATION, basic(cfg.getAri_username(), cfg.getAri_password()))
        .retrieve()
        .bodyToMono(String.class)
        .timeout(Duration.ofMillis(ARI_TIMEOUT_MS))
        .onErrorResume(e -> {
          log.warn("IVR hangup_error channel={} msg={}", channelId, e.getMessage());
          return Mono.empty();
        })
        .subscribe(resp -> {
          if (DEEP_LOGS) log.info("IVR hangup_ok channel={} respLen={}", channelId, (resp != null ? resp.length() : 0));
        });
  }

  private void redirectChannel(StasisAppConfig cfg, String channelId, String targetNumber) {
	  
	// Stop any current playback before redirect (menu audio etc)
	stopAllPlaybacks(cfg, channelId);

    String trunk = nvl(cfg.getRedirectTrunk()).trim();
    String tech = nvl(cfg.getRedirectProtocol()).trim();
    if (tech.isEmpty()) tech = "PJSIP";

    String endpoint;
    if (trunk.isEmpty()) {
      // fallback: internal endpoint style
      endpoint = tech + "/" + targetNumber;
      log.warn("IVR redirectTrunk empty -> fallback endpoint='{}' channel={}", endpoint, channelId);
    } else {
      // trunk dial: PJSIP/<number>@<trunk>
      endpoint = tech + "/" + targetNumber + "@" + trunk;
    }

    String url = nvl(cfg.getAri_rest_baseUrl()) + "/channels/" + channelId + "/redirect?endpoint=" + endpoint;

    if (DEEP_LOGS) {
      log.info("IVR redirect -> channel={} endpoint='{}' targetNumber='{}' trunk='{}' tech='{}'",
          channelId, endpoint, targetNumber, trunk, tech);
    }

    try {
    	  CallSession s = sessions.get(channelId);
    	  if (s != null) {
    	    s.setRedirectChannel(true);
    	    s.putAttr("ivr.redirected", true);
    	    s.putAttr("ivr.redirect.endpoint", endpoint);
    	    s.putAttr("ivr.redirect.target", targetNumber);
    	    if (DEEP_LOGS) log.info("IVR redirect_flag_set channel={} redirectChannel=true endpoint='{}'",
    	        channelId, endpoint);
    	  }
    } catch (Exception e) {
    	  log.warn("IVR redirect_flag_set failed channel={} msg={}", channelId, e.getMessage());
    }
    
    web.post()
        .uri(url)
        .header(HttpHeaders.AUTHORIZATION, basic(cfg.getAri_username(), cfg.getAri_password()))
        .retrieve()
        .bodyToMono(String.class)
        .timeout(Duration.ofMillis(ARI_TIMEOUT_MS))
        .onErrorResume(e -> {
          log.warn("IVR redirect_error channel={} endpoint='{}' msg={}", channelId, endpoint, e.getMessage());
          return Mono.empty();
        })
        .subscribe(resp -> {
          if (DEEP_LOGS) log.info("IVR redirect_ok channel={} respLen={}", channelId, (resp != null ? resp.length() : 0));
        });
  }

  private String pickRedirectNumberRoundRobin(String channelId, IvrDtmfRule rule) {
    if (rule == null || rule.getRedirectNumberList() == null || rule.getRedirectNumberList().isEmpty()) return "";

    int size = rule.getRedirectNumberList().size();
    int idx = rrIndexByChannel.compute(channelId, (k, old) -> (old == null ? 0 : old + 1));

    int pos = Math.floorMod(idx, size);
    String picked = rule.getRedirectNumberList().get(pos);

    if (DEEP_LOGS) {
      log.info("IVR redirect_pick_rr channel={} rrIdx={} size={} pos={} picked='{}'",
          channelId, idx, size, pos, maskDigits(picked));
    }

    return picked;
  }

  private IvrDtmfRule findMatchingRule(String channelId, StasisAppConfig cfg, String dtmf) {
    try {
      String rulesJson = nvl(cfg.getIvr_dtmf_rules_json()).trim();
      if (rulesJson.isEmpty()) {
        if (DEEP_LOGS) log.warn("IVR rules empty channel={}", channelId);
        return null;
      }

      List<IvrDtmfRule> rules = mapper.readValue(rulesJson, new TypeReference<List<IvrDtmfRule>>() {});
      if (rules == null || rules.isEmpty()) {
        if (DEEP_LOGS) log.warn("IVR rules parsed_empty channel={}", channelId);
        return null;
      }

      // 1) exact match
      for (IvrDtmfRule r : rules) {
        if (r == null) continue;
        if (nvl(r.getDtmfPressed()).trim().equalsIgnoreCase(dtmf)) return r;
      }

      // 2) fallback last digit
      String last = dtmf;
      int idx = dtmf.lastIndexOf(':');
      if (idx >= 0 && idx < dtmf.length() - 1) last = dtmf.substring(idx + 1).trim();

      for (IvrDtmfRule r : rules) {
        if (r == null) continue;
        if (nvl(r.getDtmfPressed()).trim().equalsIgnoreCase(last)) return r;
      }

      return null;

    } catch (Exception e) {
      log.error("IVR rules parse/match failed channel={} msg={}", channelId, e.getMessage(), e);
      return null;
    }
  }
}
