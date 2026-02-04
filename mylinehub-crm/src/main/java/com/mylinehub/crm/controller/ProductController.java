package com.mylinehub.crm.controller;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.CustomerProductInterestDTO;
import com.mylinehub.crm.entity.dto.MediaDto;
import com.mylinehub.crm.entity.dto.ProductDTO;
import com.mylinehub.crm.exports.ExcelHelper;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.LogRepository;
import com.mylinehub.crm.requests.MultipleIDRequest;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.FileService;
import com.mylinehub.crm.service.ProductService;
import com.mylinehub.crm.utils.ResponseMessage;

import lombok.AllArgsConstructor;

import org.springframework.context.ApplicationContext;
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

import static com.mylinehub.crm.controller.ApiMapping.PRODUCTS_REST_URL;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping(produces="application/json", path = PRODUCTS_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class ProductController {
    private final ProductService productService;
    private final EmployeeRepository employeeRepository;
    private final LogRepository logRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
	private Environment env;
	private final FileService fileService;
	private final ApplicationContext applicationContext;
	
	@GetMapping("/getProductByIdAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<ProductDTO> getProductByIdAndOrganization(@RequestParam Long id,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		ProductDTO searchSupplier = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		searchSupplier= productService.getByIdAndOrganization(id,organization);
    		return status(HttpStatus.OK).body(searchSupplier);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(searchSupplier);
    	} 	
	}
	
	
	@PostMapping("/createProductByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> createProductByOrganization(@RequestBody ProductDTO productDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()) && (employee.getOrganization().trim().equals(productDTO.getOrganization().trim())))
    	{
    		toReturn = productService.createProductByOrganization(productDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@PostMapping("/updateProductByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateProductByOrganization(@RequestBody ProductDTO ProductDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		//System.out.println("Let us update an employee");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = productService.updateProductByOrganization(ProductDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@DeleteMapping("/deleteProductByIdAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> deleteProductByIdAndOrganization(@RequestParam Long id,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn  = productService.deleteProductByIdAndOrganization(employee,id, organization);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	} 
	
	
	@GetMapping("/getAllproductsByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<ProductDTO>> getAllproductsByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<ProductDTO> products = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		products= productService.getAllProductsOnOrganization(organization);
    		return status(HttpStatus.OK).body(products);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(products);
    	} 	
	}
	
	@GetMapping("/getAllproductsOnProductTypeAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<ProductDTO>> getAllproductsOnProductTypeAndOrganization(@RequestParam String productType,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<ProductDTO> products = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		products= productService.getAllProductOnProductTypeAndOrganization(productType,organization);
    		return status(HttpStatus.OK).body(products);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(products);
    	} 	
	}
	
	@PostMapping("/findAllProductsByIdIn")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<CustomerProductInterestDTO>> findAllProductsByIdIn(@RequestBody MultipleIDRequest productIds,@RequestHeader (name="Authorization") String token){
	        
		List<CustomerProductInterestDTO> products = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(productIds.getOrganization().trim()))
    	{
    		products= productService.findAllProductsByIdIn(productIds.getIds(),productIds.getOrganization());
    		return status(HttpStatus.OK).body(products);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(products);
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
    		productService.exportToExcel(response);
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
    		productService.exportToExcelOnOrganization(organization,response);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    		Report.addLog("Unauthorized", "Employee needs manager access","Product", "Cannot Download Excel",organization,logRepository);
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
    		productService.exportToPDF(response);
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
    		productService.exportToPDFOnOrganization(organization,response);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    		Report.addLog("Unauthorized", "Employee needs manager access","Product", "Cannot Download PDF",organization,logRepository);
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
            	
            	productService.uploadProductUsingExcel(file,organization);

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
    
    @PostMapping("/uploadProductPicByEmailAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> uploadProductPicByEmailAndOrganization(@RequestParam Long id,@RequestParam String organization,@RequestParam("image") MultipartFile image,@RequestHeader (name="Authorization") String token) throws Exception{
        
        Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		productService.uploadProductPicByEmailAndOrganization(employee,image,id,organization); 
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	    
    }
    
    @GetMapping("/getProductImage")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<MediaDto> getProductImage(@RequestParam Long id,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{    
    	MediaDto returnImage = new MediaDto();
        
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	//System.out.println(token);
       	Employee employeeSelf= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	if(employeeSelf.getOrganization().trim().equals(organization.trim()))
    	{
       		try {
       		 
       		 ProductDTO current = productService.getByIdAndOrganization(id, organization);
       		 String uploadOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadProductOriginalDirectory");

       	     String name = current.getImageData();
	       	 if(name !=null)
	    	     {
	    	    	name = name.replace(uploadOriginalDirectory+"/", "");
	
		       	  try {
		       		    returnImage.setByteData(fileService.getFile(uploadOriginalDirectory, name));
		       		    returnImage.setName(current.getImageName());
		       		    returnImage.setType(current.getImageType());
		        	 }
		        	 catch(Exception e)
		        	 {
		        		 e.printStackTrace();
		        		 returnImage=null;
		        	 }  
	    	     }
	    	 else
	    	     {
	    		 returnImage = null;
	    	     }

                // Respond with the image data and an OK status code
                return status(HttpStatus.OK).body(returnImage);
            } catch (Exception e) {
                // Handle exceptions and provide appropriate error responses
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
