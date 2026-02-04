//package com.mylinehub.crm.mapper;
//
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//
//import com.mylinehub.crm.entity.CallingCost;
//import com.mylinehub.crm.entity.dto.CallingCostDTO;
//
//
//@Mapper(componentModel = "spring")
//public interface CallingCostMapper {
//	
//	@Mapping(target = "callID", source = "callDetail.id")
//	@Mapping(target = "extension", source = "extension")
//	@Mapping(target = "amount", source = "amount")
//	@Mapping(target = "callcalculation", source = "callcalculation")
//	@Mapping(target = "remarks", source = "remarks")
//	@Mapping(target = "date", source = "date")
//    CallingCostDTO mapCallingCostToDTO(CallingCost callingcost);
//	
//	@Mapping(target = "callDetail.id", source = "callID")
//	@Mapping(target = "extension", source = "extension")
//	@Mapping(target = "amount", source = "amount")
//	@Mapping(target = "callcalculation", source = "callcalculation")
//	@Mapping(target = "remarks", source = "remarks")
//	@Mapping(target = "date", source = "date")
//    CallingCost mapDTOToCallingCost(CallingCostDTO callingcostDTO);
//}