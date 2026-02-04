package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.Logs;
import com.mylinehub.crm.entity.dto.LogsDTO;


@Mapper(componentModel = "spring")
public interface LogMapper {
	
	@Mapping(target = "log", source = "log")
	@Mapping(target = "logClass", source = "logClass")
	@Mapping(target = "functionality", source = "functionality")
	@Mapping(target = "createdDate", source = "createdDate")
	@Mapping(target = "id", source = "id")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "data", source = "data")
	LogsDTO mapLogToDTO(Logs log);
	
	@Mapping(target = "log", source = "log")
	@Mapping(target = "logClass", source = "logClass")
	@Mapping(target = "functionality", source = "functionality")
	@Mapping(target = "createdDate", source = "createdDate")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "data", source = "data")
	Logs mapDTOToLog(LogsDTO logDTO);
	
}