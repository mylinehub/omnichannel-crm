package com.mylinehub.crm.ami.service.notificaton;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.data.dto.CallToExtensionDTO;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.utils.LoggerUtils;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class SendDialerCallDetailToExtensionService {
	
	public void  sendMessageToExtension(CallToExtensionDTO callToExtensionDTO, Employee employee) {	
		
		LoggerUtils.log.debug("SendCurrentCallDetailToExtensionService");
		
		ObjectMapper mapper = new ObjectMapper();
		BotInputDTO msg = new BotInputDTO();
		
		msg = new BotInputDTO();
	  	msg.setDomain(employee.getDomain());
	  	msg.setExtension(employee.getExtension());
	  	msg.setFormat("json");
	  	try {
			msg.setMessage(mapper.writeValueAsString(callToExtensionDTO));
		} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	  	msg.setMessagetype("previewCall");
	  	msg.setOrganization(employee.getOrganization());
	  	try {
	       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
	    }
	    catch(Exception e)
	    {
		   e.printStackTrace();
	    } 
	}
}
