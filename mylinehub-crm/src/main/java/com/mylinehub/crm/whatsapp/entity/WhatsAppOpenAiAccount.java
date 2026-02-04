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
@Table(name = "openAiAccount",
indexes = {
		  @Index(name = "openai_project_id_Index", columnList = "id"),
		  @Index(name = "openai_project_Organization_Index", columnList = "organization"),
		})
public class WhatsAppOpenAiAccount {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "openai_project_sequence"
    )
    @SequenceGenerator(
            name="openai_project_sequence",
            sequenceName = "openai_project_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
    private String key;
    private String adminKey;
    private String projectID;
    private String assistantID;
    private String email;  
    private String chatBotName;
    private String chatBotAccess;
    private String clientSecret;
    private String organization;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
}