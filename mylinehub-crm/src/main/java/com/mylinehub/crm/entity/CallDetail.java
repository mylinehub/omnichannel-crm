package com.mylinehub.crm.entity;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Date;
import java.util.TimeZone;

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
@Table(name = "CALL_DETAIL",
		indexes = {
				  @Index(name = "CallDetail_id_Index", columnList = "Call_DETAIL_ID"),
				  @Index(name = "CallDetail_PhoneContext_Index", columnList = "phonecontext"),
				  @Index(name = "CallDetail_Organization_Index", columnList = "organization"),
				  @Index(name = "CallDetail_callerid_Index", columnList = "callerid"),
				  @Index(name = "CallDetail_calldurationseconds_Index", columnList = "calldurationseconds"),
				  @Index(name = "CallDetail_customerid_Index", columnList = "customerid"),
				  @Index(name = "CallDetail_startdate_Index", columnList = "startdate"),
				  @Index(name = "CallDetail_isconference_Index", columnList = "isconference"),
				  @Index(name = "CallDetail_isconnected_Index", columnList = "isconnected"),
				  @Index(name = "CallDetail_timezone_Index", columnList = "timezone"),
				  @Index(name = "CallDetail_isactive_Index", columnList = "isactive"),
				  @Index(name = "CallDetail_linkId_Index", columnList = "link_id"),
				  @Index(name = "CallDetail_campaignID_Index", columnList = "campaign_id"),
				  @Index(name = "CallDetail_campaignRunDetailsId_Index", columnList = "campaign_run_details_id"),
				  @Index(name = "CallDetail_campaignRunCallLogId_Index", columnList = "campaign_run_call_log_id"),
				})
public class CallDetail {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "call_detail_sequence"
    )
    @SequenceGenerator(
            name="call_detail_sequence",
            sequenceName = "call_detail_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(name = "Call_DETAIL_ID", nullable = false)
    private Long id;
 
    @Column(name = "campaign_run_details_id")
    private Long campaignRunDetailsId;
    
    @Column(name = "campaign_run_call_log_id")
    private Long campaignRunCallLogId;
    
    @Column(name = "campaign_id")
    private Long campaignID;
    
    @Column(name = "link_id")
    private String linkId;
    
    @Column(name = "call_cost")
    private double callCost;
    
    @Column(name = "call_cost_mode")
    private String callCostMode;

    private String callerid;
    private String employeeName;
    private String customerid;
    private String customerName;
    private String organization;
    private String phoneContext;
    private double calldurationseconds;
    
    @Column(columnDefinition = "boolean default true")
    private boolean isactive;
    
    private TimeZone timezone;
    private Date startdate;
    private Date enddate;
    private LocalTime starttime;
    private LocalTime endtime;
    
    @Column(columnDefinition = "boolean default false")
    private boolean callonmobile;
    
    @Column(columnDefinition = "boolean default false")
    private boolean isconference;
    
    
    @Column(columnDefinition = "boolean default false")
    private boolean ivr;
    
    @Column(columnDefinition = "boolean default false")
    private boolean queue;
    
    @Column(columnDefinition = "boolean default false")
    private boolean pridictive;
    
    @Column(columnDefinition = "boolean default false")
    private boolean progressive;
    
    
    @Column(columnDefinition = "boolean default false")
    private boolean isconnected;
    
    private String callType;
    
    private String extraconferencechannelid1;
    private String extraconferencechannelid2;
    private String extraconferencechannelid3;
    private String extraconferencechannelid4;
    private String extraconferencechannelid5;
    private String extraconferencechannelid6;
    private String extraconferencechannelid7;
    private String extraconferencechannelid8;
    private String extraconferencechannelid9;
    private String extraconferencechannelid10;
    private String extraconferencechannelid11;
    private String extraconferencechannelid12;
    private String extraconferencechannelid13;
    private String extraconferencechannelid14;
    private String extraconferencechannelid15;
    private String extraconferencechannelid16;
    private String extraconferencechannelid17;
    private String extraconferencechannelid18;
    private String extraconferencechannelid19;
    private String extraconferencechannelid20;
    private String extraconferencechannelid21;
    private String extraconferencechannelid22;
    private String extraconferencechannelid23;
    private String extraconferencechannelid24;
    private String extraconferencechannelid25;
    private String extraconferencechannelid26;
    private String extraconferencechannelid27;
    private String extraconferencechannelid28;
    private String extraconferencechannelid29;
    private String extraconferencechannelid30;
    private String extraconferencechannelid31;
    private String extraconferencechannelid32;
    private String extraconferencechannelid33;
    private String extraconferencechannelid34;
    private String extraconferencechannelid35;
    private String extraconferencechannelid36;
    private String extraconferencechannelid37;
    private String extraconferencechannelid38;
    private String extraconferencechannelid39;
    private String extraconferencechannelid40;
    private String extraconferencechannelid41;
    private String extraconferencechannelid42;
    private String extraconferencechannelid43;
    private String extraconferencechannelid44;
    private String extraconferencechannelid45;
    private String extraconferencechannelid46;
    private String extraconferencechannelid47;
    private String extraconferencechannelid48;
    private String extraconferencechannelid49;
    private String extraconferencechannelid50;
    
    @Column(columnDefinition = "integer default 2")
    private int maximumchannels;
 
    private String country;
    
    private String callSessionId;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    CallDetail( String customerid,String employeeName,String customerName, String organization, String phoneContext,TimeZone timezone, Date startdate, LocalTime starttime,String country)
    {
    	this.customerid=customerid;
    	this.employeeName = employeeName;
    	this.customerName = customerName;
   		this.organization=organization;
   		this.phoneContext=phoneContext;
   		this.timezone=timezone;
   		this.startdate=startdate;
   		this.starttime=starttime; 
   		this.country=country;
    }
    
    
}
