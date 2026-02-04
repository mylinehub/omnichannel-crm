package com.mylinehub.crm.whatsapp.dto.general.interactive;

import com.mylinehub.crm.whatsapp.dto.general.MediaDto;
import com.mylinehub.crm.whatsapp.dto.general.TextDto;
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
public class HeaderDto {

	String type;
	MediaDto media;
	TextDto text;

}
