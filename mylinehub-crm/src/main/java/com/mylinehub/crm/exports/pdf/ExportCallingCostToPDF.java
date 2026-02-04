//package com.mylinehub.crm.exports.pdf;
//
//import static com.lowagie.text.Element.ALIGN_CENTER;
//
//import java.io.IOException;
//import java.util.List;
//
//import javax.servlet.http.HttpServletResponse;
//
//import com.lowagie.text.Document;
//import com.lowagie.text.DocumentException;
//import com.lowagie.text.PageSize;
//import com.lowagie.text.Paragraph;
//import com.lowagie.text.pdf.PdfPTable;
//import com.mylinehub.crm.entity.CallingCost;
//import com.mylinehub.crm.exports.ExportPDFRepository;
//import com.mylinehub.crm.exports.PDFFileDesignRepository;
//
//import lombok.AllArgsConstructor;
//
//
//@AllArgsConstructor
//public class ExportCallingCostToPDF implements ExportPDFRepository, PDFFileDesignRepository {
//    private final List<CallingCost> callingCostList;
//
//    /**
//     * an array that stores the content of the headers in columns
//     */
//	private static final String[] columns = {"Id","Extension","Amount","Call Calculations","Organization","Remarks"};
//	
//    @Override
//    public void writeTableData(PdfPTable table) {
//       
//        for (CallingCost callingCost : callingCostList) {
//              table.addCell(String.valueOf(callingCost.getId()));
//              table.addCell(String.valueOf(callingCost.getExtension()));
//              table.addCell(String.valueOf(callingCost.getAmount()));
//              table.addCell(String.valueOf(callingCost.getCallcalculation()));
//              table.addCell(String.valueOf(callingCost.getOrganization()));
//              table.addCell(String.valueOf(callingCost.getRemarks()));
//            }
//    }
//
//    @Override
//    public void export(HttpServletResponse response) throws DocumentException, IOException {
//        Document document = new Document(PageSize.A4);
//        setupFileStyle(response, document);
//
//        Paragraph p = new Paragraph("CallingCosts", setupFont());
//        p.setAlignment(ALIGN_CENTER);
//
//        document.add(p);
//
//        PdfPTable table = new PdfPTable(5);
//        table.setWidthPercentage(100f);
//        table.setWidths(new float[] {1.5f,  4.0f, 4.0f, 4.0f,10.f});
//        table.setSpacingBefore(10);
//
//        writeTableHeader(table, columns);
//        writeTableData(table);
//
//        document.add(table);
//        document.close();
//
//    }
//}