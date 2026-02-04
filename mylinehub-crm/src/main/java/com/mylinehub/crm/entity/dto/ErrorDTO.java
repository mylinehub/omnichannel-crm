package com.mylinehub.crm.entity.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorDTO {

    private Long id;
    String error;
    String data;
	String errorClass;
	String functionality;
	Date createdDate;
	String organization;
}