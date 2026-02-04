package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.ProductType;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, String> {
	
	 List<ProductType> findAllByOrganization(String organization);
	 
	 ProductType getProductTypeByIdAndOrganization(Long id,String organization);
	 
}
