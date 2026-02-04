package com.mylinehub.crm.ami.service;

import com.mylinehub.crm.ami.TaskScheduler.DialAutomateCallCronRunnable;
import com.mylinehub.crm.ami.TaskScheduler.DialAutomateCallReminderRunnable;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.service.SchedulerService;
import com.mylinehub.crm.utils.LoggerUtils;

import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class ScheduleDialAutomateCallService {

	private final SchedulerService schedulerService;
	
	public void removeAndScheduleDialAutomateCallCron(DialAutomateCallCronRunnable dialAutomateCallCronRunnable)
	   {
		   LoggerUtils.log.debug("removeAndScheduleDialAutomateCallCronService");
		   schedulerService.removeIfExistsAndScheduleACronTask(dialAutomateCallCronRunnable.getJobId(), dialAutomateCallCronRunnable, "0 */1 * * * ?");
	   }
	
	public void removeAndScheduleDialAutomateCallReminder(int breathingSeconds,DialAutomateCallReminderRunnable dialAutomateCallReminderRunnable)
	   {   
		   LoggerUtils.log.debug("removeAndScheduleDialAutomateCallReminderService");
		   schedulerService.removeIfExistsAndScheduleATaskAfterXSeconds(dialAutomateCallReminderRunnable.getJobId(), dialAutomateCallReminderRunnable, breathingSeconds+60);
	   }
	
	public void removeScheduleDialAutomateCall(String jobId)
	   {
		   if(schedulerService.findIfScheduledTask(jobId))
		   {
			   schedulerService.removeScheduledTask(jobId);
		   }
	   }
	
	
	public boolean findIfScheduleDialAutomateCall(String jobId)
	   {
			boolean toReturn = true;
		   if(schedulerService.findIfScheduledTask(jobId))
		   {
			   toReturn = true;
		   }
		   else
		   {
			   toReturn = false;
		   }
		   
		   return toReturn;
	   }
	
}
