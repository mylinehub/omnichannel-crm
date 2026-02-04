package com.mylinehub.crm.whatsapp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.data.dto.WhatsAppTemplateVariableListDto;
import com.mylinehub.crm.whatsapp.dto.WhatsAppPhoneNumberTemplateVariableDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplateVariable;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;
import com.mylinehub.crm.whatsapp.mapper.WhatsAppNumberTemplateVariableMapper;
import com.mylinehub.crm.whatsapp.repository.WhatsAppNumberTemplateVariableRepository;
import com.mylinehub.crm.whatsapp.repository.WhatsAppPhoneNumberTemplatesRepository;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class WhatsAppNumberTemplateVariableService {

    private final WhatsAppPhoneNumberTemplatesRepository whatsAppPhoneNumberTemplatesRepository;
    private final WhatsAppNumberTemplateVariableRepository whatsAppNumberTemplateVariableRepository;
    private final WhatsAppNumberTemplateVariableMapper whatsAppNumberTemplateVariableMapper;

    public List<WhatsAppPhoneNumberTemplateVariable> getAll(){
        //System.out.println("Fetching all WhatsAppPhoneNumberTemplateVariables");
        List<WhatsAppPhoneNumberTemplateVariable> result = whatsAppNumberTemplateVariableRepository.findAll();
        //System.out.println("Found " + (result != null ? result.size() : 0) + " template variables");
        return result;
    }

    public boolean update(Long templateId, String organization, List<WhatsAppPhoneNumberTemplateVariableDto> whatsAppPhoneNumberTemplateVariablesDto){     
        boolean toReturn = true;
        try {
            //System.out.println("Update template variables for templateId: " + templateId);
            //System.out.println("Incoming WhatsAppPhoneNumberTemplateVariablesDto size: " + (whatsAppPhoneNumberTemplateVariablesDto != null ? whatsAppPhoneNumberTemplateVariablesDto.size() : 0));

            final WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplates = whatsAppPhoneNumberTemplatesRepository.getOne(templateId);

            if(whatsAppPhoneNumberTemplates == null || organization == null) {
                //System.out.println("Template or organization is null, returning false");
                return false;
            } else {
                //System.out.println("Deleting all previous template variables for templateId: " + templateId + " and organization: " + organization);
                whatsAppNumberTemplateVariableRepository.deleteAllVariableByTemplate(whatsAppPhoneNumberTemplates, organization);
            }

            //System.out.println("Deleted all previous template variables.");

            if(whatsAppPhoneNumberTemplateVariablesDto != null && !whatsAppPhoneNumberTemplateVariablesDto.isEmpty())
            {
                List<WhatsAppPhoneNumberTemplateVariable> whatsAppPhoneNumberTemplateVariables = whatsAppPhoneNumberTemplateVariablesDto.stream()
                    .map((dto)->whatsAppNumberTemplateVariableMapper.mapDTOToWhatsAppPhoneNumberTemplateVariable(dto,whatsAppPhoneNumberTemplates))
                    .collect(Collectors.toList());

                //System.out.println("Converted DTOs to entities. Count: " + whatsAppPhoneNumberTemplateVariables.size());

                whatsAppPhoneNumberTemplateVariables = whatsAppNumberTemplateVariableRepository.saveAll(whatsAppPhoneNumberTemplateVariables);

                //System.out.println("Saved all template variables. Count: " + whatsAppPhoneNumberTemplateVariables.size());

                WhatsAppTemplateVariableListDto whatsAppTemplateVariableListDto = new WhatsAppTemplateVariableListDto();
                whatsAppTemplateVariableListDto.setToUpdate(whatsAppPhoneNumberTemplateVariables);

                //System.out.println("Adding details to memory for phone number: " + whatsAppPhoneNumberTemplates.getWhatsAppPhoneNumber().getPhoneNumber());

                // Add detail to memory Data
                WhatsAppMemoryData.workWithWhatsAppPhoneNumberTemplateVariable(
                    whatsAppPhoneNumberTemplates.getWhatsAppPhoneNumber().getPhoneNumber(),
                    null,
                    whatsAppTemplateVariableListDto,
                    "update-all"
                );

                //System.out.println("Updated memory data successfully.");
            }
            else
            {
                //System.out.println("No variables to update (empty or null list).");
            }
        }
        catch(Exception e)
        {
            //System.out.println("Exception while updating template variables:");
            e.printStackTrace();
            toReturn = false;
        }
        return toReturn;
    }

    /**
     * The method is to retrieve all whats app template variable from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all whats app template variable with specification of data in WhatsAppNumberReportDto
     */
    public List<WhatsAppPhoneNumberTemplateVariableDto> findAllByWhatsAppNumberTemplateAndOrganization(Long templateId, String organization){

        //System.out.println("Fetching template variables by templateId: " + templateId + " and organization: " + organization);

        if(templateId == null || organization == null) {
            //System.out.println("TemplateId or organization is null, returning null");
            return null;
        }

        WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplates = whatsAppPhoneNumberTemplatesRepository.getOne(templateId);

        if(whatsAppPhoneNumberTemplates != null) {
            List<WhatsAppPhoneNumberTemplateVariableDto> result = whatsAppNumberTemplateVariableRepository
                .findAllByWhatsAppNumberAndOrganization(organization, whatsAppPhoneNumberTemplates)
                .stream()
                .map(whatsAppNumberTemplateVariableMapper::mapWhatsAppPhoneNumberTemplateVariableToDTO)
                .collect(Collectors.toList());
            //System.out.println("Found " + result.size() + " variables");
            return result;
        }
        else
        {
            //System.out.println("No template found for templateId: " + templateId);
            return null;
        }
    }

    /**
     * The method is to retrieve all whats app template variable from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all whats app template variable with specification of data in WhatsAppNumberReportDto
     */
    public List<WhatsAppPhoneNumberTemplateVariable> findAllByWhatsAppNumberTemplate(WhatsAppPhoneNumberTemplates template){
        if(template != null) {
            //System.out.println("Fetching template variables by template entity");
            List<WhatsAppPhoneNumberTemplateVariable> result = whatsAppNumberTemplateVariableRepository.findAllByWhatsAppNumberTemplate(template);
            //System.out.println("Found " + (result != null ? result.size() : 0) + " variables");
            return result;
        }
        else
        {
            //System.out.println("Template is null, returning null");
            return null;
        }
    }
}
