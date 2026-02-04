import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class DepartmentService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }

       public getAllDepartmentsByOrganization(organization: string) { 

            var endpoint: string = this.constService.API_GETALL_DEPARTMENT__ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        public getDepartmentByIdAndOrganization(id:number,organization: string) { 

          var endpoint: string = this.constService.API_GET_DEPARTMENT_BY_ID_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{id}}",id.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public upload(data:FormData,organization:any) { 

          var endpoint: string = this.constService.API_UPLOAD_DEPARTMENT__ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public updateDepartmentByOrganization(data:any,organization:any) { 

          var endpoint: string = this.constService.API_UPDATE_DEPARTMENT__ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public createDepartmentByOrganization(data:any,organization:any) { 

          var endpoint: string = this.constService.API_CREATE_DEPARTMENT__ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public deleteDepartmentByIdAndOrganization(id:string,organization: string) { 

          var endpoint: string = this.constService.API_DELETE_DEPARTMENT__ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{id}}", id);
          return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

}




