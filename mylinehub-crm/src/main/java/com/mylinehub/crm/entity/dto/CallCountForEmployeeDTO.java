package com.mylinehub.crm.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CallCountForEmployeeDTO {
	String firstName;
	String lastName;
	String phoneNumber;
	String extension;
	long totalCalls;
	long incomingCalls;
	long outgoingCalls;
	String month;
	String year;
	
	public CallCountForEmployeeDTO(long totalCalls)
	{
//		System.out.println("Adding CallCountForEmployeeDTO From Database Without Time");
		this.totalCalls = totalCalls;
	}
	
	public CallCountForEmployeeDTO(String extension, long totalCalls)
	{
//		System.out.println("Adding CallCountForEmployeeDTO From Database Without Time");
		this.extension = extension;
		this.totalCalls = totalCalls;
	}
	
	public CallCountForEmployeeDTO(String extension, long totalCalls,int month, int year)
	{
//		System.out.println("Adding CallCountForEmployeeDTO From Database for Time");
//		System.out.println("extension : "+extension);
//		System.out.println("totalCalls : "+totalCalls);
//		System.out.println("month : "+month);
//		System.out.println("year : "+year);
		
		this.extension = extension;
		this.totalCalls = totalCalls;
		
		switch (month) {
        case 1:  this.month = "January";       break;
        case 2:  this.month = "February";      break;
        case 3:  this.month = "March";         break;
        case 4:  this.month = "April";         break;
        case 5:  this.month = "May";           break;
        case 6:  this.month = "June";          break;
        case 7:  this.month = "July";          break;
        case 8:  this.month = "August";        break;
        case 9:  this.month = "September";     break;
        case 10: this.month = "October";       break;
        case 11: this.month = "November";      break;
        case 12: this.month = "December";      break;
        default: this.month = "Invalid month"; break;
        }
		
		this.year = String.valueOf(year);
	}
}
