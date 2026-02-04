package com.mylinehub.crm.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.Employee;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
   
	@Transactional
    @Modifying
    @Query("UPDATE Campaign a " +
            "SET a.lastCustomerNumber = ?1 WHERE a.id = ?2 AND a.organization = ?3")
    int updateLastCustomerNumberByCampaignAndOrganization(int lastCustomerNumber, Long id,String organization);
	
	@Transactional
    @Modifying
    @Query("UPDATE Campaign a " +
            "SET a.isactive = TRUE WHERE a.id = ?1 AND a.organization = ?2")
    int activateCampaignByOrganization(Long id,String organization);
	
	@Transactional
    @Modifying
    @Query("UPDATE Campaign a " +
            "SET a.isactive = FALSE WHERE a.id = ?1 AND a.organization = ?2")
    int deactivateCampaignByOrganization(Long id,String organization);
	
	@Transactional
    @Modifying
    @Query("UPDATE Campaign a " +
            "SET a.isenabled = TRUE WHERE a.id = ?1 AND a.organization = ?2")
    int enableCampaignByOrganization(Long id,String organization);
	
	@Transactional
    @Modifying
    @Query("UPDATE Campaign a " +
            "SET a.isenabled = FALSE WHERE a.id = ?1 AND a.organization = ?2")
    int disableCampaignByOrganization(Long id,String organization);
	
	
	List<Campaign> findAllByIsactive(boolean isactive);
	
    List<Campaign> findAllByOrganization(String organization);
    List<Campaign> findAllByManagerAndOrganization(Employee manager,String organization);
    List<Campaign> findAllByCountryAndOrganization(String country,String organization);
    List<Campaign> findAllByBusinessAndOrganization(String business,String organization);
    List<Campaign> findAllByPhonecontextAndOrganization(String phonecontext,String organization);
    List<Campaign> findAllByIsonmobileAndOrganization(boolean isonmobile,String organization);
    List<Campaign> findAllByAutodialertypeAndOrganization(String autodialertype,String organization);
    
    List<Campaign> findAllByStartdateGreaterThanEqualAndOrganization(Date startdate, String organization);

    Campaign getCampaignByIdAndOrganization(Long id,String organization);
    Campaign getCampaignByNameAndOrganization(String name,String organization);
    
    
    
}