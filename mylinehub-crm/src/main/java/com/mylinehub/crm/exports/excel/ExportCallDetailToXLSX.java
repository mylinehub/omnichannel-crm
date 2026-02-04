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

import com.mylinehub.crm.entity.CallDetail;
import com.mylinehub.crm.exports.ExcelColumnsHeaderWriter;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@AllArgsConstructor
public class ExportCallDetailToXLSX implements ExcelColumnsHeaderWriter {

    /**
     * an array that stores the content of the headers in columns
     */
	private static final String[] columns = {"extension",
		    "dialedNumber",
		    "organization",
		    "phoneContext",
		    "calldurationminutes",
		    "isactive",
		    "timezone",
		    "startdate",
		    "enddate",
		    "starttime",
		    "endtime",
		    "callonmobile",
		    "isconference",
		    "isconnected",
		    "maximumchannels",
	        "country",
	        "extraconferencechannelid1",
	        "extraconferencechannelid2",
	        "extraconferencechannelid3",
	        "extraconferencechannelid4",
	        "extraconferencechannelid5",
	        "extraconferencechannelid6",
	        "extraconferencechannelid7",
	        "extraconferencechannelid8",
	        "extraconferencechannelid9",
	        "extraconferencechannelid10",
	        "extraconferencechannelid11",
	        "extraconferencechannelid12",
	        "extraconferencechannelid13",
	        "extraconferencechannelid14",
	        "extraconferencechannelid15",
	        "extraconferencechannelid16",
	        "extraconferencechannelid17",
	        "extraconferencechannelid18",
	        "extraconferencechannelid19",
	        "extraconferencechannelid20",
	        "extraconferencechannelid21",
	        "extraconferencechannelid22",
	        "extraconferencechannelid23",
	        "extraconferencechannelid24",
	        "extraconferencechannelid25",
	        "extraconferencechannelid26",
	        "extraconferencechannelid27",
	        "extraconferencechannelid28",
	        "extraconferencechannelid29",
	        "extraconferencechannelid30",
	        "extraconferencechannelid31",
	        "extraconferencechannelid32",
	        "extraconferencechannelid33",
	        "extraconferencechannelid34",
	        "extraconferencechannelid35",
	        "extraconferencechannelid36",
	        "extraconferencechannelid37",
	        "extraconferencechannelid38",
	        "extraconferencechannelid39",
	        "extraconferencechannelid40",
	        "extraconferencechannelid41",
	        "extraconferencechannelid42",
	        "extraconferencechannelid43",
	        "extraconferencechannelid44",
	        "extraconferencechannelid45",
	        "extraconferencechannelid46",
	        "extraconferencechannelid47",
	        "extraconferencechannelid48",
	        "extraconferencechannelid49",
	        "extraconferencechannelid50" };
	
	private final List<CallDetail> calldetails;

    /**
     * The method completes the sheet with data
     * @param sheet sheet to be filled with data
     */
    private void writeCellsData(Sheet sheet) {
        int rowNum = 1;
        for (CallDetail calldetail : calldetails) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(1).setCellValue(calldetail.getCallerid());
            row.createCell(2).setCellValue(calldetail.getCustomerid());
            row.createCell(3).setCellValue(calldetail.getOrganization());
            row.createCell(4).setCellValue(calldetail.getPhoneContext());
            row.createCell(5).setCellValue(calldetail.getCalldurationseconds());
            row.createCell(6).setCellValue(calldetail.isIsactive());
            row.createCell(7).setCellValue(calldetail.getTimezone().getDisplayName());
            row.createCell(8).setCellValue(calldetail.getStartdate());
            row.createCell(9).setCellValue(calldetail.getEnddate());
            row.createCell(10).setCellValue(calldetail.getStarttime().toString());
            row.createCell(11).setCellValue(calldetail.getEndtime().toString());
            row.createCell(12).setCellValue(calldetail.isCallonmobile());
            row.createCell(13).setCellValue(calldetail.isIsconference());
            row.createCell(14).setCellValue(calldetail.isIsconnected());
            row.createCell(15).setCellValue(calldetail.getMaximumchannels());
            row.createCell(16).setCellValue(calldetail.getCountry());
            row.createCell(17).setCellValue(calldetail.getExtraconferencechannelid1());
            row.createCell(18).setCellValue(calldetail.getExtraconferencechannelid2());
            row.createCell(19).setCellValue(calldetail.getExtraconferencechannelid3());
            row.createCell(20).setCellValue(calldetail.getExtraconferencechannelid4());
            row.createCell(21).setCellValue(calldetail.getExtraconferencechannelid5());
            row.createCell(22).setCellValue(calldetail.getExtraconferencechannelid6());
            row.createCell(23).setCellValue(calldetail.getExtraconferencechannelid7());
            row.createCell(24).setCellValue(calldetail.getExtraconferencechannelid8());
            row.createCell(25).setCellValue(calldetail.getExtraconferencechannelid9());
            row.createCell(26).setCellValue(calldetail.getExtraconferencechannelid10());
            row.createCell(27).setCellValue(calldetail.getExtraconferencechannelid11());
            row.createCell(28).setCellValue(calldetail.getExtraconferencechannelid12());
            row.createCell(29).setCellValue(calldetail.getExtraconferencechannelid13());
            row.createCell(30).setCellValue(calldetail.getExtraconferencechannelid14());
            row.createCell(31).setCellValue(calldetail.getExtraconferencechannelid15());
            row.createCell(32).setCellValue(calldetail.getExtraconferencechannelid16());
            row.createCell(33).setCellValue(calldetail.getExtraconferencechannelid17());
            row.createCell(34).setCellValue(calldetail.getExtraconferencechannelid18());
            row.createCell(35).setCellValue(calldetail.getExtraconferencechannelid19());
            row.createCell(36).setCellValue(calldetail.getExtraconferencechannelid20());
            row.createCell(37).setCellValue(calldetail.getExtraconferencechannelid21());
            row.createCell(38).setCellValue(calldetail.getExtraconferencechannelid22());
            row.createCell(39).setCellValue(calldetail.getExtraconferencechannelid23());
            row.createCell(40).setCellValue(calldetail.getExtraconferencechannelid24());
            row.createCell(41).setCellValue(calldetail.getExtraconferencechannelid25());
            row.createCell(42).setCellValue(calldetail.getExtraconferencechannelid26());
            row.createCell(43).setCellValue(calldetail.getExtraconferencechannelid27());
            row.createCell(44).setCellValue(calldetail.getExtraconferencechannelid28());
            row.createCell(45).setCellValue(calldetail.getExtraconferencechannelid29());
            row.createCell(46).setCellValue(calldetail.getExtraconferencechannelid30());
            row.createCell(47).setCellValue(calldetail.getExtraconferencechannelid31());
            row.createCell(48).setCellValue(calldetail.getExtraconferencechannelid32());
            row.createCell(49).setCellValue(calldetail.getExtraconferencechannelid33());
            row.createCell(50).setCellValue(calldetail.getExtraconferencechannelid34());
            row.createCell(51).setCellValue(calldetail.getExtraconferencechannelid35());
            row.createCell(52).setCellValue(calldetail.getExtraconferencechannelid36());
            row.createCell(52).setCellValue(calldetail.getExtraconferencechannelid37());
            row.createCell(53).setCellValue(calldetail.getExtraconferencechannelid38());
            row.createCell(54).setCellValue(calldetail.getExtraconferencechannelid39());
            row.createCell(55).setCellValue(calldetail.getExtraconferencechannelid40());
            row.createCell(56).setCellValue(calldetail.getExtraconferencechannelid41());
            row.createCell(57).setCellValue(calldetail.getExtraconferencechannelid42());
            row.createCell(58).setCellValue(calldetail.getExtraconferencechannelid43());
            row.createCell(59).setCellValue(calldetail.getExtraconferencechannelid44());
            row.createCell(60).setCellValue(calldetail.getExtraconferencechannelid45());
            row.createCell(61).setCellValue(calldetail.getExtraconferencechannelid46());
            row.createCell(62).setCellValue(calldetail.getExtraconferencechannelid47());
            row.createCell(63).setCellValue(calldetail.getExtraconferencechannelid48());
            row.createCell(64).setCellValue(calldetail.getExtraconferencechannelid49());
            row.createCell(65).setCellValue(calldetail.getExtraconferencechannelid50());
            
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
        String headerValue = "calldetail_" + currentDateTime + ".xlsx";
        Sheet sheet = workbook.createSheet(headerValue);

        writeColumnsHeader(workbook, sheet, columns);
        writeCellsData(sheet);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

}
