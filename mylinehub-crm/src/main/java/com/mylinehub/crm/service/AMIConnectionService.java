package com.mylinehub.crm.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.TimeoutException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.ami.ConnectionStream;
import com.mylinehub.crm.entity.AmiConnection;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.dto.AmiConnectionDTO;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.enums.USER_ROLE;
import com.mylinehub.crm.exports.excel.BulkUploadAmiConnectionToDatabase;
import com.mylinehub.crm.exports.excel.ExportAMIConnectionToXLSX;
import com.mylinehub.crm.exports.pdf.ExportAmiConnectionToPDF;
import com.mylinehub.crm.mapper.AmiConnectionMapper;
import com.mylinehub.crm.repository.AmiConnectionRepository;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;
import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class AMIConnectionService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final AmiConnectionRepository amiConnectionRepository;
    private final AmiConnectionMapper amiConnectionMapper;
    private ApplicationContext context;
    private final ErrorRepository errorRepository;
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    
    public AmiConnection getAmiConnectionByAmiuserAndOrganization(String amiuser,String organization) {
    	return amiConnectionRepository.getAmiConnectionByAmiuserAndOrganization(amiuser, organization);
    }
    
    public AmiConnectionDTO getByAmiuserAndOrganization(String amiuser,String organization) {
    	return amiConnectionMapper.mapAmiConnectionToDTO(amiConnectionRepository.getAmiConnectionByAmiuserAndOrganization(amiuser, organization));
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws TimeoutException 
     * @throws AuthenticationFailedException 
     * @throws IOException 
     * @throws IllegalStateException 
     */
    public boolean connectAmiConnectionOnAmiUserAndOrganization(String amiuser,String organization) throws IllegalStateException, IOException, AuthenticationFailedException, TimeoutException {
    	AmiConnection current = amiConnectionRepository.getAmiConnectionByAmiuserAndOrganization(amiuser,organization);
    	
    	ConnectionStream toTry = new ConnectionStream();
    	
    	ManagerConnection connection = toTry.getConnection(current.getDomain().trim(),"",false);
    	
    	if(connection == null)
    	{
			connection = toTry.createConnection(organization,current.getDomain().trim(), current.getAmiuser().trim(), current.getPassword().trim(),context);
    	}

    	return true;
    }

    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public int enableAmiConnectionOnOrganization(String amiuser,String organization) throws Exception {
        int toReturn= amiConnectionRepository.enableAmiConnectionByOrganization(amiuser, organization);
		try {
			List<AmiConnection> amiConnections = new ArrayList<AmiConnection>();
			amiConnections.add(amiConnectionRepository.getAmiConnectionByAmiuserAndOrganization(amiuser, organization));
			sendAmiNotifications("enabled",amiConnections);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return toReturn;
        
    }

    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public int disableAmiConnectionOnOrganization(String amiuser,String organization) throws Exception {
    	 int toReturn=  amiConnectionRepository.disableAmiConnectionByOrganization(amiuser,organization);
		try {
			List<AmiConnection> amiConnections = new ArrayList<AmiConnection>();
			amiConnections.add(amiConnectionRepository.getAmiConnectionByAmiuserAndOrganization(amiuser, organization));
			sendAmiNotifications("disabled",amiConnections);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return toReturn;
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public boolean createAmiConnectionByOrganization(AmiConnectionDTO amiConnectionDetails) throws Exception {
    	
    	AmiConnection current = amiConnectionRepository.getAmiConnectionByAmiuserAndOrganization(amiConnectionDetails.getAmiuser(),amiConnectionDetails.getOrganization());
    	
    	if(current==null)
    	{
    		current = amiConnectionMapper.mapDTOToAmiConnection(amiConnectionDetails);
    		current = amiConnectionRepository.save(current);
    		try {
				List<AmiConnection> amiConnections = new ArrayList<AmiConnection>();
				amiConnections.add(current);
				sendAmiNotifications("create",amiConnections);
			} catch (Exception e) {
				// TODO Auto-generated catch block
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
     * @throws Exception 
     */
    public boolean deleteAmiConnectionByAmiUserAndOrganization(String amiuser, String organization) throws Exception {
    	
    	AmiConnection current = amiConnectionRepository.getAmiConnectionByAmiuserAndOrganization(amiuser,organization);
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{    		
    		amiConnectionRepository.delete(current);
    		try {
				List<AmiConnection> amiConnections = new ArrayList<AmiConnection>();
				amiConnections.add(current);
				sendAmiNotifications("update",amiConnections);
			} catch (Exception e) {
				// TODO Auto-generated catch block
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
    
    public List<AmiConnectionDTO> getAllAmiConnectionsOnPhoneContextAndOrganization(String phoneContext, String organization){
        return amiConnectionRepository.findAllByPhonecontextAndOrganization(phoneContext, organization)
                .stream()
                .map(amiConnectionMapper::mapAmiConnectionToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<AmiConnectionDTO> getAllAmiConnectionsOnIsEnabledAndOrganization(boolean isEnabled, String organization){
        return amiConnectionRepository.findAllByIsactiveAndOrganization(isEnabled,organization)
                .stream()
                .map(amiConnectionMapper::mapAmiConnectionToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<AmiConnectionDTO> getAllAmiConnectionsOnIsEnabled(boolean isEnabled){
        return amiConnectionRepository.findAllByIsactive(isEnabled)
                .stream()
                .map(amiConnectionMapper::mapAmiConnectionToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean updateAmiConnectionByOrganization(AmiConnectionDTO amiConnectionDetails) {
    	
    	AmiConnection current = amiConnectionRepository.getAmiConnectionByIdAndOrganization(amiConnectionDetails.getId(),amiConnectionDetails.getOrganization());
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{
    		
    		try
    		{

    			current.setAmiuser(amiConnectionDetails.getAmiuser());
    			current.setDomain(amiConnectionDetails.getDomain());
    			current.setIsactive(amiConnectionDetails.isactive);
    			current.setOrganization(amiConnectionDetails.getOrganization());
    			current.setPassword(amiConnectionDetails.getPassword());
    			current.setPhonecontext(amiConnectionDetails.getPhonecontext());
    			current.setPort(amiConnectionDetails.getPort());
    			
        		amiConnectionRepository.save(current);
        		
        		try {
					List<AmiConnection> amiConnections = new ArrayList<AmiConnection>();
					amiConnections.add(current);
					sendAmiNotifications("update",amiConnections);
				} catch (Exception e) {
					// TODO Auto-generated catch block
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
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<AmiConnectionDTO> getAllAmiConnectionsOnOrganization(String organization){
        return amiConnectionRepository.findAllByOrganization(organization)
                .stream()
                .map(amiConnectionMapper::mapAmiConnectionToDTO)
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
        String headerValue = "attachment; filename=AMIConnection_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<AmiConnection> AmiConnectionList = amiConnectionRepository.findAll();

        ExportAMIConnectionToXLSX exporter = new ExportAMIConnectionToXLSX(AmiConnectionList);
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

        List<AmiConnection> amiConnectionList = amiConnectionRepository.findAllByOrganization(organization);

        ExportAMIConnectionToXLSX exporter = new ExportAMIConnectionToXLSX(amiConnectionList);
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
        String headerValue = "attachment; filename=AMIConnection_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<AmiConnection> amiConnectionList = amiConnectionRepository.findAll();

        ExportAmiConnectionToPDF exporter = new ExportAmiConnectionToPDF(amiConnectionList);
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
        String headerValue = "attachment; filename=AMIConnection_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<AmiConnection> amiConnectionList = amiConnectionRepository.findAllByOrganization(organization);

        ExportAmiConnectionToPDF exporter = new ExportAmiConnectionToPDF(amiConnectionList);
        exporter.export(response);
    }
    
    
    public void uploadAmiConnectionsUsingExcel(MultipartFile file,String organization) throws Exception {
        try {
        	
        	//System.out.println("I am inside try of uoload Employee Service");
          List<AmiConnection> amiConnections = new BulkUploadAmiConnectionToDatabase().excelToAmiConnections(this,file.getInputStream(),organization,errorRepository);
          amiConnectionRepository.saveAll(amiConnections);
          try {
			sendAmiNotifications("create",amiConnections);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
          
        } catch (IOException e) {
        	//System.out.println("I am inside catch of Upload service");
          throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
      }
    
    
    /**
     * The purpose of the method is to send notifications
     */
    public void sendAmiNotifications(String type, List<AmiConnection> amiConnections) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();
		List<Notification> allNotifications = new ArrayList<Notification>();
		amiConnections.forEach(
				(amiConnection) -> {
					
					List<Employee> allAdmins = employeeRepository.findAllByUserRoleAndOrganization(USER_ROLE.ADMIN, amiConnection.getOrganization());
					
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
							    		   notification.setMessage("AMI connection for "+amiConnection.getDomain()+" created successfully.");
							    		   notification.setNotificationType("ami");
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
							    		   notification.setMessage("AMI connection for "+amiConnection.getDomain()+" updated successfully.");
							    		   notification.setNotificationType("ami");
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
							    		   notification.setMessage("AMI connection for "+amiConnection.getDomain()+" deleted successfully.");
							    		   notification.setNotificationType("ami");
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
							    		   
	                                       case "disabled": 
	    						    		   
	    						    		   notification = new Notification();
	    						    		   notification.setCreationDate(new Date());
	    						    		   notification.setAlertType("alert-danger");
	    						    		   notification.setForExtension(employee.getExtension());
	    						    		   notification.setMessage("AMI connection for "+amiConnection.getDomain()+" disabled successfully.");
	    						    		   notification.setNotificationType("ami");
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
	    						    		   
	                                           case "enabled": 
	    						    		   
	    						    		   notification = new Notification();
	    						    		   notification.setCreationDate(new Date());
	    						    		   notification.setAlertType("alert-success");
	    						    		   notification.setForExtension(employee.getExtension());
	    						    		   notification.setMessage("AMI connection for "+amiConnection.getDomain()+" enabled successfully.");
	    						    		   notification.setNotificationType("ami");
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
							    		   
								    	   default:
								    	   break;
							    	}
								});
						
						
					}

			    	notificationRepository.saveAll(allNotifications);	
			    	
   	            });
    }
    
}
