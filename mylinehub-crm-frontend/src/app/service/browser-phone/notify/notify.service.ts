import { Injectable } from '@angular/core';
import { DialogComponent } from '../../../pages/employee/all-employees/dialog/dialog.component';
import { HeaderVariableService } from '../../header-variable/header-variable.service';
import { NbDialogService } from '@nebular/theme';
import { MessageListDataService } from '../../../@theme/components/header/message-list/message-list-data-service/message-list-data.service';
import { ConstantsService } from '../../constants/constants.service';
import { XMLParser } from 'fast-xml-parser';

@Injectable({
  providedIn: 'root'
})
export class NotifyService {

  constructor(protected headerVariableService : HeaderVariableService,
    private dialogService: NbDialogService,
    protected messageListDataService:MessageListDataService,
    protected constService : ConstantsService,
  ) { }

    // Subscription Events
    // ===================

    voicemailNotify(userAgent:any, lang:any,notification:any){

    //   console.log("I am in voicemailNotify");

      // Messages-Waiting: yes        <-- yes/no
      // Voice-Message: 1/0           <-- new/old
      // Voice-Message: 1/0 (0/0)     <-- new/old (ugent new/old)
      if(notification.request.body.indexOf("Messages-Waiting:") > -1){
          notification.accept();
  
          let messagesWaiting = (notification.request.body.indexOf("Messages-Waiting: yes") > -1)
          let newVoiceMessages = 0;
          let oldVoiceMessages = 0;
          let ugentNewVoiceMessage = 0;
          let ugentOldVoiceMessage = 0;
  
          if(messagesWaiting){
            //   console.log("Messages Waiting!");
              let lines = notification.request.body.split("\r\n");
              for(let l=0; l<lines.length; l++){
                  if(lines[l].indexOf("Voice-Message: ") > -1){
                      let value = lines[l].replace("Voice-Message: ", ""); // 1/0 (0/0)
                      if(value.indexOf(" (") > -1){
                          // With Ugent options
                          newVoiceMessages = parseInt(value.split(" (")[0].split("\/")[0]);
                          oldVoiceMessages = parseInt(value.split(" (")[0].split("\/")[1]);
                          ugentNewVoiceMessage = parseInt(value.split(" (")[1].replace(")","").split("\/")[0]);
                          ugentOldVoiceMessage = parseInt(value.split(" (")[1].replace(")","").split("\/")[1]);
                      } else {
                          // Without
                          newVoiceMessages = parseInt(value.split("\/")[0]);
                          oldVoiceMessages = parseInt(value.split("\/")[1]);
                      }
                  }
              }
            //   console.log("Voicemail: ", newVoiceMessages, oldVoiceMessages, ugentNewVoiceMessage, ugentOldVoiceMessage);
  
              // Show the messages waiting bubble
              //   $("#TxtVoiceMessages").html(""+ newVoiceMessages)
              //   $("#TxtVoiceMessages").show();
              // ******ADD************
              // Show message / voiceMessage on messages header
  
              // Show a system notification
              // if(newVoiceMessages > userAgent.lastVoicemailCount){
              //   userAgent.lastVoicemailCount = newVoiceMessages;
              //     if ("Notification" in window) {
              //         if (Notification.permission === "granted") {
  
              //             let noticeOptions = { 
              //                 body: this.lang.you_have_new_voice_mail.replace("{0}", newVoiceMessages)
              //             }
  
              //             let vmNotification = new Notification(this.lang.new_voice_mail, noticeOptions);
              //             vmNotification.onclick = (event)=> {
              //                 if(this.headerVariableService.voicemailDid != ""){
              //                   this.dialLine("audio", null, this.headerVariableService.voicemailDid, this.lang.voice_mail,'');
              //                 }
              //             }
              //         }
              //     }
              // }
  
          } else {
              // Hide the messages waiting bubble
          //   $("#TxtVoiceMessages").html("0")
          //   $("#TxtVoiceMessages").hide();
          }
  
      }
      else {
          // Doesn't seem to be an message notification https://datatracker.ietf.org/doc/html/rfc3842
          notification.reject();
      }
  }

  receiveNotify(userAgent:any, lang:any,notification:any, selfSubscribe:any) {

    // console.log("I am in receiveNotify");
  
    if (userAgent == null || !userAgent.isRegistered()) return;
  
    notification.accept();
  
    // console.log("Notification : ",notification);
  
    let extension = "";
    
    let ContentType = notification.request.headers["Content-Type"][0].parsed;
    // console.log("ContentType : ",ContentType);
  
    const options = {
        ignoreAttributes: false,
        attributeNamePrefix: '@_', // you have assign this so use this to access the attribute
        // arrayMode: "strict",
        // arrayMode: tagName => tagName === 'presence.tuple',
      };
  
    let parser = new XMLParser(options);
    let xml:any = parser.parse(notification.request.body);
    // const builder = new XMLBuilder();
    // const xmlContent = builder.build(xml);
  
    // console.log("notification.request.body");
    // console.log(notification.request.body);
    // console.log("xml");
    // console.log(xml);
  
  
  
    // console.log("Self Subscribe : ",selfSubscribe);
  
    if (ContentType == "application/pidf+xml") {
        // Handle Presence
  
        /*
        open: In the context of INSTANT MESSAGES, this value means that the
            associated <contact> element, if any, corresponds to an INSTANT
            INBOX that is ready to accept an INSTANT MESSAGE.
        
        closed: In the context of INSTANT MESSAGES, this value means that
            the associated <contact> element, if any, corresponds to an
       
        INSTANT INBOX that is unable to accept an INSTANT MESSAGE.
       */
  
        // The value of the 'entity' attribute is the 'pres' URL of the PRESENT publishing this presence document.
        // (In some cases this can present as the user... what if using DIDs)
        let observedUser = xml.presence["@_entity"];
        extension = observedUser.split("@")[0].split(":")[1];
        // buddy = xml.find("presence").find("tuple").attr("id"); // Asterisk does this, but its not correct.
        // buddy = notification.request.from.uri.user; // Unreliable 
  
        // console.log("observedUser : ",observedUser);
        // console.log("extension : ",extension);
  
        if(selfSubscribe){
  
            if(extension == ConstantsService.user.extension){               
            let availability = "closed";
            availability =  xml.presence.note; 
            if (availability == "open") 
                {
                    this.headerVariableService.selfPresence =  "success";
                    this.headerVariableService.dotClass = "dotOnline";
                }
            if (availability == "closed") 
                {
                    this.headerVariableService.selfPresence =  "danger";
                    this.headerVariableService.dotClass = "dotOffline";
                }
  
            }
            else {
                // console.warn("Self Subscribe Notify, but wrong user returned.", extension, ConstantsService.user.domain);
                this.showDialoge('Error','activity-outline','danger', "Self subscription is pointing to wrong extension. Kindly Log off and log in again."); 
            }
        }
        else
        {
            // availability = xml.find("presence").find("tuple").find("status").find("basic").text();
            let tuples = JSON.parse(JSON.stringify(xml.presence.tuple));
            // console.log("tuples : ",tuples);
            // console.log(typeof tuples); 
  
            if(typeof tuples != 'object'){
                tuples.forEach((obj:any,i:any)=>{
  
                    // console.log("i : ",i);
                    // console.log("obj : ",obj);
  
                    let currentAvailability = obj.status.basic;
                    let currentExtension = obj["@_id"];
  
                    let allEmployeeIndex = -1;
                    allEmployeeIndex = this.messageListDataService.allEmployeesData.findIndex((obj:any) => obj.extension == currentExtension);
                    // console.log("Index application/pidf+xml tuple not object: ",allEmployeeIndex);
                    if(allEmployeeIndex != -1)
                    { 
                        // console.log("User found having index : ",allEmployeeIndex);
  
                        if(currentAvailability == "open") {
                            this.messageListDataService.allEmployeesData[allEmployeeIndex].presence = "success";
                            this.messageListDataService.allEmployeesData[allEmployeeIndex].dotClass = "dotOnline";
                            
                            if(this.messageListDataService.currentRecord != null)
                                {
                                    if(this.messageListDataService.currentRecord.extension == this.messageListDataService.allEmployeesData[allEmployeeIndex].extension)
                                        {
                                            this.messageListDataService.currentRecord.presence = "success";
                                            this.messageListDataService.currentRecord.dotClass = "dotOnline";
                                        }
                                }
  
                            try{
                                let participantIndex = -1;
                                participantIndex = this.messageListDataService.participants.findIndex(obj => obj.extension == this.messageListDataService.allEmployeesData[allEmployeeIndex].extension);
                                if(participantIndex != -1)
                                    { 
                                        this.messageListDataService.participants[participantIndex].dotClass = "dotOnline";
                                        this.messageListDataService.participants[participantIndex].presence = "success";
                                    }
                            }
                            catch(e)
                            {
                                console.log(e);
                            }
                        }
                        else if (currentAvailability == "closed"){
                            this.messageListDataService.allEmployeesData[allEmployeeIndex].presence = "danger";
                            this.messageListDataService.allEmployeesData[allEmployeeIndex].dotClass = "dotOffline";
  
                            if(this.messageListDataService.currentRecord != null)
                                {
                                    if(this.messageListDataService.currentRecord.extension == this.messageListDataService.allEmployeesData[allEmployeeIndex].extension)
                                        {
                                            this.messageListDataService.currentRecord.presence = "danger";
                                            this.messageListDataService.currentRecord.dotClass = "dotOffline";
                                        }
                                }
                                
                                try{
                                    let participantIndex = -1;
                                    participantIndex = this.messageListDataService.participants.findIndex(obj => obj.extension == this.messageListDataService.allEmployeesData[allEmployeeIndex].extension);
                                    if(participantIndex != -1)
                                        { 
                                            this.messageListDataService.participants[participantIndex].dotClass = "dotOffline";
                                            this.messageListDataService.participants[participantIndex].presence = "danger";
                                        }
                                }
                                catch(e)
                                {
                                    console.log(e);
                                }
                        }
  
                    }
                    else{
                            //Do Nothing
                            // this.showDialoge('Error','activity-outline','danger', "Take a screenshot."+currentExtension+" was not found. Contact Admin.");
                    }
  
                });
            }   
            else{
  
                // So if any of the contacts are open, then say open
                let currentAvailability = tuples.status.basic;
                let currentExtension = tuples["@_id"];
  
                let allEmployeeIndex = -1;
                allEmployeeIndex = this.messageListDataService.allEmployeesData.findIndex((obj:any) => obj.extension == currentExtension);
                // console.log("Index application/pidf+xml tuple object: ",allEmployeeIndex);
                if(allEmployeeIndex != -1)
                { 
                    // console.log("User found having index : ",allEmployeeIndex);
  
                    if(currentAvailability == "open") {
  
                        // console.log("Open");
  
                        this.messageListDataService.allEmployeesData[allEmployeeIndex].presence = "success";
                        this.messageListDataService.allEmployeesData[allEmployeeIndex].dotClass = "dotOnline"
                        if(this.messageListDataService.currentRecord != null)
                            {
                                // console.log("Current record is not null");
                                if(this.messageListDataService.currentRecord.extension == this.messageListDataService.allEmployeesData[allEmployeeIndex].extension)
                                    {
                                        // console.log("Setting current record");
                                        this.messageListDataService.currentRecord.presence = "success";
                                        this.messageListDataService.currentRecord.dotClass = "dotOnline"
                                    }
                            }
  
                        // console.log("Open");
  
                        try{
                            let participantIndex = -1;
                            participantIndex = this.messageListDataService.participants.findIndex(obj => obj.extension == this.messageListDataService.allEmployeesData[allEmployeeIndex].extension);
                            if(participantIndex != -1)
                                { 
                                    this.messageListDataService.participants[participantIndex].dotClass = "dotOnline";
                                    this.messageListDataService.participants[participantIndex].presence = "success";
                                }
                        }
                        catch(e)
                        {
                            console.log(e);
                        }
  
                    }
                    else if (currentAvailability == "closed"){
  
                        this.messageListDataService.allEmployeesData[allEmployeeIndex].presence = "danger";
                        this.messageListDataService.allEmployeesData[allEmployeeIndex].dotClass = "dotOffline"

                        if(this.messageListDataService.currentRecord != null)
                            {
                                if(this.messageListDataService.currentRecord.extension == this.messageListDataService.allEmployeesData[allEmployeeIndex].extension)
                                    {
                                        this.messageListDataService.currentRecord.presence = "danger";
                                        this.messageListDataService.currentRecord.dotClass = "dotOffline"
                                    }
                            }
  
                            try{
                                let participantIndex = -1;
                                participantIndex = this.messageListDataService.participants.findIndex(obj => obj.extension == this.messageListDataService.allEmployeesData[allEmployeeIndex].extension);
                                if(participantIndex != -1)
                                    { 
                                        this.messageListDataService.participants[participantIndex].dotClass = "dotOffline";
                                        this.messageListDataService.participants[participantIndex].presence = "danger";
                                    }
                            }
                            catch(e)
                            {
                                console.log(e);
                            }
  
                    }
  
                }
                else{
                        //Do Nothing
                        // this.showDialoge('Error','activity-outline','danger', "Take a screenshot."+currentExtension+" was not found. Contact Admin."); 
            
                }
  
            }
        }
    }
    else if (ContentType == "application/dialog-info+xml") {
    // Handle "Dialog" State
    if(selfSubscribe){
            let state = xml["dialog-info"].dialog.state;
  
            // console.log("For self state : ",state);
  
            if(extension == ConstantsService.user.extension){
                //console.log("Self Notify:", this.headerVariableService.selfPresence);
                this.headerVariableService.selfState = state;
                       
                // dotOnline | dotOffline | dotRinging | dotInUse | dotReady | dotOnHold
                // if (this.headerVariableService.selfState == "Unavailable") this.headerVariableService.dotClass = "dotOffline";
  
                if (this.headerVariableService.selfState == "terminated") this.headerVariableService.dotClass = "dotOnline";
                if (this.headerVariableService.selfState == "trying") this.headerVariableService.dotClass = "dotInUse";
                if (this.headerVariableService.selfState == "proceeding") this.headerVariableService.dotClass = "dotInUse";
                if (this.headerVariableService.selfState == "confirmed") this.headerVariableService.dotClass = "dotInUse";
                if (this.headerVariableService.selfState == "early") this.headerVariableService.dotClass = "dotInUse";
                if (this.headerVariableService.selfState == "on-hold") this.headerVariableService.dotClass = "dotOnHold";
    
    
            }
            else {
                // console.warn("Self Subscribe Notify, but wrong user returned.", extension, ConstantsService.user.domain);
                this.showDialoge('Error','activity-outline','danger', "Self subscription is pointing to wrong extension. Kindly Log off and log in again."); 
            }
            return;
        }
        else{
  
            let observedUser = xml["dialog-info"]["@_entity"];
            let extension = observedUser.split("@")[0].split(":")[1];
  
            // console.log("observedUser : ",observedUser);
            // console.log("extension : ",extension);
  
            // let version = xml.find("dialog-info").attr("version"); // 1|2|etc
            // let DialogState = xml.find("dialog-info").attr("state"); // full|partial
            // let extId = xml.find("dialog-info").find("dialog").attr("id"); // qoe2vr886cbn1ccj3h.0
            let state = xml["dialog-info"].dialog.state;
             // The dialog states only report devices states, and cant say online or offline.
  
            // console.log("state : ",state);
  
            let allEmployeeIndex = -1;
            allEmployeeIndex = this.messageListDataService.allEmployeesData.findIndex(obj => obj.extension == extension);
            // console.log("Employee Index application/dialog-info+xml: ",allEmployeeIndex);
            if(allEmployeeIndex != -1)
            {
                // state = "terminated"   =  "Ready";
                // state == "trying"  =  "On the phone";
                // state == "proceeding" = "On the phone";
                // state == "early" =  "Ringing";
                // state == "confirmed" =  "On the phone";  
                // console.log("User found having index : ",allEmployeeIndex);
                this.messageListDataService.allEmployeesData[allEmployeeIndex].state = state;
  
                if (this.messageListDataService.allEmployeesData[allEmployeeIndex].state == "terminated") this.messageListDataService.allEmployeesData[allEmployeeIndex].dotClass = "dotOnline";
                if (this.messageListDataService.allEmployeesData[allEmployeeIndex].state == "trying") this.messageListDataService.allEmployeesData[allEmployeeIndex].dotClass = "dotInUse";
                if (this.messageListDataService.allEmployeesData[allEmployeeIndex].state == "proceeding") this.messageListDataService.allEmployeesData[allEmployeeIndex].dotClass = "dotInUse";
                if (this.messageListDataService.allEmployeesData[allEmployeeIndex].state == "confirmed") this.messageListDataService.allEmployeesData[allEmployeeIndex].dotClass = "dotInUse";
                if (this.messageListDataService.allEmployeesData[allEmployeeIndex].state == "early") this.messageListDataService.allEmployeesData[allEmployeeIndex].dotClass = "dotInUse";
                if (this.messageListDataService.allEmployeesData[allEmployeeIndex].state == "on-hold") this.messageListDataService.allEmployeesData[allEmployeeIndex].dotClass = "dotOnHold";
  
                try{
                       if(this.messageListDataService.currentRecord != null)
                        {
                            if(this.messageListDataService.currentRecord.extension == this.messageListDataService.allEmployeesData[allEmployeeIndex].extension)
                                {
                                    this.messageListDataService.currentRecord.state = state;
                                    this.messageListDataService.currentRecord.dotClass = this.messageListDataService.allEmployeesData[allEmployeeIndex].dotClass;
                                }
                        }
  
                        let participantIndex = -1;
                        participantIndex = this.messageListDataService.participants.findIndex(obj => obj.extension == this.messageListDataService.allEmployeesData[allEmployeeIndex].extension);
                        // console.log("Participant Index : ",participantIndex);
                        if(participantIndex != -1)
                            { 
                                this.messageListDataService.participants[participantIndex].state = state;
                                this.messageListDataService.participants[participantIndex].dotClass = this.messageListDataService.allEmployeesData[allEmployeeIndex].dotClass;
                            }
                }
                catch(e)
                {
                        console.log(e);
                }
  
            }
            else{
                    //Do Nothing
                    console.log("User not found, doing nothing");
                    // this.showDialoge('Error','activity-outline','danger', "Take a screenshot."+extension+" was not found. Contact Admin.");
            }
  
        }
      } 
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
