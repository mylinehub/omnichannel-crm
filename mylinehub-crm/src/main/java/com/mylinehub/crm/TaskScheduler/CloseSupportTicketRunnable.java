package com.mylinehub.crm.TaskScheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.context.ApplicationContext;

import com.mylinehub.crm.TaskSchedule.Service.DeleteScheduleJobFromDatabaseService;
import com.mylinehub.crm.entity.dto.CampaignDTO;
import com.mylinehub.crm.repository.SupportTicketRepository;
import com.mylinehub.crm.service.CampaignService;


import lombok.Data;

@Data
public class CloseSupportTicketRunnable implements Runnable{

		private String jobId;
        private ApplicationContext applicationContext;
        
	    @Override
	    public void run() {
	        System.out.println("jobId ID: " + jobId);
	        SupportTicketRepository supportTicketRepository = applicationContext.getBean(SupportTicketRepository.class);

	         try {
	        	// Calculate cutoff 48 hours ago
	        	 Instant cutoffTime = Instant.now().minus(48, ChronoUnit.HOURS);
	        	 // Call the repository method
	        	 int closedCount = supportTicketRepository.closeOldOpenTickets(cutoffTime);
	        	 System.out.println("closedCount : " + closedCount);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	    }
}