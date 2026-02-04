import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class CallDetailService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }


  public refreshConnectionsOnOrganization(organization: string, phoneNumber: string) {
       var endpoint: string = this.constService.API_REFRESH_CONNECTION_ENDPOINT;     
       endpoint = endpoint.replace("{{organization}}",organization);
       endpoint = endpoint.replace("{{phoneNumber}}",phoneNumber);
       return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
   } 

   public getAllCallDetailsOnOrganization(organization: string,searchText:String,pageNumber:number,size:number) { 

       var endpoint: string = this.constService.API_CALL_DETAIL_ENDPOINT;     
       endpoint = endpoint.replace("{{organization}}",organization);
       endpoint = endpoint.replace("{{searchText}}",String(searchText));
       endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
       endpoint = endpoint.replace("{{size}}",String(size));
       return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
   } 


    public findAllByPhoneContextAndOrganization(phoneContext:string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CONTEXT_ENDPOINT;

      endpoint = endpoint.replace("{{phoneContext}}",phoneContext);
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    }

    public findAllByTimezoneAndOrganization(timezone:string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_TIMEZONE_ENDPOINT;

      endpoint = endpoint.replace("{{timezone}}",timezone);
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByIsconferenceAndOrganization(isconference:boolean,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_ISCONFERENCE_ENDPOINT;

      endpoint = endpoint.replace("{{isconference}}",isconference.toString());
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 


    public findAllByIsivrAndOrganization(ivr:boolean,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_ISIVR_ENDPOINT;

      endpoint = endpoint.replace("{{ivr}}",ivr.toString());
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 


    public findAllByIspridictiveAndOrganization(pridictive:boolean,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_ISPREDICTIVE_ENDPOINT;

      endpoint = endpoint.replace("{{pridictive}}",pridictive.toString());
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 


    public findAllByIsprogressiveAndOrganization(progressive:boolean,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_ISPROGRESSIVE_ENDPOINT;

      endpoint = endpoint.replace("{{progressive}}",progressive.toString());
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 


    public findAllByIsqueueAndOrganization(queue:boolean,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_ISQUEUE_ENDPOINT;

      endpoint = endpoint.replace("{{queue}}",queue.toString());
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCustomeridAndOrganization(customerid:string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CUSTOMER_ENDPOINT;

      endpoint = endpoint.replace("{{customerid}}",customerid);
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCountryAndOrganization(country:string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_COUNTRY_ENDPOINT;

      endpoint = endpoint.replace("{{country}}",country);
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllForEmployeeHistory(dateRange:string,callerid:string,organization: string,searchText:String,pageNumber:number,size:number) { 
      var endpoint: string = this.constService.API_CALL_DETAIL_FOR_EMPLOYEE_HISTORY_ENDPOINT;
      endpoint = endpoint.replace("{{dateRange}}",dateRange);
      endpoint = endpoint.replace("{{callerid}}",callerid);
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllInMemoryDataByOrganization(organization: string) { 
      var endpoint: string = this.constService.API_CALL_DETAIL_IN_MEMORY_ALL_ENDPOINT;
      endpoint = endpoint.replace("{{organization}}",organization);
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllInMemoryDataByOrganizationAndExtension(organization: string,extension:string) { 
      var endpoint: string = this.constService.API_CALL_DETAIL_IN_MEMORY_ALL_EXTENSION_ENDPOINT;
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{extension}}",extension);
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCallonmobileAndOrganization(callonmobile:boolean,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CALL_ON_MOBILE_ENDPOINT;

      endpoint = endpoint.replace("{{callonmobile}}",callonmobile.toString());
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCalleridAndOrganization(callerid:string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CALLERID_ENDPOINT;

      endpoint = endpoint.replace("{{callerid}}",callerid);
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByStartdateGreaterThanEqualAndOrganization(startDate:string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_START_DATE_GREATOR_ENDPOINT;

      endpoint = endpoint.replace("{{startDate}}",startDate);
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCalldurationsecondsLessThanEqualAndOrganization(calldurationseconds:number,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_CALL_DURATION_LESS_ENDPOINT;

      endpoint = endpoint.replace("{{calldurationseconds}}",calldurationseconds.toString());
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCalldurationsecondsGreaterThanEqualAndOrganization(calldurationseconds:number,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_CALL_DURATION_GREATOR_ENDPOINT;

      endpoint = endpoint.replace("{{calldurationseconds}}",calldurationseconds.toString());
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    }


    public findAllByCalldurationsecondsGreaterThanEqualAndCalleridAndOrganization(calldurationseconds:number,callerid: string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CALL_DURATION_GREATOR_CALLERID_ENDPOINT;

      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{calldurationseconds}}",calldurationseconds.toString());
      endpoint = endpoint.replace("{{callerid}}",callerid);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCalldurationsecondsGreaterThanEqualAndCustomeridAndOrganization(calldurationseconds:number,customerid: string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CALL_DURATION_GREATOR_CUSTOMER_ENDPOINT;

      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{calldurationseconds}}",calldurationseconds.toString());
      endpoint = endpoint.replace("{{customerid}}",customerid);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCalldurationsecondsGreaterThanEqualAndIsconferenceAndOrganization(calldurationseconds:number,isconference: boolean,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CALL_DURATION_GREATOR_CONFERENCE_ENDPOINT;

      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{calldurationseconds}}",calldurationseconds.toString());
      endpoint = endpoint.replace("{{isconference}}",isconference.toString());
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCalldurationsecondsGreaterThanEqualAndPhoneContextAndOrganization(calldurationseconds:number,phoneContext: string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CALL_DURATION_GREATOR_PHONECONTEXT_ENDPOINT;

      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{calldurationseconds}}",calldurationseconds.toString());
      endpoint = endpoint.replace("{{phoneContext}}",phoneContext);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCalldurationsecondsGreaterThanEqualAndTimezoneAndOrganization(calldurationseconds:number,timezone: string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CALL_DURATION_GREATOR_TIMEZONE_ENDPOINT;

      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{calldurationseconds}}",calldurationseconds.toString());
      endpoint = endpoint.replace("{{timezone}}",timezone);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCalldurationsecondsLessThanEqualAndCalleridAndOrganization(calldurationseconds:number,callerid: string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CALL_DURATION_LESS_CALLERID_ENDPOINT;

      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{calldurationseconds}}",calldurationseconds.toString());
      endpoint = endpoint.replace("{{callerid}}",callerid);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCalldurationsecondsLessThanEqualAndTimezoneAndOrganization(calldurationseconds:number,timeZone: string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CALL_DURATION_LESS_TIMEZONE_ENDPOINT;

      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{calldurationseconds}}",calldurationseconds.toString());
      endpoint = endpoint.replace("{{timeZone}}",timeZone);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCalldurationsecondsLessThanEqualAndPhoneContextAndOrganization(calldurationseconds:number,phoneContext: string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CALL_DURATION_LESS_PHONECONTEXT_ENDPOINT;

      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{calldurationseconds}}",calldurationseconds.toString());
      endpoint = endpoint.replace("{{phoneContext}}",phoneContext);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCalldurationsecondsLessThanEqualAndIsconferenceAndOrganization(calldurationseconds:number,isconference: boolean,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CALL_DURATION_LESS_ISCONFERENCE_ENDPOINT;

      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{calldurationseconds}}",calldurationseconds.toString());
      endpoint = endpoint.replace("{{isconference}}",isconference.toString());
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCallonmobileAndIsconferenceAndOrganization(callonmobile:number,isconference: boolean,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CALLONMOBILE_ISCONFERENCE_ENDPOINT;

      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{callonmobile}}",callonmobile.toString());
      endpoint = endpoint.replace("{{isconference}}",isconference.toString());
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByCalldurationsecondsLessThanEqualAndCustomeridAndOrganization(calldurationseconds:number,customerid: string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALL_DETAIL_BY_CALL_DURATION_LESS_CUSTOMER_ENDPOINT;

      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{calldurationseconds}}",calldurationseconds.toString());
      endpoint = endpoint.replace("{{customerid}}",customerid);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public addCustomerIfRequiredAndConvert(data:any) { 
      var endpoint: string = this.constService.API_CALL_DETAIL_ADD_AND_CHANGE_CUSTOMER_CONVERTED_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public addCustomerIfRequiredAndUpdateRemark(data:any) { 
      var endpoint: string = this.constService.API_CALL_DETAIL_ADD_AND_CHANGE_CUSTOMER_DESCRIPTION_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public getCallCountForDashboard(data:any) { 
      var endpoint: string = this.constService.API_CALL_DETAIL_COUNT_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    } 

    public getCallCountForDashboardForEmployee(data:any) { 
      var endpoint: string = this.constService.API_CALL_DETAIL_COUNT_BY_EMPLOYEE_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    } 

    public getCallCountForDashboardForEmployeeByTime(data:any) { 
      var endpoint: string = this.constService.API_CALL_DETAIL_COUNT_BY_EMPLOYEE_AND_TIME_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    } 
              
}

