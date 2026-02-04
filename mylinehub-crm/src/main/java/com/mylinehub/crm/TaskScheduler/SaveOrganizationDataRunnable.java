package com.mylinehub.crm.TaskScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.repository.OrganizationRepository;

import lombok.Data;

@Data
public class SaveOrganizationDataRunnable   implements Runnable{

	OrganizationRepository organizationRepository;
	String jobId;
	
	@Override
	public void run() {
	    System.out.println("SaveOrganizationDataRunnable");

	    Map<String, Organization> allOrganizations =
	            OrganizationData.workWithAllOrganizationData(null, null, "get", null);

	    if (allOrganizations == null || allOrganizations.isEmpty()) {
	        System.out.println("No organizations found in source data.");
	        return;
	    }

	    // 1 Collect all organization IDs from the fetched data
	    List<Long> orgIds = allOrganizations.values().stream()
	            .map(Organization::getId)
	            .filter(Objects::nonNull)
	            .toList();

	    if (orgIds.isEmpty()) {
	        System.out.println("No valid organization IDs found to process.");
	        return;
	    }

	    // 2 Fetch all existing organizations in one DB call
	    List<Organization> existingOrgs = organizationRepository.findAllByIdIn(orgIds);

	    if (existingOrgs.isEmpty()) {
	        System.out.println("No matching organizations found in DB.");
	        return;
	    }

	    // 3 Map existing orgs by ID for quick look-up
	    Map<Long, Organization> existingOrgMap = existingOrgs.stream()
	            .collect(Collectors.toMap(Organization::getId, org -> org));

	    // 4 Prepare updated list
	    List<Organization> toSave = new ArrayList<>();

	    for (Organization newOrg : allOrganizations.values()) {
	        Organization existingOrg = existingOrgMap.get(newOrg.getId());
	        if (existingOrg != null) {

	            //  Update all fields
	            existingOrg.setOrganization(newOrg.getOrganization());
	            existingOrg.setBusinessIdentificationNumber(newOrg.getBusinessIdentificationNumber());
	            existingOrg.setEmail(newOrg.getEmail());
	            existingOrg.setPhoneNumber(newOrg.getPhoneNumber());
	            existingOrg.setNatureOfBusiness(newOrg.getNatureOfBusiness());
	            existingOrg.setAddress(newOrg.getAddress());
	            existingOrg.setPhoneContext(newOrg.getPhoneContext());
	            existingOrg.setTimezone(newOrg.getTimezone());
	            existingOrg.setCostCalculation(newOrg.getCostCalculation());
	            existingOrg.setTotalCalls(newOrg.getTotalCalls());
	            existingOrg.setCallingTotalAmountSpend(newOrg.getCallingTotalAmountSpend());
	            existingOrg.setCallingTotalAmountLoaded(newOrg.getCallingTotalAmountLoaded());
	            existingOrg.setCallLimit(newOrg.getCallLimit());
	            existingOrg.setTotalWhatsAppMessagesAmount(newOrg.getTotalWhatsAppMessagesAmount());
	            existingOrg.setTotalWhatsAppMessagesAmountSpend(newOrg.getTotalWhatsAppMessagesAmountSpend());
	            existingOrg.setWhatsAppMessageLimit(newOrg.getWhatsAppMessageLimit());
	            existingOrg.setAllowWhatsAppAutoAIMessage(newOrg.isAllowWhatsAppAutoAIMessage());
	            existingOrg.setAllowWhatsAppAutoMessage(newOrg.isAllowWhatsAppAutoMessage());
	            existingOrg.setAllowWhatsAppCampaignMessage(newOrg.isAllowWhatsAppCampaignMessage());
	            existingOrg.setRagSet(newOrg.isRagSet());
	            existingOrg.setTrunkNamesPrimary(newOrg.getTrunkNamesPrimary());
	            existingOrg.setTrunkNamesSecondary(newOrg.getTrunkNamesSecondary());
	            existingOrg.setUseSecondaryAllotedLine(newOrg.isUseSecondaryAllotedLine());
	            existingOrg.setDomain(newOrg.getDomain());
	            existingOrg.setSecondDomain(newOrg.getSecondDomain());
	            existingOrg.setPhoneTrunk(newOrg.getPhoneTrunk());
	            existingOrg.setMenuAccess(newOrg.getMenuAccess());
	            existingOrg.setAllowedUploadInMB(newOrg.getAllowedUploadInMB());
	            existingOrg.setCurrentUploadInMB(newOrg.getCurrentUploadInMB());
	            existingOrg.setAllowedEmbeddingConversion(newOrg.getAllowedEmbeddingConversion());
	            existingOrg.setConsumedEmbeddingConversion(newOrg.getConsumedEmbeddingConversion());
	            existingOrg.setAllowedUsers(newOrg.getAllowedUsers());
	            existingOrg.setEnableFileUpload(newOrg.isEnableFileUpload());
	            existingOrg.setEnableEmployeeCreation(newOrg.isEnableEmployeeCreation());
	            existingOrg.setEnableCalling(newOrg.isEnableCalling());
	            existingOrg.setEnableInternalMessaging(newOrg.isEnableInternalMessaging());
	            existingOrg.setEnableWhatsAppMessaging(newOrg.isEnableWhatsAppMessaging());
	            existingOrg.setProtocol(newOrg.getProtocol());
	            existingOrg.setSipPort(newOrg.getSipPort());
	            existingOrg.setSipPath(newOrg.getSipPath());
	            existingOrg.setPriLineType(newOrg.getPriLineType());
	            existingOrg.setWhatsAppMediaFolder(newOrg.getWhatsAppMediaFolder());
	            existingOrg.setWhatsAppMediaFolderImage(newOrg.getWhatsAppMediaFolderImage());
	            existingOrg.setWhatsAppMediaFolderImageType(newOrg.getWhatsAppMediaFolderImageType());
	            existingOrg.setWhatsAppMediaFolderImageName(newOrg.getWhatsAppMediaFolderImageName());
	            existingOrg.setActivated(newOrg.isActivated());
	            existingOrg.setLastRechargedOn(newOrg.getLastRechargedOn());

	            toSave.add(existingOrg);
	        }
	    }

	    // 5 Save all updated orgs in one go
	    if (!toSave.isEmpty()) {
	        organizationRepository.saveAll(toSave);
	        System.out.println("Updated " + toSave.size() + " organizations successfully.");
	    } else {
	        System.out.println("No existing organizations to update.");
	    }
	}

	
	

}
