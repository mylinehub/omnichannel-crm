//package com.mylinehub.crm.rag.service.impl;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.mylinehub.crm.rag.dto.AiInterfaceInputDto;
//import com.mylinehub.crm.rag.dto.AiInterfaceOutputDto;
//import com.mylinehub.crm.rag.service.LlmService;
//import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;
//
//import okhttp3.*;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//
//@Service
//public class LlmServiceImpl implements LlmService {
//
//    private final ObjectMapper mapper;
//    private final OkHttpClient client;
//
//    @Value("${openai.api-key}")
//    private String openAiApiKey;
//
//    @Value("${openai.model}")
//    private String openAiModel;
//
//    public LlmServiceImpl(OkHttpClient client, ObjectMapper mapper) {
//    	this.client=client;
//        this.mapper = mapper;
//    }
//
//    @Override
//    public AiInterfaceOutputDto generateAiInterfaceDto(String systemPrompt, String userPrompt,AiInterfaceInputDto assistantContext) throws Exception {
//    	
//    	System.out.println("generateAiInterfaceDto API call");
//	    
//        System.out.println("[LlmService] Preparing chat completion request...");
//        String url = "https://api.openai.com/v1/chat/completions";
//
//        // Compose messages array
//        ObjectNode payload = mapper.createObjectNode();
//        payload.put("model", openAiModel);
//
//        ArrayNode messages = mapper.createArrayNode();
//        
//        if (systemPrompt != null) {
//        messages.add(mapper.createObjectNode()
//                .put("role", "system")
//                .put("content", systemPrompt));
//        }
//        
//        if (assistantContext != null) {
//            String contextJson = mapper.writeValueAsString(assistantContext);
//            messages.add(mapper.createObjectNode()
//                    .put("role", "assistant")
//                    .put("content", contextJson));
//        }
//        
//        messages.add(mapper.createObjectNode()
//                .put("role", "user")
//                .put("content", userPrompt));
//
//        
//        payload.set("messages", messages);
//        payload.put("max_tokens", 1024);
//
//        String jsonPayload = payload.toString();
//
//        
//        RequestBody body = RequestBody.create(MediaType.parse("application/json"),jsonPayload);
//        Request request = new Request.Builder()
//                .url(url)
//                .header("Authorization", "Bearer " + openAiApiKey)
//                .post(body)
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            String respBody =  new OkHttpResponseFunctions().extractStringFromResponseAndCloseBuffer(response);
//            System.out.println("[LlmService] Received raw chat response.");
//            // Extract the assistant message
//            String assistantText = null;
//            try {
//                JsonNode root = mapper.readTree(respBody);
//                assistantText = root.at("/choices/0/message/content").asText();
//            } catch (Exception e) {
//                System.out.println("[LlmService] Failed to parse assistant text: " + e.getMessage());
//                assistantText = respBody;
//            }
//
//            // The assistantText must be strict JSON matching AiInterfaceDto; attempt parse
//            try {
//            	AiInterfaceOutputDto dto = mapper.readValue(assistantText, AiInterfaceOutputDto.class);
//                System.out.println("[LlmService] Parsed AiInterfaceDto from model output.");
//                return dto;
//            } catch (Exception e) {
//                System.out.println("[LlmService] Failed to parse model output to AiInterfaceDto: " + e.getMessage());
//                throw new Exception("[LlmService] Failed to parse model output to AiInterfaceDto: " + e.getMessage());
//            }
//        }
//    }
//}

