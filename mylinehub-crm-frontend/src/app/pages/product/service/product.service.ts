import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class ProductService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }

        public getAllproductsByOrganization(organization: string) { 

            var endpoint: string = this.constService.API_GETALL_PRODUCT_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        public getProductByIdAndOrganization(id: number,organization: string) { 

          var endpoint: string = this.constService.API_GETALL_PRODUCT_BY_ID_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{id}}",id.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 
        
        
        public getAllproductsOnProductTypeAndOrganization(productType: string,organization: string) { 

          var endpoint: string = this.constService.API_GETALL_PRODUCT_BY_PRODUCT_TYPE_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{productType}}",productType);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public getAllProductsByIdIn(data: any) { 
          var endpoint: string = this.constService.API_GETALL_PRODUCT_BY_IDS_IN_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        public getProductImage(id:number, organization: string) { 

          var endpoint: string = this.constService.API_GET_IMAGE_PRODUCT_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{id}}",String(id));
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

        public upload(data:FormData,organization:any) { 

          var endpoint: string = this.constService.API_UPLOAD_PRODUCT_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public updateProductByOrganization(data:any,organization:any) { 

          var endpoint: string = this.constService.API_UPDATE_PRODUCT_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public createProductByOrganization(data:any,organization:any) { 

          var endpoint: string = this.constService.API_CREATE_PRODUCT_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public deleteProductByIdAndOrganization(id:number,organization: string) { 

          var endpoint: string = this.constService.API_DELETE_PRODUCT_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{id}}", id.toString());
          return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

      public uploadPicByIdAndOrganization(data:FormData,id:number,organization: string) { 

        var endpoint: string = this.constService.API_UPLOAD_PRODUCT_PIC_ENDPOINT;
        endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{id}}", id.toString());
        return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
      } 

}







