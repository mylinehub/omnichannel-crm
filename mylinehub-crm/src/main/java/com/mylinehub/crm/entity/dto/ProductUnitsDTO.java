package com.mylinehub.crm.entity.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUnitsDTO {

     Long id;
     Long productId;
     String productName;
     String unitOfMeasure;
     String alternativeUnitOfMeasure;
     Double conversionFactor;
    String organization;
}
