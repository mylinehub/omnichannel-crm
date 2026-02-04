package com.mylinehub.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mylinehub.crm.entity.FileCategory;
import com.mylinehub.crm.entity.Media;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;

@Repository
public interface MediaRepository  extends JpaRepository<Media, Long> {
	List<Media> findAllByOrganization(String organization);
//	List<Media> findAllByWhatsAppPhoneNumberAndOrganization(WhatsAppPhoneNumber whatsAppPhoneNumber,String organization);
	List<Media> findAllByFileCategoryAndOrganization(FileCategory fileCategory,String organization);
	List<Media> findAllByFileCategoryAndWhatsAppPhoneNumberAndOrganization(FileCategory fileCategory,WhatsAppPhoneNumber whatsAppPhoneNumber,String organization);
	@Query(value = "select e from Media e where e.extension = ?1 and e.name in (?2) and e.organization = ?3")
	List<Media> findAllByExtensionAndFileNamesInAndOrganization(String extension,List<String> fileNames,String organization);
	
	@Query(value = "select e from Media e where e.fileCategory in (?1) and e.organization = ?2")
	List<Media> findAllByFileCategoryInAndOrganization(List<FileCategory> fileCategories,String organization);

	Media findByNameAndOrganization(String name,String organization);
	List<Media> findByNameInAndOrganization(List<String> names, String organization);
//	Media findByWhatsAppPhoneNumberAndNameAndOrganization(WhatsAppPhoneNumber whatsAppPhoneNumber,String name,String organization);
	Media findByWhatsAppMediaId(String whatsAppMediaId);
	Media findByExtensionAndNameAndOrganization(String extension,String name,String organization);
	
	@Query("SELECT COALESCE(SUM(e.size), 0) FROM Media e WHERE e.extension = ?1")
	Long getTotalImageSizeForExtension(String extension);
}
