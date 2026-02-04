package com.mylinehub.crm.whatsapp.taskscheduler;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.data.TrackedSchduledJobs;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Media;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.entity.SupportTicket;
import com.mylinehub.crm.entity.dto.FixedDateSchedulerDefinitionDTO;
import com.mylinehub.crm.rag.data.MylinehubMemoryRagData;
import com.mylinehub.crm.rag.dto.AiInterfaceInputDto;
import com.mylinehub.crm.rag.dto.AiInterfaceOutputDto;
import com.mylinehub.crm.rag.dto.SummarizationInputDto;
import com.mylinehub.crm.rag.enums.PromptCategoryType;
import com.mylinehub.crm.rag.service.AssistantService;
import com.mylinehub.crm.rag.service.EmbeddingService;
import com.mylinehub.crm.rag.service.FileProcessingService;
import com.mylinehub.crm.rag.service.PromptBuilderSummarizeService;
import com.mylinehub.crm.repository.MediaRepository;
import com.mylinehub.crm.repository.SupportTicketRepository;
import com.mylinehub.crm.service.JobSchedulingService;
import com.mylinehub.crm.utils.TicketUtils;
import com.mylinehub.crm.whatsapp.data.WhatsAppCustomerData;
import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerDataDto;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerParameterDataDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPrompt;
import com.mylinehub.crm.whatsapp.enums.MESSAGE_TYPE;
import com.mylinehub.crm.whatsapp.enums.chat.CHAT_ORIGIN;
import com.mylinehub.crm.whatsapp.meta.variables.SUPPORTED_FORMATS;
import com.mylinehub.crm.whatsapp.service.WhatsAppIntegrationOutboundService;
import com.mylinehub.crm.rag.dto.AiPropertyInventoryVerificationOutputDto;

import lombok.Data;

@Data
public class WhatsAppSendCustomerAIMessageRunnable implements Runnable {

    private String jobId;
    private WhatsAppCustomerParameterDataDto whatsAppCustomerParameterDataDto;
    private EmbeddingService embeddingService; 
    private FileProcessingService fileProcessingService;
    private SupportTicketRepository supportTicketRepository;
    private ObjectMapper objectMapper;
    private AssistantService assistantService;
    private ApplicationContext applicationContext;
    private Environment env;
    private WhatsAppPhoneNumber phoneNumberObject;
    private boolean calculationRequired;
    private boolean customerMaybeAskingDemoVideo;
    private PromptBuilderSummarizeService promptBuilderSummarizeService;
    private Customers currentCustomer;
    private Organization organization;
    
    @Override
    public void run() {
        //System.out.println("[WhatsAppSendCustomerAIMessageRunnable] Job started. Job ID: " + jobId);
    	try {
    			//Find customer
    			whatsAppCustomerParameterDataDto.setAction("get-one");
            	Map<String, WhatsAppCustomerDataDto> customerDataMap = WhatsAppCustomerData.workWithWhatsAppCustomerData(whatsAppCustomerParameterDataDto);
            	if (customerDataMap != null && customerDataMap.containsKey(currentCustomer.getPhoneNumber()+whatsAppCustomerParameterDataDto.getOrganization().getOrganization().trim())) {
            		WhatsAppCustomerDataDto customerMemoryData = customerDataMap.get(currentCustomer.getPhoneNumber()+whatsAppCustomerParameterDataDto.getOrganization().getOrganization().trim());


            		//Clear all collated messages in memory and set it refreshed
            		//System.out.println("Clear all collated messages in memory and set it refreshed");
            		String englishMessage = String.valueOf(customerMemoryData.getCustomerHeuristicMessageCollationEnglish());
            		String originalMessage = String.valueOf(customerMemoryData.getCustomerHeuristicMessageCollationOriginal());
            		List<WhatsAppChatHistory> listOfPreviousChats = customerMemoryData.getChatList();
            		
            		whatsAppCustomerParameterDataDto.setAction("clear-customerHeuristicMessageCollation");
            		WhatsAppCustomerData.workWithWhatsAppCustomerData(whatsAppCustomerParameterDataDto);
                   	AiInterfaceInputDto aiInterfaceInputDto = new AiInterfaceInputDto();
            		if (englishMessage != null && !englishMessage.trim().isEmpty()) {
	            		//Convert into embedding & fetch RAG response
	            		List<String> allResults = null;

            			allResults = embeddingService.searchSimilarEmbeddings(whatsAppCustomerParameterDataDto.getOrganization().getOrganization(), englishMessage);
                       	aiInterfaceInputDto.setRagResponse(allResults);		
		        	}
		        	else {
		        			//System.out.println("englishMessage was null, hence not trigger AI message");
		        	}
            			
    				//Fetch Chat History Data
    				//System.out.println("Fetch Chat History Data");
    				List<String> customerMessages = new ArrayList<>(8);
    				List<String> botMessages = new ArrayList<>(10);

    				int customerCount = 0;
    				int botCount = 0;

    				// Sort list by createdOn ascending (oldest to newest)
    				// No need of sorting as its already sorted by time
    				//listOfPreviousChats.sort(Comparator.comparing(WhatsAppChatHistory::getCreatedOn));

    				for (int i = listOfPreviousChats.size() - 1; i >= 0; i--) {
    				    WhatsAppChatHistory chat = listOfPreviousChats.get(i);

    				    String msg = chat.getMessageString();
    				    if (msg == null || msg.isBlank()) continue;

    				    if (chat.isInbound()) {
    				        if (customerCount < 8) {
    				            customerMessages.add(0, msg); // insert at beginning to maintain order
    				            customerCount++;
    				        }
    				    } else {
    				        if (botCount < 10) {
    				            botMessages.add(0, msg);
    				            botCount++;
    				        }
    				    }

    				    // Optimization: break early if all limits are met
    				    if (customerCount >= 8 && botCount >= 10) break;
    				}
	
            		//System.out.println("customerMemoryData.getCustomer().isFirstWhatsAppMessageIsSend() : "+customerMemoryData.getCustomer().isFirstWhatsAppMessageIsSend());
    				//System.out.println("englishMessage : "+englishMessage);
    				//System.out.println("originalMessage : "+originalMessage);
    				//System.out.println("botMessages : "+objectMapper.writeValueAsString(botMessages));
    				//System.out.println("customerMessages : "+objectMapper.writeValueAsString(customerMessages));
    				//System.out.println("allResults : "+objectMapper.writeValueAsString(allResults));
    				
                   	//Create LLM Input
    				//System.out.println("Create LLM Input");

               		aiInterfaceInputDto.setAllTimeFirstMessage(!customerMemoryData.getCustomer().isFirstWhatsAppMessageIsSend());
               		aiInterfaceInputDto.setSessionFirstMessage(customerMemoryData.isSessionFirstMessage());
               		aiInterfaceInputDto.setCustomer(customerMemoryData.getCustomer());
               		aiInterfaceInputDto.setCustomerConvertedMessageInput(englishMessage);

               		aiInterfaceInputDto.setCustomerOriginalMessageInput(originalMessage);
               		aiInterfaceInputDto.setMessageResponseHistoryFromLLM(botMessages);
               		aiInterfaceInputDto.setMessageResponseHistoryFromUser(customerMessages);
               		aiInterfaceInputDto.setCustomerEmail(customerMemoryData.getCustomer().getEmail());
               		
               		if(!phoneNumberObject.isStoreVerifyCustomerPropertyInventory() && calculationRequired) {
               			//System.out.println("calculationRequired is true");
               			aiInterfaceInputDto.setCalculationLogic(MylinehubMemoryRagData.MYLINEHUBCOSTCALCULATION);
               		}
               		else {
               			//System.out.println("calculationRequired is false");
               		}
               		
               		if(!phoneNumberObject.isStoreVerifyCustomerPropertyInventory() && isCustomerMaybeAskingDemoVideo()) {
               			//System.out.println("isCustomerMaybeAskingDemoVideo is true");
               			aiInterfaceInputDto.setVideoLinkData(MylinehubMemoryRagData.MYLINEHUBVIDEOS);
               		}
               		else {
               			//System.out.println("isCustomerMaybeAskingDemoVideo is false");
               		}
               		
               		//Convert aiInterfaceInputDto to string
               		String systemPromt = buildWhatsAppBot(currentCustomer.getOrganization());
               		
               		String model = this.env.getProperty("openai.model");
               		//System.out.println("systemPromt : "+systemPromt);
               		String userPrompt = objectMapper.writeValueAsString(aiInterfaceInputDto);
//               		System.out.println("Input aiInterfaceInputDto : "+userPrompt);
               		
               		systemPromt = this.assistantService.sanitize(systemPromt);
                	userPrompt = this.assistantService.sanitize(userPrompt);
               		//System.out.println("Sending request to whats app bot assistant.");
        			String responseString = this.assistantService.getCompletion(model, systemPromt, userPrompt);
        			
//        			System.out.println("responseString i.e. aiInterfaceOutputDto : "+responseString);
        			
        			List<WhatsAppChatHistory> listOfMessages = null;
        			
        			// --- DEBUG: decide output class branch ---
        			String aiOutClass =
        			        (phoneNumberObject == null || phoneNumberObject.getAiOutputClassName() == null)
        			                ? "NULL"
        			                : phoneNumberObject.getAiOutputClassName();

//        			System.out.println("[AI-OUT] phoneNumberObject null? " + (phoneNumberObject == null));
//        			System.out.println("[AI-OUT] aiOutputClassName raw = [" + aiOutClass + "]");
//        			System.out.println("[AI-OUT] aiOutputClassName trimmed = [" + (aiOutClass == null ? "NULL" : aiOutClass.trim()) + "]");
//        			System.out.println("[AI-OUT] equals aiInterfaceOutputDto? "
//        			        + ("aiInterfaceOutputDto".equals(aiOutClass)));
//        			System.out.println("[AI-OUT] equalsIgnoreCase aiInterfaceOutputDto? "
//        			        + ("aiInterfaceOutputDto".equalsIgnoreCase(aiOutClass == null ? "" : aiOutClass.trim())));
//        			System.out.println("[AI-OUT] equals AiPropertyInventoryVerificationOutputDto? "
//        			        + ("AiPropertyInventoryVerificationOutputDto".equals(aiOutClass)));

        			// --- ACTUAL BRANCHING (more robust) ---
        			String outClass = (aiOutClass == null) ? "" : aiOutClass.trim();

        			if ("aiInterfaceOutputDto".equalsIgnoreCase(outClass.trim()) || "AiInterfaceOutputDto".equalsIgnoreCase(outClass.trim())) {

        			    System.out.println("[AI-OUT] Branch = AiInterfaceOutputDto");
        			    AiInterfaceOutputDto aiInterfaceOutputDto = aiInterfaceOutputDtoFlow(responseString, model, systemPromt, userPrompt);

//        			    System.out.println("[AI-OUT] aiInterfaceOutputDto null? " + (aiInterfaceOutputDto == null));
        			    if (aiInterfaceOutputDto != null) {
//        			        System.out.println("[AI-OUT] aiInterfaceOutputDto.files null? " + (aiInterfaceOutputDto.getFiles() == null));
//        			        System.out.println("[AI-OUT] aiInterfaceOutputDto.llmResponse null? " + (aiInterfaceOutputDto.getLlmResponse() == null));
//        			        System.out.println("[AI-OUT] Convert it into list of ChatHistory objects.");
        			        listOfMessages = convertaiInterfaceOutputDtoToWhatsAppChatHistory(aiInterfaceOutputDto, phoneNumberObject.getOrganization());
        			    }

        			} else if ("AiPropertyInventoryVerificationOutputDto".equalsIgnoreCase(outClass.trim())) {

        			    System.out.println("[AI-OUT] Branch = AiPropertyInventoryVerificationOutputDto");
        			    AiPropertyInventoryVerificationOutputDto dto =
        			            aiPropertyInventoryVerificationOutputDtoFlow(responseString, model, systemPromt, userPrompt);

//        			    System.out.println("[AI-OUT] dto null? " + (dto == null));
        			    if (dto != null) {
//        			        System.out.println("[AI-OUT] Convert it into list of ChatHistory objects.");
        			        listOfMessages = convertAiPropertyInventoryVerificationOutputDto(dto, phoneNumberObject.getOrganization());
        			    }

        			} else {
        			    System.out.println("[AI-OUT] Branch = UNKNOWN. Skipping conversion. aiOutputClassName=[" + outClass + "]");
        			}

        			// --- DEBUG: after conversion ---
//        			System.out.println("[AI-OUT] listOfMessages = " + (listOfMessages == null ? "NULL" : ("size=" + listOfMessages.size())));

        			System.out.println("Trigger outbound flow");
        			if (listOfMessages == null) {
        			    System.out.println("SKIP outbound: listOfMessages is NULL");
        			} else if (listOfMessages.isEmpty()) {
        			    System.out.println("SKIP outbound: listOfMessages is EMPTY");
        			} else {
//        			    System.out.println("Calling triggerWhatsAppAiOutboundFlow with size=" + listOfMessages.size());
        			    triggerWhatsAppAiOutboundFlow(listOfMessages);
        			}
                			
            	}
            	else {
            		//System.out.println("Customer not found in memory data");
            	}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
    
   	
   	

    //If output null, heuristic message , or else shoots whats app response , trigger outbound whats app flow
   	public boolean triggerWhatsAppAiOutboundFlow(List<WhatsAppChatHistory> listOfMessages)
   	{
   		boolean toReturn = true;
   		
   		try {

   			    if(listOfMessages == null || listOfMessages.size()==0) {
   			    	//System.out.println("[triggerWhatsAppAiOutboundFlow] : No chat history object found to send.");
   			    	return false;
   			    }
   			    

   	    	    //If return is not null, iterate over list of chat history, call a sync function to send message back to customer , choose to follow path of already build layers, It should be same code
   	    	    /* 
   	    	     * --Above will save to chat history
   	    	     * --Send actual whats app message back to user
   	    	     * --Manage status
   	    	     * --Manage reports
   	    	     * --Send stomp to other employee
   	    	     */
   			    
   			    WhatsAppIntegrationOutboundService whatsAppIntegrationOutboundService = applicationContext.getBean(WhatsAppIntegrationOutboundService.class);
   			    //System.out.println("Trigger sending whats app messages : total : "+listOfMessages.size());
	   			for (WhatsAppChatHistory whatsAppChatHistory : listOfMessages) {
	   				whatsAppIntegrationOutboundService.sendOutboundWhatsAppChatHistory(whatsAppChatHistory,  CHAT_ORIGIN.ai.name());
	   			}

   		}
   		catch(Exception e)
   		{
   			e.printStackTrace();
   			toReturn = false;
   		}
   		
   		return toReturn;
   	}
   	
   	
	
	private AiPropertyInventoryVerificationOutputDto aiPropertyInventoryVerificationOutputDtoFlow(
	        String responseString,
	        String model,
	        String systemPromt,
	        String userPrompt
	) throws Exception {

		System.out.println("aiPropertyInventoryVerificationOutputDtoFlow");
	    AiPropertyInventoryVerificationOutputDto out =
	            this.assistantService.parseStringToAiPropertyInventoryVerificationOutputDto(
	                    whatsAppCustomerParameterDataDto.getOrganization().getOrganization(),
	                    null,
	                    null,
	                    responseString
	            );

	    if (out == null) return null;

	    // Save / transform into memory (Customers.propertyInventory) + update record-level lastUpdated
	    try {
	        WhatsAppCustomerParameterDataDto memDto = new WhatsAppCustomerParameterDataDto();

	        memDto.setPhoneNumber(currentCustomer.getPhoneNumber());
	        memDto.setOrganization(whatsAppCustomerParameterDataDto.getOrganization());
	        memDto.setAction("update-customer-property-inventory-from-ai");
	        memDto.setAiPropertyInventoryVerificationOutputDto(out);

	        WhatsAppCustomerData.workWithWhatsAppCustomerData(memDto);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    // if AI says exit, stop flow
	    if (out.getShouldAIExitBecauseInformationIsCompleteOrVerifiedFull() != null
	            && out.getShouldAIExitBecauseInformationIsCompleteOrVerifiedFull()) {
	        return null;
	    }

	    return out;
	}

   	
   	private AiInterfaceOutputDto aiInterfaceOutputDtoFlow(String responseString, String model,String systemPromt,String userPrompt) throws Exception {
   				
   		AiInterfaceOutputDto aiInterfaceOutputDto = this.assistantService.parseStringToAiInterfaceOutputDto(whatsAppCustomerParameterDataDto.getOrganization().getOrganization(), null,null, responseString);
		
		if(aiInterfaceOutputDto.isStopAIMessage())
		{
			//System.out.println("User wants to stop AI messages, we wont send message after this for sometime now.");
			whatsAppCustomerParameterDataDto.setAction("block-ai");
    		WhatsAppCustomerData.workWithWhatsAppCustomerData(whatsAppCustomerParameterDataDto);
		}

		boolean summarize = false;
		String summarizeText = "";

		String supportEmail = this.env.getProperty("spring.parentorginization.support.email");
		String supportPhone = this.env.getProperty("spring.parentorginization.phone");
		
		//System.out.println("supportEmail " +supportEmail);
		//System.out.println("supportPhone : "+supportPhone);
		
		    
	    List<SupportTicket> allOpenTickets = supportTicketRepository.findByOpenAndOrganizationAndCustomerId(
		        true,
		        whatsAppCustomerParameterDataDto.getOrganization().getOrganization(),
		        currentCustomer.getId()
		);
		String result = "";

		//System.out.println("Before ticket condition isCustomerAskingCreateCustomerSupportTicket: " + aiInterfaceOutputDto.isCustomerAskingCreateCustomerSupportTicket());
		//System.out.println("Before ticket condition isCustomerAskingAboutPreviousCustomerSupportTicket(prev): " + aiInterfaceOutputDto.isCustomerAskingAboutPreviousCustomerSupportTicket());

		//System.out.println("Verify Current Open Tickets");
		
		//System.out.println("All open tickets count: " + (allOpenTickets != null ? allOpenTickets.size() : 0));
	    if (aiInterfaceOutputDto.isCustomerAskingAboutPreviousCustomerSupportTicket() &&
			    !aiInterfaceOutputDto.isCustomerAskingCreateCustomerSupportTicket()) {
			    
			    //System.out.println("Fetching previous support ticket");
			    
			    if (allOpenTickets != null && !allOpenTickets.isEmpty()) {
			        result = allOpenTickets.stream()
			                .map(ticket -> (ticket.getComplaintId() != null ? ticket.getComplaintId() : "NA")
			                        + " || "
			                        + (ticket.getComplaint() != null ? ticket.getComplaint() : "No description"))
			                .collect(Collectors.joining(" ;; "));
			        //System.out.println("Ticket Results: " + result);
			        summarizeText += "User current open tickets (ticketId || complaint), data: " + result + ".";
			        summarize = true;
			    } else {
			        //System.out.println("No previous tickets found");
			    }
			    
		} 
	    
		if (aiInterfaceOutputDto.getProductOrServiceName() != null && !aiInterfaceOutputDto.getProductOrServiceName().isBlank()
			    && aiInterfaceOutputDto.getCustomerEmailId() != null && !aiInterfaceOutputDto.getCustomerEmailId().isBlank()
			    && aiInterfaceOutputDto.getComplaint() != null && !aiInterfaceOutputDto.getComplaint().isBlank()) {
			    
//			    System.out.println("   All three fields are present to create ticket:");
//			    System.out.println("   productOrServiceName = " + aiInterfaceOutputDto.getProductOrServiceName());
//			    System.out.println("   customerEmailId = " + aiInterfaceOutputDto.getCustomerEmailId());
//			    System.out.println("   complaint = " + aiInterfaceOutputDto.getComplaint());

    			if (aiInterfaceOutputDto.isCustomerAskingCreateCustomerSupportTicket()) {
    			    
    			    //System.out.println("isCustomerAskingCreateCustomerSupportTicket is true");
    			    
    			    if (allOpenTickets == null || allOpenTickets.isEmpty()) {
    			        // Create new ticket
    			        //System.out.println("Creating new support ticket");
    			        SupportTicket currentTicket = new SupportTicket();
    			        currentTicket.setComplaintId(TicketUtils.generateTicketId());
    			        currentTicket.setOpen(true);
    			        currentTicket.setOrganization(whatsAppCustomerParameterDataDto.getOrganization().getOrganization());
    			        currentTicket.setProductOrServiceName(aiInterfaceOutputDto.getProductOrServiceName());
    			        currentTicket.setCustomerEmailId(aiInterfaceOutputDto.getCustomerEmailId());
    			        currentTicket.setComplaint(aiInterfaceOutputDto.getComplaint());
    			        currentTicket.setCustomerId(currentCustomer.getId());

    			        currentTicket = supportTicketRepository.save(currentTicket);
    			        result = currentTicket.getComplaintId() + " ::: " + currentTicket.getComplaint();
    			        //System.out.println("Ticket Results: " + result);
    			        summarizeText += "New ticket created, ticketId || complaint: " + result + ".";
    			        summarize = true;

    			    } else {
    			        // Already have ticket
    			        //System.out.println("Already have open tickets");
    			        result = allOpenTickets.stream()
    			                .map(ticket -> (ticket.getComplaintId() != null ? ticket.getComplaintId() : "NA")
    			                        + " || "
    			                        + (ticket.getComplaint() != null ? ticket.getComplaint() : "No description"))
    			                .collect(Collectors.joining(" ;; "));
    			        //System.out.println("Ticket Results: " + result);
    			        summarizeText += "User already has open tickets, ticketId || complaint: " + result +
    			                ". If unresolved, contact " + supportEmail + " or WhatsApp at: " + supportPhone + ".";
    			        summarize = true;
    			    }
    			}
    			else {
    				//System.out.println("customer did not ask for ticket creation");
    			}

	    } else {
			    //System.out.println("   Partial / No data received:");
			    //System.out.println("   productOrServiceName: " + aiInterfaceOutputDto.getProductOrServiceName());
			    //System.out.println("   customerEmailId: " + aiInterfaceOutputDto.getCustomerEmailId());
			    //System.out.println("   complaint: " + aiInterfaceOutputDto.getComplaint());
	    }

			Organization organization = whatsAppCustomerParameterDataDto.getOrganization();

			//System.out.println("currentCustomer : "+objectMapper.writeValueAsString(currentCustomer));
			
			String newEmail = aiInterfaceOutputDto.getCustomerEmailId();
//			System.out.println("newEmail : " + newEmail);

			if (newEmail != null && !newEmail.isBlank()) {
			    newEmail = newEmail.trim(); // handle accidental leading/trailing spaces

			    String existingEmail = currentCustomer.getEmail();
//			    System.out.println("existingEmail : " + existingEmail);

			    if (existingEmail == null || existingEmail.isBlank() || !existingEmail.equalsIgnoreCase(newEmail)) {
//			        System.out.println("Saving new customer email: " + newEmail);
			        currentCustomer.setEmail(newEmail);
			        // Update WhatsApp customer data
			        whatsAppCustomerParameterDataDto.setPhoneNumber(currentCustomer.getPhoneNumber());
			        whatsAppCustomerParameterDataDto.setEmail(newEmail);
			        whatsAppCustomerParameterDataDto.setAction("update-customer-email");
			        WhatsAppCustomerData.workWithWhatsAppCustomerData(whatsAppCustomerParameterDataDto);
			        
			    } else {
			        System.out.println("Email unchanged; skipping database update.");
			    }
			} else {
			    System.out.println("No valid email fetched from LLM (null or blank).");
			}

			
			//System.out.println("Before ticket condition isCustomerAskingAboutCurrentScheduleCall: " + aiInterfaceOutputDto.isCustomerAskingAboutCurrentScheduleCall());
		//System.out.println("Before ticket condition isCustomerAskingAboutNewScheduleCall(prev): " + aiInterfaceOutputDto.isCustomerAskingAboutNewScheduleCall());

		//Schedule call
		//Add initial queue and divert call to queue
			//System.out.println("Verify Schedule call");
	        JobSchedulingService jobSchedulingService =  applicationContext.getBean(JobSchedulingService.class); 
		boolean jobAlreadyScheduled = false;
		
		//STILL NEED TO GET THIS
		String ivrExtension = phoneNumberObject.getAiCallExtension();
		
        if(aiInterfaceOutputDto.isCustomerAskingAboutCurrentScheduleCall()) {
        	//System.out.println("Finding previous scheduled info");
        	jobAlreadyScheduled = jobSchedulingService.findIfScheduledCallJobToCustomer(TrackedSchduledJobs.fixeddate, currentCustomer.getPhoneNumber(), ivrExtension, currentCustomer.getOrganization());
        	summarizeText = summarizeText+"User already has a call scheduled with us." +
	                " For quick turn around email at: " + supportEmail + ".";
			summarize = true;
        }
		else {
			
				//System.out.println("isCustomerAskingAboutCurrentScheduleCall is false");
    			if(aiInterfaceOutputDto.isCustomerAskingAboutNewScheduleCall())
    			{
    				//System.out.println("New scheduled call is asked");
    				jobAlreadyScheduled = jobSchedulingService.findIfScheduledCallJobToCustomer(TrackedSchduledJobs.fixeddate, currentCustomer.getPhoneNumber(), ivrExtension, currentCustomer.getOrganization());
    				
    				if(!jobAlreadyScheduled) {
    					
    					if (ivrExtension != null && !ivrExtension.isBlank()) { 
    					
    						//System.out.println("Creating new call schedule");
        					FixedDateSchedulerDefinitionDTO fixedDateSchedulerDefinitionDTO = new FixedDateSchedulerDefinitionDTO();
                 			OffsetDateTime odt = OffsetDateTime.parse(aiInterfaceOutputDto.getScheduleDateTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                 	        Date date = Date.from(odt.toInstant());
                 	        
                			fixedDateSchedulerDefinitionDTO.setFirstName(currentCustomer.getFirstname());
                 			fixedDateSchedulerDefinitionDTO.setOrganization(currentCustomer.getOrganization());
                 			fixedDateSchedulerDefinitionDTO.setPhoneNumber(currentCustomer.getPhoneNumber());
                			fixedDateSchedulerDefinitionDTO.setCampaignId(null);
                			fixedDateSchedulerDefinitionDTO.setContext(organization.getPhoneContext());
                			fixedDateSchedulerDefinitionDTO.setProtocol(organization.getProtocol());
                			fixedDateSchedulerDefinitionDTO.setPhoneTrunk(organization.getPhoneTrunk());
                			fixedDateSchedulerDefinitionDTO.setPriority(1); // or any int
                			fixedDateSchedulerDefinitionDTO.setTimeOut(60000L); // timeout in ms
                			fixedDateSchedulerDefinitionDTO.setDate(date); // or your specific date
                			fixedDateSchedulerDefinitionDTO.setDomain(organization.getDomain());
                			fixedDateSchedulerDefinitionDTO.setActionType("actionType");
                			fixedDateSchedulerDefinitionDTO.setData("");
                			fixedDateSchedulerDefinitionDTO.setCallType("");
                			fixedDateSchedulerDefinitionDTO.setFromExtension(ivrExtension);
                			
                			jobSchedulingService.scheduleAFixedDateCallToCustomer(fixedDateSchedulerDefinitionDTO, null, applicationContext);
                			
                			summarizeText = summarizeText+"Call has been scheduled with us at : "+ aiInterfaceOutputDto.getScheduleDateTime() +
        			                " For quick turn around whatsApp at: " + supportPhone + ".";
        					summarize = true;
    					}
    					else {
    						//System.out.println("Support number default AI calling route not mentioned");
    					}
    				}
    				else {
    					
    					//System.out.println("Call is already scheduled , adding information");
    					summarizeText = summarizeText+"User already has a call scheduled with us." +
    			                " For quick turn around write email at: " + supportEmail + ".";
    					//System.out.println(summarizeText);
    					summarize = true;
    				}
    			}
    			else {
    				//System.out.println("isCustomerAskingAboutNewScheduleCall is false");
    			}
		}

        
        //System.out.println("summarize : "+summarize);
        //System.out.println("summarizeText : "+summarizeText);

		//Summarize again if required
		if(summarize) {
			
			List<String> listOfOutputList = aiInterfaceOutputDto.getLlmResponse();
			systemPromt = this.promptBuilderSummarizeService.buildPrompt();
			
			SummarizationInputDto summarizationInputDto= new SummarizationInputDto();
			summarizationInputDto.setTextAddedByCode(summarizeText);
			summarizationInputDto.setLlmResponse(listOfOutputList);
			
			userPrompt = objectMapper.writeValueAsString(summarizationInputDto);

			systemPromt = this.assistantService.sanitize(systemPromt);
        	userPrompt = this.assistantService.sanitize(userPrompt);
        	
			String summaryString = this.assistantService.getCompletion(model, systemPromt, userPrompt);
			listOfOutputList=new ArrayList<>();
			listOfOutputList.add(summaryString);
			aiInterfaceOutputDto.setLlmResponse(listOfOutputList);
			
//			System.out.println("summaryString for aiInterfaceOutputDto : "+summaryString);
		}
		
		return aiInterfaceOutputDto;
   	}
   	
   	
   	List<WhatsAppChatHistory> convertAiPropertyInventoryVerificationOutputDto(AiPropertyInventoryVerificationOutputDto aiPropertyInventoryVerificationOutputDto,String organization) {
   		List<WhatsAppChatHistory> toReturn = new ArrayList<>();
   		try {
   		    //System.out.println("Working on text messages");
   		    //System.out.println("Final Text Response By LLM :"+objectMapper.writeValueAsString(llmResponse));  
   			for (String response : aiPropertyInventoryVerificationOutputDto.getLlmResponse()) {
   			    WhatsAppChatHistory chatHistory = createNewChatHistoryObject(organization);
		   		chatHistory.setMessageType(MESSAGE_TYPE.text.name());
		   		chatHistory.setMessageString(response);
		   		toReturn.add(chatHistory);
   			}
   		}
   		catch(Exception e)
   		{
   			e.printStackTrace();
   			toReturn = null;
   		}
   		return toReturn;
   	}

   	List<WhatsAppChatHistory> convertaiInterfaceOutputDtoToWhatsAppChatHistory(AiInterfaceOutputDto aiInterfaceOutputDto, String organization) {
   	    List<WhatsAppChatHistory> toReturn = new ArrayList<>();
   	    try {

   	        Map<String, String> allFiles = aiInterfaceOutputDto.getFiles();
   	        if (allFiles == null) allFiles = Map.of(); // IMPORTANT: avoid NPE

   	        List<String> llmResponse = aiInterfaceOutputDto.getLlmResponse();
   	        if (llmResponse == null) llmResponse = List.of(); // avoid NPE

   	        List<String> allFileNamesList = new ArrayList<>();

   	        MediaRepository mediaRepository = applicationContext.getBean(MediaRepository.class);

   	        // Media messages
   	        for (Map.Entry<String, String> entry : allFiles.entrySet()) {
   	            String fileName = entry.getKey();
   	            if (fileName != null && !fileName.isBlank()) {
   	                allFileNamesList.add(fileName);
   	            }
   	        }

   	        Set<String> addedFileNames = new HashSet<>();

   	        if (!allFileNamesList.isEmpty()) {
   	            List<Media> allMediaList = mediaRepository.findByNameInAndOrganization(
   	                    allFileNamesList,
   	                    whatsAppCustomerParameterDataDto.getOrganization().getOrganization()
   	            );

   	            for (Media media : allMediaList) {
   	                if (media == null || media.getName() == null) continue;

   	                if (addedFileNames.contains(media.getName())) continue;

   	                String sizeInMB = String.format("%.2f", (double) media.getSize() / (1024 * 1024));
   	                WhatsAppChatHistory chatHistory = createNewChatHistoryObject(organization);
   	                chatHistory.setMessageType(detectWhatsAppFileType(media.getName()));
   	                chatHistory.setMessageString("*" + media.getName() + ",*_" + sizeInMB + "MB_");
   	                chatHistory.setWhatsAppMediaId(String.valueOf(media.getId()));
   	                chatHistory.setBlobType(media.getType());
   	                chatHistory.setFileName(media.getName());
   	                chatHistory.setFileSizeInMB(sizeInMB);

   	                toReturn.add(chatHistory);
   	                addedFileNames.add(media.getName());
   	            }
   	        }

   	        // Text messages
   	        for (String response : llmResponse) {
   	            if (response == null || response.isBlank()) continue;
   	            WhatsAppChatHistory chatHistory = createNewChatHistoryObject(organization);
   	            chatHistory.setMessageType(MESSAGE_TYPE.text.name());
   	            chatHistory.setMessageString(response);
   	            toReturn.add(chatHistory);
   	        }

   	    } catch (Exception e) {
   	        e.printStackTrace();
   	        // DO NOT set toReturn = null; keep what we have, or return empty list
   	    }
   	    return toReturn;
   	}

   	
   	
   	
   	WhatsAppChatHistory createNewChatHistoryObject(String organization) {
   		
   	    WhatsAppChatHistory chatHistory = new WhatsAppChatHistory();
		// Basic info
		chatHistory.setOrganization(whatsAppCustomerParameterDataDto.getOrganization().getOrganization());
		chatHistory.setPhoneNumberMain(whatsAppCustomerParameterDataDto.getWhatsAppRegisteredByPhoneNumber());
		chatHistory.setPhoneNumberWith(currentCustomer.getPhoneNumber());
		chatHistory.setFromExtension(organization);
		chatHistory.setFromName("AI Agent");
		chatHistory.setFromTitle("Rag Support");
		chatHistory.setOpenAIAssistantName("multiple");
		chatHistory.setOpenAIAssistantThread(whatsAppCustomerParameterDataDto.getLanguageThread()+"::"+whatsAppCustomerParameterDataDto.getWhatsAppBotThread()+"::"+whatsAppCustomerParameterDataDto.getSummarizeThread());
   		chatHistory.setOutbound(true);
   		chatHistory.setInbound(false);
   		return chatHistory;
   	}
   	
   	public static String detectWhatsAppFileType(String fileName) {

   	    // Define all allowed file-type sets inline
	   	Set<String> audio = Set.of(SUPPORTED_FORMATS.audio);
	    Set<String> video = Set.of(SUPPORTED_FORMATS.video);
	    Set<String> document = Set.of(SUPPORTED_FORMATS.document);
	    Set<String> sticker = Set.of(SUPPORTED_FORMATS.sticker);
	    Set<String> image = Set.of(SUPPORTED_FORMATS.image);
     
   	    // Define return-type strings
   	    String TYPE_TEXT = MESSAGE_TYPE.text.name();
   	    String TYPE_AUDIO = MESSAGE_TYPE.audio.name();
   	    String TYPE_VIDEO = MESSAGE_TYPE.video.name();
   	    String TYPE_STICKER = MESSAGE_TYPE.sticker.name();
   	    String TYPE_IMAGE = MESSAGE_TYPE.image.name();
   	    String TYPE_DOCUMENT = MESSAGE_TYPE.document.name();

   	    // Handle null or invalid filename
   	    if (fileName == null || !fileName.contains(".")) {
   	        return TYPE_TEXT;
   	    }

   	    // Extract extension (after last dot)
   	    String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

   	    // Match extension against sets
   	    if (audio.contains(extension)) return TYPE_AUDIO;
   	    if (video.contains(extension)) return TYPE_VIDEO;
   	    if (sticker.contains(extension)) return TYPE_STICKER;
   	    if (image.contains(extension)) return TYPE_IMAGE;
   	    if (document.contains(extension)) return TYPE_DOCUMENT;

   	    // Fallback — not recognized
   	    System.err.println("Unsupported file type: " + extension);
   	    System.err.println("Allowed formats: {aac, mp4, mpeg, amr, ogg, 3sp, webp, png, jpeg, plain, pdf, vnd.ms-powerpoint, msword, vnd.ms-excel, vnd.openxmlformats-officedocument.*}");
   	    return TYPE_TEXT;
   	}

   	
   	private String buildWhatsAppBot(String organizationString) {
   		String systemPromt = null;

   		try {
   		    String phoneNumberKey = (phoneNumberObject != null && phoneNumberObject.getPhoneNumber() != null)
   		            ? phoneNumberObject.getPhoneNumber()
   		            : whatsAppCustomerParameterDataDto.getWhatsAppRegisteredByPhoneNumber();

   		    String categoryKey = PromptCategoryType.WHATSAPPBOT.name();

   		    Map<String, Map<String, WhatsAppPrompt>> promptMap =
   		            WhatsAppMemoryData.workWithWhatsAppPhoneCategoryPrompts(
   		                    null, null, null, null, "get"
   		            );

   		    WhatsAppPrompt promptEntity = null;
   		    if (promptMap != null) {
   		        Map<String, WhatsAppPrompt> catMap = promptMap.get(phoneNumberKey);
   		        if (catMap != null) {
   		            promptEntity = catMap.get(categoryKey);
   		        }
   		    }

   		    if (promptEntity != null && promptEntity.getPrompt() != null && !promptEntity.getPrompt().trim().isEmpty()) {
   		        systemPromt = promptEntity.getPrompt();
   		    }

   		} catch (Exception e) {
   		    e.printStackTrace();
   		}
   		
   		systemPromt = systemPromt.replace("{organization}",organizationString);
   		systemPromt = systemPromt.replace("{org_services}",organization.getNatureOfBusiness());
   		systemPromt = systemPromt.replace("{{currentDateTimeIsoForLlm}}",String.valueOf(new Date()));
   		
   		return systemPromt;
   	}
   	
   	
}
