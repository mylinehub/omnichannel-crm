package com.mylinehub.crm.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignCustomerDataDTO {

	String customerLastCall;
	boolean isCalledOnce;
}
