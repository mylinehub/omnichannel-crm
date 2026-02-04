package com.mylinehub.crm.exports.pdf;

import com.mylinehub.crm.entity.Supplier;
import com.mylinehub.crm.exports.ExportPDFRepository;
import com.mylinehub.crm.exports.PDFFileDesignRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import lombok.AllArgsConstructor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import static com.lowagie.text.Element.ALIGN_CENTER;

@AllArgsConstructor
public class ExportSuppliersToPDF implements ExportPDFRepository, PDFFileDesignRepository {

    private final List<Supplier> supplierList;

    /**
     * an array that stores the content of the headers in columns
     */
    private static final String[] columns =
            {"Id", "Name of Supplier", "Mode of transport", "Min Length", "Max Length", "Min Weight", "Max Weight", "Transport Capacity", "Activity Status","Organization"};
   
    @Override
    public void writeTableData(PdfPTable table) {
        for (Supplier supplier : supplierList) {
            table.addCell(String.valueOf(supplier.getSupplierId()));
            table.addCell(String.valueOf(supplier.getSupplierName()));
            table.addCell(String.valueOf(supplier.getModeOfTransportCode().getFullName()));
            table.addCell(String.valueOf(supplier.getModeOfTransportCode().getMaxLength()));
            table.addCell(String.valueOf(supplier.getModeOfTransportCode().getMaxWeight()));
            table.addCell(String.valueOf(supplier.getModeOfTransportCode().getMinLength()));
            table.addCell(String.valueOf(supplier.getModeOfTransportCode().getMinWeight()));
            table.addCell(String.valueOf(supplier.getModeOfTransportCode().getTransportCapacity()));
            table.addCell(String.valueOf(supplier.getActivityStatus()));
            table.addCell(String.valueOf(supplier.getOrganization()));
        }
    }

    @Override
    public void export(HttpServletResponse response) throws IOException {

    	Rectangle pageSize=new Rectangle(1000f,1000f);
    	Document document=new Document(pageSize);
    	
        //Document document = new Document(PageSize.A4);
        setupFileStyle(response, document);

        Paragraph p = new Paragraph("Suppliers", setupFont());
        p.setAlignment(ALIGN_CENTER);

        document.add(p);

        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {1.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f,3.0f});
        table.setSpacingBefore(10);

        writeTableHeader(table, columns);
        writeTableData(table);

        document.add(table);
        document.close();

    }
}

