package com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages;


import org.json.JSONObject;
import org.springframework.stereotype.Service;
import com.mylinehub.crm.whatsapp.enums.RECEPIENT_TYPE;
import com.mylinehub.crm.whatsapp.enums.SEND_MESSAGE_KEYS;
import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.SendMessageToWhatsApp;
import com.mylinehub.crm.whatsapp.enums.MESSAGE_TYPE;
import lombok.AllArgsConstructor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class OkHttpSendContactsMessageClient {
	
	private final SendMessageToWhatsApp sendMessageToWhatsApp;
	
	public JSONObject sendMessage(String messagingProduct,String recipientPhoneNumber,String previousMessageId,boolean previewURL,String jsonString,String version,String phoneNumberID, String token) throws Exception
	{
		
		JSONObject toReturn = null;
		try {
			
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body;
			
			JSONObject contextObject = new JSONObject();
			contextObject.put(SEND_MESSAGE_KEYS.message_id.name(), previousMessageId);
			
			JSONObject bodyObject = new JSONObject();
			bodyObject.put(SEND_MESSAGE_KEYS.messaging_product.name(), messagingProduct);
			bodyObject.put(SEND_MESSAGE_KEYS.recipient_type.name(), RECEPIENT_TYPE.individual.name());
			bodyObject.put(SEND_MESSAGE_KEYS.to.name(), recipientPhoneNumber);
			if(previousMessageId != null)
				bodyObject.put(SEND_MESSAGE_KEYS.context.name(), contextObject.toString());
			bodyObject.put(SEND_MESSAGE_KEYS.type.name(), MESSAGE_TYPE.contacts.name());
			bodyObject.put(SEND_MESSAGE_KEYS.contacts.name(), jsonString);
			
			
			body= RequestBody.create(mediaType,bodyObject.toString());
			
			try (Response response = sendMessageToWhatsApp.sendNormalMessage(body, version, phoneNumberID, token)) {
				toReturn = new OkHttpResponseFunctions().extractJSONObjectFromResponseAndCloseBuffer(response);
	        }
		}
	    catch(Exception e)
		{
	    	e.printStackTrace();
	    	throw e;
		}
		
		return toReturn;
	}

}
