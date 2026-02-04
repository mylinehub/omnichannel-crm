package com.mylinehub.voicebridge.service;

import com.mylinehub.voicebridge.billing.CallBillingInfo;
import com.mylinehub.voicebridge.models.CallHistoryRecord;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.session.CallSession;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CallHistoryRecordBuilder {

  public CallHistoryRecord from(String entryType, CallSession s, String reason, StasisAppConfig cfg,String summary,String callTranslationOriginal,String callTranslationEnglish) {
    if (s == null) return null;

    CallHistoryRecord r = new CallHistoryRecord();

    r.setCompleteCallTranscriptOriginal(callTranslationEnglish);
    r.setCompleteCallTranscriptOriginal(callTranslationOriginal);
    r.setSummary(summary);
    if(entryType!=null)
    r.setEntryType(entryType);
    else
    r.setEntryType("N/A");
    
    r.setChannelId(nvl(s.getChannelId()));
    r.setStasisAppName(nvl(s.getStasisAppName()));
    r.setOrganization(nvl(s.getOrganization()));
    r.setCallerNumber(nvl(s.getCallerNumber()));
    r.setEndReason(nvl(reason));

    r.setCallerTranscriptEn(nvl(s.getCallerTranscriptEnText()));
    r.setRagContext(nvl(s.getRagContextBufferText()));
    r.setFinalCompletionJson(nvl(s.getFinalCompletionJson()));

    CallBillingInfo b = s.getBillingInfo();
    if (b != null) {
      r.setStartedAt(b.getStartTime());
      r.setEndedAt(b.getEndTime());

      r.setDurationSeconds(b.getDurationSeconds());

      r.setTotalCallerWords(b.getTotalCallerWords());
      r.setTotalAiCharsSent(b.getTotalAiCharactersSent());
      r.setTotalAiCharsRecv(b.getTotalAiCharactersReceived());
      r.setTotalApproxTokens(b.getTotalApproxTokens());
      r.setTotalRagQueries(b.getTotalRagQueries());
      r.setTotalRagContextChars(b.getTotalRagContextCharacters());

      r.setRecordingPath(nvl(b.getRecordingPath()));
      r.setRecordingFileName(nvl(b.getRecordingFileName()));
    }

    // Small config snapshot (avoid dumping entire config)
    if (cfg != null) {
      r.setBotMode(nvl(cfg.getBot_mode()));
      r.setRtpCodec(nvl(cfg.getRtp_codec()));
      r.setAiPcmSampleRateHz(cfg.getAi_pcm_sampleRateHz());
    }

    r.setCreatedAt(Instant.now());
    return r;
  }

  private static String nvl(String s) {
    return (s == null) ? "" : s;
  }
}
