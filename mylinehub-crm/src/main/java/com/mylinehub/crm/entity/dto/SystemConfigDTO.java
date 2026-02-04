package com.mylinehub.crm.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfigDTO {
 
	private Long id;
    
    int jwtTokenValidationDays;
    String organization;
    String smtphost;
    int smtpport;
    String smtpusername;
    String smtppassword;
    String whatsAppNotificationNumber;
    String onboardingTemplate;
    String lowBalanceTemplate;
    String gstEngineName;
    String gstEngineNameSecond;

}
