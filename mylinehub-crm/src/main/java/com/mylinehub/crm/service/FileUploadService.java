package com.mylinehub.crm.service;

import static java.nio.file.Files.copy;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.data.dto.OrganizationWorkingDTO;
import com.mylinehub.crm.data.dto.UploadDTO;
import com.mylinehub.crm.entity.Media;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.entity.dto.MediaDto;
import com.mylinehub.crm.enums.FILE_STORE_REQUEST_TYPE;
import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.dto.service.MediaUploadDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.service.CallMediaAPIService;
import com.mylinehub.crm.whatsapp.service.CreateFileCategoryForOrgService;
import com.mylinehub.crm.entity.FileCategory;
import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class FileUploadService implements CurrentTimeInterface{
	
	private final ApplicationContext applicationContext;
	private final CreateFileCategoryForOrgService createFileCategoryForOrgService;
	private final MediaService mediaService;
	private final FileCategoryService fileCategoryService;
	private final FileService fileService;
	private final CallMediaAPIService callMediaAPIService;
	
	
	// Save image in a local directory
    public List<MediaDto> uploadFiles(String organizationString,String extension,String requestOrigin,String category,List<MultipartFile> multipartFiles,UploadDTO uploadDTO, boolean received,WhatsAppChatHistory chatHistory) throws Exception {
         
    	boolean uploadLimitCrossed = false;
    	List<MediaDto> toReturn = new ArrayList<MediaDto>();
        String DIRECTORY = applicationContext.getEnvironment().getProperty("spring.websocket.fileUpload");
		List<Media> allMedia = new ArrayList<>();
		FileCategory fileCategory = null;
		
		
        //Find Organization
		Map<String,Organization> organizationMap = OrganizationData.workWithAllOrganizationData(organizationString,null,"get-one",null);
  		
  		if(organizationMap == null || organizationMap.size() == 0)
  		{
  			new Exception("Organization not found for employee");
  		}
  		
  		Organization organization = organizationMap.get(organizationString);
  		
		if(requestOrigin!=null && requestOrigin.contains(FILE_STORE_REQUEST_TYPE.WHATSAPP.name()))
		{
			extension = organizationString;
			DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate ld = LocalDate.parse(new Date().toString(), DATEFORMATTER);
			LocalDateTime ldt = LocalDateTime.of(ld, LocalDateTime.now().toLocalTime());
			
			category = organization.getWhatsAppMediaFolder()+ "/" + uploadDTO.getWhatsAppPhoneNumber()+"/"+ldt.toString()+"/send";
			
			System.out.println("Whats app Media Upload Files");
			DIRECTORY = DIRECTORY +"/"+organizationString+"/"+category+"/";
			createFileCategoryForOrgService.createBaseCategoryForWhatsAppPhoneDayData(organizationString,uploadDTO.getWhatsAppPhoneNumber());
			
			fileCategory = fileCategoryService.findFileCategoryByExtensionAndNameAndOrganization(organizationString, category, organizationString);
		}
		else {
			
			DIRECTORY = DIRECTORY +"/"+organizationString+"/"+extension+"/"+category+"/";
			fileCategory = fileCategoryService.findFileCategoryByExtensionAndNameAndOrganization(extension, category, organizationString);
		}


		System.out.println("DIRECTORY : "+DIRECTORY);
		List<Media> alreadyUploadMediaList = null;
		List<String> allFileNames = new ArrayList<>();
		
		Map<String,Media> alreadyUploadMediaListMap = new HashMap<>();
		
		//Proceed only if fileCategory is found
		if(fileCategory != null) {
			
			//Work and define previous data, this maintains data integrity
			try {
				//Add all filenames in list
				for(MultipartFile file : multipartFiles) {
					String filename = StringUtils.cleanPath(file.getOriginalFilename());
					allFileNames.add(filename);
				}
				
				//Find all already upload media
				alreadyUploadMediaList = mediaService.findAllByExtensionAndFileNamesInAndOrganization(extension, allFileNames, organizationString);
				
				//Create replace list map to clear memeory assigned in organization table
				for(Media current : alreadyUploadMediaList) {
					//Set Map for later use
					alreadyUploadMediaListMap.put(current.getName(),current);
					

					//Delete from actual storage
					Path filePath = Path.of(DIRECTORY, current.getName());
		    		double fileBytesLength = Files.readAllBytes(filePath).length;

		    		//Delete actual file
		            if (Files.exists(filePath)) {
		                Files.delete(filePath); 
		                
		                //Delete & Free Memory Data
		   	            OrganizationWorkingDTO organizationWorkingDTO = new OrganizationWorkingDTO();
		 			    organizationWorkingDTO.setCurrentFileSize(fileBytesLength);
		 			    OrganizationData.workWithAllOrganizationData(organizationString,null,"delete-file-upload-size",organizationWorkingDTO);
		            } else {
		            }	
					
				}
				
				//Delete All media which were upload previously
				//Media files will be replaced automatically as per code below. So no need to touch it
				mediaService.deleteAll(alreadyUploadMediaList);
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw e;
			}
			
			
			//Start uploading media files to whats app, disk and in repo as per requirement
			for(MultipartFile file : multipartFiles) {
				
				try {
					//Verify Organization Upload Limit
					try {
						fileService.allowOrganizationAndUploadAsPerLimit(organization,file.getSize());
					}
					catch(Exception e) {
						uploadLimitCrossed = true;
						throw e;
					}
			  		
		            String filename = StringUtils.cleanPath(file.getOriginalFilename());
		            Path uploadPath = Path.of(DIRECTORY);
		            
		            //Create directory if it is not present
		            if (!Files.exists(uploadPath)) {
		                Files.createDirectories(uploadPath);
		            }
		            
		            //Placing file in storage actually
					try {
			            Path fileStorage = get(DIRECTORY, filename).toAbsolutePath().normalize();
			            copy(file.getInputStream(), fileStorage, REPLACE_EXISTING);
			            MediaDto current = new MediaDto();
			            current.setName(filename);
			            current.setType(file.getContentType());
			            current.setByteData(null);
			            toReturn.add(current);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}

					
					//Placing file in table
					try {
						    Media media = new Media();
						    media.setReceived(received);
			    			media.setName(filename);
			    			media.setExtension(extension);
			    			media.setFromExtension(extension);
			    			media.setOrganization(organizationString);
			    			media.setFileCategory(fileCategory);
			    			media.setType(file.getContentType());
			    			media.setSize(file.getSize());
			    			media.setCaption(file.getName());
			    			media.setMediaUploadModule(requestOrigin);
			    			media.setExternalPartyUploadSuccessful(false);
			    			
			    			//If its whats app call whats app upload media API
				            if(requestOrigin!=null && requestOrigin.contains(FILE_STORE_REQUEST_TYPE.WHATSAPP.name()))
				    		{
				            	Map<String,WhatsAppPhoneNumber> whatsAppMap = WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(uploadDTO.getWhatsAppPhoneNumber(),null,"get-one");
				    			if(whatsAppMap != null && whatsAppMap.size()>0 )
				    			{
				    				System.out.println("Whats app Media Record Added");
				        			media.setWhatsAppPhoneNumber(whatsAppMap.get(uploadDTO.getWhatsAppPhoneNumber()));
				        			media.setExtension(organizationString);
				        			
				        			//Call whats app API to upload media and get Media ID
				        			try {
				        				if(!received) {
					        				MediaUploadDto mediaUploadDto = callMediaAPIService.triggerMediaUploadAPI(file.getContentType(),file.getSize(),organizationString,category,filename,whatsAppMap.get(uploadDTO.getWhatsAppPhoneNumber()));
					        				media.setWhatsAppMediaId(mediaUploadDto.getMediaId());
					        				media.setWhatsAppMediaType(mediaUploadDto.getType());
					        				media.setWhatsAppLink(mediaUploadDto.getLink());
						        			media.setWhatsAppUploadDate(new Date());
					        				media.setExternalPartyUploadSuccessful(mediaUploadDto.isExternalPartyUploadSuccessful());
					        				media.setError(mediaUploadDto.getError());	
				        				}
				        				else {
				        					media.setWhatsAppMediaId(chatHistory.getWhatsAppMediaId());
					        				media.setWhatsAppMediaType(chatHistory.getMessageType());
					        				media.setWhatsAppLink(null);
						        			media.setWhatsAppUploadDate(new Date());
					        				media.setExternalPartyUploadSuccessful(false);
					        				media.setError(null);	
				        					
				        				}
				        			}
				        			catch(Exception e)
				        			{
				        				e.printStackTrace();
				        				media.setError(e.getMessage());
				        			}
				        			
				    			}	
				    		}

			    			allMedia.add(media);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}

					
					OrganizationWorkingDTO organizationWorkingDTO = new OrganizationWorkingDTO();
					organizationWorkingDTO.setCurrentFileSize(file.getSize());
					OrganizationData.workWithAllOrganizationData(organizationString,null,"increase-file-upload-size",organizationWorkingDTO);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					
					if(uploadLimitCrossed) {
						
						 try {
				    			//Add all media to database (that have been saved)
				                if(allMedia.size()>0){
				                	mediaService.saveAll(allMedia);
				                }
				         }
				         catch(Exception e1)
						 {
								e1.printStackTrace();
								throw e1;
						 }	
						
						throw e;
					}
				}	
	        }
            
            try {
    			//Add all media to database
                if(allMedia.size()>0){
                	mediaService.saveAll(allMedia);
                }
            }
            catch(Exception e)
			{
				e.printStackTrace();
				throw e;
			}	
		}

		return toReturn;
    }
}
