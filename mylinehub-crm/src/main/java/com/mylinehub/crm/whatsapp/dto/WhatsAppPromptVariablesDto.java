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
public class WhatsAppPromptVariablesDto {
    private Long id;
    private Long whatsAppPromptId;
    private String label;
    private String description;
    private boolean active;
    private String organization;
//    private Instant lastUpdatedOn;
}
