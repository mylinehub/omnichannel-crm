package com.mylinehub.crm.ami.service.notificaton;

import java.util.Date;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class EmployeeCallErrorNotificationService {


	/**
     * The purpose of the method is to send notifications
     */
    public void sendEmployeeCallAutodialRefreshNotifications(String fromExtension, String organization, String domain, NotificationRepository notificationRepository) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();

		BotInputDTO msg;
		Notification notification;

    	 notification = new Notification();
		 notification.setCreationDate(new Date());
		 notification.setAlertType("alert-danger");
		 notification.setForExtension(fromExtension);
		 notification.setMessage("Auto call stopped. In case your browser phone is green, refresh.");
		 notification.setNotificationType("auto-call");
		 notification.setOrganization(organization);
		 notification.setTitle("Refresh!");
		   
		 msg = new BotInputDTO();
  	     msg.setDomain(domain);
  	     msg.setExtension(fromExtension);
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

		notificationRepository.save(notification);	

    }
    
    /**
     * The purpose of the method is to send notifications
     */
    public void sendEmployeeCallErrorNotifications(String fromExtension, String firstName, String phoneNumber, String organization, String domain, NotificationRepository notificationRepository) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();

		BotInputDTO msg;
		Notification notification;

    	 notification = new Notification();
		 notification.setCreationDate(new Date());
		 notification.setAlertType("alert-danger");
		 notification.setForExtension(fromExtension);
		 notification.setMessage("Call to "+ firstName +" ("+phoneNumber+")" +" did not make. Kindly call manually.");
		 notification.setNotificationType("scheduled-call");
		 notification.setOrganization(organization);
		 notification.setTitle("Call Missed!");
		   
		 msg = new BotInputDTO();
  	     msg.setDomain(domain);
  	     msg.setExtension(fromExtension);
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

		notificationRepository.save(notification);	

    }
    
	/**
     * The purpose of the method is to send notifications
     */
    public void sendEmployeeCallLimitNotifications(String fromExtension, String organization,String domain, Long campaignId, NotificationRepository notificationRepository) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();

		BotInputDTO msg;
		Notification notification;

    	 notification = new Notification();
		 notification.setCreationDate(new Date());
		 notification.setAlertType("alert-danger");
		 notification.setForExtension(fromExtension);
		 notification.setMessage("Call limit for campaign Id :"+ campaignId +" is over.");
		 notification.setNotificationType("scheduled-call");
		 notification.setOrganization(organization);
		 notification.setTitle("Stopped!");
		   
		 msg = new BotInputDTO();
  	     msg.setDomain(domain);
  	     msg.setExtension(fromExtension);
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
		notificationRepository.save(notification);	

    }
    
}
