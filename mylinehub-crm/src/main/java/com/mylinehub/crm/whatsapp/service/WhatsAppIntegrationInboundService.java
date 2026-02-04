package com.mylinehub.crm.whatsapp.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mylinehub.crm.data.dto.MultiPartFileDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mylinehub.crm.ami.autodialer.AutodialerReinitiateAndFunctionService;
import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.data.StartedCampaignData;
import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.enums.FILE_STORE_REQUEST_TYPE;
import com.mylinehub.crm.service.CampaignService;
import com.mylinehub.crm.service.CurrentTimeInterface;
import com.mylinehub.crm.service.FileUploadService;
import com.mylinehub.crm.whatsapp.mapper.WhatsAppFlattenMessageMapper;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.media.OkHttpDownloadMediaClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.media.OkHttpRetrieveMediaUrlClient;
import com.mylinehub.crm.whatsapp.data.WhatsAppCurrentConversation;
import com.mylinehub.crm.whatsapp.data.WhatsAppCustomerData;
import com.mylinehub.crm.whatsapp.data.WhatsAppFlattenMessageConversation;
import com.mylinehub.crm.whatsapp.data.WhatsAppReportingData;
import com.mylinehub.crm.whatsapp.dto.WhatsAppFlattenMessageDTO;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppChatDataParameterDTO;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerDataDto;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerParameterDataDto;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppFlattenMessageParameterDTO;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppReportDataParameterDTO;
import com.mylinehub.crm.whatsapp.dto.general.LocationDto;
import com.mylinehub.crm.whatsapp.dto.general.MediaDto;
import com.mylinehub.crm.whatsapp.dto.general.OrderDto;
import com.mylinehub.crm.whatsapp.dto.general.ReactionDto;
import com.mylinehub.crm.whatsapp.dto.general.TextDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ButtonDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ChangeDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ComponentDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ContactDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ContextDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ConversationDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.EntryDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ErrorDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.IdentityDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.InteractiveDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.MessagesDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.PaymentDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.PricingDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ReferralDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.StatusesDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.SystemMessageDto;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ValueDto;
import com.mylinehub.crm.whatsapp.dto.service.MediaUploadDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.enums.MESSAGE_TYPE;
import com.mylinehub.crm.whatsapp.enums.SEND_MESSAGE_KEYS;
import com.mylinehub.crm.whatsapp.enums.chat.CHAT_ORIGIN;
import com.mylinehub.crm.whatsapp.enums.webhook.MESSAGE_STATUS_TYPE;
import com.mylinehub.crm.whatsapp.repository.WhatsAppChatHistoryRepository;
import com.mylinehub.crm.whatsapp.repository.WhatsAppFlattenMessageRepository;
import com.mylinehub.crm.whatsapp.repository.WhatsAppNumberReportRepository;
import lombok.AllArgsConstructor;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class WhatsAppIntegrationInboundService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
	private final SimpMessagingTemplate simpMessagingTemplate;
	private final ApplicationContext applicationCotext; 
    private final WhatsAppFlattenMessageMapper whatsAppFlattenMessageMapper;
    private final WhatsAppFlattenMessageRepository whatsAppFlattenMessageRepository;
    private final WhatsAppChatHistoryService whatsAppChatHistoryService;
    private final WhatsAppChatHistoryRepository whatsAppChatHistoryRepository;
    private final CallMediaAPIService callMediaAPIService; 
    private final OkHttpRetrieveMediaUrlClient okHttpRetrieveMediaUrlClient;
    private final OkHttpDownloadMediaClient okHttpDownloadMediaClient;
    private final FileUploadService fileUploadService;
    private final WhatsAppAIService whatsAppAIService;
    private final AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService;
    private final CampaignService campaignService;
    
	//From whats app original response extracts display phone number
	public String extract_phone_number_main(String input) {
	    //System.out.println("[extract_phone_number_main] Start extracting phone number");
	    String phoneNumber = "";

	    try {
	        JSONObject payload = new JSONObject(input);
	        JSONArray entryArray = payload.getJSONArray("entry");
	        JSONObject entry = entryArray.getJSONObject(0);
	        JSONArray changesArray = entry.getJSONArray("changes");
	        JSONObject change = changesArray.getJSONObject(0);
	        JSONObject value = change.getJSONObject("value");
	        JSONObject metadata = value.getJSONObject("metadata");
	        phoneNumber = metadata.getString("display_phone_number");
	        //System.out.println("[extract_phone_number_main] Extracted phone number: " + phoneNumber);
	    } catch (Exception e) {
	        System.err.println("[extract_phone_number_main] Error extracting phone number: " + e.getMessage());
	        return null;
	    }

	    return "+" + phoneNumber;
	}

	//Convert into flatten message
	//Save flatten message to database
	//Call trigger flow
	public WhatsAppChatHistory digestWhatsAppInputMessageAndSendToOwn(
	        WhatsAppPhoneNumber phoneNumberObject,
	        String phoneNumberMain,
	        ComponentDto whatsAppInput) throws Exception {

	    //System.out.println("[digestWhatsAppInputMessageAndSendToOwn] Started processing for phoneNumberMain: " + phoneNumberMain);
	    WhatsAppChatHistory toReturn = null;

	    String organizationString = phoneNumberObject.getOrganization();
	    //System.out.println("[digestWhatsAppInputMessageAndSendToOwn] Fetching organization data for: " + organizationString);

	    Map<String, Organization> organizationMap = OrganizationData.workWithAllOrganizationData(organizationString, null, "get-one", null);
	    Organization organization = null;

	    if (organizationMap.size() > 0) {
	        organization = organizationMap.get(organizationString);
	    }

	    if (organization == null) {
	        System.err.println("[digestWhatsAppInputMessageAndSendToOwn] Organization not found: " + organizationString);
	        throw new Exception(organizationString + " not found in database as registered organization for number: " + phoneNumberMain);
	    }

	    //System.out.println("[digestWhatsAppInputMessageAndSendToOwn] Organization found: " + organizationString);

	    try {
	        //System.out.println("[digestWhatsAppInputMessageAndSendToOwn] Processing WhatsApp input entries");

			List<EntryDto> entryList = whatsAppInput.getEntry();

			if (entryList != null && !entryList.isEmpty()) {
			    for (EntryDto entryDto : entryList) {
			        //System.out.println("[digestWhatsAppInputMessageAndSendToOwn] Processing entry");

			        // Convert to flattened messages
			        List<WhatsAppFlattenMessageDTO> whatsAppFlattenMessageDTOs = extractFlattenedMessages(entryDto, whatsAppInput);

			        for (int i = 0; i < whatsAppFlattenMessageDTOs.size(); i++) {
			            //System.out.println("[digestWhatsAppInputMessageAndSendToOwn] Processing flatten message #" + i);
			            WhatsAppFlattenMessageDTO whatsAppFlattenMessageDTO = whatsAppFlattenMessageDTOs.get(i);

			            //System.out.println("Debugging WhatsAppFlattenMessageDTO values:");
			            //System.out.println("MessageContext: " + whatsAppFlattenMessageDTO.getMessageContext());
			            //System.out.println("MessageIdentity: " + whatsAppFlattenMessageDTO.getMessageIdentity());
			            //System.out.println("MessageReferral: " + whatsAppFlattenMessageDTO.getMessageReferral());
			            //System.out.println("MessageErrors: " + whatsAppFlattenMessageDTO.getMessageErrors());
			            //System.out.println("MessageType: " + whatsAppFlattenMessageDTO.getMessageType());
			            //System.out.println("MessageText: " + whatsAppFlattenMessageDTO.getMessageText());
			            //System.out.println("MessageMedia: " + whatsAppFlattenMessageDTO.getMessageMedia());
			            //System.out.println("MessageReaction: " + whatsAppFlattenMessageDTO.getMessageReaction());
			            //System.out.println("MessageOrder: " + whatsAppFlattenMessageDTO.getMessageOrder());
			            //System.out.println("MessageContacts: " + whatsAppFlattenMessageDTO.getMessageContacts());
			            //System.out.println("MessageLocation: " + whatsAppFlattenMessageDTO.getMessageLocation());
			            //System.out.println("MessageInteractive: " + whatsAppFlattenMessageDTO.getMessageInteractive());
			            //System.out.println("MessageButton: " + whatsAppFlattenMessageDTO.getMessageButton());
			            //System.out.println("MessageSystem: " + whatsAppFlattenMessageDTO.getMessageSystem());
			            //System.out.println("StatusesStatus: " + whatsAppFlattenMessageDTO.getStatusesStatus());
			            //System.out.println("StatusesConversation: " + whatsAppFlattenMessageDTO.getStatusesConversation());
			            //System.out.println("StatusesPricing: " + whatsAppFlattenMessageDTO.getStatusesPricing());
			            //System.out.println("Payment: " + whatsAppFlattenMessageDTO.getPayment());
			            //System.out.println("Name: " + whatsAppFlattenMessageDTO.getName());
			            //System.out.println("MessageFrom: " + whatsAppFlattenMessageDTO.getMessageFrom());
			            //System.out.println("WhatsAppDisplayPhoneNumber: " + whatsAppFlattenMessageDTO.getWhatsAppDisplayPhoneNumber());
			            //System.out.println("MessageId: " + whatsAppFlattenMessageDTO.getMessageId());

			            
			            // Submit flattened message to memory/db
			            //System.out.println("[digestWhatsAppInputMessageAndSendToOwn] Submitting flatten message to database");

			            WhatsAppFlattenMessageParameterDTO whatsAppFlattenMessageParameterDTO = new WhatsAppFlattenMessageParameterDTO();
			            whatsAppFlattenMessageParameterDTO.setWhatsAppFlattenMessage(whatsAppFlattenMessageMapper.mapDTOToWhatsAppFlattenMessage(whatsAppFlattenMessageDTO));
			            whatsAppFlattenMessageParameterDTO.setWhatsAppFlattenMessageRepository(whatsAppFlattenMessageRepository);
			            whatsAppFlattenMessageParameterDTO.setAction("update");

			            WhatsAppFlattenMessageConversation.workOnWhatsAppFlattenMessageMemoryList(whatsAppFlattenMessageParameterDTO);

			            // Update customer data
//			            //System.out.println("[digestWhatsAppInputMessageAndSendToOwn] Updating customer data");

			            String fullName = whatsAppFlattenMessageDTO.getName();
			            fullName = fullName.trim();


			            int firstSpaceIndex = fullName.indexOf(" ");

			            String firstName;
			            String lastName;

			            if (firstSpaceIndex == -1) {

			                firstName = fullName;
			                lastName = "";
			            } else {
			                firstName = fullName.substring(0, firstSpaceIndex);
			                lastName = fullName.substring(firstSpaceIndex).trim();
			            }
			            
			            WhatsAppCustomerParameterDataDto whatsAppCustomerParameterDataDto = new WhatsAppCustomerParameterDataDto();
			            whatsAppCustomerParameterDataDto.setAction("get-one-and-chage-date-or-save-or-fetch-customer-if-applicable");
			            whatsAppCustomerParameterDataDto.setFirstName(firstName);
			            whatsAppCustomerParameterDataDto.setLastName(lastName);
			            whatsAppCustomerParameterDataDto.setPhoneNumber(whatsAppFlattenMessageDTO.getMessageFrom());
			            whatsAppCustomerParameterDataDto.setEmail("N/A");
			            whatsAppCustomerParameterDataDto.setWhatsAppRegisteredByPhoneNumberId(phoneNumberObject.getPhoneNumberID());
			            whatsAppCustomerParameterDataDto.setWhatsAppRegisteredByPhoneNumber(phoneNumberMain);
			            whatsAppCustomerParameterDataDto.setWhatsApp_wa_id(whatsAppFlattenMessageDTO.getWhatsApp_wa_id());
			            whatsAppCustomerParameterDataDto.setWhatsAppDisplayPhoneNumber(whatsAppFlattenMessageDTO.getWhatsAppDisplayPhoneNumber());
			            whatsAppCustomerParameterDataDto.setWhatsAppPhoneNumberId(whatsAppFlattenMessageDTO.getWhatsAppPhoneNumberId());
			            whatsAppCustomerParameterDataDto.setWhatsAppProjectId(String.valueOf(phoneNumberObject.getWhatsAppProject().getId()));
			            whatsAppCustomerParameterDataDto.setBusinessPortfolio(phoneNumberObject.getWhatsAppProject().getBusinessPortfolio());
			            
			            if(organization.isAllowWhatsAppAutoAIMessage())
			            {
			            	whatsAppCustomerParameterDataDto.setTurnOnAutoReply(true);
			            }
			            else
			            {
			            	whatsAppCustomerParameterDataDto.setTurnOnAutoReply(false);
			            }
			            
			            whatsAppCustomerParameterDataDto.setOrganization(organization);
			            
			            String messageFrom = whatsAppFlattenMessageDTO.getMessageFrom();

			            if (messageFrom != null && !messageFrom.trim().isEmpty()) {
			                //System.out.println("Valid Customer / messageFrom: '" + messageFrom + "'. Proceeding with customer data processing.");
			                Map<String, WhatsAppCustomerDataDto> retunedCustomerObject = WhatsAppCustomerData.workWithWhatsAppCustomerData(whatsAppCustomerParameterDataDto);
			                WhatsAppCustomerDataDto whatsAppCustomerDataDto = null;
			                Customers currentCustomer = null;
			                
			                if(retunedCustomerObject!=null)
			                	whatsAppCustomerDataDto = retunedCustomerObject.get(whatsAppFlattenMessageDTO.getMessageFrom()+organizationString.trim());
			                else
			                	throw new Exception("No customer found");
			                
			                if(whatsAppCustomerDataDto!=null)
			                	currentCustomer = whatsAppCustomerDataDto.getCustomer();
			                else
			                	throw new Exception("No customer found");
			                
			                if(currentCustomer == null)
				                	throw new Exception("No customer found");
			                
			                // Trigger WhatsApp flow
				            //System.out.println("[digestWhatsAppInputMessageAndSendToOwn] Triggering WhatsApp flow");
				            toReturn = triggerWhatsAppFlow(currentCustomer,phoneNumberObject, organization, whatsAppFlattenMessageDTO);
			            } else {
			                //System.out.println("Invalid Customer / messageFrom: '" + messageFrom + "'. Skipping customer data processing.");
			            }

			          
			            if (toReturn == null) {
			                System.err.println("[digestWhatsAppInputMessageAndSendToOwn] WhatsApp flow trigger failed for message #" + i);
			                // Optional: notify admin or log for missed flow
			            }
			        }
			    }
			} else {
			    //System.out.println("[digestWhatsAppInputMessageAndSendToOwn] No entries to process in WhatsApp input");
			}
	    } catch (Exception e) {
	        System.err.println("[digestWhatsAppInputMessageAndSendToOwn] Exception caught during processing: " + e.getMessage());
	        throw e;
	    }

	    //System.out.println("[digestWhatsAppInputMessageAndSendToOwn] Processing completed successfully");
	    return toReturn;
	}

	
	//Find what elements flatten message has like new message or else status etc. As per scenario triggers below function.
	public WhatsAppChatHistory triggerWhatsAppFlow(Customers currentCustomer,WhatsAppPhoneNumber phoneNumberObject, Organization organization, WhatsAppFlattenMessageDTO whatsAppFlattenMessageDTO) throws Exception {
		
        WhatsAppChatHistory whatsAppChatHistory = new WhatsAppChatHistory();
	    MediaUploadDto mediaUploadDto = null;
	    ObjectMapper mapper = new ObjectMapper();

	    try {


	        //System.out.println("Converting to BotInput, Chat History Format and Media");

	        // Helper for null or empty checks
	        Predicate<String> isNotEmpty = s -> s != null && !s.trim().isEmpty();

	     // Check and log each field
	        boolean hasMessageContext = isNotEmpty.test(whatsAppFlattenMessageDTO.getMessageContext());
	        //System.out.println("hasMessageContext: " + hasMessageContext + ", value: " + whatsAppFlattenMessageDTO.getMessageContext());

	        boolean hasMessageIdentity = isNotEmpty.test(whatsAppFlattenMessageDTO.getMessageIdentity());
	        //System.out.println("hasMessageIdentity: " + hasMessageIdentity + ", value: " + whatsAppFlattenMessageDTO.getMessageIdentity());

	        boolean hasMessageReferral = isNotEmpty.test(whatsAppFlattenMessageDTO.getMessageReferral());
	        //System.out.println("hasMessageReferral: " + hasMessageReferral + ", value: " + whatsAppFlattenMessageDTO.getMessageReferral());

	        boolean hasMessageErrors = isNotEmpty.test(whatsAppFlattenMessageDTO.getMessageErrors());
	        //System.out.println("hasMessageErrors: " + hasMessageErrors + ", value: " + whatsAppFlattenMessageDTO.getMessageErrors());

	        boolean hasMessageType = isNotEmpty.test(whatsAppFlattenMessageDTO.getMessageType());
	        //System.out.println("hasMessageType: " + hasMessageType + ", value: " + whatsAppFlattenMessageDTO.getMessageType());
	        
	        
	        if (hasMessageErrors) {
	            //System.out.println("Detected message errors");
	            List<ErrorDto> errors = mapper.readValue(whatsAppFlattenMessageDTO.getMessageErrors(), new TypeReference<List<ErrorDto>>() {});

	            if (errors.size() == 1) {
	                ErrorDto error = errors.get(0);
	                //System.out.println("Error Code: " + error.getCode());

	                if ("131051".equals(error.getCode())) {
	                    //System.out.println("Message deleted detected, verifying and updating status");
	                    verifyIfInMemoryDataAndThenUpdateStatus(
	                    		organization,
	                    		currentCustomer,
	                            mapper.writeValueAsString(error), null, phoneNumberObject,
	                            whatsAppFlattenMessageDTO.getMessageId(),
	                            MESSAGE_STATUS_TYPE.deleted.name(),
	                            whatsAppFlattenMessageDTO.getMessageFrom());
	                }
	            } else {
	                //System.out.println("Multiple errors found - feature not implemented");
	            }
	        }

	        if (hasMessageReferral) {
	            //System.out.println("Message referral present - currently no processing implemented");
	        }

	        if (hasMessageContext) {
	            //System.out.println("Message context detected, extracting previous message ID");
	            ContextDto context = mapper.readValue(whatsAppFlattenMessageDTO.getMessageContext(), ContextDto.class);
	            whatsAppChatHistory.setPreviousMessageId(context.getId());
	        }

	        if (hasMessageIdentity) {
	            //System.out.println("Message identity detected - currently no processing implemented");
	        }

	        if (!hasMessageType) {
	            //System.out.println("No message type found, may be status update");
	        } else {
	        	
	        	boolean processChatHistory = true;
	        	//System.out.println("Processing message boolean: " + processChatHistory);
	            String messageType = whatsAppFlattenMessageDTO.getMessageType();
	            //System.out.println("Processing message type: " + messageType);

		        //System.out.println("Creating WhatsApp Chat History Request");
		        whatsAppChatHistory.setCreatedOn(new Date());
		        whatsAppChatHistory.setMessageOrigin(CHAT_ORIGIN.whatsapp.name());
		        whatsAppChatHistory.setLastUpdateTime(new Date());
		        whatsAppChatHistory.setOrganization(organization.getOrganization());
		        whatsAppChatHistory.setPhoneNumberMain(phoneNumberObject.getPhoneNumber());
		        whatsAppChatHistory.setPhoneNumberWith(whatsAppFlattenMessageDTO.getMessageFrom());
		        whatsAppChatHistory.setFromExtension("Not Applicable");
		        whatsAppChatHistory.setFromName(whatsAppFlattenMessageDTO.getName());
		        whatsAppChatHistory.setFromTitle(whatsAppFlattenMessageDTO.getMessageFrom());
		        whatsAppChatHistory.setWhatsAppMessageId(whatsAppFlattenMessageDTO.getMessageId());
		        whatsAppChatHistory.setOutbound(false);
		        whatsAppChatHistory.setInbound(true);
		        whatsAppChatHistory.setSent(false);
		        whatsAppChatHistory.setDelivered(false);
		        whatsAppChatHistory.setRead(false);
		        whatsAppChatHistory.setFailed(false);
		        whatsAppChatHistory.setDeleted(false);
		        whatsAppChatHistory.setMessageType(whatsAppFlattenMessageDTO.getMessageType());

		        //System.out.println("Adding to report object");
		        WhatsAppReportDataParameterDTO whatsAppReportDataParameterDTO = new WhatsAppReportDataParameterDTO();
		        whatsAppReportDataParameterDTO.setAiMessage(false);
		        whatsAppReportDataParameterDTO.setCampaignMessage(false);
		        whatsAppReportDataParameterDTO.setManualMessage(false);
		        whatsAppReportDataParameterDTO.setWhatsAppNumberReportRepository(applicationCotext.getBean(WhatsAppNumberReportRepository.class));
		        whatsAppReportDataParameterDTO.setWhatsAppNumberReportService(applicationCotext.getBean(WhatsAppNumberReportService.class));
		        whatsAppReportDataParameterDTO.setApplicationContext(applicationCotext);
		        whatsAppReportDataParameterDTO.setInputDTO(whatsAppChatHistory);
		        whatsAppReportDataParameterDTO.setOrganization(phoneNumberObject.getOrganization());
		        whatsAppReportDataParameterDTO.setPhoneNumberMain(phoneNumberObject.getPhoneNumber());
		        whatsAppReportDataParameterDTO.setPhoneNumberWith(whatsAppFlattenMessageDTO.getMessageFrom());
		        whatsAppReportDataParameterDTO.setPhoneNumber(phoneNumberObject);
		        whatsAppReportDataParameterDTO.setAction("update-received-stats");
		        
	            switch (messageType) {
	                case "text":
	                	//System.out.println("Started processing");
	                    if (isNotEmpty.test(whatsAppFlattenMessageDTO.getMessageText())) {
	                        TextDto text = mapper.readValue(whatsAppFlattenMessageDTO.getMessageText(), TextDto.class);
	                        whatsAppChatHistory.setMessageString(text.getBody());
	                    } else {
	                        throw new Exception("Text message type but no message text found. Contact Admin.");
	                    }
	                    break;

	                case "audio":
	                case "document":
	                case "image":
	                case "sticker":
	                case "video":
	                	//System.out.println("Started processing");
	                    if (isNotEmpty.test(whatsAppFlattenMessageDTO.getMessageMedia())) {
	                        //System.out.println("Media message detected, processing media");
	                        MediaDto media = mapper.readValue(whatsAppFlattenMessageDTO.getMessageMedia(), MediaDto.class);

	                        whatsAppChatHistory.setFileName(media.getFileName());
	                        whatsAppChatHistory.setBlobType(messageType);
	                        whatsAppChatHistory.setWhatsAppMediaId(media.getId());

	                        //System.out.println("Fetching media URL from WhatsApp API");
	                        JSONObject jsonObject = okHttpRetrieveMediaUrlClient.sendMessage(
	                                media.getId(),
	                                phoneNumberObject.getWhatsAppProject().getApiVersion(),
	                                phoneNumberObject.getPhoneNumberID(),
	                                phoneNumberObject.getWhatsAppProject().getAccessToken());

	                        if (jsonObject != null) {
	                            long size = Long.parseLong(String.valueOf(jsonObject.get(SEND_MESSAGE_KEYS.file_size.name())));
	                            String mimeType = String.valueOf(jsonObject.get(SEND_MESSAGE_KEYS.mime_type.name()));
	                            String url = String.valueOf(jsonObject.get(SEND_MESSAGE_KEYS.url.name()));

	                            whatsAppChatHistory.setFileSizeInMB(String.valueOf(size / (1024.0 * 1024.0)));
	                            whatsAppChatHistory.setBlobType(mimeType);

	                            try {
	                                mediaUploadDto = callMediaAPIService.findMediaTypeAndVerifyFileSize(mimeType, size);

	                                if (mediaUploadDto != null && mediaUploadDto.isAllowedUpload()) {
	                                    //System.out.println("Media allowed for upload. Downloading media");
	                                    JSONObject mediaDownloadJsonObject = okHttpDownloadMediaClient.sendMessage(
	                                            phoneNumberObject.getWhatsAppProject().getApiVersion(),
	                                            url,
	                                            phoneNumberObject.getWhatsAppProject().getAccessToken());

	                                    MultiPartFileDTO multipartFile = new MultiPartFileDTO();
	                                    multipartFile.setInput(callMediaAPIService.convertBinaryStringToByteArray(mediaDownloadJsonObject.toString()));
	                                    multipartFile.setFileName(media.getFileName());
	                                    multipartFile.setOriginalFilename(media.getFileName());
	                                    multipartFile.setMime_type(mimeType);

	                                    List<MultipartFile> files = Collections.singletonList(multipartFile);

	                                    //System.out.println("Uploading media file to server storage");
	                                    List<com.mylinehub.crm.entity.dto.MediaDto> mediaList = fileUploadService.uploadFiles(
	                                            phoneNumberObject.getOrganization(),
	                                            "",
	                                            FILE_STORE_REQUEST_TYPE.WHATSAPP.name(),
	                                            "",
	                                            files,
	                                            null,
	                                            true,
	                                            whatsAppChatHistory);

	                                } else {
	                                    //System.out.println("Media upload not allowed: " + (mediaUploadDto == null ? "MediaUploadDto is null" : mediaUploadDto.getError()));
	                                    if (mediaUploadDto == null) {
	                                        mediaUploadDto = new MediaUploadDto();
	                                        mediaUploadDto.setError("Media format is not supported.");
	                                    }
	                                }
	                            } catch (Exception e) {
	                                System.err.println("Error during media upload: " + e.getMessage());
	                                e.printStackTrace();
	                                throw e;
	                            }
	                        } else {
	                            //System.out.println("Failed to retrieve media URL from WhatsApp API");
	                            if (mediaUploadDto == null) {
	                                mediaUploadDto = new MediaUploadDto();
	                                mediaUploadDto.setError("Media URL retrieval failed.");
	                            }
	                        }
	                    } else {
	                        throw new Exception("Media message type but no media data found. Contact Admin.");
	                    }
	                    break;

	                case "reaction":
	                	//System.out.println("Started processing");
	                    if (isNotEmpty.test(whatsAppFlattenMessageDTO.getMessageReaction())) {
	                        whatsAppChatHistory.setMessageString(whatsAppFlattenMessageDTO.getMessageReaction());
	                    } else {
	                        throw new Exception("Reaction message type but no reaction found. Contact Admin.");
	                    }
	                    break;

	                case "order":
	                	//System.out.println("Started processing");
	                    if (isNotEmpty.test(whatsAppFlattenMessageDTO.getMessageOrder())) {
	                        whatsAppChatHistory.setMessageString(whatsAppFlattenMessageDTO.getMessageOrder());
	                    } else {
	                        throw new Exception("Order message type but no order data found. Contact Admin.");
	                    }
	                    processChatHistory = false;
	                    break;

	                case "contacts":
	                	//System.out.println("Started processing");
	                    if (isNotEmpty.test(whatsAppFlattenMessageDTO.getMessageContacts())) {
	                        whatsAppChatHistory.setMessageString(whatsAppFlattenMessageDTO.getMessageContacts());
	                    } else {
	                        throw new Exception("Contacts message type but no contacts found. Contact Admin.");
	                    }
	                    processChatHistory = false;
	                    break;

	                case "location":
	                	//System.out.println("Started processing");
	                    if (isNotEmpty.test(whatsAppFlattenMessageDTO.getMessageLocation())) {
	                        whatsAppChatHistory.setMessageString(whatsAppFlattenMessageDTO.getMessageLocation());
	                    }
	                    processChatHistory = false;
	                    break;

	                case "interactive":
	                	//System.out.println("Started processing");
	                    if (isNotEmpty.test(whatsAppFlattenMessageDTO.getMessageInteractive())) {
	                        //System.out.println("Interactive messages currently unsupported, storing raw data");
	                        whatsAppChatHistory.setMessageString(whatsAppFlattenMessageDTO.getMessageInteractive());
	                    }
	                    processChatHistory = false;
	                    break;

	                case "button":
	                	//System.out.println("Started processing");
	                    if (isNotEmpty.test(whatsAppFlattenMessageDTO.getMessageButton())) {
	                        whatsAppChatHistory.setMessageString(whatsAppFlattenMessageDTO.getMessageButton());
	                    } else {
	                        throw new Exception("Button message type but no button data found. Contact Admin.");
	                    }
	                    processChatHistory = false;
	                    break;

	                case "system":
	                	//System.out.println("Started processing");
	                    if (isNotEmpty.test(whatsAppFlattenMessageDTO.getMessageSystem())) {
	                        whatsAppChatHistory.setMessageString(whatsAppFlattenMessageDTO.getMessageSystem());
	                    } else {
	                        throw new Exception("System message type but no system data found. Contact Admin.");
	                    }
	                    processChatHistory = false;
	                    break;

	                default:
	                    //System.out.println("Unsupported message type received: " + messageType);
	                    processChatHistory = false;
	                    // You may want to throw here or just log.
	                    break;
	            }
	            
	            //System.out.println("Processing message boolean: " + processChatHistory);
	            //System.out.println("Let us process data now, if true");
	            
	            if(processChatHistory)
	            processChatHistoryData(currentCustomer,organization,phoneNumberObject,whatsAppChatHistory,whatsAppReportDataParameterDTO);
                
	        }

	        //System.out.println("Processing status updates");
	        boolean hasStatutesStatus = isNotEmpty.test(whatsAppFlattenMessageDTO.getStatusesStatus());
	        boolean hasStatutesConversation = isNotEmpty.test(whatsAppFlattenMessageDTO.getStatusesConversation());
	        boolean hasStatutesPricing = isNotEmpty.test(whatsAppFlattenMessageDTO.getStatusesPricing());
	        boolean hasStatutesPayment = isNotEmpty.test(whatsAppFlattenMessageDTO.getPayment());

	        if (hasStatutesStatus) {

	            //System.out.println("hasStatutesStatus is true");
	            //System.out.println("statusesStatus: " + whatsAppFlattenMessageDTO.getStatusesStatus());

	            String conversationId = null;
	            String error = extractErrorDetails(whatsAppFlattenMessageDTO);
	            
	            if (hasStatutesPayment) {
	                //System.out.println("hasStatutesPayment is true - [Future Use]");
	                //System.out.println("payment: " + whatsAppFlattenMessageDTO.getPayment());

	                // Future implementation: Handle payment statuses (use payment enums)
	            } else {
	                //System.out.println("hasStatutesPayment is false");
	            }
	            
	            if (hasStatutesConversation) {
                    //System.out.println("hasStatutesConversation is true");
                    //System.out.println("statusesConversation: " + whatsAppFlattenMessageDTO.getStatusesConversation());

                    try {
                        //System.out.println("Mapping conversation JSON to DTO...");
                        ConversationDto conversation = mapper.readValue(
                                whatsAppFlattenMessageDTO.getStatusesConversation(),
                                ConversationDto.class
                        );

                        //System.out.println("Conversation mapped successfully. Conversation ID: " + conversation.getId());
                        conversationId = conversation.getId();

                    } catch (Exception e) {
                        System.err.println("Failed to parse statusesConversation JSON. Error: " + e.getMessage());
                        throw new Exception("Cannot convert statusesConversation string to DTO", e);
                    }

                } else {
                    System.err.println("hasStatutesConversation is false - Conversation ID not present.");
                }

                if (hasStatutesPricing) {
                    //System.out.println("hasStatutesPricing is true , Future Use");
                    //System.out.println("statusesPricing: " + whatsAppFlattenMessageDTO.getStatusesPricing());

                    // Not used currently, kept for future logic
                } else {
                    //System.out.println("hasStatutesPricing is false - No pricing info in status");
                }
                
                verifyIfInMemoryDataAndThenUpdateStatus(
                		organization,
                		currentCustomer,
                		error,
                        conversationId,
                        phoneNumberObject,
                        whatsAppFlattenMessageDTO.getStatusesId(),
                        whatsAppFlattenMessageDTO.getStatusesStatus(),
                        whatsAppFlattenMessageDTO.getMessageFrom()
                );

                //System.out.println("Status updated successfully : "+whatsAppFlattenMessageDTO.getStatusesStatus()+" ,having conversation Id : "+conversationId+" ,and display phone : "+whatsAppFlattenMessageDTO.getWhatsAppDisplayPhoneNumber()+" , and error :"+error);

	        } else {
	            //System.out.println("hasStatutesStatus is false - Skipping status block");
	        }

    	}
    	catch(Exception e)
    	{
    		whatsAppChatHistory = null;
    		e.printStackTrace();
    		throw e;
    	}
    	return whatsAppChatHistory;	
    }
	
	
    //Incoming new message
	//1.Adds chat history to memory
	//2.Adds receive message stats
	//3.Checks for auto response , if enabled
	public boolean processChatHistoryData(Customers currentCustomer,Organization organization, WhatsAppPhoneNumber phoneNumberObject,WhatsAppChatHistory whatsAppChatHistory, WhatsAppReportDataParameterDTO whatsAppReportDataParameterDTO) {
		
		boolean toReturn = true;
		
		try {
			
			 //System.out.println("Out of switch which creates chat history object");
             //System.out.println("Send to WebSocket and update chat history");
             //System.out.println("Send to phone number : /event/" + phoneNumberObject.getPhoneNumber());
             // Send to WebSocket and update chat history
             this.simpMessagingTemplate.convertAndSend("/event/" + phoneNumberObject.getPhoneNumber(), whatsAppChatHistory);

             //System.out.println("Updating chat history and report objects for system message");
             WhatsAppChatDataParameterDTO whatsAppChatDataParameterDTO = new WhatsAppChatDataParameterDTO();
             whatsAppChatDataParameterDTO.setWhatsAppMessageId(whatsAppChatHistory.getWhatsAppMessageId());
             whatsAppChatDataParameterDTO.setDetails(whatsAppChatHistory);
             whatsAppChatDataParameterDTO.setAction("update");
             
             //System.out.println("soft append incoming chat history message");
             whatsAppChatHistoryService.softAppendChatHistoryByPhoneNumberMainAndphoneNumberWithAndOrganization(whatsAppChatHistory);
             
             //System.out.println("Report incoming chat history record");
             WhatsAppReportingData.workWithWhatsAppReportMapData(whatsAppReportDataParameterDTO);
             
             if (organization.isAllowWhatsAppAutoAIMessage() &&
                 phoneNumberObject.isAutoAiMessageAllowed()) {
            	 whatsAppAIService.aiAutoReplyPrecheck(currentCustomer,phoneNumberObject, organization, whatsAppChatHistory);
             }
             else {
                 //System.out.println("AI response is not enabled");
             }
		}
		catch(Exception e) {
			e.printStackTrace();
			toReturn = false;
		}
		
		return toReturn;
	}
    

    //Updates stats in memory
    public boolean verifyIfInMemoryDataAndThenUpdateStatus(
    		Organization organization,
    		Customers currentCustomer,
            String error,
            String conversationId,
            WhatsAppPhoneNumber phoneNumberObject,
            String messageId,
            String status,
            String phoneNumberWith) {
        
        boolean toReturn = false;
        //System.out.println("[verifyIfInMemoryDataAndThenUpdateStatus] Start verifying and updating status for messageId: " + messageId);

        try {
            WhatsAppChatDataParameterDTO whatsAppChatDataParameterDTO = new WhatsAppChatDataParameterDTO();

            // Attempt to get backup conversation from memory
            whatsAppChatDataParameterDTO.setAction("get-one-backup");
            whatsAppChatDataParameterDTO.setWhatsAppMessageId(messageId);
            //System.out.println("[verify] Fetching backup chat history from memory");
            Map<String, WhatsAppChatHistory> chatHistoryBackupMap = WhatsAppCurrentConversation.workOnCurrentMemeoryConversations(whatsAppChatDataParameterDTO);

            // Attempt to get main conversation from memory
            whatsAppChatDataParameterDTO.setAction("get");
            //System.out.println("[verify] Fetching main chat history from memory");
            Map<String, WhatsAppChatHistory> chatHistoryMap = WhatsAppCurrentConversation.workOnCurrentMemeoryConversations(whatsAppChatDataParameterDTO);

            if (chatHistoryBackupMap != null) {
                //System.out.println("[verify] chatHistoryBackupMap is not null");
                WhatsAppChatHistory chatHistory = chatHistoryBackupMap.get(messageId);
                if (chatHistory != null) {
                    //System.out.println("[verify] Found backup chat history. Updating status now.");
                    updateFrontEndAndDatabaseReportAsPerWhatsAppMessagestatus(organization,currentCustomer,error, conversationId, phoneNumberObject, messageId, status, phoneNumberWith);
                } else {
                    //System.out.println("[verify] Backup chat history missing for messageId: " + messageId + ". This is unexpected.");
                }
            } else {
                //System.out.println("[verify] chatHistoryBackupMap is null");

                if (chatHistoryMap != null) {
                    //System.out.println("[verify] chatHistoryMap is not null");
                    WhatsAppChatHistory chatHistory = chatHistoryMap.get(messageId);

                    if (chatHistory != null) {
                        //System.out.println("[verify] Found main chat history. Updating status now.");
                        updateFrontEndAndDatabaseReportAsPerWhatsAppMessagestatus(organization,currentCustomer,error, conversationId, phoneNumberObject, messageId, status, phoneNumberWith);
                    } else {
                        //System.out.println("[verify] Main chat history missing for messageId: " + messageId + ". This is unexpected.");
                    }
                } else {
                    //System.out.println("[verify] chatHistoryMap is null, attempting to fetch from database");

                    WhatsAppChatHistory chatHistory = whatsAppChatHistoryRepository.findOneByWhatsAppMessageId(messageId);

                    if (chatHistory != null) {
                        //System.out.println("[verify] Found chat history in database. Updating status now.");
                        updateFrontEndAndDatabaseReportAsPerWhatsAppMessagestatus(organization,currentCustomer,error, conversationId, phoneNumberObject, messageId, status, phoneNumberWith);
                    } else {
                        //System.out.println("[verify] Chat history not found in database for messageId: " + messageId);
                        //System.out.println("[verify] Cannot update status for conversationId: " + conversationId + " with status: " + status);
                    }
                }
            }

            toReturn = true;
            //System.out.println("[verifyIfInMemoryDataAndThenUpdateStatus] Status update process completed successfully");
        } catch (Exception e) {
            System.err.println("[verifyIfInMemoryDataAndThenUpdateStatus] Exception occurred: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return toReturn;
    }

	//Extracts error details from status error
	public static String extractErrorDetails(WhatsAppFlattenMessageDTO dto) {
        String statusesErrors = dto.getStatusesErrors();

        if (statusesErrors == null || statusesErrors.trim().isEmpty()) {
            //System.out.println("statusesErrors is null or empty.");
            return null;
        }

        String result = statusesErrors;

        try {
            //System.out.println("Attempting to parse statusesErrors as JSON array.");

            JSONArray errorArray = new JSONArray(statusesErrors);

            if (errorArray.length() > 0) {
                JSONObject firstError = errorArray.getJSONObject(0);
                //System.out.println("First error object: " + firstError.toString());

                if (firstError.has("error_data")) {
                    JSONObject errorData = firstError.getJSONObject("error_data");
                    //System.out.println("Found error_data: " + errorData.toString());

                    if (errorData.has("details")) {
                        result = errorData.getString("details");
                        //System.out.println("Extracted details: " + result);
                    } else {
                        //System.out.println("Key 'details' not found in error_data.");
                    }
                } else {
                    //System.out.println("Key 'error_data' not found in first error object.");
                }
            } else {
                //System.out.println("statusesErrors JSON array is empty.");
            }
        } catch (Exception e) {
            //System.out.println("Failed to parse statusesErrors as JSON array. Using original string.");
            //System.out.println("Error: " + e.getMessage());
        }

        return result;
    }
	
	//New WhatsAppChatHistory created here. It type is status and hence only be used for front end. It not stored orelse
	//It is not called for new message, only for status
    public boolean updateFrontEndAndDatabaseReportAsPerWhatsAppMessagestatus(
    		Organization organization,
    		Customers currentCustomer,
            String error,
            String conversationId,
            WhatsAppPhoneNumber phoneNumberObject,
            String messageId,
            String status,
            String phoneNumberWith) {

        boolean toReturn = false;
        System.out.println("[updateStatus] Starting update process for messageId: " + messageId + ", status: " + status);
        
        try {
            // Send status update to front-end via WebSocket (STOMP)
            //System.out.println("[updateStatus] Preparing WhatsAppChatHistory object for STOMP message");
            WhatsAppChatHistory whatsAppChatHistory = new WhatsAppChatHistory();
            whatsAppChatHistory.setCreatedOn(new Date());
            whatsAppChatHistory.setLastUpdateTime(new Date());
            whatsAppChatHistory.setMessageType(MESSAGE_TYPE.status.name());
            whatsAppChatHistory.setWhatsAppMessageId(messageId);
            whatsAppChatHistory.setMessageString(status);
            whatsAppChatHistory.setPhoneNumberMain(phoneNumberObject.getPhoneNumber());
            whatsAppChatHistory.setPhoneNumberWith(phoneNumberWith);
            whatsAppChatHistory.setConversationId(conversationId);
            
            
            System.out.println("[updateStatus] Sending message status update to front-end on topic: /event/" + phoneNumberObject.getPhoneNumber());
            this.simpMessagingTemplate.convertAndSend("/event/" + phoneNumberObject.getPhoneNumber(), whatsAppChatHistory);
            
            Long campaignID = StartedCampaignData.getCampaignIdByWaMsgId(messageId);
        	Campaign campaign = campaignService.resolveCampaignMemoryThenDb(campaignID, organization.getOrganization());
        	
            // Prepare report DTO for updating reporting data
            //System.out.println("[updateStatus] Preparing WhatsAppReportDataParameterDTO for reporting update");
            WhatsAppReportDataParameterDTO whatsAppReportDataParameterDTO = new WhatsAppReportDataParameterDTO();
            
            
            if(campaign != null) {
                whatsAppReportDataParameterDTO.setCampaignMessage(true);
            }
            else {
            	whatsAppReportDataParameterDTO.setAiMessage(true);
//            	whatsAppReportDataParameterDTO.setManualMessage(true);	
            }
            
            whatsAppReportDataParameterDTO.setWhatsAppNumberReportRepository(applicationCotext.getBean(WhatsAppNumberReportRepository.class));
            whatsAppReportDataParameterDTO.setWhatsAppNumberReportService(applicationCotext.getBean(WhatsAppNumberReportService.class));
            whatsAppReportDataParameterDTO.setApplicationContext(applicationCotext);
            whatsAppReportDataParameterDTO.setInputDTO(whatsAppChatHistory);
            whatsAppReportDataParameterDTO.setOrganization(phoneNumberObject.getOrganization());
            whatsAppReportDataParameterDTO.setPhoneNumberMain(phoneNumberObject.getPhoneNumber());
            whatsAppReportDataParameterDTO.setPhoneNumberWith(phoneNumberWith);
            whatsAppReportDataParameterDTO.setPhoneNumber(phoneNumberObject);

            // Prepare DTO for updating chat status in memory/database
            WhatsAppChatDataParameterDTO whatsAppChatDataParameterDTO = new WhatsAppChatDataParameterDTO();
            whatsAppChatDataParameterDTO.setWhatsAppMessageId(messageId);
            
            // Update conversation only once for certain statuses
            if (conversationId != null) {
                //System.out.println("[updateStatus] Updating conversationId for messageId: " + messageId);
                whatsAppChatDataParameterDTO.setConversationId(conversationId);
                whatsAppChatDataParameterDTO.setAction("update-conversationId");
                WhatsAppCurrentConversation.workOnCurrentMemeoryConversations(whatsAppChatDataParameterDTO);
            }

            // Update error info if present
            if (error != null) {
                //System.out.println("[updateStatus] Updating error info for messageId: " + messageId + ". Error: " + error);
                whatsAppChatDataParameterDTO.setError(error);
                whatsAppChatDataParameterDTO.setAction("update-whatsAppMessageError");
                WhatsAppCurrentConversation.workOnCurrentMemeoryConversations(whatsAppChatDataParameterDTO);
            }

            System.out.println("[updateStatus] Processing status: " + status);
            
            switch (status) {
                case "sent":
                    // Currently no action for sent status
                	whatsAppChatDataParameterDTO.setAction("update-sent");
                    whatsAppReportDataParameterDTO.setAction("update-sent-stats");
                    //System.out.println("[updateStatus] Status 'sent' received");
                    break;

                case "read":
                    whatsAppChatDataParameterDTO.setAction("update-read");
                    whatsAppReportDataParameterDTO.setAction("update-read-stats");
                    //System.out.println("[updateStatus] Status 'read' received");
                    break;

                case "failed":
                    whatsAppChatDataParameterDTO.setAction("update-failed");
                    whatsAppReportDataParameterDTO.setAction("update-failed-stats");
                    //System.out.println("[updateStatus] Status 'failed' received");
                    break;

                case "delivered":
                    whatsAppChatDataParameterDTO.setAction("update-delivered");
                    whatsAppReportDataParameterDTO.setAction("update-delivered-stats");

                    if(campaign != null) {
                    	whatsAppReportDataParameterDTO.setAmount((long)campaign.getCallCost());
                    }
                    else {
                    	whatsAppReportDataParameterDTO.setAmount(null);
                    }
                    
                    //Need to update customer first updated record if its not set yet
                    if(!currentCustomer.isFirstWhatsAppMessageIsSend()) {
                    	//System.out.println("[updateStatus] Delivered, First message to customer. Upadting memory data now...");
                    	WhatsAppCustomerParameterDataDto whatsAppCustomerParameterDataDto = new WhatsAppCustomerParameterDataDto();
			            whatsAppCustomerParameterDataDto.setAction("update-first-message-sent");
			            whatsAppCustomerParameterDataDto.setPhoneNumber(currentCustomer.getPhoneNumber());
			            whatsAppCustomerParameterDataDto.setOrganization(organization);
			            WhatsAppCustomerData.workWithWhatsAppCustomerData(whatsAppCustomerParameterDataDto);
                    }
                    
                    //System.out.println("[updateStatus] Status 'delivered' received");
                    break;

                case "deleted":
                    whatsAppChatDataParameterDTO.setAction("update-deleted");
                    whatsAppReportDataParameterDTO.setAction("update-deleted-stats");
                    //System.out.println("[updateStatus] Status 'deleted' received");
                    break;

                default:
                    //System.out.println("[updateStatus] Unknown status received: " + status);
                    break;
            }

            
            //Map is whats app message Id
            System.out.println("[updateStatus] Updating memory conversations with action: " + whatsAppChatDataParameterDTO.getAction());
            WhatsAppCurrentConversation.workOnCurrentMemeoryConversations(whatsAppChatDataParameterDTO);

            //This is not based on conversation ID. Map key is phonemain+phonewith
            System.out.println("[updateStatus] Updating reporting data with action: " + whatsAppReportDataParameterDTO.getAction());
            WhatsAppReportingData.workWithWhatsAppReportMapData(whatsAppReportDataParameterDTO);
            sendWhatsAppStatusToRunningCampaign(phoneNumberObject,messageId,status,phoneNumberWith,organization);
            
            toReturn = true;
            //System.out.println("[updateStatus] Update process completed successfully for messageId: " + messageId);

        } catch (Exception e) {
            System.err.println("[updateStatus] Exception occurred while updating message status for messageId: " + messageId);
            e.printStackTrace();
            throw e;
        }

        return toReturn;
    }

    
    private void sendWhatsAppStatusToRunningCampaign(WhatsAppPhoneNumber phoneNumberObject,
            String messageId,
            String status,
            String phoneNumberWith,
            Organization organization) {

    	Long campaignID = StartedCampaignData.getCampaignIdByWaMsgId(messageId);
    	Campaign campaign = campaignService.resolveCampaignMemoryThenDb(campaignID, organization.getOrganization());
    	
		String statusCapital = "UNACCEPTED";
		Long messageCost = null;
		if (campaignID == null) {
			System.out.println("[WA-STATUS] No mapping found for wamid=" + messageId + " status=" + status);
			return; // leave it if not available (your requirement)
		}
		
		switch(status) {
            case "sent":
            	statusCapital = "SENT";
                break;
            case "read":
            	statusCapital = "READ";
            	StartedCampaignData.removeWaMsgMapping(messageId);
                break;
            case "failed":
            	statusCapital = "FAILED";
            	StartedCampaignData.removeWaMsgMapping(messageId);
                break;
            case "delivered":
            	statusCapital = "DELIVERED";
            	
            	if(campaign != null) {
            		messageCost = (long) campaign.getCallCost();
            	}
            	else {
            		if(phoneNumberObject != null) {
            			messageCost = phoneNumberObject.getCostPerOutboundMessage();
            		}
            	}
            	
                break;
            case "deleted":
            	statusCapital = "DELETED";
            	StartedCampaignData.removeWaMsgMapping(messageId);
                break;
            default:
            	break;
		}
                
		// IMPORTANT: update using campaignId (not resolveCampaignForCustomerByPhoneNumber)
		autodialerReinitiateAndFunctionService.recordCampaignRunWhatsAppMessageState(
			messageId,
			campaignID,
			null,
			statusCapital,
			phoneNumberObject.getPhoneNumber(),
			phoneNumberWith,
			messageCost
		);
	}
    
    //Converts whats app incoming message into flatten format which then after converts into WhatsAppChatHistory Object
    //Called for all input message
    public List<WhatsAppFlattenMessageDTO> extractFlattenedMessages(EntryDto entryDto, ComponentDto whatsAppInput) throws Exception {
        List<WhatsAppFlattenMessageDTO> toReturnList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            //System.out.println("********* Starting extractFlattenedMessages *********");

            String whatsAppBusinessID = entryDto.getId();
            List<ChangeDto> changeList = entryDto.getChanges();

            if (changeList != null && !changeList.isEmpty()) {
                //System.out.println("changeList size: " + changeList.size());

                for (ChangeDto changeDto : changeList) {
                    //System.out.println("Processing ChangeDto with field: " + changeDto.getField());

                    WhatsAppFlattenMessageDTO toReturn = new WhatsAppFlattenMessageDTO();
                    toReturn.setCreatedOn(Instant.now());
                    toReturn.setWhatsAppBusinessId(whatsAppBusinessID);

                    if (changeDto.getField() != null) {
                        //System.out.println("Setting field: " + changeDto.getField());
                        toReturn.setField(changeDto.getField());
                    }

                    ValueDto valueDto = changeDto.getValue();
                    if (valueDto != null) {

                    	if (valueDto.getMessaging_product() != null) {
                    	    //System.out.println("Setting messagingProduct: " + valueDto.getMessaging_product());
                    	    toReturn.setMessagingProduct(valueDto.getMessaging_product());
                    	}

                    	if (valueDto.getMetadata() != null) {
                    	    String metadataStr = mapper.writeValueAsString(valueDto.getMetadata());
                    	    //System.out.println("Setting metaData: " + metadataStr);
                    	    toReturn.setMetaData(metadataStr);

                    	    JSONObject metaDataJsonObject = new JSONObject(metadataStr);

                    	    if (metaDataJsonObject.has(SEND_MESSAGE_KEYS.display_phone_number.name())) {
                    	        String displayPhone = metaDataJsonObject.getString(SEND_MESSAGE_KEYS.display_phone_number.name());
                    	        if (displayPhone != null && !displayPhone.startsWith("+")) {
                    	            displayPhone = "+" + displayPhone;
                    	        }
                    	        //System.out.println("Setting whatsAppDisplayPhoneNumber: " + displayPhone);
                    	        toReturn.setWhatsAppDisplayPhoneNumber(displayPhone);
                    	    }

                    	    if (metaDataJsonObject.has(SEND_MESSAGE_KEYS.phone_number_id.name())) {
                    	        String phoneNumberId = metaDataJsonObject.getString(SEND_MESSAGE_KEYS.phone_number_id.name());
                    	        //System.out.println("Setting whatsAppPhoneNumberId: " + phoneNumberId);
                    	        toReturn.setWhatsAppPhoneNumberId(phoneNumberId);
                    	    }
                    	}

                        List<ContactDto> contactList = valueDto.getContacts();
                        List<MessagesDto> messagesList = valueDto.getMessages();
                        List<StatusesDto> statusesList = valueDto.getStatuses();
                        List<ErrorDto> errorList = valueDto.getErrors();

                        if (contactList != null && !contactList.isEmpty()) {
                            //System.out.println("contactList size: " + contactList.size());

                            if (contactList.size() > 1) {
                                //System.out.println("Multiple contacts found:");
                                //System.out.println("Names: " + toReturn.getName());
                                //System.out.println("WhatsApp WA IDs: " + toReturn.getWhatsApp_wa_id());
                                //System.out.println("Timestamp: " + new Date());
                                //System.out.println("Full WhatsApp input JSON:");
                                //System.out.println(mapper.writeValueAsString(whatsAppInput));
                            } else {
                                ContactDto contactDto = contactList.get(0);

                                if (contactDto.getWa_id() != null) {
                                    //System.out.println("Setting WhatsApp WA ID: " + contactDto.getWa_id());
                                    toReturn.setWhatsApp_wa_id(contactDto.getWa_id());
                                }

                                if (contactDto.getProfile() != null && contactDto.getProfile().getName() != null) {
                                    //System.out.println("Setting contact name: " + contactDto.getProfile().getName());
                                    toReturn.setName(contactDto.getProfile().getName());
                                }
                            }
                        }

                        
                        if (errorList != null && !errorList.isEmpty()) {
                            //System.out.println("errorList size: " + errorList.size());

                            if (errorList.size() > 1) {
                                //System.out.println("Multiple errors found:");
                                //System.out.println("Error Codes: " + toReturn.getErrorCode());
                                //System.out.println("Error Titles: " + toReturn.getErrorTitle());
                                //System.out.println("Timestamp: " + new Date());
                                //System.out.println("Full WhatsApp input JSON:");
                                //System.out.println(mapper.writeValueAsString(whatsAppInput));
                            } else {
                                ErrorDto errorDto = errorList.get(0);
                                if (errorDto != null) {
                                    if (errorDto.getCode() != null) {
                                        toReturn.setErrorCode(errorDto.getCode());
                                    }
                                    if (errorDto.getTitle() != null) {
                                        toReturn.setErrorTitle(errorDto.getTitle());
                                    }
                                }
                            }
                        }


                        //System.out.println("Creating a copy of WhatsAppFlattenMessageDTO for modification");
                        WhatsAppFlattenMessageDTO toReturnCopy = whatsAppFlattenMessageMapper.cloneWhatsAppFlattenMessage(toReturn);

                        // If both messagesList and statusesList are null, treat as error only message
                        if (messagesList == null && statusesList == null) {
                            //System.out.println("No messages or statuses present - adding error-only message to list");
                            toReturnList.add(toReturn);
                        }

                        if (messagesList != null) {
                            //System.out.println("Processing messagesList with size: " + messagesList.size());
                            for (MessagesDto messagesDto : messagesList) {
                                toReturn = whatsAppFlattenMessageMapper.cloneWhatsAppFlattenMessage(toReturnCopy);

                                if (messagesDto.getFrom() != null) {
                                    //System.out.println("Setting messageFrom: " + messagesDto.getFrom());
                                    String messageFrom = messagesDto.getFrom();
                		
                                    if (messageFrom != null && !messageFrom.startsWith("+")) {
                                    	messageFrom = "+" + messageFrom;
                                    }
                                    toReturn.setMessageFrom(messageFrom);
                                }
                                else {
                                	//System.out.println("Setting getFrom is null");
                                }
                                
                                if (messagesDto.getId() != null) {
                                    //System.out.println("Setting messageId: " + messagesDto.getId());
                                    toReturn.setMessageId(messagesDto.getId());
                                }
                                else {
                                	//System.out.println("Setting getId is null");
                                }
                                
                                if (messagesDto.getTimestamp() != null) {
                                    //System.out.println("Setting messageTimestamp: " + messagesDto.getTimestamp());
                                    toReturn.setMessageTimestamp(messagesDto.getTimestamp());
                                }
                                else {
                                	//System.out.println("Setting getTimestamp is null");
                                }
                                
                                if (messagesDto.getType() != null) {
                                    //System.out.println("Setting messageType: " + messagesDto.getType());
                                    toReturn.setMessageType(messagesDto.getType());
                                }
                                else {
                                	//System.out.println("Setting messageType is null");
                                }

                                ContextDto context = messagesDto.getContext();
                                IdentityDto identity = messagesDto.getIdentity();
                                MediaDto media = null;
                                InteractiveDto interactive = messagesDto.getInteractive();
                                TextDto text = messagesDto.getText();
                                SystemMessageDto system = messagesDto.getSystem();
                                ButtonDto button = messagesDto.getButton();
                                ReferralDto referral = messagesDto.getReferral();
                                ReactionDto reaction = messagesDto.getReaction();
                                List<ErrorDto> errors = messagesDto.getErrors();
                                OrderDto orders = messagesDto.getOrder();
                                LocationDto locations = messagesDto.getLocation();
                                com.mylinehub.crm.whatsapp.dto.general.contact.ContactDto contacts = messagesDto.getContacts();

                                if ("image".equals(messagesDto.getType()))
                                    media = messagesDto.getImage();
                                else if ("video".equals(messagesDto.getType()))
                                    media = messagesDto.getVideo();
                                else if ("document".equals(messagesDto.getType()))
                                    media = messagesDto.getDocument();
                                else if ("sticker".equals(messagesDto.getType()))
                                    media = messagesDto.getSticker();
                                else if ("audio".equals(messagesDto.getType()))
                                    media = messagesDto.getAudio();

                                if (context != null)
                                    toReturn.setMessageContext(mapper.writeValueAsString(context));

                                if (identity != null)
                                    toReturn.setMessageIdentity(mapper.writeValueAsString(identity));

                                if (media != null)
                                    toReturn.setMessageMedia(mapper.writeValueAsString(media));

                                if (interactive != null)
                                    toReturn.setMessageInteractive(mapper.writeValueAsString(interactive));

                                if (text != null)
                                    toReturn.setMessageText(mapper.writeValueAsString(text));

                                if (system != null)
                                    toReturn.setMessageSystem(mapper.writeValueAsString(system));

                                if (button != null)
                                    toReturn.setMessageButton(mapper.writeValueAsString(button));

                                if (referral != null)
                                    toReturn.setMessageReferral(mapper.writeValueAsString(referral));

                                if (reaction != null)
                                    toReturn.setMessageReaction(mapper.writeValueAsString(reaction));

                                if (errors != null)
                                    toReturn.setMessageErrors(mapper.writeValueAsString(errors));

                                if (orders != null)
                                    toReturn.setMessageOrder(mapper.writeValueAsString(orders));

                                if (locations != null)
                                    toReturn.setMessageLocation(mapper.writeValueAsString(locations));

                                if (contacts != null)
                                    toReturn.setMessageContacts(mapper.writeValueAsString(contacts));

                                toReturnList.add(toReturn);
                            }
                        }

                        if (statusesList != null) {
                            //System.out.println("Processing statusesList with size: " + statusesList.size());
                            for (StatusesDto statusesDto : statusesList) {
                                toReturn = whatsAppFlattenMessageMapper.cloneWhatsAppFlattenMessage(toReturnCopy);

                                toReturn.setMessageType(MESSAGE_TYPE.status.name());
                                
                                if (statusesDto.getId() != null) {
                                    //System.out.println("Setting statusesId: " + statusesDto.getId());
                                    toReturn.setStatusesId(statusesDto.getId());
                                    toReturn.setMessageId(statusesDto.getId());
                                }

                                if (statusesDto.getRecipient_id() != null) {
                                    //System.out.println("Setting statusesRecipientId: " + statusesDto.getRecipient_id());
                                
                                    String messageFrom = statusesDto.getRecipient_id();
                            		
                                    if (messageFrom != null && !messageFrom.startsWith("+")) {
                                    	messageFrom = "+" + messageFrom;
                                    }
                                    toReturn.setMessageFrom(messageFrom);
                                    toReturn.setStatusesRecipientId(messageFrom);
                                }

                                if (statusesDto.getStatus() != null) {
                                    //System.out.println("Setting statusesStatus: " + statusesDto.getStatus());
                                    toReturn.setStatusesStatus(statusesDto.getStatus());
                                }

                                if (statusesDto.getTimestamp() != null) {
                                    //System.out.println("Setting statusesTimestamp: " + statusesDto.getTimestamp());
                                    toReturn.setStatusesTimestamp(statusesDto.getTimestamp());
                                    toReturn.setMessageTimestamp(statusesDto.getTimestamp());
                                }

                                if (statusesDto.getType() != null) {
                                    //System.out.println("Setting statusesType: " + statusesDto.getType());
                                    toReturn.setStatusesType(statusesDto.getType());
                                }


                                ConversationDto conversation = statusesDto.getConversation();
                                PricingDto pricing = statusesDto.getPricing();
                                PaymentDto payment = statusesDto.getPayment();
                                List<ErrorDto> errors = statusesDto.getErrors();

                                if (conversation != null)
                                    toReturn.setStatusesConversation(mapper.writeValueAsString(conversation));

                                if (pricing != null)
                                    toReturn.setStatusesPricing(mapper.writeValueAsString(pricing));

                                if (payment != null)
                                    toReturn.setPayment(mapper.writeValueAsString(payment));

                                if (errors != null)
                                    toReturn.setStatusesErrors(mapper.writeValueAsString(errors));

                                //System.out.println("Added status entry:");
                                //System.out.println(mapper.writeValueAsString(toReturn));

                                toReturnList.add(toReturn);
                            }
                        }
                    } else {
                        //System.out.println("ValueDto is null in changeDto, skipping this change.");
                    }
                }
            } else {
                //System.out.println("No changes found in entryDto.");
            }

            //System.out.println("********* Completed extractFlattenedMessages *********");
        } catch (Exception e) {
            System.err.println("Exception while flattening WhatsApp input message:");
            e.printStackTrace();
            throw e;
        }

        return toReturnList;
    }

}
