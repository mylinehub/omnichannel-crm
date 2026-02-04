package com.mylinehub.crm.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.ami.TaskScheduler.RefreshBackEndConnectionRunnable;
import com.mylinehub.crm.ami.autodialer.InitiateAndLoadCampaignDataService;
import com.mylinehub.crm.ami.autodialer.LoopInToDialOrSendMessage;
import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.data.StartedCampaignData;
import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.entity.dto.CampaignDTO;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.entity.dto.EmployeeToCampaignDTO;
import com.mylinehub.crm.exports.excel.ExportCampaignToXLSX;
import com.mylinehub.crm.exports.pdf.ExportCampaignToPDF;
import com.mylinehub.crm.mapper.CampaignMapper;
import com.mylinehub.crm.repository.CampaignRepository;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class CampaignService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
	private final ApplicationContext applicationContext;
    private final CampaignRepository campaignRepository;
    private final ErrorRepository errorRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;
    private final EmployeeToCampaignService employeeToCampaignService;
    private final CampaignMapper campaignMapper;
    private final InitiateAndLoadCampaignDataService initiateAndLoadCampaignDataService;
    
    public Campaign resolveCampaignMemoryThenDb(Long campaignId, String organization) {
        if (campaignId == null) return null;

        // 1) Try memory
        try {
            Map<Long, Campaign> one = StartedCampaignData.workOnAllActiveCampaigns(campaignId, null, "get-one");
            if (one != null) {
                Campaign c = one.get(campaignId);
                if (c != null) return c;
            }
        } catch (Exception ignore) {}

        // 2) DB fallback
        Campaign db = null;
        try {
            if (organization != null && !organization.trim().isEmpty()) {
                db = campaignRepository.getCampaignByIdAndOrganization(campaignId, organization.trim());
            } else {
                db = campaignRepository.findById(campaignId).orElse(null);
            }
        } catch (Exception ignore) {}

        if (db == null) return null;

        // 3) Cache back to memory
        try {
            StartedCampaignData.workOnAllActiveCampaigns(campaignId, db, "update");
        } catch (Exception ignore) {}

        return db;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param campaignDetails details of campaign
     * @return enable user account
     * @throws Exception 
     */
    public boolean startCampaignByOrganization(CampaignDTO campaignDetails, String fromExtension, String domain) throws Exception {
    	
    	boolean toReturn = true;
    	
    	System.out.println("startCampaignByOrganization");
    	
    	String campaignName = campaignDetails.getName();
    	String organization = campaignDetails.getOrganization();
    	
    	Campaign campaign = campaignRepository.getCampaignByIdAndOrganization(campaignDetails.getId(), campaignDetails.getOrganization());
    	
    	
//    	System.out.println("Verify user limit before start");
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(null, null, "get");
		
//    	System.out.println("allEmployeeDataAndState.size() : "+allEmployeeDataAndState.size());
//    	System.out.println("allEmployeeDataAndState.keySet().size() : "+allEmployeeDataAndState.keySet().size());
    	
//    	System.out.println("All Map Keys");
//    	Set<String> allKeys = allEmployeeDataAndState.keySet();
//    	for (String s : allKeys) {
//    	    System.out.println(s);
//    	}
//    	System.out.println("*********************** END OF KEYS *********************");
    	
    	if(allEmployeeDataAndState.size() > 100000000)
		{
    		//10 are support users and other 10 are users of organization is product is sold. Thats why total is kept 20.
    		System.out.println("User Limit > 100000000. Campaign cannot be started");
    		
    		try {
    			sendCampaignNotifications(campaignDetails.getOrganization(),"user-limit-exceed", campaignDetails,campaignDetails.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
    	else
    	{
    		System.out.println("User Limit < 100000000. Campaign can be started");
    		System.out.println("Refreshing backend connections");
    		try
    		{
        		RefreshBackEndConnectionRunnable refreshBackEndConnectionRunnable = new RefreshBackEndConnectionRunnable();
        		refreshBackEndConnectionRunnable.setErrorRepository(errorRepository);
        		refreshBackEndConnectionRunnable.setApplicationContext(applicationContext);
        		refreshBackEndConnectionRunnable.execute("AMI");
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			//issue-while-refresh-backend-connection
    			try {
        			sendCampaignNotifications(campaignDetails.getOrganization(),"issue-while-refresh-backend-connection", campaignDetails,campaignDetails.getId());
    			} catch (Exception e1) {
    				// TODO Auto-generated catch block
    				e1.printStackTrace();
    			}
    			
    			return false;
    			
    		}
    	}
    	

		if(campaign==null)
    	{
			System.out.println("Campaign was null");
			//Do nothing
    		try {
    			initiateAndLoadCampaignDataService.sendNoChangeNotifications(campaignDetails.getId(),"campaign-not-found",fromExtension,domain,campaignName,organization);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return false;
    	}
    	else
    	{
    		boolean isEnable = campaign.isIsenabled();
        	
        	if(isEnable)
        	{
        		System.out.println("Campaign is enabled");
        		
        		int isActive = activateCampaignByOrganization(campaignDetails.getId(), campaignDetails.getOrganization());
        		
        		if(isActive == 1 )
            	{
        			campaign.setIsactive(true);
        			System.out.println("Started campaign. Its active now");
        			toReturn = initiateAutodialing(campaign, fromExtension, domain);
        			
        			if(toReturn)
        			{
        				try {
            				System.out.println("Sending notification");
            				sendCampaignNotifications(campaignDetails.getOrganization(),"start", campaignDetails,campaignDetails.getId());
            			} catch (Exception e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            			}
        			}
        			else
        			{
        				System.out.println("Deactivate Campaign now as it is required to be reset");
                		deactivateCampaignByOrganization(campaignDetails.getId(), campaignDetails.getOrganization());
                		
        			}

        			try {
        				//Logs
        				//Campaign ID and Campaign
        				System.out.println("Logs : activeCampaigns size : "+StartedCampaignData.workOnAllActiveCampaigns(null, null, "get").size());
            			//Customer Phone Number and Campaign it is into
            			System.out.println("Logs : allActiveCustomersPhoneNumbersAndItsCampaign size : "+StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(null, null, "get").size());
            			//Extension , Campaign ID's
            			System.out.println("Logs : allActiveExtensionsAndTheirCampaign size : "+StartedCampaignData.workOnAllActiveExtensionsAndTheirCampaign(null, null, "get").size());
            			//Employee Phone , Extension
            			System.out.println("Logs : allActivePhoneAndTheirExtensions size : "+StartedCampaignData.workOnAllActivePhoneAndTheirExtensions(null, null, "get").size());

        			}
        			catch(Exception e)
        			{
        				e.printStackTrace();
        			}
        			
            	}
            	else
            	{
            		return false;
            	}
        		
        	}
        	else
        	{
        		try {
        			System.out.println("Campaign was not enabled. Cannot start.");
        			initiateAndLoadCampaignDataService.sendNoChangeNotifications(campaign.getId(),"activate/deactivate",fromExtension,domain,campaign.getName(),campaign.getOrganization());
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        		
        		return false;
        	}
    	}
		
		return toReturn;	
    }
   
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param campaignDetails details of campaign
     * @return enable user account
     */
    public boolean stopCampaignByOrganization(CampaignDTO campaignDetails, String fromExtension, String domain) {

    	boolean toReturn = true;
    	
    	System.out.println("stopCampaignByOrganization");
    	
    	String campaignName = campaignDetails.getName();
    	String organization = campaignDetails.getOrganization();
    	
    	Campaign campaign = campaignRepository.getCampaignByIdAndOrganization(campaignDetails.getId(), campaignDetails.getOrganization());
    	
		if(campaign==null)
    	{
    		try {
    			System.out.println("Campaign was not found");
    			initiateAndLoadCampaignDataService.sendNoChangeNotifications(campaignDetails.getId(),"campaign-not-found",fromExtension,domain,campaignName,organization);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return false;
    	}
    	else
    	{
    		boolean isEnable = campaign.isIsenabled();
        	
        	if(isEnable)
        	{
        		System.out.println("Campaign is enabled");
        		
        		System.out.println("Campaign is not deactivated / stopped");
    			toReturn = this.stopCampaign(campaign, fromExtension, domain);	
    			
    			if(toReturn)
    			{
        			try {
        				System.out.println("sending notification");
        				sendCampaignNotifications(campaignDetails.getOrganization(),"stop", campaignDetails,campaignDetails.getId());
        			} catch (Exception e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
        			
    			}
    			
    			try {
    				//Logs
    				//Campaign ID and Campaign
    				System.out.println("Logs : activeCampaigns size : "+StartedCampaignData.workOnAllActiveCampaigns(null, null, "get").size());
        			//Customer Phone Number and Campaign it is into
        			System.out.println("Logs : allActiveCustomersPhoneNumbersAndItsCampaign size : "+StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(null, null, "get").size());
        			//Extension , Campaign ID's
        			System.out.println("Logs : allActiveExtensionsAndTheirCampaign size : "+StartedCampaignData.workOnAllActiveExtensionsAndTheirCampaign(null, null, "get").size());
        			//Employee Phone , Extension
        			System.out.println("Logs : allActivePhoneAndTheirExtensions size : "+StartedCampaignData.workOnAllActivePhoneAndTheirExtensions(null, null, "get").size());

    			}
    			catch(Exception e)
    			{
    				e.printStackTrace();
    			}
        	}
        	else
        	{
        		try {
        			System.out.println("Campaign is disabled. Cannot be deactivated");
        			initiateAndLoadCampaignDataService.sendNoChangeNotifications(campaign.getId(),"activate/deactivate",fromExtension,domain,campaign.getName(),campaign.getOrganization());
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        		
        		return false;
        	}	
    	}
		
		return toReturn;
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param campaignDetails details of campaign
     * @return enable user account
     * @throws Exception 
     */
    public boolean resetCampaignByOrganization(CampaignDTO campaignDetails, String fromExtension, String domain) throws Exception {

    	boolean toReturn = true;
    	
    	System.out.println("resetCampaignByOrganization");
    	
    	String campaignName = campaignDetails.getName();
    	String organization = campaignDetails.getOrganization();
    	
    	Campaign campaign = campaignRepository.getCampaignByIdAndOrganization(campaignDetails.getId(), campaignDetails.getOrganization());
    	
		if(campaign==null)
    	{
			try {
				System.out.println("campaign was not found");
				initiateAndLoadCampaignDataService.sendNoChangeNotifications(campaignDetails.getId(),"campaign-not-found",fromExtension,domain,campaignName,organization);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return false;
    	}
    	else
    	{
        	boolean isActive = campaign.isIsactive();
        	
        	if(isActive)
        	{
        		System.out.println("campaign is active cannot reset.");
        		try {
        			System.out.println("sending notification.");
        			initiateAndLoadCampaignDataService.sendNoChangeNotifications(campaign.getId(),"cannot-reset",fromExtension,domain,campaign.getName(),campaign.getOrganization());
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        		return false;
        	}
        	else
        	{
        		System.out.println("campaign reset started.");
        		toReturn = initiateAndLoadCampaignDataService.triggerResetCampaign(campaign, fromExtension, domain);

				if(toReturn)
				{
					try {
						System.out.println("sending notification.");
						sendCampaignNotifications(campaignDetails.getOrganization(),"reset", campaignDetails,campaignDetails.getId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
        	}
    	}
    	return toReturn;
    }
    
    
    public boolean stopCampaign(Campaign campaign, String fromExtension, String domain)
    {
      	boolean toReturn = true;
      	
      	System.out.println("initiateAutodialing.");
      	toReturn = initiateAndLoadCampaignDataService.triggerStopCampaign(campaign, fromExtension, domain,false,"stopped");	
    	return toReturn;
    }
    
    public boolean initiateAutodialing(Campaign campaign, String fromExtension, String domain) throws Exception
    {
      	boolean toReturn = true;
        String returnValue;
      	System.out.println("initiateAutodialing.");
      	returnValue = initiateAndLoadCampaignDataService.triggerStartCampaign(campaign, fromExtension, domain);

      	if(returnValue.equals("true"))
		 toReturn = new LoopInToDialOrSendMessage().initiateDialer(campaign,applicationContext);
		else
			throw new Exception(returnValue);
      	
    	return toReturn;
    }
    
   
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param campaignDetails details of campaign
     * @return enable user account
     */
    public boolean unpauseCampaignByOrganization(CampaignDTO campaignDetails, String fromExtension, String domain) {
    	
    	System.out.println("unpauseCampaignByOrganization");
    	
    	Campaign campaign = campaignRepository.getCampaignByIdAndOrganization(campaignDetails.getId(), campaignDetails.getOrganization());
    	
		if(campaign==null)
    	{
    		return false;
    	}
    	else
    	{
        	boolean isActive = campaign.isIsactive();
        	
        	if(isActive)
        	{
        		System.out.println("Campign is running. Cannot enable / disable.");
        		
        		try {
        			System.out.println("sending notification");
        			initiateAndLoadCampaignDataService.sendNoChangeNotifications(campaign.getId(),"pause/unpause",fromExtension,domain,campaign.getName(),campaign.getOrganization());
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        		return false;
        	}
        	else
        	{
        		System.out.println("Enabling campaign");
            	int isEnable = enableCampaignByOrganization(campaignDetails.getId(), campaignDetails.getOrganization());
            	if(isEnable == 1 )
            	{
            		System.out.println("Campaign is enabled");
            		return true;
            	}
            	else
            	{
            		System.out.println("Error while enabling campaign");
            		return false;
            	}
        	}
    	}
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param campaignDetails details of campaign
     * @return enable user account
     */
    public boolean pauseCampaignByOrganization(CampaignDTO campaignDetails, String fromExtension, String domain) {
    	
    	System.out.println("pauseCampaignByOrganization");
    	
    	Campaign campaign = campaignRepository.getCampaignByIdAndOrganization(campaignDetails.getId(), campaignDetails.getOrganization());
    	
		if(campaign==null)
    	{
    		return false;
    	}
    	else
    	{
        	boolean isActive = campaign.isIsactive();
        	
        	if(isActive)
        	{
        		System.out.println("Campaign is running. It cannot be disabled");
        		
        		try {
        			System.out.println("sending notification");
        			initiateAndLoadCampaignDataService.sendNoChangeNotifications(campaign.getId(),"pause/unpause",fromExtension,domain,campaign.getName(),campaign.getOrganization());
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        		return false;
        	}
        	else
        	{
        		System.out.println("disabling campaign");
        		int isDisable = disableCampaignByOrganization(campaignDetails.getId(), campaignDetails.getOrganization());
            	if(isDisable == 1 )
            	{
            		System.out.println("Campaign disabled");
            		return true;
            	}
            	else
            	{
            		System.out.println("Error while disabling campaign");
            		return false;
            	}
        	}
    	}
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param id id of campaign
     * @return enable user account
     */
    public int updateLastCustomerNumberByCampaignAndOrganization(int lastCustomerNumber,Long id,String organization) {
        return campaignRepository.updateLastCustomerNumberByCampaignAndOrganization(lastCustomerNumber,id,organization);
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param id id of campaign
     * @return enable user account
     */
    public int activateCampaignByOrganization(Long id,String organization) {
        return campaignRepository.activateCampaignByOrganization(id,organization);
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param id id of campaign
     * @return enable user account
     */
    public int deactivateCampaignByOrganization(Long id,String organization) {
        return campaignRepository.deactivateCampaignByOrganization(id,organization);
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param id id of campaign
     * @return enable user account
     */
    public int enableCampaignByOrganization(Long id,String organization) {
        return campaignRepository.enableCampaignByOrganization(id,organization);
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param id id of campaign
     * @return enable user account
     */
    public int disableCampaignByOrganization(Long id,String organization) {
        return campaignRepository.disableCampaignByOrganization(id,organization);
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param campaignDetails details of campaign
     * @return enable user account
     * @throws Exception 
     */
    public boolean createCampaignByOrganization(CampaignDTO campaignDetails) throws Exception {
    	
    	Campaign current = campaignRepository.getCampaignByNameAndOrganization(campaignDetails.getName(),campaignDetails.getOrganization());
    	
    	//System.out.println(campaignDetails.managerId);
    	
    	//System.out.println("***********************************");
    	//System.out.println("***********************************");
    	//System.out.println("***********************************");
    	//System.out.println("***********************************");
    	
    	//System.out.println(current.getManager().getId());
    	
    	if(current==null)
    	{
    		current = campaignMapper.mapDTOToCampaign(campaignDetails);
    		campaignRepository.save(current);
    		try {
				sendCampaignNotifications(campaignDetails.getOrganization(),"create", campaignDetails,campaignDetails.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else
    	{
    		
    		
    		return false;
    	}
    	
        return true;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param campaignDetails details of campaign
     * @return enable user account
     */
    public boolean updateCampaignByOrganization(CampaignDTO campaignDetails, String fromExtension, String domain) {
    	
    	Campaign current = campaignRepository.getCampaignByIdAndOrganization(campaignDetails.getId(), campaignDetails.getOrganization());
    	
		if(current==null)
    	{
    		return false;
    	}
    	else
    	{
    		boolean isActive = current.isIsactive();
        	
        	if(isActive)
        	{
        		try {
        			initiateAndLoadCampaignDataService.sendNoChangeNotifications(current.getId(),"cannotupdate",fromExtension,domain,current.getName(),current.getOrganization());
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        		return false;
        	}
        	else
        	{
        		try
        		{
        			Employee manager = employeeRepository.findById(campaignDetails.getManagerId()).get();

        			current.setDomain(campaignDetails.getDomain());
        			current.setOrganization(campaignDetails.getOrganization());
        			current.setPhonecontext(campaignDetails.getPhonecontext());
        			current.setBusiness(campaignDetails.getBusiness());
          			current.setCountry(campaignDetails.getCountry());
          			current.setCronremindercalling(campaignDetails.getCronremindercalling());
          			current.setDescription(campaignDetails.getDescription());
          			current.setRemindercalling(campaignDetails.isRemindercalling());
          			current.setAutodialertype(campaignDetails.getAutodialertype());
          			current.setIsonmobile(campaignDetails.isIsonmobile());
          			current.setManager(manager);
          			current.setName(campaignDetails.getName());
          			current.setStartdate(campaignDetails.getStartdate());
          			current.setEnddate(campaignDetails.getEnddate());
          			current.setIsactive(campaignDetails.isIsactive());
          			current.setLastCustomerNumber(campaignDetails.getLastCustomerNumber());
          			current.setBreathingSeconds(campaignDetails.getBreathingSeconds());
          			current.setIvrExtension(campaignDetails.getIvrExtension());
          			current.setConfExtension(campaignDetails.getConfExtension());
          			current.setQueueExtension(campaignDetails.getQueueExtension());
          			current.setCallLimit(campaignDetails.getCallLimit());
          			current.setParallelLines(campaignDetails.getParallelLines());
          			current.setAiApplicationName(campaignDetails.getAiApplicationName());
          			current.setAiApplicationDomain(campaignDetails.getAiApplicationDomain());
          			current.setWhatsAppNumber(campaignDetails.getWhatsAppNumber());

            		campaignRepository.save(current);
        		}
        		catch(Exception e)
        		{
        			e.printStackTrace();
        			System.out.println("Exception while updating employee");
        			return false;
        		}	
        	}
    		
    	}

        return true;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param id id of campaign
     * @return enable user account
     * @throws Exception 
     */
    public boolean deleteCampaignByIdAndOrganization(Long id, String organization, String fromExtension, String domain) throws Exception {
    	
    	Campaign current = campaignRepository.getCampaignByIdAndOrganization(id,organization);
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{    		
    		boolean isActive = current.isIsactive();
        	
        	if(isActive)
        	{
        		try {
        			initiateAndLoadCampaignDataService.sendNoChangeNotifications(current.getId(),"cannotdelete",fromExtension,domain,current.getName(),organization);
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        		return false;
        	}
        	else
        	{
        		try {
        			CampaignDTO dto = campaignMapper.mapCampaignToDTO(current);
        			campaignRepository.delete(current);
        			sendCampaignNotifications(organization,"delete", dto, id);
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        	}
    	}
    	
        return true;
    }
    
    
    public CampaignDTO getCampaignByIdAndOrganization(Long id,String organization) {
    	return campaignMapper.mapCampaignToDTO(campaignRepository.getCampaignByIdAndOrganization(id, organization));
    }
    
    public Campaign findCampaignByIdAndOrganization(Long id,String organization) {
    	return campaignRepository.getCampaignByIdAndOrganization(id, organization);
    }
    
    public CampaignDTO getCampaignByNameAndOrganization(String name,String organization) {
    	return campaignMapper.mapCampaignToDTO(campaignRepository.getCampaignByNameAndOrganization(name, organization));
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<CampaignDTO> findAllByOrganization(String organization){
        return campaignRepository.findAllByOrganization(organization)
                .stream()
                .map(campaignMapper::mapCampaignToDTO)
                .collect(Collectors.toList());
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<CampaignDTO> findAllByManagerAndOrganization(Long id,String organization){
    	
    	Employee manager = employeeRepository.findById(id).get();
    	List<CampaignDTO> toReturn = null;
    	
    	if(manager == null)
    	{
    		return toReturn;
    	}
    	else
    	{
            return campaignRepository.findAllByManagerAndOrganization(manager,organization)
                    .stream()
                    .map(campaignMapper::mapCampaignToDTO)
                    .collect(Collectors.toList());
    	}

    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<CampaignDTO> findAllByCountryAndOrganization(String country,String organization){
        return campaignRepository.findAllByCountryAndOrganization(country,organization)
                .stream()
                .map(campaignMapper::mapCampaignToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<CampaignDTO> findAllByBusinessAndOrganization(String business,String organization){
        return campaignRepository.findAllByBusinessAndOrganization(business,organization)
                .stream()
                .map(campaignMapper::mapCampaignToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<CampaignDTO> findAllByPhonecontextAndOrganization(String phonecontext,String organization){
        return campaignRepository.findAllByPhonecontextAndOrganization(phonecontext,organization)
                .stream()
                .map(campaignMapper::mapCampaignToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<CampaignDTO> findAllByIsonmobileAndOrganization(boolean isonmobile,String organization){
        return campaignRepository.findAllByIsonmobileAndOrganization(isonmobile,organization)
                .stream()
                .map(campaignMapper::mapCampaignToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<CampaignDTO> findAllByAutodialertypeAndOrganization(String autodialertype,String organization){
        return campaignRepository.findAllByAutodialertypeAndOrganization(autodialertype,organization)
                .stream()
                .map(campaignMapper::mapCampaignToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<CampaignDTO> findAllByStartdateGreaterThanEqualAndOrganization(Date startdate,String organization){
       
    	//System.out.println(String.valueOf(startdate));
    	//System.out.println(String.valueOf(organization));
    	
    	return campaignRepository.findAllByStartdateGreaterThanEqualAndOrganization(startdate,organization)
                .stream()
                .map(campaignMapper::mapCampaignToDTO)
                .collect(Collectors.toList());
    }
    
    
    
    
    /**
     * The purpose of the method is to set the details of the
     * excel file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Campaign_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Campaign> campaignList = campaignRepository.findAll();

        ExportCampaignToXLSX exporter = new ExportCampaignToXLSX(campaignList);
        exporter.export(response);
    }

    
    /**
     * The purpose of the method is to set the details of the
     * excel file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToExcelOnOrganization(String organization,HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Campaign_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Campaign> campaignList = campaignRepository.findAllByOrganization(organization);

        ExportCampaignToXLSX exporter = new ExportCampaignToXLSX(campaignList);
        exporter.export(response);
    }
    
    /**
     * The purpose of the method is to set the details of the
     * pdf file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Campaign_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Campaign> campaignList = campaignRepository.findAll();

        ExportCampaignToPDF exporter = new ExportCampaignToPDF(campaignList);
        exporter.export(response);
    }
    
    
    /**
     * The purpose of the method is to set the details of the
     * pdf file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToPDFOnOrganization(String organization,HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Campaign_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Campaign> campaignList = campaignRepository.findAllByOrganization(organization);

        ExportCampaignToPDF exporter = new ExportCampaignToPDF(campaignList);
        exporter.export(response);
    }
    
   
    /**
     * The purpose of the method is to send notifications
     */
    public void sendCampaignNotifications(String organization,String type, CampaignDTO campaignDetails, Long id) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();
    	List<EmployeeToCampaignDTO> toSend;
    	Employee manager;
		List<Employee> campaignMembers = new ArrayList<Employee>();
		
    	if(campaignDetails != null)
    	{
		  //Do nothing
    	}
    	else
    	{
    		campaignDetails = campaignMapper.mapCampaignToDTO(campaignRepository.getOne(id));
    	}
    	
    	 toSend = employeeToCampaignService.findAllByCampaignAndOrganization(campaignDetails.getId(), organization);
		 manager = employeeRepository.getOne(campaignDetails.getManagerId());
		 
		 if(toSend != null) {
		 toSend.forEach(
	 	            (employeeToCampaignDTO) -> {
	 	            	campaignMembers.add(employeeRepository.getOne(employeeToCampaignDTO.employeeid));
	 	            });
		 }
		 else {
			 System.out.println("Campaign does not have employee to send notification yet.");
		 }
		 
		 String campaignName = campaignDetails.getName();
		   /*
		    * 
		    * Example on how to set up notification
		    * 
		    *  { alertType: 'alert-success', title: 'Well done!' , message: 'You successfullyread this important.'},
			  { alertType: 'alert-info', title: 'Heads up!', message: 'This alert needs your attention, but it\'s not super important.' },
			  { alertType: 'alert-warning', title: 'Warning!', message: 'Better check yourself, you\'re not looking too good.' },
			  { alertType: 'alert-danger', title: 'Oh snap!', message: 'Change a few things up and try submitting again.' },
			  { alertType: 'alert-primary', title: 'Good Work!', message: 'You completed the training number 23450 well.' },

		    */
		
		BotInputDTO msg = new BotInputDTO();
		Notification managerNotification;
		List<Notification> allNotifications = new ArrayList<Notification>();

    	switch(type)
    	{
	    	   
    	   case "create": 
    		   
    		   if(manager != null)
    		   {
    			   managerNotification = new Notification();
        		   managerNotification.setCreationDate(new Date());
        		   managerNotification.setAlertType("alert-success");
        		   managerNotification.setForExtension(manager.getExtension());
        		   managerNotification.setMessage("'"+campaignDetails.getName()+"' created campaign successfully.");
        		   managerNotification.setNotificationType("campign");
        		   managerNotification.setOrganization(organization);
        		   managerNotification.setTitle("Created");
        		
        		   allNotifications.add(managerNotification);
        		   
        		   msg = new BotInputDTO();
    	    	   msg.setDomain(manager.getDomain());
    	    	   msg.setExtension(manager.getExtension());
    	    	   msg.setFormat("json");
    	    	   msg.setMessage(mapper.writeValueAsString(managerNotification));
    	    	   msg.setMessagetype("notification");
    	    	   msg.setOrganization(manager.getOrganization());
    		    	try {
    			       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
    			    }
    			    catch(Exception e)
    			    {
    				   e.printStackTrace();
    			    }
    		   }
    		   
    		   if(campaignMembers != null)
    		   {
    			   campaignMembers.forEach(
    	       	            (employee) -> {
    	       	            	if(employee.getExtension().equals(manager.getExtension()))
    	       	            	{
    	       	            		
    	       	            	}
    	       	            	else
    	       	            	{
        	       	            	Notification participantNotification = new Notification();
        	       	            	participantNotification.setCreationDate(new Date());
        	       	            	participantNotification.setAlertType("alert-info");
        		       	    		participantNotification.setForExtension(employee.getExtension());
        		       	    		participantNotification.setMessage("You have been added to '"+campaignName+"' campaign successfully.");
        		       	    		participantNotification.setNotificationType("campign");
        		       	    		participantNotification.setOrganization(organization);
        		       	    		participantNotification.setTitle("Got-One!");
        		       	    		allNotifications.add(participantNotification);
        		       	    		
        		       	    		BotInputDTO msgParticipant = new BotInputDTO();
        		       	    		msgParticipant.setDomain(employee.getDomain());
        		       	    		msgParticipant.setExtension(employee.getExtension());
        		       	    		msgParticipant.setFormat("json");
        		       	    		try {
        								msgParticipant.setMessage(mapper.writeValueAsString(participantNotification));
        							} catch (JsonProcessingException e) {
        								// TODO Auto-generated catch block
        								e.printStackTrace();
        							}
        		       	    		msgParticipant.setMessagetype("notification");
        		       	    		msgParticipant.setOrganization(employee.getOrganization());
        		       		    	try {
        		      		       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msgParticipant);
	        		      		    }
	        		      		    catch(Exception e)
	        		      		    {
	        		      			   e.printStackTrace();
	        		      		    }
    	       	            	}
    	       	            }); 
    		   }
    		   
    		   notificationRepository.saveAll(allNotifications);
    		   
    		      
    	   break;
    	   
    	   case "issue-while-refresh-backend-connection": 
    		   
//    		   System.out.println("User Limit exceeded notification");
    		   
    		   if(manager != null)
    		   {
    			   managerNotification = new Notification();
        		   managerNotification.setCreationDate(new Date());
        		   managerNotification.setAlertType("alert-danger");
        		   managerNotification.setForExtension(manager.getExtension());
        		   managerNotification.setMessage("'"+campaignDetails.getName()+"' campaign failed. Backend connections not setup.");
        		   managerNotification.setNotificationType("campign");
        		   managerNotification.setOrganization(organization);
        		   managerNotification.setTitle("Refresh!");
        		
        		   allNotifications.add(managerNotification);
        		   
        		   msg = new BotInputDTO();
    	    	   msg.setDomain(manager.getDomain());
    	    	   msg.setExtension(manager.getExtension());
    	    	   msg.setFormat("json");
    	    	   msg.setMessage(mapper.writeValueAsString(managerNotification));
    	    	   msg.setMessagetype("notification");
    	    	   msg.setOrganization(manager.getOrganization());
	   		       try {
				       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
				    }
				    catch(Exception e)
				    {
					   e.printStackTrace();
				    }
    		   }
    		    		   
    		   notificationRepository.saveAll(allNotifications);
    		   break;
    		   
    	   case "user-limit-exceed": 
    		   
//    		   System.out.println("User Limit exceeded notification");
    		   
    		   if(manager != null)
    		   {
    			   managerNotification = new Notification();
        		   managerNotification.setCreationDate(new Date());
        		   managerNotification.setAlertType("alert-danger");
        		   managerNotification.setForExtension(manager.getExtension());
        		   managerNotification.setMessage("'"+campaignDetails.getName()+"' campaign cannot start. Exceed user Limit");
        		   managerNotification.setNotificationType("campign");
        		   managerNotification.setOrganization(organization);
        		   managerNotification.setTitle("Information!");
        		
        		   allNotifications.add(managerNotification);
        		   
        		   msg = new BotInputDTO();
    	    	   msg.setDomain(manager.getDomain());
    	    	   msg.setExtension(manager.getExtension());
    	    	   msg.setFormat("json");
    	    	   msg.setMessage(mapper.writeValueAsString(managerNotification));
    	    	   msg.setMessagetype("notification");
    	    	   msg.setOrganization(manager.getOrganization());
	   		       try {
				       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
				    }
				    catch(Exception e)
				    {
					   e.printStackTrace();
				    }
    		   }
    		    		   
    		   notificationRepository.saveAll(allNotifications);
    		   break;
    		   
    	   case "start": 
    		   
    		   if(manager != null)
    		   {
    			   managerNotification = new Notification();
        		   managerNotification.setCreationDate(new Date());
        		   managerNotification.setAlertType("alert-primary");
        		   managerNotification.setForExtension(manager.getExtension());
        		   managerNotification.setMessage("'"+campaignDetails.getName()+"' campaign started.");
        		   managerNotification.setNotificationType("campign");
        		   managerNotification.setOrganization(organization);
        		   managerNotification.setTitle("Information!");
        		
        		   allNotifications.add(managerNotification);
        		   
        		   msg = new BotInputDTO();
    	    	   msg.setDomain(manager.getDomain());
    	    	   msg.setExtension(manager.getExtension());
    	    	   msg.setFormat("json");
    	    	   msg.setMessage(mapper.writeValueAsString(managerNotification));
    	    	   msg.setMessagetype("notification");
    	    	   msg.setOrganization(manager.getOrganization());
    	    	   try {
				       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
				    }
				    catch(Exception e)
				    {
					   e.printStackTrace();
				    }
    		   }
    		   
    		   
    		   
    		   if(campaignMembers != null)
    		   {
    			   campaignMembers.forEach(
    	       	            (employee) -> {
    	       	            	
    	       	            	if(employee.getExtension().equals(manager.getExtension()))
    	       	            	{
    	       	            		
    	       	            	}
    	       	            	else
    	       	            	{
    	       	            		Notification participantNotification = new Notification();
        	       	            	participantNotification.setCreationDate(new Date());
        	       	            	participantNotification.setAlertType("alert-primary");
        		       	    		participantNotification.setForExtension(employee.getExtension());
        		       	    		participantNotification.setMessage("'"+campaignName+"' campaign started.");
        		       	    		participantNotification.setNotificationType("campign");
        		       	    		participantNotification.setOrganization(organization);
        		       	    		participantNotification.setTitle("Information!");
        		       	    		allNotifications.add(participantNotification);
        		       	    		
        		       	    		BotInputDTO msgParticipant = new BotInputDTO();
        		       	    		msgParticipant.setDomain(employee.getDomain());
        		       	    		msgParticipant.setExtension(employee.getExtension());
        		       	    		msgParticipant.setFormat("json");
        		       	    		try {
        								msgParticipant.setMessage(mapper.writeValueAsString(participantNotification));
        							} catch (JsonProcessingException e) {
        								// TODO Auto-generated catch block
        								e.printStackTrace();
        							}
        		       	    		msgParticipant.setMessagetype("notification");
        		       	    		msgParticipant.setOrganization(employee.getOrganization());
        		       	    		try {
          		      		       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msgParticipant);
  	        		      		    }
  	        		      		    catch(Exception e)
  	        		      		    {
  	        		      			   e.printStackTrace();
  	        		      		    }
        		        		   
    	       	            	}
   
    	       	            });
    		   }
    		   
    		   
    		   
    		   notificationRepository.saveAll(allNotifications);
    		   
    		   break;
    	   
    	   
    	   case "stop": 
    		   
    		   if(manager != null)
    		   {
    			   managerNotification = new Notification();
        		   managerNotification.setCreationDate(new Date());
        		   managerNotification.setAlertType("alert-primary");
        		   managerNotification.setForExtension(manager.getExtension());
        		   managerNotification.setMessage("'"+campaignDetails.getName()+"' campaign stopped.");
        		   managerNotification.setNotificationType("campign");
        		   managerNotification.setOrganization(organization);
        		   managerNotification.setTitle("Information!");
        		
        		   allNotifications.add(managerNotification);
        		   
        		   msg = new BotInputDTO();
    	    	   msg.setDomain(manager.getDomain());
    	    	   msg.setExtension(manager.getExtension());
    	    	   msg.setFormat("json");
    	    	   msg.setMessage(mapper.writeValueAsString(managerNotification));
    	    	   msg.setMessagetype("notification");
    	    	   msg.setOrganization(manager.getOrganization());
    	    	   try {
				       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
				    }
				    catch(Exception e)
				    {
					   e.printStackTrace();
				    }
    		   }
    		   
    		  
    		   
    		   if(campaignMembers != null)
    		   {
    			   campaignMembers.forEach(
    	       	            (employee) -> {
    	       	            	
    	       	            	if(employee.getExtension().equals(manager.getExtension()))
    	       	            	{
    	       	            		
    	       	            	}
    	       	            	else
    	       	            	{
    	       	            		Notification participantNotification = new Notification();
        	       	            	participantNotification.setCreationDate(new Date());
        	       	            	participantNotification.setAlertType("alert-primary");
        		       	    		participantNotification.setForExtension(employee.getExtension());
        		       	    		participantNotification.setMessage("'"+campaignName+"' campaign stopped.");
        		       	    		participantNotification.setNotificationType("campign");
        		       	    		participantNotification.setOrganization(organization);
        		       	    		participantNotification.setTitle("Information!");
        		       	    		allNotifications.add(participantNotification);
        		       	    		
        		       	    		BotInputDTO msgParticipant = new BotInputDTO();
        		       	    		msgParticipant.setDomain(employee.getDomain());
        		       	    		msgParticipant.setExtension(employee.getExtension());
        		       	    		msgParticipant.setFormat("json");
        		       	    		try {
        								msgParticipant.setMessage(mapper.writeValueAsString(participantNotification));
        							} catch (JsonProcessingException e) {
        								// TODO Auto-generated catch block
        								e.printStackTrace();
        							}
        		       	    		msgParticipant.setMessagetype("notification");
        		       	    		msgParticipant.setOrganization(employee.getOrganization());
        		       	    		try {
          		      		       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msgParticipant);
  	        		      		    }
  	        		      		    catch(Exception e)
  	        		      		    {
  	        		      			   e.printStackTrace();
  	        		      		    }
    	       	            	}
    	       	            });
    		   }
    		   
    		   
    		   notificationRepository.saveAll(allNotifications);
    		   
    		   break;
    		   
               case "reset":
    		   
             if(manager != null)
        	  {
            	 managerNotification = new Notification();
      		   managerNotification.setCreationDate(new Date());
      		   managerNotification.setAlertType("alert-warning");
      		   managerNotification.setForExtension(manager.getExtension());
      		   managerNotification.setMessage("'"+campaignDetails.getName()+"' campaign is reset.");
      		   managerNotification.setNotificationType("campign");
      		   managerNotification.setOrganization(organization);
      		   managerNotification.setTitle("Done!");
      		
      		   allNotifications.add(managerNotification);
      		   
      		   msg = new BotInputDTO();
  	    	   msg.setDomain(manager.getDomain());
  	    	   msg.setExtension(manager.getExtension());
  	    	   msg.setFormat("json");
  	    	   msg.setMessage(mapper.writeValueAsString(managerNotification));
  	    	   msg.setMessagetype("notification");
  	    	   msg.setOrganization(manager.getOrganization());
	  	       try {
			       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
			    }
			    catch(Exception e)
			    {
				   e.printStackTrace();
			    }
        	  }
            	   
    		  
    		   
    		   if(campaignMembers != null)
    		   {
    			   campaignMembers.forEach(
    	       	            (employee) -> {
    	       	            	
    	       	            	if(employee.getExtension().equals(manager.getExtension()))
    	       	            	{
    	       	            		
    	       	            	}
    	       	            	else
    	       	            	{
        	       	            	Notification participantNotification = new Notification();
        	       	            	participantNotification.setCreationDate(new Date());
        	       	            	participantNotification.setAlertType("alert-info");
        		       	    		participantNotification.setForExtension(employee.getExtension());
        		       	    		participantNotification.setMessage("'"+campaignName+"' campaign is reset.");
        		       	    		participantNotification.setNotificationType("campign");
        		       	    		participantNotification.setOrganization(organization);
        		       	    		participantNotification.setTitle("Looped!");
        		       	    		allNotifications.add(participantNotification);
        		       	    		
        		       	    		BotInputDTO msgParticipant = new BotInputDTO();
        		       	    		msgParticipant.setDomain(employee.getDomain());
        		       	    		msgParticipant.setExtension(employee.getExtension());
        		       	    		msgParticipant.setFormat("json");
        		       	    		try {
        								msgParticipant.setMessage(mapper.writeValueAsString(participantNotification));
        							} catch (JsonProcessingException e) {
        								// TODO Auto-generated catch block
        								e.printStackTrace();
        							}
        		       	    		msgParticipant.setMessagetype("notification");
        		       	    		msgParticipant.setOrganization(employee.getOrganization());
        		       	    		try {
          		      		       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msgParticipant);
  	        		      		    }
  	        		      		    catch(Exception e)
  	        		      		    {
  	        		      			   e.printStackTrace();
  	        		      		    }
    	       	            	}		   
    	       	            }); 
    		   }
    		   
    		   
    		   notificationRepository.saveAll(allNotifications);
    		   
    		   break;
    		   
    	   case "delete":
    		   
    		   if(manager != null)
    		   {
    			   managerNotification = new Notification();
        		   managerNotification.setCreationDate(new Date());
        		   managerNotification.setAlertType("alert-danger");
        		   managerNotification.setForExtension(manager.getExtension());
        		   managerNotification.setMessage("'"+campaignDetails.getName()+"' campaign deleted.");
        		   managerNotification.setNotificationType("campign");
        		   managerNotification.setOrganization(organization);
        		   managerNotification.setTitle("Shap!");
        		
        		   allNotifications.add(managerNotification);
        		   
        		   msg = new BotInputDTO();
    	    	   msg.setDomain(manager.getDomain());
    	    	   msg.setExtension(manager.getExtension());
    	    	   msg.setFormat("json");
    	    	   msg.setMessage(mapper.writeValueAsString(managerNotification));
    	    	   msg.setMessagetype("notification");
    	    	   msg.setOrganization(manager.getOrganization());
    	    	   try {
				       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
				    }
				    catch(Exception e)
				    {
					   e.printStackTrace();
				    }
    		   }

    		   if(campaignMembers != null)
    		   {
    			   campaignMembers.forEach(
    	       	            (employee) -> {
    	       	            	
    	       	            	if(employee.getExtension().equals(manager.getExtension()))
    	       	            	{
    	       	            		
    	       	            	}
    	       	            	else
    	       	            	{
    	       	            		Notification participantNotification = new Notification();
        	       	            	participantNotification.setCreationDate(new Date());
        	       	            	participantNotification.setAlertType("alert-warning");
        		       	    		participantNotification.setForExtension(employee.getExtension());
        		       	    		participantNotification.setMessage("'"+campaignName+"' campaign deleted.");
        		       	    		participantNotification.setNotificationType("campign");
        		       	    		participantNotification.setOrganization(organization);
        		       	    		participantNotification.setTitle("Information!");
        		       	    		allNotifications.add(participantNotification);
        		       	    		
        		       	    		BotInputDTO msgParticipant = new BotInputDTO();
        		       	    		msgParticipant.setDomain(employee.getDomain());
        		       	    		msgParticipant.setExtension(employee.getExtension());
        		       	    		msgParticipant.setFormat("json");
        		       	    		try {
        								msgParticipant.setMessage(mapper.writeValueAsString(participantNotification));
        							} catch (JsonProcessingException e) {
        								// TODO Auto-generated catch block
        								e.printStackTrace();
        							}
        		       	    		msgParticipant.setMessagetype("notification");
        		       	    		msgParticipant.setOrganization(employee.getOrganization());
        		       	    		try {
          		      		       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msgParticipant);
  	        		      		    }
  	        		      		    catch(Exception e)
  	        		      		    {
  	        		      			   e.printStackTrace();
  	        		      		    }
    	       	            	}
    	       	            });
    		   }
    		   
    		   break;
    	   	   default:
    		   break;
    	}
		   
		   
	   notificationRepository.saveAll(allNotifications);
    }
    
}
