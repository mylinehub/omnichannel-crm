import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class CallingCostService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }


 public getAllCallingCostOnOrganization(organization: string,searchText:String,pageNumber:number,size:number) { 

       var endpoint: string = this.constService.API_CALLING_COST_ENDPOINT;     
       endpoint = endpoint.replace("{{organization}}",organization);
       endpoint = endpoint.replace("{{searchText}}",String(searchText));
       endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
       endpoint = endpoint.replace("{{size}}",String(size));
       return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
   } 

   public getAllCallingCostOnExtensionAndOrganization(extension:string,organization: string,searchText:String,pageNumber:number,size:number) { 

    var endpoint: string = this.constService.API_CALLING_COST_BY_EXTENSION_ENDPOINT;  
    endpoint = endpoint.replace("{{extension}}",extension);   
    endpoint = endpoint.replace("{{organization}}",organization);
    endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
    return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
} 


    public findAllByCallcalculationAndOrganization(callcalculation:string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALLING_COST_BY_CALL_CALCULATION_ENDPOINT;

      endpoint = endpoint.replace("{{callcalculation}}",callcalculation);
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    }

    public findAllByAmountLessThanEqualAndOrganization(amount:number,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALLING_COST_BY_AMOUNT_LESS_THAN_ENDPOINT;

      endpoint = endpoint.replace("{{amount}}",amount.toString());
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByAmountGreaterThanEqualAndOrganization(amount:number,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALLING_COST_BY_AMOUNT_MORE_THAN_ENDPOINT;

      endpoint = endpoint.replace("{{amount}}",amount.toString());
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public getAllCallingCostOnOrganizationViaEmail(email:string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALLING_COST__SEND_VIA_EMAIL_ENDPOINT;

      endpoint = endpoint.replace("{{email}}",email);
      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByAmountLessThanEqualAndCallcalculationAndOrganization(amount:number,callcalculation: string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALLING_COST_BY_AMOUNT_LESS_AND_COST_CALCULATION_ENDPOINT;

      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{amount}}",amount.toString());
      endpoint = endpoint.replace("{{callcalculation}}",callcalculation);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

    public findAllByAmountGreaterThanEqualAndCallcalculationAndOrganization(amount:number,callcalculation: string,organization: string,searchText:String,pageNumber:number,size:number) { 

      var endpoint: string = this.constService.API_CALLING_COST_BY_AMOUNT_GREATOR_AND_COST_CALCULATION_ENDPOINT;

      endpoint = endpoint.replace("{{organization}}",organization);
      endpoint = endpoint.replace("{{amount}}",amount.toString());
      endpoint = endpoint.replace("{{callcalculation}}",callcalculation);
      endpoint = endpoint.replace("{{searchText}}",String(searchText));
      endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
      endpoint = endpoint.replace("{{size}}",String(size));
      return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 
}


