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
public class AddressDto {

	String street;
	String city;
	String state;
	String zip;
	String country;
	String country_code;
	String type;
}
