package com.mylinehub.crm.entity.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FixedDateSchedulerDefinitionDTO {
	
	private Long campaignId;
	private String actionType;
	private String data;
	private String organization;
	private String domain;
	Date date;
	private String phoneNumber;
	private String callType;
	private String fromExtension;
	private String context;
	private int priority;
	private Long timeOut;
	private String firstName;
	private String protocol;
	private String phoneTrunk;
}