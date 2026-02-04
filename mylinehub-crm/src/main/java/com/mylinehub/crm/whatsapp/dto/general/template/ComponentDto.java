package com.mylinehub.crm.whatsapp.dto.general.template;

import java.util.List;

import com.mylinehub.crm.whatsapp.dto.general.ButtonParameterDto;
import com.mylinehub.crm.whatsapp.dto.general.ParameterDto;
import com.mylinehub.crm.whatsapp.dto.general.contact.UrlDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComponentDto {

	String type;
    String sub_type;
    String index;
    List<ParameterDto> parameters;
	
	//Button
	List<ButtonParameterDto> buttonParameterDto;
}
