package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.AMI_CONNECTION_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.io.IOException;
import java.util.List;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletResponse;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.TimeoutException;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.AmiConnectionDTO;
import com.mylinehub.crm.exports.ExcelHelper;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.LogRepository;
import com.mylinehub.crm.requests.AmiUserRequest;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.AMIConnectionService;
import com.mylinehub.crm.utils.ResponseMessage;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping(produces="application/json", path = AMI_CONNECTION_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class AMIConnectionController {

    private final EmployeeRepository employeeRepository;
    private final AMIConnectionService amiconnectionService;
    private final LogRepository logRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
	private Environment env;
	
	
	@PostMapping("/connectAmiConnectionOnAmiUserAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> connectAmiConnectionOnAmiUserAndOrganization(@RequestBody AmiUserRequest request,@RequestHeader (name="Authorization") String token) throws IllegalStateException, IOException, AuthenticationFailedException, TimeoutException{
	    
			
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		toReturn= amiconnectionService.connectAmiConnectionOnAmiUserAndOrganization(request.getAmiuser(), request.getOrganization());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 		
	} 
	
	
	@PostMapping("/enableAmiConnectionOnAmiUserAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> enableAmiConnectionOnAmiUserAndOrganization(@RequestBody AmiUserRequest request,@RequestHeader (name="Authorization") String token) throws Exception{
	    
			
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		amiconnectionService.enableAmiConnectionOnOrganization(request.getAmiuser(), request.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 		
	} 
	
	
	@PostMapping("/disableAmiConnectionOnAmiUserAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> disableAmiConnectionOnAmiUserAndOrganization(@RequestBody AmiUserRequest request,@RequestHeader (name="Authorization") String token) throws Exception{
	    
			
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		amiconnectionService.disableAmiConnectionOnOrganization(request.getAmiuser(), request.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 		
	} 
	
	
	@GetMapping("/getAllAmiConnectionsByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<AmiConnectionDTO>> getAllAmiConnectionsByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<AmiConnectionDTO> amiConnections = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		amiConnections= amiconnectionService.getAllAmiConnectionsOnOrganization(organization);
    		return status(HttpStatus.OK).body(amiConnections);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(amiConnections);
    	} 	
	}
	
	@GetMapping("/getAllAmiConnectionsOnPhoneContextAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<AmiConnectionDTO>> getAllAmiConnectionsOnPhoneContextAndOrganization(@RequestParam String phonecontext,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<AmiConnectionDTO> amiConnections = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		amiConnections= amiconnectionService.getAllAmiConnectionsOnPhoneContextAndOrganization(phonecontext,organization);
    		return status(HttpStatus.OK).body(amiConnections);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(amiConnections);
    	} 	
	}
	
	@GetMapping("/getAllAmiConnectionOnIsEnabledAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<AmiConnectionDTO>> getAllAmiConnectionOnIsEnabledAndOrganization(@RequestParam Boolean isactive,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<AmiConnectionDTO> amiConnections = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		amiConnections= amiconnectionService.getAllAmiConnectionsOnIsEnabledAndOrganization(isactive,organization);
    		return status(HttpStatus.OK).body(amiConnections);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(amiConnections);
    	} 	
	}
	
	
	@GetMapping("/getAmiConnectionByAmiuserAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<AmiConnectionDTO> getAmiConnectionByAmiuserAndOrganization(@RequestParam String amiuser,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		AmiConnectionDTO searchAmiConnection = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		searchAmiConnection= amiconnectionService.getByAmiuserAndOrganization(amiuser,organization);
    		return status(HttpStatus.OK).body(searchAmiConnection);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(searchAmiConnection);
    	} 	
	}
	
	
	@PostMapping("/createAmiConnectionByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> createAmiConnectionByOrganization(@RequestBody AmiConnectionDTO amiConnectionDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()) && (employee.getOrganization().trim().equals(amiConnectionDTO.getOrganization().trim())))
    	{
    		toReturn = amiconnectionService.createAmiConnectionByOrganization(amiConnectionDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@PostMapping("/updateAmiConnectionByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateAmiConnectionByOrganization(@RequestBody AmiConnectionDTO amiConnectionDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		//System.out.println("Let us update an employee");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = amiconnectionService.updateAmiConnectionByOrganization(amiConnectionDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@DeleteMapping("/deleteAmiConnectionByAmiUserAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> deleteAmiConnectionByAmiUserAndOrganization(@RequestParam String amiuser,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn  = amiconnectionService.deleteAmiConnectionByAmiUserAndOrganization(amiuser, organization);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	}
	
	
	
	    @GetMapping("/export/mylinehubexcel")
	    @ResponseStatus(HttpStatus.CREATED)
	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	    public void exportToExcel(HttpServletResponse response,@RequestHeader (name="Authorization") String token) throws IOException {
	    	
	    	String parentorganization = env.getProperty("spring.parentorginization");
	    	
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	 
	    	if(employee.getOrganization().trim().equals(parentorganization.trim()))
	    	{
	    		amiconnectionService.exportToExcel(response);
	    	}
	    	else
	    	{
	    		
	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    	}
	        
	    }

	    @GetMapping("/export/organization/excel")
	    @ResponseStatus(HttpStatus.CREATED)
	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	    public void exportToExcelOnOrganization(@RequestParam String organization,HttpServletResponse response, @RequestHeader (name="Authorization") String token) throws IOException {

	    	
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		amiconnectionService.exportToExcelOnOrganization(organization,response);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    		Report.addLog("Unauthorized", "Employee needs manager access","AMIConnection", "Cannot Download Excel",organization,logRepository);
	    	} 	
	    	
	    }
	    
	    @GetMapping("/export/mylinehubpdf")
	    @ResponseStatus(HttpStatus.CREATED)
	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	    public void exportToPDF(HttpServletResponse response,@RequestHeader (name="Authorization") String token) throws IOException {
	    	
	    	String parentorganization = env.getProperty("spring.parentorginization");
	    	
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	 
	    	if(employee.getOrganization().trim().equals(parentorganization.trim()))
	    	{
	    		amiconnectionService.exportToPDF(response);
	    	}
	    	else
	    	{
	    		
	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    	}
	        
	    }

	    @GetMapping("/export/organization/pdf")
	    @ResponseStatus(HttpStatus.CREATED)
	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	    public void exportToPDFOnOrganization(@RequestParam String organization,HttpServletResponse response, @RequestHeader (name="Authorization") String token) throws IOException {

	    	
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		amiconnectionService.exportToPDFOnOrganization(organization,response);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    		Report.addLog("Unauthorized", "Employee needs manager access","AMIConnection", "Cannot Download PDF",organization,logRepository);
	    	} 	
	    	
	    }
	    
	    @PostMapping("/upload")
	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam String organization,@RequestParam("file") MultipartFile file, @RequestHeader (name="Authorization") String token) {
	      
	    	//System.out.print("Inside upload File");
	    	String parentorganization = env.getProperty("spring.parentorginization");
	    	
	        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	//System.out.println(file.isEmpty());
	    	//System.out.println(file.getOriginalFilename());
	    	//System.out.println(file.getContentType());
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()) || employee.getOrganization().trim().equals(parentorganization))
	    	{
	  		
	        	String message = "";

	          if (new ExcelHelper().hasExcelFormat(file)) {
	            try {
	            	
	            	//System.out.println("I am inside try");
	            	
	            	amiconnectionService.uploadAmiConnectionsUsingExcel(file,organization);

	            	//System.out.println("I am after employee");
	              message = "Uploaded the file successfully: " + file.getOriginalFilename();
	              return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
	            } catch (Exception e) {
	              message = "Could not upload the file: " + file.getOriginalFilename() + "!";
	              return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
	            }
	          }

	          message = "Please upload an excel file!";
	          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));
	        

	    	}
	    	else
	    	{
	    		String message = "";
	    		//System.out.println("I am in else controller");
	    		return	 ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseMessage(message));
	    		//response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    	} 	
	    }
	
}
