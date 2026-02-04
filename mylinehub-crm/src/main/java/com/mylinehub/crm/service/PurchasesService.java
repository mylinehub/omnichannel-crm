package com.mylinehub.crm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.Purchases;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.entity.dto.PurchasesDTO;
import com.mylinehub.crm.mapper.PurchasesMapper;
import com.mylinehub.crm.repository.CustomerRepository;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.repository.PurchasesRepository;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
public class PurchasesService {

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final PurchasesRepository purchasesRepository;
    private final CustomerRepository customerRepository;
    private final PurchasesMapper purchasesMapper;
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    

    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<PurchasesDTO> getAllPurchasesOnOrganization(String organization){
        return purchasesRepository.findAllByOrganization(organization)
                .stream()
                .map(purchasesMapper::mapPurchasesToDTO)
                .collect(Collectors.toList());
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<PurchasesDTO> findAllByCustomerAndOrganization(Long id, String organization){
    	
    	Customers current = customerRepository.getCustomerByIdAndOrganization(id, organization);
    	
    	if(current == null)
    	{
    		return null;
    	}
    	else
    	{
    	       return purchasesRepository.findAllByCustomerAndOrganization(current, organization)
    	                .stream()
    	                .map(purchasesMapper::mapPurchasesToDTO)
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
    
    public List<PurchasesDTO> findAllByPurchaseDateGreaterThanEqualAndOrganization(Date purchaseDate, String organization){
        return purchasesRepository.findAllByPurchaseDateGreaterThanEqualAndOrganization(purchaseDate,organization)
                .stream()
                .map(purchasesMapper::mapPurchasesToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<PurchasesDTO> findAllByPurchaseDateLessThanEqualAndOrganization(Date purchaseDate, String organization){
        return purchasesRepository.findAllByPurchaseDateLessThanEqualAndOrganization(purchaseDate,organization)
                .stream()
                .map(purchasesMapper::mapPurchasesToDTO)
                .collect(Collectors.toList());
    }
    
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public boolean createPurchaseByOrganization(PurchasesDTO purchaseDetails,Employee soldBy) throws Exception {
    	
    	Purchases current = purchasesRepository.getPurchaseByIdAndOrganization(purchaseDetails.getId(),purchaseDetails.getOrganization());
    	
    	if(current==null)
    	{
    		current = purchasesMapper.mapDTOToPurchases(purchaseDetails);
    		current.setSoldBy(soldBy);
    		current.setPurchaseDate(new Date());
    		current=purchasesRepository.save(current);
    		try {
				List<Purchases> purchases = new ArrayList<Purchases>();
				purchases.add(current);
				sendPurchasesNotifications("create", purchases);
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
    
    public PurchasesDTO getByIdAndOrganization(Long purchaseID,String organization) {
    	return purchasesMapper.mapPurchasesToDTO(purchasesRepository.getPurchaseByIdAndOrganization(purchaseID, organization));
    }
    
    public Purchases getPurchaseByIdAndOrganization(Long purchaseID,String organization) {
    	return purchasesRepository.getPurchaseByIdAndOrganization(purchaseID, organization);
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean updatePurchaseByOrganization(PurchasesDTO purchaseDetails, Employee employee) {
    	
    	Purchases current = purchasesRepository.getPurchaseByIdAndOrganization(purchaseDetails.getId(),purchaseDetails.getOrganization());
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{
    		
    		try
    		{
    			current.setOrganization(purchaseDetails.getOrganization());
    			current.setPurchaseDate(purchaseDetails.getPurchaseDate());
    			current.setPurchaseName(purchaseDetails.getPurchaseName());
    			current.setQuantity(purchaseDetails.getQuantity());
    			current.setReceiptExist(purchaseDetails.isReceiptExist());
    			current.setInvoiceExist(purchaseDetails.isInvoiceExist());
    			current = purchasesRepository.save(current);
        		try {
					List<Purchases> purchases = new ArrayList<Purchases>();
					purchases.add(current);
					sendPurchasesNotifications("update", purchases);
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
    public boolean deletePurchaseByOrganization(Long purchaseId, String organization) throws Exception {
    	
    	Purchases current = purchasesRepository.getPurchaseByIdAndOrganization(purchaseId,organization);
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{    		
    		purchasesRepository.delete(current);
    		try {
				List<Purchases> purchases = new ArrayList<Purchases>();
				purchases.add(current);
				sendPurchasesNotifications("delete", purchases);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
        return true;
    }
    
    
    /**
     * The method is to retrieve all purchases from the database and display them.
     *
     * After downloading all the data about the purchase,
     * the data is mapped to dto which will display only those needed
     * @return list of all purchases with specification of data in PurchasesToDTO
     */
    
    public List<PurchasesDTO> getAllPurchases(Pageable pageable){
        return purchasesRepository.findAll(pageable)
                .stream()
                .map(purchasesMapper::mapPurchasesToDTO)
                .collect(Collectors.toList());
    }
    
    

    /**
     * The method is to download a specific purchase from the database and display it.
     * After downloading all the data about the purchase,
     * the data is mapped to dto which will display only those needed
     *
     * @param id id of the purchase to be searched for
     * @throws ResponseStatusException if the id of the purchase you are looking for does not exist throws 404 status
     * @return detailed data about a specific purchase
     */
    
    public PurchasesDTO getPurchaseById(Long id) {
        Purchases purchases = purchasesRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase cannot be found, the specified id does not exist"));
        return purchasesMapper.mapPurchasesToDTO(purchases);
    }

    /**
     * The task of the method is to add a purchase to the database.
     * @param purchase requestbody of the purchase to be saved
     * @return saving the purchase to the database
     * @throws Exception 
     */
    public Purchases addNewPurchase(Purchases purchase) throws Exception {
    	Purchases toReturn = purchasesRepository.save(purchase);
    	
    	try {
			List<Purchases> purchases = new ArrayList<Purchases>();
			purchases.add(toReturn);
			sendPurchasesNotifications("create", purchases);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return toReturn;
    }

    /**
     * Method deletes the selected purchase by id
     * @param id id of the purchase to be deleted
     * @throws Exception 
     * @throws ResponseStatusException if id of the purchase is incorrect throws 404 status with message
     */
    public void deletePurchaseById(Long id) throws Exception {
        try{
            purchasesRepository.deleteById(id);
            try {
				List<Purchases> purchases = new ArrayList<Purchases>();
				purchases.add(purchasesRepository.getOne(id));
				sendPurchasesNotifications("delete", purchases);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The specified id does not exist");
        }
    }
    
    
    /**
     * The purpose of the method is to send notifications
     */
    public void sendPurchasesNotifications(String type, List<Purchases> purchases) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();
		List<Notification> allNotifications = new ArrayList<Notification>();
		purchases.forEach(
				(purchase) -> {
					
					Employee manager;
					Employee self;
					
					self = purchase.getSoldBy();
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
				    		   notification.setMessage(self.getFirstName()+" "+self.getLastName()+" ("+self.getExtension()+") sold a product having purchase id : "+purchase.getId());
				    		   notification.setNotificationType("purchases");
				    		   notification.setOrganization(manager.getOrganization());
				    		   notification.setTitle("Sold!");
				    		   
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
				    		   notification.setMessage(self.getFirstName()+" "+self.getLastName()+" ("+self.getExtension()+") updated purchase id : "+purchase.getId());
				    		   notification.setNotificationType("update");
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
					    		   notification.setMessage(self.getFirstName()+" "+self.getLastName()+" ("+self.getExtension()+") deleted purchase id : "+purchase.getId());
					    		   notification.setNotificationType("update");
					    		   notification.setOrganization(manager.getOrganization());
					    		   notification.setTitle("Alert!");
					    		   
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
			    	
   	            });
		
    	notificationRepository.saveAll(allNotifications);	
    }
}
