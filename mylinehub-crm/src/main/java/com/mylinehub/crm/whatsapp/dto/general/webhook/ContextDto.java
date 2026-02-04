package com.mylinehub.crm.whatsapp.dto.general.webhook;

import com.mylinehub.crm.whatsapp.dto.general.contact.ReferredProductDto;
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
public class ContextDto {

	String forwarded;
	String frequently_forwarded;
	String from;
	String id;
	ReferredProductDto referred_product ;

}
