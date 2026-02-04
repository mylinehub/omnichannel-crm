package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.STATEMENT_RECONCILIATION_REST_URL;
import static java.nio.file.Files.copy;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.http.ResponseEntity.status;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.StatementReconciliation;
import com.mylinehub.crm.entity.dto.StatementReconciliationDTO;
import com.mylinehub.crm.exports.ZipHelper;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.repository.LogRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.StatementReconciliationService;
import com.mylinehub.crm.utils.ResponseMessage;

import lombok.AllArgsConstructor;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;


@RestController
@RequestMapping(produces="application/json", path = STATEMENT_RECONCILIATION_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class StatementReconciliationController {

	private final EmployeeRepository employeeRepository;
    private final StatementReconciliationService statementReconciliationService;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
    private final ErrorRepository errorRepository;
    private final ApplicationContext applicationContext;
    private final LogRepository logRepository;
	private Environment env;
	
    
    @GetMapping("/getAllStatementReconciliationByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<StatementReconciliationDTO>> getAllStatementReconciliationByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<StatementReconciliationDTO> toReturn = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn= statementReconciliationService.getAllStatementReconciliationByOrganization(organization);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}

    
    @GetMapping("/getStatementReconciliationByIdAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<StatementReconciliation> getStatementReconciliationByIdAndOrganization(@RequestParam Long batchId,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
    	StatementReconciliation statementReconciliation = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		statementReconciliation= statementReconciliationService.getStatementReconciliationByBatchIdAndOrganization(batchId, organization);
    		return status(HttpStatus.OK).body(statementReconciliation);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(statementReconciliation);
    	} 	
	}
    
    
    @DeleteMapping("/deleteStatementReconciliationByIdAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> deleteStatementReconciliationByIdAndOrganization(@RequestParam Long batchId,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = statementReconciliationService.deleteStatementReconciliationByBatchIdAndOrganization(batchId,organization);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 		
	} 
    
    
    @PostMapping("/uploadAndPerformStatementReconciliation")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN') or hasAuthority('EMPLOYEE')")
    public ResponseEntity<ResponseMessage> uploadAndPerformStatementReconciliation(@RequestParam String organization,@RequestParam("file") MultipartFile file, @RequestHeader (name="Authorization") String token) throws Exception {
      
    	ResponseMessage toReturn = null;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	System.out.println("uploadAndPerformStatementReconciliation");
    	String DIRECTORY = applicationContext.getEnvironment().getProperty("spring.websocket.uploadEmployeeReconciliationTempData");
        
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		try {
              	
              	String message = "";
              	
                if (new ZipHelper().hasZipFormat(file)) {
                  try {
                  	
                    System.out.println("I am a zip file. Save file to temp");
                    /**
                     * save file to temp
                     */
                    File zip = File.createTempFile(UUID.randomUUID().toString(), "temp");
                    FileOutputStream o = new FileOutputStream(zip);
                    try {
                    	IOUtils.copy(file.getInputStream(), o);
                    }
                    catch(Exception e)
                    {
                    	throw e;
                    }
                    finally {
                        
                        o.close();
                    }
                    
                  	System.out.println("Let us see inside it now");
            		String filename = StringUtils.cleanPath(file.getOriginalFilename());
            		
                	System.out.println("file.isEmpty() : " + file.isEmpty());
                	System.out.println("file.getOriginalFilename() : " + file.getOriginalFilename());
                	System.out.println("filename : " + filename);
                	System.out.println("file.getContentType() : " + file.getContentType());
                	
        			DIRECTORY = DIRECTORY+"/"+employee.getExtension()+"/"+new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date())+"/";

        			System.out.println("Create directories if not created");
                    Path uploadPath = Path.of(DIRECTORY);
                    //Create directory if it is not present
                    if (!Files.exists(uploadPath)) {
                         Files.createDirectories(uploadPath);
                    }		

        			
        			System.out.println("Unizp file from temp by zip4j");
        			 /**
        		     * unizp file from temp by zip4j
        		     */
        		    try {
        		         ZipFile zipFile = new ZipFile(zip);
        		         zipFile.extractAll(DIRECTORY);
        		    } catch (ZipException e) {
        		        e.printStackTrace();
        		    } finally {
        		        /**
        		         * delete temp file
        		         */
        		        zip.delete();
        		    }

                    
                    message = "Started process for file " + file.getOriginalFilename()+" and assigned new batch ID : "+ "230";
                    return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
                    
                  } catch (Exception e) {
                    message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
                  }
                }

                message = "Please upload an zip file!";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));

    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			throw e;
    		}
       	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 
    }
    
}