package com.mylinehub.crm.whatsapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.dto.WhatsAppPromptDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPrompt;
import com.mylinehub.crm.whatsapp.mapper.WhatsAppPromptMapper;
import com.mylinehub.crm.whatsapp.repository.WhatsAppPhoneNumberRepository;
import com.mylinehub.crm.whatsapp.repository.WhatsAppPromptRepository;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class WhatsAppPromptService {
	private final WhatsAppPromptRepository whatsAppPromptRepository;
	private final WhatsAppPromptMapper whatsAppPromptMapper;
	private final WhatsAppPhoneNumberRepository whatsAppPhoneNumberRepository;
	
	 public List<WhatsAppPrompt> getAllActiveByOrganization(String organization) {
	        // if you add repo method getAllByOrganizationAndActive
	        try {
	            return whatsAppPromptRepository.getAllByOrganizationAndActive(organization, true);
	        } catch (Exception e) {
	            // fallback: if not added, use existing and filter at caller if needed
	            return whatsAppPromptRepository.getAllByOrganization(organization);
	        }
	  }

	 public List<WhatsAppPrompt> getAllActive(boolean active) {
	        // if you add repo method getAllByOrganizationAndActive
	        try {
	        	return whatsAppPromptRepository.getAllByActive(active);
	        } catch (Exception e) {
	            // fallback: if not added, use existing and filter at caller if needed
	            return new ArrayList<>();
	        }
	  }
	 
	 public List<WhatsAppPrompt> getAllActiveByOrgAndPhone(String organization, WhatsAppPhoneNumber phone) {
	        return whatsAppPromptRepository.getAllByOrganizationAndWhatsAppPhoneNumberAndActive(organization, phone, true);
	  }
	    
	 public boolean create(WhatsAppPromptDto WhatsAppPromptDto){	 
		 boolean toReturn = true;
		 try {
			 WhatsAppPhoneNumber whatsAppPhoneNumber = whatsAppPhoneNumberRepository.getOne(WhatsAppPromptDto.getWhatsAppPhoneNumberId());
			
			 WhatsAppPrompt saved = whatsAppPromptRepository.save(
					    whatsAppPromptMapper.mapDTOToWhatsAppPrompt(WhatsAppPromptDto, whatsAppPhoneNumber)
					);

			 try {
					    String phone = saved.getWhatsAppPhoneNumber().getPhoneNumber();
					    String category = saved.getCategory();

					    WhatsAppMemoryData.workWithWhatsAppPhoneCategoryPrompts(
					            phone,
					            category,
					            saved,
					            null,
					            "update"
					    );
			} catch (Exception ignore) {}
		 }
		 catch(Exception e)
		 {
			 toReturn = false;
			 e.printStackTrace();
		 }
		 return toReturn;
	 }
	 
	 
	 public boolean update(WhatsAppPromptDto WhatsAppPromptDto){	 
		 boolean toReturn = true;
		 try {
			 Optional <WhatsAppPrompt> whatsAppPrompt = whatsAppPromptRepository.findById(WhatsAppPromptDto.getId());
			 if(!whatsAppPrompt.isEmpty())
			 {
				 WhatsAppPhoneNumber whatsAppPhoneNumber = whatsAppPhoneNumberRepository.getOne(WhatsAppPromptDto.getWhatsAppPhoneNumberId());
				 WhatsAppPrompt toUpdate = whatsAppPromptMapper.mapDTOToWhatsAppPrompt(WhatsAppPromptDto,whatsAppPhoneNumber);
				 WhatsAppPrompt toSave = whatsAppPrompt.get();
				 
				 toSave.setWhatsAppPhoneNumber(toUpdate.getWhatsAppPhoneNumber());
				 toSave.setPrompt(toUpdate.getPrompt());
				 toSave.setCategory(toUpdate.getCategory());
				 toSave.setActive(toUpdate.isActive());
				 toSave.setDelimiter(toUpdate.getDelimiter());
				 toSave.setOrganization(toUpdate.getOrganization());
				 
				 whatsAppPromptRepository.save(toSave); 
				 
				// ---- update memory cache too ----
				 try {
				     String phone = toSave.getWhatsAppPhoneNumber().getPhoneNumber(); // adjust getter name
				     String category = toSave.getCategory();

				     // safest: update by phone+category
				     WhatsAppMemoryData.workWithWhatsAppPhoneCategoryPrompts(
				             phone,
				             category,
				             toSave,
				             null,
				             "update"
				     );
				 } catch (Exception ignore) {
				     // don't fail DB update because memory cache failed
				 }

			 }
			 else 
				toReturn = false;
			 }
		 
		 catch(Exception e)
		 {
			 toReturn = false;
			 e.printStackTrace();
		 }
		 return toReturn;
	 }
	 
	 
	 public boolean delete(Long id){	 
		 boolean toReturn = true;
		 try {
			 Optional <WhatsAppPrompt> whatsAppPrompt =  whatsAppPromptRepository.findById(id);
			 if(!whatsAppPrompt.isEmpty()) {
				 whatsAppPromptRepository.delete(whatsAppPrompt.get());
				 
				// ---- delete from memory cache too ----
				 try {
				     WhatsAppPrompt p = whatsAppPrompt.get();
				     String phone = p.getWhatsAppPhoneNumber().getPhoneNumber(); // adjust getter
				     String category = p.getCategory();

				     WhatsAppMemoryData.workWithWhatsAppPhoneCategoryPrompts(
				             phone,
				             category,
				             null,
				             null,
				             "delete"
				     );
				 } catch (Exception ignore) {
				 }

			 }
			 else 
				toReturn = false;
			 }
		 catch(Exception e)
		 {
			 toReturn = false;
			 e.printStackTrace();
		 }
		 return toReturn;
	 }
	 
	/**
     * The method is to retrieve all prompts by organization from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all prompts by organization with specification of data in WhatsAppPromptDto
     */
    
    public List<WhatsAppPromptDto> findAllByOrganization(String organization){
        return whatsAppPromptRepository.getAllByOrganization(organization)
                .stream()
                .map(whatsAppPromptMapper::mapWhatsAppPromptToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * The method is to retrieve all prompts by organization from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all prompts by organization with specification of data in WhatsAppPromptDto
     */
    
    public List<WhatsAppPromptDto> getAllByOrganizationAndWhatsAppPhoneNumber(String organization,WhatsAppPhoneNumber whatsAppPhoneNumber){
        return whatsAppPromptRepository.getAllByOrganizationAndWhatsAppPhoneNumber(organization,whatsAppPhoneNumber)
                .stream()
                .map(whatsAppPromptMapper::mapWhatsAppPromptToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * The method is to retrieve all prompts by organization from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all prompts by organization with specification of data in WhatsAppPromptDto
     */
    
    public List<WhatsAppPromptDto> getAllByOrganizationAndWhatsAppPhoneNumberAndActive(String organization,WhatsAppPhoneNumber whatsAppPhoneNumber,boolean active){
        return whatsAppPromptRepository.getAllByOrganizationAndWhatsAppPhoneNumberAndActive(organization,whatsAppPhoneNumber,active)
                .stream()
                .map(whatsAppPromptMapper::mapWhatsAppPromptToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * The method is to retrieve all prompts by organization from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all prompts by organization with specification of data in WhatsAppPromptDto
     */
    
    public List<WhatsAppPromptDto> getAllByOrganizationAndCategory(String organization,String category){
        return whatsAppPromptRepository.getAllByOrganizationAndCategory(organization,category)
                .stream()
                .map(whatsAppPromptMapper::mapWhatsAppPromptToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * The method is to retrieve all prompts by organization from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all prompts by organization with specification of data in WhatsAppPromptDto
     */
    
    public List<WhatsAppPromptDto> getAllByOrganizationAndCategoryAndActive(String organization,String category,boolean active){
        return whatsAppPromptRepository.getAllByOrganizationAndCategoryAndActive(organization,category,active)
                .stream()
                .map(whatsAppPromptMapper::mapWhatsAppPromptToDTO)
                .collect(Collectors.toList());
    }
	
}
