package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.Customers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customers, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Customers c SET c.email = :email WHERE c.id = :id")
    int updateCustomerEmailById(@Param("id") Long id, @Param("email") String email);

    @Transactional
    @Modifying
    @Query("UPDATE Customers c SET c.firstWhatsAppMessageIsSend = :firstWhatsAppMessageIsSend WHERE c.id IN :ids")
    int updateCustomerFirstWhatsAppMessageFlagForBatch(
            @Param("firstWhatsAppMessageIsSend") boolean firstWhatsAppMessageIsSend,
            @Param("ids") List<Long> ids
    );

    @Transactional
    @Modifying
    @Query("UPDATE Customers c SET c.preferredLanguage = :preferredLanguage WHERE c.id IN :ids")
    int updateCustomerPreferredLanguageForBatch(
            @Param("preferredLanguage") String preferredLanguage,
            @Param("ids") List<Long> ids
    );

    @Transactional
    @Modifying
    @Query("UPDATE Customers c SET c.secondPreferredLanguage = :secondPreferredLanguage WHERE c.id IN :ids")
    int updateCustomerSecondPreferredLanguageForBatch(
            @Param("secondPreferredLanguage") String secondPreferredLanguage,
            @Param("ids") List<Long> ids
    );

    @Transactional
    @Modifying
    @Query("UPDATE Customers a SET a.description = ?1 WHERE a.id = ?2")
    int updateCustomerDescription(String Description, Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Customers a SET a.coverted = TRUE WHERE a.id = ?1")
    int customerGotConverted(Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Customers a SET a.coverted = FALSE WHERE a.id = ?1")
    int customerGotDiverted(Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Customers a SET a.autoWhatsAppAIReply = ?1 WHERE a.id = ?2")
    int updateWhatsAppAIAutoMessage(Boolean autoWhatsAppAIReply, Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Customers a SET a.interestedProducts = ?2 WHERE a.id = ?1")
    int updateCustomerProductInterests(Long id, String interestedProducts);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.coverted = ?1 and e.whatsAppRegisteredByPhoneNumber = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Page<Customers> findAllByCovertedAndWhatsAppRegisteredByPhoneNumber(boolean coverted, String whatsAppRegisteredByPhoneNumber, String searchText, Pageable pageable);

    // CHANGED: added e.phoneNumber LIKE %?2% (Page query must match Slice query behavior)
    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.whatsAppRegisteredByPhoneNumber = ?1 and " +
            "(e.firstname LIKE %?2% or e.lastname LIKE %?2% or e.pesel LIKE %?2% or e.email LIKE %?2% or e.phoneNumber LIKE %?2% or e.zipCode LIKE %?2% or e.country LIKE %?2% or e.business LIKE %?2% or e.datatype LIKE %?2%) " +
            "order by e.id desc")
    Page<Customers> findAllByWhatsAppRegisteredByPhoneNumber(String whatsAppRegisteredByPhoneNumber, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.coverted = ?1 and e.whatsAppProjectId = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Page<Customers> findAllByCovertedAndwhatsAppProjectId(boolean coverted, String whatsAppProjectId, String searchText, Pageable pageable);

    // CHANGED: added e.phoneNumber LIKE %?2% (Page query must match Slice query behavior)
    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.whatsAppProjectId = ?1 and " +
            "(e.firstname LIKE %?2% or e.lastname LIKE %?2% or e.pesel LIKE %?2% or e.email LIKE %?2% or e.phoneNumber LIKE %?2% or e.zipCode LIKE %?2% or e.country LIKE %?2% or e.business LIKE %?2% or e.datatype LIKE %?2%) " +
            "order by e.id desc")
    Page<Customers> findAllBywhatsAppProjectId(String whatsAppProjectId, String searchText, Pageable pageable);

    // CHANGED: added e.phoneNumber LIKE %?2% (to match Slice getAllByOrganization)
    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.organization = ?1 and " +
            "(e.firstname LIKE %?2% or e.lastname LIKE %?2% or e.pesel LIKE %?2% or e.email LIKE %?2% or e.phoneNumber LIKE %?2% or e.zipCode LIKE %?2% or e.country LIKE %?2% or e.business LIKE %?2% or e.datatype LIKE %?2%) " +
            "order by e.id desc")
    Page<Customers> findAllByOrganization(String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.coverted = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Page<Customers> findAllByCovertedAndOrganization(boolean coverted, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.country = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Page<Customers> findAllByCountryAndOrganization(String country, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.business = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Page<Customers> findAllByBusinessAndOrganization(String business, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.phoneContext = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Page<Customers> findAllByPhoneContextAndOrganization(String phoneContext, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.city = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Page<Customers> findAllByCityAndOrganization(String city, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.zipCode = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Page<Customers> findAllByZipCodeAndOrganization(String zipCode, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.email = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Page<Customers> findAllByEmailAndOrganization(String email, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.pesel = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Page<Customers> findAllByPeselAndOrganization(String pesel, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.coverted = ?1 and e.country = ?2 and e.organization = ?3 and " +
            "(e.firstname LIKE %?4% or e.lastname LIKE %?4% or e.pesel LIKE %?4% or e.email LIKE %?4% or e.phoneNumber LIKE %?4% or e.zipCode LIKE %?4% or e.country LIKE %?4% or e.business LIKE %?4% or e.datatype LIKE %?4%) " +
            "order by e.id desc")
    Page<Customers> findAllByCountryAndOrganization(boolean coverted, String country, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.coverted = ?1 and e.business = ?2 and e.organization = ?3 and " +
            "(e.firstname LIKE %?4% or e.lastname LIKE %?4% or e.pesel LIKE %?4% or e.email LIKE %?4% or e.phoneNumber LIKE %?4% or e.zipCode LIKE %?4% or e.country LIKE %?4% or e.business LIKE %?4% or e.datatype LIKE %?4%) " +
            "order by e.id desc")
    Page<Customers> findAllByBusinessAndOrganization(boolean coverted, String business, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.coverted = ?1 and e.phoneContext = ?2 and e.organization = ?3 and " +
            "(e.firstname LIKE %?4% or e.lastname LIKE %?4% or e.pesel LIKE %?4% or e.email LIKE %?4% or e.phoneNumber LIKE %?4% or e.zipCode LIKE %?4% or e.country LIKE %?4% or e.business LIKE %?4% or e.datatype LIKE %?4%) " +
            "order by e.id desc")
    Page<Customers> findAllByPhoneContextAndOrganization(boolean coverted, String phoneContext, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.coverted = ?1 and e.city = ?2 and e.organization = ?3 and " +
            "(e.firstname LIKE %?4% or e.lastname LIKE %?4% or e.pesel LIKE %?4% or e.email LIKE %?4% or e.phoneNumber LIKE %?4% or e.zipCode LIKE %?4% or e.country LIKE %?4% or e.business LIKE %?4% or e.datatype LIKE %?4%) " +
            "order by e.id desc")
    Page<Customers> findAllByCityAndOrganization(boolean coverted, String city, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.coverted = ?1 and e.zipCode = ?2 and e.organization = ?3 and " +
            "(e.firstname LIKE %?4% or e.lastname LIKE %?4% or e.pesel LIKE %?4% or e.email LIKE %?4% or e.phoneNumber LIKE %?4% or e.zipCode LIKE %?4% or e.country LIKE %?4% or e.business LIKE %?4% or e.datatype LIKE %?4%) " +
            "order by e.id desc")
    Page<Customers> findAllByZipCodeAndOrganization(boolean coverted, String zipCode, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.coverted = ?1 and e.email = ?2 and e.organization = ?3 and " +
            "(e.firstname LIKE %?4% or e.lastname LIKE %?4% or e.pesel LIKE %?4% or e.email LIKE %?4% or e.phoneNumber LIKE %?4% or e.zipCode LIKE %?4% or e.country LIKE %?4% or e.business LIKE %?4% or e.datatype LIKE %?4%) " +
            "order by e.id desc")
    Page<Customers> findAllByEmailAndOrganization(boolean coverted, String email, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.coverted = ?1 and e.pesel = ?2 and e.organization = ?3 and " +
            "(e.firstname LIKE %?4% or e.lastname LIKE %?4% or e.pesel LIKE %?4% or e.email LIKE %?4% or e.phoneNumber LIKE %?4% or e.zipCode LIKE %?4% or e.country LIKE %?4% or e.business LIKE %?4% or e.datatype LIKE %?4%) " +
            "order by e.id desc")
    Page<Customers> findAllByPeselAndOrganization(boolean coverted, String pesel, String organization, String searchText, Pageable pageable);

    Customers getCustomerByEmailAndOrganization(String email, String organization);
    Customers getCustomerByPhoneNumberAndOrganization(String phoneNumber, String organization);
    Customers getCustomerByIdAndOrganization(Long id, String organization);
    Customers getCustomerByWhatsAppPhoneNumberId(String whatsAppPhoneNumberId);
    Customers findByPhoneNumberContainingAndOrganization(String phoneNumber, String organization);
    
    
    
    @Query("select distinct e from Customers e left join fetch e.propertyInventory where e.phoneNumber in (?1) and e.organization = ?2")
    List<Customers> findAllCustomersByPhoneNumberInAndOrganization(List<String> phoneNumber, String organization);


    @Query(value = "select COUNT(e) from Customers e where e.coverted = ?5 and e.phoneNumber in " +
            "(select distinct cd.customerid from CallDetail cd where cd.startdate >= ?1 and cd.startdate <= ?2 and cd.organization = ?3 and " +
            "(cd.callerid LIKE %?4% or cd.employeeName LIKE %?4% or cd.customerName LIKE %?4% or cd.customerid LIKE %?4% or cd.phoneContext LIKE %?4% or cd.country LIKE %?4%))")
    long findCountOfAllCustomersByConvertedAndAsPerCallDetailRange(Date startdate, Date enddate, String organization, String searchText, boolean coverted);

    @Query(value = "select COUNT(e) from Customers e where e.coverted = ?6 and e.phoneNumber in " +
            "(select distinct cd.customerid from CallDetail cd where cd.startdate >= ?1 and cd.startdate <= ?2 and cd.organization = ?3 and cd.callerid in (?5) and " +
            "(cd.callerid LIKE %?4% or cd.employeeName LIKE %?4% or cd.customerName LIKE %?4% or cd.customerid LIKE %?4% or cd.phoneContext LIKE %?4% or cd.country LIKE %?4%))")
    long findCountOfAllCustomersByConvertedAndAsPerCallDetailRangeAndExtensionIn(Date startdate, Date enddate, String organization, String searchText, List<String> extensions, boolean coverted);

    @Query("SELECT COALESCE(SUM(e.imageSize), 0) FROM Customers e WHERE e.organization = ?1 AND e.imageData IS NOT NULL")
    Long getTotalImageSizeForOrganization(String organization);
    
    
    
    @Query("select new com.mylinehub.crm.entity.Customers(" +
            "e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, " +
            "e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling," +
            "e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.pesel = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or " +
            " e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Slice<Customers> getAllByPeselAndOrganization(String pesel, String organization, String searchText, Pageable pageable);

    
    // CHANGED: added e.phoneNumber LIKE %?2% (Slice query must match Page query behavior)
    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.whatsAppRegisteredByPhoneNumber = ?1 and " +
            "(e.firstname LIKE %?2% or e.lastname LIKE %?2% or e.pesel LIKE %?2% or e.email LIKE %?2% or e.phoneNumber LIKE %?2% or e.zipCode LIKE %?2% or e.country LIKE %?2% or e.business LIKE %?2% or e.datatype LIKE %?2%) " +
            "order by e.id desc")
    Slice<Customers> getAllByWhatsAppRegisteredByPhoneNumber(String whatsAppRegisteredByPhoneNumber, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.coverted = ?1 and e.whatsAppProjectId = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Slice<Customers> getAllByCovertedAndwhatsAppProjectId(boolean coverted, String whatsAppProjectId, String searchText, Pageable pageable);

    // CHANGED: added e.phoneNumber LIKE %?2% (Slice query must match Page query behavior)
    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.whatsAppProjectId = ?1 and " +
            "(e.firstname LIKE %?2% or e.lastname LIKE %?2% or e.pesel LIKE %?2% or e.email LIKE %?2% or e.phoneNumber LIKE %?2% or e.zipCode LIKE %?2% or e.country LIKE %?2% or e.business LIKE %?2% or e.datatype LIKE %?2%) " +
            "order by e.id desc")
    Slice<Customers> getAllBywhatsAppProjectId(String whatsAppProjectId, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.organization = ?1 and " +
            "(e.firstname LIKE %?2% or e.lastname LIKE %?2% or e.pesel LIKE %?2% or e.email LIKE %?2% or e.phoneNumber LIKE %?2% or e.zipCode LIKE %?2% or e.country LIKE %?2% or e.business LIKE %?2% or e.datatype LIKE %?2%) " +
            "order by e.id desc")
    Slice<Customers> getAllByOrganization(String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.country = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Slice<Customers> getAllByCountryAndOrganization(String country, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.business = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Slice<Customers> getAllByBusinessAndOrganization(String business, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.phoneContext = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Slice<Customers> getAllByPhoneContextAndOrganization(String phoneContext, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.city = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Slice<Customers> getAllByCityAndOrganization(String city, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.zipCode = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Slice<Customers> getAllByZipCodeAndOrganization(String zipCode, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.coverted = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Slice<Customers> getAllByCovertedAndOrganization(boolean coverted, String organization, String searchText, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.Customers(e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling,e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
            "from Customers e " +
            "where e.email = ?1 and e.organization = ?2 and " +
            "(e.firstname LIKE %?3% or e.lastname LIKE %?3% or e.pesel LIKE %?3% or e.email LIKE %?3% or e.phoneNumber LIKE %?3% or e.zipCode LIKE %?3% or e.country LIKE %?3% or e.business LIKE %?3% or e.datatype LIKE %?3%) " +
            "order by e.id desc")
    Slice<Customers> getAllByEmailAndOrganization(String email, String organization, String searchText, Pageable pageable);

   
	 // ------------------------------
	 // 1) OR + Page
	 // ------------------------------
	 @Query("select new com.mylinehub.crm.entity.Customers(" +
	         "e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, " +
	         "e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling," +
	         "e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
	         "from Customers e " +
	         "where e.organization = ?1 and (" +
	         "   ( " +
	         "     (?2 is not null or ?3 is not null or ?4 is not null) and " +
	         "     (?2 is null or e.country like cast(?2 as string)) and " +
	         "     (?3 is null or e.city like cast(?3 as string)) and " +
	         "     (?4 is null or e.zipCode like cast(?4 as string)) " +
	         "   ) " +
	         "   or " +
	         "   ( " +
	         "     (?5 is not null or ?6 is not null or ?7 is not null) and " +
	         "     (?5 is null or e.business like cast(?5 as string)) and " +
	         "     (?6 is null or e.datatype like cast(?6 as string)) and " +
	         "     (?7 is null or e.description like cast(?7 as string)) " +
	         "   ) " +
	         ") " +
	         "order by e.id desc")
	 Page<Customers> findAllToInsertIntoCampaignByOrAsPerParameters(
	         String organization,
	         String countryLike,
	         String cityLike,
	         String zipCodeLike,
	         String businessLike,
	         String datatypeLike,
	         String descriptionLike,
	         Pageable pageable
	 );
	
	 // ------------------------------
	 // 2) AND + Page
	 // ------------------------------
	 @Query("select new com.mylinehub.crm.entity.Customers(" +
	         "e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, " +
	         "e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling," +
	         "e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
	         "from Customers e " +
	         "where e.organization = ?1 and " +
	         "     (?2 is null or e.country like cast(?2 as string)) and " +
	         "     (?3 is null or e.city like cast(?3 as string)) and " +
	         "     (?4 is null or e.zipCode like cast(?4 as string)) and " +
	         "     (?5 is null or e.business like cast(?5 as string)) and " +
	         "     (?6 is null or e.datatype like cast(?6 as string)) and " +
	         "     (?7 is null or e.description like cast(?7 as string)) " +
	         "order by e.id desc")
	 Page<Customers> findAllToInsertIntoCampaignByAndAsPerParameters(
	         String organization,
	         String countryLike,
	         String cityLike,
	         String zipCodeLike,
	         String businessLike,
	         String datatypeLike,
	         String descriptionLike,
	         Pageable pageable
	 );
	
	 // ------------------------------
	 // 3) OR + Slice
	 // ------------------------------
	 @Query("select new com.mylinehub.crm.entity.Customers(" +
	         "e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, " +
	         "e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling," +
	         "e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
	         "from Customers e " +
	         "where e.organization = ?1 and (" +
	         "   ( " +
	         "     (?2 is not null or ?3 is not null or ?4 is not null) and " +
	         "     (?2 is null or e.country like cast(?2 as string)) and " +
	         "     (?3 is null or e.city like cast(?3 as string)) and " +
	         "     (?4 is null or e.zipCode like cast(?4 as string)) " +
	         "   ) " +
	         "   or " +
	         "   ( " +
	         "     (?5 is not null or ?6 is not null or ?7 is not null) and " +
	         "     (?5 is null or e.business like cast(?5 as string)) and " +
	         "     (?6 is null or e.datatype like cast(?6 as string)) and " +
	         "     (?7 is null or e.description like cast(?7 as string)) " +
	         "   ) " +
	         ") " +
	         "order by e.id desc")
	 Slice<Customers> getAllToInsertIntoCampaignByOrAsPerParameters(
	         String organization,
	         String countryLike,
	         String cityLike,
	         String zipCodeLike,
	         String businessLike,
	         String datatypeLike,
	         String descriptionLike,
	         Pageable pageable
	 );
	
	 // ------------------------------
	 // 4) AND + Slice
	 // ------------------------------
	 @Query("select new com.mylinehub.crm.entity.Customers(" +
	         "e.id,e.firstname, e.lastname, e.pesel, e.zipCode , e.city, e.email, e.phoneNumber , e.description, " +
	         "e.business,e.country,e.phoneContext,e.domain,e.datatype,e.organization,e.coverted,e.remindercalling," +
	         "e.cronremindercalling,e.iscalledonce,e.imageName,e.imageType,e.interestedProducts,e.lastConnectedExtension) " +
	         "from Customers e " +
	         "where e.organization = ?1 and " +
	         "     (?2 is null or e.country like cast(?2 as string)) and " +
	         "     (?3 is null or e.city like cast(?3 as string)) and " +
	         "     (?4 is null or e.zipCode like cast(?4 as string)) and " +
	         "     (?5 is null or e.business like cast(?5 as string)) and " +
	         "     (?6 is null or e.datatype like cast(?6 as string)) and " +
	         "     (?7 is null or e.description like cast(?7 as string)) " +
	         "order by e.id desc")
	 Slice<Customers> getAllToInsertIntoCampaignByAndAsPerParameters(
	         String organization,
	         String countryLike,
	         String cityLike,
	         String zipCodeLike,
	         String businessLike,
	         String datatypeLike,
	         String descriptionLike,
	         Pageable pageable
	 );


}
