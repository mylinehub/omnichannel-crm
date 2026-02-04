package com.mylinehub.crm.controller;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.PurchasesDTO;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.PurchasesService;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import static com.mylinehub.crm.controller.ApiMapping.PURCHASES_REST_URL;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping(produces="application/json", path = PURCHASES_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class PurchasesController {

    private final PurchasesService purchasesService;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
    private final EmployeeRepository employeeRepository;
    
    @GetMapping("/getAllPurchasesOnOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<PurchasesDTO>> getAllPurchasesOnOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<PurchasesDTO> purchases= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		purchases= purchasesService.getAllPurchasesOnOrganization(organization);
    		return status(HttpStatus.OK).body(purchases);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(purchases);
    	} 	
	}
	
	@GetMapping("/findAllByCustomerAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<PurchasesDTO>> findAllByCustomerAndOrganization(@RequestParam Long customerID,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<PurchasesDTO> purchases= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		purchases= purchasesService.findAllByCustomerAndOrganization(customerID,organization);
    		return status(HttpStatus.OK).body(purchases);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(purchases);
    	} 	
	}
	
	@GetMapping("/findAllByPurchaseDateGreaterThanEqualAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<PurchasesDTO>> findAllByPurchaseDateGreaterThanEqualAndOrganization(@RequestParam Date date,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<PurchasesDTO> purchases= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		try
    		{
    			//Date dateObject=new SimpleDateFormat("yyyy-mm-dd").parse(date);  
        		
        		purchases= purchasesService.findAllByPurchaseDateGreaterThanEqualAndOrganization(date,organization);
        		return status(HttpStatus.OK).body(purchases);
    		}
    		catch(Exception e)
    		{
    			return status(HttpStatus.BAD_REQUEST).body(purchases);
    		}
    		
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(purchases);
    	} 	
	}
	
	
	@GetMapping("/findAllByPurchaseDateLessThanEqualAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<PurchasesDTO>> findAllByPurchaseDateLessThanEqualAndOrganization(@RequestParam Date date,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<PurchasesDTO> purchases= null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		
    		try
    		{
    			//Date dateObject=new SimpleDateFormat("yyyy-mm-dd").parse(date);  
        		
        		purchases= purchasesService.findAllByPurchaseDateLessThanEqualAndOrganization(date,organization);
        		return status(HttpStatus.OK).body(purchases);
    		}
    		catch(Exception e)
    		{
    			return status(HttpStatus.BAD_REQUEST).body(purchases);
    		}
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(purchases);
    	} 	
	}
	
	
	
	@GetMapping("/getPurchaseByPurchaseIDAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<PurchasesDTO> getPurchaseByPurchaseIDAndOrganization(@RequestParam Long purchaseID,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		PurchasesDTO searchPurchase = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		searchPurchase= purchasesService.getByIdAndOrganization(purchaseID,organization);
    		return status(HttpStatus.OK).body(searchPurchase);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(searchPurchase);
    	} 	
	}
	
	
	@PostMapping("/createPurchaseByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> createPurchaseByOrganization(@RequestBody PurchasesDTO purchasesDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()) && (employee.getOrganization().trim().equals(purchasesDTO.getOrganization().trim())))
    	{
    		toReturn = purchasesService.createPurchaseByOrganization(purchasesDTO,employee);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@PostMapping("/updatePurchaseByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updatePurchaseByOrganization(@RequestBody PurchasesDTO purchasesDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		//System.out.println("Let us update an employee");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = purchasesService.updatePurchaseByOrganization(purchasesDTO,employee);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@DeleteMapping("/deletePurchaseByIdAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> deletePurchaseByIdAndOrganization(@RequestParam Long purchaseID,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn  = purchasesService.deletePurchaseByOrganization(purchaseID, organization);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	} 
}
