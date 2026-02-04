package com.mylinehub.crm.entity;

import lombok.*;

import java.time.Instant;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_customers_org_phone", columnNames = {"organization", "phoneNumber"})
    	},
		indexes = {
		  @Index(name = "Customers_id_Index", columnList = "CUSTOMER_ID"),
		  @Index(name = "Customers_Organization_Index", columnList = "organization"),
		  @Index(name = "Customers_firstName_Index", columnList = "firstName"),
		  @Index(name = "Customers_country_Index", columnList = "country"),
		  @Index(name = "Customers_business_Index", columnList = "business"),
		  @Index(name = "Customers_phoneContext_Index", columnList = "phoneContext"),
		  @Index(name = "Customers_city_Index", columnList = "city"),
		  @Index(name = "Customers_zipCode_Index", columnList = "ZIP_CODE"),
		  @Index(name = "Customers_coverted_Index", columnList = "coverted"),
		  @Index(name = "Customers_email_Index", columnList = "email"),
		  @Index(name = "Customers_pesel_Index", columnList = "pesel"),
		  @Index(name = "Customers_phoneNumber_Index", columnList = "phoneNumber"),
		  @Index(name = "Customers_whatsAppPhoneNumberId_Index", columnList = "whatsAppPhoneNumberId"),
		  @Index(name = "Customers_whatsAppProjectId_Index", columnList = "whatsAppProjectId"),
		})
public class Customers {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "customer_sequence"
    )
    @SequenceGenerator(
            name="customer_sequence",
            sequenceName = "customer_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(name = "CUSTOMER_ID", nullable = false, unique = true)
    private Long id;

    private String firstname;
    private String lastname;
    
//    @Column(length = 11,unique = true)
    private String pesel;
    @Column(name = "ZIP_CODE")
    private String zipCode;
    private String city;

//    @Column(unique = true)
    String email;
    
//    @Column(unique = true)
    @Column(nullable = false)
    String phoneNumber;
    
    @Column(columnDefinition = "TEXT")
    String description;
    
    @Column(columnDefinition = "TEXT")
    String business;
    
    String country;
    String phoneContext;
    String domain;
    
    String whatsAppRegisteredByPhoneNumberId;
    String whatsAppRegisteredByPhoneNumber;
    String whatsApp_wa_id;
    String whatsAppDisplayPhoneNumber;
//    @Column(unique = true)
    String whatsAppPhoneNumberId;
    String whatsAppProjectId;
    
    @Column(columnDefinition = "boolean default false")
    boolean coverted;
    
    String datatype;
    String organization;
   
    
    @Column(columnDefinition = "boolean default false")
    boolean iscalledonce;
    
    @Column(columnDefinition = "boolean default false")
    boolean remindercalling;
    
    @Column(columnDefinition = "varchar(255)")
    String cronremindercalling;

    public String preferredLanguage;
    public String secondPreferredLanguage;
    
	public String lastConnectedExtension;
	
    private String imageName;

    private String imageType;
    
    @Column(columnDefinition = "bigint default 0")
    private Long imageSize;


    @Column(columnDefinition = "varchar(500)")
    private String imageData;

    @Column(columnDefinition = "TEXT")
    private String interestedProducts;
    
    @Column(columnDefinition = "boolean default false")
    boolean autoWhatsAppAIReply;
    
    @Column(columnDefinition = "boolean default false")
    boolean firstWhatsAppMessageIsSend;
    
    @Column(columnDefinition = "boolean default false")
    boolean updatedByAI;
    
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private CustomerPropertyInventory propertyInventory;
    
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private CustomerFranchiseInventory franchiseInventory;

    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    public Customers(Long id,String firstname, String lastname, String pesel, String zipCode, String city,String email,String phoneNumber,String description,String business,String country,String phoneContext,String domain,String datatype,String organization,boolean coverted,boolean remindercalling,String cronremindercalling,boolean iscalledonce, String imageName, String imageType,String interestedProducts,String lastConnectedExtension) {
        this.id = id;
		this.firstname = firstname;
        this.lastname = lastname;
        this.pesel = pesel;
        this.zipCode = zipCode;
        this.city = city;
        this.email=email;
        this.phoneNumber=phoneNumber;
        this.description=description;
        this.business=business;
        this.country=country;
        this.phoneContext=phoneContext;
        this.domain=domain;
        this.datatype=datatype;
        this.organization=organization;
		this.coverted=coverted;
		this.remindercalling=remindercalling;
		this.cronremindercalling=cronremindercalling;
		this.iscalledonce=iscalledonce;
		this.imageName=imageName;
		this.imageType=imageType;
		this.interestedProducts=interestedProducts;
		this.lastConnectedExtension = lastConnectedExtension;
    }
	
    
}
