package com.mylinehub.crm.mapper;

import com.mylinehub.crm.entity.SellingInvoice;
import com.mylinehub.crm.entity.dto.SellingInvoiceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SellingInvoiceMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerFirstName", source = "customer.firstname")
    @Mapping(target = "customerLastName", source = "customer.lastname")
    @Mapping(target = "organization", source = "organization")
    SellingInvoiceDTO mapSellingInvoiceToDTO(SellingInvoice sellingInvoice);
}
