package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.RunningSchedule;
import com.mylinehub.crm.entity.dto.RunningScheduleDTO;


@Mapper(componentModel = "spring")
public interface RunningScheduleMapper {
	
	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "jobId", source = "jobId")
	@Mapping(target = "scheduleType", source = "scheduleType")
    @Mapping(target = "functionality", source = "functionality")
    @Mapping(target = "cronExpression", source = "cronExpression")
    @Mapping(target = "date", source = "date")
	@Mapping(target = "campaignId", source = "campaignId")
    @Mapping(target = "actionType", source = "actionType")
	@Mapping(target = "data", source = "data")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "seconds", source = "seconds")
	@Mapping(target = "phoneNumber", source = "phoneNumber")
	@Mapping(target = "callType", source = "callType")
	@Mapping(target = "fromExtension", source = "fromExtension")
	@Mapping(target = "context", source = "context")
	@Mapping(target = "priority", source = "priority")
	@Mapping(target = "timeOut", source = "timeOut")
	@Mapping(target = "firstName", source = "firstName")
	@Mapping(target = "protocol", source = "protocol")
	@Mapping(target = "phoneTrunk", source = "phoneTrunk")
	@Mapping(target = "createdOn", source = "createdOn")
    @Mapping(target = "lastUpdatedOn", source = "lastUpdatedOn")
	RunningScheduleDTO mapRunningScheduleToDto(RunningSchedule runningSchedule);

	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "jobId", source = "jobId")
	@Mapping(target = "scheduleType", source = "scheduleType")
    @Mapping(target = "functionality", source = "functionality")
    @Mapping(target = "cronExpression", source = "cronExpression")
    @Mapping(target = "date", source = "date")
	@Mapping(target = "campaignId", source = "campaignId")
    @Mapping(target = "actionType", source = "actionType")
	@Mapping(target = "data", source = "data")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "seconds", source = "seconds")
	@Mapping(target = "phoneNumber", source = "phoneNumber")
	@Mapping(target = "callType", source = "callType")
	@Mapping(target = "fromExtension", source = "fromExtension")
	@Mapping(target = "context", source = "context")
	@Mapping(target = "priority", source = "priority")
	@Mapping(target = "timeOut", source = "timeOut")
	@Mapping(target = "firstName", source = "firstName")
	@Mapping(target = "protocol", source = "protocol")
	@Mapping(target = "phoneTrunk", source = "phoneTrunk")
	@Mapping(target = "createdOn", source = "createdOn")
    @Mapping(target = "lastUpdatedOn", source = "lastUpdatedOn")
	RunningSchedule mapDtoToRunningSchedule(RunningScheduleDTO runningScheduleDTO);

}
