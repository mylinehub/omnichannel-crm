package com.mylinehub.crm.TaskScheduler;

import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import lombok.Data;

@Data
public class ExtensionEventRunnable implements Runnable{

//  private String organization;
//  private ApplicationContext applicationContext;
  
  BotInputDTO msg ;
    
  @Override
  public void run() {
      System.out.println("ExtensionEventRunnable");
      try {
          MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
      }
      catch(Exception e)
      {
    	  e.printStackTrace();
      }
  }
}