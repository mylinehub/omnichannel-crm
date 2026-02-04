package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.mylinehub.crm.entity.CallDetail;
import com.mylinehub.crm.entity.dto.CallDetailDTO;


@Mapper(componentModel = "spring")
public interface CallDetailMapper {
	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "campaignRunDetailsId", source = "campaignRunDetailsId")
	@Mapping(target = "campaignRunCallLogId", source = "campaignRunCallLogId")
	@Mapping(target = "callerid", source = "callerid")
	@Mapping(target = "employeeName", source = "employeeName")
	@Mapping(target = "customerid", source = "customerid")
	@Mapping(target = "customerName", source = "customerName")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "phoneContext", source = "phoneContext")
	@Mapping(target = "calldurationseconds", source = "calldurationseconds")
	@Mapping(target = "isactive", source = "isactive")
	@Mapping(target = "timezone", source = "timezone")
	@Mapping(target = "startdate", source = "startdate")
	@Mapping(target = "enddate", source = "enddate")
	@Mapping(target = "starttime", source = "starttime")
	@Mapping(target = "endtime", source = "endtime")
	@Mapping(target = "callonmobile", source = "callonmobile")
	@Mapping(target = "isconference", source = "isconference")
	@Mapping(target = "isconnected", source = "isconnected")
	@Mapping(target = "maximumchannels", source = "maximumchannels")
	@Mapping(target = "country", source = "country")
	@Mapping(target = "callType", source = "callType")
	@Mapping(target = "ivr", source = "ivr")
	@Mapping(target = "queue", source = "queue")
	@Mapping(target = "pridictive", source = "pridictive")
	@Mapping(target = "progressive", source = "progressive")
	@Mapping(target = "callSessionId", source = "callSessionId")
	@Mapping(target = "campaignID", source = "campaignID")
	@Mapping(target = "linkId", source = "linkId")
	@Mapping(target = "callCost", source = "callCost")
	@Mapping(target = "callCostMode", source = "callCostMode")
    CallDetailDTO mapCallDetailToDTO(CallDetail calldetail);
	
	
	@Mapping(target = "campaignRunDetailsId", source = "campaignRunDetailsId")
	@Mapping(target = "campaignRunCallLogId", source = "campaignRunCallLogId")
	@Mapping(target = "callerid", source = "callerid")
	@Mapping(target = "employeeName", source = "employeeName")
	@Mapping(target = "customerid", source = "customerid")
	@Mapping(target = "customerName", source = "customerName")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "phoneContext", source = "phoneContext")
	@Mapping(target = "calldurationseconds", source = "calldurationseconds")
	@Mapping(target = "isactive", source = "isactive")
	@Mapping(target = "timezone", source = "timezone")
	@Mapping(target = "startdate", source = "startdate")
	@Mapping(target = "enddate", source = "enddate")
	@Mapping(target = "starttime", source = "starttime")
	@Mapping(target = "endtime", source = "endtime")
	@Mapping(target = "callonmobile", source = "callonmobile")
	@Mapping(target = "isconference", source = "isconference")
	@Mapping(target = "isconnected", source = "isconnected")
	@Mapping(target = "maximumchannels", source = "maximumchannels")
	@Mapping(target = "country", source = "country")
	@Mapping(target = "callType", source = "callType")
	@Mapping(target = "ivr", source = "ivr")
	@Mapping(target = "queue", source = "queue")
	@Mapping(target = "pridictive", source = "pridictive")
	@Mapping(target = "progressive", source = "progressive")
	@Mapping(target = "callSessionId", source = "callSessionId")
	@Mapping(target = "campaignID", source = "campaignID")
	@Mapping(target = "linkId", source = "linkId")
	@Mapping(target = "callCost", source = "callCost")
	@Mapping(target = "callCostMode", source = "callCostMode")
    CallDetail mapDTOToCallDetail(CallDetailDTO calldetailDTO);
	
	
//	@Named("mapStartDate") 
//    default Date mapStartDate(Date startDate){
//		
//		try
//		{
//			System.out.println("mapStartDate");
//			if(startDate != null)
//			{
//				System.out.println("mapStartDate is not null");
//				try
//				{
//					 SimpleDateFormat formatter = new SimpleDateFormat(
//						      "dd/MM/yyyy");
//					 Date toReturn = formatter.parse(formatter.format(startDate));
//					 System.out.println("Date : " + startDate);
//					 System.out.println("Date to Return : " + toReturn);
//				     return toReturn;
//				}
//				catch(Exception e)
//				{
//					return null;
//				}
//			}
//			else
//			{
//				System.out.println("satartdate is null");
//				return null;
//			}
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//			return null;
//		}
//    }
	
}
