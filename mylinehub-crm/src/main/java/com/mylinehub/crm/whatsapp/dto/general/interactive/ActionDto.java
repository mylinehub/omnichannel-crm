package com.mylinehub.crm.whatsapp.dto.general.interactive;

import java.util.List;

import com.mylinehub.crm.whatsapp.dto.general.contact.UrlDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ButtonDto;

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
public class ActionDto {

	ButtonDto button;
	List<ButtonDto> buttons;
	List<SectionDto> sections;
	String catalog_id;
	String product_retailer_id;
}
