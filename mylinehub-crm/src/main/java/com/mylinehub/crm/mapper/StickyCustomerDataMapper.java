package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.StickyCustomerData;
import com.mylinehub.crm.entity.dto.StickyCustomerDataDTO;

@Mapper(componentModel = "spring")
public interface StickyCustomerDataMapper {

	@Mapping(target = "employeeId", source = "employee.id")
	@Mapping(target = "customerId", source = "customer.id")
	StickyCustomerDataDTO mapStickyCustomerDataToDTO(StickyCustomerData stickyCustomerData);
	

	@Mapping(target = "employee.id", source = "employeeId")
	@Mapping(target = "customer.id", source = "customerId")
	StickyCustomerData mapDTOToStickyCustomerData(StickyCustomerDataDTO campaignDTO);
}
