package com.mylinehub.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.AmiConnection;


@Repository
public interface AmiConnectionRepository extends JpaRepository<AmiConnection, Long> {
   
    List<AmiConnection> findAllByOrganization(String organization);
    
    AmiConnection getAmiConnectionByAmiuserAndOrganization(String amiuser,String organization);
    AmiConnection getAmiConnectionByIdAndOrganization(Long id,String organization);
    
    @Transactional
    @Modifying
    @Query("UPDATE AmiConnection a " +
            "SET a.isactive = TRUE WHERE a.amiuser = ?1 AND a.organization = ?2")
    int enableAmiConnectionByOrganization(String amiuser,String organization);
    
    @Transactional
    @Modifying
    @Query("UPDATE AmiConnection a " +
            "SET a.isactive = FALSE WHERE a.amiuser = ?1 AND a.organization = ?2")
    int disableAmiConnectionByOrganization(String amiuser,String organization);
    
    
    List<AmiConnection> findAllByIsactiveAndOrganization(boolean isactive,String organization);
    List<AmiConnection> findAllByIsactive(boolean isactive);
    
    List<AmiConnection> findAllByPhonecontextAndOrganization(String phonecontext, String organization);
}