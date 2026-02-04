import { Injectable } from '@angular/core';
import { ConstantsService } from '../constants/constants.service';
import { ApiHttpService } from '../http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class WhatsappdictService {

  constructor( protected constService : ConstantsService,
                 protected httpService : ApiHttpService,) { }

  public getAllWhatsAppCurrencyCodes(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_CURRENCY_CODE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppConversationTypes(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_CONVERSATION_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppAddressTypes(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_ADDRESS_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppCalender(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_CALENDER_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppComponentSubType(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_COMPONENT_SUB_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppComponentType(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_COMPONENT_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppDayOfWeek(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_DAY_OF_WEEK_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppEmailType(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_EMAIL_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppLanguageCode(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_LANGUAGE_CODE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppLanguagePolicy(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_LANGUAGE_POLICY_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppMediaSelectedCriteria(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_MEDIA_SELECTION_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppMessageType(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_MESSAGE_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppMessageProduct(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_MESSAGE_PRODUCT_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppPhoneNumberType(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_PHONE_NUMBER_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppRecepientType(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_RECEIPT_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllWhatsAppSendMessageKeys(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_SEND_MESSAGE_KEYS_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 
  public getAllWhatsAppTemplateVariables(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_TEMPLATE_VARIABLES_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 
  public getAllWhatsAppTemplateVariablesType(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_TEMPLATE_VARIABLES_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 
  public getAllWhatsAppUrlType(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_URL_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 
  public getAllWhatsAppAdSource(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_AD_SOURCE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 
  public getAllWhatsAppChangeFieldType(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_CHANGE_FIELD_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 
  public getAllWhatsAppConversationCategory(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_CONVERSATION_CATEGORY_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 
  public getAllWhatsAppMessageStatusType(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_MESSAGE_STATUS_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 
  public getAllWhatsAppPaymentStatus(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_PAYMENT_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 
  public getAllWhatsAppPricingModel(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_PRICING_MODEL_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 
  public getAllWhatsAppTypeOfInteractive(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_INTERACTIVE_TYPE_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 
  public getAllWhatsAppBlockUserParameter(organization: string) { 
            var endpoint: string = this.constService.API_WHATSAPP_BLOCK_USER_PARAMETER_ENDPOINT;
            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

}
