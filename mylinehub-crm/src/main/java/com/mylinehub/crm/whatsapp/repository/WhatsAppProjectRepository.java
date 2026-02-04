package com.mylinehub.crm.whatsapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mylinehub.crm.whatsapp.entity.WhatsAppProject;

@Repository
public interface WhatsAppProjectRepository extends JpaRepository<WhatsAppProject, Long> {
   
	List<WhatsAppProject> getAllByOrganization(String organization);
	
}