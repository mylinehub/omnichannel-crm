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

import com.mylinehub.crm.entity.AmiConnection;
import com.mylinehub.crm.entity.Errors;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.service.AMIConnectionService;


public class BulkUploadAmiConnectionToDatabase {

	  
	   /**
  * an array that stores the content of the headers in columns
  */
	//private static final String[] columns = {"Phone Context", "Organization","Extension","Domain"};
	    
	 String SHEET = "AmiConnections";
 
	  public List<AmiConnection> excelToAmiConnections(AMIConnectionService amiConnectionService,InputStream is,String organization,ErrorRepository errorRepository) throws Exception {
		 
	      
		  Workbook workbook = new XSSFWorkbook(is);
		  
	    try {
	    //	System.out.println("I am inside try of excel to Employee BulkUploadClass");
	    	
	      
	    	
	      Sheet sheet = workbook.getSheet(SHEET);
	      Iterator<Row> rows = sheet.iterator();

	      //System.out.println("Does row have next value"+rows.hasNext());
	    	
	      
	      List<AmiConnection> amiConnections = new ArrayList<AmiConnection>();

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
	    	
	    	  
	        AmiConnection amiConnection = new AmiConnection();

	        int cellIdx = 0;
	        while (cellsInRow.hasNext()) {
	        	
	        
	        	
	          Cell currentCell = cellsInRow.next();

	         // System.out.println("Row Index : "+currentCell.getRowIndex());
	         // System.out.println("Cell Address : "+currentCell.getAddress());
	          //System.out.println("Cell Type : "+currentCell.getCellType());
	         
	          switch (cellIdx) {
	        
	          case 0:amiConnection.setPhonecontext((String)currentCell.getStringCellValue());
	          System.out.println("1.Phone Context : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 1:amiConnection.setOrganization((String)currentCell.getStringCellValue());
	          System.out.println("2. Organization : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 2:amiConnection.setAmiuser((String)currentCell.getStringCellValue());
	          System.out.println("3.AMI USER : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 3:amiConnection.setPassword((String)currentCell.getStringCellValue());
	          System.out.println("4. Password : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 4:amiConnection.setDomain((String)currentCell.getStringCellValue());
	          System.out.println("5. Domain : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	        
	          default:
	        	  
	        	  System.out.println("Default");			      
	            break;
	          }

	          ++cellIdx;
	        }
	        
	        System.out.println("Out Of Row");

	        if(amiConnection.getOrganization() != null)
	        {
	        	System.out.println("Organization not null");
	        	
	        	 
		        if(amiConnection.getOrganization().equals(organization))
		        {
		        	AmiConnection verificationAmiConnection = amiConnectionService.getAmiConnectionByAmiuserAndOrganization(amiConnection.getAmiuser(), organization);
			        
		        	if(verificationAmiConnection !=null)
		        	{
	  
					        if(verificationAmiConnection.getAmiuser() ==null ||verificationAmiConnection.getAmiuser().isBlank() || amiConnection.getAmiuser().isEmpty() ||  amiConnection.getAmiuser().equals(""))
					        {
					        	//Email ID is required for user to get uploaded
					        	System.out.println("Extention is null");
					        	throw new Exception("Extension Cannot be null");
					        }
					        else
					        {
					        	if(verificationAmiConnection != null && !verificationAmiConnection.getAmiuser().isEmpty() && verificationAmiConnection.getAmiuser().trim().equals(amiConnection.getAmiuser().trim()))
						        {
						        	//Employee Already present. He should update this details from portal
					        		//System.out.println("AmiConnection Already Present");
					        		Date date = new Date(System.currentTimeMillis());
									Errors error = new Errors();
									
									error.setData(amiConnection.toString());
									error.setError("Custom Error");
									error.setErrorClass("BulkUploadAMIConnectionToDatabase");
									error.setFunctionality("AmiConnection Already Present");
									error.setCreatedDate(date);
									error.setOrganization(amiConnection.getOrganization());
									errorRepository.save(error);
						        }

					        }
		        	}
		        	else
		        	{
		        		//Employee is to be added
		        		System.out.println("New AmiConnection to be added here");
		        		
		        		//Default values will go here

		        		amiConnection.setIsactive(true);;
		        		amiConnection.setPort(5038);
		        		amiConnections.add(amiConnection);
		        	}

		        }
		        else
		        {
		
		        	Date date = new Date(System.currentTimeMillis());
					Errors error = new Errors();
					error.setData(amiConnection.toString());
					error.setError("Custom Error");
					error.setErrorClass("BulkUploadAMIConnectionToDatabase");
					error.setFunctionality("AmiConnection Of Not This Organization");
					error.setCreatedDate(date);
					error.setOrganization(amiConnection.getOrganization());
					errorRepository.save(error);
		        }
		        
	        }
	        else
	        {
	        	//AmiConnection Organization Cannot be null or end of rows
	        	System.out.println("AmiConnection Organization Cannot be null or end of rows");
	        	break;
	         }
	       

	        rowNumber = rowNumber+1;
	      }

	      System.out.println("Before Closing Workbook");
	      workbook.close();

	      return amiConnections;
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

