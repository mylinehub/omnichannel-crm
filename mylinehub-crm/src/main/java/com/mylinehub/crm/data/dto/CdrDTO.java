package com.mylinehub.crm.data.dto;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mylinehub.crm.entity.CallDetail;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CdrDTO {
	
	private Long campaignID;
    private String linkId;
	//Filled in NewLineMmeoryDataLayer	//Filled in NewLineMmeoryDataLayer
	Employee employee;
	Customers customer;
	int noOfNewLine;
	private String callerid;
    private String customerid;
    private String employeeName;
    private String customerName;
    private String trunkNumber;
    boolean callonmobile;
    String callType;
    boolean pridictive;
    boolean progressive;
    long amount;
	
	int noOfBridgeEnter;
	int noOfBridgeLeave;
	
	String callSessionId;
	CallDetail callDetail;
	Map<String,String> mapEvent;
	
    boolean isconference;
    boolean ivr;
    boolean queue;
    
    String organization;
    
    boolean triggerCustomerToExtentionInNewLineConnected;
    
    boolean completeAICall;
    
 
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
    
	Date lastUpdated;
	Date bridgeEnterTime;
}
