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
import com.mylinehub.crm.entity.Queue;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.service.QueueService;


public class BulkUploadQueueToDatabase {

	  
	   /**
  * an array that stores the content of the headers in columns
  */
	//private static final String[] columns = {"Phone Context", "Organization","Extension","Domain"};
	    
	 String SHEET = "Queues";
 
	  public List<Queue> excelToQueues(QueueService QueueService,InputStream is,String organization, ErrorRepository errorRepository) throws Exception {
		 
	      
		  Workbook workbook = new XSSFWorkbook(is);
		  
	    try {
	    //	System.out.println("I am inside try of excel to Employee BulkUploadClass");
	    	
	      
	    	
	      Sheet sheet = workbook.getSheet(SHEET);
	      Iterator<Row> rows = sheet.iterator();

	      //System.out.println("Does row have next value"+rows.hasNext());
	    	
	      
	      List<Queue> queues = new ArrayList<Queue>();

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
	    	
	    	  
	        Queue queue = new Queue();

	        int cellIdx = 0;
	        while (cellsInRow.hasNext()) {
	        	
	        
	        	
	          Cell currentCell = cellsInRow.next();

	         // System.out.println("Row Index : "+currentCell.getRowIndex());
	         // System.out.println("Cell Address : "+currentCell.getAddress());
	          //System.out.println("Cell Type : "+currentCell.getCellType());
	         
	          switch (cellIdx) {
	        
	          case 0:queue.setPhoneContext((String)currentCell.getStringCellValue());
	          System.out.println("1.Phone Context : "+String.valueOf(currentCell.getStringCellValue()));
	          
	                 
	          break;
	          case 1:queue.setOrganization((String)currentCell.getStringCellValue());  
	          System.out.println("2.Organization : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 2:
	        	  
	        	  try
	        	  {
	        		  queue.setExtension(String.valueOf((int)currentCell.getNumericCellValue())); 
	        		  System.out.println("3.Numberic Extension : "+String.valueOf(currentCell.getNumericCellValue()));
	        	  }
	        	  catch(Exception e)
	        	  {
	        		  if(e.getMessage().contains("Cannot get"))
	        		  {
	        			  queue.setExtension(String.valueOf(currentCell.getStringCellValue())); 
		        		  System.out.println("3.String Extension : "+String.valueOf(currentCell.getStringCellValue()));

	        		  }
	        		  else
	        		  {
	        			  throw new Exception("Exception while setting extension");
	        		  }
	        	  }
	        	       
	        	  
	        	  
	          break;
	          case 3:
	        	  queue.setDomain((String)currentCell.getStringCellValue());  
		          System.out.println("4.Domain : "+String.valueOf(currentCell.getStringCellValue())); 
	        	  break;
	        
	          case 4:
	        	  queue.setName((String)currentCell.getStringCellValue());  
		          System.out.println("5.Name : "+String.valueOf(currentCell.getStringCellValue())); 
	        	  break;
				  
				  
			  case 5:
	        	  queue.setType((String)currentCell.getStringCellValue());  
		          System.out.println("6.Type : "+String.valueOf(currentCell.getStringCellValue())); 
	        	  break;	  

	          default:
	        	  
	        	  System.out.println("Default");			      
	            break;
	          }

	          ++cellIdx;
	        }
	        
	        System.out.println("Out Of Row");

	        if(queue.getOrganization() != null)
	        {
	        	System.out.println("Organization not null");
	        	
	        	 
		        if(queue.getOrganization().equals(organization))
		        {
		        	Queue verificationQueue = QueueService.getQueueByExtensionAndOrganization(queue.getExtension(), organization);
			        
		        	if(verificationQueue !=null)
		        	{
	  
					        if(verificationQueue.getExtension() ==null ||verificationQueue.getExtension().isBlank() || queue.getExtension().isEmpty() ||  queue.getExtension().equals(""))
					        {
					        	//Email ID is required for user to get uploaded
					        	System.out.println("Extention is null");
					        	throw new Exception("Extension Cannot be null");
					        }
					        else
					        {
					        	if(verificationQueue != null && !verificationQueue.getExtension().isEmpty() && verificationQueue.getExtension().trim().equals(queue.getExtension().trim()))
						        {
						        	//Employee Already present. He should update this details from portal
					        		//System.out.println("Queue Already Present");
					        		Date date = new Date(System.currentTimeMillis());
									Errors error = new Errors();
									
									error.setData(queue.toString());
									error.setError("Custom Error");
									error.setErrorClass("BulkUploadQueueToDatabase");
									error.setFunctionality("Queue Already Present");
									error.setCreatedDate(date);
									error.setOrganization(queue.getOrganization());
									errorRepository.save(error);
									
						        }

					        }
		        	}
		        	else
		        	{
		        		//Employee is to be added
		        		System.out.println("New Queue to be added here");
		        		
		        		//Default values will go here

		        		queue.setIsactive(true);;
		        		queue.setProtocol("PJSIP/");
		        		queues.add(queue);
		        	}

		        }
		        else
		        {
		        	//System.out.println("Queue Of Not This Organization");
		        	
		        	Date date = new Date(System.currentTimeMillis());
					Errors error = new Errors();
					error.setData(queue.toString());
					error.setError("Custom Error");
					error.setErrorClass("BulkUploadQueueToDatabase");
					error.setFunctionality("Queue Of Not This Organization");
					error.setCreatedDate(date);
					error.setOrganization(queue.getOrganization());
					errorRepository.save(error);
		        }
		        
	        }
	        else
	        {
	        	//Queue Organization Cannot be null or end of rows
	        	System.out.println("Queue Organization Cannot be null or end of rows");
	        	break;
	         }
	       

	        rowNumber = rowNumber+1;
	      }

	      System.out.println("Before Closing Workbook");
	      workbook.close();

	      return queues;
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

