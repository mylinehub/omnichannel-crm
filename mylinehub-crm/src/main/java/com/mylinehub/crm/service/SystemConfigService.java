package com.mylinehub.crm.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.data.SystemConfigData;
import com.mylinehub.crm.entity.SystemConfig;
import com.mylinehub.crm.entity.dto.SystemConfigDTO;
import com.mylinehub.crm.mapper.SystemConfigMapper;
import com.mylinehub.crm.repository.SystemConfigRepository;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class SystemConfigService implements CurrentTimeInterface{

	/**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final SystemConfigRepository systemConfigRepository;
    private final SystemConfigMapper systemConfigMapper;
    
	/**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean createsystemConfigByOrganization(SystemConfigDTO systemConfigDetails) {
    	
    	SystemConfig current = systemConfigRepository.findById(systemConfigDetails.getId()).get();
    	
    	if(current==null)
    	{
    		current = systemConfigMapper.mapDTOToSystemConfig(systemConfigDetails);
    		SystemConfigData.systemConfig = systemConfigRepository.save(current);
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
    public boolean updatesystemConfigByOrganization(SystemConfigDTO systemConfigDetails) {
    	
    	SystemConfig current = systemConfigRepository.findById(systemConfigDetails.getId()).get();
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{
    		
    		try
    		{

    			systemConfigMapper.updateSystemConfigToSystemConfig(systemConfigDetails, current);
        		SystemConfigData.systemConfig = systemConfigRepository.save(current);
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
    public boolean deletesystemConfigByOrganization(Long id) {
    	
    	SystemConfig current = systemConfigRepository.findById(id).get();
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{    		
    		systemConfigRepository.delete(current);
    		SystemConfigData.systemConfig = null;
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
    
    public List<SystemConfigDTO> getAllsystemConfigsOnOrganization(String organization){
        return systemConfigRepository.findAllByOrganization(organization)
                .stream()
                .map(systemConfigMapper::mapSystemConfigToDTO)
                .collect(Collectors.toList());
    }
	
}