package com.mylinehub.crm.mapper;

import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.dto.CustomerDTO;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "firstname", source = "firstname")
	@Mapping(target = "lastname", source = "lastname")
	@Mapping(target = "zipCode", source = "zipCode")
	@Mapping(target = "city", source = "city")
	@Mapping(target = "email", source = "email")
	@Mapping(target = "phoneNumber", source = "phoneNumber")
	@Mapping(target = "description", source = "description")
	@Mapping(target = "business", source = "business")
	@Mapping(target = "country", source = "country")
	@Mapping(target = "phoneContext", source = "phoneContext")
	@Mapping(target = "domain", source = "domain")
//	@Mapping(target = "whatsApp_id", source = "whatsApp_id")
//	@Mapping(target = "whatsApp_wa_id", source = "whatsApp_wa_id")
//	@Mapping(target = "whatsAppDisplayPhoneNumber", source = "whatsAppDisplayPhoneNumber")
//	@Mapping(target = "whatsAppPhoneNumberId", source = "whatsAppPhoneNumberId")
//	@Mapping(target = "whatsAppProjectId", source = "whatsAppProjectId")
	@Mapping(target = "coverted", source = "coverted")
	@Mapping(target = "datatype", source = "datatype")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "remindercalling", source = "remindercalling")
	@Mapping(target = "cronremindercalling", source = "cronremindercalling")
	@Mapping(target = "iscalledonce", source = "iscalledonce")
	@Mapping(target = "pesel", source = "pesel")
	@Mapping(target = "imageName", source = "imageName")
	@Mapping(target = "imageType", source = "imageType")
	@Mapping(target = "imageData", source = "imageData")
	@Mapping(target = "lastConnectedExtension", source = "lastConnectedExtension")
	@Mapping(target = "autoWhatsAppAIReply", source = "autoWhatsAppAIReply")
	@Mapping(target = "firstWhatsAppMessageIsSend", source = "firstWhatsAppMessageIsSend")
	@Mapping(target = "preferredLanguage", source = "preferredLanguage")
	@Mapping(target = "secondPreferredLanguage", source = "secondPreferredLanguage")
	@Mapping(target = "updatedByAI", source = "updatedByAI")
	@Mapping(target = "propertyInventory", source = "propertyInventory")
	@Mapping(target = "franchiseInventory", source = "franchiseInventory")
    CustomerDTO mapCustomersToDto(Customers customers);
	
	@Mapping(target = "firstname", source = "firstname")
	@Mapping(target = "lastname", source = "lastname")
	@Mapping(target = "zipCode", source = "zipCode")
	@Mapping(target = "city", source = "city")
	@Mapping(target = "email", source = "email")
	@Mapping(target = "phoneNumber", source = "phoneNumber")
	@Mapping(target = "description", source = "description")
	@Mapping(target = "business", source = "business")
	@Mapping(target = "country", source = "country")
	@Mapping(target = "phoneContext", source = "phoneContext")
	@Mapping(target = "domain", source = "domain")
//	@Mapping(target = "whatsApp_id", source = "whatsApp_id")
//	@Mapping(target = "whatsApp_wa_id", source = "whatsApp_wa_id")
//	@Mapping(target = "whatsAppDisplayPhoneNumber", source = "whatsAppDisplayPhoneNumber")
//	@Mapping(target = "whatsAppPhoneNumberId", source = "whatsAppPhoneNumberId")
//	@Mapping(target = "whatsAppProjectId", source = "whatsAppProjectId")
	@Mapping(target = "coverted", source = "coverted")
	@Mapping(target = "datatype", source = "datatype")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "remindercalling", source = "remindercalling")
	@Mapping(target = "cronremindercalling", source = "cronremindercalling")
	@Mapping(target = "iscalledonce", source = "iscalledonce")
	@Mapping(target = "pesel", source = "pesel")
	@Mapping(target = "imageName", source = "imageName")
	@Mapping(target = "imageType", source = "imageType")
	@Mapping(target = "imageData", source = "imageData")
	@Mapping(target = "lastConnectedExtension", source = "lastConnectedExtension")
	@Mapping(target = "autoWhatsAppAIReply", source = "autoWhatsAppAIReply")
	@Mapping(target = "firstWhatsAppMessageIsSend", source = "firstWhatsAppMessageIsSend")
	@Mapping(target = "preferredLanguage", source = "preferredLanguage")
	@Mapping(target = "secondPreferredLanguage", source = "secondPreferredLanguage")
	@Mapping(target = "updatedByAI", source = "updatedByAI")
	@Mapping(target = "propertyInventory", source = "propertyInventory")
	@Mapping(target = "franchiseInventory", source = "franchiseInventory")
	Customers mapDTOtoCustomers(CustomerDTO customerDTO);
	
	@AfterMapping
	default void linkInventories(@MappingTarget Customers customer) {
	    if (customer == null) return;

	    if (customer.getPropertyInventory() != null) {
	        customer.getPropertyInventory().setCustomer(customer);
	    }

	    if (customer.getFranchiseInventory() != null) {
	        customer.getFranchiseInventory().setCustomer(customer);
	    }
	}
}
