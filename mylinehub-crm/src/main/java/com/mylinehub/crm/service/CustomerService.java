package com.mylinehub.crm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mylinehub.crm.data.StartedCampaignData;
import com.mylinehub.crm.data.dto.CustomerAndItsCampaignDTO;
import com.mylinehub.crm.entity.CustomerFranchiseInventory;
import com.mylinehub.crm.entity.CustomerPropertyInventory;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Errors;
import com.mylinehub.crm.entity.dto.CustomerDTO;
import com.mylinehub.crm.entity.dto.CustomerPageDTO;
import com.mylinehub.crm.exports.pdf.ExportCustomersToPDF;
import com.mylinehub.crm.exports.excel.BulkUploadCustomerToDatabase;
import com.mylinehub.crm.exports.excel.ExportCustomersToXLSX;
import com.mylinehub.crm.mapper.CustomerMapper;
import com.mylinehub.crm.repository.CustomerRepository;
import com.mylinehub.crm.repository.ErrorRepository;

import lombok.AllArgsConstructor;

import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class CustomerService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final ErrorRepository errorRepository;
    private final FileService fileService;
    private final ApplicationContext applicationContext;
    

    private interface StrSetter { void set(String v); }
    private interface IntSetter { void set(Integer v); }
    private interface LongSetter { void set(Long v); }
    
    /**
     * Update only the customer's email by ID.
     */
    @Transactional
    public int updateCustomerEmailById(Long id, String email) {
        return customerRepository.updateCustomerEmailById(id, email);
    }

    /**
     * Batch update: firstWhatsAppMessageIsSend flag for given customer IDs.
     */
    @Transactional
    public int updateCustomerFirstWhatsAppMessageFlagForBatch(
            boolean firstWhatsAppMessageIsSend, List<Long> ids) {
        return customerRepository.updateCustomerFirstWhatsAppMessageFlagForBatch(firstWhatsAppMessageIsSend, ids);
    }

    /**
     * Batch update: preferred language for given customer IDs.
     */
    @Transactional
    public int updateCustomerPreferredLanguageForBatch(
            String preferredLanguage, List<Long> ids) {
        return customerRepository.updateCustomerPreferredLanguageForBatch(preferredLanguage, ids);
    }

    /**
     * Batch update: second preferred language for given customer IDs.
     */
    @Transactional
    public int updateCustomerSecondPreferredLanguageForBatch(
            String secondPreferredLanguage, List<Long> ids) {
        return customerRepository.updateCustomerSecondPreferredLanguageForBatch(secondPreferredLanguage, ids);
    }
    
    @Transactional
    public void updateCustomerEmail(Customers currentCustomer, String newEmail) {
        currentCustomer.setEmail(newEmail);
        customerRepository.save(currentCustomer);
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean createCustomerByOrganization(CustomerDTO customerDetails) {
    	
    	Customers current = customerRepository.getCustomerByPhoneNumberAndOrganization(customerDetails.getPhoneNumber(),customerDetails.getOrganization());
    	
    	if(current==null)
    	{
    		current = customerMapper.mapDTOtoCustomers(customerDetails);
    		customerRepository.save(current);
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
    public Customers createCustomerByOrganizationWithoutDTO(Customers customer) {
    	
    	Customers current = customerRepository.getCustomerByPhoneNumberAndOrganization(customer.getPhoneNumber(),customer.getOrganization());
    	
    	if(current==null)
    	{
    		customer =customerRepository.save(customer);
    	}
    	else
    	{
    		return null;
    	}
    	
        return customer;
    }
    @Transactional
    public boolean updateCustomerByOrganization(CustomerDTO customerDetails, String oldPhone, boolean updatedByAi) {

        System.out.println("[UPSERT] updateCustomerByOrganization called");

        // -----------------------------
        // D0: basic request visibility
        // -----------------------------
        if (customerDetails == null) {
            System.out.println("[UPSERT][D0] customerDetails is NULL -> return false");
            return false;
        }

        System.out.println("[UPSERT][D0] dto.id=" + customerDetails.getId()
                + " dto.org=" + customerDetails.getOrganization()
                + " dto.phone=" + customerDetails.getPhoneNumber()
                + " oldPhone=" + oldPhone
                + " updatedByAi=" + updatedByAi);

        // Inventory presence in DTO
        if (customerDetails.getPropertyInventory() == null) {
            System.out.println("[UPSERT][D0] dto.propertyInventory = NULL");
        } else {
            CustomerPropertyInventory inv = customerDetails.getPropertyInventory();
            System.out.println("[UPSERT][D0] dto.propertyInventory != NULL"
                    + " inv.id=" + inv.getId()
                    + " inv.available=" + inv.getAvailable()
                    + " inv.listedDate=" + inv.getListedDate()
                    + " inv.callStatus=" + inv.getCallStatus()
                    + " inv.pid=" + inv.getPid()
                    + " inv.premiseName=" + inv.getPremiseName()
                    + " inv.propertyType=" + inv.getPropertyType());
        }

        String org = customerDetails.getOrganization();
        String newPhone = customerDetails.getPhoneNumber();

        System.out.println("[UPSERT] incoming org=" + org + " newPhone=" + newPhone + " oldPhone=" + oldPhone);

        if (org == null || org.trim().isEmpty()) {
            System.out.println("[UPSERT] organization is blank -> return false");
            return false;
        }
        if (newPhone == null || newPhone.trim().isEmpty()) {
            System.out.println("[UPSERT] new phone is blank -> return false");
            return false;
        }

        org = org.trim();
        newPhone = newPhone.trim();

        if (oldPhone == null || oldPhone.trim().isEmpty()) {
            oldPhone = newPhone;
            System.out.println("[UPSERT][D0] oldPhone was blank -> using newPhone=" + oldPhone);
        } else {
            oldPhone = oldPhone.trim();
        }

        // =====================================================
        // STEP 1: Try update in MEMORY first (if present)
        // - prevents later overwrite by scheduled "saveAllCustomerDataInMemoryToDatabase"
        // - does NOT persist DB immediately
        // =====================================================
        boolean updatedInMemory = false;

        try {
            Map<String, CustomerAndItsCampaignDTO> memOne =
                    StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(newPhone, null, "get-one");
            CustomerAndItsCampaignDTO memDTO = (memOne == null) ? null : memOne.get(newPhone);

            // If oldPhone differs, try that key as well (your memory map is keyed by phone)
            if (memDTO == null && !oldPhone.equals(newPhone)) {
                Map<String, CustomerAndItsCampaignDTO> memOld =
                        StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(oldPhone, null, "get-one");
                memDTO = (memOld == null) ? null : memOld.get(oldPhone);
            }

            if (memDTO != null && memDTO.getCustomers() != null && !memDTO.getCustomers().isEmpty()) {

                System.out.println("[UPSERT][MEMORY] found CustomerAndItsCampaignDTO in memory. customers.size="
                        + memDTO.getCustomers().size());

                // Update ONLY same-org Customers inside this DTO
                for (int i = 0; i < memDTO.getCustomers().size(); i++) {

                    Customers memCustomer = memDTO.getCustomers().get(i);
                    if (memCustomer == null) continue;

                    // CRITICAL GUARD: update only same-organization customer
                    if (memCustomer.getOrganization() == null ||
                            !org.equals(memCustomer.getOrganization())) {
                        continue;
                    }

                    System.out.println("[UPSERT][MEMORY] updating memCustomer id="
                            + memCustomer.getId()
                            + " org=" + memCustomer.getOrganization()
                            + " phone=" + memCustomer.getPhoneNumber());

                    // --------- copy customer fields (same rules as DB update) ----------
                    setIfNotBlank(memCustomer::setDomain, customerDetails.getDomain());
                    setIfNotBlank(memCustomer::setOrganization, org);
                    setIfNotBlank(memCustomer::setPhoneContext, customerDetails.getPhoneContext());
                    setIfNotBlank(memCustomer::setBusiness, customerDetails.getBusiness());
                    setIfNotBlank(memCustomer::setCity, customerDetails.getCity());
                    setIfNotBlank(memCustomer::setCountry, customerDetails.getCountry());
                    setIfNotBlank(memCustomer::setCronremindercalling, customerDetails.getCronremindercalling());
                    setIfNotBlank(memCustomer::setDescription, customerDetails.getDescription());
                    setIfNotBlank(memCustomer::setEmail, customerDetails.getEmail());
                    setIfNotBlank(memCustomer::setFirstname, customerDetails.getFirstname());
                    setIfNotBlank(memCustomer::setLastname, customerDetails.getLastname());
                    setIfNotBlank(memCustomer::setPesel, customerDetails.getPesel());
                    setIfNotBlank(memCustomer::setPhoneNumber, newPhone);
                    setIfNotBlank(memCustomer::setZipCode, customerDetails.getZipCode());

                    if (customerDetails.isCoverted()) memCustomer.setCoverted(customerDetails.isCoverted());
                    if (customerDetails.isIscalledonce()) memCustomer.setIscalledonce(customerDetails.isIscalledonce());
                    if (customerDetails.isRemindercalling()) memCustomer.setRemindercalling(customerDetails.isRemindercalling());

                    setIfNotBlank(memCustomer::setPreferredLanguage, customerDetails.getPreferredLanguage());
                    setIfNotBlank(memCustomer::setSecondPreferredLanguage, customerDetails.getSecondPreferredLanguage());

                    // --------- inventory upsert into memory customer ----------
                    if (customerDetails.getPropertyInventory() != null) {
                        System.out.println("[UPSERT][MEMORY] inventory present in DTO");

                        CustomerPropertyInventory incomingInv = customerDetails.getPropertyInventory();
                        CustomerPropertyInventory existingInv = memCustomer.getPropertyInventory();

                        if (existingInv == null) {
                            existingInv = new CustomerPropertyInventory();
                            existingInv.setCustomer(memCustomer);
                            memCustomer.setPropertyInventory(existingInv);
                        }

                        copyInventoryFieldsNullSafe(incomingInv, existingInv, updatedByAi);
                        existingInv.setCustomer(memCustomer);
                    } else {
                        System.out.println("[UPSERT][MEMORY] inventory ABSENT in DTO -> no inventory changes");
                    }
                    
                   // --------- franchise inventory upsert into memory customer ----------
                    if (customerDetails.getFranchiseInventory() != null) {
                        System.out.println("[UPSERT][MEMORY] franchiseInventory present in DTO");

                        CustomerFranchiseInventory incomingF = customerDetails.getFranchiseInventory();
                        CustomerFranchiseInventory existingF = memCustomer.getFranchiseInventory();

                        if (existingF == null) {
                            existingF = new CustomerFranchiseInventory();
                            existingF.setCustomer(memCustomer);
                            memCustomer.setFranchiseInventory(existingF);
                        }

                        copyFranchiseFieldsNullSafe(incomingF, existingF);
                        existingF.setCustomer(memCustomer);

                    } else {
                        System.out.println("[UPSERT][MEMORY] franchiseInventory ABSENT in DTO -> no franchise changes");
                    }


                    // write back updated customer to the list
                    memDTO.getCustomers().set(i, memCustomer);
                    updatedInMemory = true;
                }

                if (updatedInMemory) {
                    // Persist back to StartedCampaignData (memory)
                    String keyPhone = (memOne != null && memOne.get(newPhone) != null) ? newPhone : oldPhone;
                    memDTO.setAssignedDate(new Date());
                    memDTO.setTriggerCustomerToExtentionInNewLineConnected(false);

                    StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(keyPhone, memDTO, "update");
                    System.out.println("[UPSERT][MEMORY] updatedInMemory=true and saved to StartedCampaignData keyPhone=" + keyPhone);
                } else {
                    System.out.println("[UPSERT][MEMORY] DTO found but no same-org customer matched. No memory update applied.");
                }
            } else {
                System.out.println("[UPSERT][MEMORY] no CustomerAndItsCampaignDTO found in memory for phone=" + newPhone + " (or oldPhone)");
            }

        } catch (Exception memEx) {
            System.out.println("[UPSERT][MEMORY][EXCEPTION] " + memEx.getClass().getSimpleName() + " msg=" + memEx.getMessage());
            memEx.printStackTrace();
            // continue to DB update path (memory update failure should not block DB)
        }

        // =====================================================
        // STEP 2: DB upsert (real persistence)
        // If updated in memory but NOT in DB, values will persist only when
        // your periodic saver runs OR when the campaign page removes/saves.
        // =====================================================

        // -----------------------------------------
        // SINGLE DB HIT for old + new phone
        // -----------------------------------------
        List<String> phones =
                oldPhone.equals(newPhone)
                        ? java.util.Collections.singletonList(newPhone)
                        : java.util.Arrays.asList(oldPhone, newPhone);

        System.out.println("[UPSERT] DB lookup phones=" + phones + " org=" + org);

        List<Customers> hits =
                customerRepository.findAllCustomersByPhoneNumberInAndOrganization(phones, org);

        System.out.println("[UPSERT] DB returned rows=" + hits.size());

        Customers byOld = null;
        Customers byNew = null;

        for (Customers c : hits) {
            if (c == null || c.getPhoneNumber() == null) continue;
            if (c.getPhoneNumber().equals(oldPhone)) byOld = c;
            if (c.getPhoneNumber().equals(newPhone)) byNew = c;
        }

        System.out.println("[UPSERT] byOld=" + (byOld != null ? byOld.getId() : "null")
                + " byNew=" + (byNew != null ? byNew.getId() : "null"));

        // Conflict case
        if (byOld != null && byNew != null && !byOld.getId().equals(byNew.getId())) {
            System.out.println("[UPSERT][ERROR] phone conflict: old and new belong to different customers -> return false");
            return false;
        }

        Customers target = (byOld != null) ? byOld : byNew;

        try {
            // =====================================================
            // UPDATE PATH
            // =====================================================
            if (target != null) {

                System.out.println("[UPSERT] UPDATE existing customer id=" + target.getId());

                // -----------------------------
                // D1: show DB state BEFORE changes
                // -----------------------------
                CustomerPropertyInventory dbInvBefore = target.getPropertyInventory();
                if (dbInvBefore == null) {
                    System.out.println("[UPSERT][D1] DB target.propertyInventory = NULL (before)");
                } else {
                    System.out.println("[UPSERT][D1] DB inv(before) id=" + dbInvBefore.getId()
                            + " available=" + dbInvBefore.getAvailable()
                            + " listedDate=" + dbInvBefore.getListedDate()
                            + " updatedByAi=" + dbInvBefore.getUpdatedByAi()
                            + " callStatus=" + dbInvBefore.getCallStatus()
                            + " pid=" + dbInvBefore.getPid());
                }

                setIfNotBlank(target::setDomain, customerDetails.getDomain());
                setIfNotBlank(target::setOrganization, org);
                setIfNotBlank(target::setPhoneContext, customerDetails.getPhoneContext());
                setIfNotBlank(target::setBusiness, customerDetails.getBusiness());
                setIfNotBlank(target::setCity, customerDetails.getCity());
                setIfNotBlank(target::setCountry, customerDetails.getCountry());
                setIfNotBlank(target::setCronremindercalling, customerDetails.getCronremindercalling());
                setIfNotBlank(target::setDescription, customerDetails.getDescription());
                setIfNotBlank(target::setEmail, customerDetails.getEmail());
                setIfNotBlank(target::setFirstname, customerDetails.getFirstname());
                setIfNotBlank(target::setLastname, customerDetails.getLastname());
                setIfNotBlank(target::setPesel, customerDetails.getPesel());
                setIfNotBlank(target::setPhoneNumber, newPhone);
                setIfNotBlank(target::setZipCode, customerDetails.getZipCode());

                if (customerDetails.isCoverted()) target.setCoverted(customerDetails.isCoverted());
                if (customerDetails.isIscalledonce()) target.setIscalledonce(customerDetails.isIscalledonce());
                if (customerDetails.isRemindercalling()) target.setRemindercalling(customerDetails.isRemindercalling());

                setIfNotBlank(target::setPreferredLanguage, customerDetails.getPreferredLanguage());
                setIfNotBlank(target::setSecondPreferredLanguage, customerDetails.getSecondPreferredLanguage());

                // Inventory upsert
                if (customerDetails.getPropertyInventory() != null) {
                    System.out.println("[UPSERT] inventory present");

                    CustomerPropertyInventory incomingInv = customerDetails.getPropertyInventory();
                    CustomerPropertyInventory existingInv = target.getPropertyInventory();

                    // -----------------------------
                    // D2: show incoming inventory snapshot
                    // -----------------------------
                    System.out.println("[UPSERT][D2] incomingInv.id=" + incomingInv.getId()
                            + " available=" + incomingInv.getAvailable()
                            + " listedDate=" + incomingInv.getListedDate()
                            + " callStatus=" + incomingInv.getCallStatus()
                            + " pid=" + incomingInv.getPid()
                            + " premiseName=" + incomingInv.getPremiseName()
                            + " propertyType=" + incomingInv.getPropertyType());

                    if (existingInv == null) {
                        System.out.println("[UPSERT] creating new inventory");
                        existingInv = new CustomerPropertyInventory();
                        existingInv.setCustomer(target);
                        target.setPropertyInventory(existingInv);

                        System.out.println("[UPSERT][D2] createdInv.id=" + existingInv.getId()
                                + " (likely null until flush) customerId=" + target.getId());
                    } else {
                        System.out.println("[UPSERT][D2] existingInv.id=" + existingInv.getId()
                                + " available(beforeCopy)=" + existingInv.getAvailable()
                                + " listedDate(beforeCopy)=" + existingInv.getListedDate()
                                + " updatedByAi(beforeCopy)=" + existingInv.getUpdatedByAi());
                    }

                    // -----------------------------
                    // D3: copy + show AFTER copy
                    // -----------------------------
                    copyInventoryFieldsNullSafe(incomingInv, existingInv, updatedByAi);
                    existingInv.setCustomer(target);

                    System.out.println("[UPSERT][D3] existingInv.id=" + existingInv.getId()
                            + " available(afterCopy)=" + existingInv.getAvailable()
                            + " listedDate(afterCopy)=" + existingInv.getListedDate()
                            + " updatedByAi(afterCopy)=" + existingInv.getUpdatedByAi());
                } else {
                    System.out.println("[UPSERT] inventory ABSENT in DTO -> no inventory changes will be applied");
                }

             // Franchise Inventory upsert
                if (customerDetails.getFranchiseInventory() != null) {
                    System.out.println("[UPSERT] franchiseInventory present");

                    CustomerFranchiseInventory incomingF = customerDetails.getFranchiseInventory();
                    CustomerFranchiseInventory existingF = target.getFranchiseInventory();

                    if (existingF == null) {
                        System.out.println("[UPSERT] creating new franchiseInventory");
                        existingF = new CustomerFranchiseInventory();
                        existingF.setCustomer(target);
                        target.setFranchiseInventory(existingF);
                    }

                    copyFranchiseFieldsNullSafe(incomingF, existingF);
                    existingF.setCustomer(target);

                } else {
                    System.out.println("[UPSERT] franchiseInventory ABSENT in DTO -> no franchise changes will be applied");
                }

                // -----------------------------
                // D4: force DB write NOW
                // -----------------------------
                customerRepository.save(target);
                customerRepository.flush();

                System.out.println("[UPSERT][D4] FLUSH OK customerId=" + target.getId()
                        + " invNow=" + (target.getPropertyInventory() != null
                        ? ("id=" + target.getPropertyInventory().getId()
                        + " available=" + target.getPropertyInventory().getAvailable()
                        + " listedDate=" + target.getPropertyInventory().getListedDate()
                        + " updatedByAi=" + target.getPropertyInventory().getUpdatedByAi())
                        : "NULL"));

                System.out.println("[UPSERT] UPDATE successful id=" + target.getId()
                        + " (updatedInMemory=" + updatedInMemory + ")");
                return true;
            }

            // =====================================================
            // CREATE PATH
            // =====================================================
            System.out.println("[UPSERT] CREATE new customer");

            Customers created = customerMapper.mapDTOtoCustomers(customerDetails);
            created.setOrganization(org);
            created.setPhoneNumber(newPhone);

            // If mapper doesn't wire the back-reference, do it here to guarantee insert works
            if (created.getPropertyInventory() != null) {
                created.getPropertyInventory().setCustomer(created);
                System.out.println("[UPSERT][CREATE] inventory present in DTO, set customer backref");
            } else {
                System.out.println("[UPSERT][CREATE] inventory not present in DTO");
            }
            
            if (created.getFranchiseInventory() != null) {
                created.getFranchiseInventory().setCustomer(created);
            }


            customerRepository.save(created);
            customerRepository.flush();

            System.out.println("[UPSERT] CREATE successful id=" + created.getId()
                    + " inv=" + (created.getPropertyInventory() != null ? "present" : "null")
                    + " (updatedInMemory=" + updatedInMemory + ")");
            return true;

        } catch (Exception e) {
            System.out.println("[UPSERT][EXCEPTION] " + e.getClass().getSimpleName() + " msg=" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void copyFranchiseFieldsNullSafe(
            CustomerFranchiseInventory src,
            CustomerFranchiseInventory dst
    ) {
        if (src == null || dst == null) return;

        // interest
        if (src.getInterest() != null && !src.getInterest().trim().isEmpty()) {
            dst.setInterest(src.getInterest().trim());
        }

        // available
        if (src.getAvailable() != null) {
            dst.setAvailable(src.getAvailable());
        }
    }


    
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean deleteCustomerByIdAndOrganization(Employee employee,Long id, String organization) {
    	
    	Customers current = customerRepository.getCustomerByIdAndOrganization(id,organization);
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{    	
    		//delete customer image
    		String uploadCustomerOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadCustomerOriginalDirectory");
    		if (current.getImageData() != null && !current.getImageData().isBlank())
        	{
        		try {
        			String name = current.getImageData();
            		name = name.replace(uploadCustomerOriginalDirectory+"/", "");
            		fileService.deleteFile(employee.getOrganization(),uploadCustomerOriginalDirectory, name);
        		}
        		catch(Exception e)
        		{
        			//Donot do anything
//        			e.printStackTrace();
        		}
        		
        	}
        	
    		//delete customer in database
    		customerRepository.delete(current);
    			
    	}
    	
        return true;
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws JsonProcessingException 
     */
    public int updateCustomerProductInterests(Long id,String interestedProducts) throws JsonProcessingException {
//    	System.out.println("Adding product interest details");
//    	System.out.println("interestedProducts : " + interestedProducts );
//    	System.out.println("id : " + id );
        return customerRepository.updateCustomerProductInterests(id,interestedProducts);
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateCustomerDescription(String description, Long id) {
        return customerRepository.updateCustomerDescription(description,id);
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateWhatsAppAIAutoMessage(boolean autoWhatsAppAIReply,Long id) {
        return customerRepository.updateWhatsAppAIAutoMessage(autoWhatsAppAIReply,id);
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int customerGotConverted(Long id) {
        return customerRepository.customerGotConverted(id);
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int customerGotDiverted(Long id) {
        return customerRepository.customerGotDiverted(id);
    }

    
    public Customers getCustomerByPhoneNumberAndOrganization(String phoneNumber,String organization) {
    	return customerRepository.getCustomerByPhoneNumberAndOrganization(phoneNumber, organization);
    }
    
    
    public CustomerDTO getByPhoneNumberAndOrganization(String phoneNumber,String organization) {
    	return customerMapper.mapCustomersToDto(customerRepository.getCustomerByPhoneNumberAndOrganization(phoneNumber, organization));
    }
    
    
    /**
     * The method is to retrieve all employee from the database for a project id and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CustomerPageDTO findAllBywhatsAppProjectId(String whatsAppProjectId,String searchText,Pageable pageable){
    	
    	CustomerPageDTO toReturn = new CustomerPageDTO();
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<Customers> response = customerRepository.findAllBywhatsAppProjectId(whatsAppProjectId,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
    		Page<Customers> responseConverted = customerRepository.findAllByCovertedAndwhatsAppProjectId(true, whatsAppProjectId, searchText, pageable);
    		Page<Customers> responseDiverted = customerRepository.findAllByCovertedAndwhatsAppProjectId(false, whatsAppProjectId, searchText, pageable);
    		toReturn.setConverted(responseConverted.getTotalElements());
    		toReturn.setDiverted(responseDiverted.getTotalElements());
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<Customers> response = customerRepository.getAllBywhatsAppProjectId(whatsAppProjectId,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
  
    	}
        return toReturn;
    }
    
    /**
     * The method is to retrieve all employee from the database for a phone number within project id and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CustomerPageDTO findAllByWhatsAppRegisteredByPhoneNumber(String whatsAppRegisteredByPhoneNumber,String searchText,Pageable pageable){
    	
    	CustomerPageDTO toReturn = new CustomerPageDTO();
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<Customers> response = customerRepository.findAllByWhatsAppRegisteredByPhoneNumber(whatsAppRegisteredByPhoneNumber,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
    		Page<Customers> responseConverted = customerRepository.findAllByCovertedAndWhatsAppRegisteredByPhoneNumber(true, whatsAppRegisteredByPhoneNumber, searchText, pageable);
    		Page<Customers> responseDiverted = customerRepository.findAllByCovertedAndWhatsAppRegisteredByPhoneNumber(false, whatsAppRegisteredByPhoneNumber, searchText, pageable);
    		toReturn.setConverted(responseConverted.getTotalElements());
    		toReturn.setDiverted(responseDiverted.getTotalElements());
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<Customers> response = customerRepository.getAllByWhatsAppRegisteredByPhoneNumber(whatsAppRegisteredByPhoneNumber,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
  
    	}
        return toReturn;
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CustomerPageDTO findAllByOrganization(String organization,String searchText,Pageable pageable){
    	
    	CustomerPageDTO toReturn = new CustomerPageDTO();
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<Customers> response = customerRepository.findAllByOrganization(organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
    		Page<Customers> responseConverted = customerRepository.findAllByCovertedAndOrganization(true, organization, searchText, pageable);
    		Page<Customers> responseDiverted = customerRepository.findAllByCovertedAndOrganization(false, organization, searchText, pageable);
    		toReturn.setConverted(responseConverted.getTotalElements());
    		toReturn.setDiverted(responseDiverted.getTotalElements());
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<Customers> response = customerRepository.getAllByOrganization(organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
  
    	}
        return toReturn;
    }
    

    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CustomerPageDTO findAllByPeselAndOrganization(String pesel,String organization, String searchText, Pageable pageable){
    	
    	CustomerPageDTO toReturn = new CustomerPageDTO();
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<Customers> response = customerRepository.findAllByPeselAndOrganization(pesel,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
    		Page<Customers> responseConverted = customerRepository.findAllByPeselAndOrganization(true,pesel,organization,searchText,pageable);
    		Page<Customers> responseDiverted = customerRepository.findAllByPeselAndOrganization(false,pesel,organization,searchText,pageable);
    		toReturn.setConverted(responseConverted.getTotalElements());
    		toReturn.setDiverted(responseDiverted.getTotalElements());
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<Customers> response = customerRepository.getAllByPeselAndOrganization(pesel,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
  
    	}
        return toReturn;

    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CustomerPageDTO findAllByEmailAndOrganization(String email,String organization, String searchText, Pageable pageable){
    	
    	CustomerPageDTO toReturn = new CustomerPageDTO();
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<Customers> response = customerRepository.findAllByEmailAndOrganization(email,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
    		Page<Customers> responseConverted = customerRepository.findAllByEmailAndOrganization(true,email,organization,searchText,pageable);
    		Page<Customers> responseDiverted = customerRepository.findAllByEmailAndOrganization(false,email,organization,searchText,pageable);
    		
    		toReturn.setConverted(responseConverted.getTotalElements());
    		toReturn.setDiverted(responseDiverted.getTotalElements());
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<Customers> response = customerRepository.getAllByEmailAndOrganization(email,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
  
    	}
        return toReturn;
    }
    
    public CustomerDTO getCustomerByIdAndOrganization(Long id,String organization) {
    	return customerMapper.mapCustomersToDto(customerRepository.getCustomerByIdAndOrganization(id, organization));
    }
    
    public CustomerDTO getCustomerByWhatsAppPhoneNumberId(String whatsAppPhoneNumberId) {
    	return customerMapper.mapCustomersToDto(customerRepository.getCustomerByWhatsAppPhoneNumberId(whatsAppPhoneNumberId));
    }
   
    public CustomerDTO findByPhoneNumberContaining(String phoneNumber,String organization) {
    	return customerMapper.mapCustomersToDto(customerRepository.findByPhoneNumberContainingAndOrganization(phoneNumber,organization));
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CustomerPageDTO findAllByCountryAndOrganization(String country,String organization, String searchText, Pageable pageable) {
    	
    	CustomerPageDTO toReturn = new CustomerPageDTO();
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<Customers> response = customerRepository.findAllByCountryAndOrganization(country,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
    		Page<Customers> responseConverted = customerRepository.findAllByCountryAndOrganization(true,country,organization,searchText,pageable);
    		Page<Customers> responseDiverted = customerRepository.findAllByCountryAndOrganization(false,country,organization,searchText,pageable);
    		toReturn.setConverted(responseConverted.getTotalElements());
    		toReturn.setDiverted(responseDiverted.getTotalElements());
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<Customers> response = customerRepository.getAllByCountryAndOrganization(country,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
  
    	}
        return toReturn;

    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CustomerPageDTO findAllByBusinessAndOrganization(String business,String organization, String searchText, Pageable pageable){
    	
    	CustomerPageDTO toReturn = new CustomerPageDTO();
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<Customers> response = customerRepository.findAllByBusinessAndOrganization(business,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
    		Page<Customers> responseConverted = customerRepository.findAllByBusinessAndOrganization(true,business,organization,searchText,pageable);
    		Page<Customers> responseDiverted = customerRepository.findAllByBusinessAndOrganization(false,business,organization,searchText,pageable);
    		toReturn.setConverted(responseConverted.getTotalElements());
    		toReturn.setDiverted(responseDiverted.getTotalElements());
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<Customers> response = customerRepository.getAllByBusinessAndOrganization(business,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
  
    	}
        return toReturn;

    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CustomerPageDTO findAllByPhoneContextAndOrganization(String phoneContext,String organization, String searchText, Pageable pageable){
    	
    	CustomerPageDTO toReturn = new CustomerPageDTO();
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<Customers> response = customerRepository.findAllByPhoneContextAndOrganization(phoneContext,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
    		Page<Customers> responseConverted = customerRepository.findAllByPhoneContextAndOrganization(true,phoneContext,organization,searchText,pageable);
    		Page<Customers> responseDiverted = customerRepository.findAllByPhoneContextAndOrganization(false,phoneContext,organization,searchText,pageable);
    		toReturn.setConverted(responseConverted.getTotalElements());
    		toReturn.setDiverted(responseDiverted.getTotalElements());
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<Customers> response = customerRepository.getAllByPhoneContextAndOrganization(phoneContext,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
  
    	}
        return toReturn;

    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CustomerPageDTO findAllByCityAndOrganization(String city,String organization, String searchText, Pageable pageable){
    	
    	CustomerPageDTO toReturn = new CustomerPageDTO();
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<Customers> response = customerRepository.findAllByCityAndOrganization(city,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
    		Page<Customers> responseConverted = customerRepository.findAllByCityAndOrganization(true,city,organization,searchText,pageable);
    		Page<Customers> responseDiverted = customerRepository.findAllByCityAndOrganization(false,city,organization,searchText,pageable);
    		toReturn.setConverted(responseConverted.getTotalElements());
    		toReturn.setDiverted(responseDiverted.getTotalElements());
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<Customers> response = customerRepository.getAllByCityAndOrganization(city,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
  
    	}
        return toReturn;
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CustomerPageDTO findAllByZipCodeAndOrganization(String zipCode,String organization, String searchText, Pageable pageable) {
    	
    	CustomerPageDTO toReturn = new CustomerPageDTO();
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<Customers> response = customerRepository.findAllByZipCodeAndOrganization(zipCode,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
    		Page<Customers> responseConverted = customerRepository.findAllByZipCodeAndOrganization(true,zipCode,organization,searchText,pageable);
    		Page<Customers> responseDiverted = customerRepository.findAllByZipCodeAndOrganization(false,zipCode,organization,searchText,pageable);
    		toReturn.setConverted(responseConverted.getTotalElements());
    		toReturn.setDiverted(responseDiverted.getTotalElements());
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<Customers> response = customerRepository.getAllByZipCodeAndOrganization(zipCode,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
  
    	}
        return toReturn;
 
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CustomerPageDTO findAllByCovertedAndOrganization(boolean coverted,String organization, String searchText, Pageable pageable) {
    	
    	CustomerPageDTO toReturn = new CustomerPageDTO();
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<Customers> response = customerRepository.findAllByCovertedAndOrganization(coverted,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
    		if(coverted)
    		{
    			Page<Customers> responseConverted = customerRepository.findAllByCovertedAndOrganization(true, organization, searchText, pageable);
        		toReturn.setConverted(responseConverted.getTotalElements());
        		toReturn.setDiverted(0);
    		}
    		else
    		{
    			Page<Customers> responseDiverted = customerRepository.findAllByCovertedAndOrganization(false, organization, searchText, pageable);
    			toReturn.setConverted(0);
        		toReturn.setDiverted(responseDiverted.getTotalElements());
    		}
    		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<Customers> response = customerRepository.getAllByCovertedAndOrganization(coverted,organization,searchText,pageable);
    		
    		List<CustomerDTO> returnPart = response.getContent()
		    		.stream()
		            .map(customerMapper::mapCustomersToDto)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(returnPart);
  
    	}
        return toReturn;
    }
    
    
    
    
    
//    /**
//     * The method is to retrieve all customers from the database and display them.
//     *
//     * After downloading all the data about the customer,
//     * the data is mapped to dto which will display only those needed
//     * @return list of all customers with specification of data in CustomerDTO
//     */
//    
//    public List<CustomerDTO> getAllCustomers(Pageable pageable){
//        return customerRepository.findAll(pageable)
//                .stream()
//                .map(customerMapper::mapCustomersToDto)
//                .collect(Collectors.toList());
//    }

    
    /**
     * The method is to download a specific customer from the database and display it.
     * After downloading all the data about the customer,
     * the data is mapped to dto which will display only those needed
     *
     * @param id id of the customer to be searched for
     * @throws ResponseStatusException if the id of the customer you are looking for does not exist throws 404 status
     * @return detailed data about a specific customer
     */
    
    public CustomerDTO getCustomerById(Long id) {
        Customers customer = customerRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer cannot be found, the specified id does not exist"));
        return customerMapper.mapCustomersToDto(customer);

    }

//    /**
//     * The method is to retrieve customers whose have the firstname specified by the user.
//     * After downloading all the data about the customer,
//     * the data is mapped to dto which will display only those needed
//     * @param firstName firstname of the customer
//     * @return details of specific customers
//     */
//    
//    public List<CustomerDTO> findCustomersByFirstname(String firstName, Pageable pageable){
//        return customerRepository.findCustomersByFirstnameContainingIgnoreCase(firstName, pageable)
//                .stream()
//                .map(customerMapper::mapCustomersToDto)
//                .collect(Collectors.toList());
//    }
    

    /**
     * The task of the method is to add a customer to the database.
     * @param customer requestbody of the customer to be saved
     * @return saving the customer to the database
     */
    public Customers addNewCustomer(CustomerDTO customer) {
        return customerRepository.save(customerMapper.mapDTOtoCustomers(customer));
    }

    /**
     * Method deletes the selected customer by id
     * @param id id of the customer to be deleted
     * @throws ResponseStatusException if id of the customer is incorrect throws 404 status with message
     */
    public void deleteCustomerById(Long id) {
        try{
            customerRepository.deleteById(id);
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
        String headerValue = "attachment; filename=customers_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Customers> customersList = customerRepository.findAll();

        ExportCustomersToXLSX exporter = new ExportCustomersToXLSX(customersList);
        exporter.export(response);
    }

    /**
     * The purpose of the method is to set the details of the
     * excel file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToExcelOnOrganization(String organization,String searchText, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        Pageable pageable= PageRequest.of(0, 1000000000);
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=AMIConnection_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Customers> customersList = customerRepository.getAllByOrganization(organization,searchText,pageable).getContent();


        ExportCustomersToXLSX exporter = new ExportCustomersToXLSX(customersList);
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
        String headerValue = "attachment; filename=customers_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Customers> customersList = customerRepository.findAll();

        ExportCustomersToPDF exporter = new ExportCustomersToPDF(customersList);
        exporter.export(response);
    }
    
    /**
     * The purpose of the method is to set the details of the
     * pdf file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToPDFOnOrganization(String organization,String searchText,HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        Pageable pageable= PageRequest.of(0, 1000000000);
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=customers_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Customers> customersList = customerRepository.getAllByOrganization(organization,searchText,pageable).getContent();

        ExportCustomersToPDF exporter = new ExportCustomersToPDF(customersList);
        exporter.export(response);
    }

    @Transactional
    public CustomerDTO editCustomer(Customers customers) {
        Customers editedCustomer = customerRepository.findById(customers.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer does not exist"));
        editedCustomer.setFirstname(customers.getFirstname());
        editedCustomer.setLastname(customers.getLastname());
        editedCustomer.setCity(customers.getCity());
        editedCustomer.setZipCode(customers.getZipCode());
        editedCustomer.setPesel(customers.getPesel());
        return customerMapper.mapCustomersToDto(editedCustomer);
    }
    
    public void uploadCustomersUsingExcel(MultipartFile file, String organization) throws Exception {

        System.out.println("[UPLOAD] START org=" + organization);

        if (organization == null || organization.trim().isEmpty()) {
            System.out.println("[UPLOAD][ERROR] org is blank");
            throw new Exception("Organization is mandatory for upload. Please select organization and retry.");
        }
        if (file == null || file.isEmpty()) {
            System.out.println("[UPLOAD][ERROR] file is empty");
            throw new Exception("Uploaded file is empty.");
        }

        System.out.println("[UPLOAD] filename=" + file.getOriginalFilename());
        System.out.println("[UPLOAD] contentType=" + file.getContentType());
        System.out.println("[UPLOAD] size=" + file.getSize());

        List<Customers> parsed;
        try (InputStream in = file.getInputStream()) {
            System.out.println("[UPLOAD] parsing excel...");
            parsed = new BulkUploadCustomerToDatabase()
                    .excelToCustomers(this, in, organization, errorRepository, customerRepository);
            System.out.println("[UPLOAD] parsed count=" + (parsed == null ? 0 : parsed.size()));
        } catch (Exception ex) {
            System.out.println("[UPLOAD][EXCEPTION] parsing failed: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }

        if (parsed == null || parsed.isEmpty()) {
            System.out.println("[UPLOAD] no rows parsed. END.");
            return;
        }

        final int BATCH_SIZE = 2000;
        int from = 0;

        while (from < parsed.size()) {
            int to = Math.min(from + BATCH_SIZE, parsed.size());
            System.out.println("[UPLOAD] batch from=" + from + " to=" + to);

            List<Customers> batch = parsed.subList(from, to);

            List<String> phones = new ArrayList<>(batch.size());
            for (Customers c : batch) {
                if (c == null) continue;
                String before = c.getPhoneNumber();
                String p = normalizePhone(before);
                c.setPhoneNumber(p);
                System.out.println("[UPLOAD] phone before=" + before + " after=" + p);
                if (p != null && !p.isBlank()) phones.add(p);
            }

            if (phones.isEmpty()) {
                System.out.println("[UPLOAD] batch has no phones, skipping");
                from = to;
                continue;
            }

            List<String> uniquePhones = phones.stream().distinct().collect(Collectors.toList());
            System.out.println("[UPLOAD] uniquePhones count=" + uniquePhones.size());

            List<Customers> existingList =
                    customerRepository.findAllCustomersByPhoneNumberInAndOrganization(uniquePhones, organization);

            System.out.println("[UPLOAD] existing found=" + (existingList == null ? 0 : existingList.size()));

            Map<String, Customers> existingByPhone = new HashMap<>();
            for (Customers ex : existingList) {
                String ep = normalizePhone(ex.getPhoneNumber());
                if (ep != null) existingByPhone.put(ep, ex);
            }

            List<Customers> toSave = new ArrayList<>();
            List<Errors> errors = new ArrayList<>();
            Set<String> seenIncomingPhones = new HashSet<>();

            for (Customers incoming : batch) {
                if (incoming == null) continue;

                String phone = normalizePhone(incoming.getPhoneNumber());
                incoming.setPhoneNumber(phone);

                if (phone == null || phone.isBlank()) {
                    System.out.println("[UPLOAD][ROW-ERROR] phone missing");
                    errors.add(buildError(organization, "CustomerService", "Contact Number missing",
                            "phone=null incoming=" + safeToString(incoming)));
                    continue;
                }

                if (!seenIncomingPhones.add(phone)) {
                    System.out.println("[UPLOAD][ROW-ERROR] duplicate in file phone=" + phone);
                    errors.add(buildError(organization, "CustomerService", "Duplicate phone in file",
                            "phone=" + phone + " incoming=" + safeToString(incoming)));
                    continue;
                }

                Customers existing = existingByPhone.get(phone);

                if (existing == null) {
                    incoming.setOrganization(organization);
                    if (incoming.getPropertyInventory() != null) {
                        incoming.getPropertyInventory().setCustomer(incoming);
                    }
                    toSave.add(incoming);
                } else {
                    if (notEmpty(incoming.getFirstname())) existing.setFirstname(incoming.getFirstname());
                    if (notEmpty(incoming.getLastname())) existing.setLastname(incoming.getLastname());
                    if (notEmpty(incoming.getCity())) existing.setCity(incoming.getCity());
                    if (notEmpty(incoming.getDescription())) existing.setDescription(incoming.getDescription());

                    existing.setOrganization(organization);

                    CustomerPropertyInventory incInv = incoming.getPropertyInventory();
                    if (incInv != null) {
                        CustomerPropertyInventory exInv = existing.getPropertyInventory();
                        if (exInv == null) {
                            exInv = new CustomerPropertyInventory();
                            exInv.setCustomer(existing);
                            existing.setPropertyInventory(exInv);
                        }
                        copyInventoryFieldsNullSafe(incInv, exInv,false);
                        exInv.setCustomer(existing);
                    }

                    toSave.add(existing);
                }
            }

            System.out.println("[UPLOAD] errorsCount=" + errors.size() + " toSaveCount=" + toSave.size());

            if (!errors.isEmpty()) {
                try {
                    errorRepository.saveAll(errors);
                    System.out.println("[UPLOAD] errors saved=" + errors.size());
                } catch (Exception ex) {
                    System.out.println("[UPLOAD][EXCEPTION] saving errors failed: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            if (!toSave.isEmpty()) {
                safeSaveCustomers(toSave, organization);
            }

            from = to;
        }

        System.out.println("[UPLOAD] END org=" + organization);
    }
    
    private Errors buildError(String organization, String errorClass, String functionality, String data) {
        Errors err = new Errors();
        err.setCreatedDate(new Date(System.currentTimeMillis()));
        err.setError("Upload validation error");
        err.setErrorClass(errorClass);
        err.setFunctionality(functionality);
        err.setOrganization(organization);
        err.setData(data);
        return err;
    }

    /**
     * never fail whole upload if batch save fails
     * - try saveAll once
     * - if fails: save row-by-row and log failures
     */
    private void safeSaveCustomers(List<Customers> toSave, String organization) {
        System.out.println("[UPLOAD][DB] saveAll attempt count=" + toSave.size());
        try {
            customerRepository.saveAll(toSave);
            customerRepository.flush(); // IMPORTANT: forces DB constraint errors now
            System.out.println("[UPLOAD][DB] saveAll OK");
            return;
        } catch (Exception batchEx) {
            System.out.println("[UPLOAD][DB][EXCEPTION] saveAll FAILED: " + batchEx.getMessage());
            batchEx.printStackTrace();

            // Batch failed -> isolate bad rows
            for (Customers c : toSave) {
                try {
                    customerRepository.save(c);
                    customerRepository.flush();
                    System.out.println("[UPLOAD][DB] row OK phone=" + (c != null ? c.getPhoneNumber() : "null"));
                } catch (Exception rowEx) {
                    System.out.println("[UPLOAD][DB][ROW-FAIL] phone=" + (c != null ? c.getPhoneNumber() : "null")
                            + " msg=" + rowEx.getMessage());
                    rowEx.printStackTrace();

                    Errors err = buildError(
                            organization,
                            "CustomerService",
                            "DB save failed",
                            "phone=" + safe(c != null ? c.getPhoneNumber() : null)
                                    + " msg=" + (rowEx.getMessage() != null ? rowEx.getMessage() : rowEx.getClass().getSimpleName())
                    );
                    try {
                        errorRepository.save(err);
                    } catch (Exception e2) {
                        System.out.println("[UPLOAD][DB][ERROR] could not save error row: " + e2.getMessage());
                        e2.printStackTrace();
                    }
                }
            }
        }
    }


    private String normalizePhone(String phone) {
        if (phone == null) return null;

        String p = phone.trim();
        if (p.isEmpty()) return null;

        // Remove spaces, brackets, hyphen etc.
        p = p.replaceAll("\\s+", "");
        p = p.replaceAll("[()\\-]", "");

        // Excel numeric/scientific artifacts: 9.106354844E9 etc.
        int ePos = p.indexOf('E');
        if (ePos != -1) p = p.substring(0, ePos);

        // remove ".0" if excel exported as text/number
        if (p.endsWith(".0")) p = p.substring(0, p.length() - 2);

        // keep only digits and '+' (optional)
        // (if your input can contain other chars)
        p = p.replaceAll("[^0-9+]", "");

        // If starts with +, keep it, else treat as digits
        if (p.startsWith("+")) {
            // ok
        } else {
            // remove leading 00 (international prefix)
            if (p.startsWith("00")) p = p.substring(2);

            // If 10 digits => assume India
            if (p.matches("\\d{10}")) {
                p = "+91" + p;
            }
            // If 12 digits starting with 91 => make +91...
            else if (p.matches("91\\d{10}")) {
                p = "+" + p;
            }
            // If already countrycode without + but not India size => just prefix +
            else if (p.matches("\\d{11,15}")) {
                p = "+" + p;
            } else {
                // unknown / invalid
                return null;
            }
        }

        return p;
    }


    private String safe(String s) {
        return s == null ? "null" : s;
    }


    private boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String safeToString(Object o) {
        try {
            return String.valueOf(o);
        } catch (Exception e) {
            return "toString_failed";
        }
    }

    private void copyInventoryFieldsNullSafe(
            CustomerPropertyInventory src,
            CustomerPropertyInventory dst,
            boolean updatedByAi
    ) {
        if (src == null || dst == null) {
            System.out.println("[INV][SKIP] src or dst is NULL src=" + src + " dst=" + dst);
            return;
        }

        System.out.println("--------------------------------------------------");
        System.out.println("[INV][START] dst.id=" + dst.getId()
                + " customerId=" + (dst.getCustomer() != null ? dst.getCustomer().getId() : "null"));

        // ===== BEFORE SNAPSHOT (DB state) =====
        System.out.println("[INV][BEFORE] available=" + dst.getAvailable()
                + " listedDate=" + dst.getListedDate()
                + " updatedByAi=" + dst.getUpdatedByAi()
                + " rent=" + dst.isRent()
                + " rentValue=" + dst.getRentValue()
                + " bhk=" + dst.getBhk()
                + " sqFt=" + dst.getSqFt()
                + " callStatus=" + dst.getCallStatus());

        // ===== SOURCE SNAPSHOT (DTO state) =====
        System.out.println("[INV][SRC] available=" + src.getAvailable()
                + " listedDate=" + src.getListedDate()
                + " rent=" + getRentAsBoolean(src)
                + " rentValue=" + src.getRentValue()
                + " bhk=" + src.getBhk()
                + " sqFt=" + src.getSqFt()
                + " callStatus=" + src.getCallStatus());

        // ================= STRINGS =================
        setIfNotBlank(dst::setPremiseName, src.getPremiseName());
        setIfNotBlank(dst::setPropertyType, src.getPropertyType());
        setIfNotBlank(dst::setNearby, src.getNearby());
        setIfNotBlank(dst::setArea, src.getArea());
        setIfNotBlank(dst::setCity, src.getCity());
        setIfNotBlank(dst::setUnitType, src.getUnitType());
        setIfNotBlank(dst::setTenant, src.getTenant());
        setIfNotBlank(dst::setFacing, src.getFacing());
        setIfNotBlank(dst::setBrokerage, src.getBrokerage());
        setIfNotBlank(dst::setUnitNo, src.getUnitNo());
        setIfNotBlank(dst::setFloorNo, src.getFloorNo());
        setIfNotBlank(dst::setPid, src.getPid());
        setIfNotBlank(dst::setPropertyDescription1, src.getPropertyDescription1());
        setIfNotBlank(dst::setFurnishedType, src.getFurnishedType());

        // ================= INTEGERS =================
        setIfNotNullLong(dst::setRentValue, src.getRentValue());
        setIfNotNullInt(dst::setBhk, src.getBhk());
        setIfNotNullInt(dst::setSqFt, src.getSqFt());
        setIfNotNullInt(dst::setPropertyAge, src.getPropertyAge());
        setIfNotNullInt(dst::setTotalFloors, src.getTotalFloors());
        setIfNotNullInt(dst::setBalconies, src.getBalconies());
        setIfNotNullInt(dst::setWashroom, src.getWashroom());

        // ================= RENT =================
        Boolean rentVal = getRentAsBoolean(src);
        if (rentVal != null) {
            System.out.println("[INV][CHANGE] rent -> " + rentVal);
            setRentFromBoolean(dst, rentVal);
        } else {
            System.out.println("[INV][SKIP] rent is NULL in src");
        }

        // ================= CALL STATUS =================
        if (src.getCallStatus() != null) {
            System.out.println("[INV][CHANGE] callStatus -> " + src.getCallStatus());
            dst.setCallStatus(src.getCallStatus());
        }

        // ================= AVAILABLE =================
        if (src.getAvailable() != null) {
            System.out.println("[INV][CHANGE] available "
                    + dst.getAvailable() + " -> " + src.getAvailable());
            dst.setAvailable(src.getAvailable());
        } else {
            System.out.println("[INV][SKIP] available is NULL in src");
        }

        // ================= LISTED DATE =================
        if (src.getListedDate() == null) {
            Instant now = Instant.now();
            System.out.println("[INV][CHANGE] listedDate NULL -> now=" + now);
            dst.setListedDate(now);
        } else {
            System.out.println("[INV][CHANGE] listedDate -> " + src.getListedDate());
            dst.setListedDate(src.getListedDate());
        }

        // ================= UPDATED BY AI =================
        System.out.println("[INV][CHANGE] updatedByAi -> " + updatedByAi);
        dst.setUpdatedByAi(updatedByAi);

        // ===== AFTER SNAPSHOT =====
        System.out.println("[INV][AFTER] available=" + dst.getAvailable()
                + " listedDate=" + dst.getListedDate()
                + " updatedByAi=" + dst.getUpdatedByAi()
                + " rent=" + dst.isRent()
                + " rentValue=" + dst.getRentValue()
                + " bhk=" + dst.getBhk()
                + " sqFt=" + dst.getSqFt()
                + " callStatus=" + dst.getCallStatus());

        System.out.println("[INV][END] dst.id=" + dst.getId());
        System.out.println("--------------------------------------------------");
    }



    private void setIfNotNullLong(LongSetter setter, Long value) {
        if (value != null) setter.set(value);
    }


    private void setIfNotBlank(StrSetter setter, String value) {
        if (value == null) return;
        String v = value.trim();
        if (!v.isEmpty()) setter.set(v);
    }

    private void setIfNotNullInt(IntSetter setter, Integer value) {
        if (value != null) setter.set(value);
    }

    private boolean safeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * rent helper: supports BOTH cases:
     * 1) entity has Boolean getRent() / setRent(Boolean)
     * 2) entity has boolean isRent() / setRent(boolean)
     */
    private Boolean getRentAsBoolean(CustomerPropertyInventory inv) {
        try {
            // Try Boolean getter first
            java.lang.reflect.Method m = inv.getClass().getMethod("getRent");
            Object v = m.invoke(inv);
            return (v instanceof Boolean) ? (Boolean) v : null;
        } catch (Exception ignore) {
            // Try primitive boolean isRent()
            try {
                java.lang.reflect.Method m2 = inv.getClass().getMethod("isRent");
                Object v2 = m2.invoke(inv);
                if (v2 instanceof Boolean) return (Boolean) v2;
            } catch (Exception ignore2) { }
        }
        return null;
    }

    private void setRentFromBoolean(CustomerPropertyInventory inv, Boolean rent) {
        if (rent == null) return;
        try {
            // Try Boolean setter first
            java.lang.reflect.Method m = inv.getClass().getMethod("setRent", Boolean.class);
            m.invoke(inv, rent);
            return;
        } catch (Exception ignore) {
            // Try primitive boolean setter
            try {
                java.lang.reflect.Method m2 = inv.getClass().getMethod("setRent", boolean.class);
                m2.invoke(inv, rent.booleanValue());
            } catch (Exception ignore2) { }
        }
    }

    
    /**
     * Ensures safe default values are set for optional fields in a Customers entity.
     * Does not overwrite existing values.
     *
     * @param customer input Customers entity
     * @return same customer instance with defaults filled where applicable
     */
    public Customers applyDefaultValues(Customers customer) {
        if (customer == null) {
            return null;
        }

        // --- Language defaults ---
        if (isNullOrEmpty(customer.getPreferredLanguage())) {
            customer.setPreferredLanguage("English");
        }
        if (isNullOrEmpty(customer.getSecondPreferredLanguage())) {
            customer.setSecondPreferredLanguage("English");
        }
        
        if (customer.getImageSize() == null) {
            customer.setImageSize(0L);
        }
        
        customer.setUpdatedByAI(false);
        customer.setAutoWhatsAppAIReply(true);

        return customer;
    }

    /**
     * Utility to check if string is null or empty after trimming.
     */
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    public boolean uploadCustomerPicByEmailAndOrganization(Employee employee,MultipartFile image,Long id,String organization) throws Exception {
    	String uploadCustomerOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadCustomerOriginalDirectory");
    	Customers current = customerRepository.getCustomerByIdAndOrganization(id, organization);
    	if (current.getImageData() != null && !current.getImageData().isBlank())
    	{
    		try {
    			String name = current.getImageData();
        		name = name.replace(uploadCustomerOriginalDirectory+"/", "");
        		fileService.deleteFile(employee.getOrganization(),uploadCustomerOriginalDirectory, name);
    		}
    		catch(Exception e)
    		{
    			//Donot do anything
//    			e.printStackTrace();
    		}
    		
    	}
    	
    	
    	
    	try {
    		String imagesLocation = fileService.saveFileToStorage(employee.getOrganization(),uploadCustomerOriginalDirectory, image);
        	current.setImageData(uploadCustomerOriginalDirectory+"/"+imagesLocation);
        	current.setImageType(image.getContentType());
        	current.setImageName(image.getOriginalFilename());
        	current.setImageSize(image.getSize());
        	customerRepository.save(current);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    	return true;
    }
    
 // Put this NEW method inside CustomerService class
    public CustomerDTO getByPhoneNumberAndOrganizationPreferCache(String phoneNumber, String organization) {

        if (phoneNumber == null || organization == null) {
            return null;
        }

        String org = organization.trim();
        String inputPhone = phoneNumber.trim();

        // Build 2 candidates like your controller (+ / no +)
        // Keep order stable: first try as-is, then toggled.
        List<String> candidates = new ArrayList<>(2);
        if (!inputPhone.isEmpty()) {
            candidates.add(inputPhone);
            if (inputPhone.startsWith("+")) {
                candidates.add(inputPhone.substring(1));
            } else {
                candidates.add("+" + inputPhone);
            }
        }

        // -----------------------------
        // 1) Try memory cache first (StartedCampaignData)
        // -----------------------------
        for (String p : candidates) {
            if (p == null || p.trim().isEmpty()) continue;

            Map<String, CustomerAndItsCampaignDTO> one =
                    StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(p.trim(), null, "get-one");

            if (one == null || one.isEmpty()) continue;

            CustomerAndItsCampaignDTO bucket = one.get(p.trim());
            if (bucket == null) continue;

            Customers picked = pickCustomerFromBucket(bucket, org);
            if (picked != null) {
                return customerMapper.mapCustomersToDto(picked);
            }
        }

        // -----------------------------
        // 2) Fallback to DB (your existing behavior)
        // -----------------------------
        for (String p : candidates) {
            if (p == null || p.trim().isEmpty()) continue;

            Customers db = customerRepository.getCustomerByPhoneNumberAndOrganization(p.trim(), org);
            if (db != null) {
                return customerMapper.mapCustomersToDto(db);
            }
        }

        return null;
    }

    /**
     * Chooses the correct Customers from CustomerAndItsCampaignDTO:
     * - Prefer lastRunningCampaignID
     * - Then campaignIds order
     * - Always validate organization match
     *
     * NOTE:
     * CustomerAndItsCampaignDTO contains List<Long> campaignIds and List<Customers> customers.
     * If both lists are same size, we treat them as index-aligned (campaignIds[i] -> customers[i]).
     * Otherwise we fallback to "first customer whose organization matches".
     */
    private Customers pickCustomerFromBucket(CustomerAndItsCampaignDTO bucket, String organization) {
        if (bucket == null) return null;

        List<Long> campaignIds = bucket.getCampaignIds();
        List<Customers> customers = bucket.getCustomers();
        if (customers == null || customers.isEmpty()) return null;

        String org = (organization == null) ? null : organization.trim();
        boolean aligned = (campaignIds != null && customers != null && campaignIds.size() == customers.size());

        // 1) Prefer lastRunningCampaignID
        Long last = bucket.getLastRunningCampaignID();
        if (last != null) {
            Customers c = pickByCampaignId(last, campaignIds, customers, aligned, org);
            if (c != null) return c;
        }

        // 2) Else try by campaignIds order (use reverse so latest-added wins, if list is appended)
        if (aligned && campaignIds != null) {
            for (int i = campaignIds.size() - 1; i >= 0; i--) {
                Customers c = customers.get(i);
                if (c == null) continue;

                if (org == null || (c.getOrganization() != null && c.getOrganization().trim().equals(org))) {
                    return c;
                }
            }
        }

        // 3) Fallback: first customer with matching organization
        for (Customers c : customers) {
            if (c == null) continue;
            if (org == null) return c;

            if (c.getOrganization() != null && c.getOrganization().trim().equals(org)) {
                return c;
            }
        }

        return null;
    }

    private Customers pickByCampaignId(
            Long campaignId,
            List<Long> campaignIds,
            List<Customers> customers,
            boolean aligned,
            String org
    ) {
        if (campaignId == null || customers == null || customers.isEmpty()) return null;

        // If index-aligned lists exist, try exact index match for campaignId
        if (aligned && campaignIds != null) {
            int idx = campaignIds.indexOf(campaignId);
            if (idx >= 0 && idx < customers.size()) {
                Customers c = customers.get(idx);
                if (c != null) {
                    if (org == null) return c;
                    if (c.getOrganization() != null && c.getOrganization().trim().equals(org)) return c;
                }
            }
        }

        // Otherwise (or if org mismatch), just scan customers for org match
        for (Customers c : customers) {
            if (c == null) continue;
            if (org == null) return c;
            if (c.getOrganization() != null && c.getOrganization().trim().equals(org)) return c;
        }

        return null;
    }

    
}
