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
import com.mylinehub.crm.entity.Queue;
import com.mylinehub.crm.exports.ExportPDFRepository;
import com.mylinehub.crm.exports.PDFFileDesignRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExportQueueToPDF implements ExportPDFRepository, PDFFileDesignRepository {
    private final List<Queue> queueList;

    /**
     * an array that stores the content of the headers in columns
     */
	private static final String[] columns = {"Id", "Phone Context", "Organization","Extension","Protocol","Domain","Name","Type","Is Active"};
	
    @Override
    public void writeTableData(PdfPTable table) {
        for (Queue queue : queueList) {
              table.addCell(String.valueOf(queue.getId()));
              table.addCell(String.valueOf(queue.getPhoneContext()));
              table.addCell(String.valueOf(queue.getOrganization()));
              table.addCell(String.valueOf(queue.getExtension()));
              table.addCell(String.valueOf(queue.getProtocol()));
              table.addCell(String.valueOf(queue.getDomain())); 
              table.addCell(String.valueOf(queue.getName()));
              table.addCell(String.valueOf(queue.getType()));
              table.addCell(String.valueOf(queue.isIsactive()));
              
            }
    }

    @Override
    public void export(HttpServletResponse response) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        setupFileStyle(response, document);

        Paragraph p = new Paragraph("Queues", setupFont());
        p.setAlignment(ALIGN_CENTER);

        document.add(p);

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {2.5f, 7.0f, 7.0f, 6.0f, 5.2f, 6f, 4.5f, 4f, 4.5f});
        table.setSpacingBefore(10);

        writeTableHeader(table, columns);
        writeTableData(table);

        document.add(table);
        document.close();

    }
}