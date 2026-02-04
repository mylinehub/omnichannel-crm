package com.mylinehub.crm.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mapstruct.Context;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mylinehub.crm.TaskScheduler.HardInsertChatHistoryRunnable;
import com.mylinehub.crm.data.TrackedSchduledJobs;
import com.mylinehub.crm.data.current.CurrentConversations;
import com.mylinehub.crm.entity.ChatHistory;
import com.mylinehub.crm.entity.dto.AllChatEmployeeDTO;
import com.mylinehub.crm.entity.dto.ChatKeyValueDTO;
import com.mylinehub.crm.entity.dto.ChatKeyValueListDTO;
import com.mylinehub.crm.mapper.AllChatEmployeeMapper;
import com.mylinehub.crm.repository.ChatHistoryRepository;
import com.mylinehub.crm.repository.EmployeeRepository;

import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */

@Service
@AllArgsConstructor
public class ChatHistoryService implements CurrentTimeInterface{
	
	  /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final ChatHistoryRepository chatHistoryRepository;
    private SchedulerService schedulerService;
    private final EmployeeRepository employeeRepository;
    private final AllChatEmployeeMapper allChatEmployeeMapper;
//    private final ErrorRepository errorRepository;
    private ApplicationContext applicationContext;
    
    public List<AllChatEmployeeDTO>  getAllChatHistoryByExtensionMainAndOrganization(String extensionMain, String organization)
    {
    	List<AllChatEmployeeDTO> current = new ArrayList<AllChatEmployeeDTO>();
    	
    	List<String> allChatHistoryCandidates;
    	
//    	System.out.println("Finding all candidates from database ");
    	allChatHistoryCandidates = chatHistoryRepository.findByExtensionMainAndOrganizationAndIsDeleted(extensionMain, organization, false);
    	
//    	System.out.println("allChatHistoryCandidates : "+allChatHistoryCandidates.toString());
//    	System.out.println("Got all candidates from database ");
    	
    	List<String> allExtensions = new ArrayList<String>();
    	allChatHistoryCandidates.forEach((extension)->{
    		allExtensions.add(extension.substring( 0, extension.indexOf(",")));
    	});
    	
//    	System.out.println("Added database extensions to list");
//    	System.out.println("allExtensions : "+allExtensions.toString());
    	
    	 LinkedHashMap<String,List<ChatKeyValueDTO>> currentConversations = CurrentConversations.workOnCurrentMemeoryConversation(extensionMain,null, null, "get");
	        
    	//add code to find if we have anything in currentConversation for this user. If yes find if we have past history, if yes move it to top or else add a new value to top of list
    	if(currentConversations != null)
    	{
//    		System.out.println("CurrentConversations is not null and it has extensionMain");
    		  Set<String> setKeys = currentConversations.keySet();
    	      List<String> listKeys = new ArrayList<String>(setKeys);
    	      ListIterator<String> iterator = listKeys.listIterator( listKeys.size() );

    	      while(iterator.hasPrevious()){
    	         String currentExtension = iterator.previous();
                 if(allExtensions.contains(currentExtension))
                 {
                 	allExtensions.remove(currentExtension);
                 	allExtensions.add(0,currentExtension);
                 }
                 else
                 {
                 	allExtensions.add(0,currentExtension);
                 }
                 
    	      }
    	}
    	else
    	{
//    		System.out.println("CurrentConversations is null");
    	}
    	
//    	System.out.println("Joined by comma extensions");
//    	String input = String.join(",", allExtensions);
//    	System.out.println("Input : "+input);
    	
    	List<String> data = new ArrayList<String>();
    	data.add(extensionMain);
    	data.add(organization);
    	
    	List<ChatHistory> databaseData = chatHistoryRepository.getAllByExtensionMainAndOrganizationAndIsDeleted(extensionMain, organization, false);
    	System.out.println("DEBUG: Loaded " + databaseData.size() + " ChatHistory records from DB for extensionMain=" + extensionMain + ", organization=" + organization);

    	Map<String, ChatHistory> chatHistoryMapByWith = databaseData.stream()
    	    .collect(Collectors.toMap(
    	        ChatHistory::getExtensionWith,
    	        ch -> ch,
    	        (existing, replacement) -> {
    	            System.out.println("DEBUG: Duplicate key for extensionWith = " + existing.getExtensionWith() + ", keeping existing");
    	            return existing;
    	        }
    	    ));
    	System.out.println("DEBUG: Created chatHistoryMapByWith with " + chatHistoryMapByWith.size() + " entries");

    	List<Object[]> result = chatHistoryRepository.findLastReadIndexGroupedByExtensionWith(extensionMain, organization);
    	System.out.println("DEBUG: Loaded " + result.size() + " records of lastReadIndex grouped by extensionWith from DB");

    	Map<String, Integer> lastReadIndexMap = result.stream()
    	    .collect(Collectors.toMap(
    	        row -> (String) row[0],
    	        row -> {
    	            Integer val = ((Number) row[1]).intValue();
    	            System.out.println("DEBUG: lastReadIndexMap key = " + row[0] + ", value = " + val);
    	            return val;
    	        }
    	    ));
    	System.out.println("DEBUG: Created lastReadIndexMap with " + lastReadIndexMap.size() + " entries");

    	current = employeeRepository.findAllByExtensionsAndOrganization(allExtensions, organization)
    	    .stream()
    	    .map(item -> {
    	        ChatHistory chatHistory = chatHistoryMapByWith.get(item.getExtension());
    	        Integer lastReadIndex = lastReadIndexMap.get(item.getExtension());

    	        System.out.println("DEBUG: Mapping employee with extension = " + item.getExtension() +
    	                           ", ChatHistory found = " + (chatHistory != null) +
    	                           ", lastReadIndex = " + lastReadIndex);

    	        return allChatEmployeeMapper.mapEmployeeToChatInfoDto(item, data, chatHistory, lastReadIndexMap);
    	    })
    	    .collect(Collectors.toList());

    	System.out.println("DEBUG: Mapped " + current.size() + " employees to chat DTOs");

    	
//    	System.out.println("Fetched data from employee repo and passed through mapper");
//    	System.out.println("current : "+current.toString());
    	
    	return current;
    }
    
    
    public Integer  updateLastReadIndexByExtensionMainAndExtensionWithAndOrganizationAndIsDeleted(String extensionMain, String extensionWith, String organization,int lastReadIndex)
    {  	
        int current = 0;
        try
        {
        	chatHistoryRepository.updateLastReadIndexByExtensionMainAndExtensionWithAndOrganizationAndIsDeleted(extensionMain, extensionWith,organization, false,lastReadIndex);
        	current = 1;
        	
        }
        catch(Exception e)
        {
        	current = 0;
        	e.printStackTrace();
        	throw e;
        }
        return current;
    }
    
    
    public ChatHistory  getAllChatHistoryByExtensionMainAndExtensionWithAndOrganization(String extensionMain, String extensionWith, String organization) throws JsonMappingException, JsonProcessingException
    {  	
        ChatHistory current;
        List<ChatHistory> databaseData;
        
//        System.out.println("Finding current chat history");
        
        try
        {
//        	 System.out.println("Service Extension From : "+extensionMain);
//    		 System.out.println("Service Extension To : "+extensionWith);
//    		 System.out.println("Service Organization : "+organization);
    		
        	 databaseData = chatHistoryRepository.findByExtensionMainAndExtensionWithAndOrganizationAndIsDeleted(extensionMain, extensionWith,organization, false);
        	 if (databaseData.size() != 0)
         	 {
         		current = databaseData.get(0);
         	 }
         	 else
         	 {
         		current = null;
         	 }
//             System.out.println("Current Chat History From Databse");
//             System.out.println(current);
//             System.out.println("CurrentConversations.currentConversations");
//             System.out.println(CurrentConversations.currentConversations);
//             System.out.println("extensionMain");
//             System.out.println(extensionMain);
//             System.out.println("CurrentConversations.currentConversations.get(extensionMain)");
//             System.out.println(CurrentConversations.currentConversations.get(extensionMain));
             
        	 LinkedHashMap<String,List<ChatKeyValueDTO>> currentConversations = CurrentConversations.workOnCurrentMemeoryConversation(extensionMain,null, null, "get");
 	        
             if(currentConversations != null)
         	{
//             	System.out.println("CurrentConversations.currentConversations is not null");
         		if(currentConversations.get(extensionWith) != null)
             	{
         			try
         			{
         				
//         				System.out.println("CurrentConversations.currentConversations Main & With is not null");
             			List<ChatKeyValueDTO> currentChatHistory = currentConversations.get(extensionWith);
             			
//             			System.out.println("Fetched list of current conversation");
             			List<ChatKeyValueDTO> databaseChatHistoryList;
             			List<ChatKeyValueDTO> newChatHistoryList;
             			
             			if (current !=null)
             			{
//             				System.out.println("Fetched list of history conversation");
//             				System.out.println("Converted string chat to object");
             				databaseChatHistoryList = current.getChats().getAllChats();
//                 			System.out.println("Combining two list");
             				newChatHistoryList = Stream.of(databaseChatHistoryList,currentChatHistory)
                                     .flatMap(Collection::stream)
                                     .collect(Collectors.toList());
//             				System.out.println("After combining two list");
             				ChatKeyValueListDTO chatWrapper = new ChatKeyValueListDTO();
             				chatWrapper.setAllChats(newChatHistoryList);
                 			current.setChats(chatWrapper);
//                 			System.out.println("Combined chat added to Chat History Object");
             			}
             			else
             			{
//             				System.out.println("Current Chat History is null , hence putting only current variable data");
             				current = new ChatHistory();
             				current.setExtensionMain(extensionMain);
             				current.setExtensionWith(extensionWith);
             				current.setOrganization(organization);
             				current.setLastUpdateTime(new Date());
             				current.setId(1L);
             				current.setCreatedOn(Instant.now());
//             				System.out.println("Putting value to current");
             				ChatKeyValueListDTO chatWrapper = new ChatKeyValueListDTO();
             				chatWrapper.setAllChats(currentChatHistory);
                 			current.setChats(chatWrapper);
             			}
//             			List<ChatKeyValueDTO> participantJsonList = mapper.readValue(databaseChatHistory, new TypeReference<List<ChatKeyValueDTO>>(){});
             			
         			}
         			catch(Exception e)
         			{
//         				System.out.println("Error while getting chat data : "+e.getMessage());
//         				e.printStackTrace();
         				
         				throw e;
         			}
         			
              	}
         		else
             	{
         			//Do Nothing
             	}
         	}
         	else
         	{
         		//Do Nothing
         	}
             
//             System.out.println("**************************************** Current Chat History Total ****************************************");
//             System.out.println(current);
        }
        catch(Exception e)
        {
//        	System.out.println("Error while getting chat data : "+e.getMessage());
//        	e.printStackTrace();
        	throw e;
        }
       
        return current;
    }
    
    
    public int softAppendChatHistoryByExtensionMainAndExtensionWithAndOrganization(String extensionMain, String extensionWith, String organization, List<ChatKeyValueDTO> chat)
    {  	
//    	System.out.println("softAppendChatHistoryByExtensionMainAndExtensionWithAndOrganization");
    	//Set Current conversation
    	int result = 0;

    		//First check Scheduled job so as it is not trigger while below is executed
        	if(schedulerService.findIfScheduledTask("CC"+extensionMain+extensionWith))
        	{
//        		System.out.println("removing scheduled service");
        		schedulerService.removeScheduledTask("CC"+extensionMain+extensionWith);
        	}
        	else
        	{
//        		System.out.println("service was not scheduled");
        	}
        	
//        	System.out.println("Scheduling hard insert");
        	//Create new scheduled job as previous one is removed
        	String jobId = TrackedSchduledJobs.chatHistory+extensionMain+extensionWith;
			schedulerService.removeScheduledTask(jobId);
			
        	HardInsertChatHistoryRunnable hardInsertChatHistoryRunnable = new HardInsertChatHistoryRunnable();
        	hardInsertChatHistoryRunnable.setOrganization(organization);
        	hardInsertChatHistoryRunnable.setExtensionMain(extensionMain);
        	hardInsertChatHistoryRunnable.setExtensionWith(extensionWith);
        	hardInsertChatHistoryRunnable.setApplicationContext(applicationContext);
        	hardInsertChatHistoryRunnable.setJobId(organization);
        	hardInsertChatHistoryRunnable.setJobId(jobId);
        	schedulerService.removeIfExistsAndScheduleATaskAfterXSeconds(jobId, hardInsertChatHistoryRunnable, 300);
        	
        	LinkedHashMap<String,List<ChatKeyValueDTO>> currentConversations = CurrentConversations.workOnCurrentMemeoryConversation(extensionMain,null, null, "get");
 	        
        	if(currentConversations != null)
        	{
//        		System.out.println("CurrentConversations.currentConversations is not null for extension main");
        		if(currentConversations.get(extensionWith) != null)
            	{
//        			System.out.println("Extension With is not null while soft append");
        			List<ChatKeyValueDTO> current = currentConversations.get(extensionWith);
        			
//        			System.out.println("Adding new chat to current chat and adding it to current chat");
        			List<ChatKeyValueDTO> newList = Stream.of(current, chat)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
        			
//        			System.out.println("Adding value to current conversation");
        			CurrentConversations.workOnCurrentMemeoryConversation(extensionMain,extensionWith, newList, "update");
             	}
        		else
            	{
//        			System.out.println("Extension With is null while soft append");
        			 CurrentConversations.workOnCurrentMemeoryConversation(extensionMain,extensionWith, chat, "update");
            	}
        	}
        	else
        	{
//        		System.out.println("CurrentConversations.currentConversations is null for extension main");
        		CurrentConversations.workOnCurrentMemeoryConversation(extensionMain,extensionWith, chat, "update");
        	}
    	
        return result;
    }
    
    
    public int  hardAppendChatHistoryByExtensionMainAndExtensionWithAndOrganization(String extensionMain, String extensionWith, String organization) throws JsonProcessingException
    {  	
    	int result = 0;
    	
//    	System.out.println("hardAppendChatHistoryByExtensionMainAndExtensionWithAndOrganization");
//    	System.out.println("extensionMain : "+ extensionMain);
//    	System.out.println("extensionWith : "+ extensionWith);
//    	
//    		System.out.println("insert");
    		
    		LinkedHashMap<String,List<ChatKeyValueDTO>> currentConversations = CurrentConversations.workOnCurrentMemeoryConversation(extensionMain,null, null, "get");
 	        
    		
    		List<ChatKeyValueDTO> chat;
        	if(currentConversations != null)
        	{
//        		System.out.println("CurrentConversations.currentConversations is not null for main");
        		if(currentConversations.get(extensionWith) != null)
            	{
//        			System.out.println("CurrentConversations.currentConversations is not null for with extension");
//        			ObjectMapper objectMapper = new ObjectMapper();
//        			System.out.println("chat string has current chat value now");
//        			chat = objectMapper.writeValueAsString(CurrentConversations.currentConversations.get(extensionMain).get(extensionWith));
        			chat = currentConversations.get(extensionWith);
            	}
        		else
            	{
//        			System.out.println("CurrentConversations.currentConversations is null for with extension");
            		chat = null;
            	}
        	}
        	else
        	{
//        		System.out.println("CurrentConversations.currentConversations is null for main");
        		chat = null;
        	}
        	
//        	System.out.println("Creating chat history object container");
        	List<ChatHistory> databaseData;
        	ChatHistory current;
//        	System.out.println("Finding chat history from database");
        	databaseData  = chatHistoryRepository.findByExtensionMainAndExtensionWithAndOrganizationAndIsDeleted(extensionMain, extensionWith,organization, false); 
        	if (databaseData.size() != 0)
        	{
        		current = databaseData.get(0);
        	}
        	else
        	{
        		current = null;
        	}
        	if(current != null && chat !=null)
        	{
//        		System.out.println("Current chat object is not null and hence appending it via Repo");
//        		result = chatHistoryRepository.appendChatByExtensionMainAndExtensionWithAndOrganizationAndIsDeleted(extensionMain, extensionWith,organization, false,chat,new Date());
        		List<ChatKeyValueDTO> databaseChatHistoryList = current.getChats().getAllChats();
        		List<ChatKeyValueDTO> currentChatHistory = currentConversations.get(extensionWith);
        		List<ChatKeyValueDTO> newChatHistoryList;
        		
//        		System.out.println("***********************************New Chat***********************************");
//        		System.out.println(currentChatHistory);
        		
        		newChatHistoryList = Stream.of(databaseChatHistoryList,currentChatHistory)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        		
        		ChatKeyValueListDTO chatWrapper = new ChatKeyValueListDTO();
 				chatWrapper.setAllChats(newChatHistoryList);
        		current.setChats(chatWrapper);
        		
//        		System.out.println("***********************************All Chat***********************************");
//        		System.out.println(current);
        		current.setLastUpdateTime(new Date());
        		chatHistoryRepository.save(current);
        		result = 1;
        		
        	}
        	else if(chat != null)
        	{

//        		System.out.println("Current chat object is null , hence creating and inserting it into database as new ");
        		//Creating chat Wrapper
        		ChatKeyValueListDTO chatWrapper = new ChatKeyValueListDTO();
 				chatWrapper.setAllChats(chat);
        		//Add new record
        		ChatHistory toAdd = new ChatHistory();
        		toAdd.setExtensionMain(extensionMain);
        		toAdd.setExtensionWith(extensionWith);
        		toAdd.setOrganization(organization);
 				toAdd.setChats(chatWrapper);
        		toAdd.setLastUpdateTime(new Date());
        		
//        		System.out.println("***********************************chat***********************************");
//        		System.out.println(toAdd);
        		toAdd.setLastUpdateTime(new Date());
        		chatHistoryRepository.save(toAdd);
        		result = 1;
        	} 
        	else
        	{
        		//Do Nothing as chat is null. When there is no chat to add then why should be wait for anything.
        	}
        	
//        	System.out.println("Removing current chat");
        	//Removing previous current Chat Value
        	if(currentConversations != null)
        	{
        		if(currentConversations.get(extensionWith) != null)
            	{
        			CurrentConversations.workOnCurrentMemeoryConversation(extensionMain,extensionWith, null, "remove");
            	}
        	}
    	
        return result;
    }
    
    
    public int  deleteChatHistoryByExtensionMainAndExtensionWithAndOrganization(String extensionMain, String extensionWith, String organization)
    {  	
    	int result;
    	
    	try
    	{
    		LinkedHashMap<String,List<ChatKeyValueDTO>> currentConversations = CurrentConversations.workOnCurrentMemeoryConversation(extensionMain,null, null, "get");
 	        
        	if(currentConversations.get(extensionWith)!=null)
        	{
        		CurrentConversations.workOnCurrentMemeoryConversation(extensionMain,extensionWith, null, "remove");
        	}
        	
        	chatHistoryRepository.softDeleteByExtensionMainAndExtensionWithAndOrganizationAndIsDeleted(extensionMain, extensionWith, organization, false);
        	result = 1;
    	}
    	catch(Exception e)
    	{
    		 result = 0;
    		 System.out.println(e.getMessage());
    		 throw e;
    	}
    	
        return result;
    }
    
    
    public int  deleteAllChatHistoryByExtensionMainAndOrganization(String extensionMain, String organization)
    {  	
    	int result;
    	try
    	{
    		LinkedHashMap<String,List<ChatKeyValueDTO>> currentConversations = CurrentConversations.workOnCurrentMemeoryConversation(extensionMain,null, null, "get");
	        
    		if(currentConversations!=null)
        	{
        		CurrentConversations.workOnCurrentMemeoryConversation(extensionMain,null, null, "delete");
        	}
        	chatHistoryRepository.softDeleteAllByExtensionMainAndOrganizationAndIsDeleted(extensionMain, organization, false);
        	result = 1;
    	}
    	catch(Exception e)
    	{
    		 System.out.println(e.getMessage());
    		 result = 0;
    		 throw e;
    	}
    	
    
        return result;
    }
	
}
