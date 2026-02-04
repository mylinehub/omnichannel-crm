package com.mylinehub.crm.whatsapp.taskscheduler;

import com.mylinehub.crm.service.CustomerService;
import com.mylinehub.crm.whatsapp.data.WhatsAppCustomerData;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerParameterDataDto;

import lombok.Data;

@Data
public class WhatsAppCleanCustomerDataRunnable implements Runnable {

    private String jobId;
	private CustomerService customerService;
	
    @Override
    public void run() {
        //System.out.println("[WhatsAppCleanCustomerDataRunnable] Job started. Job ID: " + jobId);
        try {
            WhatsAppCustomerParameterDataDto whatsAppCustomerParameterDataDto = new WhatsAppCustomerParameterDataDto();
            whatsAppCustomerParameterDataDto.setAction("clear-non-frequent-data");
            whatsAppCustomerParameterDataDto.setDeleteLastNDays(1);

            //System.out.println("[WhatsAppCleanCustomerDataRunnable] Calling cleanWhatsAppCustomerData with parameters: " 
                                //+ "action=" + whatsAppCustomerParameterDataDto.getAction() 
                                //+ ", deleteLastNDays=" + whatsAppCustomerParameterDataDto.getDeleteLastNDays());

            WhatsAppCustomerData.workWithWhatsAppCustomerData(whatsAppCustomerParameterDataDto);

            //System.out.println("[WhatsAppCleanCustomerDataRunnable] Job completed successfully. Job ID: " + jobId);
       
            //Need code to update customer memory data to database
        } catch (Exception e) {
            //System.out.println("[WhatsAppCleanCustomerDataRunnable] Exception occurred during job execution. Job ID: " + jobId);
            e.printStackTrace();
        }
    }
}
