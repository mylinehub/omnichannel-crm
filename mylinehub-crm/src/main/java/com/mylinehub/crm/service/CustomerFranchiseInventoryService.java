package com.mylinehub.crm.service;

import com.mylinehub.crm.entity.CustomerFranchiseInventory;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.dto.CustomerFranchiseInventoryDTO;
import com.mylinehub.crm.entity.dto.FranchiseInventoryPageDTO;
import com.mylinehub.crm.exports.excel.ExportCustomerFranchiseInventoryToXLSX;
import com.mylinehub.crm.repository.CustomerFranchiseInventoryRepository;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

@Service
@AllArgsConstructor
public class CustomerFranchiseInventoryService {

    private final CustomerFranchiseInventoryRepository franchiseRepository;

    public FranchiseInventoryPageDTO findAllFranchiseByOrganization(
            String organization,
            String searchText,
            Boolean available,
            Pageable pageable
    ) {

        String org = (organization == null) ? "" : organization.trim();
        String q = (searchText == null) ? "" : searchText.trim();

        if (org.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "organization is required");
        }

        FranchiseInventoryPageDTO out = new FranchiseInventoryPageDTO();

        // page 0 => Page (counts)
        if (pageable.getPageNumber() == 0) {
            Page<CustomerFranchiseInventory> page =
                franchiseRepository.findAllByOrganization_Page0(org, q, available, pageable);

            List<CustomerFranchiseInventoryDTO> dto =
                page.getContent().stream().map(this::toDto).collect(Collectors.toList());

            out.setData(dto);
            out.setTotalRecords(page.getTotalElements());
            out.setNumberOfPages(page.getTotalPages());
            return out;
        }

        // page 1.. => Slice (fast)
        Slice<CustomerFranchiseInventory> slice =
            franchiseRepository.findAllByOrganization_Slice(org, q, available, pageable);

        List<CustomerFranchiseInventoryDTO> dto =
            slice.getContent().stream().map(this::toDto).collect(Collectors.toList());

        out.setData(dto);
        return out;
    }

    public CustomerFranchiseInventoryDTO getByIdAndOrganization(Long id, String organization) {

        String org = (organization == null) ? "" : organization.trim();
        if (org.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "organization is required");
        if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id is required");

        CustomerFranchiseInventory f =
            franchiseRepository.findByIdAndOrganizationWithCustomer(id, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise row not found"));

        return toDto(f);
    }

    public CustomerFranchiseInventoryDTO getByCustomerIdAndOrganization(Long customerId, String organization) {

        String org = (organization == null) ? "" : organization.trim();
        if (org.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "organization is required");
        if (customerId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");

        CustomerFranchiseInventory f =
            franchiseRepository.findByCustomerIdAndOrganizationWithCustomer(customerId, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise row not found"));

        return toDto(f);
    }

    // ---------------------------------
    // Mapper: entity -> DTO (FLAT)
    // ---------------------------------
    private CustomerFranchiseInventoryDTO toDto(CustomerFranchiseInventory f) {

        Customers c = f.getCustomer();

        CustomerFranchiseInventoryDTO dto = new CustomerFranchiseInventoryDTO();
        dto.setId(f.getId());

        if (c != null) {
            dto.setCustomerId(c.getId());
            dto.setCustomerFirstname(c.getFirstname());
            dto.setCustomerLastname(c.getLastname());
            dto.setCustomerPhoneNumber(c.getPhoneNumber());
            dto.setCustomerEmail(c.getEmail());
            dto.setCustomerCity(c.getCity());
            dto.setCustomerOrganization(c.getOrganization());
        }

        dto.setInterest(f.getInterest());
        dto.setAvailable(f.getAvailable());

        dto.setCreatedOn(f.getCreatedOn());
        dto.setLastUpdatedOn(f.getLastUpdatedOn());

        return dto;
    }
    
    public void exportToExcelAfterCreatedDate(
            String organization,
            Instant from,
            Boolean available,
            HttpServletResponse response
    ) throws java.io.IOException {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=franchise_after_" + from.toString().replace(":", "-") + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<CustomerFranchiseInventory> rows =
                franchiseRepository.findAllForExportAfterCreatedDate(organization, from, available);

        ExportCustomerFranchiseInventoryToXLSX exporter = new ExportCustomerFranchiseInventoryToXLSX(rows);
        exporter.export(response);
    }
}
