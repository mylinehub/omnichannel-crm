package com.mylinehub.crm.whatsapp.api.cloud.wrapper.media;

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
public class OkHttpSendDeleteMediaClient {

	private final SendMessageToWhatsApp sendMessageToWhatsApp;
	
	public JSONObject sendMessage(String mediaId,String version,String phoneNumberID, String token) throws Exception
	{
		
		JSONObject toReturn = null;
		try {

			MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
			RequestBody body = RequestBody.create(mediaType,"{}");
			 
			 try (Response response = sendMessageToWhatsApp.deleteMedia(body, version, mediaId, phoneNumberID,token)) {
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
