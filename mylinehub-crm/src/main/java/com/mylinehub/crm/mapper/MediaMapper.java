package com.mylinehub.crm.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.mylinehub.crm.entity.Media;
import com.mylinehub.crm.entity.dto.MediaDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.repository.WhatsAppPhoneNumberRepository;

@Mapper(componentModel = "spring")
public abstract class MediaMapper {

	
	@Mapping(target = "whatsAppPhoneNumberId", source = "Media.whatsAppPhoneNumber.id")
	public abstract MediaDto mapMediaToDTO(Media Media);

	@Mapping(target = "whatsAppPhoneNumber", source = "MediaDto.whatsAppPhoneNumberId", qualifiedByName = "mapWhatsAppPhone")
	public abstract Media mapDTOToMedia(MediaDto MediaDto, @Context WhatsAppPhoneNumber whatsAppPhoneNumber);
	
	
	@Named("mapWhatsAppPhone") 
    WhatsAppPhoneNumber mapWhatsAppPhone(Long whatsAppPhoneNumberId, @Context WhatsAppPhoneNumber whatsAppPhoneNumber) throws Exception{
		
		if(whatsAppPhoneNumber == null)
		{
//			throw new Exception("Cannot find phone number associated to add media");
		}
		
		return whatsAppPhoneNumber;
	}
}
