package com.mylinehub.crm.entity.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {

     Long supplierId;
     String supplierName;
     String typeOfTransport;
     String activityStatus;
     String organization;
     String weightunit;
     String lengthunit;
     String suppliertype;
     String priceunits;
     String transportcapacity;
     String modeOfTransport;
     String supplierPhoneNumber;

}
