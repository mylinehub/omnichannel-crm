package com.mylinehub.crm.exports.pdf;

import static com.lowagie.text.Element.ALIGN_CENTER;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.mylinehub.crm.entity.CallDetail;
import com.mylinehub.crm.exports.ExportPDFRepository;
import com.mylinehub.crm.exports.PDFFileDesignRepository;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class ExportCallDetailToPDF implements ExportPDFRepository, PDFFileDesignRepository {
    private final List<CallDetail> callDetailList;

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
	
    @Override
    public void writeTableData(PdfPTable table) {

        for (CallDetail callDetail : callDetailList) {
              table.addCell(String.valueOf(callDetail.getCallerid()));
              table.addCell(String.valueOf(callDetail.getCustomerid()));
              table.addCell(String.valueOf(callDetail.getOrganization()));
              table.addCell(String.valueOf(callDetail.getPhoneContext()));
              table.addCell(String.valueOf(callDetail.getCalldurationseconds()));
              table.addCell(String.valueOf(callDetail.isIsactive()));
              table.addCell(String.valueOf(callDetail.getTimezone().getDisplayName()));
              table.addCell(String.valueOf(callDetail.getStartdate()));
              table.addCell(String.valueOf(callDetail.getEnddate()));
              table.addCell(String.valueOf(callDetail.getStarttime().toString()));
              table.addCell(String.valueOf(callDetail.getEndtime().toString()));
              table.addCell(String.valueOf(callDetail.isCallonmobile()));
              table.addCell(String.valueOf(callDetail.isIsconference()));
              table.addCell(String.valueOf(callDetail.isIsconnected()));
              table.addCell(String.valueOf(callDetail.getMaximumchannels()));
              table.addCell(String.valueOf(callDetail.getCountry()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid1()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid2()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid3()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid4()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid5()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid6()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid7()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid8()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid9()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid10()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid11()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid12()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid13()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid14()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid15()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid16()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid17()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid18()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid19()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid20()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid21()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid22()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid23()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid24()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid25()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid26()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid27()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid28()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid29()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid30()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid31()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid32()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid33()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid34()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid35()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid36()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid37()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid38()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid39()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid40()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid41()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid42()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid43()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid44()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid45()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid46()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid47()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid48()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid49()));
              table.addCell(String.valueOf(callDetail.getExtraconferencechannelid50()));
              
            
              
            }
    }

    @Override
    public void export(HttpServletResponse response) throws DocumentException, IOException {
    	Rectangle pageSize=new Rectangle(11000f,11000f);
    	Document document=new Document(pageSize);
    	
        //Document document = new Document(PageSize.A4);
        setupFileStyle(response, document);

        Paragraph p = new Paragraph("Call Details", setupFont());
        p.setAlignment(ALIGN_CENTER);

        document.add(p);

        PdfPTable table = new PdfPTable(66);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {1.5f, 3.5f, 3.0f, 3.0f,3.0f,3.0f, 3.5f, 3.0f, 3.0f,3.0f,3.0f,3.0f,3.0f, 3.5f, 3.0f, 3.0f,3.0f,3.0f, 3.5f, 3.0f, 3.0f,3.0f,3.0f, 3.5f, 3.0f, 3.0f,3.0f,3.0f,3.0f,3.0f, 3.5f, 3.0f, 3.0f,3.0f,3.0f,3.5f, 3.5f, 3.0f, 3.0f,3.0f,3.0f,3.0f,3.0f, 3.5f, 3.0f, 3.0f,3.0f,3.0f, 3.5f, 3.0f, 3.0f,3.0f,3.0f, 3.5f, 3.0f, 3.0f,3.0f,3.0f,3.0f,3.0f, 3.5f, 3.0f, 3.0f,3.0f,3.0f,3.5f});
        
        //table.setWidths(new float[] {1.5f, 3.0f, 4.0f, 4.0f, 3.2f, 3.2f, 4.5f});
        table.setSpacingBefore(10);

        writeTableHeader(table, columns);
        writeTableData(table);

        document.add(table);
        document.close();

    }
}