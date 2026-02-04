package com.mylinehub.crm.reconciliation;

import java.io.InputStream;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.service.DepartmentService;
import com.mylinehub.crm.service.EmployeeService;

public class PerformStatementReconciliation {
	
	  public List<Employee> loadExcelToRawDataMap(EmployeeService employeeService,DepartmentService departmentService,InputStream is,String email,String organization,BCryptPasswordEncoder passwordEncoder, ErrorRepository errorRepository) throws Exception {
		 
		 return null;
	  }
}


