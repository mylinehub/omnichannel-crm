package com.mylinehub.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.SipProvider;


@Repository
public interface SipProviderRepository extends JpaRepository<SipProvider, Long> {
	
	@Transactional
    @Modifying
    @Query("UPDATE SipProvider a " +
            "SET a.active = TRUE WHERE a.id = ?1 AND a.organization = ?2")
    int enableSipProviderByOrganization(Long id,String organization);
    
    @Transactional
    @Modifying
    @Query("UPDATE SipProvider a " +
            "SET a.active = FALSE WHERE a.id = ?1 AND a.organization = ?2")
    int disableSipProviderByOrganization(Long id,String organization);
    
	
	List<SipProvider> findAllByOrganization(String organization);
	List<SipProvider> findAllByActiveAndOrganization(boolean active,String organization);
	SipProvider findByPhoneNumberAndActiveAndOrganization(String phoneNumber,boolean active,String organization);
	SipProvider findByPhoneNumberAndOrganization(String phoneNumber,String organization);
	
}