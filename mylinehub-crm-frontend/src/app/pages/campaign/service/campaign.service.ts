import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class CampaignService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }

          public getAllAutodialerTypes(organization: string) { 

            var endpoint: string = this.constService.API_GET_ALL_AUTODIALER_TYPE_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);

            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

          public getAllReminderCallingType(organization: string) { 

            var endpoint: string = this.constService.API_GET_ALL_REMINDER_CALLING_TYPE_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);

            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


          public getAllCampaignsOnOrganization(organization: string) { 

            var endpoint: string = this.constService.API_GET_ALL_CAMPAIGNS_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);

            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        public getCampaignByNameAndOrganization(campaignName: string,organization: string) { 

          var endpoint: string = this.constService.API_GET_CAMPAIGN_BY_NAME_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{campaignName}}",campaignName);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public getCampaignByIdAndOrganization(campaignId: number,organization: string) { 

          var endpoint: string = this.constService.API_GET_CAMPAIGN_BY_ID_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{campaignId}}",campaignId.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllByPhonecontextAndOrganization(phonecontext: string,organization: string) { 

          var endpoint: string = this.constService.API_GET_CAMPAIGN_BY_PHONECONTEXT_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{phonecontext}}",phonecontext);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllByIsonmobileAndOrganization(isonmobile: boolean,organization: string) { 

          var endpoint: string = this.constService.API_GET_CAMPAIGN_BY_ISMOBILE_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{isonmobile}}",isonmobile.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllByCountryAndOrganization(country: string,organization: string) { 

          var endpoint: string = this.constService.API_GET_CAMPAIGN_BY_COUNTRY_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{country}}",country);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllByBusinessAndOrganization(business: string,organization: string) { 

          var endpoint: string = this.constService.API_GET_CAMPAIGN_BY_BUSINESS_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{business}}",business);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllByAutodialertypeAndOrganization(autodialertype: string,organization: string) { 

          var endpoint: string = this.constService.API_GET_CAMPAIGN_BY_AUTODIALER_TYPE_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{autodialertype}}",autodialertype);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllByManagerAndOrganization(managerId: number,organization: string) { 

          var endpoint: string = this.constService.API_GET_CAMPAIGN_BY_MANAGER_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{managerId}}",managerId.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllByStartdateGreaterThanEqualAndOrganization(startdate: string,organization: string) { 

          var endpoint: string = this.constService.API_GET_ALL_CAMPAIGNS_BY_STARTDATE_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{startdate}}",startdate);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllByEmployeeAndOrganization(extension: string,organization: string) { 

          var endpoint: string = this.constService.API_GET_ALL_CAMPAIGNS_FOR_EMPLOYEE_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{extension}}",extension);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllEmployeesByCampaignAndOrganization(id: number,organization: string) { 

          var endpoint: string = this.constService.API_GET_ALL_EMPLOYEES_OF_CAMPAIGN_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{id}}",id.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllByCustomerAndOrganization(phoneNumber: string,organization: string) { 

          var endpoint: string = this.constService.API_GET_ALL_CAMPAIGN_FOR_CUSTOMER_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{phoneNumber}}",phoneNumber);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllCustomersByCampaignAndOrganization(id: number,organization: string,searchText:String,pageNumber:number,size:number) { 

          var endpoint: string = this.constService.API_GET_ALL_CUSTOMERS_OF_CAMPAIGN_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{id}}",id.toString());
          endpoint = endpoint.replace("{{searchText}}",String(searchText));
          endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
          endpoint = endpoint.replace("{{size}}",String(size));
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 


        
        public removeStartScheduledCampgin(scheduleType: String,campaignId: number,organization: string) { 

          var endpoint: string = this.constService.API_REMOVE_SCHEDULED_START_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{scheduleType}}",scheduleType.toString());
          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{campaignId}}",campaignId.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findIfStartScheduledJobsOrganization(scheduleType: String,campaignId: string,organization: string) { 

          var endpoint: string = this.constService.API_Find_If_SCHEDULED_START_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{scheduleType}}",scheduleType.toString());
          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{campaignId}}",campaignId);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public scheduleAFixedDateStartCampaign(data:any) { 

          var endpoint: string = this.constService.API_SCHEDULE_START_CAMPAIGN_FIXEDDATE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public scheduleAfterNSecStartCampaign(data:any) { 

          var endpoint: string = this.constService.API_SCHEDULE_START_CAMPAIGN_AFTERNSEC_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public scheduleCronStartCampaign(data:any) { 

          var endpoint: string = this.constService.API_SCHEDULE_START_CAMPAIGN_CRON_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public removeStopScheduledCampaign(scheduleType: String,campaignId: number,organization: string) { 

          var endpoint: string = this.constService.API_REMOVE_SCHEDULED_STOP_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{scheduleType}}",scheduleType.toString());
          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{campaignId}}",campaignId.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findIfStopScheduledJobsOrganization(scheduleType: String,campaignId: string,organization: string) { 

          var endpoint: string = this.constService.API_Find_If_SCHEDULED_STOP_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{scheduleType}}",scheduleType.toString());
          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{campaignId}}",campaignId);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public scheduleAFixedDateStopCampaign(data:any) { 

          var endpoint: string = this.constService.API_SCHEDULE_STOP_CAMPAIGN_FIXEDDATE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public scheduleAfterNSecStopCampaign(data:any) { 

          var endpoint: string = this.constService.API_SCHEDULE_STOP_CAMPAIGN_AFTERNSEC_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public scheduleCronStopCampaign(data:any) { 

          var endpoint: string = this.constService.API_SCHEDULE_STOP_CAMPAIGN_CRON_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        public pauseCampaignByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_PAUSE_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public resetCampaignByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_RESET_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public startCampaignByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_START_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public stopCampaignByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_STOP_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public unpauseCampaignByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_UNPAUSE_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public updateCampaignByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_UPDATE_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public createCampaignByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_CREATE_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public createEmployeeToCampaignByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_CREATE_EMPLOYEE_TO_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public updateEmployeeToCampaignByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_UPDATE_EMPLOYEE_TO_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public deleteEmployeeToCampaignByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_DELETE_EMPLOYEE_TO_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public createCustomerToCampaignByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_CREATE_CUSTOMER_TO_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        public getCountForCustomerToCampaignByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_GET_COUNT_CUSTOMER_TO_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public updateCustomerToCampaignByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_UPDATE_CUSTOMER_TO_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public deleteCustomerToCampaignByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_DELETE_CUSTOMER_TO_CAMPAIGN_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 


        public deleteCampaignByIdAndOrganization(id:number,organization: string) { 

          var endpoint: string = this.constService.API_DELETE_CAMPAIGN_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{id}}", id.toString());
          return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

      public deleteAllEmployeesByCampaignIdAndOrganization(id:number,organization: string) { 

        var endpoint: string = this.constService.API_DELETE_ALL_EMPLOYEE_TO_CAMPAIGN_ENDPOINT;

        endpoint = endpoint.replace("{{organization}}",organization);
        endpoint = endpoint.replace("{{id}}", id.toString());
        return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

      public deleteAllCustomersByCampaignIdAndOrganization(id:number,organization: string) { 

        var endpoint: string = this.constService.API_DELETE_ALL__CUSTOMER_TO_CAMPAIGN_ENDPOINT;

        endpoint = endpoint.replace("{{organization}}",organization);
        endpoint = endpoint.replace("{{id}}", id.toString());
        return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 


}


