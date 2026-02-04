package com.mylinehub.crm.data.dto;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignEmployeeDataDTO {
	
	PageInfoDTO pageInfoDTO;
	List<String> customerPage;
	Date deletedDateTime;
}
