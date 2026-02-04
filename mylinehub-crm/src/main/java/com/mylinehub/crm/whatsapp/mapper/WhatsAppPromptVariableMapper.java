package com.mylinehub.crm.whatsapp.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.mylinehub.crm.whatsapp.dto.WhatsAppPromptVariablesDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPrompt;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPromptVariables;
import com.mylinehub.crm.whatsapp.repository.WhatsAppPromptRepository;

@Mapper(componentModel = "spring")
public abstract class WhatsAppPromptVariableMapper {

	
	@Mapping(target = "whatsAppPromptId", source = "whatsAppPromptVariables.whatsAppPrompt.id")
	public abstract WhatsAppPromptVariablesDto mapWhatsAppPromptVariableToDTO(WhatsAppPromptVariables whatsAppPromptVariables);
	
	@Mapping(target = "whatsAppPrompt", source = "whatsAppPromptVariablesDto.whatsAppPromptId", qualifiedByName = "mapWhatsAppPrompt")
	public abstract WhatsAppPromptVariables mapDTOToWhatsAppPromptVariable(WhatsAppPromptVariablesDto whatsAppPromptVariablesDto, @Context WhatsAppPrompt whatsAppPrompt);
	
	@Named("mapWhatsAppPrompt") 
    WhatsAppPrompt mapWhatsAppPrompt(Long whatsAppPromptId, @Context WhatsAppPrompt whatsAppPrompt) throws Exception{
		
		if(whatsAppPrompt == null)
		{
			throw new Exception("Cannot find prompt associated to prompt variable");
		}
		
		return whatsAppPrompt;
	}
}
