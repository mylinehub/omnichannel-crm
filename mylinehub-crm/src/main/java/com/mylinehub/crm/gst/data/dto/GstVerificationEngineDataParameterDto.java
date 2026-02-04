package com.mylinehub.crm.gst.data.dto;

import com.mylinehub.crm.gst.entity.GstVerificationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GstVerificationEngineDataParameterDto {

	GstVerificationEngine details;
	String action;
	String engineName;
	
}
