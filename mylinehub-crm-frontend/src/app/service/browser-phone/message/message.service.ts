import { Injectable } from '@angular/core';
import { Messager, UserAgent } from 'sip.js';
import { DialogComponent } from '../../../pages/employee/all-employees/dialog/dialog.component';
import { NotifyService } from '../notify/notify.service';
import { UtilsService } from '../utils/utils.service';
import { PhoneMusicService } from '../../phone-music/phone-music.service';
import { NbDialogService } from '@nebular/theme';
import * as moment from 'moment';

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  constructor(protected notifyService : NotifyService,
    protected utilsService : UtilsService,
    protected phoneMusicService:PhoneMusicService,
    private dialogService: NbDialogService,) { }


  sendChatMessage(userAgent:any,message:any,extension:any,domain:any) {
    if (userAgent == null) return;
    if (!userAgent.isRegistered()) return;
    var messageId = this.utilsService.uID();

    // Update Stream
    let DateTime = moment.utc().format("YYYY-MM-DD HH:mm:ss UTC");

    // SIP Messages (Note, this may not work as required)
    // ============
    let chatBuddy = UserAgent.makeURI("sip:"+ extension + "@" + domain);
    //console.log("MESSAGE: "+ chatBuddy + " (extension)");

    var MessagerMessageOptions = {
            requestDelegate : {
                onAccept: (sip:any)=>{
                    //console.log("Message Accepted:", messageId);
                    // MarkMessageSent(buddyObj, messageId, true);
                },
                onReject: (sip:any)=>{
                    console.warn("Message Error", sip.message.reasonPhrase);
                    // MarkMessageNotSent(buddyObj, messageId, true);
                    this.showDialoge('Error','activity-outline','danger', "Error while sending message with reason : "+sip.message.reasonPhrase);
                }
            },
            requestOptions : {
                extraHeaders: [],
            }
        };
  
    var messageObj = new Messager(userAgent, chatBuddy, message, "text/plain");
  
    messageObj.message(MessagerMessageOptions).then(function(){
            // Custom Web hook
            // if(typeof web_hook_on_message !== 'undefined') web_hook_on_message(messageObj);
        });
}

receiveOutOfDialogMessage(userAgent:any,message:any,lang:any,didLength:any) {
  
//   //console.log("I am in receiveOutOfDialogMessage");
  let callerID = message.request.from.displayName;
  let did = message.request.from.uri.normal.user;

//   //console.log("callerID : ",callerID);
//   //console.log("did : ",did);
//   //console.log("*************************************message start***************************************");
//   //console.log(JSON.stringify(message));
//   //console.log("*************************************message end***************************************");

  // Out of dialog Message Receiver
  let messageType = (message.request.headers["Content-Type"].length >=1)? message.request.headers["Content-Type"][0].parsed : "Unknown" ;
  // Text Messages
  if(messageType.indexOf("text/plain") > -1){
      // Plain Text Messages SIP SIMPLE
    //console.log("New Incoming Message!", "\""+ callerID +"\" <"+ did +">");

      if(did.length > didLength) {
          // Contacts cannot receive Test Messages, because they cannot reply
          // This may change with FAX, Email, WhatsApp etc
        // console.warn("DID length greater then extensions length")
          return;
      }

      let CurrentCalls = this.utilsService.countSessions(userAgent,"0");
      let originalMessage = message.request.body;
      let messageId = this.utilsService.uID();
      let DateTime = this.utilsService.utcDateNow();

      message.accept();

      //Write code to show text messages on header
      this.showMessageOnHeader(messageId, "MSG", originalMessage, DateTime);
      //dummy call for some extra work
      this.refreshMessages("","");
      this.phoneMusicService.ringMessageMusic(originalMessage);
  }
  // Message Summary
  else if(messageType.indexOf("application/simple-message-summary") > -1){
    console.warn("This message-summary is unsolicited (out-of-dialog). Consider using the SUBSCRIBE method.")
      this.notifyService.voicemailNotify(userAgent,lang,message);
  }
  else{
    console.warn("Unknown Out Of Dialog Message Type: ", messageType);
      message.reject();
  }

}

showMessageOnHeader(messageId:any, type:any, message:any, DateTime:any){
         //Write code to show text messages on header
         //console.log("I am in showMessageOnHeader");
     }
  
refreshMessages(Obj:any, filter:any) {

    //console.log("I am in refreshMessages");

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

}
