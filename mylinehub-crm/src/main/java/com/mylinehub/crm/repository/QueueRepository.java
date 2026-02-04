package com.mylinehub.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.Queue;

@Repository
public interface QueueRepository extends JpaRepository<Queue, Long> {
   
	@Transactional
    @Modifying
    @Query("UPDATE Queue a " +
            "SET a.isactive = TRUE WHERE a.extension = ?1 AND a.organization = ?2")
    int enableQueueByOrganization(String extension,String organization);
    
    @Transactional
    @Modifying
    @Query("UPDATE Queue a " +
            "SET a.isactive = FALSE WHERE a.extension = ?1 AND a.organization = ?2")
    int disableQueueByOrganization(String extension,String organization);
    
    
    List<Queue> findAllByOrganization(String organization);
    
    Queue getQueueByExtensionAndOrganization(String extension,String organization);
    List<Queue> findAllByIsactiveAndOrganization(boolean isactive,String organization);
    List<Queue> findAllByPhoneContextAndOrganization(String phoneContext, String organization);
    Queue getQueueByNameAndOrganization(String name,String organization);
    
    Queue getQueueByExtension(String extension);
    
}