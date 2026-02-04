package com.mylinehub.crm.requests;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BridgeTwoActiveCallsRequest {
	
	String organization;
	String domain;
	String secondDomain;
	boolean isSecondLine;
	String channelToCall;
	String callingChannel;
	boolean tone;
	Long timeOut;

}
