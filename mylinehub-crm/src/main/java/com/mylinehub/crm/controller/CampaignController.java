package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.CAMPAIGN_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
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
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.AriBackToCampaignDTO;
import com.mylinehub.crm.entity.dto.CampaignDTO;
import com.mylinehub.crm.enums.AUTODIALER_TYPE;
import com.mylinehub.crm.enums.REMINDER_CALLING;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.LogRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.CampaignService;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping(produces="application/json", path = CAMPAIGN_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class CampaignController {

    private final EmployeeRepository employeeRepository;
    private final CampaignService campaignService;
    private final JwtConfiguration jwtConfiguration;
    private final LogRepository logRepository;
    private final SecretKey secretKey;
	private Environment env;
	
	
	@PostMapping("/returnARIToCampaignByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> returnARIToCampaignByOrganization(@RequestBody AriBackToCampaignDTO ariBackToCampaignDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
	    
//		System.out.println("Starting campaign");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = null;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	   	
	} 
	
	@PostMapping("/startCampaignByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> startCampaignByOrganization(@RequestBody CampaignDTO campaignDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
	    
//		System.out.println("Starting campaign");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = campaignService.startCampaignByOrganization(campaignDTO,employee.getExtension(),employee.getDomain());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	   	
	} 
	
	
	@PostMapping("/pauseCampaignByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> pauseCampaignByOrganization(@RequestBody CampaignDTO campaignDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
//		System.out.println("pauseCampaignByOrganization");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = campaignService.pauseCampaignByOrganization(campaignDTO,employee.getExtension(),employee.getDomain());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	
	@PostMapping("/unpauseCampaignByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> unpauseCampaignByOrganization(@RequestBody CampaignDTO campaignDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
//		System.out.println("unpauseCampaignByOrganization");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = campaignService.unpauseCampaignByOrganization(campaignDTO,employee.getExtension(),employee.getDomain());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	
	@PostMapping("/stopCampaignByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> stopCampaignByOrganization(@RequestBody CampaignDTO campaignDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
//		System.out.println("stopCampaignByOrganization");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = campaignService.stopCampaignByOrganization(campaignDTO,employee.getExtension(),employee.getDomain());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	

	@PostMapping("/resetCampaignByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> resetCampaignByOrganization(@RequestBody CampaignDTO campaignDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
	    
//		System.out.println("resetCampaignByOrganization");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = campaignService.resetCampaignByOrganization(campaignDTO,employee.getExtension(),employee.getDomain());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	

	
	@GetMapping("/getAllAutodialerTypes")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAutodialerTypes(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allAutodialerTypes = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(AUTODIALER_TYPE.class).forEach(value ->allAutodialerTypes.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allAutodialerTypes);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allAutodialerTypes);
    	} 	
	}
	
	
	@GetMapping("/getAllReminderCallingType")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllReminderCallingType(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allTypes = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(REMINDER_CALLING.class).forEach(value ->allTypes.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allTypes);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allTypes);
    	} 	
	}

		@PostMapping("/createCampaignByOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<Boolean> createCampaignByOrganization(@RequestBody CampaignDTO campaignDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
		    
			Boolean toReturn = false;
			
	        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println("Lets create campaign");
	    	
	    	//System.out.println(campaignDTO.getManagerId());
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()) && (employee.getOrganization().trim().equals(campaignDTO.getOrganization().trim())))
	    	{
	    		toReturn = campaignService.createCampaignByOrganization(campaignDTO);
	    		return status(HttpStatus.OK).body(toReturn);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	} 	
		} 
		
		@PostMapping("/updateCampaignByOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<Boolean> updateCampaignByOrganization(@RequestBody CampaignDTO campaignDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		    
			//System.out.println("Let us update an employee");
			Boolean toReturn = false;
			
	        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	        //System.out.println("Email : "+employeeDTO.getEmail());
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		toReturn = campaignService.updateCampaignByOrganization(campaignDTO,employee.getExtension(),employee.getDomain());
	    		return status(HttpStatus.OK).body(toReturn);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	} 	
		} 
		
		@DeleteMapping("/deleteCampaignByIdAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<Boolean> deleteCampaignByIdAndOrganization(@RequestParam Long id,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
		    
			Boolean toReturn = false;
			
	        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		toReturn  = campaignService.deleteCampaignByIdAndOrganization(id, organization,employee.getExtension(),employee.getDomain());
	    		return status(HttpStatus.OK).body(toReturn);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	} 	
	    	
			
		} 
	
		@GetMapping("/getCampaignByIdAndOrganization")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CampaignDTO> getCampaignByIdAndOrganization(@RequestParam Long campaignId,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		        
			CampaignDTO campaigns= null;
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		campaigns= campaignService.getCampaignByIdAndOrganization(campaignId,organization);
	    		return status(HttpStatus.OK).body(campaigns);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(campaigns);
	    	} 	
		}
	
	
	
		@GetMapping("/getCampaignByNameAndOrganization")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CampaignDTO> getCampaignByNameAndOrganization(@RequestParam String campaignName,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		        
			CampaignDTO campaigns= null;
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		campaigns= campaignService.getCampaignByNameAndOrganization(campaignName,organization);
	    		return status(HttpStatus.OK).body(campaigns);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(campaigns);
	    	} 	
		}
		
	
		@GetMapping("/getAllCampaignsOnOrganization")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<List<CampaignDTO>> getAllCampaignsOnOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		        
			List<CampaignDTO> campaigns= null;
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		campaigns= campaignService.findAllByOrganization(organization);
	    		return status(HttpStatus.OK).body(campaigns);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(campaigns);
	    	} 	
		}
	
	
		@GetMapping("/findAllByManagerAndOrganization")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<List<CampaignDTO>> findAllByManagerAndOrganization(@RequestParam Long managerId,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		        
			List<CampaignDTO> campaigns= null;
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		campaigns= campaignService.findAllByManagerAndOrganization(managerId,organization);
	    		return status(HttpStatus.OK).body(campaigns);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(campaigns);
	    	} 	
		}
	
		
		
		@GetMapping("/findAllByCountryAndOrganization")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<List<CampaignDTO>> findAllByCountryAndOrganization(@RequestParam String country,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		        
			List<CampaignDTO> campaigns= null;
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		campaigns= campaignService.findAllByCountryAndOrganization(country,organization);
	    		return status(HttpStatus.OK).body(campaigns);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(campaigns);
	    	} 	
		}
	
		
		
		@GetMapping("/findAllByBusinessAndOrganization")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<List<CampaignDTO>> findAllByBusinessAndOrganization(@RequestParam String business,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		        
			List<CampaignDTO> campaigns= null;
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		campaigns= campaignService.findAllByBusinessAndOrganization(business,organization);
	    		return status(HttpStatus.OK).body(campaigns);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(campaigns);
	    	} 	
		}
	
		
		
		@GetMapping("/findAllByPhonecontextAndOrganization")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<List<CampaignDTO>> findAllByPhonecontextAndOrganization(@RequestParam String phonecontext,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		        
			List<CampaignDTO> campaigns= null;
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		campaigns= campaignService.findAllByPhonecontextAndOrganization(phonecontext,organization);
	    		return status(HttpStatus.OK).body(campaigns);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(campaigns);
	    	} 	
		}
	
		
		
		@GetMapping("/findAllByIsonmobileAndOrganization")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<List<CampaignDTO>> findAllByIsonmobileAndOrganization(@RequestParam boolean isonmobile,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		        
			List<CampaignDTO> campaigns= null;
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		campaigns= campaignService.findAllByIsonmobileAndOrganization(isonmobile,organization);
	    		return status(HttpStatus.OK).body(campaigns);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(campaigns);
	    	} 	
		}
	
		
		
		@GetMapping("/findAllByAutodialertypeAndOrganization")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<List<CampaignDTO>> findAllByAutodialertypeAndOrganization(@RequestParam String autodialertype,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		        
			List<CampaignDTO> campaigns= null;
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		campaigns= campaignService.findAllByAutodialertypeAndOrganization(autodialertype,organization);
	    		return status(HttpStatus.OK).body(campaigns);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(campaigns);
	    	} 	
		}
	
		
		
		@GetMapping("/findAllByStartdateGreaterThanEqualAndOrganization")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<List<CampaignDTO>> findAllByStartdateGreaterThanEqualAndOrganization(@RequestParam Date startdate,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		        
			List<CampaignDTO> campaigns= null;
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		campaigns= campaignService.findAllByStartdateGreaterThanEqualAndOrganization(startdate,organization);
	    		return status(HttpStatus.OK).body(campaigns);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(campaigns);
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
	    		campaignService.exportToExcel(response);
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
	    		campaignService.exportToExcelOnOrganization(organization,response);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    		Report.addLog("Unauthorized", "Employee needs manager access","Campaign", "Cannot Download Excel",organization,logRepository);	
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
	    		campaignService.exportToPDF(response);
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
	    		campaignService.exportToPDFOnOrganization(organization,response);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    		Report.addLog("Unauthorized", "Employee needs manager access","Campaign", "Cannot Download PDF",organization,logRepository);	
	    	} 	
	    	
	    }
	
}