package com.mylinehub.crm.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.SupportTicket;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    // 1 Find all tickets by open, organization, and customerId
    List<SupportTicket> findByOpenAndOrganizationAndCustomerId(boolean open, String organization, Long customerId);

    // 2 Close (set open = false) all tickets older than 48 hours
    // Note: createdOn < (now - 48h)
    @Transactional
    @Modifying
    @Query("UPDATE SupportTicket s SET s.open = false WHERE s.open = true AND s.createdOn < :cutoffTime")
    int closeOldOpenTickets(@Param("cutoffTime") Instant cutoffTime);
}
