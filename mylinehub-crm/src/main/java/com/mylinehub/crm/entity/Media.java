package com.mylinehub.crm.entity;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "Media",
uniqueConstraints= {
@UniqueConstraint(columnNames={"name","organization"})}
,
indexes = {
		  @Index(name = "mediaid_Index", columnList = "id"),
		  @Index(name= "media_organization_Index",columnList = "organization"),
		  @Index(name= "media_WhatsAppPhoneNumberId_Index",columnList = "WhatsAppPhoneNumberId"),
		  @Index(name= "media_fileCategoryId_Index",columnList = "fileCategoryId"),
		  @Index(name= "media_Name_Index",columnList = "name")
		})
public class Media {

	  @Id
	    @GeneratedValue(
	            strategy = GenerationType.SEQUENCE,
	            generator = "mediasequence"
	    )
	    @SequenceGenerator(
	            name="mediasequence",
	            sequenceName = "whatsappmediasequence",
	            allocationSize = 1,
	            initialValue = 100
	    )
	    @Column(nullable = false)
	    private Long id;
	  
	    @ManyToOne
	    @JoinColumn(name = "fileCategoryId")
	    private FileCategory fileCategory;
	  
	    @ManyToOne
	    @JoinColumn(name = "WhatsAppPhoneNumberId")
	    private WhatsAppPhoneNumber whatsAppPhoneNumber;

	    //In case of whats app extension is organization
	    private String extension;
	    
	    //Only used in case of whats app. Or else extension and fromExtension are same. It is because in case of whats app extension is filled with organization.
	    //This is because any employee can upload document and it should be visible to everyone.
	    private String fromExtension;
	    private String name;
	    private String type;
	    private long size;
	    private String caption;
	    private String organization;
	    private String mediaUploadModule;
	    
	    private String error;
	    
	    @Column(unique = true)
	    private String whatsAppMediaId;
	    private String whatsAppMediaType;
	    private Date whatsAppUploadDate;
	    
	    private String whatsAppLink;
	    private String sha256;

	    private boolean externalPartyUploadSuccessful;
	    private boolean received;
	    
	    @Column(updatable = false)
	    @CreationTimestamp
	    private Instant createdOn;
	    
	    @UpdateTimestamp
	    private Instant lastUpdatedOn;
}