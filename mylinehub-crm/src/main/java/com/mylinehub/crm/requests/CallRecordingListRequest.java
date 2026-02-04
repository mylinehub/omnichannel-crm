package com.mylinehub.crm.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CallRecordingListRequest {

	String organization;
	String domain;
	String extension;
	String fileName;
	int year;
	int month;
	int day;
	
}
