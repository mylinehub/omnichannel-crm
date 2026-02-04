package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.SshConnection;
import com.mylinehub.crm.entity.dto.SshConnectionDTO;



@Mapper(componentModel = "spring")
public interface SshMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "phonecontext", source = "phonecontext")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "sshHostType", source = "sshHostType")
	@Mapping(target = "type", source = "type")
	@Mapping(target = "sshUser", source = "sshUser")
	@Mapping(target = "password", source = "password")
	@Mapping(target = "authType", source = "authType")
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "port", source = "port")
	@Mapping(target = "pemFileName", source = "pemFileName")
	@Mapping(target = "pemFileLocation", source = "pemFileLocation")
	@Mapping(target = "connectionString", source = "connectionString")
	@Mapping(target = "active", source = "active")
	@Mapping(target = "privateKey", source = "privateKey")
	@Mapping(target = "publicKey", source = "publicKey")
	@Mapping(target = "extraKey", source = "extraKey")
    SshConnectionDTO mapSshConnectionToDTO(SshConnection sshConnection);
	
	
	@Mapping(target = "phonecontext", source = "phonecontext")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "sshHostType", source = "sshHostType")
	@Mapping(target = "type", source = "type")
	@Mapping(target = "sshUser", source = "sshUser")
	@Mapping(target = "password", source = "password")
	@Mapping(target = "authType", source = "authType")
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "port", source = "port")
	@Mapping(target = "pemFileName", source = "pemFileName")
	@Mapping(target = "pemFileLocation", source = "pemFileLocation")
	@Mapping(target = "connectionString", source = "connectionString")
	@Mapping(target = "active", source = "active")
	@Mapping(target = "privateKey", source = "privateKey")
	@Mapping(target = "publicKey", source = "publicKey")
	@Mapping(target = "extraKey", source = "extraKey")
    SshConnection mapDtoToSshConnection(SshConnectionDTO sshConnectionDTO);
	
}