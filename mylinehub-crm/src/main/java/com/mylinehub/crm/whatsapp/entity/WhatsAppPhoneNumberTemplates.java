package com.mylinehub.crm.whatsapp.entity;

import java.time.Instant;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.mylinehub.crm.entity.Product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "WhatsAppPhoneNumberTemplates",
indexes = {
		  @Index(name = "whatsapp_phonenumber_template_id_Index", columnList = "id"),
		  @Index(name = "whatsapp_phonenumber_template_Organization_Index", columnList = "organization"),
		  @Index(name= "whatsapp_phonenumber_template_whatsAppPhoneNumberId_Index",columnList = "whatsAppPhoneNumberId")
		})
public class WhatsAppPhoneNumberTemplates {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "whatsapp_phonenumber_template_sequence"
    )
    @SequenceGenerator(
            name="whatsapp_phonenumber_template_sequence",
            sequenceName = "whatsapp_phonenumber_template_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "whatsAppPhoneNumberId")
    private WhatsAppPhoneNumber whatsAppPhoneNumber;
    
    private String templateName;
    private String conversationType;
    private String organization;
    private String mediaPath;
    private String mediaType;
    private String mediaId;
    private Date mediaIdLastUpdatedDate;
    private String languageCode;
    private String currency;
    private boolean followOrder;
    
    @OneToOne
    @JoinColumn(name = "productId")
    private Product product;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
}