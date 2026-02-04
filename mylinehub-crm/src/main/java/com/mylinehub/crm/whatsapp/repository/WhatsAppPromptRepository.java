package com.mylinehub.crm.whatsapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPrompt;


@Repository
public interface WhatsAppPromptRepository extends JpaRepository<WhatsAppPrompt, Long> {
   
	List<WhatsAppPrompt> getAllByActive(boolean active);
	List<WhatsAppPrompt> getAllByOrganization(String organization);
	List<WhatsAppPrompt> getAllByOrganizationAndWhatsAppPhoneNumber(String organization,WhatsAppPhoneNumber whatsAppPhoneNumber);
	List<WhatsAppPrompt> getAllByOrganizationAndWhatsAppPhoneNumberAndActive(String organization,WhatsAppPhoneNumber whatsAppPhoneNumber,boolean active);
	List<WhatsAppPrompt> getAllByOrganizationAndCategory(String organization,String category);
	List<WhatsAppPrompt> getAllByOrganizationAndCategoryAndActive(String organization,String category,boolean active);
	List<WhatsAppPrompt> getAllByOrganizationAndActive(String organization, boolean active);
	List<WhatsAppPrompt> getAllByWhatsAppPhoneNumberAndActive(WhatsAppPhoneNumber whatsAppPhoneNumber, boolean active);

}