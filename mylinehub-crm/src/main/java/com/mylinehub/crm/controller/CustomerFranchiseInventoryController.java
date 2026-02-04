package com.mylinehub.crm.controller;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.CustomerFranchiseInventoryDTO;
import com.mylinehub.crm.entity.dto.FranchiseInventoryPageDTO;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.CustomerFranchiseInventoryService;

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

import static com.mylinehub.crm.controller.ApiMapping.CUSTOMER_FRANCHISE_INVENTORY_REST_URL;

@RestController
@RequestMapping(produces = "application/json", path = CUSTOMER_FRANCHISE_INVENTORY_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class CustomerFranchiseInventoryController {

    private final CustomerFranchiseInventoryService franchiseService;

    private final EmployeeRepository employeeRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;

    @GetMapping("/findAllByOrganization")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<FranchiseInventoryPageDTO> findAllByOrganization(
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

        FranchiseInventoryPageDTO resp = null;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
        Employee employee = new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

        if (employee.getOrganization().trim().equals(organization.trim())) {
            resp = franchiseService.findAllFranchiseByOrganization(organization, searchText, available, pageable);
            return status(HttpStatus.OK).body(resp);
        } else {
            return status(HttpStatus.UNAUTHORIZED).body(resp);
        }
    }

    @GetMapping("/getByIdAndOrganization")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<CustomerFranchiseInventoryDTO> getByIdAndOrganization(
            @RequestParam Long id,
            @RequestParam String organization,
            @RequestHeader(name="Authorization") String token
    ) {

        CustomerFranchiseInventoryDTO resp = null;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
        Employee employee = new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

        if (employee.getOrganization().trim().equals(organization.trim())) {
            resp = franchiseService.getByIdAndOrganization(id, organization);
            return status(HttpStatus.OK).body(resp);
        } else {
            return status(HttpStatus.UNAUTHORIZED).body(resp);
        }
    }

    @GetMapping("/getByCustomerIdAndOrganization")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<CustomerFranchiseInventoryDTO> getByCustomerIdAndOrganization(
            @RequestParam Long customerId,
            @RequestParam String organization,
            @RequestHeader(name="Authorization") String token
    ) {

        CustomerFranchiseInventoryDTO resp = null;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
        Employee employee = new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

        if (employee.getOrganization().trim().equals(organization.trim())) {
            resp = franchiseService.getByCustomerIdAndOrganization(customerId, organization);
            return status(HttpStatus.OK).body(resp);
        } else {
            return status(HttpStatus.UNAUTHORIZED).body(resp);
        }
    }
    
    @GetMapping("/fetchExcelAfterCreatedDate")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public void exportFranchiseAfterCreatedDate(
            @RequestParam String organization,
            @RequestParam String fromCreatedDateIso, // example: 2025-01-01T00:00:00Z
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

        Instant from = Instant.parse(fromCreatedDateIso.trim());
        franchiseService.exportToExcelAfterCreatedDate(organization, from, available, response);
        response.setStatus(HttpStatus.OK.value());
    }
}
