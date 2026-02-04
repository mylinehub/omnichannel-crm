package com.mylinehub.crm.entity;

import com.mylinehub.crm.enums.USER_ROLE;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Table(name = "SYS_USER",indexes = {
		  @Index(name = "Employee_Organization_Index", columnList = "organization"),
		  @Index(name = "Employee_extension_Index", columnList = "extension"),
		  @Index(name = "Employee_userRole_Index", columnList = "userRole"),
		  @Index(name = "Employee_phoneContext_Index", columnList = "phoneContext"),
		  @Index(name = "Employee_costCalculation_Index", columnList = "costCalculation"),
		  @Index(name = "Employee_allotednumber1_Index", columnList = "allotednumber1"),
		  @Index(name = "Employee_allotednumber2_Index", columnList = "allotednumber2"),
		  @Index(name = "Employee_email_Index", columnList = "email"),
		  @Index(name = "Employee_sex_Index", columnList = "sex"),
		  @Index(name = "Employee_phoneNumber_Index", columnList = "phonenumber"),
		  @Index(name = "Employee_IS_ENABLED_Index", columnList = "IS_ENABLED"),
		})
public class Employee implements UserDetails {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1)
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence")
    @Column(name = "USER_ID")
    private Long id;
    private String firstName;
    private String lastName;
    
    @Column(unique = true)
    private String email;
    
    private String password;
    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "USER_ROLE")
    private USER_ROLE userRole;
    
    @Column(unique = true)
    private String pesel;
    private String sex;
    private Date birthdate;
    private Double salary;
    @Column(name = "IS_LOCKED")
    private Boolean isLocked = false;
    @Column(name = "IS_ENABLED")
    private Boolean isEnabled = true;
    
    //This means many employees can be into one department
    //Which means two employees can have same department code
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "DEPARTMENT_CODE")
    private Departments department;
 
    @Column(columnDefinition = "varchar(255) default 'from-internal'")
    private String phoneContext;
    
    //it is name of trunk which should be put here. Then it will work otherwise it will not
    @Column(columnDefinition = "varchar(255) default 'from-trunk'")
    private String phoneTrunk;
    
    private String organization;
    
    @Column(columnDefinition = "varchar(25) default '99'")
    private String extensionPrefix;
    
    @Column(unique = true)
    private String extension;
    
    @Column(columnDefinition = "varchar(25) default '99'")
    private String confExtensionPrefix;
    
    @Column(unique = true)
    private String confExtension;
    
    private String extensionpassword;
    private String domain;
    private String secondDomain;
    
    @Column(columnDefinition = "integer default 5060")
    private int sipPort;
    
    @Column(columnDefinition = "varchar(255) default '//'")
    private String sipPath;
    

   
    @Column(columnDefinition = "boolean default false")
    private boolean doNotDisturb;
    @Column(columnDefinition = "boolean default false")
    private boolean startVideoFullScreen;
    
    @Column(columnDefinition = "boolean default false")
    private boolean callWaiting;
    
    @Column(columnDefinition = "boolean default false")
    private boolean recordAllCalls;
    @Column(columnDefinition = "boolean default true")
    private boolean intercomPolicy;
    @Column(columnDefinition = "boolean default false")
    private boolean freeDialOption;
    @Column(columnDefinition = "boolean default true")
    private boolean textDictateOption;
    @Column(columnDefinition = "boolean default true")
    private boolean textMessagingOption;
    
    @Column(columnDefinition = "varchar(255) default 'PJSIP//'")
    private String protocol;
    
    @Column(columnDefinition = "integer default 0")
    private int totalparkedchannels;
    
    @Column(columnDefinition = "boolean default false")
    private boolean allowedToSwitchOffWhatsAppAI;
    
    private String parkedchannel1;
    private String parkedchannel2;
    private String parkedchannel3;
    private String parkedchannel4;
    private String parkedchannel5;
    private String parkedchannel6;
    private String parkedchannel7;
    private String parkedchannel8;
    private String parkedchannel9;
    private String parkedchannel10;
    
    private TimeZone timezone;
    
    //Will define type of costing for the employee
    @Column(columnDefinition = "varchar(255) default 'EXTERNAL'")
    private String type;
    
  //Will define type of costing for the employee
    @Column(columnDefinition = "varchar(255) default 'METERED'")
    private String costCalculation;
    
    private String amount;
    
    @Column(columnDefinition = "boolean default false")
    private boolean callonnumber;
    
    @Column(unique = true)
    private String phonenumber;
    
    private String provider1;
    
    @Column(columnDefinition = "boolean default false")
    private boolean useSecondaryAllotedLine;
    
    @Column(unique = true)
    private String allotednumber1;
    
    private String provider2;
    
    @Column(unique = true)
    private String allotednumber2;
    
    private String transfer_phone_1;
    private String transfer_phone_2;
    
    
    private String imageName;

    private String imageType;

    @Column(columnDefinition = "varchar(500)")
    private String imageData;
    @Column(columnDefinition = "bigint default 0")
    private Long imageSize;
    
    @Column(columnDefinition = "varchar(500)")
    private String iconImageData;
    @Column(columnDefinition = "bigint default 0")
    private Long iconImageSize;
    
    private String doc1ImageType;
    @Column(columnDefinition = "varchar(500)")
    private String governmentDocument1Data;
    private String governmentDocumentID1;
    @Column(columnDefinition = "bigint default 0")
    private Long doc1ImageSize;
    
    private String doc2ImageType;
    @Column(columnDefinition = "varchar(500)")
    private String governmentDocument2Data;
    private String governmentDocumentID2;
    @Column(columnDefinition = "bigint default 0")
    private Long doc2ImageSize;
    
    @Column(columnDefinition = "bigint default 0")
    private Long sizeMediaUploadInMB;
    
    @Column(columnDefinition = "varchar(255) default 'Light'")
    private String uiTheme; 
    
    @Column(columnDefinition = "boolean default false")
    private boolean autoAnswer; 

    @Column(columnDefinition = "boolean default false")
    private boolean autoConference; 
    
    @Column(columnDefinition = "boolean default false")
    private boolean autoVideo; 
    
    @Column(columnDefinition = "varchar(255) default 'Default'")
    private String micDevice; 
    @Column(columnDefinition = "varchar(255) default 'Default'")
    private String speakerDevice; 
    @Column(columnDefinition = "varchar(255) default 'Default'")
    private String videoDevice; 
    
    @Column(columnDefinition = "varchar(255) default 'Mirror'")
    private String videoOrientation; 
    @Column(columnDefinition = "varchar(255) default 'HD'")
    private String videoQuality; 
    @Column(columnDefinition = "varchar(255) default '30'")
    private String videoFrameRate; 
    
    @Column(columnDefinition = "varchar(255) default 'true'")
    private String autoGainControl; 
    @Column(columnDefinition = "varchar(255) default 'true'")
    private String echoCancellation;
    @Column(columnDefinition = "varchar(255) default 'true'")
    private String noiseSupression; 
    
    //Below can be XMPP as well. We have front end code but it is not yet integrated hence we keep it only SIP as of now.
    @Column(columnDefinition = "varchar(255) default 'SIP'")
    private String chatEngine;
    
    @Column(columnDefinition = "boolean default true")
    private boolean notificationDot; 
    
    @Column(columnDefinition = "varchar(255) default ''")
	public String lastConnectedCustomerPhone;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    //Retrieve all information for manager and admins
   // public Employee(Long id, String firstName,String lastName,String email,USER_ROLE userRole,String pesel,String sex,Date birthdate,Double salary,Departments department,String phoneContext,Boolean isLocked,Boolean isEnabled,String organization,String extension,String domain,String protocol,TimeZone timezone,String type,boolean callonnumber,boolean useSecondaryAllotedLine,String phonenumber,String provider1,String allotednumber1,String provider2,String allotednumber2,String costCalculation,String amount,String transfer_phone_1,String transfer_phone_2,String imageName,String imageType,String governmentDocumentID1,String governmentDocumentID2,int totalparkedchannels,String parkedchannel1,String parkedchannel2,String parkedchannel3,String parkedchannel4,String parkedchannel5,String parkedchannel6,String parkedchannel7,String parkedchannel8,String parkedchannel9,String parkedchannel10,boolean autoAnswer, boolean autoConference, boolean autoVideo, String micDevice, String speakerDevice, String videoDevice, String videoOrientation, String videoQuality, String videoFrameRate, String autoGainControl, String echoCancellation,String noiseSupression) {
   // public Employee(Long id, String firstName,String lastName,String email,USER_ROLE userRole,String pesel,String sex,Date birthdate,Double salary,Departments department,String phoneContext,Boolean isLocked,Boolean isEnabled,String organization,String extension,String domain,String protocol,TimeZone timezone,String type,boolean callonnumber,boolean useSecondaryAllotedLine,String phonenumber,String provider1,String allotednumber1,String provider2,String allotednumber2,String costCalculation,String amount,String transfer_phone_1,String transfer_phone_2,String imageName,String imageType,String governmentDocumentID1,String governmentDocumentID2,int totalparkedchannels,String parkedchannel1,String parkedchannel2,String parkedchannel3,String parkedchannel4,String parkedchannel5,String parkedchannel6,String parkedchannel7,String parkedchannel8,String parkedchannel9,String parkedchannel10, boolean recordAllCalls,boolean intercomPolicy,boolean freeDialOption,boolean textDictateOption,boolean textMessagingOption,boolean doNotDisturb,boolean startVideoFullScreen) {
      public Employee(Long id, String firstName,String lastName,String email,USER_ROLE userRole,String pesel,String sex,Date birthdate,Double salary,Departments department,String phoneContext,Boolean isLocked,Boolean isEnabled,String organization,String extension,String domain,String protocol,TimeZone timezone,String type,boolean callonnumber,boolean useSecondaryAllotedLine,String phonenumber,String provider1,String allotednumber1,String provider2,String allotednumber2,String costCalculation,String amount,String transfer_phone_1,String transfer_phone_2,String imageName,String imageType,String governmentDocumentID1,String governmentDocumentID2,int totalparkedchannels,String parkedchannel1,String parkedchannel2,String parkedchannel3,String parkedchannel4,String parkedchannel5,String parkedchannel6,String parkedchannel7,String parkedchannel8,String parkedchannel9,String parkedchannel10,boolean autoAnswer, boolean autoConference, boolean autoVideo, String micDevice, String speakerDevice, String videoDevice, String videoOrientation, String videoQuality, String videoFrameRate, String autoGainControl, String echoCancellation,String noiseSupression, boolean recordAllCalls,boolean intercomPolicy,boolean freeDialOption,boolean textDictateOption,boolean textMessagingOption,boolean doNotDisturb,boolean startVideoFullScreen,String confExtension,String extensionPrefix,String confExtensionPrefix,boolean callWaiting,String doc1ImageType,String doc2ImageType,String phoneTrunk,String secondDomain, String lastConnectedCustomerPhone) {
    	     
    	this.id = id;
    	this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userRole = userRole;
        this.pesel = pesel;
        this.sex = sex;
        this.birthdate = birthdate;
        this.salary = salary;
        this.department = department;
        this.phoneContext=phoneContext;
		this.isLocked=isLocked;
		this.isEnabled=isEnabled;
        this.organization=organization;
        this.extension=extension;
        this.domain=domain;
		this.protocol=protocol;
        this.timezone=timezone;
		this.type=type;
		this.callonnumber=callonnumber;
		this.useSecondaryAllotedLine=useSecondaryAllotedLine;
        this.phonenumber=phonenumber;
		this.provider1=provider1;
		this.allotednumber1=allotednumber1;
		this.provider2=provider2;
		this.allotednumber2=allotednumber2;
		this.costCalculation=costCalculation;
		this.amount=amount;
        this.transfer_phone_1=transfer_phone_1;
        this.transfer_phone_2=transfer_phone_2;
		this.imageName=imageName;
		this.imageType=imageType;
		this.doc1ImageType= doc1ImageType;
		this.doc2ImageType= doc2ImageType;
		this.governmentDocumentID1=governmentDocumentID1;
		this.governmentDocumentID2=governmentDocumentID2;
		this.totalparkedchannels=totalparkedchannels;
		this.parkedchannel1=parkedchannel1;
		this.parkedchannel2=parkedchannel2;
		this.parkedchannel3=parkedchannel3;
		this.parkedchannel4=parkedchannel4;
		this.parkedchannel5=parkedchannel5;
		this.parkedchannel6=parkedchannel6;
		this.parkedchannel7=parkedchannel7;
		this.parkedchannel8=parkedchannel8;
		this.parkedchannel9=parkedchannel9;
		this.parkedchannel10=parkedchannel10;
		this.recordAllCalls=recordAllCalls;
		this.intercomPolicy=intercomPolicy;
		this.freeDialOption=freeDialOption;
		this.textDictateOption=textDictateOption;
		this.textMessagingOption=textMessagingOption;
		
		this.doNotDisturb=doNotDisturb;
		this.startVideoFullScreen=startVideoFullScreen;
		this.autoAnswer=autoAnswer; 
		this.autoConference=autoConference; 
		this.autoVideo=autoVideo; 
		this.micDevice=micDevice; 
		this.speakerDevice=speakerDevice; 
		this.videoDevice=videoDevice; 
		this.videoOrientation=videoOrientation; 
		this.videoQuality=videoQuality; 
		this.videoFrameRate=videoFrameRate; 
		this.autoGainControl=autoGainControl; 
		this.echoCancellation=echoCancellation;
		this.noiseSupression=noiseSupression; 
		this.confExtension = confExtension;
		this.extensionPrefix = extensionPrefix;
		this.confExtensionPrefix = confExtensionPrefix;
		this.callWaiting = callWaiting;
		this.phoneTrunk = phoneTrunk;
		this.secondDomain = secondDomain;
		this.lastConnectedCustomerPhone =lastConnectedCustomerPhone;
    }
	
      
      //To retrieve basic information from database.
      public Employee(Long id, String firstName,String lastName,String email,USER_ROLE userRole,String pesel,Date birthdate,Departments department,String extension,String phonenumber,String imageType,String iconImageData) {
 	     
        	this.id = id;
          	this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.userRole = userRole;
            this.pesel = pesel;
            this.birthdate = birthdate;
            this.department = department;
        	this.imageType=imageType;
        	this.iconImageData= iconImageData;
        	this.extension=extension;
            this.phonenumber=phonenumber;
      }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(userRole.name());
        return Collections.singletonList(authority);
    }

    public boolean isAdmin(){
        return this.getUserRole().equals(USER_ROLE.ADMIN);
    }

    @Override
    public String getPassword(){
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
