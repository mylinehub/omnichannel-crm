package com.mylinehub.crm.controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.CustomerDTO;
import com.mylinehub.crm.entity.dto.CustomerPageDTO;
import com.mylinehub.crm.entity.dto.MediaDto;
import com.mylinehub.crm.exports.ExcelHelper;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.LogRepository;
import com.mylinehub.crm.requests.BooleanValueRequest;
import com.mylinehub.crm.requests.CustomerDescriptionRequest;
import com.mylinehub.crm.requests.IdRequest;
import com.mylinehub.crm.requests.StringValueRequest;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.CustomerService;
import com.mylinehub.crm.service.FileService;
import com.mylinehub.crm.utils.ResponseMessage;

import lombok.AllArgsConstructor;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.mylinehub.crm.controller.ApiMapping.CUSTOMERS_REST_URL;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping(produces="application/json", path = CUSTOMERS_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class CustomerController {



    private final CustomerService customerService;
    private final EmployeeRepository employeeRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
    private final LogRepository logRepository;
	private Environment env;
	private final FileService fileService;
	private final ApplicationContext applicationContext;
	
	@PostMapping("/createCustomerByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> createCustomerByOrganization(@RequestBody CustomerDTO customerDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim())   && (employee.getOrganization().trim().equals(customerDTO.getOrganization().trim())) )
    	{
    		toReturn = customerService.createCustomerByOrganization(customerDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@PostMapping("/updateCustomerByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateCustomerByOrganization(@RequestBody CustomerDTO customerDTO,@RequestParam String oldPhone , @RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		System.out.println("Let us update an customer");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = customerService.updateCustomerByOrganization(customerDTO,oldPhone,false);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	
	@PostMapping("/updateCustomerByOrganizationByParent")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateCustomerByOrganizationByParent(@RequestBody CustomerDTO customerDTO,@RequestParam String oldPhone , @RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		System.out.println("Let us update an customer via parent");
		Boolean toReturn = false;
		
		String parentorganization = env.getProperty("spring.parentorginization");
    	
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	 
    	if(employee.getOrganization().trim().equals(parentorganization.trim()))
    	{
    		toReturn = customerService.updateCustomerByOrganization(customerDTO,oldPhone,true);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@DeleteMapping("/deleteCustomerByIdAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> deleteCustomerByIdAndOrganization(@RequestParam Long id,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn  = customerService.deleteCustomerByIdAndOrganization(employee,id, organization);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	} 
	
	@PostMapping("/updateCustomerProductInterests")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateCustomerProductInterests(@RequestBody StringValueRequest request,@RequestHeader (name="Authorization") String token) throws JsonProcessingException{
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		customerService.updateCustomerProductInterests(request.getId(),request.getValue());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	
	@PostMapping("/updateCustomerDescription")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateCustomerDescription(@RequestBody CustomerDescriptionRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		customerService.updateCustomerDescription(request.getDescription(),request.getId());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	} 
	
	
	@PostMapping("/customerGotConverted")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> customerGotConverted(@RequestBody IdRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		customerService.customerGotConverted(request.getId());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	} 
	
	
	@PostMapping("/customerGotDiverted")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> customerGotDiverted(@RequestBody IdRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		customerService.customerGotDiverted(request.getId());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	} 
	
	
	@PostMapping("/updateWhatsAppAIAutoMessage")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateWhatsAppAIAutoMessage(@RequestBody BooleanValueRequest request,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
		if(request.getValue() == null) {
			request.setValue(false);
		}
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		if(employee.isAllowedToSwitchOffWhatsAppAI()) {
    		customerService.updateWhatsAppAIAutoMessage(request.getValue(),request.getId());
    			toReturn = true;
    		}
    		else {
    			toReturn = false;
    		}
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	} 
	
	
	@GetMapping("/getCustomerByIdAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerDTO> getCustomerByIdAndOrganization(@RequestParam Long customerId,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		CustomerDTO customers= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customers= customerService.getCustomerByIdAndOrganization(customerId,organization);
    		return status(HttpStatus.OK).body(customers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
    	} 	
	}
	
	
	@GetMapping("/getCustomerByWhatsAppPhoneNumberId")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerDTO> getCustomerByWhatsAppPhoneNumberId(@RequestParam String whatsAppPhoneNumberId,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
	        
		CustomerDTO customers= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customers= customerService.getCustomerByWhatsAppPhoneNumberId(whatsAppPhoneNumberId);
    		return status(HttpStatus.OK).body(customers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
    	} 	
	}
	
	
	
	@GetMapping("/getByPhoneNumberAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerDTO> getByPhoneNumberAndOrganization(@RequestParam String phoneNumber,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		CustomerDTO customers= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	phoneNumber = phoneNumber.trim();
//    	System.out.println(token);
//    	System.out.println(phoneNumber.trim());
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
//    		System.out.println("Inside search");
    		customers= customerService.getByPhoneNumberAndOrganization(phoneNumber.trim(),organization);
    		if(customers == null)
    		{
    			if(phoneNumber.contains("+"))
    	    	{
    				phoneNumber = phoneNumber.replace("+", "");
    	    	}
    	    	else
    	    	{
    	    		phoneNumber = "+"+phoneNumber;
    	    	}
    			customers= customerService.getByPhoneNumberAndOrganization(phoneNumber.trim(),organization);
    		}
    		return status(HttpStatus.OK).body(customers);
    		
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
    	} 	
	}
	
	
	@GetMapping("/getByPhoneNumberAndOrganizationByParent")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerDTO> getByPhoneNumberAndOrganizationByParent(@RequestParam String phoneNumber,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		System.out.println("getByPhoneNumberAndOrganizationByParent");
		
		CustomerDTO customers= null;
    	phoneNumber = phoneNumber.trim();
    	String parentorganization = env.getProperty("spring.parentorginization");
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	 
    	if(employee.getOrganization().trim().equals(parentorganization.trim()))
    	{	
    		customers= customerService.getByPhoneNumberAndOrganizationPreferCache(phoneNumber.trim(),organization);
    		if(customers == null)
    		{
    			if(phoneNumber.contains("+"))
    	    	{
    				phoneNumber = phoneNumber.replace("+", "");
    	    	}
    	    	else
    	    	{
    	    		phoneNumber = "+"+phoneNumber;
    	    	}
    			customers= customerService.getByPhoneNumberAndOrganizationPreferCache(phoneNumber.trim(),organization);
    		}
    		return status(HttpStatus.OK).body(customers);
    		
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
    	} 	
	}
	
	@GetMapping("/findAllBywhatsAppProjectId")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerPageDTO> findAllBywhatsAppProjectId(@RequestParam String organization,@RequestParam String whatsAppProjectId,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
	        
		Pageable pageable;
		if (pageNumber < 0) {
		    pageable = PageRequest.of(0, 1_000_000_000, Sort.by(Sort.Direction.DESC, "id"));
		} else {
		    pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "id"));
		}
		
		CustomerPageDTO customers= null;

		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customers= customerService.findAllBywhatsAppProjectId(whatsAppProjectId,searchText,pageable);
    		return status(HttpStatus.OK).body(customers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
    	} 	
	}
	
	
	@GetMapping("/findAllByWhatsAppRegisteredByPhoneNumber")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerPageDTO> findAllByWhatsAppRegisteredByPhoneNumber(@RequestParam String whatsAppRegisteredByPhoneNumber,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
	        
		Pageable pageable;
		if (pageNumber < 0) {
		    pageable = PageRequest.of(0, 1_000_000_000, Sort.by(Sort.Direction.DESC, "id"));
		} else {
		    pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "id"));
		}
		
		CustomerPageDTO customers= null;

		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customers= customerService.findAllByWhatsAppRegisteredByPhoneNumber(whatsAppRegisteredByPhoneNumber,searchText,pageable);
    		return status(HttpStatus.OK).body(customers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
    	} 	
	}
	
	@GetMapping("/getAllCustomersOnOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerPageDTO> getAllCustomersOnOrganization(@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
	        
		Pageable pageable;
		if (pageNumber < 0) {
		    pageable = PageRequest.of(0, 1_000_000_000, Sort.by(Sort.Direction.DESC, "id"));
		} else {
		    pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "id"));
		}
		
		CustomerPageDTO customers= null;

		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customers= customerService.findAllByOrganization(organization,searchText,pageable);
    		return status(HttpStatus.OK).body(customers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
    	} 	
	}
	
	@GetMapping("/getCustomerByEmailAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerPageDTO> getCustomerByEmailAndOrganization(@RequestParam String email,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
        
		Pageable pageable;
		if (pageNumber < 0) {
		    pageable = PageRequest.of(0, 1_000_000_000, Sort.by(Sort.Direction.DESC, "id"));
		} else {
		    pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "id"));
		}
		
		CustomerPageDTO customers= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customers= customerService.findAllByEmailAndOrganization(email,organization,searchText,pageable);
    		return status(HttpStatus.OK).body(customers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
    	} 	
	}
	
	
	
	@GetMapping("/getCustomerByPeselAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerPageDTO> getCustomerByPeselAndOrganization(@RequestParam String pesel,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
        
		Pageable pageable;
		if (pageNumber < 0) {
		    pageable = PageRequest.of(0, 1_000_000_000, Sort.by(Sort.Direction.DESC, "id"));
		} else {
		    pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "id"));
		}
		
		CustomerPageDTO customers= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customers= customerService.findAllByPeselAndOrganization(pesel,organization,searchText,pageable);
    		return status(HttpStatus.OK).body(customers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
    	} 	
	}

	
	
	@GetMapping("/findAllByCountryAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerPageDTO> findAllByCountryAndOrganization(@RequestParam String country,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
        
		Pageable pageable;
		if (pageNumber < 0) {
		    pageable = PageRequest.of(0, 1_000_000_000, Sort.by(Sort.Direction.DESC, "id"));
		} else {
		    pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "id"));
		}
	
	    CustomerPageDTO customers= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customers= customerService.findAllByCountryAndOrganization(country,organization,searchText,pageable);
    		return status(HttpStatus.OK).body(customers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
    	} 	
	}
	
	
	@GetMapping("/findAllByBusinessAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerPageDTO> findAllByBusinessAndOrganization(@RequestParam String business,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
        
		Pageable pageable;
		if (pageNumber < 0) {
		    pageable = PageRequest.of(0, 1_000_000_000, Sort.by(Sort.Direction.DESC, "id"));
		} else {
		    pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "id"));
		}
	
		CustomerPageDTO customers= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customers= customerService.findAllByBusinessAndOrganization(business,organization,searchText,pageable);
    		return status(HttpStatus.OK).body(customers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
    	} 	
	}
	
	
	
	@GetMapping("/findAllByPhoneContextAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerPageDTO> findAllByPhoneContextAndOrganization(@RequestParam String phoneContext,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
        
		Pageable pageable;
		if (pageNumber < 0) {
		    pageable = PageRequest.of(0, 1_000_000_000, Sort.by(Sort.Direction.DESC, "id"));
		} else {
		    pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "id"));
		}
	
		CustomerPageDTO customers= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customers= customerService.findAllByPhoneContextAndOrganization(phoneContext,organization,searchText,pageable);
    		return status(HttpStatus.OK).body(customers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
    	} 	
	}
	
	
	
	@GetMapping("/findAllByCityAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerPageDTO> findAllByCityAndOrganization(@RequestParam String city,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
        
		Pageable pageable;
		if (pageNumber < 0) {
		    pageable = PageRequest.of(0, 1_000_000_000, Sort.by(Sort.Direction.DESC, "id"));
		} else {
		    pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "id"));
		}
	
		CustomerPageDTO customers= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customers= customerService.findAllByCityAndOrganization(city,organization,searchText,pageable);
    		return status(HttpStatus.OK).body(customers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
    	} 	
	}
	
	
	@GetMapping("/findAllByZipCodeAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerPageDTO> findAllByZipCodeAndOrganization(@RequestParam String zipCode,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
        
		Pageable pageable;
		if (pageNumber < 0) {
		    pageable = PageRequest.of(0, 1_000_000_000, Sort.by(Sort.Direction.DESC, "id"));
		} else {
		    pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "id"));
		}
	
		CustomerPageDTO customers= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customers= customerService.findAllByZipCodeAndOrganization(zipCode,organization,searchText,pageable);
    		return status(HttpStatus.OK).body(customers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
    	} 	
	}
	
	
	@GetMapping("/findAllByCovertedAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerPageDTO> findAllByCovertedAndOrganization(@RequestParam boolean coverted,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
        
		Pageable pageable;
		if (pageNumber < 0) {
		    pageable = PageRequest.of(0, 1_000_000_000, Sort.by(Sort.Direction.DESC, "id"));
		} else {
		    pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "id"));
		}
	
		CustomerPageDTO customers= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customers= customerService.findAllByCovertedAndOrganization(coverted,organization,searchText,pageable);
    		return status(HttpStatus.OK).body(customers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customers);
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
    		customerService.exportToExcel(response);
    	}
    	else
    	{
    		
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    	}
        
    }

    @GetMapping("/export/organization/excel")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public void exportToExcelOnOrganization(@RequestParam String organization,@RequestParam String searchText,HttpServletResponse response, @RequestHeader (name="Authorization") String token) throws IOException {

    	
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customerService.exportToExcelOnOrganization(organization,searchText,response);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    		Report.addLog("Unauthorized", "Employee needs manager access","Customer", "Cannot Download Excel",organization,logRepository);
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
    		customerService.exportToPDF(response);
    	}
    	else
    	{
    		
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    	}
        
    }

    @GetMapping("/export/organization/pdf")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public void exportToPDFOnOrganization(@RequestParam String organization,@RequestParam String searchText,HttpServletResponse response, @RequestHeader (name="Authorization") String token) throws IOException {

    	
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customerService.exportToPDFOnOrganization(organization,searchText,response);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    		Report.addLog("Unauthorized", "Employee needs manager access","Customer", "Cannot Download PDF",organization,logRepository);
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

        	if (ExcelHelper.hasExcelFormat(file)) {
            try {
            	
            	//System.out.println("I am inside try");
            	
            	customerService.uploadCustomersUsingExcel(file,organization);

            	//System.out.println("I am after employee");
              message = "Uploaded the file successfully: " + file.getOriginalFilename();
              return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
            } catch (Exception e) {
        	  System.out.println("[UPLOAD][CONTROLLER][EXCEPTION] " + e.getMessage());
        	  e.printStackTrace();

        	  message = "Could not upload the file: " + file.getOriginalFilename() + " | error=" + e.getMessage();
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
    
    @PostMapping("/uploadCustomerPicByIdAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> uploadCustomerPicByIdAndOrganization(@RequestParam Long id,@RequestParam String organization,@RequestParam("image") MultipartFile image,@RequestHeader (name="Authorization") String token) throws Exception{
        
        Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customerService.uploadCustomerPicByEmailAndOrganization(employee,image,id,organization);  
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	    
    }
    
    
    @GetMapping("/getCustomerImage")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<MediaDto> getCustomerImage(@RequestParam String phoneNumber,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{    
    	MediaDto returnImage = new MediaDto();
           token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	//System.out.println(token);
       	Employee employeeSelf= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	if(employeeSelf.getOrganization().trim().equals(organization.trim()))
    	{
       		try {
       		 
//       		 System.out.println("Inside try");
       		 phoneNumber = phoneNumber.trim();
       		 
        	
       		 Customers current = customerService.getCustomerByPhoneNumberAndOrganization(phoneNumber, organization);
       		
       		 if(current == null)
       		 {
       			if(phoneNumber.contains("+"))
          		 {
       				phoneNumber = phoneNumber.replace("+", "");
          		 }
          		 else
          		 {
          			 phoneNumber = "+"+phoneNumber;
          		 }
       			
       			current = customerService.getCustomerByPhoneNumberAndOrganization(phoneNumber, organization);
       		 }
       		 
       		 String uploadOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadCustomerOriginalDirectory");

//       		System.out.println("After uploadOriginalDirectory");
       		
       	     String imageData = current.getImageData();
       	     
       	     if(imageData !=null)
       	     {
//       	    	System.out.println("Name not null");
       	    	imageData = imageData.replace(uploadOriginalDirectory+"/", "");

  	       	  try {
//  	       		         System.out.println("Setting image data");
  	       		         returnImage.setByteData(fileService.getFile(uploadOriginalDirectory, imageData));
	  	       		     returnImage.setName(current.getImageName());
		       		     returnImage.setType(current.getImageType());
	  	        	 }
  	        	 catch(Exception e)
  	        	 {
//  	        		System.out.println("Inner Catch");
  	        		 e.printStackTrace();
  	        		returnImage=null;
  	        	 }  
       	     }
       	     else
       	     {
//       	    	System.out.println("Image data is null");
       	    	returnImage = null;
       	     }
       	     
                // Respond with the image data and an OK status code
                return status(HttpStatus.OK).body(returnImage);
            } catch (Exception e) {
//            	System.out.println("Outer Catch");
                // Handle exceptions and provide appropriate error responses
            	e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }	
       	}
       	else
       	{
       		//System.out.println("I am in else controller");
       		
       		return status(HttpStatus.UNAUTHORIZED).body(returnImage);
       	} 	    
       }
}
