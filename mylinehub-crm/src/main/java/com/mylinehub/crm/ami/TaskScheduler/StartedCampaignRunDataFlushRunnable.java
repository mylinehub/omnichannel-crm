package com.mylinehub.crm.ami.TaskScheduler;

import com.mylinehub.crm.data.StartedCampaignData;
import com.mylinehub.crm.service.CampaignRunTrackingService;
import com.mylinehub.crm.utils.LoggerUtils;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
public class StartedCampaignRunDataFlushRunnable implements Runnable {

    private String jobId;
    private CampaignRunTrackingService trackingService;

    /**
     * If true => force flush everything pending (used during shutdown).
     * If false => normal periodic flush (flushIfDue).
     */
    private boolean forceFlush = false;

    @Override
    public void run() {
        try {
        	
        	System.out.println("StartedCampaignRunDataFlushRunnable");
            // 1) campaigns with pending logs
            List<Long> pendingIds = StartedCampaignData.listCampaignIdsWithPendingLogs();

            // 2) campaigns with memory run summary (RUNNING/STOPPED/COMPLETED)
            List<Long> summaryIds = StartedCampaignData.listCampaignIdsWithRunSummary();

            Set<Long> all = new LinkedHashSet<>();
            if (pendingIds != null) all.addAll(pendingIds);
            if (summaryIds != null) all.addAll(summaryIds);

            System.out.println("pendingIds size : "+pendingIds.size());
            System.out.println("summaryIds size : "+summaryIds.size());
            System.out.println("all size : "+all.size());
            
            if (all.isEmpty()) return;

            for (Long campaignId : all) {
                if (campaignId == null) continue;

                try {
                    if (forceFlush) {
                        trackingService.flushNow(campaignId);
                    } else {
                        trackingService.flushIfDue(campaignId);
                    }
                } catch (Exception e) {
                    LoggerUtils.log.error(
                            "CampaignRunFlushRunnable error campaignId={} msg={}",
                            campaignId, String.valueOf(e.getMessage()), e
                    );
                }
            }
            
            System.out.println("After StartedCampaignRunDataFlushRunnable");
            
        } catch (Exception e) {
            LoggerUtils.log.error(
                    "CampaignRunFlushRunnable fatal msg={}",
                    String.valueOf(e.getMessage()), e
            );
        }
    }
}
