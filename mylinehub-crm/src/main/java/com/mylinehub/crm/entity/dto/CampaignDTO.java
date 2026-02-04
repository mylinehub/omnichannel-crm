package com.mylinehub.crm.entity.dto;

import java.time.LocalTime;
import java.util.Date;
import java.util.TimeZone;

import javax.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignDTO {

	    public Long id;
	    public String domain;
	    public String organization;
	    public String name;
	    public String description;
	    public String aiApplicationName;
	    private String aiApplicationDomain;
	    public boolean isactive;
	    public boolean isenabled;
	    //public TimeZone timezone;
	    public Date startdate;
	    public Date enddate;
	   // public LocalTime starttime;
	    //public LocalTime endtime;
	    public String phonecontext;
	    public boolean isonmobile;
	    
	    public Long managerId;

	    public String country;
	    public String business;
	    
	    public String autodialertype;
	    private String template;
	    private String whatsAppNumber;
	    public boolean remindercalling;
	    public String cronremindercalling;
	    public int lastCustomerNumber;
	    public int breathingSeconds;
	    
	    private String ivrExtension;
	    
	    private String confExtension;
	    
	    private String queueExtension;
	    
	    int parallelLines;
	    int totalCallsMade;
	    int callLimit;
	    double callCost;
	    String callCostMode;
}
