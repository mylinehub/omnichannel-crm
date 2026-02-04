package com.mylinehub.crm.mapper;

import com.mylinehub.crm.entity.Purchases;
import com.mylinehub.crm.entity.dto.PurchasesDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchasesMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "soldById", source = "soldBy.id")
    @Mapping(target = "customerFirstName", source = "customer.firstname")
    @Mapping(target = "customerLastName", source = "customer.lastname")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "purchaseName", source = "purchaseName")
    @Mapping(target = "quantity", source = "quantity")
    PurchasesDTO mapPurchasesToDTO(Purchases purchases);
    
    @Mapping(target = "customer.id", source = "customerId")
    @Mapping(target = "soldBy.id", source = "soldById")
    @Mapping(target = "customer.firstname", source = "customerFirstName")
    @Mapping(target = "customer.lastname", source = "customerLastName")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "purchaseName", source = "purchaseName")
    @Mapping(target = "quantity", source = "quantity")
    Purchases mapDTOToPurchases(PurchasesDTO purchases);
    
    
}
