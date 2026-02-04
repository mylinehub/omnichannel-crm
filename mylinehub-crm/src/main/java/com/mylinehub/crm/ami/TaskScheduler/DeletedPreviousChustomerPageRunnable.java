package com.mylinehub.crm.ami.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import com.mylinehub.crm.data.DeletedCampaignData;
import com.mylinehub.crm.data.dto.CustomerAndItsCampaignDTO;
import com.mylinehub.crm.utils.LoggerUtils;

import lombok.Data;

@Data
public class DeletedPreviousChustomerPageRunnable implements Runnable {

    String jobId;

    @Override
    public void run() {

        LoggerUtils.log.debug("DeletedPreviousChustomerPageRunnable jobId={}", jobId);

        try {
            Map<String, CustomerAndItsCampaignDTO> allCustomers =
                    DeletedCampaignData.workWithAllDeletedCustomerData(null, null, "get");

            if (allCustomers == null || allCustomers.isEmpty()) {
                return;
            }

            for (Map.Entry<String, CustomerAndItsCampaignDTO> entry : allCustomers.entrySet()) {
                try {
                    String phoneNumber = entry.getKey();
                    CustomerAndItsCampaignDTO customerAndItsCampaignDTO = entry.getValue();

                    if (phoneNumber == null || phoneNumber.isEmpty()) {
                        continue;
                    }
                    if (customerAndItsCampaignDTO == null) {
                        continue;
                    }

                    Date deletedDate = customerAndItsCampaignDTO.getDeletedDate();
                    if (deletedDate == null) {
                        continue;
                    }

                    boolean result = isAtleastTwentyFiveMinutesAgo(deletedDate);

                    if (result) {
                        DeletedCampaignData.workWithAllDeletedCustomerData(phoneNumber, null, "delete");
                    }
                } catch (Exception e) {
                    LoggerUtils.log.error("DeletedPreviousChustomerPageRunnable loop error jobId={} msg={}",
                            jobId, String.valueOf(e.getMessage()), e);
                }
            }

            // Keep same behavior: fetch again (even if unused) as in your original code
            DeletedCampaignData.workWithAllDeletedCustomerData(null, null, "get");

        } catch (Exception e) {
            LoggerUtils.log.error("DeletedPreviousChustomerPageRunnable error jobId={} msg={}", jobId, String.valueOf(e.getMessage()), e);
        }
    }

    private static boolean isAtleastTwentyFiveMinutesAgo(Date date) {
        Instant instant = Instant.ofEpochMilli(date.getTime());
        Instant twentyFiveMinutesAgo = Instant.now().minus(Duration.ofMinutes(25));
        return instant.isBefore(twentyFiveMinutesAgo);
    }
}
