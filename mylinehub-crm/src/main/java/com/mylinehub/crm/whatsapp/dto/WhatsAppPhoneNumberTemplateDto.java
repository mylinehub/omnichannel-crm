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
public class WhatsAppPhoneNumberTemplateDto {
	private Long id;
	private Long whatsAppPhoneNumberId;
    private String templateName;
    private String conversationType;
    private String organization;
    private String mediaPath;
    private String mediaType;
    private String mediaId;
    private String currency;
    private String languageCode;
    private boolean followOrder;
    private Long productId;
//    private Instant lastUpdatedOn;
}
