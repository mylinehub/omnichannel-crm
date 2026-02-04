package com.mylinehub.crm.controller;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.mylinehub.crm.ami.TaskScheduler.RefreshBackEndConnectionRunnable;
import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.BulkUploadEmployeeDto;
import com.mylinehub.crm.entity.dto.EmployeeBasicInfoDTO;
import com.mylinehub.crm.entity.dto.EmployeeDTO;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.entity.dto.SshConnectionDTO;
import com.mylinehub.crm.enums.COST_CALCULATION;
import com.mylinehub.crm.enums.METERED_PLANS_AMOUNT;
import com.mylinehub.crm.enums.UNLIMITED_PLANS_AMOUNT;
import com.mylinehub.crm.enums.USER_ROLE;
import com.mylinehub.crm.exports.ExcelHelper;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.repository.LogRepository;
import com.mylinehub.crm.requests.BooleanValueRequest;
import com.mylinehub.crm.requests.CallRecordingListRequest;
import com.mylinehub.crm.requests.EmailRequest;
import com.mylinehub.crm.requests.PasswordRequest;
import com.mylinehub.crm.requests.StringValueRequest;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.EmployeeService;
import com.mylinehub.crm.service.FileService;
import com.mylinehub.crm.service.SshConnectionService;
import com.mylinehub.crm.utils.ResponseMessage;
import com.mylinehub.shh.SshWrapper;

import lombok.AllArgsConstructor;

import org.asteriskjava.manager.TimeoutException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;
import java.io.IOException;

import static com.mylinehub.crm.controller.ApiMapping.EMPLOYEES_REST_URL;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping(produces="application/json", path = EMPLOYEES_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final SshConnectionService sshConnectionService;
    private final EmployeeRepository employeeRepository;
    private final ErrorRepository errorRepository;
    private final ApplicationContext applicationContext;
    private final LogRepository logRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
	private Environment env;
	private final FileService fileService;
	
	/*@GetMapping("/sendWelcomeEmail")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> sendWelcomeEmail(@RequestParam String organization,@RequestParam String email,@RequestHeader (name="Authorization") String token){
	        
		Boolean toReturn= false;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	System.out.println("Inside getAllCallingCostOnOrganizationViaEmail");
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		
    		Boolean result = employeeService.sendWelcomeEmail(email,organization);
        	if(result)
        	{
        		return status(HttpStatus.OK).body(toReturn);
    		}
    		else
    		{
    			return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    		}
    		
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	*/
	
	
	@GetMapping("/getAllMeteredPlanAmount")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllMeteredPlanAmount(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(METERED_PLANS_AMOUNT.class).forEach(value ->allValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allValues);
    	} 	
	}
	
	@GetMapping("/getAllCostCalcultationType")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllCostCalcultationType(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(COST_CALCULATION.class).forEach(value ->allValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allValues);
    	} 	
	}
	
	@GetMapping("/getAllUnlimitedPlanAmount")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllUnlimitedPlanAmount(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(UNLIMITED_PLANS_AMOUNT.class).forEach(value ->allValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allValues);
    	} 	
	}
	
	
	@GetMapping("/getAllEmployeesBasicInfoByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<EmployeeBasicInfoDTO>> getAllEmployeesBasicInfoByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<EmployeeBasicInfoDTO> employees = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		employees= employeeService.getAllEmployeesBasicInfoByOrganization(organization,employee);
    		return status(HttpStatus.OK).body(employees);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(employees);
    	} 	
	}
	
	@GetMapping("/getAllEmployeesByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<EmployeeDTO>> getAllEmployeesByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<EmployeeDTO> employees = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		employees= employeeService.getAllEmployeesByOrganization((organization));
    		return status(HttpStatus.OK).body(employees);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(employees);
    	} 	
	}
	 

	@GetMapping("/getAllEmployeesOnPhoneContextAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<EmployeeDTO>> getAllEmployeesOnPhoneContextAndOrganization(@RequestParam String phoneContext,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<EmployeeDTO> employees = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		employees= employeeService.getAllEmployeesOnPhoneContextAndOrganization(phoneContext,organization);
    		return status(HttpStatus.OK).body(employees);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(employees);
    	} 	
	}
	
	
	@GetMapping("/getAllEmployeesOnSexAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<EmployeeDTO>> getAllEmployeesOnSexAndOrganization(@RequestParam String sex,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<EmployeeDTO> employees = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		employees= employeeService.getAllEmployeesOnSexAndOrganization(sex,organization);
    		return status(HttpStatus.OK).body(employees);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(employees);
    	} 	
	}
	
	
	@GetMapping("/getAllEmployeesOnUserRoleAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<EmployeeDTO>> getAllEmployeesOnUserRoleAndOrganization(@RequestParam String role,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<EmployeeDTO> employees = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    	
    	//System.out.println("User Role : "+userRole);
    		if(USER_ROLE.ADMIN.name().equalsIgnoreCase(role))
      	  {
    			//System.out.println("User Role -IF-ELSE- Admin");
    			
    			employees= employeeService.getAllEmployeesOnUserRoleAndOrganization(USER_ROLE.ADMIN,organization);
        		return status(HttpStatus.OK).body(employees);
      	  }
      	  else if(USER_ROLE.EMPLOYEE.name().equalsIgnoreCase(role)) 
      	  {
      		//System.out.println("User Role -IF-ELSE- EMPLOYEE");
      		
      		employees= employeeService.getAllEmployeesOnUserRoleAndOrganization(USER_ROLE.EMPLOYEE,organization);
    		return status(HttpStatus.OK).body(employees);
      	  }
      	  else if(USER_ROLE.MANAGER.name().equalsIgnoreCase(role))  
      	  {
      		  
      		//System.out.println("User Role -IF-ELSE- MANAGER");
      		employees= employeeService.getAllEmployeesOnUserRoleAndOrganization(USER_ROLE.MANAGER,organization);
    		return status(HttpStatus.OK).body(employees);
      		 
      	  }
    		//System.out.println("User Role -IF-ELSE- Bad Request");
    		return status(HttpStatus.BAD_REQUEST).body(employees);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(employees);
    	} 	
	}
	
	@GetMapping("/getAllEmployeesOnIsEnabledAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<EmployeeDTO>> getAllEmployeesOnIsEnabledAndOrganization(@RequestParam Boolean isEnabled,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<EmployeeDTO> employees = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		employees= employeeService.getAllEmployeesOnIsEnabledAndOrganization(isEnabled,organization);
    		return status(HttpStatus.OK).body(employees);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(employees);
    	} 	
	}
	
	@GetMapping("/getEmployeeByEmailAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<EmployeeDTO> getEmployeeByEmailAndOrganization(@RequestParam String email,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		EmployeeDTO searchEmployee = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		searchEmployee= employeeService.getEmployeeByEmailAndOrganization(email,organization);
    		return status(HttpStatus.OK).body(searchEmployee);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(searchEmployee);
    	} 	
	}
	
	@GetMapping("/getEmployeeByExtensionAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<EmployeeDTO> getEmployeeByExtensionAndOrganization(@RequestParam String extension,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		EmployeeDTO searchEmployee = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		searchEmployee= employeeService.getEmployeeByExtensionAndOrganization(extension,organization);
    		return status(HttpStatus.OK).body(searchEmployee);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(searchEmployee);
    	} 	
	}
	
	
	@GetMapping("/findAllBycostCalculationAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<EmployeeDTO>> findAllBycostCalculationAndOrganization(@RequestParam String costCalculation,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<EmployeeDTO> searchEmployees = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		searchEmployees= employeeService.findAllBycostCalculationAndOrganization(costCalculation,organization);
    		return status(HttpStatus.OK).body(searchEmployees);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(searchEmployees);
    	} 	
	}
	
	@GetMapping("/getEmployeeByPhonenumberAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<EmployeeDTO> getEmployeeByPhonenumberAndOrganization(@RequestParam String phonenumber,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		
		phonenumber = phonenumber.trim();
		CharSequence s = "+";
		if(!phonenumber.contains(s))
		{
			//System.out.println("Inside '+' logic");
			phonenumber = "+"+phonenumber.trim();
		}
		
	    
		//System.out.println("Phone Number : "+phonenumber);
		//System.out.println("Organization : "+organization);
			
		EmployeeDTO searchEmployee = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		//System.out.println("Searching Employee");
    		
    		searchEmployee= employeeService.getEmployeeByPhonenumberAndOrganization(phonenumber,organization);
    		
    		
    		//System.out.println("Search Employee : "+String.valueOf(searchEmployee));
    		
    		return status(HttpStatus.OK).body(searchEmployee);
    		
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(searchEmployee);
    	} 	
	}
	
	
    
	
	@PostMapping("/enableUserOnEmailAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> enableUserOnEmailAndOrganization(@RequestBody EmailRequest emailRequest,@RequestHeader (name="Authorization") String token){
	    
		System.out.println("Email : "+emailRequest.getEmail());
		System.out.println("Organization : "+emailRequest.getOrganization());
		
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(emailRequest.getOrganization()))
    	{
    		employeeService.enableUserOnOrganization(emailRequest.getEmail(), emailRequest.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	} 
	
	
	@PostMapping("/disableUserOnEmailAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> disableUserOnEmailAndOrganization(@RequestBody EmailRequest emailRequest,@RequestHeader (name="Authorization") String token){
	    
		//System.out.println("Email : ");

		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(emailRequest.getOrganization()))
    	{
    		employeeService.disableUserOnOrganization(emailRequest.getEmail(),emailRequest.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	} 
	
	
	@PostMapping("/enableUseAllotedSecondLineByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> enableUseAllotedSecondLineByOrganization(@RequestBody EmailRequest emailRequest,@RequestHeader (name="Authorization") String token){
	    
		System.out.println("Email : "+emailRequest.getEmail());
		System.out.println("Organization : "+emailRequest.getOrganization());
		
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(emailRequest.getOrganization()))
    	{
    		employeeService.enableUseAllotedSecondLineByOrganization(emailRequest.getEmail(), emailRequest.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
	} 
	
	
	@PostMapping("/disableUseAllotedSecondLineByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> disableUseAllotedSecondLineByOrganization(@RequestBody EmailRequest emailRequest,@RequestHeader (name="Authorization") String token){
	    
		//System.out.println("Email : ");

		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(emailRequest.getOrganization()))
    	{
    		employeeService.disableUseAllotedSecondLineByOrganization(emailRequest.getEmail(),emailRequest.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	} 
	
	
	@PostMapping("/enableSelfCallOnMobile")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> enableSelfCallOnMobile(@RequestBody EmailRequest emailRequest,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(emailRequest.getOrganization().trim()) && employee.getEmail().equals(emailRequest.getEmail()))
    	{
    		employeeService.enableUserCallOnMobileByOrganization(employee.getEmail(), emailRequest.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	} 
	
	@PostMapping("/disableSelfCallOnMobile")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> disableSelfCallOnMobile(@RequestBody EmailRequest emailRequest,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(emailRequest.getOrganization()) && employee.getEmail().equals(emailRequest.getEmail()))
    	{
    		employeeService.disableUserCallOnMobileByOrganization(employee.getEmail(), emailRequest.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	@PostMapping("/enableEmployeeCallOnMobile")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> enableEmployeeCallOnMobile(@RequestBody EmailRequest emailRequest,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(emailRequest.getOrganization().trim()))
    	{
    		employeeService.enableUserCallOnMobileByOrganization(emailRequest.getEmail(), emailRequest.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	
	} 
	
	
	@PostMapping("/disableEmployeeCallOnMobile")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> disableEmployeeCallOnMobile(@RequestBody EmailRequest emailRequest,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(emailRequest.getOrganization().trim()))
    	{
    		employeeService.disableUserCallOnMobileByOrganization(emailRequest.getEmail(), emailRequest.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	
	} 
	
	@PostMapping("/updateNotificationDotStatusByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateNotificationDotStatusByOrganization(@RequestBody BooleanValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateNotificationDotStatusByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	
	@PostMapping("/updateUserAllowedToSwitchOffWhatsAppAIByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateUserAllowedToSwitchOffWhatsAppAIByOrganization(@RequestBody BooleanValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		employeeService.updateUserAllowedToSwitchOffWhatsAppAIByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	@PostMapping("/updateEmployeeRecordAllCallsByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateEmployeeRecordAllCallsByOrganization(@RequestBody BooleanValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		employeeService.updateUserRecordAllCallsByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	
	@PostMapping("/updateSelfDoNotDisturbByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfDoNotDisturbByOrganization(@RequestBody BooleanValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserDoNotDisturbByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	
	@PostMapping("/updateSelfStartVideoFullScreenByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfStartVideoFullScreenByOrganization(@RequestBody BooleanValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserStartVideoFullScreenByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	
	@PostMapping("/updateSelfCallWaitingByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateUserCallWaitingByOrganization(@RequestBody BooleanValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserCallWaitingByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	@PostMapping("/updateEmployeeIntercomPolicyByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateEmployeeIntercomPolicyByOrganization(@RequestBody BooleanValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		employeeService.updateUserIntercomPolicyByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	@PostMapping("/updateEmployeeFreeDialOptionByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateEmployeeFreeDialOptionByOrganization(@RequestBody BooleanValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		employeeService.updateUserFreeDialOptionByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	@PostMapping("/updateEmployeeTextDictationByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateEmployeeTextDictationByOrganization(@RequestBody BooleanValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		employeeService.updateUserTextDictationByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	@PostMapping("/updateEmployeeTextMessagingByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateEmployeeTextMessagingByOrganization(@RequestBody BooleanValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		employeeService.updateUserTextMessagingByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	
	
	
	@PostMapping("/updateSelfUiThemeByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfUiThemeByOrganization(@RequestBody StringValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserUiThemeByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	
	@PostMapping("/updateSelfAutoAnswerByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfAutoAnswerByOrganization(@RequestBody BooleanValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserAutoAnswerByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	

	@PostMapping("/updateSelfAutoConferenceByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfAutoConferenceByOrganization(@RequestBody BooleanValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserAutoConferenceByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	
	
	@PostMapping("/updateSelfAutoVideoByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfAutoVideoByOrganization(@RequestBody BooleanValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserAutoVideoByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	
	@PostMapping("/updateSelfMicDeviceByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfMicDeviceByOrganization(@RequestBody StringValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserMicDeviceByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 

	
	@PostMapping("/updateSelfSpeakerDeviceByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfSpeakerDeviceByOrganization(@RequestBody StringValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserSpeakerDeviceByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	@PostMapping("/updateSelfVideoDeviceByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfVideoDeviceByOrganization(@RequestBody StringValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserVideoDeviceByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	@PostMapping("/updateSelfVideoOrientationByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfVideoOrientationByOrganization(@RequestBody StringValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserVideoOrientationByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	@PostMapping("/updateSelfVideoQualityByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfVideoQualityByOrganization(@RequestBody StringValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserVideoQualityByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	@PostMapping("/updateSelfVideoFrameRateByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfVideoFrameRateByOrganization(@RequestBody StringValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserVideoFrameRateByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	@PostMapping("/updateSelfAutoGainControlByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfAutoGainControlByOrganization(@RequestBody StringValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserAutoGainControlByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	@PostMapping("/updateSelfEchoCancellationByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfEchoCancellationByOrganization(@RequestBody StringValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserEchoCancellationByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	@PostMapping("/updateSelfNoiseSupressionByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfNoiseSupressionByOrganization(@RequestBody StringValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()) && employee.getEmail().equals(request.getEmail()))
    	{
    		employeeService.updateUserNoiseSupressionByOrganization(request.getEmail(), request.getOrganization(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	@PostMapping("/updateSelfWebPassword")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfWebPassword(@RequestBody PasswordRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization().trim()) && employee.getEmail().equals(request.getEmail()))
    	{
    		int value = employeeService.updateWebPassword(request.getPassword(), employee.getEmail(), request.getOrganization());
    		
    		if(value==0)
    		{
    			toReturn = false;
    		}
    		else
    		{
    			toReturn = true;
    		}
    		
    		try
    		{
    			employeeService.sendPasswordResetEmail(request.getEmail(),request.getPassword(),request.getOrganization());
    			
    			String passcodeTemplate = env.getProperty("spring.template.passwordrecovery");
    			BulkUploadEmployeeDto bulkUploadEmployeeDto = new BulkUploadEmployeeDto();
        		bulkUploadEmployeeDto.setActualPassword(request.getPassword());
        		bulkUploadEmployeeDto.setEmployee(employee);
        		employeeService.sendEmployeeSpecificWhatsAppMessageAsPerTemplateName(bulkUploadEmployeeDto,passcodeTemplate,null,"0","url");
   			 
    		}
    		catch(Exception e)
    		{
    			 Report.addError("While sending whatsapp : ", e.getMessage(),"Employee Controller", "Cannot send email",request.getOrganization(),errorRepository);	
					
    		}
    		
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	
	} 
	
	@PostMapping("/updateSelfExtensionPassword")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfExtensionPassword(@RequestBody PasswordRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization().trim()) && employee.getEmail().equals(request.getEmail()))
    	{
    		int value = employeeService.updateExtentionPassword(request.getPassword(), employee.getEmail(), request.getOrganization());
    		if(value==0)
    		{
    			toReturn = false;
    		}
    		else
    		{
    			toReturn = true;
    		}
    		
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	
	
	@PostMapping("/updateWebPassword")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateWebPassword(@RequestBody PasswordRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
        //System.out.println(request.getEmail());
        //System.out.println(request.getOrganization());
        //System.out.println(request.getPassword());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization().trim()))
    	{
    		int value = employeeService.updateWebPassword(request.getPassword(), request.getEmail(), request.getOrganization());

    		if(value==0)
    		{
    			toReturn = false;
    		}
    		else
    		{
    			toReturn = true;
    		}
    		
    		try
    		{
    			employeeService.sendPasswordResetEmail(request.getEmail(),request.getPassword(),request.getOrganization());
    		}
    		catch(Exception e)
    		{
    			 Report.addError("While sending email", e.getMessage(),"Employee Controller", "Cannot send email",request.getOrganization(),errorRepository);	
					
    		}
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	
	} 
	
	@PostMapping("/updateExtensionPassword")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateExtensionPassword(@RequestBody PasswordRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization().trim()))
    	{
    		int value = employeeService.updateExtentionPassword(request.getPassword(), request.getEmail(), request.getOrganization());
    		
    		if(value==0)
    		{
    			toReturn = false;
    		}
    		else
    		{
    			toReturn = true;
    		}
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}	
	} 
	
	
	
	
	@PostMapping("/updateEmployeeByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateEmployeeByOrganization(@RequestBody EmployeeDTO employeeDTO,@RequestParam String oldEmail, @RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		//System.out.println("Let us update an employee");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = employeeService.updateEmployeeByOrganization(employeeDTO,oldEmail);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	
	
	@PostMapping("/updateSelfByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSelfByOrganization(@RequestBody EmployeeDTO employeeDTO,@RequestParam String oldEmail, @RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
	    
		//System.out.println("Let us update an employee");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
       //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()) && employee.getEmail().equals(employeeDTO.getEmail()))
    	{
    		toReturn = employeeService.updateSelfByOrganization(employeeDTO,oldEmail);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@PostMapping("/createEmployeeByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> createEmployeeByOrganization(@RequestBody EmployeeDTO employeeDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim())  && (employee.getOrganization().trim().equals(employeeDTO.getOrganization().trim())))
    	{
    		toReturn= employeeService.createEmployeeByOrganization(employeeDTO);
    		
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	
	@PostMapping("/createEmployeeByOrganizationUsingParent")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> createEmployeeByOrganizationUsingParent(@RequestBody EmployeeDTO employeeDTO,@RequestHeader (name="Authorization") String token) throws Exception{
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(applicationContext.getEnvironment().getProperty("spring.parentorginization")))
    	{
    		toReturn= employeeService.createEmployeeByOrganization(employeeDTO);

    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	
	/*
	
	@DeleteMapping("/deleteEmployeeByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> deleteEmployeeByEmailAndOrganization(@RequestParam String email,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	} 
	
*/
	

	
	@PostMapping("/getAllRecordingDataForEmployee")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Vector<ChannelSftp.LsEntry>> getAllRecordingDataForEmployee(@RequestBody CallRecordingListRequest request,@RequestHeader (name="Authorization") String token) throws Exception{
	    
		System.out.println("getAllRecordingDataForEmployee");
		
		SshWrapper sshWrapper = new SshWrapper();
		Channel channel = SshWrapper.allChannel.get(request.getOrganization());
		Vector<ChannelSftp.LsEntry> toReturn = null;
		Vector<ChannelSftp.LsEntry> lastReturn = new Vector<ChannelSftp.LsEntry>();
		String path;
		
		System.out.println(String.valueOf(request));
		
		if(request.getMonth()< 10)
		{
			if(request.getDay() < 10)
			{
				path = "/var/spool/asterisk/monitor/"+request.getYear()+"/0"+request.getMonth()+"/0"+request.getDay()+"/";
				
			}
			else
			{
				path = "/var/spool/asterisk/monitor/"+request.getYear()+"/0"+request.getMonth()+"/"+request.getDay()+"/";
			}
			
			
		}
		else
		{
			if(request.getDay() < 10)
			{
				path = "/var/spool/asterisk/monitor/"+request.getYear()+"/"+request.getMonth()+"/0"+request.getDay()+"/";
				
			}
			else
			{
				path = "/var/spool/asterisk/monitor/"+request.getYear()+"/"+request.getMonth()+"/"+request.getDay()+"/";
			}
			
		}
		
		System.out.println("Path : "+ path);
		
		EmployeeDTO searchEmployeeDTO= employeeService.getEmployeeByExtensionAndOrganization(request.getExtension(),request.getOrganization());
		
		if(searchEmployeeDTO != null)
		{
			if(channel == null || channel.isClosed())
			{
				
				System.out.println("Channel is null");
				//Channel Not set up. Let us set it up or restart system
				Report.addError("Do not have active channel for this org. Please check asterisk server.", "getAllRecordingDataForEmployee","EmployeeController", "Issue for organization ",request.getOrganization(),errorRepository);			
				 //Initialize all SSH Connections if they can be	
		    	try
		    	{
		    		System.out.println("Starting to get SSH connection again");
		    		
		    		List<SshConnectionDTO> sshConnections = sshConnectionService.getAllsshConnectionsOnIsEnabledAndOrganization(true,request.getOrganization());

		        	sshConnections.forEach(
		                    (sshConnection) -> { 
		                    	System.out.println("Creating SSH Connection and adding listner");
		                 		SshConnectionDTO current = sshConnection;
		                 		
		                 		if(current.getPassword() != null)
		                 		{
		                 			try {
										sshWrapper.configureOrGetChannelUsingPassword(current.getOrganization(), current.getPassword(), current.getSshUser(), current.getDomain(),applicationContext);
									} catch (IllegalArgumentException | IllegalStateException | IOException
											| TimeoutException e) {
										// TODO Auto-generated catch block
										Report.addError(e.getMessage(), "Ssh Connection","Startup System", "cannot connect to ssh",current.getOrganization(),errorRepository);		
									}	
		                 		}
		                 		else
		                 		{
		                 			try {
		                 				System.out.println("creating system using pem file");
										sshWrapper.configureOrGetChannelUsingPem(current.getOrganization(),current.getPemFileName(), current.getSshUser(), current.getDomain(),applicationContext);
		                 			} catch (IllegalArgumentException | IllegalStateException | IOException
											| TimeoutException e) {
										// TODO Auto-generated catch block
		                 				e.printStackTrace();
										Report.addError(e.getMessage(), "Ssh Connection","Startup System", "cannot connect to ssh",current.getOrganization(),errorRepository);		
									}	
		                     		
		                 		}
		                    });      
		    	}
		    	catch(Exception e)
		    	{
		    		System.out.println("Inside Exception when channel is null");
		    		System.out.println(e.getMessage());
		    		Report.addError(e.getMessage(), "Ssh Connection","Startup System", "cannot connect to SSH","N/A",errorRepository);

		    	}
			}
			
			try
			{
				channel = SshWrapper.allChannel.get(request.getOrganization());
			}
			catch(Exception e)
			{
				throw new Exception ("SSH connection broke.");
			}

			if (!(channel == null || channel.isClosed()))
			{
				System.out.println("Channel is not null");
				 token = token.replace(jwtConfiguration.getTokenPrefix(), "");
			    	
			    	System.out.println(token);
			    	
			    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
			    	
			    	if(employee.getOrganization().trim().equals(request.getOrganization()))
			    	{
			    		System.out.println("Given organization is correct");
			    		
			    		try {
			    			System.out.println("Getting all files list");
			    			System.out.println("Getting folder list");
			    			toReturn= sshWrapper.getFolderList((ChannelSftp) channel, path);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Report.addError(e.getMessage(), "getAllRecordingDataForEmployee","EmployeeController", "Issue for organization ",request.getOrganization(),errorRepository);			
							e.printStackTrace();
						}

			    		System.out.print("Getting into for loop");
			    		 
			    		// Print the vector
			            for (Integer i = 0; i < toReturn.size(); i++)
			            {
			            	System.out.print("Before going in if - else");
			            	System.out.print(toReturn.get(i).toString());
			            	
			                String extension = searchEmployeeDTO.getExtension();
			                
			                String phoneNumber = searchEmployeeDTO.getPhonenumber();
			                if(phoneNumber!= null)
			                phoneNumber = phoneNumber.substring(3);
			                else
			                phoneNumber = "This is not a valid case.";
			                
			                String allotedNumber1 = searchEmployeeDTO.getAllotednumber1();
			                if(allotedNumber1!= null)
			                allotedNumber1 = allotedNumber1.substring(3);
			                else
			                allotedNumber1 = "This is not a valid case.";
			                
			                String allotedNumber2 = searchEmployeeDTO.getAllotednumber2();
			                if(allotedNumber2!= null)
			                allotedNumber2 = allotedNumber2.substring(3);
			                else
			                allotedNumber2 = "This is not a valid case.";
			                
			                if(toReturn.get(i).toString().contains(extension.trim())||toReturn.get(i).toString().contains(phoneNumber.trim())||toReturn.get(i).toString().contains(allotedNumber1.trim())||toReturn.get(i).toString().contains(allotedNumber2.trim())) {
			                	//Do nothing
			                	System.out.print("Adding value" );
			                	System.out.print("Loop " + i );
			                	System.out.print(toReturn.get(i).toString());
			                	//add this value
			                	lastReturn.add(toReturn.get(i));
			                }
			                else
			                {
			                	System.out.print("Doing Nothing" );
			                	System.out.print("Loop " + i );
			                	System.out.print(toReturn.get(i).toString());
			                	
			                }
			                
			            }
			    		
			    		return status(HttpStatus.OK).body(lastReturn);
			    	}
			    	else
			    	{
			    		//System.out.println("I am in else controller");
			    		
			    		return status(HttpStatus.UNAUTHORIZED).body(lastReturn);
			    	} 		
			}
		}
		
		return status(HttpStatus.UNAUTHORIZED).body(lastReturn);

	} 

	@PostMapping("/downloadRecordingForEmployee")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<InputStreamResource> downloadRecordingForEmployee(@RequestBody CallRecordingListRequest request,@RequestHeader (name="Authorization") String token){
	    
		SshWrapper sshWrapper = new SshWrapper();
		Channel channel = SshWrapper.allChannel.get(request.getOrganization());
		InputStreamResource toReturn = null;
		String path =  null;
		if(request.getMonth()< 10)
		{
			if(request.getDay() < 10)
			{
				path = "/var/spool/asterisk/monitor/"+request.getYear()+"/0"+request.getMonth()+"/0"+request.getDay()+"/";
				
			}
			else
			{
				path = "/var/spool/asterisk/monitor/"+request.getYear()+"/0"+request.getMonth()+"/"+request.getDay()+"/";
			}
			
			
		}
		else
		{
			if(request.getDay() < 10)
			{
				path = "/var/spool/asterisk/monitor/"+request.getYear()+"/"+request.getMonth()+"/0"+request.getDay()+"/";
				
			}
			else
			{
				path = "/var/spool/asterisk/monitor/"+request.getYear()+"/"+request.getMonth()+"/"+request.getDay()+"/";
			}
			
		}
		
		EmployeeDTO searchEmployeeDTO= employeeService.getEmployeeByExtensionAndOrganization(request.getExtension(),request.getOrganization());
		
		if(searchEmployeeDTO != null)
		{
			if(channel == null)
			{
				//Channel Not set up. Let us set it up or restart system
				Report.addError("Do not have active channel for this org. Please check asterisk server.", "getAllRecordingDataForEmployee","EmployeeController", "Issue for organization ",request.getOrganization(),errorRepository);			
				 //Initialize all SSH Connections if they can be	
		    	try
		    	{
		    		List<SshConnectionDTO> sshConnections = sshConnectionService.getAllsshConnectionsOnIsEnabledAndOrganization(true,request.getOrganization());

		        	sshConnections.forEach(
		                    (sshConnection) -> { 
		                    	//System.out.println("Creating AMI Connection and adding listner");
		                 		SshConnectionDTO current = sshConnection;
		                 		
		                 		if(current.getPassword() != null)
		                 		{
		                 			try {
										sshWrapper.configureOrGetChannelUsingPassword(current.getOrganization(), current.getPassword(), current.getSshUser(), current.getDomain(),applicationContext);
									} catch (IllegalArgumentException | IllegalStateException | IOException
											| TimeoutException e) {
										// TODO Auto-generated catch block
										Report.addError(e.getMessage(), "Ssh Connection","Startup System", "cannot connect to ssh",current.getOrganization(),errorRepository);		
									}	
		                 		}
		                 		else
		                 		{
		                 			try {
		                 				//System.out.println("creating system using pem file");
										sshWrapper.configureOrGetChannelUsingPem(current.getOrganization(),current.getPemFileName(), current.getSshUser(), current.getDomain(),applicationContext);
										//System.out.println("received channel");
										//System.out.println(channel.getId());
										//System.out.println(channel.getExitStatus());
										//System.out.println("Is SSH Connection Connected : " + channel.isConnected());
										//System.out.println(channel.toString());
		                 			} catch (IllegalArgumentException | IllegalStateException | IOException
											| TimeoutException e) {
										// TODO Auto-generated catch block
		                 				e.printStackTrace();
										Report.addError(e.getMessage(), "Ssh Connection","Startup System", "cannot connect to ssh",current.getOrganization(),errorRepository);		
									}	
		                     		
		                 		}
		                    });      
		    	}
		    	catch(Exception e)
		    	{
		    		//System.out.println(e.getMessage());
		    		Report.addError(e.getMessage(), "Ssh Connection","Startup System", "cannot connect to SSH","N/A",errorRepository);

		    	}
			}
			else
			{
				 token = token.replace(jwtConfiguration.getTokenPrefix(), "");
			    	
			    	//System.out.println(token);
			    	
			    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
			    	
			    	if(employee.getOrganization().trim().equals(request.getOrganization()))
			    	{
			    		try {
			    			toReturn= new InputStreamResource(sshWrapper.downloadFile((ChannelSftp) channel, path, request.getFileName()));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Report.addError(e.getMessage(), "getAllRecordingDataForEmployee","EmployeeController", "Issue for organization ",request.getOrganization(),errorRepository);			
							e.printStackTrace();
						}

			    		HttpHeaders headers = new HttpHeaders();
			    		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + request.getFileName());
			    		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			    		headers.add("Pragma", "no-cache");
			    		headers.add("Expires", "0");
			    	    
			    		return status(HttpStatus.OK)
			    				.headers(headers)
			    				.contentType(MediaType.parseMediaType("audio/wav"))
			    				.body(toReturn);
			    	}
			    	else
			    	{
			    		//System.out.println("I am in else controller");
			    		
			    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
			    	} 		
			}
		}	
		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	} 
	
	
	@GetMapping("/refreshEmployeeMemoryData")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public void refreshEmployeeMemoryData (HttpServletResponse response,@RequestHeader (name="Authorization") String token) throws IOException {
    	
    	String parentorganization = env.getProperty("spring.parentorginization");
    	
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	 
    	if(employee.getOrganization().trim().equals(parentorganization.trim()))
    	{
    		EmployeeDataAndState.workOnAllEmployeeDataAndState(null, null, "reset");
    		EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(null, null, "reset");
    		EmployeeRepository employeeRepository = applicationContext.getBean(EmployeeRepository.class);
	    	List<Employee> allEmployees= employeeRepository.findAll();
	    	RefreshBackEndConnectionRunnable.setInitialExtensionStates(errorRepository,applicationContext,allEmployees);	
    	}
    	else
    	{
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    	}
    }
	
	
	@PostMapping("/refreshSpecificEmployeeMemoryData")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> refreshSpecificEmployeeMemoryData (@RequestBody List<String> extensionList,@RequestHeader (name="Authorization") String token) throws IOException {
    	
    	String parentorganization = env.getProperty("spring.parentorginization");
    	
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	 
    	if(employee.getOrganization().trim().equals(parentorganization.trim()))
    	{
    		for(int i = 0 ; i < extensionList.size(); i++) {
    			
    			Employee currentEmployee = employeeRepository.findByExtension(extensionList.get(i));
    			
    			if(currentEmployee != null) {
    				
    				EmployeeDataAndState.workOnAllEmployeeDataAndState(extensionList.get(i), null, "delete");
    	    		EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(currentEmployee.getPhonenumber(), extensionList.get(i), "delete");
    	    		
    	    		String state = "terminated";
    				String presence = "success";
    				String dotClass = "dotOnline";
    				List<String> combinedValue = new ArrayList<String>();
    				combinedValue.add(state);
    				combinedValue.add(presence);
    				combinedValue.add(dotClass);
    				EmployeeDataAndStateDTO current = new EmployeeDataAndStateDTO();
    				current.setEmployee(currentEmployee);
    				current.setMemberState(combinedValue);
    				current.setExtensionState(combinedValue);
    				current.setRunningCamapignId(-1L);
    				
    	    		EmployeeDataAndState.workOnAllEmployeeDataAndState(extensionList.get(i), current, "update");
    	    		EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(currentEmployee.getPhonenumber(),extensionList.get(i), "update");
    			}
    			else {
    				//Do nothing extension was not found
    			}
    			
    		}
    		return status(HttpStatus.OK).body(true);
    	}
    	else
    	{
    		return status(HttpStatus.UNAUTHORIZED).body(false);
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
    		employeeService.exportToExcel(response);
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
    		employeeService.exportToExcelOnOrganization(organization,response);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    		Report.addLog("Unauthorized", "Employee needs manager access","Employee", "Cannot Download Excel",organization,logRepository);	
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
    		employeeService.exportToPDF(response);
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
    	
    	System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	System.out.println(employee.getOrganization());
    	System.out.println(organization.trim());
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		System.out.println("starting");
    		employeeService.exportToPDFOnOrganization(organization,response);
    	}
    	else
    	{
    		System.out.println("I am in else controller");
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    		Report.addLog("Unauthorized", "Employee needs manager access","Employee", "Cannot Download PDF",organization,logRepository);	
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
            	
            	employeeService.uploadEmployeeUsingExcel(file,employee.getEmail(),organization);

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
    
    
    @PostMapping("/uploadProfilePicByEmailAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> uploadProfilePicByEmailAndOrganization(@RequestParam String email,@RequestParam String organization,@RequestParam("image") MultipartFile image,@RequestHeader (name="Authorization") String token) throws Exception{    
        Boolean toReturn = false;
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	//System.out.println(token);
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		employeeService.uploadProfilePicByEmailAndOrganization(employee,image,email,organization); 
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	    
    }
    
    
    @PostMapping("/uploadDocOneByEmailAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> uploadDocOneByEmailAndOrganization(@RequestParam String email,@RequestParam String organization,@RequestParam("image") MultipartFile image,@RequestHeader (name="Authorization") String token) throws Exception{    
        Boolean toReturn = false;
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	//System.out.println(token);
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		employeeService.uploadDocOneByEmailAndOrganization(employee,image,email,organization); 
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	    
    }
    
    @PostMapping("/uploadDocTwoByEmailAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> uploadDocTwoByEmailAndOrganization(@RequestParam String email,@RequestParam String organization,@RequestParam("image") MultipartFile image,@RequestHeader (name="Authorization") String token) throws Exception{    
        Boolean toReturn = false;
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	//System.out.println(token);
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		employeeService.uploadDocTwoByEmailAndOrganization(employee,image,email,organization); 
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	    
    }
    
    
    @GetMapping("/getEmployeeImages")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<List<byte[]>> getEmployeeImages(@RequestParam String email,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{    
       
//    	System.out.println("getEmployeeImages");
    	List<byte[]> imageBytesList = new ArrayList<>();
           token = token.replace(jwtConfiguration.getTokenPrefix(), "");

       	Employee employeeSelf= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	if(employeeSelf.getOrganization().trim().equals(organization.trim()))
    	{
       		
       		try {
       		 
       		 Employee employee = employeeRepository.findByEmailAndOrganization(email, organization);
       		 String picOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadEmployeeOriginalDirectory");
             String doc1OriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadDoc1OriginalDirectory");
       	     String doc2OriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadDoc2OriginalDirectory");
       	    
       	     String picOriginal = employee.getImageData();
       	     if(picOriginal!=null)
       	    	picOriginal = picOriginal.replace(picOriginalDirectory+"/", "");

       	     
        	 String doc1Original = employee.getGovernmentDocument1Data();
        	 if(doc1Original!=null)
        	 doc1Original = doc1Original.replace(doc1OriginalDirectory+"/", "");
       		
       		 String doc2Original = employee.getGovernmentDocument2Data();
       		 if(doc2Original!=null)
       		 doc2Original = doc2Original.replace(doc2OriginalDirectory+"/", "");

                // Retrieve image filenames associated with the specified entity (adsId) from the database
                String[] imageNames = new String[]{picOriginal,doc1Original,doc2Original};
                String[] imageDirectory = new String[]{picOriginalDirectory,doc1OriginalDirectory,doc2OriginalDirectory};
                
                // Fetch image data as byte arrays
                for (int i=0 ; i<imageNames.length; i++) {
   	            	 try {
   	                     byte[] imageBytes = fileService.getFile(imageDirectory[i], imageNames[i]);
   	                     imageBytesList.add(imageBytes);                 
//   	                     Blob current;
//   	                     try
//   	                     {
//   	                    	 if(imageBytes != null)
//   	                    	 {
//   	                    		System.out.println("Image byte is not null");
//   	                    		
//   	                    		current = new SerialBlob(imageBytes);
//   	                    		
//   	                    		System.out.println("Created Serial Blob");
//   	                    		System.out.println("currentBlob : "+current);
//   	                    		System.out.println("currentBlob length : "+current.length());
//   	                    		
//   	                    		imageBytesList.add(current);
//   	                    	 }
//   	                    	 else
//   	                    	 {
//   	                    		System.out.println("Image byte is null");
//   	                    		imageBytesList.add(null);
//   	                    	 }
//   	                    	
//   	                     }
//   	                     catch(Exception e)
//   	                     {
//   	                    	System.out.println("Exception found while creating blob");
//   	                    	e.printStackTrace();
//   	                    	imageBytesList.add(null);
//   	                     }                   
   	            	 }
   	            	 catch(Exception e)
   	            	 {
   	            		 e.printStackTrace();
   	            		 imageBytesList.add(null);
   	            	 } 
                }

                // Respond with the image data and an OK status code
                return status(HttpStatus.OK).body(imageBytesList);
            } catch (Exception e) {
                // Handle exceptions and provide appropriate error responses
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }	
       	}
       	else
       	{
       		//System.out.println("I am in else controller");
       		
       		return status(HttpStatus.UNAUTHORIZED).body(imageBytesList);
       	} 	    
       }
    
}
