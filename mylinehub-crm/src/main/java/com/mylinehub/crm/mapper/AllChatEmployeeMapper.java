package com.mylinehub.crm.mapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.mylinehub.crm.data.current.CurrentConversations;
import com.mylinehub.crm.entity.ChatHistory;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.AllChatEmployeeDTO;
import com.mylinehub.crm.entity.dto.ChatKeyValueDTO;


@Mapper(componentModel = "spring")
public abstract class AllChatEmployeeMapper {
	
	
	@Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "role", source = "userRole")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "departmentName", source = "department.departmentName")
    @Mapping(target = "extension", source = "extension")
    @Mapping(target = "phonenumber", source = "phonenumber")
	@Mapping(target = "imageType", source = "imageType")
	@Mapping(target = "iconImageData", source = "employee.iconImageData", qualifiedByName = "mapIconImage")
	@Mapping(target = "lastReadIndex", source = "employee", qualifiedByName = "mapEmployeeLastIndexRead")
	@Mapping(target = "badgeText", source = "employee", qualifiedByName = "mapEmployeeBadgeTextRead")
	public abstract AllChatEmployeeDTO mapEmployeeToChatInfoDto(Employee employee,@Context List<String> data, @Context ChatHistory databaseData, @Context Map<String, Integer> lastReadIndexMap);
    
	
	@Named("mapEmployeeBadgeTextRead") 
    int mapEmployeeBatchTextRead(Employee employee,@Context List<String> data, @Context ChatHistory databaseData) throws IOException{
		
		String extensionFrom = data.get(0);
		String organization = data.get(1);
		
//		System.out.println("mapEmployeeBatchTextRead");
//		System.out.println("Extension From : "+extensionFrom);
//		System.out.println("Extension To : "+employee.getExtension());
//		System.out.println("Organization : "+organization);
		
		int toReturn=0;
		
		try
		{

			ChatHistory chatHistory;
			
//			System.out.println("chatHistoryRepository : "+chatHistoryRepository);
//			System.out.println("databaseData size : "+databaseData.size());
			
			if (databaseData != null)
        	{
				chatHistory = databaseData;
//				ObjectMapper mapper = new ObjectMapper();
				int lastReadIndex = chatHistory.getLastReadIndex();
				
//				System.out.println("lastReadIndex : "+lastReadIndex);
				
//				String databaseChatHistory = chatHistory.getChats();
//				List<ChatKeyValueDTO> databaseChatHistoryList = new ArrayList<ChatKeyValueDTO>();
//				databaseChatHistoryList = Arrays.asList(mapper.readValue(databaseChatHistory, ChatKeyValueDTO[].class));

				List<ChatKeyValueDTO> databaseChatHistoryList =  chatHistory.getChats().getAllChats();

		        int chatLength = databaseChatHistoryList.size();
		        
//		        System.out.println("chatLength: "+chatLength);
		        
		        LinkedHashMap<String,List<ChatKeyValueDTO>> currentConversations = CurrentConversations.workOnCurrentMemeoryConversation(extensionFrom,null, null, "get");
		        
		        if(currentConversations != null) 
		        {
		        	if(currentConversations.get(employee.getExtension()) != null)
		        		{
		        			chatLength = chatLength + currentConversations.get(employee.getExtension()).size();
//		        			System.out.println("chatLength after current : "+chatLength);
		        		}
		        }
		        
		        int difference = chatLength - lastReadIndex;
		        
//		        System.out.println("difference : "+difference);
		        
		        if(difference == 0 || difference < 0)
		        {
		        	toReturn = 0;
		        }
		        else
		        {
		        	toReturn = difference;
		        }
		        
        	}
        	else
        	{
        		chatHistory = null;
        	}
			return toReturn;
		}
		catch(Exception e)
		{
//			System.out.println("Error Message : "+e.getLocalizedMessage());
			return 0;
		}
    }
	
	@Named("mapEmployeeLastIndexRead") 
    int mapEmployeeLastIndexRead(Employee employee,@Context Map<String, Integer> lastReadIndexMap) throws IOException{
		try
		{
			 String extension = employee.getExtension();
			 // Debug logs (temporary)
			 System.out.println("DEBUG: Mapping lastReadIndex for extension: " + extension);

			 return lastReadIndexMap.getOrDefault(extension, 0);
		}
		catch(Exception e)
		{
			return 0;
		}
    }
	
	@Named("mapIconImage") 
    byte[] mapEmployeeIconImage(String iconImageData) throws IOException{
		
//		System.out.println("mapEmployeeIconImage : "+iconImageData);
		if(iconImageData != null)
		{
//			System.out.println("iconImageData is not null");
			try
			{
				byte[] image;		
				String uploadIconDirectory = iconImageData.substring(0,iconImageData.lastIndexOf("/"));
//				System.out.println("uploadIconDirectory : "+uploadIconDirectory);
				iconImageData = iconImageData.replace(uploadIconDirectory+"/", "");
//				System.out.println("iconImageData : "+iconImageData);
				image = getImage(uploadIconDirectory, iconImageData);
		        return image;
			}
			catch(Exception e)
			{
				return null;
			}
			
		}
		else
		{
			return null;
		}
    }
	
	// To view an image
	byte[] getImage(String imageDirectory, String imageName) throws IOException {
        Path imagePath = Path.of(imageDirectory, imageName);

        if (Files.exists(imagePath)) {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            return imageBytes;
        } else {
            return null; // Handle missing images
        }
    }
	 
}
