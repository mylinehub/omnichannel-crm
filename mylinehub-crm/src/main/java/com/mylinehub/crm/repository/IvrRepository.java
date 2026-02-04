package com.mylinehub.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.Ivr;


@Repository
public interface IvrRepository extends JpaRepository<Ivr, Long> {
   
	
	@Transactional
    @Modifying
    @Query("UPDATE Ivr a " +
            "SET a.isactive = TRUE WHERE a.extension = ?1 AND a.organization = ?2")
    int enableIvrByOrganization(String extension,String organization);
    
    @Transactional
    @Modifying
    @Query("UPDATE Ivr a " +
            "SET a.isactive = FALSE WHERE a.extension = ?1 AND a.organization = ?2")
    int disableIvrByOrganization(String extension,String organization);
    
    
    List<Ivr> findAllByOrganization(String organization);
    List<Ivr> findAllByIsactiveAndOrganization(boolean isactive,String organization);
    List<Ivr> findAllByPhoneContextAndOrganization(String phoneContext, String organization);
    Ivr getIvrByExtensionAndOrganization(String extension,String organization);
    
    Ivr getIvrByExtension(String extension);
    

}