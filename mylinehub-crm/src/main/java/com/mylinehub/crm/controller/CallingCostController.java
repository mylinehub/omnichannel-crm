//package com.mylinehub.crm.controller;
//
//import static com.mylinehub.crm.controller.ApiMapping.CALLING_COST_REST_URL;
//import static org.springframework.http.ResponseEntity.status;
//
//import com.mylinehub.crm.security.email.EmailBuilder;
//import com.mylinehub.crm.security.email.EmailService;
//
//import java.io.IOException;
//import java.util.Date;
//
//import javax.crypto.SecretKey;
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.core.env.Environment;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestController;
//import com.mylinehub.crm.entity.Employee;
//import com.mylinehub.crm.entity.dto.CallingCostPageDTO;
//import com.mylinehub.crm.report.Report;
//import com.mylinehub.crm.repository.EmployeeRepository;
//import com.mylinehub.crm.repository.LogRepository;
//import com.mylinehub.crm.security.jwt.JwtConfiguration;
//import com.mylinehub.crm.security.jwt.JwtVerify;
//import com.mylinehub.crm.service.CallingCostService;
//
//import lombok.AllArgsConstructor;
//
//
//@RestController
//@RequestMapping(produces="application/json", path = CALLING_COST_REST_URL)
//@AllArgsConstructor
//@CrossOrigin(origins="*")
//public class CallingCostController {
//
//    private final EmployeeRepository employeeRepository;
//    private final CallingCostService callingCostService;
//    private final LogRepository logRepository;
//    private final JwtConfiguration jwtConfiguration;
//    private final EmailService emailService;
//    private final SecretKey secretKey;
//	private Environment env;
//	
//	
//	@GetMapping("/getAllCallingCostOnOrganizationViaEmail")
//	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//	public ResponseEntity<Boolean> getAllCallingCostOnOrganizationViaEmail(@RequestParam String organization,@RequestParam String email,@RequestHeader (name="Authorization") String token){
//	        
//		Boolean toReturn= null;
//		
//    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//    	
//    	System.out.println("Inside getAllCallingCostOnOrganizationViaEmail");
//    	
//    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//    	
//    	if(employee.getOrganization().trim().equals(organization.trim()))
//    	{
//    		System.out.println("Inside calling service");
//    		emailService.send(email, EmailBuilder.buildEmail("",""),"Test Subject","support@mylinehub.com");
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
//		@GetMapping("/getAllCallingCostOnOrganization")
//		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//		public ResponseEntity<CallingCostPageDTO> getAllCallingCostOnOrganization(@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
//		        
//			Pageable pageable;
//			if(pageNumber <0)
//			{
//				pageable= PageRequest.of(0, 1000000000);
//			}
//			else
//			{
//				pageable = PageRequest.of(pageNumber, size);
//			}
//			
//			CallingCostPageDTO callingCosts= null;
//			
//			
//	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//	    	
//	    	//System.out.println(token);
//	    	
//	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//	    	
//	    	if(employee.getOrganization().trim().equals(organization.trim()))
//	    	{
//	    		callingCosts= callingCostService.findAllByOrganization(organization,searchText,pageable);
//	    		return status(HttpStatus.OK).body(callingCosts);
//	    	}
//	    	else
//	    	{
//	    		//System.out.println("I am in else controller");
//	    		
//	    		return status(HttpStatus.UNAUTHORIZED).body(callingCosts);
//	    	} 	
//		}
//	
//		
//		@GetMapping("/getAllCallingCostByExtensionAndOrganization")
//		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//		public ResponseEntity<CallingCostPageDTO> getAllCallingCostByExtensionAndOrganization(@RequestParam String extension,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
//	        
//		Pageable pageable;
//		if(pageNumber <0)
//		{
//			pageable= PageRequest.of(0, 1000000000);
//		}
//		else
//		{
//			pageable = PageRequest.of(pageNumber, size);
//		}
//		
//		CallingCostPageDTO callingCosts= null;
//			
//	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//	    	
//	    	//System.out.println(token);
//	    	
//	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//	    	
//	    	if(employee.getOrganization().trim().equals(organization.trim()))
//	    	{
//	    		callingCosts= callingCostService.findAllByExtensionAndOrganization(extension,organization,searchText,pageable);
//	    		return status(HttpStatus.OK).body(callingCosts);
//	    	}
//	    	else
//	    	{
//	    		//System.out.println("I am in else controller");
//	    		
//	    		return status(HttpStatus.UNAUTHORIZED).body(callingCosts);
//	    	} 	
//		}
//		
//		
//		@GetMapping("/findAllByAmountGreaterThanEqualAndOrganization")
//		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//		public ResponseEntity<CallingCostPageDTO> findAllByAmountGreaterThanEqualAndOrganization(@RequestParam Double amount,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
//	        
//		Pageable pageable;
//		if(pageNumber <0)
//		{
//			pageable= PageRequest.of(0, 1000000000);
//		}
//		else
//		{
//			pageable = PageRequest.of(pageNumber, size);
//		}
//		
//		CallingCostPageDTO callingCosts= null;
//			
//	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//	    	
//	    	//System.out.println(token);
//	    	
//	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//	    	
//	    	if(employee.getOrganization().trim().equals(organization.trim()))
//	    	{
//	    		callingCosts= callingCostService.findAllByAmountGreaterThanEqualAndOrganization(amount,organization,searchText,pageable);
//	    		return status(HttpStatus.OK).body(callingCosts);
//	    	}
//	    	else
//	    	{
//	    		//System.out.println("I am in else controller");
//	    		
//	    		return status(HttpStatus.UNAUTHORIZED).body(callingCosts);
//	    	} 	
//		}
//		
//		
//		@GetMapping("/findAllByAmountLessThanEqualAndOrganization")
//		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//		public ResponseEntity<CallingCostPageDTO> findAllByAmountLessThanEqualAndOrganization(@RequestParam Double amount,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
//	        
//		Pageable pageable;
//		if(pageNumber <0)
//		{
//			pageable= PageRequest.of(0, 1000000000);
//		}
//		else
//		{
//			pageable = PageRequest.of(pageNumber, size);
//		}
//		
//		CallingCostPageDTO callingCosts= null;
//			
//	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//	    	
//	    	//System.out.println(token);
//	    	
//	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//	    	
//	    	if(employee.getOrganization().trim().equals(organization.trim()))
//	    	{
//	    		callingCosts= callingCostService.findAllByAmountLessThanEqualAndOrganization(amount,organization,searchText,pageable);
//	    		return status(HttpStatus.OK).body(callingCosts);
//	    	}
//	    	else
//	    	{
//	    		//System.out.println("I am in else controller");
//	    		
//	    		return status(HttpStatus.UNAUTHORIZED).body(callingCosts);
//	    	} 	
//		}
//		
//		
//		@GetMapping("/findAllByAmountGreaterThanEqualAndCallcalculationAndOrganization")
//		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//		public ResponseEntity<CallingCostPageDTO> findAllByAmountGreaterThanEqualAndCallcalculationAndOrganization(@RequestParam Double amount,@RequestParam String callcalculation,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
//	        
//		Pageable pageable;
//		if(pageNumber <0)
//		{
//			pageable= PageRequest.of(0, 1000000000);
//		}
//		else
//		{
//			pageable = PageRequest.of(pageNumber, size);
//		}
//		
//		CallingCostPageDTO callingCosts= null;
//			
//	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//	    	
//	    	//System.out.println(token);
//	    	
//	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//	    	
//	    	if(employee.getOrganization().trim().equals(organization.trim()))
//	    	{
//	    		callingCosts= callingCostService.findAllByAmountGreaterThanEqualAndCallcalculationAndOrganization(amount,callcalculation,organization,searchText,pageable);
//	    		return status(HttpStatus.OK).body(callingCosts);
//	    	}
//	    	else
//	    	{
//	    		//System.out.println("I am in else controller");
//	    		
//	    		return status(HttpStatus.UNAUTHORIZED).body(callingCosts);
//	    	} 	
//		}
//		
//		@GetMapping("/findAllByAmountLessThanEqualAndCallcalculationAndOrganization")
//		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//		public ResponseEntity<CallingCostPageDTO> findAllByAmountLessThanEqualAndCallcalculationAndOrganization(@RequestParam Double amount,@RequestParam String callcalculation,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
//	        
//		Pageable pageable;
//		if(pageNumber <0)
//		{
//			pageable= PageRequest.of(0, 1000000000);
//		}
//		else
//		{
//			pageable = PageRequest.of(pageNumber, size);
//		}
//		
//		CallingCostPageDTO callingCosts= null;
//			
//	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//	    	
//	    	//System.out.println(token);
//	    	
//	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//	    	
//	    	if(employee.getOrganization().trim().equals(organization.trim()))
//	    	{
//	    		callingCosts= callingCostService.findAllByAmountLessThanEqualAndCallcalculationAndOrganization(amount,callcalculation,organization,searchText,pageable);
//	    		return status(HttpStatus.OK).body(callingCosts);
//	    	}
//	    	else
//	    	{
//	    		//System.out.println("I am in else controller");
//	    		
//	    		return status(HttpStatus.UNAUTHORIZED).body(callingCosts);
//	    	} 	
//		}
//		
//		@GetMapping("/findAllByCallcalculationAndOrganization")
//		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//		public ResponseEntity<CallingCostPageDTO> findAllByCallcalculationAndOrganization(@RequestParam String callcalculation,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
//	        
//		Pageable pageable;
//		if(pageNumber <0)
//		{
//			pageable= PageRequest.of(0, 1000000000);
//		}
//		else
//		{
//			pageable = PageRequest.of(pageNumber, size);
//		}
//		
//		CallingCostPageDTO callingCosts= null;
//			
//	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//	    	
//	    	//System.out.println(token);
//	    	
//	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//	    	
//	    	if(employee.getOrganization().trim().equals(organization.trim()))
//	    	{
//	    		callingCosts= callingCostService.findAllByCallcalculationAndOrganization(callcalculation,organization,searchText,pageable);
//	    		return status(HttpStatus.OK).body(callingCosts);
//	    	}
//	    	else
//	    	{
//	    		//System.out.println("I am in else controller");
//	    		
//	    		return status(HttpStatus.UNAUTHORIZED).body(callingCosts);
//	    	} 	
//		}
//	
//	    @GetMapping("/export/mylinehubexcel")
//	    @ResponseStatus(HttpStatus.CREATED)
//	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//	    public void exportToExcel(HttpServletResponse response,@RequestHeader (name="Authorization") String token) throws IOException {
//	    	
//	    	String parentorganization = env.getProperty("spring.parentorginization");
//	    	
//	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//	    	
//	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//	    	 
//	    	if(employee.getOrganization().trim().equals(parentorganization.trim()))
//	    	{
//	    		callingCostService.exportToExcel(response);
//	    	}
//	    	else
//	    	{
//	    		
//	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//	    	}
//	        
//	    }
//
//	    @GetMapping("/export/organization/excel")
//	    @ResponseStatus(HttpStatus.CREATED)
//	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//	    public void exportToExcelOnOrganization(@RequestParam String organization,@RequestParam Date date,@RequestParam Date endDate,HttpServletResponse response, @RequestHeader (name="Authorization") String token) throws IOException {
//
//	    	
//	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//	    	
//	    	//System.out.println(token);
//	    	
//	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//	    	
//	    	if(employee.getOrganization().trim().equals(organization.trim()))
//	    	{
//	    		callingCostService.exportToExcelOnOrganization(date,endDate,organization,response);
//	    	}
//	    	else
//	    	{
//	    		//System.out.println("I am in else controller");
//	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//	    		Report.addLog("Unauthorized", "Employee needs manager access","CallingCost", "Cannot Download Excel",organization,logRepository);	
//	    	} 	
//	    	
//	    }
//	    
//	    @GetMapping("/export/mylinehubpdf")
//	    @ResponseStatus(HttpStatus.CREATED)
//	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//	    public void exportToPDF(HttpServletResponse response,@RequestHeader (name="Authorization") String token) throws IOException {
//	    	
//	    	String parentorganization = env.getProperty("spring.parentorginization");
//	    	
//	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//	    	
//	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//	    	 
//	    	if(employee.getOrganization().trim().equals(parentorganization.trim()))
//	    	{
//	    		callingCostService.exportToPDF(response);
//	    	}
//	    	else
//	    	{
//	    		
//	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//	    	}
//	        
//	    }
//
//	    @GetMapping("/export/organization/pdf")
//	    @ResponseStatus(HttpStatus.CREATED)
//	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//	    public void exportToPDFOnOrganization(@RequestParam String organization,@RequestParam Date date,@RequestParam Date endDate,HttpServletResponse response, @RequestHeader (name="Authorization") String token) throws IOException {
//
//	    	
//	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//	    	
//	    	//System.out.println(token);
//	    	
//	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//	    	
//	    	if(employee.getOrganization().trim().equals(organization.trim()))
//	    	{
//	    		callingCostService.exportToPDFOnOrganization(date,endDate,organization,response);
//	    	}
//	    	else
//	    	{
//	    		//System.out.println("I am in else controller");
//	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//	    		Report.addLog("Unauthorized", "Employee needs manager access","CallingCost", "Cannot Download PDF",organization,logRepository);	
//	    	} 	
//	    	
//	    }
//	
//}
