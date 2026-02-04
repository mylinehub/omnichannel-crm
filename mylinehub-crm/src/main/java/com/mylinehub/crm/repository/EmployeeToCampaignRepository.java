package com.mylinehub.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.EmployeeToCampaign;



@Repository
public interface EmployeeToCampaignRepository extends JpaRepository<EmployeeToCampaign, Long> {

	List<EmployeeToCampaign> findAllByCampaignAndOrganization(Campaign campaign,String organization);
	List<EmployeeToCampaign> findAllByEmployeeAndOrganization(Employee employee,String organization);
	EmployeeToCampaign findByEmployeeAndCampaignAndOrganization(Employee employee,Campaign campaign,String organization);
	
	@Query(value = "select  e from EmployeeToCampaign e where e.campaign = ?1 AND e.employee in (?2)")
	List<EmployeeToCampaign> findAllByCampaignAndCustomerIn(Campaign campaign,List<Employee> employee);
	
	
	@Transactional
    @Modifying
    @Query("UPDATE EmployeeToCampaign a " +
            "SET a.lastCustomerNumber = 0 WHERE a.campaign = ?1 AND a.organization = ?2")
    int resetCampaignEmployees(Campaign campaign,String organization);
	
}
