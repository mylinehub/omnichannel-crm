package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.CustomerToCampaign;
import com.mylinehub.crm.entity.dto.CustomerToCampaignDTO;

@Mapper(componentModel = "spring")
public interface CustomerToCampaignMapper {

    @Mapping(target = "campaignid", source = "campaign.id")
    @Mapping(target = "campaignName", source = "campaign.name")
    @Mapping(target = "customerid", source = "customer.id")
    @Mapping(target = "firstname", source = "customer.firstname")
    @Mapping(target = "email", source = "customer.email")
    @Mapping(target = "phoneNumber", source = "customer.phoneNumber")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "isCalledOnce", source = "isCalledOnce")
    @Mapping(target = "lastConnectedExtension", source = "lastConnectedExtension")
    CustomerToCampaignDTO mapCustomerToCampaignToDto(CustomerToCampaign customerToCampign);
    
    @Mapping(target = "campaign.id", source = "campaignid")
    @Mapping(target = "campaign.name", source = "campaignName")
    @Mapping(target = "customer.id", source = "customerid")
    @Mapping(target = "customer.firstname", source = "firstname")
    @Mapping(target = "customer.email", source = "email")
    @Mapping(target = "customer.phoneNumber", source = "phoneNumber")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "isCalledOnce", source = "isCalledOnce")
    @Mapping(target = "lastConnectedExtension", source = "lastConnectedExtension")
    CustomerToCampaign mapDTOToCustomerToCampaign(CustomerToCampaignDTO customerToCampignDTO);
}
