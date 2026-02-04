package com.mylinehub.crm.whatsapp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.entity.FileCategory;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.enums.FILE_STORE_REQUEST_TYPE;
import com.mylinehub.crm.mapper.FileCategoryMapper;
import com.mylinehub.crm.repository.FileCategoryRepository;
import com.mylinehub.crm.service.FileCategoryService;
import com.mylinehub.crm.service.FileService;
import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.data.dto.FileCategoryDateCheckDto;

import lombok.AllArgsConstructor;

/**
 * Service to create and manage file categories for WhatsApp organizations.
 */
@Service
@AllArgsConstructor
public class CreateFileCategoryForOrgService {

    private final FileCategoryRepository fileCategoryRepository;
    private final FileCategoryService fileCategoryService;
    private final FileCategoryMapper fileCategoryMapper;
    private final ApplicationContext applicationContext;

    public void createBaseCategoryForWhatsAppOrgData(String organizationString) {
        //System.out.println("createBaseCategoryForWhatsAppOrgData called for: " + organizationString);

        Map<String, Organization> organizationMap = OrganizationData.workWithAllOrganizationData(organizationString, null, "get-one", null);
        if (organizationMap == null || organizationMap.isEmpty()) {
            //System.out.println("Organization not found: " + organizationString);
            return;
        }

        Organization organization = organizationMap.get(organizationString);
        //System.out.println("Organization found: " + organization.getOrganization());

        FileCategory fileCategory = fileCategoryRepository.findByExtensionAndNameAndOrganization(
            organization.getOrganization(),
            organization.getWhatsAppMediaFolder(),
            organization.getOrganization()
        );

        if (fileCategory == null) {
            //System.out.println("Base file category not found, creating new...");

            fileCategory = new FileCategory();
            fileCategory.setExtension(organization.getOrganization());
            fileCategory.setOrganization(organization.getOrganization());
            fileCategory.setName(organization.getWhatsAppMediaFolder());
            fileCategory.setIconImageData(organization.getWhatsAppMediaFolderImageName());
            fileCategory.setIconImageType(organization.getWhatsAppMediaFolderImageType());
            fileCategory.setRoot(true);

            String uploadOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadFileCategoryOriginalDirectory");
            uploadOriginalDirectory = uploadOriginalDirectory + "/" + organization.getOrganization();

            boolean proceed = false;
            try {
                //System.out.println("Uploading icon image for base file category...");
                proceed = fileCategoryService.uploadIconImageByExtensionAndNameAndOrganization(
                    organization.getOrganization(),
                    "create",
                    "",
                    FileService.convertFiletoMultiPart(organization.getWhatsAppMediaFolderImage()),
                    fileCategory,
                    uploadOriginalDirectory
                );
            } catch (Exception e) {
                System.err.println("Exception during icon upload: " + e.getMessage());
                e.printStackTrace();
            }
            if (!proceed) {
                System.err.println("Failed to upload icon image for base category.");
            }
        } else {
            //System.out.println("File category already present. No action taken.");
        }
    }

    public void modifyBaseCategoryForWhatsAppPhoneData(String organizationString, String phoneNumber, String type, String oldName) throws Exception {
        //System.out.println("modifyBaseCategoryForWhatsAppPhoneData called:");
        //System.out.println(" organizationString: " + organizationString);
        //System.out.println(" phoneNumber: " + phoneNumber);
        //System.out.println(" type: " + type);
        //System.out.println(" oldName: " + oldName);

        Map<String, Organization> organizationMap = OrganizationData.workWithAllOrganizationData(organizationString, null, "get-one", null);
        if (organizationMap == null || organizationMap.isEmpty()) {
            System.err.println("Organization not found: " + organizationString);
            return;
        }

        Organization organization = organizationMap.get(organizationString);
        //System.out.println("Organization found: " + organization.getOrganization());

        String uploadOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadFileCategoryOriginalDirectory");
        uploadOriginalDirectory = uploadOriginalDirectory + "/" + organization.getOrganization();

        FileCategory fileCategory = fileCategoryRepository.findByExtensionAndNameAndOrganization(
            organization.getOrganization(),
            organization.getWhatsAppMediaFolder() + "/" + phoneNumber,
            organization.getOrganization()
        );

        boolean proceed = false;

        if (fileCategory == null) {
            //System.out.println("File category is null, creating new...");

            fileCategory = new FileCategory();
            fileCategory.setExtension(organization.getOrganization());
            fileCategory.setOrganization(organization.getOrganization());
            fileCategory.setName(organization.getWhatsAppMediaFolder() + "/" + phoneNumber);
            fileCategory.setRoot(false);

            try {
                if ("create".equalsIgnoreCase(type)) {
                    proceed = fileCategoryService.uploadIconImageByExtensionAndNameAndOrganization(
                        organization.getOrganization(),
                        type,
                        oldName,
                        null,
                        fileCategory,
                        uploadOriginalDirectory
                    );
                }
            } catch (Exception e) {
                System.err.println("Exception during icon upload: " + e.getMessage());
                e.printStackTrace();
            }
            if (!proceed) {
                System.err.println("Failed to upload icon image for phone category.");
            }
        } else {
            //System.out.println("File category exists.");

            if ("delete".equalsIgnoreCase(type)) {
                //System.out.println("Deleting file category...");
                proceed = fileCategoryService.deleteByExtensionAndNameAndOrganization(
                    FILE_STORE_REQUEST_TYPE.WHATSAPP.name(),
                    organization.getOrganization(),
                    fileCategoryMapper.mapFileCategoryToDTO(fileCategory),
                    uploadOriginalDirectory
                );
            } else if ("update".equalsIgnoreCase(type)) {
                //System.out.println("Updating file category...");
                proceed = fileCategoryService.updateFileCategoryByOrganization(
                    FILE_STORE_REQUEST_TYPE.WHATSAPP.name(),
                    organization.getOrganization(),
                    oldName,
                    null,
                    fileCategoryMapper.mapFileCategoryToDTO(fileCategory),
                    uploadOriginalDirectory
                );
            }
        }

        if (!proceed) {
            System.err.println("Operation '" + type + "' may have failed or was unnecessary.");
        }
    }

    public List<FileCategory> createBaseCategoryForWhatsAppPhoneDayData(String organizationString, String phoneNumber) throws Exception {
        LocalDateTime ldt = LocalDateTime.now().withNano(0);

        //System.out.println("createBaseCategoryForWhatsAppPhoneDayData called:");
        //System.out.println(" organizationString: " + organizationString);
        //System.out.println(" phoneNumber: " + phoneNumber);
        //System.out.println(" Date: " + ldt);

        Map<String, Map<String, FileCategoryDateCheckDto>> phoneNumberCategory = WhatsAppMemoryData.workWithVerifyIfCategoryCreatedMapData(phoneNumber, null, "get-one");
        Map<String, FileCategoryDateCheckDto> phoneNumberCategoryCheck = (phoneNumberCategory != null) ? phoneNumberCategory.get(phoneNumber) : null;

        FileCategoryDateCheckDto fileCategoryDateCheckDto = null;

        if (phoneNumberCategoryCheck == null) {
            //System.out.println("phoneNumberCategoryCheck is null, creating new entry...");

            fileCategoryDateCheckDto = new FileCategoryDateCheckDto();
            fileCategoryDateCheckDto.setSendCategoryCreated(true);
            fileCategoryDateCheckDto.setReceiveCategoryCreated(true);

            phoneNumberCategoryCheck = new ConcurrentHashMap<>();
            phoneNumberCategoryCheck.put(ldt.toString(), fileCategoryDateCheckDto);
            WhatsAppMemoryData.workWithVerifyIfCategoryCreatedMapData(phoneNumber, phoneNumberCategoryCheck, "update");

            return proceedToCreateCategoryForWhatsAppPhoneDayData(organizationString, phoneNumber, ldt);
        } else {
            //System.out.println("phoneNumberCategoryCheck exists.");

            fileCategoryDateCheckDto = phoneNumberCategoryCheck.get(ldt.toString());

            if (fileCategoryDateCheckDto == null) {
                //System.out.println("fileCategoryDateCheckDto is null for today, creating...");

                fileCategoryDateCheckDto = new FileCategoryDateCheckDto();
                fileCategoryDateCheckDto.setSendCategoryCreated(true);
                fileCategoryDateCheckDto.setReceiveCategoryCreated(true);

                phoneNumberCategoryCheck.put(ldt.toString(), fileCategoryDateCheckDto);
                WhatsAppMemoryData.workWithVerifyIfCategoryCreatedMapData(phoneNumber, phoneNumberCategoryCheck, "update");

                return proceedToCreateCategoryForWhatsAppPhoneDayData(organizationString, phoneNumber, ldt);
            } else {
                //System.out.println("fileCategoryDateCheckDto already exists for today, skipping creation.");
                return null;
            }
        }
    }

    public List<FileCategory> proceedToCreateCategoryForWhatsAppPhoneDayData(String organizationString, String phoneNumber, LocalDateTime ldt) throws Exception {
        //System.out.println("proceedToCreateCategoryForWhatsAppPhoneDayData called:");
        //System.out.println(" organizationString: " + organizationString);
        //System.out.println(" phoneNumber: " + phoneNumber);
        //System.out.println(" Date: " + ldt);

        List<FileCategory> fileCategoryList = new ArrayList<>();

        Map<String, Organization> organizationMap = OrganizationData.workWithAllOrganizationData(organizationString, null, "get-one", null);
        if (organizationMap == null || organizationMap.isEmpty()) {
            System.err.println("Organization not found: " + organizationString);
            return null;
        }

        Organization organization = organizationMap.get(organizationString);
        //System.out.println("Organization found: " + organization.getOrganization());

        FileCategory fileCategory = new FileCategory();
        fileCategory.setExtension(organization.getOrganization());
        fileCategory.setOrganization(organization.getOrganization());
        fileCategory.setName(organization.getWhatsAppMediaFolder() + "/" + phoneNumber + "/" + ldt.toString());
        fileCategory.setRoot(false);
        fileCategoryList.add(fileCategory);

        fileCategory = new FileCategory();
        fileCategory.setExtension(organization.getOrganization());
        fileCategory.setOrganization(organization.getOrganization());
        fileCategory.setName(organization.getWhatsAppMediaFolder() + "/" + phoneNumber + "/" + ldt.toString() + "/send");
        fileCategory.setRoot(false);
        fileCategoryList.add(fileCategory);

        fileCategory = new FileCategory();
        fileCategory.setExtension(organization.getOrganization());
        fileCategory.setOrganization(organization.getOrganization());
        fileCategory.setName(organization.getWhatsAppMediaFolder() + "/" + phoneNumber + "/" + ldt.toString() + "/receive");
        fileCategory.setRoot(false);
        fileCategoryList.add(fileCategory);

        //System.out.println("Created file categories, now saving...");

        try {
            fileCategoryRepository.saveAll(fileCategoryList);
            //System.out.println("File categories saved successfully.");
        } catch (Exception e) {
            System.err.println("Error saving file categories: " + e.getMessage());
            e.printStackTrace();
        }

        return fileCategoryList;
    }
}
