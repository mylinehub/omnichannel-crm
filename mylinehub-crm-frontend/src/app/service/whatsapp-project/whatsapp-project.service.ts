
import { Injectable } from '@angular/core';
import { ConstantsService } from '../constants/constants.service';
import { ApiHttpService } from '../http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class WhatsappProjectService {

 constructor( protected constService : ConstantsService,
                protected httpService : ApiHttpService,) { }

  public getAllProjectByOrganization(organization: string) { 
    var endpoint: string = this.constService.API_WHATSAPP_GET_ALL_PROJECT_ENDPOINT;
    endpoint = endpoint.replace("{{organization}}",organization);
    return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public createProject(data:any) { 
    var endpoint: string = this.constService.API_WHATSAPP_CREATE_PROJECT_ENDPOINT;
    return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  } 

  public updateProject(data:any) { 
    var endpoint: string = this.constService.API_WHATSAPP_UPDATE_PROJECT_ENDPOINT;
    return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  } 

  public deleteProject(organization:any, id:any) { 
    var endpoint: string = this.constService.API_WHATSAPP_DELETE_PROJECT_ENDPOINT;
    endpoint = endpoint.replace("{{organization}}",organization);
    endpoint = endpoint.replace("{{id}}",id);
    return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 
              
  public getAllOpenAIAccountByOrganization(organization: string) { 
    var endpoint: string = this.constService.API_WHATSAPP_GET_ALL_OPENAPI_ENDPOINT;
    endpoint = endpoint.replace("{{organization}}",organization);
    return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public createOpenAIAccount(data:any) { 
    var endpoint: string = this.constService.API_WHATSAPP_CREATE_OPENAPI_ENDPOINT;
    return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  } 

  public updateOpenAIAccount(data:any) { 
    var endpoint: string = this.constService.API_WHATSAPP_UPDATE_OPENAPI_ENDPOINT;
    return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  } 

  public deleteOpenAIAccount(organization:any, id:any) { 
    var endpoint: string = this.constService.API_WHATSAPP_DELETE_OPENAPI_ENDPOINT;
    endpoint = endpoint.replace("{{organization}}",organization);
    endpoint = endpoint.replace("{{id}}",id);
    return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

}
