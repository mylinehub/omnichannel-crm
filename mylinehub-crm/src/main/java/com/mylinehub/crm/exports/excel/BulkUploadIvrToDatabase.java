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

import com.mylinehub.crm.entity.Errors;
import com.mylinehub.crm.entity.Ivr;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.service.IvrService;


public class BulkUploadIvrToDatabase {

	  
	   /**
  * an array that stores the content of the headers in columns
  */
	//private static final String[] columns = {"Phone Context", "Organization","Extension","Domain"};
	    
	 String SHEET = "ivrs";
 
	  public List<Ivr> excelToIvrs(IvrService ivrService,InputStream is,String organization,ErrorRepository errorRepository) throws Exception {
		 
	      
		  Workbook workbook = new XSSFWorkbook(is);
		  
	    try {
	    //	System.out.println("I am inside try of excel to Employee BulkUploadClass");
	    	
	      
	    	
	      Sheet sheet = workbook.getSheet(SHEET);
	      Iterator<Row> rows = sheet.iterator();

	      //System.out.println("Does row have next value"+rows.hasNext());
	    	
	      
	      List<Ivr> ivrs = new ArrayList<Ivr>();

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
	    	
	    	  
	        Ivr ivr = new Ivr();

	        int cellIdx = 0;
	        while (cellsInRow.hasNext()) {
	        	
	        
	        	
	          Cell currentCell = cellsInRow.next();

	         // System.out.println("Row Index : "+currentCell.getRowIndex());
	         // System.out.println("Cell Address : "+currentCell.getAddress());
	          //System.out.println("Cell Type : "+currentCell.getCellType());
	         
	          switch (cellIdx) {
	        
	          case 0:ivr.setPhoneContext((String)currentCell.getStringCellValue());
	          System.out.println("1.Phone Context : "+String.valueOf(currentCell.getStringCellValue()));
	          
	                 
	          break;
	          case 1:ivr.setOrganization((String)currentCell.getStringCellValue());  
	          System.out.println("2.Organization : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 2:
	        	  
	        	  try
	        	  {
	        		  ivr.setExtension(String.valueOf((int)currentCell.getNumericCellValue())); 
	        		  System.out.println("3.Numberic Extension : "+String.valueOf(currentCell.getNumericCellValue()));
	        	  }
	        	  catch(Exception e)
	        	  {
	        		  if(e.getMessage().contains("Cannot get"))
	        		  {
	        			  ivr.setExtension(String.valueOf(currentCell.getStringCellValue())); 
		        		  System.out.println("3.String Extension : "+String.valueOf(currentCell.getStringCellValue()));

	        		  }
	        		  else
	        		  {
	        			  throw new Exception("Exception while setting extension");
	        		  }
	        	  }
	        	       
	        	  
	        	  
	          break;
	          case 3:
	        	  ivr.setDomain((String)currentCell.getStringCellValue());  
		          System.out.println("4.Domain : "+String.valueOf(currentCell.getStringCellValue())); 
	        	  break;
	        

	          default:
	        	  
	        	  System.out.println("Default");			      
	            break;
	          }

	          ++cellIdx;
	        }
	        
	        System.out.println("Out Of Row");

	        if(ivr.getOrganization() != null)
	        {
	        	System.out.println("Organization not null");
	        	
	        	 
		        if(ivr.getOrganization().equals(organization))
		        {
		        	Ivr verificationivr = ivrService.getIvrByExtensionAndOrganization(ivr.getExtension(), organization);
			        
		        	if(verificationivr !=null)
		        	{
	  
					        if(verificationivr.getExtension() ==null ||verificationivr.getExtension().isBlank() || ivr.getExtension().isEmpty() ||  ivr.getExtension().equals(""))
					        {
					        	//Email ID is required for user to get uploaded
					        	System.out.println("Extention is null");
					        	throw new Exception("Extension Cannot be null");
					        }
					        else
					        {
					        	if(verificationivr != null && !verificationivr.getExtension().isEmpty() && verificationivr.getExtension().trim().equals(ivr.getExtension().trim()))
						        {
						        	//Employee Already present. He should update this details from portal
					        		//System.out.println("IVR Already Present");
					        		Date date = new Date(System.currentTimeMillis());
									Errors error = new Errors();
								
									error.setData(ivr.toString());
									error.setError("Custom Error");
									error.setErrorClass("BulkUploadIvrToDatabase");
									error.setFunctionality("IVR Already Present");
									error.setCreatedDate(date);
									error.setOrganization(ivr.getOrganization());
									errorRepository.save(error);
						        }

					        }
		        	}
		        	else
		        	{
		        		//Employee is to be added
		        		System.out.println("New IVR to be added here");
		        		
		        		//Default values will go here

		        		ivr.setIsactive(true);;
		        		ivr.setProtocol("PJSIP/");
		        		ivrs.add(ivr);
		        	}

		        }
		        else
		        {
		        	//System.out.println("IVR Of Not This Organization");
		        	Date date = new Date(System.currentTimeMillis());
					Errors error = new Errors();
					error.setData(ivr.toString());
					error.setError("Custom Error");
					error.setErrorClass("BulkUploadIvrToDatabase");
					error.setFunctionality("IVR Of Not This Organization");
					error.setCreatedDate(date);
					error.setOrganization(ivr.getOrganization());
					errorRepository.save(error);
		        }
		        
	        }
	        else
	        {
	        	//Ivr Organization Cannot be null or end of rows
	        	System.out.println("IVR Organization Cannot be null or end of rows");
	        	break;
	         }
	       

	        rowNumber = rowNumber+1;
	      }

	      System.out.println("Before Closing Workbook");
	      workbook.close();

	      return ivrs;
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

