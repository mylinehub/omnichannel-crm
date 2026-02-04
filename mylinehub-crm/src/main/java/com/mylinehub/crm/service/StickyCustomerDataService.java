package com.mylinehub.crm.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.StickyCustomerData;
import com.mylinehub.crm.entity.dto.StickyCustomerDataDTO;
import com.mylinehub.crm.mapper.StickyCustomerDataMapper;
import com.mylinehub.crm.repository.CustomerRepository;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.StickyCustomerDataRepository;

import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class StickyCustomerDataService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final StickyCustomerDataRepository stickyCustomerDataRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final StickyCustomerDataMapper stickyCustomerDataMapper;
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public StickyCustomerDataDTO findFirstByCustomerAndOrganizationOrderByCreatedDateDesc(Long customerId,String organization){
    
    	Customers currentCustomer = customerRepository.getCustomerByIdAndOrganization(customerId, organization);
    	
    	if(currentCustomer == null)
    	{
    		return null;
    	}
    	else
    	{
    		 return stickyCustomerDataMapper.mapStickyCustomerDataToDTO(stickyCustomerDataRepository.findFirstByCustomerAndOrganizationOrderByCreatedDateDesc(currentCustomer, organization));
    	}  
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public boolean addOrUpdateRecord(Long customerId,String extension,String organization){
    
    	Customers currentCustomer = customerRepository.getCustomerByIdAndOrganization(customerId, organization);
    	Employee currentEmployee = employeeRepository.findByExtensionAndOrganization(extension, organization);
    	
    	StickyCustomerData current = stickyCustomerDataRepository.findFirstByCustomerAndOrganizationOrderByCreatedDateDesc(currentCustomer, organization);
    	
    	if(current == null)
    	{	
        	if(currentCustomer == null)
        	{
        		return false;
        	}
        	else
        	{
        		if(currentEmployee==null)
        		{
        			return false;
        		}
        		else
        		{
        			StickyCustomerData toAdd = new StickyCustomerData();
        			
        			toAdd.setEmployee(currentEmployee);
        			toAdd.setCustomer(currentCustomer);
        			toAdd.setOrganization(organization);
        			toAdd.setCreatedDate(Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        			stickyCustomerDataRepository.save(toAdd);
        		}
        		
        		 return true;
        	}  
    	}
    	else
    	{
    		if(currentCustomer == null)
        	{
        		return false;
        	}
        	else
        	{
        		if(currentEmployee==null)
        		{
        			return false;
        		}
        		else
        		{
        			StickyCustomerData toAdd = new StickyCustomerData();
        			
        			current.setEmployee(currentEmployee);
        			current.setCustomer(currentCustomer);
        			current.setOrganization(organization);
        			current.setCreatedDate(Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        			stickyCustomerDataRepository.save(toAdd);
        		}
        		
        		 return true;
        	}  
    	}
    }
    
    
    
}