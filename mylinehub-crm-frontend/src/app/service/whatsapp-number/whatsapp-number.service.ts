import { Injectable } from '@angular/core';
import { ConstantsService } from '../constants/constants.service';
import { ApiHttpService } from '../http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class WhatsappNumberService {

  constructor( protected constService : ConstantsService,
                 protected httpService : ApiHttpService,) { }

  public embeddedSignupComplete(organization: string, data: any) {
    let endpoint: string = this.constService.API_WHATSAPP_EMBEDDED_SIGNUP_COMPLETE_ENDPOINT;
    endpoint = endpoint.replace("{{organization}}", encodeURIComponent(organization));

    return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint, data);
  }


  public getAllByOrganization(organization: string) { 
      var endpoint: string = this.constService.API_WHATSAPP_GET_ALL_PHONE_ENDPOINT;
      endpoint = endpoint.replace("{{organization}}",organization);
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 
  
  public getAllByOrganizationAndAdmin(organization: string, adminEmployeeId:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_GET_ALL_BY_ADMIN_PHONE_ENDPOINT;
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{adminEmployeeId}}",adminEmployeeId);
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    }

  public findAllByEmployeeInExtensionAccessListOrAdmin(organization: string, employeeExtension:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_GET_ALL_BY_EMPLOYEE_AND_ORG_PHONE_ENDPOINT;
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{employeeExtension}}",employeeExtension);
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    }

  public updateAdminEmployeeForWhatsAppNumberByOrganization(data:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_UPDATE_ADMIN_FOR_PHONE_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }  

  public updateEmployeeAccessListByOrganization(data:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_UPDATE_EMPLOYEE_ACCESS_REQUEST_PHONE_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    } 

  public create(data:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_CREATE_PHONE_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }  

  public update(data:any,oldPhone:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_UPDATE_PHONE_ENDPOINT;
      endpoint = endpoint.replace("{{oldPhone}}",oldPhone);
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }  
   
  public getAllByOrganizationAndWhatsAppProject(data:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_GET_ALL_BY_PROJECT_PHONE_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }  

  public getAllByOrganizationAndWhatsAppProjectAndActive(data:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_GET_ALL_BY_PROJECT__AND_ACTIVE_PHONE_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }  

  public delete(organization:any, id:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_DELETE_PHONE_ENDPOINT;
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{id}}",id);
      return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 
}

