package com.mylinehub.crm.service;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.entity.FileCategory;
import com.mylinehub.crm.entity.Media;
import com.mylinehub.crm.entity.dto.MediaDto;
import com.mylinehub.crm.mapper.MediaMapper;
import com.mylinehub.crm.repository.MediaRepository;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.repository.WhatsAppPhoneNumberRepository;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class MediaService implements CurrentTimeInterface {
	
	
	 /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
	private final MediaRepository mediaRepository;
	private final WhatsAppPhoneNumberRepository whatsAppPhoneNumberRepository;
    private final MediaMapper mediaMapper;

	 /**
     * The task of the method is create media in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public boolean createMediaByExtensionAndOrganization(MediaDto mediaDetails) throws Exception {
    	
    	Media currentMedia = mediaRepository.findByExtensionAndNameAndOrganization(mediaDetails.getExtension(),mediaDetails.getName(),mediaDetails.getOrganization());
    	
    	if(currentMedia==null)
    	{
    		
    		WhatsAppPhoneNumber whatsAppPhoneNumber = whatsAppPhoneNumberRepository.getOne(mediaDetails.getWhatsAppPhoneNumberId());
    		currentMedia = mediaMapper.mapDTOToMedia(mediaDetails,whatsAppPhoneNumber);
    		mediaRepository.save(currentMedia);
    	}
    	else
    	{
    		return false;
    	}
        return true;
    }
    
    /**
     * The task of the method is create media in the database after confirming the account
     * @param id id of the media
     * @return enable user account
     * @throws Exception 
     */
    public boolean deleteMediaById(Long id) throws Exception {
    	
    	Media currentMedia = mediaRepository.getOne(id);
    	
    	if(currentMedia!=null)
    	{
    		mediaRepository.delete(currentMedia);
    	}
    	else
    	{
    		return false;
    	}
        return true;
    }
    
    /**
     * The task of the method is get media from database as per parameters
     * * @param whatsAppPhoneNumber whatsAppPhoneNumber to fetch details of
     * @param fileName fileName to fetch details of
     * @param organization organization of the user
     * @return enable user account
     * @throws Exception 
     */
    public Media findByFileNameAndOrganization(String fileName,String organization) throws Exception {
        return mediaRepository.findByNameAndOrganization(fileName, organization);
    }
//    
//    /**
//     * The task of the method is get media from database as per parameters
//     * * @param whatsAppPhoneNumber whatsAppPhoneNumber to fetch details of
//     * @param fileName fileName to fetch details of
//     * @param organization organization of the user
//     * @return enable user account
//     * @throws Exception 
//     */
//    public Media findByWhatsAppPhoneNumberAndNameAndOrganization(WhatsAppPhoneNumber whatsAppPhoneNumber,String name,String organization) throws Exception {
//        return mediaRepository.findByWhatsAppPhoneNumberAndNameAndOrganization(whatsAppPhoneNumber,name, organization);
//    }
    
    /**
     * The task of the method is get media from database as per parameters
     * @param whatsAppMediaId whatsAppMediaId to fetch details of
     * @return enable user account
     * @throws Exception 
     */
    public Media findByWhatsAppMediaId(String whatsAppMediaId) throws Exception {
        return mediaRepository.findByWhatsAppMediaId(whatsAppMediaId);
    }
    
    /**
     * The task of the method is get media from database as per parameters
     * @param extension extension of the user
     * @param fileNames fileNames to fetch details of
     * @param organization organization of the user
     * @return enable user account
     * @throws Exception 
     */
    public List<Media> findAllByExtensionAndFileNamesInAndOrganization(String extension,List<String> fileNames,String organization) throws Exception {
        return mediaRepository.findAllByExtensionAndFileNamesInAndOrganization(extension, fileNames, organization);
    }
    
    /**
     * The task of the method is get media from database as per parameters
     * @param fileCategories List of fileCategory to fetch details of
     * @param organization organization of the user
     * @return enable user account
     * @throws Exception 
     */
    public List<Media> findAllByFileCategoryInAndOrganization(List<FileCategory> fileCategories,String organization) throws Exception {
        return mediaRepository.findAllByFileCategoryInAndOrganization(fileCategories, organization);
    }
    
    /**
     * The task of the method is get media from database as per parameters
     * @param fileCategory fileCategory to fetch details of
     * @param organization organization of the user
     * @return enable user account
     * @throws Exception 
     */
    public List<MediaDto> findAllByFileCategoryAndOrganization(FileCategory fileCategory,String organization) throws Exception {
        return mediaRepository.findAllByFileCategoryAndOrganization(fileCategory, organization)
        		.stream()
                .map(mediaMapper::mapMediaToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * The task of the method is to create multiple media in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public List<Media> saveAll(List<Media> mediaFiles) throws Exception {
        return mediaRepository.saveAll(mediaFiles);
    }
    
    
    /**
     * The task of the method is delete multiple media in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public void deleteAll(List<Media> mediaFiles) throws Exception {
        mediaRepository.deleteAll(mediaFiles);
    }
    
}
