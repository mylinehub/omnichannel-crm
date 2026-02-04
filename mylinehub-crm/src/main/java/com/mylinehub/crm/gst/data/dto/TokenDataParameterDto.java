package com.mylinehub.crm.gst.data.dto;


import com.mylinehub.crm.gst.api.cloud.wrapper.OkHttpSendVueAuthClient;
import com.mylinehub.crm.gst.repository.GstVerificationEngineRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenDataParameterDto {

	OkHttpSendVueAuthClient okHttpSendVueAuthClient;
	GstVerificationEngineRepository gstVerificationEngineRepository;
	String action;
	TokenDataDto token;
	String engine;
	
}
