package com.mylinehub.crm.whatsapp.dto;

import java.util.Map;


import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppMessageCountForNumberDTO {
	String adminFirstName;
	String adminLastName;
	String adminPhoneNumber;
	String adminExtension;
	String adminEmail;
	String phoneNumberMain;
	String phoneNumberWith;
	long totalMessagesReceived;
	long totalMessagesSend;
	long totalPhoneNumberWith;
	
	String month;
	String year;
	
	
	public WhatsAppMessageCountForNumberDTO(String phoneNumberMain, long totalMessagesSend,long totalMessagesReceived,long totalPhoneNumberWith)
	{
//		System.out.println("Adding CallCountForEmployeeDTO From Database Without Time");
		this.phoneNumberMain = phoneNumberMain;
		this.totalMessagesSend = totalMessagesSend;
		this.totalMessagesReceived = totalMessagesReceived;
		this.totalPhoneNumberWith = totalPhoneNumberWith;
		
		System.out.println("******************WhatsAppMessageCountForNumberDTO Fetch Whats App Number ******************");
		Map<String,WhatsAppPhoneNumber> phoneMap = WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(phoneNumberMain, null, "get-one");
		
		if(phoneMap != null && phoneMap.size() > 0)
		{
			WhatsAppPhoneNumber phoneNumberObject = phoneMap.get(phoneNumberMain);
			if(phoneNumberObject != null) {
				
				Employee admin = phoneNumberObject.getAdmin();
				adminFirstName = admin.getFirstName();
				adminLastName = admin.getLastName();
				adminPhoneNumber = admin.getPhonenumber();
				adminExtension = admin.getExtension();
				adminEmail = admin.getEmail();
				
			}
		}
	}
	
	public WhatsAppMessageCountForNumberDTO(String phoneNumberMain, long totalMessagesSend,long totalMessagesReceived,long totalPhoneNumberWith,int month, int year)
	{
		this(phoneNumberMain,totalMessagesSend,totalMessagesReceived,totalPhoneNumberWith);
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
