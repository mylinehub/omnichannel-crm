package com.mylinehub.crm.entity.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchasesPositionsDTO {

     Long id;
     Double amount;
     Long productId;
     String productName;
     Long purchaseId;
     Long customerId;
     String sellingPrice;
     String purchaseDate;
     Character reclamationExist;
     String organization;

}