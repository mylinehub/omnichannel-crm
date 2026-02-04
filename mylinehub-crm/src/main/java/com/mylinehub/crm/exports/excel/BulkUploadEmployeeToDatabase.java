package com.mylinehub.crm.exports.excel;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.entity.Departments;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Errors;
import com.mylinehub.crm.entity.dto.BulkUploadEmployeeDto;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.enums.COST_CALCULATION;
import com.mylinehub.crm.enums.USER_ROLE;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.service.DepartmentService;
import com.mylinehub.crm.service.EmployeeService;


public class BulkUploadEmployeeToDatabase {

	  
	   /**
     * an array that stores the content of the headers in columns
     */
   // private final String[] columns = {"First Name", "Last Name", "Department", "Role", "Salary", "Email","DOB-(YYYY-MM-DD)","Web-Password", "Phone Context", "Organization", "Domain", "Extension", "Extension Password","Time Zone","Phone Number","Transfer Phone 1","Transfer Phone 2","Cost Calculation"};
    String SHEET = "employees";
    
	  public List<BulkUploadEmployeeDto> excelToEmployees(EmployeeService employeeService,DepartmentService departmentService,InputStream is,String email,String organization,BCryptPasswordEncoder passwordEncoder, ErrorRepository errorRepository) throws Exception {
		 
	      
		  Workbook workbook = new XSSFWorkbook(is);
		  
	    try {
	    //	System.out.println("I am inside try of excel to Employee BulkUploadClass");
	    	
	      
	    	
	      Sheet sheet = workbook.getSheet(SHEET);
	      Iterator<Row> rows = sheet.iterator();

	      //System.out.println("Does row have next value"+rows.hasNext());
	    	
	      
	      List<BulkUploadEmployeeDto> employees = new ArrayList<BulkUploadEmployeeDto>();
	      List<Errors> allErrors = new ArrayList<Errors>();

	      int rowNumber = 0;
	   
	      
	      while (rows.hasNext()) {
	    	  
	    	BulkUploadEmployeeDto bulkUploadEmployeeDto= new BulkUploadEmployeeDto();
	    	System.out.println("Row Information : Row Number : "+rowNumber+" : "+rows.toString());
		    	
	    	  
	        Row currentRow = rows.next();

	       // System.out.println("currentRow Information : ");
	    	
	    	  
	        // skip header
	        if (rowNumber == 0) {
	          rowNumber++;
	          continue;
	        }
	       // System.out.println("Physical Cells : " +currentRow.getPhysicalNumberOfCells());
	        Iterator<Cell> cellsInRow = currentRow.iterator();

	        //System.out.println("After Iterator : ");
	        //System.out.println(cellsInRow.hasNext());
	    	
	    	  
	        Employee employee = new Employee();

	        int cellIdx = 0;
	        while (cellsInRow.hasNext()) {
	        	
	        
	        	
	          Cell currentCell = cellsInRow.next();

	         // System.out.println("Row Index : "+currentCell.getRowIndex());
	         // System.out.println("Cell Address : "+currentCell.getAddress());
	          //System.out.println("Cell Type : "+currentCell.getCellType());
	         
	          switch (cellIdx) {
	        
	          case 0:employee.setFirstName((String)currentCell.getStringCellValue());
//	          System.out.println("1.First Name : "+String.valueOf(currentCell.getStringCellValue()));
	          
	                 
	          break;
	          case 1:employee.setLastName((String)currentCell.getStringCellValue());  
//	          System.out.println("2.Last Name : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 2:
	        	  
//	        	  System.out.println("3.Department  Name : "+String.valueOf(currentCell.getNumericCellValue()));
	        	  
	        	       try
	        	       {
	        	    	   Departments department = departmentService.getDepartmentById((long) currentCell.getNumericCellValue()); 
	        	    	  // (long) currentCell.getNumericCellValue(
	        	    	   employee.setDepartment(department);  
	        	       }
	        	       catch(Exception e)
	        	       {
	        	    	   throw new Exception("Ecxeption while inserting department");
	        	       }
	        	       
	        	  
	        	  
	          break;
	          case 3:
	        	  
//	        	  System.out.println("4.User Role  Name : "+String.valueOf(currentCell.getStringCellValue()));
	        	  
	        	  
	        	  if(USER_ROLE.ADMIN.name().equalsIgnoreCase((String)currentCell.getStringCellValue()))
	        	  {
	        		  employee.setUserRole(USER_ROLE.ADMIN);
	        	  }
	        	  else if(USER_ROLE.EMPLOYEE.name().equalsIgnoreCase((String)currentCell.getStringCellValue())) 
	        	  {
	        		  employee.setUserRole(USER_ROLE.EMPLOYEE);
	        	  }
	        	  else if(USER_ROLE.MANAGER.name().equalsIgnoreCase((String)currentCell.getStringCellValue()))  
	        	  {
	        		  employee.setUserRole(USER_ROLE.MANAGER);
	        	  }
	        	  else
	        	  { employee.setUserRole(USER_ROLE.EMPLOYEE);
	        		  
	        	  }
	        	  
	        	  
	        	  break;
	          case 4:
	          
	          try
        	  {
	        	  employee.setSalary((double) currentCell.getNumericCellValue()); 
//		          System.out.println("5.Numberic Salary : "+String.valueOf(currentCell.getNumericCellValue()));
	          }
        	  catch(Exception e)
        	  {
        		  if(e.getMessage().contains("Cannot get"))
        		  {
        			  employee.setSalary(Double.parseDouble(currentCell.getStringCellValue().trim())); 
//    		          System.out.println("5. String Salary : "+String.valueOf(currentCell.getStringCellValue()));
     
        		  }
        		  else
        		  {
        			  throw new Exception("Exception while setting salary");
        		  }
        	  }
	          
	          break;
	          
	          case 5:employee.setEmail((String)currentCell.getStringCellValue());  
//	          System.out.println("6.Email : "+String.valueOf(employee.getEmail()));
	          break;
	          
	          
	          case 6:
	          try
        	  {
//        		  System.out.println("7. Numeric birth : "+String.valueOf(currentCell.getNumericCellValue()));
        		  Date javaDate= DateUtil.getJavaDate((double) currentCell.getNumericCellValue());
//        		  System.out.println("7. Numeric birth : "+javaDate);
        		  employee.setBirthdate(javaDate); 
        	  }
        	  catch(Exception e)
        	  {
        		  if(e.getMessage().contains("Cannot get"))
        		  {
        			  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);  			  
        			  employee.setBirthdate((formatter.parse(currentCell.getStringCellValue()))); 
//	        		  System.out.println("7 String birth : "+String.valueOf(employee.getBirthdate()));

        		  }
        		  else
        		  {
        			  throw new Exception("Exception while setting birth");
        		  }
        	  }
        	  
	          
	          break;
	          
	          
	          case 7:
	        	  
	          try
        	  {
        		  employee.setPassword(passwordEncoder.encode(String.valueOf((int)currentCell.getNumericCellValue()))); 
//        		  System.out.println("8.Numberic Password : "+String.valueOf((int)currentCell.getNumericCellValue()));
        		  bulkUploadEmployeeDto.setActualPassword(String.valueOf((int)currentCell.getNumericCellValue()));
        	  }
        	  catch(Exception e)
        	  {
        		  if(e.getMessage().contains("Cannot get"))
        		  {
        			  employee.setPassword(passwordEncoder.encode(String.valueOf(currentCell.getStringCellValue()))); 
//	        		  System.out.println("8.String Password : "+String.valueOf(currentCell.getStringCellValue()));
        			  bulkUploadEmployeeDto.setActualPassword(String.valueOf(currentCell.getStringCellValue()));
        		  }
        		  else
        		  {
        			  throw new Exception("Exception while setting password");
        		  }
        	  }
        	  
	          
	          break;
	          
	          
	          case 8:employee.setPhoneContext((String)currentCell.getStringCellValue());  
//	          System.out.println("9.Phone Context : "+String.valueOf(currentCell.getStringCellValue()));
		         break;
	          case 9:employee.setOrganization((String)currentCell.getStringCellValue()); 
//	          System.out.println("10.Organization : "+String.valueOf(currentCell.getStringCellValue()));
		         
	          break;
	          case 10:employee.setDomain((String)currentCell.getStringCellValue()); 
//	          System.out.println("11.Domain : "+String.valueOf(currentCell.getStringCellValue()));
		         
	          break;
	          case 11:
	        	  
	        	  try
	        	  {
	        		  employee.setExtension(String.valueOf((int)currentCell.getNumericCellValue())); 
//	        		  System.out.println("12.Numberic Extension : "+String.valueOf((int)currentCell.getNumericCellValue()));
	        	  }
	        	  catch(Exception e)
	        	  {
	        		  if(e.getMessage().contains("Cannot get"))
	        		  {
	        			  employee.setExtension(String.valueOf(currentCell.getStringCellValue())); 
//		        		  System.out.println("12.String Extension : "+String.valueOf(currentCell.getStringCellValue()));

	        		  }
	        		  else
	        		  {
	        			  throw new Exception("Exception while setting extension");
	        		  }
	        	  }
	        	  
	        	 
	          
		         
	          break;
	          case 12:employee.setExtensionpassword((String)currentCell.getStringCellValue());
	         
//	          System.out.println("13.Extension Password : "+String.valueOf(currentCell.getStringCellValue()));
		      break;
	          
	          case 13:
	        	  
	        	  employee.setTimezone(TimeZone.getTimeZone((String)currentCell.getStringCellValue())); 
//	        	  System.out.println("14.Time Zone : "+String.valueOf(employee.getTimezone().getDisplayName()));
			      
	        	  break;
	          case 14:
	          
	        	  try
	        	  {
	        		  String phoneNumber="+"+String.valueOf(currentCell.getNumericCellValue()).replaceAll(Pattern.quote("."),"");
	  	        	
		        	  
		        	  int positionOfE = phoneNumber.indexOf('E');
//		        	  System.out.println("6.positionOfE: +"+positionOfE);
		        	  if(positionOfE != -1)
		        	  {
		        		  phoneNumber = phoneNumber.substring(0, positionOfE);
		        	  }	
		        			  
		        	  employee.setPhonenumber(phoneNumber); 
//	        		  System.out.println("15.Numberic Phone Number : +"+phoneNumber);
	        	  
	        	  
	        	  }
	        	  catch(Exception e)
	        	  {
	        		  if(e.getMessage().contains("Cannot get"))
	        		  {
	        			  employee.setPhonenumber(String.valueOf(currentCell.getStringCellValue())); 
//		        		  System.out.println("15.String Phone number : "+String.valueOf(currentCell.getStringCellValue()));

	        		  }
	        		  else
	        		  {
	        			  throw new Exception("Exception while setting phone number");
	        		  }
	        	  }
	          
	          break;
	          case 15:
	          try
        	  {	  
        		  employee.setTransfer_phone_1(String.valueOf((int)currentCell.getNumericCellValue())); 
//        		  System.out.println("16.Transfer Number 1 : +"+String.valueOf((int)currentCell.getNumericCellValue()));
        	  
        	  
        	  }
        	  catch(Exception e)
        	  {
        		  if(e.getMessage().contains("Cannot get"))
        		  {
        			  employee.setTransfer_phone_1(String.valueOf(currentCell.getStringCellValue())); 
//	        		  System.out.println("16.Transfer Number 1 : "+String.valueOf(currentCell.getStringCellValue()));

        		  }
        		  else
        		  {
        			  throw new Exception("Exception while setting phone number");
        		  }
        	  }
	          
	          break;
	          case 16:
		      
	          try
        	  {	  
        		  employee.setTransfer_phone_2(String.valueOf((int)currentCell.getNumericCellValue())); 
//        		  System.out.println("17.Transfer Number 2 : +"+String.valueOf((int)currentCell.getNumericCellValue()));
        	  
        	  
        	  }
        	  catch(Exception e)
        	  {
        		  if(e.getMessage().contains("Cannot get"))
        		  {
        			  employee.setTransfer_phone_2(String.valueOf(currentCell.getStringCellValue())); 
//	        		  System.out.println("17.Transfer Number 2 : "+String.valueOf(currentCell.getStringCellValue()));

        		  }
        		  else
        		  {
        			  throw new Exception("Exception while setting phone number");
        		  }
        	  }
	          
	          break;

	          
	          case 17:
		          
	        	  employee.setProvider1(String.valueOf(currentCell.getStringCellValue())); 
//        		  System.out.println("18.Provider 1 : +"+String.valueOf(currentCell.getStringCellValue()));
	          
	          break;
	          
	          case 18:
		          
	        	  try
	        	  {
	        		  String alottedNumber1="+"+String.valueOf(currentCell.getNumericCellValue()).replaceAll(Pattern.quote("."),"");
	  	        	
		        	  int positionOfE = alottedNumber1.indexOf('E');
//		        	  System.out.println("19.positionOfE: +"+positionOfE);
		        	  if(positionOfE != -1)
		        	  {
		        		  alottedNumber1 = alottedNumber1.substring(0, positionOfE);
		        	  }	
		        			  
		        	  employee.setAllotednumber1(alottedNumber1); 
//	        		  System.out.println("19.Alotted Phone Number 1 : +"+alottedNumber1);
	        	  
	        	  
	        	  }
	        	  catch(Exception e)
	        	  {
	        		  if(e.getMessage().contains("Cannot get"))
	        		  {
	        			  employee.setAllotednumber1(String.valueOf(currentCell.getStringCellValue())); 
		        		  System.out.println("19.Alotted Phone number 1 : "+String.valueOf(currentCell.getStringCellValue()));

	        		  }
	        		  else
	        		  {
	        			  throw new Exception("Exception while setting phone number");
	        		  }
	        	  }
	          
	          break;
	         

	          case 19:
		          
	        	  employee.setProvider2(String.valueOf(currentCell.getStringCellValue())); 
//        		  System.out.println("20.Provider 2 : +"+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          
	          case 20:
		          
	        	  try
	        	  {
	        		  String alottedNumber2="+"+String.valueOf(currentCell.getNumericCellValue()).replaceAll(Pattern.quote("."),"");
	  	        	
		        	  int positionOfE = alottedNumber2.indexOf('E');
		        	  
		        	  if(positionOfE != -1)
		        	  {
		        		  alottedNumber2= alottedNumber2.substring(0, positionOfE);
		        	  }
		        	  
		        	  employee.setAllotednumber2(alottedNumber2); 
//	        		  System.out.println("21.Alotted Phone Number2 : +"+alottedNumber2);
	        	  
	        	  
	        	  }
	        	  catch(Exception e)
	        	  {
	        		  if(e.getMessage().contains("Cannot get"))
	        		  {
	        			  employee.setAllotednumber2(String.valueOf(currentCell.getStringCellValue())); 
//		        		  System.out.println("21.Alotted Phone number 2 : "+String.valueOf(currentCell.getStringCellValue()));

	        		  }
	        		  else
	        		  {
	        			  throw new Exception("Exception while setting phone number");
	        		  }
	        	  }
	          
	          break;
	          
	          
	          case 21:
	        	  employee.setCostCalculation((String)currentCell.getStringCellValue());  
//		          System.out.println("22.Cost Calculation : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          
	          case 22:
	        	  employee.setAmount((String)currentCell.getStringCellValue());  
//		          System.out.println("23. Amount : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          
	          case 23:
	        	  employee.setPhoneTrunk((String)currentCell.getStringCellValue());  
//		          System.out.println("23. Phone Trunk : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          
	          case 24:
	        	  employee.setSecondDomain((String)currentCell.getStringCellValue());  
//		          System.out.println("23. Second Domain : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          
	          default:
	        	  
//	        	  System.out.println("Default");			      
	            break;
	          }

	          ++cellIdx;
	        }
	        
//	        System.out.println("Out Of Row");

	        if(employee.getOrganization() != null)
	        {
//	        	System.out.println("Organization not null");
	        	 
		        if(employee.getOrganization().equals(organization))
		        {
		        	EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
		        	Employee verificationEmployee = null;
		        	boolean allow = true;
		        	
		        	if(employee.getExtension() != null && employee.getPhonenumber() != null && employee.getEmail() != null)
		        	{
		        		Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(employee.getExtension(), null, "get-one");
		        		if(allEmployeeDataAndState != null)
		        		{
		        			employeeDataAndStateDTO = allEmployeeDataAndState.get(employee.getExtension());
		        		} 
		        	}
		        	else
		        	{
		        		allow = false;
		        		//System.out.println("Employee Already Present");
			        	 Date date = new Date(System.currentTimeMillis());
						 Errors error = new Errors();
							
						 error.setData(employee.toString());
						 error.setError("Custom Error");
						 error.setErrorClass("BulkUploadEmployeeToDatabase");
						 error.setFunctionality("Employee did not had extension/Phone/email");
						 error.setCreatedDate(date);
						 error.setOrganization(employee.getOrganization());
						 allErrors.add(error);
		        	}
		        	
		        	if(employeeDataAndStateDTO != null)
		        	{
		        		verificationEmployee = employeeDataAndStateDTO.getEmployee();
		        	}		
			        
		        	if(allow && verificationEmployee ==null)
		        	{						 
						//Employee is to be added
//			        		System.out.println("Employee added");
			        		
			        		//Default values will go here
			        		
			        		employee = addEmployeeDefault(employee);
			        		
			        		bulkUploadEmployeeDto.setEmployee(employee);
			        		employees.add(bulkUploadEmployeeDto);
		        	}
		        	else
		        	{

		        		 System.out.println(verificationEmployee.getEmail());
		        		 //Employee Already present. He should update this details from portal
			        	 //System.out.println("Employee Already Present");
			        	 Date date = new Date(System.currentTimeMillis());
						 Errors error = new Errors();
							
						 error.setData(employee.toString());
						 error.setError("Custom Error");
						 error.setErrorClass("BulkUploadEmployeeToDatabase");
						 error.setFunctionality("Employee Already Present");
						 error.setCreatedDate(date);
						 error.setOrganization(employee.getOrganization());
						 allErrors.add(error);
		        	}

		        }
		        else
		        {
		        	//System.out.println("Employee Of Not This Organization");
		        	Date date = new Date(System.currentTimeMillis());
					Errors error = new Errors();
					error.setData(employee.toString());
					error.setError("Custom Error");
					error.setErrorClass("BulkUploadEmployeeToDatabase");
					error.setFunctionality("Employee Of Not This Organization");
					error.setCreatedDate(date);
					error.setOrganization(employee.getOrganization());
					allErrors.add(error);
		        }
		        
	        }
	        else
	        {
	        	//Employee Organization Cannot be null or end of rows
	        	System.out.println("Employee Organization Cannot be null or end of rows");
	         }
	       

	        rowNumber = rowNumber+1;
	      }

//	      System.out.println("Before Closing Workbook");
	      workbook.close();
	      
	      errorRepository.saveAll(allErrors);

	      return employees;
	    } catch (Exception e) {
	    	
	    	System.out.println("I am inside catch of Bulk Upload");
	    	e.printStackTrace();
	    	
	      throw e;
	    }
	    finally {
	    	workbook.close();
	    }
	  }
	  
	  public static Employee addEmployeeDefault(Employee employee) {
		  
		employee.setIsEnabled(true);
  		employee.setIsLocked(false);
  		employee.setCallonnumber(false);
  		employee.setChatEngine("SIP");
  		employee.setConfExtensionPrefix("99");
  		employee.setExtensionPrefix("99");
  		employee.setSipPath("/ws");
  		employee.setSipPort(8089);
  		employee.setProtocol("PJSIP/");
  		employee.setTotalparkedchannels(0);
  		employee.setType("External");
  		employee.setUseSecondaryAllotedLine(false);
  		employee.setPhoneContext("from-internal");
  		employee.setPhoneTrunk("from-trunk");
  		employee.setIntercomPolicy(true);
  		employee.setFreeDialOption(true);
  		employee.setTextDictateOption(true);
  		employee.setTextMessagingOption(true);
  		employee.setCostCalculation(COST_CALCULATION.UNLIMITED.name());
  		employee.setUiTheme("Light");
  		employee.setMicDevice("Default");
  		employee.setSpeakerDevice("Default"); 
  		employee.setVideoDevice("Default");
  		employee.setVideoOrientation("Mirror");
  		employee.setVideoQuality("HD"); 
  		employee.setVideoFrameRate("30");
  		employee.setAutoGainControl("true");
  		employee.setEchoCancellation("true");
  		employee.setNoiseSupression("true");
  		employee.setNotificationDot(true);
  		employee.setSex("N/A");
  		
  		if(employee.getUserRole() == USER_ROLE.ADMIN) {
  			employee.setAllowedToSwitchOffWhatsAppAI(true);
  		}
  		else {
  			employee.setAllowedToSwitchOffWhatsAppAI(false);
  		}
  		
//  		employee.setPesel(null);
//  		employee.setAllotednumber1(null);
//  		employee.setAllotednumber2(null);
  		
		  return employee;
	  }
	  
}
