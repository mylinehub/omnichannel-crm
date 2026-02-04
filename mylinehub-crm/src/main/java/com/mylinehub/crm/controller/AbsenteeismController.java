package com.mylinehub.crm.controller;

import com.mylinehub.crm.entity.Absenteeism;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.AbsenteeismDTO;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.LogRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.AbsenteeismService;
import lombok.AllArgsConstructor;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.mylinehub.crm.controller.ApiMapping.ABSENTEEISMS_REST_URL;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping(produces="application/json", path = ABSENTEEISMS_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class AbsenteeismController {
	
    private final AbsenteeismService absenteeismService;
    private final EmployeeRepository employeeRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
    private final LogRepository logRepository;
	private Environment env;
	
	
	@GetMapping("/getAllAbsenteeismOnOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<AbsenteeismDTO>> getAllAbsenteeismOnOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<AbsenteeismDTO> absenteeism = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		absenteeism = absenteeismService.getAllAbsenteeismOnOrganization(organization);
    		return status(HttpStatus.OK).body(absenteeism);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(absenteeism);
    	} 	
	}
	
	@GetMapping("/findAllByReasonForAbsenseAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<AbsenteeismDTO>> findAllByReasonForAbsenseAndOrganization(@RequestParam String reasonForAbsense,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<AbsenteeismDTO> absenteeism = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		absenteeism = absenteeismService.findAllByReasonForAbsenseAndOrganization(reasonForAbsense,organization);
    		return status(HttpStatus.OK).body(absenteeism);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(absenteeism);
    	} 	
	}
	
	@GetMapping("/findAllByEmployeeAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<AbsenteeismDTO>> findAllByEmployeeAndOrganization(@RequestParam Long employeeID,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<AbsenteeismDTO> absenteeism = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee searchEmployee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	Employee employee= employeeRepository.findById(employeeID).get();
    	
    	if(searchEmployee.getOrganization().trim().equals(organization.trim()))
    	{
    		absenteeism = absenteeismService.findAllByEmployeeAndOrganization(employee,organization);
    		return status(HttpStatus.OK).body(absenteeism);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(absenteeism);
    	} 	
	}
	
	@GetMapping("/findAllByDateFromGreaterThanEqualAndDateToLessThanEqualOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<AbsenteeismDTO>> findAllByDateFromGreaterThanEqualAndDateToLessThanEqualOrganization(@RequestParam Date dateFrom,@RequestParam Date dateTo,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<AbsenteeismDTO> absenteeism = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		absenteeism = absenteeismService.findAllByDateFromGreaterThanEqualAndDateToLessThanEqualOrganization(dateFrom,dateTo,organization);
    		return status(HttpStatus.OK).body(absenteeism);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(absenteeism);
    	} 	
	}
	
	
	@PostMapping("/createAbsenteeismByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> createAbsenteeismByOrganization(@RequestBody AbsenteeismDTO absenteeismDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim())&& employee.getId().equals(absenteeismDTO.getEmployeeID()))
    	{
    		toReturn = absenteeismService.createAbsenteeismByOrganization(absenteeismDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@PostMapping("/updateAbsenteeismByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateAbsenteeismByOrganization(@RequestBody AbsenteeismDTO absenteeismDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		//System.out.println("Let us update an employee");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim())&& employee.getId().equals(absenteeismDTO.getEmployeeID()))
    	{
    		toReturn = absenteeismService.updateAbsenteeismByOrganization(absenteeismDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@DeleteMapping("/deleteAbsenteeismByIdAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> deleteAbsenteeismByIdAndOrganization(@RequestParam Long id,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	Absenteeism current = absenteeismService.getAbsenteeismByIdAndOrganization(id,organization);
    	
    	if(employee.getOrganization().trim().equals(organization.trim())&& employee.getId().equals(current.getEmployee().getId()))
    	{
    		toReturn  = absenteeismService.deleteAbsenteeismByOrganization(id, organization);
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
    		absenteeismService.exportToExcel(response);
    	}
    	else
    	{
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    		Report.addLog("Unauthorized", "Sys Employee needs admin access","Absenteeism", "Cannot Download Excel","",logRepository);		
            
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
    		absenteeismService.exportToExcelOnOrganization(organization,response);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    		Report.addLog("Unauthorized", "Employee needs manager access","Absenteeism", "Cannot Download Excel",organization,logRepository);		
        	
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
    		absenteeismService.exportToPDF(response);
    	}
    	else
    	{
    		
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    		Report.addLog("Unauthorized", "Sys Employee needs admin access","Absenteeism", "Cannot Download PDF","",logRepository);		
        	
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
    		absenteeismService.exportToPDFOnOrganization(organization,response);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    		Report.addLog("Unauthorized", "Employee needs manager access","Absenteeism", "Cannot Download PDF",organization,logRepository);		
    	} 	
    	
    }
}
