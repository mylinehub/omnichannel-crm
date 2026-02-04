package com.mylinehub.crm.TaskScheduler;


import org.springframework.context.ApplicationContext;

import com.mylinehub.crm.TaskSchedule.Service.DeleteScheduleJobFromDatabaseService;
import com.mylinehub.crm.entity.dto.CampaignDTO;
import com.mylinehub.crm.service.CampaignService;
import lombok.Data;

@Data
public class StopCampaignRunnable implements Runnable{
	
	private String jobId;
    private Long campaignID;
    private String organization;
    private String fromExtension;
    private String domain;
    private ApplicationContext applicationContext;
    
    @Override
    public void run() {
        System.out.println("Campaign ID: " + campaignID);
        System.out.println("Organization: " + organization);
        
        CampaignService campaignService = applicationContext.getBean(CampaignService.class);
        DeleteScheduleJobFromDatabaseService deleteScheduleJobFromDatabaseService = applicationContext.getBean(DeleteScheduleJobFromDatabaseService.class);
        
        CampaignDTO campaignDTO = campaignService.getCampaignByIdAndOrganization(campaignID, organization);
        try {
        	if(campaignDTO!=null) {
        		campaignService.stopCampaignByOrganization(campaignDTO,fromExtension,domain);
        	}
        	else {
        		 System.out.println("Stop did not happen as campaign was not found");
        	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        deleteScheduleJobFromDatabaseService.deleteScheduleJobFromDatabaseIfExecuted(jobId);  
    }

    
    public void setOrganization(String organization) {
        this.organization=organization;
    }

}
