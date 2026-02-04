package com.mylinehub.crm.whatsapp.dto;


import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppFlattenMessageDTO {

	@Builder.Default
	String whatsAppBusinessId ="";
	@Builder.Default
	String field="";
	@Builder.Default
	String messagingProduct="";
	@Builder.Default
	String metaData="";
	@Builder.Default
	String whatsAppDisplayPhoneNumber="";
	@Builder.Default
	String whatsAppPhoneNumberId="";
	@Builder.Default
	String name="";
	@Builder.Default
	String whatsApp_wa_id="";
	@Builder.Default
	String errorCode="";
	@Builder.Default
	String errorTitle="";
	@Builder.Default
	String messageFrom="";
	@Builder.Default
	String messageId="";
	
	//****************** Message Level Check *****************
	//Messages
	@Builder.Default
	String messageTimestamp ="";
	@Builder.Default
	String messageType="";
	@Builder.Default
	String messageContext="";
	@Builder.Default
	String messageIdentity="";
	@Builder.Default
	String messageInteractive="";
	@Builder.Default
	String messageText="";
	@Builder.Default
	String messageErrors="";
	@Builder.Default
	String messageSystem="";
	@Builder.Default
	String messageButton="";
	@Builder.Default
	String messageReferral="";
	@Builder.Default
	String messageReaction="";
	@Builder.Default
	String messageLocation="";
	@Builder.Default
	String messageOrder="";
	@Builder.Default
	String messageContacts="";
	@Builder.Default
	String messageMedia="";
	
	//****************** Statuses Level Check *****************
	@Builder.Default
	String statusesId="";
	@Builder.Default
	String statusesRecipientId="";
	@Builder.Default
	String statusesStatus="";
	@Builder.Default
	String statusesTimestamp="";
	@Builder.Default
	String statusesType="";
	@Builder.Default
	String statusesConversation="";
	@Builder.Default
	String statusesPricing="";
	@Builder.Default
	String statusesErrors="";
	@Builder.Default
	String payment="";

	private Instant lastUpdatedOn;
	private Instant createdOn;
}
