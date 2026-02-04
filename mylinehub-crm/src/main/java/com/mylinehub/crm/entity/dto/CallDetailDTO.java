package com.mylinehub.crm.entity.dto;

import java.time.LocalDate;
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
public class CallDetailDTO {

		public long id;
	    private Long campaignRunDetailsId;
	    private Long campaignRunCallLogId;
		private Long campaignID;
		private String linkId;    
	    public String callerid;
	    private double callCost;
	    private String callCostMode;
	    private String employeeName;
	    public String customerid;
	    private String customerName;
	    public String organization;
	    public String phoneContext;
	    public int calldurationseconds;
	    public boolean isactive;
	    public TimeZone timezone;
	    public LocalDate startdate;
	    public Date enddate;
	    public LocalTime starttime;
	    public LocalTime endtime;
	    public boolean callonmobile;
	    public boolean isconference;
	    public boolean isconnected;
	    public int maximumchannels;
        public String country;
        public String callType;
        private boolean ivr;
        private boolean queue;
        private boolean pridictive;
        private boolean progressive;
        boolean coverted;
        public String description;
        private String callSessionId;
        
	    
}
