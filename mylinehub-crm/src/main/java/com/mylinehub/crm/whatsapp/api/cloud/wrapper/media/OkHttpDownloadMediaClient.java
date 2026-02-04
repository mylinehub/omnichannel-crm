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
public class OkHttpDownloadMediaClient {

	private final SendMessageToWhatsApp sendMessageToWhatsApp;
	
	public JSONObject sendMessage(String version,String mediaUrl, String token) throws Exception
	{
		
		JSONObject toReturn = null;
		try {
			
				MediaType mediaType = MediaType.parse("text/plain");
				RequestBody body = RequestBody.create(mediaType,"");
				
				try (Response response = sendMessageToWhatsApp.downloadMedia(body, version, mediaUrl, token)) {
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
