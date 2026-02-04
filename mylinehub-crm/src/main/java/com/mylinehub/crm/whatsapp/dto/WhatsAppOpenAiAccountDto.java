package com.mylinehub.crm.whatsapp.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppOpenAiAccountDto {
	 private Long id;
	    private String key;
	    private String adminKey;
	    private String projectID;
	    private String assistantID;
	    private String email;  
	    private String chatBotName;
	    private String chatBotAccess;
	    private String clientSecret;
	    private String organization;
	    private Instant lastUpdatedOn;
}
