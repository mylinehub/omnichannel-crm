package com.mylinehub.crm.exports.pdf;

import static com.lowagie.text.Element.ALIGN_CENTER;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.mylinehub.crm.entity.Conference;
import com.mylinehub.crm.exports.ExportPDFRepository;
import com.mylinehub.crm.exports.PDFFileDesignRepository;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class ExportConferenceToPDF implements ExportPDFRepository, PDFFileDesignRepository {
    private final List<Conference> conferenceList;

    /**
     * an array that stores the content of the headers in columns
     */
	private static final String[] columns = {"Id", "Conference Extension","Conference Name","Domain","Organization","Phone Context", "Owner","Bridge","User Profile","Menu","Is Dynamic","Is Room Active", "Is Conference Active"};
	
    @Override
    public void writeTableData(PdfPTable table) {
      
        for (Conference conference : conferenceList) {
              table.addCell(String.valueOf(conference.getId()));
              table.addCell(String.valueOf(conference.getConfextension()));
              table.addCell(String.valueOf(conference.getConfname()));
              table.addCell(String.valueOf(conference.getDomain()));
              table.addCell(String.valueOf(conference.getOrganization()));
              table.addCell(String.valueOf(conference.getPhonecontext()));
              table.addCell(String.valueOf(conference.getOwner()));
              table.addCell(String.valueOf(conference.getBridge()));
              table.addCell(String.valueOf(conference.getUserprofile()));
              table.addCell(String.valueOf(conference.getMenu()));
              table.addCell(String.valueOf(conference.isIsdynamic()));
              table.addCell(String.valueOf(conference.isIsroomactive()));
              table.addCell(String.valueOf(conference.isIsconferenceactive()));
            }
    }

    @Override
    public void export(HttpServletResponse response) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        setupFileStyle(response, document);

        Paragraph p = new Paragraph("Conferences", setupFont());
        p.setAlignment(ALIGN_CENTER);

        document.add(p);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {2.5f, 8.0f, 4.3f, 4.0f, 4f, 3.2f, 4.5f});
        table.setSpacingBefore(10);

        writeTableHeader(table, columns);
        writeTableData(table);

        document.add(table);
        document.close();

    }
}