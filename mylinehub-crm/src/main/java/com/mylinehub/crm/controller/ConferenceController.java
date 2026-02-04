package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.CONFERENCE_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.io.IOException;
import java.util.List;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletResponse;

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
import com.mylinehub.crm.entity.dto.ConferenceDTO;
import com.mylinehub.crm.exports.ExcelHelper;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.LogRepository;
import com.mylinehub.crm.requests.ExtensionRequest;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.ConferenceService;
import com.mylinehub.crm.utils.ResponseMessage;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping(produces="application/json", path = CONFERENCE_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class ConferenceController {

    private final EmployeeRepository employeeRepository;
    private final ConferenceService conferenceService;
    private final LogRepository logRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
	private Environment env;
	
	
	@PostMapping("/enableConferenceOnExtensionAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> enableConferenceOnExtensionAndOrganization(@RequestBody ExtensionRequest request,@RequestHeader (name="Authorization") String token){
	    
			
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		conferenceService.enableConferenceOnOrganization(request.getExtension(), request.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 		
	} 
	
	
	@PostMapping("/disableConferenceOnExtensionAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> disableConferenceOnExtensionAndOrganization(@RequestBody ExtensionRequest request,@RequestHeader (name="Authorization") String token){
	    
			
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		conferenceService.disableConferenceOnOrganization(request.getExtension(), request.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 		
	} 
	
	
	@GetMapping("/getAllConferenceByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<ConferenceDTO>> getAllConferenceByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<ConferenceDTO> ivrs = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		ivrs= conferenceService.getAllConferenceOnOrganization(organization);
    		return status(HttpStatus.OK).body(ivrs);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(ivrs);
    	} 	
	}
	
	@GetMapping("/getAllConferenceOnPhoneContextAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<ConferenceDTO>> getAllConferenceOnPhoneContextAndOrganization(@RequestParam String phoneContext,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<ConferenceDTO> ivrs = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		ivrs= conferenceService.getAllConferenceOnPhoneContextAndOrganization(phoneContext,organization);
    		return status(HttpStatus.OK).body(ivrs);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(ivrs);
    	} 	
	}
	
	@GetMapping("/getAllConferenceOnIsEnabledAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<ConferenceDTO>> getAllConferenceOnIsEnabledAndOrganization(@RequestParam Boolean isEnabled,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<ConferenceDTO> ivrs = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		ivrs= conferenceService.getAllConferenceOnIsEnabledAndOrganization(isEnabled,organization);
    		return status(HttpStatus.OK).body(ivrs);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(ivrs);
    	} 	
	}
	
	
	@GetMapping("/getConferenceByExtensionAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<ConferenceDTO> getConferenceByExtensionAndOrganization(@RequestParam String extension,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		ConferenceDTO searchIvr = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		searchIvr= conferenceService.getByExtensionAndOrganization(extension,organization);
    		return status(HttpStatus.OK).body(searchIvr);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(searchIvr);
    	} 	
	}
	
	
	@PostMapping("/createConferenceByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> createConferenceByOrganization(@RequestBody ConferenceDTO ConferenceDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim())  && (employee.getOrganization().trim().equals(ConferenceDTO.getOrganization().trim())))
    	{
    		toReturn = conferenceService.createConferenceByOrganization(ConferenceDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@PostMapping("/updateConferenceByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateConferenceByOrganization(@RequestBody ConferenceDTO ConferenceDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		//System.out.println("Let us update an employee");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = conferenceService.updateConferenceByOrganization(ConferenceDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@DeleteMapping("/deleteConferenceByExtensionAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> deleteConferenceByExtensionAndOrganization(@RequestParam String extension,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn  = conferenceService.deleteConferenceByExtensionAndOrganization(extension, organization);
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
	    		conferenceService.exportToExcel(response);
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
	    		conferenceService.exportToExcelOnOrganization(organization,response);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    		Report.addLog("Unauthorized", "Employee needs manager access","Conference", "Cannot Download Excel",organization,logRepository);	
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
	    		conferenceService.exportToPDF(response);
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
	    		conferenceService.exportToPDFOnOrganization(organization,response);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    		Report.addLog("Unauthorized", "Employee needs manager access","Conference", "Cannot Download PDF",organization,logRepository);	
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
	            	
	            	conferenceService.uploadConferencesUsingExcel(file,organization);

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