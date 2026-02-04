import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class AbsenteeismService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }


      public getAllAbsenteeismOnOrganization(organization: string) { 

        var endpoint: string = this.constService.API_ALL_ABSENTEEISM_DETAIL_ENDPOINT;

        endpoint = endpoint.replace("{{organization}}",organization);

        return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

      public findAllByDateFromGreaterThanEqualAndDateToLessThanEqualOrganization(dateFrom: string,dateTo: string,organization: string) { 

        var endpoint: string = this.constService.API_GET_ALL_ABSENTEEISM_BY_DATE_ENDPOINT;

        endpoint = endpoint.replace("{{organization}}",organization);
        endpoint = endpoint.replace("{{dateFrom}}",dateFrom);
        endpoint = endpoint.replace("{{dateTo}}",dateTo);
        return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
    } 

        public findAllByEmployeeAndOrganization(employeeID: string,organization: string) { 

          var endpoint: string = this.constService.API_GET_ALL_ABSENTEEISM_BY_EMPLOYEE_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{employeeID}}",employeeID);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

      public findAllByReasonForAbsenseAndOrganization(reasonForAbsense: string,organization: string) { 

        var endpoint: string = this.constService.API_GET_ALL_ABSENTEEISM_BY_ROA_ENDPOINT;

        endpoint = endpoint.replace("{{organization}}",organization);
        endpoint = endpoint.replace("{{reasonForAbsense}}",reasonForAbsense);
        return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

        public createAbsenteeismByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_CREATE_ABSENTEEISM__ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
      } 

        public updateAbsenteeismByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_UPDATE_ABSENTEEISM__ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
      } 

        public deleteAbsenteeismByIdAndOrganization(id:number,organization: string) { 

          var endpoint: string = this.constService.API_DELETE_ABSENTEEISM__ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{id}}", id.toString());
          return this.httpService.deleteWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

}
