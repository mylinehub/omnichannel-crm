import { Injectable } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { HeaderVariableService } from '../../header-variable/header-variable.service';
import { DialogComponent } from '../../../pages/employee/all-employees/dialog/dialog.component';
import { Inviter, UserAgent } from 'sip.js';
import * as moment from 'moment';
import { ConstantsService } from '../../constants/constants.service';
import { UtilsService } from '../utils/utils.service';
import { SessionService } from '../session/session.service';
import { InviteService } from '../invite/invite.service';
import { VideoDialogDataService } from '../../../@theme/components/header/video-dialog/video-dialog-data-service/video-dialog-data.service';

@Injectable({
  providedIn: 'root'
})
export class DialLineService {

  constructor(private dialogService: NbDialogService,
    protected headerVariableService : HeaderVariableService,
    protected constService : ConstantsService,
    protected utilsService : UtilsService,
    protected sessionService:SessionService,
    protected inviteService:InviteService,
    protected videoDialogDataService:VideoDialogDataService,
  ) { }

    //DialLine
    dialLine(userAgent:any,lang:any,type:any, extraHeaders:any,didLength){
    
      //console.log("I am in dialLine");

      if(userAgent == null || userAgent.isRegistered() == false){
        //   console.error("Browser phone is not registered to call");
          this.showDialoge('Not Registered','activity-outline','danger', 'Browser phone is not registered to call'); 
          return;
      }

      let numDial = this.headerVariableService.dialValue;

    //   if(this.headerVariableService.enableAlphanumericDial){
    //       numDial = numDial.replace(this.headerVariableService.telAlphanumericRegEx, "").substring(0,this.headerVariableService.maxDidLength);
    //   }
    //   else {
    //       numDial = numDial.replace(this.headerVariableService.telNumericRegEx, "").substring(0,this.headerVariableService.maxDidLength);
    //   }

    if(numDial.length == 0) {
        // console.warn("Enter number to dial");
        return;
    }


     let internal = true;
     if(numDial.length != didLength) {
        internal = false;
     }

     this.headerVariableService.isConference = false;
     
      // Start Call Invite
      if(type == "audio"){
          this.videoDialogDataService.setInitialTwoMembers(false,!internal,numDial);
          this.audioCall(userAgent,lang,numDial, extraHeaders,didLength);
      } 
      else {
          this.videoDialogDataService.setInitialTwoMembers(true,!internal,numDial);
          this.videoCall(userAgent,lang,numDial, extraHeaders,didLength);
      }
  } 
  
    // Outbound Calling
  // ================
  videoCall(userAgent:any,lang:any,dialledNumber:any, extraHeaders:any,didLength) {

      //console.log("I am in videoCall");

      if(userAgent == null) return;
      if(!userAgent.isRegistered()) return;

      if(this.headerVariableService.hasAudioDevice == false){
        // console.error("No Microphone");
          this.showDialoge('No Microphone','activity-outline','danger', lang.alert_no_microphone); 
          return;
      }

      if(this.headerVariableService.hasVideoDevice == false){
        //   console.warn("No video devices (webcam) found, switching to audio call.");
          this.audioCall(userAgent,lang,dialledNumber,"",didLength);
          return;
      }

      let supportedConstraints = navigator.mediaDevices.getSupportedConstraints();
      let spdOptions = {
          //extraHeaders:{},
          earlyMedia: true,
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
              if(currentAudioDevice == this.headerVariableService.micList[i].deviceId) {
                  confirmedAudioDevice = true;
                  break;
              }
          }
          if(confirmedAudioDevice) {
              spdOptions.sessionDescriptionHandlerOptions.constraints.audio.deviceId = { exact: currentAudioDevice }
          }
          else {
              console.warn("The audio device you used before is no longer available, default settings applied.");
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
              if(currentVideoDevice == this.headerVariableService.videoList[i].deviceId) {
                  confirmedVideoDevice = true;
                  break;
              }
          }
          if(confirmedVideoDevice){
              spdOptions.sessionDescriptionHandlerOptions.constraints.video.deviceId = { exact: currentVideoDevice }
          }
          else {
              console.warn("The video device you used before is no longer available, default settings applied.");
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
      // Extra Headers
      if(extraHeaders) {
          spdOptions["extraHeaders"] = extraHeaders;
      }

      this.headerVariableService.browserPhoneTitle = lang.starting_video_call;


      let startTime = moment.utc();

      // Invite
      //console.log("INVITE (video): " + dialledNumber + "@" + ConstantsService.user.domain); 

      let targetURI = UserAgent.makeURI("sip:" + dialledNumber.replace(/#/g, "%23") + "@" + ConstantsService.user.domain);
      let sipSession:any = new Inviter(userAgent, targetURI, spdOptions);
      sipSession.data = {}
      sipSession.data.calldirection = "outbound";
      sipSession.data.dst = dialledNumber;
      sipSession.data.callstart = startTime.format("YYYY-MM-DD HH:mm:ss UTC");
      sipSession.data.callTimer = window.setInterval(()=>{
          let now = moment.utc();
          let duration = moment.duration(now.diff(startTime)); 
          let timeStr = this.utilsService.formatShortDuration(duration.asSeconds());
          // $("#line-" + lineObj.LineNumber + "-timer").html(timeStr);
          // $("#line-" + lineObj.LineNumber + "-datetime").html(timeStr);
      }, 1000);
      sipSession.data.VideoSourceDevice = this.headerVariableService.currentVideoStringValue;
      sipSession.data.AudioSourceDevice = this.headerVariableService.currentMicStringValue;
      sipSession.data.AudioOutputDevice = this.headerVariableService.currentSpeakerStringValue;
      sipSession.data.terminateby = "them";
      sipSession.data.withvideo = true;
      sipSession.data.earlyReject = false;
      sipSession.isOnHold = false;
      sipSession.delegate = {
          onBye: (sip:any)=>{
              this.sessionService.onSessionReceivedBye(lang,sipSession, sip);
          },
          onMessage: (sip:any)=>{
              this.sessionService.onSessionReceivedMessage(sipSession, sip);
          },
          onInvite: (sip:any)=>{
              this.sessionService.onSessionReinvited(sipSession, sip);
          },
          onSessionDescriptionHandler: (sdh:any, provisional:any)=>{
              this.sessionService.onSessionDescriptionHandlerCreated(sipSession, sdh, provisional, true);
          }
      }
      let inviterOptions = {
          requestDelegate: { // OutgoingRequestDelegate
              onTrying: (sip:any)=>{
                  this.inviteService.onInviteTrying(lang,sipSession, sip);
              },
              onProgress:(sip:any)=>{
                  this.inviteService.onInviteProgress(userAgent,lang,sipSession, sip,dialledNumber,didLength);
              },
              onRedirect:(sip:any)=>{
                  this.inviteService.onInviteRedirected(lang,sipSession, sip);
              },
              onAccept:(sip:any)=>{
                  this.inviteService.onInviteAccepted(sipSession, true, sip);
              },
              onReject:(sip:any)=>{
                  this.inviteService.onInviteRejected(lang,sipSession, sip);
              }
          }
      }

      sipSession.invite(inviterOptions).catch((e:any)=>{
          console.warn("Failed to send INVITE:", e);
      });

      this.sessionService.outgoingCallSessionData = sipSession;
      
      // $("#line-" + lineObj.LineNumber + "-btn-settings").removeAttr('disabled');
      // $("#line-" + lineObj.LineNumber + "-btn-audioCall").prop('disabled','disabled');
      // $("#line-" + lineObj.LineNumber + "-btn-videoCall").prop('disabled','disabled');
      // $("#line-" + lineObj.LineNumber + "-btn-search").removeAttr('disabled');

      // $("#line-" + lineObj.LineNumber + "-progress").show();
      // $("#line-" + lineObj.LineNumber + "-msg").show();

  }


  audioCall(userAgent:any,lang:any,dialledNumber:any, extraHeaders:any,didLength) {

      //console.log("I am in audioCall");

      if(userAgent == null) return;
      if(userAgent.isRegistered() == false) return;

      if(this.headerVariableService.hasAudioDevice == false){
        console.error("No Microphone 1");
          this.showDialoge('No Microphone','activity-outline','danger', lang.alert_no_microphone); 
          return;
      }

      let supportedConstraints = navigator.mediaDevices.getSupportedConstraints();

      let spdOptions = {
          earlyMedia: true,
          sessionDescriptionHandlerOptions: {
              constraints: {
                  audio: { deviceId : {} },
                  video: false
              }
          }
      }
      // Configure Audio
      let currentAudioDevice = this.headerVariableService.currentMicStringValue;
      if(currentAudioDevice != "default"){
          let confirmedAudioDevice = false;
          for (let i = 0; i < this.headerVariableService.micList.length; ++i) {
              if(currentAudioDevice == this.headerVariableService.micList[i].deviceId) {
                  confirmedAudioDevice = true;
                  break;
              }
          }
          if(confirmedAudioDevice) {
              spdOptions.sessionDescriptionHandlerOptions.constraints.audio.deviceId = { exact: currentAudioDevice }
          }
          else {
              console.warn("The audio device you used before is no longer available, default settings applied.");
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
      // Extra Headers
      if(extraHeaders) {
          spdOptions["extraHeaders"] = extraHeaders;
      }

      // $("#line-" + lineObj.LineNumber + "-timer").show();
      this.headerVariableService.browserPhoneTitle = lang.starting_audio_call;

      let startTime = moment.utc();

      // Invite
      //console.log("INVITE (audio): " + dialledNumber + "@" + ConstantsService.user.domain);

      let targetURI = UserAgent.makeURI("sip:" + dialledNumber.replace(/#/g, "%23") + "@" + ConstantsService.user.domain);
      let sipSession:any = new Inviter(userAgent, targetURI, spdOptions);
      sipSession.data = {}
      sipSession.data.calldirection = "outbound";
      sipSession.data.dst = dialledNumber;
      sipSession.data.callstart = startTime.format("YYYY-MM-DD HH:mm:ss UTC");
      let now = moment.utc();
      let duration = moment.duration(now.diff(startTime));
      let timeStr = this.utilsService.formatShortDuration(duration.asSeconds());
      sipSession.data.callTimer = timeStr;
      this.headerVariableService.timer = timeStr;
      sipSession.data.VideoSourceDevice = null;
      sipSession.data.AudioSourceDevice = this.headerVariableService.currentMicStringValue;
      sipSession.data.AudioOutputDevice = this.headerVariableService.currentSpeakerStringValue;
      sipSession.data.terminateby = "them";
      sipSession.data.withvideo = false;
      sipSession.data.earlyReject = false;
      sipSession.isOnHold = false;
      sipSession.delegate = {
          onBye: (sip:any)=>{
              this.sessionService.onSessionReceivedBye(lang,sipSession, sip);
          },
          onMessage: (sip:any)=>{
              this.sessionService.onSessionReceivedMessage(sipSession, sip);
          },
          onInvite: (sip:any)=>{
              this.sessionService.onSessionReinvited(sipSession, sip);
          },
          onSessionDescriptionHandler: (sdh:any, provisional:any)=>{
              this.sessionService.onSessionDescriptionHandlerCreated(sipSession, sdh, provisional, false);
          }
      }
      let inviterOptions = {
          requestDelegate: { // OutgoingRequestDelegate
              onTrying: (sip:any)=>{
                  this.inviteService.onInviteTrying(lang,sipSession, sip);
              },
              onProgress:(sip:any)=>{
                  this.inviteService.onInviteProgress(userAgent,lang,sipSession, sip,dialledNumber,didLength);
              },
              onRedirect:(sip:any)=>{
                  this.inviteService.onInviteRedirected(lang,sipSession, sip);
              },
              onAccept:(sip:any)=>{
                  this.inviteService.onInviteAccepted(sipSession, false, sip);
              },
              onReject:(sip:any)=>{
                  this.inviteService.onInviteRejected(lang,sipSession, sip);
              }
          }
      }

      sipSession.invite(inviterOptions).catch((e:any)=>{
          console.warn("Failed to send INVITE:", e);
      });

      this.sessionService.outgoingCallSessionData = sipSession;

      // $("#line-" + lineObj.LineNumber + "-btn-settings").removeAttr('disabled');
      // $("#line-" + lineObj.LineNumber + "-btn-audioCall").prop('disabled','disabled');
      // $("#line-" + lineObj.LineNumber + "-btn-videoCall").prop('disabled','disabled');
      // $("#line-" + lineObj.LineNumber + "-btn-search").removeAttr('disabled');

      // $("#line-" + lineObj.LineNumber + "-progress").show();
      // $("#line-" + lineObj.LineNumber + "-msg").show();    
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
