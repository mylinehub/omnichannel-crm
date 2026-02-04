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
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.Queue;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.entity.dto.QueueDTO;
import com.mylinehub.crm.enums.USER_ROLE;
import com.mylinehub.crm.exports.excel.BulkUploadQueueToDatabase;
import com.mylinehub.crm.exports.excel.ExportQueueToXLSX;
import com.mylinehub.crm.exports.pdf.ExportQueueToPDF;
import com.mylinehub.crm.mapper.QueueMapper;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.repository.QueueRepository;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class QueueService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final QueueRepository queueRepository;
    private final QueueMapper queueMapper;
    private final ErrorRepository errorRepository;
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public int enableQueueOnOrganization(String extension,String organization) throws Exception {
    	int toReturn=  queueRepository.enableQueueByOrganization(extension,organization);
    	try {
			List<Queue> queues = new ArrayList<Queue>();
			queues.add(queueRepository.getQueueByExtension(extension));
			sendQueueNotifications("enabled", queues);
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
    public int disableQueueOnOrganization(String extension,String organization) throws Exception {
    	int toReturn=  queueRepository.disableQueueByOrganization(extension,organization);
    	try {
			List<Queue> queues = new ArrayList<Queue>();
			queues.add(queueRepository.getQueueByExtension(extension));
			sendQueueNotifications("disabled", queues);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return toReturn;
    }
    
    public QueueDTO getByExtension(String extension) {
    	return queueMapper.mapQueueToDTO(queueRepository.getQueueByExtension(extension));
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public boolean createQueueByOrganization(QueueDTO queueDetails) throws Exception {
    	
    	Queue current = queueRepository.getQueueByExtensionAndOrganization(queueDetails.getExtension(),queueDetails.getOrganization());
    	
    	if(current==null)
    	{
    		current = queueMapper.mapDTOToQueue(queueDetails);
    		current = queueRepository.save(current);
    		try {
				List<Queue> queues = new ArrayList<Queue>();
				queues.add(current);
				sendQueueNotifications("create", queues);
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
    
    public QueueDTO getByExtensionAndOrganization(String extension,String organization) {
    	return queueMapper.mapQueueToDTO(queueRepository.getQueueByExtensionAndOrganization(extension, organization));
    }
    
    public Queue getQueueByExtensionAndOrganization(String extension,String organization) {
    	return queueRepository.getQueueByExtensionAndOrganization(extension, organization);
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean updateQueueByOrganization(QueueDTO queueDetails) {
    	
    	Queue current = queueRepository.getQueueByExtensionAndOrganization(queueDetails.getExtension(),queueDetails.getOrganization());
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{
    		
    		try
    		{

    			current.setDomain(queueDetails.getDomain());
    			current.setExtension(queueDetails.getExtension());
    			current.setOrganization(queueDetails.getOrganization());
    			current.setPhoneContext(queueDetails.getPhoneContext());
    			current.setName(queueDetails.getName());
    			current.setType(queueDetails.getType());
    			current.setIsactive(queueDetails.isactive);
    			current = queueRepository.save(current);
        		try {
					List<Queue> queues = new ArrayList<Queue>();
					queues.add(current);
					sendQueueNotifications("update", queues);
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
    public boolean deleteQueueByExtensionAndOrganization(String extension, String organization) throws Exception {
    	
    	Queue current = queueRepository.getQueueByExtensionAndOrganization(extension,organization);
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{    		
    		queueRepository.delete(current);
    		
    		try {
				List<Queue> queues = new ArrayList<Queue>();
				queues.add(current);
				sendQueueNotifications("delete", queues);
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
    
    public List<QueueDTO> getAllQueuesOnOrganization(String organization){
        return queueRepository.findAllByOrganization(organization)
                .stream()
                .map(queueMapper::mapQueueToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<QueueDTO> getAllQueuesOnPhoneContextAndOrganization(String phoneContext, String organization){
        return queueRepository.findAllByPhoneContextAndOrganization(phoneContext, organization)
                .stream()
                .map(queueMapper::mapQueueToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<QueueDTO> getAllQueuesOnIsEnabledAndOrganization(boolean isEnabled, String organization){
        return queueRepository.findAllByIsactiveAndOrganization(isEnabled,organization)
                .stream()
                .map(queueMapper::mapQueueToDTO)
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
        String headerValue = "attachment; filename=Queue_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Queue> queueList = queueRepository.findAll();

        ExportQueueToXLSX exporter = new ExportQueueToXLSX(queueList);
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
        String headerValue = "attachment; filename=Queue_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Queue> QueueList = queueRepository.findAllByOrganization(organization);

        ExportQueueToXLSX exporter = new ExportQueueToXLSX(QueueList);
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
        String headerValue = "attachment; filename=Queue_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Queue> queueList = queueRepository.findAll();

        ExportQueueToPDF exporter = new ExportQueueToPDF(queueList);
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
        String headerValue = "attachment; filename=Queue_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Queue> queueList = queueRepository.findAllByOrganization(organization);

        ExportQueueToPDF exporter = new ExportQueueToPDF(queueList);
        exporter.export(response);
    }
    
    public void uploadQueueUsingExcel(MultipartFile file,String organization) throws Exception {
        try {
        	
        	//System.out.println("I am inside try of uoload Employee Service");
          List<Queue> queues = new BulkUploadQueueToDatabase().excelToQueues(this,file.getInputStream(),organization,errorRepository);
          queueRepository.saveAll(queues);
          
          try {
			sendQueueNotifications("create",queues);
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
    public void sendQueueNotifications(String type, List<Queue> queues) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();
		List<Notification> allNotifications = new ArrayList<Notification>();
		queues.forEach(
				(queue) -> {
					
					List<Employee> allAdmins = employeeRepository.findAllByUserRoleAndOrganization(USER_ROLE.ADMIN, queue.getOrganization());
					
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
							    		   notification.setMessage("Queue extension "+queue.getExtension()+" created successfully.");
							    		   notification.setNotificationType("queue");
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
							    		   notification.setMessage("Queue extension "+queue.getExtension()+" update successfully.");
							    		   notification.setNotificationType("queue");
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
							    		   notification.setMessage("Queue extension "+queue.getExtension()+" deleted successfully.");
							    		   notification.setNotificationType("queue");
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
	    						    		   notification.setMessage("Queue extension "+queue.getExtension()+" disabled successfully.");
	    						    		   notification.setNotificationType("queue");
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
	    						    		   notification.setMessage("Queue extension "+queue.getExtension()+" enabled successfully.");
	    						    		   notification.setNotificationType("queue");
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