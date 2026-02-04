package com.mylinehub.crm.whatsapp.mapper;

import org.mapstruct.Mapper;

import com.mylinehub.crm.whatsapp.dto.WhatsAppNumberReportDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppNumberReport;


@Mapper(componentModel = "spring")
public abstract class WhatsAppNumberReportMapper {
	
	public abstract WhatsAppNumberReportDto mapWhatsAppNumberReportToDTO(WhatsAppNumberReport whatsAppNumberReport);
	public abstract WhatsAppNumberReport mapDTOToWhatsAppNumberReport(WhatsAppNumberReportDto whatsAppNumberReportDto);
}

