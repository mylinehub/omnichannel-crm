package com.mylinehub.crm.exports.excel;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.CustomerPropertyInventory;
import com.mylinehub.crm.entity.Errors;
import com.mylinehub.crm.repository.CustomerRepository;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.service.CustomerService;

public class BulkUploadCustomerToDatabase {

    private static final String SHEET = "Customers";

    // Toggle
    private static final boolean DEEP_LOGS = true;

    // Common date patterns users put in Excel as TEXT
    private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("dd/MM/uuuu"),
            DateTimeFormatter.ofPattern("dd-MM-uuuu"),
            DateTimeFormatter.ofPattern("uuuu-MM-dd")
    };

    private void log(String msg) {
        if (DEEP_LOGS) {
            System.out.println("[BulkUploadCustomerToDatabase] " + msg);
        }
    }

    public List<Customers> excelToCustomers(
            CustomerService customersService,
            InputStream is,
            String organization,
            ErrorRepository errorRepository,
            CustomerRepository customerRepository
    ) throws Exception {

        log("ENTER excelToCustomers org=" + organization + " inputStream=" + (is != null));

        if (organization == null || organization.trim().isEmpty()) {
            log("ERROR organization missing");
            throw new Exception("Organization is mandatory for upload. Please select organization and retry.");
        }

        List<Customers> customers = new ArrayList<>();
        List<Errors> allErrors = new ArrayList<>();

        // try-with-resources: always closes workbook
        try (Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheet(SHEET);
            if (sheet == null) {
                log("ERROR sheet not found sheetName=" + SHEET);
                throw new Exception("Sheet '" + SHEET + "' not found in uploaded Excel.");
            }

            Iterator<Row> rows = sheet.iterator();
            if (!rows.hasNext()) {
                log("ERROR sheet empty sheetName=" + SHEET);
                throw new Exception("Excel sheet '" + SHEET + "' is empty.");
            }

            // Header row -> map headerNameNormalized -> columnIndex
            Row headerRow = rows.next();
            Map<String, Integer> headerMap = buildHeaderMap(headerRow);
            log("HEADER_MAP size=" + headerMap.size() + " keys=" + headerMap.keySet());

            int rowNumber = 1; // because header is row 0
            while (rows.hasNext()) {
                Row row = rows.next();
                rowNumber++;

                try {
                    if (isRowEmpty(row)) {
                        log("SKIP emptyRow row=" + rowNumber);
                        continue;
                    }

                    Customers customer = new Customers();
                    customer.setOrganization(organization);

                    // ---------------------------
                    // Customer fields from Excel
                    // ---------------------------

                    // name -> firstname/lastname
                    String fullName = getStringByHeader(row, headerMap, "name");
                    log("ROW " + rowNumber + " rawName=" + fullName);

                    if (notBlank(fullName)) {
                        String[] parts = fullName.trim().split("\\s+");
                        if (parts.length == 1) {
                            customer.setFirstname(parts[0]);
                            customer.setLastname("-");
                        } else {
                            customer.setFirstname(parts[0]);
                            customer.setLastname(String.join(" ", Arrays.copyOfRange(parts, 1, parts.length)));
                        }
                    } else {
                        customer.setFirstname("Unknown");
                        customer.setLastname("Unknown");
                    }

                    // Contact Number -> phoneNumber
                    String contact = getPhoneAsStringByHeader(row, headerMap, "contact number");
                    if (!notBlank(contact)) {
                        contact = getStringByHeader(row, headerMap, "contact number");
                    }
                    log("ROW " + rowNumber + " rawContact=" + contact);
                    contact = normalizePhone(contact);
                    log("ROW " + rowNumber + " normalizedContact=" + contact);
                    
                    if (!notBlank(contact)) {
                        throw new Exception("Contact Number is empty");
                    }
                    if (!isValidPhone(contact)) {
                        throw new Exception("Invalid Contact Number: " + contact);
                    }

                    customer.setPhoneNumber(contact);

                    // city (customer city)
                    String city = getStringByHeader(row, headerMap, "city");
                    if (notBlank(city)) customer.setCity(city);
                    log("ROW " + rowNumber + " city=" + city);

                    // description
                    String desc = getStringByHeader(row, headerMap, "description");
                    if (notBlank(desc)) customer.setDescription(desc);
                    log("ROW " + rowNumber + " description=" + (desc != null ? ("len=" + desc.length()) : "null"));

                    // email
                    String email = getStringByHeader(row, headerMap, "email");
                    email = normalizeEmail(email);
                    log("ROW " + rowNumber + " emailNormalized=" + email);

                    if (notBlank(email)) {
                        if (!isValidEmailLoose(email)) {
                            throw new Exception("Invalid Email: " + email);
                        }
                        customer.setEmail(email);
                    }

                    // zipCode
                    String zip = getStringByHeader(row, headerMap, "zip code");
                    if (!notBlank(zip)) {
                        // allow alternate header name
                        zip = getStringByHeader(row, headerMap, "zipcode");
                    }
                    zip = normalizeZip(zip);
                    log("ROW " + rowNumber + " zipNormalized=" + zip);

                    if (notBlank(zip)) {
                        customer.setZipCode(zip);
                    }

                    // pesel
                    String pesel = getStringByHeader(row, headerMap, "pesel");
                    pesel = normalizePesel(pesel);
                    log("ROW " + rowNumber + " peselNormalized=" + pesel);

                    if (notBlank(pesel)) {
                        customer.setPesel(pesel);
                    }

                    // business
                    String business = getStringByHeader(row, headerMap, "business");
                    if (notBlank(business)) {
                        customer.setBusiness(business);
                    }
                    log("ROW " + rowNumber + " business=" + business);

                    // country
                    String country = getStringByHeader(row, headerMap, "country");
                    country = normalizeCountry(country);
                    if (notBlank(country)) {
                        customer.setCountry(country);
                    }
                    log("ROW " + rowNumber + " countryNormalized=" + country);

                    // default flags
                    customer.setIscalledonce(false);
                    customer.setCoverted(false);
                    customer = customersService.applyDefaultValues(customer);

                    // ---------------------------
                    // Inventory entity from Excel
                    // ---------------------------
                    CustomerPropertyInventory inv = new CustomerPropertyInventory();
                    inv.setCustomer(customer);
                    customer.setPropertyInventory(inv);

                    inv.setPremiseName(getStringByHeader(row, headerMap, "premise name"));
                    log("ROW " + rowNumber + " premiseName=" + inv.getPremiseName());

                    // listedDate (supports Date cell OR string)
                    Instant listedDate = getInstantByHeader(row, headerMap, "listeddate");
                    if (listedDate == null) {
                        listedDate = LocalDate.now(ZoneId.of("Asia/Kolkata"))
                                .atStartOfDay(ZoneId.of("Asia/Kolkata"))
                                .toInstant();
                        log("ROW " + rowNumber + " listedDate missing -> defaultNow=" + listedDate);
                    } else {
                        log("ROW " + rowNumber + " listedDate=" + listedDate);
                    }
                    inv.setListedDate(listedDate);

                    inv.setPropertyType(getStringByHeader(row, headerMap, "type"));
                    log("ROW " + rowNumber + " propertyType=" + inv.getPropertyType());

                    // rent
                    Boolean rentBool = getBooleanByHeader(row, headerMap, "rent");
                    inv.setRent(rentBool != null ? rentBool : false);
                    log("ROW " + rowNumber + " rentBool=" + rentBool + " finalRent=" + inv.isRent());

                    // rentValue
                    Long rentValue = getLongByHeader(row, headerMap, "rentvalue");
                    inv.setRentValue(rentValue);
                    log("ROW " + rowNumber + " rentValue=" + rentValue);

                    // bhk
                    Integer bhk = getIntegerByHeader(row, headerMap, "bhk");
                    inv.setBhk(bhk);
                    log("ROW " + rowNumber + " bhk=" + bhk);

                    inv.setFurnishedType(getStringByHeader(row, headerMap, "furnishedtype"));
                    log("ROW " + rowNumber + " furnishedType=" + inv.getFurnishedType());

                    Integer sqft = getIntegerByHeader(row, headerMap, "sqft");
                    inv.setSqFt(sqft);
                    log("ROW " + rowNumber + " sqft=" + sqft);

                    inv.setNearby(getStringByHeader(row, headerMap, "nearby"));
                    inv.setArea(getStringByHeader(row, headerMap, "area"));
                    inv.setCity(city);
                    log("ROW " + rowNumber + " nearby=" + inv.getNearby() + " area=" + inv.getArea() + " invCity=" + inv.getCity());

                    inv.setCallStatus(getStringByHeader(row, headerMap, "call status"));
                    log("ROW " + rowNumber + " callStatus=" + inv.getCallStatus());

                    Integer age = getIntegerByHeader(row, headerMap, "age");
                    inv.setPropertyAge(age);
                    log("ROW " + rowNumber + " propertyAge=" + age);

                    inv.setUnitType(getStringByHeader(row, headerMap, "unittype"));
                    inv.setTenant(getStringByHeader(row, headerMap, "tenant"));
                    inv.setFacing(getStringByHeader(row, headerMap, "facing"));
                    inv.setPurpose(getStringByHeader(row, headerMap, "purpose"));
                    Integer totalFloors = getIntegerByHeader(row, headerMap, "totalfloors");
                    inv.setTotalFloors(totalFloors);
                    log("ROW " + rowNumber + " unitType=" + inv.getUnitType()
                            + " tenant=" + inv.getTenant()
                            + " facing=" + inv.getFacing()
                            + " purpose=" + inv.getPurpose()
                            + " totalFloors=" + totalFloors);

                    inv.setBrokerage(getStringByHeader(row, headerMap, "brokerage"));
                    log("ROW " + rowNumber + " brokerage=" + inv.getBrokerage());

                    Integer balconies = getIntegerByHeader(row, headerMap, "balconies");
                    inv.setBalconies(balconies);

                    Integer washroom = getIntegerByHeader(row, headerMap, "washroom");
                    inv.setWashroom(washroom);

                    inv.setUnitNo(getStringByHeader(row, headerMap, "unitno"));
                    inv.setFloorNo(getStringByHeader(row, headerMap, "floorno"));

                    inv.setPid(getStringByHeader(row, headerMap, "pid"));
                    inv.setPropertyDescription1(getStringByHeader(row, headerMap, "property description1"));

                    log("ROW " + rowNumber + " balconies=" + balconies
                            + " washroom=" + washroom
                            + " unitNo=" + inv.getUnitNo()
                            + " floorNo=" + inv.getFloorNo()
                            + " pid=" + inv.getPid()
                            + " propDesc1=" + (inv.getPropertyDescription1() != null ? ("len=" + inv.getPropertyDescription1().length()) : "null"));

                    customers.add(customer);
                    log("ROW " + rowNumber + " OK addedCustomer phone=" + customer.getPhoneNumber()
                            + " name=" + customer.getFirstname() + " " + customer.getLastname());

                } catch (Exception rowEx) {
                    // IMPORTANT: do not stop upload. Record error + continue.
                    log("ROW " + rowNumber + " ERROR " + safeMsg(rowEx));

                    Errors error = new Errors();
                    error.setCreatedDate(new Date(System.currentTimeMillis()));
                    error.setError("Row error");
                    error.setErrorClass("BulkUploadCustomerToDatabase");
                    error.setFunctionality("Upload row skipped");
                    error.setOrganization(organization);
                    error.setData("row=" + rowNumber + " msg=" + safeMsg(rowEx));
                    allErrors.add(error);
                }
            }

            // Save all row errors
            if (!allErrors.isEmpty()) {
                log("Saving errors count=" + allErrors.size());
                errorRepository.saveAll(allErrors);
            } else {
                log("No row errors to save");
            }

            log("EXIT excelToCustomers customersParsed=" + customers.size());
            return customers;
        }
    }

    // -------- Helpers --------

    private Map<String, Integer> buildHeaderMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        short last = headerRow.getLastCellNum();
        for (int i = 0; i < last; i++) {
            Cell c = headerRow.getCell(i);
            String h = cellToString(c);
            if (h != null) {
                String norm = normalizeHeader(h);
                if (!norm.isEmpty()) {
                    map.put(norm, i);
                }
            }
        }
        return map;
    }

    private String normalizeHeader(String h) {
        return h == null ? "" : h.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        int last = row.getLastCellNum();
        for (int i = 0; i < last; i++) {
            Cell c = row.getCell(i);
            if (c != null && c.getCellType() != CellType.BLANK) {
                String s = cellToString(c);
                if (notBlank(s)) return false;
            }
        }
        return true;
    }

    private String getStringByHeader(Row row, Map<String, Integer> headerMap, String headerName) {
        Integer idx = headerMap.get(normalizeHeader(headerName));
        if (idx == null) return null;
        return trimToNull(cellToString(row.getCell(idx)));
    }


    private String getPhoneAsStringByHeader(Row row, Map<String, Integer> headerMap, String headerName) {
        Integer idx = headerMap.get(normalizeHeader(headerName));
        if (idx == null) return null;

        Cell c = row.getCell(idx);
        if (c == null) return null;

        try {
            // Handle FORMULA cell that evaluates to NUMERIC
            CellType ct = c.getCellType();
            if (ct == CellType.FORMULA) {
                try { ct = c.getCachedFormulaResultType(); } catch (Exception ignored) {}
            }

            if (ct == CellType.NUMERIC) {
                BigDecimal bd = BigDecimal.valueOf(c.getNumericCellValue()).setScale(0, RoundingMode.HALF_UP);
                String plain = bd.toPlainString();
                String digits = plain.replaceAll("\\D+", "");
                return digits.isEmpty() ? null : digits;
            }

            DataFormatter fmt = new DataFormatter(Locale.US);
            String raw = fmt.formatCellValue(c);
            raw = (raw == null) ? null : raw.trim();
            return (raw == null || raw.isEmpty()) ? null : raw;

        } catch (Exception ignored) {
            return null;
        }
    }


    private boolean isValidPhone(String p) {
        if (!notBlank(p)) return false;

        // keep only digits for length checks
        String digits = p.replaceAll("\\D+", "");

        // basic global sanity
        if (digits.length() < 8) return false;     // too short to be real
        if (digits.length() > 15) return false;    // E.164 max

        // India strict rule (if you want)
        if (p.startsWith("+91")) {
            return digits.length() == 12; // 91 + 10 digits
        }

        return true;
    }

    private Integer getIntegerByHeader(Row row, Map<String, Integer> headerMap, String headerName) {
        String s = getStringByHeader(row, headerMap, headerName);
        Integer v = parseIntLoose(s);
        if (v != null) return v;

        Integer idx = headerMap.get(normalizeHeader(headerName));
        if (idx == null) return null;
        Cell c = row.getCell(idx);
        if (c == null) return null;
        try {
            if (c.getCellType() == CellType.NUMERIC) return (int) Math.round(c.getNumericCellValue());
        } catch (Exception ignored) {}
        return null;
    }

    private Long getLongByHeader(Row row, Map<String, Integer> headerMap, String headerName) {
        String s = getStringByHeader(row, headerMap, headerName);
        Long v = parseLongLoose(s);
        if (v != null) return v;

        Integer idx = headerMap.get(normalizeHeader(headerName));
        if (idx == null) return null;
        Cell c = row.getCell(idx);
        if (c == null) return null;
        try {
            if (c.getCellType() == CellType.NUMERIC) return Math.round(c.getNumericCellValue());
        } catch (Exception ignored) {}
        return null;
    }

    private Boolean getBooleanByHeader(Row row, Map<String, Integer> headerMap, String headerName) {
        String s = getStringByHeader(row, headerMap, headerName);
        if (!notBlank(s)) return null;
        String v = s.trim().toLowerCase();
        if (v.equals("true") || v.equals("yes") || v.equals("1")) return true;
        if (v.equals("false") || v.equals("no") || v.equals("0")) return false;
        if (v.contains("rent")) return true;
        if (v.contains("sale")) return false;
        return null;
    }

    private Instant getInstantByHeader(Row row, Map<String, Integer> headerMap, String headerName) {
        Integer idx = headerMap.get(normalizeHeader(headerName));
        if (idx == null) return null;
        Cell c = row.getCell(idx);
        if (c == null) return null;

        try {
            if (c.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(c)) {
                return c.getDateCellValue().toInstant();
            }
        } catch (Exception ignored) {}

        String s = trimToNull(cellToString(c));
        if (s == null) return null;

        try { return Instant.parse(s); } catch (Exception ignored) {}

        for (DateTimeFormatter f : DATE_FORMATS) {
            try {
                LocalDate ld = LocalDate.parse(s, f);
                return ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
            } catch (Exception ignored) {}
        }

        return null;
    }

    private String cellToString(Cell c) {
        if (c == null) return null;
        try {
            switch (c.getCellType()) {
                case STRING: return c.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(c)) {
                        return c.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toString();
                    }
                    double d = c.getNumericCellValue();
                    if (d == Math.rint(d)) return String.valueOf((long) d);
                    return String.valueOf(d);
                case BOOLEAN: return String.valueOf(c.getBooleanCellValue());
                case FORMULA:
                    try { return c.getStringCellValue(); }
                    catch (Exception e) { return String.valueOf(c.getNumericCellValue()); }
                case BLANK:
                default: return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String p = phone.trim();
        if (p.isEmpty()) return null;

        // Convert scientific notation BEFORE stripping non-digits
        if (p.matches("[-+]?\\d+(\\.\\d+)?[eE][-+]?\\d+")) {
            try {
                p = new BigDecimal(p).setScale(0, RoundingMode.HALF_UP).toPlainString();
            } catch (Exception ignored) {}
        }

        // Remove spaces, (), -
        p = p.replaceAll("[\\s()\\-]", "");

        // Keep only + and digits
        p = p.replaceAll("[^+0-9]", "");

        // collapse multiple leading '+' like '++91...'
        while (p.startsWith("++")) p = p.substring(1);

        // handle international prefix 00 (0091xxxx -> +91xxxx)
        if (p.startsWith("00")) p = "+" + p.substring(2);

        // drop leading 0 like 09711...
        if (p.startsWith("0") && p.length() > 1 && p.substring(1).matches("\\d+")) {
            p = p.substring(1);
        }

        // Add + if missing. If 10 digits, assume India (+91)
        if (!p.startsWith("+") && p.matches("\\d+")) {
            if (p.length() == 10) return "+91" + p;
            return "+" + p;
        }

        return p;
    }


    private String safeMsg(Exception e) {
        if (e == null) return "unknown";
        String m = e.getMessage();
        if (!notBlank(m)) return e.getClass().getSimpleName();
        return m;
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private Integer parseIntLoose(String s) {
        if (!notBlank(s)) return null;
        try {
            String cleaned = s.replaceAll("[^0-9\\-]", "");
            if (cleaned.isEmpty() || cleaned.equals("-")) return null;
            return Integer.parseInt(cleaned);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long parseLongLoose(String s) {
        if (!notBlank(s)) return null;
        try {
            String cleaned = s.replaceAll("[^0-9\\-]", "");
            if (cleaned.isEmpty() || cleaned.equals("-")) return null;
            return Long.parseLong(cleaned);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        String e = email.trim().toLowerCase();
        if (e.isEmpty()) return null;
        // remove spaces inside accidentally pasted emails
        e = e.replaceAll("\\s+", "");
        return e;
    }

    private boolean isValidEmailLoose(String email) {
        if (!notBlank(email)) return false;
        // loose check (avoid rejecting real emails)
        return email.contains("@") && email.indexOf('@') > 0 && email.indexOf('@') < email.length() - 1;
    }

    private String normalizeZip(String zip) {
        if (zip == null) return null;
        String z = zip.trim();
        if (z.isEmpty()) return null;
        // keep letters/digits/- only (supports many formats)
        z = z.replaceAll("[^A-Za-z0-9\\-]", "");
        return z.isEmpty() ? null : z;
    }

    private String normalizeCountry(String country) {
        if (country == null) return null;
        String c = country.trim();
        if (c.isEmpty()) return null;
        // example normalization
        if (c.equalsIgnoreCase("india") || c.equalsIgnoreCase("in")) return "India";
        return c;
    }

    private String normalizePesel(String pesel) {
        if (pesel == null) return null;
        String p = pesel.trim();
        if (p.isEmpty()) return null;
        // keep digits only (if user typed spaces/dashes)
        p = p.replaceAll("\\D+", "");
        return p.isEmpty() ? null : p;
    }

}
