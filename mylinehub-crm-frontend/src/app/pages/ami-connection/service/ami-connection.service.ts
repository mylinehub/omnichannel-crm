import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class AmiConnectionService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }

               
      public refreshAmiConnectionForOrganization(domain:string,organization: string) { 

        var endpoint: string = this.constService.API_REFRESH_AMI_ENDPOINT;

        endpoint = endpoint.replace("{{domain}}",domain);
        endpoint = endpoint.replace("{{organization}}",organization);
        return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 
    
      public getAmiConnectionByAmiuserAndOrganization(amiuser:string,organization: string) { 

        var endpoint: string = this.constService.API_AMI_BY_AMIUSER_ENDPOINT;

        endpoint = endpoint.replace("{{organization}}",organization);
        endpoint = endpoint.replace("{{amiuser}}",amiuser);
        return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 
        
        public getAllAmiConnectionsOnPhoneContextAndOrganization(phonecontext:string,organization: string) { 

          var endpoint: string = this.constService.API_GETALL_AMI_BY_PHONECONTEXT_ENDPOINT;

          endpoint = endpoint.replace("{{phonecontext}}",phonecontext);
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

      public getAllAmiConnectionsByOrganization(organization: string) { 

        var endpoint: string = this.constService.API_GETALL_AMI_ENDPOINT;

        endpoint = endpoint.replace("{{organization}}",organization);
        return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

      public getAllAmiConnectionOnIsEnabledAndOrganization(isactive:boolean,organization: string) { 

        var endpoint: string = this.constService.API_GETALL_AMI_BY_ISENABLED_ENDPOINT;

        endpoint = endpoint.replace("{{isactive}}",isactive.toString());
        endpoint = endpoint.replace("{{organization}}",organization);
        return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

    public upload(data:FormData,organization:string) { 

            var endpoint: string = this.constService.API_UPLOAD_AMI_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }
    public updateAmiConnectionByOrganization(data:any,organization:any) { 

      var endpoint: string = this.constService.API_UPDATE_AMI_ENDPOINT;
      endpoint = endpoint.replace("{{organization}}",organization);
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }
    public enableAmiConnectionOnAmiUserAndOrganization(data:any) { 

      var endpoint: string = this.constService.API_ENABLE_AMI_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }
    public disableAmiConnectionOnAmiUserAndOrganization(data:any) { 

      var endpoint: string = this.constService.API_DISABLE_AMI_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }
    public createAmiConnectionByOrganization(data:any,organization:any) { 

      var endpoint: string = this.constService.API_CREATE_AMI_ENDPOINT;
      endpoint = endpoint.replace("{{organization}}",organization);
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }
    public connectAmiConnectionOnAmiUserAndOrganization(data:any) { 

      var endpoint: string = this.constService.API_CONNECT_AMI_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public deleteAmiConnectionByAmiUserAndOrganization(amiuser:string,organization: string) { 

      var endpoint: string = this.constService.API_DELETE_AMI_ENDPOINT;

      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{amiuser}}", amiuser);
      return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

}
