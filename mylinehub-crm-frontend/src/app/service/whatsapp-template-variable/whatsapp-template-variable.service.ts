import { Injectable } from '@angular/core';
import { ConstantsService } from '../constants/constants.service';
import { ApiHttpService } from '../http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class WhatsappTemplateVariableService {

  constructor( protected constService : ConstantsService,
                   protected httpService : ApiHttpService,) { }

  public findAllByWhatsAppNumberTemplateAndOrganization(templateId:string,organization: string) { 
      var endpoint: string = this.constService.API_WHATSAPP_GET_ALL_TEMPLATE_VARIABLE_ENDPOINT;
      endpoint = endpoint.replace("{{templateId}}",templateId);
      endpoint = endpoint.replace("{{organization}}",organization);
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

  public update(templateId:string,organization: string,data:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_UPDATE_TEMPLATE_VARIABLE_ENDPOINT;
      endpoint = endpoint.replace("{{templateId}}",templateId);
      endpoint = endpoint.replace("{{organization}}",organization);
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }  
}
