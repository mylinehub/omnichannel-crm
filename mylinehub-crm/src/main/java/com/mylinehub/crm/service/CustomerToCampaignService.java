package com.mylinehub.crm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.CustomerToCampaign;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.dto.CustomerToCampaignCreateDTO;
import com.mylinehub.crm.entity.dto.CustomerToCampaignDTO;
import com.mylinehub.crm.entity.dto.CustomerToCampaignPageDTO;
import com.mylinehub.crm.mapper.CustomerToCampaignMapper;
import com.mylinehub.crm.repository.CampaignRepository;
import com.mylinehub.crm.repository.CustomerRepository;
import com.mylinehub.crm.repository.CustomerToCampaignRepository;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class CustomerToCampaignService implements CurrentTimeInterface {

    // ============================================================
    // DEEP LOGS TOGGLE
    // ============================================================
    private static final boolean DEEP_LOGS = true;

    private final CustomerRepository customerRepository;
    private final CustomerToCampaignRepository customerToCampaignRepository;
    private final CampaignRepository campaignRepository;
    private final CustomerToCampaignMapper CustomerToCampaignMapper;

    // ----------------------------------------------------
    // FIX: Always keep pagination ordering stable
    // MUST match repo: "order by e.id desc"
    // ----------------------------------------------------
    private Pageable stablePageable(Pageable pageable) {
        if (pageable == null) {
            if (DEEP_LOGS) System.out.println("[CTC][stablePageable] pageable=null => default 0,10 sort=id desc");
            return PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        }
        Pageable stable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );
        if (DEEP_LOGS) {
            System.out.println("[CTC][stablePageable] in page=" + pageable.getPageNumber()
                    + " size=" + pageable.getPageSize()
                    + " => stable sort=id desc");
        }
        return stable;
    }


    private String n(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        return s; // important: DB side lower() only on column
    }

    private String like(String s) {
        s = n(s);
        return (s == null) ? null : "%" + s + "%";
    }

    private boolean isTrue(String v) {
        // tolerant: "true", "TRUE", " True ", etc.
        return v != null && "true".equalsIgnoreCase(v.trim());
    }

    private void log(String msg) {
        if (DEEP_LOGS) System.out.println(msg);
    }

    public List<CustomerToCampaignDTO> findAllByCustomerAndOrganization(String phoneNumber, String organization) {

        log("[CTC][findAllByCustomerAndOrganization] START phoneNumber=" + phoneNumber + " org=" + organization);

        Customers current = customerRepository.getCustomerByPhoneNumberAndOrganization(phoneNumber, organization);

        if (current == null) {
            log("[CTC][findAllByCustomerAndOrganization] customer NOT FOUND => return null");
            return null;
        }

        List<CustomerToCampaignDTO> out = customerToCampaignRepository.findAllByCustomerAndOrganization(current, organization)
                .stream()
                .map(CustomerToCampaignMapper::mapCustomerToCampaignToDto)
                .collect(Collectors.toList());

        log("[CTC][findAllByCustomerAndOrganization] END rows=" + (out == null ? 0 : out.size()));
        return out;
    }

    public CustomerToCampaignPageDTO findAllByCampaignAndOrganization(Long id, String organization, String searchText, Pageable pageable) {

        log("[CTC][findAllByCampaignAndOrganization] START campaignId=" + id + " org=" + organization
                + " searchText=" + (searchText == null ? "null" : ("'" + searchText + "'"))
                + " page=" + (pageable == null ? "null" : pageable.getPageNumber())
                + " size=" + (pageable == null ? "null" : pageable.getPageSize()));

        Campaign current = campaignRepository.getCampaignByIdAndOrganization(id, organization);

        if (current == null) {
            log("[CTC][findAllByCampaignAndOrganization] campaign NOT FOUND => return null");
            return null;
        }

        CustomerToCampaignPageDTO toReturn = new CustomerToCampaignPageDTO();

        String q = (searchText == null) ? "" : searchText;
        Pageable stable = stablePageable(pageable);

        if (stable.getPageNumber() == 0) {
            log("[CTC][findAllByCampaignAndOrganization] Using Page query (page 0)");

            Page<CustomerToCampaign> response =
                    customerToCampaignRepository.findAllByCampaignAndOrganization(current, organization, q, stable);

            List<CustomerToCampaignDTO> returnPart = response.getContent()
                    .stream()
                    .map(CustomerToCampaignMapper::mapCustomerToCampaignToDto)
                    .collect(Collectors.toList());

            toReturn.setData(returnPart);
            toReturn.setTotalRecords(response.getTotalElements());
            toReturn.setNumberOfPages(response.getTotalPages());

            log("[CTC][findAllByCampaignAndOrganization] Page0 rows=" + response.getNumberOfElements()
                    + " totalRecords=" + response.getTotalElements()
                    + " totalPages=" + response.getTotalPages());
        } else {
            log("[CTC][findAllByCampaignAndOrganization] Using Slice query (page>0)");

            Slice<CustomerToCampaign> response =
                    customerToCampaignRepository.getAllByCampaignAndOrganization(current, organization, q, stable);

            List<CustomerToCampaignDTO> returnPart = response.getContent()
                    .stream()
                    .map(CustomerToCampaignMapper::mapCustomerToCampaignToDto)
                    .collect(Collectors.toList());

            toReturn.setData(returnPart);

            log("[CTC][findAllByCampaignAndOrganization] Slice rows=" + response.getNumberOfElements()
                    + " hasNext=" + response.hasNext());
        }

        log("[CTC][findAllByCampaignAndOrganization] END");
        return toReturn;
    }

    @Transactional
    public boolean createCustomerToCampaignByOrganization(CustomerToCampaignCreateDTO dto) {

        try {
            log("[CTC][createCustomerToCampaignByOrganization] START");

            if (dto == null) {
                log("[CTC][createCustomerToCampaignByOrganization][ERROR] dto is null");
                return false;
            }

            // Normalize inputs (trim + empty->null)
            dto.setCountry(like(dto.getCountry()));
            dto.setCity(like(dto.getCity()));
            dto.setZipCode(like(dto.getZipCode()));
            dto.setBusiness(like(dto.getBusiness()));
            dto.setDatatype(like(dto.getDatatype()));
            dto.setDescription(like(dto.getDescription()));

            boolean anyFilter =
                    dto.getCountry() != null ||
                    dto.getCity() != null ||
                    dto.getZipCode() != null ||
                    dto.getBusiness() != null ||
                    dto.getDatatype() != null ||
                    dto.getDescription() != null;

            log("[CTC][createCustomerToCampaignByOrganization] org=" + dto.getOrganization()
                    + " campaignId=" + dto.getCampaignId()
                    + " isAndOperator=" + dto.getIsAndOperator()
                    + " start=" + dto.getStart()
                    + " limit=" + dto.getLimit());
            log("[CTC][createCustomerToCampaignByOrganization] filters:"
                    + " country=" + dto.getCountry()
                    + " city=" + dto.getCity()
                    + " zip=" + dto.getZipCode()
                    + " business=" + dto.getBusiness()
                    + " datatype=" + dto.getDatatype()
                    + " description=" + dto.getDescription());
            log("[CTC][createCustomerToCampaignByOrganization] anyFilter=" + anyFilter);

            // GUARD
            if (!anyFilter) {
                log("[CTC][createCustomerToCampaignByOrganization][GUARD] No filters provided. Refusing bulk insert.");
                return false;
            }

            Campaign currentCampaign = campaignRepository.findById(dto.getCampaignId()).orElse(null);
            if (currentCampaign == null) {
                log("[CTC][createCustomerToCampaignByOrganization][ERROR] campaign not found id=" + dto.getCampaignId());
                return false;
            }

            int pageSize = 2000;
            Pageable pageable = PageRequest.of(0, pageSize);
            Page<Customers> currentCustomerPage;

            Long totalRecords;
            int totalPages;

            int startOfRecord = dto.getStart();
            int endOfRecord = dto.getLimit();

            boolean andMode = isTrue(dto.getIsAndOperator());
            log("[CTC][createCustomerToCampaignByOrganization] mode=" + (andMode ? "AND" : "OR"));

            if (andMode) {
                currentCustomerPage = customerRepository.findAllToInsertIntoCampaignByAndAsPerParameters(
                        dto.getOrganization(),
                        dto.getCountry(),
                        dto.getCity(),
                        dto.getZipCode(),
                        dto.getBusiness(),
                        dto.getDatatype(),
                        dto.getDescription(),
                        pageable
                );
            } else {
                currentCustomerPage = customerRepository.findAllToInsertIntoCampaignByOrAsPerParameters(
                        dto.getOrganization(),
                        dto.getCountry(),
                        dto.getCity(),
                        dto.getZipCode(),
                        dto.getBusiness(),
                        dto.getDatatype(),
                        dto.getDescription(),
                        pageable
                );
            }

            totalRecords = currentCustomerPage.getTotalElements();
            totalPages = currentCustomerPage.getTotalPages();

            log("[CTC][createCustomerToCampaignByOrganization] totalRecords=" + totalRecords + " totalPages=" + totalPages);

            if (startOfRecord > totalRecords) {
                log("[CTC][createCustomerToCampaignByOrganization][ERROR] startOfRecord > totalRecords. start="
                        + startOfRecord + " total=" + totalRecords);
                return false;
            }

            // If both 0 => means "all"
            if (startOfRecord == 0 && endOfRecord == 0) {
                endOfRecord = totalRecords.intValue();
                log("[CTC][createCustomerToCampaignByOrganization] start=0 & limit=0 => set endOfRecord=" + endOfRecord);
            }

            if (endOfRecord > totalRecords) {
                endOfRecord = totalRecords.intValue();
                log("[CTC][createCustomerToCampaignByOrganization] endOfRecord capped to totalRecords => " + endOfRecord);
            }

            Slice<Customers> currentCustomerSlice;

            // Special: single record selection
            if ((startOfRecord == endOfRecord) && (startOfRecord != 0)) {
                Pageable one = PageRequest.of((startOfRecord - 1), 1);
                log("[CTC][createCustomerToCampaignByOrganization] single record mode page=" + (startOfRecord - 1));

                if (andMode) {
                    currentCustomerSlice = customerRepository.getAllToInsertIntoCampaignByAndAsPerParameters(
                            dto.getOrganization(),
                            dto.getCountry(),
                            dto.getCity(),
                            dto.getZipCode(),
                            dto.getBusiness(),
                            dto.getDatatype(),
                            dto.getDescription(),
                            one
                    );
                } else {
                    currentCustomerSlice = customerRepository.getAllToInsertIntoCampaignByOrAsPerParameters(
                            dto.getOrganization(),
                            dto.getCountry(),
                            dto.getCity(),
                            dto.getZipCode(),
                            dto.getBusiness(),
                            dto.getDatatype(),
                            dto.getDescription(),
                            one
                    );
                }

                if (currentCustomerSlice == null || currentCustomerSlice.getContent() == null || currentCustomerSlice.getContent().isEmpty()) {
                    log("[CTC][createCustomerToCampaignByOrganization][ERROR] single record slice empty");
                    return false;
                }

                CustomerToCampaign insert = new CustomerToCampaign();
                insert.setCampaign(currentCampaign);
                insert.setCustomer(currentCustomerSlice.getContent().get(0));
                insert.setOrganization(currentCampaign.getOrganization());

                customerToCampaignRepository.save(insert);

                log("[CTC][createCustomerToCampaignByOrganization] single record inserted customerId="
                        + (insert.getCustomer() != null ? insert.getCustomer().getId() : null));
                return true;
            }

            // Multi range mode
            int pageFrom = (startOfRecord / pageSize);
            int startIndex = (startOfRecord % pageSize); // 0-based
            int pageTo = (endOfRecord / pageSize);
            int endIndex = (endOfRecord % pageSize);     // 0-based boundary indicator

            log("[CTC][createCustomerToCampaignByOrganization] range:"
                    + " startOfRecord=" + startOfRecord
                    + " endOfRecord=" + endOfRecord
                    + " pageFrom=" + pageFrom + " startIndex=" + startIndex
                    + " pageTo=" + pageTo + " endIndex=" + endIndex);

            for (int i = pageFrom; i <= pageTo; i++) {

                Pageable p = PageRequest.of(i, pageSize);

                if (andMode) {
                    currentCustomerSlice = customerRepository.getAllToInsertIntoCampaignByAndAsPerParameters(
                            dto.getOrganization(),
                            dto.getCountry(),
                            dto.getCity(),
                            dto.getZipCode(),
                            dto.getBusiness(),
                            dto.getDatatype(),
                            dto.getDescription(),
                            p
                    );
                } else {
                    currentCustomerSlice = customerRepository.getAllToInsertIntoCampaignByOrAsPerParameters(
                            dto.getOrganization(),
                            dto.getCountry(),
                            dto.getCity(),
                            dto.getZipCode(),
                            dto.getBusiness(),
                            dto.getDatatype(),
                            dto.getDescription(),
                            p
                    );
                }

                List<Customers> allCustomers = (currentCustomerSlice == null ? null : currentCustomerSlice.getContent());
                int sliceSize = (allCustomers == null ? 0 : allCustomers.size());

                log("[CTC][createCustomerToCampaignByOrganization] page=" + i + " sliceSize=" + sliceSize);

                if (allCustomers == null || allCustomers.isEmpty()) {
                    log("[CTC][createCustomerToCampaignByOrganization] page=" + i + " empty slice -> continue");
                    continue;
                }

                List<CustomerToCampaign> insert = new ArrayList<>();

                // IMPORTANT FIX:
                // - previously you used (startIndex-1) which breaks when startIndex==0.
                // - use safe 0-based indices
                int fromIndex = 0;
                int toIndexInclusive = allCustomers.size() - 1;

                if (i == pageFrom) {
                    fromIndex = Math.max(0, startIndex);
                }

                if (i == pageTo) {
                    // endIndex is "position within page"
                    // if endIndex == 0 it means boundary aligned (e.g. 2000, 4000) => include entire page
                    if (endIndex == 0) {
                        toIndexInclusive = allCustomers.size() - 1;
                    } else {
                        toIndexInclusive = Math.min(allCustomers.size() - 1, endIndex - 1);
                    }
                }

                // If range is inverted, skip
                if (fromIndex > toIndexInclusive) {
                    log("[CTC][createCustomerToCampaignByOrganization] page=" + i + " fromIndex>toIndexInclusive ("
                            + fromIndex + ">" + toIndexInclusive + ") -> skip");
                    continue;
                }

                log("[CTC][createCustomerToCampaignByOrganization] page=" + i
                        + " selecting fromIndex=" + fromIndex + " toIndexInclusive=" + toIndexInclusive);

                for (int j = fromIndex; j <= toIndexInclusive; j++) {
                    Customers cust = allCustomers.get(j);

                    CustomerToCampaign current = new CustomerToCampaign();
                    current.setCampaign(currentCampaign);
                    current.setCustomer(cust);
                    current.setOrganization(currentCampaign.getOrganization());
                    insert.add(current);
                }

                log("[CTC][createCustomerToCampaignByOrganization] page=" + i + " inserting rows=" + insert.size());

                if (!insert.isEmpty()) {
                    customerToCampaignRepository.saveAll(insert);
                }
            }

            log("[CTC][createCustomerToCampaignByOrganization] END OK");
            return true;

        } catch (Exception e) {
            System.out.println("[CTC][createCustomerToCampaignByOrganization][EXCEPTION] " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Long getCountForCustomerToCampaignByOrganization(CustomerToCampaignCreateDTO dto) {

        log("[CTC][getCountForCustomerToCampaignByOrganization] START");

        if (dto == null) {
            log("[CTC][getCountForCustomerToCampaignByOrganization][ERROR] dto is null => return 0");
            return 0L;
        }

        dto.setCountry(like(dto.getCountry()));
        dto.setCity(like(dto.getCity()));
        dto.setZipCode(like(dto.getZipCode()));
        dto.setBusiness(like(dto.getBusiness()));
        dto.setDatatype(like(dto.getDatatype()));
        dto.setDescription(like(dto.getDescription()));

        boolean anyFilter =
                dto.getCountry() != null ||
                dto.getCity() != null ||
                dto.getZipCode() != null ||
                dto.getBusiness() != null ||
                dto.getDatatype() != null ||
                dto.getDescription() != null;

        log("[CTC][getCountForCustomerToCampaignByOrganization] org=" + dto.getOrganization()
                + " isAndOperator=" + dto.getIsAndOperator()
                + " filters:"
                + " country=" + dto.getCountry()
                + " city=" + dto.getCity()
                + " zip=" + dto.getZipCode()
                + " business=" + dto.getBusiness()
                + " datatype=" + dto.getDatatype()
                + " description=" + dto.getDescription()
                + " anyFilter=" + anyFilter);

        if (!anyFilter) {
            log("[CTC][getCountForCustomerToCampaignByOrganization][GUARD] No filters => return 0");
            return 0L;
        }

        Pageable pageable = PageRequest.of(0, 5);
        Page<Customers> currentCustomerPage;

        boolean andMode = isTrue(dto.getIsAndOperator());

        if (andMode) {
            currentCustomerPage = customerRepository.findAllToInsertIntoCampaignByAndAsPerParameters(
                    dto.getOrganization(),
                    dto.getCountry(),
                    dto.getCity(),
                    dto.getZipCode(),
                    dto.getBusiness(),
                    dto.getDatatype(),
                    dto.getDescription(),
                    pageable
            );
        } else {
            currentCustomerPage = customerRepository.findAllToInsertIntoCampaignByOrAsPerParameters(
                    dto.getOrganization(),
                    dto.getCountry(),
                    dto.getCity(),
                    dto.getZipCode(),
                    dto.getBusiness(),
                    dto.getDatatype(),
                    dto.getDescription(),
                    pageable
            );
        }

        long count = currentCustomerPage.getTotalElements();
        log("[CTC][getCountForCustomerToCampaignByOrganization] END count=" + count);
        return count;
    }

    public int updateCustomerToCampaignByOrganization(List<CustomerToCampaignDTO> customersToCampaign) {

        log("[CTC][updateCustomerToCampaignByOrganization] START rows=" + (customersToCampaign == null ? 0 : customersToCampaign.size()));

        int numberOfRow = 0;

        if (customersToCampaign == null || customersToCampaign.isEmpty()) {
            log("[CTC][updateCustomerToCampaignByOrganization] empty input => return 0");
            return 0;
        }

        customersToCampaign.forEach((customerToCampaign) -> {
            try {
                if (customerToCampaign == null) return;

                CustomerToCampaign currentCustomerToCampaign =
                        customerToCampaignRepository.findById(customerToCampaign.getId()).orElse(null);

                if (currentCustomerToCampaign == null) {
                    log("[CTC][update] missing mapping id=" + customerToCampaign.getId());
                    return;
                }

                Customers currentCustomer = customerRepository.findById(customerToCampaign.getCustomerid()).orElse(null);
                Campaign currentCampaign = campaignRepository.findById(customerToCampaign.getCampaignid()).orElse(null);

                if (currentCampaign == null) {
                    log("[CTC][update] missing campaign id=" + customerToCampaign.getCampaignid());
                    return;
                }
                if (currentCustomer == null) {
                    log("[CTC][update] missing customer id=" + customerToCampaign.getCustomerid());
                    return;
                }

                if (currentCustomer.getOrganization() != null && currentCampaign.getOrganization() != null
                        && currentCustomer.getOrganization().trim().equals(currentCampaign.getOrganization().trim())) {

                    currentCustomerToCampaign.setCampaign(currentCampaign);
                    currentCustomerToCampaign.setCustomer(currentCustomer);
                    currentCustomerToCampaign.setOrganization(currentCampaign.getOrganization());

                    customerToCampaignRepository.save(currentCustomerToCampaign);
                    log("[CTC][update] updated mapping id=" + currentCustomerToCampaign.getId());
                } else {
                    log("[CTC][update] org mismatch customerOrg=" + currentCustomer.getOrganization()
                            + " campaignOrg=" + currentCampaign.getOrganization());
                }
            } catch (Exception e) {
                System.out.println("[CTC][update][EXCEPTION] " + e.getMessage());
                e.printStackTrace();
            }
        });

        log("[CTC][updateCustomerToCampaignByOrganization] END numberOfRow=" + numberOfRow + " (note: original code never increments)");
        return numberOfRow;
    }

    public int deleteCustomerToCampaignByOrganization(List<CustomerToCampaignDTO> customersToCampaign) {

        log("[CTC][deleteCustomerToCampaignByOrganization] START rows=" + (customersToCampaign == null ? 0 : customersToCampaign.size()));

        int numberOfRow = 0;

        if (customersToCampaign == null || customersToCampaign.isEmpty()) {
            log("[CTC][deleteCustomerToCampaignByOrganization] empty input => return 0");
            return 0;
        }

        customersToCampaign.forEach((customerToCampaign) -> {
            try {
                if (customerToCampaign == null) return;

                CustomerToCampaign currentCustomerToCampaign =
                        customerToCampaignRepository.findById(customerToCampaign.getId()).orElse(null);

                if (currentCustomerToCampaign == null) {
                    log("[CTC][delete] missing mapping id=" + customerToCampaign.getId());
                    return;
                }

                customerToCampaignRepository.delete(currentCustomerToCampaign);
                log("[CTC][delete] deleted mapping id=" + currentCustomerToCampaign.getId());
            } catch (Exception e) {
                System.out.println("[CTC][delete][EXCEPTION] " + e.getMessage());
                e.printStackTrace();
            }
        });

        log("[CTC][deleteCustomerToCampaignByOrganization] END numberOfRow=" + numberOfRow + " (note: original code never increments)");
        return numberOfRow;
    }

    /**
     * FIXED: deletes all pages correctly (no page-0 repeat)
     */
    public boolean deleteAllByCampaignAndOrganization(Campaign campaign, String organization) {
        boolean result = false;
        try {
            log("[CTC][deleteAllByCampaignAndOrganization] START campaignId=" + (campaign == null ? "null" : campaign.getId())
                    + " org=" + organization);

            if (campaign == null) {
                log("[CTC][deleteAllByCampaignAndOrganization][ERROR] campaign is null");
                return false;
            }

            int pageSize = 2000;

            Pageable pageable0 = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "id"));
            Page<CustomerToCampaign> currentPage =
                    customerToCampaignRepository.findAllByCampaignAndOrganization(campaign, organization, "", pageable0);

            int totalPages = currentPage.getTotalPages();
            int deleted0 = (currentPage.getContent() == null ? 0 : currentPage.getContent().size());

            customerToCampaignRepository.deleteAll(currentPage.getContent());
            log("[CTC][deleteAllByCampaignAndOrganization] deleted page0 rows=" + deleted0 + " totalPages=" + totalPages);

            for (int j = 1; j < totalPages; j++) {
                Pageable pageableJ = PageRequest.of(j, pageSize, Sort.by(Sort.Direction.DESC, "id"));
                Slice<CustomerToCampaign> currentSlice =
                        customerToCampaignRepository.getAllByCampaignAndOrganization(campaign, organization, "", pageableJ);

                int del = (currentSlice.getContent() == null ? 0 : currentSlice.getContent().size());
                customerToCampaignRepository.deleteAll(currentSlice.getContent());

                log("[CTC][deleteAllByCampaignAndOrganization] deleted page=" + j + " rows=" + del + " hasNext=" + currentSlice.hasNext());
            }

            result = true;
            log("[CTC][deleteAllByCampaignAndOrganization] END OK");

        } catch (Exception e) {
            System.out.println("[CTC][deleteAllByCampaignAndOrganization][EXCEPTION] " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}
