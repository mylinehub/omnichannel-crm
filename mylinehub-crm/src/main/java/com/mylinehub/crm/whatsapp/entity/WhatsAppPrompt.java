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
import javax.persistence.UniqueConstraint;

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
@Table(name = "WhatsAppPrompt",
uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_whatsapp_prompt_org_category",
            columnNames = {"organization", "category"}
        )
    },
indexes = {
		  @Index(name = "whatsapp_prompt_id_Index", columnList = "id"),
		  @Index(name = "whatsapp_prompt_Organization_Index", columnList = "organization"),
		  @Index(name = "whatsapp_prompt_category_Organization_Index", columnList = "category"),
		  @Index(name = "whatsapp_prompt_active_Organization_Index", columnList = "active"),
		  @Index(name= "whatsapp_prompt_whatsAppPhoneNumberId_Index",columnList = "whatsAppPhoneNumberId")
		})
public class WhatsAppPrompt {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "whatsapp_prompt_sequence"
    )
    @SequenceGenerator(
            name="whatsapp_prompt_sequence",
            sequenceName = "whatsapp_prompt_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "whatsAppPhoneNumberId")
    private WhatsAppPhoneNumber whatsAppPhoneNumber;
    
    @Column(columnDefinition="TEXT")
    private String prompt;
    
    @Column( unique = true)
    private String category;
    
    private boolean active = true;
    private String delimiter;
    private String organization;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
}