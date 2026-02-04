package com.mylinehub.crm.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.mylinehub.crm.converter.StringListJsonConverter;

import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Table(name = "organization",indexes = {
		  @Index(name = "Organization_Index", columnList = "organization"),
          @Index(name = "Organization_createdOn_Index", columnList = "createdOn"),
		  @Index(name = "Organization_businessIdentificationNumber_Index", columnList = "businessIdentificationNumber")
		})
public class Organization {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SequenceGenerator(
            name = "organization_sequence",
            sequenceName = "organization_sequence",
            allocationSize = 1)
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "organization_sequence")
    @Column(name = "Org_ID")
    private Long id;
  
	@Column(unique = true)
    private String organization;
    
	@Column(unique = true)
	private String businessIdentificationNumber;
	
	private String email;
	private String phoneNumber;
	
	private String natureOfBusiness;
	private String address;
	
    private String phoneContext;
    
    private TimeZone timezone;
    
  //Will define type of costing for the employee
    @Column(columnDefinition = "varchar(255) default 'METERED'")
    private String costCalculation;

    @Column(columnDefinition = "integer default 0")
    private int totalCalls;
    
    @Column(columnDefinition="double precision default '0'")
    private double callingTotalAmountLoaded;
    
    @Column(columnDefinition="double precision default '0'")
    private double callingTotalAmountSpend;
    
    @Column(columnDefinition = "integer default 1000")
    private int callLimit;

    @Column(columnDefinition="double precision default '600'")
    private double totalWhatsAppMessagesAmount;
    
    @Column(columnDefinition="double precision default '0'")
    private double totalWhatsAppMessagesAmountSpend;
    
    @Column(columnDefinition = "integer default 1000")
    private int whatsAppMessageLimit;
    
    @Column(columnDefinition = "boolean default true")
    private boolean allowWhatsAppAutoAIMessage;
    
    @Column(columnDefinition = "boolean default true")
    private boolean allowWhatsAppAutoMessage;
    
    @Column(columnDefinition = "boolean default true")
    private boolean allowWhatsAppCampaignMessage;
    
    @Column(columnDefinition = "boolean default false")
    private boolean ragSet;
    
    private String trunkNamesPrimary;
    private String trunkNamesSecondary;
    
    @Column(columnDefinition = "boolean default false")
    private boolean useSecondaryAllotedLine;
    
    private String domain;
    private String secondDomain;
    
    //it is name of trunk which should be put here. Then it will work otherwise it will not
    private String phoneTrunk;

    @Column(columnDefinition="TEXT")
    private String menuAccess;
    
    @Column(columnDefinition="double precision default '50'")
    private double allowedUploadInMB;
    
    @Column(columnDefinition="double precision default '0'")
    private double currentUploadInMB;;
    
    @Column(columnDefinition = "integer default 1")
    private int allowedEmbeddingConversion;
    
    @Column(columnDefinition = "integer default 0")
    private int consumedEmbeddingConversion;
    
    //For 10 users put in 21 = x (which is 10) + 10 support users + 1 extra user for mylinehub
    @Column(columnDefinition = "integer default 21")
    private int allowedUsers;
    
    @Column(columnDefinition = "boolean default false")
    private boolean enableFileUpload;
    
    @Column(columnDefinition = "boolean default false")
    private boolean enableEmployeeCreation;
    
    @Column(columnDefinition = "boolean default false")
    private boolean enableCalling;
    
    @Column(columnDefinition = "boolean default false")
    private boolean enableInternalMessaging;
    
    @Column(columnDefinition = "boolean default true")
    private boolean enableWhatsAppMessaging;
    
    @Column(columnDefinition = "varchar(255) default 'PJSIP//'")
    private String protocol;
    
    @Column(columnDefinition = "integer default 5060")
    private int sipPort;
    
    @Column(columnDefinition = "varchar(255) default '//'")
    private String sipPath;
    
    @Column(columnDefinition = "varchar(255) default 'indirectToFreePBX'")
    private String priLineType;
    
    @Column(columnDefinition = "varchar(255) default 'WhatsApp'")
    private String whatsAppMediaFolder;
    
    @Column(columnDefinition = "varchar(255) default 'src/main/resources/images/icon/whatsapp.png'")
    private String whatsAppMediaFolderImage;
    
    @Column(columnDefinition = "varchar(255) default 'image/png'")
    private String whatsAppMediaFolderImageType;
    
    @Column(columnDefinition = "varchar(255) default 'whatsapp.png'")
    private String whatsAppMediaFolderImageName;
    
    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "ari_application", columnDefinition = "TEXT")
    private List<String> ariApplication;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "ari_application_domain", columnDefinition = "TEXT")
    private List<String> ariApplicationDomain;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "ari_application_port", columnDefinition = "TEXT")
    private List<String> ariApplicationPort;

    
    //For 10 users put in 21 = x (which is 10) + 10 support users + 1 extra user for mylinehub
    @Column(columnDefinition = "integer default 10")
    private int aiCallChargeAmount;
    
    @Column(columnDefinition = "varchar(255) default 'call'")
    private String aiCallChargeType;
    
    @Column(columnDefinition = "varchar(255) default ''")
    private String simSelector;  
    
    @Column(columnDefinition = "boolean default true")
    private boolean simSelectorRequired;
    
    @Column(columnDefinition = "boolean default true")
    private boolean activated;

    private Date lastRechargedOn;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
   
}
