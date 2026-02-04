package com.mylinehub.crm.service;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.FileCategory;
import com.mylinehub.crm.entity.Media;
import com.mylinehub.crm.entity.dto.FileCategoryDTO;
import com.mylinehub.crm.enums.FILE_STORE_REQUEST_TYPE;
import com.mylinehub.crm.mapper.FileCategoryMapper;
import com.mylinehub.crm.repository.FileCategoryRepository;
import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class FileCategoryService implements CurrentTimeInterface {
	
	
	 /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final FileCategoryRepository fileCategoryRepository;
    private final FileCategoryMapper fileCategoryMapper;
    private final FileService fileService;
    private final MediaService mediaService;
    private final ApplicationContext applicationContext;
    
	 /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public boolean createFileCategoryByOrganization(Employee employee,MultipartFile image,FileCategoryDTO fileCategoryDetails,String uploadOriginalDirectory) throws Exception {
    	
    	FileCategory currentFileCategory = fileCategoryRepository.findByExtensionAndNameAndOrganization(fileCategoryDetails.getExtension(),fileCategoryDetails.getName(),fileCategoryDetails.getOrganization());
    	
    	if(currentFileCategory==null)
    	{
    		
    		FileCategory fileCategory = fileCategoryMapper.mapDTOToFileCategory(fileCategoryDetails);
    		
    		if(fileCategory.getName().contains("/"))
    		{
    			fileCategory.setRoot(false);
    		}
    		else
    		{
    			fileCategory.setRoot(true);
    		}
    		
//    		System.out.println("currentFileCategory is null");
            boolean proceed = uploadIconImageByExtensionAndNameAndOrganization(employee.getOrganization(),"create","",image,fileCategory,uploadOriginalDirectory);
    	    if(!proceed)
    	    {
    	    	return false;
    	    }
    	}
    	else
    	{
    		return false;
    	}
    
    	
        return true;
    }

    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public boolean deleteByExtensionAndNameAndOrganization(String requestOrigin,String organizationString, FileCategoryDTO fileCategoryDetails,String uploadOriginalDirectory) throws Exception {
    	
    	FileCategory currentFileCategory = fileCategoryRepository.findByExtensionAndNameAndOrganization(fileCategoryDetails.getExtension(),fileCategoryDetails.getName(),fileCategoryDetails.getOrganization());
    	
    	
    	if(currentFileCategory==null)
    	{
    		return false;
    	}
    	else
    	{       		
    		//Find all database records
    		List<FileCategory> deleteAll = fileCategoryRepository.findByExtensionAndOrganizationAndNameContainingIgnoreCase(fileCategoryDetails.getExtension(),fileCategoryDetails.getOrganization(),fileCategoryDetails.getName());
    		
    		//DELETE ALL CATEGORY IMAGES
    		deleteAll.stream().forEach((item) -> {
    			
    			if(item.getIconImageData() != null && item.getIconImageData() != "")
            	{
            		try {
            			String name = currentFileCategory.getIconImageData();
                		name = name.replace(uploadOriginalDirectory+"/", "");
                		fileService.deleteFile(organizationString,uploadOriginalDirectory, name);
            		}
            		catch(Exception e)
            		{
            			//Donot do anything
            			e.printStackTrace();
            		}
            		
            	}
    			
    		});
    		
   		 
   		 
	   		 //Delete Media in table
	   		 List<Media> allMedias = mediaService.findAllByFileCategoryInAndOrganization(deleteAll, organizationString);
	   		 
	   		 if(allMedias != null && allMedias.size() > 0)
	   		 {
	   			 mediaService.deleteAll(allMedias);
	   		 }
    		
	   		 
    		//DELETE ALL DATABASE RECORDS
    		fileCategoryRepository.deleteAll(deleteAll);
    		
    		
    		//Deleting Categories files and folder as well
    		
    		 String DIRECTORY = applicationContext.getEnvironment().getProperty("spring.websocket.fileUpload");
    	    	//System.out.println(token);
    		 
    		 if(requestOrigin!=null && requestOrigin.contains(FILE_STORE_REQUEST_TYPE.WHATSAPP.name()))
    			{
    				System.out.println("Whats app Media Upload");
    				DIRECTORY = DIRECTORY +"/"+fileCategoryDetails.getOrganization()+"/"+fileCategoryDetails.getName();
    			}
    		 else {
    			 DIRECTORY = DIRECTORY +"/"+fileCategoryDetails.getOrganization()+"/"+ fileCategoryDetails.getExtension()+"/"+fileCategoryDetails.getName();
    		    }
    		 
    		 //Delete files on system
    		 fileService.deleteDirectory(organizationString,DIRECTORY);

    		 
    	}
    	
        return true;
    }
    
    
    /**
     * The method is to retrieve all FileCategory from the database and display them.
     *
     * After downloading all the data about the FileCategory,
     * the data is mapped to dto which will display only those needed
     * @return list of all FileCategorys with specification of data in FileCategoryDTO
     */
    
    public List<FileCategoryDTO> getAllFileCategoryByExtensionAndOrganization(String extention,String organization){
    	return fileCategoryRepository.findByExtensionAndOrganizationAndRoot(extention,organization,true)
                .stream()
                .map(fileCategoryMapper::mapFileCategoryToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all FileCategory from the database and display them.
     *
     * After downloading all the data about the FileCategory,
     * the data is mapped to dto which will display only those needed
     * @return list of all FileCategorys with specification of data in FileCategoryDTO
     */
    
    public List<FileCategoryDTO> findByExtensionAndOrganizationAndRootAndNameContainingIgnoreCase(String extention,String organization,String nameContaining){
    	return fileCategoryRepository.findByExtensionAndOrganizationAndRootAndNameContainingIgnoreCase(extention,organization,false,nameContaining)
                .stream()
                .map(fileCategoryMapper::mapFileCategoryToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * The method is to retrieve all FileCategory from the database and display them.
     *
     * After downloading all the data about the FileCategory,
     * the data is mapped to dto which will display only those needed
     * @return list of all FileCategorys with specification of data in FileCategoryDTO
     */
    
    public FileCategoryDTO getFileCategoryByExtensionAndNameAndOrganization(String extention,String name,String organization){
    	return fileCategoryMapper.mapFileCategoryToDTO(fileCategoryRepository.findByExtensionAndNameAndOrganization(extention,name,organization));

    }
    
    
    /**
     * The method is to retrieve all FileCategory from the database and display them.
     *
     * After downloading all the data about the FileCategory,
     * the data is mapped to dto which will display only those needed
     * @return list of all FileCategorys with specification of data in FileCategory
     */
    
    public FileCategory findFileCategoryByExtensionAndNameAndOrganization(String extention,String name,String organization){
    	return fileCategoryRepository.findByExtensionAndNameAndOrganization(extention,name,organization);

    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public boolean updateFileCategoryByOrganization(String requestOrigin,String organiation,String oldName,MultipartFile image,FileCategoryDTO fileCategoryDetails,String uploadOriginalDirectory) throws Exception {
    	
//    	System.out.println("I am in updateFileCategoryByOrganization");
    	
    	FileCategory currentFileCategory = fileCategoryRepository.findByExtensionAndNameAndOrganization(fileCategoryDetails.getExtension(),oldName,fileCategoryDetails.getOrganization());
    	
//    	System.out.println("After finding");
    	
    	if(currentFileCategory==null)
    	{
    		return false;
    	}
    	else
    	{
    		if(!(requestOrigin!=null && requestOrigin.contains(FILE_STORE_REQUEST_TYPE.WHATSAPP.name())))
 			{
        		if(fileCategoryDetails.getName().contains("/"))
        		{
        			currentFileCategory.setRoot(false);
        		}
        		else
        		{
        			currentFileCategory.setRoot(true);
        		}
 			}

    		
//    		System.out.println("fileCategoryDetails.getName() : "+fileCategoryDetails.getName());
    		
    		currentFileCategory.setName(fileCategoryDetails.getName());
    		currentFileCategory.setBusinessType(fileCategoryDetails.getBusinessType());
//    		currentFileCategory.setDomain(fileCategoryDetails.getDomain());
//    		currentFileCategory.setExtension(fileCategoryDetails.getExtension());
//    		currentFileCategory.setOrganization(fileCategoryDetails.getOrganization());
           	
//    		System.out.println("currentFileCategory is not null");
            boolean proceed = uploadIconImageByExtensionAndNameAndOrganization(organiation,"update",oldName,image,currentFileCategory,uploadOriginalDirectory);
    	    if(!proceed)
    	    {
    	    	return false;
    	    }
    	    
    	    try {
    	    	
        	    //Update folder name
        	    String DIRECTORY = applicationContext.getEnvironment().getProperty("spring.websocket.fileUpload");
//    	    	System.out.println("Updating folder name");
//    	    	System.out.println("DIRECTORY : "+DIRECTORY);
//    			DIRECTORY = DIRECTORY +"/"+fileCategoryDetails.getOrganization()+"/"+ fileCategoryDetails.getExtension()+"/"+oldName;
//    			System.out.println("fileCategoryDetails.getName() : "+fileCategoryDetails.getName());
    			
    			if(requestOrigin!=null && requestOrigin.contains(FILE_STORE_REQUEST_TYPE.WHATSAPP.name()))
     			{
     				System.out.println("Whats app Media Upload");
     				DIRECTORY = DIRECTORY +"/"+fileCategoryDetails.getOrganization()+"/"+oldName;
     			}
    			else {
     			 DIRECTORY = DIRECTORY +"/"+fileCategoryDetails.getOrganization()+"/"+ fileCategoryDetails.getExtension()+"/"+oldName;
     		    }
    			
    			fileService.renameDirectory(DIRECTORY,fileCategoryDetails.getName());
    	    }
    	    catch(Exception e)
    	    {
    	    	e.printStackTrace();
    	    	throw e;
    	    }
    	}
    
        return true;
    }
    
    
    public boolean uploadIconImageByExtensionAndNameAndOrganization(String organization,String type,String oldName, MultipartFile image,FileCategory currentFileCategory,String uploadOriginalDirectory) throws Exception {
    	
//    	System.out.println("uploadIconImageByExtensionAndNameAndOrganization");
//    	System.out.println("Image : "+image);
//    	System.out.println("currentFileCategory.getIconImageData() : "+currentFileCategory.getIconImageData());
//    	System.out.println("oldName : "+oldName);
    	if( image!=null && currentFileCategory.getIconImageData() != null && currentFileCategory.getIconImageData() != "")
		{
//    		System.out.println("Image is not null");
    		try {
    			String name = currentFileCategory.getIconImageData();
    			
    			if(name!=null)
    			{
    				name = name.replace(uploadOriginalDirectory+"/", "");
            		fileService.deleteFile(organization,uploadOriginalDirectory, name);
    			}
    		}
    		catch(Exception e)
    		{
    			//Donot do anything
    			e.printStackTrace();
    		}
		}
		else
		{
		}
    	
    	
    	try {
    		if(image != null)
    		{
    			String imagesLocation = fileService.saveFileToStorage(organization,uploadOriginalDirectory, image);
        		currentFileCategory.setIconImageData(uploadOriginalDirectory+"/"+imagesLocation);
        		currentFileCategory.setIconImageType(image.getContentType());
    		}
    		else
    		{
    		}
    		
    		if(type == "create")
    		{
    			fileCategoryRepository.save(currentFileCategory);
    		}
    		else if (type == "update")
    		{
    			List<FileCategory> correctAll = fileCategoryRepository.findByExtensionAndOrganizationAndNameContainingIgnoreCase(currentFileCategory.getExtension(),currentFileCategory.getOrganization(),oldName);
    			List<FileCategory> correctedAll = new ArrayList<FileCategory>();

    			
    			correctAll.stream().forEach((item) -> {
    				
//    				System.out.println("****************COMPARE***********");
//	    			System.out.println("item : "+item.getName());
//	    			System.out.println("Old :"+oldName);
//	    			System.out.println("currentFileCategory.getName() :"+currentFileCategory.getName());
    				if(item.getName() == currentFileCategory.getName())
    				{
    	    			
//    	    			System.out.println("****************SAME***********");
//    	    			System.out.println("After : "+currentFileCategory.getName());
//    	    			System.out.println(currentFileCategory.getName());
    					correctedAll.add(currentFileCategory);
    				}
    				else
    				{
//    					System.out.println("************************Other***********");
//    					System.out.println("Before : "+item.getName());
    					item.setName(item.getName().replace(oldName, currentFileCategory.getName()));
//    					System.out.println("After : "+item.getName());
    					correctedAll.add(item);
    				}
    				
    			});
    			
    			fileCategoryRepository.saveAll(correctedAll);
    			
    		}
    		else
    		{
    			return false;
    		}
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    	
    	return true;
    }
}
