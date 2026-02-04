package com.mylinehub.crm.whatsapp.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.mylinehub.crm.whatsapp.dto.WhatsAppPhoneNumberTemplateVariableDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplateVariable;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;
import com.mylinehub.crm.whatsapp.repository.WhatsAppPhoneNumberTemplatesRepository;

@Mapper(componentModel = "spring")
public abstract class WhatsAppNumberTemplateVariableMapper {

//	@Autowired
//    protected WhatsAppPhoneNumberTemplatesRepository whatsAppPhoneNumberTemplateRepository;
//	
	
	@Mapping(target = "whatsAppPhoneNumberTemplateId", source = "whatsAppPhoneNumberTemplateVariable.whatsAppPhoneNumberTemplates.id")
	public abstract WhatsAppPhoneNumberTemplateVariableDto mapWhatsAppPhoneNumberTemplateVariableToDTO(WhatsAppPhoneNumberTemplateVariable whatsAppPhoneNumberTemplateVariable);

	@Mapping(target = "whatsAppPhoneNumberTemplates", source = "whatsAppPhoneNumberTemplateVariableDto.whatsAppPhoneNumberTemplateId", qualifiedByName = "mapWhatsAppPhoneTemplate")
	public abstract WhatsAppPhoneNumberTemplateVariable mapDTOToWhatsAppPhoneNumberTemplateVariable(WhatsAppPhoneNumberTemplateVariableDto whatsAppPhoneNumberTemplateVariableDto, @Context WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplates);

	@Named("mapWhatsAppPhoneTemplate") 
	WhatsAppPhoneNumberTemplates mapWhatsAppPhoneTemplate(Long whatsAppPhoneNumberTemplateId, @Context WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplates) throws Exception{;
		
		if(whatsAppPhoneNumberTemplates == null)
		{
			throw new Exception("Cannot find template associated with variables");
		}
		
		return whatsAppPhoneNumberTemplates;
	}
}
