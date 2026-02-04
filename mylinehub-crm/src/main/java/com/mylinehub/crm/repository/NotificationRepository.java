package com.mylinehub.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	
	   List<Notification> findByForExtensionAndOrganizationAndIsDeletedOrderByCreationDateDesc(String forExtension, String organization, boolean isDeleted);
    
	
	    @Transactional
	    @Modifying
	    @Query("UPDATE Notification n " +
	            "SET n.isDeleted = true WHERE n.forExtension = ?1 AND n.organization = ?2 AND n.isDeleted = ?3")
	    int softDeleteAllByForExtensionAndOrganizationAndIsDeleted(String forExtension, String organization, boolean isDeleted);
	    
	    
	    @Transactional
	    @Modifying
	    @Query("UPDATE Notification n " +
	            "SET n.isDeleted = true WHERE n.id in (?1) AND n.forExtension = ?2 AND n.organization = ?3 AND n.isDeleted = ?4")
	    int softDeleteByIdsAndForExtensionAndExtensionWithAndOrganizationAndIsDeleted(List<Long> ids, String forExtension, String organization, boolean isDeleted);
	    
	    
	    @Transactional
	    @Modifying
	    @Query("DELETE FROM Notification n " +
	            "WHERE n.forExtension = ?1 AND n.organization = ?2 AND n.isDeleted = ?3")
	    int hardDeleteAllByForExtensionAndOrganizationAndIsDeleted(String forExtension, String organization, boolean isDeleted);
	    
	    
	    @Transactional
	    @Modifying
	    @Query("DELETE FROM Notification n " +
	            "WHERE n.id = ?1 AND n.forExtension = ?2  AND n.organization = ?3 AND n.isDeleted = ?4")
	    int hardDeleteByIdAndForExtensionAndExtensionWithAndOrganizationAndIsDeleted(Long id, String forExtension, String organization, boolean isDeleted);
	    
	    
}
