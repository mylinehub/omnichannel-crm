import { Injectable } from '@angular/core';
import { ConstantsService } from '../constants/constants.service';
import { ApiHttpService } from '../http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class OrganizationService {

 constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }

 public getOrganizationalData(organization: string) { 
    var endpoint: string = this.constService.API_ORG_DATA_ENDPOINT;
    endpoint = endpoint.replace("{{organization}}",organization);
    return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 
  
}
