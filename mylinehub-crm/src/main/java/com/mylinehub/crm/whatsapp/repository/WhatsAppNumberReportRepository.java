package com.mylinehub.crm.whatsapp.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mylinehub.crm.whatsapp.dto.WhatsAppMessageCountForNumberDTO;
import com.mylinehub.crm.whatsapp.entity.WhatsAppNumberReport;

@Repository
public interface WhatsAppNumberReportRepository extends JpaRepository<WhatsAppNumberReport, Long> {

	List<WhatsAppNumberReport> findAllByOrganizationOrderByDayUpdatedAsc(String organization);
	List<WhatsAppNumberReport> findAllByDayUpdatedGreaterThanEqualAndPhoneNumberMainAndOrganization(Date dayUpdated,String phoneNumberMain,String organization);
	List<WhatsAppNumberReport> findAllByDayUpdatedGreaterThanEqualAndPhoneNumberMainAndTypeOfReportAndOrganization(Date dayUpdated,String phoneNumberMain,String typeOfReport,String organization);
	List<WhatsAppNumberReport> findAllByPhoneNumberMainAndOrganization(String phoneNumberMain,String organization);
	
	WhatsAppNumberReport findAllByPhoneNumberMainAndPhoneNumberWithAndDayUpdated(String phoneNumberMain,String phoneNumberWith,Date dayUpdated);
	
//	 @Query("select  e from WhatsAppNumberReport e where e.dayUpdated >= ?1 and e.organization = ?2and e.whatsAppPhoneNumber = ?3 order by e.dayUpdated desc")
//	 Slice<WhatsAppNumberReport> getAllByDateGreaterThanEqualAndOrganizationAndWhatsAppPhoneNumber(Date dayUpdated,String organization,WhatsAppPhoneNumber whatsAppPhoneNumber,Pageable pageable);
	
	@Query("select  e from WhatsAppNumberReport e where e.dayUpdated >= ?1 and  e.dayUpdated < ?2 and e.organization = ?3 order by e.dayUpdated desc")
	List<WhatsAppNumberReport> findAllByDayUpdatedGreaterThanEqualAndDayUpdatedLessThanAndOrganization(Date startDate,Date enddate,String organization);
	@Query("select  e from WhatsAppNumberReport e where e.dayUpdated >= ?1 and  e.dayUpdated < ?2 and e.organization = ?3 and e.phoneNumberMain in (?4) order by e.dayUpdated desc")
	List<WhatsAppNumberReport> findAllByDayUpdatedGreaterThanEqualAndDayUpdatedLessThanAndOrganizationAndPhoneNumberIn(Date startDate,Date enddate,String organization,List<String> phoneNumbers);
	
	
//	@Query("select  e from WhatsAppNumberReport e where e.dayUpdated >= ?1 and  e.dayUpdated <= ?2 and e.typeOfReport = ?3 and e.organization = ?4 order by e.dayUpdated desc")
//	List<WhatsAppNumberReport> findAllByDayUpdatedGreaterThanEqualAndDayUpdatedLessThanEqualAndTypeOfReportAndOrganization(Date startDate,Date enddate,String typeOfReport,String organization);
//	@Query("select  e from WhatsAppNumberReport e where e.dayUpdated >= ?1 and  e.dayUpdated <= ?2 and e.typeOfReport = ?3 and e.organization = ?4 and e.phoneNumberMain in (?5) order by e.dayUpdated desc")
//	List<WhatsAppNumberReport> findAllByDayUpdatedGreaterThanEqualAndDayUpdatedLessThanEqualAndTypeOfReportAndOrganizationAndPhoneNumberIn(Date startDate,Date enddate,String typeOfReport,String organization,List<String> phoneNumbers);
	
	
	@Query("select  new com.mylinehub.crm.whatsapp.dto.WhatsAppMessageCountForNumberDTO(e.phoneNumberMain,(SUM(e.manualMessageSend)+SUM(e.campaignMessageSend)+SUM(e.aiMessagesSend)) as totalMessagesSend,SUM(e.totalMessagesReceived) as totalMessagesReceived,COUNT( DISTINCT e.phoneNumberWith) as totalPhoneNumberWith) from WhatsAppNumberReport e where e.dayUpdated >= ?1 and  e.dayUpdated < ?2 and e.organization = ?3 GROUP BY e.phoneNumberMain")
	List<WhatsAppMessageCountForNumberDTO> findGroupByNumberAllByDayUpdatedGreaterThanEqualAndDayUpdatedLessThanAndOrganization(Date startDate,Date enddate,String organization);
	@Query("select  new com.mylinehub.crm.whatsapp.dto.WhatsAppMessageCountForNumberDTO(e.phoneNumberMain,(SUM(e.manualMessageSend)+SUM(e.campaignMessageSend)+SUM(e.aiMessagesSend)) as totalMessagesSend,SUM(e.totalMessagesReceived) as totalMessagesReceived,COUNT( DISTINCT e.phoneNumberWith) as totalPhoneNumberWith) from WhatsAppNumberReport e where e.dayUpdated >= ?1 and  e.dayUpdated < ?2 and e.organization = ?3 and e.phoneNumberMain in (?4) GROUP BY e.phoneNumberMain")
	List<WhatsAppMessageCountForNumberDTO> findGroupByNumberAllByDayUpdatedGreaterThanEqualAndDayUpdatedLessThanAndOrganizationAndPhoneNumberIn(Date startDate,Date enddate,String organization,List<String> phoneNumbers);
	
	
	@Query("select  new com.mylinehub.crm.whatsapp.dto.WhatsAppMessageCountForNumberDTO(e.phoneNumberMain,(SUM(e.manualMessageSend)+SUM(e.campaignMessageSend)+SUM(e.aiMessagesSend)) as totalMessagesSend,SUM(e.totalMessagesReceived) as totalMessagesReceived,COUNT( DISTINCT e.phoneNumberWith) as totalPhoneNumberWith, MONTH(e.dayUpdated), YEAR(e.dayUpdated)) from WhatsAppNumberReport e where e.dayUpdated >= ?1 and  e.dayUpdated < ?2 and e.organization = ?3 GROUP BY e.phoneNumberMain, YEAR(e.dayUpdated), MONTH(e.dayUpdated) order by YEAR(e.dayUpdated), MONTH(e.dayUpdated) desc")
	List<WhatsAppMessageCountForNumberDTO> findGroupByNumberAndTimeAllByDayUpdatedGreaterThanEqualAndDayUpdatedLessThanAndOrganization(Date startDate,Date enddate,String organization);
	@Query("select  new com.mylinehub.crm.whatsapp.dto.WhatsAppMessageCountForNumberDTO(e.phoneNumberMain,(SUM(e.manualMessageSend)+SUM(e.campaignMessageSend)+SUM(e.aiMessagesSend)) as totalMessagesSend,SUM(e.totalMessagesReceived) as totalMessagesReceived,COUNT( DISTINCT e.phoneNumberWith) as totalPhoneNumberWith, MONTH(e.dayUpdated), YEAR(e.dayUpdated)) from WhatsAppNumberReport e where e.dayUpdated >= ?1 and  e.dayUpdated < ?2 and e.organization = ?3 and e.phoneNumberMain in (?4) GROUP BY e.phoneNumberMain, YEAR(e.dayUpdated), MONTH(e.dayUpdated) order by YEAR(e.dayUpdated), MONTH(e.dayUpdated) desc")
	List<WhatsAppMessageCountForNumberDTO> findGroupByNumberAndTimeAllByDayUpdatedGreaterThanEqualAndDayUpdatedLessThanAndOrganizationAndPhoneNumberIn(Date startDate,Date enddate,String organization,List<String> phoneNumbers);
	
}