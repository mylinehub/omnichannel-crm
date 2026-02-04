package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.Queue;
import com.mylinehub.crm.entity.dto.QueueDTO;

@Mapper(componentModel = "spring")
public interface QueueMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "phoneContext", source = "phoneContext")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "extension", source = "extension")
	@Mapping(target = "protocol", source = "protocol")
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "type", source = "type")
	@Mapping(target = "isactive", source = "isactive")
    QueueDTO mapQueueToDTO(Queue queue);
	
	
	@Mapping(target = "phoneContext", source = "phoneContext")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "extension", source = "extension")
	@Mapping(target = "protocol", source = "protocol")
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "type", source = "type")
	@Mapping(target = "isactive", source = "isactive")
    Queue mapDTOToQueue(QueueDTO queue);
}
