package com.mylinehub.crm.whatsapp.entity;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import com.mylinehub.crm.whatsapp.dto.WhatsAppExtensionReportingDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "WhatsAppNumberReport",
uniqueConstraints= {
@UniqueConstraint(columnNames={"phoneNumberMain","phoneNumberWith","dayUpdated"})}
,
indexes = {
		  @Index(name = "whatsapp_reportid_Index", columnList = "id"),
		  @Index(name= "whatsapp_report_type_Index",columnList = "typeOfReport"),
		  @Index(name= "whatsapp_organization_type_Index",columnList = "organization"),
		  @Index(name= "whatsapp_report_PhoneNumberMain_Index",columnList = "phoneNumberMain"),
		  @Index(name= "whatsapp_report_PhoneNumberWith_Index",columnList = "phoneNumberWith")
		})
public class WhatsAppNumberReport {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "whatsapp_reportsequence"
    )
    @SequenceGenerator(
            name="whatsapp_reportsequence",
            sequenceName = "whatsapp_reportsequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;

    private String phoneNumberMain;
    private String phoneNumberWith;
    
    @Column(columnDefinition="double precision default '0'")
    private Long manualMessageSend;
    
    @Column(columnDefinition="double precision default '0'")
    private Long campaignMessageSend;
    
    @Column(columnDefinition="double precision default '0'")
    private Long aiMessagesSend;
    
    @Column(columnDefinition="double precision default '0'")
    private Long totalMessagesReceived;

    @Column(columnDefinition="double precision default '0'")
    private Long manualMessageDelivered;
    
    @Column(columnDefinition="double precision default '0'")
    private Long campaignMessageDelivered;
    
    @Column(columnDefinition="double precision default '0'")
    private Long aiMessagesDelivered;
    
    @Column(columnDefinition="double precision default '0'")
    private Long manualMessageRead;
    
    @Column(columnDefinition="double precision default '0'")
    private Long campaignMessageRead;
    
    @Column(columnDefinition="double precision default '0'")
    private Long aiMessagesRead;
    
    @Column(columnDefinition="double precision default '0'")
    private Long manualMessageFailed;
    
    @Column(columnDefinition="double precision default '0'")
    private Long campaignMessageFailed;
    
    @Column(columnDefinition="double precision default '0'")
    private Long aiMessagesFailed;
    
    @Column(columnDefinition="double precision default '0'")
    private Long manualMessageDeleted;
    
    @Column(columnDefinition="double precision default '0'")
    private Long campaignMessageDeleted;
    
    @Column(columnDefinition="double precision default '0'")
    private Long aiMessagesDeleted;
    
    @Column(columnDefinition="double precision default '0'")
    private Long aiTokenSend;
    
    @Column(columnDefinition="double precision default '0'")
    private Long totalTokenReceived;
    
    @Column(columnDefinition="double precision default '0'")
    private Long totalAmountSpend;
    
    @Column(columnDefinition="double precision default '0'")
    private Long totalMediaSizeSendMB;
    
    private String typeOfReport;
    
	@Type(type = "jsonb")
    @Column(name = "chats", columnDefinition = "jsonb", nullable = false)
    private Map<String, WhatsAppExtensionReportingDTO> extensionReport;
	
    private Date dayUpdated;
    
    private String organization;
    
    @UpdateTimestamp
    private Date lastUpdatedOn;
}