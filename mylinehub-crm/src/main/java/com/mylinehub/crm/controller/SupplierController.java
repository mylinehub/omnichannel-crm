package com.mylinehub.crm.controller;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.SupplierDTO;
import com.mylinehub.crm.exports.ExcelHelper;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.LogRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.SupplierService;
import com.mylinehub.crm.utils.ResponseMessage;

import lombok.AllArgsConstructor;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.mylinehub.crm.controller.ApiMapping.SUPPLIERS_REST_URL;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping(produces="application/json", path = SUPPLIERS_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class SupplierController {
    private final SupplierService supplierService;
    private final EmployeeRepository employeeRepository;
    private final LogRepository logRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
	private Environment env;
	
	@GetMapping("/getAllsuppliersByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<SupplierDTO>> getAllsuppliersByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<SupplierDTO> suppliers = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		suppliers= supplierService.getAllSuppliersOnOrganization(organization);
    		return status(HttpStatus.OK).body(suppliers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(suppliers);
    	} 	
	}
	
	@GetMapping("/getAllsuppliersOnTransportcapacityAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<SupplierDTO>> getAllsuppliersOnTransportcapacityAndOrganization(@RequestParam String transportcapacity,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<SupplierDTO> suppliers = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		suppliers= supplierService.findAllByTransportcapacityAndOrganization(transportcapacity,organization);
    		return status(HttpStatus.OK).body(suppliers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(suppliers);
    	} 	
	}
	
	@GetMapping("/getAllSupplierOnTypeAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<SupplierDTO>> getAllSupplierOnTypeAndOrganization(@RequestParam String supplierType,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<SupplierDTO> suppliers = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		suppliers= supplierService.findAllBySuppliertypeAndOrganization(supplierType,organization);
    		return status(HttpStatus.OK).body(suppliers);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(suppliers);
    	} 	
	}
	
	@GetMapping("/getSupplierByIdAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<SupplierDTO> getSupplierByIdAndOrganization(@RequestParam Long id,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		SupplierDTO searchSupplier = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		searchSupplier= supplierService.getByIdAndOrganization(id,organization);
    		return status(HttpStatus.OK).body(searchSupplier);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(searchSupplier);
    	} 	
	}
	
	
	@PostMapping("/createSupplierByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> createSupplierByOrganization(@RequestBody SupplierDTO supplierDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()) && (employee.getOrganization().trim().equals(supplierDTO.getOrganization().trim())))
    	{
    		toReturn = supplierService.createSupplierByOrganization(supplierDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@PostMapping("/updateSupplierByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSupplierByOrganization(@RequestBody SupplierDTO SupplierDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		//System.out.println("Let us update an employee");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = supplierService.updateSupplierByOrganization(SupplierDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@DeleteMapping("/deleteSupplierByIdAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> deleteSupplierByIdAndOrganization(@RequestParam Long id,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn  = supplierService.deleteSupplierByIdAndOrganization(id, organization);
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
    		supplierService.exportToExcel(response);
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
    		supplierService.exportToExcelOnOrganization(organization,response);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    		Report.addLog("Unauthorized", "Employee needs manager access","Supplier", "Cannot Download Excel",organization,logRepository);
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
    		supplierService.exportToPDF(response);
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
    		supplierService.exportToPDFOnOrganization(organization,response);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    		Report.addLog("Unauthorized", "Employee needs manager access","Supplier", "Cannot Download PDF",organization,logRepository);
    	} 	
    	
    }
    
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
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
            	
            	supplierService.uploadSupplierUsingExcel(file,organization);

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
