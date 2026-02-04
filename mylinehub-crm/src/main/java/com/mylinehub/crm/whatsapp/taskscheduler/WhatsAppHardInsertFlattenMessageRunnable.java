package com.mylinehub.crm.whatsapp.taskscheduler;

import com.mylinehub.crm.whatsapp.data.WhatsAppFlattenMessageConversation;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppFlattenMessageParameterDTO;
import com.mylinehub.crm.whatsapp.repository.WhatsAppFlattenMessageRepository;

import lombok.Data;

@Data
public class WhatsAppHardInsertFlattenMessageRunnable implements Runnable {

    private WhatsAppFlattenMessageRepository whatsAppFlattenMessageRepository;
    private String jobId;

    @Override
    public void run() {
        //System.out.println("[WhatsAppHardInsertFlattenMessageRunnable] Job started. Job ID: " + jobId);
        try {
            WhatsAppFlattenMessageParameterDTO whatsAppFlattenMessageParameterDTO = new WhatsAppFlattenMessageParameterDTO();

            // Backup phase
            //System.out.println("[WhatsAppHardInsertFlattenMessageRunnable] Setting action to 'put-all-to-backup'");
            whatsAppFlattenMessageParameterDTO.setAction("put-all-to-backup");
            whatsAppFlattenMessageParameterDTO.setWhatsAppFlattenMessageRepository(whatsAppFlattenMessageRepository);

            //System.out.println("[WhatsAppHardInsertFlattenMessageRunnable] Executing workOnWhatsAppFlattenMessageMemoryList()");
            WhatsAppFlattenMessageConversation.workOnWhatsAppFlattenMessageMemoryList(whatsAppFlattenMessageParameterDTO);

            // Database insert phase
            //System.out.println("[WhatsAppHardInsertFlattenMessageRunnable] Setting action to 'put-all-to-database'");
            whatsAppFlattenMessageParameterDTO.setAction("put-all-to-database");

            //System.out.println("[WhatsAppHardInsertFlattenMessageRunnable] Executing workOnBackupWhatsAppFlattenMessageMemoryList()");
            WhatsAppFlattenMessageConversation.workOnBackupWhatsAppFlattenMessageMemoryList(whatsAppFlattenMessageParameterDTO);

            //System.out.println("[WhatsAppHardInsertFlattenMessageRunnable] Job completed successfully. Job ID: " + jobId);
        } catch (Exception e) {
            //System.out.println("[WhatsAppHardInsertFlattenMessageRunnable] Exception occurred. Job ID: " + jobId);
            e.printStackTrace();
        }
    }
}
