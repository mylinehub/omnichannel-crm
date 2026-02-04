package com.mylinehub.crm.whatsapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.control.DeepClone;

import com.mylinehub.crm.whatsapp.dto.WhatsAppFlattenMessageDTO;
import com.mylinehub.crm.whatsapp.entity.WhatsAppFlattenMessage;

@Mapper(componentModel = "spring",mappingControl = DeepClone.class)
public interface WhatsAppFlattenMessageMapper {
	WhatsAppFlattenMessageDTO mapWhatsAppFlattenMessageToDTO(WhatsAppFlattenMessage whatsAppFlattenMessage);
	WhatsAppFlattenMessage mapDTOToWhatsAppFlattenMessage(WhatsAppFlattenMessageDTO whatsAppFlattenMessageDTO);
	WhatsAppFlattenMessageDTO cloneWhatsAppFlattenMessage(WhatsAppFlattenMessageDTO whatsAppFlattenMessageDTO);
}