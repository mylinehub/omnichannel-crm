import { Injectable, OnDestroy } from '@angular/core';
import { ConstantsService } from '../constants/constants.service';
import { NbDialogService } from '@nebular/theme';
import { Subject, Subscription } from 'rxjs';
import { myRxStompConfig } from './stomp.config';
// import { RxServiceService } from '../rx-stomp/rx-service.service';
import { NotificationListDataService } from '../../@theme/components/header/notification-list/notification-list-data-service/notification-list-data.service';
import { MessageListDataService } from '../../@theme/components/header/message-list/message-list-data-service/message-list-data.service';
import { DialogComponent } from '../../pages/employee/all-employees/dialog/dialog.component';
import { HeaderVariableService } from '../header-variable/header-variable.service';
import { PhoneMusicService } from '../phone-music/phone-music.service';
import { VideoDialogDataService } from '../../@theme/components/header/video-dialog/video-dialog-data-service/video-dialog-data.service';
import { PreviewCustomerDataService } from '../../pages/customer/preview-customer/preview-customer-data-service/preview-customer-data.service';
import { PreviewScheduleDataServiceService } from '../../pages/customer/preview-schedule-customers/preview-schedule-customer-data-service/preview-schedule-data-service.service';
import { WhatsappDataServiceService } from '../../pages/whatsapp-chat/chat/whatsapp-data-service/whatsapp-data-service.service';
import { JwtHelperService } from '@auth0/angular-jwt';
// import { Client } from 'stompjs';
// import { WebSocket } from 'ws';
// import SockJS from 'sockjs-client';
// import { RxStomp } from '@stomp/rx-stomp';
// import { Message,Stomp } from '@stomp/stompjs';
declare var SockJS: any;
declare var Stomp: any;

@Injectable({
  providedIn: 'root'
})
export class StompService implements OnDestroy { 
  private destroy$: Subject<void> = new Subject<void>();

  stompClient:any = null;
  private genericSubscription: Subscription;
  private extensionSubscription: Subscription;
  private conferenceSubscription: Subscription;
  private whatsAppSubscription: Subscription[] = [];  
  private whatsAppNumbers: string[] = [];
  updateLastUpdateIndexForPhoneId: any;
  reconnectDelay = 10000; // 10 seconds

  constructor(protected constantService : ConstantsService,
              private dialogService: NbDialogService,
              // private rxStompService: RxServiceService,
              private notificationListDataService: NotificationListDataService,
              protected messageListDataService:MessageListDataService,
              protected headerVariableService:HeaderVariableService,
              protected phoneMusicService:PhoneMusicService,
              protected videoDialogDataService:VideoDialogDataService,
              protected previewCustomerDataService:PreviewCustomerDataService,
              protected previewScheduleDataService:PreviewScheduleDataServiceService,
              protected whatsappDataService : WhatsappDataServiceService,
              protected jwtHelperService:JwtHelperService,) {
  }


   createClient(orgData: any): void {
    console.log('[STOMP] Initiating STOMP connection check...');

    const token = localStorage.getItem('token');

    // 🚫 No token found
    if (!token) {
      console.warn('[STOMP] No token found. Retrying in 10 seconds...');
      this.retryConnect(orgData);
      return;
    }

    // 🔐 Token check
    const isExpired  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));

    if (isExpired) {
      console.warn('[STOMP] 🔐 Token expired. Skipping connection. Will retry in 10 seconds...');
      this.retryConnect(orgData);
      return;
    }

    // ✅ If already connected
    if (this.stompClient && this.stompClient.connected) {
      console.log('[STOMP] Already connected. Skipping reconnection.');
      return;
    }

    // 🌐 Start connection
    console.log('[STOMP] Creating new STOMP client...');
    const webSocketEndPoint = ConstantsService.webSocketEndPoint;
    const ws = new SockJS(webSocketEndPoint);
    this.stompClient = Stomp.over(ws);
    this.stompClient.debug = null;
    this.stompClient.reconnect_delay = 0; // disable auto-reconnect, handle manually

    try {
      this.stompClient.connect(
        { auth: token },
        (frame: any) => {
          console.log('[STOMP] Connected:', frame);

          try {
            this.startUnsubscription(orgData);
            console.log('[STOMP] Unsubscribed old topics.');
          } catch (e) {
            console.error('[STOMP] Error during unsubscription:', e);
          }

          try {
            this.startSubscription(orgData);
            console.log('[STOMP] Subscribed to topics.');
          } catch (e) {
            console.error('[STOMP] Error during subscription:', e);
          }
        },
        (error: any) => {
          console.error('[STOMP] Connection error callback triggered:', error);
          console.warn('[STOMP] Will retry connection in 10 seconds...');
          this.retryConnect(orgData);
        }
      );
    } catch (e) {
      console.error('[STOMP] Exception during connect():', e);
      this.retryConnect(orgData);
    }
  }

  private retryConnect(orgData: any) {
    setTimeout(() => {
      console.log('[STOMP] Retrying STOMP connection...');
      this.createClient(orgData);
    }, this.reconnectDelay);
  }

  disconnect()
  {
    console.log("Disconnect Stomp Client");
    this.stompClient.disconnect();
  }

  startUnsubscription(orgData: any) {
    const genericEvent: any = ConstantsService.event + ConstantsService.user.organization;
    console.log("Unsubscribing from organization:", ConstantsService.user.organization);
    try {
      this.stompClient.unsubscribe(genericEvent, (message: any) => {
        this.callBackForGeneric(message);
      }, this.callBackErrorForGeneric);
    } catch (e) {
      console.error("Error unsubscribing from organization:", e);
    }
    this.genericSubscription = null;

    const extensionEvent: any = ConstantsService.event + ConstantsService.user.extension;
    console.log("Unsubscribing from extension:", ConstantsService.user.extension);
    try {
      this.stompClient.unsubscribe(extensionEvent, (message: any) => {
        this.callBackForExtension(message);
      }, this.callBackErrorForExtension);
    } catch (e) {
      console.error("Error unsubscribing from extension:", e);
    }
    this.extensionSubscription = null;

    if (orgData.enableWhatsAppMessaging) {
      console.log("Unsubscribing STOMP for WhatsApp in header component");
      console.log("this.whatsAppSubscription length:", this.whatsAppSubscription.length);
      console.log("this.whatsAppNumbers length:", this.whatsAppNumbers.length);

      this.whatsappDataService.allNumbersData.forEach((element, index) => {
        try {
          const phoneNumberEventEvent: any = ConstantsService.event + element.phoneNumber;
          console.log("Unsubscribing from phoneNumberEventEvent:", phoneNumberEventEvent);
          this.stompClient.unsubscribe(phoneNumberEventEvent, (message: any) => {
            this.callBackForWhatsAppPhone(message, 4000);
          }, this.callBackErrorForWhatsAppPhone);
        } catch (err) {
          console.error("Error unsubscribing from phone number:", err);
        }
      });

      this.whatsAppNumbers = [];
      this.whatsAppSubscription = [];
    } else {
      console.log("Not allowed to initialize STOMP for WhatsApp in header component");
    }
  }

  startSubscription(orgData: any) {
    const genericEvent: any = ConstantsService.event + ConstantsService.user.organization;
    console.log("Subscribing to organization:", ConstantsService.user.organization);
    try {
      this.genericSubscription = this.stompClient.subscribe(genericEvent, (message: any) => {
        this.callBackForGeneric(message);
      }, this.callBackErrorForGeneric);
    } catch (e) {
      console.error("Error subscribing to organization:", e);
    }

    const extensionEvent: any = ConstantsService.event + ConstantsService.user.extension;
    console.log("Subscribing to extension:", ConstantsService.user.extension);
    try {
      this.extensionSubscription = this.stompClient.subscribe(extensionEvent, (message: any) => {
        this.callBackForExtension(message);
      }, this.callBackErrorForExtension);
    } catch (e) {
      console.error("Error subscribing to extension:", e);
    }

    if (orgData.enableWhatsAppMessaging) {
      console.log("Subscribing STOMP for WhatsApp in header component");
      this.whatsappDataService.allNumbersData.forEach((element, index) => {
        try {
          const phoneNumberEventEvent: any = ConstantsService.event + element.phoneNumber;
          console.log("Subscribing to phoneNumberEventEvent:", phoneNumberEventEvent);
          this.whatsAppNumbers[index] = element.phoneNumber;
          this.whatsAppSubscription[index] = this.stompClient.subscribe(phoneNumberEventEvent, (message: any) => {
            this.callBackForWhatsAppPhone(message, 4000);
          }, this.callBackErrorForWhatsAppPhone);
        } catch (err) {
          console.error("Error subscribing to phone number:", err);
        }
      });

      console.log("this.whatsAppSubscription length:", this.whatsAppSubscription.length);
      console.log("this.whatsAppNumbers length:", this.whatsAppNumbers.length);
    } else {
      console.log("Not allowed to initialize STOMP for WhatsApp in header component");
    }
  }

  unsubscribeConference(confId:any)
  {
    console.log("Un-Subscribing to conference : ", confId);
    this.stompClient.unsubscribe(confId, (message:any)=> {
      this.callBackForConference(message);
    },this.callBackErrorForConference);
    this.conferenceSubscription = null;

  }

  subscribeConference(confId:any)
  {
    console.log("Subscribing to conference : ", confId);
    this.conferenceSubscription = this.stompClient.subscribe(confId, (message:any)=> {
      this.callBackForConference(message);
    },this.callBackErrorForConference);
  }

  callBackForConference(message:any)
  {
  }

  callBackForExtension(message:any)
  {
      try{
        // console.log('callBackForExtension : ',JSON.parse(message.body));
        // console.log("JSON.parse(message.body).messagetype :" + JSON.parse(message.body).messagetype)
       if(JSON.parse(message.body).messagetype == 'notification')
       {
          this.notificationListDataService.ingestNotification(JSON.parse(JSON.parse(message.body).message));
       }
       else if (JSON.parse(message.body).messagetype == 'chat')
       {
            this.ingestChat(message);
       }
       else if (JSON.parse(message.body).messagetype == 'previewCall')
        {
            //  console.log("previewCall received")
             this.ingestCallPreviewMessage(message);
        }
        else if (JSON.parse(message.body).messagetype == 'previewScheduleCall')
          {
              //  console.log("previewScheduleCall received")
               this.ingestScheduleCallPreviewMessage(message);
          }

      }
      catch(e)
      {
        // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(e)); 
      }   
  }


  callBackForGeneric(message:any)
  {
    // console.log('callBackForGeneric : ',JSON.parse(message.body));
    try{
      if(JSON.parse(message.body).messagetype == 'notification')
       {
          this.notificationListDataService.ingestNotification(JSON.parse(JSON.parse(message.body).message));
       }
    }
    catch(e)
    {
      // console.log("Error : "+ JSON.stringify(err));
      this.showDialoge('Error','activity-outline','danger', JSON.stringify(e)); 
    }
  }


  callBackForWhatsAppPhone(message:any,timeout:number)
  {
    console.log('callBackForWhatsAppPhone mesage : ');
    console.log(message);
    this.ingestPhoneNumberMessage(message,timeout);
  }

  
  ingestPhoneNumberMessage(message: any, timeout: number) {
  console.log('📨 ingestPhoneNumberMessage');

  try {
    const messageBody = JSON.parse(message.body);
    console.log('messageBody.phoneNumberMain : '+messageBody.phoneNumberMain);
    console.log('this.whatsappDataService.phoneNumberMain : '+this.whatsappDataService.phoneNumberMain);

    if (messageBody.phoneNumberMain === this.whatsappDataService.phoneNumberMain) {
      console.log('📲 Received message is from currently open support number.');

      const phoneNumberWith = messageBody.phoneNumberWith;

      console.log('phoneNumberWith : '+phoneNumberWith);
      console.log('this.whatsappDataService.currentCustomerRecord?.phoneNumber : ' +
      (this.whatsappDataService.currentCustomerRecord?.phoneNumber ?? 'N/A'));

      if (
        phoneNumberWith &&
        this.whatsappDataService.currentCustomerRecord &&
        phoneNumberWith === this.whatsappDataService.currentCustomerRecord.phoneNumber
      ) {
        console.log('✅ Received message is open in current chat window');

        if(messageBody.messageType == null ||  messageBody.messageType==""){
            //do nothing
            console.log("Message type was null");
        }
        else if(messageBody.messageType == 'status'){
            console.log("Message type was status")

            // WhatsApp message ID you're searching for:
            const targetMessageId = messageBody.whatsAppMessageId;
            console.log("targetMessageId : "+targetMessageId);
            // Loop from end to start to find the message
            if (Array.isArray(this.whatsappDataService.allChatData)) {
              for (let i = this.whatsappDataService.allChatData.length - 1; i >= 0; i--) {
                  const chatItem = this.whatsappDataService.allChatData[i];
                  if (!chatItem) continue; // Skip null entries safely
                  console.log(`Index: ${i}, messageId: ${chatItem.whatsAppMessageId}, messageString: ${chatItem.messageString}`);

                  if (this.whatsappDataService.allChatData[i].whatsAppMessageId === targetMessageId) {
                      // 🔄 Update values here
                      if (messageBody.messageString === "sent") {
                            console.log("[updateStatus] Status 'sent' received");
                            this.whatsappDataService.allChatData[i].sent = true;
                        } else if (messageBody.messageString === "read") {
                            console.log("[updateStatus] Status 'read' received");
                            this.whatsappDataService.allChatData[i].read = true;
                        } else if (messageBody.messageString === "failed") {
                            console.log("[updateStatus] Status 'failed' received");
                            this.whatsappDataService.allChatData[i].failed = true;

                            if(messageBody.fromExtension === ConstantsService.user.extension){
                              this.showDialoge('Error', 'activity-outline', 'danger', '24hr window / conversation session is closed. System can only send template message to this user.');
                            }
                            
                        } else if (messageBody.messageString === "delivered") {
                            console.log("[updateStatus] Status 'delivered' received");
                            this.whatsappDataService.allChatData[i].delivered = true;
                        } else if (messageBody.messageString === "deleted") {
                            console.log("[updateStatus] Status 'deleted' received");
                            this.whatsappDataService.allChatData[i].deleted = true;
                        } else {
                            console.log("[updateStatus] Unknown status received: " + messageBody.messageString);
                        }
                      console.log(`Message with ID ${targetMessageId} found at index ${i} of chat history and updated.`);
                      break; // ✅ Exit loop after finding the message
                  }
              }
            }
            else {
              console.log("⚠️ allChatData not initialized, skipping message status update");
            }
        }
        else{

            // Add record to current chat
            console.log('📥 Pushing message body into current chat data (new message) - Message Type : '+messageBody.messageType);
            this.whatsappDataService.allChatCurrent.push(messageBody);

            console.log('📥 Pushing message body into all chat data (old + new messages)');
            this.whatsappDataService.allChatData.push(messageBody);

            // Update read status (once in 4 seconds if message exchange has stopped)
            if (this.updateLastUpdateIndexForPhoneId == null) {
              console.log('⏳ No timeout set. Setting up new timeout for 4 seconds');
            } else {
              console.log('♻️ Timeout already set. Clearing and setting new 4-second timeout');
              clearTimeout(this.updateLastUpdateIndexForPhoneId);
            }

            this.updateLastUpdateIndexForPhoneId = setTimeout(() => {
              console.log('Sending updateLastUpdateIndexForPhoneId');
              this.whatsappDataService.updateLastUpdateIndexForPhone(phoneNumberWith);
            }, timeout);
        } 

        if(this.whatsappDataService.selectedAction == 'Chat'){
          console.log('this.whatsappDataService.selectedAction == Chat, hence increasing batch ass well');
          this.increaseWhatsAppParticipantBatchOrElseAddNew(messageBody, phoneNumberWith);
        }
      } 
      else {
        console.log('Current customer is not same as in chat window');
        this.increaseWhatsAppParticipantBatchOrElseAddNew(messageBody, phoneNumberWith);
      }

      // Play ringing sound if message is not from self
      console.log('🔊 Ringing sound');
      if (messageBody.messageType != 'status' && messageBody.fromExtension !== ConstantsService.user.extension) {
        try{
        this.phoneMusicService.ringAlertMusic();
        }
        catch(e){
          console.log(e);
        }
      }
      else{
        console.log("Did not ring bell for message Extension : "+messageBody.fromExtension + " and messageType : "+messageBody.messageType);
      }
    } else {
      console.log('❌ Received message is not currently open as support number.');
      console.log('➡️ It will not be required to search for this message at all. It will not be in participants as well');
    }
  } catch (err) {
    console.log('🚨 Error:', err);
    // this.showDialoge('Error', 'activity-outline', 'danger', JSON.stringify(err));
  }
}

 increaseWhatsAppParticipantBatchOrElseAddNew(messageBody:any, phoneNumberWith:any){
  console.log('increaseWhatsAppParticipantBatch');
  if(messageBody.messageType == null ||  messageBody.messageType==""){
            //do nothing
            console.log("Message type was null - Do nothing");
        }
        else if(messageBody.messageType == 'status'){
            console.log("Message type was status - Do Nothing")
        }
        else{
              console.log('📪 Received message is NOT in the currently open chat window.');

        // Add badge for unseen messages
        const participants = this.whatsappDataService.participants ?? [];
        const allParticipantIndex = participants.findIndex(
          (obj) => obj.phoneNumber === phoneNumberWith
        );

        console.log('🔍 Index in all participants list:', allParticipantIndex);

        if (allParticipantIndex !== -1) {
          console.log('🔔 Incrementing badge text for unseen message');
          const participant = this.whatsappDataService.participants[allParticipantIndex];

          if (participant.badgeText == null) {
            participant.badgeText = 1;
          } else {
            participant.badgeText += 1;
          }
        }else {
            console.log('➕ Adding new participant to whatsappDataService.participants');

            const participantIndex = this.whatsappDataService.participants.length + 1;
            console.log(`ℹ️ Current participants count: ${this.whatsappDataService.participants.length}`);
            console.log(`ℹ️ New participant will be at index: ${participantIndex}`);

            const nameParts = messageBody.fromName?.trim().split(' ') || [];
            const firstName = nameParts[0] || '';
            const lastName = nameParts.slice(1).join(' ') || ''; // Handles multi-part last names

            console.log(`📝 Parsed firstName: "${firstName}"`);
            console.log(`📝 Parsed lastName: "${lastName}"`);
            console.log(`📞 phoneNumberWith: ${messageBody.phoneNumberWith}`);

            const participant = {
              phoneNumberId: '',
              projectID: '',
              phoneNumber: messageBody.phoneNumberWith,
              badgeText: 1,
              firstName: firstName,
              lastName: lastName,
              email: ''
            };

            this.whatsappDataService.participants.unshift(participant);
            console.log('✅ Participant added successfully');
            console.log('📊 Updated participants count:', this.whatsappDataService.participants.length);
          }
        }
 }


  ingestCallPreviewMessage(message:any)
  {
    this.previewCustomerDataService.setData(message);
  }


  ingestScheduleCallPreviewMessage(message:any)
  {
    this.previewScheduleDataService.setData(message);
  }

  ingestChat(message:any)
  {
    this.headerVariableService.messageBadgeDot = true;

    try{
      this.phoneMusicService.ringMessageMusic(message);
    }
    catch(e)
    {
      console.log(e);
      //Donot stop system to move ahead if some problem with music
    }
            
            // console.log("***********************************Received chat message***********************************");
            // console.log(JSON.parse(message.body));

            const now = new Date(); 
            if(JSON.parse(message.body).format == 'blob')
            {
              // console.log("Blob message received");
              if((this.messageListDataService.currentRecord != undefined) && (this.messageListDataService.currentRecord.extension == JSON.parse(message.body).fromExtension))
              {
                    // console.log("From Extension is equal to current message record extension");
                    let allChatLength = this.messageListDataService.allChatData.length;

                    //updating local all chat variable
                    if(allChatLength !=0)
                    {
                        // console.log("All Chat length is not zero");
                        if(this.messageListDataService.allChatData[allChatLength-1].fromExtension == JSON.parse(message.body).fromExtension)
                        {
                          // console.log("Previous message was from same extension");
                          this.messageListDataService.attachFileToPreviousRecordAllMessagesPerType(JSON.parse(message.body).message,JSON.parse(message.body).fileName,JSON.parse(message.body).blobType,JSON.parse(message.body).fileSizeInMB);
                        }
                        else{
                          // console.log("Previous message was not from same extension");
                          this.messageListDataService.attachFileToNewRecordAllMessagesPerType(JSON.parse(message.body).message,JSON.parse(message.body).fileName,JSON.parse(message.body).blobType,JSON.parse(message.body).fileSizeInMB,JSON.parse(message.body).fromExtension,JSON.parse(message.body).firstName+" "+JSON.parse(message.body).lastName,JSON.parse(message.body).role);
                        }
                    }
                    else{
                      // console.log("All Chat length is zero");
                      this.messageListDataService.attachFileToNewRecordAllMessagesPerType(JSON.parse(message.body).message,JSON.parse(message.body).fileName,JSON.parse(message.body).blobType,JSON.parse(message.body).fileSizeInMB,JSON.parse(message.body).fromExtension,JSON.parse(message.body).firstName+" "+JSON.parse(message.body).lastName,JSON.parse(message.body).role);
                    }
              }
              else{
                     this.markAndAddToParticipant(JSON.parse(message.body).fromExtension);
              }
            }
            else 
            {
              // console.log(JSON.parse(message.body).format+" message received");
               let updateMessage:any = {};
               if(JSON.parse(message.body).format == 'anchor')
                      {
                        updateMessage={
                          messageSubType:'anchor',
                          stringMessage:null,
                          anchorMessage:JSON.parse(message.body).message,
                          dateTime:now,
                        };
                      }
                else{
                        updateMessage={
                          messageSubType:'string',
                          stringMessage:JSON.parse(message.body).message,
                          anchorMessage:null,
                          dateTime:now,
                        };
                  }
                  
                // console.log(this.messageListDataService.currentRecord);  
                if((this.messageListDataService.currentRecord != undefined) && (this.messageListDataService.currentRecord.extension == JSON.parse(message.body).fromExtension))
                {
                      let allChatLength = this.messageListDataService.allChatData.length;
                  
                      //updating local all chat variable
                      if(allChatLength !=0)
                      {
                          if(this.messageListDataService.allChatData[allChatLength-1].fromExtension == JSON.parse(message.body).fromExtension)
                          {
                              this.messageListDataService.setPreviousRecordAllMessageAsPerType(updateMessage);
                          }
                          else{
                            this.messageListDataService.setNewAllMessageAsPerType(updateMessage,JSON.parse(message.body).fromExtension,JSON.parse(message.body).firstName+" "+JSON.parse(message.body).lastName,JSON.parse(message.body).role);
                          }
                      }
                      else{
                        this.messageListDataService.setNewAllMessageAsPerType(updateMessage,JSON.parse(message.body).fromExtension,JSON.parse(message.body).firstName+" "+JSON.parse(message.body).lastName,JSON.parse(message.body).role);
                      }
                
                }
                else{
                       this.markAndAddToParticipant(JSON.parse(message.body).fromExtension);
                }
            }
  }

 markAndAddToParticipant(fromExtension:String)
  {
      //Mark and add number to participant

      let allEmployeeIndex = -1;
      allEmployeeIndex = this.messageListDataService.allEmployeesData.findIndex((obj:any) => obj.extension == fromExtension);
      let participantIndex = -1;
      participantIndex = this.messageListDataService.participants.findIndex((obj:any) => obj.extension == fromExtension);
  
      if(participantIndex == -1)
        {
            let record:any = this.messageListDataService.allEmployeesData[allEmployeeIndex];
                record.lastReadIndex=0;
                record.badgeText=1;
            this.messageListDataService.participants.unshift(record);
        }
        else if (participantIndex > -1){
            let record:any = this.messageListDataService.participants[participantIndex];
            record.badgeText=record.badgeText+1;
            this.messageListDataService.participants.splice(participantIndex, 1);
            this.messageListDataService.participants.unshift(record);
        }

        // console.log("****************************************** All Participant Data ******************************************")
        // console.log(JSON.stringify(this.messageListDataService.participants));
        
  }

  callBackErrorForConference(error:any)
  {
    //console.log('connectErrorForConference : ',error);
  }

  callBackErrorForExtension(error:any)
  {
    //console.log('callBackErrorForExtension : ',error);
  }

  callBackErrorForGeneric(error:any)
  {
    //console.log('callBackErrorForGeneric : ',error);
  }

   callBackErrorForWhatsAppPhone(error:any)
  {
    console.log('callBackErrorForWhatsAppPhone : ',error);
  }

  //This error function is for stomp client not for subscription
  connectErrorForExtension(error:any)
  {
    //console.log('connectErrorForExtension : ',error);
  }

  connectErrorForWhatsAppPhone(error:any)
  {
    console.log('connectErrorForWhatsAppPhone : ',error);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    // this.stompClient.deactivate();
    if (this.stompClient !== null) {
      this.stompClient.disconnect();
  }
  }

  sendMessageToConference(confId:any,organization:string,domain:string,message:string,format:string)
  {
    // console.log("*************sendMessageToConference****************")
    // const message = `Generic message generated at ${new Date()}`;
    let sendMessage = { 
                        organization:organization,
                        conferenceId:confId,
                        fromExtension:ConstantsService.user.extension,
                        firstName:ConstantsService.user.firstName,
                        lastName:ConstantsService.user.lastName,
                        role:ConstantsService.user.role,
                        messagetype:'conference',
                        message:message,
                        format:format,
                        domain:domain,
                      };

    // console.log(JSON.stringify(sendMessage))
    this.stompClient.send('/mylinehub/sendConferenceMessage' , {}, JSON.stringify(sendMessage));
  }

  sendMessageToExtension(extension:string,organization:string,domain:string,message:string,format:string)
  {
    // console.log("*************sendMessageToExtension****************")
    // const message = `Generic message generated at ${new Date()}`;
    let sendMessage = { 
                        organization:organization,
                        extension:extension,
                        fromExtension:ConstantsService.user.extension,
                        firstName:ConstantsService.user.firstName,
                        lastName:ConstantsService.user.lastName,
                        role:ConstantsService.user.role,
                        messagetype:'chat',
                        message:message,
                        format:format,
                        domain:domain,
                      };

    // console.log(JSON.stringify(sendMessage))
    this.stompClient.send('/mylinehub/sendcalldetails' , {}, JSON.stringify(sendMessage));
  }

  sendAttachedFileToExtension(extension:string,blobMessage:string,fileName:string,blobType:string,fileSizeInMB:string)
  {
    // const message = `Generic message generated at ${new Date()}`;
    // console.log("*************sendAttachedFileToExtension****************")
    let sendMessage = { 
                        organization:ConstantsService.user.organization,
                        extension:extension,
                        fromExtension:ConstantsService.user.extension,
                        firstName:ConstantsService.user.firstName,
                        lastName:ConstantsService.user.lastName,
                        role:ConstantsService.user.role,
                        messagetype:'chat',
                        message:blobMessage,
                        format:'blob',
                        domain:ConstantsService.user.domain,
                        fileName:fileName,
	                      blobType:blobType,
	                      fileSizeInMB:fileSizeInMB,
                      };
    // console.log(JSON.stringify(sendMessage))
    this.stompClient.send('/mylinehub/sendcalldetails' , {}, JSON.stringify(sendMessage));
  }


  sendMessageToWhatsAppPhone(phoneNumberMain:string,phoneNumberWith:string,
                               previousMessageId:string,messageString:string,
                               messageType:string,whatsAppMediaId:any, blobType:any, fileName:any, fileSizeInMB:any)
  {
    console.log("*************sendMessageToWhatsAppPhone****************")
    const message = `Whats App message generated at ${new Date()}`;
    let sendMessage = { 
                        organization:ConstantsService.user.organization,
                        phoneNumberMain:phoneNumberMain,
                        phoneNumberWith:phoneNumberWith,
                        fromExtension:ConstantsService.user.extension,
                        fromName:ConstantsService.user.firstName +" "+ConstantsService.user.lastName,
                        fromTitle:ConstantsService.user.role,
                        previousMessageId:previousMessageId,
                        messageString:messageString,
                        messageType:messageType,
                        whatsAppMediaId:whatsAppMediaId,
	                      blobType:blobType,
                        fileName:fileName,
                        fileSizeInMB:fileSizeInMB,
                        outbound:true,
                        inbound:false,
                        messageOrigin:"extension"
                      };

    console.log(JSON.stringify(sendMessage))
    this.stompClient.send('/mylinehub/sendToWhatsAppPhone' , {}, JSON.stringify(sendMessage));
  }

  showDialoge(header: string,icon: string,status: string, message:string) {

    this.dialogService.open(DialogComponent, {
      context: {
        title: status,
        data: message,
        header: header,
        icon: icon
      },
    });
    }
  // sendGenericMessage(organization:any)
  // {
  //   const message = `Generic message generated at ${new Date()}`;
  //   this.stompClient.publish({ destination: '/mylinehub/sendevent', body: message });
  // }

  // sendExtensionMessage(extension:any)
  // {
  //   const message = `Extension message generated at ${new Date()}`;
  //   this.stompClient.publish({ destination: '/mylinehub/sendcalldetails', body: message });
  // }

}
