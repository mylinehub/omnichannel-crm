package com.mylinehub.crm.entity.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SellingInvoiceDTO {

     Long id;
     String invoiceDate;
     Long customerId;
     String customerFirstName;
     String customerLastName;
     Double netWorth;
     Double grossValue;
     Double taxRate;
     String currency;
     String organization;

}
