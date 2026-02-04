package com.mylinehub.crm.exports.excel;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.exports.ExcelColumnsHeaderWriter;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Anand Goel
 * @version 1.0
 */
@AllArgsConstructor
public class ExportEmployeeToXLSX implements ExcelColumnsHeaderWriter {

    /**
     * an array that stores the content of the headers in columns
     */
	private static final String[] columns = {"Id", "First Name", "Last Name", "Department", "Role", "Salary", "Email", "Phone Context", "Organization", "Domain", "Extension", "Extension Password","Time Zone","Type","Call On Mobile","Phone Number","Transfer Phone 1","Transfer Phone 2","Provider 1","Alotted Number 1","Provider 2","Alotted Number 2","Cost Calculation","Amount"};
	private final List<Employee> employees;

    /**
     * The method completes the sheet with data
     * @param sheet sheet to be filled with data
     */
    private void writeCellsData(Sheet sheet) {
        int rowNum = 1;
        for (Employee employee : employees) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(employee.getId());
            row.createCell(1).setCellValue(employee.getFirstName());
            row.createCell(2).setCellValue(employee.getLastName());
            row.createCell(3).setCellValue(employee.getDepartment().getDepartmentName());
            row.createCell(4).setCellValue(employee.getUserRole().toString().toLowerCase());
            row.createCell(5).setCellValue(employee.getSalary());  
            row.createCell(6).setCellValue(employee.getEmail());
            row.createCell(7).setCellValue(employee.getPhoneContext());
            row.createCell(8).setCellValue(employee.getOrganization());
            row.createCell(9).setCellValue(employee.getDomain());
            row.createCell(10).setCellValue(employee.getExtension());
            row.createCell(11).setCellValue(employee.getExtensionpassword());
            row.createCell(12).setCellValue(employee.getTimezone().getDisplayName());
            row.createCell(13).setCellValue(employee.getType());
            row.createCell(14).setCellValue(employee.isCallonnumber());
            row.createCell(15).setCellValue(employee.getPhonenumber());
            row.createCell(16).setCellValue(employee.getTransfer_phone_1());
            row.createCell(17).setCellValue(employee.getTransfer_phone_2());
            
            row.createCell(18).setCellValue(employee.getProvider1());
            row.createCell(19).setCellValue(employee.getAllotednumber1());
            row.createCell(20).setCellValue(employee.getProvider2());
            row.createCell(21).setCellValue(employee.getAllotednumber2());
            
            row.createCell(22).setCellValue(employee.getCostCalculation());
            row.createCell(23).setCellValue(employee.getAmount());
            // Resize all columns to fit the content size
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
        }
    }

    /**
     * The method allows you to create a file and export it
     * @param response response responsible for the ability to download the file
     * @throws IOException exception thrown in case of erroneous data
     */
    public void export(HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateTime = dateFormatter.format(new Date());
        String headerValue = "employees_" + currentDateTime + ".xlsx";
        Sheet sheet = workbook.createSheet(headerValue);

        writeColumnsHeader(workbook, sheet, columns);
        writeCellsData(sheet);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

}
