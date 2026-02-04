package com.mylinehub.crm.entity.dto;

import java.util.Date;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchasesDTO {

     Long id;
     Long customerId;
     Long soldById;
     String customerFirstName;
     String customerLastName;
     Date purchaseDate;
     String organization;
     String purchaseName;
     String quantity;
     boolean receiptExist;
     boolean invoiceExist;
}