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
import com.mylinehub.crm.entity.Ivr;
import com.mylinehub.crm.exports.ExportPDFRepository;
import com.mylinehub.crm.exports.PDFFileDesignRepository;

import lombok.AllArgsConstructor;



@AllArgsConstructor
public class ExportIvrToPDF implements ExportPDFRepository, PDFFileDesignRepository {
    private final List<Ivr> ivrList;

    /**
     * an array that stores the content of the headers in columns
     */
	private static final String[] columns = {"Id", "Phone Context", "Organization","Extension","Protocol","Domain","Is Active"};
  
    @Override
    public void writeTableData(PdfPTable table) {
        for (Ivr ivr : ivrList) {
              table.addCell(String.valueOf(ivr.getId()));
              table.addCell(String.valueOf(ivr.getPhoneContext()));
              table.addCell(String.valueOf(ivr.getOrganization()));
              table.addCell(String.valueOf(ivr.getExtension()));
              table.addCell(String.valueOf(ivr.getProtocol()));
              table.addCell(String.valueOf(ivr.getDomain()));  
              table.addCell(String.valueOf(ivr.isIsactive()));
              
            }
    }

    @Override
    public void export(HttpServletResponse response) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        setupFileStyle(response, document);

        Paragraph p = new Paragraph("Ivrs", setupFont());
        p.setAlignment(ALIGN_CENTER);

        document.add(p);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {1.5f, 3.0f, 4.0f, 4.0f, 3.2f, 3.2f, 4.5f});
        table.setSpacingBefore(10);

        writeTableHeader(table, columns);
        writeTableData(table);

        document.add(table);
        document.close();

    }
}