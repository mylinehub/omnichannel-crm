package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.EmployeeToCampaign;
import com.mylinehub.crm.entity.dto.EmployeeToCampaignDTO;

@Mapper(componentModel = "spring")
public interface EmployeeToCampaignMapper {
    
    @Mapping(target = "campaignid", source = "campaign.id")
    @Mapping(target = "campaignName", source = "campaign.name")
    @Mapping(target = "employeeid", source = "employee.id")
    @Mapping(target = "firstName", source = "employee.firstName")
    @Mapping(target = "email", source = "employee.email")
    @Mapping(target = "phonenumber", source = "employee.phonenumber")
    @Mapping(target = "lastConnectedCustomerPhone", source = "lastConnectedCustomerPhone")
    @Mapping(target = "lastCustomerNumber", source = "lastCustomerNumber")
    EmployeeToCampaignDTO mapEmployeeToCampaignToDto(EmployeeToCampaign employeeToCampaign);
    
    @Mapping(target = "campaign.id", source = "campaignid")
    @Mapping(target = "campaign.name", source = "campaignName")
    @Mapping(target = "employee.id", source = "employeeid")
    @Mapping(target = "employee.firstName", source = "firstName")
    @Mapping(target = "employee.email", source = "email")
    @Mapping(target = "employee.phonenumber", source = "phonenumber")
    @Mapping(target = "lastConnectedCustomerPhone", source = "lastConnectedCustomerPhone")
    @Mapping(target = "lastCustomerNumber", source = "lastCustomerNumber")
    EmployeeToCampaign mapDTOToEmployeeToCampaign(EmployeeToCampaignDTO employeeToCampaignDTO);
    
    
}
