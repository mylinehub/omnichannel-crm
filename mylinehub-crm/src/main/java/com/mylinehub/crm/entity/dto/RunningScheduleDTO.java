package com.mylinehub.crm.entity.dto;

import java.time.Instant;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RunningScheduleDTO {

	Long id;
	 private String jobId;
    String scheduleType;
    String functionality;
    String cronExpression;
	Date date;
    Long campaignId;
	String actionType;
	String data;
	String organization;
	String domain;
	int seconds;
	String phoneNumber;
	String callType;
	String fromExtension;
	String context;
	int priority;
	Long timeOut;
	String firstName;
	String protocol;
	String phoneTrunk;
    Instant createdOn;
    Instant lastUpdatedOn;
}
