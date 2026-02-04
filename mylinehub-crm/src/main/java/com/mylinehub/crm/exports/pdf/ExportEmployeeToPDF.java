package com.mylinehub.crm.exports.pdf;

import com.mylinehub.crm.entity.Employee;
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
public class ExportEmployeeToPDF implements ExportPDFRepository, PDFFileDesignRepository {

    private final List<Employee> employeesList;

    /**
     * an array that stores the content of the headers in columns
     */
	private static final String[] columns = {"Id", "First Name", "Last Name", "Department", "Role", "Salary", "Email", "Phone Context", "Organization", "Domain", "Extension", "Extension Password","Time Zone","Type","Call On Mobile","Phone Number","Transfer Phone 1","Transfer Phone 2","Provider 1","Alotted Number 1","Provider 2","Alotted Number 2","Cost Calculation","Amount"};
	
	
    @Override
    public void writeTableData(PdfPTable table) {
        for (Employee employee : employeesList) {
        	table.addCell(String.valueOf(employee.getId()));
        	table.addCell(String.valueOf(employee.getFirstName()));
        	table.addCell(String.valueOf(employee.getLastName()));
        	table.addCell(String.valueOf(employee.getDepartment().getDepartmentName()));
        	table.addCell(String.valueOf(employee.getUserRole().toString().toLowerCase()));
        	table.addCell(String.valueOf(employee.getSalary()));
        	table.addCell(String.valueOf(employee.getEmail()));
        	table.addCell(String.valueOf(employee.getPhoneContext()));
        	table.addCell(String.valueOf(employee.getOrganization()));
        	table.addCell(String.valueOf(employee.getDomain()));
        	table.addCell(String.valueOf(employee.getExtension()));
        	table.addCell(String.valueOf(employee.getExtensionpassword()));
        	table.addCell(String.valueOf(employee.getTimezone().getDisplayName()));
        	table.addCell(String.valueOf(employee.getType()));
        	table.addCell(String.valueOf(employee.isCallonnumber()));
        	table.addCell(String.valueOf(employee.getPhonenumber()));
        	table.addCell(String.valueOf(employee.getTransfer_phone_1()));
        	table.addCell(String.valueOf(employee.getTransfer_phone_2()));
        	table.addCell(String.valueOf(employee.getProvider1()));
        	table.addCell(String.valueOf(employee.getAllotednumber1()));
        	table.addCell(String.valueOf(employee.getProvider2()));
        	table.addCell(String.valueOf(employee.getAllotednumber2()));
        	table.addCell(String.valueOf(employee.getCostCalculation()));
        	table.addCell(String.valueOf(employee.getAmount()));
            
        }
    }

    @Override
    public void export(HttpServletResponse response) throws IOException {
    	
    	Rectangle pageSize=new Rectangle(2500f,2500f);
    	Document document=new Document(pageSize);
    	
        //Document document = new Document(PageSize.A4);
        setupFileStyle(response, document);

        Paragraph p = new Paragraph("Employees", setupFont());
        p.setAlignment(ALIGN_CENTER);

        document.add(p);

        PdfPTable table = new PdfPTable(24);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {1.5f, 3.5f, 3.0f, 3.0f,3.0f,3.0f, 3.5f, 3.0f, 3.0f,3.0f,3.0f,3.0f,3.0f, 3.5f, 3.0f, 3.0f,3.0f,3.0f,3.0f,3.0f,3.0f,3.0f,3.0f,3.0f});
        //table.setWidths(new float[] {1.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f,3.5f});
        table.setSpacingBefore(10);

        writeTableHeader(table, columns);
        writeTableData(table);

        document.add(table);
        document.close();

    }
}
