package com.mylinehub.crm.whatsapp.entity;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "WhatsAppPromptVariables",
indexes = {
		  @Index(name = "whatsapp_prompt_variabes_id_Index", columnList = "id"),
		  @Index(name = "whatsapp_prompt_variabes_Organization_Index", columnList = "organization"),
		  @Index(name = "whatsapp_prompt_active_variabes_Organization_Index", columnList = "active"),
		  @Index(name= "whatsapp_prompt_whatsAppPromptId_Index",columnList = "whatsAppPromptId")
		})
public class WhatsAppPromptVariables {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "whatsapp_prompt_variabes_sequence"
    )
    @SequenceGenerator(
            name="whatsapp_prompt_variabes_sequence",
            sequenceName = "whatsapp_prompt_variabes_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "whatsAppPromptId")
    private WhatsAppPrompt whatsAppPrompt;
    
    private String label;
    private String description;
    private boolean active = true;
    private String organization;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
}