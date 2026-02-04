package com.mylinehub.crm.entity.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

     Long id;
     String name;
     String productType;
     Double sellingPrice;
     Double purchasePrice;
     Double taxRate;
     String organization;
     String units;
     String productStringType;
     
     private String imageName;
     private String imageType;
     private String imageData;
    
}