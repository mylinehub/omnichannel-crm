package com.mylinehub.crm.whatsapp.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.whatsapp.dto.WhatsAppPhoneNumberDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppProject;


@Mapper(componentModel = "spring")
public abstract class WhatsAppPhoneNumberMapper {

	
	@Mapping(target = "adminEmployeeId", source = "WhatsAppPhoneNumber.admin.id")
	@Mapping(target = "whatsAppProjectId", source = "WhatsAppPhoneNumber.whatsAppProject.id")
	public abstract WhatsAppPhoneNumberDto mapWhatsAppPhoneNumberToDTO(WhatsAppPhoneNumber WhatsAppPhoneNumber);
	
//	@Mapping(ignore = true, target = "aiModel")
//	@Mapping(ignore = true, target = "costPerInboundMessage")
//	@Mapping(ignore = true, target = "costPerOutboundMessage")
	@Mapping(target = "admin", source = "WhatsAppPhoneNumberDto.adminEmployeeId", qualifiedByName = "mapWhatsAppNumberAdmin")
	@Mapping(target = "whatsAppProject", source = "WhatsAppPhoneNumberDto.whatsAppProjectId", qualifiedByName = "mapWhatsAppProject")
	public abstract WhatsAppPhoneNumber mapDTOToWhatsAppPhoneNumber(WhatsAppPhoneNumberDto WhatsAppPhoneNumberDto, @Context WhatsAppProject whatsAppProject, @Context Employee employee);

	@Named("mapWhatsAppProject") 
	WhatsAppProject mapWhatsAppProject(Long whatsAppProjectId, @Context WhatsAppProject whatsAppProject) throws Exception{

		if(whatsAppProject == null)
		{
			throw new Exception("Cannot find facebook project associated with phone number");
		}
		
		return whatsAppProject;
	}
	
	@Named("mapWhatsAppNumberAdmin") 
	Employee mapWhatsAppNumberAdmin(Long adminEmployeeId, @Context Employee employee) throws Exception{

		if(employee == null)
		{
			throw new Exception("Cannot find admin employee associated with phone number");
		}
		
		return employee;
	}
}