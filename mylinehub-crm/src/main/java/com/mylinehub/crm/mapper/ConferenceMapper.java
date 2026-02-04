package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.Conference;
import com.mylinehub.crm.entity.dto.ConferenceDTO;

@Mapper(componentModel = "spring")
public interface ConferenceMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "confextension", source = "confextension")
	@Mapping(target = "confname", source = "confname")
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "phonecontext", source = "phonecontext")
	@Mapping(target = "owner", source = "owner")
	@Mapping(target = "bridge", source = "bridge")
	@Mapping(target = "userprofile", source = "userprofile")
	@Mapping(target = "menu", source = "menu")
	@Mapping(target = "isdynamic", source = "isdynamic")
	@Mapping(target = "isroomactive", source = "isroomactive")
	@Mapping(target = "isconferenceactive", source = "isconferenceactive")
    ConferenceDTO mapConferenceToDTO(Conference conference);
	
	@Mapping(target = "confextension", source = "confextension")
	@Mapping(target = "confname", source = "confname")
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "phonecontext", source = "phonecontext")
	@Mapping(target = "owner", source = "owner")
	@Mapping(target = "bridge", source = "bridge")
	@Mapping(target = "userprofile", source = "userprofile")
	@Mapping(target = "menu", source = "menu")
	@Mapping(target = "isdynamic", source = "isdynamic")
	@Mapping(target = "isroomactive", source = "isroomactive")
	@Mapping(target = "isconferenceactive", source = "isconferenceactive")
    Conference mapDTOToConference(ConferenceDTO conference);
}

