package com.mylinehub.crm.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListenQuietlyRequest {
	String organization;
	String domain;
	String secondDomain;
	boolean isSecondLine;
	String callingChannel;
	String callerID;
	String channelToCall;
	int priority;
	Long timeOut;
	boolean async;
	
}
