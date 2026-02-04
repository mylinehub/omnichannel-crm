package com.mylinehub.crm.whatsapp.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.FileCategory;
import com.mylinehub.crm.entity.Media;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.repository.MediaRepository;
import com.mylinehub.crm.service.CurrentTimeInterface;
import com.mylinehub.crm.service.MediaService;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendAudioMessageByIdClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendContactsMessageClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendDocumentMessageByIdClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendImageMessageByIdClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendInteractiveMessageClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendLocationMessageClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendReactionMessageClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendStickerMessageByIdClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendTemplateMessageClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendTextMessageClient;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendVideoMessageByIdClient;
import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.dto.service.MediaUploadDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;
import com.mylinehub.crm.whatsapp.enums.MESSAGE_TYPE;
import com.mylinehub.crm.whatsapp.enums.MESSAGING_PRODUCT;
import com.mylinehub.crm.whatsapp.enums.SEND_MESSAGE_KEYS;
import com.mylinehub.crm.whatsapp.enums.chat.CHAT_ORIGIN;
import com.mylinehub.crm.whatsapp.requests.WhatsAppTemplateVariableRequest;

import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class WhatsAppIntegrationOutboundService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */

    private final OkHttpSendTextMessageClient okHttpSendTextMessageClient;
    private final OkHttpSendReactionMessageClient okHttpSendReactionMessageClient;
    private final OkHttpSendContactsMessageClient okHttpSendContactsMessageClient;
    private final OkHttpSendLocationMessageClient okHttpSendLocationMessageClient;
    private final OkHttpSendInteractiveMessageClient okHttpSendInteractiveMessageClient;
    
    private final MediaService mediaService;
    private final MediaRepository mediaRepository;
    private final CallMediaAPIService callMediaAPIService;
    private final WhatsAppChatHistoryService whatsAppChatHistoryService;
    private final OkHttpSendAudioMessageByIdClient okHttpSendAudioMessageByIdClient;
    private final OkHttpSendDocumentMessageByIdClient okHttpSendDocumentMessageByIdClient;
    private final OkHttpSendImageMessageByIdClient okHttpSendImageMessageByIdClient;
    private final OkHttpSendStickerMessageByIdClient okHttpSendStickerMessageByIdClient;
    private final OkHttpSendVideoMessageByIdClient okHttpSendVideoMessageByIdClient;
    private final OkHttpSendTemplateMessageClient okHttpSendTemplateMessageClient;
	private final SimpMessagingTemplate simpMessagingTemplate;
	
	
    public void sendOutboundWhatsAppChatHistory(WhatsAppChatHistory inputDTO,String chatOrigin) 
    {
    	try {
    		
    		if(chatOrigin == null) {
    			inputDTO.setMessageOrigin(CHAT_ORIGIN.extension.name());
    		}
    		else {
    			inputDTO.setMessageOrigin(chatOrigin);
    		}
    		
    		System.out.println("sendOutboundWhatsAppChatHistory");
        	//System.out.println(inputDTO.toString());
        	
        	//System.out.println("Setting date for chat History outgoing whats app");
        	inputDTO.setCreatedOn(new Date());
        	
        	inputDTO.setLastUpdateTime(new Date());
        	
        	//System.out.println("First Calling Whats App Conversion and Sending to End User");
        	JSONObject jsonObject = digestOutboundMessageAndSendToWhatsApp(inputDTO);
        	   	
        	//Message Response
//        	{
//        	    "messaging_product": "whatsapp",
//        	    "contacts": [
//        	        {
//        	            "input": "MY_PHONE_NUMBER",
//        	            "wa_id": "MY_PHONE_NUMBER"
//        	        }
//        	    ],
//        	    "messages": [
//        	        {
//        	            "id": "wamid.HBgMNTU2NTkyNjY1MDYwFQI=="
//        	        }
//        	    ]
//        	}
        	
        	//System.out.println("Response after whats app upload : "+ jsonObject);
			JSONArray array = (JSONArray) jsonObject.get(SEND_MESSAGE_KEYS.messages.name());
			
			//System.out.println("Array : "+ array.toString());
			
			if(array.length() > 0)
			{
				//System.out.println("Array length greator than 0");
				JSONObject object = array.getJSONObject(0);
				//System.out.println("Adding Whats App Message ID : "+object.get(SEND_MESSAGE_KEYS.id.name()).toString());
				inputDTO.setWhatsAppMessageId(object.get(SEND_MESSAGE_KEYS.id.name()).toString());
			}	
        	
			
        	//System.out.println("After then sending to other clients registered to this phone number");
        	this.simpMessagingTemplate.convertAndSend("/event/"+inputDTO.getPhoneNumberMain(), inputDTO);
        	
        	//System.out.println("Save to ChatHistory. Do not save to chat history from front end as we need to update whats app message ID associated");
        	boolean result = digestOutboundMessageToChatHistory(inputDTO);
        	//Below make sure everyone know a new message was sent to customer
    	
        	if(!result) {
        		//System.out.println("Send notification. Outbound message had failure");
        		//Send notification. Outbound message had failure
        	}
        	else {
        		System.out.println("sendOutboundWhatsAppChatHistory completed");
        	}
    	}
    	catch(Exception e) {
    		//System.out.println("Exception : sendToWhatsAppPhone");
    		e.printStackTrace();
    	}
    }
    
    public JSONObject digestOutboundMessageAndSendToWhatsApp(WhatsAppChatHistory inputDTO) throws Exception {
        JSONObject toReturn = null;

        try {
            //System.out.println("digestOutboundMessageAndSendToWhatsApp started for phoneNumberMain: " + inputDTO.getPhoneNumberMain());

            Map<String, WhatsAppPhoneNumber> phoneNumberMap = WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(inputDTO.getPhoneNumberMain(), null, "get-one");

            if (phoneNumberMap != null && phoneNumberMap.size() > 0) {
                WhatsAppPhoneNumber phoneNumber = phoneNumberMap.get(inputDTO.getPhoneNumberMain());

                if (phoneNumber != null) {
                    //System.out.println("Convert And Send To WhatsApp for phoneNumberMain: " + inputDTO.getPhoneNumberMain());
                    // Convert And Send To WhatsApp
                    toReturn = sendWhatsAppMessageAsPerType(inputDTO, phoneNumber);

                    //No reporting required here. Sent , Delivered, Read, Failed status are updated via week hook
                } else {
                    //System.out.println("Main Phone number not present: " + inputDTO.getPhoneNumberMain());
                    toReturn = null;
                    throw new Exception(inputDTO.getPhoneNumberMain() + " not found in database as registered WhatsApp user.");
                }
            } else {
                //System.out.println("Main Phone number not present: " + inputDTO.getPhoneNumberMain());
                toReturn = null;
                throw new Exception(inputDTO.getPhoneNumberMain() + " not found in database as registered WhatsApp user.");
            }

        } catch (Exception e) {
            //System.out.println("Exception in digestOutboundMessageAndSendToWhatsApp");
            e.printStackTrace();
            toReturn = null;
            throw e;
        }

        //System.out.println("digestOutboundMessageAndSendToWhatsApp completed successfully");
        return toReturn;
    }

    public boolean digestOutboundMessageToChatHistory(WhatsAppChatHistory inputDTO) throws Exception {
        boolean toReturn = true;
        try {
            System.out.println("digestOutboundMessageToChatHistory started for phoneNumberMain: " + inputDTO.getPhoneNumberMain());

            Map<String, WhatsAppPhoneNumber> phoneNumberMap = WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(inputDTO.getPhoneNumberMain(), null, "get-one");

            if (phoneNumberMap != null && phoneNumberMap.size() > 0) {
                WhatsAppPhoneNumber phoneNumber = phoneNumberMap.get(inputDTO.getPhoneNumberMain());

                if (phoneNumber != null) {
                    //System.out.println("Soft append started for messageId: " + inputDTO.getWhatsAppMessageId());

                    Integer result = whatsAppChatHistoryService.softAppendChatHistoryByPhoneNumberMainAndphoneNumberWithAndOrganization(inputDTO);

                    //System.out.println("Soft append chat history result: " + result);
                    //System.out.println("After adding chat to history and vice versa");
                } else {
                    //System.out.println("Main Phone number not present: " + inputDTO.getPhoneNumberMain());
                    toReturn = false;
                    throw new Exception(inputDTO.getPhoneNumberMain() + " not found in database as registered WhatsApp user.");
                }
            } else {
                //System.out.println("Main Phone number not present: " + inputDTO.getPhoneNumberMain());
                toReturn = false;
                throw new Exception(inputDTO.getPhoneNumberMain() + " not found in database as registered WhatsApp user.");
            }
        } catch (Exception e) {
            //System.out.println("Exception in digestOutboundMessageToChatHistory");
            e.printStackTrace();
            toReturn = false;
            throw e;
        }

        //System.out.println("digestOutboundMessageToChatHistory completed successfully");
        return toReturn;
    }

    public JSONObject sendWhatsAppMessageAsPerType(WhatsAppChatHistory inputDTO, WhatsAppPhoneNumber phoneNumber) throws Exception {
        JSONObject toReturn = null;

        try {
            System.out.println("sendWhatsAppMessageAsPerType started for messageType: " + inputDTO.getMessageType());

            if (inputDTO.getMessageType().equals(MESSAGE_TYPE.text.name()) || inputDTO.getMessageType().equals(MESSAGE_TYPE.message.name())) {
                toReturn = okHttpSendTextMessageClient.sendMessage(
                    MESSAGING_PRODUCT.whatsapp.name(),
                    inputDTO.getPhoneNumberWith(),
                    null,
                    false,
                    inputDTO.getMessageString(),
                    phoneNumber.getWhatsAppProject().getApiVersion(),
                    phoneNumber.getPhoneNumberID(),
                    phoneNumber.getWhatsAppProject().getAccessToken()
                );
            } else if (inputDTO.getMessageType().equals(MESSAGE_TYPE.audio.name()) ||
                       inputDTO.getMessageType().equals(MESSAGE_TYPE.document.name()) ||
                       inputDTO.getMessageType().equals(MESSAGE_TYPE.image.name()) ||
                       inputDTO.getMessageType().equals(MESSAGE_TYPE.sticker.name()) ||
                       inputDTO.getMessageType().equals(MESSAGE_TYPE.video.name())) {
                Media media = mediaService.findByWhatsAppMediaId(inputDTO.getWhatsAppMediaId());

                if (media != null) {
                    if ((media.getWhatsAppUploadDate() == null) || isAtleastTwentyeightDaysAgo(media.getWhatsAppUploadDate())) {
                        //System.out.println("Media is older than 28 days or upload date null, reuploading media with mediaId: " + media.getWhatsAppMediaId());

                        FileCategory fileCategory = media.getFileCategory();

                        try {
                            MediaUploadDto mediaUploadDto = callMediaAPIService.triggerMediaUploadAPI(
                                media.getType(),
                                media.getSize(),
                                media.getOrganization(),
                                fileCategory.getName(),
                                media.getName(),
                                phoneNumber
                            );
                            media.setReceived(false);
                            media.setWhatsAppMediaId(mediaUploadDto.getMediaId());
                            media.setWhatsAppMediaType(mediaUploadDto.getType());
                            media.setWhatsAppLink(mediaUploadDto.getLink());
                            media.setWhatsAppUploadDate(new Date());
                            media.setExternalPartyUploadSuccessful(mediaUploadDto.isExternalPartyUploadSuccessful());
                            media.setError(mediaUploadDto.getError());
                            media = mediaRepository.save(media);
                            //System.out.println("Media re-upload successful with new mediaId: " + media.getWhatsAppMediaId());
                        } catch (Exception e) {
                            //System.out.println("Exception during media reupload");
                            e.printStackTrace();
                            media.setError(e.getMessage());
                        }
                    }

                    toReturn = sendWhatsAppMessageAsPerBlobType(media, inputDTO, phoneNumber);
                } else {
                    //System.out.println("Media not found for WhatsAppMediaId: " + inputDTO.getWhatsAppMediaId());
                    toReturn = null;
                    throw new Exception(inputDTO.getWhatsAppMediaId() + " media not found in storage. Please connect with admin.");
                }
            } else if (inputDTO.getMessageType().equals(MESSAGE_TYPE.url.name())) {
                //System.out.println("Sending URL type message");
                toReturn = okHttpSendTextMessageClient.sendMessage(
                    MESSAGING_PRODUCT.whatsapp.name(),
                    inputDTO.getPhoneNumberWith(),
                    null,
                    true,
                    inputDTO.getMessageString(),
                    phoneNumber.getWhatsAppProject().getApiVersion(),
                    phoneNumber.getPhoneNumberID(),
                    phoneNumber.getWhatsAppProject().getAccessToken()
                );
            } else if (inputDTO.getMessageType().equals(MESSAGE_TYPE.reaction.name())) {
                //System.out.println("Sending reaction type message");
                toReturn = okHttpSendReactionMessageClient.sendMessage(
                    MESSAGING_PRODUCT.whatsapp.name(),
                    inputDTO.getPhoneNumberWith(),
                    null,
                    false,
                    inputDTO.getMessageString(),
                    phoneNumber.getWhatsAppProject().getApiVersion(),
                    phoneNumber.getPhoneNumberID(),
                    phoneNumber.getWhatsAppProject().getAccessToken()
                );
            } else if (inputDTO.getMessageType().equals(MESSAGE_TYPE.contacts.name())) {
                //System.out.println("Sending contacts type message");
                toReturn = okHttpSendContactsMessageClient.sendMessage(
                    MESSAGING_PRODUCT.whatsapp.name(),
                    inputDTO.getPhoneNumberWith(),
                    null,
                    false,
                    inputDTO.getMessageString(),
                    phoneNumber.getWhatsAppProject().getApiVersion(),
                    phoneNumber.getPhoneNumberID(),
                    phoneNumber.getWhatsAppProject().getAccessToken()
                );
            } else if (inputDTO.getMessageType().equals(MESSAGE_TYPE.location.name())) {
                //System.out.println("Sending location type message");
                toReturn = okHttpSendLocationMessageClient.sendMessage(
                    MESSAGING_PRODUCT.whatsapp.name(),
                    inputDTO.getPhoneNumberWith(),
                    null,
                    false,
                    inputDTO.getMessageString(),
                    phoneNumber.getWhatsAppProject().getApiVersion(),
                    phoneNumber.getPhoneNumberID(),
                    phoneNumber.getWhatsAppProject().getAccessToken()
                );
            } else if (inputDTO.getMessageType().equals(MESSAGE_TYPE.interactive.name())) {
                //System.out.println("Sending interactive type message");
                toReturn = okHttpSendInteractiveMessageClient.sendMessage(
                    MESSAGING_PRODUCT.whatsapp.name(),
                    inputDTO.getPhoneNumberWith(),
                    null,
                    false,
                    inputDTO.getMessageString(),
                    phoneNumber.getWhatsAppProject().getApiVersion(),
                    phoneNumber.getPhoneNumberID(),
                    phoneNumber.getWhatsAppProject().getAccessToken()
                );
            } else {
                //System.out.println("Unsupported message type: " + inputDTO.getMessageType());
                throw new Exception(inputDTO.getMessageType() + " not allowed. Please connect with admin.");
            }
        } catch (Exception e) {
            //System.out.println("Exception in sendWhatsAppMessageAsPerType");
            e.printStackTrace();
            throw e;
        }

        //System.out.println("sendWhatsAppMessageAsPerType completed successfully for messageType: " + inputDTO.getMessageType());
        return toReturn;
    }

    
    
    public JSONObject sendWhatsAppMessageAsPerTemplateName(Customers customer,String templateName,String connectedLine) throws Exception {
    	JSONObject jsonObject = null;
    	try {
    		//System.out.println("Send Employee Specific WhatsApp Message As Per Template Name");
    		
    		WhatsAppTemplateVariableRequest whatsAppTemplateVariableRequest = new WhatsAppTemplateVariableRequest();
    		whatsAppTemplateVariableRequest.setCustomer(customer);
    		whatsAppTemplateVariableRequest.setDate(new Date());
    		whatsAppTemplateVariableRequest.setToday_date(new Date());
    		whatsAppTemplateVariableRequest.setName(customer.getFirstname()+" "+customer.getLastname());
    		whatsAppTemplateVariableRequest.setEmail(customer.getEmail());
    		
    		Map<String,Organization> organizationMap = OrganizationData.workWithAllOrganizationData(customer.getOrganization(),null,"get-one",null);
    		
    		if(organizationMap == null || organizationMap.size() == 0) {
    			throw new Exception("Organization not found for employee having org : "+ customer.getOrganization());
    		}
    		
    		Organization Organization = organizationMap.get(customer.getOrganization());
    		whatsAppTemplateVariableRequest.setOrganization(Organization);

    		//Add detail to memory Data
    		Map<String,List<WhatsAppPhoneNumberTemplates>> allTemplatesMap = WhatsAppMemoryData.workWithWhatsAppPhoneNumbersTemplates(connectedLine,null,"get-one");
    		
    		//System.out.println("Total templates fetched : "+allTemplatesMap.size());
    		
    		if(allTemplatesMap == null || allTemplatesMap.size() == 0) {
    				throw new Exception("No template found for parent. Please connect with support.");
    		}
    		
    		List<WhatsAppPhoneNumberTemplates> allTemplates = allTemplatesMap.get(connectedLine);

    		for (WhatsAppPhoneNumberTemplates element : allTemplates) {
                if (element == null || element.getTemplateName() == null) continue;

                String requested = templateName == null ? null : templateName.trim();
                String stored = element.getTemplateName() == null ? null : element.getTemplateName().trim();
                if (stored != null && requested != null && stored.equalsIgnoreCase(requested)) {

                	whatsAppTemplateVariableRequest.setWhatsAppPhoneNumberTemplate(element);

                    return okHttpSendTemplateMessageClient.sendMessage(
                            MESSAGING_PRODUCT.whatsapp.name(),
                            customer.getPhoneNumber(),
                            whatsAppTemplateVariableRequest,
                            element.getLanguageCode(), // or req.getWhatsAppPhoneNumberTemplate().getLanguageCode()
                            element.getWhatsAppPhoneNumber().getWhatsAppProject().getApiVersion(),
                            element.getWhatsAppPhoneNumber().getPhoneNumberID(),
                            element.getWhatsAppPhoneNumber().getWhatsAppProject().getAccessToken()
                    );
                }
            }
    	}
    	catch(Exception  e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    	
    	return jsonObject;
    }
    
    
    public JSONObject sendWhatsAppMessageAsPerBlobType(Media media, WhatsAppChatHistory inputDTO, WhatsAppPhoneNumber phoneNumber) throws Exception {
        JSONObject toReturn = null;

        try {
            String whatsAppMediaType = media.getWhatsAppMediaType();

            System.out.println("sendWhatsAppMessageAsPerBlobType of type : " + whatsAppMediaType);

            if (whatsAppMediaType.equals(MESSAGE_TYPE.audio.name())) {
                // String messagingProduct,String recipientPhoneNumber,String previousMessageId,String audioId,String version,String phoneNumberID, String token
                toReturn = okHttpSendAudioMessageByIdClient.sendMessage(
                    inputDTO.getMessageString(), media.getName(), MESSAGING_PRODUCT.whatsapp.name(), 
                    inputDTO.getPhoneNumberWith(), null, inputDTO.getWhatsAppMediaId(), 
                    phoneNumber.getWhatsAppProject().getApiVersion(), phoneNumber.getPhoneNumberID(), 
                    phoneNumber.getWhatsAppProject().getAccessToken());
            } else if (whatsAppMediaType.equals(MESSAGE_TYPE.video.name())) {
                toReturn = okHttpSendVideoMessageByIdClient.sendMessage(
                    inputDTO.getMessageString(), media.getName(), MESSAGING_PRODUCT.whatsapp.name(), 
                    inputDTO.getPhoneNumberWith(), null, inputDTO.getWhatsAppMediaId(), 
                    phoneNumber.getWhatsAppProject().getApiVersion(), phoneNumber.getPhoneNumberID(), 
                    phoneNumber.getWhatsAppProject().getAccessToken());
            } else if (whatsAppMediaType.equals(MESSAGE_TYPE.image.name())) {
                toReturn = okHttpSendImageMessageByIdClient.sendMessage(
                    inputDTO.getMessageString(), media.getName(), MESSAGING_PRODUCT.whatsapp.name(), 
                    inputDTO.getPhoneNumberWith(), null, inputDTO.getWhatsAppMediaId(), 
                    phoneNumber.getWhatsAppProject().getApiVersion(), phoneNumber.getPhoneNumberID(), 
                    phoneNumber.getWhatsAppProject().getAccessToken());
            } else if (whatsAppMediaType.equals(MESSAGE_TYPE.sticker.name())) {
                toReturn = okHttpSendStickerMessageByIdClient.sendMessage(
                    inputDTO.getMessageString(), media.getName(), MESSAGING_PRODUCT.whatsapp.name(), 
                    inputDTO.getPhoneNumberWith(), null, inputDTO.getWhatsAppMediaId(), 
                    phoneNumber.getWhatsAppProject().getApiVersion(), phoneNumber.getPhoneNumberID(), 
                    phoneNumber.getWhatsAppProject().getAccessToken());
            } else if (whatsAppMediaType.equals(MESSAGE_TYPE.document.name())) {
                toReturn = okHttpSendDocumentMessageByIdClient.sendMessage(
                    inputDTO.getMessageString(), media.getName(), MESSAGING_PRODUCT.whatsapp.name(), 
                    inputDTO.getPhoneNumberWith(), null, inputDTO.getWhatsAppMediaId(), 
                    phoneNumber.getWhatsAppProject().getApiVersion(), phoneNumber.getPhoneNumberID(), 
                    phoneNumber.getWhatsAppProject().getAccessToken());
            } else {
                //System.out.println("Unsupported media type: " + whatsAppMediaType);
                throw new Exception("Media type " + whatsAppMediaType + " not supported for sending.");
            }
        } catch (Exception e) {
            //System.out.println("Exception in sendWhatsAppMessageAsPerBlobType");
            e.printStackTrace();
            throw e;
        }

        return toReturn;
    }

    private boolean isAtleastTwentyeightDaysAgo(Date date) {
        //System.out.println("isAtleastTwentyeightDaysAgo called with date: " + date);
        Instant instant = Instant.ofEpochMilli(date.getTime());
        Instant twentyEightDaysAgo = Instant.now().minus(Duration.ofDays(28));

        try {
            boolean result = instant.isBefore(twentyEightDaysAgo);
            //System.out.println("Date " + date + " is at least 28 days ago? " + result);
            return result;
        } catch (Exception e) {
            //System.out.println("Exception in isAtleastTwentyeightDaysAgo");
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

}
