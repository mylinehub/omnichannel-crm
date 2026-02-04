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

import com.mylinehub.crm.entity.Conference;
import com.mylinehub.crm.entity.Errors;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.service.ConferenceService;


public class BulkUploadConferenceToDatabase {

	  
	   /**
  * an array that stores the content of the headers in columns
  */
	//private static final String[] columns = {"Phone Context", "Organization","Extension","Domain"};
	    
	 String SHEET = "Conferences";
 
	  public List<Conference> excelToConferences(ConferenceService ConferenceService,InputStream is,String organization,ErrorRepository errorRepository) throws Exception {
		 
	      
		  Workbook workbook = new XSSFWorkbook(is);
		  
	    try {
	    //	System.out.println("I am inside try of excel to Employee BulkUploadClass");
	    	
	      
	    	
	      Sheet sheet = workbook.getSheet(SHEET);
	      Iterator<Row> rows = sheet.iterator();

	      //System.out.println("Does row have next value"+rows.hasNext());
	    	
	      
	      List<Conference> conferences = new ArrayList<Conference>();

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
	    	
	    	  
	        Conference conference = new Conference();

	        int cellIdx = 0;
	        while (cellsInRow.hasNext()) {
	        	
	        
	        	
	          Cell currentCell = cellsInRow.next();

	         // System.out.println("Row Index : "+currentCell.getRowIndex());
	         // System.out.println("Cell Address : "+currentCell.getAddress());
	          //System.out.println("Cell Type : "+currentCell.getCellType());
	         
	          switch (cellIdx) {
	        
	          case 0:
	        	  
	          try
        	  {
	        	  conference.setConfextension(String.valueOf((int)currentCell.getNumericCellValue())); 
        		  System.out.println("1.Numberic Conference Extension : "+String.valueOf(currentCell.getNumericCellValue()));
        	  }
        	  catch(Exception e)
        	  {
        		  if(e.getMessage().contains("Cannot get"))
        		  {
        			  conference.setConfextension(String.valueOf(currentCell.getStringCellValue())); 
	        		  System.out.println("1.String Conference Extension : "+String.valueOf(currentCell.getStringCellValue()));

        		  }
        		  else
        		  {
        			  throw new Exception("Exception while setting Conference extension");
        		  }
        	  }
        	  
	          
	          break;
	          case 1:conference.setConfname((String)currentCell.getStringCellValue());
	          System.out.println("2.Conference Name : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 2:conference.setDomain((String)currentCell.getStringCellValue());
	          System.out.println("3.Domain : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 3:conference.setOrganization((String)currentCell.getStringCellValue());
	          System.out.println("4.Organization : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 4:conference.setPhonecontext((String)currentCell.getStringCellValue());
	          System.out.println("5.Phone Context : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 5:conference.setBridge((String)currentCell.getStringCellValue());
	          System.out.println("6.Bridge : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 6:conference.setUserprofile((String)currentCell.getStringCellValue());
	          System.out.println("7.User Profile : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 7:conference.setMenu((String)currentCell.getStringCellValue());
	          System.out.println("8.Menu : "+String.valueOf(currentCell.getStringCellValue()));
	          
	          break;
	          

	          default:
	        	  
	        	  System.out.println("Default");			      
	            break;
	          }

	          ++cellIdx;
	        }
	        
	        System.out.println("Out Of Row");

	        if(conference.getOrganization() != null)
	        {
	        	System.out.println("Organization not null");
	        	
	        	 
		        if(conference.getOrganization().equals(organization))
		        {
		        	Conference verificationConference = ConferenceService.getConferenceByConfextensionAndOrganization(conference.getConfextension(), organization);
			        
		        	if(verificationConference !=null)
		        	{
	  
					        if(verificationConference.getConfextension() ==null ||verificationConference.getConfextension().isBlank() || conference.getConfextension().isEmpty() ||  conference.getConfextension().equals(""))
					        {
					        	//Email ID is required for user to get uploaded
					        	System.out.println("Extention is null");
					        	throw new Exception("Extension Cannot be null");
					        }
					        else
					        {
					        	if(verificationConference != null && !verificationConference.getConfextension().isEmpty() && verificationConference.getConfextension().trim().equals(conference.getConfextension().trim()))
						        {
						        	//Employee Already present. He should update this details from portal
					        		//System.out.println("Conference Already Present");
					        		Date date = new Date(System.currentTimeMillis());
									Errors error = new Errors();
									
									error.setData(conference.toString());
									error.setError("Custom Error");
									error.setErrorClass("BulkUploadConferenceToDatabase");
									error.setFunctionality("Conference Already Present");
									error.setCreatedDate(date);
									error.setOrganization(conference.getOrganization());
									errorRepository.save(error);
						        }

					        }
		        	}
		        	else
		        	{
		        		//Employee is to be added
		        		System.out.println("New Conference to be added here");
		        		
		        		//Default values will go here

		        		conference.setIsdynamic(false);
		        		conference.setIsroomactive(false);
		        		conference.setProtocol("PJSIP/");
		        		conferences.add(conference);
		        	}

		        }
		        else
		        {
		        	//System.out.println("Conference Of Not This Organization");
		        	Date date = new Date(System.currentTimeMillis());
					Errors error = new Errors();
					error.setData(conference.toString());
					error.setError("Custom Error");
					error.setErrorClass("BulkUploadConferenceToDatabase");
					error.setFunctionality("Conference Of Not This Organization");
					error.setCreatedDate(date);
					error.setOrganization(conference.getOrganization());
					errorRepository.save(error);
		        }
		        
	        }
	        else
	        {
	        	//Conference Organization Cannot be null or end of rows
	        	System.out.println("Conference Organization Cannot be null or end of rows");
	        	break;
	         }
	       

	        rowNumber = rowNumber+1;
	      }

	      System.out.println("Before Closing Workbook");
	      workbook.close();

	      return conferences;
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

