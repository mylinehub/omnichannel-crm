package com.mylinehub.crm.whatsapp.api.cloud.wrapper.controller;

import static com.mylinehub.crm.controller.ApiMapping.WHATS_APP_SEND_MESSAGE_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.data.dto.MultiPartFileDTO;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.enums.FILE_STORE_REQUEST_TYPE;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.LogRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.media.OkHttpDownloadMediaClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.media.OkHttpRetrieveMediaUrlClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendAudioMessageByIdClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendAudioMessageByUrlClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendDocumentMessageByIdClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendDocumentMessageByUrlClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendImageMessageByIdClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendImageMessageByUrlClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendStickerMessageByIdClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendStickerMessageByUrlClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendTemplateMessageClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendTextMessageClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendVideoMessageByIdClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendVideoMessageByUrlClient;
import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ComponentDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.enums.MESSAGING_PRODUCT;
import com.mylinehub.crm.whatsapp.enums.SEND_MESSAGE_KEYS;
import com.mylinehub.crm.whatsapp.requests.SendWhatsAppControllerRequest;

import lombok.AllArgsConstructor;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

@RestController
@RequestMapping(produces="application/json", path = WHATS_APP_SEND_MESSAGE_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class MessageController {
	
	 private final EmployeeRepository employeeRepository;
	 private final OkHttpSendAudioMessageByIdClient okHttpSendAudioMessageByIdClient;
	 private final OkHttpSendAudioMessageByUrlClient okHttpSendAudioMessageByUrlClient;
	 private final OkHttpSendDocumentMessageByIdClient okHttpSendDocumentMessageByIdClient;
	 private final OkHttpSendDocumentMessageByUrlClient okHttpSendDocumentMessageByUrlClient;
	 private final OkHttpSendImageMessageByIdClient okHttpSendImageMessageByIdClient;
	 private final OkHttpSendImageMessageByUrlClient okHttpSendImageMessageByUrlClient;
	 private final OkHttpSendStickerMessageByIdClient okHttpSendStickerMessageByIdClient;
	 private final OkHttpSendStickerMessageByUrlClient okHttpSendStickerMessageByUrlClient;
	 private final OkHttpSendTemplateMessageClient okHttpSendTemplateMessageClient;
	 private final OkHttpSendTextMessageClient okHttpSendTextMessageClient;
	 private final OkHttpSendVideoMessageByIdClient okHttpSendVideoMessageByIdClient;
	 private final OkHttpSendVideoMessageByUrlClient okHttpSendVideoMessageByUrlClient;
	 private final OkHttpDownloadMediaClient okHttpDownloadMediaClient;
	 private final OkHttpRetrieveMediaUrlClient okHttpRetrieveMediaUrlClient;
	 private final LogRepository logRepository;
	 private final JwtConfiguration jwtConfiguration;
	 private final SecretKey secretKey;
	 private Environment env;
	 
	 @PostMapping("/sendTextMessage")
	 @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	 public ResponseEntity<String> sendTextMessage(@RequestBody SendWhatsAppControllerRequest sendWhatsAppControllerRequest,@RequestParam String oldPhone , @RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
		    
			//System.out.println("Let us update an employee");
		 	String toReturn = null;
			
	        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	        //System.out.println("Email : "+employeeDTO.getEmail());
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		try {
		    		JSONObject jsonObject = okHttpSendTextMessageClient.sendMessage(MESSAGING_PRODUCT.whatsapp.name(),sendWhatsAppControllerRequest.getCustomerPhoneNumber(),sendWhatsAppControllerRequest.getPreviousMessageId(),sendWhatsAppControllerRequest.isPreviewURL(),sendWhatsAppControllerRequest.getTextBody(),sendWhatsAppControllerRequest.getVersion(),sendWhatsAppControllerRequest.getPhoneNumberID(),sendWhatsAppControllerRequest.getToken());
		    		toReturn = jsonObject.toString();
	    		}
	    		catch(Exception e)
	    		{
	    			throw e;
	    		}
	    		return status(HttpStatus.OK).body(toReturn);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	} 	
		} 

	 @PostMapping("/sendTemplateMessage")
	 @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	 public ResponseEntity<Response> sendTemplateMessage(@RequestBody SendWhatsAppControllerRequest sendWhatsAppControllerRequest,@RequestParam String oldPhone , @RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
		    
			//System.out.println("Let us update an employee");
		 	Response toReturn = null;
			
	        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	        //System.out.println("Email : "+employeeDTO.getEmail());
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		try {    		
	    			

//	    			WhatsAppPhoneNumberTemplates template = whatsAppPhoneNumberTemplatesRepository.getOne(sendWhatsAppControllerRequest.getTemplateId());
//	    			
//	    			if(template == null)
//	    				throw new Exception("Template not found, cannot send message");
//	    			
	    			
//	    			Customers customer = null;
//	    			if(sendWhatsAppControllerRequest.getCustomerPhoneNumber() != null && sendWhatsAppControllerRequest.getOrganization() !=null && sendWhatsAppControllerRequest.getCustomerPhoneNumber() != "" && sendWhatsAppControllerRequest.getOrganization() !="")
//	    			customer = customerService.getCustomerByPhoneNumberAndOrganization(sendWhatsAppControllerRequest.getCustomerPhoneNumber(), sendWhatsAppControllerRequest.getOrganization());
//	    			
	    //
//	    			Purchases purchase = null;
//	    			if(sendWhatsAppControllerRequest.getPurchaseId() != null || sendWhatsAppControllerRequest.getPurchaseId() != 0)
//	    			purchase = purchasesRepository.getOne(sendWhatsAppControllerRequest.getPurchaseId());
	    //
	    			
//		    		toReturn = okHttpSendTemplateMessageClient.sendMessage(MESSAGING_PRODUCT.whatsapp.name(),sendWhatsAppControllerRequest.getRecipientPhoneNumber(),sendWhatsAppControllerRequest.getTemplateName(),sendWhatsAppControllerRequest.getVariables(), sendWhatsAppControllerRequest.getLanguageCode(),sendWhatsAppControllerRequest.getVersion(),sendWhatsAppControllerRequest.getPhoneNumberID(),sendWhatsAppControllerRequest.getToken());
	    		}
	    		catch(Exception e)
	    		{
	    			throw e;
	    		}
	    		return status(HttpStatus.OK).body(toReturn);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	} 	
		} 


	 @PostMapping("/getWhatsAppMediaUrl")
	 @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	 public ResponseEntity<String> getWhatsAppMediaUrl(@RequestBody SendWhatsAppControllerRequest sendWhatsAppControllerRequest,@RequestHeader (name="Authorization") String token) throws Exception{
		    
			//System.out.println("Let us update an employee");
		 	String url = null;
		 	WhatsAppPhoneNumber phoneNumberObject = null;
	        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	        //System.out.println("Email : "+employeeDTO.getEmail());
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(sendWhatsAppControllerRequest.getOrganization().trim()))
	    	{
	    		try { 
	    			
	    		Map<String,WhatsAppPhoneNumber> phoneMap = WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(sendWhatsAppControllerRequest.getPhoneNumberMain(), null, "get-one");
	        		if(phoneMap != null && phoneMap.size() > 0)
	        		{
	        			phoneNumberObject = phoneMap.get(sendWhatsAppControllerRequest.getPhoneNumberMain());
	        			if(phoneNumberObject == null) {
	        				  throw new IOException("Sender Phone Number was null : " + sendWhatsAppControllerRequest.getPhoneNumberMain());
	   			     	}
	        		}
	   	
	        		
	        		JSONObject jsonObject = okHttpRetrieveMediaUrlClient.sendMessage(sendWhatsAppControllerRequest.getId(), phoneNumberObject.getWhatsAppProject().getApiVersion(), phoneNumberObject.getPhoneNumberID(), phoneNumberObject.getWhatsAppProject().getAccessToken());
					if(jsonObject != null)
					 {
						 url = String.valueOf(jsonObject.get(SEND_MESSAGE_KEYS.url.name()));
					 }
			     
	    		}
	    		catch(Exception e)
	    		{
	    			throw e;
	    		}
	    		return status(HttpStatus.OK).body(url);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(url);
	    	} 	
		} 

}
