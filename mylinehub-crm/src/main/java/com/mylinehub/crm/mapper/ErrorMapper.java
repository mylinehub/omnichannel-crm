package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.Errors;
import com.mylinehub.crm.entity.dto.ErrorDTO;

@Mapper(componentModel = "spring")
public interface ErrorMapper {
	
	
	@Mapping(target = "error", source = "error")
	@Mapping(target = "errorClass", source = "errorClass")
	@Mapping(target = "functionality", source = "functionality")
	@Mapping(target = "createdDate", source = "createdDate")
	@Mapping(target = "id", source = "id")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "data", source = "data")
	ErrorDTO mapErrorToDTO(Errors error);
	
	@Mapping(target = "error", source = "error")
	@Mapping(target = "errorClass", source = "errorClass")
	@Mapping(target = "functionality", source = "functionality")
	@Mapping(target = "createdDate", source = "createdDate")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "data", source = "data")
	Errors mapDTOToError(ErrorDTO errorDTO);
	
}