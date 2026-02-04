package com.mylinehub.crm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.SshConnection;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.entity.dto.SshConnectionDTO;
import com.mylinehub.crm.enums.USER_ROLE;
import com.mylinehub.crm.mapper.SshMapper;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.repository.SshConnectionRepository;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;
import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class SshConnectionService implements CurrentTimeInterface{

	
	/**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final SshConnectionRepository sshConnectionRepository;
    private final SshMapper sshConnectionMapper;
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
	 /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int enableSshConnectionOnOrganization(Long id,String organization) {
    	int toReturn = sshConnectionRepository.enableSshConnectionByOrganization(id, organization);
        try {
			List<SshConnectionDTO> sshConnectionsDTO = new ArrayList<SshConnectionDTO>();
			sshConnectionsDTO.add(sshConnectionMapper.mapSshConnectionToDTO(sshConnectionRepository.findById(id).get()));
			sendSshNotifications("enabled", sshConnectionsDTO);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return toReturn;
    }

    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int disableSshConnectionOnOrganization(Long id,String organization) {
        int toReturn = sshConnectionRepository.disableSshConnectionByOrganization(id,organization);
        try {
			List<SshConnectionDTO> sshConnectionsDTO = new ArrayList<SshConnectionDTO>();
			sshConnectionsDTO.add(sshConnectionMapper.mapSshConnectionToDTO(sshConnectionRepository.findById(id).get()));
			sendSshNotifications("disabled", sshConnectionsDTO);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return toReturn;
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public boolean createsshConnectionByOrganization(SshConnectionDTO sshConnectionDetails) throws Exception {
    	
    	SshConnection current = sshConnectionRepository.findByDomainAndOrganization(sshConnectionDetails.getDomain(),sshConnectionDetails.getOrganization());
    	
    	if(current==null)
    	{
    		current = sshConnectionMapper.mapDtoToSshConnection(sshConnectionDetails);
    		current = sshConnectionRepository.save(current);
    		try {
				List<SshConnectionDTO> sshConnectionsDTO = new ArrayList<SshConnectionDTO>();
				sshConnectionsDTO.add(sshConnectionMapper.mapSshConnectionToDTO(current));
				sendSshNotifications("create", sshConnectionsDTO);
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
     * @param email email of the user
     * @return enable user account
     */
    public boolean updatesshConnectionByOrganization(SshConnectionDTO sshConnectionDetails) {
    	
    	SshConnection current = sshConnectionRepository.getOne(sshConnectionDetails.getId());
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{
    		
    		try
    		{

    			current.setDomain(sshConnectionDetails.getDomain());
    			current.setActive(sshConnectionDetails.isActive());
    			current.setAuthType(sshConnectionDetails.getAuthType());
    			current.setConnectionString(sshConnectionDetails.getConnectionString());
    			current.setOrganization(sshConnectionDetails.getOrganization());
    			current.setPassword(sshConnectionDetails.getPassword());
    			current.setPemFileLocation(sshConnectionDetails.getPemFileLocation());
    			current.setPemFileName(sshConnectionDetails.getPemFileName());
    			current.setPhonecontext(sshConnectionDetails.getPhonecontext());
    			current.setPort(sshConnectionDetails.getPort());
    			current.setSshHostType(sshConnectionDetails.getSshHostType());
    			current.setSshUser(sshConnectionDetails.getSshUser());
    			current.setType(sshConnectionDetails.getType());
    			current.setPrivateKey(sshConnectionDetails.getPrivateKey());
    			current.setPublicKey(sshConnectionDetails.getPublicKey());
    			current.setExtraKey(sshConnectionDetails.getExtraKey());
    			current = sshConnectionRepository.save(current);
        		try {
					List<SshConnectionDTO> sshConnectionsDTO = new ArrayList<SshConnectionDTO>();
					sshConnectionsDTO.add(sshConnectionMapper.mapSshConnectionToDTO(current));
					sendSshNotifications("update", sshConnectionsDTO);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
     * @throws Exception 
     */
    public boolean deletesSshConnectionByDomainAndOrganization(String domain, String organization) throws Exception {
    	
    	SshConnection current = sshConnectionRepository.findByDomainAndOrganization(domain,organization);
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{    		
    		sshConnectionRepository.delete(current);
    		try {
				List<SshConnectionDTO> sshConnectionsDTO = new ArrayList<SshConnectionDTO>();
				sshConnectionsDTO.add(sshConnectionMapper.mapSshConnectionToDTO(current));
				sendSshNotifications("delete", sshConnectionsDTO);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
    
    public List<SshConnectionDTO> getAllsshConnectionsOnIsEnabledAndOrganization(boolean isEnabled, String organization){
        return sshConnectionRepository.findAllByActiveAndOrganization(isEnabled,organization)
                .stream()
                .map(sshConnectionMapper::mapSshConnectionToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<SshConnectionDTO> getAllsshConnectionsOnIsEnabled(boolean isEnabled){
        return sshConnectionRepository.findAllByActive(isEnabled)
                .stream()
                .map(sshConnectionMapper::mapSshConnectionToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<SshConnectionDTO> getAllSshConnectionsOnOrganization(String organization){
        return sshConnectionRepository.findAllByOrganization(organization)
                .stream()
                .map(sshConnectionMapper::mapSshConnectionToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * The purpose of the method is to send notifications
     */
    public void sendSshNotifications(String type, List<SshConnectionDTO> sshConnectionsDTO) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();
		List<Notification> allNotifications = new ArrayList<Notification>();
		sshConnectionsDTO.forEach(
				(sshConnectionDTO) -> {
					
					List<Employee> allAdmins = employeeRepository.findAllByUserRoleAndOrganization(USER_ROLE.ADMIN, sshConnectionDTO.getOrganization());
					
					if(allAdmins != null)
					{
						allAdmins.forEach(
								(employee) -> {
									
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
							    		   notification.setForExtension(employee.getExtension());
							    		   notification.setMessage("SSH connection for "+sshConnectionDTO.getDomain()+" created successfully.");
							    		   notification.setNotificationType("ssh");
							    		   notification.setOrganization(employee.getOrganization());
							    		   notification.setTitle("Use-It!");
							    		   
							    		   msg = new BotInputDTO();
								    	   msg.setDomain(employee.getDomain());
								    	   msg.setExtension(employee.getExtension());
								    	   msg.setFormat("json");
								    	   try {
											msg.setMessage(mapper.writeValueAsString(notification));
											} catch (JsonProcessingException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
								    	   msg.setMessagetype("notification");
								    	   msg.setOrganization(employee.getOrganization());
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
							    		   notification.setAlertType("alert-info");
							    		   notification.setForExtension(employee.getExtension());
							    		   notification.setMessage("SSH connection for "+sshConnectionDTO.getDomain()+" updated successfully.");
							    		   notification.setNotificationType("ssh");
							    		   notification.setOrganization(employee.getOrganization());
							    		   notification.setTitle("Know!");
							    		   
							    		   msg = new BotInputDTO();
								    	   msg.setDomain(employee.getDomain());
								    	   msg.setExtension(employee.getExtension());
								    	   msg.setFormat("json");
								    	   try {
											msg.setMessage(mapper.writeValueAsString(notification));
											} catch (JsonProcessingException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
								    	   msg.setMessagetype("notification");
								    	   msg.setOrganization(employee.getOrganization());
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
							    		   notification.setForExtension(employee.getExtension());
							    		   notification.setMessage("SSH connection for "+sshConnectionDTO.getDomain()+" delete successfully.");
							    		   notification.setNotificationType("ssh");
							    		   notification.setOrganization(employee.getOrganization());
							    		   notification.setTitle("Stop!");
							    		   
							    		   msg = new BotInputDTO();
								    	   msg.setDomain(employee.getDomain());
								    	   msg.setExtension(employee.getExtension());
								    	   msg.setFormat("json");
								    	   try {
											msg.setMessage(mapper.writeValueAsString(notification));
											} catch (JsonProcessingException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
								    	   msg.setMessagetype("notification");
								    	   msg.setOrganization(employee.getOrganization());
								    	   try {
									         	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
										   }
										   catch(Exception e)
										   {
										      e.printStackTrace();
										   }
							    		   allNotifications.add(notification); 
							    		   break;
							    		   
	                                       case "disabled": 
	    						    		   
	    						    		   notification = new Notification();
	    						    		   notification.setCreationDate(new Date());
	    						    		   notification.setAlertType("alert-danger");
	    						    		   notification.setForExtension(employee.getExtension());
	    						    		   notification.setMessage("SSH connection for "+sshConnectionDTO.getDomain()+" disabled successfully.");
	    						    		   notification.setNotificationType("ssh");
	    						    		   notification.setOrganization(employee.getOrganization());
	    						    		   notification.setTitle("Stop!");
	    						    		   
	    						    		   msg = new BotInputDTO();
	    							    	   msg.setDomain(employee.getDomain());
	    							    	   msg.setExtension(employee.getExtension());
	    							    	   msg.setFormat("json");
	    							    	   try {
	    										msg.setMessage(mapper.writeValueAsString(notification));
	    										} catch (JsonProcessingException e) {
	    											// TODO Auto-generated catch block
	    											e.printStackTrace();
	    										}
	    							    	   msg.setMessagetype("notification");
	    							    	   msg.setOrganization(employee.getOrganization());
									    	   try {
										         	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
											   }
											   catch(Exception e)
											   {
											      e.printStackTrace();
											   }
	    						    		   allNotifications.add(notification); 
	    						    		   break;
	    						    		   
	                                           case "enabled": 
	    						    		   
	    						    		   notification = new Notification();
	    						    		   notification.setCreationDate(new Date());
	    						    		   notification.setAlertType("alert-success");
	    						    		   notification.setForExtension(employee.getExtension());
	    						    		   notification.setMessage("SSH connection for "+sshConnectionDTO.getDomain()+" enabled successfully.");
	    						    		   notification.setNotificationType("ssh");
	    						    		   notification.setOrganization(employee.getOrganization());
	    						    		   notification.setTitle("Use-It!");
	    						    		   
	    						    		   msg = new BotInputDTO();
	    							    	   msg.setDomain(employee.getDomain());
	    							    	   msg.setExtension(employee.getExtension());
	    							    	   msg.setFormat("json");
	    							    	   try {
	    										msg.setMessage(mapper.writeValueAsString(notification));
	    										} catch (JsonProcessingException e) {
	    											// TODO Auto-generated catch block
	    											e.printStackTrace();
	    										}
	    							    	   msg.setMessagetype("notification");
	    							    	   msg.setOrganization(employee.getOrganization());
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
			    	
   	            });
    }
	
}