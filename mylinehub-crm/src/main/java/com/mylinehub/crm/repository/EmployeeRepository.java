package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.enums.USER_ROLE;
import com.mylinehub.crm.requests.ImageSizeSummaryRequest;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {


	@EntityGraph(attributePaths = {"department"})
    Optional<Employee> findByEmail(String email);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.notificationDot = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateNotificationDotStatusByOrganization(String email,String organization,boolean value);

	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.allowedToSwitchOffWhatsAppAI = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserAllowedToSwitchOffWhatsAppAIByOrganization(String email,String organization,boolean value);
	
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.recordAllCalls = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserRecordAllCallsByOrganization(String email,String organization,boolean value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.doNotDisturb = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserDoNotDisturbrByOrganization(String email,String organization,boolean value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.startVideoFullScreen = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserStartVideoFullScreenByOrganization(String email,String organization,boolean value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.callWaiting = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserCallWaitingByOrganization(String email,String organization,boolean value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.intercomPolicy = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserIntercomPolicyByOrganization(String email,String organization,boolean value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.freeDialOption = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserFreeDialOptionByOrganization(String email,String organization,boolean value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.textDictateOption = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserTextDictationByOrganization(String email,String organization,boolean value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.textMessagingOption = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserTextMessagingByOrganization(String email,String organization,boolean value);
	
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.uiTheme = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserUiThemeByOrganization(String email,String organization,String value);
	
    
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.autoAnswer = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserAutoAnswerByOrganization(String email,String organization,boolean value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.autoConference = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserAutoConferenceByOrganization(String email,String organization,boolean value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.autoVideo = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserAutoVideoByOrganization(String email,String organization,boolean value);
	
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.micDevice = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserMicDeviceByOrganization(String email,String organization,String value);
	
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.speakerDevice = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserSpeakerDeviceByOrganization(String email,String organization,String value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.videoDevice = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserVideoDeviceByOrganization(String email,String organization,String value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.videoOrientation = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserVideoOrientationByOrganization(String email,String organization,String value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.videoQuality = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserVideoQualityByOrganization(String email,String organization,String value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.videoFrameRate = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserVideoFrameRateByOrganization(String email,String organization,String value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.autoGainControl = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserAutoGainControlByOrganization(String email,String organization,String value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.echoCancellation = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserEchoCancellationByOrganization(String email,String organization,String value);
	
	@Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.noiseSupression = ?3 WHERE a.email = ?1 AND a.organization = ?2")
    int updateUserNoiseSupressionByOrganization(String email,String organization,String value);
	
	
    @Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.isEnabled = TRUE WHERE a.email = ?1 AND a.organization = ?2")
    int enableUserByOrganization(String email,String organization);
    
    @Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.isEnabled = FALSE WHERE a.email = ?1 AND a.organization = ?2")
    int disableUserByOrganization(String email,String organization);
    
   
    @Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.useSecondaryAllotedLine = TRUE WHERE a.email = ?1 AND a.organization = ?2")
    int enableUseAllotedSecondLineByOrganization(String email,String organization);
    
    @Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.useSecondaryAllotedLine = FALSE WHERE a.email = ?1 AND a.organization = ?2")
    int disableUseAllotedSecondLineByOrganization(String email,String organization);
    
    
    
    
    @Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.callonnumber = TRUE WHERE a.email = ?1 AND a.organization = ?2")
    int enableUserCallOnMobileByOrganization(String email,String organization);
    
    @Transactional
    @Modifying
    @Query("UPDATE Employee a " +
            "SET a.callonnumber = FALSE WHERE a.email = ?1 AND a.organization = ?2")
    int disableUserCallOnMobileByOrganization(String email,String organization);
    
    @Query("select  new com.mylinehub.crm.entity.Employee(e.id,e.firstName, e.lastName, e.email, e.userRole , e.pesel, e.birthdate , e.department,e.extension, e.phonenumber, e.imageType,e.iconImageData) from Employee e where e.organization = ?1 and e.extension <> ?2")
    List<Employee> findAllBasicByOrganizationWithIconImage(String organization, String extension);
    
    
    //@Query("select  new com.mylinehub.crm.entity.Employee(e.id,e.firstName, e.lastName, e.email, e.userRole , e.pesel, e.sex, e.birthdate , e.salary, e.department,e.phoneContext,e.isLocked , e.isEnabled,    e.organization, e.extension,  e.domain, e.protocol, e.timezone, e.type, e.callonnumber, e.useSecondaryAllotedLine, e.phonenumber, e.provider1, e.allotednumber1, e.provider2, e.allotednumber2,e.costCalculation, e.amount, e.transfer_phone_1, e.transfer_phone_2,  e.imageName, e.imageType, e.governmentDocumentID1, e.governmentDocumentID2,e.totalparkedchannels,e.parkedchannel1,e.parkedchannel2,e.parkedchannel3,e.parkedchannel4,e.parkedchannel5,e.parkedchannel6,e.parkedchannel7,e.parkedchannel8,e.parkedchannel9,e.parkedchannel10,e.autoAnswer,e.autoConference,e.autoVideo,e.micDevice,e.speakerDevice,e.videoDevice,e.videoOrientation,e.videoQuality,e.videoFrameRate,e.autoGainControl,e.echoCancellation,e.noiseSupression) from Employee e where e.organization = ?1")
    //@Query("select  new com.mylinehub.crm.entity.Employee(e.id,e.firstName, e.lastName, e.email, e.userRole , e.pesel, e.sex, e.birthdate , e.salary, e.department,e.phoneContext,e.isLocked , e.isEnabled,    e.organization, e.extension,  e.domain, e.protocol, e.timezone, e.type, e.callonnumber, e.useSecondaryAllotedLine, e.phonenumber, e.provider1, e.allotednumber1, e.provider2, e.allotednumber2,e.costCalculation, e.amount, e.transfer_phone_1, e.transfer_phone_2,  e.imageName, e.imageType, e.governmentDocumentID1, e.governmentDocumentID2,e.totalparkedchannels,e.parkedchannel1,e.parkedchannel2,e.parkedchannel3,e.parkedchannel4,e.parkedchannel5,e.parkedchannel6,e.parkedchannel7,e.parkedchannel8,e.parkedchannel9,e.parkedchannel10) from Employee e where e.organization = ?1")
    //@Query("select  new com.mylinehub.crm.entity.Employee(e.id,e.firstName, e.lastName, e.email, e.userRole , e.pesel, e.sex, e.birthdate , e.salary, e.department,e.phoneContext,e.isLocked , e.isEnabled,    e.organization, e.extension,  e.domain, e.protocol, e.timezone, e.type, e.callonnumber, e.useSecondaryAllotedLine, e.phonenumber, e.provider1, e.allotednumber1, e.provider2, e.allotednumber2,e.costCalculation, e.amount, e.transfer_phone_1, e.transfer_phone_2,  e.imageName, e.imageType, e.governmentDocumentID1, e.governmentDocumentID2,e.totalparkedchannels,e.parkedchannel1,e.parkedchannel2,e.parkedchannel3,e.parkedchannel4,e.parkedchannel5,e.parkedchannel6,e.parkedchannel7,e.parkedchannel8,e.parkedchannel9,e.parkedchannel10,e.recordAllCalls,e.intercomPolicy,e.freeDialOption,e.textDictateOption,e.textMessagingOption) from Employee e where e.organization = ?1") 
    @Query("select  e from Employee e where e.organization = ?1")
    List<Employee> findFullDetailOfAllByOrganization(String organization);
    
    
    @Query("select  new com.mylinehub.crm.entity.Employee(e.id,e.firstName, e.lastName, e.email, e.userRole , e.pesel, e.sex, e.birthdate , e.salary, e.department,e.phoneContext,e.isLocked , e.isEnabled,    e.organization, e.extension,  e.domain, e.protocol, e.timezone, e.type, e.callonnumber, e.useSecondaryAllotedLine, e.phonenumber, e.provider1, e.allotednumber1, e.provider2, e.allotednumber2,e.costCalculation, e.amount, e.transfer_phone_1, e.transfer_phone_2,  e.imageName, e.imageType, e.governmentDocumentID1, e.governmentDocumentID2,e.totalparkedchannels,e.parkedchannel1,e.parkedchannel2,e.parkedchannel3,e.parkedchannel4,e.parkedchannel5,e.parkedchannel6,e.parkedchannel7,e.parkedchannel8,e.parkedchannel9,e.parkedchannel10,e.autoAnswer,e.autoConference,e.autoVideo,e.micDevice,e.speakerDevice,e.videoDevice,e.videoOrientation,e.videoQuality,e.videoFrameRate,e.autoGainControl,e.echoCancellation,e.noiseSupression,e.recordAllCalls,e.intercomPolicy,e.freeDialOption,e.textDictateOption,e.textMessagingOption,e.doNotDisturb,e.startVideoFullScreen,e.confExtension,e.extensionPrefix,e.confExtensionPrefix,e.callWaiting,e.doc1ImageType,e.doc2ImageType,e.phoneTrunk,e.secondDomain,e.lastConnectedCustomerPhone) from Employee e where e.organization = ?1")
    List<Employee> findAllByOrganization(String organization);
    
    
    Employee findByEmailAndOrganization(String email,String organization);

    Employee findByExtension(String extension);
    
    Employee findByExtensionAndOrganization(String extension,String organization);
    
    Employee findByPhonenumberAndOrganization(String phonenumber,String organization);

    Employee findByPhonenumberContaining(String phonenumber);
    
    Employee findByAllotednumber1Containing(String allotednumber1);
    Employee findByAllotednumber1(String allotednumber1);
    
    Employee findByAllotednumber2Containing(String allotednumber2);
    Employee findByAllotednumber2(String allotednumber2);
    
    Employee findByPhonenumber(String phonenumber);
    
    @Query("select  new com.mylinehub.crm.entity.Employee(e.id,e.firstName, e.lastName, e.email, e.userRole , e.pesel, e.sex, e.birthdate , e.salary, e.department,e.phoneContext,e.isLocked , e.isEnabled,    e.organization, e.extension,  e.domain, e.protocol, e.timezone, e.type, e.callonnumber, e.useSecondaryAllotedLine, e.phonenumber, e.provider1, e.allotednumber1, e.provider2, e.allotednumber2,e.costCalculation, e.amount, e.transfer_phone_1, e.transfer_phone_2,  e.imageName, e.imageType, e.governmentDocumentID1, e.governmentDocumentID2,e.totalparkedchannels,e.parkedchannel1,e.parkedchannel2,e.parkedchannel3,e.parkedchannel4,e.parkedchannel5,e.parkedchannel6,e.parkedchannel7,e.parkedchannel8,e.parkedchannel9,e.parkedchannel10,e.autoAnswer,e.autoConference,e.autoVideo,e.micDevice,e.speakerDevice,e.videoDevice,e.videoOrientation,e.videoQuality,e.videoFrameRate,e.autoGainControl,e.echoCancellation,e.noiseSupression,e.recordAllCalls,e.intercomPolicy,e.freeDialOption,e.textDictateOption,e.textMessagingOption,e.doNotDisturb,e.startVideoFullScreen,e.confExtension,e.extensionPrefix,e.confExtensionPrefix,e.callWaiting,e.doc1ImageType,e.doc2ImageType,e.phoneTrunk,e.secondDomain,e.lastConnectedCustomerPhone) from Employee e where e.phoneContext = ?1 and e.organization = ?2")
    List<Employee> findAllByPhoneContextAndOrganization(String phoneContext,String organization);
    
    @Query("select  new com.mylinehub.crm.entity.Employee(e.id,e.firstName, e.lastName, e.email, e.userRole , e.pesel, e.sex, e.birthdate , e.salary, e.department,e.phoneContext,e.isLocked , e.isEnabled,    e.organization, e.extension,  e.domain, e.protocol, e.timezone, e.type, e.callonnumber, e.useSecondaryAllotedLine, e.phonenumber, e.provider1, e.allotednumber1, e.provider2, e.allotednumber2,e.costCalculation, e.amount, e.transfer_phone_1, e.transfer_phone_2,  e.imageName, e.imageType, e.governmentDocumentID1, e.governmentDocumentID2,e.totalparkedchannels,e.parkedchannel1,e.parkedchannel2,e.parkedchannel3,e.parkedchannel4,e.parkedchannel5,e.parkedchannel6,e.parkedchannel7,e.parkedchannel8,e.parkedchannel9,e.parkedchannel10,e.autoAnswer,e.autoConference,e.autoVideo,e.micDevice,e.speakerDevice,e.videoDevice,e.videoOrientation,e.videoQuality,e.videoFrameRate,e.autoGainControl,e.echoCancellation,e.noiseSupression,e.recordAllCalls,e.intercomPolicy,e.freeDialOption,e.textDictateOption,e.textMessagingOption,e.doNotDisturb,e.startVideoFullScreen,e.confExtension,e.extensionPrefix,e.confExtensionPrefix,e.callWaiting,e.doc1ImageType,e.doc2ImageType,e.phoneTrunk,e.secondDomain,e.lastConnectedCustomerPhone) from Employee e where e.sex = ?1 and e.organization = ?2")
    List<Employee> findAllBySexAndOrganization(String sex,String organization);
	
    @Query("select  new com.mylinehub.crm.entity.Employee(e.id,e.firstName, e.lastName, e.email, e.userRole , e.pesel, e.sex, e.birthdate , e.salary, e.department,e.phoneContext,e.isLocked , e.isEnabled,    e.organization, e.extension,  e.domain, e.protocol, e.timezone, e.type, e.callonnumber, e.useSecondaryAllotedLine, e.phonenumber, e.provider1, e.allotednumber1, e.provider2, e.allotednumber2,e.costCalculation, e.amount, e.transfer_phone_1, e.transfer_phone_2,  e.imageName, e.imageType, e.governmentDocumentID1, e.governmentDocumentID2,e.totalparkedchannels,e.parkedchannel1,e.parkedchannel2,e.parkedchannel3,e.parkedchannel4,e.parkedchannel5,e.parkedchannel6,e.parkedchannel7,e.parkedchannel8,e.parkedchannel9,e.parkedchannel10,e.autoAnswer,e.autoConference,e.autoVideo,e.micDevice,e.speakerDevice,e.videoDevice,e.videoOrientation,e.videoQuality,e.videoFrameRate,e.autoGainControl,e.echoCancellation,e.noiseSupression,e.recordAllCalls,e.intercomPolicy,e.freeDialOption,e.textDictateOption,e.textMessagingOption,e.doNotDisturb,e.startVideoFullScreen,e.confExtension,e.extensionPrefix,e.confExtensionPrefix,e.callWaiting,e.doc1ImageType,e.doc2ImageType,e.phoneTrunk,e.secondDomain,e.lastConnectedCustomerPhone) from Employee e where e.costCalculation = ?1 and e.organization = ?2")
    List<Employee> findAllBycostCalculationAndOrganization(String costCalculation,String organization);
    
    @Query("select  new com.mylinehub.crm.entity.Employee(e.id,e.firstName, e.lastName, e.email, e.userRole , e.pesel, e.sex, e.birthdate , e.salary, e.department,e.phoneContext,e.isLocked , e.isEnabled,    e.organization, e.extension,  e.domain, e.protocol, e.timezone, e.type, e.callonnumber, e.useSecondaryAllotedLine, e.phonenumber, e.provider1, e.allotednumber1, e.provider2, e.allotednumber2,e.costCalculation, e.amount, e.transfer_phone_1, e.transfer_phone_2,  e.imageName, e.imageType, e.governmentDocumentID1, e.governmentDocumentID2,e.totalparkedchannels,e.parkedchannel1,e.parkedchannel2,e.parkedchannel3,e.parkedchannel4,e.parkedchannel5,e.parkedchannel6,e.parkedchannel7,e.parkedchannel8,e.parkedchannel9,e.parkedchannel10,e.autoAnswer,e.autoConference,e.autoVideo,e.micDevice,e.speakerDevice,e.videoDevice,e.videoOrientation,e.videoQuality,e.videoFrameRate,e.autoGainControl,e.echoCancellation,e.noiseSupression,e.recordAllCalls,e.intercomPolicy,e.freeDialOption,e.textDictateOption,e.textMessagingOption,e.doNotDisturb,e.startVideoFullScreen,e.confExtension,e.extensionPrefix,e.confExtensionPrefix,e.callWaiting,e.doc1ImageType,e.doc2ImageType,e.phoneTrunk,e.secondDomain,e.lastConnectedCustomerPhone) from Employee e where e.userRole = ?1 and e.organization = ?2")
    List<Employee> findAllByUserRoleAndOrganization(USER_ROLE userRole,String organization);
    
    @Query("select  new com.mylinehub.crm.entity.Employee(e.id,e.firstName, e.lastName, e.email, e.userRole , e.pesel, e.sex, e.birthdate , e.salary, e.department,e.phoneContext,e.isLocked , e.isEnabled,    e.organization, e.extension,  e.domain, e.protocol, e.timezone, e.type, e.callonnumber, e.useSecondaryAllotedLine, e.phonenumber, e.provider1, e.allotednumber1, e.provider2, e.allotednumber2,e.costCalculation, e.amount, e.transfer_phone_1, e.transfer_phone_2,  e.imageName, e.imageType, e.governmentDocumentID1, e.governmentDocumentID2,e.totalparkedchannels,e.parkedchannel1,e.parkedchannel2,e.parkedchannel3,e.parkedchannel4,e.parkedchannel5,e.parkedchannel6,e.parkedchannel7,e.parkedchannel8,e.parkedchannel9,e.parkedchannel10,e.autoAnswer,e.autoConference,e.autoVideo,e.micDevice,e.speakerDevice,e.videoDevice,e.videoOrientation,e.videoQuality,e.videoFrameRate,e.autoGainControl,e.echoCancellation,e.noiseSupression,e.recordAllCalls,e.intercomPolicy,e.freeDialOption,e.textDictateOption,e.textMessagingOption,e.doNotDisturb,e.startVideoFullScreen,e.confExtension,e.extensionPrefix,e.confExtensionPrefix,e.callWaiting,e.doc1ImageType,e.doc2ImageType,e.phoneTrunk,e.secondDomain,e.lastConnectedCustomerPhone) from Employee e where e.isEnabled is ?1 and e.organization = ?2")
    List<Employee> findAllByIsEnabledAndOrganization(Boolean isEnabled,String organization);
    
    
    @Query("select  new com.mylinehub.crm.entity.Employee(e.id,e.firstName, e.lastName, e.email, e.userRole , e.pesel, e.birthdate , e.department,e.extension, e.phonenumber, e.imageType,e.iconImageData) from Employee e where e.extension in (?1) and e.organization = ?2")
    List<Employee> findAllByExtensionsAndOrganization(List<String> extensions,String organization);
    
    
    @Override
    Optional<Employee> findById(Long id);

    
//    @Query("SELECT SUM(e.imageSize) + SUM(e.iconImageSize) + SUM(e.doc1ImageSize) + SUM(e.doc2ImageSize) " +
//    	       "FROM Employee e " +
//    	       "WHERE e.organization = ?1 AND " +
//    	       "(e.imageData IS NOT NULL OR e.governmentDocument1Data IS NOT NULL OR e.governmentDocument2Data IS NOT NULL)")
//    Long getTotalImageSizeForOrganization(String organization);
    
    @Query("SELECT COALESCE(SUM(e.imageSize), 0) + " +
    	       "COALESCE(SUM(e.iconImageSize), 0) + " +
    	       "COALESCE(SUM(e.doc1ImageSize), 0) + " +
    	       "COALESCE(SUM(e.doc2ImageSize), 0) " +
    	       "FROM Employee e " +
    	       "WHERE e.organization = ?1 AND " +
    	       "(e.imageData IS NOT NULL OR e.governmentDocument1Data IS NOT NULL OR e.governmentDocument2Data IS NOT NULL)")
    	Long getTotalImageSizeForOrganization(String organization);
    
}
