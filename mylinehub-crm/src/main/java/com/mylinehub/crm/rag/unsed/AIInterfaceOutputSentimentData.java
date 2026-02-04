//package com.mylinehub.crm.rag.data;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.mylinehub.crm.rag.data.dto.AiInterfaceOutputParameterDTO;
//import com.mylinehub.crm.rag.model.AiSentimentsEnitity;
//import com.mylinehub.crm.rag.repository.AiSentimentsRepository;
//import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerDataDto;
//
//
//public class AIInterfaceOutputSentimentData {
//
//	
//    private static List<AiSentimentsEnitity> sentimentList = new ArrayList<>();
//
//    public static List<AiSentimentsEnitity> workWithSentimentListData(AiInterfaceOutputParameterDTO dto) {
//    	List<AiSentimentsEnitity> toReturn = new ArrayList<>();
//
//        try {
//            WhatsAppCustomerDataDto current = null;
//
//            switch (dto.getAction()) {
//		            case "update":
//		            	sentimentList.add(dto.getAiSentimentsEnitity());
//		            	return sentimentList;
//
//    				case "update-to-db": 
//    			
//    					// Step 1: Make a local copy
//    	                List<AiSentimentsEnitity> localCopy = new ArrayList<>(sentimentList);
//
//    	                // Step 2: Clear the original list by resetting it
//    	                sentimentList = new ArrayList<>();
//
//    	                // Step 3: Batch save
//    	                AiSentimentsRepository repository = dto.getAiSentimentsRepository();
//    	                if (repository != null && !localCopy.isEmpty()) {
//    	                    int batchSize = 150;
//    	                    for (int i = 0; i < localCopy.size(); i += batchSize) {
//    	                        int end = Math.min(i + batchSize, localCopy.size());
//    	                        List<AiSentimentsEnitity> batch = localCopy.subList(i, end);
//    	                        repository.saveAll(batch); // Assumes Spring Data JPA
//    	                    }
//    	                }
//
//    	                return localCopy;
//    					
//	                default:
//	                    System.out.println("[WARN] Unknown action: " + dto.getAction());
//	                    break;
//            }
//
//        } catch (Exception e) {
//            System.err.println("[ERROR] Exception in workWithSentimentListData:");
//            e.printStackTrace();
//        }
//
//        return toReturn;
//    }	
//}


