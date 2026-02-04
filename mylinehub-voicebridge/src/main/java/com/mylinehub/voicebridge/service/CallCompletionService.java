package com.mylinehub.voicebridge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mylinehub.voicebridge.billing.CallBillingInfo;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.service.RecordingTranscriptionService.TranscriptionResult;
import com.mylinehub.voicebridge.session.CallSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CallCompletionService {

  private static final Logger log = LoggerFactory.getLogger(CallCompletionService.class);
  private static final boolean DEEP_LOGS = true;

  // Billing constants
  private static final long MIN_BILLING_SECONDS = 60;

  private final ObjectMapper mapper;
  private final CustomerUpdateCompletionService customerInventoryCompletionService;
  private final CallReportingService callReportingService;
  private final CallHistoryIngestService callHistoryIngestService;
  private final DeductAiAmountService deductAiAmountService;
  
  // NEW: offline STT from saved WAV (does NOT overwrite realtime session transcript)
  private final RecordingTranscriptionService recordingTranscriptionService;

  public CallCompletionService(
      ObjectMapper mapper,
      CustomerUpdateCompletionService customerInventoryCompletionService,
      CallReportingService callReportingService,
      CallHistoryIngestService callHistoryIngestService,
      RecordingTranscriptionService recordingTranscriptionService,
      DeductAiAmountService deductAiAmountService
  ) {
    this.mapper = mapper;
    this.customerInventoryCompletionService = customerInventoryCompletionService;
    this.callReportingService = callReportingService;
    this.callHistoryIngestService = callHistoryIngestService;
    this.recordingTranscriptionService = recordingTranscriptionService;
    this.deductAiAmountService=deductAiAmountService;
  }

  /**
   * Finalize the call ONCE and fire downstream side-effects.
   *
   * IMPORTANT (this version):
   * - NO retries for CRM / REPORT / DEDUCT calls.
   * - Each side-effect is attempted once and errors are swallowed.
   * - Adds deep IN/OUT logs for every side-effect.
   *
   * NEW:
   * - Runs offline transcription from saved WAV (billing.recordingPath) BEFORE building JSON.
   * - DOES NOT overwrite realtime transcript stored on session.
   * - Uses offline transcript for JSON + CRM if available; otherwise falls back to realtime session transcript.
   */
  public void finalizeAndPersist(CallSession s, String reason, StasisAppConfig cfg, boolean isIvrCall) {
    if (s == null) return;

    final String ch = nvl(s.getChannelId());

    // Correlation id for THIS completion run (helps when multiple calls interleave logs)
    final String fanoutId = buildFanoutId();

    // Snapshot session fields early
    final String sessApp = nvl(s.getStasisAppName());
    final String sessOrg = nvl(s.getOrganization());
    final String sessCaller = nvl(s.getCallerNumber());

    if (DEEP_LOGS) {
      log.info("COMPLETE start fanoutId={} channel={} reason={} cfgNull={} sess.app='{}' sess.org='{}' sess.caller='{}' thread={}",
          fanoutId, ch, nvl(reason), (cfg == null), sessApp, sessOrg, sessCaller, Thread.currentThread().getName());
    }

    // Ensure it runs ONCE per call
    boolean first;
    try {
      first = s.markCompletedOnce();
    } catch (Exception e) {
      log.error("COMPLETE markCompletedOnce_failed fanoutId={} channel={} msg={}", fanoutId, ch, safe(e.getMessage()), e);
      // safest: continue
      first = true;
    }

    if (!first) {
      if (DEEP_LOGS) {
        log.info("COMPLETE skip_already_completed fanoutId={} channel={} reason={}", fanoutId, ch, nvl(reason));
      }
      return;
    }

    try {
      // ------------------------------------------------------------------
      // Billing end time + duration
      // ------------------------------------------------------------------
      CallBillingInfo b = s.getBillingInfo();
      if (DEEP_LOGS) {
        log.info("COMPLETE billing_snapshot fanoutId={} channel={} billingNull={} startTime={} endTime={} existingDurationSecs={}",
            fanoutId,
            ch,
            (b == null),
            (b != null && b.getStartTime() != null ? b.getStartTime().toString() : "null"),
            (b != null && b.getEndTime() != null ? b.getEndTime().toString() : "null"),
            (b != null ? b.getDurationSeconds() : -1));
      }

      if (b != null) {
        if (b.getEndTime() == null) {
          b.setEndTime(Instant.now());
        }

        if (b.getStartTime() != null) {
          long dur = (b.getEndTime().toEpochMilli() - b.getStartTime().toEpochMilli()) / 1000;
          b.setDurationSeconds(Math.max(dur, 0));
        } else {
          b.setDurationSeconds(0);
        }

        b.setTotalCallerWords(s.getCallerWordCount());
      }

      long actualDurationSecs = (b != null) ? b.getDurationSeconds() : 0;
      long billedDurationSecs = Math.max(actualDurationSecs, MIN_BILLING_SECONDS);

      if (DEEP_LOGS) {
        log.info("COMPLETE duration fanoutId={} channel={} actualSecs={} billedSecs={} minBillingSecs={}",
            fanoutId, ch, actualDurationSecs, billedDurationSecs, MIN_BILLING_SECONDS);
      }

      // ------------------------------------------------------------------
      // OFFLINE TRANSCRIPTION (from saved recording WAV)
      // NOTE: does NOT overwrite realtime transcript stored on session.
      // ------------------------------------------------------------------
      final String realtimeCaller = nvl(s.getCallerTranscriptEnText()).trim();
      final int callerChars = realtimeCaller.length();

      // Caller spoke once = at least 5 chars (your rule)
      final boolean callerSpoke = callerChars >= 1;

      log.info("CALLER-GATE fanoutId={} channel={} callerChars={} callerSpoke={}",
    		    fanoutId, ch, callerChars, callerSpoke);
      
      TranscriptionResult offlineTranscript = null;

      try {
    	  
    	  if (isIvrCall) {
    		    log.info("STT-POST fanoutId={} channel={} result=skipped reason=ivr_mode", fanoutId, ch);
    		  } 
    	  else if (!callerSpoke) {
          log.info("STT-POST fanoutId={} channel={} result=skipped reason=caller_not_spoken callerChars={}",
              fanoutId, ch, callerChars);
          } else {

          String recPath = (b != null) ? nvl(b.getRecordingPath()) : "";
          String recFile = (b != null) ? nvl(b.getRecordingFileName()) : "";

          if (recPath.isBlank()) {
            log.info("STT-POST fanoutId={} channel={} result=skipped reason=no_recordingPath", fanoutId, ch);
          } else {

            long sizeBytes = -1L;
            try { sizeBytes = java.nio.file.Files.size(java.nio.file.Path.of(recPath)); } catch (Exception ignore) {}

            final long MIN_WAV_BYTES = 44 + 320; // header + tiny audio payload gate
            if (sizeBytes >= 0 && sizeBytes < MIN_WAV_BYTES) {
              log.warn("STT-POST fanoutId={} channel={} result=skipped reason=file_too_small sizeBytes={} minBytes={} path='{}'",
                  fanoutId, ch, sizeBytes, MIN_WAV_BYTES, recPath);
            } else {
              log.info("STT-PRE fanoutId={} channel={} recordingPath='{}' recordingFile='{}' sizeBytes={}",
                  fanoutId, ch, recPath, recFile, sizeBytes);

              offlineTranscript = recordingTranscriptionService.transcribeWavFileDetailed(recPath, cfg, ch);

              int offLen = (offlineTranscript != null && offlineTranscript.getEnglishReadableConversation() != null)
                  ? offlineTranscript.getEnglishReadableConversation().trim().length()
                  : 0;

              log.info("STT-POST fanoutId={} channel={} result=ok offlineLen={}", fanoutId, ch, offLen);
            }
          }
        }
      } catch (Exception sttEx) {
        log.error("STT-POST fanoutId={} channel={} result=error msg={}",
            fanoutId, ch, safe(sttEx.getMessage()), sttEx);
      }

      // ------------------------------------------------------------------
      // Build FINAL JSON summary
      // - Use OFFLINE transcript if available, else fall back to realtime session transcript.
      // - DOES NOT modify session transcript field.
      // ------------------------------------------------------------------
      String transcriptEn =
    		    (offlineTranscript != null && offlineTranscript.getEnglishReadableConversation() != null
    		        && !offlineTranscript.getEnglishReadableConversation().trim().isEmpty())
    		        ? offlineTranscript.getEnglishReadableConversation().trim()
    		        : realtimeCaller;

      String ragCtx = nvl(s.getRagContextBufferText());

      if (DEEP_LOGS) {
        log.info("COMPLETE text_snapshot fanoutId={} channel={} transcriptLen={} ragCtxLen={} callerWords={}",
            fanoutId, ch, transcriptEn.length(), ragCtx.length(), s.getCallerWordCount());
      }

      String entryType = null;
      
      if(cfg.getSavePropertyInventory()) {
		   entryType = "Property Inventory";
	   }
	   else if(Boolean.TRUE.equals(cfg.getSaveIvrToFranchiseListing())) {
		   entryType = "Franchise Inventory";
	   }
      
      String dtmf = "";
      ObjectNode root = mapper.createObjectNode();
      root.put("channelId", ch);
      root.put("organization", sessOrg);
      root.put("callerNumber", sessCaller);
      root.put("stasisAppName", sessApp);
      root.put("endReason", nvl(reason));
      root.put("callerTranscriptEn", transcriptEn);
      root.put("ragContext", ragCtx);

	   // ----------------------------
	   // IVR details (DTMF) into finalJson
	   // ----------------------------
	   if (isIvrCall) {
	     try { dtmf = nvl(s.getAttr("ivr.dtmf", String.class)).trim(); } catch (Exception ignore) {}
	     ObjectNode ivr = root.putObject("ivr");
	     ivr.put("dtmfSoFar", dtmf);
	     ivr.put("hasDtmf", !dtmf.isEmpty());
	     ivr.put("saveIvrToFranchiseListing", (cfg != null && Boolean.TRUE.equals(cfg.getSaveIvrToFranchiseListing())));
	
	     if (DEEP_LOGS) {
	       log.info("IVR json_added fanoutId={} channel={} dtmf='{}' saveIvrToFranchiseListing={}",
	           fanoutId, ch, dtmf, (cfg != null && Boolean.TRUE.equals(cfg.getSaveIvrToFranchiseListing())));
	     }
	   }

   
      if (b != null) {
        ObjectNode billing = root.putObject("billing");
        billing.put("startTime", b.getStartTime() != null ? b.getStartTime().toString() : "");
        billing.put("endTime", b.getEndTime() != null ? b.getEndTime().toString() : "");
        billing.put("durationSeconds", b.getDurationSeconds());
        billing.put("billedDurationSeconds", billedDurationSecs);
        billing.put("totalCallerWords", b.getTotalCallerWords());
        billing.put("totalAiCharsSent", b.getTotalAiCharactersSent());
        billing.put("totalAiCharsRecv", b.getTotalAiCharactersReceived());
        billing.put("totalApproxTokens", b.getTotalApproxTokens());
        billing.put("totalRagQueries", b.getTotalRagQueries());
        billing.put("totalRagContextChars", b.getTotalRagContextCharacters());
        billing.put("recordingPath", nvl(b.getRecordingPath()));
        billing.put("recordingFileName", nvl(b.getRecordingFileName()));
      }

      String finalJson = root.toString();
      s.setFinalCompletionJson(finalJson);

      if (DEEP_LOGS) {
        log.info("COMPLETE finalJson_set fanoutId={} channel={} jsonLen={}", fanoutId, ch, finalJson.length());
        log.debug("COMPLETE finalJson_preview fanoutId={} channel={} json={}", fanoutId, ch, truncate(finalJson, 1500));
      }

      String rawOrig = (offlineTranscript != null) ? nvl(offlineTranscript.getRawOriginal()) : "";
      String summary = (offlineTranscript != null) ? nvl(offlineTranscript.getSummary()) : "";

      if (isIvrCall) {
    	  if (rawOrig.isBlank()) rawOrig = dtmf;
    	  if (summary.isBlank()) summary = dtmf.isBlank() ? "IVR: no_dtmf" : ("IVR DTMF: " + dtmf);
    	  transcriptEn = ""; // keep empty for IVR
      }
      
      // ------------------------------------------------------------------
      // Call history ingest (always attempted; ingest decides by cfg flags)
      // ------------------------------------------------------------------
      try {
        log.info("HISTORY IN fanoutId={} channel={} app='{}' org='{}' caller='{}' reason='{}' cfgNull={} cfg.save_call_details={}",
            fanoutId, ch, sessApp, sessOrg, sessCaller, nvl(reason),
            (cfg == null),
            (cfg != null ? String.valueOf(cfg.getSave_Call_Details()) : "null"));

        callHistoryIngestService.ingest(entryType,s, reason, cfg, summary,rawOrig, transcriptEn);

        log.info("HISTORY OUT fanoutId={} channel={} result=ok", fanoutId, ch);
      } catch (Exception ex) {
        log.error("HISTORY OUT fanoutId={} channel={} result=error msg={}", fanoutId, ch, safe(ex.getMessage()), ex);
      }

      // ------------------------------------------------------------------
      // Flags from config
      // ------------------------------------------------------------------
      
      final String rawOrigFinal = rawOrig;
      final String transcriptEnFinal = transcriptEn;
      final String summaryFinal = summary;
      final String dtmfFinal = dtmf;
      boolean saveCustomerPropertyInventory = (cfg != null) && Boolean.TRUE.equals(cfg.getSavePropertyInventory());
      boolean saveCustomerFranchiseInventory = (cfg != null) && Boolean.TRUE.equals(Boolean.TRUE.equals(cfg.getSaveIvrToFranchiseListing()));  
      boolean saveCallDetails = (cfg != null) && Boolean.TRUE.equals(cfg.getSave_Call_Details());

      log.info("FLAGS fanoutId={} channel={} saveCustomerInventory={} saveCustomerFranchise={} saveCallDetails={} cfg.save_customer={} cfg.save_call_details={}",
          fanoutId,
          ch,
          saveCustomerPropertyInventory,
          saveCustomerFranchiseInventory,
          saveCallDetails,
          (cfg != null ? String.valueOf(cfg.getSavePropertyInventory()) : "null"),
          (cfg != null ? String.valueOf(cfg.getSave_Call_Details()) : "null"));

      log.info("SESSION_KEYS fanoutId={} channel={} app='{}' org='{}' caller='{}' appBlank={} orgBlank={}",
          fanoutId, ch, sessApp, sessOrg, sessCaller, sessApp.isBlank(), sessOrg.isBlank());

      // ------------------------------------------------------------------
      // CRM customer update (NO RETRY) - FULL IN/OUT logs
      // ------------------------------------------------------------------
      Mono<Void> crmMono = Mono.defer(() -> {
    	  
    	  final long t0 = System.nanoTime();

    	  if (!isIvrCall && !callerSpoke) {
    	    log.info("CRM OUT fanoutId={} channel={} result=skipped reason=caller_not_spoken callerChars={} ms={}",
    	        fanoutId, ch, callerChars, msSince(t0));
    	    return Mono.empty();
    	  }

        log.info("CRM IN fanoutId={} channel={} saveCustomerInventory={} saveCustomerFranchise={} app='{}' org='{}' caller='{}' transcriptLen={}",
            fanoutId, ch, saveCustomerPropertyInventory,saveCustomerFranchiseInventory, sessApp, sessOrg, sessCaller, transcriptEnFinal.length());

        if (!(saveCustomerPropertyInventory||saveCustomerFranchiseInventory)) {
          log.info("CRM OUT fanoutId={} channel={} result=skipped reason=saveCustomer_false ms={}",
              fanoutId, ch, msSince(t0));
          return Mono.empty();
        }
        if (sessApp.isBlank() || sessOrg.isBlank()) {
          log.error("CRM OUT fanoutId={} channel={} result=skipped reason=missing_keys app='{}' org='{}' ms={}",
              fanoutId, ch, sessApp, sessOrg, msSince(t0));
          return Mono.empty();
        }

        AtomicBoolean started = new AtomicBoolean(false);

        return customerInventoryCompletionService
            .completeAndUpdateCrm(sessApp, sessOrg, ch, sessCaller, rawOrigFinal,transcriptEnFinal,summaryFinal,isIvrCall,saveCustomerPropertyInventory,saveCustomerFranchiseInventory,dtmfFinal)
            .doOnSubscribe(sub -> {
              started.set(true);
              log.info("CRM MID fanoutId={} channel={} stage=subscribed ms={}", fanoutId, ch, msSince(t0));
            })
            .doOnSuccess(ok -> {
              log.info("CRM OUT fanoutId={} channel={} result=success started={} ok={} ms={}",
                  fanoutId, ch, started.get(), ok, msSince(t0));
            })
            .doOnError(ex -> {
              log.error("CRM OUT fanoutId={} channel={} result=error started={} ms={} msg={}",
                  fanoutId, ch, started.get(), msSince(t0), safe(ex.getMessage()), ex);
            })
            .onErrorResume(ex -> Mono.empty())
            .then()
            .doFinally(sig -> {
              log.info("CRM FINALLY fanoutId={} channel={} signal={} started={} ms={}",
                  fanoutId, ch, String.valueOf(sig), started.get(), msSince(t0));
            });
      });

      // ------------------------------------------------------------------
      // Call reporting (NO RETRY) - FULL IN/OUT logs
      // ------------------------------------------------------------------
      Mono<Void> reportMono = Mono.defer(() -> {
        final long t0 = System.nanoTime();

        log.info("REPORT IN fanoutId={} channel={} saveCallDetails={} app='{}' org='{}' caller='{}'",
            fanoutId, ch, saveCallDetails, sessApp, sessOrg, sessCaller);

        if (!saveCallDetails) {
          log.info("REPORT OUT fanoutId={} channel={} result=skipped reason=saveCallDetails_false ms={}",
              fanoutId, ch, msSince(t0));
          return Mono.empty();
        }

        return Mono.fromRunnable(() -> {
          log.info("REPORT MID fanoutId={} channel={} stage=run_start ms={}", fanoutId, ch, msSince(t0));
          callReportingService.reportCall(s, b);
        })
        .doOnSuccess(v -> log.info("REPORT OUT fanoutId={} channel={} result=success ms={}", fanoutId, ch, msSince(t0)))
        .doOnError(ex -> log.error("REPORT OUT fanoutId={} channel={} result=error ms={} msg={}",
            fanoutId, ch, msSince(t0), safe(ex.getMessage()), ex))
        .onErrorResume(ex -> Mono.empty())
        .then()
        .doFinally(sig -> log.info("REPORT FINALLY fanoutId={} channel={} signal={} ms={}",
            fanoutId, ch, String.valueOf(sig), msSince(t0)));
      });

      // ------------------------------------------------------------------
      // AI amount deduction (NO RETRY) - FULL IN/OUT logs + logs returned Boolean
      // ------------------------------------------------------------------
      Mono<Void> deductMono = Mono.defer(() -> {
        final long t0 = System.nanoTime();

        log.info("DEDUCT IN fanoutId={} channel={} app='{}' org='{}' billedSecs={}",
            fanoutId, ch, sessApp, sessOrg, billedDurationSecs);

        if (sessOrg.isBlank() || sessApp.isBlank()) {
          log.error("DEDUCT OUT fanoutId={} channel={} result=skipped reason=missing_keys app='{}' org='{}' ms={}",
              fanoutId, ch, sessApp, sessOrg, msSince(t0));
          return Mono.empty();
        }

        AtomicBoolean started = new AtomicBoolean(false);

        return deductAiAmountService
            .deductAiAmount(sessApp, sessOrg, billedDurationSecs,cfg.getDynamicCost(),cfg.getCallCost(),cfg.getCallCostMode(),s.getChannelId(),sessCaller,s.isRedirectChannel(),s.isIvrCall())
            .doOnSubscribe(sub -> {
              started.set(true);
              log.info("DEDUCT MID fanoutId={} channel={} stage=subscribed ms={}", fanoutId, ch, msSince(t0));
            })
            .doOnNext(ok -> {
              log.info("DEDUCT OUT fanoutId={} channel={} result=success started={} ok={} ms={}",
                  fanoutId, ch, started.get(), ok, msSince(t0));
            })
            .doOnError(ex -> {
              log.error("DEDUCT OUT fanoutId={} channel={} result=error started={} ms={} msg={}",
                  fanoutId, ch, started.get(), msSince(t0), safe(ex.getMessage()), ex);
            })
            .onErrorResume(ex -> Mono.empty())
            .then()
            .doFinally(sig -> {
              log.info("DEDUCT FINALLY fanoutId={} channel={} signal={} started={} ms={}",
                  fanoutId, ch, String.valueOf(sig), started.get(), msSince(t0));
            });
      });

      // ------------------------------------------------------------------
      // Final fan-out
      // ------------------------------------------------------------------
      log.info("FANOUT BEGIN fanoutId={} channel={} crmWanted={} reportWanted={} deductWanted={}",
          fanoutId,
          ch,
          ((saveCustomerPropertyInventory || saveCustomerFranchiseInventory) && (isIvrCall || callerSpoke) && !sessApp.isBlank() && !sessOrg.isBlank()),
          saveCallDetails,
          (!sessApp.isBlank() && !sessOrg.isBlank()));

      final long fan0 = System.nanoTime();

      Mono.whenDelayError(crmMono, reportMono, deductMono)
          .doOnError(ex -> log.error("FANOUT OUT fanoutId={} channel={} result=error ms={} msg={}",
              fanoutId, ch, msSince(fan0), safe(ex.getMessage()), ex))
          .doOnSuccess(v -> log.info("FANOUT OUT fanoutId={} channel={} result=success ms={}",
              fanoutId, ch, msSince(fan0)))
          .doFinally(sig -> log.info("COMPLETE done fanoutId={} channel={} reason={} signal={} ms={}",
              fanoutId, ch, nvl(reason), String.valueOf(sig), msSince(fan0)))
          .subscribe();

    } catch (Exception e) {
      log.error("COMPLETE error fanoutId={} channel={} reason={} msg={}",
          fanoutId, ch, nvl(reason), safe(e.getMessage()), e);
    }
  }

  private static String nvl(String s) {
    return (s == null) ? "" : s;
  }

  private static String safe(String s) {
    if (s == null) return "null";
    return s.replace("\r", " ").replace("\n", " ").trim();
  }

  private static String truncate(String s, int max) {
    if (s == null) return "null";
    String v = safe(s);
    if (v.length() <= max) return v;
    return v.substring(0, max) + " ...[truncated len=" + v.length() + "]";
  }

  private static long msSince(long t0Nanos) {
    return Duration.ofNanos(System.nanoTime() - t0Nanos).toMillis();
  }

  private static String buildFanoutId() {
    // short correlation id: time + random
    long now = System.currentTimeMillis();
    int r = ThreadLocalRandom.current().nextInt(1000, 9999);
    return now + "-" + r;
  }
}
