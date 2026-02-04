package com.mylinehub.crm.whatsapp.controller;

import static com.mylinehub.crm.controller.ApiMapping.WHATS_APP_PHONE_NUMBER_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

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
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.whatsapp.dto.EmbeddedSignupResultDto;
import com.mylinehub.crm.whatsapp.dto.WhatsAppPhoneNumberDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppProject;
import com.mylinehub.crm.whatsapp.repository.WhatsAppProjectRepository;
import com.mylinehub.crm.whatsapp.requests.WhatsAppMainControllerRequest;
import com.mylinehub.crm.whatsapp.service.WhatsAppPhoneNumberService;


import lombok.AllArgsConstructor;

@RestController
@RequestMapping(produces="application/json", path = WHATS_APP_PHONE_NUMBER_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class WhatsAppPhoneNumberController {

	private final WhatsAppProjectRepository whatsAppProjectRepository; 
	private final EmployeeRepository employeeRepository; 
	private final WhatsAppPhoneNumberService whatsAppPhoneNumberService; 
	private final Environment env;
	private final JwtConfiguration jwtConfiguration;
	private final SecretKey secretKey;
	
	
	@PostMapping("/embedded-signup/complete")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> completeEmbeddedSignup(
	        @RequestParam String organization,
	        @RequestBody EmbeddedSignupResultDto payload,
	        @RequestHeader(name="Authorization") String token
	) {
	    Boolean toReturn = false;

	    token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    Employee employee = new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

	    if (!employee.getOrganization().trim().equals(organization.trim())) {
	        return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    }

	    if (payload == null || payload.getCode() == null || payload.getCode().isEmpty()) {
	        return status(HttpStatus.BAD_REQUEST).body(toReturn);
	    }

	    // Call service – this will also fetch waba_id/phone_number_id if missing
	    toReturn = whatsAppPhoneNumberService.processEmbeddedSignupCompleteFlow(
	            organization, payload, employee
	    );

	    return status(HttpStatus.OK).body(toReturn);
	}


	@GetMapping("/resetPhoneMemoryDataAsPerPhoneNumber")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> resetPhoneMemoryDataAsPerPhoneNumber(@RequestParam String phoneNumber, @RequestHeader (name="Authorization") String token) throws Exception{
		
		Boolean toReturn = false;
		String parentOrg = env.getProperty("spring.parentorginization");
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	////System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(parentOrg))
    	{
    		return status(HttpStatus.OK).body(whatsAppPhoneNumberService.resetPhoneMemoryDataAsPerPhoneNumber(phoneNumber));
    	}
    	else
    	{
    		////System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
	} 
	
	@PostMapping("/updateAdminEmployeeForWhatsAppNumberByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateAdminEmployeeForWhatsAppNumberByOrganization(@RequestBody WhatsAppMainControllerRequest whatsAppControllerRequest,@RequestHeader (name="Authorization") String token){
		
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	////System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(whatsAppControllerRequest.getOrganization()))
    	{
    		Optional<Employee> admin = employeeRepository.findById(whatsAppControllerRequest.getAdminEmployeeID());
    		
    		if(!admin.isEmpty())
    		whatsAppPhoneNumberService.updateAdminEmployeeForWhatsAppNumberByOrganization(admin.get(), whatsAppControllerRequest.getId());
    		
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		////System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
	} 
	
	
	@PostMapping("/updateEmployeeAccessListByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateEmployeeAccessListByOrganization(@RequestBody WhatsAppMainControllerRequest whatsAppControllerRequest,@RequestHeader (name="Authorization") String token) throws JsonProcessingException{
		
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
        //System.out.println("updateEmployeeAccessListByOrganization");
    	
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(whatsAppControllerRequest.getOrganization()))
    	{
    		whatsAppPhoneNumberService.updateEmployeeAccessListByOrganization(whatsAppControllerRequest.getEmployeeExtensionAccessList(), whatsAppControllerRequest.getId());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		////System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
	} 
	
	@PostMapping("/create")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> create(@RequestBody WhatsAppPhoneNumberDto whatsAppPhoneNumberDto,@RequestHeader (name="Authorization") String token){
		Boolean toReturn = true;
		
		token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	////System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(whatsAppPhoneNumberDto.getOrganization().trim()))
    	{
    		whatsAppPhoneNumberService.create(whatsAppPhoneNumberDto);
    		toReturn= true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		////System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 
	}
	
	@PostMapping("/update")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> update(@RequestParam String oldPhone,@RequestBody WhatsAppPhoneNumberDto whatsAppPhoneNumberDto,@RequestHeader (name="Authorization") String token){
		Boolean toReturn = true;
		
		token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	////System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(whatsAppPhoneNumberDto.getOrganization().trim()))
    	{
    		toReturn= whatsAppPhoneNumberService.update(whatsAppPhoneNumberDto,oldPhone);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		////System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 
	}
	
	
	@DeleteMapping("/delete")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> delete(@RequestParam Long id,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		Boolean toReturn = true;
		
		token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	////System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization))
    	{
    		toReturn= whatsAppPhoneNumberService.delete(id);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		////System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 
	}
	
    @GetMapping("/findAllByEmployeeInExtensionAccessListOrAdmin")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<WhatsAppPhoneNumber>> findAllByEmployeeInExtensionAccessListOrAdmin(@RequestParam String employeeExtension,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
	        
		List<WhatsAppPhoneNumber> toReturn = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println("findAllByEmployeeInExtensionAccessListOrAdmin");
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		try {
    			toReturn= whatsAppPhoneNumberService.findAllByEmployeeInExtensionAccessListOrAdmin(employeeExtension);
    		}
    		catch(Exception e) {
    			e.printStackTrace();
    			throw e;
    		}
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		////System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
    
    @GetMapping("/getAllByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<WhatsAppPhoneNumberDto>> getAllByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<WhatsAppPhoneNumberDto> toReturn = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	////System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn= whatsAppPhoneNumberService.findAllByOrganization(organization);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		////System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
    
    @PostMapping("/getAllByOrganizationAndWhatsAppProject")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<WhatsAppPhoneNumberDto>> getAllByOrganizationAndWhatsAppProject(@RequestBody WhatsAppMainControllerRequest whatsAppControllerRequest,@RequestHeader (name="Authorization") String token){
	        
		List<WhatsAppPhoneNumberDto> toReturn = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	////System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(whatsAppControllerRequest.getOrganization().trim()))
    	{
    		Optional<WhatsAppProject> whatsAppProject = whatsAppProjectRepository.findById(whatsAppControllerRequest.getWhatsappProjectId());
    		
    		if(!whatsAppProject.isEmpty())
    		toReturn= whatsAppPhoneNumberService.getAllByOrganizationAndWhatsAppProject(whatsAppControllerRequest.getOrganization(),whatsAppProject.get());
    		else 
           	return status(HttpStatus.NO_CONTENT).body(toReturn);
    		
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		////System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
    
    
    @PostMapping("/getAllByOrganizationAndWhatsAppProjectAndActive")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<WhatsAppPhoneNumberDto>> getAllByOrganizationAndWhatsAppProjectAndActive(@RequestBody WhatsAppMainControllerRequest whatsAppControllerRequest,@RequestHeader (name="Authorization") String token){
	        
		List<WhatsAppPhoneNumberDto> toReturn = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	////System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(whatsAppControllerRequest.getOrganization().trim()))
    	{
    		Optional<WhatsAppProject> whatsAppProject = whatsAppProjectRepository.findById(whatsAppControllerRequest.getWhatsappProjectId());
    		
    		if(!whatsAppProject.isEmpty())
    		toReturn= whatsAppPhoneNumberService.getAllByOrganizationAndWhatsAppProjectAndActive(whatsAppControllerRequest.getOrganization(),whatsAppProject.get(),whatsAppControllerRequest.isActive());
    		else 
           	return status(HttpStatus.NO_CONTENT).body(toReturn);
    		
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		////System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
    
    
    @GetMapping("/getAllByOrganizationAndAdmin")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<WhatsAppPhoneNumberDto>> getAllByOrganizationAndAdmin(@RequestParam Long adminEmployeeId,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<WhatsAppPhoneNumberDto> toReturn = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	////System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		Optional<Employee> admin  = employeeRepository.findById(adminEmployeeId);
    		
    		if(!admin.isEmpty())
    		toReturn= whatsAppPhoneNumberService.getAllByOrganizationAndAdmin(organization,admin.get());
    		else 
           	return status(HttpStatus.NO_CONTENT).body(toReturn);
    		
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		////System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
}
