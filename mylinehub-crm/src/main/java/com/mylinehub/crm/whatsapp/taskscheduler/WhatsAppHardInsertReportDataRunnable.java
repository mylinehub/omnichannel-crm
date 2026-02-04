package com.mylinehub.crm.whatsapp.taskscheduler;

import com.mylinehub.crm.whatsapp.data.WhatsAppReportingData;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppReportDataParameterDTO;
import com.mylinehub.crm.whatsapp.repository.WhatsAppNumberReportRepository;

import lombok.Data;

@Data
public class WhatsAppHardInsertReportDataRunnable implements Runnable {

    private WhatsAppNumberReportRepository whatsAppNumberReportRepository;
    private String jobId;

    @Override
    public void run() {
        //System.out.println("[WhatsAppHardInsertReportDataRunnable] Job started. Job ID: " + jobId);
        try {
            //System.out.println("[WhatsAppHardInsertReportDataRunnable] Action: send-to-backup");
            WhatsAppReportDataParameterDTO whatsAppReportDataParameterDTO = new WhatsAppReportDataParameterDTO();
            whatsAppReportDataParameterDTO.setAction("send-to-backup");
            whatsAppReportDataParameterDTO.setWhatsAppNumberReportRepository(whatsAppNumberReportRepository);

            WhatsAppReportingData.workWithWhatsAppReportMapData(whatsAppReportDataParameterDTO);

            //System.out.println("[WhatsAppHardInsertReportDataRunnable] Action: send-backup-to-database");
            whatsAppReportDataParameterDTO.setAction("send-backup-to-database");

            WhatsAppReportingData.workWithBackupWhatsAppReportMapData(whatsAppReportDataParameterDTO);

            //System.out.println("[WhatsAppHardInsertReportDataRunnable] Job completed successfully. Job ID: " + jobId);
        } catch (Exception e) {
            //System.out.println("[WhatsAppHardInsertReportDataRunnable] Exception occurred. Job ID: " + jobId);
            e.printStackTrace();
        }
    }
}
