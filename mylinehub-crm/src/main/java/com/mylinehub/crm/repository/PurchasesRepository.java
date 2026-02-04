package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Purchases;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchasesRepository extends JpaRepository<Purchases, Long> {
	
	List<Purchases> findAllByOrganization(String organization);
	
	List<Purchases> findAllByCustomerAndOrganization(Customers customer,String organization);
	
	List<Purchases> findAllByPurchaseDateGreaterThanEqualAndOrganization(Date purchaseDate, String organization);
	
	List<Purchases> findAllByPurchaseDateLessThanEqualAndOrganization(Date purchaseDate, String organization);
	
	Purchases getPurchaseByIdAndOrganization(Long Id,String organization);
	
}
