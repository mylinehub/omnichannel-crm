import { Injectable } from '@angular/core';
import { HeaderVariableService } from '../../header-variable/header-variable.service';
import { DialogComponent } from '../../../pages/employee/all-employees/dialog/dialog.component';
import { NbDialogService } from '@nebular/theme';
import { Subscriber, SubscriptionState, UserAgent } from 'sip.js';
import { MessageListDataService } from '../../../@theme/components/header/message-list/message-list-data-service/message-list-data.service';
import { ConstantsService } from '../../constants/constants.service';
import { NotifyService } from '../notify/notify.service';

@Injectable({
  providedIn: 'root'
})
export class RegisterService {

  constructor(protected headerVariableService : HeaderVariableService,
              private dialogService: NbDialogService,
              protected messageListDataService:MessageListDataService,
              protected constService : ConstantsService,
              protected notifyService : NotifyService
            ) { }

      
    // Registration
    // ============
    register(userAgent:any, lang:any) {

      //console.log("I am in register");

      if (userAgent == null) return;
      if (userAgent.registering == true) return;
      if (userAgent.isRegistered()) return;

      let RegistererRegisterOptions = {
          requestDelegate: {
              onReject: (sip:any)=>{
                  this.onRegisterFailed(userAgent,lang,sip.message.reasonPhrase, sip.message.statusCode);
                  
              }
          }
      }

      //console.log("Sending Registration...");
      userAgent.registering = true
      userAgent.registerer.register(userAgent,RegistererRegisterOptions);
      this.headerVariableService.browserPhoneTitle=lang.connected_to_web_socket;
    }

  unregister(userAgent:any, lang:any,skipUnsubscribe:any) {

      //console.log("I am in unregister");

      if (userAgent == null || !userAgent.isRegistered()) return;

      if(skipUnsubscribe == true){
        //console.log("Skipping Unsubscribe");
      } else {
        //console.log("Unsubscribing...");
        this.headerVariableService.browserPhoneTitle=lang.unsubscribing;
          try {
            this.unsubscribeAll(userAgent,lang);
          } catch (e) { }
      }

    //console.log("Unregister...");
    this.headerVariableService.browserPhoneTitle=lang.disconnecting;
      userAgent.registerer.unregister();

      userAgent.transport.attemptingReconnection = false;
      userAgent.registering = false;
      userAgent.isReRegister = false;
    }

    // Registration Events
    // ===================
    /**
    * Called when account is registered
    */
  onRegistered(userAgent:any, lang:any){
      // This code fires on re-register after session timeout
      // to ensure that events are not fired multiple times
      // a isReRegister state is kept.
      // TODO: This check appears obsolete
      //console.log("I am in onRegistered");
      userAgent.registrationCompleted = true;
      if(!userAgent.isReRegister) {
        //console.log("Registered!");

         // Start Subscribe Loop
          window.setTimeout(()=>{
              this.subscribeAll(userAgent,lang);
          }, 500);

          // Start XMPP
        // if(this.chatEngine == "XMPP") this.reconnectXmpp();

          userAgent.registering = false;

      }
      else {
          userAgent.registering = false;

        //console.log("ReRegistered!");
      }
      userAgent.isReRegister = true;
      this.headerVariableService.browserPhoneTitle=lang.registered;
      this.headerVariableService.isBrowserPhoneOnline = true;
      this.headerVariableService.isOnlineStatus = "success";

    }


    /**
    * Called if userAgent can connect, but not register.
    * @param {string} response Incoming request message
    * @param {string} cause Cause message. Unused
    **/
  onRegisterFailed(userAgent:any, lang:any,response:any, cause:any){
    //console.log("I am in onRegisterFailed");
    //console.log("Registration Failed: " + response);
    this.headerVariableService.browserPhoneTitle=lang.registration_failed;
      this.showDialoge('Registration Failed','activity-outline','danger', lang.registration_failed); 
      userAgent.registering = false;
      this.headerVariableService.browserPhoneTitle=lang.unregistered;
      this.headerVariableService.isBrowserPhoneOnline = false;
      this.headerVariableService.isOnlineStatus = "danger";
    }



  onUnregistered(userAgent:any, lang:any){

      //console.log("I am in onUnregistered");

      //console.log("Unregistered, bye! onUnregistered function");
      // We set this flag here so that the re-register attempts are fully completed.
      userAgent.isReRegister = false;
      this.headerVariableService.browserPhoneTitle=lang.unregistered;
      this.headerVariableService.isBrowserPhoneOnline = false;
      this.headerVariableService.isOnlineStatus = "danger";
    }

                    /**
    * Called when Unregister is requested
    */
    
  onInitial(userAgent:any, lang:any)
  {
      //console.log("I am in onInitial");
      //console.log("Called when register state is initial");
  }

  onTerminated(userAgent:any, lang:any)
  {
      //console.log("I am in onTerminated");
  }


       // Subscription Events
  // ===================

   // Presence / Subscribe
    // ====================
    subscribeAll(userAgent:any, lang:any) {

      //console.log("I am in subscribeAll");

      if(!userAgent.isRegistered()) return;
  
      // Start subscribe all
      if(userAgent.BlfSubs && userAgent.BlfSubs.length > 0){
                  this.unsubscribeAll(userAgent,lang);
      }
              
      userAgent.BlfSubs = [];

      if(this.headerVariableService.voiceMailSubscribe){
      this.subscribeVoicemail(userAgent,lang);
      }
      if(this.headerVariableService.subscribeToYourself){
      this.selfSubscribe(userAgent,lang);
      }

      this.messageListDataService.allEmployeesData.forEach((value:any)=>{
          this.subscribeExtension(userAgent,lang,value.extension);
        });

      // this.setExtensionSubscriptionTimeut = setTimeout(()=>{
          
      // },10000);

  }

  selfSubscribe(userAgent:any, lang:any){

      //console.log("I am in selfSubscribe");

      if(!userAgent.isRegistered()) return;
  
      if(userAgent.selfSub){
          //console.log("Unsubscribe from old self subscribe...");
          this.selfUnsubscribe(userAgent,lang);
      }
  
      let targetURI = UserAgent.makeURI("sip:"+ ConstantsService.user.extension + "@" + ConstantsService.user.domain);

  
  
      let options:any = { 
          expires: this.headerVariableService.subscribeBuddyExpires, 
          extraHeaders: ['Accept: '+ this.headerVariableService.subscribeBuddyAccept]
      }
  
      userAgent.selfSub = new Subscriber(userAgent, targetURI, this.headerVariableService.subscribeBuddyEvent, options);
      userAgent.selfSub.delegate = {
          onNotify: (sip:any)=> {
          this.notifyService.receiveNotify(userAgent,lang,sip, true);
          }
      }
      //console.log("SUBSCRIBE Self: "+ ConstantsService.user.extension +"@" + ConstantsService.user.domain);
      userAgent.selfSub.subscribe().catch((error:any)=>{
          console.warn("Error subscribing to yourself:", error);
      });
  }

  subscribeVoicemail(userAgent:any, lang:any){

      //console.log("I am in subscribeVoicemail");

      if(!userAgent.isRegistered()) return;
  
      if(userAgent.voicemailSub){
          //console.log("Unsubscribe from old voicemail Messages...");
          this.unsubscribeVoicemail(userAgent,lang);
      }
  
      let vmOptions:any = { expires : this.headerVariableService.subscribeVoicemailExpires }
      let targetURI = UserAgent.makeURI("sip:"+ ConstantsService.user.extension + "@" + ConstantsService.user.domain);
      userAgent.voicemailSub = new Subscriber(userAgent, targetURI, "message-summary", vmOptions);
      userAgent.voicemailSub.delegate = {
          onNotify: (sip:any) =>{
          this.notifyService.voicemailNotify(userAgent,lang,sip);
          }
      }
      //console.log("SUBSCRIBE VOICEMAIL: "+ ConstantsService.user.extension +"@" + ConstantsService.user.domain);
      userAgent.voicemailSub.subscribe().catch((error:any)=>{
          console.warn("Error subscribing to voicemail notifications:", error);
      });
  }
  
  selfUnsubscribe(userAgent:any, lang:any){

      //console.log("I am in selfUnsubscribe");

      if(!userAgent.isRegistered()) return;
  
      if(userAgent.selfSub){
          //console.log("Unsubscribe from yourself...", userAgent.selfSub.state);
          if(userAgent.selfSub.state == SubscriptionState.Subscribed){
          userAgent.selfSub.unsubscribe().catch((error:any)=>{
                  console.warn("Error self subscription:", error);
              });
          }
          userAgent.selfSub.dispose().catch((error:any)=>{
              console.warn("Error disposing self subscription:", error);
          });
      } else {
          //console.log("Not subscribed to Yourself");
      }
      userAgent.selfSub = null;
  }
  
  unsubscribeAll(userAgent:any, lang:any) {

      //console.log("I am in unsubscribeAll");

      if(!userAgent.isRegistered()) return;

      //console.log("Unsubscribe from voicemail Messages...");
      this.unsubscribeVoicemail(userAgent,lang);

      if(userAgent.BlfSubs && userAgent.BlfSubs.length > 0){
          //console.log("Unsubscribing "+ userAgent.BlfSubs.length + " subscriptions...");
          for (let blf = 0; blf < userAgent.BlfSubs.length; blf++) {
              this.unsubscribeBlf(userAgent,lang,userAgent.BlfSubs[blf]);
          }
          userAgent.BlfSubs = [];
      }
  }

  unsubscribeVoicemail(userAgent:any, lang:any){

      //console.log("I am in unsubscribeVoicemail");

      if(!userAgent.isRegistered()) return;
  
      if(userAgent.voicemailSub){
          //console.log("Unsubscribe to voicemail Messages...", userAgent.voicemailSub.state);
          if(userAgent.voicemailSub.state == SubscriptionState.Subscribed){
          userAgent.voicemailSub.unsubscribe().catch((error:any)=>{
                  console.warn("Error removing voicemail notifications:", error);
              });
          }
          userAgent.voicemailSub.dispose().catch((error:any)=>{
              console.warn("Error disposing voicemail notifications:", error);
          });
      } else {
          //console.log("Not subscribed to MWI");
      }
      userAgent.voicemailSub = null;
  }
  
  unsubscribeBlf(userAgent:any, lang:any,blfSubscribe:any){

      //console.log("I am in unsubscribeBlf");

      if(!userAgent.isRegistered()) return;
  
      if(blfSubscribe.state == SubscriptionState.Subscribed){
          //console.log("Unsubscribe to BLF Messages...", ConstantsService.user.extension);
          blfSubscribe.unsubscribe().catch((error:any)=>{
              console.warn("Error removing BLF notifications:", error);
          });
      } 
      else {
          //console.log("Incorrect buddy subscribe state", ConstantsService.user.extension, blfSubscribe.state);
      }
      blfSubscribe.dispose().catch((error:any)=>{
          console.warn("Error disposing BLF notifications:", error);
      });
      blfSubscribe = null;
  }

      
  

  subscribeExtension(userAgent:any, lang:any,extension:string) {

      //console.log("I am in subscribeExtension");
      if(!userAgent.isRegistered()) return;
      let targetURI = UserAgent.makeURI("sip:" + extension + "@" + ConstantsService.user.domain);

      //console.log("targetURI :",targetURI);
  
      let options = { 
          expires: this.headerVariableService.subscribeBuddyExpires, 
          extraHeaders: ['Accept: '+ this.headerVariableService.subscribeBuddyAccept]
      }

      // //console.log("options :",options);

      let blfSubscribe = new Subscriber(userAgent, targetURI, this.headerVariableService.subscribeBuddyEvent, options);
      blfSubscribe.data = extension;

      blfSubscribe.delegate = {
          onNotify: (sip:any)=> {
              this.notifyService.receiveNotify(userAgent,lang,sip, false);
          }
      }

      blfSubscribe.subscribe().catch((error:any)=>{
          // console.warn("Error subscribing to Buddy notifications:", error);
          this.showDialoge('Error SUBSCRIBING - '+extension,'activity-outline','danger', error); 
        
      });


      options = { 
        expires: this.headerVariableService.subscribeBuddyExpires, 
        extraHeaders: ['Accept: '+ this.headerVariableService.subscribeBuddyDialog]
    }

    // //console.log("options :",options);

    blfSubscribe = new Subscriber(userAgent, targetURI, this.headerVariableService.subscribeBuddyDialogEvent, options);
    blfSubscribe.data = extension;

    blfSubscribe.delegate = {
        onNotify: (sip:any)=> {
            this.notifyService.receiveNotify(userAgent,lang,sip, false);
        }
    }

    blfSubscribe.subscribe().catch((error:any)=>{
        // console.warn("Error subscribing to Buddy notifications:", error);
        this.showDialoge('Error SUBSCRIBING - '+extension,'activity-outline','danger', error); 
      
    });

      //console.log("blfSubscribe :",blfSubscribe);

      if(!userAgent.BlfSubs) userAgent.BlfSubs = [];
      userAgent.BlfSubs.push(blfSubscribe);    
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
