package com.mylinehub.crm.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AfterNSecondsSchedulerDefinitionDTO {
	
	private Long campaignId;
	private String actionType;
	private String data;
	private String organization;
	private String domain;
	int seconds;
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