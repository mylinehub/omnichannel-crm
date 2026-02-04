package com.mylinehub.crm.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CustomerFranchiseInventoryDTO {

    private Long id;

    // customer
    private Long customerId;
    private String customerFirstname;
    private String customerLastname;
    private String customerPhoneNumber;
    private String customerEmail;
    private String customerCity;
    private String customerOrganization;

    // franchise
    private String interest;
    private Boolean available;

    // audit
    private Instant createdOn;
    private Instant lastUpdatedOn;
}
