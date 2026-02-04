package com.mylinehub.crm.whatsapp.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppPhoneNumberTemplateVariableDto {
	private Long id;
    private Long whatsAppPhoneNumberTemplateId;
    private int orderNumber;
    private String variableName;
    private String variableType;
    private String variableHeaderType;
    private String mediaID;
    private String mediaUrl;
    private String fileName;
    private String caption;
    private String mediaSelectionType;
    private String organization;
}
