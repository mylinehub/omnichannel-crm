package com.mylinehub.crm.mapper;

import com.mylinehub.crm.entity.Supplier;
import com.mylinehub.crm.entity.dto.SupplierDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    @Mapping(target = "typeOfTransport", source = "modeOfTransportCode.fullName")
    SupplierDTO mapSupplierToDTO(Supplier supplier);
    
    @Mapping(target = "modeOfTransportCode.fullName", source = "typeOfTransport")
    Supplier mapDTOToSupplier(SupplierDTO supplier);
}
