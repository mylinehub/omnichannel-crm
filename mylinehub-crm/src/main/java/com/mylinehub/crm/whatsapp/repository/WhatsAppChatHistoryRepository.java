package com.mylinehub.crm.whatsapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.whatsapp.data.dto.UnreadCountDTO;
import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface WhatsAppChatHistoryRepository extends JpaRepository<WhatsAppChatHistory, Long> {
    
//	@Transactional
//    @Modifying
//    @Query("select distinct ch.phoneNumberWith,ch.lastUpdateTime from WhatsAppChatHistory ch " +
//            "WHERE ch.phoneNumberMain = ?1 AND ch.organization = ?2 AND ch.deleteSelf = ?3 ORDER BY ch.lastUpdateTime DESC")
//	List<String> findByPhoneNumberMainAndOrganizationAndDeleteSelf(String phoneNumberMain,String organization, boolean deleteSelf);
//	    

//	@Query("SELECT DISTINCT ch.phoneNumberWith FROM WhatsAppChatHistory ch " +
//		       "WHERE ch.phoneNumberMain = ?1 AND ch.organization = ?2 AND ch.deleteSelf = ?3 " +
//		       "ORDER BY ch.lastUpdateTime DESC")
//	List<String> findDistinctPhoneNumberWithByPhoneNumberMainAndOrganizationAndDeleteSelfOrderByLastUpdateTimeDesc(
//		       String phoneNumberMain, String organization, boolean deleteSelf);
//	
    
//	@Query(value = "SELECT DISTINCT ON (ch.phone_number_with) ch.phone_number_with " +
//            "FROM whats_app_chat_history ch " +
//            "WHERE ch.phone_number_main = ?1 " +
//            "AND ch.organization = ?2 " +
//            "AND ch.delete_self = ?3 " +
//            "ORDER BY ch.phone_number_with, ch.last_update_time DESC",
//    nativeQuery = true)
//	List<String> findDistinctPhoneNumberWithByPhoneNumberMainAndOrganizationAndDeleteSelfOrderByLastUpdateTimeDesc(String phoneNumberMain, String organization, boolean deleteSelf);

	@Query(
		    value = "SELECT sub.phone_number_with, sub.last_update_time " +
		            "FROM ( " +
		            "    SELECT DISTINCT ON (ch.phone_number_with) " +
		            "        ch.phone_number_with, ch.last_update_time " +
		            "    FROM whats_app_chat_history ch " +
		            "    WHERE ch.phone_number_main = :phoneNumberMain " +
		            "      AND ch.organization = :organization " +
		            "      AND ch.delete_self = :deleteSelf " +
		            "      AND ch.last_update_time BETWEEN :startTime AND :endTime " +
		            "    ORDER BY ch.phone_number_with, ch.last_update_time DESC " +
		            ") sub " +
		            "ORDER BY sub.last_update_time DESC",
		    nativeQuery = true
	)
	List<Object[]> findDistinctPhoneNumberWithByPhoneNumberMainAndOrganizationAndDeleteSelfAndDateRange(
		        @Param("phoneNumberMain") String phoneNumberMain,
		        @Param("organization") String organization,
		        @Param("deleteSelf") boolean deleteSelf,
		        @Param("startTime") Timestamp startTime,
		        @Param("endTime") Timestamp endTime
		);

	
	List<WhatsAppChatHistory> findByPhoneNumberMainAndPhoneNumberWithAndOrganizationAndDeleteSelfOrderByLastUpdateTimeAsc(String phoneNumberMain, String phoneNumberWith, String organization, boolean deleteSelf);
    
    
    @Transactional
    @Modifying
    @Query("UPDATE WhatsAppChatHistory ch " +
            "SET ch.readSelf = TRUE WHERE ch.phoneNumberMain = ?1 AND ch.phoneNumberWith = ?2  AND ch.organization = ?3 AND ch.deleteSelf = ?4")
    int updateLastReadSelfByPhoneNumberMainAndPhoneNumberWithAndOrganizationAndDeleteSelf(String phoneNumberMain, String phoneNumberWith,String organization, boolean deleteSelf);
    
    
    @Transactional
    @Modifying
    @Query("SELECT COUNT(ch) " +
            "FROM WhatsAppChatHistory ch WHERE ch.phoneNumberMain = ?1 AND ch.phoneNumberWith = ?2  AND ch.organization = ?3 AND ch.deleteSelf = ?4AND ch.readSelf = false")
    long getUnReadIndexByPhoneNumberMainAndPhoneNumberWithAndOrganizationAndDeleteSelf(String phoneNumberMain, String phoneNumberWith,String organization, boolean deleteSelf);
    
    
    @Query("SELECT new com.mylinehub.crm.whatsapp.data.dto.UnreadCountDTO(" +
    	       "ch.phoneNumberWith, COUNT(ch)) " +
    	       "FROM WhatsAppChatHistory ch " +
    	       "WHERE ch.phoneNumberMain = :phoneNumberMain " +
    	       "AND ch.organization = :organization " +
    	       "AND ch.deleteSelf = false " +
    	       "AND ch.readSelf = false " +
    	       "GROUP BY ch.phoneNumberWith")
    	List<UnreadCountDTO> getUnreadCountsByPhoneNumberWith(
    	    @Param("phoneNumberMain") String phoneNumberMain,
    	    @Param("organization") String organization
    	);

    
    
    @Transactional
    @Modifying
    @Query("UPDATE WhatsAppChatHistory ch " +
            "SET ch.deleteSelf = true WHERE ch.phoneNumberMain = ?1 AND ch.organization = ?2 AND ch.deleteSelf = ?3")
    int softDeleteAllByPhoneNumberMainAndOrganizationAndDeleteSelf(String phoneNumberMain, String organization, boolean deleteSelf);
    
    
    @Transactional
    @Modifying
    @Query("UPDATE WhatsAppChatHistory ch " +
            "SET ch.deleteSelf = true WHERE ch.phoneNumberMain = ?1 AND ch.phoneNumberWith = ?2 AND ch.organization = ?3 AND ch.deleteSelf = ?4")
    int softDeleteByPhoneNumberMainAndPhoneNumberWithAndOrganizationAndDeleteSelf(String phoneNumberMain, String phoneNumberWith, String organization, boolean deleteSelf);
    
    
    @Transactional
    @Modifying
    @Query("DELETE FROM WhatsAppChatHistory ch " +
            "WHERE ch.phoneNumberMain = ?1 AND ch.organization = ?2 AND ch.deleteSelf = ?3")
    int hardDeleteAllByPhoneNumberMainAndOrganizationAndIsDeleteSelf(String phoneNumberMain, String organization, boolean deleteSelf);
    
    
    @Transactional
    @Modifying
    @Query("DELETE FROM WhatsAppChatHistory ch " +
            "WHERE ch.phoneNumberMain = ?1 AND ch.phoneNumberWith = ?2  AND ch.organization = ?3 AND ch.deleteSelf = ?4")
    int hardDeleteAllByPhoneNumberMainAndPhoneNumberWithAndOrganizationAndDeleteSelf(String phoneNumberMain,String phoneNumberWith, String organization, boolean deleteSelf);
    
//    @Transactional
//    @Modifying
//    @Query(value = "UPDATE WhatsAppChatHistory ch SET ch.lastUpdateTime = ?6 , ch.chats = ch.chats || ?5::jsonb "
//    		+ "WHERE ch.phoneNumberMain = ?1 AND ch.phoneNumberWith = ?2 AND ch.organization = ?3 AND ch.deleteSelf = ?4", nativeQuery = true)
//    int appendChatByPhoneNumberMainAndPhoneNumberWithAndOrganizationAndDeleteSelf(String phoneNumberMain, String phoneNumberWith, String organization, boolean deleteSelf, List<WhatsAppChatKeyValueDTO> chat, Date lastUpdateTime);
   
    @Query("select ch from WhatsAppChatHistory ch where ch.whatsAppMessageId in (?1) ORDER BY ch.lastUpdateTime asc")
    List<WhatsAppChatHistory> findAllByWhatsAppMessageIdIn(List<String> messageIds);
    
    
    @Query("select ch from WhatsAppChatHistory ch where ch.whatsAppMessageId = ?1")
    WhatsAppChatHistory findOneByWhatsAppMessageId(String messageId);
    
    
    @Query("select ch from WhatsAppChatHistory ch where ch.conversationId in (?1) ORDER BY ch.lastUpdateTime asc")
    List<WhatsAppChatHistory> findAllByconversationIdIn(List<String> conversationIds);
    
    @Query(
            value =
                "select * " +
                "from whats_app_chat_history ch " +
                "where ch.organization = :organization " +
                "  and ch.phone_number_main = :phoneNumberMain " +
                "  and ch.delete_self = false " +
                "  and ch.last_update_time between :startTime and :endTime " +
                "order by ch.last_update_time asc",
            nativeQuery = true
        )
    List<WhatsAppChatHistory> findAllForExportByPhoneMainOrgAndDateRange(
                @Param("phoneNumberMain") String phoneNumberMain,
                @Param("organization") String organization,
                @Param("startTime") Timestamp startTime,
                @Param("endTime") Timestamp endTime
    );
}

