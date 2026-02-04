import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class SipProviderService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }

        public getAllSipProvidersByOrganization(organization: string) { 

            var endpoint: string = this.constService.API_GETALL_SIP_PROVIDER_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        public getSipProviderByPhoneNumberAndOrganization(phoneNumber: string,organization: string) { 

          var endpoint: string = this.constService.API_GETALL_SIP_PROVIDER_BY_PHONECONTEXT_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{phoneNumber}}",phoneNumber);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 
        

        public updateSipProviderByOrganization(data:any,organization:string) { 

          var endpoint: string = this.constService.API_UPDATE_SIP_PROVIDER_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public enableSipProviderOnIdAndOrganization(data:any) { 

          var endpoint: string = this.constService.API_ENABLE_SIP_PROVIDER_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public disableSipProviderOnIdAndOrganization(data:any) { 

          var endpoint: string = this.constService.API_DIABLE_SIP_PROVIDER_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public createSipProviderByOrganization(data:any,organization:string) { 

          var endpoint: string = this.constService.API_CREATE_SIP_PROVIDER_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public deleteSipProviderByPhoneNumberAndOrganization(phoneNumber:string,organization: string) { 

          var endpoint: string = this.constService.API_DELETE_SIP_PROVIDER_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{phoneNumber}}", phoneNumber);
          return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 
}









