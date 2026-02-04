package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.Supplier;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
	
	List<Supplier> findAllByOrganization(String organization);
	
	Supplier getSupplierBySupplierNameAndOrganization(String supplierName,String organization);

	Supplier getSupplierBySupplierIdAndOrganization(Long supplierId,String organization);

    List<Supplier> findAllByTransportcapacityAndOrganization(String transportcapacity,String organization);   
    
    List<Supplier> findAllBySuppliertypeAndOrganization(String suppliertype,String organization);  
    
}
