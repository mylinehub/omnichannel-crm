package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.dto.CampaignDTO;

@Mapper(componentModel = "spring")
public interface CampaignMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "description", source = "description")
	@Mapping(target = "aiApplicationName", source = "aiApplicationName")
	@Mapping(target = "aiApplicationDomain", source = "aiApplicationDomain")
	@Mapping(target = "isactive", source = "isactive")
	@Mapping(target = "isenabled", source = "isenabled")
	@Mapping(target = "startdate", source = "startdate")
	@Mapping(target = "enddate", source = "enddate")
	@Mapping(target = "phonecontext", source = "phonecontext")
	@Mapping(target = "isonmobile", source = "isonmobile")
	//@Mapping(expression = "java(null == MyObjectDetailMapper.getLastOne(myObject) ? null : MyObjectDetailMapper.getLastOne(myObject).getNumber())", target = "number") 
	@Mapping(target = "managerId", source = "manager.id")
	@Mapping(target = "country", source = "country")
	@Mapping(target = "autodialertype", source = "autodialertype")
	@Mapping(target = "business", source = "business")
	@Mapping(target = "remindercalling", source = "remindercalling")
	@Mapping(target = "cronremindercalling", source = "cronremindercalling")
	@Mapping(target = "lastCustomerNumber", source = "lastCustomerNumber")
	@Mapping(target = "breathingSeconds", source = "breathingSeconds")
	@Mapping(target = "ivrExtension", source = "ivrExtension")
	@Mapping(target = "confExtension", source = "confExtension")
	@Mapping(target = "queueExtension", source = "queueExtension")
	@Mapping(target = "totalCallsMade", source = "totalCallsMade")
	@Mapping(target = "callLimit", source = "callLimit")
	@Mapping(target = "parallelLines", source = "parallelLines")
	@Mapping(target = "whatsAppNumber", source = "whatsAppNumber")
	@Mapping(target = "template", source = "template")
	@Mapping(target = "callCost", source = "callCost")
	@Mapping(target = "callCostMode", source = "callCostMode")
    CampaignDTO mapCampaignToDTO(Campaign campaign);
	
	
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "description", source = "description")
	@Mapping(target = "aiApplicationName", source = "aiApplicationName")
	@Mapping(target = "aiApplicationDomain", source = "aiApplicationDomain")
	@Mapping(target = "isactive", source = "isactive")
	@Mapping(target = "isenabled", source = "isenabled")
	@Mapping(target = "startdate", source = "startdate")
	@Mapping(target = "enddate", source = "enddate")
	@Mapping(target = "phonecontext", source = "phonecontext")
	@Mapping(target = "isonmobile", source = "isonmobile")
	//@Mapping(expression = "java(null == MyObjectDetailMapper.getLastOne(myObject) ? null : MyObjectDetailMapper.getLastOne(myObject).getNumber())", target = "number") 
	@Mapping(target = "manager.id", source = "managerId")
	@Mapping(target = "country", source = "country")
	@Mapping(target = "autodialertype", source = "autodialertype")
	@Mapping(target = "business", source = "business")
	@Mapping(target = "remindercalling", source = "remindercalling")
	@Mapping(target = "cronremindercalling", source = "cronremindercalling")
	@Mapping(target = "lastCustomerNumber", source = "lastCustomerNumber")
	@Mapping(target = "breathingSeconds", source = "breathingSeconds")
	@Mapping(target = "ivrExtension", source = "ivrExtension")
	@Mapping(target = "confExtension", source = "confExtension")
	@Mapping(target = "queueExtension", source = "queueExtension")
	@Mapping(target = "totalCallsMade", source = "totalCallsMade")
	@Mapping(target = "callLimit", source = "callLimit")
	@Mapping(target = "parallelLines", source = "parallelLines")
	@Mapping(target = "whatsAppNumber", source = "whatsAppNumber")
	@Mapping(target = "template", source = "template")
	@Mapping(target = "callCost", source = "callCost")
	@Mapping(target = "callCostMode", source = "callCostMode")
    Campaign mapDTOToCampaign(CampaignDTO campaignDTO);
}

