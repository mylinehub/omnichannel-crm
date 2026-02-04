package com.mylinehub.crm.whatsapp.api.cloud.wrapper.block;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.SendMessageToWhatsApp;

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
public class OkHttpUnBlockUserClient {
	
	private final SendMessageToWhatsApp sendMessageToWhatsApp;
	
	public JSONObject unblockUser(String version,String phoneNumberID, String token) throws Exception
	{
		
		JSONObject toReturn = null;
		try {
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType,"{\n    \"messaging_product\": \"whatsapp\",\n    \"block_users\": [\n        {\n            \"user\": \"{{Recipient-Phone-Number}}\"\n        }\n    ]\n}");

			 try (Response response = sendMessageToWhatsApp.sendUnBlockUserMessage(body, version, phoneNumberID,token)) {
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
