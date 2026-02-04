//package com.mylinehub.crm.exports.excel;
//
//import java.io.IOException;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import com.mylinehub.crm.entity.CallingCost;
//import com.mylinehub.crm.exports.ExcelColumnsHeaderWriter;
//
//import lombok.AllArgsConstructor;
//
//
///**
// * @author Anand Goel
// * @version 1.0
// */
//@AllArgsConstructor
//public class ExportCallingCostToXLSX implements ExcelColumnsHeaderWriter {
//
//    /**
//     * an array that stores the content of the headers in columns
//     */
//	private static final String[] columns = {"Id","Extension","Amount","Call Calculations","Organization","Remarks"};
//	private final List<CallingCost> callingcosts;
//
//    /**
//     * The method completes the sheet with data
//     * @param sheet sheet to be filled with data
//     */
//    private void writeCellsData(Sheet sheet) {
//        int rowNum = 1;
//        for (CallingCost callingcost : callingcosts) {
//            Row row = sheet.createRow(rowNum++);
//            row.createCell(0).setCellValue(callingcost.getId());
//            row.createCell(1).setCellValue(callingcost.getExtension());
//            row.createCell(2).setCellValue(callingcost.getAmount());
//            row.createCell(3).setCellValue(callingcost.getCallcalculation());
//            row.createCell(4).setCellValue(callingcost.getOrganization());
//            row.createCell(5).setCellValue(callingcost.getRemarks());
//            
//            // Resize all columns to fit the content size
//            for (int i = 0; i < columns.length; i++) {
//                sheet.autoSizeColumn(i);
//            }
//        }
//    }
//
//    /**
//     * The method allows you to create a file and export it
//     * @param response response responsible for the ability to download the file
//     * @throws IOException exception thrown in case of erroneous data
//     */
//    public void export(HttpServletResponse response) throws IOException {
//        Workbook workbook = new XSSFWorkbook();
//
//        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
//        String currentDateTime = dateFormatter.format(new Date());
//        String headerValue = "callingcost_" + currentDateTime + ".xlsx";
//        Sheet sheet = workbook.createSheet(headerValue);
//
//        writeColumnsHeader(workbook, sheet, columns);
//        writeCellsData(sheet);
//
//        workbook.write(response.getOutputStream());
//        workbook.close();
//    }
//
//}
