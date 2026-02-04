package com.mylinehub.voicebridge.crm.dto;

import java.time.Instant;

import com.fasterxml.jackson.databind.node.BooleanNode;

import lombok.Builder;
import lombok.Data;

/**
 * DTO sent from VoiceBridge -> MyLineHub CRM CDR API.
 *
 * NOTE:
 *  - Existing fields kept as-is.
 *  - New billing fields added at the end.
 */
@Data
@Builder
public class CdrDTO {

    // ---- Existing CRM / dialer fields ----
    private String callerid;
    private String customerid;
    private String employeeName;
    private String customerName;
    private String trunkNumber;
    private boolean callonmobile;
    private String callType;
    private boolean pridictive;
    private boolean progressive;
    private boolean isconference;
    private boolean ivr;
    private boolean queue;
    private String organization;
    private String callSessionId;
    private long   durationSeconds;

    private boolean completeAICall;
    
    // ---- NEW: full billing / session info from VoiceBridge ----
    private int ragErrorCount = 0;
    private int callerWordCount = 0;
    
    /** Start time of the call (from CallBillingInfo.startTime). */
    private Instant startTime;

    /** End time of the call (from CallBillingInfo.endTime). */
    private Instant endTime;

    /** Channel id used in VoiceBridge / Asterisk. */
    private String channelId;

    /** Total number of caller words recognized. */
    private Long totalCallerWords;

    /** How many RAG queries were made during the call. */
    private Long totalRagQueries;

    /** Approximate tokens consumed for the call. */
    private Long totalApproxTokens;

    /** Total characters of RAG context text we injected back into Realtime. */
    private long totalRagContextCharacters;

    /** Total characters of text we sent to Realtime (system instructions + RAG context, etc.). */
    private long totalAiCharactersSent;

    /** Total characters of text we received from Realtime (JSON events, etc., rough). */
    private long totalAiCharactersReceived;
    
    private String recordingFileName;
    /** Recording path/file name from VoiceBridge. */
    private String recordingPath;
}
