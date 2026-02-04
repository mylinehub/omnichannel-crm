package com.mylinehub.crm.rag.service;

import com.mylinehub.crm.rag.data.PromptAndAssistantData;
import com.mylinehub.crm.rag.data.dto.PromptAndAssistantDataDTO;
import com.mylinehub.crm.rag.dto.AiInterfaceOutputDto;
import com.mylinehub.crm.rag.dto.AiPropertyInventoryVerificationOutputDto;
import com.mylinehub.crm.rag.dto.AssistantResponse;
import com.mylinehub.crm.rag.dto.LanguageAndHeuristicCheckResponse;
import com.mylinehub.crm.rag.dto.ThreadResponse;
import com.mylinehub.crm.rag.model.*;
import com.mylinehub.crm.rag.repository.AssistantRepository;
import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Service
public class AssistantService {

	    @Value("${openai.api-key}")
	    private String openAiApiKey;

	    private final String BASE_URL = "https://api.openai.com/v1";
	    private final ObjectMapper mapper = new ObjectMapper();
	    private final AssistantRepository repository;
	    private final AIErrorLoggingService aIErrorLoggingService;
	    private final OkHttpClient client;

	    public AssistantService(OkHttpClient client,AssistantRepository repository,AIErrorLoggingService aIErrorLoggingService) {
	    	this.client = client;
	        this.repository = repository;
	        this.aIErrorLoggingService = aIErrorLoggingService;
	    }
	    
	    
	    public String getCompletion(String model, String systemPrompt, String userPrompt) throws Exception {

	        final int MAX_ATTEMPTS = 3;
	        long backoffMs = 400;
	        Exception lastException = null;

	        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
	            long start = System.currentTimeMillis();
	            try {
	                String result = getCompletionOnce(model, systemPrompt, userPrompt);

	                // HARD GUARD: never allow empty result
	                if (result == null || result.trim().isEmpty()) {
	                    throw new IOException("OpenAI returned empty content");
	                }

	                if (attempt > 1) {
	                    System.out.println("[OpenAI] success after retry attempt=" + attempt);
	                }
	                return result;

	            } catch (Exception e) {
	                lastException = e;

	                String msg = e.getMessage();
	                System.err.println(
	                    "[OpenAI] attempt=" + attempt +
	                    ", elapsed=" + (System.currentTimeMillis() - start) + "ms" +
	                    ", exception=" + e.getClass().getSimpleName() +
	                    ", message=" + (msg == null ? "<null>" : msg)
	                );

	                // retry only on retry-worthy errors
	                boolean retryable =
	                        e instanceof java.io.InterruptedIOException ||
	                        e instanceof java.net.SocketTimeoutException ||
	                        e instanceof java.net.ConnectException ||
	                        e instanceof javax.net.ssl.SSLException ||
	                        (msg != null && msg.contains("Unexpected code"));

	                if (!retryable || attempt == MAX_ATTEMPTS) {
	                    throw e;
	                }

	                try {
	                    Thread.sleep(backoffMs);
	                } catch (InterruptedException ie) {
	                    throw ie;
	                }

	                backoffMs = Math.min(backoffMs * 2, 2000);
	            }
	        }

	        throw lastException; // should never reach here
	    }
	    
	    
	    private String getCompletionOnce(String model, String systemPrompt, String userPrompt) throws Exception {

	        ObjectMapper mapper = new ObjectMapper();
	        List<Map<String, String>> messages = new ArrayList<>();

	        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
	            messages.add(Map.of("role", "system", "content", systemPrompt));
	        }
	        messages.add(Map.of("role", "user", "content", userPrompt));

	        Map<String, Object> payloadMap = new HashMap<>();
	        payloadMap.put("model", model);
	        payloadMap.put("messages", messages);

	        String payload = mapper.writeValueAsString(payloadMap);

	        Request request = new Request.Builder()
	                .url("https://api.openai.com/v1/chat/completions")
	                .post(RequestBody.create(MediaType.get("application/json"), payload))
	                .addHeader("Authorization", "Bearer " + openAiApiKey)
	                .build();

	        try (Response response = client.newCall(request).execute()) {

	            if (!response.isSuccessful()) {
	                throw new IOException("Unexpected code " + response.code());
	            }

	            String responseBody = response.body().string();
	            JsonNode root = mapper.readTree(responseBody);

	            return root.path("choices")
	                       .path(0)
	                       .path("message")
	                       .path("content")
	                       .asText(null);
	        }
	    }



	    
	    /**
	     * Build assistant name in format: {type}-{normalizedOrgName}
	     * Example:
	     *   type = language, organization = "MyLineHub CRM Pvt Ltd"
	     *   Result: language-MyLineHub-CRM-Pvt-Ltd
	     *
	     * Rules:
	     * - Replace all spaces, tabs, and multiple whitespace with single '-'
	     * - Trim leading/trailing spaces
	     * - Keep each word's first letter capitalized for neatness
	     */
	    public String buildAssistantName(String type, String organization) {
	        if (organization == null || organization.isBlank()) {
	            return null;
	        }

	        // Normalize whitespace (replace spaces, tabs, etc. with single hyphen)
	        String normalized = organization.trim().replaceAll("\\s+", "-");

	        // Optionally beautify capitalization: Title Case each word split by '-'
	        String[] parts = normalized.split("-");
	        StringBuilder formattedOrg = new StringBuilder();
	        for (int i = 0; i < parts.length; i++) {
	            String word = parts[i];
	            if (!word.isEmpty()) {
	                formattedOrg.append(
	                    word.substring(0, 1).toUpperCase()
	                    + word.substring(1).toLowerCase()
	                );
	                if (i < parts.length - 1) {
	                    formattedOrg.append("-");
	                }
	            }
	        }

	        // Combine enum name and formatted organization
	        return type.toLowerCase() + "-" + formattedOrg;
	    }

	
	    /**
	     * Synchronize assistants between OpenAI and local database.
	     * - Fetch all assistants (100 per batch)
	     * - Insert missing ones if they contain metadata (organization + systemPromptId)
	     * - Delete assistants remotely if metadata missing (likely old / manually created)
	     * - Save all new assistants together to minimize DB cost
	     */
	    public void syncAssistantsWithOpenAI() throws Exception {
	        //System.out.println("=== Starting assistant synchronization with OpenAI ===");

	        List<JsonNode> allAssistants = new ArrayList<>();
	        String paginationCursor = null;
	        int limit = 100;

	        // Step 1: Fetch all assistants in batches of 100
	        do {
	            // Build URL dynamically only when cursor is valid
	            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/assistants?limit=" + limit);
	            if (paginationCursor != null && !"null".equalsIgnoreCase(paginationCursor) && !paginationCursor.isBlank()) {
	                urlBuilder.append("&after=").append(paginationCursor);
	            }
	            String url = urlBuilder.toString();
	            //System.out.println("[DEBUG] Fetching assistants with URL: " + url); // debug log

	            Request listRequest = new Request.Builder()
	                    .url(url)
	                    .get()
	                    .addHeader("Authorization", "Bearer " + openAiApiKey)
	                    .addHeader("OpenAI-Beta", "assistants=v2")
	                    .build();

	            int fetchedCount = 0;

	            try (Response listResponse = client.newCall(listRequest).execute()) {
	                String listBody = new OkHttpResponseFunctions().extractStringFromResponseAndCloseBuffer(listResponse);

	                // Null check before parsing JSON
	                if (listBody == null || listBody.isBlank()) {
	                    //System.out.println("[WARN] Empty or null response body from OpenAI.");
	                    break;
	                }

	                JsonNode root = mapper.readTree(listBody);
	                JsonNode data = root.get("data");

	                // Proper handling of last_id to avoid "null" string issue
	                if (root.hasNonNull("last_id")) {
	                    paginationCursor = root.get("last_id").asText();
	                } else {
	                    paginationCursor = null;
	                }

	                // Handle data safely
	                if (data != null && data.isArray()) {
	                    fetchedCount = data.size();
	                    //System.out.println("[INFO] Assistant data fetched in this batch: " + fetchedCount);
	                    for (JsonNode assistant : data) {
	                        allAssistants.add(assistant);
	                    }
	                } else {
	                    fetchedCount = 0;
	                    //System.out.println("[INFO] No assistant data found in this batch.");
	                }

	                //System.out.println("[DEBUG] Total assistants collected so far: " + allAssistants.size());
	            }

	            // If fetched less than limit → no more records to fetch
	            if (fetchedCount < limit) {
	                paginationCursor = null;
	                //System.out.println("[INFO] Less than limit fetched. Ending pagination.");
	            }

	        } while (paginationCursor != null && !"null".equalsIgnoreCase(paginationCursor) && !paginationCursor.isBlank());

	        if (allAssistants.isEmpty()) {
	            //System.out.println("[INFO] No assistants found on OpenAI.");
	            return;
	        }

	        // Step 2: Collect all assistant IDs
	        List<String> remoteIds = new ArrayList<>();
	        for (JsonNode node : allAssistants) {
	            if (node.has("id")) {
	                remoteIds.add(node.get("id").asText());
	            }
	        }
	        //System.out.println("[INFO] Total assistants fetched from OpenAI: " + remoteIds.size());

	        // Step 3: Fetch all existing local assistants by IDs (single DB call)
	        List<AssistantEntity> localAssistants = repository.findByAssistantIdIn(remoteIds);
	        Set<String> localIds = new HashSet<>();
	        for (AssistantEntity local : localAssistants) {
	            localIds.add(local.getAssistantId());
	        }

	        // Step 4: Prepare batch list for new assistants to insert
	        List<AssistantEntity> newAssistants = new ArrayList<>();

	        // Step 5: Process each OpenAI assistant
	        for (JsonNode node : allAssistants) {
	            String assistantId = node.has("id") ? node.get("id").asText() : null;
	            String name = node.has("name") ? node.get("name").asText() : null;
	            String model = node.has("model") ? node.get("model").asText() : null;

	            JsonNode metadata = node.get("metadata");
	            String organization = (metadata != null && metadata.has("organization"))
	                    ? metadata.get("organization").asText()
	                    : null;
	            String systemPromptId = (metadata != null && metadata.has("systemPromptId"))
	                    ? metadata.get("systemPromptId").asText()
	                    : null;

	            // Case 1: Assistant missing metadata delete remotely
	            if (metadata == null || organization == null || systemPromptId == null) {
	                //System.out.println("[INFO] Deleting assistant without metadata: " + name + " (" + assistantId + ")");
	                try {
	                    deleteRemoteAssistantById(assistantId);
	                } catch (Exception e) {
	                    //System.out.println("[ERROR] Failed to delete assistant " + assistantId + ": " + e.getMessage());
	                }
	                continue;
	            }

	            // Case 2: Assistant exists remotely but not locally prepare to insert
	            if (!localIds.contains(assistantId)) {
	                AssistantEntity entity = new AssistantEntity();
	                entity.setAssistantId(assistantId);
	                entity.setName(name);
	                entity.setModel(model);
	                entity.setOrganization(organization);
	                entity.setSystemPromptId(systemPromptId);
	                //System.out.println("Adding Assistant to Database : "+mapper.writeValueAsString(entity));
	                newAssistants.add(entity);
	            }
	        }

	        // Step 6: Batch save all new assistants in one DB call
	        if (!newAssistants.isEmpty()) {
	            repository.saveAll(newAssistants);
	            //System.out.println("[INFO] Inserted new assistants in batch: " + newAssistants.size());

	            // Step 7: Update in-memory cache for each new assistant
	            for (AssistantEntity entity : newAssistants) {
	                PromptAndAssistantDataDTO dto = new PromptAndAssistantDataDTO();
	                dto.setAction("update");
	                dto.setOrganization(entity.getOrganization());
	                dto.setAssistantEntity(entity);
	                PromptAndAssistantData.workWithAllAssistants(dto);
	            }
	        } else {
	            //System.out.println("[INFO] No new assistants to insert.");
	        }

	        //System.out.println("=== Assistant synchronization completed successfully ===");
	    }

	
	    /**
	     * Helper method to delete assistant remotely by ID (used internally in sync).
	     */
	    private void deleteRemoteAssistantById(String assistantId) {
	        try {
	            //System.out.println("Deleting remote assistant by ID: " + assistantId);
	            Request request = new Request.Builder()
	                    .url(BASE_URL + "/assistants/" + assistantId)
	                    .delete()
	                    .addHeader("Authorization", "Bearer " + openAiApiKey)
	                    .addHeader("OpenAI-Beta", "assistants=v2")
	                    .build();

	            try (Response response = client.newCall(request).execute()) {
	            	try {
		                if (!response.isSuccessful()) {
		                    //System.out.println("Failed to delete remote assistant " + assistantId + ": " + response.code());
		                }
	            	}
	            	catch(Exception e) {
		        		throw e;
		        	}
		        	finally {
		        		response.close();
		        	}
	            }

	        } catch (Exception e) {
	            //System.out.println("Exception while deleting assistant " + assistantId + ": " + e.getMessage());
	        }
	    }


	    // 1 Create Assistant (local + OpenAI)
	    public AssistantEntity createAssistant(String name, String organization, String systemPromptId, String instructions, String model) throws Exception {
	        //System.out.println("Creating assistant for org: " + organization);

	        Optional<AssistantEntity> existing = repository.findByNameAndOrganization(name, organization);
	        if (existing.isPresent()) {
	        	AssistantEntity current = existing.get();
	            //System.out.println("Assistant already exists for this organization: " + name+" ,hence updating it instead creating.");
	            return updateAssistant(current,name,organization,systemPromptId,instructions,model);
	        }
	        else {

	        	//System.out.println("Creating new Assistant");
	        	
	        	String safeInstructions = instructions
	        		    .replace("\\", "\\\\")
	        		    .replace("\"", "\\\"")
	        		    .replace("\n", "\\n")
	        		    .replace("\r", "\\r");

	        	
	        	String payload = String.format(
	        		    "{" +
	        		        "\"name\":\"%s\"," +
	        		        "\"instructions\":\"%s\"," +
	        		        "\"model\":\"%s\"," +
	        		        "\"metadata\":{" +
	        		            "\"organization\":\"%s\"," +
	        		            "\"systemPromptId\":\"%s\"" +
	        		        "}" +
	        		    "}",
	        		    name, safeInstructions, model, organization, systemPromptId
	        		);

		        Request request = new Request.Builder()
		                .url(BASE_URL + "/assistants")
		                .post(RequestBody.create(MediaType.get("application/json"),payload))
		                .addHeader("Authorization", "Bearer " + openAiApiKey)
		                .addHeader("OpenAI-Beta", "assistants=v2")
		                .build();

		        try (Response response = client.newCall(request).execute()) {
		        	String respBody =  new OkHttpResponseFunctions().extractStringFromResponseAndCloseBuffer(response);
		            AssistantResponse resp = mapper.readValue(respBody, AssistantResponse.class);

		            AssistantEntity entity = new AssistantEntity();
		            entity.setAssistantId(resp.getId());
		            entity.setName(resp.getName());
		            entity.setModel(resp.getModel());
		            entity.setOrganization(organization);
		            entity.setSystemPromptId(systemPromptId);

		            entity = repository.save(entity);
		            
		            //System.out.println("Update in-memory cache");
		            PromptAndAssistantDataDTO dto = new PromptAndAssistantDataDTO();
		            dto.setAction("update");
		            dto.setOrganization(organization);
		            dto.setAssistantEntity(entity);
		            PromptAndAssistantData.workWithAllAssistants(dto);
		            
		            //System.out.println("Assistant saved locally with ID: " + entity.getId());
		            return entity;
		        }
	        }

	    }

	    // 2 Update Assistant
	    public AssistantEntity updateAssistant(AssistantEntity current,String name, String organization, String systemPromptId,String newInstructions, String newModel) throws Exception {
	    	//System.out.println("updateAssistant");
	    	AssistantEntity existing = null;
	    	if(current == null) {
	    	existing = repository.findByNameAndOrganization(name, organization)
	                .orElseThrow(() -> new IllegalStateException("Assistant not found for organization: " + organization));
	    	}
	    	else {
	    		existing = current;
	    	}
	    	
	    	String safeInstructions = newInstructions
        		    .replace("\\", "\\\\")
        		    .replace("\"", "\\\"")
        		    .replace("\n", "\\n")
        		    .replace("\r", "\\r");
	    	

	        String payload = String.format(
	        	    "{" +
	        	        "\"name\":\"%s\"," +
	        	        "\"instructions\":\"%s\"," +
	        	        "\"model\":\"%s\"," +
	        	        "\"metadata\":{" +
	        	            "\"organization\":\"%s\"," +
	        	            "\"systemPromptId\":\"%s\"" +
	        	        "}" +
	        	    "}",
	        	    name, safeInstructions, newModel, organization, systemPromptId
	        	);

	        
	        Request request = new Request.Builder()
	                .url(BASE_URL + "/assistants/" + existing.getAssistantId())
	                .post(RequestBody.create(MediaType.get("application/json"),payload))
	                .addHeader("Authorization", "Bearer " + openAiApiKey)
	                .addHeader("OpenAI-Beta", "assistants=v2")
	                .build();

	        try (Response response = client.newCall(request).execute()) {
	        	try {
	        		if (!response.isSuccessful()) throw new RuntimeException("Failed to update assistant");
	        	}
	        	catch(Exception e) {
	        		throw e;
	        	}
	        	finally {
	        		response.close();
	        	}
	            existing.setModel(newModel);
	            existing = repository.save(existing);
	            
	            // Update in-memory cache
	            //System.out.println("Update in-memory cache");
	            PromptAndAssistantDataDTO dto = new PromptAndAssistantDataDTO();
	            dto.setAction("update");
	            dto.setOrganization(organization);
	            dto.setAssistantEntity(existing);
	            PromptAndAssistantData.workWithAllAssistants(dto);
	            
	            //System.out.println("Assistant updated successfully for " + organization);
	            return existing;
	        }
	    }

	    // 3 Delete Assistant
	    public String deleteAssistant(String name, String organization) throws Exception {
	    	//System.out.println("deleteAssistant");
	    	AssistantEntity entity = repository.findByNameAndOrganization(name, organization)
	                .orElseThrow(() -> new IllegalStateException("Assistant not found for organization: " + organization));

	        Request request = new Request.Builder()
	                .url(BASE_URL + "/assistants/" + entity.getAssistantId())
	                .delete()
	                .addHeader("Authorization", "Bearer " + openAiApiKey)
	                .addHeader("OpenAI-Beta", "assistants=v2")
	                .build();

	        try (Response response = client.newCall(request).execute()) {
	        	
	        	try {
	        		if (!response.isSuccessful()) throw new RuntimeException("Failed to delete assistant");
	        	}
	        	catch(Exception e) {
	        		throw e;
	        	}
	        	finally {
	        		response.close();
	        	}
	        	
	            repository.delete(entity);
	            // Remove from in-memory cache
	            //System.out.println("Update in-memory cache");
	            PromptAndAssistantDataDTO dto = new PromptAndAssistantDataDTO();
	            dto.setAction("delete");
	            dto.setOrganization(organization);
	            dto.setAssistantEntity(entity); // Pass the assistant to delete
	            PromptAndAssistantData.workWithAllAssistants(dto);
	            
	            //System.out.println("Assistant deleted for org: " + organization);
	            return "Deleted assistant for org: " + organization;
	        }
	    }

	    // 4 Create Thread
	    public ThreadResponse createThread() throws Exception {
	        Request request = new Request.Builder()
	                .url(BASE_URL + "/threads")
	                .post(RequestBody.create(MediaType.get("application/json"),""))
	                .addHeader("Authorization", "Bearer " + openAiApiKey)
	                .addHeader("OpenAI-Beta", "assistants=v2")
	                .build();

	        try (Response response = client.newCall(request).execute()) {
	        	String respBody =  new OkHttpResponseFunctions().extractStringFromResponseAndCloseBuffer(response);
	            return mapper.readValue(respBody, ThreadResponse.class);
	        }
	    }

	  
//	    // 5 Add Message & Stream Response
//	    public String addMessageAndStream(String organization, String assistantId, String threadId, String userMessage) throws Exception {
//	        //System.out.println("=== addMessageAndStream started ===");
//	        //System.out.println("Organization: " + organization + ", AssistantId: " + assistantId + ", ThreadId: " + threadId);
//	        //System.out.println("User Message: " + userMessage);
//
//	        String msgPayload = String.format("{\"role\":\"user\",\"content\":\"%s\"}", userMessage);
//	        //System.out.println("Message Payload: " + msgPayload);
//
//	        Request msgRequest = new Request.Builder()
//	                .url(BASE_URL + "/threads/" + threadId + "/messages")
//	                .post(RequestBody.create(MediaType.get("application/json"), msgPayload))
//	                .addHeader("Authorization", "Bearer " + openAiApiKey)
//	                .addHeader("OpenAI-Beta", "assistants=v2")
//	                .build();
//
//	        try (Response msgResp = client.newCall(msgRequest).execute()) {
//	            //System.out.println("Message request executed. Response code: " + msgResp.code());
//	        } catch (Exception e) {
//	            System.err.println("Error sending message: " + e.getMessage());
//	            throw e;
//	        }
//
//	        String runPayload = String.format("{\"assistant_id\":\"%s\",\"stream\":true}", assistantId);
//	        //System.out.println("Run Payload: " + runPayload);
//
//	        Request runRequest = new Request.Builder()
//	                .url(BASE_URL + "/threads/" + threadId + "/runs")
//	                .post(RequestBody.create(MediaType.get("application/json"), runPayload))
//	                .addHeader("Authorization", "Bearer " + openAiApiKey)
//	                .addHeader("OpenAI-Beta", "assistants=v2")
//	                .build();
//
//	        StringBuilder fullResponse = new StringBuilder();
//	        BufferedReader reader = null;
//	        Response response = null;
//
//	        try {
//	        	
//	            response = streamingClient.newCall(runRequest).execute();
//	            
//	            if (!response.isSuccessful()) {
//	                String err = response.body() != null ? response.body().string() : "";
//	                throw new IOException("Streaming request failed: HTTP " + response.code() + " body=" + err);
//	            }
//
//	            //System.out.println("Streaming request executed. Response code: " + response.code());
//
//	            reader = new BufferedReader(
//	                    new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8)
//	            );
//
//	            String line = "";
//	            while ((line = reader.readLine()) != null) {
//	                if (!line.trim().isEmpty()) {
//	                    ////System.out.println("Event line received: " + line);
//	                    try {
//	                    	if (line.startsWith("data: ")) {
//	                    	    String jsonPart = line.substring(6).trim();
//
//	                    	    // IMPORTANT: SSE stream termination
//	                    	    if ("[DONE]".equals(jsonPart)) {
//	                    	        break;
//	                    	    }
//
//	                    	    JsonNode node = mapper.readTree(jsonPart);
//
//	                            ////System.out.println("Parsed JSON node: " + node.toString());
//
//	                            String finalJson = extractFinalJson(node);
//	                            if (finalJson != null) {
//	                                //System.out.println("Final JSON extracted: " + finalJson);
//	                                if (fullResponse.length() == 0) {
//	                                    fullResponse.append(finalJson);
//	                                    //System.out.println("FullResponse updated.");
//	                                }
//	                            } else {
//	                                //System.out.println("No valid JSON in this node.");
//	                            }
//	                        }
//	                    } catch (Exception e) {
//	                        System.err.println("Error processing line: " + e.getMessage());
//	                    }
//	                }
//	            }
//
//	        } catch (IOException e) {
//	            System.err.println("Error during streaming: " + e.getMessage());
//	            e.printStackTrace();
//	        } finally {
//	            if (reader != null) {
//	                reader.close();
//	                //System.out.println("Reader closed.");
//	            }
//	            if (response != null) {
//	                response.close();
//	                //System.out.println("Response closed.");
//	            }
//	        }
//
//	        //System.out.println("Final Response: " + fullResponse.toString());
//	        //System.out.println("=== addMessageAndStream finished ===");
//	        return fullResponse.toString();
//	    }

	    public String extractFinalJson(JsonNode node) {
	        try {
	            // Case 1: your existing logic
	            String v1 = extractFromNodeIfCompleted(node);
	            if (v1 != null) return v1;

	            // Case 2: sometimes wrapped inside "data"
	            if (node.has("data")) {
	                String v2 = extractFromNodeIfCompleted(node.get("data"));
	                if (v2 != null) return v2;
	            }
	        } catch (Exception e) {
	            System.err.println("Error in extractFinalJson: " + e.getMessage());
	        }
	        return null;
	    }

	    private String extractFromNodeIfCompleted(JsonNode n) throws Exception {
	        if (n == null) return null;
	        if (n.has("status") && "completed".equals(n.get("status").asText())) {
	            if (n.has("content") && n.get("content").isArray()) {
	                for (JsonNode contentNode : n.get("content")) {
	                    if (contentNode.has("type") && "text".equals(contentNode.get("type").asText())) {
	                        JsonNode textNode = contentNode.get("text");
	                        if (textNode != null && textNode.has("value")) {
	                            String value = textNode.get("value").asText();
	                            try {
	                                return mapper.readTree(value).toString(); // must be JSON
	                            } catch (Exception ignore) {
	                                return null;
	                            }
	                        }
	                    }
	                }
	            }
	        }
	        return null;
	    }

	    
	    // -------------------------
	    // 6 Find a thread by ID
	    // -------------------------
	    public JsonNode findThread(String threadId) {
	        Request request = new Request.Builder()
	                .url(BASE_URL + "/threads/" + threadId)
	                .get()
	                .addHeader("Authorization", "Bearer " + openAiApiKey)
	                .addHeader("OpenAI-Beta", "assistants=v2")
	                .build();

	        try (Response response = client.newCall(request).execute()) {
	        	try {
		            if (response.code() == 200) {
		                String body = response.body().string();
		                return mapper.readTree(body);
		            } else if (response.code() == 404) {
		                //System.out.println("Thread not found: " + threadId);
		                return null;
		            } else {
		                //System.out.println("Unexpected response code: " + response.code());
		                return null;
		            }
	        	}
	        	catch(Exception e) {
	        		throw e;
	        	}
		        finally {
		        	response.close();
		        }
	        } catch (IOException e) {
	            System.err.println("Error finding thread: " + e.getMessage());
	            return null;
	        }

	    }

	    // -------------------------
	    // 7 Delete a thread by ID
	    // -------------------------
	    public boolean deleteThread(String threadId) {
	        Request request = new Request.Builder()
	                .url(BASE_URL + "/threads/" + threadId)
	                .delete()
	                .addHeader("Authorization", "Bearer " + openAiApiKey)
	                .addHeader("OpenAI-Beta", "assistants=v2")
	                .build();

	        try (Response response = client.newCall(request).execute()) {
	        	try {
			            if (response.isSuccessful()) {
			                //System.out.println("Thread deleted successfully: " + threadId);
			                return true;
			            } else {
			                //System.out.println("Failed to delete thread. Code: " + response.code());
			                return false;
			            }
	        	}
	        	catch(Exception e) {
	        		throw e;
	        	}
		        finally {
		        	response.close();
		        }
	        } catch (IOException e) {
	            System.err.println("Error deleting thread: " + e.getMessage());
	            return false;
	        }
	    }
	    
	    // -------------------------
	    // 8 Delete multiple threads
	    // -------------------------
	    public void deleteMultipleThreads(List<String> threadIds) {
	        for (String threadId : threadIds) {
	            boolean success = deleteThread(threadId);
	            if (!success) {
	                //System.out.println("Failed to delete thread: " + threadId);
	            }

	            // Wait 1.5 seconds before next deletion
	            try {
	                Thread.sleep(1500); // 1500 milliseconds = 1.5 seconds
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt(); // restore interrupted status
	                System.err.println("Thread sleep interrupted: " + e.getMessage());
	            }
	        }
	    }

	    // -------------------------
	    // 9 List all threads for a given assistant
	    // -------------------------
	    public List<JsonNode> listThreadsByAssistant(String assistantId) {
	        List<JsonNode> threads = new ArrayList<>();

	        // You can optionally paginate if many threads exist
	        Request request = new Request.Builder()
	                .url(BASE_URL + "/threads?assistant=" + assistantId)
	                .get()
	                .addHeader("Authorization", "Bearer " + openAiApiKey)
	                .addHeader("OpenAI-Beta", "assistants=v2")
	                .build();

	        try (Response response = client.newCall(request).execute()) {
	        	try {
		            if (response.isSuccessful()) {
		                String body = response.body().string();
		                JsonNode root = mapper.readTree(body);
	
		                // OpenAI returns an array of thread objects
		                if (root.has("data") && root.get("data").isArray()) {
		                    for (JsonNode thread : root.get("data")) {
		                        threads.add(thread);
		                    }
		                }
		            } else {
		                //System.out.println("Failed to list threads. Code: " + response.code());
		            }
		        }
	        	catch(Exception e) {
	        		throw e;
	        	}
	        finally {
	        	response.close();
	        }
	        } catch (IOException e) {
	            System.err.println("Error listing threads: " + e.getMessage());
	        }

	        return threads;
	    }
	    
	    public AiInterfaceOutputDto parseStringToAiInterfaceOutputDto(String organization, String thread, String assistantId, String jsonString) {
	        try {
	        	jsonString=cleanJsonInputFromLLM(jsonString);
	        	AiInterfaceOutputDto dto = mapper.readValue(jsonString, AiInterfaceOutputDto.class);
	            //System.out.println("Parsed DTO: " + dto);
	            return dto;
	        } catch (Exception e) {
	            System.err.println("JSON parse failed for Assistant: " + assistantId);
	            aIErrorLoggingService.logError(
	                    organization,
	                    thread,
	                    assistantId,
	                    e.getMessage(),
	                    jsonString
	            );
	            throw new RuntimeException("Invalid JSON format from Assistant", e);
	        }
	    }
	    
	    public AiPropertyInventoryVerificationOutputDto parseStringToAiPropertyInventoryVerificationOutputDto(String organization, String thread, String assistantId, String jsonString) {
	        try {
	        	jsonString=cleanJsonInputFromLLM(jsonString);
	        	AiPropertyInventoryVerificationOutputDto dto = mapper.readValue(jsonString, AiPropertyInventoryVerificationOutputDto.class);
	            //System.out.println("Parsed DTO: " + dto);
	            return dto;
	        } catch (Exception e) {
	            System.err.println("JSON parse failed for Assistant: " + assistantId);
	            aIErrorLoggingService.logError(
	                    organization,
	                    thread,
	                    assistantId,
	                    e.getMessage(),
	                    jsonString
	            );
	            throw new RuntimeException("Invalid JSON format from Assistant", e);
	        }
	    }
	    public LanguageAndHeuristicCheckResponse parseStringToLanguageAndHeuristicCheckResponse(String organization, String thread, String assistantId, String jsonString) {
	        try {

	        	jsonString=cleanJsonInputFromLLM(jsonString);
	        	LanguageAndHeuristicCheckResponse dto = mapper.readValue(jsonString, LanguageAndHeuristicCheckResponse.class);
	            //System.out.println("Parsed DTO: " + dto);
	            return dto;
	        } catch (Exception e) {
	            System.err.println("JSON parse failed for language Assistant: " + assistantId);
	            aIErrorLoggingService.logError(
	                    organization,
	                    thread,
	                    assistantId,
	                    e.getMessage(),
	                    jsonString
	            );
	            throw new RuntimeException("Invalid JSON format from language Assistant", e);
	        }
	    }
	    
	    public String cleanJsonInputFromLLM(String jsonString) {
	        if (jsonString == null || jsonString.isBlank()) {
	            System.err.println("[cleanJsonInputFromLLM] Input is null or empty.");
	            return "{}"; // return empty JSON as safe fallback
	        }

	        String original = jsonString;

	        try {
	            // 1. Strip markdown code fences (```json ... ``` or ``` ... ```)
	            jsonString = jsonString
	                    .replaceAll("(?s)```json\\s*", "")  // remove ```json at start
	                    .replaceAll("(?s)```\\s*", "")      // remove closing ```
	                    .trim();

	            // 2. Remove wrapping quotes if present
	            if (jsonString.startsWith("\"") && jsonString.endsWith("\"")) {
	                jsonString = jsonString.substring(1, jsonString.length() - 1)
	                                       .replace("\\\"", "\"");  // unescape quotes
	            }

	            // 3. Remove any leading/trailing text outside JSON braces
	            int firstBrace = jsonString.indexOf("{");
	            int lastBrace = jsonString.lastIndexOf("}");
	            if (firstBrace >= 0 && lastBrace >= 0 && lastBrace > firstBrace) {
	                jsonString = jsonString.substring(firstBrace, lastBrace + 1);
	            } else {
	                // no valid braces found, return empty JSON and log
	                System.err.println("[cleanJsonInputFromLLM] No valid JSON braces found. Returning {}");
	                return "{}";
	            }

	            // Optional: Trim again in case whitespace remained
	            jsonString = jsonString.trim();

	            if (!jsonString.equals(original)) {
	                //System.out.println("[cleanJsonInputFromLLM] Cleaned JSON from LLM output.");
	            }

	            return jsonString;

	        } catch (Exception e) {
	            System.err.println("[cleanJsonInputFromLLM] Exception while cleaning JSON: " + e.getMessage());
	            return "{}";
	        }
	    }


	    public String sanitize(String input) {
	        if (input == null) return "";

	        // Ensure UTF-8
	        byte[] utf8 = input.getBytes(StandardCharsets.UTF_8);
	        String safe = new String(utf8, StandardCharsets.UTF_8);

	        // Remove control characters (non-printable)
	        safe = safe.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

	        // Trim
	        safe = safe.trim();

	        return safe;
	    }
	    
}

