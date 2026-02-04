package com.mylinehub.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.Conference;

@Repository
public interface ConferenceRepository extends JpaRepository<Conference, Long> {
   
    List<Conference> findAllByOrganization(String organization);
    
    Conference getConferenceByConfextensionAndOrganization(String confextension,String organization);
    
    
    @Transactional
    @Modifying
    @Query("UPDATE Conference a " +
            "SET a.isconferenceactive = TRUE WHERE a.confextension = ?1 AND a.organization = ?2")
    int enableConferenceByOrganization(String confextension,String organization);
    
    @Transactional
    @Modifying
    @Query("UPDATE Conference a " +
            "SET a.isconferenceactive = FALSE WHERE a.confextension = ?1 AND a.organization = ?2")
    int disableConferenceByOrganization(String confextension,String organization);
    
    
    List<Conference> findAllByIsconferenceactiveAndOrganization(boolean isconferenceactive,String organization);
    List<Conference> findAllByPhonecontextAndOrganization(String phonecontext, String organization);
    
    Conference getConferenceByConfextension(String confextension);
    
}