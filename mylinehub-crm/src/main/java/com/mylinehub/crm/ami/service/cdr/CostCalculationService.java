//package com.mylinehub.crm.ami.service.cdr;
//
//import java.util.Map;
//
//import org.springframework.stereotype.Service;
//
//import com.mylinehub.crm.data.OrganizationData;
//import com.mylinehub.crm.data.dto.OrganizationWorkingDTO;
//import com.mylinehub.crm.entity.CallDetail;
//import com.mylinehub.crm.entity.Campaign;
//import com.mylinehub.crm.entity.Employee;
//import com.mylinehub.crm.enums.COST_CALCULATION;
//import com.mylinehub.crm.enums.Cdr_Event;
//import com.mylinehub.crm.report.Report;
//import com.mylinehub.crm.repository.ErrorRepository;
//import com.mylinehub.crm.utils.LoggerUtils;
//
//import lombok.AllArgsConstructor;
//
///**
// * @author Anand Goel
// * @version 1.0
// */
//@Service
//@AllArgsConstructor
//public class CostCalculationService {
//
//    private final ErrorRepository errorRepository;
//
//    public Double cdrToCost(Campaign campaign,String organization, Map<String, String> crd, String costCalc,boolean isCallOnMobile, boolean isIvr) {
//    	Double toReturn = 0d;
//        try {
//            if (costCalc != null) {
//                toReturn = this.cdrToCostWhenEmployeeIsNotNull(campaign,organization, crd, costCalc, isCallOnMobile, isIvr);
//            }
//        } catch (Exception e) {
//            toReturn = null;
//            e.printStackTrace();
//        }
//        return toReturn;
//    }
//
//    public Double cdrToCostWhenEmployeeIsNotNull(Campaign campaign,String organization, Map<String, String> crd,  String costCalc,boolean isCallOnMobile, boolean isIvr) {
//        
//    	Double toReturn = 0d;
//
//        try {
//
//            if (costCalc == null) {
//                Report.addError("NULL", "Does not have cost calculation", "EventListner", "cdrToCallingCost", organization, errorRepository);
//                return null;
//            }
//
//            if (costCalc.equals(COST_CALCULATION.UNLIMITED.name())) {
//                OrganizationData.workWithAllOrganizationData(organization, null, "increase-total-calls", null);
//                return null;
//            } else if (costCalc.contains(COST_CALCULATION.METERED.name())) {
//
//                String plan = costCalc.replace(COST_CALCULATION.METERED.name() + "_", "");
//                int billingSeconds = safeParseInt(crd.get(Cdr_Event.billableseconds.name().trim()), 0);
//
//                if (plan.contains("PM")) {
//                    plan = plan.replace("PM", "");
//                    if (plan.contains("I")) plan = plan.replace("I", ".");
//
//                    long rate = (long) Double.parseDouble(plan.trim());
//
//                    double billingMinutes = ((double) billingSeconds / 60);
//                    double amount = billingMinutes * rate;
//
//                    if (isCallOnMobile) {
//                    	toReturn = (Double) amount * 2;
//                    } else {
//                        toReturn = (Double) amount;
//                    }
//
//                    OrganizationWorkingDTO organizationWorkingDTO = new OrganizationWorkingDTO();
//                    organizationWorkingDTO.setAmount((long) amount);
//                    organizationWorkingDTO.setCallOnMobile(isCallOnMobile);
//                    OrganizationData.workWithAllOrganizationData(organization, null, "update-amount", organizationWorkingDTO);
//                    OrganizationData.workWithAllOrganizationData(organization, null, "increase-total-calls", null);
//
//                    return toReturn;
//
//                } else if (plan.contains("PS")) {
//                    plan = plan.replace("PS", "");
//                    if (plan.contains("I")) plan = plan.replace("I", ".");
//
//                    long rate = (long) Double.parseDouble(plan.trim());
//
//                    double amount = (double) billingSeconds * rate;
//                    LoggerUtils.log.debug("Amount : " + amount);
//
//                    if (isCallOnMobile) {
//                        toReturn = (Double) amount * 2;
//                    } else {
//                        toReturn = (Double) amount;
//                    }
//
//                    OrganizationWorkingDTO organizationWorkingDTO = new OrganizationWorkingDTO();
//                    organizationWorkingDTO.setAmount((long) amount);
//                    organizationWorkingDTO.setCallOnMobile(isCallOnMobile);
//                    OrganizationData.workWithAllOrganizationData(organization, null, "update-amount", organizationWorkingDTO);
//                    OrganizationData.workWithAllOrganizationData(organization, null, "increase-total-calls", null);
//
//                    return toReturn;
//                } else {
//                    Report.addError(plan, "Does not have metered amount plan", "EventListner", "cdrToCallingCost", organization, errorRepository);
//                }
//
//            } else {
//                Report.addError(costCalc, "Does not have cost calculation", "EventListner", "cdrToCallingCost", organization, errorRepository);
//            }
//
//        } catch (Exception e) {
//            toReturn = null;
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    private int safeParseInt(String v, int def) {
//        try {
//            if (v == null) return def;
//            String x = v.trim();
//            if (x.isEmpty()) return def;
//            return Integer.parseInt(x);
//        } catch (Exception e) {
//            return def;
//        }
//    }
//}
