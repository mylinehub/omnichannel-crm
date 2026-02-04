package com.mylinehub.crm.exports.excel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mylinehub.crm.entity.Errors;
import com.mylinehub.crm.entity.Supplier;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.service.SupplierService;


public class BulkUploadSuppliersToDatabase {
	
	   /**
  * an array that stores the content of the headers in columns
  */
	//private static final String[] columns = {"Phone Context", "Organization","Extension","Domain"};
	    
	 String SHEET = "Suppliers";
 
	  public List<Supplier> excelToSuppliers(SupplierService suppliersService,InputStream is,String organization,ErrorRepository errorRepository) throws Exception {
		 
	      
		  Workbook workbook = new XSSFWorkbook(is);
		  
	    try {
	    //	System.out.println("I am inside try of excel to Employee BulkUploadClass");
	    	
	      
	    	
	      Sheet sheet = workbook.getSheet(SHEET);
	      Iterator<Row> rows = sheet.iterator();

	      //System.out.println("Does row have next value"+rows.hasNext());
	    	
	      
	      List<Supplier> suppliers = new ArrayList<Supplier>();

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
	    	
	    	  
	        Supplier supplier = new Supplier();

	        int cellIdx = 0;
	        while (cellsInRow.hasNext()) {
	        	
	        
	        	
	          Cell currentCell = cellsInRow.next();

	         // System.out.println("Row Index : "+currentCell.getRowIndex());
	         // System.out.println("Cell Address : "+currentCell.getAddress());
	          //System.out.println("Cell Type : "+currentCell.getCellType());
	         
	          switch (cellIdx) {
	        
	          case 0:supplier.setSupplierName((String)currentCell.getStringCellValue());
	          System.out.println("1.Name : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 1:supplier.setSuppliertype((String)currentCell.getStringCellValue());
	          System.out.println("2.Supplier Type : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          
	          case 2:supplier.setModeOfTransport((String)currentCell.getStringCellValue());
	          System.out.println("3. Mode Of Transport : "+String.valueOf(currentCell.getStringCellValue()));
	           break;
	          
	          case 3:supplier.setPriceunits((String)currentCell.getStringCellValue());
	          System.out.println("4.Price Unit : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          
	          case 4:supplier.setWeightunit((String)currentCell.getStringCellValue());
	          System.out.println("5.Weight Unit : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 5:supplier.setLengthunit((String)currentCell.getStringCellValue());
	          System.out.println("6.Length Unit : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          
	          case 6:
	          try
        	  {
	        	  supplier.setTransportcapacity(String.valueOf((int) currentCell.getNumericCellValue())); 
		          System.out.println("7. Numeric Capaciity : "+String.valueOf((int)currentCell.getNumericCellValue()));
	          }
        	  catch(Exception e)
        	  {
        		  if(e.getMessage().contains("Cannot get"))
        		  {
        			  supplier.setTransportcapacity(String.valueOf(Double.parseDouble(currentCell.getStringCellValue().trim()))); 
    		          System.out.println("7. String Capacity : "+String.valueOf(currentCell.getStringCellValue()));
     
        		  }
        		  else
        		  {
        			  throw new Exception("Exception while setting salary");
        		  }
        	  }
	          
	          break;
	          case 7:supplier.setOrganization((String)currentCell.getStringCellValue());
	          System.out.println("8.Organization : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          
	          case 8:
		          
	        	  try
	        	  {
	        		  String phoneNumber="+"+String.valueOf(currentCell.getNumericCellValue()).replaceAll(Pattern.quote("."),"");
	  	        	
		        	  int positionOfE = phoneNumber.indexOf('E');
		        	  
		        	  phoneNumber= phoneNumber.substring(0, positionOfE);
		        			  
		        	  supplier.setSupplierPhoneNumber(phoneNumber); 
	        		  System.out.println("8.Numberic Phone Number : +"+phoneNumber);
	        	  
	        	  
	        	  }
	        	  catch(Exception e)
	        	  {
	        		  if(e.getMessage().contains("Cannot get"))
	        		  {
	        			  supplier.setSupplierPhoneNumber(String.valueOf(currentCell.getStringCellValue())); 
		        		  System.out.println("8.String Phone number : "+String.valueOf(currentCell.getStringCellValue()));

	        		  }
	        		  else
	        		  {
	        			  throw new Exception("Exception while setting phone number");
	        		  }
	        	  }
	        	  
	          default:
	        	  
	        	  System.out.println("Default");			      
	            break;
	          }

	          ++cellIdx;
	        }
	        
	        System.out.println("Out Of Row");

	        if(supplier.getOrganization() != null)
	        {
	        	System.out.println("Organization not null");
	        	
	        	 
		        if(supplier.getOrganization().equals(organization))
		        {
		        	Supplier verificationSupplier = suppliersService.getSupplierByNameAndOrganization(supplier.getSupplierName(), organization);
			        
		        	if(verificationSupplier !=null)
		        	{
	  
					        if(verificationSupplier.getSupplierName()==null ||verificationSupplier.getSupplierName().isBlank() || supplier.getSupplierName().isEmpty() ||  supplier.getSupplierName().equals(""))
					        {
					        	//Email ID is required for user to get uploaded
					        	System.out.println("Extention is null");
					        	throw new Exception("Extension Cannot be null");
					        }
					        else
					        {
					        	if(verificationSupplier != null && !verificationSupplier.getSupplierName().isEmpty() && verificationSupplier.getSupplierName().trim().equals(supplier.getSupplierName().trim()))
						        {
						        	//Employee Already present. He should update this details from portal
					        		//System.out.println("Suppliers Already Present");
					        		
					        		Date date = new Date(System.currentTimeMillis());
									Errors error = new Errors();
									error.setData(supplier.toString());
									error.setError("Custom Error");
									error.setErrorClass("BulkUploadSuppliersToDatabase");
									error.setFunctionality("Suppliers Already Present");
									error.setCreatedDate(date);
									error.setOrganization(supplier.getOrganization());
									errorRepository.save(error);
									
						        }

					        }
		        	}
		        	else
		        	{
		        		//Employee is to be added
		        		System.out.println("New Suppliers to be added here");
		        		
		        		//Default values will go here

		        		supplier.setActivityStatus("Active");
		        		suppliers.add(supplier);
		        	}

		        }
		        else
		        {
		        	//System.out.println("Suppliers Of Not This Organization");
		        	
		        	Date date = new Date(System.currentTimeMillis());
					Errors error = new Errors();
					error.setData(supplier.toString());
					error.setError("Custom Error");
					error.setErrorClass("BulkUploadSuppliersToDatabase");
					error.setFunctionality("Suppliers Of Not This Organization");
					error.setCreatedDate(date);
					error.setOrganization(supplier.getOrganization());
					errorRepository.save(error);
		        }
		        
	        }
	        else
	        {
	        	//Suppliers Organization Cannot be null or end of rows
	        	System.out.println("Suppliers Organization Cannot be null or end of rows");
	        	break;
	         }
	       

	        rowNumber = rowNumber+1;
	      }

	      System.out.println("Before Closing Workbook");
	      workbook.close();

	      return suppliers;
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

