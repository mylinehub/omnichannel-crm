package com.mylinehub.crm.whatsapp.entity;

import java.time.Instant;
import java.util.List;

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
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.whatsapp.dto.WhatsAppManagementEmployeeDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "WhatsAppPhoneNumber",
indexes = {
		  @Index(name = "whatsapp_phone_numberid_Index", columnList = "id"),
		  @Index(name = "whatsapp_phone_numberOrganization_Index", columnList = "organization"),
		  @Index(name = "whatsapp_phone_number_Index", columnList = "phoneNumber"),
		  @Index(name= "whatsapp_phone_number_project_Index",columnList = "whatsAppProjectID"),
		  @Index(name= "whatsapp_phone_number_admin_Index",columnList = "adminEmployeeId"),
		  @Index(name= "whatsapp_phone_number_active_Index",columnList = "active")
		})
public class WhatsAppPhoneNumber {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "whatsapp_phone_numbersequence"
    )
    @SequenceGenerator(
            name="whatsapp_phone_numbersequence",
            sequenceName = "whatsapp_phone_numbersequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "whatsAppProjectID")
    private WhatsAppProject whatsAppProject;
    
    @Column(unique=true)
    private String phoneNumber;

    private String verifyToken;
    
    private String phoneNumberID;
    private String whatsAppAccountID;
    
    @Column(columnDefinition = "varchar(255) default 'GPT-4o mini'")
    private String aiModel;
    private String callBackURL;
    private String callBackSecret;
    private String organization;
    
    @Column(columnDefinition = "bigint default 0")
    private Long costPerInboundMessage;
    @Column(columnDefinition = "bigint default 1")
    private Long costPerOutboundMessage;

    @Column(columnDefinition = "bigint default 1")
    private Long costPerInboundAIMessageToken;
    @Column(columnDefinition = "bigint default 1")
    private Long costPerOutboundAIMessageToken;
    
    private String country;
    private String currency;
    private String aiCallExtension;
    private boolean active = true;
    
    private boolean autoAiMessageAllowed;
    private int autoAiMessageLimit;
    
    @ManyToOne
    @JoinColumn(name = "adminEmployeeId")
    private Employee admin;

    @ManyToOne
    @JoinColumn(name = "secondAdminEmployeeId")
    private Employee secondAdmin;
    
    @Column(columnDefinition = "TEXT")
    private String employeeExtensionAccessList;
  
    @Column(name="store_verify_customer_property_inventory")
    private boolean storeVerifyCustomerPropertyInventory;
    
    private String aiOutputClassName;

    @Column(columnDefinition = "TEXT")
    private String ridContacts;
    @Column(columnDefinition = "TEXT")
    private String ridHistory;
    
    @Column(name = "coexistence_sync_requested")
    private boolean coexistenceSyncRequested;
   
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
}