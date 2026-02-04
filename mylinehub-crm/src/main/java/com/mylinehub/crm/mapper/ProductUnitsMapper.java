package com.mylinehub.crm.mapper;

import com.mylinehub.crm.entity.ProductUnits;
import com.mylinehub.crm.entity.dto.ProductUnitsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductUnitsMapper {

    @Mapping(target = "unitOfMeasure", source = "unitOfMeasure.")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "organization", source = "organization")
    ProductUnitsDTO mapProductUnitsToDTO(ProductUnits productUnits);
}
