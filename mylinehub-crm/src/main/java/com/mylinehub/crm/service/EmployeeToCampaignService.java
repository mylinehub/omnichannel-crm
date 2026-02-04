package com.mylinehub.crm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.EmployeeToCampaign;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.entity.dto.EmployeeToCampaignDTO;
import com.mylinehub.crm.mapper.EmployeeToCampaignMapper;
import com.mylinehub.crm.repository.CampaignRepository;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.EmployeeToCampaignRepository;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;
import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class EmployeeToCampaignService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final EmployeeToCampaignRepository employeeToCampaignRepository;
    private final EmployeeRepository employeeRepository;
    private final CampaignRepository campaignRepository;
    private final EmployeeToCampaignMapper employeeToCampaignMapper;
    private final NotificationRepository notificationRepository;
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<EmployeeToCampaignDTO> findAllByEmployeeAndOrganization(String extension, String organization){
    	
        Employee current = employeeRepository.findByExtensionAndOrganization(extension,organization);
    	
    	if(current == null)
    	{
    		return null;
    	}
    	else
    	{
    		 return employeeToCampaignRepository.findAllByEmployeeAndOrganization(current,organization)
    	                .stream()
    	                .map(employeeToCampaignMapper::mapEmployeeToCampaignToDto)
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
    
    public List<EmployeeToCampaignDTO> findAllByCampaignAndOrganization(Long id, String organization){
    	
    	Campaign current = campaignRepository.getCampaignByIdAndOrganization(id, organization);
    	
    	if(current == null)
    	{
    		return null;
    	}
    	else
    	{
    		 return employeeToCampaignRepository.findAllByCampaignAndOrganization(current,organization)
    	                .stream()
    	                .map(employeeToCampaignMapper::mapEmployeeToCampaignToDto)
    	                .collect(Collectors.toList());
    	}  
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int createEmployeeToCampaignByOrganization(List<EmployeeToCampaignDTO> employeesToCampaign, String requestByExtension,String domain) {

    	System.out.println("createEmployeeToCampaignByOrganization");
    	int numberOfRow = 0;
    	
    	List<EmployeeToCampaign> list = new ArrayList<EmployeeToCampaign>();
    	
    	if(employeesToCampaign.size()>0)
		{
    		System.out.println("employeesToCampaign.size() : "+ employeesToCampaign.size());

			Campaign campaign = campaignRepository.findById(employeesToCampaign.get(0).getCampaignid()).get();
			boolean isActive = campaign.isIsactive();
			
			System.out.println("isActive : "+ isActive);
			
			if(isActive)
			{
				System.out.println("Sending cannot create notification");
				try {
					sendNoChangeNotifications("cannotcreate", requestByExtension,domain, campaign.getName(), campaign.getOrganization());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				System.out.println("Campaign is inactive");
				 // Lambda expression printing all elements in a List
		    	employeesToCampaign.forEach(
		            (employeeToCampaign) -> { 
		            	
		            	
		            	Employee currentEmployee = employeeRepository.getOne(employeeToCampaign.getEmployeeid());
		            	
		            	if(currentEmployee != null)
		            	{
		            		System.out.println("Found Employee");
			            	
			                EmployeeToCampaign currentEmployeeToCampaign = employeeToCampaignRepository.findByEmployeeAndCampaignAndOrganization(currentEmployee,campaign,campaign.getOrganization());
			            	
			            	if(currentEmployeeToCampaign != null)
			            	{
			            		System.out.println("currentEmployeeToCampaign is not null");
			            	}
			            	else
			            	{
			            		System.out.println("Inserting Employee");
			            		EmployeeToCampaign insert =  new EmployeeToCampaign();
	                     		
	                     		insert.setCampaign(campaign);
	                     		insert.setEmployee(currentEmployee);
	                     		insert.setOrganization(campaign.getOrganization());
	                     		list.add(insert);
			            	}
		            	}
		            	else
		            	{
		            		System.out.println("Current employee was null");
		            	}
		            });
		    	
		    	numberOfRow = list.size();
		    	employeeToCampaignRepository.saveAll(list);
		    	
		    	try {
		    		System.out.println("Sending notification");
					sendCampaignNotifications("create", list);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

    	return numberOfRow;

    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateEmployeeToCampaignByOrganization(List<EmployeeToCampaignDTO> employeesToCampaign, String requestByExtension,String domain) {

    	int numberOfRow = 0;
    	System.out.println("updateEmployeeToCampaignByOrganization");
    	
    	List<EmployeeToCampaign> list = new ArrayList<EmployeeToCampaign>();
    	
    	if(employeesToCampaign.size()>0)
		{
    		System.out.println("employeesToCampaign.size() : "+ employeesToCampaign.size());
    		Campaign campaign = campaignRepository.findById(employeesToCampaign.get(0).getCampaignid()).get();
			
			boolean isActive = campaign.isIsactive();
			
			if(isActive)
			{
				try {
					sendNoChangeNotifications("cannotupdate", requestByExtension,domain, campaign.getName(), campaign.getOrganization());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{

		        // Lambda expression printing all elements in a List
		    	employeesToCampaign.forEach(
		            (employeeToCampaign) -> { 
		            	
		            	EmployeeToCampaign currentEmployeeToCampaign = employeeToCampaignRepository.findById(employeeToCampaign.getId()).get();
		            	
		            	if(currentEmployeeToCampaign == null)
		            	{
		            		
		            	}
		            	else
		            	{
		            		Employee currentEmployee = currentEmployeeToCampaign.getEmployee();
		                	Campaign currentCampaign = currentEmployeeToCampaign.getCampaign();
		                	
		                	if(currentCampaign == null)
		                	{
		                		// Do not do anything
		                	}
		                	else
		                	{
		                		if(currentEmployee == null)
		                     	{
		                     		// Do nothing again
		                     	}
		                     	else
		                     	{
		                     		if (currentEmployee.getOrganization().trim().equals(currentCampaign.getOrganization().trim()))
		                     		{             		
		                     			currentEmployeeToCampaign.setCampaign(currentCampaign);
		                     			currentEmployeeToCampaign.setEmployee(currentEmployee);
		                     			currentEmployeeToCampaign.setOrganization(currentCampaign.getOrganization());
		                     			
		                        		list.add(currentEmployeeToCampaign);
		                     		}
		                     		else
		                     		{
		                     			// Do nothing
		                     		}
		                     		
		                     	} 	
		                	}
		            	}
		            });
		    	
		    	numberOfRow = list.size();
		    	employeeToCampaignRepository.saveAll(list);
		    	
		     	try {
					sendCampaignNotifications("update", list);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

    	return numberOfRow;

    }
    
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int deleteEmployeeToCampaignByOrganization(List<EmployeeToCampaignDTO> employeesToCampaign, String requestByExtension,String domain) {

    	int numberOfRow = 0;
    	
    	System.out.println("deleteEmployeeToCampaignByOrganization");
    	
    	List<EmployeeToCampaign> list = new ArrayList<EmployeeToCampaign>();
    	
    	if(employeesToCampaign.size()>0)
		{
    		System.out.println("employeesToCampaign.size() : "+ employeesToCampaign.size());	
    		Campaign campaign = campaignRepository.findById(employeesToCampaign.get(0).getCampaignid()).get();
    		
			boolean isActive = campaign.isIsactive();
			
			if(isActive)
			{
				try {
					System.out.println("cannot delete as campaign is running");
					sendNoChangeNotifications("cannotdelete", requestByExtension,domain, campaign.getName(), campaign.getOrganization());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				 // Lambda expression printing all elements in a List
		    	employeesToCampaign.forEach(
		            (employeeToCampaign) -> { 
		            	
		            	EmployeeToCampaign currentEmployeeToCampaign = employeeToCampaignRepository.findById(employeeToCampaign.getId()).get();
		            	if(currentEmployeeToCampaign == null)
		            	{
		            		System.out.println("currentEmployeeToCampaign is null");
		            	}
		            	else
		            	{
		            		list.add(currentEmployeeToCampaign);
		            	}
			
		            });
		    	
		    	System.out.println("deleting all");
		    	numberOfRow = list.size();
		    	employeeToCampaignRepository.deleteAll(list);
				
		    	try {
					sendCampaignNotifications("delete", list);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	
			}
		}
    	
    	return numberOfRow;

    }
    
    
    /**
     * The task of the method is to delete all data related to campaign
     * @param email email of the user
     * @return enable user account
     */
    public boolean deleteAllByCampaignAndOrganization(Campaign campaign, String organization, String requestByExtension,String domain) {
    	boolean result = false;

    	System.out.println("deleteAllByCampaignAndOrganization");
    	
    	List<EmployeeToCampaign> list = employeeToCampaignRepository.findAllByCampaignAndOrganization(campaign, organization);

    	try
    	{
    		if(list.size()>0)
    		{
    			boolean isActive = campaign.isIsactive();
    			
    			if(isActive)
    			{
    				System.out.println("cannot delete as campign is running");
    				sendNoChangeNotifications("cannotdelete", requestByExtension,domain, campaign.getName(), campaign.getOrganization());
    			}
    			else
    			{
    				System.out.println("deleting all");
    				employeeToCampaignRepository.deleteAll(list);
    	    		
    	    		try {
    	    			System.out.println("sending notification");
    	    			sendCampaignNotifications("delete", list);
    	    		} catch (Exception e) {
    	    			// TODO Auto-generated catch block
    	    			e.printStackTrace();
    	    		}
    	    		
    			}
    		}
    		
    		result = true;
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	
    	return result;
    }
    
    
    /**
     * The purpose of the method is to send notifications
     */
    public void sendNoChangeNotifications(String type, String extension,String domain, String campaignName, String organization) throws Exception {
    	
    	ObjectMapper mapper = new ObjectMapper();
		List<Notification> allNotifications = new ArrayList<Notification>();
		
    	BotInputDTO msg;
		Notification notification;
    	switch(type)
    	{
    	   case "cannotcreate": 
    		   
    		   notification = new Notification();
    		   notification.setCreationDate(new Date());
    		   notification.setAlertType("alert-danger");
    		   notification.setForExtension(extension);
    		   notification.setMessage("Campaign '"+campaignName+"' is started. No employee added.");
    		   notification.setNotificationType("campign");
    		   notification.setOrganization(organization);
    		   notification.setTitle("Cannot!");
    		   
    		   msg = new BotInputDTO();
	    	   msg.setDomain(domain);
	    	   msg.setExtension(extension);
	    	   msg.setFormat("json");
	    	   try {
				msg.setMessage(mapper.writeValueAsString(notification));
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	   msg.setMessagetype("notification");
	    	   msg.setOrganization(organization);
		    	try {
			       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
			    }
			    catch(Exception e)
			    {
				   e.printStackTrace();
			    }
    		   allNotifications.add(notification);
    		      
    	   break;
    	   
    	   case "cannotupdate": 
    		   
    		   notification = new Notification();
    		   notification.setCreationDate(new Date());
    		   notification.setAlertType("alert-danger");
    		   notification.setForExtension(extension);
    		   notification.setMessage("Campaign '"+campaignName+"' is started. No employee updated.");
    		   notification.setNotificationType("campign");
    		   notification.setOrganization(organization);
    		   notification.setTitle("Cannot!");
    		   
    		   msg = new BotInputDTO();
	    	   msg.setDomain(domain);
	    	   msg.setExtension(extension);
	    	   msg.setFormat("json");
	    	   try {
				msg.setMessage(mapper.writeValueAsString(notification));
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	   msg.setMessagetype("notification");
	    	   msg.setOrganization(organization);
		    	try {
			       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
			    }
			    catch(Exception e)
			    {
				   e.printStackTrace();
			    }
    		   allNotifications.add(notification);
    		      
    	   break;
    	   
    	   case "cannotdelete": 
	   
		   notification = new Notification();
		   notification.setCreationDate(new Date());
		   notification.setAlertType("alert-danger");
		   notification.setForExtension(extension);
		   notification.setMessage("Campaign '"+campaignName+"' is started. No employee deleted.");
		   notification.setNotificationType("campign");
		   notification.setOrganization(organization);
		   notification.setTitle("Cannot!");
		   
		   msg = new BotInputDTO();
		   msg.setDomain(domain);
		   msg.setExtension(extension);
		   msg.setFormat("json");
		   try {
			msg.setMessage(mapper.writeValueAsString(notification));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   msg.setMessagetype("notification");
		   msg.setOrganization(organization);
	    	try {
		       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
		    }
		    catch(Exception e)
		    {
			   e.printStackTrace();
		    }
		   allNotifications.add(notification);
		      
	       break;
    	   default:
    	   break;
    	}
    	
		notificationRepository.saveAll(allNotifications);
    }
    
    
    /**
     * The purpose of the method is to send notifications
     */
    public void sendCampaignNotifications(String type, List<EmployeeToCampaign> employeesToCampaign) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();
		List<Notification> allNotifications = new ArrayList<Notification>();
		
		if(employeesToCampaign!=null)
		{
			employeesToCampaign.forEach(
					(employeeToCampaign) -> {
						Employee employeeDetails;
				    	Campaign campaignDetails;
				    	employeeDetails = employeeToCampaign.getEmployee();
				    	campaignDetails = employeeToCampaign.getCampaign();
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
				    	BotInputDTO msg;
						Notification notification;
				    	switch(type)
				    	{
				    	   case "create": 
				    		   
				    		   notification = new Notification();
				    		   notification.setCreationDate(new Date());
				    		   notification.setAlertType("alert-success");
				    		   notification.setForExtension(employeeDetails.getExtension());
				    		   notification.setMessage("You were added to '"+campaignDetails.getName()+"' campaign successfully.");
				    		   notification.setNotificationType("campign");
				    		   notification.setOrganization(employeeDetails.getOrganization());
				    		   notification.setTitle("Go-On!");
				    		   
				    		   msg = new BotInputDTO();
					    	   msg.setDomain(employeeDetails.getDomain());
					    	   msg.setExtension(employeeDetails.getExtension());
					    	   msg.setFormat("json");
					    	   try {
								msg.setMessage(mapper.writeValueAsString(notification));
								} catch (JsonProcessingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
					    	   msg.setMessagetype("notification");
					    	   msg.setOrganization(employeeDetails.getOrganization());
						    	try {
							       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
							    }
							    catch(Exception e)
							    {
								   e.printStackTrace();
							    }
				    		   allNotifications.add(notification);
				    		      
				    	   break;
				    	   case "update": 
				    		   
				    		   notification = new Notification();
				    		   notification.setCreationDate(new Date());
				    		   notification.setAlertType("alert-primary");
				    		   notification.setForExtension(employeeDetails.getExtension());
				    		   notification.setMessage("Your details were updated to '"+campaignDetails.getName()+"' campaign successfully.");
				    		   notification.setNotificationType("campign");
				    		   notification.setOrganization(employeeDetails.getOrganization());
				    		   notification.setTitle("Info!");
				    		   
				    		   msg = new BotInputDTO();
					    	   msg.setDomain(employeeDetails.getDomain());
					    	   msg.setExtension(employeeDetails.getExtension());
					    	   msg.setFormat("json");
					    	   try {
								msg.setMessage(mapper.writeValueAsString(notification));
								} catch (JsonProcessingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
					    	   msg.setMessagetype("notification");
					    	   msg.setOrganization(employeeDetails.getOrganization());
						    	try {
							       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
							    }
							    catch(Exception e)
							    {
								   e.printStackTrace();
							    }
				    		   allNotifications.add(notification);
				    		   
				    		   break;
				    	   
				    	   
				    	   case "delete": 
				    		   
				    		   notification = new Notification();
				    		   notification.setCreationDate(new Date());
				    		   notification.setAlertType("alert-danger");
				    		   notification.setForExtension(employeeDetails.getExtension());
				    		   notification.setMessage("You were deleted from '"+campaignDetails.getName()+"' campaign successfully.");
				    		   notification.setNotificationType("campign");
				    		   notification.setOrganization(employeeDetails.getOrganization());
				    		   notification.setTitle("Spare!");
				    		   
				    		   msg = new BotInputDTO();
					    	   msg.setDomain(employeeDetails.getDomain());
					    	   msg.setExtension(employeeDetails.getExtension());
					    	   msg.setFormat("json");
					    	   try {
								msg.setMessage(mapper.writeValueAsString(notification));
								} catch (JsonProcessingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
					    	   msg.setMessagetype("notification");
					    	   msg.setOrganization(employeeDetails.getOrganization());
						    	try {
							       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
							    }
							    catch(Exception e)
							    {
								   e.printStackTrace();
							    }
				    		   allNotifications.add(notification);
				    		   
				    		   break;
					    	   default:
					    		   break;
				    	}
	   	            });
		}
		
		notificationRepository.saveAll(allNotifications);
		
    }
}