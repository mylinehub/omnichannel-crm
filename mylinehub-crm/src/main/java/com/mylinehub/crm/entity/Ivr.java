package com.mylinehub.crm.entity;

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
@Table(name = "IVR",
indexes = {
		  @Index(name = "IVR_PhoneContext_Index", columnList = "phoneContext"),
		  @Index(name = "IVR_Organization_Index", columnList = "organization"),
		  @Index(name = "IVR_extension_Index", columnList = "extension"),
		  @Index(name = "IVR_isactive_Index", columnList = "isactive"),
		})
public class Ivr {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ivr_sequence"
    )
    @SequenceGenerator(
            name="ivr_sequence",
            sequenceName = "ivr_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
    
    private  String phoneContext;
    private  String organization;
    
    @Column(unique = true)
    private  String extension;

    private  String name;
    
    @Column(columnDefinition = "varchar(255) default 'PJSIP\'")
    private  String protocol;
    
    private  String domain;
    
    @Column(columnDefinition = "boolean default true")
    private boolean isactive;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    Ivr( String phoneContext, String organization, String extension, String domain)
    {
    	this.phoneContext=phoneContext;
    	this.organization=organization;
    	this.extension=extension;
    	this.domain=domain;
    }
    
}