package com.mylinehub.crm.whatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppReportVariableDto {

	
	public long totalMessagesReceived;
	public long totalMediaSizeSendMB;
	public long totalAmountSpend;
	public long totalTokenReceived;
	public long totalTokenSend;


	public long totalMessagesSend;
	public long aiMessagesSend;
	public long campaignMessagesSend;
	public long manualMessagesSend;


	public long totalMessagesDelivered;
	public long aiMessagesDelivered;
	public long campaignMessagesDelivered;
	public long manualMessagesDelivered;

	public long totalMessagesRead;
	public long aiMessagesRead;
	public long campaignMessagesRead;
	public long manualMessagesRead;

	public long totalMessagesFailed;
	public long aiMessagesFailed;
	public long campaignMessagesFailed;
	public long manualMessagesFailed;

	public long totalMessagesDeleted;
	public long aiMessagesDeleted;
	public long campaignMessagesDeleted;
	public long manualMessagesDeleted;


}
