package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.SellingInvoice;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellingInvoiceRepository extends JpaRepository<SellingInvoice, Long> {
	
	List<SellingInvoice> findAllByOrganization(String organization);
	
}
