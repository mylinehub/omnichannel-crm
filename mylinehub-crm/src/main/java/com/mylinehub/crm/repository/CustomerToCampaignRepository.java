package com.mylinehub.crm.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.CustomerToCampaign;
import com.mylinehub.crm.entity.Customers;

@Repository
public interface CustomerToCampaignRepository extends JpaRepository<CustomerToCampaign, Long> {
   
	
	@Transactional
    @Modifying
    @Query("UPDATE CustomerToCampaign a " +
            "SET a.isCalledOnce = 'false' WHERE a.campaign = ?1 AND a.organization = ?2")
    int resetCampaignCustomers(Campaign campaign,String organization);
	
	@Query("select  e from CustomerToCampaign e where e.campaign = ?1 and e.organization = ?2 and(e.customer.firstname LIKE %?3% or e.customer.email LIKE %?3% or e.customer.lastname LIKE %?3% or e.customer.phoneNumber LIKE %?3% or e.customer.business LIKE %?3% or e.customer.description LIKE %?3% or e.customer.country LIKE %?3% or e.customer.city LIKE %?3% or e.customer.zipCode LIKE %?3%) order by e.id desc")
    Page<CustomerToCampaign> findAllByCampaignAndOrganization(Campaign campaign,String organization,String searchText, Pageable pageable);
    
	@Query("select  e from CustomerToCampaign e where e.campaign = ?1 and e.organization = ?2 and(e.customer.firstname LIKE %?3% or e.customer.email LIKE %?3% or e.customer.lastname LIKE %?3% or e.customer.phoneNumber LIKE %?3% or e.customer.business LIKE %?3% or e.customer.description LIKE %?3% or e.customer.country LIKE %?3% or e.customer.city LIKE %?3% or e.customer.zipCode LIKE %?3%) order by e.id desc")
	Slice<CustomerToCampaign> getAllByCampaignAndOrganization(Campaign campaign,String organization,String searchText, Pageable pageable);
	
    
	List<CustomerToCampaign> findAllByCustomerAndOrganization(Customers customer,String organization);

	@Query(value = "select  e from CustomerToCampaign e where e.campaign = ?1 AND e.customer in (?2)")
	List<CustomerToCampaign> findAllByCampaignAndCustomerIn(Campaign campaign,List<Customers> customers);
	
}
