//package com.mylinehub.crm.whatsapp.taskscheduler;
//
//import java.time.OffsetDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import org.springframework.context.ApplicationContext;
//import org.springframework.core.env.Environment;
//
////import org.springframework.beans.BeanUtils;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mylinehub.crm.data.TrackedSchduledJobs;
//import com.mylinehub.crm.entity.Customers;
//import com.mylinehub.crm.entity.Media;
//import com.mylinehub.crm.entity.Organization;
//import com.mylinehub.crm.entity.SupportTicket;
//import com.mylinehub.crm.entity.dto.FixedDateSchedulerDefinitionDTO;
//import com.mylinehub.crm.rag.data.MylinehubMemoryRagData;
////import com.mylinehub.crm.rag.data.AIInterfaceOutputSentimentData;
////import com.mylinehub.crm.rag.data.dto.AiInterfaceOutputParameterDTO;
//import com.mylinehub.crm.rag.dto.AiInterfaceInputDto;
//import com.mylinehub.crm.rag.dto.AiInterfaceOutputDto;
//import com.mylinehub.crm.rag.dto.ResultDTO;
////import com.mylinehub.crm.rag.model.AiSentimentsEnitity;
//import com.mylinehub.crm.rag.model.AssistantEntity;
//import com.mylinehub.crm.rag.service.AssistantService;
//import com.mylinehub.crm.rag.service.EmbeddingService;
//import com.mylinehub.crm.rag.service.FileProcessingService;
//import com.mylinehub.crm.rag.service.PromptBuilderSummarizeService;
//import com.mylinehub.crm.rag.service.PromptBuilderWhatsAppBotService;
//import com.mylinehub.crm.repository.MediaRepository;
//import com.mylinehub.crm.repository.SupportTicketRepository;
//import com.mylinehub.crm.service.JobSchedulingService;
//import com.mylinehub.crm.utils.TicketUtils;
//import com.mylinehub.crm.whatsapp.data.WhatsAppCustomerData;
//import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerDataDto;
//import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerParameterDataDto;
//import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;
//import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
//import com.mylinehub.crm.whatsapp.enums.MESSAGE_TYPE;
//import com.mylinehub.crm.whatsapp.enums.chat.CHAT_ORIGIN;
//import com.mylinehub.crm.whatsapp.meta.variables.SUPPORTED_FORMATS;
//import com.mylinehub.crm.whatsapp.service.WhatsAppIntegrationOutboundService;
//
//import lombok.Data;
//
//@Data
//public class WhatsAppSendCustomerAIMessageViaAssistantRunnable implements Runnable {
//
//    private String jobId;
//    private WhatsAppCustomerParameterDataDto whatsAppCustomerParameterDataDto;
//    private EmbeddingService embeddingService; 
//    private FileProcessingService fileProcessingService;
//    private SupportTicketRepository supportTicketRepository;
//    private ObjectMapper objectMapper;
//    private AssistantService assistantService;
//    private AssistantEntity whatsAppAssistantEntity;
//    private AssistantEntity summaryAssistantEntity;
//    private ApplicationContext applicationContext;
//    private Environment env;
//    private WhatsAppPhoneNumber phoneNumberObject;
//    private boolean isCalculationRequired;
//    private boolean customerMaybeAskingDemoVideo;
//    private PromptBuilderWhatsAppBotService promptBuilderWhatsAppBotService;
//    private PromptBuilderSummarizeService promptBuilderSummarizeService;
//    private Customers currentCustomer;
//    
//    @Override
//    public void run() {
//        //System.out.println("[WhatsAppSendCustomerAIMessageRunnable] Job started. Job ID: " + jobId);
//    	try {
//    			//Find customer
//    			whatsAppCustomerParameterDataDto.setAction("get-one");
//            	Map<String, WhatsAppCustomerDataDto> customerDataMap = WhatsAppCustomerData.workWithWhatsAppCustomerData(whatsAppCustomerParameterDataDto);
//            	if (customerDataMap != null && customerDataMap.containsKey(currentCustomer.getPhoneNumber()+whatsAppCustomerParameterDataDto.getOrganization().getOrganization().trim())) {
//            		WhatsAppCustomerDataDto customerMemoryData = customerDataMap.get(currentCustomer.getPhoneNumber()+whatsAppCustomerParameterDataDto.getOrganization().getOrganization().trim());
//
//
//            		//Clear all collated messages in memory and set it refreshed
//            		//System.out.println("Clear all collated messages in memory and set it refreshed");
//            		String englishMessage = String.valueOf(customerMemoryData.getCustomerHeuristicMessageCollationEnglish());
//            		String originalMessage = String.valueOf(customerMemoryData.getCustomerHeuristicMessageCollationOriginal());
//            		List<WhatsAppChatHistory> listOfPreviousChats = customerMemoryData.getChatList();
//            		
//            		whatsAppCustomerParameterDataDto.setAction("clear-customerHeuristicMessageCollation");
//            		WhatsAppCustomerData.workWithWhatsAppCustomerData(whatsAppCustomerParameterDataDto);
//            		
//            		if(englishMessage!= null && englishMessage != "") {
//
//            				//Fetch Chat History Data
//            				//System.out.println("Fetch Chat History Data");
//            				List<String> customerMessages = new ArrayList<>(3);
//            				List<String> botMessages = new ArrayList<>(1);
//
//            				int customerCount = 0;
//            				int botCount = 0;
//
//            				// Sort list by createdOn ascending (oldest to newest)
//            				// No need of sorting as its already sorted by time
//            				//listOfPreviousChats.sort(Comparator.comparing(WhatsAppChatHistory::getCreatedOn));
//
//            				for (int i = listOfPreviousChats.size() - 1; i >= 0; i--) {
//            				    WhatsAppChatHistory chat = listOfPreviousChats.get(i);
//
//            				    String msg = chat.getMessageString();
//            				    if (msg == null || msg.isBlank()) continue;
//
//            				    if (chat.isInbound()) {
//            				        if (customerCount < 3) {
//            				            customerMessages.add(0, msg); // insert at beginning to maintain order
//            				            customerCount++;
//            				        }
//            				    } else {
//            				        if (botCount < 1) {
//            				            botMessages.add(0, msg);
//            				            botCount++;
//            				        }
//            				    }
//
//            				    // Optimization: break early if all limits are met
//            				    if (customerCount >= 3 && botCount >= 1) break;
//            				}
//
//            				//System.out.println("customerMemoryData.getCustomer().isFirstWhatsAppMessageIsSend() : "+customerMemoryData.getCustomer().isFirstWhatsAppMessageIsSend());
//            				//System.out.println("whatsAppCustomerParameterDataDto.isSessionFirstMessage() : "+whatsAppCustomerParameterDataDto.isSessionFirstMessage());
//            				//System.out.println("englishMessage : "+englishMessage);
//            				//System.out.println("originalMessage : "+originalMessage);
//            				//System.out.println("botMessages : "+objectMapper.writeValueAsString(botMessages));
//            				//System.out.println("customerMessages : "+objectMapper.writeValueAsString(customerMessages));
//            				//System.out.println("allResults : "+objectMapper.writeValueAsString(allResults));
//            				
//                           	//Create LLM Input
//            				//System.out.println("Create LLM Input");
//                           	AiInterfaceInputDto aiInterfaceInputDto = new AiInterfaceInputDto();
//                       		aiInterfaceInputDto.setAllTimeFirstMessage(customerMemoryData.getCustomer().isFirstWhatsAppMessageIsSend());
//                       		aiInterfaceInputDto.setSessionFirstMessage(customerMemoryData.isSessionFirstMessage());
//                       		aiInterfaceInputDto.setCustomerName(customerMemoryData.getCustomer().getFirstname()+" "+customerMemoryData.getCustomer().getLastname());
//                       		aiInterfaceInputDto.setCustomerConvertedMessageInput(englishMessage);
//                       		aiInterfaceInputDto.setCustomerOriginalMessageInput(originalMessage);
//                       		aiInterfaceInputDto.setMessageResponseHistoryFromLLM(botMessages);
//                       		aiInterfaceInputDto.setMessageResponseHistoryFromUser(customerMessages);
//                       		aiInterfaceInputDto.setRagResponse(allResults);
//
//                       		if(isCalculationRequired) {
//                       			//System.out.println("isCalculationRequired is true");
//                       			aiInterfaceInputDto.setCalculationLogic(MylinehubMemoryRagData.MYLINEHUBCOSTCALCULATION);
//                       		}
//                       		else {
//                       			//System.out.println("isCalculationRequired is false");
//                       		}
//                       		
//                       		if(isCustomerMaybeAskingDemoVideo()) {
//                       			//System.out.println("isCustomerMaybeAskingDemoVideo is true");
//                       			aiInterfaceInputDto.setVideoLinkData(MylinehubMemoryRagData.MYLINEHUBVIDEOS);
//                       		}
//                       		else {
//                       			//System.out.println("isCustomerMaybeAskingDemoVideo is false");
//                       		}
//                       		
//                       		//Convert aiInterfaceInputDto to string
//                       		//System.out.println("Convert aiInterfaceInputDto to string");
//                       		String llmInput = objectMapper.writeValueAsString(aiInterfaceInputDto);
//                       		//System.out.println("Input aiInterfaceInputDto : "+llmInput);
//                       		
//                       		//System.out.println("Sending request to whats app bot assistant.");
//                			String responseString = this.assistantService.addMessageAndStream(whatsAppCustomerParameterDataDto.getOrganization().getOrganization(), this.whatsAppAssistantEntity.getAssistantId(), whatsAppCustomerParameterDataDto.getWhatsAppBotThread(), llmInput);
//                			//System.out.println("responseString i.e. aiInterfaceOutputDto : "+responseString);
//                			AiInterfaceOutputDto aiInterfaceOutputDto = this.assistantService.parseStringToAiInterfaceOutputDto(whatsAppCustomerParameterDataDto.getOrganization().getOrganization(), whatsAppCustomerParameterDataDto.getWhatsAppBotThread(), this.whatsAppAssistantEntity.getAssistantId(), responseString);
//                			
//                			if(aiInterfaceOutputDto.isStopAIMessage())
//                			{
//                				//System.out.println("User wants to stop AI messages, we wont send message after this for sometime now.");
//                				whatsAppCustomerParameterDataDto.setAction("block-ai");
//                        		WhatsAppCustomerData.workWithWhatsAppCustomerData(whatsAppCustomerParameterDataDto);
//                			}
//
//                			boolean summarize = false;
//                			String summarizeText = "";
//                			List <SupportTicket> allOpenTickets = null;
//                			String supportEmail = this.env.getProperty("spring.parentorginization.support.email");
//                			String supportPhone = this.env.getProperty("spring.parentorginization.phone");
//                			String result = "";
//                			
//                			//System.out.println("Verify Current Open Tickets");
//                			if(aiInterfaceOutputDto.isCustomerAskingAboutPreviousCustomerSupportTicket()) {
//                				allOpenTickets = supportTicketRepository.findByOpenAndOrganizationAndCustomerId(true, whatsAppCustomerParameterDataDto.getOrganization().getOrganization(), currentCustomer.getId());
//                				result = allOpenTickets.stream()
//                					    .map(ticket -> (ticket.getComplaintId() != null ? ticket.getComplaintId() : "NA")
//                					                + " || "
//                					                + (ticket.getComplaint() != null ? ticket.getComplaint() : "No description"))
//                					    .collect(Collectors.joining(" ;; "));
//                				
//                				summarizeText = summarizeText+"User current open tickets (ticketId || complaint), data: " + result + ".";
//                				//System.out.println(summarizeText);
//            					summarize = true;
//                			}
//                			else {
//                    			
//                    			if(aiInterfaceOutputDto.isCustomerAskingCreateCustomerSupportTicket())
//                    			{
//                    				allOpenTickets = supportTicketRepository.findByOpenAndOrganizationAndCustomerId(true, whatsAppCustomerParameterDataDto.getOrganization().getOrganization(), currentCustomer.getId());
//                    			
//                    				if (allOpenTickets == null || allOpenTickets.isEmpty()) {
//                    					
//                    					SupportTicket currenTicket = new SupportTicket();
//                    					currenTicket.setComplaintId(TicketUtils.generateTicketId());
//                    					currenTicket.setOpen(true);
//                    					currenTicket.setOrganization(whatsAppCustomerParameterDataDto.getOrganization().getOrganization());
//                    					currenTicket.setProductOrServiceName(aiInterfaceOutputDto.getProductOrServiceName());
//                    					currenTicket.setCustomerEmailId(aiInterfaceOutputDto.getCustomerEmailId());
//                    					currenTicket.setComplaint(aiInterfaceOutputDto.getComplaint());
//                    					currenTicket.setCustomerId(currentCustomer.getId());
//                    					
//                    					currenTicket = supportTicketRepository.save(currenTicket);
//                    					result = currenTicket.getComplaintId() + " ::: " + currenTicket.getComplaint();
//                    					
//                    					summarizeText = summarizeText+"New ticket created, ticketId || complaint: " + result + ".";
//                    					//System.out.println(summarizeText);
//                    					summarize = true;
//                    					
//                    				}else {
//                    					
//                    					result = allOpenTickets.stream()
//                    						    .map(ticket -> (ticket.getComplaintId() != null ? ticket.getComplaintId() : "NA")
//                    						                + " || "
//                    						                + (ticket.getComplaint() != null ? ticket.getComplaint() : "No description"))
//                    						    .collect(Collectors.joining(" ;; "));
//                        				
//                    					summarizeText = summarizeText+"User already has open tickets, ticketId || complaint: " + result +
//                    			                ". If unresolved, contact " + supportEmail +
//                    			                " or WhatsApp at: " + supportPhone + ".";
//                    					//System.out.println(summarizeText);
//                    					summarize = true;
//                    				}
//                    			}
//                			}
//                			
//                			
//                			Customers currentCustomers = currentCustomer;
//                 			Organization organization = whatsAppCustomerParameterDataDto.getOrganization();
//
//                			//Schedule call
//                			//Add initial queue and divert call to queue
//                 			//System.out.println("Verify Schedule call");
//           			        JobSchedulingService jobSchedulingService =  applicationContext.getBean(JobSchedulingService.class); 
//                			boolean jobAlreadyScheduled = false;
//                			
//                			//STILL NEED TO GET THIS
//                			String ivrExtension = phoneNumberObject.getAiCallExtension();
//                			
//                            if(aiInterfaceOutputDto.isCustomerAskingAboutCurrentScheduleCall()) {
//                            	jobAlreadyScheduled = jobSchedulingService.findIfScheduledCallJobToCustomer(TrackedSchduledJobs.fixeddate, currentCustomers.getPhoneNumber(), ivrExtension, currentCustomers.getOrganization());
//                            	summarizeText = summarizeText+"User already has a call scheduled with us." +
//            			                " For quick turn around whatsApp at: " + supportPhone + ".";
//            					//System.out.println(summarizeText);
//            					summarize = true;
//                            }
//                			else {
//                    			
//                    			if(aiInterfaceOutputDto.isCustomerAskingAboutNewScheduleCall())
//                    			{
//                    				jobAlreadyScheduled = jobSchedulingService.findIfScheduledCallJobToCustomer(TrackedSchduledJobs.fixeddate, currentCustomers.getPhoneNumber(), ivrExtension, currentCustomers.getOrganization());
//                    				
//                    				if(!jobAlreadyScheduled) {
//                    					
//                    					if(ivrExtension != null && ivrExtension == "") 
//                    					{
//	                    					FixedDateSchedulerDefinitionDTO fixedDateSchedulerDefinitionDTO = new FixedDateSchedulerDefinitionDTO();
//	                             			OffsetDateTime odt = OffsetDateTime.parse(aiInterfaceOutputDto.getScheduleDateTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
//	                             	        Date date = Date.from(odt.toInstant());
//	                             	        
//	                            			fixedDateSchedulerDefinitionDTO.setFirstName(currentCustomers.getFirstname());
//	                             			fixedDateSchedulerDefinitionDTO.setOrganization(currentCustomers.getOrganization());
//	                             			fixedDateSchedulerDefinitionDTO.setPhoneNumber(currentCustomers.getPhoneNumber());
//	                            			fixedDateSchedulerDefinitionDTO.setCampaignId(null);
//	                            			fixedDateSchedulerDefinitionDTO.setContext(organization.getPhoneContext());
//	                            			fixedDateSchedulerDefinitionDTO.setProtocol(organization.getProtocol());
//	                            			fixedDateSchedulerDefinitionDTO.setPhoneTrunk(organization.getPhoneTrunk());
//	                            			fixedDateSchedulerDefinitionDTO.setPriority(1); // or any int
//	                            			fixedDateSchedulerDefinitionDTO.setTimeOut(60000L); // timeout in ms
//	                            			fixedDateSchedulerDefinitionDTO.setDate(date); // or your specific date
//	                            			fixedDateSchedulerDefinitionDTO.setDomain(organization.getDomain());
//	                            			fixedDateSchedulerDefinitionDTO.setActionType("actionType");
//	                            			fixedDateSchedulerDefinitionDTO.setData("");
//	                            			fixedDateSchedulerDefinitionDTO.setCallType("");
//	                            			fixedDateSchedulerDefinitionDTO.setFromExtension(ivrExtension);
//	                            			
//	                            			jobSchedulingService.scheduleAFixedDateCallToCustomer(fixedDateSchedulerDefinitionDTO, null, applicationContext);
//	                            			
//	                            			summarizeText = summarizeText+"Call has been scheduled with us at aiInterfaceOutputDto.getScheduleDateTime()." +
//	                    			                " For quick turn around whatsApp at: " + supportPhone + ".";
//	                    					//System.out.println(summarizeText);
//	                    					summarize = true;
//                    					}
//                    					else {
//                    						//System.out.println("Support number default AI calling route not mentioned");
//                    					}
//                    				}
//                    				else {
//                    					summarizeText = summarizeText+"User already has a call scheduled with us." +
//                    			                " For quick turn around whatsApp at: " + supportPhone + ".";
//                    					//System.out.println(summarizeText);
//                    					summarize = true;
//                    				}
//                    			}
//                			}
//                			
//                            
//
//                			//Summarize again if required
//                			if(summarize) {
//                				List<String> listOfOutputList = aiInterfaceOutputDto.getLlmResponse();
//                				String combinedString = summarizeText;
//                				combinedString = combinedString + String.join(" ", listOfOutputList);
//                				//System.out.println("Sending request to summary bot assistant.");
//                    			String summaryString = this.assistantService.addMessageAndStream(whatsAppCustomerParameterDataDto.getOrganization().getOrganization(), this.summaryAssistantEntity.getAssistantId(), whatsAppCustomerParameterDataDto.getSummarizeThread(), combinedString);
//                    			listOfOutputList=new ArrayList<>();
//                    			listOfOutputList.add(summaryString);
//                    			aiInterfaceOutputDto.setLlmResponse(listOfOutputList);
//                    			//System.out.println("summaryString for aiInterfaceOutputDto : "+summaryString);
//                			}
//                			
//                			
//               				//Convert it into list of ChatHistory object and return back data.
//                			//System.out.println("Convert it into list of ChatHistory objects.");
//                		    List<WhatsAppChatHistory> listOfMessages = convertaiInterfaceOutputDtoToWhatsAppChatHistory(aiInterfaceOutputDto);
//                		    
//                		    //System.out.println("Trigger outbound flow");
//                			triggerWhatsAppAiOutboundFlow(listOfMessages);
//                			
//                			}
//                			else {
//                				//System.out.println("chunk vector size zero");
//                			}
//            			
//            			}
//            			else {
//            				//System.out.println("Chunk text size 0");
//            			} 
//            		}
//            		else {
//            			//System.out.println("englishMessage was null, hence not trigger AI message");
//            		}
//            	}
//            	else {
//            		//System.out.println("Customer not found in memory data");
//            	}
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//    }
//    
//   	
//   	List<WhatsAppChatHistory> convertaiInterfaceOutputDtoToWhatsAppChatHistory(AiInterfaceOutputDto aiInterfaceOutputDto) {
//   		List<WhatsAppChatHistory> toReturn = new ArrayList<>();
//   		try {
//   	    	   //Covert into chat-history object
//   			Map <String, String> allFiles = aiInterfaceOutputDto.getFiles();
//   			List <String> llmResponse = aiInterfaceOutputDto.getLlmResponse();
//   			List <String> allFileNamesList = new ArrayList<>();
//   			
//   			MediaRepository mediaRepository = applicationContext.getBean(MediaRepository.class);
//   			
//   			//System.out.println("Working on media messages");
//   			for (Map.Entry<String, String> entry : allFiles.entrySet()) {
//   			    String fileName = entry.getKey();
//   			    String fileContent = entry.getValue();
//   			    //System.out.println("File: " + fileName + " → " + fileContent);
//		   		allFileNamesList.add(fileName);
//   			}
//   			
//   			//System.out.println("Initial Media List By LLM :"+objectMapper.writeValueAsString(allFiles));
//   			
//   			List <Media> allMediaList = mediaRepository.findByNameInAndOrganization(allFileNamesList, whatsAppCustomerParameterDataDto.getOrganization().getOrganization());
//
//   			//System.out.println("Medial List By Mylinehub database :"+objectMapper.writeValueAsString(allMediaList));
//   			
//   			Set<String> addedFileNames = new HashSet<>();
//   			
//   			for (Media media : allMediaList) {
//   			  
//   				// Skip if filename already added
//   			    if (addedFileNames.contains(media.getName())) {
//   			        continue;
//   			    }
//   			    
//   				String sizeInMB = String.format("%.2f", (double) media.getSize() / (1024 * 1024));
//			    WhatsAppChatHistory chatHistory = createNewChatHistoryObject();
//		   		chatHistory.setMessageType(detectWhatsAppFileType(media.getName()));
//		   		//Bold: *text* or Italic: _text_
//		   		chatHistory.setMessageString("*"+media.getName()+",*_"+sizeInMB+"MB_");
//		   		chatHistory.setWhatsAppMediaId(String.valueOf(media.getId()));
//		   		chatHistory.setBlobType(media.getType());
//		   		chatHistory.setFileName(media.getName());
//		   		chatHistory.setFileSizeInMB(sizeInMB);	
//		   		
//		   		toReturn.add(chatHistory);
//		   	    addedFileNames.add(media.getName()); // track added filename
//		   	    
//   			}
//   			
//   			
//   			
//   		    //System.out.println("Working on text messages");
//   		    //System.out.println("Final Text Response By LLM :"+objectMapper.writeValueAsString(llmResponse));  
//   			for (String response : llmResponse) {
//   			    WhatsAppChatHistory chatHistory = createNewChatHistoryObject();
//		   		chatHistory.setMessageType(MESSAGE_TYPE.text.name());
//		   		chatHistory.setMessageString(response);
//		   		toReturn.add(chatHistory);
//   			}
//   		}
//   		catch(Exception e)
//   		{
//   			e.printStackTrace();
//   			toReturn = null;
//   		}
//   		return toReturn;
//   	}
//   	
//   	
//   	
//   	WhatsAppChatHistory createNewChatHistoryObject() {
//   		
//   	    WhatsAppChatHistory chatHistory = new WhatsAppChatHistory();
//		// Basic info
//		chatHistory.setOrganization(whatsAppCustomerParameterDataDto.getOrganization().getOrganization());
//		chatHistory.setPhoneNumberMain(whatsAppCustomerParameterDataDto.getWhatsAppRegisteredByPhoneNumber());
//		chatHistory.setPhoneNumberWith(currentCustomer.getPhoneNumber());
//		chatHistory.setFromExtension("MYLINEHUB");
//		chatHistory.setFromName("AI Agent");
//		chatHistory.setFromTitle("Rag Support");
//		chatHistory.setOpenAIAssistantName("multiple");
//		chatHistory.setOpenAIAssistantThread(whatsAppCustomerParameterDataDto.getLanguageThread()+"::"+whatsAppCustomerParameterDataDto.getWhatsAppBotThread()+"::"+whatsAppCustomerParameterDataDto.getSummarizeThread());
//   		chatHistory.setOutbound(true);
//   		chatHistory.setInbound(false);
//   		return chatHistory;
//   	}
//   	
//
//    //If output null, heuristic message , or else shoots whats app response , trigger outbound whats app flow
//   	public boolean triggerWhatsAppAiOutboundFlow(List<WhatsAppChatHistory> listOfMessages)
//   	{
//   		boolean toReturn = true;
//   		
//   		try {
//
//   			    if(listOfMessages == null || listOfMessages.size()==0) {
//   			    	//System.out.println("[triggerWhatsAppAiOutboundFlow] : No chat history object found to send.");
//   			    	return false;
//   			    }
//   			    
//
//   	    	    //If return is not null, iterate over list of chat history, call a sync function to send message back to customer , choose to follow path of already build layers, It should be same code
//   	    	    /* 
//   	    	     * --Above will save to chat history
//   	    	     * --Send actual whats app message back to user
//   	    	     * --Manage status
//   	    	     * --Manage reports
//   	    	     * --Send stomp to other employee
//   	    	     */
//   			    
//   			    WhatsAppIntegrationOutboundService whatsAppIntegrationOutboundService = applicationContext.getBean(WhatsAppIntegrationOutboundService.class);
//   			    //System.out.println("Trigger sending whats app messages : total : "+listOfMessages.size());
//	   			for (WhatsAppChatHistory whatsAppChatHistory : listOfMessages) {
//	   				whatsAppIntegrationOutboundService.sendOutboundWhatsAppChatHistory(whatsAppChatHistory,  CHAT_ORIGIN.ai.name());
//	   			}
//
//   		}
//   		catch(Exception e)
//   		{
//   			e.printStackTrace();
//   			toReturn = false;
//   		}
//   		
//   		return toReturn;
//   	}
//   	
//   	
//   	public static String detectWhatsAppFileType(String fileName) {
//
//   	    // Define all allowed file-type sets inline
//	   	Set<String> audio = Set.of(SUPPORTED_FORMATS.audio);
//	    Set<String> video = Set.of(SUPPORTED_FORMATS.video);
//	    Set<String> document = Set.of(SUPPORTED_FORMATS.document);
//	    Set<String> sticker = Set.of(SUPPORTED_FORMATS.sticker);
//	    Set<String> image = Set.of(SUPPORTED_FORMATS.image);
//     
//   	    // Define return-type strings
//   	    String TYPE_TEXT = MESSAGE_TYPE.text.name();
//   	    String TYPE_AUDIO = MESSAGE_TYPE.audio.name();
//   	    String TYPE_VIDEO = MESSAGE_TYPE.video.name();
//   	    String TYPE_STICKER = MESSAGE_TYPE.sticker.name();
//   	    String TYPE_IMAGE = MESSAGE_TYPE.image.name();
//   	    String TYPE_DOCUMENT = MESSAGE_TYPE.document.name();
//
//   	    // Handle null or invalid filename
//   	    if (fileName == null || !fileName.contains(".")) {
//   	        return TYPE_TEXT;
//   	    }
//
//   	    // Extract extension (after last dot)
//   	    String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
//
//   	    // Match extension against sets
//   	    if (audio.contains(extension)) return TYPE_AUDIO;
//   	    if (video.contains(extension)) return TYPE_VIDEO;
//   	    if (sticker.contains(extension)) return TYPE_STICKER;
//   	    if (image.contains(extension)) return TYPE_IMAGE;
//   	    if (document.contains(extension)) return TYPE_DOCUMENT;
//
//   	    // Fallback — not recognized
//   	    System.err.println("Unsupported file type: " + extension);
//   	    System.err.println("Allowed formats: {aac, mp4, mpeg, amr, ogg, 3sp, webp, png, jpeg, plain, pdf, vnd.ms-powerpoint, msword, vnd.ms-excel, vnd.openxmlformats-officedocument.*}");
//   	    return TYPE_TEXT;
//   	}
//
//   	
//   	
//}
