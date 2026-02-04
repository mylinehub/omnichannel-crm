package com.mylinehub.crm.ami.TaskScheduler;

import org.springframework.context.ApplicationContext;

import com.mylinehub.crm.ami.service.notificaton.EmployeeCallErrorNotificationService;
import com.mylinehub.crm.ami.autodialer.AutodialerReinitiateAndFunctionService;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.utils.LoggerUtils;

import lombok.Data;

@Data
public class DialAutomateCallReminderRunnable  implements Runnable{
	
	private String jobId;
	private String phoneNumber;
	private String fromExtension;
	private String fromPhoneNumber;
	boolean isCallOnMobile;
	private String callType;
    private String organization;
    private String domain;
    private ApplicationContext applicationContext;
    private String context;
    private int priority;
    private Long timeOut;
	private String firstName;
	private String protocol;
	private String phoneTrunk;
	boolean useSecondaryLine;
	String secondDomain;
	int breathingSeconds;
	String autodialerType;
	
    @Override
    public void run() {
    	
    	System.out.println("DialAutomateCallReminderRunnable");
    	
    	EmployeeCallErrorNotificationService employeeCallErrorNotificationService  = applicationContext.getBean(EmployeeCallErrorNotificationService.class);
    	AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService = applicationContext.getBean(AutodialerReinitiateAndFunctionService.class);

    	try {	
    		  autodialerReinitiateAndFunctionService.executeAutodialerCall(jobId, phoneNumber, fromExtension, fromPhoneNumber, isCallOnMobile, callType, organization, domain, context, priority, timeOut, firstName, protocol, phoneTrunk, useSecondaryLine, secondDomain, breathingSeconds, autodialerType,null,true);
    		}
    	catch(Exception e)
    	{
    		//In case of error send a notification to extension about call
    		try {
	    			e.printStackTrace();
	    	        NotificationRepository notificationRepository = applicationContext.getBean(NotificationRepository.class);
	    	        System.out.println("Eror while originating scheduled call, sending notification to employee who scheduled it.");
	    			employeeCallErrorNotificationService.sendEmployeeCallErrorNotifications(fromExtension, firstName, fromPhoneNumber, organization, domain, notificationRepository);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}	
    }
}
