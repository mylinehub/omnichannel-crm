package com.mylinehub.crm.whatsapp.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.dto.WhatsAppOpenAiAccountDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppOpenAiAccount;
import com.mylinehub.crm.whatsapp.mapper.WhatsAppOpenAiAccountMapper;
import com.mylinehub.crm.whatsapp.repository.WhatsAppOpenAiAccountRepository;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class WhatsAppOpenAiAccountService {

    private final WhatsAppOpenAiAccountRepository whatsAppOpenAiAccountRepository;
    private final WhatsAppOpenAiAccountMapper whatsAppOpenAiAccountMapper;

    public List<WhatsAppOpenAiAccount> getAll(){
        //System.out.println("Fetching all WhatsAppOpenAiAccounts");
        List<WhatsAppOpenAiAccount> accounts = whatsAppOpenAiAccountRepository.findAll();
        //System.out.println("Found " + (accounts != null ? accounts.size() : 0) + " OpenAI accounts");
        return accounts;
    }

    public boolean create(WhatsAppOpenAiAccountDto whatsAppOpenAiAccountDto){     
        boolean toReturn = true;
        try {
            //System.out.println("Creating new WhatsAppOpenAiAccount for organization: " + whatsAppOpenAiAccountDto.getOrganization());

            WhatsAppOpenAiAccount current = whatsAppOpenAiAccountRepository
                    .save(whatsAppOpenAiAccountMapper.mapDTOToWhatsAppOpenAiAccount(whatsAppOpenAiAccountDto)); 

            //System.out.println("Saved WhatsAppOpenAiAccount with ID: " + current.getId());

            // Add detail to memory Data
            WhatsAppMemoryData.workWithwhatsAppOpenAIAccount(current.getOrganization(), current, "update");

            //System.out.println("Updated memory data for organization: " + current.getOrganization());

        }
        catch(Exception e)
        {
            toReturn = false;
            //System.out.println("Exception while creating WhatsAppOpenAiAccount:");
            e.printStackTrace();
        }
        return toReturn;
    }

    public boolean update(WhatsAppOpenAiAccountDto whatsAppOpenAiAccountDto){     
        boolean toReturn = true;
        try {
            //System.out.println("Updating WhatsAppOpenAiAccount with ID: " + whatsAppOpenAiAccountDto.getId());

            Optional<WhatsAppOpenAiAccount> whatsAppOpenAiAccount = whatsAppOpenAiAccountRepository.findById(whatsAppOpenAiAccountDto.getId());
            if(whatsAppOpenAiAccount.isPresent())
            {
                WhatsAppOpenAiAccount toUpdate = whatsAppOpenAiAccountMapper.mapDTOToWhatsAppOpenAiAccount(whatsAppOpenAiAccountDto);
                WhatsAppOpenAiAccount toSave = whatsAppOpenAiAccount.get();

                toSave.setKey(toUpdate.getKey());
                toSave.setAdminKey(toUpdate.getAdminKey());
                toSave.setProjectID(toUpdate.getProjectID());
                toSave.setAssistantID(toUpdate.getAssistantID());
                toSave.setEmail(toUpdate.getEmail());
                toSave.setChatBotName(toUpdate.getChatBotName());
                toSave.setChatBotAccess(toUpdate.getChatBotAccess());
                toSave.setClientSecret(toUpdate.getClientSecret());
                toSave.setOrganization(toUpdate.getOrganization());

                WhatsAppOpenAiAccount current = whatsAppOpenAiAccountRepository.save(toSave);

                //System.out.println("Updated WhatsAppOpenAiAccount with ID: " + current.getId());

                // Add detail to memory Data
                WhatsAppMemoryData.workWithwhatsAppOpenAIAccount(current.getOrganization(), current, "update-existing");

                //System.out.println("Updated memory data for organization: " + current.getOrganization());
            }
            else 
            {
                //System.out.println("WhatsAppOpenAiAccount not found with ID: " + whatsAppOpenAiAccountDto.getId());
                toReturn = false;
            }
        }
        catch(Exception e)
        {
            toReturn = false;
            //System.out.println("Exception while updating WhatsAppOpenAiAccount:");
            e.printStackTrace();
        }
        return toReturn;
    }

    public boolean delete(Long id){     
        boolean toReturn = true;
        try {
            //System.out.println("Deleting WhatsAppOpenAiAccount with ID: " + id);

            Optional<WhatsAppOpenAiAccount> whatsAppOpenAiAccount = whatsAppOpenAiAccountRepository.findById(id);
            if(whatsAppOpenAiAccount.isPresent())
            {
                WhatsAppOpenAiAccount accountToDelete = whatsAppOpenAiAccount.get();
                whatsAppOpenAiAccountRepository.delete(accountToDelete);

                //System.out.println("Deleted WhatsAppOpenAiAccount with ID: " + id);

                // Delete detail to memory Data
                WhatsAppMemoryData.workWithwhatsAppOpenAIAccount(accountToDelete.getOrganization(), accountToDelete, "delete");

                //System.out.println("Deleted memory data for organization: " + accountToDelete.getOrganization());
            }
            else 
            {
                //System.out.println("WhatsAppOpenAiAccount not found with ID: " + id);
                toReturn = false;
            }
        }
        catch(Exception e)
        {
            toReturn = false;
            //System.out.println("Exception while deleting WhatsAppOpenAiAccount:");
            e.printStackTrace();
        }
        return toReturn;
    }

    /**
     * The method is to retrieve all whats app open AI Account by organization from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all whats app open AI Account by organization with specification of data in WhatsAppOpenAiAccountDto
     */
    public List<WhatsAppOpenAiAccountDto> findAllByOrganization(String organization){
        //System.out.println("Fetching WhatsAppOpenAiAccounts for organization: " + organization);

        if(organization == null || organization.isEmpty()) {
            //System.out.println("Organization is null or empty, returning empty list");
            return List.of();
        }

        List<WhatsAppOpenAiAccountDto> result = whatsAppOpenAiAccountRepository.getAllByOrganization(organization)
                .stream()
                .map(whatsAppOpenAiAccountMapper::mapWhatsAppOpenAiAccountToDTO)
                .collect(Collectors.toList());

        //System.out.println("Found " + result.size() + " OpenAI accounts for organization: " + organization);

        return result;
    }
}
