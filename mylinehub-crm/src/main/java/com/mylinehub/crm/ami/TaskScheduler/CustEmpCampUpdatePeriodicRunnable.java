package com.mylinehub.crm.ami.TaskScheduler;

import com.mylinehub.crm.ami.service.SaveMemoryDataToDatabaseService;
import com.mylinehub.crm.utils.LoggerUtils;

import lombok.Data;

@Data
public class CustEmpCampUpdatePeriodicRunnable implements Runnable {

    SaveMemoryDataToDatabaseService campaignDataToDatabaseService;
    String jobId;

    @Override
    public void run() {

        LoggerUtils.log.debug("CustEmpCampUpdatePeriodicRunnable jobId={}", jobId);

        if (campaignDataToDatabaseService == null) {
            LoggerUtils.log.error("CustEmpCampUpdatePeriodicRunnable campaignDataToDatabaseService is null jobId={}", jobId);
            return;
        }

        try {
            LoggerUtils.log.debug("Updating Campaign Last Record Number jobId={}", jobId);
            campaignDataToDatabaseService.saveLastCommonRecordForAllCampaign();

            LoggerUtils.log.debug("Updating Customer Data jobId={}", jobId);
            campaignDataToDatabaseService.saveAllCustomerDataInMemoryToDatabase();

            LoggerUtils.log.debug("Updating Employee Data jobId={}", jobId);
            campaignDataToDatabaseService.saveAllEmployeeDataInMemoryToDatabase();

            LoggerUtils.log.debug("saveCustomerAndEmployeeForAllCampaigns jobId={}", jobId);
            campaignDataToDatabaseService.saveCustomerAndEmployeeForAllCampaigns();

        } catch (Exception e) {
            LoggerUtils.log.error("CustEmpCampUpdatePeriodicRunnable error jobId={} msg={}", jobId, String.valueOf(e.getMessage()), e);
        }
    }
}
