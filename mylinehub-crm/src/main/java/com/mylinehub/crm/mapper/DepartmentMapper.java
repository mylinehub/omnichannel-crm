package com.mylinehub.crm.mapper;

import com.mylinehub.crm.entity.Departments;
import com.mylinehub.crm.entity.dto.DepartmentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "departmentId", source = "id")
    @Mapping(target = "departmentName", source = "departmentName")
    @Mapping(target = "managerFirstName", source = "managers.firstName")
    @Mapping(target = "managerLastName", source = "managers.lastName")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "managerId", source = "managers.id")
    DepartmentDTO mapDepartmentToDto(Departments departments);
    
    
    @Mapping(target = "departmentName", source = "departmentName")
    @Mapping(target = "managers.firstName", source = "managerFirstName")
    @Mapping(target = "managers.lastName", source = "managerLastName")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "managers.id", source = "managerId")
    Departments mapDTOToDepartment(DepartmentDTO departmentDTO);

}
