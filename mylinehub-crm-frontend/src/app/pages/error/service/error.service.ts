import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class ErrorService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }

         public getAllErrorsByOrganization(organization: string) { 

            var endpoint: string = this.constService.API_GETALL_ERRORS_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


}






