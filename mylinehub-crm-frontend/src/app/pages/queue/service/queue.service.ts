import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class QueueService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }

        public getAllQueuesByOrganization(organization: string) { 

            var endpoint: string = this.constService.API_GET_ALL_QUEUE_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        public getQueueByExtensionAndOrganization(extension: string,organization: string) { 

          var endpoint: string = this.constService.API_GET_ALL_QUEUE_BY_EXTENSION_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{extension}}",extension);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 
        
        
        public getAllQueuesOnPhoneContextAndOrganization(phoneContext: string,organization: string) { 

          var endpoint: string = this.constService.API_GET_ALL_QUEUE_BY_PHONECONTEXT_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{phoneContext}}",phoneContext);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        
        public getAllQueueOnIsEnabledAndOrganization(isEnabled: boolean,organization: string) { 

          var endpoint: string = this.constService.API_GET_ALL_QUEUE_BY_ISENABLED_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{isEnabled}}",isEnabled.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        public upload(data:FormData,organization:any) { 

          var endpoint: string = this.constService.API_UPLOAD_QUEUE_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public updateQueueByOrganization(data:any,organization:any) { 

          var endpoint: string = this.constService.API_UPDATE_QUEUE_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public enableQueueOnExtensionAndOrganization(data:any) { 

          var endpoint: string = this.constService.API_ENABLE_QUEUE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public disableQueueOnEmailAndOrganization(data:any) { 

          var endpoint: string = this.constService.API_DISABLE_QUEUE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public createQueueByOrganization(data:any,organization:any) { 

          var endpoint: string = this.constService.API_CREATE_QUEUE_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public deleteQueueByExtensionAndOrganization(extension:string,organization: string) { 

          var endpoint: string = this.constService.API_DELETE_QUEUE_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{extension}}", extension);
          return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

}








