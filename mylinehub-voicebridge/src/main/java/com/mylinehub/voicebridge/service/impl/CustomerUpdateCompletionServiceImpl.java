package com.mylinehub.voicebridge.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.voicebridge.models.IvrDtmfRule;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.models.StasisAppInstruction;
import com.mylinehub.voicebridge.service.CompletionErrorLogger;
import com.mylinehub.voicebridge.service.CompletionJsonValidator;
import com.mylinehub.voicebridge.service.CompletionPromptBuilder;
import com.mylinehub.voicebridge.service.CrmCustomerService;
import com.mylinehub.voicebridge.service.CustomerUpdateCompletionService;
import com.mylinehub.voicebridge.service.LlmCompletionClient;
import com.mylinehub.voicebridge.service.StasisAppConfigService;
import com.mylinehub.voicebridge.service.dto.CrmCustomerUpdateRequestDto;
import com.mylinehub.voicebridge.service.dto.CrmFranchiseInventoryUpdateDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerUpdateCompletionServiceImpl implements CustomerUpdateCompletionService {

  // Toggle for verbose logs (prompt/transcript/raw outputs will still be truncated for safety)
  private static final boolean DEEP_LOGS = true;

  private static final int MAX_ATTEMPTS = 2;

  // Truncation caps for logs
  private static final int LOG_PROMPT_MAX = 1200;
  private static final int LOG_TRANSCRIPT_MAX = 1200;
  private static final int LOG_LLM_OUT_MAX = 2000;
  private static final int LOG_CRM_OUT_MAX = 300; // boolean only, keep small

  private final StasisAppConfigService configService;
  private final StasisAppConfigService stasisService; // used for instruction fetch
  private final CompletionPromptBuilder promptBuilder;
  private final LlmCompletionClient llmClient;
  private final CompletionJsonValidator validator;
  private final CompletionErrorLogger errorLogger;
  private final CrmCustomerService crmCustomerService;
  private final ObjectMapper mapper;
	
  @Override
  public Mono<Boolean> completeAndUpdateCrm(String stasisAppName,
                                           String organization,
                                           String channelId,
                                           String callerNumber,
                                           String rawOrig,
                                           String transcriptEn,
                                           String summary,
                                           boolean isIvrCall,
                                           boolean savePropertyInventory,
                                           boolean saveFranchiseInventory,
                                           String ivrDtmf) {
	  
	final long t0 = System.nanoTime();

	if (!savePropertyInventory && !saveFranchiseInventory) {
		  if (DEEP_LOGS) {
		    log.info("[COMP] skip no_updates_requested stasisApp={} channel={} caller={}",
		        safe(stasisAppName), safe(channelId), safe(callerNumber));
		  }
		  return Mono.just(true);
	}

    if (DEEP_LOGS) {
      log.info("[COMP] start stasisApp={} org={} channel={} caller={} transcriptLen={}",
          safe(stasisAppName), safe(organization), safe(channelId), safe(callerNumber),
          (transcriptEn != null ? transcriptEn.length() : 0));
    }

    StasisAppConfig cfg = configService.getConfigOrNull(stasisAppName);
    if (cfg == null) {
      log.error("[COMP] no_config stasisApp={} channel={} org={} caller={}",
          safe(stasisAppName), safe(channelId), safe(organization), safe(callerNumber));
      return Mono.error(new IllegalStateException("No config for stasisApp=" + stasisAppName));
    }

	 // ------------------------------------------------------------
	 // IVR + Franchise update (NO LLM)
	 // ------------------------------------------------------------
	 if (isIvrCall) {
	
	   if (saveFranchiseInventory) {
		   String dtmf = (ivrDtmf != null ? ivrDtmf.trim() : "");
		   String rulesJson = (cfg.getIvr_dtmf_rules_json() != null ? cfg.getIvr_dtmf_rules_json().trim() : "");
		
		   boolean available = false;
		   String interest = "";
		
		   if (!dtmf.isEmpty() && !rulesJson.isEmpty()) {
		     try {
		       List<IvrDtmfRule> rules = mapper.readValue(rulesJson, new TypeReference<List<IvrDtmfRule>>() {});
		       IvrDtmfRule matched = null;
		
		       if (rules != null) {
		         // 1) exact match (ex: "1:2")
		         for (IvrDtmfRule r : rules) {
		           if (r == null) continue;
		           if (safe(r.getDtmfPressed()).trim().equalsIgnoreCase(dtmf)) { matched = r; break; }
		         }
		
		         // 2) fallback: last digit
		         if (matched == null) {
		           String last = dtmf;
		           int idx = dtmf.lastIndexOf(':');
		           if (idx >= 0 && idx < dtmf.length() - 1) last = dtmf.substring(idx + 1).trim();
		
		           for (IvrDtmfRule r : rules) {
		             if (r == null) continue;
		             if (safe(r.getDtmfPressed()).trim().equalsIgnoreCase(last)) { matched = r; break; }
		           }
		         }
		       }
		
		       if (matched != null) {
		         String i = safe(matched.getInterest()).trim();
		         if (!i.isEmpty()) {
		           interest = i;
		           available = true;
		         }
		       }
		
		     } catch (Exception e) {
		       log.error("[COMP] franchise_rules_parse_err stasisApp={} channel={} msg={}",
		           safe(stasisAppName), safe(channelId), safe(e.getMessage()), e);
		       // keep available=false, interest=""
		     }
		   } else {
		     // dtmf missing OR rules missing => available=false, interest=""
		     if (dtmf.isEmpty()) {
		       log.warn("[COMP] franchise_no_dtmf -> available=false stasisApp={} channel={} caller={}",
		           safe(stasisAppName), safe(channelId), safe(callerNumber));
		     }
		     if (rulesJson.isEmpty()) {
		       log.warn("[COMP] franchise_no_rules_json -> available=false stasisApp={} channel={} caller={}",
		           safe(stasisAppName), safe(channelId), safe(callerNumber));
		     }
		   }
		
		   CrmCustomerUpdateRequestDto dto = new CrmCustomerUpdateRequestDto();
		   dto.setOrganization(organization);
		   dto.setPhoneNumber(callerNumber);

		   dto.setFranchiseInventory(
		       CrmFranchiseInventoryUpdateDto.builder()
		           .interest(interest)       // "" when no input/match
		           .available(available)     // false when no input/match
		           .build()
		   );
		
		   if (DEEP_LOGS) {
		     log.info("[COMP] franchise_update_req stasisApp={} channel={} caller={} dtmf='{}' available={} interest='{}'",
		         safe(stasisAppName), safe(channelId), safe(callerNumber), safe(dtmf), available, safe(interest));
		   }
		
		   long tCrm0 = System.nanoTime();
		   return crmCustomerService.updateCustomerByOrganization(stasisAppName, organization, callerNumber, dto)
		       .flatMap(ok -> {
		         long crmMs = Duration.ofNanos(System.nanoTime() - tCrm0).toMillis();
		         log.info("[COMP] franchise_crm_done stasisApp={} channel={} ms={} ok={}",
		             safe(stasisAppName), safe(channelId), crmMs, ok);
		         if (ok == null || !ok) return Mono.error(new IllegalStateException("CRM update returned false"));
		         return Mono.just(true);
		       })
		       .onErrorResume(e -> {
		         log.error("[COMP] franchise_crm_err stasisApp={} channel={} msg={}",
		             safe(stasisAppName), safe(channelId), safe(e.getMessage()), e);
		         return Mono.just(false);
		       });   
	   }
	   else {
		   return Mono.just(true);
	   }
	 }

	if (!savePropertyInventory) {
		  if (DEEP_LOGS) {
		    log.info("[COMP] skip_llm savePropertyInventory=false stasisApp={} channel={} caller={}",
		        safe(stasisAppName), safe(channelId), safe(callerNumber));
		  }
		  return Mono.just(true);
	}
	
    // IMPORTANT: fetch completion_instructions from DB-backed instruction cache
    StasisAppInstruction ins = stasisService.getInstructionOrNull(stasisAppName);
    String completionTemplate = (ins != null ? ins.getCompletionInstructions() : null);
    if (completionTemplate == null || completionTemplate.trim().isEmpty()) {
      log.error("[COMP] missing_completion_instructions stasisApp={} channel={} org={} caller={} insPresent={}",
          safe(stasisAppName), safe(channelId), safe(organization), safe(callerNumber), (ins != null));
      return Mono.error(new IllegalStateException("Missing completion_instructions for stasisApp=" + stasisAppName));
    }

    String prompt = promptBuilder.build(
        completionTemplate,
        organization,
        callerNumber,
        channelId,
        rawOrig,
        transcriptEn,
        summary
    );

    // DB-driven
    String apiKey = cfg.getAi_openai_apiKey(); // do NOT log this
    String model = cfg.getAi_model_completion();

    // Input can be transcript only; instructions contain the JSON schema/spec.
    String input = transcriptEn != null ? transcriptEn : "";

    if (DEEP_LOGS) {
      log.info("[COMP] prepared stasisApp={} channel={} model={} tplLen={} promptLen={} promptHash={} inputLen={} inputHash={}",
          safe(stasisAppName), safe(channelId), safe(model),
          completionTemplate.length(),
          (prompt != null ? prompt.length() : 0),
          sha256Hex(prompt),
          input.length(),
          sha256Hex(input));

      log.debug("[COMP] prompt_preview stasisApp={} channel={} prompt={}",
          safe(stasisAppName), safe(channelId), truncate(prompt, LOG_PROMPT_MAX));

      log.debug("[COMP] transcript_preview stasisApp={} channel={} transcript={}",
          safe(stasisAppName), safe(channelId), truncate(transcriptEn, LOG_TRANSCRIPT_MAX));
    }

    return attempt(stasisAppName, organization, channelId, callerNumber, transcriptEn, model, apiKey, prompt, input, 1)
        .doOnSuccess(ok -> {
          long ms = Duration.ofNanos(System.nanoTime() - t0).toMillis();
          log.info("[COMP] done stasisApp={} channel={} caller={} ms={} ok={}",
              safe(stasisAppName), safe(channelId), safe(callerNumber), ms, ok);
        })
        .doOnError(err -> {
          long ms = Duration.ofNanos(System.nanoTime() - t0).toMillis();
          log.error("[COMP] done_err stasisApp={} channel={} caller={} ms={} err={}",
              safe(stasisAppName), safe(channelId), safe(callerNumber), ms,
              (err != null ? safe(err.getMessage()) : "null"), err);
        });
  }

  private Mono<Boolean> attempt(String stasisAppName,
                               String organization,
                               String channelId,
                               String callerNumber,
                               String transcriptEn,
                               String model,
                               String apiKey,
                               String prompt,
                               String input,
                               int attempt) {

    final long t0 = System.nanoTime();

    log.info("[COMP] llm_call_start stasisApp={} channel={} attempt={} model={} promptLen={} inputLen={}",
        safe(stasisAppName), safe(channelId), attempt, safe(model),
        (prompt != null ? prompt.length() : 0),
        (input != null ? input.length() : 0));

    return llmClient.runJsonCompletion(apiKey, model, prompt, input)
        .flatMap(rawOut -> {
          long ms = Duration.ofNanos(System.nanoTime() - t0).toMillis();

          log.info("[COMP] llm_call_ok stasisApp={} channel={} attempt={} ms={} outLen={} outHash={}",
              safe(stasisAppName), safe(channelId), attempt, ms,
              (rawOut != null ? rawOut.length() : 0),
              sha256Hex(rawOut));

          log.debug("[COMP] llm_raw_out stasisApp={} channel={} attempt={} out={}",
              safe(stasisAppName), safe(channelId), attempt,
              truncate(rawOut, LOG_LLM_OUT_MAX));

          try {
            long tVal0 = System.nanoTime();
            CrmCustomerUpdateRequestDto dto = validator.parseAndValidateOrThrow(rawOut, callerNumber);
            long valMs = Duration.ofNanos(System.nanoTime() - tVal0).toMillis();

            log.info("[COMP] validate_ok stasisApp={} channel={} attempt={} ms={} dtoNull={}",
                safe(stasisAppName), safe(channelId), attempt, valMs, (dto == null));

            if (DEEP_LOGS && dto != null) {
              log.debug("[COMP] dto_preview stasisApp={} channel={} attempt={} dto={}",
                  safe(stasisAppName), safe(channelId), attempt,
                  truncate(String.valueOf(dto), 1200));
            }

            // Defensive: ensure org is present in body (CRM service reads body.organization)
            if (dto != null) {
              try {
                dto.setOrganization(organization);
                dto.setPhoneNumber(callerNumber);
                if (dto.getPropertyInventory() != null) {
                    if (dto.getPropertyInventory().getListedDate() != null) {
                        dto.getPropertyInventory().setListedDate(null);
                    }
                }
                
              } catch (Exception ignore) {
                // If your DTO doesn't have organization field, you MUST add it.
                log.error("[COMP] dto_missing_organization_field -> ADD organization to CrmCustomerUpdateRequestDto");
              }
            }

            long tCrm0 = System.nanoTime();
            return crmCustomerService.updateCustomerByOrganization(
                    stasisAppName,
                    organization,
                    callerNumber, // oldPhone (your CRM uses oldPhone param)
                    dto
                )
                .flatMap(ok -> {
                  long crmMs = Duration.ofNanos(System.nanoTime() - tCrm0).toMillis();

                  log.info("[COMP] crm_update_done stasisApp={} channel={} attempt={} ms={} ok={}",
                      safe(stasisAppName), safe(channelId), attempt, crmMs, ok);

                  if (DEEP_LOGS) {
                    log.debug("[COMP] crm_update_preview stasisApp={} channel={} attempt={} crm={}",
                        safe(stasisAppName), safe(channelId), attempt,
                        truncate(String.valueOf(ok), LOG_CRM_OUT_MAX));
                  }

                  if (ok == null || !ok) {
                    return Mono.error(new IllegalStateException("CRM update returned false"));
                  }
                  return Mono.just(true);
                })
                .doOnError(e -> {
                  long crmMs = Duration.ofNanos(System.nanoTime() - tCrm0).toMillis();
                  log.error("[COMP] crm_update_err stasisApp={} channel={} attempt={} ms={} err={}",
                      safe(stasisAppName), safe(channelId), attempt, crmMs,
                      (e != null ? safe(e.getMessage()) : "null"));
                });

          } catch (Exception parseErr) {
            String msg = (parseErr.getMessage() != null ? parseErr.getMessage() : parseErr.getClass().getSimpleName());

            log.warn("[COMP] validate_fail stasisApp={} channel={} attempt={} reason={}",
                safe(stasisAppName), safe(channelId), attempt, safe(msg));

            errorLogger.logError(
                stasisAppName, organization, channelId, callerNumber,
                model, attempt,
                "JSON_PARSE",
                msg,
                prompt,
                transcriptEn,
                rawOut
            );

            if (attempt < MAX_ATTEMPTS) {
              log.warn("[COMP] retrying stasisApp={} channel={} nextAttempt={} reason={}",
                  safe(stasisAppName), safe(channelId), attempt + 1, safe(msg));

              String repairPrompt = (prompt == null ? "" : prompt)
                  + "\n\nSTRICT FIX:\nReturn ONLY valid JSON for the exact schema. No extra text.";

              if (DEEP_LOGS) {
                log.debug("[COMP] repair_prompt_preview stasisApp={} channel={} attempt={} prompt={}",
                    safe(stasisAppName), safe(channelId), attempt + 1,
                    truncate(repairPrompt, LOG_PROMPT_MAX));
              }

              return attempt(stasisAppName, organization, channelId, callerNumber, transcriptEn,
                  model, apiKey, repairPrompt, input, attempt + 1);
            }

            log.warn("[COMP] giving_up_parse stasisApp={} channel={} attempts={} caller={}",
                safe(stasisAppName), safe(channelId), attempt, safe(callerNumber));

            return Mono.just(false);
          }
        })
        .onErrorResume(callErr -> {
          long ms = Duration.ofNanos(System.nanoTime() - t0).toMillis();
          String msg = (callErr.getMessage() != null ? callErr.getMessage() : callErr.getClass().getSimpleName());

          log.error("[COMP] llm_call_err stasisApp={} channel={} attempt={} ms={} err={}",
              safe(stasisAppName), safe(channelId), attempt, ms, safe(msg));

          errorLogger.logError(
              stasisAppName, organization, channelId, callerNumber,
              model, attempt,
              "LLM_CALL",
              msg,
              prompt,
              transcriptEn,
              null
          );

          if (attempt < MAX_ATTEMPTS) {
            log.warn("[COMP] retrying_llm_call stasisApp={} channel={} nextAttempt={} reason={}",
                safe(stasisAppName), safe(channelId), attempt + 1, safe(msg));

            return attempt(stasisAppName, organization, channelId, callerNumber, transcriptEn,
                model, apiKey, prompt, input, attempt + 1);
          }

          log.warn("[COMP] giving_up_llm_call stasisApp={} channel={} attempts={} caller={}",
              safe(stasisAppName), safe(channelId), attempt, safe(callerNumber));

          return Mono.just(false);
        });
  }

  // -------------------------
  // Helpers (safe logging)
  // -------------------------

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

  private static String sha256Hex(String s) {
    if (s == null) return "null";
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(dig.length * 2);
      for (byte b : dig) {
        sb.append(Character.forDigit((b >> 4) & 0xF, 16));
        sb.append(Character.forDigit(b & 0xF, 16));
      }
      return sb.toString();
    } catch (Exception e) {
      return "sha256_err";
    }
  }
}
