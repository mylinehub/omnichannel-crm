package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.ProductUnits;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUnitsRepository extends JpaRepository<ProductUnits, Long> {
	
	List<ProductUnits> findAllByOrganization(String organization);
}
