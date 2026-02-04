package com.mylinehub.crm.whatsapp.dto.general.webhook;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mylinehub.crm.whatsapp.dto.general.contact.UrlDto;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusesDto {

	String id;
	String recipient_id;
	String status;
	String timestamp ;
	String type;
	ConversationDto conversation;
	PricingDto pricing;
	PaymentDto payment;
	List<ErrorDto> errors;
}
