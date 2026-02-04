package com.mylinehub.crm.whatsapp.controller.chat;

import static com.mylinehub.crm.controller.ApiMapping.WHATS_APP_WEBHOOK_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.util.Date;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ComponentDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppErrorMessages;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.repository.WhatsAppErrorMessageRepository;
import com.mylinehub.crm.whatsapp.service.WhatsAppIntegrationInboundService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(produces="application/json", path = WHATS_APP_WEBHOOK_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class WhatsAppWebHookController {

	private final WhatsAppIntegrationInboundService whatsAppInboundIntegrationService;
	private final WhatsAppErrorMessageRepository whatsAppErrorMessageRepository;
	private final Environment env;
	
	@GetMapping("/{supportPhoneNumber}/{appSecret}")
    public ResponseEntity<String> verify(@PathVariable String supportPhoneNumber, @PathVariable String appSecret,@RequestParam("hub.mode") String mode, @RequestParam("hub.verify_token") String verifyToken,
                                         @RequestParam("hub.challenge") String challenge) {
		
		String supportPhoneNumberParent = env.getProperty("spring.whatsapp.phone");
		String supportPhoneNumberVerificationTokenParent = env.getProperty("spring.whatsapp.verifyToken");
		
		//System.out.println("******************Verify Whats App Web Hook******************");
		//System.out.println("phoneNumber : "+supportPhoneNumber);
		//System.out.println("appSecret : "+appSecret);
		Map<String,WhatsAppPhoneNumber> phoneMap = WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(supportPhoneNumber, null, "get-one");
		
		if(supportPhoneNumberParent.equals(supportPhoneNumber)) {
			//System.out.println("Support phone is parent");
			
			String VERIFY_TOKEN = supportPhoneNumberVerificationTokenParent;
			
			if (mode != null && verifyToken != null) {
			    if (mode.equals("subscribe") && verifyToken.equals(VERIFY_TOKEN)) {
			          //System.out.println("Webhook Verified");
			          return ResponseEntity.ok(challenge);
			       }
			 }
			
		}
		else {
			
			//System.out.println("Support phone is not parent");
			
			if(phoneMap != null && phoneMap.size() > 0)
			{
				WhatsAppPhoneNumber phoneNumberObject = phoneMap.get(supportPhoneNumber);
				if(phoneNumberObject != null) {
					
					//System.out.println("******************Found phone number******************");
					
					String VERIFY_TOKEN = phoneNumberObject.getVerifyToken();
					
					if (mode != null && verifyToken != null) {
					    if (mode.equals("subscribe") && verifyToken.equals(VERIFY_TOKEN)) {
					          //System.out.println("Webhook Verified");
					          return ResponseEntity.ok(challenge);
					       }
					 }
				}
			}
		}
		
        return ResponseEntity.status(403).body(null);
    }

	
    @PostMapping("/{supportPhoneNumber}/{appSecret}")
    public ResponseEntity<String> webhook(@PathVariable String supportPhoneNumber,@PathVariable String appSecret,@RequestBody String incomingMessage) throws Exception {
        
    	String phoneNumberMain = "";
    	
        try {
        	
    		System.out.println("*******************************************************************");
        	System.out.println("******************Whats App Web Hook Message******************");
        	System.out.println("*******************************************************************");
        	System.out.println("Support phone number : "+supportPhoneNumber);
            //System.out.println("Date : " + new Date()); //TODO : change to message timing
            //System.out.println("incomingMessage : " + incomingMessage);
            
            
            phoneNumberMain = whatsAppInboundIntegrationService.extract_phone_number_main(incomingMessage);
            //System.out.println("After retriving cloud phone number : " + phoneNumberMain);

        	Map<String,WhatsAppPhoneNumber> phoneMap = WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(phoneNumberMain, null, "get-one");
    		if(phoneMap != null && phoneMap.size() > 0)
    		{
    			WhatsAppPhoneNumber phoneNumberObject = phoneMap.get(phoneNumberMain);
    			if(phoneNumberObject != null) {
    				if(phoneNumberObject.getWhatsAppProject().getAppSecret().equals(appSecret)) {
        	        	ObjectMapper mapper = new ObjectMapper();
        	        	ComponentDto component = mapper.readValue(incomingMessage, ComponentDto.class);
        	        	//System.out.println("component : " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(component));
        	        	whatsAppInboundIntegrationService.digestWhatsAppInputMessageAndSendToOwn(phoneNumberObject,phoneNumberMain, component);
        	        	//System.out.println("After digesting inbound message from whats app client");
    				}
    			}
    		}
    		else {
    			//System.out.println("*********************Phone Number Main is Null************************");
            	
            	WhatsAppErrorMessages whatsAppErrorMessages = new WhatsAppErrorMessages();
            	whatsAppErrorMessages.setError("Phone number main not found :"+phoneNumberMain);
            	whatsAppErrorMessages.setMessageInput(incomingMessage);
            	whatsAppErrorMessages.setPhoneNumberMain(phoneNumberMain);
            	whatsAppErrorMessageRepository.save(whatsAppErrorMessages);
    		}
    		
            //System.out.println("*********************After Message Injestion ***************************");
         }
        catch(Exception e)
        {
        	//System.out.println("*******************************************************************");
        	//System.out.println("***********************       ERROR       *************************");
        	//System.out.println("*******************************************************************");
        	//System.out.println("Cloud phone number : "+phoneNumberMain);
            //System.out.println("Date : " + new Date()); //TODO : change to message timing
            //System.out.println("incomingMessage : " + incomingMessage);
            //System.out.println("*******************************************************************");
            //System.out.println("*******************************************************************");
            
        	e.printStackTrace();
        	
        	WhatsAppErrorMessages whatsAppErrorMessages = new WhatsAppErrorMessages();
        	whatsAppErrorMessages.setError(e.getMessage());
        	whatsAppErrorMessages.setMessageInput(incomingMessage);
        	whatsAppErrorMessages.setPhoneNumberMain(phoneNumberMain);
        	whatsAppErrorMessageRepository.save(whatsAppErrorMessages);
        	
        }
        
        return status(HttpStatus.OK).body("successful");
        
    }
}
