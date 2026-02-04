package com.mylinehub.crm.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileCategoryDTO {
	
	private Long id;
    private String domain;
    private String organization;
    private String extension;
    private Long whatsAppPhoneID;
    private String name;
    private String businessType;
    private String iconImageType;
    private String iconImageData;
    private byte[] iconImageByteData;

}
