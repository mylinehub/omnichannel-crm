package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.SipProvider;
import com.mylinehub.crm.entity.dto.SipProviderDTO;

@Mapper(componentModel = "spring")
public interface SipProviderMapper {
	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "providerName", source = "providerName")
	@Mapping(target = "active", source = "active")
	@Mapping(target = "phoneNumber", source = "phoneNumber")
	@Mapping(target = "company", source = "company")
	@Mapping(target = "costCalculation", source = "costCalculation")
	@Mapping(target = "meteredPlanAmount", source = "meteredPlanAmount")
	SipProviderDTO mapSipProviderToDTO(SipProvider sipProvider);
	
	
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "providerName", source = "providerName")
	@Mapping(target = "active", source = "active")
	@Mapping(target = "phoneNumber", source = "phoneNumber")
	@Mapping(target = "company", source = "company")
	@Mapping(target = "costCalculation", source = "costCalculation")
	@Mapping(target = "meteredPlanAmount", source = "meteredPlanAmount")
	SipProvider mapDTOToSipProvider(SipProviderDTO sipProviderDTO);
	
}