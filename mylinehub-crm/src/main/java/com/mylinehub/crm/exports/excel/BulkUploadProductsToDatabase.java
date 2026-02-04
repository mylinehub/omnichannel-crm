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
import com.mylinehub.crm.entity.Product;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.service.ProductService;


public class BulkUploadProductsToDatabase {

	  
	   /**
  * an array that stores the content of the headers in columns
  */
	//private static final String[] columns = {"Phone Context", "Organization","Extension","Domain"};
	    
	 String SHEET = "Products";
 
	  public List<Product> excelToProducts(ProductService productsService,InputStream is,String organization, ErrorRepository errorRepository) throws Exception {
		 
	      
		  Workbook workbook = new XSSFWorkbook(is);
		  
	    try {
	    //	System.out.println("I am inside try of excel to Employee BulkUploadClass");
	    	
	      
	    	
	      Sheet sheet = workbook.getSheet(SHEET);
	      Iterator<Row> rows = sheet.iterator();

	      //System.out.println("Does row have next value"+rows.hasNext());
	    	
	      
	      List<Product> products = new ArrayList<Product>();

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
	    	
	    	  
	        Product product = new Product();

	        int cellIdx = 0;
	        while (cellsInRow.hasNext()) {
	        	
	        
	        	
	          Cell currentCell = cellsInRow.next();

	         // System.out.println("Row Index : "+currentCell.getRowIndex());
	         // System.out.println("Cell Address : "+currentCell.getAddress());
	          //System.out.println("Cell Type : "+currentCell.getCellType());
	         
	          switch (cellIdx) {
	        
	          case 0:product.setName(currentCell.getStringCellValue());
	          System.out.println("1.Product Name : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 1:product.setProductStringType((currentCell.getStringCellValue()));
	          System.out.println("2.Product Type : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 2:product.setUnits((currentCell.getStringCellValue()));
	          System.out.println("3.Product Unit : "+String.valueOf(currentCell.getStringCellValue()));
	          break;
	          case 3:product.setSellingPrice((currentCell.getNumericCellValue()));
	          System.out.println("4.Product Selling Price : "+String.valueOf(currentCell.getNumericCellValue()));
	          break;
	          case 4:product.setPurchasePrice((currentCell.getNumericCellValue()));
	          System.out.println("5.Product Purchase Price : "+String.valueOf(currentCell.getNumericCellValue()));
	          break;
	          case 5:product.setTaxRate((currentCell.getNumericCellValue()));
	          System.out.println("6.Product Tax Rate : "+String.valueOf(currentCell.getNumericCellValue()));
	          break;
	          case 6:product.setOrganization((currentCell.getStringCellValue()));
	          System.out.println("7.Organization : "+String.valueOf(currentCell.getStringCellValue()));
	          
	          break;
	         
	          default:
	        	  
	        	  System.out.println("Default");			      
	            break;
	          }

	          ++cellIdx;
	        }
	        
	        System.out.println("Out Of Row");

	        if(product.getOrganization() != null)
	        {
	        	System.out.println("Organization not null");
	        	
	        	 
		        if(product.getOrganization().equals(organization))
		        {
		        	Product verificationProduct = productsService.getProductByNameAndOrganization(product.getName(),organization);
			        
		        	if(verificationProduct !=null)
		        	{
	  
					        if(verificationProduct.getName() ==null ||verificationProduct.getName().isBlank() || product.getName().isEmpty() ||  product.getName().equals(""))
					        {
					        	//Email ID is required for user to get uploaded
					        	System.out.println("Extention is null");
					        	throw new Exception("Extension Cannot be null");
					        }
					        else
					        {
					        	if(verificationProduct != null && !verificationProduct.getName().isEmpty() && verificationProduct.getName().trim().equals(product.getName().trim()))
						        {
						        	//Employee Already present. He should update this details from portal
					        		//System.out.println("Products Already Present");
					        		
					        		Date date = new Date(System.currentTimeMillis());
									Errors error = new Errors();
									
									error.setData(product.toString());
									error.setError("Custom Error");
									error.setErrorClass("BulkUploadProductToDatabase");
									error.setFunctionality("Products Already Present");
									error.setCreatedDate(date);
									error.setOrganization(product.getOrganization());
									errorRepository.save(error);
						        }

					        }
		        	}
		        	else
		        	{
		        		//Employee is to be added
		        		System.out.println("New Products to be added here");
		        		
		        		//Default values will go here

		        		
		        		//product.setProtocol("PJSIP/");
		        		products.add(product);
		        	}

		        }
		        else
		        {
		        	//System.out.println("Products Of Not This Organization");
		        	Date date = new Date(System.currentTimeMillis());
					Errors error = new Errors();
					error.setData(product.toString());
					error.setError("Custom Error");
					error.setErrorClass("BulkUploadProductToDatabase");
					error.setFunctionality("Products Of Not This Organization");
					error.setCreatedDate(date);
					error.setOrganization(product.getOrganization());
					errorRepository.save(error);
		        }
		        
	        }
	        else
	        {
	        	//Products Organization Cannot be null or end of rows
	        	System.out.println("Products Organization Cannot be null or end of rows");
	        	break;
	         }
	       

	        rowNumber = rowNumber+1;
	      }

	      System.out.println("Before Closing Workbook");
	      workbook.close();

	      return products;
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

