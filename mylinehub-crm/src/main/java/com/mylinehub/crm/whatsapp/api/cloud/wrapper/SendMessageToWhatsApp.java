package com.mylinehub.crm.whatsapp.api.cloud.wrapper;

import java.util.Map;

import org.springframework.stereotype.Service;
import com.mylinehub.crm.whatsapp.enums.user.BLOCK_USER_PARAMTERS;
import lombok.AllArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class SendMessageToWhatsApp {

	private final OkHttpClient client;

	
	public Response sendNormalMessage(RequestBody body, String version,String phoneNumberID, String token)
	{
		Response toReturn = null;
		try {
			
			    System.out.println("SendMessageToWhatsApp sendNormalMessage");

			    System.out.println("SendMessageToWhatsApp client created ...");
                Request request = new Request.Builder()
					  .url("https://graph.facebook.com/"+version+"/"+phoneNumberID+"/messages")
					  .method("POST", body)
					  .addHeader("Content-Type", "application/json")
					  .addHeader("Authorization", "Bearer "+ token)
					  .build();
                System.out.println("SendMessageToWhatsApp sending request to server ...");
                
                System.out.println("*******************************************************");
                System.out.println("Whats App request");
                System.out.println("*******************************************************");
                System.out.println(request);
                System.out.println(request.body().toString());
				toReturn = client.newCall(request).execute();
		}
		catch(Exception e)
		{
			//System.out.println("SendMessageToWhatsApp sendNormalMessage exception");
			e.printStackTrace();
		}
		
		return toReturn;
	}
	
	public Response getAllBlockUsersMessage(RequestBody body, String version,String phoneNumberID, String token, Map<String,String> queryParameters)
	{
		Response toReturn = null;
		try {
			
				//System.out.println("SendMessageToWhatsApp getAllBlockUsersMessage");
			    
			    
			    String urlString = "https://graph.facebook.com/"+version+"/"+phoneNumberID+"/block_users";

			    if(queryParameters != null && queryParameters.size() >0) {
			    	urlString = urlString + "?";
			    	
			    	for (BLOCK_USER_PARAMTERS parameter : BLOCK_USER_PARAMTERS.values()) { 
			    	    if(queryParameters.get(parameter.name()) != null) {
			    	    	
			    	    	String parameterString = "";
			    	    	if(urlString.endsWith("block_users"))
			    	    	{
			    	    		parameterString = "?parameter.name()="+queryParameters.get(parameter.name());
			    	    	}
			    	    	else {
			    	    		parameterString = "&parameter.name()="+queryParameters.get(parameter.name());
							}
			    	    	urlString = queryParameters + parameterString;
			    	    }
			    	}
			    }
			    
                Request request = new Request.Builder()
					  .url(urlString)
					  .method("GET", body)
					  .addHeader("Authorization", "Bearer "+ token)
					  .build();
				toReturn = client.newCall(request).execute();
		}
		catch(Exception e)
		{
			//System.out.println("SendMessageToWhatsApp getAllBlockUsersMessage exception");
			e.printStackTrace();
		}
		
		return toReturn;
	}
	
	
	public Response sendBlockUserMessage(RequestBody body, String version,String phoneNumberID, String token)
	{
		Response toReturn = null;
		try {
				//System.out.println("SendMessageToWhatsApp sendBlockUsersMessage");
		    
                Request request = new Request.Builder()
					  .url("https://graph.facebook.com/"+version+"/"+phoneNumberID+"/block_users")
					  .method("POST", body)
					  .addHeader("Content-Type", "application/json")
					  .addHeader("Authorization", "Bearer "+ token)
					  .build();
				toReturn = client.newCall(request).execute();
		}
		catch(Exception e)
		{
			//System.out.println("SendMessageToWhatsApp sendBlockUsersMessage exception");
			e.printStackTrace();
		}
		
		return toReturn;
	}
	
	
	public Response sendUnBlockUserMessage(RequestBody body, String version,String phoneNumberID, String token)
	{
		Response toReturn = null;
		try {
				//System.out.println("SendMessageToWhatsApp sendUnBlockUsersMessage");
		    
                Request request = new Request.Builder()
					  .url("https://graph.facebook.com/"+version+"/"+phoneNumberID+"/block_users")
					  .method("DELETE", body)
					  .addHeader("Content-Type", "application/json")
					  .addHeader("Authorization", "Bearer "+ token)
					  .build();
				toReturn = client.newCall(request).execute();
		}
		catch(Exception e)
		{
			//System.out.println("SendMessageToWhatsApp sendUnBlockUsersMessage exception");
			e.printStackTrace();
		}
		
		return toReturn;
	}
	
	
	
	public Response sendMediaMessage(RequestBody body, String version,String phoneNumberID, String token)
	{
		Response toReturn = null;
		try {
				//System.out.println("SendMessageToWhatsApp sendMediaMessage");

                Request request = new Request.Builder()
					  .url("https://graph.facebook.com/"+version+"/"+phoneNumberID+"/media")
					  .method("POST", body)
					  .addHeader("Authorization", "Bearer "+ token)
					  .build();
				toReturn = client.newCall(request).execute();
		}
		catch(Exception e)
		{
			//System.out.println("SendMessageToWhatsApp sendMediaMessage exception");
			e.printStackTrace();
		}
		
		return toReturn;
	}
	
	public Response sendMediaMessageWithContentTypeAsJson(RequestBody body, String version,String phoneNumberID, String token)
	{
		Response toReturn = null;
		try {
				//System.out.println("SendMessageToWhatsApp sendMediaMessageWithContentTypeAsJson");

                Request request = new Request.Builder()
					  .url("https://graph.facebook.com/"+version+"/"+phoneNumberID+"/media")
					  .method("POST", body)
					  .addHeader("Content-Type", "application/json")
					  .addHeader("Authorization", "Bearer "+ token)
					  .build();
				toReturn = client.newCall(request).execute();
		}
		catch(Exception e)
		{
			//System.out.println("SendMessageToWhatsApp sendMediaMessageWithContentTypeAsJson exception");
			e.printStackTrace();
		}
		
		return toReturn;
	}
	
	public Response downloadMedia(RequestBody body, String version,String mediaUrl, String token)
	{
		Response toReturn = null;
		try {
				//System.out.println("SendMessageToWhatsApp retrieveMedia");
		    
                Request request = new Request.Builder()
					  .url("https://graph.facebook.com/"+version+"/"+mediaUrl)
					  .method("GET", body)
					  .addHeader("Authorization", "Bearer "+ token)
					  .build();
				toReturn = client.newCall(request).execute();
		}
		catch(Exception e)
		{
			//System.out.println("SendMessageToWhatsApp retrieveMedia exception");
			e.printStackTrace();
		}
		
		return toReturn;
	}
	
	public Response retrieveMediaUrl(RequestBody body, String version,String mediaId, String phoneNumberId,String token)
	{
		Response toReturn = null;
		try {
				//System.out.println("SendMessageToWhatsApp retrieveMediaUrl");
		    
                Request request = new Request.Builder()
					  .url("https://graph.facebook.com/"+version+"/"+mediaId+"?phone_number_id="+phoneNumberId)
					  .method("GET", body)
					  .addHeader("Authorization", "Bearer "+ token)
					  .build();
				toReturn = client.newCall(request).execute();
		}
		catch(Exception e)
		{
			//System.out.println("SendMessageToWhatsApp retrieveMediaUrl exception");
			e.printStackTrace();
		}
		
		return toReturn;
	}

	public Response deleteMedia(RequestBody body, String version,String mediaId, String phoneNumberId,String token)
	{
		Response toReturn = null;
		try {
				//System.out.println("SendMessageToWhatsApp deleteMedia");

                Request request = new Request.Builder()
					  .url("https://graph.facebook.com/"+version+"/"+mediaId+"?phone_number_id="+phoneNumberId)
					  .method("DELETE", body)
					  .addHeader("Authorization", "Bearer "+ token)
					  .build();
				toReturn = client.newCall(request).execute();
		}
		catch(Exception e)
		{
			//System.out.println("SendMessageToWhatsApp deleteMedia exception");
			e.printStackTrace();
		}
		
		return toReturn;
	}
}
