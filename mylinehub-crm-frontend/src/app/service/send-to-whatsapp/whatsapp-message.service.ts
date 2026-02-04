import { Injectable } from '@angular/core';
import { ConstantsService } from '../constants/constants.service';
import { ApiHttpService } from '../http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class WhatsappMessageService {

  constructor( protected constService : ConstantsService,
                       protected httpService : ApiHttpService,) { }
    
    public getWhatsAppMediaUrl(data:any) { 
              var endpoint: string = this.constService.API_WHATSAPP_GET_MEDIA_URL_ENDPOINT;
              return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }
}
