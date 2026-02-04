package com.mylinehub.crm.exports;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public class ExcelHelper {

    private static final Set<String> EXCEL_CONTENT_TYPES = Set.of(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
        "application/vnd.ms-excel.sheet.macroEnabled.12",                     // .xlsm
        "application/vnd.ms-excel"                                           // .xls (optional)
    );

    public static boolean hasExcelFormat(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        System.out.println("[EXCEL] filename=" + filename);
        System.out.println("[EXCEL] contentType=" + contentType);

        // Accept by content-type
        if (contentType != null && EXCEL_CONTENT_TYPES.contains(contentType)) {
            return true;
        }

        // Fallback: accept by extension (important for some browsers)
        if (filename != null) {
            String lower = filename.toLowerCase();
            return lower.endsWith(".xlsx")
                || lower.endsWith(".xlsm")
                || lower.endsWith(".xls");
        }

        return false;
    }
}
