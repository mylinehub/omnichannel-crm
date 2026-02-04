package com.mylinehub.crm.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.mylinehub.crm.entity.SipProvider;
import com.mylinehub.crm.entity.dto.SipProviderDTO;
import com.mylinehub.crm.mapper.SipProviderMapper;
import com.mylinehub.crm.repository.SipProviderRepository;

import lombok.AllArgsConstructor;
/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class SipProviderService implements CurrentTimeInterface{


	/**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final SipProviderRepository sipProviderRepository;
    private final SipProviderMapper sipProviderMapper;
  
    
	 /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int enableSipProviderOnOrganization(Long id,String organization) {
        return sipProviderRepository.enableSipProviderByOrganization(id,organization);
    }

    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int disableSipProviderOnOrganization(Long id,String organization) {
        return sipProviderRepository.disableSipProviderByOrganization(id,organization);
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean createsipProviderByOrganization(SipProviderDTO sipProviderDetails) {
    	
    	SipProvider current = sipProviderRepository.findByPhoneNumberAndOrganization(sipProviderDetails.getPhoneNumber(),sipProviderDetails.getOrganization());
    	
    	if(current==null)
    	{
    		current = sipProviderMapper.mapDTOToSipProvider(sipProviderDetails);
    		sipProviderRepository.save(current);
    	}
    	else
    	{
    		
    		
    		return false;
    	}
    	
        return true;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean updateSipProviderByOrganization(SipProviderDTO sipProviderDetails) {
    	
    	SipProvider current = sipProviderRepository.findById(sipProviderDetails.getId()).get();
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{
    		
    		try
    		{

    			current.setActive(sipProviderDetails.isActive());
    			current.setCompany(sipProviderDetails.getCompany());
    			current.setCostCalculation(sipProviderDetails.getCostCalculation());
    			current.setMeteredPlanAmount(sipProviderDetails.getMeteredPlanAmount());
    			current.setOrganization(sipProviderDetails.getOrganization());
    			current.setPhoneNumber(sipProviderDetails.getPhoneNumber());
    			current.setProviderName(sipProviderDetails.getProviderName());
        		sipProviderRepository.save(current);
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			System.out.println("Exception while updating employee");
    			return false;
    		}
    		
    	}
    	
        return true;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean deletesipProviderByPhoneNumberAndOrganization(String phoneNumber, String organization) {
    	
    	SipProvider current = sipProviderRepository.findByPhoneNumberAndOrganization(phoneNumber,organization);
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{    		
    		sipProviderRepository.delete(current);
    	}
    	
        return true;
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<SipProviderDTO> getAllSipProvidersOnIsEnabledAndOrganization(boolean isEnabled, String organization){
        return sipProviderRepository.findAllByActiveAndOrganization(isEnabled,organization)
                .stream()
                .map(sipProviderMapper::mapSipProviderToDTO)
                .collect(Collectors.toList());
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<SipProviderDTO> getAllSipProvidersOnOrganization(String organization){
        return sipProviderRepository.findAllByOrganization(organization)
                .stream()
                .map(sipProviderMapper::mapSipProviderToDTO)
                .collect(Collectors.toList());
    }
	
    public SipProvider getSipProviderByPhoneNumberAndActiveAndOrganization(String phoneNumber,boolean active,String organization) {
    	return sipProviderRepository.findByPhoneNumberAndActiveAndOrganization(phoneNumber,active, organization);
    }
    
    
    public SipProviderDTO getSipProviderByPhoneNumberAndOrganization(String phoneNumber,String organization) {
    	return sipProviderMapper.mapSipProviderToDTO(sipProviderRepository.findByPhoneNumberAndOrganization(phoneNumber, organization));
    }
    
}