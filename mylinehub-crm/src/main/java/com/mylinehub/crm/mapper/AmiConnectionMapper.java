package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.AmiConnection;
import com.mylinehub.crm.entity.dto.AmiConnectionDTO;

@Mapper(componentModel = "spring")
public interface AmiConnectionMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "port", source = "port")
	@Mapping(target = "amiuser", source = "amiuser")
	@Mapping(target = "password", source = "password")
	@Mapping(target = "phonecontext", source = "phonecontext")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "isactive", source = "isactive")
    AmiConnectionDTO mapAmiConnectionToDTO(AmiConnection amiconnection);
	
	
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "port", source = "port")
	@Mapping(target = "amiuser", source = "amiuser")
	@Mapping(target = "password", source = "password")
	@Mapping(target = "phonecontext", source = "phonecontext")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "isactive", source = "isactive")
    AmiConnection mapDTOToAmiConnection(AmiConnectionDTO amiconnection);
}