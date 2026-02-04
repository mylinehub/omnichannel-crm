import { Injectable } from '@angular/core';
import { ConstantsService } from '../constants/constants.service';
import { ApiHttpService } from '../http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class ChatHistoryService {

  constructor( protected constService : ConstantsService,
    protected httpService : ApiHttpService,) { }
    

    public getAllChatHistoryCandidatesByExtensionAndOrganization(data:any) { 
      var endpoint: string = this.constService.API_GET_CHAT_HISTORY_CANDIDATES;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public getAllChatHistoryByTwoExtensionsAndOrganization(data:any) { 
      var endpoint: string = this.constService.API_GET_CHAT_HISTORY;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public deleteAllChatHistoryByExtensionAndOrganization(data:any) { 
      var endpoint: string = this.constService.API_DELETE_ALL_CHAT_HISTORY_FOR_ALL;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public deleteAllChatHistoryByTwoExtensionsAndOrganization(data:any) { 
      var endpoint: string = this.constService.API_DELETE_ALL_CHAT_HISTORY_FOR_ONE;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public appendChatHistoryByTwoExtensionsAndOrganization(data:any) { 
      var endpoint: string = this.constService.API_APPEND_CHAT_HISTORY;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public updateLastReadIndexByTwoExtensionsAndOrganization(data:any) { 
      var endpoint: string = this.constService.API_UPDATE_LASTREADINDEX;
      return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

}
