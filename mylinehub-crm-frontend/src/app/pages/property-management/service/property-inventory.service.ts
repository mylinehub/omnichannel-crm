import { Injectable } from '@angular/core';
import { ConstantsService } from '../../../service/constants/constants.service';
import { ApiHttpService } from '../../../service/http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class PropertyInventoryService {

  constructor( protected constService : ConstantsService,
                 protected httpService : ApiHttpService,) { }

   public getAllInventoryOnOrganization(organization: string,searchText:String,available:boolean,pageNumber:number,size:number) { 

            var endpoint: string = this.constService.API_GETALL_INVENTORY_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            endpoint = endpoint.replace("{{searchText}}",String(searchText));
            endpoint = endpoint.replace("{{available}}", String(available));
            endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
            endpoint = endpoint.replace("{{size}}",String(size));
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
   } 

   public getInventoryByCustomerOnOrganization(organization: string,customerId:String) { 

            var endpoint: string = this.constService.API_GET_INVENTORY_BY_CUSTOMER__ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            endpoint = endpoint.replace("{{customerId}}",String(customerId));
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
   } 

   public fetchExcelAfterListedDate(organization: string, fromListedDateIso: String,available:boolean,) {

    let endpoint: string = this.constService.API_DOWNLOAD_CUSTOMER_INVENTORY_ENDPOINT;
    endpoint = endpoint.replace("{{organization}}", organization);
    endpoint = endpoint.replace("{{fromListedDateIso}}", String(fromListedDateIso));
    endpoint = endpoint.replace("{{available}}", String(available));

    // IMPORTANT: Excel endpoint must be blob
    return this.httpService.getWithTokenAndBlobAsJson(this.constService.API_BASE_ENDPOINT + endpoint);
  }

}
