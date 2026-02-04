package com.mylinehub.crm.entity;

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
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.mylinehub.crm.entity.dto.ChatKeyValueListDTO;

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
		  @Index(name = "ChatHistory_Organization_Index", columnList = "organization"),
		  @Index(name = "ChatHistory_extensionMain_Index", columnList = "extensionMain"),
		  @Index(name = "ChatHistory_extensionWith_Index", columnList = "extensionWith"),
		  @Index(name = "ChatHistory_isDeleted_Index", columnList = "isDeleted"),
		})
@TypeDef(name="jsonb", typeClass= JsonBinaryType.class)
public class ChatHistory {
	
	@Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "chat_history_sequence"
    )
    @SequenceGenerator(
            name="chat_history_sequence",
            sequenceName = "chat_history_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(name = "History_ID", nullable = false, unique = true)
    private Long id;
	public String organization;
	@Column(nullable = false)
    public String extensionMain;
	@Column(nullable = false)
    public String extensionWith; 
	@Column(columnDefinition = "integer default -1")
	public int lastReadIndex;
	
//	@Column(columnDefinition = "jsonb", nullable = false)
	@Type(type = "jsonb")
    @Column(name = "chats", columnDefinition = "jsonb", nullable = false)
    public ChatKeyValueListDTO chats;
	
	@Column(columnDefinition = "boolean default false")
    public boolean isDeleted;
	
	public Date lastUpdateTime;
	
	@Column(updatable = false)
	@CreationTimestamp
	private Instant createdOn;
	    
}



