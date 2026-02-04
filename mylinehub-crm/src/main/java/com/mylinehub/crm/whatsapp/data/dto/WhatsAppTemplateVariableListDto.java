package com.mylinehub.crm.whatsapp.data.dto;

import java.util.List;


import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplateVariable;

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
public class WhatsAppTemplateVariableListDto {
	List<WhatsAppPhoneNumberTemplateVariable> toUpdate;
}
