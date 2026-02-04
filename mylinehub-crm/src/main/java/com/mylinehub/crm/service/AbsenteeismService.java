package com.mylinehub.crm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.entity.Absenteeism;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.dto.AbsenteeismDTO;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.exports.pdf.ExportAbsenteeismToPDF;
import com.mylinehub.crm.exports.excel.ExportAbsenteeismToXLSX;
import com.mylinehub.crm.mapper.AbsenteeismMapper;
import com.mylinehub.crm.repository.AbsenteeismRepository;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
public class AbsenteeismService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final AbsenteeismRepository absenteeismRepository;
    private final EmployeeRepository employeeRepository;
    private final AbsenteeismMapper absenteeismMapper;
    private final NotificationRepository notificationRepository;
    

    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<AbsenteeismDTO> getAllAbsenteeismOnOrganization(String organization){
        return absenteeismRepository.findAllByOrganization(organization)
                .stream()
                .map(absenteeismMapper::mapAbsenteeismToDto)
                .collect(Collectors.toList());
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<AbsenteeismDTO> findAllByReasonForAbsenseAndOrganization(String reasonForAbsense, String organization){

    	return absenteeismRepository.findAllByReasonForAbsenseAndOrganization(reasonForAbsense, organization)
                .stream()
                .map(absenteeismMapper::mapAbsenteeismToDto)
                .collect(Collectors.toList());
    	
 
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<AbsenteeismDTO> findAllByEmployeeAndOrganization(Employee employee, String organization){
    	
        Employee current = employeeRepository.findByExtensionAndOrganization(employee.getExtension(), organization);
    	
    	if(current == null)
    	{
    		return null;
    	}
    	else
    	{
    		 return absenteeismRepository.findAllByEmployeeAndOrganization(employee,organization)
    	                .stream()
    	                .map(absenteeismMapper::mapAbsenteeismToDto)
    	                .collect(Collectors.toList());
    	}
    	
       
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<AbsenteeismDTO> findAllByDateFromGreaterThanEqualAndDateToLessThanEqualOrganization(Date dateFrom,Date dateTo, String organization){
        return absenteeismRepository.findAllByDateFromGreaterThanEqualAndDateToLessThanEqualAndOrganization(dateFrom,dateTo,organization)
                .stream()
                .map(absenteeismMapper::mapAbsenteeismToDto)
                .collect(Collectors.toList());
    }
    
    
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public boolean createAbsenteeismByOrganization(AbsenteeismDTO absenteeismDetails) throws Exception {
    	
    	
    	Employee current = employeeRepository.findById(absenteeismDetails.getEmployeeID()).get();
    	
     	if(current == null)
     	{
     		return false;
     	}
     	else
     	{
     		absenteeismDetails = absenteeismMapper.mapAbsenteeismToDto(absenteeismRepository.save(absenteeismMapper.mapDTOToAbsenteeism(absenteeismDetails)));
     		
     		try {
				List<AbsenteeismDTO> currentAbsenteeismDTO = new ArrayList<AbsenteeismDTO>();
				currentAbsenteeismDTO.add(absenteeismDetails);
				sendEmployeeAbsenteeismNotifications("create", currentAbsenteeismDTO);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
     	} 	
        return true;
    }
    
    public AbsenteeismDTO getByIdAndOrganization(Long absenteeismID,String organization) {
    	return absenteeismMapper.mapAbsenteeismToDto(absenteeismRepository.getAbsenteeismByIdAndOrganization(absenteeismID, organization));
    }
    
    public Absenteeism getAbsenteeismByIdAndOrganization(Long absenteeismID,String organization) {
    	return absenteeismRepository.getAbsenteeismByIdAndOrganization(absenteeismID, organization);
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean updateAbsenteeismByOrganization(AbsenteeismDTO absenteeismDetails) {
    	
    	Absenteeism current = absenteeismRepository.getAbsenteeismByIdAndOrganization(absenteeismDetails.getId(),absenteeismDetails.getOrganization());
    	Employee currentEmployee = employeeRepository.findById(absenteeismDetails.getEmployeeID()).get();
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{
    		
    		try
    		{
    			current.setEmployee(currentEmployee);
    			current.setOrganization(absenteeismDetails.getOrganization());
    			current.setReasonForAbsense(absenteeismDetails.getReasonForAbsense());
    			current.setDateFrom(absenteeismDetails.getDateFrom());
    			current.setDateTo(absenteeismDetails.getDateTo());
    			absenteeismDetails = absenteeismMapper.mapAbsenteeismToDto(absenteeismRepository.save(current));
        		try {
					List<AbsenteeismDTO> currentAbsenteeismDTO = new ArrayList<AbsenteeismDTO>();
					currentAbsenteeismDTO.add(absenteeismDetails);
					sendEmployeeAbsenteeismNotifications("update", currentAbsenteeismDTO);
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
    public boolean deleteAbsenteeismByOrganization(Long absenteeismID, String organization) throws Exception {
    	
    	Absenteeism current = absenteeismRepository.getAbsenteeismByIdAndOrganization(absenteeismID,organization);
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{    		
    		absenteeismRepository.delete(current);
    		try {
				List<AbsenteeismDTO> currentAbsenteeismDTO = new ArrayList<AbsenteeismDTO>();
				currentAbsenteeismDTO.add(absenteeismMapper.mapAbsenteeismToDto(current));
				sendEmployeeAbsenteeismNotifications("delete", currentAbsenteeismDTO);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
        return true;
    }
    
    
    /**
     * The method is to retrieve all absenteeisms from the database and display them.
     *
     * After downloading all the data about the absenteeism,
     * the data is mapped to dto which will display only those needed
     * @return list of all absenteeisms with specification of data in AbsenteeismsDTO
     */
    
    public List<AbsenteeismDTO> getAllAbsenteeisms(Pageable pageable){
        return absenteeismRepository.findAllBy(pageable)
                .stream()
                .map(absenteeismMapper::mapAbsenteeismToDto)
                .collect(Collectors.toList());
    }

    /**
     * The method is to download a specific absenteeism from the database and display it.
     * After downloading all the data about the absenteeism,
     * the data is mapped to dto which will display only those needed
     *
     * @param id id of the absenteeism to be searched for
     * @throws ResponseStatusException if the id of the absenteeism you are looking for does not exist
     * @return detailed data about a specific absenteeism
     */
    
    public AbsenteeismDTO getAbsenteeismById(Long id){
        Absenteeism absenteeism = absenteeismRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Absenteeism cannot be found, the specified id does not exist"));
        return absenteeismMapper.mapAbsenteeismToDto(absenteeism);
    }

    /**
     * The task of the method is to add a absenteeism to the database.
     * @param absenteeism requestbody of the absenteeism to be saved
     * @return saving the absenteeism to the database
     */
    public Absenteeism addNewAbsenteeism(Absenteeism absenteeism) {
        return absenteeismRepository.save(absenteeism);
    }

    /**
     * Method deletes the selected absenteeism by id
     * @param id id of the absenteeism to be deleted
     * @throws ResponseStatusException if id of the absenteeism is incorrect throws 404 status with message
     */
    public void deleteAbsenteeismById(Long id) {
        try{
            absenteeismRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Absenteeism cannot be found, the specified id does not exist");
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
        String headerValue = "attachment; filename=absenteeisms_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Absenteeism> absenteeismList = absenteeismRepository.findAll();

        ExportAbsenteeismToXLSX exporter = new ExportAbsenteeismToXLSX(absenteeismList);
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

        List<Absenteeism> absenteeismList = absenteeismRepository.findAllByOrganization(organization);


        ExportAbsenteeismToXLSX exporter = new ExportAbsenteeismToXLSX(absenteeismList);
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
        String headerValue = "attachment; filename=absenteeisms_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Absenteeism> absenteeismList = absenteeismRepository.findAll();

        ExportAbsenteeismToPDF exporter = new ExportAbsenteeismToPDF(absenteeismList);
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
        String headerValue = "attachment; filename=absenteeisms_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Absenteeism> absenteeismList = absenteeismRepository.findAllByOrganization(organization);

        ExportAbsenteeismToPDF exporter = new ExportAbsenteeismToPDF(absenteeismList);
        exporter.export(response);
    }
    
    /**
     * The purpose of the method is to send notifications
     */
    public void sendEmployeeAbsenteeismNotifications(String type, List<AbsenteeismDTO> absenteeismsDetails) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();
		List<Notification> allNotifications = new ArrayList<Notification>();
		absenteeismsDetails.forEach(
				(absenteeismDetails) -> {
					
					Employee manager;
					Employee self;
					
					self = employeeRepository.findById(absenteeismDetails.getEmployeeID()).get();
					manager = employeeRepository.findByExtension(self.getTransfer_phone_1());
					
					
					if(manager != null)
					{
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
				    		   notification.setAlertType("alert-warning");
				    		   notification.setForExtension(manager.getExtension());
				    		   notification.setMessage(self.getFirstName()+" "+self.getLastName()+" ("+self.getExtension()+") applied abseenteism.");
				    		   notification.setNotificationType("absenteeism");
				    		   notification.setOrganization(manager.getOrganization());
				    		   notification.setTitle("Leave!");
				    		   
				    		   msg = new BotInputDTO();
					    	   msg.setDomain(manager.getDomain());
					    	   msg.setExtension(manager.getExtension());
					    	   msg.setFormat("json");
					    	   try {
								msg.setMessage(mapper.writeValueAsString(notification));
								} catch (JsonProcessingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
					    	   msg.setMessagetype("notification");
					    	   msg.setOrganization(manager.getOrganization());  
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
				    		   notification.setAlertType("alert-warning");
				    		   notification.setForExtension(manager.getExtension());
				    		   notification.setMessage(self.getFirstName()+" "+self.getLastName()+" ("+self.getExtension()+") updated abseenteism.");
				    		   notification.setNotificationType("absenteeism");
				    		   notification.setOrganization(manager.getOrganization());
				    		   notification.setTitle("Changed!");
				    		   
				    		   msg = new BotInputDTO();
					    	   msg.setDomain(manager.getDomain());
					    	   msg.setExtension(manager.getExtension());
					    	   msg.setFormat("json");
					    	   try {
								msg.setMessage(mapper.writeValueAsString(notification));
								} catch (JsonProcessingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
					    	   msg.setMessagetype("notification");
					    	   msg.setOrganization(manager.getOrganization());
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
					    		   notification.setForExtension(manager.getExtension());
					    		   notification.setMessage(self.getFirstName()+" "+self.getLastName()+" ("+self.getExtension()+") deleted abseenteism.");
					    		   notification.setNotificationType("absenteeism");
					    		   notification.setOrganization(manager.getOrganization());
					    		   notification.setTitle("Done!");
					    		   
					    		   msg = new BotInputDTO();
						    	   msg.setDomain(manager.getDomain());
						    	   msg.setExtension(manager.getExtension());
						    	   msg.setFormat("json");
						    	   try {
									msg.setMessage(mapper.writeValueAsString(notification));
									} catch (JsonProcessingException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
						    	   msg.setMessagetype("notification");
						    	   msg.setOrganization(manager.getOrganization());
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
					}
			    	
			    	
			    	notificationRepository.saveAll(allNotifications);	
			    	
   	            });
    }
    
}
