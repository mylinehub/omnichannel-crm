package com.mylinehub.crm.whatsapp.controller;

import static com.mylinehub.crm.controller.ApiMapping.WHATS_APP_PHONE_REPORT_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.util.List;
import javax.crypto.SecretKey;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.whatsapp.dto.WhatsAppMessageCountForNumberDTO;
import com.mylinehub.crm.whatsapp.dto.WhatsAppNumberReportDto;
import com.mylinehub.crm.whatsapp.dto.WhatsAppReportNumberResponseDTO;
import com.mylinehub.crm.whatsapp.dto.WhatsAppReportVariableDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.requests.WhatsAppMainControllerRequest;
import com.mylinehub.crm.whatsapp.requests.WhatsAppReportRequest;
import com.mylinehub.crm.whatsapp.service.WhatsAppNumberReportService;
import com.mylinehub.crm.whatsapp.service.WhatsAppPhoneNumberService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(produces="application/json", path = WHATS_APP_PHONE_REPORT_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class WhatsAppNumberReportController {
	
	private final WhatsAppNumberReportService whatsAppNumberReportService; 
	private final WhatsAppPhoneNumberService whatsAppPhoneNumberService;
	private final JwtConfiguration jwtConfiguration;
	private final EmployeeRepository employeeRepository;
	private final SecretKey secretKey;
	    
//	@PostMapping("/create")
//	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//	public ResponseEntity<Boolean> create(@RequestBody WhatsAppNumberReportDto whatsAppNumberReportDto,@RequestHeader (name="Authorization") String token){
//		Boolean toReturn = true;
//		
//		token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//    	//System.out.println(token);
//    	
//    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//    	
//    	if(employee.getOrganization().trim().equals(whatsAppNumberReportDto.getOrganization().trim()))
//    	{
//    		toReturn= whatsAppNumberReportService.create(whatsAppNumberReportDto);
//    		return status(HttpStatus.OK).body(toReturn);
//    	}
//    	else
//    	{
//    		//System.out.println("I am in else controller");
//    		
//    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
//    	} 
//	}
//	
//	@PostMapping("/update")
//	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//	public ResponseEntity<Boolean> update(@RequestBody WhatsAppNumberReportDto whatsAppNumberReportDto,@RequestHeader (name="Authorization") String token){
//		Boolean toReturn = true;
//		
//		token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//    	//System.out.println(token);
//    	
//    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//    	
//    	if(employee.getOrganization().trim().equals(whatsAppNumberReportDto.getOrganization().trim()))
//    	{
//    		toReturn= whatsAppNumberReportService.update(whatsAppNumberReportDto);
//    		return status(HttpStatus.OK).body(toReturn);
//    	}
//    	else
//    	{
//    		//System.out.println("I am in else controller");
//    		
//    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
//    	} 
//	}
//	
//	
//	@DeleteMapping("/delete")
//	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//	public ResponseEntity<Boolean> delete(@RequestParam Long id,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
//		Boolean toReturn = true;
//		
//		token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//    	//System.out.println(token);
//    	
//    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//    	
//    	if(employee.getOrganization().trim().equals(organization))
//    	{
//    		toReturn= whatsAppNumberReportService.delete(id);
//    		return status(HttpStatus.OK).body(toReturn);
//    	}
//    	else
//    	{
//    		//System.out.println("I am in else controller");
//    		
//    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
//    	} 
//	}
//	
	
	
//	@GetMapping("/saveDataFromMemoryToDatabase")
//	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//	public ResponseEntity<Boolean> saveDataFromMemoryToDatabase(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
//	        
//		Boolean toReturn = false;
//		
//    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//    	
//    	//System.out.println(token);
//    	
//    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//    	
//    	if(employee.getOrganization().trim().equals(organization.trim()))
//    	{
//    		whatsAppNumberReportService.saveDataFromMemoryToDatabase();
//    		toReturn = true;
//    		return status(HttpStatus.OK).body(toReturn);
//    	}
//    	else
//    	{
//    		//System.out.println("I am in else controller");
//    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
//    	} 	
//	}
	
	
    @GetMapping("/getAllByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<WhatsAppNumberReportDto>> getAllByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<WhatsAppNumberReportDto> toReturn = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn= whatsAppNumberReportService.findAllByOrganization(organization);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
    
    @PostMapping("/findAllByWhatsAppPhoneNumberAndOrganization")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<List<WhatsAppNumberReportDto>> findAllByWhatsAppPhoneNumberAndOrganization(@RequestBody WhatsAppMainControllerRequest whatsAppControllerRequest,@RequestHeader (name="Authorization") String token){
   	        
   		List<WhatsAppNumberReportDto> toReturn = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	//System.out.println(token);
       	
       	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	
       	if(employee.getOrganization().trim().equals(whatsAppControllerRequest.getOrganization().trim()))
       	{
       		WhatsAppPhoneNumber whatsAppPhoneNumber = whatsAppPhoneNumberService.findByPhoneNumber(whatsAppControllerRequest.getPhoneNumber());
       		
       		if(whatsAppPhoneNumber != null)
       		toReturn= whatsAppNumberReportService.findAllByPhoneNumberMainAndOrganization(whatsAppPhoneNumber.getPhoneNumber(),whatsAppControllerRequest.getOrganization());
       		else 
           	return status(HttpStatus.NO_CONTENT).body(toReturn);
       		
       		return status(HttpStatus.OK).body(toReturn);
       	}
       	else
       	{
       		//System.out.println("I am in else controller");
       		
       		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
       	} 	
   	}
       
    
    @PostMapping("/findAllByDayUpdatedGreaterThanEqualAndWhatsAppPhoneNumberAndOrganization")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<List<WhatsAppNumberReportDto>> findAllByDayUpdatedGreaterThanEqualAndWhatsAppPhoneNumberAndOrganization(@RequestBody WhatsAppMainControllerRequest whatsAppControllerRequest,@RequestHeader (name="Authorization") String token){
   	        
   		List<WhatsAppNumberReportDto> toReturn = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	//System.out.println(token);
       	
       	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	
       	if(employee.getOrganization().trim().equals(whatsAppControllerRequest.getOrganization().trim()))
       	{
       		WhatsAppPhoneNumber whatsAppPhoneNumber = whatsAppPhoneNumberService.findByPhoneNumber(whatsAppControllerRequest.getPhoneNumber());
       		
       		if(whatsAppPhoneNumber != null)
       		toReturn= whatsAppNumberReportService.findAllByDayUpdatedGreaterThanEqualAndPhoneNumberMainAndOrganization(whatsAppControllerRequest.getDayUpdated(),whatsAppPhoneNumber.getPhoneNumber(),whatsAppControllerRequest.getOrganization());
       		else 
           	return status(HttpStatus.NO_CONTENT).body(toReturn);
       		
       		return status(HttpStatus.OK).body(toReturn);
       	}
       	else
       	{
       		//System.out.println("I am in else controller");
       		
       		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
       	} 	
   	}
       
    
    @PostMapping("/findAllByDayUpdatedGreaterThanEqualAndWhatsAppPhoneNumberAndTypeOfReportAndOrganization")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<List<WhatsAppNumberReportDto>> findAllByDayUpdatedGreaterThanEqualAndWhatsAppPhoneNumberAndTypeOfReportAndOrganization(@RequestBody WhatsAppMainControllerRequest whatsAppControllerRequest,@RequestHeader (name="Authorization") String token){
   	        
   		List<WhatsAppNumberReportDto> toReturn = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	//System.out.println(token);
       	
       	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	
       	if(employee.getOrganization().trim().equals(whatsAppControllerRequest.getOrganization().trim()))
       	{
       		WhatsAppPhoneNumber whatsAppPhoneNumber = whatsAppPhoneNumberService.findByPhoneNumber(whatsAppControllerRequest.getPhoneNumber());
       		
       		if(whatsAppPhoneNumber != null)
       		toReturn= whatsAppNumberReportService.findAllByDayUpdatedGreaterThanEqualAndPhoneNumberMainAndTypeOfReportAndOrganization(whatsAppControllerRequest.getDayUpdated(),whatsAppPhoneNumber.getPhoneNumber(),whatsAppControllerRequest.getTypeOfReport(),whatsAppControllerRequest.getOrganization());
       		else 
           	return status(HttpStatus.NO_CONTENT).body(toReturn);
       		
       		return status(HttpStatus.OK).body(toReturn);
       	}
       	else
       	{
       		//System.out.println("I am in else controller");
       		
       		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
       	} 	
   	}
    
    
    
    @PostMapping("/getReportCountForDashboard")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<WhatsAppReportVariableDto> getReportCountForDashboard(@RequestBody WhatsAppReportRequest whatsAppReportRequest,@RequestHeader (name="Authorization") String token){
   	        
    	WhatsAppReportVariableDto toReturn = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	//System.out.println(token);
       	
       	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	
       	if(employee.getOrganization().trim().equals(whatsAppReportRequest.getOrganization().trim()))
       	{
       		toReturn= whatsAppNumberReportService.getReportCountForDashboard(whatsAppReportRequest);
       		return status(HttpStatus.OK).body(toReturn);
       	}
       	else
       	{
       		//System.out.println("I am in else controller");
       		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
       	} 	
   	}
    
    
    @PostMapping("/getReportCountForDashboardForNumber")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<List<WhatsAppMessageCountForNumberDTO>> getReportCountForDashboardForNumber(@RequestBody WhatsAppReportRequest whatsAppReportRequest,@RequestHeader (name="Authorization") String token){
   	        
    	List<WhatsAppMessageCountForNumberDTO> toReturn = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	//System.out.println(token);
       	
       	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	
       	if(employee.getOrganization().trim().equals(whatsAppReportRequest.getOrganization().trim()))
       	{
       		toReturn= whatsAppNumberReportService.getReportCountForDashboardForNumber(whatsAppReportRequest);
       		return status(HttpStatus.OK).body(toReturn);
       	}
       	else
       	{
       		//System.out.println("I am in else controller");
       		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
       	} 	
   	}
       
    
    @PostMapping("/getReportCountForDashboardForNumberByTime")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<List<WhatsAppReportNumberResponseDTO>> getReportCountForDashboardForNumberByTime(@RequestBody WhatsAppReportRequest whatsAppReportRequest,@RequestHeader (name="Authorization") String token){
   	        
    	List<WhatsAppReportNumberResponseDTO> toReturn = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	//System.out.println(token);
       	
       	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	
       	if(employee.getOrganization().trim().equals(whatsAppReportRequest.getOrganization().trim()))
       	{
       		toReturn= whatsAppNumberReportService.getReportCountForDashboardForNumberByTime(whatsAppReportRequest);
       		return status(HttpStatus.OK).body(toReturn);
       	}
       	else
       	{
       		//System.out.println("I am in else controller");
       		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
       	} 	
   	}
    
}
