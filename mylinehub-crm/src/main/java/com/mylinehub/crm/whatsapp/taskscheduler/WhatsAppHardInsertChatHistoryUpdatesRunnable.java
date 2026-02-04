package com.mylinehub.crm.whatsapp.taskscheduler;

import com.mylinehub.crm.whatsapp.data.WhatsAppCurrentConversation;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppChatDataParameterDTO;
import com.mylinehub.crm.whatsapp.repository.WhatsAppChatHistoryRepository;

import lombok.Data;

@Data
public class WhatsAppHardInsertChatHistoryUpdatesRunnable implements Runnable {

    private WhatsAppChatHistoryRepository whatsAppChatHistoryRepository;
    private String jobId;

    @Override
    public void run() {
        System.out.println("[WhatsAppHardInsertChatHistoryUpdatesRunnable] Job started. Job ID: " + jobId);
        try {
            System.out.println("[WhatsAppHardInsertChatHistoryUpdatesRunnable] Preparing WhatsAppChatDataParameterDTO with action 'put-updates-to-database'");

            WhatsAppChatDataParameterDTO whatsAppChatDataParameterDTO = new WhatsAppChatDataParameterDTO();
            whatsAppChatDataParameterDTO.setAction("put-updates-to-database");
            whatsAppChatDataParameterDTO.setWhatsAppChatHistoryRepository(whatsAppChatHistoryRepository);

            System.out.println("[WhatsAppHardInsertChatHistoryUpdatesRunnable] Calling workOnUpdateChatHistoryObject()");
            WhatsAppCurrentConversation.workOnUpdateChatHistoryObject(whatsAppChatDataParameterDTO);

            System.out.println("[WhatsAppHardInsertChatHistoryUpdatesRunnable] Job completed successfully. Job ID: " + jobId);
        } catch (Exception e) {
            System.out.println("[WhatsAppHardInsertChatHistoryUpdatesRunnable] Exception occurred. Job ID: " + jobId);
            e.printStackTrace();
        }
    }
}
