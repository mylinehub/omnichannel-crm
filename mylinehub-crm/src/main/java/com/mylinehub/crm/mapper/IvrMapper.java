package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.Ivr;
import com.mylinehub.crm.entity.dto.IvrDTO;


@Mapper(componentModel = "spring")
public interface IvrMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "phoneContext", source = "phoneContext")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "extension", source = "extension")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "protocol", source = "protocol")
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "isactive", source = "isactive")
    IvrDTO mapIvrToDTO(Ivr ivr);
	
	
	@Mapping(target = "phoneContext", source = "phoneContext")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "extension", source = "extension")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "protocol", source = "protocol")
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "isactive", source = "isactive")
    Ivr mapDTOToIvr(IvrDTO ivr);
}
