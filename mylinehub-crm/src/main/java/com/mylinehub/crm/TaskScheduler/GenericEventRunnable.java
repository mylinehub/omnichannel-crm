package com.mylinehub.crm.TaskScheduler;

import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import lombok.Data;

@Data
public class GenericEventRunnable implements Runnable{

//    private String organization;
//    private ApplicationContext applicationContext;
    
	BotInputDTO msg;
    
    @Override
    public void run() {
        System.out.println("GenericEventRunnable");
        try {
        	  MyStompSessionHandler.sendMessage("/mylinehub/sendevent", msg);
        }
        catch(Exception e)
        {
      	  e.printStackTrace();
        }
    }
}
