package com.mylinehub.crm.service;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import com.mylinehub.crm.TaskScheduler.CustomTaskScheduler;

@Service
@AllArgsConstructor
public class SchedulerService {

		private final ApplicationContext applicationContext;
	    Map<String, ScheduledFuture<?>> jobsMap = new ConcurrentHashMap<>();

	 // Schedule a cron task
	    public ScheduledFuture<?> removeIfExistsAndScheduleACronTask(String jobId, Runnable tasklet, String cronExpression) {
	        //System.out.println("Scheduling task with job id: " + jobId + " and cron expression: " + cronExpression);
	        removeScheduledTask(jobId); // Remove if exists before scheduling
	        ScheduledFuture<?> scheduledTask = applicationContext.getBean(CustomTaskScheduler.class)
	                .schedule(tasklet, new CronTrigger(cronExpression, TimeZone.getTimeZone(TimeZone.getDefault().getID())));
	        jobsMap.put(jobId, scheduledTask);
	        //System.out.println("Job scheduled, current job count: " + jobsMap.size());
	        return scheduledTask;
	    }
	    
	    
	    public ScheduledFuture<?> removeIfExistsAndScheduleATaskOnDate(String jobId, Runnable tasklet, Date date) {
	        //System.out.println("Scheduling task with job id: " + jobId + " and on date : " + date);
	        removeScheduledTask(jobId);
	        ScheduledFuture<?> scheduledTask = applicationContext.getBean(CustomTaskScheduler.class).schedule(tasklet, date);
	        jobsMap.put(jobId, scheduledTask);
	        //System.out.println("Job size: " +jobsMap.size());
	        return scheduledTask;
	    }

	    
	    public ScheduledFuture<?> removeIfExistsAndScheduleATaskAfterXSeconds(String jobId, Runnable tasklet, int seconds) {
	        //System.out.println("Scheduling task with job id: " + jobId + " and after seconds : " + seconds);
	        removeScheduledTask(jobId);
	        Instant updatedInstant;
	        Instant currentInstant = new Date().toInstant();
	        updatedInstant = currentInstant.plusSeconds(seconds);  
	        ScheduledFuture<?> scheduledTask = applicationContext.getBean(CustomTaskScheduler.class).schedule(tasklet, updatedInstant);
	        jobsMap.put(jobId, scheduledTask);
	        //System.out.println("Job size: " +jobsMap.size());
	        return scheduledTask;
	    }
	    
//	    public void scheduleATaskAfterXSeconds(String jobId, Runnable tasklet, int seconds) {
//	        //System.out.println("Scheduling task with job id: " + jobId + " and after seconds : " + seconds);
//	        ScheduledFuture<?> scheduledTask = CustomTaskScheduler.getInstance().scheduleWithFixedDelay(tasklet, (seconds*1000));
//	        jobsMap.put(jobId, scheduledTask);
//	    }
	    
	    
	    
	 // Remove and cancel a scheduled task
	    public void removeScheduledTask(String jobId) {
	        //System.out.println("Try removing Job with id: " + jobId);
	        if (jobsMap.containsKey(jobId)) {
	            ScheduledFuture<?> scheduledTask = jobsMap.get(jobId);
	            if (scheduledTask != null) {
	                boolean isCancelled = false;
	                
	                if(scheduledTask.isCancelled())
	                {
	                	isCancelled = true;
	                }
	                else {
	                	isCancelled = scheduledTask.cancel(true);
	                }

	                if (isCancelled) {
	                    jobsMap.remove(jobId); // Only remove if successfully cancelled
	                    //System.out.println("Job with id: " + jobId + " has been cancelled and removed.");
	                } else {
	                    //System.out.println("Failed to cancel job with id: " + jobId);
	                }
	            }
	        } else {
	            //System.out.println("No task found for job id: " + jobId);
	        }
	    }
	    
	    
	 // Check if a task is scheduled and return its state
	    public boolean findIfScheduledTask(String jobId) {
	    	//System.out.println("Tryint to find if job id is running / cancelled: " + jobId);
	        if (jobsMap.containsKey(jobId)) {
	            ScheduledFuture<?> scheduledTask = jobsMap.get(jobId);
	            if (scheduledTask != null) {
	                boolean isDone = scheduledTask.isDone();
	                boolean isCancelled = scheduledTask.isCancelled();
	                //System.out.println("Job status for job id: " + jobId + " - isDone: " + isDone + ", isCancelled: " + isCancelled);
	                return !isDone && !isCancelled; // Task is still running if not done or cancelled
	            }
	        }
	        return false; // Task not found
	    }
	    

}
