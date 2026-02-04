import { Injectable } from '@angular/core';
import { ConstantsService } from '../constants/constants.service';
import { ApiHttpService } from '../http/api-http.service';


@Injectable({
  providedIn: 'root'
})
export class WhatsappReportService {

  constructor( protected constService : ConstantsService,
                 protected httpService : ApiHttpService,) { }
  
   public getAllByOrganization(organization: string) { 
    var endpoint: string = this.constService.API_WHATSAPP_ALL_REPORT_ENDPOINT;
    endpoint = endpoint.replace("{{organization}}",organization);
    return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public findAllByWhatsAppPhoneNumberAndOrganization(data:any) { 
    var endpoint: string = this.constService.API_WHATSAPP_All_BY_ONLY_PHONE_REPORT_ENDPOINT;
    return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  } 

  public findAllByDayUpdatedGreaterThanEqualAndWhatsAppPhoneNumberAndOrganization(data:any) { 
    var endpoint: string = this.constService.API_WHATSAPP_All_BY_DAY_GREATOR_REPORT_ENDPOINT;
    return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  } 

  public findAllByDayUpdatedGreaterThanEqualAndWhatsAppPhoneNumberAndTypeOfReportAndOrganization(data:any) { 
    var endpoint: string = this.constService.API_WHATSAPP_BY_DAY_GREATOR_AND_TYPE_REPORT_ENDPOINT;
    return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  } 

  public getReportCountForDashboard(data:any) { 
    var endpoint: string = this.constService.API_WHATSAPP_DASHBOARD_COUNT_ENDPOINT;
    return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  } 
  public getReportCountForDashboardForNumber(data:any) { 
    var endpoint: string = this.constService.API_WHATSAPP_DASHBOARD_COUNT_BY_NUMBER_ENDPOINT;
    return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  } 
  public getReportCountForDashboardForNumberByTime(data:any) { 
    var endpoint: string = this.constService.API_WHATSAPP_DASHBOARD_COUNT_BY_NUMBER_AND_TIME_ENDPOINT;
    return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  } 

}

