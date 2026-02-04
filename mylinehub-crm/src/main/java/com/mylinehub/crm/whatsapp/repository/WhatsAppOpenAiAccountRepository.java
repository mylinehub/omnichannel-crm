package com.mylinehub.crm.whatsapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mylinehub.crm.whatsapp.entity.WhatsAppOpenAiAccount;


@Repository
public interface WhatsAppOpenAiAccountRepository extends JpaRepository<WhatsAppOpenAiAccount, Long> {
   
	List<WhatsAppOpenAiAccount> getAllByOrganization(String organization);
}