import { Injectable } from '@angular/core';
import { HeaderVariableService } from '../../header-variable/header-variable.service';
import { UtilsService } from '../utils/utils.service';
import { SessionService } from '../session/session.service';
import { RecordingService } from '../recording/recording.service';
import { PhoneMusicService } from '../../phone-music/phone-music.service';
import { DialogComponent } from '../../../pages/employee/all-employees/dialog/dialog.component';
import { NbDialogRef, NbDialogService } from '@nebular/theme';
import * as moment from 'moment';

@Injectable({
  providedIn: 'root'
})
export class InviteService {
  showDialogeStatusRef: NbDialogRef<DialogComponent>;

  constructor(protected headerVariableService : HeaderVariableService,
              protected utilsService : UtilsService,
              protected sessionService:SessionService,
              protected recordingService:RecordingService,
              protected phoneMusicService:PhoneMusicService,
              private dialogService: NbDialogService,
            ) { }

        // Session Events
        // ==============
        // Incoming INVITE
        onInviteCancel(lang:any,session:any, response:any){
          //console.log("I am in onInviteCancel");
            // Remote Party Canceled while ringing...
  
            // Check to see if this call has been completed elsewhere
            // https://github.com/InnovateAsterisk/Browser-Phone/issues/405
            let temp_cause = 0;
            let reason = response.headers["Reason"];
  
            //console.log("Invite cancel reason : ", reason);
  
            if (reason !== undefined && reason.length > 0){
                for (let i = 0; i < reason.length; i++){
                    let cause = reason[i].raw.toLowerCase().trim(); // Reason: Q.850 ;cause=16 ;text="Terminated"
                    let items = cause.split(';');
                    if (items.length >= 2 && (items[0].trim() == "sip" || items[0].trim() == "q.850") && items[1].includes("cause") && cause.includes("call completed elsewhere")){
                        temp_cause = parseInt(items[1].substring(items[1].indexOf("=")+1).trim());
                        // No sample provided for "token"
                        //console.log("Changing temp case to what it should be  : ",temp_cause);
                        break;
                    }
                }
            }
  
            session.data.terminateby = "them";
            session.data.reasonCode = temp_cause;
  
            //console.log("Invite cancel reason is set into session via temp cause : ",temp_cause);
  
            if(temp_cause == 0){
                session.data.reasonText = "Call Cancelled";
                //console.log("Call canceled by remote party before answer");
            } else {
                session.data.reasonText = "Call completed elsewhere";
                //console.log("Call completed elsewhere before answer");
            }
  
            session.dispose().catch((error:any)=>{
                //console.log("Failed to dispose the cancel dialog", error);
            })
  
            this.sessionService.teardownSession(lang,session);
        }
  
  
        // Both Incoming and outgoing INVITE
        onInviteAccepted(sessionData:any, includeVideo:any, response:any){
        //console.log("I am in onInviteAccepted");
  
        
        this.headerVariableService.callInProgress = true;
        this.headerVariableService.callStatus = "success";
        this.sessionService.currentConnectedSessionData = sessionData;
        this.headerVariableService.isVideoCall = includeVideo;
  
        // Call in progress
        let session = sessionData;
  
        //console.log("Pausing music if not yet paused");
  
        if(session.data.earlyMedia){
            session.data.earlyMedia.pause();
            session.data.earlyMedia.removeAttribute('src');
            session.data.earlyMedia.load();
            session.data.earlyMedia = null;
        }
  
        window.clearInterval(session.data.callTimer);
      //   window.clearInterval(this.headerVariableService.timer);
  
        //console.log("Call has started , setting initial value for session.data.callTimer variable : ",session.data.callTimer);
  
        let startTime = moment.utc();
        session.data.startTime = startTime;
  
        session.data.callTimer = window.setInterval(()=>{
            let now = moment.utc();
            let duration = moment.duration(now.diff(startTime));
            let timeStr = this.utilsService.formatShortDuration(duration.asSeconds());
            this.headerVariableService.timer = timeStr;
            //console.log("Call Timer as function is called : ",now, duration, timeStr);
        }, 1000);
  
  
        session.isOnHold = false;
        session.data.started = true;
  
        //console.log("Session data started inside Accept");
  
        if(includeVideo){
            //console.log("Viedo is included in this scenario");
            // Preview our stream from peer connection
            let localVideoStream = new MediaStream();
            let pc = session.sessionDescriptionHandler.peerConnection;
            pc.getSenders().forEach((sender:any) =>{
                if(sender.track && sender.track.kind == "video"){
                    localVideoStream.addTrack(sender.track);
                }
            });
  
           let localVideo = this.headerVariableService.localVideo;
            localVideo.srcObject = localVideoStream;
            localVideo.onloadedmetadata = (e:any)=> {
                localVideo.play();
            }
  
            // Apply Call Bandwidth Limits
            if(this.headerVariableService.maxVideoBandwidth > -1){
                pc.getSenders().forEach((sender:any)=> {
                    if(sender.track && sender.track.kind == "video"){
  
                        let parameters = sender.getParameters();
                        if(!parameters.encodings) parameters.encodings = [{}];
                        parameters.encodings[0].maxBitrate =  0 //'this.maxVideoBandwidth' * 1000;
  
                        //console.log("Applying limit for Bandwidth to: ", this.headerVariableService.maxVideoBandwidth + "kb per second")
  
                        // Only going to try without re-negotiations
                        sender.setParameters(parameters).catch((e:any)=>{
                            console.warn("Cannot apply Bandwidth Limits", e);
                        });
  
                    }
                });
            }
        }
  
        // Start Call Recording
        if(this.headerVariableService.recordAllCalls) {
          //console.log("Record all call is true , hence starting call recording");
          this.recordingService.startRecording(session);
        }
      }


        // Outgoing INVITE
        onInviteTrying(lang:any,sessionData:any, response:any){
          //console.log("I am in onInviteTrying");
          this.headerVariableService.browserPhoneTitle = lang.trying;
        }
        
        onInviteProgress(userAgent,lang:any,sessionData:any, response:any,dialledNumber:any,didLength){
    
          //console.log("I am in onInviteProgress");
          //console.log("Call Progress:", response.message.statusCode);
          
          // Provisional 1xx
          // response.message.reasonPhrase
          if(response.message.statusCode == 180){
            this.headerVariableService.browserPhoneTitle = lang.ringing;
             this.phoneMusicService.ringEarlyMedia(sessionData);
          }
          else if(response.message.statusCode === 183){
            // console.error("response.message code 183 : ",response.message);
            if(this.showDialogeStatusRef == null)
              {
                if(response.message.reasonPhrase == "Session Progress" && !(this.headerVariableService.callRinging))
                {

                }
                else if (response.message.reasonPhrase != "Session Progress")
                {
                  this.showDialoge('Error','activity-outline','danger', response.message.reasonPhrase + "...");
                }
                
              }
              else{
                if(response.message.reasonPhrase == "Session Progress")
                  {

                  }
                else
                {
                  this.showDialogeStatusRef.close();
                  this.showDialoge('Error','activity-outline','danger', response.message.reasonPhrase + "...");
                }
              }
                                                    
              // Add UI to allow DTMF
            //   $("#line-" + lineObj.LineNumber + "-early-dtmf").show();
          }
          else {
              // 181 = Call is Being Forwarded
              // 182 = Call is queued (Busy server!)
              // 199 = Call is Terminated (Early Dialog)
              console.error("response.message no code: ",response.message);
              if(this.showDialogeStatusRef == null)
                {
                  this.showDialoge('Error','activity-outline','danger', response.message.reasonPhrase + "..."); 
                }
                else{
                  this.showDialogeStatusRef.close();
                  this.showDialoge('Error','activity-outline','danger', response.message.reasonPhrase + "..."); 
                }
            
          }

          let CurrentCalls = this.utilsService.countSessions(userAgent,sessionData.id);
          //console.log("Current Call Count:", CurrentCalls);
      
          let callerID = sessionData.remoteIdentity.displayName;
          let did = sessionData.remoteIdentity.uri.user;
          if (typeof callerID === 'undefined') callerID = did;

          this.headerVariableService.callRinging = true;
          this.sessionService.outgoingCallSessionData = sessionData;
          this.headerVariableService.callType= "Outgoing-Call";
          this.headerVariableService.callerName = "";
          this.headerVariableService.phoneNumber = dialledNumber;
          this.headerVariableService.isVideoCall = sessionData.data.withvideo;
          this.headerVariableService.isInternal = (Number(did)==didLength);

          let startTime = moment.utc();
          sessionData.data = {}
          sessionData.data.calldirection = "outbound";
          sessionData.data.terminateby = "";
          sessionData.data.src = did;
          sessionData.data.callstart = startTime.format("YYYY-MM-DD HH:mm:ss UTC");
          let now = moment.utc();
          let duration = moment.duration(now.diff(startTime));
          let timeStr = this.utilsService.formatShortDuration(duration.asSeconds());
          sessionData.data.callTimer = timeStr;
          this.headerVariableService.timer = timeStr;

          // Possible Early Rejection options
          if(this.headerVariableService.doNotDisturb == true) {
            //console.log("Do Not Disturb Enabled, rejecting call.");
            sessionData.data.earlyReject = true;
            this.sessionService.endSession(lang,sessionData);
            return;
        }

        if(CurrentCalls >= 1){
            if(this.headerVariableService.callWaiting == false ){
                //console.log("Call Waiting Disabled, rejecting call.");
                sessionData.data.earlyReject = true;
                this.sessionService.endSession(lang,sessionData);
                return;
            }
        }

        }
    
    
        onInviteRejected(lang:any,sessionData:any, response:any){
          //console.log("I am in onInviteRejected");
          //console.log("INVITE Rejected:", response.message.reasonPhrase);
        
          sessionData.data.terminateby = "them";
          sessionData.data.reasonCode = response.message.statusCode;
          sessionData.data.reasonText = response.message.reasonPhrase;
        
          this.sessionService.teardownSession(lang,sessionData);
        }
        
        onInviteRedirected(lang:any,sessionData:any,response:any){
          //console.log("I am in onInviteRedirected");
          //console.log("onInviteRedirected", response);
          // Follow???
        }
    

        showDialoge(header: string,icon: string,status: string, message:string) {

          this.showDialogeStatusRef = this.dialogService.open(DialogComponent, {
            context: {
              title: status,
              data: message,
              header: header,
              icon: icon
            },
          });
  
         this.showDialogeStatusRef.onClose.subscribe((value) => {
          //console.log("Upload dialog is closed");
          //console.log("Value : ",value);
          this.showDialogeStatusRef = null;
         });

          }

}
