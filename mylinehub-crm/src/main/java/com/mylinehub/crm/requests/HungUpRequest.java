package com.mylinehub.crm.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HungUpRequest {

	
	String organization;
	String domain;
	String secondDomain;
	boolean isSecondLine;
	String channelToHungUp;
	Integer cause;
	Long timeOut;
}
