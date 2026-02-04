package com.mylinehub.crm.exports.excel;

import com.mylinehub.crm.entity.CustomerPropertyInventory;
import com.mylinehub.crm.entity.Customers;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public class ExportCustomerPropertyInventoryToXLSX {

    private final List<CustomerPropertyInventory> list;
    private final Workbook workbook;
    private Sheet sheet;

    public ExportCustomerPropertyInventoryToXLSX(List<CustomerPropertyInventory> list) {
        this.list = list;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderRow() {
        sheet = workbook.createSheet("Inventory");

        Row row = sheet.createRow(0);
        int col = 0;

        // Customer columns
        col = setHeader(row, col, "customerId");
        col = setHeader(row, col, "firstname");
        col = setHeader(row, col, "lastname");
        col = setHeader(row, col, "phoneNumber");
        col = setHeader(row, col, "email");
        col = setHeader(row, col, "customerCity");
        col = setHeader(row, col, "zipCode");
        col = setHeader(row, col, "organization");

        // Inventory columns
        col = setHeader(row, col, "inventoryId");
        col = setHeader(row, col, "pid");
        col = setHeader(row, col, "premiseName");
        col = setHeader(row, col, "listedDate");
        col = setHeader(row, col, "propertyType");
        col = setHeader(row, col, "purpose");
        col = setHeader(row, col, "rent");
        col = setHeader(row, col, "rentValue");
        col = setHeader(row, col, "bhk");
        col = setHeader(row, col, "furnishedType");
        col = setHeader(row, col, "sqFt");
        col = setHeader(row, col, "nearby");
        col = setHeader(row, col, "area");
        col = setHeader(row, col, "city");
        col = setHeader(row, col, "callStatus");
        col = setHeader(row, col, "propertyAge");
        col = setHeader(row, col, "unitType");
        col = setHeader(row, col, "tenant");
        col = setHeader(row, col, "facing");
        col = setHeader(row, col, "totalFloors");
        col = setHeader(row, col, "brokerage");
        col = setHeader(row, col, "balconies");
        col = setHeader(row, col, "washroom");
        col = setHeader(row, col, "unitNo");
        col = setHeader(row, col, "floorNo");
        col = setHeader(row, col, "propertyDescription1");
        col = setHeader(row, col, "moreThanOneProperty");
        col = setHeader(row, col, "createdOn");
        col = setHeader(row, col, "lastUpdatedOn");
    }

    private int setHeader(Row row, int col, String name) {
        Cell cell = row.createCell(col++);
        cell.setCellValue(name);
        return col;
    }

    private void writeDataRows() {
        int rowCount = 1;

        for (CustomerPropertyInventory i : list) {
            Row row = sheet.createRow(rowCount++);
            int col = 0;

            Customers c = i.getCustomer();

            // Customer
            col = setCell(row, col, (c != null ? c.getId() : null));
            col = setCell(row, col, (c != null ? c.getFirstname() : null));
            col = setCell(row, col, (c != null ? c.getLastname() : null));
            col = setCell(row, col, (c != null ? c.getPhoneNumber() : null));
            col = setCell(row, col, (c != null ? c.getEmail() : null));
            col = setCell(row, col, (c != null ? c.getCity() : null));
            col = setCell(row, col, (c != null ? c.getZipCode() : null));
            col = setCell(row, col, (c != null ? c.getOrganization() : null));

            // Inventory
            col = setCell(row, col, i.getId());
            col = setCell(row, col, i.getPid());
            col = setCell(row, col, i.getPremiseName());
            col = setCell(row, col, instantToString(i.getListedDate()));
            col = setCell(row, col, i.getPropertyType());
            col = setCell(row, col, i.getPurpose());
            col = setCell(row, col, i.isRent());
            col = setCell(row, col, i.getRentValue());
            col = setCell(row, col, i.getBhk());
            col = setCell(row, col, i.getFurnishedType());
            col = setCell(row, col, i.getSqFt());
            col = setCell(row, col, i.getNearby());
            col = setCell(row, col, i.getArea());
            col = setCell(row, col, i.getCity());
            col = setCell(row, col, i.getCallStatus());
            col = setCell(row, col, i.getPropertyAge());
            col = setCell(row, col, i.getUnitType());
            col = setCell(row, col, i.getTenant());
            col = setCell(row, col, i.getFacing());
            col = setCell(row, col, i.getTotalFloors());
            col = setCell(row, col, i.getBrokerage());
            col = setCell(row, col, i.getBalconies());
            col = setCell(row, col, i.getWashroom());
            col = setCell(row, col, i.getUnitNo());
            col = setCell(row, col, i.getFloorNo());
            col = setCell(row, col, i.getPropertyDescription1());
            col = setCell(row, col, i.getMoreThanOneProperty());
            col = setCell(row, col, instantToString(i.getCreatedOn()));
            col = setCell(row, col, instantToString(i.getLastUpdatedOn()));
        }

        // autosize only first ~20 columns to avoid slow excel generation on huge files
        int maxAuto = Math.min(20, sheet.getRow(0).getLastCellNum());
        for (int i = 0; i < maxAuto; i++) sheet.autoSizeColumn(i);
    }

    private String instantToString(Instant t) {
        return t == null ? "" : t.toString();
    }

    private int setCell(Row row, int col, String v) {
        row.createCell(col++).setCellValue(v == null ? "" : v);
        return col;
    }

    private int setCell(Row row, int col, Long v) {
        Cell cell = row.createCell(col++);
        if (v == null) cell.setCellValue("");
        else cell.setCellValue(v.doubleValue());
        return col;
    }

    private int setCell(Row row, int col, Integer v) {
        Cell cell = row.createCell(col++);
        if (v == null) cell.setCellValue("");
        else cell.setCellValue(v.doubleValue());
        return col;
    }

    private int setCell(Row row, int col, Boolean v) {
        Cell cell = row.createCell(col++);
        if (v == null) cell.setCellValue("");
        else cell.setCellValue(v.booleanValue());
        return col;
    }

    private int setCell(Row row, int col, boolean v) {
        row.createCell(col++).setCellValue(v);
        return col;
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderRow();
        writeDataRows();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}
