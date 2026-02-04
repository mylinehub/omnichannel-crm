package com.mylinehub.crm.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AbsenteeismDTO {

    //source = "employee.id"
	private Long id;
     Long employeeID;
    //source = "employee.firstName"
     String firstName;
    //source = "employee.lastName"
     String lastName;
    //source = "employee.department.departmentName"
     String departmentName;
    //source = "reasonOfAbsenteeismCode.absenteeismName"
     String absenteeismName;
     Date dateFrom;
     Date dateTo;
     String reasonForAbsense;
     String organization;
}
