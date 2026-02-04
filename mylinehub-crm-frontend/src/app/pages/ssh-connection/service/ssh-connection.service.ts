import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class SshConnectionService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }

        public getAllSshConnectionsByOrganization(organization: string) { 

            var endpoint: string = this.constService.API_GETALL_SSH_CONNECTION_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        public refreshSshConnectionForOrganization(domain: string,organization: string) { 

          var endpoint: string = this.constService.API_REFRESH_SSH_CONNECTION_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{domain}}",domain);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 
        
        public updateSshConnectionByOrganization(data:any,organization:string) { 
          var endpoint: string = this.constService.API_UPDATE_SSH_CONNECTION_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public enableSshConnectionOnIdAndOrganization(data:any) { 

          var endpoint: string = this.constService.API_ENABLE_SSH_CONNECTION_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public disableSshConnectionOnIdAndOrganization(data:any) { 

          var endpoint: string = this.constService.API_DISABLE_SSH_CONNECTION_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public createSshConnectionByOrganization(data:any,organization:string) { 

          var endpoint: string = this.constService.API_CREATE_SSH_CONNECTION_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public deleteSshConnectionByDomainAndOrganization(domain:string,organization: string) { 

          var endpoint: string = this.constService.API_DELETE_SSH_CONNECTION_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{domain}}", domain);
          return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 
}










