package com.mylinehub.crm.entity.dto;

import java.util.Date;
import java.util.List;
import com.mylinehub.crm.entity.Employee;

import lombok.Data;

@Data
public class EmployeeDataAndStateDTO {
	Employee employee;
	//First value is state
	//Second value is presence
	//Third value is dotClass
	List<String> memberState;
	List<String> extensionState;
	Long runningCamapignId;
	String employeeLastCall;
	Date lastCalledTime;
}
