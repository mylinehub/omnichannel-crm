package com.mylinehub.crm.whatsapp.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.mylinehub.crm.whatsapp.dto.WhatsAppPromptDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPrompt;


@Mapper(componentModel = "spring")
public abstract class WhatsAppPromptMapper {
	
	
	@Mapping(target = "whatsAppPhoneNumberId", source = "whatsAppPrompt.whatsAppPhoneNumber.id")
	public abstract WhatsAppPromptDto mapWhatsAppPromptToDTO(WhatsAppPrompt whatsAppPrompt);
	
	@Mapping(target = "whatsAppPhoneNumber", source = "whatsAppPromptDto.whatsAppPhoneNumberId", qualifiedByName = "mapWhatsAppPhone")
	public abstract WhatsAppPrompt mapDTOToWhatsAppPrompt(WhatsAppPromptDto whatsAppPromptDto, @Context WhatsAppPhoneNumber whatsAppPhoneNumber);
	
	@Named("mapWhatsAppPhone") 
    WhatsAppPhoneNumber mapWhatsAppPhone(Long whatsAppPhoneNumberId, @Context WhatsAppPhoneNumber whatsAppPhoneNumber) throws Exception{

		if(whatsAppPhoneNumber == null)
		{
			throw new Exception("Cannot find phone number associated to template");
		}
		
		return whatsAppPhoneNumber;
	}
	
}