package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.mylinehub.crm.entity.Organization;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

	@Mapping(target = "id",ignore = true)
	@Mapping(target = "organization",ignore = true)
	@Mapping(target = "businessIdentificationNumber",ignore = true)
	void updateOrgToOrg(Organization source, @MappingTarget Organization target);
}


