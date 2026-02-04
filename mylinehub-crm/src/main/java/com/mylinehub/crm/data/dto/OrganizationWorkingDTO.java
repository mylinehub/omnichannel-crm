package com.mylinehub.crm.data.dto;

import org.springframework.context.ApplicationContext;

import com.mylinehub.crm.mapper.OrganizationMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationWorkingDTO {

	double currentFileSize;
	boolean isCallOnMobile;
	double deductAIAmount;
	double amount;
	String ariApplication;
	OrganizationMapper organizationMapper;
	ApplicationContext applicationContext;
}
