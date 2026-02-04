package com.mylinehub.crm.utils.okhttp;

import java.io.IOException;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class OkHttpResponseFunctions {
	
	
public String extractStringFromResponseAndCloseBuffer(Response response) throws IOException {
		
	    String toReturn = null;
	    ResponseBody responseBody = null;
	    BufferedSource responseSource = null;
	    Buffer responseBuffer = null;
        String responseBodyString = null;
        
	    try {
			//System.out.println("extractStringFromResponseAndCloseBuffer");
			if (!response.isSuccessful()) {
	            System.err.println("Initial request failed: " + response.code());
	            System.err.println("Response Body : " + response.body().string());
	            return null;
	        }
			else {
				//System.out.println("Response success");
			}
			
	        responseBody = response.body();
	        responseSource = responseBody.source();
	        responseSource.request(Long.MAX_VALUE); // request the entire body.
	        responseBuffer = responseSource.getBuffer();
	        responseBodyString = responseBuffer.clone().readString(Charset.forName("UTF-8"));
	        // clone buffer before reading from it		
	        //System.out.println("responseBodyString : "+responseBodyString);
	        
	        // Process the response body content here, e.g., using responseBody.string(), responseBody.bytes(), or responseBody.source()
	    }
	    catch(Exception e)
	    {
	    	throw e;
	    }
	    finally {
	        // Ensure the response body is closed after processing
	        if (responseSource != null) {
	        	responseSource.close();
	        }
	        
	        //System.out.println("After closing source");
	        
	        if (responseBuffer != null) {
	        	responseBuffer.close();
	        }
	        
	        //System.out.println("After closing buffer");
	        
	        if (responseBody != null) {
	        	responseBody.close();
	        }
	        
	        if(response != null) {
	        	response.close();
	        }
	    }

        //System.out.println("Initial Response: " + responseBodyString);
        
        toReturn = responseBodyString;
        
        return toReturn;
		
	}
	
	public JSONObject extractJSONObjectFromResponseAndCloseBuffer(Response response) throws IOException {
		
		JSONObject toReturn = null;
		ResponseBody responseBody = null;
		BufferedSource responseSource = null;
		Buffer responseBuffer = null;
	    String responseBodyString = null;
	        
	    try {
			//System.out.println("extractJSONObjectFromResponseAndCloseBuffer");
			if (!response.isSuccessful()) {
	            System.err.println("Initial request failed: " + response.code());
	            System.err.println("Response Body : " + response.body().string());
	            return null;
	        }
			else {
				//System.out.println("Response success");
			}
			
	        responseBodyString = null;
	        responseBody = response.body();
	        responseSource = responseBody.source();
	        responseSource.request(Long.MAX_VALUE); // request the entire body.
	        responseBuffer = responseSource.getBuffer();
	        responseBodyString = responseBuffer.clone().readString(Charset.forName("UTF-8"));
	        // clone buffer before reading from it		
	        //System.out.println("responseBody : "+responseBody);
	        
	        // Process the response body content here, e.g., using responseBody.string(), responseBody.bytes(), or responseBody.source()
	
		 }
	    catch(Exception e)
	    {
	    	throw e;
	    }
	    finally {
	        // Ensure the response body is closed after processing
	        if (responseSource != null) {
	        	responseSource.close();
	        }
	        
	        //System.out.println("After closing source");
	        
	        if (responseBuffer != null) {
	        	responseBuffer.close();
	        }
	        
	        //System.out.println("After closing buffer");
	        
	        if (responseBody != null) {
	        	responseBody.close();
	        }
	        
	        if(response != null) {
	        	response.close();
	        }
	    }

        //System.out.println("Initial Response: " + responseBodyString);
        
        toReturn = new JSONObject(responseBodyString);
        
        return toReturn;
		
	}
	
	public JSONObject extractFirstJSONObjectFromResponseAndCloseBufferViaJsonArray(Response response) throws IOException {
		
		JSONObject toReturn = null;
		ResponseBody responseBody = null;
		BufferedSource responseSource = null;
		Buffer responseBuffer = null;
	    String responseBodyString = null;
	        
	    try {
			//System.out.println("extractFirstJSONObjectFromResponseAndCloseBufferViaJsonArray");
			if (!response.isSuccessful()) {
	            System.err.println("Initial request failed: " + response.code());
	            System.err.println("Response Body : " + response.body().string());
	            return null;
	        }
			else {
				//System.out.println("Response success");
			}
			
	        responseBodyString = null;
	        responseBody = response.body();
	        responseSource = responseBody.source();
	        responseSource.request(Long.MAX_VALUE); // request the entire body.
	        responseBuffer = responseSource.getBuffer();
	        responseBodyString = responseBuffer.clone().readString(Charset.forName("UTF-8"));
	        // clone buffer before reading from it		
	        //System.out.println("responseBody : "+responseBody);
	        
	        // Process the response body content here, e.g., using responseBody.string(), responseBody.bytes(), or responseBody.source()
	    }
        catch(Exception e)
	    {
	    	throw e;
	    }
	    finally {
	        // Ensure the response body is closed after processing
	        if (responseSource != null) {
	        	responseSource.close();
	        }
	        
	        //System.out.println("After closing source");
	        
	        if (responseBuffer != null) {
	        	responseBuffer.close();
	        }
	        
	        //System.out.println("After closing buffer");
	        
	        if (responseBody != null) {
	        	responseBody.close();
	        }
	        
	        if(response != null) {
	        	response.close();
	        }
	    }
	    

        //System.out.println("Initial Response: " + responseBodyString);
        JSONArray interimArray = null;
        try {
        	//System.out.println("Converting to json array");
            interimArray = new JSONArray(responseBodyString);
        }
        catch(Exception e) {
        	return null;
        }
         
        //System.out.println("Fetching first object of json array");
        toReturn =  new JSONObject(interimArray.get(0));
        
        return toReturn;
		
	}

}
