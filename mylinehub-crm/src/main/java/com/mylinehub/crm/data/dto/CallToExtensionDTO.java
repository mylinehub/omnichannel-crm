package com.mylinehub.crm.data.dto;

import java.util.Date;

import com.mylinehub.crm.entity.dto.CustomerDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CallToExtensionDTO {
	
	private String autodialertype;
	private Long campginId;
	private String campginName;
	private CustomerDTO customer;
	private boolean remindercalling;
	private Date currentDate;
	
}
