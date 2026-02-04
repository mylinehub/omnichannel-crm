package com.mylinehub.crm.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Ivr;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.entity.dto.IvrDTO;
import com.mylinehub.crm.enums.USER_ROLE;
import com.mylinehub.crm.exports.excel.BulkUploadIvrToDatabase;
import com.mylinehub.crm.exports.excel.ExportIvrToXLSX;
import com.mylinehub.crm.exports.pdf.ExportIvrToPDF;
import com.mylinehub.crm.mapper.IvrMapper;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.repository.IvrRepository;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class IvrService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final IvrRepository ivrRepository;
    private final IvrMapper ivrMapper;
    private final ErrorRepository errorRepository;
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public int enableIvrOnOrganization(String extension,String organization) throws Exception {
    	
    	int toReturn= ivrRepository.enableIvrByOrganization(extension,organization);
    	
    	try {
			List<Ivr> ivrs = new ArrayList<Ivr>();
			ivrs.add(ivrRepository.getIvrByExtension(extension));
			sendIvrNotifications("enabled", ivrs);
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
    public int disableIvrOnOrganization(String extension,String organization) throws Exception {
    	int toReturn=  ivrRepository.disableIvrByOrganization(extension,organization);
    	try {
			List<Ivr> ivrs = new ArrayList<Ivr>();
			ivrs.add(ivrRepository.getIvrByExtension(extension));
			sendIvrNotifications("disabled", ivrs);
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
    public boolean createIvrByOrganization(IvrDTO ivrDetails) throws Exception {
    	
    	Ivr current = ivrRepository.getIvrByExtensionAndOrganization(ivrDetails.getExtension(),ivrDetails.getOrganization());
    	
    	if(current==null)
    	{
    		current = ivrMapper.mapDTOToIvr(ivrDetails);
    		current = ivrRepository.save(current);
    		try {
				List<Ivr> ivrs = new ArrayList<Ivr>();
				ivrs.add(current);
				sendIvrNotifications("create", ivrs);
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
    
    public IvrDTO getByExtension(String extension) {
    	return ivrMapper.mapIvrToDTO(ivrRepository.getIvrByExtension(extension));
    }
    
    public IvrDTO getByExtensionAndOrganization(String extension,String organization) {
    	return ivrMapper.mapIvrToDTO(ivrRepository.getIvrByExtensionAndOrganization(extension, organization));
    }
    
    public Ivr getIvrByExtensionAndOrganization(String extension,String organization) {
    	return ivrRepository.getIvrByExtensionAndOrganization(extension, organization);
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean updateIvrByOrganization(IvrDTO ivrDetails) {
    	
    	Ivr current = ivrRepository.getIvrByExtensionAndOrganization(ivrDetails.getExtension(),ivrDetails.getOrganization());
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{
    		
    		try
    		{

    			current.setDomain(ivrDetails.getDomain());
    			current.setExtension(ivrDetails.getExtension());
    			current.setOrganization(ivrDetails.getOrganization());
    			current.setPhoneContext(ivrDetails.getPhoneContext());
    			current.setIsactive(ivrDetails.isactive);
    			current=ivrRepository.save(current);
        		try {
					List<Ivr> ivrs = new ArrayList<Ivr>();
					ivrs.add(current);
					sendIvrNotifications("update", ivrs);
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
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public boolean deleteIvrByExtensionAndOrganization(String extension, String organization) throws Exception {
    	
    	Ivr current = ivrRepository.getIvrByExtensionAndOrganization(extension,organization);
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{    		
    		ivrRepository.delete(current);
    		try {
				List<Ivr> ivrs = new ArrayList<Ivr>();
				ivrs.add(current);
				sendIvrNotifications("delete", ivrs);
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
    
    public List<IvrDTO> getAllIvrsOnPhoneContextAndOrganization(String phoneContext, String organization){
        return ivrRepository.findAllByPhoneContextAndOrganization(phoneContext, organization)
                .stream()
                .map(ivrMapper::mapIvrToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<IvrDTO> getAllIvrsOnIsEnabledAndOrganization(boolean isEnabled, String organization){
        return ivrRepository.findAllByIsactiveAndOrganization(isEnabled,organization)
                .stream()
                .map(ivrMapper::mapIvrToDTO)
                .collect(Collectors.toList());
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<IvrDTO> getAllIvrsOnOrganization(String organization){
        return ivrRepository.findAllByOrganization(organization)
                .stream()
                .map(ivrMapper::mapIvrToDTO)
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
        String headerValue = "attachment; filename=ivr_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Ivr> ivrList = ivrRepository.findAll();

        ExportIvrToXLSX exporter = new ExportIvrToXLSX(ivrList);
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
        String headerValue = "attachment; filename=ivr_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Ivr> ivrList = ivrRepository.findAllByOrganization(organization);

        ExportIvrToXLSX exporter = new ExportIvrToXLSX(ivrList);
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
        String headerValue = "attachment; filename=ivr_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Ivr> ivrList = ivrRepository.findAll();

        ExportIvrToPDF exporter = new ExportIvrToPDF(ivrList);
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
        String headerValue = "attachment; filename=ivr_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Ivr> ivrList = ivrRepository.findAllByOrganization(organization);

        ExportIvrToPDF exporter = new ExportIvrToPDF(ivrList);
        exporter.export(response);
    }
    
    public void uploadIvrUsingExcel(MultipartFile file,String organization) throws Exception {
        try {
        	
        	//System.out.println("I am inside try of uoload Employee Service");
          List<Ivr> ivrs = new BulkUploadIvrToDatabase().excelToIvrs(this,file.getInputStream(),organization,errorRepository);
          ivrRepository.saveAll(ivrs);
          try {
			sendIvrNotifications("create",ivrs);
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
    public void sendIvrNotifications(String type, List<Ivr> ivrs) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();
		List<Notification> allNotifications = new ArrayList<Notification>();
		ivrs.forEach(
				(ivr) -> {
					
					List<Employee> allAdmins = employeeRepository.findAllByUserRoleAndOrganization(USER_ROLE.ADMIN, ivr.getOrganization());
					
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
							    		   notification.setMessage("IVR extension "+ivr.getExtension()+" created successfully.");
							    		   notification.setNotificationType("ivr");
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
							    		   notification.setMessage("IVR extension "+ivr.getExtension()+" update successfully.");
							    		   notification.setNotificationType("ivr");
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
							    		   notification.setMessage("IVR extension "+ivr.getExtension()+" deleted successfully.");
							    		   notification.setNotificationType("ivr");
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
	    						    		   notification.setMessage("Ivr extension "+ivr.getExtension()+" disabled successfully.");
	    						    		   notification.setNotificationType("ivr");
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
	    						    		   notification.setMessage("Ivr extension "+ivr.getExtension()+" enabled successfully.");
	    						    		   notification.setNotificationType("ivr");
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
   	
   	            });
    	notificationRepository.saveAll(allNotifications);
    }
    
}