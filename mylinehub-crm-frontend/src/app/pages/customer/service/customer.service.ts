import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class CustomerService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }

       public findAllBywhatsAppProjectId(whatsAppProjectId:string,organization: string,searchText:String,pageNumber:number,size:number) { 

            var endpoint: string = this.constService.API_GETALL_CUSTOMER_BY_WHATSAPP_PROJECT_ID_ENDPOINT;
            endpoint = endpoint.replace("{{whatsAppProjectId}}",whatsAppProjectId);
            endpoint = endpoint.replace("{{organization}}",organization);
            endpoint = endpoint.replace("{{searchText}}",String(searchText));
            endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
            endpoint = endpoint.replace("{{size}}",String(size));
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

     public findAllByWhatsAppRegisteredByPhoneNumber(whatsAppRegisteredByPhoneNumber:string,organization: string,searchText:String,pageNumber:number,size:number) { 

            var endpoint: string = this.constService.API_GETALL_CUSTOMER_BY_WHATSAPP_REGISTERED_BY_ID_ENDPOINT;
            endpoint = endpoint.replace("{{whatsAppRegisteredByPhoneNumber}}",whatsAppRegisteredByPhoneNumber);
            endpoint = endpoint.replace("{{organization}}",organization);
            endpoint = endpoint.replace("{{searchText}}",String(searchText));
            endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
            endpoint = endpoint.replace("{{size}}",String(size));
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


         public getAllCustomersOnOrganization(organization: string,searchText:String,pageNumber:number,size:number) { 

            var endpoint: string = this.constService.API_GETALL_CUSTOMER_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            endpoint = endpoint.replace("{{searchText}}",String(searchText));
            endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
            endpoint = endpoint.replace("{{size}}",String(size));
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        public getCustomerByPeselAndOrganization(pesel: string,organization: string,searchText:String,pageNumber:number,size:number) { 

          var endpoint: string = this.constService.API_GETALL_CUSTOMER_BY_PESEL_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{pesel}}",pesel);
          endpoint = endpoint.replace("{{searchText}}",String(searchText));
          endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
          endpoint = endpoint.replace("{{size}}",String(size));
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 
        
        
        public getCustomerByIdAndOrganization(customerId: number,organization: string) { 

          var endpoint: string = this.constService.API_GETALL_CUSTOMER_BY_ID_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{customerId}}",customerId.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        
        public getCustomerByEmailAndOrganization(email: string,organization: string,searchText:String,pageNumber:number,size:number) { 

          var endpoint: string = this.constService.API_GETALL_CUSTOMER_BY_EMAIL_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{email}}",email);
          endpoint = endpoint.replace("{{searchText}}",String(searchText));
          endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
          endpoint = endpoint.replace("{{size}}",String(size));
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        
        public getByPhoneNumberAndOrganization(phoneNumber: string,organization: string) { 

          var endpoint: string = this.constService.API_GETALL_CUSTOMER_BY_PHONE_NUMBER_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{phoneNumber}}",phoneNumber);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        
        public findAllByZipCodeAndOrganization(zipCode: string,organization: string,searchText:String,pageNumber:number,size:number) { 

          var endpoint: string = this.constService.API_GETALL_CUSTOMER_BY_ZIP_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{zipCode}}",zipCode);
          endpoint = endpoint.replace("{{searchText}}",String(searchText));
          endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
          endpoint = endpoint.replace("{{size}}",String(size));
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        
        public findAllByPhoneContextAndOrganization(phoneContext: string,organization: string,searchText:String,pageNumber:number,size:number) { 

          var endpoint: string = this.constService.API_GETALL_CUSTOMER_BY_PHONECONTEXT_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{phoneContext}}",phoneContext);
          endpoint = endpoint.replace("{{searchText}}",String(searchText));
          endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
          endpoint = endpoint.replace("{{size}}",String(size));
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        
        public findAllByCovertedAndOrganization(coverted: boolean,organization: string,searchText:String,pageNumber:number,size:number) { 

          var endpoint: string = this.constService.API_GETALL_CUSTOMER_BY_ISCONVERTED_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{coverted}}",coverted.toString());
          endpoint = endpoint.replace("{{searchText}}",String(searchText));
          endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
          endpoint = endpoint.replace("{{size}}",String(size));
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        
        public findAllByCountryAndOrganization(country: string,organization: string,searchText:String,pageNumber:number,size:number) { 

          var endpoint: string = this.constService.API_GETALL_CUSTOMER_BY_COUNTRY_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{country}}",country);
          endpoint = endpoint.replace("{{searchText}}",String(searchText));
          endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
          endpoint = endpoint.replace("{{size}}",String(size));
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        
        public findAllByCityAndOrganization(city: string,organization: string,searchText:String,pageNumber:number,size:number) { 

          var endpoint: string = this.constService.API_GETALL_CUSTOMER_BY_CITY_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{city}}",city);
          endpoint = endpoint.replace("{{searchText}}",String(searchText));
          endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
          endpoint = endpoint.replace("{{size}}",String(size));
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        
        public findAllByBusinessAndOrganization(business: string,organization: string,searchText:String,pageNumber:number,size:number) { 

          var endpoint: string = this.constService.API_GETALL_CUSTOMER_BY_BUSINESS_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{business}}",business);
          endpoint = endpoint.replace("{{searchText}}",String(searchText));
          endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
          endpoint = endpoint.replace("{{size}}",String(size));
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public getCustomerImage(phoneNumber:string, organization: string) { 

          var endpoint: string = this.constService.API_GET_IMAGE_CUSTOMER_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{phoneNumber}}",phoneNumber);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

        public upload(data:FormData,organization:any) { 

          var endpoint: string = this.constService.API_UPLOAD_CUSTOMER_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public updateCustomerByOrganization(data:any,organization:any,oldPhone:any) { 

          var endpoint: string = this.constService.API_UPDATE_CUSTOMER_ENDPOINT;
          endpoint = endpoint.replace("{{oldPhone}}",oldPhone);
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        public updateCustomerDescription(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_CUSTOMER_DESCRIPTION_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public customerGotDiverted(data:any) { 

          var endpoint: string = this.constService.API_MARK_DIVERTED_CUSTOMER_ENDPOINT;
          
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public customerGotConverted(data:any) { 

          var endpoint: string = this.constService.API_MARK_CONVERTED_CUSTOMER_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        public updateWhatsAppAIAutoMessage(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_AUTO_WHATSAPP_CUSTOMER_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public createCustomerByOrganization(data:any,organization) { 

          var endpoint: string = this.constService.API_CREATE_CUSTOMER_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public deleteCustomerByIdAndOrganization(id:number,organization: string) { 

          var endpoint: string = this.constService.API_DELETE_CUSTOMER_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{id}}", id.toString());
          return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 


      public removeScheduledCall(scheduleType: String,phoneNumber: String,fromExtension: String,organization: string) { 

        var endpoint: string = this.constService.API_REMOVE_SCHEDULED_CALL_ENDPOINT;

        endpoint = endpoint.replace("{{scheduleType}}",scheduleType.toString());
        endpoint = endpoint.replace("{{organization}}",organization);
        endpoint = endpoint.replace("{{phoneNumber}}",phoneNumber.toString());
        endpoint = endpoint.replace("{{fromExtension}}",fromExtension.toString());
        return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 
      
      public findIfScheduledCallJobByOrganization(scheduleType: String,phoneNumber: String,fromExtension: String,organization: string) { 

        var endpoint: string = this.constService.API_Find_If_SCHEDULED_CALL_ENDPOINT;
        endpoint = endpoint.replace("{{scheduleType}}",scheduleType.toString());
        endpoint = endpoint.replace("{{organization}}",organization);
        endpoint = endpoint.replace("{{phoneNumber}}",phoneNumber.toString());
        endpoint = endpoint.replace("{{fromExtension}}",fromExtension.toString());
        return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

      public scheduleAFixedDateCall(data:any) { 

        var endpoint: string = this.constService.API_SCHEDULE_CALL_FIXEDDATE_ENDPOINT;
        return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
      } 


      public scheduleAfterNSecCall(data:any) { 

        var endpoint: string = this.constService.API_SCHEDULE_CALL_AFTERNSEC_ENDPOINT;
        return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
      } 


      public scheduleCronCall(data:any) { 

        var endpoint: string = this.constService.API_SCHEDULE_CALL_CRON_ENDPOINT;
        return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
      } 

      public updateCustomerProductInterests(data:any) { 

        var endpoint: string = this.constService.API_Update_Customer_Product_Interest_ENDPOINT;
        return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
      } 

      public uploadPicByIdAndOrganization(data:FormData,id:number,organization: string) { 

        var endpoint: string = this.constService.API_UPLOAD_CUSTOMER_PIC_ENDPOINT;
        endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{id}}", id.toString());
        return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
      } 

}




