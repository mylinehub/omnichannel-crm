package com.mylinehub.crm.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerToCampaignCreateDTO {
	
		Long campaignId;
		
	    String zipCode;
	    String city;
	    String country;
	    String organization;
	    String datatype;
	    String business;
	    String description;
	    int start;
	    int limit;
	    String isAndOperator;

}
