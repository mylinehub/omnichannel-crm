package com.mylinehub.voicebridge.crm.mapper;

import com.mylinehub.voicebridge.billing.CallBillingInfo;
import com.mylinehub.voicebridge.session.CallSession;
import com.mylinehub.voicebridge.crm.dto.CdrDTO;

import java.time.Instant;

/**
 * Bridge mapper: convert VoiceBridge CallSession + CallBillingInfo
 * into CRM CdrDTO for the new CDR API.
 *
 * IMPORTANT:
 *  - When we don't have real values, we fill placeholders:
 *      callerid/customerid: "AICALL-<epochMillis>" or use billing.channelId
 *      employeeName: "AI-Call"
 *      customerName: callerid
 */
public class CallSessionToCdrDTO {

    public static CdrDTO convert(CallSession session, CallBillingInfo billing) {

        // Unique fallback string
        String baseUnique = "AICALL-" + Instant.now().toEpochMilli();

        String organization = (billing != null) ? billing.getOrganization() : null;
        String channelId    = (billing != null) ? billing.getChannelId() : null;

        // ---------- Caller ID fallback ----------
        String callerId = baseUnique;
        // You can replace this with session.getCallerNumber() if available.

        String customerId   = callerId;
        String employeeName = "AI-Call";
        String customerName = callerId;

        // ---------- Call type fallback ----------
        String callType = "Inbound";
        // If billing has this, use billing.getCallType()

        // ---------- Session ID ----------
        String sessionId =
                (channelId != null && !channelId.trim().isEmpty())
                        ? ("AICALL-" + channelId)
                        : baseUnique;

        // ---------- FIXED: primitive long cannot be null ----------
        long durationSeconds =
                (billing != null)
                        ? billing.getDurationSeconds()  // primitive long
                        : 0L;

        return CdrDTO.builder()
                .callerid(callerId)
                .customerid(customerId)
                .employeeName(employeeName)
                .customerName(customerName)
                .trunkNumber(null)
                .callonmobile(false)
                .callType(callType)
                .pridictive(false)
                .progressive(false)
                .isconference(false)
                .ivr(false)
                .queue(false)
                .organization(organization)
                .callSessionId(sessionId)
                .durationSeconds(durationSeconds)
                .completeAICall(true)
                .totalAiCharactersReceived(billing.getTotalAiCharactersReceived())
                .totalAiCharactersSent(billing.getTotalAiCharactersSent())
                .totalApproxTokens(billing.getTotalApproxTokens())
                .ragErrorCount(session.getRagErrorCount())
                .callerWordCount(session.getCallerWordCount())
                .startTime(billing.getStartTime())
                .endTime(billing.getEndTime())
                .channelId(billing.getChannelId())
                .totalCallerWords(billing.getTotalCallerWords())
                .totalRagQueries(billing.getTotalRagQueries())
                .totalApproxTokens(billing.getTotalApproxTokens())
                .recordingFileName(billing.getRecordingFileName())
                .recordingPath(billing.getRecordingPath())
                .build();
    }
}
