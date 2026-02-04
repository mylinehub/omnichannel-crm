package com.mylinehub.crm.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerToCampaignDTO {

	public Long id;
	public Long campaignid;
	public String campaignName;
    public Long customerid;
    public String organization;
    public String firstname;
    public String email; 
    public String phoneNumber;
    public String isCalledOnce;
	public String lastConnectedExtension;
}

