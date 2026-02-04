package com.mylinehub.crm.service;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.dto.NotificationDTO;
import com.mylinehub.crm.mapper.NotificationMapper;
import com.mylinehub.crm.repository.NotificationRepository;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class NotificationService implements CurrentTimeInterface{
	
	  /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    
    public int  saveNotificationsByExtensionAndOrganizationAndIsDeleted(List<Notification> entities)
    {
    	int current =  0;
    	try {
    		notificationRepository.saveAll(entities);
    		current =  1;
    	}
    	catch(Exception e)
    	{
    		current =  0;
    	}
    	
    	return current;
    }
    
    public List<NotificationDTO>  findAllByExtensionAndOrganizationAndIsDeleted(String forExtension, String organization)
    {
    	List<NotificationDTO> current = new ArrayList<NotificationDTO>();
    	current = notificationRepository.findByForExtensionAndOrganizationAndIsDeletedOrderByCreationDateDesc(forExtension, organization,false)
                .stream()
                .map(notificationMapper::mapNotificationToDTO)
                .collect(Collectors.toList());
    	return current;
    }
  
    
    public int  deleteAllByForExtensionAndOrganizationAndIsDeleted(String forExtension, String organization)
    {  	
    	int result;
    	result = notificationRepository.softDeleteAllByForExtensionAndOrganizationAndIsDeleted(forExtension, organization, false);
        return result;
    }
    
    
    public int  deleteByIdAndForExtensionAndExtensionWithAndOrganizationAndIsDeleted(List<Long> ids, String forExtension, String organization)
    {  	
    	int result;
//        String allDeletedIds = "";
//    	boolean first = true;
//    	
//    	// Traversing the ArrayList
//        for (Long id : ids) {
// 
//            // Each element in ArrayList is appended
//            // followed by comma
//        	if(first)
//        	{
//        		allDeletedIds = String.valueOf(id);
//        		first = false;
//        	}
//        	else
//        	{
//        		allDeletedIds = allDeletedIds + ","+ String.valueOf(id);
//        	}
//        }
// 
//        
////    	ids.forEach(
////                (id) -> { 
////                	
////                	
////                });
//    	
//        System.out.println("allDeletedIds : "+allDeletedIds);
        
        System.out.println("I am sending request to database");
    	result = notificationRepository.softDeleteByIdsAndForExtensionAndExtensionWithAndOrganizationAndIsDeleted(ids, forExtension, organization, false);
        return result;
    }

}
