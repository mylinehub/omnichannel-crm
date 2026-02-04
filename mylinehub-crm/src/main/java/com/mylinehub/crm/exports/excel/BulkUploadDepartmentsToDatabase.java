package com.mylinehub.crm.exports.excel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mylinehub.crm.entity.Departments;
import com.mylinehub.crm.entity.Errors;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.service.DepartmentService;


public class BulkUploadDepartmentsToDatabase {

	  
	   /**
  * an array that stores the content of the headers in columns
  */
	//private static final String[] columns = {"Phone Context", "Organization","Extension","Domain"};
	    
	 String SHEET = "Departments";
 
	  public List<Departments> excelToDepartments(DepartmentService departmentsService,InputStream is,String organization,ErrorRepository errorRepository) throws Exception {
		 
	      
		  Workbook workbook = new XSSFWorkbook(is);
		  
	    try {
	    //	System.out.println("I am inside try of excel to Employee BulkUploadClass");
	    	
	      
	    	
	      Sheet sheet = workbook.getSheet(SHEET);
	      Iterator<Row> rows = sheet.iterator();

	      //System.out.println("Does row have next value"+rows.hasNext());
	    	
	      
	      List<Departments> departments = new ArrayList<Departments>();

	      int rowNumber = 0;
	   
	      
	      while (rows.hasNext()) {
	    	  
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
	    	
	    	  
	        Departments department = new Departments();

	        int cellIdx = 0;
	        while (cellsInRow.hasNext()) {
	        	
	        
	        	
	          Cell currentCell = cellsInRow.next();

	         // System.out.println("Row Index : "+currentCell.getRowIndex());
	         // System.out.println("Cell Address : "+currentCell.getAddress());
	          //System.out.println("Cell Type : "+currentCell.getCellType());
	         
	          switch (cellIdx) {
	        
	          case 0:department.setDepartmentName((String)currentCell.getStringCellValue());
	          System.out.println("1.Department Name : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 1:department.setCity((String)currentCell.getStringCellValue());
	          System.out.println("1.Department City : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 2:department.setOrganization((String)currentCell.getStringCellValue());
	          System.out.println("1.Department Organization : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          
	          default:
	        	  
	        	  System.out.println("Default");			      
	            break;
	          }

	          ++cellIdx;
	        }
	        
	        System.out.println("Out Of Row");

	        if(department.getOrganization() != null)
	        {
	        	System.out.println("Organization not null");
	        	
	        	 
		        if(department.getOrganization().equals(organization))
		        {
		        	Departments verificationDepartment = departmentsService.getDepartmentByDepartmentNameAndOrganization(department.getDepartmentName(), organization);
			        
		        	if(verificationDepartment !=null)
		        	{
	  
					        if(verificationDepartment.getDepartmentName() ==null ||verificationDepartment.getDepartmentName().isBlank() || department.getDepartmentName().isEmpty() ||  department.getDepartmentName().equals(""))
					        {
					        	//Email ID is required for user to get uploaded
					        	System.out.println("Extention is null");
					        	throw new Exception("Extension Cannot be null");
					        }
					        else
					        {
					        	if(verificationDepartment != null && !verificationDepartment.getDepartmentName().isEmpty() && verificationDepartment.getDepartmentName().trim().equals(department.getDepartmentName().trim()))
						        {
						        	//Employee Already present. He should update this details from portal
					        		//System.out.println("Departments Already Present");
					        		Date date = new Date(System.currentTimeMillis());
									Errors error = new Errors();
									
									error.setData(department.toString());
									error.setError("Custom Error");
									error.setErrorClass("BulkUploadDepartmentToDatabase");
									error.setFunctionality("Departments Already Present");
									error.setCreatedDate(date);
									error.setOrganization(department.getOrganization());
									errorRepository.save(error);
						        }

					        }
		        	}
		        	else
		        	{
		        		//Employee is to be added
		        		System.out.println("New Departments to be added here");
		        		
		        		//Default values will go here

		        		department.setManagers(null);
		        		departments.add(department);
		        	}

		        }
		        else
		        {
		        	//System.out.println("Departments Of Not This Organization");
		        	Date date = new Date(System.currentTimeMillis());
					Errors error = new Errors();
					error.setData(department.toString());
					error.setError("Custom Error");
					error.setErrorClass("BulkUploadDepartmentToDatabase");
					error.setFunctionality("Departments Of Not This Organization");
					error.setCreatedDate(date);
					error.setOrganization(department.getOrganization());
					errorRepository.save(error);
		        }
		        
	        }
	        else
	        {
	        	//Departments Organization Cannot be null or end of rows
	        	System.out.println("Departments Organization Cannot be null or end of rows");
	        	break;
	         }
	       

	        rowNumber = rowNumber+1;
	      }

	      System.out.println("Before Closing Workbook");
	      workbook.close();

	      return departments;
	    } catch (Exception e) {
	    	
	    	System.out.println("I am inside catch of Bulk Upload");
	    	e.printStackTrace();
	    	
	      throw e;
	    }
	    finally {
	    	workbook.close();
	    }
	  }
	  
}

