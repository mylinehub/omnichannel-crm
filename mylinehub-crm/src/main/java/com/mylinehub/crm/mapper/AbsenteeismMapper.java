package com.mylinehub.crm.mapper;

import com.mylinehub.crm.entity.Absenteeism;
import com.mylinehub.crm.entity.dto.AbsenteeismDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AbsenteeismMapper {

	@Mapping(target = "id", source = "id")
    @Mapping(target = "employeeID", source = "employee.id")
    @Mapping(target = "firstName", source = "employee.firstName")
    @Mapping(target = "lastName", source = "employee.lastName")
    @Mapping(target = "departmentName", source = "employee.department.departmentName")
    @Mapping(target = "absenteeismName", source = "absenteeismName")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "reasonForAbsense", source = "reasonForAbsense")
    @Mapping(target = "dateFrom", source = "dateFrom")
    @Mapping(target = "dateTo", source = "dateTo")
    AbsenteeismDTO mapAbsenteeismToDto(Absenteeism absenteeism);
    
    
    @Mapping(target = "employee.id", source = "employeeID")
    @Mapping(target = "employee.firstName", source = "firstName")
    @Mapping(target = "employee.lastName", source = "lastName")
    @Mapping(target = "employee.department.departmentName", source = "departmentName")
    @Mapping(target = "absenteeismName", source = "absenteeismName")
    @Mapping(target = "reasonForAbsense", source = "reasonForAbsense")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "dateFrom", source = "dateFrom")
    @Mapping(target = "dateTo", source = "dateTo")
    Absenteeism mapDTOToAbsenteeism(AbsenteeismDTO absenteeism);

}
