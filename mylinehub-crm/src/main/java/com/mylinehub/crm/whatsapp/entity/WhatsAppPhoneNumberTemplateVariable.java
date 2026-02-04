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
@Table(name = "WhatsAppPhoneNumberTemplateVariable",
indexes = {
		  @Index(name = "whatsapp_phonenumber_template_variable_id_Index", columnList = "id"),
		  @Index(name = "whatsapp_phonenumber_template_variable_Organization_Index", columnList = "organization"),
		  @Index(name = "whatsapp_phonenumber_template_variable_order_Index", columnList = "orderNumber"),
		  @Index(name= "whatsapp_phonenumber_template_variable_whatsAppPhoneNumberTemplateId_Index",columnList = "whatsAppPhoneNumberTemplateId")
		})
public class WhatsAppPhoneNumberTemplateVariable {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "whatsapp_phonenumber_template_variable_sequence"
    )
    @SequenceGenerator(
            name="whatsapp_phonenumber_template_variable_sequence",
            sequenceName = "whatsapp_phonenumber_template_variable_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "whatsAppPhoneNumberTemplateId")
    private WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplates;
    
    private int orderNumber;
    private String variableName;
    private String variableType;
    private String variableHeaderType;
    private String mediaID;
    private String mediaUrl;
    private String fileName;
    private String caption;
    private String mediaSelectionType;
    private String organization;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
}