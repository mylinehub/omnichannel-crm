package com.mylinehub.crm.exports;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

public class ZipHelper {

  public boolean hasZipFormat(MultipartFile file) {

	  
	System.out.println("File Content Type");
	System.out.println(file.getContentType());
	System.out.println("File Original Name");
	System.out.println(file.getOriginalFilename());
	
    String extension = FilenameUtils.getExtension(file.getOriginalFilename());
    
    System.out.println("extension");
	System.out.println(extension);
	
    if(extension.equals(".zip"))
     {
        // enter logic here  
      	return true;
     }
    else
     {
       	return false;
     }
        
  }
}
