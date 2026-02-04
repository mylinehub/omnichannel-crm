package com.mylinehub.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mylinehub.crm.entity.StatementReconciliation;


@Repository
public interface StatementReconciliationRepository extends JpaRepository<StatementReconciliation, Long> {
	
	
	@Query("select  new com.mylinehub.crm.entity.StatementReconciliation(e.id,e.name, e.organization, e.byExtension, e.client , e.reportHeading, e.bankName, e.noObservationFound , e.numberOfParallelThreads, e.createdOn) from StatementReconciliation e where e.organization = ?1")
	List<StatementReconciliation> findAllByOrganization(String organization);
    
	StatementReconciliation findByIdAndOrganization(Long id,String organization);
    StatementReconciliation deleteByIdAndOrganization(Long id,String organization);
    
}

