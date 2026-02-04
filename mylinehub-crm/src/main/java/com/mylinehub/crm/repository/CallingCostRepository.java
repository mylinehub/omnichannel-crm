//package com.mylinehub.crm.repository;
//
//import java.util.Date;
//import java.util.List;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Slice;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import com.mylinehub.crm.entity.CallDetail;
//import com.mylinehub.crm.entity.CallingCost;
//
//@Repository
//public interface CallingCostRepository extends JpaRepository<CallingCost, Long> {
//
//    // =========================================================
//    // PAGE 0 (Page + count)
//    // IMPORTANT: Use SAME stable ordering as Slice queries
//    // =========================================================
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.organization = ?1 " +
//        "and (e.extension LIKE %?2% or e.callcalculation LIKE %?2% or e.remarks LIKE %?2%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Page<CallingCost> findAllByOrganization(String organization, String searchText, Pageable pageable);
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.extension = ?1 and e.organization = ?2 " +
//        "and (e.extension LIKE %?3% or e.callcalculation LIKE %?3% or e.remarks LIKE %?3%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Page<CallingCost> findAllByExtensionAndOrganization(String extension, String organization, String searchText, Pageable pageable);
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.amount >= ?1 and e.organization = ?2 " +
//        "and (e.extension LIKE %?3% or e.callcalculation LIKE %?3% or e.remarks LIKE %?3%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Page<CallingCost> findAllByAmountGreaterThanEqualAndOrganization(Double amount, String organization, String searchText, Pageable pageable);
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.amount <= ?1 and e.organization = ?2 " +
//        "and (e.extension LIKE %?3% or e.callcalculation LIKE %?3% or e.remarks LIKE %?3%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Page<CallingCost> findAllByAmountLessThanEqualAndOrganization(Double amount, String organization, String searchText, Pageable pageable);
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.amount >= ?1 and e.callcalculation = ?2 and e.organization = ?3 " +
//        "and (e.extension LIKE %?4% or e.callcalculation LIKE %?4% or e.remarks LIKE %?4%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Page<CallingCost> findAllByAmountGreaterThanEqualAndCallcalculationAndOrganization(
//            Double amount, String callcalculation, String organization, String searchText, Pageable pageable);
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.amount <= ?1 and e.callcalculation = ?2 and e.organization = ?3 " +
//        "and (e.extension LIKE %?4% or e.callcalculation LIKE %?4% or e.remarks LIKE %?4%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Page<CallingCost> findAllByAmountLessThanEqualAndCallcalculationAndOrganization(
//            Double amount, String callcalculation, String organization, String searchText, Pageable pageable);
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.callcalculation = ?1 and e.organization = ?2 " +
//        "and (e.extension LIKE %?3% or e.callcalculation LIKE %?3% or e.remarks LIKE %?3%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Page<CallingCost> findAllByCallcalculationAndOrganization(String callcalculation, String organization, String searchText, Pageable pageable);
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.date >= ?1 and e.organization = ?2 " +
//        "and (e.extension LIKE %?3% or e.callcalculation LIKE %?3% or e.remarks LIKE %?3%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Page<CallingCost> findAllByDateGreaterThanEqualAndOrganization(Date date, String organization, String searchText, Pageable pageable);
//
//
//    // =========================================================
//    // PAGE 1..N (Slice – fast)
//    // IMPORTANT: Keep EXACT SAME ordering as Page queries
//    // =========================================================
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.organization = ?1 " +
//        "and (e.extension LIKE %?2% or e.callcalculation LIKE %?2% or e.remarks LIKE %?2%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Slice<CallingCost> getAllByOrganization(String organization, String searchText, Pageable pageable);
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.extension = ?1 and e.organization = ?2 " +
//        "and (e.extension LIKE %?3% or e.callcalculation LIKE %?3% or e.remarks LIKE %?3%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Slice<CallingCost> getAllByExtensionAndOrganization(String extension, String organization, String searchText, Pageable pageable);
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.amount >= ?1 and e.organization = ?2 " +
//        "and (e.extension LIKE %?3% or e.callcalculation LIKE %?3% or e.remarks LIKE %?3%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Slice<CallingCost> getAllByAmountGreaterThanEqualAndOrganization(Double amount, String organization, String searchText, Pageable pageable);
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.amount <= ?1 and e.organization = ?2 " +
//        "and (e.extension LIKE %?3% or e.callcalculation LIKE %?3% or e.remarks LIKE %?3%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Slice<CallingCost> getAllByAmountLessThanEqualAndOrganization(Double amount, String organization, String searchText, Pageable pageable);
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.amount >= ?1 and e.callcalculation = ?2 and e.organization = ?3 " +
//        "and (e.extension LIKE %?4% or e.callcalculation LIKE %?4% or e.remarks LIKE %?4%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Slice<CallingCost> getAllByAmountGreaterThanEqualAndCallcalculationAndOrganization(
//            Double amount, String callcalculation, String organization, String searchText, Pageable pageable);
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.amount <= ?1 and e.callcalculation = ?2 and e.organization = ?3 " +
//        "and (e.extension LIKE %?4% or e.callcalculation LIKE %?4% or e.remarks LIKE %?4%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Slice<CallingCost> getAllByAmountLessThanEqualAndCallcalculationAndOrganization(
//            Double amount, String callcalculation, String organization, String searchText, Pageable pageable);
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.callcalculation = ?1 and e.organization = ?2 " +
//        "and (e.extension LIKE %?3% or e.callcalculation LIKE %?3% or e.remarks LIKE %?3%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Slice<CallingCost> getAllByCallcalculationAndOrganization(String callcalculation, String organization, String searchText, Pageable pageable);
//
//    @Query(
//        "select e from CallingCost e " +
//        "where e.date >= ?1 and e.organization = ?2 " +
//        "and (e.extension LIKE %?3% or e.callcalculation LIKE %?3% or e.remarks LIKE %?3%) " +
//        "order by e.date desc, e.id desc"
//    )
//    Slice<CallingCost> getAllByDateGreaterThanEqualAndOrganization(Date date, String organization, String searchText, Pageable pageable);
//
//
//    // =========================================================
//    // OTHER (unchanged)
//    // =========================================================
//
//    CallingCost findByCallDetailAndOrganization(CallDetail callDetail, String organization);
//
//    List<CallingCost> getAllByDateGreaterThanEqualAndDateEndLessThanEqualAndOrganization(Date date, Date dateEnd, String organization);
//}
