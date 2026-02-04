//package com.mylinehub.crm.rag.taskscheduler;
//
//import com.mylinehub.crm.rag.data.AIInterfaceOutputSentimentData;
//import com.mylinehub.crm.rag.data.dto.AiInterfaceOutputParameterDTO;
//import com.mylinehub.crm.rag.repository.AiSentimentsRepository;
//
//
//import lombok.Data;
//
//@Data
//public class AiSentimentToDBRunnable implements Runnable {
//
//    private String jobId;
//    private AiSentimentsRepository aiSentimentsRepository;
//
//    @Override
//    public void run() {
//        System.out.println("[AiSentimentToDBRunnable] Job started. Job ID: " + jobId);
//        try {
//            
//			AiInterfaceOutputParameterDTO aiInterfaceOutputParameterDTO = new AiInterfaceOutputParameterDTO();
//			aiInterfaceOutputParameterDTO.setAction("update-to-db");
//			aiInterfaceOutputParameterDTO.setAiSentimentsRepository(aiSentimentsRepository);
//
//			AIInterfaceOutputSentimentData.workWithSentimentListData(aiInterfaceOutputParameterDTO);
//			
//
//            System.out.println("[AiSentimentToDBRunnable] Job completed successfully. Job ID: " + jobId);
//        } catch (Exception e) {
//            System.out.println("[AiSentimentToDBRunnable] Exception occurred during job execution. Job ID: " + jobId);
//            e.printStackTrace();
//        }
//    }
//}


