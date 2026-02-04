import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class IvrService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }

          public getAllIvrsByOrganization(organization: string) { 

            var endpoint: string = this.constService.API_ALL_IVR_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        public getIvrByExtensionAndOrganization(extension: string,organization: string) { 

          var endpoint: string = this.constService.API_ALL_IVR__BY_EXTENSION_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{extension}}",extension);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 
        
        
        public getAllIvrsOnPhoneContextAndOrganization(phoneContext: string,organization: string) { 

          var endpoint: string = this.constService.API_ALL_IVR__BY_PHONECONTEXT_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{phoneContext}}",phoneContext);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        
        public getAllIvrOnIsEnabledAndOrganization(isEnabled: boolean,organization: string) { 

          var endpoint: string = this.constService.API_ALL_IVR_BY_ISENABLEDENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{isEnabled}}",isEnabled.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        public createIvrByOrganization(data:any,organization:any) { 

          var endpoint: string = this.constService.API_CREATE_IVR_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public disableIvrOnEmailAndOrganization(data:any) { 

          var endpoint: string = this.constService.API_DIABLE_IVR_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public enableIvrOnExtensionAndOrganization(data:any) { 

          var endpoint: string = this.constService.API_ENABLE_IVR_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public updateIvrByOrganization(data:any,organization:any) { 

          var endpoint: string = this.constService.API_UPDATE_IVR_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public upload(data:any,organization:any) { 

          var endpoint: string = this.constService.API_UPLOAD_IVR_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public deleteIvrByExtensionAndOrganization(extension:string,organization: string) { 

          var endpoint: string = this.constService.API_DELETE_IVR_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{extension}}", extension);
          return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 


}







