package com.mylinehub.crm.whatsapp.taskscheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mylinehub.crm.whatsapp.repository.WhatsAppChatHistoryRepository;
import com.mylinehub.crm.whatsapp.service.WhatsAppChatHistoryService;

import lombok.Data;

@Data
public class WhatsAppHardInsertChatHistoryRunnable implements Runnable {

    private WhatsAppChatHistoryService whatsAppChatHistoryService;
    private WhatsAppChatHistoryRepository whatsAppChatHistoryRepository;
    private String jobId;

    @Override
    public void run() {
        //System.out.println("[WhatsAppHardInsertChatHistoryRunnable] Job started. Job ID: " + jobId);
        try {
            //System.out.println("[WhatsAppHardInsertChatHistoryRunnable] Calling hardAppendChatHistoryByPhoneNumberMainAndPhoneNumberWithAndOrganization()");
            whatsAppChatHistoryService.hardAppendChatHistoryByPhoneNumberMainAndPhoneNumberWithAndOrganization(whatsAppChatHistoryRepository);
            //System.out.println("[WhatsAppHardInsertChatHistoryRunnable] Job completed successfully. Job ID: " + jobId);
        } catch (JsonProcessingException e) {
            //System.out.println("[WhatsAppHardInsertChatHistoryRunnable] JsonProcessingException occurred. Job ID: " + jobId);
            e.printStackTrace();
        } catch (Exception e) {
            //System.out.println("[WhatsAppHardInsertChatHistoryRunnable] Unexpected exception occurred. Job ID: " + jobId);
            e.printStackTrace();
        }
    }
}
