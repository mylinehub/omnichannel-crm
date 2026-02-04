package com.mylinehub.crm.gst.api.cloud.wrapper;

import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.moczul.ok2curl.CurlInterceptor;
import com.mylinehub.crm.utils.OkHttpLoggerUtils;
import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;

import lombok.AllArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class OkHttpSendVueGstClient {
	
	private final ApplicationContext applicationContext;
	
	public JSONObject sendMessage(String token,String clientSecret,String gstNumber)
	{
		JSONObject toReturn = null;
		try {	
			
				OkHttpLoggerUtils myLogger = new OkHttpLoggerUtils(); // Your custom logger
		        CurlInterceptor curlInterceptor = new CurlInterceptor(myLogger);
			    OkHttpClient client = new OkHttpClient().newBuilder()
			    	  .addInterceptor(curlInterceptor)
//		              .socketFactory(new OkHttpPoolSocketFactorySocket())
//		              .connectionPool(new ConnectionPool(0, 1, TimeUnit.MILLISECONDS))  // Disable keep-alive
					  .build();
		    
//				MediaType mediaType = MediaType.parse("text/plain");
//				RequestBody body = RequestBody.create(mediaType,"");
				Request request = new Request.Builder()
					  .url("https://"+applicationContext.getEnvironment().getProperty("spring.gst.deep.vue.domain")+"/v1/verification/gstinlite?gstin_number="+gstNumber)
//					  .method("GET", body)
					  .get()
					  .addHeader("Authorization", "Bearer "+token)
					  .addHeader("x-api-key", clientSecret)
					  .build();
				try (Response response = client.newCall(request).execute()) {
					toReturn = new OkHttpResponseFunctions().extractJSONObjectFromResponseAndCloseBuffer(response);
		        }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return toReturn;
	}
	
}
