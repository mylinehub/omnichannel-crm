package com.mylinehub.crm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.Product;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.entity.dto.CustomerProductInterestDTO;
import com.mylinehub.crm.entity.dto.ProductDTO;
import com.mylinehub.crm.enums.USER_ROLE;
import com.mylinehub.crm.exports.pdf.ExportProductsToPDF;
import com.mylinehub.crm.exports.excel.BulkUploadProductsToDatabase;
import com.mylinehub.crm.exports.excel.ExportProductsToXLSX;
import com.mylinehub.crm.mapper.CustomerProductInterestMapper;
import com.mylinehub.crm.mapper.ProductMapper;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.repository.ProductRepository;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;
import lombok.AllArgsConstructor;

import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class ProductService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CustomerProductInterestMapper customerProductInterestMapper;
    private final ErrorRepository errorRepository;
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    private final FileService fileService;
    private final ApplicationContext applicationContext;
    
    public Product getProductByNameAndOrganization(String name,String organization) {
    	return productRepository.getProductByNameAndOrganization(name, organization);
    }
    
    public ProductDTO getByIdAndOrganization(Long id,String organization) {
    	return productMapper.mapProductToDto(productRepository.getProductByIdAndOrganization(id, organization));
    }
    
    public Product getProductByIdAndOrganization(Long id,String organization) {
    	return productRepository.getProductByIdAndOrganization(id, organization);
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean createProductByOrganization(ProductDTO productDetails) {
    	
    	Product current = productRepository.getProductByNameAndOrganization(productDetails.getName(),productDetails.getOrganization());
    	
    	if(current==null)
    	{
    		current = productMapper.mapDTOToProduct(productDetails);
    		productRepository.save(current);
    		try {
      		  List<Product> products = new ArrayList<Product>();
      		  products.add(current);
            	  sendProductNotifications("create",products);
              }
              catch(Exception e)
              {
            	  e.printStackTrace();
              }
    	}
    	else
    	{
    		
    		
    		return false;
    	}
    	
        return true;
    }

    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean updateProductByOrganization(ProductDTO productDetails) {
    	
    	Product current = productRepository.getProductByIdAndOrganization(productDetails.getId(),productDetails.getOrganization());
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{
    		
    		try
    		{

    			current.setOrganization(productDetails.getOrganization());
    			current.setName(productDetails.getName());
    			current.setProductStringType(productDetails.getProductStringType());
    			current.setPurchasePrice(productDetails.getPurchasePrice());
    			current.setSellingPrice(productDetails.getSellingPrice());
    			current.setTaxRate(productDetails.getTaxRate());
    			current.setUnits(productDetails.getUnits());
    			current.setImageName(productDetails.getImageName());
      			current.setImageType(productDetails.getImageType());
      			current.setImageData(productDetails.getImageData());
        		productRepository.save(current);
        		try {
          		  List<Product> products = new ArrayList<Product>();
          		  products.add(current);
                	  sendProductNotifications("update",products);
                  }
                  catch(Exception e)
                  {
                	  e.printStackTrace();
                  }
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			System.out.println("Exception while updating employee");
    			return false;
    		}
    		
    	}
    	
        return true;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean deleteProductByIdAndOrganization(Employee employee,Long id, String organization) {
    	
    	Product current = productRepository.getProductByIdAndOrganization(id,organization);
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{    

    		//Delete product image
    		String uploadProductOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadProductOriginalDirectory");
        	if(current.getImageData() != null || current.getImageData() != "")
        	{
        		try {
        			String name = current.getImageData();
            		name = name.replace(uploadProductOriginalDirectory+"/", "");
            		fileService.deleteFile(employee.getOrganization(),uploadProductOriginalDirectory, name);
        		}
        		catch(Exception e)
        		{
        			//Donot do anything
        			System.out.println("While deleting product");
//        			e.printStackTrace();
        		}
        		
        	}
        	
        	
    		//Delete product in database
    		productRepository.delete(current);
    		
    		
    		try {
    		  List<Product> products = new ArrayList<Product>();
    		  products.add(current);
          	  sendProductNotifications("delete",products);
            }
            catch(Exception e)
            {
          	  e.printStackTrace();
            }
    	}
    	
        return true;
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<ProductDTO> getAllProductOnProductTypeAndOrganization(String productType, String organization){
        return productRepository.findAllByProductStringTypeAndOrganization(productType, organization)
                .stream()
                .map(productMapper::mapProductToDto)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<ProductDTO> getAllProductsOnOrganization(String organization){
        return productRepository.findAllByOrganization(organization)
                .stream()
                .map(productMapper::mapProductToDto)
                .collect(Collectors.toList());
    }

    /**
     * The purpose of the method is to set the details of the
     * excel file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToExcelOnOrganization(String organization,HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=AMIConnection_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Product> productList = productRepository.findAllByOrganization(organization);

        ExportProductsToXLSX exporter = new ExportProductsToXLSX(productList);
        exporter.export(response);
    }
    
    /**
     * The purpose of the method is to set the details of the
     * pdf file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=products_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Product> productList = productRepository.findAll();

        ExportProductsToPDF exporter = new ExportProductsToPDF(productList);
        exporter.export(response);
    }

    /**
     * The purpose of the method is to set the details of the
     * pdf file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToPDFOnOrganization(String organization,HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=products_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Product> productList = productRepository.findAllByOrganization(organization);

        ExportProductsToPDF exporter = new ExportProductsToPDF(productList);
        exporter.export(response);
    }
    
    /**
     * The method is to retrieve products whose have the name specified by the user.
     * After downloading all the data about the product,
     * the data is mapped to dto which will display only those needed
     * @param name name of the product
     * @return details of specific products
     */
    
    public List<ProductDTO> findAllByName(String name, Pageable pageable) {
        return productRepository.findProductsByNameContaining(name, pageable)
                .stream()
                .map(productMapper::mapProductToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * The method is to retrieve products whose have the name specified by the user.
     * After downloading all the data about the product,
     * the data is mapped to dto which will display only those needed
     * @param name name of the product
     * @return details of specific products
     */
    
    public List<CustomerProductInterestDTO> findAllProductsByIdIn(List<Long> ids, String organization) {
        return productRepository.findAllProductsByIdIn(ids, organization)
                .stream()
                .map(customerProductInterestMapper::mapProductToCustomerProductInterestDto)
                .collect(Collectors.toList());
    }

    /**
     * The method is to retrieve products whose have the type specified by the user.
     * After downloading all the data about the product,
     * the data is mapped to dto which will display only those needed
     * @param productType type of the product
     * @return details of specific products
     */
    
    public List<ProductDTO> findAllByProductType(String productType, Pageable pageable){
        return productRepository.findProductsByProductStringTypeFullNameContaining(productType, pageable)
                .stream()
                .map(productMapper::mapProductToDto)
                .collect(Collectors.toList());
    }
    
    public void uploadProductUsingExcel(MultipartFile file,String organization) throws Exception {
        try {
        	
        	//System.out.println("I am inside try of uoload Employee Service");
          List<Product> products = new BulkUploadProductsToDatabase().excelToProducts(this,file.getInputStream(),organization,errorRepository);
          productRepository.saveAll(products);
          try {
        	  sendProductNotifications("create",products);
          }
          catch(Exception e)
          {
        	  e.printStackTrace();
          }
        } catch (IOException e) {
        	//System.out.println("I am inside catch of Upload service");
          throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
        
        
        
      }
    
    
    /**
     * The method is to retrieve all products from the database and display them.
     *
     * After downloading all the data about the product,
     * the data is mapped to dto which will display only those needed
     * @return list of all products with specification of data in ProductDTO
     */
    
    public List<ProductDTO> getAllProducts(Pageable pageable){
        return productRepository.findAll(pageable)
                .stream()
                .map(productMapper::mapProductToDto)
                .collect(Collectors.toList());
    }

    /**
     * The method is to download a specific products from the database and display it.
     * After downloading all the data about the products,
     * the data is mapped to dto which will display only those needed
     *
     * @param id id of the product to be searched for
     * @throws ResponseStatusException if the id of the product you are looking for does not exist throws 404 status
     * @return detailed data about a specific product
     */
    
    public ProductDTO getProductById(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product cannot be found, the specified id does not exist"));
        return productMapper.mapProductToDto(product);
    }

    @Transactional
    public ProductDTO editProduct(Product product){
        Product editedProduct = productRepository.findById(product.getId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product cannot be found"));
        editedProduct.setName(product.getName());
        editedProduct.setSellingPrice(product.getSellingPrice());
        editedProduct.setPurchasePrice(product.getPurchasePrice());
        editedProduct.setTaxRate(product.getTaxRate());
        return productMapper.mapProductToDto(editedProduct);
    }

    /**
     * The task of the method is to add a product to the database.
     * @param product requestbody of the customer to be saved
     * @return saving the product to the database
     */
    public Product addNewProduct(Product product) {
        return productRepository.save(product);
    }

    /**
     * Method deletes the selected product by id
     * @param id id of the product to be deleted
     * @throws ResponseStatusException if id of the product is incorrect throws 404 status with message
     */
    public void deleteProductById(Long id) {
        try{
            productRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The specified id does not exist");
        }
    }

    /**
     * The purpose of the method is to set the details of the
     * excel file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=products_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Product> productList = productRepository.findAll();

        ExportProductsToXLSX exporter = new ExportProductsToXLSX(productList);
        exporter.export(response);
    }
    
    public boolean uploadProductPicByEmailAndOrganization(Employee employee,MultipartFile image,Long id,String organization) throws Exception {
    	String uploadProductOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadProductOriginalDirectory");
    	Product current = productRepository.getProductByIdAndOrganization(id, organization);
    	if(current.getImageData() != null || current.getImageData() != "")
    	{
    		try {
    			String name = current.getImageData();
        		name = name.replace(uploadProductOriginalDirectory+"/", "");
        		fileService.deleteFile(employee.getOrganization(),uploadProductOriginalDirectory, name);
    		}
    		catch(Exception e)
    		{
    			//Donot do anything
    		}
    		
    	}
    	
    	try {
    		String imagesLocation = fileService.saveFileToStorage(employee.getOrganization(),uploadProductOriginalDirectory, image);
        	current.setImageData(uploadProductOriginalDirectory+"/"+imagesLocation);
        	current.setImageType(image.getContentType());
        	current.setImageName(image.getOriginalFilename());
        	current.setImageSize(image.getSize());
        	productRepository.save(current);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    	return true;
    }
    
    
    /**
     * The purpose of the method is to send notifications
     */
    public void sendProductNotifications(String type, List<Product> products) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();
		List<Notification> allNotifications = new ArrayList<Notification>();
		products.forEach(
				(product) -> {
					
					List<Employee> allAdmins = employeeRepository.findAllByUserRoleAndOrganization(USER_ROLE.ADMIN, product.getOrganization());
					
					if(allAdmins != null)
					{
						allAdmins.forEach(
								(employee) -> {
									
									  /*
									    * 
									    * Example on how to set up notification
									    * 
									    *  { alertType: 'alert-success', title: 'Well done!' , message: 'You successfullyread this important.'},
										  { alertType: 'alert-info', title: 'Heads up!', message: 'This alert needs your attention, but it\'s not super important.' },
										  { alertType: 'alert-warning', title: 'Warning!', message: 'Better check yourself, you\'re not looking too good.' },
										  { alertType: 'alert-danger', title: 'Oh snap!', message: 'Change a few things up and try submitting again.' },
										  { alertType: 'alert-primary', title: 'Good Work!', message: 'You completed the training number 23450 well.' },

									    */
							    	BotInputDTO msg;
									Notification notification;
							    	switch(type)
							    	{
							    	 case "create": 
							    		   
							    		   notification = new Notification();
							    		   notification.setCreationDate(new Date());
							    		   notification.setAlertType("alert-success");
							    		   notification.setForExtension(employee.getExtension());
							    		   notification.setMessage("Organization deals in product "+product.getName()+" now.");
							    		   notification.setNotificationType("product");
							    		   notification.setOrganization(employee.getOrganization());
							    		   notification.setTitle("Use-It!");
							    		   
							    		   msg = new BotInputDTO();
								    	   msg.setDomain(employee.getDomain());
								    	   msg.setExtension(employee.getExtension());
								    	   msg.setFormat("json");
								    	   try {
											msg.setMessage(mapper.writeValueAsString(notification));
											} catch (JsonProcessingException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
								    	   msg.setMessagetype("notification");
								    	   msg.setOrganization(employee.getOrganization());
									    	try {
										       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
										    }
										    catch(Exception e)
										    {
											   e.printStackTrace();
										    }
							    		   allNotifications.add(notification);
							    		      
							    	   break;
							    	   
							    	   case "update": 
							    		   
							    		   notification = new Notification();
							    		   notification.setCreationDate(new Date());
							    		   notification.setAlertType("alert-info");
							    		   notification.setForExtension(employee.getExtension());
							    		   notification.setMessage("Organization updated product "+product.getName()+".");
							    		   notification.setNotificationType("product");
							    		   notification.setOrganization(employee.getOrganization());
							    		   notification.setTitle("Know!");
							    		   
							    		   msg = new BotInputDTO();
								    	   msg.setDomain(employee.getDomain());
								    	   msg.setExtension(employee.getExtension());
								    	   msg.setFormat("json");
								    	   try {
											msg.setMessage(mapper.writeValueAsString(notification));
											} catch (JsonProcessingException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
								    	   msg.setMessagetype("notification");
								    	   msg.setOrganization(employee.getOrganization());
									    	try {
										       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
										    }
										    catch(Exception e)
										    {
											   e.printStackTrace();
										    }
							    		   allNotifications.add(notification); 
							    		   break;
							    		   
	                                       case "delete": 
							    		   
							    		   notification = new Notification();
							    		   notification.setCreationDate(new Date());
							    		   notification.setAlertType("alert-danger");
							    		   notification.setForExtension(employee.getExtension());
							    		   notification.setMessage("Organization does not deal in product "+product.getName()+" now.");
							    		   notification.setNotificationType("product");
							    		   notification.setOrganization(employee.getOrganization());
							    		   notification.setTitle("Stop!");
							    		   
							    		   msg = new BotInputDTO();
								    	   msg.setDomain(employee.getDomain());
								    	   msg.setExtension(employee.getExtension());
								    	   msg.setFormat("json");
								    	   try {
											msg.setMessage(mapper.writeValueAsString(notification));
											} catch (JsonProcessingException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
								    	   msg.setMessagetype("notification");
								    	   msg.setOrganization(employee.getOrganization());
									    	try {
										       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
										    }
										    catch(Exception e)
										    {
											   e.printStackTrace();
										    }
							    		   allNotifications.add(notification); 
							    		   break;
							    		   
								    	   default:
								    	   break;
							    	}
								});
						
						
					}
   	            });

    	notificationRepository.saveAll(allNotifications);	
    }
}
