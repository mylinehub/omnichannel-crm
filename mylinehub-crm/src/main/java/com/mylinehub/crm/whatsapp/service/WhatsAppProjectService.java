package com.mylinehub.crm.whatsapp.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.whatsapp.dto.WhatsAppProjectDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppProject;
import com.mylinehub.crm.whatsapp.mapper.WhatsAppProjectMapper;
import com.mylinehub.crm.whatsapp.repository.WhatsAppProjectRepository;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class WhatsAppProjectService {
	private final WhatsAppProjectRepository whatsAppProjectRepository;
	private final WhatsAppProjectMapper whatsAppProjectMapper;
	
	
	 public WhatsAppProject create(WhatsAppProjectDto whatsAppProjectDto){	 
		 WhatsAppProject toReturn = null;
		 try {
			 toReturn = whatsAppProjectRepository.
			 		save(whatsAppProjectMapper.mapDTOToWhatsAppProject(whatsAppProjectDto)); }
		 catch(Exception e)
		 {
			 toReturn = null;
			 e.printStackTrace();
		 }
		 return toReturn;
	 }
	 
	 
	 public boolean update(WhatsAppProjectDto whatsAppProjectDto){	 
		 boolean toReturn = true;
		 try {
			 Optional <WhatsAppProject> whatsAppProject = whatsAppProjectRepository.findById(whatsAppProjectDto.getId());
			 if(!whatsAppProject.isEmpty())
			 {
				 WhatsAppProject toUpdate = whatsAppProjectMapper.mapDTOToWhatsAppProject(whatsAppProjectDto);
				 WhatsAppProject toSave = whatsAppProject.get();
				 
				 toSave.setAppEmail(toUpdate.getAppEmail());
				 toSave.setAppID(toUpdate.getAppID());
				 toSave.setAppName(toUpdate.getAppName());
				 toSave.setAppSecret(toUpdate.getAppSecret());
				 toSave.setApiVersion(toUpdate.getApiVersion());
				 toSave.setBusinessID(toUpdate.getBusinessID());
				 toSave.setBusinessPortfolio(toUpdate.getBusinessPortfolio());
				 toSave.setAccessToken(toUpdate.getAccessToken());
				 toSave.setClientToken(toUpdate.getClientToken());
				 toSave.setOrganization(toUpdate.getOrganization());
				 
				 whatsAppProjectRepository.save(toSave); 
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
			 Optional <WhatsAppProject> whatsAppProject =  whatsAppProjectRepository.findById(id);
			 if(!whatsAppProject.isEmpty())
				 whatsAppProjectRepository.delete(whatsAppProject.get());
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
     * The method is to retrieve all whats app projects by organization from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all whats app projects by organization with specification of data in WhatsAppProjectDto
     */
    
    public List<WhatsAppProjectDto> findAllByOrganization(String organization){
        return whatsAppProjectRepository.getAllByOrganization(organization)
                .stream()
                .map(whatsAppProjectMapper::mapWhatsAppProjectToDTO)
                .collect(Collectors.toList());
    }
	
}
