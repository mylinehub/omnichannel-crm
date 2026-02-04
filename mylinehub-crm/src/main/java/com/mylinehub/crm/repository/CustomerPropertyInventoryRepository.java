package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.CustomerPropertyInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerPropertyInventoryRepository
        extends JpaRepository<CustomerPropertyInventory, Long> {

	// =========================
	// PAGE 0 (Page + count)
	// =========================
	@EntityGraph(attributePaths = "customer")
	@Query(
	    value =
	        "select i from CustomerPropertyInventory i " +
	        "join i.customer c " +
	        "where c.organization = :org " +
	        "and i.updatedByAi = true " +
	        "and ( :available is null or i.available = :available ) " +
	        "and ( " +
	        "   :q = '' or " +
	        "   lower(coalesce(i.pid,'')) like lower(concat('%',:q,'%')) or " +
	        "   lower(coalesce(i.city,'')) like lower(concat('%',:q,'%')) or " +
	        "   lower(coalesce(i.area,'')) like lower(concat('%',:q,'%')) or " +
	        "   lower(coalesce(i.propertyType,'')) like lower(concat('%',:q,'%')) or " +
	        "   lower(coalesce(i.callStatus,'')) like lower(concat('%',:q,'%')) or " +
	        "   lower(coalesce(c.firstname,'')) like lower(concat('%',:q,'%')) or " +
	        "   lower(coalesce(c.lastname,'')) like lower(concat('%',:q,'%')) or " +
	        "   lower(coalesce(c.phoneNumber,'')) like lower(concat('%',:q,'%')) " +
	        ") " +
	        "order by i.listedDate desc, i.id desc",
	    countQuery =
	        "select count(i) from CustomerPropertyInventory i " +
	        "join i.customer c " +
	        "where c.organization = :org " +
	        "and i.updatedByAi = true " +
	        "and ( :available is null or i.available = :available ) " +
	        "and ( " +
	        "   :q = '' or " +
	        "   lower(coalesce(i.pid,'')) like lower(concat('%',:q,'%')) or " +
	        "   lower(coalesce(i.city,'')) like lower(concat('%',:q,'%')) or " +
	        "   lower(coalesce(i.area,'')) like lower(concat('%',:q,'%')) or " +
	        "   lower(coalesce(i.propertyType,'')) like lower(concat('%',:q,'%')) or " +
	        "   lower(coalesce(i.callStatus,'')) like lower(concat('%',:q,'%')) or " +
	        "   lower(coalesce(c.firstname,'')) like lower(concat('%',:q,'%')) or " +
	        "   lower(coalesce(c.lastname,'')) like lower(concat('%',:q,'%')) or " +
	        "   lower(coalesce(c.phoneNumber,'')) like lower(concat('%',:q,'%')) " +
	        ")"
	)
	Page<CustomerPropertyInventory> findAllByOrganizationWithArea_Page0_UpdatedByAi(
	        @Param("org") String organization,
	        @Param("q") String searchText,
	        @Param("available") Boolean available,
	        Pageable pageable
	);

	
	// =========================
	// PAGE 1..N (Slice – fast)
	// =========================
	@EntityGraph(attributePaths = "customer")
	@Query(
	    "select i from CustomerPropertyInventory i " +
	    "join i.customer c " +
	    "where c.organization = :org " +
	    "and i.updatedByAi = true " +
	    "and ( :available is null or i.available = :available ) " +
	    "and ( " +
	    "   :q = '' or " +
	    "   lower(coalesce(i.pid,'')) like lower(concat('%',:q,'%')) or " +
	    "   lower(coalesce(i.city,'')) like lower(concat('%',:q,'%')) or " +
	    "   lower(coalesce(i.area,'')) like lower(concat('%',:q,'%')) or " +
	    "   lower(coalesce(i.propertyType,'')) like lower(concat('%',:q,'%')) or " +
	    "   lower(coalesce(i.callStatus,'')) like lower(concat('%',:q,'%')) or " +
	    "   lower(coalesce(c.firstname,'')) like lower(concat('%',:q,'%')) or " +
	    "   lower(coalesce(c.lastname,'')) like lower(concat('%',:q,'%')) or " +
	    "   lower(coalesce(c.phoneNumber,'')) like lower(concat('%',:q,'%')) " +
	    ") " +
	    "order by i.listedDate desc, i.id desc"
	)
	Slice<CustomerPropertyInventory> findAllByOrganizationWithArea_Slice_UpdatedByAi(
	        @Param("org") String organization,
	        @Param("q") String searchText,
	        @Param("available") Boolean available,
	        Pageable pageable
	);


    // =========================
    // By Inventory ID (single row => fetch ok)
    // =========================
    @Query(
        "select i from CustomerPropertyInventory i " +
        "join fetch i.customer c " +
        "where i.id = :id " +
        "and c.organization = :org "
    )
    Optional<CustomerPropertyInventory> findByIdAndOrganizationWithCustomer(
            @Param("id") Long id,
            @Param("org") String organization
    );

    // =========================
    // By Customer ID (single row => fetch ok)
    // =========================
    @Query(
        "select i from CustomerPropertyInventory i " +
        "join fetch i.customer c " +
        "where c.id = :customerId " +
        "and c.organization = :org " 
    )
    Optional<CustomerPropertyInventory> findByCustomerIdAndOrganizationWithCustomer(
            @Param("customerId") Long customerId,
            @Param("org") String organization
    );

    
	 // =========================
	 // Export (no pagination => fetch ok)
	 // =========================
	 @Query(
	     "select i from CustomerPropertyInventory i " +
	     "join fetch i.customer c " +
	     "where c.organization = :org " +
	     "and i.updatedByAi = true " +
	     "and ( :available is null or i.available = :available ) " +
	     "and i.listedDate is not null " +
	     "and i.listedDate >= :from " +
	     "order by i.listedDate desc, i.id desc"
	 )
	 List<CustomerPropertyInventory> findAllForExportAfterListedDate_UpdatedByAi(
	         @Param("org") String organization,
	         @Param("from") Instant from,
	         @Param("available") Boolean available
	 );

}
