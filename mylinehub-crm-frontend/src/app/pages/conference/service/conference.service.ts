import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class ConferenceService {

  constructor( protected constService : ConstantsService,

               protected httpService : ApiHttpService,) { }

         public getAllConferenceByOrganization(organization: string) { 

            var endpoint: string = this.constService.API_ALL_CONFERENCE_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        public getConferenceByExtensionAndOrganization(extension: string,organization: string) { 

          var endpoint: string = this.constService.API_ALL_CONFERENCE_BY_EXTENSION_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{extension}}",extension);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public getAllConferenceOnPhoneContextAndOrganization(phoneContext: string,organization: string) { 

          var endpoint: string = this.constService.API_ALL_CONFERENCE_BY_PHONECONTEXT_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{phoneContext}}",phoneContext);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public getAllConferenceOnIsEnabledAndOrganization(isEnabled: boolean,organization: string) { 

          var endpoint: string = this.constService.API_ALL_CONFERENCE_BY_ISENABLED_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{isEnabled}}",isEnabled.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public createConferenceByOrganization(data:any,organization:any) { 

          var endpoint: string = this.constService.API_CREATE_CONFERENCE_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public disableConferenceOnExtensionAndOrganization(data:any) { 

          var endpoint: string = this.constService.API_DISABLE_CONFERENCE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public enableConferenceOnExtensionAndOrganization(data:any) { 

          var endpoint: string = this.constService.API_ENABLE_CONFERENCE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public updateConferenceByOrganization(data:any,organization:any) { 

          var endpoint: string = this.constService.API_UPDATE_CONFERENCE_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public upload(data:FormData,organization:string) { 

          var endpoint: string = this.constService.API_UPLOAD_CONFERENCE_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public deleteConferenceByExtensionAndOrganization(extension:string,organization: string) { 

          var endpoint: string = this.constService.API_DELETE_CONFERENCE_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{extension}}", extension);
          return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

}



