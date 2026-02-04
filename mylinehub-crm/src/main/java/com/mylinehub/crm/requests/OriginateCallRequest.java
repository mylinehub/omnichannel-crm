package com.mylinehub.crm.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OriginateCallRequest {

	String organization;
	String domain;
	String secondDomain;
	boolean isSecondLine;
	String channelToCall;
	String callerID;
	String context;
	String extensionToCall;
	int priority;
	Long timeOut;
	boolean async;
	
}
