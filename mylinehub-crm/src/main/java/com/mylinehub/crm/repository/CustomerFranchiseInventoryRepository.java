package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.CustomerFranchiseInventory;

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
public interface CustomerFranchiseInventoryRepository extends JpaRepository<CustomerFranchiseInventory, Long> {

    // =========================
    // PAGE 0 (Page + count)
    // =========================
    @EntityGraph(attributePaths = "customer")
    @Query(
        value =
            "select f from CustomerFranchiseInventory f " +
            "join f.customer c " +
            "where c.organization = :org " +
            "and ( :available is null or f.available = :available ) " +
            "and ( " +
            "   :q = '' or " +
            "   lower(coalesce(f.interest,'')) like lower(concat('%',:q,'%')) or " +
            "   lower(coalesce(c.firstname,'')) like lower(concat('%',:q,'%')) or " +
            "   lower(coalesce(c.lastname,'')) like lower(concat('%',:q,'%')) or " +
            "   lower(coalesce(c.phoneNumber,'')) like lower(concat('%',:q,'%')) " +
            ") " +
            "order by f.createdOn desc, f.id desc",
        countQuery =
            "select count(f) from CustomerFranchiseInventory f " +
            "join f.customer c " +
            "where c.organization = :org " +
            "and ( :available is null or f.available = :available ) " +
            "and ( " +
            "   :q = '' or " +
            "   lower(coalesce(f.interest,'')) like lower(concat('%',:q,'%')) or " +
            "   lower(coalesce(c.firstname,'')) like lower(concat('%',:q,'%')) or " +
            "   lower(coalesce(c.lastname,'')) like lower(concat('%',:q,'%')) or " +
            "   lower(coalesce(c.phoneNumber,'')) like lower(concat('%',:q,'%')) " +
            ")"
    )
    Page<CustomerFranchiseInventory> findAllByOrganization_Page0(
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
        "select f from CustomerFranchiseInventory f " +
        "join f.customer c " +
        "where c.organization = :org " +
        "and ( :available is null or f.available = :available ) " +
        "and ( " +
        "   :q = '' or " +
        "   lower(coalesce(f.interest,'')) like lower(concat('%',:q,'%')) or " +
        "   lower(coalesce(c.firstname,'')) like lower(concat('%',:q,'%')) or " +
        "   lower(coalesce(c.lastname,'')) like lower(concat('%',:q,'%')) or " +
        "   lower(coalesce(c.phoneNumber,'')) like lower(concat('%',:q,'%')) " +
        ") " +
        "order by f.createdOn desc, f.id desc"
    )
    Slice<CustomerFranchiseInventory> findAllByOrganization_Slice(
        @Param("org") String organization,
        @Param("q") String searchText,
        @Param("available") Boolean available,
        Pageable pageable
    );

    // =========================
    // By ID (single row)
    // =========================
    @Query(
        "select f from CustomerFranchiseInventory f " +
        "join fetch f.customer c " +
        "where f.id = :id " +
        "and c.organization = :org "
    )
    Optional<CustomerFranchiseInventory> findByIdAndOrganizationWithCustomer(
        @Param("id") Long id,
        @Param("org") String organization
    );

    // =========================
    // By Customer ID (single row)
    // =========================
    @Query(
        "select f from CustomerFranchiseInventory f " +
        "join fetch f.customer c " +
        "where c.id = :customerId " +
        "and c.organization = :org "
    )
    Optional<CustomerFranchiseInventory> findByCustomerIdAndOrganizationWithCustomer(
        @Param("customerId") Long customerId,
        @Param("org") String organization
    );
    
    @Query(
    	    "select f from CustomerFranchiseInventory f " +
    	    "join fetch f.customer c " +
    	    "where c.organization = :org " +
    	    "and ( :available is null or f.available = :available ) " +
    	    "and f.createdOn is not null " +
    	    "and f.createdOn >= :from " +
    	    "order by f.createdOn desc, f.id desc"
    	)
    List<CustomerFranchiseInventory> findAllForExportAfterCreatedDate(
    	        @Param("org") String organization,
    	        @Param("from") Instant from,
    	        @Param("available") Boolean available
    	);
}
