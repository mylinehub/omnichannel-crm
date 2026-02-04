import { Injectable } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { HeaderVariableService } from '../../header-variable/header-variable.service';
import { SessionService } from '../session/session.service';
import { DialogComponent } from '../../../pages/employee/all-employees/dialog/dialog.component';
import { SessionState } from 'sip.js';
import * as moment from 'moment';
import { InviteService } from '../invite/invite.service';
import { UtilsService } from '../utils/utils.service';
import { PhoneMusicService } from '../../phone-music/phone-music.service';
import { VideoDialogDataService } from '../../../@theme/components/header/video-dialog/video-dialog-data-service/video-dialog-data.service';

@Injectable({
  providedIn: 'root'
})
export class AnswerService {

  constructor(private dialogService: NbDialogService,
    protected headerVariableService : HeaderVariableService,
    protected sessionService:SessionService,
    protected inviteService:InviteService,
    protected utilsService : UtilsService,
    protected phoneMusicService:PhoneMusicService,
    protected videoDialogDataService:VideoDialogDataService,
  ) { }

      // Inbound Calls
    // =============
    answeraudioCall(lang:any,sessionData:any) {

      //console.log("I am in answeraudioCall");

      // CloseWindow();
      let session = sessionData;

      //console.log("Got session data : ",session);

      // Stop the ringtone
      if(session.data.ringerObj){
          session.data.ringerObj.pause();
          session.data.ringerObj.removeAttribute('src');
          session.data.ringerObj.load();
          session.data.ringerObj = null;
      }

      //console.log("Stopped Ringing");

      // Check vitals
      if(this.headerVariableService.hasAudioDevice == false){

        //console.log("System does not have audio device");

        this.showDialoge('Error','activity-outline','danger', "No microphone found to receive incomming."); 
        this.headerVariableService.callInProgress = false;
        this.headerVariableService.callStatus = 'danger';
        this.headerVariableService.callRinging = false;
        //we should tear down session here
        this.sessionService.endSession(lang,sessionData);
         return;
      }

      //console.log("System has audio device");
    
      // Start SIP handling
      let supportedConstraints = navigator.mediaDevices.getSupportedConstraints();

      //console.log("supportedConstraints : ",supportedConstraints);

      let spdOptions:any = {
          sessionDescriptionHandlerOptions: {
              constraints: {
                  audio: { deviceId : {}
                            },
                  video: false
              }
          }
      }
    
      //console.log("Empty spdOptions created");

      // Configure Audio
      let currentAudioDevice = this.headerVariableService.currentMicStringValue;
      if(currentAudioDevice != "default"){
          let confirmedAudioDevice = false;
          for (let i = 0; i < this.headerVariableService.micList.length; ++i) {
              if(currentAudioDevice == this.headerVariableService.micList[i]) {
                  confirmedAudioDevice = true;
                  break;
              }
          }
          if(confirmedAudioDevice) {
              spdOptions.sessionDescriptionHandlerOptions.constraints.audio.deviceId = { exact: currentAudioDevice };
              //console.log("Audio added to spdOptions.sessionDescriptionHandlerOptions.constraints.audio.deviceId");
          }
          else {
            
              console.warn("The audio device you used before is no longer available, default settings applied.");
              this.showDialoge('Error','activity-outline','danger', "The audio device you used before is no longer available, default settings applied."); 
              this.headerVariableService.callInProgress = false;
              this.headerVariableService.callStatus = 'danger';
              this.headerVariableService.callRinging = false;
              this.sessionService.endSession(lang,sessionData);
               return;
          }
      }
      // Add additional Constraints
      if(supportedConstraints.autoGainControl) {
          spdOptions.sessionDescriptionHandlerOptions.constraints.audio.autoGainControl = this.headerVariableService.autoGainControl;
      }
      if(supportedConstraints.echoCancellation) {
          spdOptions.sessionDescriptionHandlerOptions.constraints.audio.echoCancellation = this.headerVariableService.echoCancellation;
      }
      if(supportedConstraints.noiseSuppression) {
          spdOptions.sessionDescriptionHandlerOptions.constraints.audio.noiseSuppression = this.headerVariableService.noiseSeperation;
      }
    
      //console.log("As per supported constraints added autoGainControl,echoCancellation,noiseSuppression to spdOptions.sessionDescriptionHandlerOptions.constraints.audio");

      // Save Devices
      session.data.withvideo = false;
      session.data.VideoSourceDevice = null;
      session.data.AudioSourceDevice = this.headerVariableService.currentMicStringValue
      session.data.AudioOutputDevice = this.headerVariableService.currentSpeakerStringValue;
    
      //console.log("Added video , audio details");

      //console.log("Accepting call");
      // Send Answer
      session.accept(spdOptions).then(()=>
      {
        //console.log("After accepting call, Calling now onInviteAccepted");
        this.inviteService.onInviteAccepted(session,false,"");
      }).catch((error:any)=>
      {
          //console.log("Got error while receiving audio call. Please find below error details");
          console.warn("Failed to answer call", error, session);
          this.showDialoge('Failed to answer call','activity-outline','danger', error); 
          session.data.reasonCode = 500;
          session.data.reasonText = "Client Error";
          this.sessionService.teardownSession(lang,session);
      });
    }
    
    answervideoCall(lang:any,sessionData:any) {
      //console.log("I am in answervideoCall");
      // Stop the ringtone
      if(sessionData.data.ringerObj){
        sessionData.data.ringerObj.pause();
        sessionData.data.ringerObj.removeAttribute('src');
        sessionData.data.ringerObj.load();
        sessionData.data.ringerObj = null;
      }

      // Check vitals
      if(this.headerVariableService.hasAudioDevice == false){
        this.showDialoge('Error','activity-outline','danger', "No microphone found to receive incomming."); 
        this.headerVariableService.callInProgress = false;
        this.headerVariableService.callStatus = 'danger';
          return;
      }
    
      // Start SIP handling
      let supportedConstraints = navigator.mediaDevices.getSupportedConstraints();

      let spdOptions = {
          sessionDescriptionHandlerOptions: {
              constraints: {
                  audio: { deviceId : {} },
                  video: { deviceId : {} }
              }
          }
      }
    
      // Configure Audio
      let currentAudioDevice = this.headerVariableService.currentMicStringValue;
      if(currentAudioDevice != "default"){
          let confirmedAudioDevice = false;
          for (let i = 0; i < this.headerVariableService.micList.length; ++i) {
              if(currentAudioDevice == this.headerVariableService.micList[i]) {
                  confirmedAudioDevice = true;
                  break;
              }
          }
          if(confirmedAudioDevice) {
              spdOptions.sessionDescriptionHandlerOptions.constraints.audio.deviceId = { exact: currentAudioDevice }
          }
          else {
              console.warn("The audio device you used before is no longer available, default settings applied.");
              this.showDialoge('Error','activity-outline','danger', "The audio device you used before is no longer available, default settings applied."); 
        
          }
      }

      // Add additional Constraints
      if(supportedConstraints.autoGainControl) {
          spdOptions.sessionDescriptionHandlerOptions.constraints.audio["autoGainControl"] = this.headerVariableService.autoGainControl;
      }
      if(supportedConstraints.echoCancellation) {
          spdOptions.sessionDescriptionHandlerOptions.constraints.audio["echoCancellation"] = this.headerVariableService.echoCancellation;
      }
      if(supportedConstraints.noiseSuppression) {
          spdOptions.sessionDescriptionHandlerOptions.constraints.audio["noiseSuppression"] = this.headerVariableService.noiseSeperation;
      }
    
      // Configure Video
      let currentVideoDevice = this.headerVariableService.currentVideoStringValue;
      if(currentVideoDevice != "default"){
          let confirmedVideoDevice = false;
          for (let i = 0; i < this.headerVariableService.videoList.length; ++i) {
              if(currentVideoDevice == this.headerVariableService.videoList[i]) {
                  confirmedVideoDevice = true;
                  break;
              }
          }
          if(confirmedVideoDevice){
              spdOptions.sessionDescriptionHandlerOptions.constraints.video["deviceId"] = { exact: currentVideoDevice }
          }
          else {
              console.warn("The video device you used before is no longer available, default settings applied.");
              this.showDialoge('Error','activity-outline','danger', "The video device you used before is no longer available, default settings applied."); 
        
          }
      }
      
      // Add additional Constraints
      if(supportedConstraints.frameRate && this.headerVariableService.maxFrameRate != "") {
          spdOptions.sessionDescriptionHandlerOptions.constraints.video["frameRate"] = this.headerVariableService.maxFrameRate;
      }
      if(supportedConstraints.height && this.headerVariableService.videoHeight != "") {
          spdOptions.sessionDescriptionHandlerOptions.constraints.video["height"] = this.headerVariableService.videoHeight;
      }
      if(supportedConstraints.aspectRatio && this.headerVariableService.videoAspectRatio != "") {
          spdOptions.sessionDescriptionHandlerOptions.constraints.video["aspectRatio"] = this.headerVariableService.videoAspectRatio;
      }
    
      // Save Devices
      sessionData.data.withvideo = true;
      sessionData.data.VideoSourceDevice = this.headerVariableService.currentVideoStringValue;
      sessionData.data.AudioSourceDevice = this.headerVariableService.currentMicStringValue;
      sessionData.data.AudioOutputDevice = this.headerVariableService.currentSpeakerStringValue;

    
      // Send Answer
      sessionData.accept(spdOptions).then(()=>{
        this.inviteService.onInviteAccepted(sessionData,true,"");
      }).catch((error:any)=>{
          console.warn("Failed to answer call", error, sessionData);
          sessionData.data.reasonCode = 500;
          sessionData.data.reasonText = "Client Error";
          this.sessionService.teardownSession(lang,sessionData);
      });
    }
    
    rejectCall(lang:any,sessionData:any) {
      //console.log("I am in rejectCall");
      let session = sessionData;
      if (session == null) {
          console.warn("Reject failed, null session");
        this.showDialoge('Error','activity-outline','danger', lang.call_failed); 
      }

      
      if(session.state == SessionState.Established){
          session.bye().catch((e:any)=>{
              console.warn("Problem in rejectCall(), could not bye() call", e, session);
          });
      }
      else {
          session.reject({ 
              statusCode: 486, 
              reasonPhrase: "Busy Here" 
          }).catch((e:any)=>{
              console.warn("Problem in rejectCall(), could not reject() call", e, session);
              this.showDialoge('Error','activity-outline','danger', e);
          });
      }
    //   $("#line-" + lineObj.LineNumber + "-msg").html(lang.call_rejected);
    
      session.data.terminateby = "us";
      session.data.reasonCode = 486;
      session.data.reasonText = "Busy Here";
      this.sessionService.teardownSession(lang,session);
    }

        
    receiveCall(userAgent:any,lang:any,session:any,didLength:any,enableRingtone:any) {

      //console.log("I am in receiveCall");
      //console.log("session:", session);

      let callerID = session.remoteIdentity.displayName;
      let did = session.remoteIdentity.uri.user;
      if (typeof callerID === 'undefined') callerID = did;
      
      //console.log("New Incoming Call!", callerID +" <"+ did +">");
      
      let CurrentCalls = this.utilsService.countSessions(userAgent,session.id);
      //console.log("Current Call Count:", CurrentCalls);
      
      
      let startTime = moment.utc();
      session.data = {}
      session.data.calldirection = "inbound";
      session.data.terminateby = "";
      session.data.src = did;
      session.data.callstart = startTime.format("YYYY-MM-DD HH:mm:ss UTC");
      let now = moment.utc();
      let duration = moment.duration(now.diff(startTime));
      let timeStr = this.utilsService.formatShortDuration(duration.asSeconds());
      session.data.callTimer = timeStr;
      this.headerVariableService.timer = timeStr;

      session.data.earlyReject = false;

      // Detect Video
      session.data.withvideo = false;

      //console.log("Detecting call type");

      if(session.request.body){
          // Asterisk 13 PJ_SIP always sends m=video if endpoint has video codec,
          // even if original invite does not specify video.
          if(session.request.body.indexOf("m=video") > -1) {
              //console.log("Video call");
              session.data.withvideo = true;
              this.headerVariableService.isVideoCall = true;
          }
          else
          {
              //console.log("Audio call");
              this.headerVariableService.isVideoCall = false;
          }
      }
      
      // Session Delegates
      session.delegate = {
          onBye: (sip:any)=>{
              //console.log("Receive call onBye");
              this.sessionService.onSessionReceivedBye(lang,session,sip)
          },
          onMessage: (sip:any)=>{
              //console.log("Receive call onMessage");
              this.sessionService.onSessionReceivedMessage(session,sip);
          },
          onInvite: (sip:any)=>{
              //console.log("Receive call onInvite");
              this.sessionService.onSessionReinvited(session,sip);
          },
          onSessionDescriptionHandler: (sdh:any, provisional:any)=>{
              //console.log("Receive call onSessionDescriptionHandler");
              this.sessionService.onSessionDescriptionHandlerCreated(session,sdh, provisional, session.data.withvideo);
          }
      }
      // incomingInviteRequestDelegate
      session.incomingInviteRequest.delegate = {
          onCancel: (sip:any)=>{
              this.inviteService.onInviteCancel(lang,session,sip)
          }
      }

      // Possible Early Rejection options
      if(this.headerVariableService.doNotDisturb == true) {
          //console.log("Do Not Disturb Enabled, rejecting call.");
          session.data.earlyReject = true;
          this.rejectCall(lang,session);
          return;
      }
      if(CurrentCalls >= 1){
          if(this.headerVariableService.callWaiting == false ){
              //console.log("Call Waiting Disabled, rejecting call.");
              session.data.earlyReject = true;
              this.rejectCall(lang,session); 
              return;
          }
      }
      
      // Auto Answer options
      let answerTimeout = 1000;
      if(this.headerVariableService.autoAnswer){
          if(CurrentCalls == 0){ // There are no other calls, so you can answer
              //console.log("Going to Auto Answer this call...");
              window.setTimeout(()=>{
                  // If the call is with video, assume the auto answer is also
                  // In order for this to work nicely, the recipient maut be "ready" to accept video calls
                  // In order to ensure video call compatibility (i.e. the recipient must have their web cam in, and working)
                  // The NULL video should be configured
                  // https://github.com/InnovateAsterisk/Browser-Phone/issues/26
                  if(session.data.withvideo) {
                      this.answervideoCall(lang,session);
                  }
                  else {
                      this.answeraudioCall(lang,session);
                  }
              }, answerTimeout);
              return;
          }
          else {
              console.warn("Could not auto answer call, already on a call.");
          }
      }

      if(CurrentCalls == 0)
      {
          this.headerVariableService.callInProgress = false;
          this.headerVariableService.callStatus = 'danger';
      }
      this.headerVariableService.callRinging = true;
      //Assinging session to header to pass when person picks or rejects call. Hence window notification is not required. This notification UI is instead handled by application itself
      this.sessionService.incommingCallSessionData = session;
      this.headerVariableService.callType= "Incoming-Call";
      this.headerVariableService.callerName = callerID;
      this.headerVariableService.phoneNumber = did;
      this.headerVariableService.isVideoCall = session.data.withvideo;
      this.headerVariableService.isInternal = (Number(did)==didLength);
      this.headerVariableService.isConference = false;
      this.videoDialogDataService.setInitialTwoMembers(this.headerVariableService.isVideoCall,!this.headerVariableService.isInternal,did);

      // Play Ring Tone if not on the phone
      if(enableRingtone == true){
          if(CurrentCalls >= 1){
              // Play Alert
              let ringer : any;
              try{
                ringer = this.phoneMusicService.ringCallWaitingMusic();
               }
               catch(e)
               {
                  //console.log(e);
               }
              session.data.ringerObj = ringer;
          } else {
            let ringer : any;
            try{
              ringer = this.phoneMusicService.ringIcomingCallMusic();
             }
             catch(e)
             {
                //console.log(e);
             }
              session.data.ringerObj = ringer;
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
