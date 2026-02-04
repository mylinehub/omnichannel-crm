package com.mylinehub.voicebridge.service;

import com.mylinehub.voicebridge.models.CallHistoryRecord;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.session.CallSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CallHistoryIngestService {

  private static final Logger log = LoggerFactory.getLogger(CallHistoryIngestService.class);

  // =========================
  // DEEP LOGS TOGGLE (TOP)
  // =========================
  private static final boolean DEEP_LOGS = true;

  private final CallHistoryRecordBuilder builder;
  private final CallHistoryBufferService buffer;

  public CallHistoryIngestService(CallHistoryRecordBuilder builder,
                                  CallHistoryBufferService buffer) {
    this.builder = builder;
    this.buffer = buffer;
  }

  public void ingest(String entryType, CallSession s, String reason, StasisAppConfig cfg,String summary,String callTranslationOriginal,String callTranslationEnglish) {
    String channelId = (s != null) ? safe(s.getChannelId()) : "";
    String app = (s != null) ? safe(s.getStasisAppName()) : "";
    String org = (s != null) ? safe(s.getOrganization()) : "";
    String caller = (s != null) ? safe(s.getCallerNumber()) : "";

    if (DEEP_LOGS) {
      log.debug("CALL-HIST ingest_start channel={} app={} org={} caller={} reason={} cfgPresent={}",
          channelId, app, org, caller, safe(reason), (cfg != null));
    }

    try {
      if (s == null) {
        if (DEEP_LOGS) log.debug("CALL-HIST ingest_skip reason=null_session");
        return;
      }

      CallHistoryRecord r = builder.from(entryType,s, reason, cfg,summary,callTranslationOriginal,callTranslationEnglish);
      if (r == null) {
        if (DEEP_LOGS) {
          log.debug("CALL-HIST ingest_skip channel={} reason=builder_returned_null app={} org={}",
              channelId, app, org);
        }
        return;
      }

      buffer.enqueue(r);

      if (DEEP_LOGS) {
        log.debug("CALL-HIST enqueue_ok channel={} app={} org={} caller={} reason={}",
            channelId, app, org, caller, safe(reason));
      }

    } catch (Exception e) {
      log.error("CALL-HIST ingest_error channel={} app={} org={} reason={} msg={}",
          channelId, app, org, safe(reason), e.getMessage(), e);
    }
  }

  private static String safe(String s) {
    return (s == null) ? "" : s;
  }
}
