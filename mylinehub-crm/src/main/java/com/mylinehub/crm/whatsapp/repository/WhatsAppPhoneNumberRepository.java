package com.mylinehub.crm.whatsapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.whatsapp.dto.WhatsAppManagementEmployeeDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppProject;


@Repository
public interface WhatsAppPhoneNumberRepository extends JpaRepository<WhatsAppPhoneNumber, Long> {
   
	WhatsAppPhoneNumber findByPhoneNumber(String phoneNumber);
	List<WhatsAppPhoneNumber> getAllByOrganization(String organization);
	List<WhatsAppPhoneNumber> getAllByOrganizationAndWhatsAppProject(String organization,WhatsAppProject whatsAppProject);
	List<WhatsAppPhoneNumber> getAllByOrganizationAndWhatsAppProjectAndActive(String organization,WhatsAppProject whatsAppProject,boolean active);
	List<WhatsAppPhoneNumber> getAllByOrganizationAndAdmin(String organization,Employee admin);
	
	@Query("select  a from WhatsAppPhoneNumber a where a.phoneNumber in (?1) and a.organization = ?2")
	List<WhatsAppPhoneNumber> findAllByPhoneNumbersAndOrganization(List<String> phoneNumbers,String organization);
	    

//    @Query("select a from WhatsAppPhoneNumber a" +
//            " WHERE jsonb_exists_any(a.employeeExtensionAccessList,ARRAY[?1]) or admin = ?2")
//    @Query(value = "SELECT phone_number FROM whats_app_phone_number WHERE jsonb_exists_any(employee_extension_access_list,ARRAY[?1]) or admin_employee_id = ?2", nativeQuery = true)
//	@Query("select  a from WhatsAppPhoneNumber a where a.phoneNumber in (?1) or admin = ?2")
	List<WhatsAppPhoneNumber> findAllByEmployeeExtensionAccessListContainingOrAdmin(String employeeExtension,Employee admin);

	
	@Transactional
    @Modifying
    @Query("UPDATE WhatsAppPhoneNumber a " +
            "SET a.employeeExtensionAccessList = ?1 WHERE a.id = ?2")
    int updateEmployeeAccessListByOrganization(String employeeExtensionAccessList,Long id);

	@Transactional
    @Modifying
    @Query("UPDATE WhatsAppPhoneNumber a " +
            "SET a.admin = ?1 WHERE a.id = ?2")
    int updateAdminEmployeeForWhatsAppNumberByOrganization(Employee admin,Long id);

}