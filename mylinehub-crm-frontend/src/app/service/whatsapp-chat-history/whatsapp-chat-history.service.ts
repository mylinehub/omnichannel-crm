import { Injectable } from '@angular/core';
import { ConstantsService } from '../constants/constants.service';
import { ApiHttpService } from '../http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class WhatsappChatHistoryService {

  constructor( protected constService : ConstantsService,
                     protected httpService : ApiHttpService,) { }
  

public downloadChatHistoryExcelDbOnly(data: any) {
  var endpoint: string = this.constService.API_DOWNLOAD_CHAT_HISTORY_ENDPOINT;
  return this.httpService.postWithTokenAndBlobAsJson(
    this.constService.API_BASE_ENDPOINT + endpoint,
    data
  );
}

  public getAllChatHistoryForPhoneNumberMain(data:any) { 
            var endpoint: string = this.constService.API_GET_All_CHAT_HISTORY_FOR_PHONE_MAIN_ENDPOINT;
            return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  }

  public getAllChatHistoryByTwoPhoneNumbersAndOrganization(data:any) { 
            var endpoint: string = this.constService.API_GET_All_CHAT_HISTORY_FOR_TWO_PHONE_ENDPOINT;
            return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  }

  public deleteAllChatHistoryByPhoneNumberMainAndOrganization(data:any) { 
            var endpoint: string = this.constService.API_DELETE_All_CHAT_HISTORY_FOR_PHONE_MAIN_ENDPOINT;
            return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  }

  public deleteAllChatHistoryByTwoPhoneNumbersAndOrganization(data:any) { 
            var endpoint: string = this.constService.API_DELETE_All_CHAT_HISTORY_FOR_TWO_PHONE_ENDPOINT;
            return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  }

  public updateLastReadIndexByTwoPhoneNumbersAndOrganization(data:any) { 
            var endpoint: string = this.constService.API_UPDATE_LAST_READ_INDEX_FOR_TWO_PHONE_ENDPOINT;
            return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  }
   
}
