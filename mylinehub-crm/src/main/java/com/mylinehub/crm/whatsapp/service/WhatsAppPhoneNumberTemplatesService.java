package com.mylinehub.crm.whatsapp.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mapstruct.Context;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.entity.Product;
import com.mylinehub.crm.repository.ProductRepository;
import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.dto.WhatsAppPhoneNumberTemplateDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;
import com.mylinehub.crm.whatsapp.mapper.WhatsAppPhoneNumberTemplatesMapper;
import com.mylinehub.crm.whatsapp.repository.WhatsAppPhoneNumberTemplatesRepository;
import com.mylinehub.crm.whatsapp.repository.WhatsAppPhoneNumberRepository;
import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class WhatsAppPhoneNumberTemplatesService {

	private final WhatsAppPhoneNumberTemplatesRepository whatsAppPhoneNumberTemplatesRepository;
	private final WhatsAppPhoneNumberTemplatesMapper whatsAppPhoneNumberTemplatesMapper;
	private final WhatsAppPhoneNumberRepository WhatsAppPhoneNumberRepository;
	private final ProductRepository ProductRepository;
	
	public List<WhatsAppPhoneNumberTemplates> getAll(){
        //System.out.println("getAll called");
        List<WhatsAppPhoneNumberTemplates> list = whatsAppPhoneNumberTemplatesRepository.findAll();
        //System.out.println("getAll returning " + list.size() + " templates");
        return list;
    }

	public WhatsAppPhoneNumberTemplates create(Product product,WhatsAppPhoneNumberTemplateDto whatsAppPhoneNumberTemplatesDto){	 
		//System.out.println("create called with DTO: " + whatsAppPhoneNumberTemplatesDto);
		WhatsAppPhoneNumberTemplates toReturn = null;
		try {
			

			WhatsAppPhoneNumber whatsAppPhoneNumber = WhatsAppPhoneNumberRepository.getOne(whatsAppPhoneNumberTemplatesDto.getWhatsAppPhoneNumberId());
			
			toReturn = whatsAppPhoneNumberTemplatesRepository.
			 		save(whatsAppPhoneNumberTemplatesMapper.mapDTOToWhatsAppPhoneNumberTemplates(whatsAppPhoneNumberTemplatesDto,whatsAppPhoneNumber,product));
			//System.out.println("Created WhatsAppPhoneNumberTemplates with id: " + toReturn.getId());

			//Add detail to memory Data
			WhatsAppMemoryData.workWithWhatsAppPhoneNumbersTemplates(toReturn.getWhatsAppPhoneNumber().getPhoneNumber(), toReturn, "update");
		}
		catch(Exception e) {
			toReturn = null;
			//System.out.println("Exception in create:");
			e.printStackTrace();
		}
		return toReturn;
	}

	public boolean update(WhatsAppPhoneNumberTemplateDto whatsAppPhoneNumberTemplateDto){	 
		//System.out.println("update called with DTO: " + whatsAppPhoneNumberTemplateDto);
		boolean toReturn = true;
		try {
			Optional<WhatsAppPhoneNumberTemplates> watsAppPhoneNumberTemplate = whatsAppPhoneNumberTemplatesRepository.findById(whatsAppPhoneNumberTemplateDto.getId());
			if(watsAppPhoneNumberTemplate.isPresent()) {
				
				WhatsAppPhoneNumber whatsAppPhoneNumber = WhatsAppPhoneNumberRepository.getOne(whatsAppPhoneNumberTemplateDto.getWhatsAppPhoneNumberId());
				Product product = ProductRepository.getOne(whatsAppPhoneNumberTemplateDto.getProductId());
				
				
				WhatsAppPhoneNumberTemplates toUpdate = whatsAppPhoneNumberTemplatesMapper.mapDTOToWhatsAppPhoneNumberTemplates(whatsAppPhoneNumberTemplateDto,whatsAppPhoneNumber,product);
				WhatsAppPhoneNumberTemplates toSave = watsAppPhoneNumberTemplate.get();

				toSave.setWhatsAppPhoneNumber(toUpdate.getWhatsAppPhoneNumber());
				toSave.setTemplateName(toUpdate.getTemplateName());
				toSave.setConversationType(whatsAppPhoneNumberTemplateDto.getConversationType());
				toSave.setProduct(toUpdate.getProduct());
				toSave.setOrganization(toUpdate.getOrganization());
				toSave.setFollowOrder(toUpdate.isFollowOrder());
				toSave.setCurrency(toUpdate.getCurrency());
				toSave.setMediaPath(toUpdate.getMediaPath());
				toSave.setMediaType(toUpdate.getMediaType());
				WhatsAppPhoneNumberTemplates current = whatsAppPhoneNumberTemplatesRepository.save(toSave);
				//System.out.println("Updated WhatsAppPhoneNumberTemplates with id: " + current.getId());

				//Add detail to memory Data
				WhatsAppMemoryData.workWithWhatsAppPhoneNumbersTemplates(current.getWhatsAppPhoneNumber().getPhoneNumber(), current, "update-existing");
			} else {
				//System.out.println("No WhatsAppPhoneNumberTemplates found with id: " + whatsAppPhoneNumberTemplateDto.getId());
				toReturn = false;
			}
		}
		catch(Exception e) {
			toReturn = false;
			//System.out.println("Exception in update:");
			e.printStackTrace();
		}
		return toReturn;
	}

	public boolean delete(Long id){	 
		//System.out.println("delete called with id: " + id);
		boolean toReturn = true;
		try {
			Optional<WhatsAppPhoneNumberTemplates> watsAppPhoneNumberTemplate = whatsAppPhoneNumberTemplatesRepository.findById(id);
			if(watsAppPhoneNumberTemplate.isPresent()) {
				whatsAppPhoneNumberTemplatesRepository.delete(watsAppPhoneNumberTemplate.get());
				//System.out.println("Deleted WhatsAppPhoneNumberTemplates with id: " + id);

				//Delete detail to memory Data
				WhatsAppMemoryData.workWithWhatsAppPhoneNumbersTemplates(watsAppPhoneNumberTemplate.get().getWhatsAppPhoneNumber().getPhoneNumber(), watsAppPhoneNumberTemplate.get(), "delete");
			} else {
				//System.out.println("No WhatsAppPhoneNumberTemplates found with id: " + id);
				toReturn = false;
			}
		}
		catch(Exception e) {
			toReturn = false;
			//System.out.println("Exception in delete:");
			e.printStackTrace();
		}
		return toReturn;
	}

	/**
     * The method is to retrieve all whats app templates by organization from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all templates by organization with specification of data in WhatsAppPhoneNumberTemplateDto
     */
    public List<WhatsAppPhoneNumberTemplateDto> findAllByOrganization(String organization){
    	//System.out.println("findAllByOrganization called with organization: " + organization);
        List<WhatsAppPhoneNumberTemplateDto> dtos = whatsAppPhoneNumberTemplatesRepository.getAllByOrganization(organization)
                .stream()
                .map(whatsAppPhoneNumberTemplatesMapper::mapWhatsAppPhoneNumberTemplatesToDTO)
                .collect(Collectors.toList());
        //System.out.println("findAllByOrganization returning " + dtos.size() + " templates");
        return dtos;
    }

    /**
     * The method is to retrieve all whats app templates by organization and whatsAppPhoneNumber from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all templates by organization and whatsAppPhoneNumber with specification of data in WhatsAppPhoneNumberTemplateDto
     */
    public List<WhatsAppPhoneNumberTemplateDto> getAllByOrganizationAndWhatsAppPhoneNumber(String organization, WhatsAppPhoneNumber whatsAppPhoneNumber){
    	//System.out.println("getAllByOrganizationAndWhatsAppPhoneNumber called with organization: " + organization + ", phoneNumber: " + (whatsAppPhoneNumber != null ? whatsAppPhoneNumber.getPhoneNumber() : "null"));
        List<WhatsAppPhoneNumberTemplateDto> dtos = whatsAppPhoneNumberTemplatesRepository.getAllByOrganizationAndWhatsAppPhoneNumber(organization, whatsAppPhoneNumber)
                .stream()
                .map(whatsAppPhoneNumberTemplatesMapper::mapWhatsAppPhoneNumberTemplatesToDTO)
                .collect(Collectors.toList());
        //System.out.println("getAllByOrganizationAndWhatsAppPhoneNumber returning " + dtos.size() + " templates");
        return dtos;
    }

    /**
     * The method is to retrieve all whats app templates by whatsAppPhoneNumber from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all templates by organization and whatsAppPhoneNumber with specification of data in WhatsAppPhoneNumberTemplateDto
     */
    public List<WhatsAppPhoneNumberTemplates> getAllByWhatsAppPhoneNumber(WhatsAppPhoneNumber whatsAppPhoneNumber){
    	//System.out.println("getAllByWhatsAppPhoneNumber called with phoneNumber: " + (whatsAppPhoneNumber != null ? whatsAppPhoneNumber.getPhoneNumber() : "null"));
        List<WhatsAppPhoneNumberTemplates> list = whatsAppPhoneNumberTemplatesRepository.getAllByWhatsAppPhoneNumber(whatsAppPhoneNumber);
        //System.out.println("getAllByWhatsAppPhoneNumber returning " + list.size() + " templates");
        return list;
    }

}
