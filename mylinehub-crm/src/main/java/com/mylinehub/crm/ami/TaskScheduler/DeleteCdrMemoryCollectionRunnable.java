package com.mylinehub.crm.ami.TaskScheduler;

import com.mylinehub.crm.data.CDRMemoryCollection;
import com.mylinehub.crm.utils.LoggerUtils;

import lombok.Data;

@Data
public class DeleteCdrMemoryCollectionRunnable implements Runnable {

    String jobId;

    @Override
    public void run() {
        try {
            LoggerUtils.log.debug("DeleteCdrMemoryCollectionRunnable for backup jobId={}", jobId);

            // IMPORTANT: do not fetch + pass map around (reference leak risk)
            // Use clear to wipe backup safely
            CDRMemoryCollection.workWithCDRBackupInterimData(null, null, "clear");

        } catch (Exception e) {
            LoggerUtils.log.error("DeleteCdrMemoryCollectionRunnable error jobId={} msg={}", jobId, String.valueOf(e.getMessage()), e);
        }
    }
}
