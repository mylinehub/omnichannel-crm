package com.mylinehub.crm.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProductInterestDTO {

	 Long id;
     String name;
     String productStringType;
     private String imageType;
     byte[] imageByteData;
     
}
