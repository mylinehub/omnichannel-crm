import { Injectable } from '@angular/core';
import { ConstantsService } from '../constants/constants.service';
import { ApiHttpService } from '../http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class WhatsappTemplateService {

  constructor( protected constService : ConstantsService,
                     protected httpService : ApiHttpService,) { }

  public getAllByOrganization(organization: string) { 
      var endpoint: string = this.constService.API_WHATSAPP_GET_ALL_TEMPLATE_ENDPOINT;
      endpoint = endpoint.replace("{{organization}}",organization);
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

  public create(data:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_CREATE_TEMPLATE_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }  

  public update(data:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_UPDATE_TEMPLATE_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }  


  public getAllByOrganizationAndWhatsAppPhoneNumber(data:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_GET_ALL_BY_PHONE_TEMPLATE_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }  

  public delete(organization:any, id:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_DELETE_TEMPLATE_ENDPOINT;
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{id}}",id);
      return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

}
