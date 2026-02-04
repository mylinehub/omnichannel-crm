import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class SupplierService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }

      public getAllsuppliersByOrganization(organization: string) { 

            var endpoint: string = this.constService.API_GETALL_SUPPLIER_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        public getSupplierByIdAndOrganization(id: number,organization: string) { 

          var endpoint: string = this.constService.API_GET_SUPPLIER_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{id}}",id.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public getAllsuppliersOnTransportcapacityAndOrganization(transportcapacity: string,organization: string) { 

          var endpoint: string = this.constService.API_GETALL_SUPPLIER_BY_TRANSPORTCAPACITY_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{transportcapacity}}",transportcapacity);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 
        
        public getAllSupplierOnTypeAndOrganization(supplierType: string,organization: string) { 

          var endpoint: string = this.constService.API_GETALL_SUPPLIER_BY_SUPPLIER_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{supplierType}}",supplierType);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        public upload(data:FormData,organization:any) { 

          var endpoint: string = this.constService.API_UPLOAD_SUPPLIER_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public updateSupplierByOrganization(data:any,organization:any) { 

          var endpoint: string = this.constService.API_UPDATE_SUPPLIER_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public createSupplierByOrganization(data:any,organization:any) { 

          var endpoint: string = this.constService.API_CREATE_SUPPLIER_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public deleteSupplierByIdAndOrganization(id:number,organization: string) { 

          var endpoint: string = this.constService.API_DELETE_SUPPLIER_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{id}}", id.toString());
          return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 
}











