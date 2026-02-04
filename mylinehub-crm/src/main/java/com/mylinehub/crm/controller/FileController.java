package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.FILE_REST_URL;
import static java.nio.file.Paths.get;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.ResponseEntity.status;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.FileCategory;
import com.mylinehub.crm.entity.dto.FileCategoryDTO;
import com.mylinehub.crm.entity.dto.MediaDto;
import com.mylinehub.crm.enums.FILE_STORE_REQUEST_TYPE;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.FileCategoryService;
import com.mylinehub.crm.service.FileService;
import com.mylinehub.crm.service.FileUploadService;
import com.mylinehub.crm.service.MediaService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(produces="application/json", path = FILE_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class FileController {

	private final EmployeeRepository employeeRepository;
	private final FileService fileService;
	private final MediaService mediaService;
	private final FileCategoryService fileCategoryService;
    private final ApplicationContext applicationContext;
	private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
    private final FileUploadService fileUploadService;

    @GetMapping("/getAllFileCategoryByExtensionAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<FileCategoryDTO>> getAllFileCategoryByExtensionAndOrganization(@RequestParam String extension,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<FileCategoryDTO> returnData = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    			returnData= fileCategoryService.getAllFileCategoryByExtensionAndOrganization(extension,organization);
        		return status(HttpStatus.OK).body(returnData);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(returnData);
    	} 	
	}
	
    
    @PostMapping("/deleteFileCategoryByExtensionAndNameAndOrganization")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<Boolean> deleteFileCategoryByExtensionAndNameAndOrganization(@RequestParam(defaultValue = "FILESTORE") String requestOrigin,@RequestBody FileCategoryDTO fileCategoryDetails,@RequestHeader (name="Authorization") String token) throws Exception{
   	        
    	Boolean returnData = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	//System.out.println(token);
       	
       	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	String uploadOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadFileCategoryOriginalDirectory");
       	
       	if(requestOrigin.contains(FILE_STORE_REQUEST_TYPE.WHATSAPP.name()))
       	{
           	uploadOriginalDirectory = uploadOriginalDirectory + "/" + employee.getOrganization();
       	}
       	else {
           	uploadOriginalDirectory = uploadOriginalDirectory + "/" + employee.getExtension();
       	}
       	
       	fileCategoryDetails.setDomain(employee.getDomain());
       	fileCategoryDetails.setExtension(employee.getExtension());
       	fileCategoryDetails.setOrganization(employee.getOrganization());
       	
       	if(employee.getOrganization().trim().equals(fileCategoryDetails.getOrganization().trim()))
       	{
       			returnData= fileCategoryService.deleteByExtensionAndNameAndOrganization(requestOrigin,employee.getOrganization(),fileCategoryDetails, uploadOriginalDirectory);
           		return status(HttpStatus.OK).body(returnData);
       	}
       	else
       	{
       		//System.out.println("I am in else controller");
       		
       		return status(HttpStatus.UNAUTHORIZED).body(returnData);
       	} 	
   	}
    
    
    @PostMapping("/createFileCategoryByExtensionAndNameAndOrganization")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<Boolean> createFileCategoryByExtensionAndNameAndOrganization(@RequestParam(defaultValue = "FILESTORE") String requestOrigin,@RequestParam("data") String fileCategoryDetailsString,@RequestParam("image") MultipartFile image,@RequestHeader (name="Authorization") String token) throws Exception{
   	        
    	Boolean returnData = null;
    	
    	FileCategoryDTO fileCategoryDetails;
    	
    	ObjectMapper mapper = new ObjectMapper();
    	try{
//    		System.out.println("fileCategoryDetailsString : "+fileCategoryDetailsString);
    		fileCategoryDetails= mapper.readValue(fileCategoryDetailsString, FileCategoryDTO.class);
    		token = token.replace(jwtConfiguration.getTokenPrefix(), "");
           	
           	//System.out.println(token);
           	
           	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
           	
           	String uploadOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadFileCategoryOriginalDirectory");
           	
           	if(requestOrigin.contains(FILE_STORE_REQUEST_TYPE.WHATSAPP.name()))
           	{
               	uploadOriginalDirectory = uploadOriginalDirectory + "/" + employee.getOrganization();
           	}
           	else {
               	uploadOriginalDirectory = uploadOriginalDirectory + "/" + employee.getExtension();
           	}

           	fileCategoryDetails.setDomain(employee.getDomain());
           	fileCategoryDetails.setExtension(employee.getExtension());
           	fileCategoryDetails.setOrganization(employee.getOrganization());
           	
           	if(employee.getOrganization().trim().equals(fileCategoryDetails.getOrganization().trim()))
           	{
//           		    System.out.println("Calling file Category Service");
           			returnData= fileCategoryService.createFileCategoryByOrganization(employee,image,fileCategoryDetails, uploadOriginalDirectory);
               		return status(HttpStatus.OK).body(returnData);
           	}
           	else
           	{
           		//System.out.println("I am in else controller");
           		
           		return status(HttpStatus.UNAUTHORIZED).body(returnData);
           	} 	
    	}
    	catch (Exception e) {
    	    e.printStackTrace();
    	    return status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnData);
    	}
   	}
    
    
    
    @PostMapping("/updateFileCategoryByExtensionAndNameAndOrganizationWithoutImage")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<Boolean> updateFileCategoryByExtensionAndNameAndOrganizationWithoutImage(@RequestParam(defaultValue = "FILESTORE") String requestOrigin,@RequestParam("oldName") String oldName,@RequestParam("data") String fileCategoryDetailsString,@RequestHeader (name="Authorization") String token) throws Exception{
   	        
    	Boolean returnData = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	FileCategoryDTO fileCategoryDetails;
       	//System.out.println(token);
       	ObjectMapper mapper = new ObjectMapper();
    	try{
    		fileCategoryDetails= mapper.readValue(fileCategoryDetailsString, FileCategoryDTO.class);
    		Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
           	
           	String uploadOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadFileCategoryOriginalDirectory");
           
           	if(requestOrigin.contains(FILE_STORE_REQUEST_TYPE.WHATSAPP.name()))
           	{
               	uploadOriginalDirectory = uploadOriginalDirectory + "/" + employee.getOrganization();
           	}
           	else {
               	uploadOriginalDirectory = uploadOriginalDirectory + "/" + employee.getExtension();
           	}

           	fileCategoryDetails.setDomain(employee.getDomain());
           	fileCategoryDetails.setExtension(employee.getExtension());
           	fileCategoryDetails.setOrganization(employee.getOrganization());
           	
           	if(employee.getOrganization().trim().equals(fileCategoryDetails.getOrganization().trim()))
           	{
           			returnData= fileCategoryService.updateFileCategoryByOrganization(requestOrigin,employee.getOrganization(),oldName,null,fileCategoryDetails, uploadOriginalDirectory);
               		return status(HttpStatus.OK).body(returnData);
           	}
           	else
           	{
           		//System.out.println("I am in else controller");
           		
           		return status(HttpStatus.UNAUTHORIZED).body(returnData);
           	} 	
    	}
    	catch (Exception e) {
    	    e.printStackTrace();
    	    return status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnData);
    	}
   	}
    
    @PostMapping("/updateFileCategoryByExtensionAndNameAndOrganization")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<Boolean> updateFileCategoryByExtensionAndNameAndOrganization(@RequestParam(defaultValue = "FILESTORE") String requestOrigin,@RequestParam("oldName") String oldName,@RequestParam("data") String fileCategoryDetailsString,@RequestParam("image") MultipartFile image,@RequestHeader (name="Authorization") String token) throws Exception{
   	        
    	Boolean returnData = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	FileCategoryDTO fileCategoryDetails;
       	//System.out.println(token);
       	ObjectMapper mapper = new ObjectMapper();
    	try{
    		fileCategoryDetails= mapper.readValue(fileCategoryDetailsString, FileCategoryDTO.class);
    		Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
           	
           	String uploadOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadFileCategoryOriginalDirectory");
           	if(requestOrigin.contains(FILE_STORE_REQUEST_TYPE.WHATSAPP.name()))
           	{
               	uploadOriginalDirectory = uploadOriginalDirectory + "/" + employee.getOrganization();
           	}
           	else {
               	uploadOriginalDirectory = uploadOriginalDirectory + "/" + employee.getExtension();
           	}
           	
           	fileCategoryDetails.setDomain(employee.getDomain());
           	fileCategoryDetails.setExtension(employee.getExtension());
           	fileCategoryDetails.setOrganization(employee.getOrganization());
           	
           	if(employee.getOrganization().trim().equals(fileCategoryDetails.getOrganization().trim()))
           	{
           			returnData= fileCategoryService.updateFileCategoryByOrganization(requestOrigin,employee.getOrganization(),oldName,image,fileCategoryDetails, uploadOriginalDirectory);
               		return status(HttpStatus.OK).body(returnData);
           	}
           	else
           	{
           		//System.out.println("I am in else controller");
           		
           		return status(HttpStatus.UNAUTHORIZED).body(returnData);
           	} 	
    	}
    	catch (Exception e) {
    	    e.printStackTrace();
    	    return status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnData);
    	}
   	}
    
    
    
	@GetMapping("/getAllFileNamesOfUserByOrganizationAndCategory")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Map<String,String>> getAllFileNamesOfUserByOrganizationAndCategory(@RequestParam(defaultValue = "FILESTORE") String requestOrigin,@RequestParam String category,@RequestHeader (name="Authorization") String token) throws Exception{
		
		Map<String,String> toReturn = new HashMap<String,String>();
		ObjectMapper mapper = new ObjectMapper();
		List<MediaDto> allFiles = null;
		List<FileCategoryDTO> allFolders = null;
		FileCategory curentCategory;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
        String DIRECTORY = applicationContext.getEnvironment().getProperty("spring.websocket.fileUpload");
       
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee != null)
    	{
    		if(requestOrigin.contains(FILE_STORE_REQUEST_TYPE.WHATSAPP.name()))
           	{
                DIRECTORY = DIRECTORY +"/"+employee.getOrganization()+"/"+"/"+category+"/";
                curentCategory = fileCategoryService.findFileCategoryByExtensionAndNameAndOrganization(employee.getOrganization(), category, employee.getOrganization());
           	}
           	else {
           		DIRECTORY = DIRECTORY +"/"+employee.getOrganization()+"/"+ employee.getExtension()+"/"+category+"/";
           		curentCategory = fileCategoryService.findFileCategoryByExtensionAndNameAndOrganization(employee.getExtension(), category, employee.getOrganization());
           	}
    		
    		
//    		File[] files = new File(DIRECTORY).listFiles();
//    		//If this pathname does not denote a directory, then listFiles() returns null. 
//
//    		if(files != null)
//    		{
//    			for (File file : files) {
//        			if(file.isFile()) {
//        				FileDTO current = new FileDTO();
//        				current.setName(file.getName());
//        				current.setType(Files.probeContentType(file.toPath()));
//        				current.setSize(round(((file.length()/1024)/1024),2));
//        				allFiles.add(current);
//        		    }
//        		}
//        		if(allFiles.size() != 0)
//        		{
//        			toReturn.put("files",mapper.writeValueAsString(allFiles));
//        		}
//        		else
//        		{
//        			toReturn.put("files",null);
//        		}
//    		}
//    		else
//    		{
//    			toReturn.put("files",null);
//    		}
    		
    		
    		if(curentCategory!=null)
    		allFiles = mediaService.findAllByFileCategoryAndOrganization(curentCategory, employee.getOrganization());
    		
    		if(allFiles != null) {
    			if(allFiles.size() != 0)
        		{
        			toReturn.put("files",mapper.writeValueAsString(allFiles));
        		}
        		else
        		{
        			toReturn.put("files",null);
        		}
    		}
    		else {
    			toReturn.put("files",null);
			}
    		
    		allFolders = fileCategoryService.findByExtensionAndOrganizationAndRootAndNameContainingIgnoreCase(employee.getExtension(),employee.getOrganization(),category+"/");
    		
    		if(allFolders != null)
    		{
        		
        		List<FileCategoryDTO> allCurrentValidFolders = new ArrayList<FileCategoryDTO>();
        		for (FileCategoryDTO current : allFolders) {
      			  String verifyNow = current.getName();
      			  verifyNow = verifyNow.replace(category+"/", "");
      			  
      			  if(verifyNow.contains("/"))
      			  {
      				  //Donot add anything as it is not required
      			  }
      			  else
      			  {
      				allCurrentValidFolders.add(current);
      			  }
      			  
      			}
        		
        		if(allCurrentValidFolders.size() != 0)
        		{
        			toReturn.put("folders",mapper.writeValueAsString(allCurrentValidFolders));
        		}
        		else
        		{
        			toReturn.put("folders",null);
        		}
        		
    		}
    		else
    		{
    			toReturn.put("folders",null);
    		}
	
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
	} 
	
	
	@PostMapping("/uploadUserFilesByOrganizationAndCategory")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<MediaDto>> uploadUserFilesByOrganizationAndCategory(@RequestParam(defaultValue = "FILESTORE") String requestOrigin,@RequestParam String category,@RequestParam("files")List<MultipartFile> multipartFiles,@RequestHeader (name="Authorization") String token) throws Exception{
		
		List<MediaDto> toReturn = new ArrayList<MediaDto>();
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
        
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee != null)
    	{
    		return status(HttpStatus.OK).body(fileUploadService.uploadFiles(employee.getOrganization(),employee.getExtension(),requestOrigin,category,multipartFiles,null,false,null));
			
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
	} 
	
	
	@GetMapping("/downloadUserFileByOrganizationAndCategory")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Resource> downloadUserFileByOrganizationAndCategory(@RequestParam(defaultValue = "FILESTORE") String requestOrigin,@RequestParam String category,@RequestParam String filename,@RequestHeader (name="Authorization") String token) throws IOException{
	        
		Resource toReturn = null;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
        String DIRECTORY = applicationContext.getEnvironment().getProperty("spring.websocket.fileUpload");
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee != null)
    	{
    		
    		 if(requestOrigin.contains(FILE_STORE_REQUEST_TYPE.WHATSAPP.name()))
            	{
                 DIRECTORY = DIRECTORY +"/"+employee.getOrganization()+"/"+category+"/";
            	}
            else{
            	 DIRECTORY = DIRECTORY +"/"+employee.getOrganization()+"/"+ employee.getExtension()+"/"+category+"/";
            	}
    		 
    		 
    		 Path filePath = get(DIRECTORY).toAbsolutePath().normalize().resolve(filename);
    	        if(!Files.exists(filePath)) {
    	            throw new FileNotFoundException(filename + " was not found on the server");
    	        }
    	     Resource resource = new UrlResource(filePath.toUri());
    	     HttpHeaders httpHeaders = new HttpHeaders();
    	     httpHeaders.add("File-Name", filename);
    	     httpHeaders.add(CONTENT_DISPOSITION, "attachment;File-Name=" + resource.getFilename());
    	     return ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(filePath)))
    	             .headers(httpHeaders).body(resource);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
	}
	
	
	
	@GetMapping(value ="/downloadMultipleUserFileByOrganizationAndCategory", produces="application/zip")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public void downloadMultipleUserFileByOrganizationAndCategory(@RequestParam(defaultValue = "FILESTORE") String requestOrigin,@RequestParam String category,@RequestParam List<String> fileNames,@RequestHeader (name="Authorization") String token, HttpServletResponse response) throws IOException{
	        
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
        String DIRECTORY = applicationContext.getEnvironment().getProperty("spring.websocket.fileUpload");
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee != null)
    	{
 
    		 if(requestOrigin.contains(FILE_STORE_REQUEST_TYPE.WHATSAPP.name()))
            	{
                    DIRECTORY = DIRECTORY +"/"+employee.getOrganization()+"/"+category+"/";
            	}
             else {
            		DIRECTORY = DIRECTORY +"/"+employee.getOrganization()+"/"+ employee.getExtension()+"/"+category+"/";
            	}
    		 
//    		    System.out.println("Sending data to response stream");
    	        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
    	        
    	    	for (String fileName : fileNames) {
//    	    		System.out.println("Directory : "+DIRECTORY + fileName);
    	    		FileSystemResource resource = new FileSystemResource(DIRECTORY + fileName);
    	    		ZipEntry zipEntry = new ZipEntry(resource.getFilename());
    	    		zipEntry.setSize(resource.contentLength());
    	    		zipOut.putNextEntry(zipEntry);
    	    		StreamUtils.copy(resource.getInputStream(), zipOut);
    	    		zipOut.closeEntry();
    	    	}
    	    	zipOut.finish();
    	    	zipOut.close();
    	    	response.setStatus(HttpServletResponse.SC_OK);
//    	    	response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"");
    	    	response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "download" + "\"");
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    	}
	}
	
	@GetMapping("/deleteUserFileByOrganizationAndCategory")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> deleteUserFileByOrganizationAndCategory(@RequestParam(defaultValue = "FILESTORE") String requestOrigin,@RequestParam String category,@RequestParam String filename,@RequestHeader (name="Authorization") String token) throws IOException{
	        
		Integer toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
        String DIRECTORY = applicationContext.getEnvironment().getProperty("spring.websocket.fileUpload");
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee != null)
    	{
    		 if(requestOrigin.contains(FILE_STORE_REQUEST_TYPE.WHATSAPP.name()))
           	 {
                DIRECTORY = DIRECTORY +"/"+employee.getOrganization()+"/"+category+"/";
           	 }
           	 else {
           		DIRECTORY = DIRECTORY +"/"+employee.getOrganization()+"/"+ employee.getExtension()+"/"+category+"/";
           	 }
    		
    		 toReturn = fileService.deleteFile(employee.getOrganization(),DIRECTORY, filename);
    			
    		 return ResponseEntity.ok().body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
	}
	
	
	@PostMapping("/deleteMultipleUserFileByOrganizationAndCategory")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> deleteMultipleUserFileByOrganizationAndCategory(@RequestParam(defaultValue = "FILESTORE") String requestOrigin,@RequestBody List<String> fileNameList,@RequestParam String category,@RequestHeader (name="Authorization") String token) throws IOException{
	        
		Integer toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
        String DIRECTORY = applicationContext.getEnvironment().getProperty("spring.websocket.fileUpload");
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee != null)
    	{

    		if(requestOrigin.contains(FILE_STORE_REQUEST_TYPE.WHATSAPP.name()))
           	{
                DIRECTORY = DIRECTORY +"/"+employee.getOrganization()+"/"+category+"/";
           	}
           	else {
           		DIRECTORY = DIRECTORY +"/"+employee.getOrganization()+"/"+ employee.getExtension()+"/"+category+"/";
           	}
    		
    		for(int i = 0 ; i <fileNameList.size(); i++)
    		{	
    			try {
    				fileService.deleteFile(employee.getOrganization(),DIRECTORY, fileNameList.get(i));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw e;
				}
    			
    		};
    		
    		
    		 toReturn = 1;
    		 return ResponseEntity.ok().body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
	}
	
	public float round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (float) tmp / factor;
	}
	
}
