package com.mylinehub.crm.whatsapp.entity;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "WhatsAppFlattenMessage",
indexes = {
		  @Index(name = "whatsapp_flattenmessageid_Index", columnList = "id"),
		  @Index(name= "whatsapp_whatsAppMessageId_Index",columnList = "messageId"),
		})
public class WhatsAppFlattenMessage {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "whatsapp_flattenmessagesequence"
    )
    @SequenceGenerator(
            name="whatsapp_flattenmessagesequence",
            sequenceName = "whatsapp_flattenmessagesequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;


	String whatsAppBusinessId;
	
	String field;
	
	String messagingProduct;
	
	@Column(columnDefinition="TEXT")
	String metaData;
	
	String whatsAppDisplayPhoneNumber;
	
	String whatsAppPhoneNumberId;
	
	String name;
	
	String whatsApp_wa_id;
	
	String errorCode;
	
	@Column(columnDefinition = "TEXT")
	String errorTitle;
	
	String messageFrom;
	
	String messageId;
	
	String messageTimestamp;
	
	String messageType;
	
	@Column(columnDefinition = "TEXT")
	String messageContext;
	@Column(columnDefinition = "TEXT")
	String messageIdentity;
    @Column(columnDefinition = "TEXT")
	String messageInteractive;
    @Column(columnDefinition = "TEXT")
	String messageText;
    @Column(columnDefinition = "TEXT")
	String messageErrors;
    @Column(columnDefinition = "TEXT")
	String messageSystem;
    @Column(columnDefinition = "TEXT")
	String messageButton;
    @Column(columnDefinition = "TEXT")
	String messageReferral;
    @Column(columnDefinition = "TEXT")
	String messageReaction;
    @Column(columnDefinition = "TEXT")
	String messageLocation;
    @Column(columnDefinition = "TEXT")
	String messageOrder;
    @Column(columnDefinition = "TEXT")
	String messageContacts;
    @Column(columnDefinition = "TEXT")
	String messageMedia;

	//Statutes
	String statusesId;
	
	String statusesRecipientId;
	
	String statusesStatus;
	
	String statusesTimestamp;
	
	String statusesType;
	
    @Column(columnDefinition = "TEXT")
	String statusesConversation;
    @Column(columnDefinition = "TEXT")
	String statusesPricing;
    @Column(columnDefinition = "TEXT")
	String statusesErrors;
    @Column(columnDefinition = "TEXT")
	String payment;
    
	@UpdateTimestamp
    private Instant lastUpdatedOn;
	 
	private Instant createdOn;
	
}