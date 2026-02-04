import { Injectable } from '@angular/core';
import { ConstantsService } from '../constants/constants.service';
import { ApiHttpService } from '../http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class NotificationDataService {

  constructor( protected constService : ConstantsService,
    protected httpService : ApiHttpService,) { }
    

    public getAllNotificationsByExtensionAndOrganization(data:any) { 

      var endpoint: string = this.constService.API_GET_ALL_NOTIFICATION_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    } 

    public deleteAllNotificationsByExtensionAndOrganization(data:any) { 

      var endpoint: string = this.constService.API_DELETE_ALL_NOTIFICATION_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    } 

    public deleteNotificationByIdsAndExtensionsAndOrganization(data:any) { 

      var endpoint: string = this.constService.API_DELETE_FEW_NOTIFICATION_ENDPOINT;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    } 
}
