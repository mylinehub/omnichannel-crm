package com.mylinehub.crm.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IvrDTO {
	
	private Long id;
    public String phoneContext;
    public String organization;
    public String extension;
    public  String name;
    public String protocol;
    public String domain;
    public boolean isactive;
}
