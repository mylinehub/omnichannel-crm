package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.entity.dto.BotOutputDTO;

@Mapper(componentModel = "spring")
public interface BotMapper {

    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "messagetype", source = "messagetype")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "format", source = "format")
    @Mapping(target = "domain", source = "domain")
    BotOutputDTO mapBotInputToOutputDto(BotInputDTO botMessage);

}

