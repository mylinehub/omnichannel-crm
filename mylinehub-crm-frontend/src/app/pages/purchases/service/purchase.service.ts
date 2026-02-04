import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class PurchaseService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }

        public getAllPurchasesOnOrganization(organization: string) { 

            var endpoint: string = this.constService.API_GETALL_PURCHASE_BY_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        public getPurchaseByPurchaseIDAndOrganization(purchaseID: string,organization: string) { 

          var endpoint: string = this.constService.API_GET_PURCHASE_BY_PURCHASEID_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{purchaseID}}",purchaseID);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllByPurchaseDateLessThanEqualAndOrganization(date: string,organization: string) { 

          var endpoint: string = this.constService.API_GETALL_PURCHASE_BY_PURCHASEDATE_LESS_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{date}}",date);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllByPurchaseDateGreaterThanEqualAndOrganization(date: string,organization: string) { 

          var endpoint: string = this.constService.API_GETALL_PURCHASE_BY_PURCHASEDATE_GREATOR_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{date}}",date);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllByCustomerAndOrganization(customerID: number,organization: string) { 

          var endpoint: string = this.constService.API_GETALL_PURCHASE_BY_CUSTOMER_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{customerID}}",customerID.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public updatePurchaseByOrganization(data:any,organization:any) { 

          var endpoint: string = this.constService.API_UPDATE_PURCHASE_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public createPurchaseByOrganization(data:any,organization:any) { 

          var endpoint: string = this.constService.API_CREATE_PURCHASE_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public deletePurchaseByIdAndOrganization(purchaseID:string,organization: string) { 

          var endpoint: string = this.constService.API_DELETE_PURCHASE_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{purchaseID}}", purchaseID);
          return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

}







