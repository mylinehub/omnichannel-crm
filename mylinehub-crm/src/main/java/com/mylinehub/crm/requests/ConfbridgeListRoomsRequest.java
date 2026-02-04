package com.mylinehub.crm.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfbridgeListRoomsRequest {

	String organization;
	String domain;
	String secondDomain;
	boolean isSecondLine;
	Long timeOut;
	
}
