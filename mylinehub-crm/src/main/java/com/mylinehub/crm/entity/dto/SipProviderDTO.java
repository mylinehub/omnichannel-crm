package com.mylinehub.crm.entity.dto;

import javax.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SipProviderDTO {

    Long id;
	String organization;
	String providerName;
	boolean active;
	String phoneNumber;
	String company;
	String costCalculation;
	String meteredPlanAmount;
	
}
