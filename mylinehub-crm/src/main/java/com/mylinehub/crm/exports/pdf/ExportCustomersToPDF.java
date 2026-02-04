package com.mylinehub.crm.exports.pdf;

import com.mylinehub.crm.entity.Customers;
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
public class ExportCustomersToPDF implements ExportPDFRepository, PDFFileDesignRepository {

    private final List<Customers> customersList;

    /**
     * an array that stores the content of the headers in columns
     */
	private static final String[] columns = {"Id", "First Name", "Last Name", "Zip Code", "City", "Email","Phone Number", "Phone Context", "Description", "Business", "Country", "Domain","Converted","Data type","Organization","Reminder Calling","Cron Reminder Calling","Is Called Once", "Pesel"};
	
    @Override
    public void writeTableData(PdfPTable table) {
        for (Customers customers : customersList) {
            table.addCell(String.valueOf(customers.getId()));
            table.addCell(String.valueOf(customers.getFirstname()));
            table.addCell(String.valueOf(customers.getLastname()));
            table.addCell(String.valueOf(customers.getZipCode()));
            table.addCell(String.valueOf(customers.getCity()));
            
            table.addCell(String.valueOf(customers.getEmail()));
            table.addCell(String.valueOf(customers.getPhoneNumber()));
            table.addCell(String.valueOf(customers.getPhoneContext()));
            table.addCell(String.valueOf(customers.getDescription()));
            table.addCell(String.valueOf(customers.getBusiness()));
            table.addCell(String.valueOf(customers.getCountry()));
            table.addCell(String.valueOf(customers.getDomain()));
            table.addCell(String.valueOf(customers.isCoverted()));
            table.addCell(String.valueOf(customers.getDatatype()));
            table.addCell(String.valueOf(customers.getOrganization()));
            table.addCell(String.valueOf(customers.isRemindercalling()));
            table.addCell(String.valueOf(customers.getCronremindercalling()));
            table.addCell(String.valueOf(customers.isIscalledonce()));
            table.addCell(String.valueOf(customers.getPesel()));
        }
    }

    @Override
    public void export(HttpServletResponse response) throws IOException {
    	Rectangle pageSize=new Rectangle(3000f,3000f);
    	Document document=new Document(pageSize);
    	
        //Document document = new Document(PageSize.A4);
        setupFileStyle(response, document);

        Paragraph p = new Paragraph("Customers", setupFont());
        p.setAlignment(ALIGN_CENTER);

        document.add(p);

        PdfPTable table = new PdfPTable(19);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {2.5f, 4f, 4f, 4f, 4.0f,4.0f, 4f, 4f, 4f, 4.0f,4.0f, 4f, 4f, 4f, 4.0f,4.0f, 4f, 4f, 4f});
        table.setSpacingBefore(10);

        writeTableHeader(table, columns);
        writeTableData(table);

        document.add(table);
        document.close();

    }
}

