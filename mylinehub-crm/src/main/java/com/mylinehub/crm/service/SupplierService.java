package com.mylinehub.crm.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.Supplier;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.entity.dto.SupplierDTO;
import com.mylinehub.crm.enums.USER_ROLE;
import com.mylinehub.crm.exports.excel.BulkUploadSuppliersToDatabase;
import com.mylinehub.crm.exports.excel.ExportSuppliersToXLSX;
import com.mylinehub.crm.exports.pdf.ExportSuppliersToPDF;
import com.mylinehub.crm.mapper.SupplierMapper;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.repository.SupplierRepository;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
public class SupplierService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final ErrorRepository errorRepository;
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    
    public Supplier getSupplierByNameAndOrganization(String SupplierName,String organization) {
    	return supplierRepository.getSupplierBySupplierNameAndOrganization(SupplierName, organization);
    }
    
    public Supplier getSupplierByIdAndOrganization(Long id,String organization) {
    	return supplierRepository.getSupplierBySupplierIdAndOrganization(id, organization);
    }
    
    public SupplierDTO getByIdAndOrganization(Long id,String organization) {
    	return supplierMapper.mapSupplierToDTO(supplierRepository.getSupplierBySupplierIdAndOrganization(id, organization));
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean createSupplierByOrganization(SupplierDTO supplierDetails) {
    	
    	Supplier current = supplierRepository.getSupplierBySupplierNameAndOrganization(supplierDetails.getSupplierName(),supplierDetails.getOrganization());
    	
    	if(current==null)
    	{
    		try
    		{
    			current = new Supplier();
    			
    			current.setSupplierName(String.valueOf(supplierDetails.getSupplierName()));
    			current.setSuppliertype(String.valueOf(supplierDetails.getSuppliertype()));
    			current.setTransportcapacity(String.valueOf(supplierDetails.getTransportcapacity()));
    			current.setWeightunit(String.valueOf(supplierDetails.getWeightunit()));
    			current.setPriceunits(String.valueOf(supplierDetails.getPriceunits()));
    			current.setOrganization(String.valueOf(supplierDetails.getOrganization()));
    			current.setModeOfTransport(String.valueOf(supplierDetails.getModeOfTransport()));
    			current.setLengthunit(String.valueOf(supplierDetails.getLengthunit()));
    			current.setActivityStatus(String.valueOf(supplierDetails.getActivityStatus()));
    			current.setSupplierPhoneNumber(String.valueOf(supplierDetails.getSupplierPhoneNumber()));
        		supplierRepository.save(current);
        		
        		try {
        		      List<Supplier> suppliers = new ArrayList<Supplier>();
        		      suppliers.add(current);
              	  sendSupplierNotifications("create",suppliers);
                }
                catch(Exception e)
                {
              	  e.printStackTrace();
                }
        		
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			System.out.println("Exception while creating employee");
    			return false;
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
    public boolean updateSupplierByOrganization(SupplierDTO supplierDetails) {
    	
    	Supplier current = supplierRepository.getSupplierBySupplierIdAndOrganization(supplierDetails.getSupplierId(),supplierDetails.getOrganization());
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{
    		
    		try
    		{
    			current.setSupplierName(supplierDetails.getSupplierName());
    			current.setSuppliertype(supplierDetails.getSuppliertype());
    			current.setTransportcapacity(supplierDetails.getTransportcapacity());
    			current.setWeightunit(supplierDetails.getWeightunit());
    			current.setPriceunits(supplierDetails.getPriceunits());
    			current.setOrganization(supplierDetails.getOrganization());
    			current.setModeOfTransport(supplierDetails.getModeOfTransport());
    			current.setLengthunit(supplierDetails.getLengthunit());
    			current.setActivityStatus(supplierDetails.getActivityStatus());
    			current.setSupplierPhoneNumber(String.valueOf(supplierDetails.getSupplierPhoneNumber()));
        		supplierRepository.save(current);
        		try {
        		      List<Supplier> suppliers = new ArrayList<Supplier>();
        		      suppliers.add(current);
              	  sendSupplierNotifications("update",suppliers);
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
    public boolean deleteSupplierByIdAndOrganization(Long id, String organization) {
    	
    	Supplier current = supplierRepository.getSupplierBySupplierIdAndOrganization(id,organization);
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{    		
    		supplierRepository.delete(current);
    		try {
      		      List<Supplier> suppliers = new ArrayList<Supplier>();
      		      suppliers.add(current);
            	  sendSupplierNotifications("delete",suppliers);
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
    
    public List<SupplierDTO> findAllByTransportcapacityAndOrganization(String transportcapacity, String organization){
        return supplierRepository.findAllByTransportcapacityAndOrganization(transportcapacity, organization)
                .stream()
                .map(supplierMapper::mapSupplierToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<SupplierDTO> findAllBySuppliertypeAndOrganization(String supplierType, String organization){
        return supplierRepository.findAllBySuppliertypeAndOrganization(supplierType, organization)
                .stream()
                .map(supplierMapper::mapSupplierToDTO)
                .collect(Collectors.toList());
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<SupplierDTO> getAllSuppliersOnOrganization(String organization){
        return supplierRepository.findAllByOrganization(organization)
                .stream()
                .map(supplierMapper::mapSupplierToDTO)
                .collect(Collectors.toList());
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
        String headerValue = "attachment; filename=suppliers_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Supplier> supplierList = supplierRepository.findAll();

        ExportSuppliersToXLSX exporter = new ExportSuppliersToXLSX(supplierList);
        exporter.export(response);
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

        List<Supplier> supplierList = supplierRepository.findAllByOrganization(organization);

        ExportSuppliersToXLSX exporter = new ExportSuppliersToXLSX(supplierList);
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
        String headerValue = "attachment; filename=suppliers_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Supplier> supplierList = supplierRepository.findAll();

        ExportSuppliersToPDF exporter = new ExportSuppliersToPDF(supplierList);
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
        String headerValue = "attachment; filename=suppliers_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Supplier> supplierList = supplierRepository.findAllByOrganization(organization);

        ExportSuppliersToPDF exporter = new ExportSuppliersToPDF(supplierList);
        exporter.export(response);
    }
    
    
    public void uploadSupplierUsingExcel(MultipartFile file,String organization) throws Exception {
        try {
        	
        	//System.out.println("I am inside try of uoload Employee Service");
          List<Supplier> suppliers = new BulkUploadSuppliersToDatabase().excelToSuppliers(this,file.getInputStream(),organization,errorRepository);
          supplierRepository.saveAll(suppliers);
          
          try {
        	  sendSupplierNotifications("create",suppliers);
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
     * The method is to retrieve all suppliers from the database and display them.
     *
     * After downloading all the data about the supplier,
     * the data is mapped to dto which will display only those needed
     * @return list of all suppliers with specification of data in SupplierToDTO
     */
    
    public List<SupplierDTO> getAllSuppliers(Pageable pageable){
        return supplierRepository.findAll(pageable)
                .stream()
                .map(supplierMapper::mapSupplierToDTO)
                .collect(Collectors.toList());
    }

    /**
     * The method is to download a specific supplier from the database and display it.
     * After downloading all the data about the supplier,
     * the data is mapped to dto which will display only those needed
     *
     * @param id id of the supplier to be searched for
     * @throws ResponseStatusException if the id of the supplier you are looking for does not exist throws 404 status
     * @return detailed data about a specific supplier
     */
    
    public SupplierDTO getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier cannot be found, the specified id does not exist"));
        return supplierMapper.mapSupplierToDTO(supplier);
    }

    /**
     * The task of the method is to add a supplier to the database.
     * @param supplier requestbody of the supplier to be saved
     * @return saving the supplier to the database
     */
    public Supplier addNewSuppiler(Supplier supplier){
        return supplierRepository.save(supplier);
    }

    /**
     * Method deletes the selected supplier by id
     * @param id id of the supplier to be deleted
     * @throws ResponseStatusException if id of the supplier is incorrect throws 404 status with message
     */
    public void deleteSupplierById(Long id) {
        try {
            supplierRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The specified id does not exist");
        }
    }

    /**
     * Method enabling editing name and activicity status of the selected supplier.
     * @param supplier requestbody of the supplier to be edited
     * @return edited supplier
     */
    public SupplierDTO editSupplier(Supplier supplier){
        Supplier editedSupplier = supplierRepository.findById(supplier.getSupplierId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier does not exist"));
        editedSupplier.setSupplierName(supplier.getSupplierName());
        editedSupplier.setActivityStatus(supplier.getActivityStatus());
        return supplierMapper.mapSupplierToDTO(editedSupplier);
    }
    
    /**
     * The purpose of the method is to send notifications
     */
    public void sendSupplierNotifications(String type, List<Supplier> suppliers) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();
		List<Notification> allNotifications = new ArrayList<Notification>();
		suppliers.forEach(
				(supplier) -> {
					
					List<Employee> allAdmins = employeeRepository.findAllByUserRoleAndOrganization(USER_ROLE.ADMIN, supplier.getOrganization());
					
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
							    		   notification.setMessage("Organization has a new supplier "+supplier.getSupplierName()+" now.");
							    		   notification.setNotificationType("supplier");
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
							    		   notification.setMessage("Organization updated supplier "+supplier.getSupplierName()+" now.");
							    		   notification.setNotificationType("supplier");
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
							    		   notification.setMessage("Organization does not deal with supplier "+supplier.getSupplierName()+" now.");
							    		   notification.setNotificationType("supplier");
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
