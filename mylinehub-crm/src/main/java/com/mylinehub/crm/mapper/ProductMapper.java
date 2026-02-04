package com.mylinehub.crm.mapper;

import com.mylinehub.crm.entity.Product;
import com.mylinehub.crm.entity.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

	    
	    
    @Mapping(target = "productType", source = "productType")
    @Mapping(target = "imageName", source = "imageName")
	@Mapping(target = "imageType", source = "imageType")
	@Mapping(target = "imageData", source = "imageData")
    @Mapping(target = "productStringType", source = "productStringType")
	@Mapping(target = "units", source = "units")
	@Mapping(target = "sellingPrice", source = "sellingPrice")
    @Mapping(target = "purchasePrice", source = "purchasePrice")
	@Mapping(target = "taxRate", source = "taxRate")
    ProductDTO mapProductToDto(Product product);
    
    @Mapping(target = "productType", source = "productType")
    @Mapping(target = "imageName", source = "imageName")
	@Mapping(target = "imageType", source = "imageType")
	@Mapping(target = "imageData", source = "imageData")
    @Mapping(target = "productStringType", source = "productStringType")
	@Mapping(target = "units", source = "units")
	@Mapping(target = "sellingPrice", source = "sellingPrice")
    @Mapping(target = "purchasePrice", source = "purchasePrice")
	@Mapping(target = "taxRate", source = "taxRate")
    Product mapDTOToProduct(ProductDTO product);
}
