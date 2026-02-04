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
public class WhatsAppProjectDto {
	private Long id;
    private String appName;
    private String appEmail;
    private String appID;
    private String appSecret;
    private String apiVersion;
    private String businessID;  
    private String businessPortfolio;
    private String accessToken;
    private String clientToken;
    private String organization;
//    private Instant lastUpdatedOn;
}
