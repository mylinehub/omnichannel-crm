package com.mylinehub.crm.service;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.data.dto.OrganizationWorkingDTO;
import com.mylinehub.crm.entity.Media;
import com.mylinehub.crm.entity.Organization;
import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class FileService implements CurrentTimeInterface{
	
	private final MediaService mediaService;
	
	// Save image in a local directory
    // Below function should not be used to send upload files to whats app. It should only be above one
    public String saveFileToStorage(String organization,String uploadDirectory, MultipartFile file) throws Exception {
        
    	System.out.println("File Service : saveFileToStorage");
    	
		Map<String,Organization> organizationMap = OrganizationData.workWithAllOrganizationData(organization,null,"get-one",null);
  		
  		if(organizationMap == null || organizationMap.size() == 0)
  		{
  			new Exception("Organization not found for employee");
  		}
  		
        //Verify Organization Upload Limit
  		allowOrganizationAndUploadAsPerLimit(organizationMap.get(organization),file.getSize());
  		
    	String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        Path uploadPath = Path.of(uploadDirectory);
        Path filePath = uploadPath.resolve(uniqueFileName);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        //Upload file Size in Organization Data
        OrganizationWorkingDTO organizationWorkingDTO = new OrganizationWorkingDTO();
		organizationWorkingDTO.setCurrentFileSize(file.getSize());
		OrganizationData.workWithAllOrganizationData(organization,null,"increase-file-upload-size",organizationWorkingDTO);
		
        return uniqueFileName;
    }
    

    public Organization allowOrganizationAndUploadAsPerLimit(Organization organization, long newFileSize) throws Exception
    {
    	Organization toReturn = null;
    	
    	try {

    		double newFileSizeInMB = (newFileSize / (1024*1024));
    		System.out.println("File Service : allowOrganizationAndUploadAsPerLimit");
    		
    		if((organization.getAllowedUploadInMB() != -1) && ((organization.getCurrentUploadInMB()+newFileSizeInMB)>= organization.getAllowedUploadInMB()))
    		{
    			throw new Exception("Upload Limit Exceeded. Contact admin to get it increased");
    		}
    		else {
    			toReturn = organization;
			}
    	}
    	catch(Exception e)
    	{
    		throw e;
    	}
    	
    	return toReturn;
    }
    
    
    // To view an image
    public byte[] getFile(String directory, String name) throws IOException {
    	try
    	{
    		System.out.println("File Service : getFile");
    		
            Path filePath = Path.of(directory, name);
            if (Files.exists(filePath)) {
                byte[] fileBytes = Files.readAllBytes(filePath);
                return fileBytes;
            } else {
                return null; // Handle missing images
            }
    	}
    	catch(Exception e)
    	{
    		return null;
    	}
    }

    // Delete an image
    public int deleteFile(String organization,String directory, String name) throws IOException {
    	try
    	{
    		System.out.println("File Service : deleteFile");
   		 
	    	 //Find Organization
	    	 Map<String,Organization> organizationMap = OrganizationData.workWithAllOrganizationData(organization,null,"get-one",null);
	    		
	    	 if(organizationMap == null || organizationMap.size() == 0)
	    	 {
	    		new Exception("Organization not found for employee");
	    	 }
	    	 
			
    		Path filePath = Path.of(directory, name);
    		double fileBytesLength = Files.readAllBytes(filePath).length;
    		
    		boolean isFileDeleted = false;
    		//Delete actual file
            if (Files.exists(filePath)) {
                Files.delete(filePath); 
   	            OrganizationWorkingDTO organizationWorkingDTO = new OrganizationWorkingDTO();
 			    organizationWorkingDTO.setCurrentFileSize(fileBytesLength);
 			    OrganizationData.workWithAllOrganizationData(organization,null,"delete-file-upload-size",organizationWorkingDTO);
 			   isFileDeleted = true;
            } else {
            }	
            
            //Delete file from media table
          //Delete Media in table
   		 Media media = mediaService.findByFileNameAndOrganization(name, organization);
   		 
   		 if(media != null)
   		 {
   			 mediaService.deleteMediaById(media.getId());
   		 }
   		 
   		 if(isFileDeleted)
   		 {
   			 return 1;
   		 }
   		 else
   		 {
   			 return 0;
   		 }
			 
    	}
    	catch(Exception e)
    	{
    		return 0;
    	}
    }
    
    
    // Delete directory and its contents
    public boolean deleteDirectory(String organization,String directory) throws IOException {
    	try
    	{
    		System.out.println("File Service : deleteDirectory");
    		
    		Path directoryPath = Path.of(directory);
    		
            if (Files.exists(directoryPath)) {
                Files
                .walk(directoryPath) // Traverse the file tree in depth-first order
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
//                        System.out.println("Deleting: " + path);
                        Files.delete(path);  //delete each file or directory
                        
                        if (Files.isRegularFile(path)) {
                        	double fileBytesLength = Files.readAllBytes(path).length;
                            OrganizationWorkingDTO organizationWorkingDTO = new OrganizationWorkingDTO();
             			    organizationWorkingDTO.setCurrentFileSize(fileBytesLength);
             			    OrganizationData.workWithAllOrganizationData(organization,null,"delete-file-upload-size",organizationWorkingDTO);
                        }
             		
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                
                return true;
            } else {
                return false; // Handle missing images
            }
    	}
    	catch(Exception e)
    	{
    		return false;
    	}
    }
    
    public boolean renameDirectory(String directory, String newName) throws IOException
    {
    	System.out.println("File Service : renameDirectory");
    	
    	try
    	{
//    		System.out.println("newName : "+newName);
//    		
//    		System.out.println("directory : "+directory);

    		File dir = new File(directory);
    		
//    		System.out.println("Absolute path old: "+dir.getAbsolutePath());
    		
    		
    		if (!dir.isDirectory()) {
//    		  System.err.println("There is no directory @ given path");
    		  return false;
    		} else {
    			

    			Path to = Path.of(dir.getParent() + "\\" + newName.substring(newName.lastIndexOf('/') + 1).trim()+"\\");
//    			System.err.println("to : "+to.toString());
                //Create directory if it is not present
                if (!Files.exists(to)) {
                    Files.createDirectories(to);
                }
               
    			Path from = Path.of(directory+"\\");
    			
//    			System.err.println("from : "+from.toString());

    			try {   				    
    			    Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
//    			    JOptionPane.showMessageDialog(null, "test");
    			} catch (IOException e) {
    			    // TODO Auto-generated catch block
    			    e.printStackTrace();
    			    return false;
    			}
    			
//    		    System.out.println("Enter new name of directory(Only Name and Not Path).");
//    		    File newDir = new File(dir.getParent() + "\\" + newName);
//    		    
//    		    System.out.println("Absolute path new: "+newDir.getAbsolutePath());
//    		    
//    		    System.out.println(dir.renameTo(newDir));
//    		    
//    		    System.out.println("Absolute path after renaming: "+newDir.getAbsolutePath());
    				
    		    return true;
    		}
    	}
    	catch(Exception e)
    	{
    		return false;
    	}
    }

    
    // compress the image bytes before storing it in the database
    public static byte[] compressBytes(byte[] data) {
    	System.out.println("File Service : compressBytes");
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
        }
        System.out.println("Compressed Image Byte Size - " + outputStream.toByteArray().length);

        return outputStream.toByteArray();
    }

    // uncompress the image bytes before returning it to the angular application
    public static byte[] decompressBytes(byte[] data) {
    	System.out.println("File Service : decompressBytes");
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException ioe) {
        } catch (DataFormatException e) {
        }
        return outputStream.toByteArray();
    }
    
    
    public static MultipartFile convertFiletoMultiPart(String filePath) throws IOException {
        try {
        	
        	 System.out.println("File Service : convertFiletoMultiPart");
        	 
            File file = new File(filePath);
            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

            if (file.exists()) {
                System.out.println("File Exist => " + file.getName() + " :: " + file.getAbsolutePath());
            }
            else {
            	System.out.println("File Service : file does not exist");
            }
            
            FileInputStream input = new FileInputStream(file);
            
            System.out.println("Opened Input Stream");
            
            MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(),mimeTypesMap.getContentType(file),
                    IOUtils.toByteArray(input));
            
            System.out.println("Created multipart file");
            
            System.out.println("multipartFile => " + multipartFile.isEmpty() + " :: "
                    + multipartFile.getOriginalFilename() + " :: " + multipartFile.getName() + " :: "
                    + multipartFile.getSize() + " :: " + multipartFile.getBytes());
        
            return multipartFile;
            
        } catch (IOException e) {
            System.out.println("Exception => " + e.getLocalizedMessage());
            throw e;
        }
    }

}
