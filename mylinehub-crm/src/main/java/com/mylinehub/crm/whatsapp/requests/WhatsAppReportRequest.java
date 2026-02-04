package com.mylinehub.crm.whatsapp.requests;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppReportRequest {
	
	public String dateRange;
	public List<String> whatsAppPhoneNumbers;
	public String organization;

}
