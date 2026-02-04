import { Injectable } from '@angular/core';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { ApiHttpService } from '../../../../service/http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class RunCampaignService {

  constructor( protected constService : ConstantsService,
                 protected httpService : ApiHttpService,) { }
  
  public listCampaignIdsMerged(organization: string) {
     var endpoint: string = this.constService.API_List_ATLEAST_ONCE_RUN_CAMPAIGN_ID_ENDPOINT;
     endpoint = endpoint.replace("{{organization}}",organization);
     return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public listRunIdsForCampaignMerged(organization: string,campaignId: number) {
     var endpoint: string = this.constService.API_List__CAMPAIGN_Run_ID_ENDPOINT;
     endpoint = endpoint.replace("{{organization}}",organization);
     endpoint = endpoint.replace("{{campaignId}}",String(campaignId));
     return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getCallLogsMergedForRun(organization: string,campaignId: number,runId: number,pageNumber: number,size: number,searchText: string) {
     var endpoint: string = this.constService.API_CAMPAIGN_Run_ID_CALL_LOGS_OLD_ENDPOINT;
     endpoint = endpoint.replace("{{organization}}",organization);
     endpoint = endpoint.replace("{{campaignId}}",String(campaignId));
     endpoint = endpoint.replace("{{runId}}",String(runId));
     endpoint = endpoint.replace("{{pageNumber}}",String(pageNumber));
     endpoint = endpoint.replace("{{size}}",String(size));
     endpoint = endpoint.replace("{{searchText}}",searchText);
     return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getCurrentRunLiveLogsMemoryOnly(organization: string,campaignId: number,searchText: string) {
     var endpoint: string = this.constService.API_CAMPAIGN_Run_ID_CALL_LOGS_CURRENT_MEMORY_ENDPOINT;
     endpoint = endpoint.replace("{{organization}}",organization);
     endpoint = endpoint.replace("{{campaignId}}",String(campaignId));
     endpoint = endpoint.replace("{{searchText}}",searchText);
     return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public exportRunExcelDbOnly(organization: string, campaignId: number, runId: number) {
   var endpoint: string = this.constService.API_CAMPAIGN_RUN_EXCEL_EXPORT_DBONLY_ENDPOINT;
   endpoint = endpoint.replace("{{organization}}", organization);
   endpoint = endpoint.replace("{{campaignId}}", String(campaignId));
   endpoint = endpoint.replace("{{runId}}", String(runId));

   // IMPORTANT: returns Blob via ApiHttpService (token + responseType blob)
   return this.httpService.getWithTokenAndBlobAsJson(this.constService.API_BASE_ENDPOINT + endpoint);
   }

   public exportRunRecordings(organization: string, campaignId: number, runId: number) {
   var endpoint: string = this.constService.API_CAMPAIGN_RUN_RECORDINGS_ENDPOINT;
   endpoint = endpoint.replace("{{organization}}", organization);
   endpoint = endpoint.replace("{{campaignId}}", String(campaignId));
   endpoint = endpoint.replace("{{runId}}", String(runId));

   // IMPORTANT: returns Blob via ApiHttpService (token + responseType blob)
   return this.httpService.getWithTokenAndBlobAsJson(this.constService.API_BASE_ENDPOINT + endpoint);
   }
}
