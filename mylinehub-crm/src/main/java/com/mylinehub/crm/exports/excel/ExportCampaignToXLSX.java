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

import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.exports.ExcelColumnsHeaderWriter;

import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */
@AllArgsConstructor
public class ExportCampaignToXLSX implements ExcelColumnsHeaderWriter {

    /**
     * an array that stores the content of the headers in columns
     */
	private static final String[] columns = {"ID","Organization","Domain","Name","Description","Is Active","Time Zome","Start Date","End Date","Start Time","End Time","Phone Context","Is On Mobile", "Admin ID","Country","Busines","Auto Dialer Type","Ivr Extension","Conf Extension","Queue Extension"};
	private final List<Campaign> campaigns;

    /**
     * The method completes the sheet with data
     * @param sheet sheet to be filled with data
     */
    private void writeCellsData(Sheet sheet) {
        int rowNum = 1;
        for (Campaign campaign : campaigns) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(campaign.getId());
            row.createCell(1).setCellValue(campaign.getOrganization());
            row.createCell(2).setCellValue(campaign.getDomain());
            row.createCell(3).setCellValue(campaign.getName());
            row.createCell(4).setCellValue(campaign.getDescription());
            row.createCell(5).setCellValue(campaign.isIsactive());  
            row.createCell(6).setCellValue(campaign.getTimezone().getDisplayName());
            row.createCell(7).setCellValue(campaign.getStartdate());
            row.createCell(8).setCellValue(campaign.getEnddate());
            row.createCell(9).setCellValue(campaign.getStarttime().toString());
            row.createCell(10).setCellValue(campaign.getEndtime().toString());
            row.createCell(11).setCellValue(campaign.getPhonecontext());
            row.createCell(12).setCellValue(campaign.isIsonmobile());
            row.createCell(13).setCellValue(campaign.getManager().getId());
            row.createCell(14).setCellValue(campaign.getCountry());
            row.createCell(15).setCellValue(campaign.getBusiness());
            row.createCell(16).setCellValue(campaign.getAutodialertype());
            
            row.createCell(17).setCellValue(campaign.getIvrExtension());
            row.createCell(18).setCellValue(campaign.getConfExtension());
            row.createCell(19).setCellValue(campaign.getQueueExtension());
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
        String headerValue = "campaign_" + currentDateTime + ".xlsx";
        Sheet sheet = workbook.createSheet(headerValue);

        writeColumnsHeader(workbook, sheet, columns);
        writeCellsData(sheet);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

}
