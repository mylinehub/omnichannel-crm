package com.mylinehub.crm.data.dto;

import java.util.Date;
import java.util.Map;

public class DeletedCampaignCustomerDataDTO {
	Map<String,CampaignCustomerDataDTO> deletedCustomer;
	boolean savedToDatabase;
	Date deletedDateTime;
}
