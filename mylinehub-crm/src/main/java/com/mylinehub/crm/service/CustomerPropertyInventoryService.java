package com.mylinehub.crm.service;

import com.mylinehub.crm.entity.CustomerPropertyInventory;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.dto.CustomerPropertyInventoryDTO;
import com.mylinehub.crm.entity.dto.InventoryPageDTO;
import com.mylinehub.crm.exports.excel.ExportCustomerPropertyInventoryToXLSX;
import com.mylinehub.crm.repository.CustomerPropertyInventoryRepository;
import com.mylinehub.crm.whatsapp.data.WhatsAppCustomerData;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerDataDto;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerParameterDataDto;

import io.jsonwebtoken.io.IOException;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

@Service
@AllArgsConstructor
public class CustomerPropertyInventoryService {

    private final CustomerPropertyInventoryRepository inventoryRepository;
    
    @Transactional
    public CustomerPropertyInventory saveOne(CustomerPropertyInventory inv) {
        if (inv == null) return null;
        CustomerPropertyInventory saved = inventoryRepository.save(inv);
        inventoryRepository.flush(); // ensure ID is generated now
        return saved;
    }

    
    @Transactional
    public int saveCustomerPropertyInventoriesInBatches(List<CustomerPropertyInventory> inventories, int batchSize) {
        if (inventories == null || inventories.isEmpty()) return 0;

        int size = Math.max(1, batchSize);
        int saved = 0;

        for (int i = 0; i < inventories.size(); i += size) {
            int end = Math.min(i + size, inventories.size());
            List<CustomerPropertyInventory> batch = inventories.subList(i, end);

            inventoryRepository.saveAll(batch);
            inventoryRepository.flush(); // force DB write now

            saved += batch.size();
        }
        return saved;
    }

    
    public InventoryPageDTO findAllInventoryByOrganization(String organization, String searchText, Boolean available,Pageable pageable) {

        String org = (organization == null) ? "" : organization.trim();
        String q = (searchText == null) ? "" : searchText.trim();

        if (org.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "organization is required");
        }

        InventoryPageDTO out = new InventoryPageDTO();

        // page 0 => Page (counts)
        if (pageable.getPageNumber() == 0) {
            Page<CustomerPropertyInventory> page =
                    inventoryRepository.findAllByOrganizationWithArea_Page0_UpdatedByAi(org, q, available,pageable);
            overlayFromWhatsAppMemory(page.getContent());
            List<CustomerPropertyInventoryDTO> dto =
                    page.getContent().stream().map(this::toDto).collect(Collectors.toList());

            out.setData(dto);
            out.setTotalRecords(page.getTotalElements());
            out.setWithAreaRecords(page.getTotalElements());
            out.setNumberOfPages(page.getTotalPages());
            return out;
        }

        // page 1.. => Slice (fast)
        Slice<CustomerPropertyInventory> slice =
                inventoryRepository.findAllByOrganizationWithArea_Slice_UpdatedByAi(org, q,available, pageable);
        overlayFromWhatsAppMemory(slice.getContent());
        List<CustomerPropertyInventoryDTO> dto =
                slice.getContent().stream().map(this::toDto).collect(Collectors.toList());

        out.setData(dto);
        return out;
    }

    public CustomerPropertyInventoryDTO getInventoryByIdAndOrganization(Long inventoryId, String organization) {

        String org = (organization == null) ? "" : organization.trim();
        if (org.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "organization is required");
        }
        if (inventoryId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "inventoryId is required");
        }

        CustomerPropertyInventory inv =
                inventoryRepository.findByIdAndOrganizationWithCustomer(inventoryId, org)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Inventory not found for this organization (or area is empty)."
                        ));
        overlayFromWhatsAppMemory(java.util.List.of(inv));
        return toDto(inv);
    }

    public CustomerPropertyInventoryDTO getInventoryByCustomerIdAndOrganization(Long customerId, String organization) {

        String org = (organization == null) ? "" : organization.trim();
        if (org.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "organization is required");
        }
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");
        }

        CustomerPropertyInventory inv =
                inventoryRepository.findByCustomerIdAndOrganizationWithCustomer(customerId, org)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Inventory not found for this customer/org (or area is empty)."
                        ));
        overlayFromWhatsAppMemory(java.util.List.of(inv));
        return toDto(inv);
    }

    // ---------------------------------
    // Mapper: entity -> DTO (FLAT)
    // ---------------------------------
    private CustomerPropertyInventoryDTO toDto(CustomerPropertyInventory i) {

        Customers c = i.getCustomer();

        CustomerPropertyInventoryDTO dto = new CustomerPropertyInventoryDTO();
        dto.setId(i.getId());

        // ---- FLAT customer fields ----
        if (c != null) {
            dto.setCustomerId(c.getId());
            dto.setCustomerFirstname(c.getFirstname());
            dto.setCustomerLastname(c.getLastname());
            dto.setCustomerPhoneNumber(c.getPhoneNumber());
            dto.setCustomerEmail(c.getEmail());
            dto.setCustomerCity(c.getCity());
            dto.setCustomerOrganization(c.getOrganization());
        }

        // ---- inventory fields ----
        dto.setPremiseName(i.getPremiseName());
        dto.setListedDate(i.getListedDate());
        dto.setPropertyType(i.getPropertyType());

        dto.setRent(i.isRent());
        dto.setRentValue(i.getRentValue());

        dto.setBhk(i.getBhk());
        dto.setFurnishedType(i.getFurnishedType());
        dto.setSqFt(i.getSqFt());
        dto.setNearby(i.getNearby());
        dto.setPurpose(i.getPurpose());
        
        dto.setArea(i.getArea());
        dto.setCity(i.getCity());

        dto.setCallStatus(i.getCallStatus());
        dto.setPropertyAge(i.getPropertyAge());

        dto.setUnitType(i.getUnitType());
        dto.setTenant(i.getTenant());
        dto.setFacing(i.getFacing());

        dto.setTotalFloors(i.getTotalFloors());
        dto.setBrokerage(i.getBrokerage());

        dto.setBalconies(i.getBalconies());
        dto.setWashroom(i.getWashroom());

        dto.setUnitNo(i.getUnitNo());
        dto.setFloorNo(i.getFloorNo());

        dto.setPid(i.getPid());
        dto.setPropertyDescription1(i.getPropertyDescription1());
        dto.setMoreThanOneProperty(i.getMoreThanOneProperty());

        dto.setCreatedOn(i.getCreatedOn());
        dto.setLastUpdatedOn(i.getLastUpdatedOn());

        return dto;
    }
    
    public void exportToExcelAfterListedDate(String organization, Instant from, Boolean available, HttpServletResponse response) throws IOException, java.io.IOException {
        response.setContentType(
        	    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        	);
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=inventory_after_" + from.toString().replace(":", "-") + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<CustomerPropertyInventory> rows =
        		inventoryRepository.findAllForExportAfterListedDate_UpdatedByAi(organization, from,available);

        ExportCustomerPropertyInventoryToXLSX exporter = new ExportCustomerPropertyInventoryToXLSX(rows);
        exporter.export(response);
    }
    
    private void overlayFromWhatsAppMemory(List<CustomerPropertyInventory> rows) {
        try {
            if (rows == null || rows.isEmpty()) return;

            // inventoryId -> cacheKey (phone + org)
            Map<Long, String> invIdToCacheKey = WhatsAppCustomerData.getSavedInventoryIdToCacheKey();
            if (invIdToCacheKey == null || invIdToCacheKey.isEmpty()) return;
            
            List<String> cacheKeys = new ArrayList<>(invIdToCacheKey.values());
            // Fetch full snapshot ONCE (safe copy returned by your WhatsAppCustomerData "get")
            WhatsAppCustomerParameterDataDto p = new WhatsAppCustomerParameterDataDto();
            p.setAction("get-many-by-cache-keys");
            p.setCacheKeys(cacheKeys);
            Map<String, WhatsAppCustomerDataDto> mem = WhatsAppCustomerData.workWithWhatsAppCustomerData(p);
            if (mem == null || mem.isEmpty()) return;

            for (CustomerPropertyInventory dbInv : rows) {
                if (dbInv == null) continue;

                Long invId = dbInv.getId();
                if (invId == null) continue;

                String cacheKey = invIdToCacheKey.get(invId);
                if (cacheKey == null || cacheKey.trim().isEmpty()) continue;

                WhatsAppCustomerDataDto memDto = mem.get(cacheKey);
                if (memDto == null) continue;

                Customers memCust = memDto.getCustomer();
                if (memCust == null) continue;

                CustomerPropertyInventory memInv = memCust.getPropertyInventory();
                if (memInv == null) continue;

                // -------------------------------
                // Overlay inventory from MEMORY -> DB entity (for response only)
                // -------------------------------
                // Strings: overwrite if memory has text
                if (memInv.getPurpose() != null && !memInv.getPurpose().trim().isEmpty()) dbInv.setPurpose(memInv.getPurpose().trim());
                if (memInv.getPremiseName() != null && !memInv.getPremiseName().trim().isEmpty()) dbInv.setPremiseName(memInv.getPremiseName().trim());
                if (memInv.getPropertyType() != null && !memInv.getPropertyType().trim().isEmpty()) dbInv.setPropertyType(memInv.getPropertyType().trim());
                if (memInv.getFurnishedType() != null && !memInv.getFurnishedType().trim().isEmpty()) dbInv.setFurnishedType(memInv.getFurnishedType().trim());
                if (memInv.getCity() != null && !memInv.getCity().trim().isEmpty()) dbInv.setCity(memInv.getCity().trim());
                if (memInv.getNearby() != null && !memInv.getNearby().trim().isEmpty()) dbInv.setNearby(memInv.getNearby().trim());
                if (memInv.getArea() != null && !memInv.getArea().trim().isEmpty()) dbInv.setArea(memInv.getArea().trim());
                if (memInv.getUnitType() != null && !memInv.getUnitType().trim().isEmpty()) dbInv.setUnitType(memInv.getUnitType().trim());
                if (memInv.getTenant() != null && !memInv.getTenant().trim().isEmpty()) dbInv.setTenant(memInv.getTenant().trim());
                if (memInv.getFacing() != null && !memInv.getFacing().trim().isEmpty()) dbInv.setFacing(memInv.getFacing().trim());
                if (memInv.getBrokerage() != null && !memInv.getBrokerage().trim().isEmpty()) dbInv.setBrokerage(memInv.getBrokerage().trim());
                if (memInv.getUnitNo() != null && !memInv.getUnitNo().trim().isEmpty()) dbInv.setUnitNo(memInv.getUnitNo().trim());
                if (memInv.getFloorNo() != null && !memInv.getFloorNo().trim().isEmpty()) dbInv.setFloorNo(memInv.getFloorNo().trim());
                if (memInv.getPid() != null && !memInv.getPid().trim().isEmpty()) dbInv.setPid(memInv.getPid().trim());
                if (memInv.getPropertyDescription1() != null && !memInv.getPropertyDescription1().trim().isEmpty())
                    dbInv.setPropertyDescription1(memInv.getPropertyDescription1().trim());

                // Always safe overwrite
                dbInv.setRent(memInv.isRent());

                // Non-String fields
                if (memInv.getListedDate() != null) dbInv.setListedDate(memInv.getListedDate());
                if (memInv.getRentValue() != null) dbInv.setRentValue(memInv.getRentValue());
                if (memInv.getBhk() != null) dbInv.setBhk(memInv.getBhk());
                if (memInv.getSqFt() != null) dbInv.setSqFt(memInv.getSqFt());
                if (memInv.getCallStatus() != null && !memInv.getCallStatus().trim().isEmpty()) dbInv.setCallStatus(memInv.getCallStatus().trim());
                if (memInv.getPropertyAge() != null) dbInv.setPropertyAge(memInv.getPropertyAge());
                if (memInv.getTotalFloors() != null) dbInv.setTotalFloors(memInv.getTotalFloors());
                if (memInv.getBalconies() != null) dbInv.setBalconies(memInv.getBalconies());
                if (memInv.getWashroom() != null) dbInv.setWashroom(memInv.getWashroom());
                if (memInv.getMoreThanOneProperty() != null) dbInv.setMoreThanOneProperty(memInv.getMoreThanOneProperty());
                if (memInv.getUpdatedByAi() != null) dbInv.setUpdatedByAi(memInv.getUpdatedByAi());
                if (memInv.getAvailable() != null) dbInv.setAvailable(memInv.getAvailable());

                
                // -------------------------------
                // Overlay customer name updates too (optional but you said AI may update)
                // -------------------------------
                Customers dbCust = dbInv.getCustomer();
                if (dbCust != null) {
                    if (memCust.getFirstname() != null && !memCust.getFirstname().trim().isEmpty())
                        dbCust.setFirstname(memCust.getFirstname().trim());

                    if (memCust.getLastname() != null && !memCust.getLastname().trim().isEmpty())
                        dbCust.setLastname(memCust.getLastname().trim());

                    // keep email/phone/org from DB (usually correct), so not overwriting here
                    dbInv.setCustomer(dbCust);
                }
            }

        } catch (Exception e) {
            System.out.println("[InventoryService] overlayFromWhatsAppMemory ERROR");
            e.printStackTrace();
        }
    }

}
