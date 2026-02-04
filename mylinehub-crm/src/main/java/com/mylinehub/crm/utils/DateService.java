package com.mylinehub.crm.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class DateService {

    public boolean isXDaysAgo(int x, Date inputDate) {
        try {
        	
        	 //System.out.println("DateService : isXDaysAgo");
        	
        	 LocalDate currentDate = LocalDate.now();
             LocalDate comparisonDate = inputDate.toInstant()
                                                 .atZone(ZoneId.systemDefault())
                                                 .toLocalDate();
             LocalDate xDaysAgo = currentDate.minusDays(x);

             // Logs
             //System.out.println("Current Date: " + currentDate);
             //System.out.println("Input Date (converted): " + comparisonDate);
             //System.out.println(x + " days ago: " + xDaysAgo);

             boolean result = comparisonDate.isBefore(xDaysAgo);
             //System.out.println("Is input date more than " + x + " days ago? " + result);

             return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

