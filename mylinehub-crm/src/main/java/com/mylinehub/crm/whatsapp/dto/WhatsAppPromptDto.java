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
public class WhatsAppPromptDto {
	private Long id;
    private Long whatsAppPhoneNumberId;
    private String prompt;
    private String category;
    private boolean active;
    private String delimiter;
    private String organization;
//    private Instant lastUpdatedOn;
}
