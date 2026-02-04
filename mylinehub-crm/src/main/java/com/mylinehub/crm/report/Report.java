package com.mylinehub.crm.report;

import java.util.Date;

import com.mylinehub.crm.entity.Errors;
import com.mylinehub.crm.entity.Logs;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.repository.LogRepository;

public class Report {

	public static void addError(String data, String errorMessage,String errorClass, String functionality,String organization,ErrorRepository errorRepository) {	
		 Date date = new Date(System.currentTimeMillis());
		 Errors error = new Errors();
		 error.setData(data);
		 error.setError(errorMessage);
		 error.setErrorClass(errorClass);
		 error.setFunctionality(functionality);
		 error.setCreatedDate(date);
		 error.setOrganization(organization);
		 errorRepository.save(error);
	}
	
	public static void addLog(String data, String logMessage,String logClass, String functionality,String organization,LogRepository logRepository) {	
		 Date date = new Date(System.currentTimeMillis());
		 Logs log = new Logs();
		 log.setData(data);
		 log.setLog(logMessage);
		 log.setLogClass(logClass);
		 log.setFunctionality(functionality);
		 log.setCreatedDate(date);
		 log.setOrganization(organization);
		 logRepository.save(log);
	}
}
