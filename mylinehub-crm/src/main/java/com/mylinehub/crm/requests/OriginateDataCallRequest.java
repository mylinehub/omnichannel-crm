package com.mylinehub.crm.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OriginateDataCallRequest {

	String organization;
	String domain;
	String secondDomain;
	boolean isSecondLine;
	String channelToCall;
	String callerID;
	String data;
	String application;
	int priority;
	Long timeOut;
	boolean async;
	
}
