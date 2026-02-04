package com.mylinehub.crm.controller;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.CustomerPropertyInventoryDTO;
import com.mylinehub.crm.entity.dto.InventoryPageDTO;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.CustomerPropertyInventoryService;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.ResponseEntity.status;

import java.io.IOException;
import java.time.Instant;

import static com.mylinehub.crm.controller.ApiMapping.CUSTOMER_PROPERTY_INVENTORY_REST_URL;

@RestController
@RequestMapping(produces = "application/json", path = CUSTOMER_PROPERTY_INVENTORY_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class CustomerPropertyInventoryController {

    private final CustomerPropertyInventoryService inventoryService;

    private final EmployeeRepository employeeRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;

    // List inventory (ONLY where area not null/blank), pagination same style as Customers
    @GetMapping("/findAllByOrganization")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<InventoryPageDTO> findAllByOrganization(
            @RequestParam String organization,
            @RequestParam(defaultValue = "") final String searchText,
            @RequestParam(required = false) final Boolean available,
            @RequestParam(defaultValue = "0") final Integer pageNumber,
            @RequestParam(defaultValue = "10") final Integer size,
            @RequestHeader(name="Authorization") String token
    ) {

        Pageable pageable;
        if (pageNumber < 0) {
            pageable = PageRequest.of(0, 1000000000);
        } else {
            pageable = PageRequest.of(pageNumber, size);
        }

        InventoryPageDTO resp = null;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
        Employee employee = new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

        if (employee.getOrganization().trim().equals(organization.trim())) {
            resp = inventoryService.findAllInventoryByOrganization(organization, searchText, available,pageable);
            return status(HttpStatus.OK).body(resp);
        } else {
            return status(HttpStatus.UNAUTHORIZED).body(resp);
        }
    }

    // Get inventory by inventoryId (ONLY if area not null/blank)
    @GetMapping("/getByIdAndOrganization")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<CustomerPropertyInventoryDTO> getByIdAndOrganization(
            @RequestParam Long inventoryId,
            @RequestParam String organization,
            @RequestHeader(name="Authorization") String token
    ) {

        CustomerPropertyInventoryDTO resp = null;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
        Employee employee = new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

        if (employee.getOrganization().trim().equals(organization.trim())) {
            resp = inventoryService.getInventoryByIdAndOrganization(inventoryId, organization);
            return status(HttpStatus.OK).body(resp);
        } else {
            return status(HttpStatus.UNAUTHORIZED).body(resp);
        }
    }

    // Get inventory by customerId (ONLY if area not null/blank)
    @GetMapping("/getByCustomerIdAndOrganization")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<CustomerPropertyInventoryDTO> getByCustomerIdAndOrganization(
            @RequestParam Long customerId,
            @RequestParam String organization,
            @RequestHeader(name="Authorization") String token
    ) {

        CustomerPropertyInventoryDTO resp = null;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
        Employee employee = new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

        if (employee.getOrganization().trim().equals(organization.trim())) {
            resp = inventoryService.getInventoryByCustomerIdAndOrganization(customerId, organization);
            return status(HttpStatus.OK).body(resp);
        } else {
            return status(HttpStatus.UNAUTHORIZED).body(resp);
        }
    }
    
    @GetMapping("/fetchExcelAfterListedDate")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public void exportInventoryAfterListedDate(
            @RequestParam String organization,
            @RequestParam String fromListedDateIso, // example: 2025-01-01T00:00:00Z
            @RequestParam(defaultValue = "true") final Boolean available,
            HttpServletResponse response,
            @RequestHeader(name = "Authorization") String token
    ) throws IOException {

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
        Employee employee = new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

        if (!employee.getOrganization().trim().equals(organization.trim())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Instant from = Instant.parse(fromListedDateIso.trim());
        inventoryService.exportToExcelAfterListedDate(organization, from,available, response);
        response.setStatus(HttpStatus.OK.value());
    }
}
