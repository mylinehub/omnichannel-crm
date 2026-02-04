package com.mylinehub.crm.mapper;

import com.mylinehub.crm.entity.PurchasesPositions;
import com.mylinehub.crm.entity.dto.PurchasesPositionsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchasesPositionsMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "purchaseId", source = "purchases.id")
    @Mapping(target = "customerId", source = "purchases.customer.id")
    @Mapping(target = "purchaseDate", source = "purchases.purchaseDate")
    @Mapping(target = "sellingPrice", source = "product.sellingPrice")
    @Mapping(target = "organization", source = "organization")
    PurchasesPositionsDTO mapPurchasesPositionsToDTO(PurchasesPositions purchasesPositions);
}