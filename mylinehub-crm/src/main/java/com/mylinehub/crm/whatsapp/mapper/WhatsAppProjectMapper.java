package com.mylinehub.crm.whatsapp.mapper;

import org.mapstruct.Mapper;

import com.mylinehub.crm.whatsapp.dto.WhatsAppProjectDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppProject;

@Mapper(componentModel = "spring")
public interface WhatsAppProjectMapper {
	
	WhatsAppProjectDto mapWhatsAppProjectToDTO(WhatsAppProject whatsAppProject);

	WhatsAppProject mapDTOToWhatsAppProject(WhatsAppProjectDto whatsAppProjectDto);
}