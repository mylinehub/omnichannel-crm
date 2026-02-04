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
public class LogsDTO {

    private Long id;
    String log;
    String data;
	String logClass;
	String functionality;
	Date createdDate;
	String organization;
}