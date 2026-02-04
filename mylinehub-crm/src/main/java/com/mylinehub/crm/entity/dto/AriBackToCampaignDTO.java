package com.mylinehub.crm.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AriBackToCampaignDTO {

	    public String aiApplicationName;
	    public String organization;
	    public String callerPhone;
	    public String channelId;
	    
}
