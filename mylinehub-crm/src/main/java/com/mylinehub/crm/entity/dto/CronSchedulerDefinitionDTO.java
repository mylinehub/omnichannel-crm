package com.mylinehub.crm.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CronSchedulerDefinitionDTO {

	private Long campaignId;
    private String cronExpression;
    private String actionType;
    private String data;
    private String organization;
    private String domain;
    private String fromExtension;
    private String phoneNumber;
    private String callType;
    private String context;
	private int priority;
	private Long timeOut;
	private String firstName;
	private String protocol;
	private String phoneTrunk;
}
