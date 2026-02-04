package com.mylinehub.crm.whatsapp.dto.general.contact;

import java.util.List;

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
public class ContactDto {

	List<AddressDto> addresses;
	String birthday;
	List<EmailDto> emails;
	NameDto name;
	OrgDto org;
	List<PhoneDto> phones;
	List<UrlDto> urls;
}
