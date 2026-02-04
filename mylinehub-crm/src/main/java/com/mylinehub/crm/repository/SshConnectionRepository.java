package com.mylinehub.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.SshConnection;


@Repository
public interface SshConnectionRepository extends JpaRepository<SshConnection, Long> {
	
	@Transactional
    @Modifying
    @Query("UPDATE SshConnection a " +
            "SET a.active = TRUE WHERE a.id = ?1 AND a.organization = ?2")
    int enableSshConnectionByOrganization(Long id,String organization);
    
    @Transactional
    @Modifying
    @Query("UPDATE SshConnection a " +
            "SET a.active = FALSE WHERE a.id = ?1 AND a.organization = ?2")
    int disableSshConnectionByOrganization(Long id,String organization);
    
    
	List<SshConnection> findAllByOrganization(String organization);
	List<SshConnection> findAllByActive(boolean active);
	List<SshConnection> findAllByActiveAndOrganization(boolean active,String organization);
	SshConnection findByDomainAndOrganization(String domain,String organization);
	
   
}