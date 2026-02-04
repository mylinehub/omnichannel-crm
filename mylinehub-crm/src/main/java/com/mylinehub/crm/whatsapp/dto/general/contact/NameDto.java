package com.mylinehub.crm.whatsapp.dto.general.contact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ButtonDto;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NameDto {

	String formatted_name;
	String first_name;
	String last_name;
	String middle_name;
	String suffix;
	String prefix;
}
