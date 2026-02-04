package com.mylinehub.crm.whatsapp.api.cloud.wrapper.media;

import java.io.File;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.SendMessageToWhatsApp;
import com.mylinehub.crm.whatsapp.enums.SEND_MESSAGE_KEYS;

import lombok.AllArgsConstructor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class OkHttpUploadDocClient {

	private final SendMessageToWhatsApp sendMessageToWhatsApp;
	
	//Used in other kind of files
	public JSONObject uploadUsingMultipart(String messagingProduct,String filePath,String version,String phoneNumberID, String token) throws Exception
	{
		
		JSONObject toReturn = null;
		try {
			MediaType mediaType = MediaType.parse("application/octet-stream");
			RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
					  .addFormDataPart("messaging_product",messagingProduct)
					  .addFormDataPart("file",filePath,RequestBody.create(mediaType,new File(filePath)))
					  .build();
			
			try (Response response = sendMessageToWhatsApp.sendMediaMessage(body, version, phoneNumberID,token)) {
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
	
	
	//Usedin audio
	public JSONObject uploadUsingJson(String messagingProduct,String filePath,String mimeType,String version,String phoneNumberID, String token) throws Exception
	{
		
		JSONObject toReturn = null;
		try {
				MediaType mediaType = MediaType.parse("application/json");
				JSONObject documentObject = new JSONObject();
				documentObject.put(SEND_MESSAGE_KEYS.file.name(), filePath);
				documentObject.put(SEND_MESSAGE_KEYS.type.name(), mimeType);
				documentObject.put(SEND_MESSAGE_KEYS.messaging_product.name(), messagingProduct);
				RequestBody body = RequestBody.create(mediaType,documentObject.toString());
				try (Response response = sendMessageToWhatsApp.sendMediaMessageWithContentTypeAsJson(body, version, phoneNumberID,token)) {
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
