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
import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.exports.ExportPDFRepository;
import com.mylinehub.crm.exports.PDFFileDesignRepository;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class ExportCampaignToPDF implements ExportPDFRepository, PDFFileDesignRepository {
    private final List<Campaign> campaignList;

    /**
     * an array that stores the content of the headers in columns
     */
	private static final String[] columns = {"ID","Organization","Domain","Name","Description","Is Active","Time Zome","Start Date","End Date","Start Time","End Time","Phone Context","Is On Mobile", "Admin ID","Country","Busines","Auto Dialer Type","Ivr Extension","Conf Extension","Queue Extension"};
	
	
    @Override
    public void writeTableData(PdfPTable table) {
        
        for (Campaign campaign : campaignList) {
              table.addCell(String.valueOf(campaign.getId()));
              table.addCell(String.valueOf(campaign.getOrganization()));
              table.addCell(String.valueOf(campaign.getDomain()));
              table.addCell(String.valueOf(campaign.getName()));
              table.addCell(String.valueOf(campaign.getDescription()));
              table.addCell(String.valueOf(campaign.isIsactive()));
              table.addCell(String.valueOf(campaign.getTimezone().getDisplayName()));
              table.addCell(String.valueOf(campaign.getStartdate()));
              table.addCell(String.valueOf(campaign.getEnddate()));
              table.addCell(String.valueOf(campaign.getStarttime().toString()));
              table.addCell(String.valueOf(campaign.getEndtime().toString()));
              table.addCell(String.valueOf(campaign.getPhonecontext()));
              table.addCell(String.valueOf(campaign.isIsonmobile()));
              table.addCell(String.valueOf(campaign.getManager().getId()));
              table.addCell(String.valueOf(campaign.getCountry()));
              table.addCell(String.valueOf(campaign.getBusiness()));
              table.addCell(String.valueOf(campaign.getAutodialertype()));
              
              table.addCell(String.valueOf(campaign.getIvrExtension()));
              table.addCell(String.valueOf(campaign.getConfExtension()));
              table.addCell(String.valueOf(campaign.getQueueExtension()));
            }
    }

    @Override
    public void export(HttpServletResponse response) throws DocumentException, IOException {
    	Rectangle pageSize=new Rectangle(2500f,2500f);
    	Document document=new Document(pageSize);
    	
        //Document document = new Document(PageSize.A4);
        setupFileStyle(response, document);
        Paragraph p = new Paragraph("Campaigns", setupFont());
        p.setAlignment(ALIGN_CENTER);

        document.add(p);

        PdfPTable table = new PdfPTable(20);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {1.5f, 3.5f, 3.0f, 3.0f,3.0f,3.0f, 3.5f, 3.0f, 3.0f,3.0f,3.0f,3.0f,3.0f, 3.5f, 3.0f, 3.0f,3.0f, 3.0f, 3.0f,3.0f});
        table.setSpacingBefore(10);

        writeTableHeader(table, columns);
        writeTableData(table);

        document.add(table);
        document.close();

    }
}