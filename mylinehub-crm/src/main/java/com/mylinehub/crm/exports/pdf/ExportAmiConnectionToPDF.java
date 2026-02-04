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
import com.mylinehub.crm.entity.AmiConnection;
import com.mylinehub.crm.exports.ExportPDFRepository;
import com.mylinehub.crm.exports.PDFFileDesignRepository;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class ExportAmiConnectionToPDF implements ExportPDFRepository, PDFFileDesignRepository {
    private final List<AmiConnection> amiConnectionList;

    private static final String[] columns = {"Id", "Phone Context", "Organization","AMI User","Password","Port","Domain","Is Active"};
    
    @Override
    public void writeTableData(PdfPTable table) {
        
        for (AmiConnection amiConnection : amiConnectionList) {
              table.addCell(String.valueOf(amiConnection.getId()));
              table.addCell(amiConnection.getPhonecontext());
              table.addCell(amiConnection.getOrganization());
              table.addCell(amiConnection.getAmiuser());
              table.addCell(amiConnection.getPassword());
              table.addCell(String.valueOf(amiConnection.getPort()));  
              table.addCell(amiConnection.getDomain());
              table.addCell(String.valueOf(amiConnection.isIsactive()));
              
            }
    }

    @Override
    public void export(HttpServletResponse response) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        setupFileStyle(response, document);

        Paragraph p = new Paragraph("AmiConnections", setupFont());
        p.setAlignment(ALIGN_CENTER);

        document.add(p);

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {1.5f, 3.0f, 4.0f, 4.0f, 3.2f, 3.2f, 4.5f,3.2f});
        table.setSpacingBefore(10);

        writeTableHeader(table, columns);
        writeTableData(table);

        document.add(table);
        document.close();

    }
}