package com.mylinehub.crm.whatsapp.service;

import java.util.Date;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.data.TrackedSchduledJobs;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.rag.dto.LanguageAndHeuristicCheckResponse;
import com.mylinehub.crm.rag.service.AssistantService;
import com.mylinehub.crm.rag.service.EmbeddingService;
import com.mylinehub.crm.rag.service.FileProcessingService;
import com.mylinehub.crm.rag.service.PromptBuilderHeuristicAndEnglishLanguageConvertorService;
import com.mylinehub.crm.rag.service.PromptBuilderSummarizeService;
import com.mylinehub.crm.repository.SupportTicketRepository;
import com.mylinehub.crm.service.CurrentTimeInterface;
import com.mylinehub.crm.service.SchedulerService;
import com.mylinehub.crm.whatsapp.data.WhatsAppCustomerData;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerDataDto;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerParameterDataDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.enums.MESSAGE_TYPE;
import com.mylinehub.crm.whatsapp.taskscheduler.WhatsAppSendCustomerAIMessageRunnable;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
public class WhatsAppAIService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
	
	private int heuristicWaitTimeSeconds = 5;
	private final Environment env;
	private final AssistantService assistantService;
	private final EmbeddingService embeddingService;
	private final SchedulerService schedulerService;
	private final FileProcessingService fileProcessingService;
    private final ObjectMapper objectMapper;
    private final SupportTicketRepository supportTicketRepository;
    private final ApplicationContext applicationContext;
    private final PromptBuilderHeuristicAndEnglishLanguageConvertorService promptBuilderHeuristicAndEnglishLanguageConvertorService;
    private final PromptBuilderSummarizeService promptBuilderSummarizeService;
    
    
	WhatsAppAIService(PromptBuilderSummarizeService promptBuilderSummarizeService,PromptBuilderHeuristicAndEnglishLanguageConvertorService promptBuilderHeuristicAndEnglishLanguageConvertorService,ApplicationContext applicationContext,SupportTicketRepository supportTicketRepository,ObjectMapper objectMapper,FileProcessingService fileProcessingService,SchedulerService schedulerService,Environment env,AssistantService assistantService,EmbeddingService embeddingService){
	
		//System.out.println("Calling Whatsapp AI service constructor");
		
		//System.out.println("Setting autowired properties");
		this.promptBuilderSummarizeService = promptBuilderSummarizeService;
		this.env = env;
		this.assistantService = assistantService;
		this.embeddingService=embeddingService; 
		this.schedulerService = schedulerService;
		this.fileProcessingService = fileProcessingService;
		this.objectMapper = objectMapper;
		this.supportTicketRepository = supportTicketRepository;
		this.applicationContext =applicationContext;
		this.promptBuilderHeuristicAndEnglishLanguageConvertorService = promptBuilderHeuristicAndEnglishLanguageConvertorService;
	}
	
	
	//Function currently not in use / integrated with AI system.
    public boolean aiAutoReplyPrecheck(Customers currentCustomer,WhatsAppPhoneNumber phoneNumberObject, Organization organization, WhatsAppChatHistory chatHistory) {
        boolean toReturn = true;
       //System.out.println("[aiAutoReplyPrecheck]  AI precheck started");
        try {
        		if(chatHistory.getMessageType().equalsIgnoreCase(MESSAGE_TYPE.text.name()) || chatHistory.getMessageType().equalsIgnoreCase(MESSAGE_TYPE.message.name())){
        			//System.out.println("[aiAutoReplyPrecheck]  organization.getTotalWhatsAppMessagesAmountSpend() : "+organization.getTotalWhatsAppMessagesAmountSpend());
        			//System.out.println("[aiAutoReplyPrecheck]  organization.getTotalWhatsAppMessagesAmount() : "+organization.getTotalWhatsAppMessagesAmount());
        			if(organization.getTotalWhatsAppMessagesAmountSpend() < organization.getTotalWhatsAppMessagesAmount())
                	{
                		//System.out.println("Organization has balance");
                		if(currentCustomer.isAutoWhatsAppAIReply())
                    	{
                			//System.out.println("Customer is allowed: AI Messages");
                			toReturn = aiAutoReplyCustomerDenyCheck(currentCustomer, phoneNumberObject, organization, chatHistory);
                    	}
                		else {
                    		//Organization does not have enough money for AI automation.
                    		//System.out.println("[aiAutoReplyPrecheck] Customer AI rsponse is permanently switched off by admin.");
                    		toReturn = false;
                    	}	
                	}
                	else {
                		//Organization does not have enough money for AI automation.
                		//System.out.println("[aiAutoReplyPrecheck] Organization does not have enough money for AI automation.");
                		toReturn = false;
                	}
            	}
            	else {
            		throw new Exception("Cannot response to any other message type other than text") ;
            	}
        } catch (Exception e) {
            System.err.println("[aiAutoReplyPrecheck] Exception occurred: " + e.getMessage());
            e.printStackTrace();
            toReturn = false;
        }
        
       //System.out.println("[aiAutoReplyPrecheck] Finished AI auto reply process with result: " + toReturn);
        return toReturn;
    }
    
    
    
    //Function currently not in use / integrated with AI system.
    public boolean aiAutoReplyCustomerDenyCheck(Customers currentCustomer,WhatsAppPhoneNumber phoneNumberObject, Organization organization, WhatsAppChatHistory chatHistory) {
        boolean toReturn = true;
//       //System.out.println("[aiAutoReply] Start AI auto reply process started");
        try {
        	
//        	//System.out.println("Customer : "+objectMapper.writeValueAsString(currentCustomer));
        	// -----------------------------------------------
        	// Verify if customer is off boarded from auto AI for few hours because of his wish
        	// -----------------------------------------------
        	//System.out.println("[aiAutoReply] Verify if customer is off boarded from auto AI for few hours because of his wish");
        	WhatsAppCustomerParameterDataDto dto = new WhatsAppCustomerParameterDataDto();
        	dto.setAction("get-one");
        	dto.setPhoneNumber(currentCustomer.getPhoneNumber());
        	dto.setOrganization(organization);
        	Map<String, WhatsAppCustomerDataDto> customerDataMap = WhatsAppCustomerData.workWithWhatsAppCustomerData(dto);

        	if (customerDataMap != null && customerDataMap.containsKey(currentCustomer.getPhoneNumber()+organization.getOrganization().trim())) {
        		WhatsAppCustomerDataDto customerMemoryData = customerDataMap.get(currentCustomer.getPhoneNumber()+organization.getOrganization().trim());

        	    if (customerMemoryData != null) {
        	    	if(customerMemoryData.isBlockedForAI()) {
        	    		//System.out.println("Customer is temporarily blocked for AI Messages");
        	    		Date lastBlockedTime = customerMemoryData.getLastBlockedTime();
            	        if (lastBlockedTime != null) {
            	            long diffMillis = new Date().getTime() - lastBlockedTime.getTime();
            	            long diffHours = diffMillis / (1000 * 60 * 60);
            	           //System.out.println("diffMillis : "+diffMillis);
            	            if (diffHours < WhatsAppCustomerData.aiBlockTimeHours) {  // still within block period
            	               //System.out.println("[aiAutoReply] Customer is still blocked for AI auto reply. Hours since block: " + diffHours);
            	                return false;
            	            } else {
            	            	//System.out.println("Unblock the customer since block time has expired");
            	                // Unblock the customer since block time has expired
            	            	customerMemoryData.setIsBlockedForAICount(0);
            	            	customerMemoryData.setBlockedForAI(false);
            	                toReturn=processAiRequest(currentCustomer, phoneNumberObject, organization, chatHistory);

            	               //System.out.println("[aiAutoReply] Block expired. Customer unblocked for AI auto reply.");
            	            }
            	        }
        	    	}
        	    	else {
        	    		  //System.out.println("Customer is temporarily NOT blocked for AI Messages");
        	    		   toReturn=processAiRequest(currentCustomer, phoneNumberObject, organization, chatHistory);
        	    	}
        	    }
        	    else {
        	    	//System.out.println("Customer context data is not found in memory");
        	    }
        	}
        	else {
        		//System.out.println("Customer not found in memory");
        	}
        	
        } catch (Exception e) {
            System.err.println("[aiAutoReply] Exception occurred: " + e.getMessage());
            e.printStackTrace();
            toReturn = false;
        }
        
       //System.out.println("[aiAutoReply] Finished AI auto reply process with result: " + toReturn);
        return toReturn;
    }
    
    
    //Gets assistant , threads , verify if customer first message, and send for output creation / heuristic test.
    public boolean  processAiRequest(Customers currentCustomer,WhatsAppPhoneNumber phoneNumberObject, Organization organization, WhatsAppChatHistory chatHistory){
    	boolean toReturn = true;
        try {
        	
    		//System.out.println("[processAiREquest] Updating stored customer info");
    	    //New ChatHistory inbound message - Send to WhatsAppChatContextData
    		WhatsAppCustomerParameterDataDto dto = new WhatsAppCustomerParameterDataDto();
        	dto.setAction("update");
        	dto.setPhoneNumber(currentCustomer.getPhoneNumber());
    		dto.setOrganization(organization);
    		dto.setWhatsAppPhoneNumberId(phoneNumberObject.getPhoneNumberID());
    		dto.setWhatsAppRegisteredByPhoneNumber(phoneNumberObject.getPhoneNumber());
    		dto.setWhatsAppProjectId(String.valueOf(phoneNumberObject.getWhatsAppProject().getId()));
    		dto.setBusinessPortfolio(phoneNumberObject.getWhatsAppProject().getBusinessPortfolio());//New ChatHistory inbound message - Send to WhatsAppChatContextData
        	dto.setWhatsAppChatHistory(chatHistory);

        	WhatsAppCustomerData.workWithWhatsAppCustomerData(dto);
        	
    	    //Send WhatsAppChatContextData function which creates AiInterfaceOutputDto and send for further processing as per heuristics results.
        	languageConvertorAndHeuristicCheckForCustomer(organization,currentCustomer,phoneNumberObject,dto);
        	
	    } catch (Exception e) {
	        System.err.println("[processAiREquest] Exception occurred: " + e.getMessage());
	        e.printStackTrace();
	        toReturn = false;
	    }
	    
	   //System.out.println("[processAiREquest] Finished AI auto reply process with result: " + toReturn);
	    return toReturn;
    }
    
    
    public boolean languageConvertorAndHeuristicCheckForCustomer(Organization organization,Customers currentCustomer,WhatsAppPhoneNumber phoneNumberObject,WhatsAppCustomerParameterDataDto dto)
   	{
   		boolean toReturn = true;
   		
   		try {
   			
        	//System.out.println("[processAiREquest] Building System Prompt");
            String systemPromt = this.promptBuilderHeuristicAndEnglishLanguageConvertorService.buildPrompt();
        	String model = this.env.getProperty("openai.model");
   			
   			//System.out.println("Sending request to languge assistant.");
        	systemPromt = this.assistantService.sanitize(systemPromt);
        	String userPrompt = this.assistantService.sanitize(dto.getWhatsAppChatHistory().getMessageString());
   			String responseString = this.assistantService.getCompletion(model, systemPromt, userPrompt);

   			LanguageAndHeuristicCheckResponse languageAndHeuristicCheckResponse = new LanguageAndHeuristicCheckResponse();

   			languageAndHeuristicCheckResponse = this.assistantService.parseStringToLanguageAndHeuristicCheckResponse(dto.getOrganization().getOrganization(), null, null,  responseString);
   			
   			if(languageAndHeuristicCheckResponse != null) {
   				
   				//System.out.println("languageAndHeuristicCheckResponse : "+objectMapper.writeValueAsString(languageAndHeuristicCheckResponse));
   				
   				if(languageAndHeuristicCheckResponse.isNoFurtherTextRequired()) {
   					//System.out.println("No further text required as per user input, reasoned by LLM for input : "+dto.getWhatsAppChatHistory().getMessageString());
   					//System.out.println("As of now we do not close conversation here, its just to collect info");
   				}
   				
   				dto.setAction("update-language-and-customerHeuristicMessageCollation");
   	        	dto.setLanguage(languageAndHeuristicCheckResponse.getLanguage());
   	        	dto.setEnglishMessageToAdd(languageAndHeuristicCheckResponse.getEnglishTranslation());
   	        	WhatsAppCustomerData.workWithWhatsAppCustomerData(dto);
   				
   				//System.out.println("Creating job ID");
   				String jobId= TrackedSchduledJobs.whatAppAIMessageRunnable+currentCustomer.getPhoneNumber()+currentCustomer.getOrganization().trim();
   				
   				//System.out.println("Creating Runnable instance");
   				WhatsAppSendCustomerAIMessageRunnable whatsAppSendCustomerAIMessageRunnable = new WhatsAppSendCustomerAIMessageRunnable();
   				whatsAppSendCustomerAIMessageRunnable.setJobId(jobId);
   				whatsAppSendCustomerAIMessageRunnable.setWhatsAppCustomerParameterDataDto(dto);
   				whatsAppSendCustomerAIMessageRunnable.setEmbeddingService(embeddingService);
   				whatsAppSendCustomerAIMessageRunnable.setFileProcessingService(fileProcessingService);
   				whatsAppSendCustomerAIMessageRunnable.setObjectMapper(objectMapper);
   				whatsAppSendCustomerAIMessageRunnable.setAssistantService(assistantService);
   				whatsAppSendCustomerAIMessageRunnable.setSupportTicketRepository(supportTicketRepository);
   				whatsAppSendCustomerAIMessageRunnable.setEnv(env);
   				whatsAppSendCustomerAIMessageRunnable.setApplicationContext(applicationContext);
   				whatsAppSendCustomerAIMessageRunnable.setPhoneNumberObject(phoneNumberObject);
   				whatsAppSendCustomerAIMessageRunnable.setPromptBuilderSummarizeService(promptBuilderSummarizeService);
   				whatsAppSendCustomerAIMessageRunnable.setOrganization(organization);

   				whatsAppSendCustomerAIMessageRunnable.setCalculationRequired(languageAndHeuristicCheckResponse.isCalculationRequired());

   				whatsAppSendCustomerAIMessageRunnable.setCustomerMaybeAskingDemoVideo(languageAndHeuristicCheckResponse.isCustomerMaybeAskingDemoVideo());

   				whatsAppSendCustomerAIMessageRunnable.setCurrentCustomer(currentCustomer);
   				
   	        	//System.out.println("Scheduling job as per requirement");
   				if(languageAndHeuristicCheckResponse.isCustomerStillWriting()) {
   					//Reschedule job with new time for this customer + org
   					//System.out.println("isCustomerStillWriting true : Reschedule job with new time for this customer + org");
   			     	schedulerService.removeIfExistsAndScheduleATaskAfterXSeconds(jobId, whatsAppSendCustomerAIMessageRunnable, heuristicWaitTimeSeconds);
   				}
   				else {
   					
   					//Execute scheduled job for this customer + org now
   					//System.out.println("isCustomerStillWriting false : Execute job now for this customer + org");
   					schedulerService.removeIfExistsAndScheduleATaskAfterXSeconds(jobId, whatsAppSendCustomerAIMessageRunnable, 0);
   				}
   			}
   			else {
   				throw new Exception("Heuristic And English Language Convertor did not return response in required format");
   			}
   		}
   		catch(Exception e)
   		{
   			e.printStackTrace();
   			toReturn = false;
   		}
   		
   		return toReturn;
   	}
   
}
