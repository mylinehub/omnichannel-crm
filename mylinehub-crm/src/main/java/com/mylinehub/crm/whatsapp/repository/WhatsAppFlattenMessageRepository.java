package com.mylinehub.crm.whatsapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.mylinehub.crm.whatsapp.entity.WhatsAppFlattenMessage;

@Repository
public interface WhatsAppFlattenMessageRepository extends JpaRepository<WhatsAppFlattenMessage, Long> {
	
	@Query("select ch from WhatsAppFlattenMessage ch where ch.messageId in (?1) ORDER BY ch.lastUpdatedOn asc")
    List<WhatsAppFlattenMessage> findAllByMessageIdIn(List<String> messageIds);
    
}