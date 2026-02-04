package com.mylinehub.crm.entity.dto;


import javax.persistence.Column;

import com.mylinehub.crm.entity.CustomerFranchiseInventory;
import com.mylinehub.crm.entity.CustomerPropertyInventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDTO {
	Long id;
    String firstname;
    String lastname;
    String zipCode;
    String city;
    String email;
    String phoneNumber;
    String description;
    String business;
    String country;
    String phoneContext;
    String domain;
//These are backend information. They are not required in front end.
//     String whatsAppRegisteredByPhoneNumberId;
//    String whatsApp_wa_id;
//    String whatsAppDisplayPhoneNumber;
//    String whatsAppPhoneNumberId;
//    String whatsAppProjectId;
    boolean coverted;
    String datatype;
    String organization;
    boolean remindercalling;
    String cronremindercalling;
    boolean iscalledonce;
    String pesel;
    public String preferredLanguage;
    public String secondPreferredLanguage;
    private String imageName;
    private String imageType;
    private String imageData;
    
    private String interestedProducts;
    boolean autoWhatsAppAIReply;
    boolean firstWhatsAppMessageIsSend;
    public String lastConnectedExtension;
    boolean updatedByAI;
    private CustomerPropertyInventory propertyInventory;
    private CustomerFranchiseInventory franchiseInventory;
}
