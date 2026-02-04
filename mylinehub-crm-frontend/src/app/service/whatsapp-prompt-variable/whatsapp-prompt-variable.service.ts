import { Injectable } from '@angular/core';
import { ConstantsService } from '../constants/constants.service';
import { ApiHttpService } from '../http/api-http.service';


@Injectable({
  providedIn: 'root'
})
export class WhatsappPromptVariableService {

 constructor( protected constService : ConstantsService,
                 protected httpService : ApiHttpService,) { }

  public getAllByOrganization(organization: string) { 
      var endpoint: string = this.constService.API_WHATSAPP_PROMPT_GET_ALL_VARIABLE_ENDPOINT;
      endpoint = endpoint.replace("{{organization}}",organization);
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

  public getAllByOrganizationAndWhatsAppPrompt(data:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_PROMPT_GET_ALL_BY_PROMPT_VARIABLE_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }  

  public getAllByOrganizationAndWhatsAppPromptAndActive(data:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_PROMPT_GET_ALL_BY_PROMPT_AND_ACTIVE_VARIABLE_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }  
   
  public create(data:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_CREATE_PROMPT_VARIABLE_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }  

  public update(data:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_UPDATE_PROMPT_VARIABLE_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    } 

  public delete(organization:any, id:any) { 
      var endpoint: string = this.constService.API_WHATSAPP_PROMPT_DELETE_VARIABLE_ENDPOINT;
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{id}}",id);
      return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 
    
}

