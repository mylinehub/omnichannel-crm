package com.mylinehub.crm.whatsapp.mapper;

import org.mapstruct.Mapper;

import com.mylinehub.crm.whatsapp.dto.WhatsAppOpenAiAccountDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppOpenAiAccount;


@Mapper(componentModel = "spring")
public interface WhatsAppOpenAiAccountMapper {
	
	WhatsAppOpenAiAccountDto mapWhatsAppOpenAiAccountToDTO(WhatsAppOpenAiAccount whatsAppOpenAiAccount);

	WhatsAppOpenAiAccount mapDTOToWhatsAppOpenAiAccount(WhatsAppOpenAiAccountDto whatsAppOpenAiAccountDto);
}