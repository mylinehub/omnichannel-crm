package com.mylinehub.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import com.mylinehub.crm.entity.FileCategory;

@Repository
public interface FileCategoryRepository extends JpaRepository<FileCategory, Long>  {
	
	List<FileCategory> findByExtensionAndOrganizationAndRoot(String extension, String organization,boolean value);
	List<FileCategory> findByExtensionAndOrganizationAndNameContainingIgnoreCase(String extension, String organization,String name);
	List<FileCategory> findByExtensionAndOrganizationAndRootAndNameContainingIgnoreCase(String extension, String organization,boolean value,String name);
	FileCategory findByExtensionAndNameAndOrganization(String extension,String name, String organization);
	FileCategory findByWhatsAppPhoneIDAndNameAndOrganization(Long whatsAppPhoneID,String name, String organization);
	
	List<FileCategory> findByWhatsAppPhoneIDAndOrganizationAndNameContainingIgnoreCase(Long whatsAppPhoneID, String organization,String name);
	
	@Query("SELECT COALESCE(SUM(e.iconImageSize), 0) FROM FileCategory e WHERE e.organization = ?1 AND e.iconImageData IS NOT NULL")
    Long getTotalImageSizeForOrganization(String organization);
}
