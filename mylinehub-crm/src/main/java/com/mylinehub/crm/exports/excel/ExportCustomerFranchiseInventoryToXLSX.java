package com.mylinehub.crm.exports.excel;

import com.mylinehub.crm.entity.CustomerFranchiseInventory;
import com.mylinehub.crm.entity.Customers;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class ExportCustomerFranchiseInventoryToXLSX {

    private final List<CustomerFranchiseInventory> rows;

    public ExportCustomerFranchiseInventoryToXLSX(List<CustomerFranchiseInventory> rows) {
        this.rows = rows;
    }

    public void export(HttpServletResponse response) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Franchise Inventory");

            // header
            Row headerRow = sheet.createRow(0);
            String[] headers = new String[]{
                    "Inventory ID",
                    "Customer ID",
                    "First Name",
                    "Last Name",
                    "Phone Number",
                    "City",
                    "Interest",
                    "Available",
                    "Created On",
                    "Last Updated On"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // data
            int rowIdx = 1;
            for (CustomerFranchiseInventory f : rows) {
                Row row = sheet.createRow(rowIdx++);

                Customers c = f.getCustomer();

                int col = 0;
                row.createCell(col++).setCellValue(nvl(f.getId()));
                row.createCell(col++).setCellValue(nvl(c != null ? c.getId() : null));
                row.createCell(col++).setCellValue(nvls(c != null ? c.getFirstname() : null));
                row.createCell(col++).setCellValue(nvls(c != null ? c.getLastname() : null));
                row.createCell(col++).setCellValue(nvls(c != null ? c.getPhoneNumber() : null));
                row.createCell(col++).setCellValue(nvls(c != null ? c.getCity() : null));
                row.createCell(col++).setCellValue(nvls(f.getInterest()));
                row.createCell(col++).setCellValue(String.valueOf(f.getAvailable() != null ? f.getAvailable() : true));
                row.createCell(col++).setCellValue(f.getCreatedOn() != null ? f.getCreatedOn().toString() : "");
                row.createCell(col++).setCellValue(f.getLastUpdatedOn() != null ? f.getLastUpdatedOn().toString() : "");
            }

            // autosize (small set only)
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        }
    }

    private static String nvls(String s) {
        return (s == null) ? "" : s;
    }

    private static String nvl(Object o) {
        return (o == null) ? "" : String.valueOf(o);
    }
}
