package com.mylinehub.crm.whatsapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.dto.EmbeddedSignupResultDto;
import com.mylinehub.crm.whatsapp.dto.WhatsAppManagementEmployeeDto;
import com.mylinehub.crm.whatsapp.dto.WhatsAppPhoneNumberDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplateVariable;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;
import com.mylinehub.crm.whatsapp.entity.WhatsAppProject;
import com.mylinehub.crm.whatsapp.mapper.WhatsAppPhoneNumberMapper;
import com.mylinehub.crm.whatsapp.repository.WhatsAppPhoneNumberRepository;
import com.mylinehub.crm.whatsapp.repository.WhatsAppProjectRepository;

import lombok.AllArgsConstructor;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.web.client.RestTemplate;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class WhatsAppPhoneNumberService {

	private final WhatsAppPhoneNumberRepository whatsAppPhoneNumberRepository;
	private final WhatsAppProjectRepository whatsAppProjectRepository;
	private final EmployeeRepository employeeRepository;
	private final WhatsAppPhoneNumberMapper whatsAppPhoneNumberMapper;
	private final CreateFileCategoryForOrgService createFileCategoryForOrgService;
	private final WhatsAppPhoneNumberTemplatesService whatsAppPhoneNumberTemplatesService; 
	private final WhatsAppNumberTemplateVariableService whatsAppNumberTemplateVariableService; 
	private final ApplicationContext applicationContext;
	
	@lombok.Data
	static class MetaTokenResponse {
	    private String access_token;
	    private String token_type;
	    private Long expires_in;
	}

	
	public WhatsAppPhoneNumberDto setDefault(WhatsAppPhoneNumberDto input) {
	    //System.out.println("WhatsAppPhoneNumberDto setDefault called");

	    if(input == null) {
	        //System.out.println("Input is null, returning null");
	        return null;
	    }

	    //System.out.println("input.getCostPerInboundMessage() : " + input.getCostPerInboundMessage());
	    //System.out.println("input.getCostPerOutboundMessage() : " + input.getCostPerOutboundMessage());
	    //System.out.println("input.getCostPerInboundAIMessageToken() : " + input.getCostPerInboundAIMessageToken());
	    //System.out.println("input.getCostPerOutboundAIMessageToken() : " + input.getCostPerOutboundAIMessageToken());

	    if(input.getCostPerInboundMessage() == null) {
	        //System.out.println("Setting default CostPerInboundMessage to 0");
	        input.setCostPerInboundMessage(0L);
	    }

	    if(input.getCostPerOutboundMessage() == null) {
	        //System.out.println("Setting default CostPerOutboundMessage to 1");
	        input.setCostPerOutboundMessage(1L);
	    }

	    if(input.getCostPerInboundAIMessageToken() == null) {
	        //System.out.println("Setting default CostPerInboundAIMessageToken to 1");
	        input.setCostPerInboundAIMessageToken(1L);
	    }

	    if(input.getCostPerOutboundAIMessageToken() == null) {
	        //System.out.println("Setting default CostPerOutboundAIMessageToken to 1");
	        input.setCostPerOutboundAIMessageToken(1L);
	    }

	    if(!input.isActive()) {
	        //System.out.println("Setting active to true");
	        input.setActive(true);
	    }

	    //System.out.println("Setting AutoAiMessageAllowed to true");
	    input.setAutoAiMessageAllowed(true);

	    //System.out.println("Setting AutoAiMessageLimit to -1");
	    input.setAutoAiMessageLimit(-1);

	    //System.out.println("Setting Country to INDIA");
	    input.setCountry("INDIA");

	    //System.out.println("Setting Currency to INR");
	    input.setCurrency("INR");

	    return input;
	}

	public Boolean resetPhoneMemoryDataAsPerPhoneNumber(String phoneNumber) throws Exception {
	    //System.out.println("resetPhoneMemoryDataAsPerPhoneNumber called for phoneNumber: " + phoneNumber);

	    Boolean toReturn = true;

	    WhatsAppPhoneNumber whatsAppPhoneNumber = findByPhoneNumber(phoneNumber);
	    List<WhatsAppPhoneNumberTemplates> templateListData = null;

	    if(whatsAppPhoneNumber != null) {
	        templateListData = whatsAppPhoneNumberTemplatesService.getAllByWhatsAppPhoneNumber(whatsAppPhoneNumber);
	        //System.out.println("Found " + (templateListData != null ? templateListData.size() : 0) + " templates");
	    } else {
	        //System.out.println("No WhatsAppPhoneNumber found for phoneNumber: " + phoneNumber);
	    }

	    try {
	        //System.out.println("Setting up WhatsApp Phone Number Memory Data");
	        SetupWhatsAppMemoryData.setupWhatsAppPhoneNumberData(applicationContext);

	        if(whatsAppPhoneNumber != null) {
	            //System.out.println("Deleting all memory data for phoneNumber: " + phoneNumber);
	            WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(phoneNumber, null, "delete-all");

	            //System.out.println("Updating memory data for phoneNumber: " + whatsAppPhoneNumber.getPhoneNumber());
	            WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(whatsAppPhoneNumber.getPhoneNumber(), whatsAppPhoneNumber, "update");
	        }
	    } catch(Exception e) {
	        //System.out.println("Error while setting up WhatsApp Phone Number data");
	        toReturn = false;
	        throw e;
	    }

	    try {
	        if(whatsAppPhoneNumber != null) {
	            //System.out.println("Deleting all phone number templates memory data for phoneNumber: " + phoneNumber);
	            WhatsAppMemoryData.workWithWhatsAppPhoneNumbersTemplates(phoneNumber, null, "delete-all");

	            if(templateListData != null && !templateListData.isEmpty()) {
	                for(WhatsAppPhoneNumberTemplates element : templateListData) {
	                    //System.out.println("Updating template memory data for template of phoneNumber: " + element.getWhatsAppPhoneNumber().getPhoneNumber());
	                    WhatsAppMemoryData.workWithWhatsAppPhoneNumbersTemplates(element.getWhatsAppPhoneNumber().getPhoneNumber(), element, "update");

	                    try {
	                        //System.out.println("Deleting all template variables for phoneNumber: " + phoneNumber);
	                        WhatsAppMemoryData.workWithWhatsAppPhoneNumberTemplateVariable(phoneNumber, null, null, "delete-all");

	                        List<WhatsAppPhoneNumberTemplateVariable> listData = whatsAppNumberTemplateVariableService.findAllByWhatsAppNumberTemplate(element);

	                        if(listData != null && !listData.isEmpty()) {
	                            for(WhatsAppPhoneNumberTemplateVariable variable : listData) {
	                                //System.out.println("Updating template variable memory data for phoneNumber: " + variable.getWhatsAppPhoneNumberTemplates().getWhatsAppPhoneNumber().getPhoneNumber());
	                                WhatsAppMemoryData.workWithWhatsAppPhoneNumberTemplateVariable(variable.getWhatsAppPhoneNumberTemplates().getWhatsAppPhoneNumber().getPhoneNumber(), variable, null, "update");
	                            }
	                        }
	                    } catch(Exception e) {
	                        //System.out.println("Error while setting up WhatsApp Phone Number Template Variables data");
	                        throw e;
	                    }
	                }
	            }
	        }
	    } catch(Exception e) {
	        //System.out.println("Error while setting up WhatsApp Phone Number Template data");
	        toReturn = false;
	        throw e;
	    }

	    return toReturn;
	}

	public List<WhatsAppPhoneNumber> getAll() {
	    //System.out.println("Fetching all WhatsAppPhoneNumbers");
	    List<WhatsAppPhoneNumber> list = whatsAppPhoneNumberRepository.findAll();
	    //System.out.println("Found " + (list != null ? list.size() : 0) + " WhatsAppPhoneNumbers");
	    return list;
	}

	public List<WhatsAppPhoneNumber> findAllByPhoneNumbersAndOrganization(List<String> phoneNumbers, String organization) {
	    //System.out.println("Fetching WhatsAppPhoneNumbers by phoneNumbers and organization");
	    if(phoneNumbers == null || phoneNumbers.isEmpty()) {
	        //System.out.println("phoneNumbers list is null or empty, returning empty list");
	        return List.of();
	    }
	    if(organization == null || organization.isEmpty()) {
	        //System.out.println("organization is null or empty, returning empty list");
	        return List.of();
	    }
	    List<WhatsAppPhoneNumber> result = whatsAppPhoneNumberRepository.findAllByPhoneNumbersAndOrganization(phoneNumbers, organization);
	    //System.out.println("Found " + (result != null ? result.size() : 0) + " WhatsAppPhoneNumbers");
	    return result;
	}

	public WhatsAppPhoneNumber create(WhatsAppPhoneNumberDto whatsAppPhoneNumberDto) {
	    //System.out.println("Creating WhatsAppPhoneNumber");
	    WhatsAppPhoneNumber toReturn = null;
	    try {
	        if(whatsAppPhoneNumberDto == null) {
	            //System.out.println("Input WhatsAppPhoneNumberDto is null, returning null");
	            return null;
	        }

	        // Set default values
	        whatsAppPhoneNumberDto = setDefault(whatsAppPhoneNumberDto);

	        WhatsAppProject whatsAppProject = whatsAppProjectRepository.getOne(whatsAppPhoneNumberDto.getWhatsAppProjectId());
	        
	        Employee employee = employeeRepository.getOne(whatsAppPhoneNumberDto.getAdminEmployeeId());
	        
	        toReturn = whatsAppPhoneNumberRepository.save(whatsAppPhoneNumberMapper.mapDTOToWhatsAppPhoneNumber(whatsAppPhoneNumberDto,whatsAppProject,employee));

	        //System.out.println("Saved WhatsAppPhoneNumber with ID: " + (toReturn != null ? toReturn.getId() : "null"));

	        createFileCategoryForOrgService.modifyBaseCategoryForWhatsAppPhoneData(whatsAppPhoneNumberDto.getOrganization(), whatsAppPhoneNumberDto.getPhoneNumber(), "create", "");

	        //System.out.println("Created file category for WhatsAppPhoneNumber");

	        WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(whatsAppPhoneNumberDto.getPhoneNumber(), toReturn, "update");

	        //System.out.println("Updated memory data for phoneNumber: " + whatsAppPhoneNumberDto.getPhoneNumber());
	    } catch(Exception e) {
	        toReturn = null;
	        //System.out.println("Exception while creating WhatsAppPhoneNumber:");
	        e.printStackTrace();
	    }
	    return toReturn;
	}

	public boolean update(WhatsAppPhoneNumberDto whatsAppPhoneNumberDto, String oldPhone) {
	    //System.out.println("Updating WhatsAppPhoneNumber with ID: " + (whatsAppPhoneNumberDto != null ? whatsAppPhoneNumberDto.getId() : "null"));
	    boolean toReturn = true;
	    try {
	        if(whatsAppPhoneNumberDto == null) {
	            //System.out.println("Input WhatsAppPhoneNumberDto is null");
	            return false;
	        }

	        Optional<WhatsAppPhoneNumber> whatsAppPhoneNumber = whatsAppPhoneNumberRepository.findById(whatsAppPhoneNumberDto.getId());
	        if(whatsAppPhoneNumber.isPresent()) {
	        	
	        	WhatsAppProject whatsAppProject = whatsAppProjectRepository.getOne(whatsAppPhoneNumberDto.getWhatsAppProjectId());
		        
		        Employee employee = employeeRepository.getOne(whatsAppPhoneNumberDto.getAdminEmployeeId());
		        
	            WhatsAppPhoneNumber toUpdate = whatsAppPhoneNumberMapper.mapDTOToWhatsAppPhoneNumber(whatsAppPhoneNumberDto,whatsAppProject,employee);
	            WhatsAppPhoneNumber toSave = whatsAppPhoneNumber.get();

	            toSave.setWhatsAppProject(toUpdate.getWhatsAppProject());
	            toSave.setPhoneNumber(toUpdate.getPhoneNumber());
	            toSave.setPhoneNumberID(toUpdate.getPhoneNumberID());
	            toSave.setWhatsAppAccountID(toUpdate.getWhatsAppAccountID());
//	          toSave.setAiModel(toUpdate.getAiModel());
	            toSave.setCallBackURL(toUpdate.getCallBackURL());
	            toSave.setCallBackSecret(toUpdate.getCallBackSecret());
	            toSave.setOrganization(toUpdate.getOrganization());
//	          toSave.setCostPerInboundMessage(toUpdate.getCostPerInboundMessage());
//	          toSave.setCostPerOutboundMessage(toUpdate.getCostPerOutboundMessage());
	            toSave.setCountry(toUpdate.getCountry());
	            toSave.setCurrency(toUpdate.getCurrency());
	            toSave.setActive(toUpdate.isActive());
	            toSave.setAdmin(toUpdate.getAdmin());
	            toSave.setAiCallExtension(toUpdate.getAiCallExtension());
	            toSave.setEmployeeExtensionAccessList(toUpdate.getEmployeeExtensionAccessList());
	            WhatsAppPhoneNumber current = whatsAppPhoneNumberRepository.save(toSave);
	            //System.out.println("Updated WhatsAppPhoneNumber with ID: " + current.getId());

	            WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(whatsAppPhoneNumberDto.getPhoneNumber(), current, "update");
	            //System.out.println("Updated memory data for phoneNumber: " + whatsAppPhoneNumberDto.getPhoneNumber());

	            if(oldPhone != null && !oldPhone.equals(whatsAppPhoneNumberDto.getPhoneNumber())) {
	                //System.out.println("Phone number changed, updating file category for new and old phones");
	                createFileCategoryForOrgService.modifyBaseCategoryForWhatsAppPhoneData(whatsAppPhoneNumberDto.getOrganization(), whatsAppPhoneNumberDto.getPhoneNumber(), "update", oldPhone);
	            }
	        } else {
	            //System.out.println("WhatsAppPhoneNumber not found with ID: " + whatsAppPhoneNumberDto.getId());
	            toReturn = false;
	        }
	    } catch(Exception e) {
	        toReturn = false;
	        //System.out.println("Exception while updating WhatsAppPhoneNumber:");
	        e.printStackTrace();
	    }
	    return toReturn;
	}

	public boolean delete(Long id) {
	    //System.out.println("Deleting WhatsAppPhoneNumber with ID: " + id);
	    boolean toReturn = true;
	    try {
	        Optional<WhatsAppPhoneNumber> whatsAppPhoneNumber = whatsAppPhoneNumberRepository.findById(id);
	        if(whatsAppPhoneNumber.isPresent()) {
	            WhatsAppPhoneNumber phoneNumberToDelete = whatsAppPhoneNumber.get();

	            whatsAppPhoneNumberRepository.delete(phoneNumberToDelete);
	            //System.out.println("Deleted WhatsAppPhoneNumber with ID: " + id);

	            createFileCategoryForOrgService.modifyBaseCategoryForWhatsAppPhoneData(phoneNumberToDelete.getOrganization(), phoneNumberToDelete.getPhoneNumber(), "delete", "");
	            //System.out.println("Deleted file category for phoneNumber: " + phoneNumberToDelete.getPhoneNumber());

	            WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(phoneNumberToDelete.getPhoneNumber(), phoneNumberToDelete, "delete");
	            //System.out.println("Deleted memory data for phoneNumber: " + phoneNumberToDelete.getPhoneNumber());
	        } else {
	            //System.out.println("WhatsAppPhoneNumber not found with ID: " + id);
	            toReturn = false;
	        }
	    } catch(Exception e) {
	        toReturn = false;
	        //System.out.println("Exception while deleting WhatsAppPhoneNumber:");
	        e.printStackTrace();
	    }
	    return toReturn;
	}

	 
	
	/**
	 * The task of the method is enable user in the database after confirming the account
	 * @param employeeExtensionAccessList employee access list for whats app number
	 * @return organization user organization
	 * @throws JsonProcessingException 
	 */
	public int updateEmployeeAccessListByOrganization(List<WhatsAppManagementEmployeeDto> employeeExtensionAccessList, Long id) throws JsonProcessingException {
	    
	    //System.out.println("updateEmployeeAccessListByOrganization called with employeeExtensionAccessList size : " + (employeeExtensionAccessList != null ? employeeExtensionAccessList.size() : "null"));
	    
	    ObjectMapper objectMapper = new ObjectMapper();
	    
	    String jsonString = null;
	    if(employeeExtensionAccessList != null) {
	        jsonString = objectMapper.writeValueAsString(employeeExtensionAccessList);
	        //System.out.println("EmployeeExtensionAccessList JSON: " + jsonString);
	    } else {
	        //System.out.println("EmployeeExtensionAccessList is null");
	        jsonString = "[]";  // Avoid null issues in DB update
	    }
	    
	    int toReturn = whatsAppPhoneNumberRepository.updateEmployeeAccessListByOrganization(jsonString, id);
	    //System.out.println("Database update result for updateEmployeeAccessListByOrganization: " + toReturn);
	    
	    Optional<WhatsAppPhoneNumber> whatsAppPhoneNumber = whatsAppPhoneNumberRepository.findById(id);
	    if(whatsAppPhoneNumber.isPresent()) {
	        //System.out.println("Updating WhatsAppMemoryData for phoneNumber: " + whatsAppPhoneNumber.get().getPhoneNumber());
	        WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(whatsAppPhoneNumber.get().getPhoneNumber(), whatsAppPhoneNumber.get(), "update");
	    } else {
	        //System.out.println("WhatsAppPhoneNumber not found for id: " + id);
	    }
	    
	    return toReturn;
	}


	/**
	 * The task of the method is enable user in the database after confirming the account
	 * @param admin admin for phone number
	 * @return organization user organization
	 */
	public int updateAdminEmployeeForWhatsAppNumberByOrganization(Employee admin, Long id) {
	    //System.out.println("updateAdminEmployeeForWhatsAppNumberByOrganization called with admin: " + (admin != null ? admin.getId() : "null") + " and id: " + id);
	    
	    int toReturn = whatsAppPhoneNumberRepository.updateAdminEmployeeForWhatsAppNumberByOrganization(admin, id);
	    //System.out.println("Database update result for updateAdminEmployeeForWhatsAppNumberByOrganization: " + toReturn);
	    
	    Optional<WhatsAppPhoneNumber> whatsAppPhoneNumber = whatsAppPhoneNumberRepository.findById(id);
	    if(whatsAppPhoneNumber.isPresent()) {
	        //System.out.println("Updating WhatsAppMemoryData for phoneNumber: " + whatsAppPhoneNumber.get().getPhoneNumber());
	        WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(whatsAppPhoneNumber.get().getPhoneNumber(), whatsAppPhoneNumber.get(), "update");
	    } else {
	        //System.out.println("WhatsAppPhoneNumber not found for id: " + id);
	    }
	    
	    return toReturn;
	}


	/**
	 * The method is to retrieve whats app numbers by organization from the database and display them.
	 *
	 * After downloading all the data about the employee,
	 * the data is mapped to dto which will display only those needed
	 * @return list of whats app numbers by organization with specification of data in WhatsAppPhoneNumberDto
	 */
	public WhatsAppPhoneNumber findByPhoneNumber(String phoneNumber) {
	    //System.out.println("findByPhoneNumber called with phoneNumber: " + phoneNumber);
	    WhatsAppPhoneNumber result = whatsAppPhoneNumberRepository.findByPhoneNumber(phoneNumber);
	    //System.out.println("findByPhoneNumber result: " + (result != null ? "found" : "not found"));
	    return result;
	}


	/**
	 * The method is to retrieve all whats app numbers by EmployeeInExtensionAccessList Or Admin from the database and display them.
	 *
	 * After downloading all the data about the employee,
	 * the data is mapped to dto which will display only those needed
	 * @return list of all whats app numbers by organization with specification of data in WhatsAppPhoneNumberDto
	 * @throws Exception 
	 */
	public List<WhatsAppPhoneNumber> findAllByEmployeeInExtensionAccessListOrAdmin(String employeeExension) throws Exception {
	    List<WhatsAppPhoneNumber> toReturn = new ArrayList<>();

	    try {
	        if (employeeExension == null || employeeExension.trim().isEmpty()) {
	            throw new Exception("Employee extension is required");
	        }

	        employeeExension = employeeExension.trim();

	        // 1) Try memory first
	        Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
	                EmployeeDataAndState.workOnAllEmployeeDataAndState(employeeExension, null, "get-one");

	        EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
	        if (allEmployeeDataAndState != null) {
	            employeeDataAndStateDTO = allEmployeeDataAndState.get(employeeExension);
	        }

	        // 2) If not in memory, check DB and then refresh memory
	        if (employeeDataAndStateDTO == null || employeeDataAndStateDTO.getEmployee() == null) {

	            // ---- DB LOOKUP (REPLACE THIS LINE WITH YOUR ACTUAL METHOD) ----
	            // Example options (pick the one that exists in your EmployeeRepository):
	            // Employee empDb = employeeRepository.findByExtension(employeeExension);
	            // Employee empDb = employeeRepository.findByExtensionAndOrganization(employeeExension, orgName);
	            // Employee empDb = employeeRepository.getOneByExtension(employeeExension);
	            Employee empDb = employeeRepository.findByExtension(employeeExension);
	            // --------------------------------------------------------------

	            if (empDb == null) {
	                throw new Exception("Employee not found having extension: " + employeeExension);
	            }

	            // Build DTO and update memory
	            EmployeeDataAndStateDTO dtoToCache = new EmployeeDataAndStateDTO();
	            dtoToCache.setEmployee(empDb);

	            // If your DTO has "state" fields, keep defaults or set them here safely.
	            // dtoToCache.setCurrentState("...");

	            EmployeeDataAndState.workOnAllEmployeeDataAndState(employeeExension, dtoToCache, "update");

	            // Re-read from memory so below code stays unchanged
	            Map<String, EmployeeDataAndStateDTO> reloaded =
	                    EmployeeDataAndState.workOnAllEmployeeDataAndState(employeeExension, null, "get-one");
	            if (reloaded != null) {
	                employeeDataAndStateDTO = reloaded.get(employeeExension);
	            }
	        }

	        if (employeeDataAndStateDTO == null || employeeDataAndStateDTO.getEmployee() == null) {
	            throw new Exception("Employee not found having extension: " + employeeExension);
	        }

	        String searchString = "extension\":\"" + employeeExension;

	        toReturn = whatsAppPhoneNumberRepository.findAllByEmployeeExtensionAccessListContainingOrAdmin(
	                searchString,
	                employeeDataAndStateDTO.getEmployee()
	        );

	    } catch (Exception e) {
	        throw e;
	    }

	    return toReturn;
	}



	/**
	 * The method is to retrieve all whats app numbers by organization from the database and display them.
	 *
	 * After downloading all the data about the employee,
	 * the data is mapped to dto which will display only those needed
	 * @return list of all whats app numbers by organization with specification of data in WhatsAppPhoneNumberDto
	 */
	public List<WhatsAppPhoneNumberDto> findAllByOrganization(String organization) {
	    //System.out.println("findAllByOrganization called with organization: " + organization);
	    
	    List<WhatsAppPhoneNumberDto> dtos = whatsAppPhoneNumberRepository.getAllByOrganization(organization)
	            .stream()
	            .map(whatsAppPhoneNumberMapper::mapWhatsAppPhoneNumberToDTO)
	            .collect(Collectors.toList());
	    
	    //System.out.println("findAllByOrganization returning " + dtos.size() + " DTO(s)");
	    
	    return dtos;
	}

	/**
	 * The method is to retrieve all whats app numbers by organization and whats app project from the database and display them.
	 *
	 * After downloading all the data about the employee,
	 * the data is mapped to dto which will display only those needed
	 * @return list of all whats app numbers by organization and whats app project with specification of data in WhatsAppPhoneNumberDto
	 */
	public List<WhatsAppPhoneNumberDto> getAllByOrganizationAndWhatsAppProject(String organization, WhatsAppProject whatsAppProject) {
	    //System.out.println("getAllByOrganizationAndWhatsAppProject called with organization: " + organization + ", whatsAppProject: " + (whatsAppProject != null ? whatsAppProject.getId() : "null"));
	    List<WhatsAppPhoneNumberDto> dtos = whatsAppPhoneNumberRepository.getAllByOrganizationAndWhatsAppProject(organization, whatsAppProject)
	            .stream()
	            .map(whatsAppPhoneNumberMapper::mapWhatsAppPhoneNumberToDTO)
	            .collect(Collectors.toList());
	    //System.out.println("getAllByOrganizationAndWhatsAppProject returning " + dtos.size() + " DTO(s)");
	    return dtos;
	}

	/**
	 * The method is to retrieve all whats app numbers by organization and whats app project and active from the database and display them.
	 *
	 * After downloading all the data about the employee,
	 * the data is mapped to dto which will display only those needed
	 * @return list of all whats app numbers by organization and whats app project and active with specification of data in WhatsAppPhoneNumberDto
	 */
	public List<WhatsAppPhoneNumberDto> getAllByOrganizationAndWhatsAppProjectAndActive(String organization, WhatsAppProject whatsAppProject, boolean active) {
	    //System.out.println("getAllByOrganizationAndWhatsAppProjectAndActive called with organization: " + organization + ", whatsAppProject: " + (whatsAppProject != null ? whatsAppProject.getId() : "null") + ", active: " + active);
	    List<WhatsAppPhoneNumberDto> dtos = whatsAppPhoneNumberRepository.getAllByOrganizationAndWhatsAppProjectAndActive(organization, whatsAppProject, active)
	            .stream()
	            .map(whatsAppPhoneNumberMapper::mapWhatsAppPhoneNumberToDTO)
	            .collect(Collectors.toList());
	    //System.out.println("getAllByOrganizationAndWhatsAppProjectAndActive returning " + dtos.size() + " DTO(s)");
	    return dtos;
	}
	

	/**
	 * The method is to retrieve all whats app numbers by organization and admin from the database and display them.
	 *
	 * After downloading all the data about the employee,
	 * the data is mapped to dto which will display only those needed
	 * @return list of all whats app numbers by organization and admin with specification of data in WhatsAppPhoneNumberDto
	 */
	public List<WhatsAppPhoneNumberDto> getAllByOrganizationAndAdmin(String organization, Employee admin) {
	    //System.out.println("getAllByOrganizationAndAdmin called with organization: " + organization + ", admin: " + (admin != null ? admin.getId() : "null"));
	    List<WhatsAppPhoneNumberDto> dtos = whatsAppPhoneNumberRepository.getAllByOrganizationAndAdmin(organization, admin)
	            .stream()
	            .map(whatsAppPhoneNumberMapper::mapWhatsAppPhoneNumberToDTO)
	            .collect(Collectors.toList());
	    //System.out.println("getAllByOrganizationAndAdmin returning " + dtos.size() + " DTO(s)");
	    return dtos;
	}
	

	public Boolean processEmbeddedSignupCompleteFlow(
	        String organization,
	        EmbeddedSignupResultDto payload,
	        Employee employee
	) {
	    try {
	        String org = organization != null ? organization.trim() : "";

	        System.out.println("\n================== EMBEDDED SIGNUP START ==================");
	        System.out.println("[EmbeddedSignup] org = " + org);
	        System.out.println("[EmbeddedSignup] employeeId = " + (employee != null ? employee.getId() : "null"));
	        System.out.println("[EmbeddedSignup] payloadNull = " + (payload == null));

	        if (org.isBlank() || payload == null || isBlank(payload.getCode())) {
	            System.out.println("[EmbeddedSignup][ERROR] Invalid input. orgBlank=" + org.isBlank()
	                    + ", payloadNull=" + (payload == null)
	                    + ", codeBlank=" + (payload == null ? "true" : isBlank(payload.getCode())));
	            System.out.println("================== EMBEDDED SIGNUP END (FAIL) ==================\n");
	            return false;
	        }

	        // 1) Project for THIS organization (only for appId/appSecret/systemToken)
	        WhatsAppProject project = getProjectForOrganization(org);
	        if (project == null) {
	            System.out.println("[EmbeddedSignup][ERROR] No WhatsAppProject found for org=" + org);
	            System.out.println("================== EMBEDDED SIGNUP END (FAIL) ==================\n");
	            return false;
	        }

	        String graphVersion = !isBlank(project.getApiVersion()) ? project.getApiVersion().trim() : "v23.0";
	        String appId = project.getAppID();
	        String appSecret = project.getAppSecret();

	        System.out.println("[EmbeddedSignup] projectId = " + project.getId());
	        System.out.println("[EmbeddedSignup] graphVersion = " + graphVersion);
	        System.out.println("[EmbeddedSignup] appIdPresent = " + (!isBlank(appId)));
	        System.out.println("[EmbeddedSignup] appSecretPresent = " + (!isBlank(appSecret)));

	        if (isBlank(appId) || isBlank(appSecret)) {
	            System.out.println("[EmbeddedSignup][ERROR] Missing appId/appSecret for projectId=" + project.getId());
	            System.out.println("================== EMBEDDED SIGNUP END (FAIL) ==================\n");
	            return false;
	        }

	        String redirectUri = applicationContext.getEnvironment().getProperty("meta.redirect.uri", "");
	        System.out.println("[EmbeddedSignup] meta.redirect.uri = " + redirectUri);

	        if (isBlank(redirectUri)) {
	            System.out.println("[EmbeddedSignup][ERROR] meta.redirect.uri missing in application properties");
	            System.out.println("================== EMBEDDED SIGNUP END (FAIL) ==================\n");
	            return false;
	        }

	        RestTemplate rt = new RestTemplate();

	        // 2) Exchange code -> access_token (TEMP token; DO NOT store in DB)
	        String tokenUrl = "https://graph.facebook.com/" + graphVersion + "/oauth/access_token"
	                + "?client_id=" + URLEncoder.encode(appId, StandardCharsets.UTF_8)
	                + "&client_secret=" + URLEncoder.encode(appSecret, StandardCharsets.UTF_8)
	                + "&code=" + URLEncoder.encode(payload.getCode(), StandardCharsets.UTF_8)
	                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

	        System.out.println("[EmbeddedSignup] Exchanging code -> access_token ...");
	        MetaTokenResponse tokenResp = rt.getForObject(tokenUrl, MetaTokenResponse.class);

	        if (tokenResp == null || isBlank(tokenResp.getAccess_token())) {
	            System.out.println("[EmbeddedSignup][ERROR] Token exchange failed. tokenRespNull=" + (tokenResp == null));
	            System.out.println("================== EMBEDDED SIGNUP END (FAIL) ==================\n");
	            return false;
	        }

	        String codeAccessToken = tokenResp.getAccess_token();
	        System.out.println("[EmbeddedSignup] Token exchange SUCCESS. expires_in=" + tokenResp.getExpires_in());

	        // 3) IDs: prefer payload (frontend sends them)
	        String wabaId = safeTrim(payload.getWaba_id());
	        String phoneNumberId = safeTrim(payload.getPhone_number_id());
	        String businessId = safeTrim(payload.getBusiness_id());

	        System.out.println("[EmbeddedSignup] payload.business_id = " + maskId(businessId));
	        System.out.println("[EmbeddedSignup] payload.waba_id = " + maskId(wabaId));
	        System.out.println("[EmbeddedSignup] payload.phone_number_id = " + maskId(phoneNumberId));

	        // IMPORTANT CHANGE:
	        // If payload missing businessId, DO NOT fallback to DB.
	        // Always fetch from Graph using codeAccessToken.
	        if (isBlank(businessId)) {
	            System.out.println("[EmbeddedSignup][INFO] businessId missing in payload. Fetching via /me/businesses using code token...");
	            businessId = fetchFirstBusinessId(rt, graphVersion, codeAccessToken);
	        }

	        System.out.println("[EmbeddedSignup] resolved businessId = " + maskId(businessId));

	        if (isBlank(businessId)) {
	            System.out.println("[EmbeddedSignup][ERROR] Could not resolve businessId from Graph (/me/businesses).");
	            System.out.println("================== EMBEDDED SIGNUP END (FAIL) ==================\n");
	            return false;
	        }

	        // 4) If wabaId or phoneNumberId missing, fetch from Graph (business -> waba -> phone_numbers)
	        if (isBlank(wabaId) || isBlank(phoneNumberId)) {
	            System.out.println("[EmbeddedSignup][WARN] Missing wabaId/phoneNumberId; attempting Graph fetch using businessId=" + maskId(businessId));

	            // Fetch WABA list
	            String wabaUrl = "https://graph.facebook.com/" + graphVersion + "/" + businessId
	                    + "/client_whatsapp_business_accounts?access_token="
	                    + URLEncoder.encode(codeAccessToken, StandardCharsets.UTF_8);

	            System.out.println("[EmbeddedSignup] Fetching WABA via: /" + businessId + "/client_whatsapp_business_accounts");
	            Map wabaResp = rt.getForObject(wabaUrl, Map.class);
	            wabaId = pickFirstIdFromDataArray(wabaResp);

	            if (isBlank(wabaId)) {
	                System.out.println("[EmbeddedSignup][ERROR] Could not resolve wabaId from businessId=" + maskId(businessId));
	                System.out.println("================== EMBEDDED SIGNUP END (FAIL) ==================\n");
	                return false;
	            }

	            // Fetch phone numbers under WABA
	            String phonesUrl = "https://graph.facebook.com/" + graphVersion + "/" + wabaId
	                    + "/phone_numbers?access_token="
	                    + URLEncoder.encode(codeAccessToken, StandardCharsets.UTF_8);

	            System.out.println("[EmbeddedSignup] Fetching phone numbers via: /" + wabaId + "/phone_numbers");
	            Map phonesResp = rt.getForObject(phonesUrl, Map.class);
	            phoneNumberId = pickFirstIdFromDataArray(phonesResp);

	            if (isBlank(phoneNumberId)) {
	                System.out.println("[EmbeddedSignup][ERROR] Could not resolve phoneNumberId from wabaId=" + maskId(wabaId));
	                System.out.println("================== EMBEDDED SIGNUP END (FAIL) ==================\n");
	                return false;
	            }

	            System.out.println("[EmbeddedSignup] Resolved via Graph: wabaId=" + maskId(wabaId) + ", phoneNumberId=" + maskId(phoneNumberId));
	        }

	        // 5) Fetch display phone number from Graph using phone_number_id (code token)
	        System.out.println("[EmbeddedSignup] Fetching display_phone_number using code token for phoneNumberId=" + maskId(phoneNumberId));
	        String displayPhone = fetchDisplayPhoneNumber(rt, graphVersion, codeAccessToken, phoneNumberId);

	        if (isBlank(displayPhone)) {
	            System.out.println("[EmbeddedSignup][ERROR] Failed to fetch display_phone_number via code token.");
	            System.out.println("================== EMBEDDED SIGNUP END (FAIL) ==================\n");
	            return false;
	        }

	        String normalizedPhone = normalizePhoneNumber(displayPhone);
	        System.out.println("[EmbeddedSignup] display_phone_number = " + displayPhone);
	        System.out.println("[EmbeddedSignup] normalizedPhone = " + normalizedPhone);

	        if (normalizedPhone.isBlank()) {
	            System.out.println("[EmbeddedSignup][ERROR] Normalized phone is blank/invalid");
	            System.out.println("================== EMBEDDED SIGNUP END (FAIL) ==================\n");
	            return false;
	        }

	        // 6) Verify YOUR permanent/system token can access this phone
	        // IMPORTANT: This token is stable and does not change.
	        String systemToken = safeTrim(project.getAccessToken());
	        System.out.println("[EmbeddedSignup] systemTokenPresent = " + (!isBlank(systemToken)));

	        boolean systemTokenHasAccess = false;
	        if (!isBlank(systemToken)) {
	            System.out.println("[EmbeddedSignup] Verifying system token access for phoneNumberId=" + maskId(phoneNumberId));
	            String displayViaSystem = fetchDisplayPhoneNumber(rt, graphVersion, systemToken, phoneNumberId);
	            systemTokenHasAccess = !isBlank(displayViaSystem);
	            System.out.println("[EmbeddedSignup] system token access = " + systemTokenHasAccess);
	        } else {
	            System.out.println("[EmbeddedSignup][WARN] System token is missing in WhatsAppProject.accessToken");
	        }

	        if (!systemTokenHasAccess) {
	            System.out.println("[EmbeddedSignup][WARN] Embedded signup linked the app, but system token cannot access this phone_number_id.");
	            System.out.println("[EmbeddedSignup][WARN] This means asset permissions are NOT granted to your system user yet.");
	            System.out.println("[EmbeddedSignup][WARN] Fix Meta Business permissions/asset assignment, then retry.");
	            System.out.println("================== EMBEDDED SIGNUP END (FAIL) ==================\n");
	            return false;
	        }
	        

	        // 7) Create/update WhatsAppPhoneNumber
	        WhatsAppPhoneNumber existing = whatsAppPhoneNumberRepository.findByPhoneNumber(normalizedPhone);
	        System.out.println("[EmbeddedSignup] existingRecordFound = " + (existing != null));
	        if (existing != null) System.out.println("[EmbeddedSignup] existingId = " + existing.getId());

	        String verifyToken = (existing != null && !isBlank(existing.getVerifyToken()))
	                ? existing.getVerifyToken()
	                : generateVerifyToken(normalizedPhone, org);

	        if (existing != null) {
	            existing.setOrganization(org);
	            existing.setWhatsAppProject(project);
	            existing.setWhatsAppAccountID(wabaId);
	            existing.setPhoneNumberID(phoneNumberId);
	            existing.setVerifyToken(verifyToken);
	            existing.setAdmin(employee);
	            if (existing.getAiCallExtension() == null) existing.setAiCallExtension(employee.getExtension());
	            existing.setActive(true);

	            whatsAppPhoneNumberRepository.save(existing);
	            WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(existing.getPhoneNumber(), existing, "update");

	            System.out.println("[EmbeddedSignup] Updated WhatsAppPhoneNumber id=" + existing.getId()
	                    + " phone=" + normalizedPhone + " org=" + org);
	            System.out.println("================== EMBEDDED SIGNUP END (SUCCESS) ==================\n");
	            return true;
	        }
	        

	        boolean isCoexistence = isCoexistenceNumber(rt, graphVersion, systemToken, phoneNumberId);
	        String ridContacts = "";
	        String ridHistory = "";
	        
	        if (isCoexistence) {
	            System.out.println("[EmbeddedSignup] Coexistence detected. Triggering contacts + history sync...");

	            // IMPORTANT: Meta allows this only once per phone_number_id.
	            // So you must store flags in DB to prevent calling again.
	            ridContacts = requestSmbAppDataSync(rt, graphVersion, systemToken, phoneNumberId, "smb_app_state_sync");
	            ridHistory = requestSmbAppDataSync(rt, graphVersion, systemToken, phoneNumberId, "history");

	            System.out.println("[EmbeddedSignup] Sync requested: contactsRequestId=" + ridContacts + ", historyRequestId=" + ridHistory);
	        } else {
	            System.out.println("[EmbeddedSignup] Not a coexistence number. Skipping SMB sync.");
	        }

	        WhatsAppPhoneNumberDto dto = WhatsAppPhoneNumberDto.builder()
	                .whatsAppProjectId(project.getId())
	                .organization(org)
	                .phoneNumber(normalizedPhone)
	                .verifyToken(verifyToken)
	                .whatsAppAccountID(wabaId)
	                .phoneNumberID(phoneNumberId)
	                .adminEmployeeId(employee.getId())
	                .secondAdminEmployeeId(null)
	                .callBackURL(null)
	                .callBackSecret(null)
	                .aiModel(null)
	                .aiOutputClassName(null)
	                .active(true)
	                .autoAiMessageAllowed(true)
	                .autoAiMessageLimit(-1)
	                .aiCallExtension(employee.getExtension())
	                .storeVerifyCustomerPropertyInventory(false)
	                .build();

	        WhatsAppPhoneNumber created = this.create(dto);
	        created.setRidContacts(ridContacts);
	        created.setRidHistory(ridHistory);
	        if(isCoexistence)
	        created.setCoexistenceSyncRequested(true);
	        whatsAppPhoneNumberRepository.save(created);
	        WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(created.getPhoneNumber(), created, "update");

	        
	        System.out.println("[EmbeddedSignup] Create result createdNull=" + (created == null));
	        if (created != null) {
	            System.out.println("[EmbeddedSignup] Created WhatsAppPhoneNumber id=" + created.getId()
	                    + " phone=" + normalizedPhone + " org=" + org);
	        }

	        
	        System.out.println("================== EMBEDDED SIGNUP END (" + (created != null ? "SUCCESS" : "FAIL") + ") ==================\n");
	        return created != null;

	    } catch (Exception e) {
	        System.out.println("[EmbeddedSignup][EXCEPTION] " + e.getMessage());
	        e.printStackTrace();
	        System.out.println("================== EMBEDDED SIGNUP END (EXCEPTION) ==================\n");
	        return false;
	    }
	}

	
	private String safeTrim(String s) {
	    return s == null ? null : s.trim();
	}

	@SuppressWarnings({"rawtypes"})
	private String fetchDisplayPhoneNumber(
	        RestTemplate rt,
	        String graphVersion,
	        String accessToken,
	        String phoneNumberId
	) {
	    try {
	        String url = "https://graph.facebook.com/" + graphVersion + "/" + phoneNumberId
	                + "?fields=display_phone_number"
	                + "&access_token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);

	        System.out.println("[EmbeddedSignup] Calling Graph API to fetch display_phone_number");
	        // System.out.println("[EmbeddedSignup][DEBUG] url=" + url); // enable only for debugging

	        Map resp = rt.getForObject(url, Map.class);
	        if (resp == null) {
	            System.out.println("[EmbeddedSignup][ERROR] Graph response is null for phoneNumberId=" + phoneNumberId);
	            return null;
	        }

	        Object v = resp.get("display_phone_number");
	        if (v == null) {
	            System.out.println("[EmbeddedSignup][ERROR] display_phone_number missing in Graph response: " + resp);
	            return null;
	        }

	        return String.valueOf(v);

	    } catch (Exception e) {
	        System.out.println("[EmbeddedSignup][ERROR] Exception while fetching display_phone_number: " + e.getMessage());
	        return null;
	    }
	}


	// helper for logs (mask ids)
	private String maskId(String id) {
	    if (id == null) return null;
	    String s = id.trim();
	    if (s.length() <= 6) return s;
	    return s.substring(0, 3) + "..." + s.substring(s.length() - 3);
	}

	
	private WhatsAppProject getProjectForOrganization(String org) {
	    if (org == null || org.trim().isEmpty()) return null;
	    List<WhatsAppProject> list = whatsAppProjectRepository.getAllByOrganization(org.trim());
	    if (list == null || list.isEmpty()) return null;
	    return list.get(0); // if you later add "active" flag, pick active
	}

	@SuppressWarnings({"rawtypes"})
	private boolean isCoexistenceNumber(RestTemplate rt, String graphVersion, String accessToken, String phoneNumberId) {
	    try {
	        String url = "https://graph.facebook.com/" + graphVersion + "/" + phoneNumberId
	                + "?fields=is_on_biz_app,platform_type"
	                + "&access_token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);

	        Map resp = rt.getForObject(url, Map.class);
	        if (resp == null) return false;

	        Object isOnBizApp = resp.get("is_on_biz_app");
	        Object platformType = resp.get("platform_type");

	        boolean onBizApp = (isOnBizApp instanceof Boolean) ? ((Boolean) isOnBizApp) : "true".equalsIgnoreCase(String.valueOf(isOnBizApp));
	        String platform = platformType != null ? String.valueOf(platformType) : "";

	        System.out.println("[EmbeddedSignup] is_on_biz_app=" + onBizApp + ", platform_type=" + platform);

	        return onBizApp && "CLOUD_API".equalsIgnoreCase(platform);

	    } catch (Exception e) {
	        System.out.println("[EmbeddedSignup][WARN] Could not check coexistence flags: " + e.getMessage());
	        return false;
	    }
	}

	
	@SuppressWarnings({"rawtypes"})
	private String requestSmbAppDataSync(RestTemplate rt, String graphVersion, String accessToken, String phoneNumberId, String syncType) {
	    try {
	        String url = "https://graph.facebook.com/" + graphVersion + "/" + phoneNumberId + "/smb_app_data";

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        // Meta expects bearer token
	        headers.set("Authorization", "Bearer " + accessToken);

	        String body = "{"
	                + "\"messaging_product\":\"whatsapp\","
	                + "\"sync_type\":\"" + syncType + "\""
	                + "}";

	        HttpEntity<String> entity = new HttpEntity<>(body, headers);

	        ResponseEntity<Map> res = rt.exchange(url, HttpMethod.POST, entity, Map.class);
	        Map resp = res.getBody();

	        if (resp == null) return null;

	        Object requestId = resp.get("request_id");
	        String rid = requestId != null ? String.valueOf(requestId) : null;

	        System.out.println("[EmbeddedSignup][SYNC] sync_type=" + syncType + " request_id=" + rid);
	        return rid;

	    } catch (Exception e) {
	        System.out.println("[EmbeddedSignup][SYNC][ERROR] sync_type=" + syncType + " failed: " + e.getMessage());
	        return null;
	    }
	}


	@SuppressWarnings({"rawtypes","unchecked"})
	private String fetchFirstBusinessId(RestTemplate rt, String graphVersion, String accessToken) {
	    try {
	        String url = "https://graph.facebook.com/" + graphVersion
	                + "/me/businesses?access_token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
	        Map resp = rt.getForObject(url, Map.class);
	        return pickFirstIdFromDataArray(resp);
	    } catch (Exception e) {
	        return null;
	    }
	}


	private boolean isBlank(String s) {
	    return s == null || s.trim().isEmpty();
	}

	private String normalizePhoneNumber(String phoneNumber) {
	    if (phoneNumber == null) return "";
	    String p = phoneNumber.trim().replace(" ", "");
	    if (!p.startsWith("+")) p = "+" + p;
	    if (p.length() < 8) return "";
	    return p;
	}

	private String generateVerifyToken(String phoneNumber, String organization) {
	    return "mlh-" + organization + "-" + phoneNumber.replace("+", "") + "-" + java.util.UUID.randomUUID();
	}


	// Generic helper: expects response like {"data":[{"id":"..."}]}
	@SuppressWarnings("unchecked")
	private String pickFirstIdFromDataArray(Map resp) {
	    if (resp == null) return null;
	    Object data = resp.get("data");
	    if (!(data instanceof List)) return null;
	    List list = (List) data;
	    if (list.isEmpty()) return null;
	    Object first = list.get(0);
	    if (!(first instanceof Map)) return null;
	    Object id = ((Map) first).get("id");
	    return id != null ? String.valueOf(id) : null;
	}

	
}
