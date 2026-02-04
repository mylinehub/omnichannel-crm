package com.mylinehub.crm.whatsapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplateVariable;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;

@Repository
public interface WhatsAppNumberTemplateVariableRepository extends JpaRepository<WhatsAppPhoneNumberTemplateVariable, Long> {

	@Transactional
    @Modifying
    @Query("DELETE FROM WhatsAppPhoneNumberTemplateVariable a " +
            "WHERE a.whatsAppPhoneNumberTemplates = ?1 AND a.organization = ?2")
    int deleteAllVariableByTemplate(WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplates,String organization);
    
	
	@Query("select  e from WhatsAppPhoneNumberTemplateVariable e where e.organization = ?1 and e.whatsAppPhoneNumberTemplates = ?2 order by e.orderNumber asc")
	List<WhatsAppPhoneNumberTemplateVariable> findAllByWhatsAppNumberAndOrganization(String organization,WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplates);
	
	@Query("select  e from WhatsAppPhoneNumberTemplateVariable e where e.whatsAppPhoneNumberTemplates = ?1 order by e.orderNumber asc")
	List<WhatsAppPhoneNumberTemplateVariable> findAllByWhatsAppNumberTemplate(WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplates);
	
}
