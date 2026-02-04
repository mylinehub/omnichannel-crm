package com.mylinehub.crm.ami.TaskScheduler;

import org.springframework.context.ApplicationContext;

import com.mylinehub.crm.ami.autodialer.LoopInToDialOrSendMessage;
import com.mylinehub.crm.entity.Campaign;

import lombok.Data;

@Data
public class DialReinitiateOnlyCustomerDialerRunnable  implements Runnable{

	Campaign campaign;
	ApplicationContext applicationContext;
	String jobId;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			System.out.println("DialReinitiateOnlyCustomerDialerRunnable");
			new LoopInToDialOrSendMessage().initiateAutomationOnlyCustomer(campaign, applicationContext,false);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		
	}
	
}
