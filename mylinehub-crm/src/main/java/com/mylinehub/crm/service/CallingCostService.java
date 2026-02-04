//package com.mylinehub.crm.service;
//
//import java.io.IOException;
//import java.util.Date;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Slice;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//
//import com.mylinehub.crm.entity.CallDetail;
//import com.mylinehub.crm.entity.CallingCost;
//import com.mylinehub.crm.entity.dto.CallingCostDTO;
//import com.mylinehub.crm.entity.dto.CallingCostPageDTO;
//import com.mylinehub.crm.exports.excel.ExportCallingCostToXLSX;
//import com.mylinehub.crm.exports.pdf.ExportCallingCostToPDF;
//import com.mylinehub.crm.mapper.CallingCostMapper;
//import com.mylinehub.crm.repository.CallDetailRepository;
//import com.mylinehub.crm.repository.CallingCostRepository;
//
//import lombok.AllArgsConstructor;
//
///**
// * @author Anand Goel
// * @version 1.0
// */
//@Service
//@AllArgsConstructor
//public class CallingCostService implements CurrentTimeInterface {
//
//    private final CallingCostRepository callingCostRepository;
//    private final CallDetailRepository callDetailRepository;
//    private final CallingCostMapper callingCostMapper;
//
//    // Always keep pagination ordering stable across pages
//    private Pageable stablePageable(Pageable pageable) {
//        if (pageable == null) {
//            return PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
//        }
//        return PageRequest.of(
//                pageable.getPageNumber(),
//                pageable.getPageSize(),
//                Sort.by(Sort.Direction.DESC, "id") // MUST match repo order by e.id desc
//        );
//    }
//
//    public boolean addOrUpdateCallingCostByOrganization(CallingCostDTO callingCostDTO) {
//
//        // getOne() deprecated -> getReferenceById() (same behavior: proxy ref)
//        CallDetail current = callDetailRepository.getOne(callingCostDTO.getCallID());
//
//        CallingCost verifyCall =
//                callingCostRepository.findByCallDetailAndOrganization(current, callingCostDTO.getOrganization());
//
//        if (verifyCall == null) {
//            if (current == null) {
//                return false;
//            } else {
//                CallingCost toAdd = new CallingCost();
//
//                toAdd.setCallDetail(current);
//                toAdd.setCallcalculation(callingCostDTO.getCallcalculation());
//                toAdd.setOrganization(callingCostDTO.getOrganization());
//                toAdd.setAmount(callingCostDTO.getAmount());
//                callingCostRepository.save(toAdd);
//            }
//        } else {
//            verifyCall.setCallDetail(current);
//            verifyCall.setCallcalculation(callingCostDTO.getCallcalculation());
//            verifyCall.setOrganization(callingCostDTO.getOrganization());
//            verifyCall.setAmount(callingCostDTO.getAmount());
//            callingCostRepository.save(verifyCall);
//        }
//
//        return true;
//    }
//
//    public CallingCostPageDTO findAllByOrganization(String organization, String searchText, Pageable pageable) {
//
//        CallingCostPageDTO toReturn = new CallingCostPageDTO();
//
//        String q = (searchText == null) ? "" : searchText;
//        Pageable stable = stablePageable(pageable);
//
//        if (stable.getPageNumber() == 0) {
//            Page<CallingCost> response =
//                    callingCostRepository.findAllByOrganization(organization, q, stable);
//
//            List<CallingCostDTO> returnPart = response.getContent()
//                    .stream()
//                    .map(callingCostMapper::mapCallingCostToDTO)
//                    .collect(Collectors.toList());
//
//            toReturn.setData(returnPart);
//            toReturn.setTotalRecords(response.getTotalElements());
//            toReturn.setNumberOfPages(response.getTotalPages());
//        } else {
//            Slice<CallingCost> response =
//                    callingCostRepository.getAllByOrganization(organization, q, stable);
//
//            List<CallingCostDTO> returnPart = response.getContent()
//                    .stream()
//                    .map(callingCostMapper::mapCallingCostToDTO)
//                    .collect(Collectors.toList());
//
//            toReturn.setData(returnPart);
//        }
//
//        return toReturn;
//    }
//
//    public CallingCostPageDTO findAllByExtensionAndOrganization(String extension, String organization,
//                                                              String searchText, Pageable pageable) {
//
//        CallingCostPageDTO toReturn = new CallingCostPageDTO();
//
//        String q = (searchText == null) ? "" : searchText;
//        Pageable stable = stablePageable(pageable);
//
//        if (stable.getPageNumber() == 0) {
//            Page<CallingCost> response =
//                    callingCostRepository.findAllByExtensionAndOrganization(extension, organization, q, stable);
//
//            List<CallingCostDTO> returnPart = response.getContent()
//                    .stream()
//                    .map(callingCostMapper::mapCallingCostToDTO)
//                    .collect(Collectors.toList());
//
//            toReturn.setData(returnPart);
//            toReturn.setTotalRecords(response.getTotalElements());
//            toReturn.setNumberOfPages(response.getTotalPages());
//        } else {
//            Slice<CallingCost> response =
//                    callingCostRepository.getAllByExtensionAndOrganization(extension, organization, q, stable);
//
//            List<CallingCostDTO> returnPart = response.getContent()
//                    .stream()
//                    .map(callingCostMapper::mapCallingCostToDTO)
//                    .collect(Collectors.toList());
//
//            toReturn.setData(returnPart);
//        }
//
//        return toReturn;
//    }
//
//    public CallingCostPageDTO findAllByAmountGreaterThanEqualAndOrganization(Double amount, String organization,
//                                                                            String searchText, Pageable pageable) {
//
//        CallingCostPageDTO toReturn = new CallingCostPageDTO();
//
//        String q = (searchText == null) ? "" : searchText;
//        Pageable stable = stablePageable(pageable);
//
//        if (stable.getPageNumber() == 0) {
//            Page<CallingCost> response =
//                    callingCostRepository.findAllByAmountGreaterThanEqualAndOrganization(amount, organization, q, stable);
//
//            List<CallingCostDTO> returnPart = response.getContent()
//                    .stream()
//                    .map(callingCostMapper::mapCallingCostToDTO)
//                    .collect(Collectors.toList());
//
//            toReturn.setData(returnPart);
//            toReturn.setTotalRecords(response.getTotalElements());
//            toReturn.setNumberOfPages(response.getTotalPages());
//        } else {
//            Slice<CallingCost> response =
//                    callingCostRepository.getAllByAmountGreaterThanEqualAndOrganization(amount, organization, q, stable);
//
//            List<CallingCostDTO> returnPart = response.getContent()
//                    .stream()
//                    .map(callingCostMapper::mapCallingCostToDTO)
//                    .collect(Collectors.toList());
//
//            toReturn.setData(returnPart);
//        }
//
//        return toReturn;
//    }
//
//    public CallingCostPageDTO findAllByAmountLessThanEqualAndOrganization(Double amount, String organization,
//                                                                         String searchText, Pageable pageable) {
//
//        CallingCostPageDTO toReturn = new CallingCostPageDTO();
//
//        String q = (searchText == null) ? "" : searchText;
//        Pageable stable = stablePageable(pageable);
//
//        if (stable.getPageNumber() == 0) {
//            Page<CallingCost> response =
//                    callingCostRepository.findAllByAmountLessThanEqualAndOrganization(amount, organization, q, stable);
//
//            List<CallingCostDTO> returnPart = response.getContent()
//                    .stream()
//                    .map(callingCostMapper::mapCallingCostToDTO)
//                    .collect(Collectors.toList());
//
//            toReturn.setData(returnPart);
//            toReturn.setTotalRecords(response.getTotalElements());
//            toReturn.setNumberOfPages(response.getTotalPages());
//        } else {
//            Slice<CallingCost> response =
//                    callingCostRepository.getAllByAmountLessThanEqualAndOrganization(amount, organization, q, stable);
//
//            List<CallingCostDTO> returnPart = response.getContent()
//                    .stream()
//                    .map(callingCostMapper::mapCallingCostToDTO)
//                    .collect(Collectors.toList());
//
//            toReturn.setData(returnPart);
//        }
//
//        return toReturn;
//    }
//
//    public CallingCostPageDTO findAllByAmountGreaterThanEqualAndCallcalculationAndOrganization(Double amount,
//                                                                                              String callcalculation,
//                                                                                              String organization,
//                                                                                              String searchText,
//                                                                                              Pageable pageable) {
//
//        CallingCostPageDTO toReturn = new CallingCostPageDTO();
//
//        String q = (searchText == null) ? "" : searchText;
//        Pageable stable = stablePageable(pageable);
//
//        if (stable.getPageNumber() == 0) {
//            Page<CallingCost> response =
//                    callingCostRepository.findAllByAmountGreaterThanEqualAndCallcalculationAndOrganization(
//                            amount, callcalculation, organization, q, stable
//                    );
//
//            List<CallingCostDTO> returnPart = response.getContent()
//                    .stream()
//                    .map(callingCostMapper::mapCallingCostToDTO)
//                    .collect(Collectors.toList());
//
//            toReturn.setData(returnPart);
//            toReturn.setTotalRecords(response.getTotalElements());
//            toReturn.setNumberOfPages(response.getTotalPages());
//        } else {
//            Slice<CallingCost> response =
//                    callingCostRepository.getAllByAmountGreaterThanEqualAndCallcalculationAndOrganization(
//                            amount, callcalculation, organization, q, stable
//                    );
//
//            List<CallingCostDTO> returnPart = response.getContent()
//                    .stream()
//                    .map(callingCostMapper::mapCallingCostToDTO)
//                    .collect(Collectors.toList());
//
//            toReturn.setData(returnPart);
//        }
//
//        return toReturn;
//    }
//
//    public CallingCostPageDTO findAllByAmountLessThanEqualAndCallcalculationAndOrganization(Double amount,
//                                                                                           String callcalculation,
//                                                                                           String organization,
//                                                                                           String searchText,
//                                                                                           Pageable pageable) {
//
//        CallingCostPageDTO toReturn = new CallingCostPageDTO();
//
//        String q = (searchText == null) ? "" : searchText;
//        Pageable stable = stablePageable(pageable);
//
//        if (stable.getPageNumber() == 0) {
//            Page<CallingCost> response =
//                    callingCostRepository.findAllByAmountLessThanEqualAndCallcalculationAndOrganization(
//                            amount, callcalculation, organization, q, stable
//                    );
//
//            List<CallingCostDTO> returnPart = response.getContent()
//                    .stream()
//                    .map(callingCostMapper::mapCallingCostToDTO)
//                    .collect(Collectors.toList());
//
//            toReturn.setData(returnPart);
//            toReturn.setTotalRecords(response.getTotalElements());
//            toReturn.setNumberOfPages(response.getTotalPages());
//        } else {
//            Slice<CallingCost> response =
//                    callingCostRepository.getAllByAmountLessThanEqualAndCallcalculationAndOrganization(
//                            amount, callcalculation, organization, q, stable
//                    );
//
//            List<CallingCostDTO> returnPart = response.getContent()
//                    .stream()
//                    .map(callingCostMapper::mapCallingCostToDTO)
//                    .collect(Collectors.toList());
//
//            toReturn.setData(returnPart);
//        }
//
//        return toReturn;
//    }
//
//    public CallingCostPageDTO findAllByCallcalculationAndOrganization(String callcalculation,
//                                                                     String organization,
//                                                                     String searchText,
//                                                                     Pageable pageable) {
//
//        CallingCostPageDTO toReturn = new CallingCostPageDTO();
//
//        String q = (searchText == null) ? "" : searchText;
//        Pageable stable = stablePageable(pageable);
//
//        if (stable.getPageNumber() == 0) {
//            Page<CallingCost> response =
//                    callingCostRepository.findAllByCallcalculationAndOrganization(callcalculation, organization, q, stable);
//
//            List<CallingCostDTO> returnPart = response.getContent()
//                    .stream()
//                    .map(callingCostMapper::mapCallingCostToDTO)
//                    .collect(Collectors.toList());
//
//            toReturn.setData(returnPart);
//            toReturn.setTotalRecords(response.getTotalElements());
//            toReturn.setNumberOfPages(response.getTotalPages());
//        } else {
//            Slice<CallingCost> response =
//                    callingCostRepository.getAllByCallcalculationAndOrganization(callcalculation, organization, q, stable);
//
//            List<CallingCostDTO> returnPart = response.getContent()
//                    .stream()
//                    .map(callingCostMapper::mapCallingCostToDTO)
//                    .collect(Collectors.toList());
//
//            toReturn.setData(returnPart);
//        }
//
//        return toReturn;
//    }
//
//    public void exportToExcel(HttpServletResponse response) throws IOException {
//        response.setContentType("application/vnd.ms-excel");
//        String headerKey = "Content-Disposition";
//        String headerValue = "attachment; filename=CallingCost_" + getCurrentDateTime() + ".xlsx";
//        response.setHeader(headerKey, headerValue);
//
//        List<CallingCost> callingCostList = callingCostRepository.findAll();
//
//        ExportCallingCostToXLSX exporter = new ExportCallingCostToXLSX(callingCostList);
//        exporter.export(response);
//    }
//
//    public void exportToExcelOnOrganization(Date date, Date dateEnd, String organization, HttpServletResponse response)
//            throws IOException {
//
//        response.setContentType("application/vnd.ms-excel");
//        String headerKey = "Content-Disposition";
//        String headerValue = "attachment; filename=CallingCost_" + getCurrentDateTime() + ".xlsx";
//        response.setHeader(headerKey, headerValue);
//
//        List<CallingCost> callingCostList =
//                callingCostRepository.getAllByDateGreaterThanEqualAndDateEndLessThanEqualAndOrganization(date, dateEnd, organization);
//
//        ExportCallingCostToXLSX exporter = new ExportCallingCostToXLSX(callingCostList);
//        exporter.export(response);
//    }
//
//    public void exportToPDF(HttpServletResponse response) throws IOException {
//        response.setContentType("application/pdf");
//        String headerKey = "Content-Disposition";
//        String headerValue = "attachment; filename=CallingCost_" + getCurrentDateTime() + ".pdf";
//        response.setHeader(headerKey, headerValue);
//
//        List<CallingCost> callingCostList = callingCostRepository.findAll();
//
//        ExportCallingCostToPDF exporter = new ExportCallingCostToPDF(callingCostList);
//        exporter.export(response);
//    }
//
//    public void exportToPDFOnOrganization(Date date, Date dateEnd, String organization, HttpServletResponse response)
//            throws IOException {
//
//        response.setContentType("application/pdf");
//        String headerKey = "Content-Disposition";
//        String headerValue = "attachment; filename=CallingCost_" + getCurrentDateTime() + ".pdf";
//        response.setHeader(headerKey, headerValue);
//
//        List<CallingCost> callingCostList =
//                callingCostRepository.getAllByDateGreaterThanEqualAndDateEndLessThanEqualAndOrganization(date, dateEnd, organization);
//
//        ExportCallingCostToPDF exporter = new ExportCallingCostToPDF(callingCostList);
//        exporter.export(response);
//    }
//}
