
package com.mylinehub.crm.whatsapp.dto;

import java.time.Instant;
import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppPhoneNumberDto {
	private Long id;

    private Long whatsAppProjectId;
    
    private String phoneNumber;
    private String verifyToken;
    private String phoneNumberID;
    private String whatsAppAccountID;
    private String aiModel;
    private String callBackURL;
    private String callBackSecret;
    private String organization;
    private String country;
    private String currency;
    private String aiCallExtension;
    private Long adminEmployeeId;
    private Long secondAdminEmployeeId;
    private String employeeExtensionAccessList;
    private Long costPerInboundMessage;
    private Long costPerOutboundMessage;
    private Long costPerInboundAIMessageToken;
    private Long costPerOutboundAIMessageToken;
    private boolean active;
    private boolean autoAiMessageAllowed;
    private int autoAiMessageLimit;
    private boolean storeVerifyCustomerPropertyInventory;
    private String aiOutputClassName;
//    private Instant lastUpdatedOn;
}
