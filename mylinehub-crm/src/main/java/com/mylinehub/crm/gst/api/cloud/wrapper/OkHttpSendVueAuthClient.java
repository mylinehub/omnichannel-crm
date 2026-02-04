package com.mylinehub.crm.gst.api.cloud.wrapper;

import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.moczul.ok2curl.CurlInterceptor;
import com.mylinehub.crm.utils.OkHttpLoggerUtils;
import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;

import lombok.AllArgsConstructor;
import okhttp3.MultipartBody;
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
public class OkHttpSendVueAuthClient {

    private final ApplicationContext applicationContext;

    public JSONObject sendMessage(String clientId, String clientSecret) {
        System.out.println("OkHttpSendVueAuthClient sendMessage");
        JSONObject toReturn = null;

        try {
            System.out.println("Before generating http client");

            OkHttpLoggerUtils myLogger = new OkHttpLoggerUtils(); // Your custom logger
            CurlInterceptor curlInterceptor = new CurlInterceptor(myLogger);

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .addInterceptor(curlInterceptor)
                    .build();

            System.out.println("After generating http client");
            System.out.println("clientId : " + safe(clientId));
            // IMPORTANT: Never log secrets in plaintext
            System.out.println("clientSecret : " + mask(clientSecret));

            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("client_id", clientId)
                    .addFormDataPart("client_secret", clientSecret)
                    .build();

            System.out.println("After generating http body");

            String domain = applicationContext.getEnvironment().getProperty("spring.gst.deep.vue.domain");

            Request request = new Request.Builder()
                    .url("https://" + domain + "/v1/authorize")
                    .post(body)
                    .addHeader("Accept", "application/json")
                    .build();

            System.out.println("Request : " + request);
            System.out.println("After generating http request, sending it now...");

            try (Response response = client.newCall(request).execute()) {
                toReturn = new OkHttpResponseFunctions().extractJSONObjectFromResponseAndCloseBuffer(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    private static String safe(String s) {
        return s == null ? "null" : s;
    }

    private static String mask(String s) {
        if (s == null || s.isEmpty()) return "null/empty";
        int keep = Math.min(3, s.length());
        String tail = s.substring(Math.max(0, s.length() - 4));
        return s.substring(0, keep) + "****" + tail;
    }
}
