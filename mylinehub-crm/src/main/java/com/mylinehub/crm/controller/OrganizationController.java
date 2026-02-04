package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.Organization_REST_URL;

import java.util.Map;

import javax.crypto.SecretKey;

import static org.springframework.http.ResponseEntity.status;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
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

import com.mylinehub.crm.TaskScheduler.RecalculateOrganizationStorageRunnable;
import com.mylinehub.crm.TaskScheduler.SaveOrganizationDataRunnable;
import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.data.TrackedSchduledJobs;
import com.mylinehub.crm.data.dto.OrganizationWorkingDTO;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.entity.dto.UpdateOrgDTO;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.OrganizationRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.OrganizationService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(produces="application/json", path = Organization_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class OrganizationController {

	private final EmployeeRepository employeeRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
	private final Environment env;
	private final OrganizationService organizationService;
	private final OrganizationRepository organizationRepository;
    private final ApplicationContext applicationContext;
    
    
	@GetMapping("/getOrganizationalData")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Organization> getOrganizationalData(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		Organization toReturn = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		Map<String,Organization> map= 	OrganizationData.workWithAllOrganizationData(organization,null,"get-one",null);
    		
    		if(map.containsKey(organization))
    		return status(HttpStatus.OK).body(map.get(organization));
    		else
    		return status(HttpStatus.NO_CONTENT).body(toReturn);	
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	
	@GetMapping("/getOrganizationalDataAsPerARI")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Organization> getOrganizationalDataAsPerARI(@RequestParam String ariApplication,@RequestHeader (name="Authorization") String token){
	        
		Organization toReturn = null;
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	//System.out.println(token);	

    	try
    	{
        	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
        	
    		OrganizationWorkingDTO organizationWorkingDTO = new OrganizationWorkingDTO();
    		organizationWorkingDTO.setAriApplication(ariApplication);
    		Map<String,Organization> map= 	OrganizationData.workWithAllOrganizationData(null,null,"get-by-ariApplication",organizationWorkingDTO);
    		
    		if (map != null && map.size() == 1) {
    		    // Get the first (and only) Organization
    			toReturn = map.values().iterator().next();
    		    
    		}
    		
    		if(toReturn!=null)
    		return status(HttpStatus.OK).body(toReturn);
    		else
    		return status(HttpStatus.NO_CONTENT).body(toReturn);	
    	}
    	catch(Exception e)
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	@GetMapping("/updateOrganizationMemoryDataFromDatabase")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Organization> updateOrganizationMemoryDataFromDatabase(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		Organization toReturn = null;
		String parentorganization = env.getProperty("spring.parentorginization");
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(parentorganization))
    	{
    		
    		toReturn = organizationService.findByName(organization);
    		
    		if(toReturn!=null) {
    			OrganizationData.workWithAllOrganizationData(organization,null,"delete",null);
    			OrganizationData.workWithAllOrganizationData(organization,toReturn,"update",null);
    		}
    		else {
        		
        		return status(HttpStatus.NO_CONTENT).body(toReturn);
    		}	
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
    	return status(HttpStatus.OK).body(toReturn);
	}
	
	
	@GetMapping("/saveMemoryOrgDataToDatabaseNow")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> saveMemoryOrgDataToDatabaseNow(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		Boolean toReturn = false;
		String parentorganization = env.getProperty("spring.parentorginization");
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(parentorganization))
    	{
    		SaveOrganizationDataRunnable saveOrganizationDataRunnable = new SaveOrganizationDataRunnable();
    		saveOrganizationDataRunnable.setOrganizationRepository(organizationRepository);
    		saveOrganizationDataRunnable.run();
    		toReturn = true;
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
    	return status(HttpStatus.OK).body(toReturn);
	}
	
	 @PostMapping("/update")
	 public ResponseEntity<Boolean> update(@RequestBody UpdateOrgDTO updateOrgDTO,@RequestHeader (name="Authorization") String token) throws Exception {
	        
	        try {
	        	
	        	Boolean toReturn = false;
	    		
	            token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	        	
	        	System.out.println(token);
	        	
	        	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	        	
	        	if(employee.getOrganization().trim().equals(applicationContext.getEnvironment().getProperty("spring.parentorginization")))
	        	{
	        		organizationService.update(updateOrgDTO.getUpdates());
	        		return status(HttpStatus.OK).body(toReturn);
	        	}
	        	else
	        	{
	        		//System.out.println("I am in else controller");
	        		
	        		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	        	}
	        	
	         }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        	return status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
	        	
	        }
	        
	    }
	    
	    
	    @PostMapping("/recalculateOrganizationStorage")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<Boolean> recalculateOrganizationStorage(@RequestBody String organization,@RequestHeader (name="Authorization") String token) throws Exception{
		    
			Boolean toReturn = false;
			
	        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(applicationContext.getEnvironment().getProperty("spring.parentorginization")))
	    	{
	    		RecalculateOrganizationStorageRunnable recalculateOrganizationStorageRunnable = new RecalculateOrganizationStorageRunnable();

	    		recalculateOrganizationStorageRunnable.setJobId(TrackedSchduledJobs.recalculateFileStorage+"-"+organization);
	    		recalculateOrganizationStorageRunnable.setApplicationContext(applicationContext);
	    		recalculateOrganizationStorageRunnable.run();
	    		
	    		toReturn = true;
	    		return status(HttpStatus.OK).body(toReturn);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	} 	
		}

	

}
