package com.mylinehub.crm.TaskScheduler;

import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mylinehub.crm.service.ChatHistoryService;

import lombok.Data;

@Data
public class HardInsertChatHistoryRunnable  implements Runnable{

	 String jobId;
     public String organization;
     public String extensionMain;
     public String extensionWith;
     private ApplicationContext applicationContext;
     
	@Override
	public void run() {
		// TODO Auto-generated method stub
//		System.out.println("Inside Hard Insert Chat Runnable");
//		System.out.println("Getting chat history service bean");
		ChatHistoryService chatHistoryService = applicationContext.getBean(ChatHistoryService.class);
		try {
//			System.out.println("Hard appending data now");
			chatHistoryService.hardAppendChatHistoryByExtensionMainAndExtensionWithAndOrganization(extensionMain, extensionWith, organization);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
