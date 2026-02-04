package com.mylinehub.crm.exports.excel;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mylinehub.crm.entity.Conference;
import com.mylinehub.crm.exports.ExcelColumnsHeaderWriter;

import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */
@AllArgsConstructor
public class ExportConferenceToXLSX implements ExcelColumnsHeaderWriter {

    /**
     * an array that stores the content of the headers in columns
     */
	private static final String[] columns = {"Id", "Conference Extension","Conference Name","Domain","Organization","Phone Context", "Owner","Bridge","User Profile","Menu","Is Dynamic","Is Room Active", "Is Conference Active"};
	private final List<Conference> conferences;

    /**
     * The method completes the sheet with data
     * @param sheet sheet to be filled with data
     */
    private void writeCellsData(Sheet sheet) {
        int rowNum = 1;
        for (Conference conference : conferences) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(conference.getId());
            row.createCell(1).setCellValue(conference.getConfextension());
            row.createCell(2).setCellValue(conference.getConfname());
            row.createCell(3).setCellValue(conference.getDomain());
            row.createCell(4).setCellValue(conference.getOrganization());
            row.createCell(5).setCellValue(conference.getPhonecontext());  
            row.createCell(6).setCellValue(conference.getOwner());
            row.createCell(7).setCellValue(conference.getBridge());
            row.createCell(8).setCellValue(conference.getUserprofile());
            row.createCell(9).setCellValue(conference.getMenu());
            row.createCell(10).setCellValue(conference.isIsdynamic());
            row.createCell(11).setCellValue(conference.isIsroomactive());
            row.createCell(12).setCellValue(conference.isIsconferenceactive());
            
            
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
        String headerValue = "conference_" + currentDateTime + ".xlsx";
        Sheet sheet = workbook.createSheet(headerValue);

        writeColumnsHeader(workbook, sheet, columns);
        writeCellsData(sheet);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

}