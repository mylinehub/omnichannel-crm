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
import com.mylinehub.crm.entity.Conference;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.entity.dto.ConferenceDTO;
import com.mylinehub.crm.enums.USER_ROLE;
import com.mylinehub.crm.exports.excel.BulkUploadConferenceToDatabase;
import com.mylinehub.crm.exports.excel.ExportConferenceToXLSX;
import com.mylinehub.crm.exports.pdf.ExportConferenceToPDF;
import com.mylinehub.crm.mapper.ConferenceMapper;
import com.mylinehub.crm.repository.ConferenceRepository;
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
public class ConferenceService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final ConferenceRepository conferenceRepository;
    private final ConferenceMapper conferenceMapper;
    private final ErrorRepository errorRepository;
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    
    public Conference getConferenceByConfextensionAndOrganization(String confextension,String organization) {
    	return conferenceRepository.getConferenceByConfextensionAndOrganization(confextension, organization);
    }
    
    public ConferenceDTO getByExtensionAndOrganization(String extension,String organization) {
    	return conferenceMapper.mapConferenceToDTO(conferenceRepository.getConferenceByConfextensionAndOrganization(extension, organization));
    }
 
    public ConferenceDTO getByExtension(String confextension) {
    	return conferenceMapper.mapConferenceToDTO(conferenceRepository.getConferenceByConfextension(confextension));
    }

    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int enableConferenceOnOrganization(String extension,String organization) {
    	int toReturn=  conferenceRepository.enableConferenceByOrganization(extension,organization);
    	try {
			Conference conference = conferenceRepository.getConferenceByConfextension(extension);
			sendConferenceNotifications("enabled", conference);
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
     */
    public int disableConferenceOnOrganization(String extension,String organization) {
    	
    	
    	int toReturn=  conferenceRepository.disableConferenceByOrganization(extension,organization);
    	try {
			Conference conference = conferenceRepository.getConferenceByConfextension(extension);
			sendConferenceNotifications("disabled", conference);
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
     */
    public boolean createConferenceByOrganization(ConferenceDTO conferenceDetails) {
    	
    	Conference current = conferenceRepository.getConferenceByConfextensionAndOrganization(conferenceDetails.getConfextension(),conferenceDetails.getOrganization());
    	
    	if(current==null)
    	{
    		current = conferenceMapper.mapDTOToConference(conferenceDetails);
    		conferenceRepository.save(current);
    		try {
				sendConferenceNotifications("create", current);
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
     */
    public boolean updateConferenceByOrganization(ConferenceDTO conferenceDetails) {
    	
    	Conference current = conferenceRepository.getConferenceByConfextensionAndOrganization(conferenceDetails.getConfextension(),conferenceDetails.getOrganization());
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{
    		
    		try
    		{

    			current.setDomain(conferenceDetails.getDomain());
    			current.setConfextension(conferenceDetails.getConfextension());
    			current.setOrganization(conferenceDetails.getOrganization());
    			current.setPhonecontext(conferenceDetails.getPhonecontext());
    			current.setIsconferenceactive(conferenceDetails.isIsconferenceactive());
    			current.setBridge(conferenceDetails.getBridge());
    			current.setConfname(conferenceDetails.getConfextension());
    			current.setIsdynamic(conferenceDetails.isIsdynamic());
    			current.setMenu(conferenceDetails.getMenu());
    			current.setOwner(conferenceDetails.getOwner());
    			current.setUserprofile(conferenceDetails.getUserprofile());
    			current.setProtocol(conferenceDetails.getProtocol());

        		conferenceRepository.save(current);
        		
        		try {
    				sendConferenceNotifications("update", current);
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
     */
    public boolean deleteConferenceByExtensionAndOrganization(String extension, String organization) {
    	
    	Conference current = conferenceRepository.getConferenceByConfextensionAndOrganization(extension,organization);
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{    		
    		conferenceRepository.delete(current);
    		
    		try {
				sendConferenceNotifications("delete", current);
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
    
    public List<ConferenceDTO> getAllConferenceOnPhoneContextAndOrganization(String phoneContext, String organization){
        return conferenceRepository.findAllByPhonecontextAndOrganization(phoneContext, organization)
                .stream()
                .map(conferenceMapper::mapConferenceToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<ConferenceDTO> getAllConferenceOnIsEnabledAndOrganization(boolean isEnabled, String organization){
        return conferenceRepository.findAllByIsconferenceactiveAndOrganization(isEnabled,organization)
                .stream()
                .map(conferenceMapper::mapConferenceToDTO)
                .collect(Collectors.toList());
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<ConferenceDTO> getAllConferenceOnOrganization(String organization){
        return conferenceRepository.findAllByOrganization(organization)
                .stream()
                .map(conferenceMapper::mapConferenceToDTO)
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
        String headerValue = "attachment; filename=Conference_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Conference> ConferenceList = conferenceRepository.findAll();

        ExportConferenceToXLSX exporter = new ExportConferenceToXLSX(ConferenceList);
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
        String headerValue = "attachment; filename=Conference_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Conference> ConferenceList = conferenceRepository.findAllByOrganization(organization);

        ExportConferenceToXLSX exporter = new ExportConferenceToXLSX(ConferenceList);
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
        String headerValue = "attachment; filename=Conference_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Conference> ConferenceList = conferenceRepository.findAll();

        ExportConferenceToPDF exporter = new ExportConferenceToPDF(ConferenceList);
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
        String headerValue = "attachment; filename=Conference_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Conference> ConferenceList = conferenceRepository.findAllByOrganization(organization);

        ExportConferenceToPDF exporter = new ExportConferenceToPDF(ConferenceList);
        exporter.export(response);
    }
    
    public void uploadConferencesUsingExcel(MultipartFile file,String organization) throws Exception {
        try {
        	
        	//System.out.println("I am inside try of uoload Employee Service");
          List<Conference> conferences = new BulkUploadConferenceToDatabase().excelToConferences(this,file.getInputStream(),organization,errorRepository);
          conferenceRepository.saveAll(conferences);
        } catch (IOException e) {
        	//System.out.println("I am inside catch of Upload service");
          throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
      }
    
    
    /**
     * The purpose of the method is to send notifications
     */
    public void sendConferenceNotifications(String type, Conference conference) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();
		List<Notification> allNotifications = new ArrayList<Notification>();
		List<Employee> allAdmins = employeeRepository.findAllByUserRoleAndOrganization(USER_ROLE.ADMIN, conference.getOrganization());
		
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
				    		   notification.setMessage("Conference extension "+conference.getConfextension()+" created successfully.");
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
				    		   notification.setMessage("Conference extension "+conference.getConfextension()+" update successfully.");
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
				    		   notification.setMessage("Conference extension "+conference.getConfextension()+" deleted successfully.");
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
					    		   notification.setMessage("Conference extension "+conference.getConfextension()+" disabled successfully.");
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
					    		   notification.setMessage("Conference extension "+conference.getConfextension()+" enabled successfully.");
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
    	notificationRepository.saveAll(allNotifications);	
    }
    
}

