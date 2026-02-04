package com.mylinehub.crm.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttemptedTransferCallRequest {
	
	String organization;
	String domain;
	String secondDomain;
	boolean isSecondLine;
	String channelFromTransfer;
	String callerID;
	String context;
	String extensionToTransfer;
	int priority;
	Long timeOut;

}
