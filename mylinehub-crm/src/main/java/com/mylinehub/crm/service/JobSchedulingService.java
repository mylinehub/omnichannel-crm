package com.mylinehub.crm.service;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import com.mylinehub.crm.TaskScheduler.CustomerCallRunnable;
import com.mylinehub.crm.data.TrackedSchduledJobs;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.RunningSchedule;
import com.mylinehub.crm.entity.dto.AfterNSecondsSchedulerDefinitionDTO;
import com.mylinehub.crm.entity.dto.CronSchedulerDefinitionDTO;
import com.mylinehub.crm.entity.dto.FixedDateSchedulerDefinitionDTO;
import com.mylinehub.crm.repository.NotificationRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JobSchedulingService {

    private final SchedulerService schedulerService;
    private final RunningScheduleService runningScheduleService;
    private final NotificationRepository notificationRepository;

    
 // -------------------- Fixed Date Call to Customer --------------------
    public boolean scheduleACronCallToCustomer(CronSchedulerDefinitionDTO cronSchedulerDefinitionDTO,Employee employee, ApplicationContext applicationContext) {
        boolean toReturn = false;

        if (!cronSchedulerDefinitionDTO.getPhoneNumber().trim().startsWith("+")) {
			cronSchedulerDefinitionDTO.setPhoneNumber("+" + cronSchedulerDefinitionDTO.getPhoneNumber());
        }
		
		String jobId = TrackedSchduledJobs.cron+"-"+TrackedSchduledJobs.customerCallRunnable+cronSchedulerDefinitionDTO.getFromExtension().toString()+cronSchedulerDefinitionDTO.getPhoneNumber().toString();
		CustomerCallRunnable customerCallRunnable = new CustomerCallRunnable();
		customerCallRunnable.setJobId(jobId);
		customerCallRunnable.setPhoneNumber(cronSchedulerDefinitionDTO.getPhoneNumber());
		customerCallRunnable.setFromExtension(cronSchedulerDefinitionDTO.getFromExtension());
		customerCallRunnable.setCallType(cronSchedulerDefinitionDTO.getCallType());
		customerCallRunnable.setOrganization(cronSchedulerDefinitionDTO.getOrganization());
		customerCallRunnable.setDomain(cronSchedulerDefinitionDTO.getDomain());
		customerCallRunnable.setContext(cronSchedulerDefinitionDTO.getContext());
		customerCallRunnable.setProtocol(cronSchedulerDefinitionDTO.getProtocol());
		customerCallRunnable.setPhoneTrunk(cronSchedulerDefinitionDTO.getPhoneTrunk());
		customerCallRunnable.setPriority(cronSchedulerDefinitionDTO.getPriority());
		customerCallRunnable.setTimeOut(cronSchedulerDefinitionDTO.getTimeOut());
		customerCallRunnable.setFirstName(cronSchedulerDefinitionDTO.getFirstName());
		customerCallRunnable.setApplicationContext(applicationContext);
		customerCallRunnable.setNotificationRepository(notificationRepository);
		
		if(employee != null) {
			customerCallRunnable.setUseSecondaryLine(employee.isUseSecondaryAllotedLine());
			customerCallRunnable.setCallOnMobile(employee.isCallonnumber());
			customerCallRunnable.setFromPhoneNumber(employee.getPhonenumber());
			customerCallRunnable.setSecondDomain(employee.getSecondDomain());
			}
		else {
			customerCallRunnable.setUseSecondaryLine(false);
			customerCallRunnable.setCallOnMobile(false);
			customerCallRunnable.setFromPhoneNumber("Not-Required");
			customerCallRunnable.setSecondDomain("Not-Required");
		}
	
    	schedulerService.removeIfExistsAndScheduleACronTask(jobId, customerCallRunnable, cronSchedulerDefinitionDTO.getCronExpression());
    	
    	//Schedule to database
    	RunningSchedule runningSchedule = new RunningSchedule();
    	runningSchedule.setJobId(jobId);
    	runningSchedule.setFunctionality(TrackedSchduledJobs.customerCallRunnable);
    	runningSchedule.setScheduleType(TrackedSchduledJobs.cron);
    	runningSchedule.setCronExpression(cronSchedulerDefinitionDTO.getCronExpression());
    	runningSchedule.setDate(null);
    	runningSchedule.setCampaignId(cronSchedulerDefinitionDTO.getCampaignId());
    	runningSchedule.setActionType(cronSchedulerDefinitionDTO.getActionType());
    	runningSchedule.setData(cronSchedulerDefinitionDTO.getData());
    	runningSchedule.setOrganization(cronSchedulerDefinitionDTO.getOrganization());
    	runningSchedule.setDomain(cronSchedulerDefinitionDTO.getDomain());
    	runningSchedule.setSeconds(0);
    	runningSchedule.setPhoneNumber(cronSchedulerDefinitionDTO.getPhoneNumber());
    	runningSchedule.setCallType(cronSchedulerDefinitionDTO.getCallType());
    	runningSchedule.setFromExtension(cronSchedulerDefinitionDTO.getFromExtension());
    	runningSchedule.setContext(cronSchedulerDefinitionDTO.getContext());
    	runningSchedule.setPriority(cronSchedulerDefinitionDTO.getPriority());
    	runningSchedule.setTimeOut(cronSchedulerDefinitionDTO.getTimeOut());
    	runningSchedule.setFirstName(cronSchedulerDefinitionDTO.getFirstName());
    	runningSchedule.setProtocol(cronSchedulerDefinitionDTO.getProtocol());
    	runningSchedule.setPhoneTrunk(cronSchedulerDefinitionDTO.getPhoneTrunk());
    	runningScheduleService.removeIfExistsAndCreateRunningSchedules(runningSchedule);
    	
    	toReturn = true;

        return toReturn;
    }
    
    
 // -------------------- Fixed Date Call to Customer --------------------
    public boolean scheduleAFixedDateCallToCustomer(FixedDateSchedulerDefinitionDTO fixedDateSchedulerDefinitionDTO,Employee employee, ApplicationContext applicationContext) {
        boolean toReturn = false;

        if (!fixedDateSchedulerDefinitionDTO.getPhoneNumber().trim().startsWith("+")) {
        	fixedDateSchedulerDefinitionDTO.setPhoneNumber("+" + fixedDateSchedulerDefinitionDTO.getPhoneNumber());
        }
        
        String jobId = TrackedSchduledJobs.fixeddate+"-"+TrackedSchduledJobs.customerCallRunnable+fixedDateSchedulerDefinitionDTO.getFromExtension().toString()+fixedDateSchedulerDefinitionDTO.getPhoneNumber().toString();
		CustomerCallRunnable customerCallRunnable = new CustomerCallRunnable();
		customerCallRunnable.setJobId(jobId);
		customerCallRunnable.setPhoneNumber(fixedDateSchedulerDefinitionDTO.getPhoneNumber());
		customerCallRunnable.setFromExtension(fixedDateSchedulerDefinitionDTO.getFromExtension());
		customerCallRunnable.setCallType(fixedDateSchedulerDefinitionDTO.getCallType());
		customerCallRunnable.setOrganization(fixedDateSchedulerDefinitionDTO.getOrganization());
		customerCallRunnable.setDomain(fixedDateSchedulerDefinitionDTO.getDomain());
		customerCallRunnable.setContext(fixedDateSchedulerDefinitionDTO.getContext());
		customerCallRunnable.setProtocol(fixedDateSchedulerDefinitionDTO.getProtocol());
		customerCallRunnable.setPhoneTrunk(fixedDateSchedulerDefinitionDTO.getPhoneTrunk());
		customerCallRunnable.setPriority(fixedDateSchedulerDefinitionDTO.getPriority());
		customerCallRunnable.setTimeOut(fixedDateSchedulerDefinitionDTO.getTimeOut());
		customerCallRunnable.setFirstName(fixedDateSchedulerDefinitionDTO.getFirstName());
		customerCallRunnable.setApplicationContext(applicationContext);
		customerCallRunnable.setNotificationRepository(notificationRepository);
		
		if(employee != null) {
			customerCallRunnable.setUseSecondaryLine(employee.isUseSecondaryAllotedLine());
			customerCallRunnable.setCallOnMobile(employee.isCallonnumber());
			customerCallRunnable.setFromPhoneNumber(employee.getPhonenumber());
			customerCallRunnable.setSecondDomain(employee.getSecondDomain());
		}
		else {
			customerCallRunnable.setUseSecondaryLine(false);
			customerCallRunnable.setCallOnMobile(false);
			customerCallRunnable.setFromPhoneNumber("Not-Required");
			customerCallRunnable.setSecondDomain("Not-Required");
		}
		
		
    	schedulerService.removeIfExistsAndScheduleATaskOnDate(jobId, customerCallRunnable, fixedDateSchedulerDefinitionDTO.getDate());
    	
    	//Schedule to database
    	RunningSchedule runningSchedule = new RunningSchedule();
    	runningSchedule.setJobId(jobId);
    	runningSchedule.setFunctionality(TrackedSchduledJobs.customerCallRunnable);
    	runningSchedule.setScheduleType(TrackedSchduledJobs.fixeddate);
    	runningSchedule.setCronExpression(null);
    	runningSchedule.setDate(fixedDateSchedulerDefinitionDTO.getDate());
    	runningSchedule.setCampaignId(fixedDateSchedulerDefinitionDTO.getCampaignId());
    	runningSchedule.setActionType(fixedDateSchedulerDefinitionDTO.getActionType());
    	runningSchedule.setData(fixedDateSchedulerDefinitionDTO.getData());
    	runningSchedule.setOrganization(fixedDateSchedulerDefinitionDTO.getOrganization());
    	runningSchedule.setDomain(fixedDateSchedulerDefinitionDTO.getDomain());
    	runningSchedule.setSeconds(0);
    	runningSchedule.setPhoneNumber(fixedDateSchedulerDefinitionDTO.getPhoneNumber());
    	runningSchedule.setCallType(fixedDateSchedulerDefinitionDTO.getCallType());
    	runningSchedule.setFromExtension(fixedDateSchedulerDefinitionDTO.getFromExtension());
    	runningSchedule.setContext(fixedDateSchedulerDefinitionDTO.getContext());
    	runningSchedule.setPriority(fixedDateSchedulerDefinitionDTO.getPriority());
    	runningSchedule.setTimeOut(fixedDateSchedulerDefinitionDTO.getTimeOut());
    	runningSchedule.setFirstName(fixedDateSchedulerDefinitionDTO.getFirstName());
    	runningSchedule.setProtocol(fixedDateSchedulerDefinitionDTO.getProtocol());
    	runningSchedule.setPhoneTrunk(fixedDateSchedulerDefinitionDTO.getPhoneTrunk());
    	runningScheduleService.removeIfExistsAndCreateRunningSchedules(runningSchedule);
    	
    	toReturn = true;

        return toReturn;
    }

    // -------------------- Fixed Date Call to Customer --------------------
    public boolean scheduleAfterNSecCallToCustomer(AfterNSecondsSchedulerDefinitionDTO afterNSecondsSchedulerDefinitionDTO,Employee employee, ApplicationContext applicationContext) {
        boolean toReturn = false;

        if (!afterNSecondsSchedulerDefinitionDTO.getPhoneNumber().trim().startsWith("+")) {
			afterNSecondsSchedulerDefinitionDTO.setPhoneNumber("+" + afterNSecondsSchedulerDefinitionDTO.getPhoneNumber());
        }
        
		
		String jobId = TrackedSchduledJobs.afternseconds+"-"+TrackedSchduledJobs.customerCallRunnable+afterNSecondsSchedulerDefinitionDTO.getFromExtension().toString()+afterNSecondsSchedulerDefinitionDTO.getPhoneNumber().toString();
		CustomerCallRunnable customerCallRunnable = new CustomerCallRunnable();
		customerCallRunnable.setJobId(jobId);
		customerCallRunnable.setPhoneNumber(afterNSecondsSchedulerDefinitionDTO.getPhoneNumber());
		customerCallRunnable.setFromExtension(afterNSecondsSchedulerDefinitionDTO.getFromExtension());
		customerCallRunnable.setCallType(afterNSecondsSchedulerDefinitionDTO.getCallType());
		customerCallRunnable.setOrganization(afterNSecondsSchedulerDefinitionDTO.getOrganization());
		customerCallRunnable.setDomain(afterNSecondsSchedulerDefinitionDTO.getDomain());
		customerCallRunnable.setContext(afterNSecondsSchedulerDefinitionDTO.getContext());
		customerCallRunnable.setProtocol(afterNSecondsSchedulerDefinitionDTO.getProtocol());
		customerCallRunnable.setPhoneTrunk(afterNSecondsSchedulerDefinitionDTO.getPhoneTrunk());
		customerCallRunnable.setPriority(afterNSecondsSchedulerDefinitionDTO.getPriority());
		customerCallRunnable.setTimeOut(afterNSecondsSchedulerDefinitionDTO.getTimeOut());
		customerCallRunnable.setFirstName(afterNSecondsSchedulerDefinitionDTO.getFirstName());
		customerCallRunnable.setApplicationContext(applicationContext);
		customerCallRunnable.setNotificationRepository(notificationRepository);
		
		if(employee != null) {
			customerCallRunnable.setUseSecondaryLine(employee.isUseSecondaryAllotedLine());
			customerCallRunnable.setCallOnMobile(employee.isCallonnumber());
			customerCallRunnable.setFromPhoneNumber(employee.getPhonenumber());
			customerCallRunnable.setSecondDomain(employee.getSecondDomain());
			}
		else {
			customerCallRunnable.setUseSecondaryLine(false);
			customerCallRunnable.setCallOnMobile(false);
			customerCallRunnable.setFromPhoneNumber("Not-Required");
			customerCallRunnable.setSecondDomain("Not-Required");
		}

		System.out.println("after setting runnable");

    	schedulerService.removeIfExistsAndScheduleATaskAfterXSeconds(jobId, customerCallRunnable, afterNSecondsSchedulerDefinitionDTO.getSeconds());
    	
    	//Schedule to database
    	RunningSchedule runningSchedule = new RunningSchedule();
    	runningSchedule.setJobId(jobId);
    	runningSchedule.setFunctionality(TrackedSchduledJobs.customerCallRunnable);
    	runningSchedule.setScheduleType(TrackedSchduledJobs.afternseconds);
    	runningSchedule.setCronExpression(null);
    	runningSchedule.setDate(null);
    	runningSchedule.setCampaignId(afterNSecondsSchedulerDefinitionDTO.getCampaignId());
    	runningSchedule.setActionType(afterNSecondsSchedulerDefinitionDTO.getActionType());
    	runningSchedule.setData(afterNSecondsSchedulerDefinitionDTO.getData());
    	runningSchedule.setOrganization(afterNSecondsSchedulerDefinitionDTO.getOrganization());
    	runningSchedule.setDomain(afterNSecondsSchedulerDefinitionDTO.getDomain());
    	runningSchedule.setSeconds(afterNSecondsSchedulerDefinitionDTO.getSeconds());
    	runningSchedule.setPhoneNumber(afterNSecondsSchedulerDefinitionDTO.getPhoneNumber());
    	runningSchedule.setCallType(afterNSecondsSchedulerDefinitionDTO.getCallType());
    	runningSchedule.setFromExtension(afterNSecondsSchedulerDefinitionDTO.getFromExtension());
    	runningSchedule.setContext(afterNSecondsSchedulerDefinitionDTO.getContext());
    	runningSchedule.setPriority(afterNSecondsSchedulerDefinitionDTO.getPriority());
    	runningSchedule.setTimeOut(afterNSecondsSchedulerDefinitionDTO.getTimeOut());
    	runningSchedule.setFirstName(afterNSecondsSchedulerDefinitionDTO.getFirstName());
    	runningSchedule.setProtocol(afterNSecondsSchedulerDefinitionDTO.getProtocol());
    	runningSchedule.setPhoneTrunk(afterNSecondsSchedulerDefinitionDTO.getPhoneTrunk());
    	runningScheduleService.removeIfExistsAndCreateRunningSchedules(runningSchedule);
    	
    	toReturn = true;

        return toReturn;
    }

   
   public boolean removeScheduledCallToCustomer(String scheduleType,String phoneNumber,String fromExtension,String organization) {
        
    	boolean toReturn= false;
    	if (!phoneNumber.trim().startsWith("+")) {
    	    phoneNumber = "+" + phoneNumber;
    	}

		String jobId = scheduleType+"-"+TrackedSchduledJobs.customerCallRunnable+fromExtension.toString()+phoneNumber.toString().trim();
		schedulerService.removeScheduledTask(jobId);
    	runningScheduleService.deleteAllRunningSchedulesByJobId(jobId);
		toReturn = true;	
        return toReturn;
    }
 

    public boolean findIfScheduledCallJobToCustomer(String scheduleType,String phoneNumber,String fromExtension,String organization) {
        
    	boolean toReturn= false;
    	if (!phoneNumber.trim().startsWith("+")) {
    	    phoneNumber = "+" + phoneNumber;
    	}
    	String jobId = scheduleType+"-"+TrackedSchduledJobs.customerCallRunnable+fromExtension.toString()+phoneNumber.toString().trim();
		toReturn = schedulerService.findIfScheduledTask(jobId);
    	
        return toReturn;
    }

    // Additional find methods can be added here in the same exact way
}
