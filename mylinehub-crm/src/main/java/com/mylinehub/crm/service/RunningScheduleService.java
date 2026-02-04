package com.mylinehub.crm.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.TaskScheduler.CustomerCallRunnable;
import com.mylinehub.crm.TaskScheduler.StartCampaignRunnable;
import com.mylinehub.crm.TaskScheduler.StopCampaignRunnable;
import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.data.TrackedSchduledJobs;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.RunningSchedule;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.repository.RunningScheduleRepository;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class RunningScheduleService implements CurrentTimeInterface{
	
	 private final RunningScheduleRepository runningScheduleRepository;
	 private final SchedulerService schedulerService;
	 private final ApplicationContext applicationContext;
	 private final NotificationRepository notificationRepository;
	 
	 
	 public List<RunningSchedule> getAllRunningSchedules() {
			
		 List<RunningSchedule> toReturn = null;
		 
		 try
		 {
			 toReturn = runningScheduleRepository.findAll();
			 
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
			 throw e;
		 }
		 return toReturn;
	 }
	 
	 
	 public List<RunningSchedule> getAllRunningSchedulesByOrganization(String organization) {
		
		 List<RunningSchedule> toReturn = null;
		 
		 try
		 {
			 toReturn = runningScheduleRepository.findAllByOrganizationOrderByIdAsc(organization);
			 
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
			 throw e;
		 }
		 return toReturn;
	 }
	 
	 
	 public List<RunningSchedule> getAllRunningSchedulesByJobId(String jobId) {
			
		 List<RunningSchedule> toReturn = null;
		 
		 try
		 {
			 toReturn = runningScheduleRepository.findAllByJobId(jobId);
			 
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
			 throw e;
		 }
		 return toReturn;
	 }
	 
	 
	 public boolean deleteAllRunningSchedulesByJobId(String jobId) {
		boolean toReturn = true;
		 
		 try
		 {
			 List<RunningSchedule>toDelete = runningScheduleRepository.findAllByJobId(jobId);
			 
			 if(toDelete != null)
			 {
				 runningScheduleRepository.deleteAll(toDelete);
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
	 
	 
	 public void deleteRunningSchedules(List<RunningSchedule> runningSchedules)
	 {
		 try
		 {
			 runningScheduleRepository.deleteAll(runningSchedules);
			 
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
			 throw e;
		 }
	 }
	 
	 public RunningSchedule removeIfExistsAndCreateRunningSchedules(RunningSchedule runningSchedule)
	 {
		 try
		 {
			 List<RunningSchedule>toDelete = runningScheduleRepository.findAllByJobId(runningSchedule.getJobId());
			 
			 if(toDelete != null)
			 {
				 runningScheduleRepository.deleteAll(toDelete);
			 }
			 
			 runningSchedule =  runningScheduleRepository.save(runningSchedule);
			 
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
			 throw e;
		 }
		 
		 return runningSchedule;
	 }

	 
	 public boolean runningSchedulesToJobsAfterRestart(List<RunningSchedule> runningSchedules) throws Exception
	 {
		 boolean toReturn = true;
		 
		 try
		 {
			//Convert to jobs
			 
			 for(int i =0; i< runningSchedules.size(); i++)
			 {
				 RunningSchedule runningSchedule = runningSchedules.get(i);
				 
				 if(runningSchedule.getFunctionality().equals(TrackedSchduledJobs.startCampaignRunnable))
				 {
					 StartCampaignRunnable startCampaignRunnable = new StartCampaignRunnable();
					 startCampaignRunnable.setJobId(runningSchedule.getJobId());
					 startCampaignRunnable.setCampaignID(runningSchedule.getCampaignId());
					 startCampaignRunnable.setOrganization(runningSchedule.getOrganization());
					 startCampaignRunnable.setFromExtension(runningSchedule.getFromExtension());
					 startCampaignRunnable.setDomain(runningSchedule.getDomain());
					 startCampaignRunnable.setApplicationContext(applicationContext);
			    	 
					 if(runningSchedule.getScheduleType().equals(TrackedSchduledJobs.cron))
					 {
				         schedulerService.removeIfExistsAndScheduleACronTask(runningSchedule.getJobId(), startCampaignRunnable, runningSchedule.getCronExpression());
				        	
					 }
					 else if (runningSchedule.getScheduleType().equals(TrackedSchduledJobs.fixeddate))
					 {
					     Date date1 = new Date();
					     Date date2 = runningSchedule.getDate();
					     //the value 0 if the argument Date is equal tothis Date; a value less than 0 if this Dateis before the Date argument; 
					     // and a value greater than 0 if this Date is after the Date argument.
					     int campareResult = date2.compareTo(date1);
					        
					     if(campareResult > 0)
					     {
						        
							 //check date if before or after. If before sheet message notification
					         schedulerService.removeIfExistsAndScheduleATaskOnDate(runningSchedule.getJobId(),startCampaignRunnable, runningSchedule.getDate());
					        	 
					     }
					     else
					     {
					    	 //Send Notification
					    	 this.sendScheduleDidNotHappenNotifications(runningSchedule.getFromExtension(), runningSchedule.getFirstName(), runningSchedule.getPhoneNumber(), runningSchedule.getOrganization(), runningSchedule.getDomain(), notificationRepository, runningSchedule.getCampaignId(), runningSchedule.getFunctionality(), runningSchedule.getScheduleType());
					    	 runningScheduleRepository.delete(runningSchedule);
					     }
					 }
					 else if (runningSchedule.getScheduleType().equals(TrackedSchduledJobs.afternseconds))
					 {
						 //schedule jobs after n seconds are not triggered but just reminded
						//Send message notification
						 this.sendScheduleDidNotHappenNotifications(runningSchedule.getFromExtension(), runningSchedule.getFirstName(), runningSchedule.getPhoneNumber(), runningSchedule.getOrganization(), runningSchedule.getDomain(), notificationRepository, runningSchedule.getCampaignId(), runningSchedule.getFunctionality(), runningSchedule.getScheduleType());
						 runningScheduleRepository.delete(runningSchedule);
					 }
					 else
					 {
						 //Do nothing , we do not track this kind of job
						 runningScheduleRepository.delete(runningSchedule);
					 }
					 
				 }
				 if(runningSchedule.getFunctionality().equals(TrackedSchduledJobs.stopCampaignRunnable))
				 {
					 StopCampaignRunnable stopCampaignRunnable = new StopCampaignRunnable();
					 stopCampaignRunnable.setJobId(runningSchedule.getJobId());
					 stopCampaignRunnable.setCampaignID(runningSchedule.getCampaignId());
					 stopCampaignRunnable.setOrganization(runningSchedule.getOrganization());
					 stopCampaignRunnable.setFromExtension(runningSchedule.getFromExtension());
					 stopCampaignRunnable.setDomain(runningSchedule.getDomain());
					 stopCampaignRunnable.setApplicationContext(applicationContext);
			    	 
					 if(runningSchedule.getScheduleType().equals(TrackedSchduledJobs.cron))
					 {
				         schedulerService.removeIfExistsAndScheduleACronTask(runningSchedule.getJobId(), stopCampaignRunnable, runningSchedule.getCronExpression());
				        	
					 }
					 else if (runningSchedule.getScheduleType().equals(TrackedSchduledJobs.fixeddate))
					 {
					     Date date1 = new Date();
					     Date date2 = runningSchedule.getDate();
					     //the value 0 if the argument Date is equal tothis Date; a value less than 0 if this Dateis before the Date argument; 
					     // and a value greater than 0 if this Date is after the Date argument.
					     int campareResult = date2.compareTo(date1);
					        
					     if(campareResult > 0)
					     {
						        
							 //check date if before or after. If before sheet message notification
					         schedulerService.removeIfExistsAndScheduleATaskOnDate(runningSchedule.getJobId(),stopCampaignRunnable, runningSchedule.getDate());
					        	 
					     }
					     else
					     {
					    	 //Send Notification
					    	 this.sendScheduleDidNotHappenNotifications(runningSchedule.getFromExtension(), runningSchedule.getFirstName(), runningSchedule.getPhoneNumber(), runningSchedule.getOrganization(), runningSchedule.getDomain(), notificationRepository, runningSchedule.getCampaignId(), runningSchedule.getFunctionality(), runningSchedule.getScheduleType());
					    	 runningScheduleRepository.delete(runningSchedule);
					     }
					 }
					 else if (runningSchedule.getScheduleType().equals(TrackedSchduledJobs.afternseconds))
					 {
						 //schedule jobs after n seconds are not triggered but just reminded
						//Send message notification
						 this.sendScheduleDidNotHappenNotifications(runningSchedule.getFromExtension(), runningSchedule.getFirstName(), runningSchedule.getPhoneNumber(), runningSchedule.getOrganization(), runningSchedule.getDomain(), notificationRepository, runningSchedule.getCampaignId(), runningSchedule.getFunctionality(), runningSchedule.getScheduleType());
						 runningScheduleRepository.delete(runningSchedule);
					 }
					 else
					 {
						 //Do nothing , we do not track this kind of job
						 runningScheduleRepository.delete(runningSchedule);
					 }
					 
				 }
				 else if (runningSchedule.getFunctionality().equals(TrackedSchduledJobs.customerCallRunnable))
				 {
					 Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(runningSchedule.getFromExtension(), null, "get-one");
					 EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
					 if(allEmployeeDataAndState != null)
					 {
					 	employeeDataAndStateDTO = allEmployeeDataAndState.get(runningSchedule.getFromExtension());
					 } 
					 
					 if(employeeDataAndStateDTO != null)
					 {
						 Employee employee = employeeDataAndStateDTO.getEmployee();
				         

						 CustomerCallRunnable customerCallRunnable = new CustomerCallRunnable();
						 customerCallRunnable.setJobId(runningSchedule.getJobId());
						 customerCallRunnable.setPhoneNumber(runningSchedule.getPhoneNumber());
						 customerCallRunnable.setFromExtension(runningSchedule.getFromExtension());
						 customerCallRunnable.setCallType(runningSchedule.getCallType());
						 customerCallRunnable.setOrganization(runningSchedule.getOrganization());
						 customerCallRunnable.setDomain(runningSchedule.getDomain());
						 customerCallRunnable.setContext(runningSchedule.getContext());
						 customerCallRunnable.setProtocol(runningSchedule.getProtocol());
						 customerCallRunnable.setPhoneTrunk(runningSchedule.getPhoneTrunk());
						 customerCallRunnable.setPriority(runningSchedule.getPriority());
						 customerCallRunnable.setTimeOut(runningSchedule.getTimeOut());
						 customerCallRunnable.setFirstName(runningSchedule.getFirstName());
						 customerCallRunnable.setApplicationContext(applicationContext);
						 customerCallRunnable.setNotificationRepository(notificationRepository);
						 customerCallRunnable.setUseSecondaryLine(employee.isUseSecondaryAllotedLine());
						 customerCallRunnable.setCallOnMobile(employee.isCallonnumber());
						 customerCallRunnable.setFromPhoneNumber(employee.getPhonenumber());
				    	 customerCallRunnable.setSecondDomain(employee.getSecondDomain());
				    	 
						 if(runningSchedule.getScheduleType().equals(TrackedSchduledJobs.cron))
						 {	
							 schedulerService.removeIfExistsAndScheduleACronTask(runningSchedule.getJobId(), customerCallRunnable, runningSchedule.getCronExpression()); 
						 }
						 else if (runningSchedule.getScheduleType().equals(TrackedSchduledJobs.fixeddate))
						 {
							 Date date1 = new Date();
						     Date date2 = runningSchedule.getDate();
						     //the value 0 if the argument Date is equal tothis Date; a value less than 0 if this Dateis before the Date argument; 
						     // and a value greater than 0 if this Date is after the Date argument.
						     int campareResult = date2.compareTo(date1);
						        
						     if(campareResult > 0)
						     {
						    	 schedulerService.removeIfExistsAndScheduleATaskOnDate(runningSchedule.getJobId(), customerCallRunnable, runningSchedule.getDate());
						     }
						     else
						     {
						    	 //send Notification
						    	 this.sendScheduleDidNotHappenNotifications(runningSchedule.getFromExtension(), runningSchedule.getFirstName(), runningSchedule.getPhoneNumber(), runningSchedule.getOrganization(), runningSchedule.getDomain(), notificationRepository, runningSchedule.getCampaignId(), runningSchedule.getFunctionality(), runningSchedule.getScheduleType());
						    	 runningScheduleRepository.delete(runningSchedule);
						     }
						 }
						 else if (runningSchedule.getScheduleType().equals(TrackedSchduledJobs.afternseconds))
						 {
							 //Send notification
							 this.sendScheduleDidNotHappenNotifications(runningSchedule.getFromExtension(), runningSchedule.getFirstName(), runningSchedule.getPhoneNumber(), runningSchedule.getOrganization(), runningSchedule.getDomain(), notificationRepository, runningSchedule.getCampaignId(), runningSchedule.getFunctionality(), runningSchedule.getScheduleType());
							 runningScheduleRepository.delete(runningSchedule);
						 }
						 else
						 {
							 //Do nothing , we do not track this kind of job
							 runningScheduleRepository.delete(runningSchedule);
						 }
					 }
				 }
				 else
				 {
					 
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
	 
	 
	 /**
	     * The purpose of the method is to send notifications
	     */
	    public void sendScheduleDidNotHappenNotifications(String fromExtension, String firstName, String phoneNumber, String organization, String domain, NotificationRepository notificationRepository,Long campaignId, String functionality, String scheduleType) throws Exception {
	        
	    	ObjectMapper mapper = new ObjectMapper();

			BotInputDTO msg;
			Notification notification;
	    	
	    	 notification = new Notification();
			 notification.setCreationDate(new Date());
			 notification.setAlertType("alert-danger");
			 notification.setForExtension(fromExtension);
			 if(functionality.equals(TrackedSchduledJobs.startCampaignRunnable))
			 {
				 notification.setMessage("Campaign having id : "+ campaignId+" is released from start schedule. Schedule again.");
				 notification.setNotificationType("start-campaign");
				 notification.setOrganization(organization);
				 notification.setTitle(scheduleType+"!");
			 }
			 else if(functionality.equals(TrackedSchduledJobs.stopCampaignRunnable))
			 {
				 notification.setMessage("Campaign having id : "+ campaignId+" is released from stop schedule. Schedule again.");
				 notification.setNotificationType("stop-campaign");
				 notification.setOrganization(organization);
				 notification.setTitle(scheduleType+"!");
			 }
			 else if (functionality.equals(TrackedSchduledJobs.customerCallRunnable))
			 {
				 notification.setMessage("Call scheduled to "+ firstName +" ("+phoneNumber+")" +" did not make. Kindly call manually.");
				 notification.setNotificationType("scheduled-call");
				 notification.setOrganization(organization);
				 notification.setTitle(scheduleType+"!");
			 }
			 else
			 {
			
				 return;
				 
			 }
  
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
