package com.mylinehub.voicebridge.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmCustomerUpdateRequestDto {
	
	private String organization;
    private String business;
    private String city;
    private String country;
    private String cronremindercalling;
    private String description;
    private String email;
    private String firstname;
    private String lastname;
    private String phoneNumber;
    private String preferredLanguage;
    private CrmPropertyInventoryUpdateDto propertyInventory;
    private CrmFranchiseInventoryUpdateDto franchiseInventory;
    private String secondPreferredLanguage;
    private String zipCode;
}
