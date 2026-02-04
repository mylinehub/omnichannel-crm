import { Injectable } from '@angular/core';
import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
@Injectable({
  providedIn: 'root'
})
export class EmployeeService {

  constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,) { }

         public getAllCostCalcultationType(organization: string) { 

            var endpoint: string = this.constService.API_GETALL_COST_CALCULATION_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

          public getAllUnlimitedPlanAmount(organization: string) { 

            var endpoint: string = this.constService.API_GETALL_UNLIMITED_PLAN_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
         } 

          public getAllMeteredPlanAmount(organization: string) { 

            var endpoint: string = this.constService.API_GETALL_METERED_PLAN_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
          } 

      public getAllEmployeesByOrganization(organization: string) { 

              var endpoint: string = this.constService.API_GETALL_EMPLOYEE_DETAIL_ENDPOINT;

              endpoint = endpoint.replace("{{organization}}",organization);
              return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
          } 

      public getAllEmployeesBasicInfoByOrganization(organization: string) { 

            var endpoint: string = this.constService.API_ALL_EMPLOYEE_BASIC_INFO_ENDPOINT;

            endpoint = endpoint.replace("{{organization}}",organization);
            return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public getEmployeeByEmailAndOrganization(email: string,organization: string) { 

          var endpoint: string = this.constService.API_EMPLOYEE_DETAIL_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{email}}",email);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public getEmployeeByPhonenumberAndOrganization(phonenumber: string,organization: string) { 

          var endpoint: string = this.constService.API_EMPLOYEE_DETAIL_BY_PHONENUMBER_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{phonenumber}}",phonenumber);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public getEmployeeByExtensionAndOrganization(extension: string,organization: string) { 

          var endpoint: string = this.constService.API_EMPLOYEE_DETAIL_BY_EXTENSION_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{extension}}",extension);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public getAllEmployeesOnUserRoleAndOrganization(role: string,organization: string) { 

          var endpoint: string = this.constService.API_EMPLOYEE_DETAIL_BY_USERROLE_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{role}}",role);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public getAllEmployeesOnSexAndOrganization(sex: string,organization: string) { 

          var endpoint: string = this.constService.API_EMPLOYEE_DETAIL_BY_SEX_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{sex}}",sex);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public getAllEmployeesOnPhoneContextAndOrganization(phoneContext: string,organization: string) { 

          var endpoint: string = this.constService.API_EMPLOYEE_DETAIL_BY_PHONECONTEXT_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{phoneContext}}",phoneContext);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public getAllEmployeesOnIsEnabledAndOrganization(isEnabled: boolean,organization: string) { 

          var endpoint: string = this.constService.API_EMPLOYEE_DETAIL_BY_ISENABLED_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{isEnabled}}",isEnabled.toString());
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        } 

        public findAllBycostCalculationAndOrganization(costCalculation: string,organization: string) { 

          var endpoint: string = this.constService.API_GETALL_EMPLOYEE_DETAIL_BY_COSTCALCULATION_ENDPOINT;

          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{costCalculation}}",costCalculation);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
        }
        
        public getEmployeeImages(email:string, organization: string) { 

          var endpoint: string = this.constService.API_GET_ALL_IMAGES_EMPLOYEE_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{email}}",email);
          return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
      } 

        public upload(data:FormData,organization:any) { 

          var endpoint: string = this.constService.API_UPLOAD_EMPLOYEE_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public updateWebPassword(data:any) { 

          var endpoint: string = this.constService.API_UPDATE_WEB_PASSWORD_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 
        
        public updateSelfExtensionPassword(data:any) { 

          var endpoint: string = this.constService.API_UPDATE_SELF_EXTENSION_PASSWORD_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public updateSelfByOrganization(data:any,organization:any,oldEmail:any) { 

          var endpoint: string = this.constService.API_UPDATE_SELF_EMPLOYEE_ENDPOINT;
          endpoint = endpoint.replace("{{oldEmail}}",oldEmail);
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public updateExtensionPassword(data:any) { 

          var endpoint: string = this.constService.API_UPDATE_EXTENSION_PASSWORD_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public updateEmployeeByOrganization(data:any,organization: string,oldEmail:any) { 

          var endpoint: string = this.constService.API_UPDATE_EMPLOYEE_ENDPOINT;
          endpoint = endpoint.replace("{{oldEmail}}",oldEmail);
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        public uploadProfilePicByEmailAndOrganization(data:FormData,organization:any,email:any) { 

          var endpoint: string = this.constService.API_UPLOAD_EMPLOYEE_PROFILE_PIC_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{email}}",email);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        public uploadDocOneByEmailAndOrganization(data:FormData,organization:any,email:any) { 

          var endpoint: string = this.constService.API_UPLOAD_EMPLOYEE_DOC1_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{email}}",email);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        public uploadDocTwoByEmailAndOrganization(data:FormData,organization:any,email:any) { 

          var endpoint: string = this.constService.API_UPLOAD_EMPLOYEE_DOC2_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          endpoint = endpoint.replace("{{email}}",email);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public getAllRecordingDataForEmployee(data:any) { 

          var endpoint: string = this.constService.API_GET_ALL_RECORDING__EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public enableUserOnEmailAndOrganization(data:any) { 

          var endpoint: string = this.constService.API_ENABLE_EMPLOYEE__ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public enableUseAllotedSecondLineByOrganization(data:any) { 

          var endpoint: string = this.constService.API_ENABLE_EMPLOYEE_SECOND_ALLOTED_LINE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public enableSelfCallOnMobile(data:any) { 

          var endpoint: string = this.constService.API_ENABLE_EMPLOYEE_SELF_CALL_ON_MOBILE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public enableEmployeeCallOnMobile(data:any) { 

          var endpoint: string = this.constService.API_ENABLE_EMPLOYEE_CALL_ON_MOBILE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public downloadRecordingForEmployee(data:any) { 

          var endpoint: string = this.constService.API_DOWNLOAD_RECORDING_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public disableUserOnEmailAndOrganization(data:any) { 

          var endpoint: string = this.constService.API_DIASBLE_EMPLOYEE__ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public disableUseAllotedSecondLineByOrganization(data:any) { 

          var endpoint: string = this.constService.API_DIASBLE_EMPLOYEE_SECOND_ALLOTED_LINE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public disableSelfCallOnMobile(data:any) { 

          var endpoint: string = this.constService.API_DIASBLE_EMPLOYEE_SELF_CALL_ON_MOBILE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public disableEmployeeCallOnMobile(data:any) { 

          var endpoint: string = this.constService.API_DIASBLE_EMPLOYEE_CALL_ON_MOBILE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        
        public createEmployeeByOrganization(data:any,organization: string) { 

          var endpoint: string = this.constService.API_CREATE_EMPLOYEE_ENDPOINT;
          endpoint = endpoint.replace("{{organization}}",organization);
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

        public updateNotificationDotStatusByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_Notification_Dot_Status_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

         public updateUserAllowedToSwitchOffWhatsAppAIByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_CONTROL_AI_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateEmployeeRecordAllCallsByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_RECORD_ALL_CALLS_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfDoNotDisturbByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_DO_NOT_DISTURB_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfStartVideoFullScreenByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_START_FULL_SCREEN_VIDEO_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfCallWaitingByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_CALL_WAITING_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateEmployeeIntercomPolicyByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_INTERCOM_POLICY_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateEmployeeFreeDialOptionByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_FREE_DIAL_OPTION_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateEmployeeTextDictationByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_TEXT_DICTATION_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateEmployeeTextMessagingByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_TEXT_MESSAGING_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfThemeByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_THEME_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfAutoAnswerByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_AUTO_ANSWER_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfAutoConferenceByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_AUTO_CONFERENCE_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfAutoVideoByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_AUTO_VIDEO_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfMicDeviceByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_MIC_DEVICE_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfSpeakerDeviceByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_SPEAKER_DEVICE_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfVideoDeviceByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_VIDEO_DEVICE_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfVideoOrientationByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_VIDEO_ORIENTATION_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfVideoQualityByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_VIDEO_QUALITY_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfVideoFrameRateByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_VIDEO_FRAME_RATE_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfAutoGainControlByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_AUTO_GAIN_CONTROL_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfEchoCancellationByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_ECHO_CANCELLATION_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }

        public updateSelfNoiseSupressionByOrganization(data:any) { 
          var endpoint: string = this.constService.API_UPDATE_SELF_NOISE_SUPRESSION_EMPLOYEE_ENDPOINT;
          return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }
               
}





