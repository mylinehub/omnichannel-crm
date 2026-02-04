package com.mylinehub.voicebridge.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmCustomerDto {

    private Long id;

    private String organization;
    private String phoneNumber;
    private String phoneContext;

    private String firstname;
    private String lastname;

    private String email;
    private String business;
    private String domain;
    private String description;

    private String city;
    private String country;
    private String zipCode;

    private String preferredLanguage;
    private String secondPreferredLanguage;

    private Boolean remindercalling;
    private String cronremindercalling;

    private Boolean iscalledonce;
    private Boolean firstWhatsAppMessageIsSend;
    private Boolean autoWhatsAppAIReply;

    private String lastConnectedExtension;

    private Boolean coverted; // keeping your original spelling from curl

    private String interestedProducts;
    private String datatype;
    private String pesel;

    // If CRM returns these as base64 or something else, keep as String for safety
    private String imageName;
    private String imageType;
    private String imageData;

    private CrmPropertyInventoryDto propertyInventory;
    private CustomerFranchiseInventoryDto franchiseInventory;

}
