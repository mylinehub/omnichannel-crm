package com.mylinehub.crm.ami.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.data.StartedCampaignData;
import com.mylinehub.crm.data.dto.CampaignCustomerDataDTO;
import com.mylinehub.crm.data.dto.CampaignEmployeeDataDTO;
import com.mylinehub.crm.data.dto.CustomerAndItsCampaignDTO;
import com.mylinehub.crm.data.dto.EmployeeAndItsCampaignDTO;
import com.mylinehub.crm.data.dto.PageInfoDTO;
import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.CustomerToCampaign;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.EmployeeToCampaign;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.repository.CampaignRepository;
import com.mylinehub.crm.repository.CustomerRepository;
import com.mylinehub.crm.repository.CustomerToCampaignRepository;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.EmployeeToCampaignRepository;
import com.mylinehub.crm.utils.LoggerUtils;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class SaveMemoryDataToDatabaseService {

	/**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final CampaignRepository campaignRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeToCampaignRepository  employeeToCampaignRepository;
    private final CustomerToCampaignRepository  customerToCampaignRepository;


	 public boolean saveLastCommonRecordForAllCampaign()
	    {
	    	LoggerUtils.log.debug("saveLastCommonRecordForAllCampaign");
			boolean toReturn = true;
			
			try {
				List<Campaign> allCampaigns= new ArrayList<Campaign>();
				
				Map<Long,Campaign> activeCampaigns = StartedCampaignData.workOnAllActiveCampaigns(null, null, "get");
				
				if(activeCampaigns != null)
				{
					for (Map.Entry<Long,Campaign>  entry : activeCampaigns.entrySet()) {
						Campaign value = entry.getValue();
						PageInfoDTO campaignPageInfoDTO = StartedCampaignData.updateOrGetCampaignCommonPageInfo(value.getId(), null,"get");
						int lastCustomerNumber = (((campaignPageInfoDTO.getCurrentPage()-1)*StartedCampaignData.getPageSize())+campaignPageInfoDTO.getRecordOfPage());
						value.setTotalCallsMade(entry.getValue().getTotalCallsMade());
						value.setLastCustomerNumber(lastCustomerNumber);
						allCampaigns.add(value);
					}
				}
				
				LoggerUtils.log.debug("Saving all campaign data to database");
				
				if(allCampaigns!=null && allCampaigns.size()>0)
				campaignRepository.saveAll(allCampaigns);
				
			}
			catch(Exception e)
			{
				toReturn = false;
				e.printStackTrace();
				throw e;
			}
			
			return toReturn;
	    }


	 public boolean saveLastCommonRecordForCampaign(Long campaignId)
	    {
	    	LoggerUtils.log.debug("saveLastCommonRecordForCampaign");
			boolean toReturn = true;
			
			Map<Long,Campaign> activeCampaigns = StartedCampaignData.workOnAllActiveCampaigns(null, null, "get");
			Campaign campaign = null;
			if(activeCampaigns != null)
			{
				campaign = activeCampaigns.get(campaignId);
			}
			
			try {
				if(campaign != null)
				{
					PageInfoDTO campaignPageInfoDTO = StartedCampaignData.updateOrGetCampaignCommonPageInfo(campaign.getId(), null,"get");
					int lastCustomerNumber = (((campaignPageInfoDTO.getCurrentPage()-1)*StartedCampaignData.getPageSize())+campaignPageInfoDTO.getRecordOfPage());
					campaign.setLastCustomerNumber(lastCustomerNumber);
					campaign.setTotalCallsMade(campaign.getTotalCallsMade());
					campaignRepository.save(campaign);
				}
				else
				{
					toReturn = false;
				}
			}
			catch(Exception e)
			{
				toReturn = false;
				e.printStackTrace();
				throw e;
			}
			
			return toReturn;
	    }



	 public boolean saveAllCustomerDataInMemoryToDatabase()
	    {
	    	
	    	LoggerUtils.log.debug("saveAllCustomerDataInMemoryToDatabase");
			boolean toReturn = true;
			
			try {
		    	List<Customers> allCampaignCustomer= new ArrayList<Customers>();
		    	
		    	Map<String,CustomerAndItsCampaignDTO> allActiveCustomersAndItsCampaign= StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(null,null, "get");

	        	 if(allActiveCustomersAndItsCampaign!=null && allActiveCustomersAndItsCampaign.size()>0)
	        	 {
	        		 for (Map.Entry<String,CustomerAndItsCampaignDTO>  entry : allActiveCustomersAndItsCampaign.entrySet()) {
	 				    CustomerAndItsCampaignDTO value = entry.getValue();
	 				    List<Customers> allCustomersWithSameNumber = value.getCustomers();
	 				    for(int i =0; i< allCustomersWithSameNumber.size();i++)
	 				    {
	 					    allCampaignCustomer.add(allCustomersWithSameNumber.get(i));
	 				    }
	 				}
	 				
	        	 }
				
				LoggerUtils.log.debug("Saving all customer data to database");
				if(allCampaignCustomer!=null && allCampaignCustomer.size()>0)
				customerRepository.saveAll(allCampaignCustomer);
			}
			catch(Exception e)
			{
				toReturn = false;
				e.printStackTrace();
				throw e;
			}
			
			return toReturn;
	    }


	  public boolean saveAllEmployeeDataInMemoryToDatabase()
	    {
	    	
	    	LoggerUtils.log.debug("saveAllEmployeeDataInMemoryToDatabase");
			boolean toReturn = true;
			
			try {
				List<Employee> allCampaignEmployees= new ArrayList<Employee>();
				
				Map<String,EmployeeAndItsCampaignDTO> allActiveExtensionsAndTheirCampaign= StartedCampaignData.workOnAllActiveExtensionsAndTheirCampaign(null,null, "get");

				if(allActiveExtensionsAndTheirCampaign!=null)
				{
					for (Map.Entry<String,EmployeeAndItsCampaignDTO>  entry : allActiveExtensionsAndTheirCampaign.entrySet()) {
						
						Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(entry.getKey(), null, "get-one");
						EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
						if(allEmployeeDataAndState != null)
						{
							employeeDataAndStateDTO = allEmployeeDataAndState.get(entry.getKey());
						} 
						
						allCampaignEmployees.add(employeeDataAndStateDTO.getEmployee());
					}
				}

				
				LoggerUtils.log.debug("Saving all employee data to database");
				if(allCampaignEmployees!=null && allCampaignEmployees.size()>0)
				employeeRepository.saveAll(allCampaignEmployees);
			}
			catch(Exception e)
			{
				toReturn = false;
				e.printStackTrace();
				throw e;
			}
			
			return toReturn;
	    }


	 public boolean saveCustomerAndEmployeeForAllCampaigns()
	    {
	    	LoggerUtils.log.debug("saveCustomerAndEmployeeForAllCampaigns");
	    	
	    	boolean toReturn = true;
			
			try {
				
				Map<Long,Campaign> activeCampaigns = StartedCampaignData.workOnAllActiveCampaigns(null, null, "get");
				
				if(activeCampaigns != null)
				{
					for (Map.Entry<Long,Campaign>  entry : activeCampaigns.entrySet()) {
						Long campaignId = entry.getKey();
						this.saveCustomerRelatedToCampaignLastNum(campaignId);
						this.saveEmployeeRelatedToCampaignLastNum(campaignId);
					}
				}
				
			}
			catch(Exception e)
			{
				toReturn = false;
				e.printStackTrace();
				throw e;
			}
			
			return toReturn;
	    }


	 public boolean saveCustomerRelatedToCampaignLastNum(Long campaignId)
	    {
	    	LoggerUtils.log.debug("saveCustomerRelatedToCampaignLastNum by campaign");
			boolean toReturn = true;
			
			try {
				
				Map<Long,Campaign> activeCampaigns = StartedCampaignData.workOnAllActiveCampaigns(null, null, "get");
				Campaign campaign = null;
				if(activeCampaigns != null)
				{
					campaign = activeCampaigns.get(campaignId);
				}
				
				//Set lastExtension for cutomer to campaign
				Map<String,CampaignCustomerDataDTO> allCustomersDataForCampaign = StartedCampaignData.workOnActiveCampaignAndCommonCustomersPage(campaignId,null,null,null, "get");
				
				List<Customers> allCustomerWhoseDataIsToBeAdded = new ArrayList<Customers>();
				
				if(allCustomersDataForCampaign!=null)
				{
					for (String phoneNumber : allCustomersDataForCampaign.keySet()) {
						
						 Map<String,CustomerAndItsCampaignDTO> allCustomersAndItsCampaignDTO= StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(phoneNumber,null, "get-one");
			        	 CustomerAndItsCampaignDTO customerAndItsCampaignDTO = null;
			        	 if(allCustomersAndItsCampaignDTO != null)
			        	 {
			        		 customerAndItsCampaignDTO = allCustomersAndItsCampaignDTO.get(phoneNumber);
			        	 }

						 if(customerAndItsCampaignDTO != null)
						 {
							 List<Customers> allCustomersWithSameNumber = customerAndItsCampaignDTO.getCustomers();
							    
							 for(int i =0; i< allCustomersWithSameNumber.size();i++)
							  {
								 allCustomerWhoseDataIsToBeAdded.add(allCustomersWithSameNumber.get(i));
							  }
							 
						 }
					    }

					List<CustomerToCampaign> allValues = customerToCampaignRepository.findAllByCampaignAndCustomerIn(campaign, allCustomerWhoseDataIsToBeAdded);
					
					
					if(allValues!=null)
					{
						for(int i =0 ; i< allValues.size(); i++)
						{
							CampaignCustomerDataDTO valueToUpdate = allCustomersDataForCampaign.get(allValues.get(i).getCustomer().getPhoneNumber());
							if(valueToUpdate != null)
							{
								allValues.get(i).setLastConnectedExtension(valueToUpdate.getCustomerLastCall());
								if(valueToUpdate.isCalledOnce())
								{
									allValues.get(i).setIsCalledOnce("true");
								}
								else
								{
									allValues.get(i).setIsCalledOnce("false");
								}
								
							}
						}
					}
					
					customerToCampaignRepository.saveAll(allValues);
				}	
			}
			catch(Exception e)
			{
				toReturn = false;
				e.printStackTrace();
				throw e;
			}
			
			return toReturn;
	    }
	    
	    
	     public boolean saveEmployeeRelatedToCampaignLastNum(Long campaignId)
	    {
			
	    	LoggerUtils.log.debug("saveEmployeeRelatedToCampaignLastNum by campaign");
	    	
			boolean toReturn = true;
			
			try {
				
				Map<Long,Campaign> activeCampaigns = StartedCampaignData.workOnAllActiveCampaigns(null, null, "get");
				Campaign campaign = null;
				if(activeCampaigns != null)
				{
					campaign = activeCampaigns.get(campaignId);
				}
				
				Map<String,CampaignEmployeeDataDTO> alldataIsToBeAdded = StartedCampaignData.workOnActiveCampaignAndAllEmployeeData(campaignId,null,null,null, "get");

				List<Employee> allEmployeesWhoseDataIsToBeAdded = new ArrayList<Employee>();
				
				if(alldataIsToBeAdded!=null)
				{
					for (String extension : alldataIsToBeAdded.keySet()) {
						
						Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(extension, null, "get-one");
						EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
						if(allEmployeeDataAndState != null)
						{
							employeeDataAndStateDTO = allEmployeeDataAndState.get(extension);
						} 
						
						 if(employeeDataAndStateDTO != null)
						 {
							Employee currentEmployee = employeeDataAndStateDTO.getEmployee();
							allEmployeesWhoseDataIsToBeAdded.add(currentEmployee);
						 }
					    }

					List<EmployeeToCampaign> allValues = employeeToCampaignRepository.findAllByCampaignAndCustomerIn(campaign, allEmployeesWhoseDataIsToBeAdded);
					
					
					if(allValues!=null)
					{
						
						for(int i =0 ; i< allValues.size(); i++)
						{
							String extension = allValues.get(i).getEmployee().getExtension();
							CampaignEmployeeDataDTO valueToUpdate = alldataIsToBeAdded.get(extension);
							
							Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(extension, null, "get-one");
							EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
							if(allEmployeeDataAndState != null)
							{
								employeeDataAndStateDTO = allEmployeeDataAndState.get(extension);
							} 
							
							if(valueToUpdate != null & employeeDataAndStateDTO !=null)
							{
								allValues.get(i).setLastConnectedCustomerPhone(employeeDataAndStateDTO.getEmployeeLastCall());
							}
						}
						
						employeeToCampaignRepository.saveAll(allValues);
					}
				}
				
			}
			catch(Exception e)
			{
				toReturn = false;
				e.printStackTrace();
				throw e;
			}
			
			return toReturn;
	    }
	
}
