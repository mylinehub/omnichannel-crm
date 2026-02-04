package com.mylinehub.crm.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateFileCategoryDTO {
	
	
	private Long id;
    private String domain;
    private String extension;
	private String oldName;
	private String name;
    private String businessType;
    private String organization;

}
