package com.mylinehub.crm.whatsapp.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.whatsapp.dto.WhatsAppPromptVariablesDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPrompt;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPromptVariables;
import com.mylinehub.crm.whatsapp.mapper.WhatsAppPromptVariableMapper;
import com.mylinehub.crm.whatsapp.repository.WhatsAppPromptRepository;
import com.mylinehub.crm.whatsapp.repository.WhatsAppPromptVariableRepository;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class WhatsAppPromptVariableService {
	private final WhatsAppPromptVariableRepository whatsAppPromptVariableRepository;
	private final WhatsAppPromptRepository whatsAppPromptRepository;
	private final WhatsAppPromptVariableMapper whatsAppPromptVariableMapper;
	
	public List<WhatsAppPromptVariables> getAll(){
        return whatsAppPromptVariableRepository.findAll();
    }
	
	 public boolean create(WhatsAppPromptVariablesDto whatsAppPromptVariableDto){	 
		 boolean toReturn = true;
		 try {
			 WhatsAppPrompt whatsAppPrompt = whatsAppPromptRepository.getOne(whatsAppPromptVariableDto.getWhatsAppPromptId());
			 whatsAppPromptVariableRepository.
			 		save(whatsAppPromptVariableMapper.mapDTOToWhatsAppPromptVariable(whatsAppPromptVariableDto,whatsAppPrompt)); }
		 catch(Exception e)
		 {
			 toReturn = false;
			 e.printStackTrace();
		 }
		 return toReturn;
	 }
	 
	 
	 public boolean update(WhatsAppPromptVariablesDto whatsAppPromptVariableDto){	 
		 boolean toReturn = true;
		 try {
			 Optional <WhatsAppPromptVariables> whatsAppPromptVariables = whatsAppPromptVariableRepository.findById(whatsAppPromptVariableDto.getId());
			 WhatsAppPrompt whatsAppPrompt = whatsAppPromptRepository.getOne(whatsAppPromptVariableDto.getWhatsAppPromptId());
			 if(!whatsAppPromptVariables.isEmpty())
			 {
				 
				 WhatsAppPromptVariables toUpdate = whatsAppPromptVariableMapper.mapDTOToWhatsAppPromptVariable(whatsAppPromptVariableDto,whatsAppPrompt);
				 WhatsAppPromptVariables toSave = whatsAppPromptVariables.get();
				 
				 toSave.setWhatsAppPrompt(toUpdate.getWhatsAppPrompt());
				 toSave.setLabel(toUpdate.getLabel());
				 toSave.setDescription(toUpdate.getDescription());
				 toSave.setActive(toUpdate.isActive());
				 toSave.setOrganization(toUpdate.getOrganization());
				 
				 whatsAppPromptVariableRepository.save(toSave);
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
			 Optional <WhatsAppPromptVariables> whatsAppPromptVariables =  whatsAppPromptVariableRepository.findById(id);
			 if(!whatsAppPromptVariables.isEmpty())
				 whatsAppPromptVariableRepository.delete(whatsAppPromptVariables.get());
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
     * The method is to retrieve all prompt variables by organization from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all prompt variables by organization with specification of data in WhatsAppPromptVariablesDto
     */
    
    public List<WhatsAppPromptVariablesDto> findAllByOrganization(String organization){
        return whatsAppPromptVariableRepository.getAllByOrganization(organization)
                .stream()
                .map(whatsAppPromptVariableMapper::mapWhatsAppPromptVariableToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * The method is to retrieve all prompt variables by organization from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all prompt variables by organization with specification of data in WhatsAppPromptVariablesDto
     */
    
    public List<WhatsAppPromptVariablesDto> getAllByOrganizationAndWhatsAppPrompt(String organization,WhatsAppPrompt whatsAppPrompt){
        return whatsAppPromptVariableRepository.getAllByOrganizationAndWhatsAppPrompt(organization,whatsAppPrompt)
                .stream()
                .map(whatsAppPromptVariableMapper::mapWhatsAppPromptVariableToDTO)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all prompt variables by organization from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all prompt variables by organization with specification of data in WhatsAppPromptVariablesDto
     */
    
    public List<WhatsAppPromptVariablesDto> getAllByOrganizationAndWhatsAppPromptAndActive(String organization,WhatsAppPrompt whatsAppPrompt,boolean active){
        return whatsAppPromptVariableRepository.getAllByOrganizationAndWhatsAppPromptAndActive(organization,whatsAppPrompt,active)
                .stream()
                .map(whatsAppPromptVariableMapper::mapWhatsAppPromptVariableToDTO)
                .collect(Collectors.toList());
    }
	
}
