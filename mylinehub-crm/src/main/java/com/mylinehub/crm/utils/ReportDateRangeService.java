package com.mylinehub.crm.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.enums.DASHBOARD_TIME_LINE;

import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class ReportDateRangeService {

	public List<Date> fetchStartAndEndDateListAsPerDateRange(String stringDateRange) {
		List<Date> toReturn = new ArrayList<>();
		Date endDate = new Date(); 
   	 	Date startDate = new Date(); 
		try {
			 if(stringDateRange.equals(DASHBOARD_TIME_LINE.Today.name()))
    		 {
    			 LoggerUtils.log.debug("Fetching results for : "+DASHBOARD_TIME_LINE.Today.name());
            	 endDate = new Date(); 
            	 Instant inst = endDate.toInstant();
            	 LocalDate localDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
            	 Instant dayInst = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            	 startDate = Date.from(dayInst);
            	 LoggerUtils.log.debug("endDate : " + endDate);
            	 LoggerUtils.log.debug("startDate : "+startDate);  
    		 }
    		 else if(stringDateRange.equals(DASHBOARD_TIME_LINE.Yesterday.name()))
    		 {
    			 LoggerUtils.log.debug("Fetching results for : "+DASHBOARD_TIME_LINE.Yesterday.name());            	 
            	 Instant instEnd = Instant.now();
            	 LocalDate localEndDate = instEnd.atZone(ZoneId.systemDefault()).toLocalDate();
            	 Instant dayEndInst = localEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            	 endDate = Date.from(dayEndInst);
            	 
            	 Instant inststart = instEnd.minus(1, ChronoUnit.DAYS);
            	 LocalDate localStartDate = inststart.atZone(ZoneId.systemDefault()).toLocalDate();
            	 Instant dayStartInst = localStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            	 startDate = Date.from(dayStartInst);
            	 LoggerUtils.log.debug("endDate : " + endDate);
            	 LoggerUtils.log.debug("startDate : "+startDate);  
    		 }
    		 else if(stringDateRange.equals(DASHBOARD_TIME_LINE.Week.name()))
    		 {
    			 LoggerUtils.log.debug("Fetching results for : "+DASHBOARD_TIME_LINE.Week.name());
    			 Instant now = Instant.now();
    			 Instant interimInstant = now.minus(7, ChronoUnit.DAYS);
    			 Date interimtoFindStartDate = Date.from(interimInstant); 
            	 Instant inst = interimtoFindStartDate.toInstant();
            	 LocalDate localDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
            	 Instant dayInst = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            	 startDate = Date.from(dayInst);
            	 LoggerUtils.log.debug("endDate : " + endDate);
            	 LoggerUtils.log.debug("startDate : "+startDate);
    		 }
    		 else if(stringDateRange.equals(DASHBOARD_TIME_LINE.Month.name()))
    		 {
    			 LoggerUtils.log.debug("Fetching results for : "+DASHBOARD_TIME_LINE.Month.name());
    			 Instant now = Instant.now();
    			 Instant interimInstant = now.minus(30, ChronoUnit.DAYS);
    			 Date interimtoFindStartDate = Date.from(interimInstant); 
            	 Instant inst = interimtoFindStartDate.toInstant();
            	 LocalDate localDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
            	 Instant dayInst = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            	 startDate = Date.from(dayInst);
            	 LoggerUtils.log.debug("endDate : " + endDate);
            	 LoggerUtils.log.debug("startDate : "+startDate);
    		 }
    		 else if(stringDateRange.equals(DASHBOARD_TIME_LINE.Quater.name()))
    		 {
    			 LoggerUtils.log.debug("Fetching results for : "+DASHBOARD_TIME_LINE.Quater.name());
    			 Instant now = Instant.now();
    			 Instant interimInstant = now.minus(91, ChronoUnit.DAYS);
    			 Date interimtoFindStartDate = Date.from(interimInstant); 
            	 Instant inst = interimtoFindStartDate.toInstant();
            	 LocalDate localDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
            	 Instant dayInst = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            	 startDate = Date.from(dayInst);
            	 LoggerUtils.log.debug("endDate : " + endDate);
            	 LoggerUtils.log.debug("startDate : "+startDate);
    		 }
    		 else if(stringDateRange.equals(DASHBOARD_TIME_LINE.Year.name()))
    		 {
    			 LoggerUtils.log.debug("Fetching results for : "+DASHBOARD_TIME_LINE.Year.name());
    			 Instant now = Instant.now();
    			 Instant interimInstant = now.minus(365, ChronoUnit.DAYS);
    			 Date interimtoFindStartDate = Date.from(interimInstant); 
            	 Instant inst = interimtoFindStartDate.toInstant();
            	 LocalDate localDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
            	 Instant dayInst = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            	 startDate = Date.from(dayInst);
            	 LoggerUtils.log.debug("endDate : " + endDate);
            	 LoggerUtils.log.debug("startDate : "+startDate);
    		 }
			 
			 toReturn.add(startDate);
			 toReturn.add(endDate);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		
		return toReturn;
	}
    
}

