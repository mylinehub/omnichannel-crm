package com.mylinehub.crm.whatsapp.entity;

import java.time.Instant;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Table(indexes = {
		  @Index(name = "WhatsAppChatHistory_Organization_Index", columnList = "organization"),
		  @Index(name = "WhatsAppChatHistory_extensionMain_Index", columnList = "phoneNumberMain"),
		  @Index(name = "WhatsAppChatHistory_extensionWith_Index", columnList = "phoneNumberWith"),
		  @Index(name = "WhatsAppChatHistory_whatsAppMessageId_Index", columnList = "whatsAppMessageId"),
		  @Index(name = "WhatsAppChatHistory_deleteSelf_Index", columnList = "deleteSelf"),
//		  @Index(name = "WhatsAppChatHistory_issend_Index", columnList = "send"),
//		  @Index(name = "WhatsAppChatHistory_isdelivered_Index", columnList = "delivered"),
//		  @Index(name = "WhatsAppChatHistory_isread_Index", columnList = "read"),
		  @Index(name = "WhatsAppChatHistory_isfailed_Index", columnList = "failed"),
		  @Index(name = "WhatsAppChatHistory_isdeleted_Index", columnList = "deleted"),
		})
@TypeDef(name="jsonb", typeClass= JsonBinaryType.class)
public class WhatsAppChatHistory {

	@Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "whatsapp_chat_history_sequence"
    )
    @SequenceGenerator(
            name="whatsapp_chat_history_sequence",
            sequenceName = "whatsapp_chat_history_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(name = "id", nullable = false, unique = true)
    private Long id;
	public String organization;
	@Column(nullable = false)
    public String phoneNumberMain;
	@Column(nullable = false)
    public String phoneNumberWith; 

	public String fromExtension;
	public String fromName;
	public boolean readSelf;
	
	public String fromTitle;
	public String messageOrigin;
	public String conversationId;
	
	//Below is actually used as context message Id. It is named as previous message ID.
	public String previousMessageId;
	
	@Column(unique = true)
	public String whatsAppMessageId;
	public boolean inbound;
	public boolean outbound;
	public boolean sent;
	public boolean delivered;
	public boolean read;
	public boolean failed;
	public boolean deleted;
	public String messageType;
	
	@Column(columnDefinition = "TEXT")
	public String messageString;
	
	public String whatsAppMediaId;
	//Say a message type is audio , then blob type can be either of acc , mp4, mpeg etc.
	public String blobType;
	public String fileName;
	public String fileSizeInMB;
	
	public String whatsAppError;
	public String openAIAssistantName;
	public String openAIAssistantThread;
	
	@Column(columnDefinition = "boolean default false")
    public boolean deleteSelf;
	@Column(columnDefinition = "boolean default false")
	public boolean whatsAppActualBillable;
	
	public Date lastUpdateTime;

	private Date createdOn;
}
