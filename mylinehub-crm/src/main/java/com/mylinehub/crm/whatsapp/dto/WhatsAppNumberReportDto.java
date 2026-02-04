package com.mylinehub.crm.whatsapp.dto;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppNumberReportDto {
	    private Long id;
	    
	    private String phoneNumberMain;
	    private String phoneNumberWith;
	    
	    private Long manualMessageSend;
	    private Long campaignMessageSend;
	    private Long aiMessagesSend;
	    private Long totalMessagesReceived;

	    private Long manualMessageDelivered;
	    private Long campaignMessageDelivered;
	    private Long aiMessagesDelivered;
	    
	    private Long manualMessageRead;
	    private Long campaignMessageRead;
	    private Long aiMessagesRead;
	    
	    private Long manualMessageFailed;
	    private Long campaignMessageFailed;
	    private Long aiMessagesFailed;
	    
	    private Long manualMessageDeleted;
	    private Long campaignMessageDeleted;
	    private Long aiMessagesDeleted;
	    
	    private Long aiTokenSend;
	    private Long totalTokenReceived;
	    
	    private Long totalAmountSpend;
	    private Long totalMediaSizeSendMB;
	    private String typeOfReport;
	    
	    private Map<String, WhatsAppExtensionReportingDTO> extensionReport;
	    
	    private Date dayUpdated;
	    private String organization;
	    private Instant lastUpdatedOn;
}
