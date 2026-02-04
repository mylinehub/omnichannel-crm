package com.mylinehub.crm.whatsapp.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.mylinehub.crm.entity.Product;
import com.mylinehub.crm.utils.LoggerUtils;
import com.mylinehub.crm.whatsapp.dto.WhatsAppPhoneNumberTemplateDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;

@Mapper(componentModel = "spring")
public abstract class WhatsAppPhoneNumberTemplatesMapper {

	
	@Mapping(target = "productId", source = "whatsAppPhoneNumberTemplates.product.id")
	@Mapping(target = "whatsAppPhoneNumberId", source = "whatsAppPhoneNumberTemplates.whatsAppPhoneNumber.id")
	public abstract WhatsAppPhoneNumberTemplateDto mapWhatsAppPhoneNumberTemplatesToDTO(WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplates);

	@Mapping(target = "product", source = "whatsAppPhoneNumberTemplatesDto.productId", qualifiedByName = "mapProduct")
	@Mapping(target = "whatsAppPhoneNumber", source = "whatsAppPhoneNumberTemplatesDto.whatsAppPhoneNumberId", qualifiedByName = "mapWhatsAppPhone")
	public abstract WhatsAppPhoneNumberTemplates mapDTOToWhatsAppPhoneNumberTemplates(WhatsAppPhoneNumberTemplateDto whatsAppPhoneNumberTemplatesDto, @Context WhatsAppPhoneNumber whatsAppPhoneNumber, @Context Product product);

	@Named("mapWhatsAppPhone") 
    WhatsAppPhoneNumber mapWhatsAppPhone(Long whatsAppPhoneNumberId, @Context WhatsAppPhoneNumber whatsAppPhoneNumber) throws Exception{

		if(whatsAppPhoneNumber == null)
		{
			throw new Exception("Cannot find phone number associated to template");
		}
		
		return whatsAppPhoneNumber;
	}
	
	
	@Named("mapProduct") 
    Product mapProduct(Long productId, @Context Product product) throws Exception{
		LoggerUtils.log.debug("Map product to template.");
		if(product==null) {
			LoggerUtils.log.debug("Product is null");
		}
		return product;
	}
}