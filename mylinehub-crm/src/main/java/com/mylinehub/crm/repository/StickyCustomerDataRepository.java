package com.mylinehub.crm.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.StickyCustomerData;

@Repository
public interface StickyCustomerDataRepository extends JpaRepository<StickyCustomerData, Long> {
	
	
	StickyCustomerData findFirstByCustomerAndOrganizationOrderByCreatedDateDesc(Customers customer,String organization);
	
}
