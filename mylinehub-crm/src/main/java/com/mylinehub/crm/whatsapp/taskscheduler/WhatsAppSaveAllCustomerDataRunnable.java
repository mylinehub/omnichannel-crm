package com.mylinehub.crm.whatsapp.taskscheduler;

import com.mylinehub.crm.service.CustomerService;
import com.mylinehub.crm.whatsapp.data.WhatsAppCustomerData;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerParameterDataDto;

import lombok.Data;

@Data
public class WhatsAppSaveAllCustomerDataRunnable implements Runnable {

    private String jobId;
	private CustomerService customerService;
	
    @Override
    public void run() {
        //System.out.println("[whatsAppSaveAllCustomerDataRunnable] Job started. Job ID: " + jobId);
        try {
        	
        	if(customerService == null) {
	            WhatsAppCustomerParameterDataDto whatsAppCustomerParameterDataDto = new WhatsAppCustomerParameterDataDto();
	            whatsAppCustomerParameterDataDto.setAction("save-data-to-db");
	            whatsAppCustomerParameterDataDto.setDeleteLastNDays(2);
	
	            //System.out.println("[whatsAppSaveAllCustomerDataRunnable] Calling cleanWhatsAppCustomerData with parameters: " 
	                                //+ "action=" + whatsAppCustomerParameterDataDto.getAction() 
	                                //+ ", deleteLastNDays=" + whatsAppCustomerParameterDataDto.getDeleteLastNDays());
	
	            WhatsAppCustomerData.workWithWhatsAppCustomerData(whatsAppCustomerParameterDataDto);
        	}
        	else {
        		
        		WhatsAppCustomerData.saveCustomerFlagsForCleaning(customerService);
        		
        	}
            //System.out.println("[whatsAppSaveAllCustomerDataRunnable] Job completed successfully. Job ID: " + jobId);
       
            //Need code to update customer memory data to database
        } catch (Exception e) {
            //System.out.println("[whatsAppSaveAllCustomerDataRunnable] Exception occurred during job execution. Job ID: " + jobId);
            e.printStackTrace();
        }
    }
}
