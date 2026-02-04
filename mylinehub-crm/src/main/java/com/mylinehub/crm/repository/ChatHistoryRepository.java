package com.mylinehub.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.ChatHistory;
import com.mylinehub.crm.entity.dto.ChatKeyValueDTO;

import java.util.Date;
import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    
	@Transactional
    @Modifying
    @Query("select distinct ch.extensionWith,ch.lastUpdateTime from ChatHistory ch " +
            "WHERE ch.extensionMain = ?1 AND ch.organization = ?2 AND ch.isDeleted = ?3 ORDER BY ch.lastUpdateTime DESC")
	List<String> findByExtensionMainAndOrganizationAndIsDeleted(String extensionMain,String organization, boolean isDeleted);
	    
	
	@Transactional
    @Modifying
    @Query("select ch.lastReadIndex from ChatHistory ch " +
            "WHERE ch.extensionMain = ?1 AND ch.extensionWith = ?2 AND ch.organization = ?3 AND ch.isDeleted = ?4")
	int findLastReadIndexByExtensionMainAndExtensionWithAndOrganizationAndIsDeleted(String extensionMain,String extensionWith,String organization, boolean isDeleted);
	    
	
	@Query("SELECT ch.extensionWith, MAX(ch.lastReadIndex) " +
		       "FROM ChatHistory ch " +
		       "WHERE ch.extensionMain = :extensionMain " +
		       "AND ch.organization = :organization " +
		       "AND ch.isDeleted = false " +
		       "GROUP BY ch.extensionWith")
	List<Object[]> findLastReadIndexGroupedByExtensionWith(
		    @Param("extensionMain") String extensionMain,
		    @Param("organization") String organization
		);
	
		
    List<ChatHistory> findByExtensionMainAndExtensionWithAndOrganizationAndIsDeleted(String extensionMain, String extensionWith, String organization, boolean isDeleted);
    
    List<ChatHistory> getAllByExtensionMainAndOrganizationAndIsDeleted(String extensionMain, String organization, boolean isDeleted);
    
    
    @Transactional
    @Modifying
    @Query("UPDATE ChatHistory ch " +
            "SET ch.lastReadIndex = ?5 WHERE ch.extensionMain = ?1 AND ch.extensionWith = ?2  AND ch.organization = ?3 AND ch.isDeleted = ?4")
    int updateLastReadIndexByExtensionMainAndExtensionWithAndOrganizationAndIsDeleted(String extensionMain, String extensionWith,String organization, boolean isDeleted, int lastReadIndex);
    
    
    
    @Transactional
    @Modifying
    @Query("UPDATE ChatHistory ch " +
            "SET ch.isDeleted = true WHERE ch.extensionMain = ?1 AND ch.organization = ?2 AND ch.isDeleted = ?3")
    int softDeleteAllByExtensionMainAndOrganizationAndIsDeleted(String extensionMain, String organization, boolean isDeleted);
    
    
    @Transactional
    @Modifying
    @Query("UPDATE ChatHistory ch " +
            "SET ch.isDeleted = true WHERE ch.extensionMain = ?1 AND ch.extensionWith = ?2 AND ch.organization = ?3 AND ch.isDeleted = ?4")
    int softDeleteByExtensionMainAndExtensionWithAndOrganizationAndIsDeleted(String extensionMain, String extensionWith, String organization, boolean isDeleted);
    
    
    @Transactional
    @Modifying
    @Query("DELETE FROM ChatHistory ch " +
            "WHERE ch.extensionMain = ?1 AND ch.organization = ?2 AND ch.isDeleted = ?3")
    int hardDeleteAllByExtensionMainAndOrganizationAndIsDeleted(String extensionMain, String organization, boolean isDeleted);
    
    
    @Transactional
    @Modifying
    @Query("DELETE FROM ChatHistory ch " +
            "WHERE ch.extensionMain = ?1 AND ch.extensionWith = ?2  AND ch.organization = ?3 AND ch.isDeleted = ?4")
    int hardDeleteAllByExtensionMainAndExtensionWithAndOrganizationAndIsDeleted(String extensionMain,String extensionWith, String organization, boolean isDeleted);
    
    
    @Transactional
    @Modifying
    @Query(value = "UPDATE ChatHistory ch SET ch.lastUpdateTime = ?6 , ch.chats = ch.chats || ?5::jsonb "
    		+ "WHERE ch.extensionMain = ?1 AND ch.extensionWith = ?2 AND ch.organization = ?3 AND ch.isDeleted = ?4", nativeQuery = true)
    int appendChatByExtensionMainAndExtensionWithAndOrganizationAndIsDeleted(String extensionMain, String extensionWith, String organization, boolean isDeleted, List<ChatKeyValueDTO> chat, Date lastUpdateTime);
    
    
}

