package com.mylinehub.crm.whatsapp.requests;

import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Product;
import com.mylinehub.crm.entity.Purchases;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendWhatsAppControllerRequest {

	String messagingProduct;
	String phoneNumberMain;
	String phoneNumberWith;
	String previousMessageId;
	String messageType;
	String id;
	String link;
	String caption;
	String fileName;
	String languageCode;
	boolean previewURL;
	String textBody;
	String sub_type;
	String index;
	String version;
	String phoneNumberID;
	String token;
	
	Long templateId;
	String customerPhoneNumber;
	Long purchaseId;
	String organization;
	
	WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplates;
//	Organization organization;
	Customers customer;
	Product product;
	Purchases purchase;
}
