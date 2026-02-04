import { Injectable } from '@angular/core';
import { ConstantsService } from '../../../service/constants/constants.service';
import { ApiHttpService } from '../../../service/http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class RegistrationService {

  constructor( protected constService : ConstantsService,
                   protected httpService : ApiHttpService,) { }

   public registerUser(data:any) { 
          var endpoint: string = this.constService.API_REGISTER_BUSINESS_USING_GSTIN;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }                 
}
