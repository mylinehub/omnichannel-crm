package com.mylinehub.crm.whatsapp.service;

import java.io.IOException;
import java.util.List;

import org.springframework.context.ApplicationContext;

import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.entity.WhatsAppOpenAiAccount;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplateVariable;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPrompt;

/**
 * @author Anand Goel
 * @version 1.0
 */
public class SetupWhatsAppMemoryData {

    public static void setupWhatsAppPhoneNumberData(ApplicationContext applicationContext) throws IOException {
       //System.out.println("Starting setupWhatsAppPhoneNumberData...");
        try {
            WhatsAppPhoneNumberService whatsAppPhoneNumberService = applicationContext.getBean(WhatsAppPhoneNumberService.class);
            List<WhatsAppPhoneNumber> allWhatsAppPhoneNumbers = whatsAppPhoneNumberService.getAll();
            WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(null, null, "set-new");
           //System.out.println("Cleared old WhatsApp phone number data");

            if (allWhatsAppPhoneNumbers != null && !allWhatsAppPhoneNumbers.isEmpty()) {
                for (WhatsAppPhoneNumber phoneNumber : allWhatsAppPhoneNumbers) {
                   //System.out.println("Adding phone number to memory: " + phoneNumber.getPhoneNumber());
                    WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(phoneNumber.getPhoneNumber(), phoneNumber, "update");
                }
            } else {
               //System.out.println("No WhatsApp phone numbers found to load.");
            }
        } catch (Exception e) {
           //System.out.println("Exception in setupWhatsAppPhoneNumberData: " + e.getMessage());
            e.printStackTrace();
        }
       //System.out.println("Completed setupWhatsAppPhoneNumberData");
    }

    public static void setupWhatsAppOpenAiAccountData(ApplicationContext applicationContext) throws IOException {
       //System.out.println("Starting setupWhatsAppOpenAiAccountData...");
        try {
            WhatsAppOpenAiAccountService whatsAppOpenAiAccountService = applicationContext.getBean(WhatsAppOpenAiAccountService.class);
            List<WhatsAppOpenAiAccount> allWhatsAppOpenAiAccounts = whatsAppOpenAiAccountService.getAll();
            WhatsAppMemoryData.workWithwhatsAppOpenAIAccount(null, null, "set-new");
           //System.out.println("Cleared old WhatsApp OpenAI account data");

            if (allWhatsAppOpenAiAccounts != null && !allWhatsAppOpenAiAccounts.isEmpty()) {
                for (WhatsAppOpenAiAccount account : allWhatsAppOpenAiAccounts) {
                   //System.out.println("Adding OpenAI account for organization: " + account.getOrganization());
                    WhatsAppMemoryData.workWithwhatsAppOpenAIAccount(account.getOrganization(), account, "update");
                }
            } else {
               //System.out.println("No WhatsApp OpenAI accounts found to load.");
            }
        } catch (Exception e) {
           //System.out.println("Exception in setupWhatsAppOpenAiAccountData: " + e.getMessage());
            e.printStackTrace();
        }
       //System.out.println("Completed setupWhatsAppOpenAiAccountData");
    }

    public static void setupWhatsAppPhoneNumberTemplates(ApplicationContext applicationContext) throws IOException {
       //System.out.println("Starting setupWhatsAppPhoneNumberTemplates...");
        try {
            WhatsAppPhoneNumberTemplatesService whatsAppPhoneNumberTemplatesService = applicationContext.getBean(WhatsAppPhoneNumberTemplatesService.class);
            List<WhatsAppPhoneNumberTemplates> allWhatsAppPhoneNumberTemplates = whatsAppPhoneNumberTemplatesService.getAll();
            WhatsAppMemoryData.workWithWhatsAppPhoneNumbersTemplates(null, null, "set-new");
           //System.out.println("Cleared old WhatsApp phone number templates");

            if (allWhatsAppPhoneNumberTemplates != null && !allWhatsAppPhoneNumberTemplates.isEmpty()) {
                for (WhatsAppPhoneNumberTemplates template : allWhatsAppPhoneNumberTemplates) {
                    String phoneNumber = (template.getWhatsAppPhoneNumber() != null) ? template.getWhatsAppPhoneNumber().getPhoneNumber() : "unknown";
                   //System.out.println("Adding phone number template for phone: " + phoneNumber);
                    WhatsAppMemoryData.workWithWhatsAppPhoneNumbersTemplates(phoneNumber, template, "update");
                }
            } else {
               //System.out.println("No WhatsApp phone number templates found to load.");
            }
        } catch (Exception e) {
           //System.out.println("Exception in setupWhatsAppPhoneNumberTemplates: " + e.getMessage());
            e.printStackTrace();
        }
       //System.out.println("Completed setupWhatsAppPhoneNumberTemplates");
    }

    public static void setupWhatsAppPhoneNumberTemplateVariable(ApplicationContext applicationContext) throws IOException {
       //System.out.println("Starting setupWhatsAppPhoneNumberTemplateVariable...");
        try {
            WhatsAppNumberTemplateVariableService whatsAppNumberTemplateVariableService = applicationContext.getBean(WhatsAppNumberTemplateVariableService.class);
            List<WhatsAppPhoneNumberTemplateVariable> allVariables = whatsAppNumberTemplateVariableService.getAll();
            WhatsAppMemoryData.workWithWhatsAppPhoneNumberTemplateVariable(null, null, null, "set-new");
           //System.out.println("Cleared old WhatsApp phone number template variables");

            if (allVariables != null && !allVariables.isEmpty()) {
                for (WhatsAppPhoneNumberTemplateVariable variable : allVariables) {
                    String phoneNumber = "unknown";
                    if (variable.getWhatsAppPhoneNumberTemplates() != null && variable.getWhatsAppPhoneNumberTemplates().getWhatsAppPhoneNumber() != null) {
                        phoneNumber = variable.getWhatsAppPhoneNumberTemplates().getWhatsAppPhoneNumber().getPhoneNumber();
                    }
                   //System.out.println("Adding template variable for phone number: " + phoneNumber);
                    WhatsAppMemoryData.workWithWhatsAppPhoneNumberTemplateVariable(phoneNumber, variable, null, "update");
                }
            } else {
               //System.out.println("No WhatsApp phone number template variables found to load.");
            }
        } catch (Exception e) {
           //System.out.println("Exception in setupWhatsAppPhoneNumberTemplateVariable: " + e.getMessage());
            e.printStackTrace();
        }
       //System.out.println("Completed setupWhatsAppPhoneNumberTemplateVariable");
    }
    
    public static void setupWhatsAppPhoneNumberPrompts(ApplicationContext applicationContext) throws IOException {
        try {
            WhatsAppPromptService promptService = applicationContext.getBean(WhatsAppPromptService.class);

            // You need a method like this in WhatsAppPromptService:
            // List<WhatsAppPrompt> getAllActive();
            List<WhatsAppPrompt> allActivePrompts = promptService.getAllActive(true);

            // reset memory map
            WhatsAppMemoryData.workWithWhatsAppPhoneCategoryPrompts(null, null, null, null, "set-new");

            if (allActivePrompts != null && !allActivePrompts.isEmpty()) {
                for (WhatsAppPrompt p : allActivePrompts) {
                    if (p == null) continue;
                    if (!p.isActive()) continue;

                    if (p.getWhatsAppPhoneNumber() == null) continue;
                    if (p.getWhatsAppPhoneNumber().getPhoneNumber() == null) continue;

                    String phoneNumber = p.getWhatsAppPhoneNumber().getPhoneNumber();
                    String category = (p.getCategory() != null && !p.getCategory().trim().isEmpty())
                            ? p.getCategory().trim()
                            : "default";

                    // store whole entity (NOT prompt string)
                    WhatsAppMemoryData.workWithWhatsAppPhoneCategoryPrompts(
                            phoneNumber,
                            category,
                            p,       // entity
                            null,    // categoryPromptMap (not needed for update)
                            "update"
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
