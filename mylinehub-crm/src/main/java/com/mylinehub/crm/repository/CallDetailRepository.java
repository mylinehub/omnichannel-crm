package com.mylinehub.crm.repository;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.CallDetail;
import com.mylinehub.crm.entity.dto.CallCountByCallTypeDTO;
import com.mylinehub.crm.entity.dto.CallCountForEmployeeDTO;

@Repository
public interface CallDetailRepository extends JpaRepository<CallDetail, Long> {

	CallDetail findTopByLinkIdAndOrganization(String linkId, String organization);

	@Transactional
	@Modifying
	@Query("update CallDetail c set c.callCost = ?1, c.callCostMode = ?2 where c.linkId = ?3 and c.organization = ?4")
	int updateCallCostAndModeByLinkIdAndOrganization(double callCost, String callCostMode, String linkId, String organization);
	
    // -------------------- PAGE --------------------

    @Query("select e from CallDetail e " +
           "where e.organization = ?1 and " +
           "(e.callerid LIKE %?2% or e.employeeName LIKE %?2% or e.customerName LIKE %?2% or e.customerid LIKE %?2% or e.phoneContext LIKE %?2% or e.callType LIKE %?2% or e.country LIKE %?2%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByOrganization(String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds >= ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCalldurationsecondsGreaterThanEqualAndOrganization(double calldurationseconds, String organization, String searchText, Pageable pageable);

    // NOTE: made LIKE block consistent with Slice and other methods (added employeeName/customerName)
    @Query("select e from CallDetail e " +
           "where e.calldurationseconds <= ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCalldurationsecondsLessThanEqualAndOrganization(double calldurationseconds, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.callonmobile = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCallonmobileAndOrganization(boolean callonmobile, String organization, String searchText, Pageable pageable);

    // NOTE: made LIKE block consistent with Slice and other methods (added employeeName/customerName)
    @Query("select e from CallDetail e " +
           "where e.timezone = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByTimezoneAndOrganization(TimeZone timezone, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.isconference = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByIsconferenceAndOrganization(boolean isconference, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.country = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCountryAndOrganization(String country, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.customerid = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCustomeridAndOrganization(String customerid, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.phoneContext = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByPhoneContextAndOrganization(String phoneContext, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.callerid = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCalleridAndOrganization(String callerid, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds >= ?1 and e.callerid = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCalldurationsecondsGreaterThanEqualAndCalleridAndOrganization(double calldurationseconds, String callerid, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds <= ?1 and e.customerid = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCalldurationsecondsLessThanEqualAndCustomeridAndOrganization(double calldurationseconds, String customerid, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds >= ?1 and e.isconference = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCalldurationsecondsGreaterThanEqualAndIsconferenceAndOrganization(double calldurationseconds, boolean isconference, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds <= ?1 and e.isconference = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCalldurationsecondsLessThanEqualAndIsconferenceAndOrganization(double calldurationseconds, boolean isconference, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds <= ?1 and e.callerid = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCalldurationsecondsLessThanEqualAndCalleridAndOrganization(double calldurationseconds, String callerid, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds >= ?1 and e.timezone = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCalldurationsecondsGreaterThanEqualAndTimezoneAndOrganization(double calldurationseconds, TimeZone timezone, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds <= ?1 and e.timezone = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCalldurationsecondsLessThanEqualAndTimezoneAndOrganization(double calldurationseconds, TimeZone timezone, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds >= ?1 and e.phoneContext = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCalldurationsecondsGreaterThanEqualAndPhoneContextAndOrganization(double calldurationseconds, String phoneContext, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds <= ?1 and e.phoneContext = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCalldurationsecondsLessThanEqualAndPhoneContextAndOrganization(double calldurationseconds, String phoneContext, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.callonmobile = ?1 and e.isconference = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCallonmobileAndIsconferenceAndOrganization(boolean callonmobile, boolean isconference, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds >= ?1 and e.customerid = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByCalldurationsecondsGreaterThanEqualAndCustomeridAndOrganization(double calldurationseconds, String customerid, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.startdate >= ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByStartdateGreaterThanEqualAndOrganization(Date startdate, String organization, String searchText, Pageable pageable);

    // FIXED: phoneContext was unused + organization parameter position was wrong
    @Query("select e from CallDetail e " +
           "where e.isconnected = ?1 and e.customerid = ?2 and e.phoneContext = ?3 and e.organization = ?4 and " +
           "(e.callerid LIKE %?5% or e.employeeName LIKE %?5% or e.customerName LIKE %?5% or e.customerid LIKE %?5% or e.phoneContext LIKE %?5% or e.callType LIKE %?5% or e.country LIKE %?5%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByIsconnectedAndCustomeridAndPhoneContextAndOrganization(String isconnected, String customerid, String phoneContext, String organization, String searchText, Pageable pageable);

    // FIXED: phoneContext was unused + organization parameter position was wrong
    @Query("select e from CallDetail e " +
           "where e.isconnected = ?1 and e.callerid = ?2 and e.phoneContext = ?3 and e.organization = ?4 and " +
           "(e.callerid LIKE %?5% or e.employeeName LIKE %?5% or e.customerName LIKE %?5% or e.customerid LIKE %?5% or e.phoneContext LIKE %?5% or e.callType LIKE %?5% or e.country LIKE %?5%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByIsconnectedAndCalleridAndPhoneContextAndOrganization(String isconnected, String callerid, String phoneContext, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.ivr = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByIvrAndOrganization(boolean ivr, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.queue = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByQueueAndOrganization(boolean queue, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.pridictive = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByPridictiveAndOrganization(boolean pridictive, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.progressive = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByprogressiveAndOrganization(boolean progressive, String organization, String searchText, Pageable pageable);


    // -------------------- SLICE --------------------

    @Query("select e from CallDetail e " +
           "where e.organization = ?1 and " +
           "(e.callerid LIKE %?2% or e.employeeName LIKE %?2% or e.customerName LIKE %?2% or e.customerid LIKE %?2% or e.phoneContext LIKE %?2% or e.callType LIKE %?2% or e.country LIKE %?2%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByOrganization(String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds >= ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCalldurationsecondsGreaterThanEqualAndOrganization(double calldurationseconds, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds <= ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCalldurationsecondsLessThanEqualAndOrganization(double calldurationseconds, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.callonmobile = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCallonmobileAndOrganization(boolean callonmobile, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.timezone = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByTimezoneAndOrganization(TimeZone timezone, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.isconference = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByIsconferenceAndOrganization(boolean isconference, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.country = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCountryAndOrganization(String country, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.customerid = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCustomeridAndOrganization(String customerid, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.phoneContext = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByPhoneContextAndOrganization(String phoneContext, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.callerid = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCalleridAndOrganization(String callerid, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds >= ?1 and e.callerid = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCalldurationsecondsGreaterThanEqualAndCalleridAndOrganization(double calldurationseconds, String callerid, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds <= ?1 and e.customerid = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCalldurationsecondsLessThanEqualAndCustomeridAndOrganization(double calldurationseconds, String customerid, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds >= ?1 and e.isconference = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCalldurationsecondsGreaterThanEqualAndIsconferenceAndOrganization(double calldurationseconds, boolean isconference, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds <= ?1 and e.isconference = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCalldurationsecondsLessThanEqualAndIsconferenceAndOrganization(double calldurationseconds, boolean isconference, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds <= ?1 and e.callerid = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCalldurationsecondsLessThanEqualAndCalleridAndOrganization(double calldurationseconds, String callerid, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds >= ?1 and e.timezone = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCalldurationsecondsGreaterThanEqualAndTimezoneAndOrganization(double calldurationseconds, TimeZone timezone, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds <= ?1 and e.timezone = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCalldurationsecondsLessThanEqualAndTimezoneAndOrganization(double calldurationseconds, TimeZone timezone, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds >= ?1 and e.phoneContext = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCalldurationsecondsGreaterThanEqualAndPhoneContextAndOrganization(double calldurationseconds, String phoneContext, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds <= ?1 and e.phoneContext = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCalldurationsecondsLessThanEqualAndPhoneContextAndOrganization(double calldurationseconds, String phoneContext, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.callonmobile = ?1 and e.isconference = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCallonmobileAndIsconferenceAndOrganization(boolean callonmobile, boolean isconference, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.calldurationseconds >= ?1 and e.customerid = ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByCalldurationsecondsGreaterThanEqualAndCustomeridAndOrganization(double calldurationseconds, String customerid, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.startdate >= ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.customerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByStartdateGreaterThanEqualAndOrganization(Date startdate, String organization, String searchText, Pageable pageable);

    // FIXED: phoneContext was unused + organization parameter position was wrong
    @Query("select e from CallDetail e " +
           "where e.isconnected = ?1 and e.customerid = ?2 and e.phoneContext = ?3 and e.organization = ?4 and " +
           "(e.callerid LIKE %?5% or e.employeeName LIKE %?5% or e.customerName LIKE %?5% or e.customerid LIKE %?5% or e.phoneContext LIKE %?5% or e.callType LIKE %?5% or e.country LIKE %?5%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByIsconnectedAndCustomeridAndPhoneContextAndOrganization(String isconnected, String customerid, String phoneContext, String organization, String searchText, Pageable pageable);

    // FIXED: phoneContext was unused + organization parameter position was wrong
    @Query("select e from CallDetail e " +
           "where e.isconnected = ?1 and e.callerid = ?2 and e.phoneContext = ?3 and e.organization = ?4 and " +
           "(e.callerid LIKE %?5% or e.employeeName LIKE %?5% or e.customerName LIKE %?5% or e.customerid LIKE %?5% or e.phoneContext LIKE %?5% or e.callType LIKE %?5% or e.country LIKE %?5%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByIsconnectedAndCalleridAndPhoneContextAndOrganization(String isconnected, String callerid, String phoneContext, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.ivr = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByIvrAndOrganization(boolean ivr, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.queue = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByQueueAndOrganization(boolean queue, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.pridictive = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByPridictiveAndOrganization(boolean pridictive, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.progressive = ?1 and e.organization = ?2 and " +
           "(e.callerid LIKE %?3% or e.employeeName LIKE %?3% or e.customerName LIKE %?3% or e.customerid LIKE %?3% or e.phoneContext LIKE %?3% or e.callType LIKE %?3% or e.country LIKE %?3%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByprogressiveAndOrganization(boolean progressive, String organization, String searchText, Pageable pageable);


    // -------------------- OTHER (unchanged behavior) --------------------

    CallDetail findTopByCalleridOrderByIdDesc(String callerid);

    List<CallDetail> findAllByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganization(Date startdate, Date enddate, String organization);

    @Query("select e from CallDetail e " +
           "where e.startdate >= ?1 and e.startdate <= ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganization(Date startdate, Date enddate, String organization, String searchText, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.startdate >= ?1 and e.startdate <= ?2 and e.organization = ?3 and e.callType = ?5 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganizationAndCallType(Date startdate, Date enddate, String organization, String searchText, String CallType, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.startdate >= ?1 and e.startdate <= ?2 and e.organization = ?3 and e.callType = ?5 and e.callerid in (?6) and " +
           "(e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganizationAndCallTypeAndExtensionIn(Date startdate, Date enddate, String organization, String searchText, String CallType, List<String> extensions, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.startdate >= ?1 and e.startdate <= ?2 and e.organization = ?3 and e.callerid in (?5) and " +
           "(e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Page<CallDetail> findAllByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganizationAndExtensionIn(Date startdate, Date enddate, String organization, String searchText, List<String> extensions, Pageable pageable);

    @Query("select e from CallDetail e " +
           "where e.startdate >= ?1 and e.startdate <= ?2 and e.organization = ?3 and e.callerid in (?5) and " +
           "(e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "order by e.startdate desc, e.id desc")
    Slice<CallDetail> getAllByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganizationAndExtensionIn(Date startdate, Date enddate, String organization, String searchText, List<String> extensions, Pageable pageable);

    @Query("select new com.mylinehub.crm.entity.dto.CallCountForEmployeeDTO(COUNT(e.id)) " +
           "from CallDetail e " +
           "where e.startdate >= ?1 and e.startdate <= ?2 and e.organization = ?3 and e.calldurationseconds >= ?5 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.country LIKE %?4%)")
    CallCountForEmployeeDTO findByCallGreatorThanXSecondAndStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganization(Date startdate, Date enddate, String organization, String searchText, double calldurationseconds);

    @Query("select new com.mylinehub.crm.entity.dto.CallCountForEmployeeDTO(COUNT(e.id)) " +
           "from CallDetail e " +
           "where e.startdate >= ?1 and e.startdate <= ?2 and e.organization = ?3 and e.callerid in (?5) and e.calldurationseconds >= ?6 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.country LIKE %?4%)")
    CallCountForEmployeeDTO findByCallGreatorThanXSecondAndStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganizationAndExtensionIn(Date startdate, Date enddate, String organization, String searchText, List<String> extensions, double calldurationseconds);

    @Query("select new com.mylinehub.crm.entity.dto.CallCountByCallTypeDTO(e.callType,COUNT(e.id)) " +
           "from CallDetail e " +
           "where e.startdate >= ?1 and e.startdate <= ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.country LIKE %?4%) " +
           "GROUP BY e.callType order by e.callType desc")
    List<CallCountByCallTypeDTO> findGroupByCallTypeByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganization(Date startdate, Date enddate, String organization, String searchText);

    @Query("select new com.mylinehub.crm.entity.dto.CallCountByCallTypeDTO(e.callType,COUNT(e.id)) " +
           "from CallDetail e " +
           "where e.startdate >= ?1 and e.startdate <= ?2 and e.organization = ?3 and e.callerid in (?5) and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.country LIKE %?4%) " +
           "GROUP BY e.callType order by e.callType desc")
    List<CallCountByCallTypeDTO> findGroupByCallTypeByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganizationAndExtensionIn(Date startdate, Date enddate, String organization, String searchText, List<String> extensions);

    @Query("select new com.mylinehub.crm.entity.dto.CallCountForEmployeeDTO(e.callerid,COUNT(e.callerid)) " +
           "from CallDetail e " +
           "where e.startdate >= ?1 and e.startdate <= ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "GROUP BY e.callerid order by e.callerid desc")
    List<CallCountForEmployeeDTO> findGroupByEmployeeByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganization(Date startdate, Date enddate, String organization, String searchText);

    @Query("select new com.mylinehub.crm.entity.dto.CallCountForEmployeeDTO(e.callerid,COUNT(e.callerid)) " +
           "from CallDetail e " +
           "where e.startdate >= ?1 and e.startdate <= ?2 and e.organization = ?3 and e.callerid in (?5) and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "GROUP BY e.callerid order by e.callerid desc")
    List<CallCountForEmployeeDTO> findGroupByEmployeeByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganizationAndExtensionIn(Date startdate, Date enddate, String organization, String searchText, List<String> extensions);

    @Query("select new com.mylinehub.crm.entity.dto.CallCountForEmployeeDTO(e.callerid,COUNT(e.callerid), MONTH(e.startdate), YEAR(e.startdate)) " +
           "from CallDetail e " +
           "where e.startdate >= ?1 and e.startdate <= ?2 and e.organization = ?3 and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "GROUP BY e.callerid, YEAR(e.startdate), MONTH(e.startdate) " +
           "order by YEAR(e.startdate), MONTH(e.startdate) desc")
    List<CallCountForEmployeeDTO> findGroupByEmployeeAndTimeByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganization(Date startdate, Date enddate, String organization, String searchText);

    @Query("select new com.mylinehub.crm.entity.dto.CallCountForEmployeeDTO(e.callerid,COUNT(e.callerid), MONTH(e.startdate), YEAR(e.startdate)) " +
           "from CallDetail e " +
           "where e.startdate >= ?1 and e.startdate <= ?2 and e.organization = ?3 and e.callerid in (?5) and " +
           "(e.callerid LIKE %?4% or e.employeeName LIKE %?4% or e.customerName LIKE %?4% or e.customerid LIKE %?4% or e.phoneContext LIKE %?4% or e.callType LIKE %?4% or e.country LIKE %?4%) " +
           "GROUP BY e.callerid, YEAR(e.startdate), MONTH(e.startdate) " +
           "order by YEAR(e.startdate), MONTH(e.startdate) desc")
    List<CallCountForEmployeeDTO> findGroupByEmployeeAndTimeByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganizationAndExtensionIn(Date startdate, Date enddate, String organization, String searchText, List<String> extensions);
}
