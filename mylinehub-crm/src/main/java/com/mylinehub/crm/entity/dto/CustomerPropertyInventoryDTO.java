package com.mylinehub.crm.entity.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPropertyInventoryDTO {

    private Long id;

    // =========================
    // Customer fields (FLAT)
    // =========================
    private Long customerId;
    private String customerFirstname;
    private String customerLastname;
    private String customerPhoneNumber;
    private String customerEmail;
    private String customerCity;
    private String customerOrganization;

    // =========================
    // Inventory fields
    // =========================
    private String premiseName;
    private Instant listedDate;
    private String propertyType;

    private Boolean rent;
    private Long rentValue;

    private Integer bhk;
    private String furnishedType;
    private Integer sqFt;
    private String nearby;
    private String purpose;
    private String area;
    private String city;

    private String callStatus;
    private Integer propertyAge;

    private String unitType;
    private String tenant;
    private String facing;

    private Integer totalFloors;
    private String brokerage;

    private Integer balconies;
    private Integer washroom;

    private String unitNo;
    private String floorNo;

    private String pid;
    private String propertyDescription1;
    private Boolean moreThanOneProperty;
    private Boolean updatedByAi;
    private Boolean available;
    
    private Instant createdOn;
    private Instant lastUpdatedOn;
}
