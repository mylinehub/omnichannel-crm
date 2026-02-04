package com.mylinehub.crm.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonitorActionRequest {
	
	String organization;
	String domain;
	String secondDomain;
	boolean isSecondLine;
	String channel;
	Long actionTimeOut;
	String format;
	Boolean mix;

}
