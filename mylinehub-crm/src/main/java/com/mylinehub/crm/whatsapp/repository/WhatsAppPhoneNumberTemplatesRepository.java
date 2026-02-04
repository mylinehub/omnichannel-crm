package com.mylinehub.crm.whatsapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;

@Repository
public interface WhatsAppPhoneNumberTemplatesRepository extends JpaRepository<WhatsAppPhoneNumberTemplates, Long> {
   
	List<WhatsAppPhoneNumberTemplates> getAllByOrganization(String organization);
	
	List<WhatsAppPhoneNumberTemplates> getAllByOrganizationAndWhatsAppPhoneNumber(String organization,WhatsAppPhoneNumber whatsAppPhoneNumber);
	List<WhatsAppPhoneNumberTemplates> getAllByWhatsAppPhoneNumber(WhatsAppPhoneNumber whatsAppPhoneNumber);
	
}
