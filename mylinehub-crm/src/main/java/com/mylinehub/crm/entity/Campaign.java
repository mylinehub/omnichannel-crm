package com.mylinehub.crm.entity;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Date;
import java.util.TimeZone;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Transient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "CAMPAIGN",
indexes = {
		  @Index(name = "Campaign_Organization_Index", columnList = "organization"),
		  @Index(name = "Campaign_Manager_ID_Index", columnList = "Manager_ID"),
		  @Index(name = "Campaign_country_Index", columnList = "country"),
		  @Index(name = "Campaign_business_Index", columnList = "business"),
		  @Index(name = "Campaign_isonmobile_Index", columnList = "isonmobile"),
		  @Index(name = "Campaign_autodialertype_Index", columnList = "autodialertype"),
		  @Index(name = "Campaign_startdate_Index", columnList = "startdate"),
		  @Index(name = "Campaign_name_Index", columnList = "name"),
		})
public class Campaign {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "campaign_sequence"
    )
    @SequenceGenerator(
            name="campaign_sequence",
            sequenceName = "campaign_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(name = "CAMPAIGN_ID",nullable = false)
    private Long id;
    
    private String domain;
    private String organization;
    private String name;
    private String description;
    private String aiApplicationName;
    private String aiApplicationDomain;
    
    @Column(columnDefinition = "boolean default false")
    private boolean isactive;
    
    @Column(columnDefinition = "boolean default true")
    public boolean isenabled;
    
    @Transient
    private TimeZone timezone;
    
    private Date startdate;
    private Date enddate;
    private LocalTime starttime;
    private LocalTime endtime;
    private String phonecontext;
    private boolean isonmobile;
    
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "Manager_ID")
    //@JsonBackReference
    private Employee manager;
    
    private String ivrExtension;
    private String template;
    private String whatsAppNumber;
    private String confExtension;
    
    private String queueExtension;
    
    private String country;
    private String business;

    @Column(columnDefinition = "varchar(255) default 'Predictive'")
    String autodialertype;
    
    @Column(columnDefinition = "boolean default false")
    boolean remindercalling;
    
    @Column(columnDefinition = "varchar(255)")
    String cronremindercalling;
    
    @Column(columnDefinition = "integer default 0")
    int lastCustomerNumber;
    
    @Column(columnDefinition = "integer default 15")
    int breathingSeconds;   
    
    
    @Column(columnDefinition = "integer default 0")
    int totalCallsMade;
    
    
    @Column(columnDefinition = "integer default -1")
    int callLimit;
    
    @Column(columnDefinition = "integer default 1")
    int parallelLines;
    
    @Column(name = "call_cost")
    private double callCost;
    
    @Column(name = "call_cost_mode")
    private String callCostMode;
    
    @Column(name = "ai_recording_domain")
    private String aiRecordingDomin;
    
    @Column(name = "ai_recording_port")
    private String aiRecordingPort;
    
    @Column(name = "ai_recording_server_path")
    private String aiRecordingServerPath;
    
    @Column(name = "ai_recording_server_token")
    private String aiRecordingServerToken;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    Campaign ( String domain,String organization,String name,String description,TimeZone timezone,Date startdate,Date enddate,LocalTime starttime,LocalTime endtime,String phonecontext,boolean isonmobile, String country,String business,Employee manager)
    {
    	this.domain=
    			this.organization=organization;
    			this.name=name;
    			this.description=description;
    			this.startdate=startdate;
    			this.enddate=enddate;
    			this.starttime=starttime;
    			this.endtime=endtime;
    			this.phonecontext=phonecontext;
    			this.isonmobile= isonmobile;
    			this.country=country;
    			this.business=business;
    			this.manager=manager;
    }
    
    Campaign ( String domain,String organization,String name,String description,TimeZone timezone,Date startdate,Date enddate,LocalTime starttime,LocalTime endtime,String phonecontext,boolean isonmobile, String country,String business)
    {
    	this.domain=
    			this.organization=organization;
    			this.name=name;
    			this.description=description;
    			this.startdate=startdate;
    			this.enddate=enddate;
    			this.starttime=starttime;
    			this.endtime=endtime;
    			this.phonecontext=phonecontext;
    			this.isonmobile= isonmobile;
    			this.country=country;
    			this.business=business;
    }
    
}
