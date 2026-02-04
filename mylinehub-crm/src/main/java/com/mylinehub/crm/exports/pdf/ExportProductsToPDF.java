package com.mylinehub.crm.exports.pdf;

import com.mylinehub.crm.entity.Product;
import com.mylinehub.crm.enums.CURRENCY;
import com.mylinehub.crm.exports.ExportPDFRepository;
import com.mylinehub.crm.exports.PDFFileDesignRepository;
import com.lowagie.text.pdf.PdfPTable;

import java.util.List;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.lowagie.text.*;
import lombok.AllArgsConstructor;
import static com.lowagie.text.Element.ALIGN_CENTER;

@AllArgsConstructor
public class ExportProductsToPDF implements ExportPDFRepository, PDFFileDesignRepository {

    private final List<Product> productList;

    /**
     * an array that stores the content of the headers in columns
     */
    private static final String[] columns = {"Id", "Name of Product", "Product Type", "Selling price", "Purchase price", "Tax rate","Organization"};
   
    @Override
    public void writeTableData(PdfPTable table) {
        for (Product product : productList) {
            table.addCell(String.valueOf(product.getId()));
            table.addCell(product.getName());
            table.addCell(product.getProductType());
            table.addCell(product.getSellingPrice().toString() + " " + CURRENCY.PLN.name());
            table.addCell(product.getPurchasePrice().toString() + " " + CURRENCY.PLN.name());
            table.addCell(product.getTaxRate().toString() + "%");
            table.addCell(product.getOrganization());
        }
    }

    @Override
    public void export(HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4);
        setupFileStyle(response, document);

        Paragraph p = new Paragraph("Products", setupFont());
        p.setAlignment(ALIGN_CENTER);

        document.add(p);

        
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {2.5f, 6.0f, 4.0f, 4.0f, 6f, 3.2f, 6f});
        table.setSpacingBefore(10);

        writeTableHeader(table, columns);
        writeTableData(table);

        document.add(table);
        document.close();

    }
}
