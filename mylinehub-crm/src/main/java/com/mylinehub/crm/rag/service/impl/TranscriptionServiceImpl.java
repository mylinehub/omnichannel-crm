package com.mylinehub.crm.rag.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moczul.ok2curl.CurlInterceptor;
import com.mylinehub.crm.rag.service.TranscriptionService;
import com.mylinehub.crm.utils.OkHttpLoggerUtils;
import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class TranscriptionServiceImpl implements TranscriptionService {

    private final ObjectMapper mapper;
    private final OkHttpClient client;
    
    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Value("${openai.whisper-model}")
    private String whisperModel;

    public TranscriptionServiceImpl(OkHttpClient client, ObjectMapper mapper) {
    	this.client=client;
        this.mapper = mapper;
    }

    @Override
    public String transcribe(File file, String mimeType) throws Exception {
    	
    	System.out.println("transcribe API call");
	    
        System.out.println("[TranscriptionService] Starting transcription via OpenAI for file: " + file.getName());
        String url = "https://api.openai.com/v1/audio/transcriptions";

        RequestBody fileBody = RequestBody.create(MediaType.parse(mimeType != null ? mimeType : "application/octet-stream"),file);

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .addFormDataPart("model", whisperModel);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + openAiApiKey)
                .post(builder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            String respBody = new OkHttpResponseFunctions().extractStringFromResponseAndCloseBuffer(response);
            JsonNode root = mapper.readTree(respBody);
            String text = root.has("text") ? root.get("text").asText() : null;
            System.out.println("[TranscriptionService] Transcription done, length: " + (text != null ? text.length() : 0));
            return text;
        }
    }
}
