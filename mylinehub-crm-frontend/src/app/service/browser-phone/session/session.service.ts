import { Injectable } from '@angular/core';
import { HeaderVariableService } from '../../header-variable/header-variable.service';
import { UtilsService } from '../utils/utils.service';
import { SessionState } from 'sip.js';
import { RecordingService } from '../recording/recording.service';
import { VideoDialogDataService } from '../../../@theme/components/header/video-dialog/video-dialog-data-service/video-dialog-data.service';

@Injectable({
  providedIn: 'root'
})
export class SessionService {

  incommingCallSessionData : any  = null;
  outgoingCallSessionData : any  = null;
  currentConnectedSessionData : any  = null;
  conferenceNewSession:any = null;

  constructor(protected headerVariableService : HeaderVariableService,
              protected utilsService : UtilsService,
              protected recordingService:RecordingService,
              protected videoDialogDataService:VideoDialogDataService,
            ) { }

    // General Session delegates
    onSessionReceivedBye(lang,sessionData: any, response: any){
          // console.log("I am in onSessionReceivedBye");
          //console.log("Call ended, bye!");
          sessionData.data.terminateby = "them";
          sessionData.data.reasonCode = 16;
          sessionData.data.reasonText = "Normal Call clearing";

        //   console.log("Accepting response");
          response.accept(); // Send OK
        //   console.log("Tearing down session");
          this.teardownSession(lang,sessionData);
        }
  
    onSessionReinvited(sessionData: any, response: any){
          // console.log("I am in onSessionReinvited");
          // This may be used to include video streams
          let sdp = response.body;
        //   console.log("response : ",response);
        //   console.log("sdp : ",sdp);
        //   console.log("sessionData : ",sessionData);

          let session = sessionData;
          // All the possible streams will get 
          // Note, this will probably happen after the streams are added
          session.data.videoChannelNames = [];
          let videoSections = sdp.split("m=video");

        //   console.log("videoSections : ", videoSections);

          if(videoSections.length >= 1){

            // console.log("videoSections length is greator than 1");

              for(let m=0; m<videoSections.length; m++){
                  if(videoSections[m].indexOf("a=mid:") > -1 && videoSections[m].indexOf("a=label:") > -1){
                      // We have a label for the media
                      let lines = videoSections[m].split("\r\n");
                      let channel = "";
                      let mid = "";
                      for(let i=0; i<lines.length; i++){
                          if(lines[i].indexOf("a=label:") == 0) {
                              channel = lines[i].replace("a=label:", "");
                          }
                          if(lines[i].indexOf("a=mid:") == 0){
                              mid = lines[i].replace("a=mid:", "");
                          }
                      }
                      session.data.videoChannelNames.push({"mid" : mid, "channel" : channel });
                  }
                else
                {
                    // console.log("videoSections[m].indexOf('a=mid:')< -1 or videoSections[m].indexOf('a=label:') < -1");
                }
              }
            //   console.log("videoChannelNames:", session.data.videoChannelNames);

            // Need to understand why redraw stage was required. Another thing here to understand is when this function will be called
            // this.redrawStage(session, false);
          }
        }
  
    onSessionReceivedMessage(sessionData:any, response:any){

         //Need to understand why this function is required. We are doing messaging using JPA. Hence may be this will not be required.
          // console.log("I am in onSessionReceivedMessage");
          let messageType = (response.request.headers["Content-Type"].length >=1)? response.request.headers["Content-Type"][0].parsed : "Unknown" ;
        //   console.log("response: ",response);
        //   console.log("sessionData: ",sessionData);
        //   console.log("messageType: ",messageType);
          
          if(messageType.indexOf("application/x-asterisk-confbridge-event") > -1){

            //   console.log("messageType.indexOf('application/x-asterisk-confbridge-event') > -1");
              // Conference Events JSON
              let msgJson = JSON.parse(response.request.body);
            //   console.log("msgJson: ",msgJson);

              let session = sessionData;
              if(!session.data.ConfbridgeChannels) session.data.ConfbridgeChannels = [];
              if(!session.data.ConfbridgeEvents) session.data.ConfbridgeEvents = [];
  
              if(msgJson.type == "ConfbridgeStart"){
                  //console.log("ConfbridgeStart!");
              }
              else if(msgJson.type == "ConfbridgeWelcome"){
                //   console.log("Welcome to the Asterisk Conference");
                //   console.log("Bridge ID:", msgJson.bridge.id);
                //   console.log("Bridge Name:", msgJson.bridge.name);
                //   console.log("Created at:", msgJson.bridge.creationtime);
                //   console.log("Video Mode:", msgJson.bridge.video_mode);
  
                  session.data.ConfbridgeChannels = msgJson.channels; // Write over this
                  session.data.ConfbridgeChannels.forEach((chan:any)=> {
                      // The mute and unmute status doesn't appear to be a realtime state, only what the 
                      // startmuted= setting of the default profile is.
                      //console.log(chan.caller.name, "Is in the conference. Muted:", chan.muted, "Admin:", chan.admin);
                  });
              }
              else if(msgJson.type == "ConfbridgeJoin"){
                  msgJson.channels.forEach((chan:any)=> {
                      let found = false;
                      session.data.ConfbridgeChannels.forEach((existingChan:any)=> {
                          if(existingChan.id == chan.id) found = true;
                      });
                      if(!found){
                          session.data.ConfbridgeChannels.push(chan);
                          session.data.ConfbridgeEvents.push({ event: chan.caller.name + " ("+ chan.caller.number +") joined the conference", eventTime: this.utilsService.utcDateNow() });
                          console.log(chan.caller.name, "Joined the conference. Muted: ", chan.muted);
                      }
                  });
              }
              else if(msgJson.type == "ConfbridgeLeave"){
                  msgJson.channels.forEach((chan:any)=> {
                      session.data.ConfbridgeChannels.forEach((existingChan:any, i:any)=> {
                          if(existingChan.id == chan.id){
                              session.data.ConfbridgeChannels.splice(i, 1);
                              console.log(chan.caller.name, "Left the conference");
                              session.data.ConfbridgeEvents.push({ event: chan.caller.name + " ("+ chan.caller.number +") left the conference", eventTime: this.utilsService.utcDateNow() });
                          }
                      });
                  });
              }
              else if(msgJson.type == "ConfbridgeTalking"){
                  let videoContainer :any= "this.headerVariableService.videoContainer";
                  if(videoContainer){
                      msgJson.channels.forEach((chan:any)=> {
                          videoContainer.find('video').each(()=> {
                              if("this.headerVariableService.videoObject.channel" == chan.id) {
                                  if(chan.talking_status == "on"){
                                      //console.log(chan.caller.name, "is talking.");
                                    //   this.headerVariableService.videoObject.isTalking = true;
                                      // $(this).css("border","1px solid red");
                                  }
                                  else {
                                      //console.log(chan.caller.name, "stopped talking.");
                                    //   this.headerVariableService.videoObject.isTalking = false;
                                      // $(this).css("border","1px solid transparent");
                                  }
                              }
                          });
                      });
                  }
              }
              else if(msgJson.type == "ConfbridgeMute"){
                  msgJson.channels.forEach((chan:any)=> {
                      session.data.ConfbridgeChannels.forEach((existingChan:any)=> {
                          if(existingChan.id == chan.id){
                              console.log(existingChan.caller.name, "is now muted");
                              existingChan.muted = true;
                          }
                      });
                  });
                //   this.redrawStage(session, false);
              }
              else if(msgJson.type == "ConfbridgeUnmute"){
                  msgJson.channels.forEach((chan:any)=> {
                      session.data.ConfbridgeChannels.forEach((existingChan:any)=> {
                          if(existingChan.id == chan.id){
                              console.log(existingChan.caller.name, "is now unmuted");
                              existingChan.muted = false;
                          }
                      });
                  });
                //   this.redrawStage(session, false);
              }
              else if(msgJson.type == "ConfbridgeEnd"){
                //   console.log("The Asterisk Conference has ended, bye!");
              }
              else {
                  console.warn("Unknown Asterisk Conference Event:", msgJson.type, msgJson);
              }
              // RefreshLineActivity(lineObj.LineNumber);
              response.accept();
          } 
          else if(messageType.indexOf("application/x-myphone-confbridge-chat") > -1){
            //   console.log("x-myphone-confbridge-chat", response);
  
  
              response.accept();
          }
          else {
              console.warn("Unknown message type")
              response.reject();
          }
        }
  

    onSessionDescriptionHandlerCreated(sessionData:any, sdh:any, provisional:any, includeVideo:any){
          console.log("I am in onSessionDescriptionHandlerCreated");
  
          if (sdh) {
              if(sdh.peerConnection){
                // console.log("Adding tracks to session description handler having data : ",sdh);
                  sdh.peerConnection.ontrack = (event:any)=>{
                       console.log(event);
                      this.onTrackAddedEvent(sessionData, includeVideo);
                  }
              }
              else{
                  console.warn("onSessionDescriptionHandler fired without a peerConnection");
              }
          }
          else{
              console.warn("onSessionDescriptionHandler fired without a sessionDescriptionHandler");
          }
        }

   // In-Session Call Func
    // =============================   
    
    endSession(lang:any,sessionData:any) {
      console.log("I am in endSession");
    //   console.log("Ending call having session: "+ sessionData);
      sessionData.data.terminateby = "us";
      sessionData.data.reasonCode = 16;
      sessionData.data.reasonText = "Normal Call clearing";
  
      sessionData.bye().catch((e:any)=>{
          console.warn("Failed to bye the session!", e);
      });
      this.headerVariableService.browserPhoneTitle = lang.call_ended;

      this.teardownSession(lang,sessionData);
  }

  cancelSession(lang:any,sessionData:any) {
      console.log("I am in cancelSession");
      sessionData.data.terminateby = "us";
      sessionData.data.reasonCode = 0;
      sessionData.data.reasonText = "Call Cancelled";
  
    //   console.log("Cancelling session : "+ sessionData);
      if(sessionData.state == SessionState.Initial || sessionData.state == SessionState.Establishing){
          sessionData.cancel();
          this.teardownSession(lang,sessionData);
      }
      else {
          console.warn("Session not in correct state for cancel.", sessionData.state);
        //   console.log("Attempting teardown : "+ sessionData);
          this.teardownSession(lang,sessionData);
      }
     this.headerVariableService.browserPhoneTitle = lang.call_cancelled;
  }

  holdSession(lang:any,sessionData:any) {
      console.log("I am in holdSession");
      let session = sessionData;
      if(session.isOnHold == true) {
        //   console.log("Call is is already on hold:", session);
          return;
      }
    //   console.log("Putting Call on hold:", session);
      session.isOnHold = true;
  
    //   console.log("sessionData : ", sessionData);
      let sessionDescriptionHandlerOptions = session.sessionDescriptionHandlerOptionsReInvite;
      sessionDescriptionHandlerOptions.hold = true;
      session.sessionDescriptionHandlerOptionsReInvite = sessionDescriptionHandlerOptions;
  
      let options = {
          requestDelegate: {
              onAccept: ()=>{
                  if(session && session.sessionDescriptionHandler && session.sessionDescriptionHandler.peerConnection){

                    //   console.log("Getting peer connection");
                      let pc = session.sessionDescriptionHandler.peerConnection;

                    //   console.log("Stop all the inbound streams");
                      // Stop all the inbound streams
                      pc.getReceivers().forEach((rTCRtpReceiver:any)=>{
                          if (rTCRtpReceiver.track) rTCRtpReceiver.track.enabled = false;
                      });

                    //   console.log("Stop all the outbound streams (especially useful for Conference Calls!!)");
                      // Stop all the outbound streams (especially useful for Conference Calls!!)
                      pc.getSenders().forEach((rTCRtpSender:any)=>{
                          // Mute Audio
                          if(rTCRtpSender.track && rTCRtpSender.track.kind == "audio") {
                              if(rTCRtpSender.track.IsMixedTrack == true){
                                  if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
                                    //   console.log("Muting Mixed Audio Track : "+ session.data.AudioSourceTrack.label);
                                      session.data.AudioSourceTrack.enabled = false;
                                  }
                              }
                            //   console.log("Muting Audio Track : "+ rTCRtpSender.track.label);
                              rTCRtpSender.track.enabled = false;
                          }
                          // Stop Video
                          else if(rTCRtpSender.track && rTCRtpSender.track.kind == "video"){
                            // console.log("Stop Video");
                            rTCRtpSender.track.enabled = false;
                          }
                      });
                  }
                  session.isOnHold = true;
                //   console.log("Call is is on hold:", session);
                  this.headerVariableService.browserPhoneTitle = lang.call_on_hold;

                //   console.log("Hold completed at : ",this.utilsService.utcDateNow());

                  // Log Hold
                  if(!session.data.hold) session.data.hold = [];
                  session.data.hold.push({ event: "hold", eventTime: this.utilsService.utcDateNow() });
              },
              onReject: ()=>{
                  session.isOnHold = false;
                  console.warn("Failed to put the call on hold:", session);
              }
          }
      };
      session.invite(options).catch((error:any)=>{
          session.isOnHold = false;
          console.warn("Error attempting to put the call on hold:", error);
      });
  }


  unholdSession(lang:any,sessionData:any) {
      console.log("I am in unholdSession");
      let session = sessionData;
      if(session.isOnHold == false) {
          //console.log("Call is already off hold:", session);
          return;
      }
    //   console.log("Taking call off hold:", session);
      session.isOnHold = false;
  
    //   console.log("sessionData : ", sessionData);
      let sessionDescriptionHandlerOptions = session.sessionDescriptionHandlerOptionsReInvite;
      sessionDescriptionHandlerOptions.hold = false;
      session.sessionDescriptionHandlerOptionsReInvite = sessionDescriptionHandlerOptions;
  
      let options = {
          requestDelegate: {
              onAccept: ()=>{
                  if(session && session.sessionDescriptionHandler && session.sessionDescriptionHandler.peerConnection){

                    //   console.log("Getting peer connection");
                      let pc = session.sessionDescriptionHandler.peerConnection;

                    //   console.log("Restore all the inbound streams");
                      // Restore all the inbound streams
                      pc.getReceivers().forEach((rTCRtpReceiver:any)=>{
                          if (rTCRtpReceiver.track) rTCRtpReceiver.track.enabled = true;
                      });

                    //   console.log("Restore all the outbound streams");
                      // Restore all the outbound streams
                      pc.getSenders().forEach((rTCRtpSender:any)=>{
                          // Unmute Audio
                          if(rTCRtpSender.track && rTCRtpSender.track.kind == "audio") {
                              if(rTCRtpSender.track.IsMixedTrack == true){
                                  if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
                                      //console.log("Unmuting Mixed Audio Track : "+ session.data.AudioSourceTrack.label);
                                      session.data.AudioSourceTrack.enabled = true;
                                  }
                              }
                              //console.log("Unmuting Audio Track : "+ RTCRtpSender.track.label);
                              rTCRtpSender.track.enabled = true;
                          }
                          else if(rTCRtpSender.track && rTCRtpSender.track.kind == "video") {
                            rTCRtpSender.track.enabled = true;
                          }
                      });
                  }
                  session.isOnHold = false;

                //   console.log("Call is off hold:", session);

                  this.headerVariableService.browserPhoneTitle = lang.call_in_progress;

                //   console.log("Unhold completed at : ",this.utilsService.utcDateNow());
                  // Log Hold
                  if(!session.data.hold) session.data.hold = [];
                  session.data.hold.push({ event: "unhold", eventTime: this.utilsService.utcDateNow() });
              },
              onReject: ()=>{
                  session.isOnHold = true;
                  console.warn("Failed to put the call on hold", session);
              }
          }
      };

    //   console.log("Sending invite for unhold");
      session.invite(options).catch((error:any)=>{
          session.isOnHold = true;
          console.warn("Error attempting to take to call off hold", error);
      });

  }
  
  muteSession(lang:any,sessionData:any){
      console.log("I am in muteSession");
      let session = sessionData;
      let pc = session.sessionDescriptionHandler.peerConnection;

    //   console.log("Mute track for all senders");
      pc.getSenders().forEach((rTCRtpSender:any) => {
          if(rTCRtpSender.track && rTCRtpSender.track.kind == "audio") {
              if(rTCRtpSender.track.IsMixedTrack == true){
                  if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
                    //   console.log("Muting Mixed Audio Track : "+ session.data.AudioSourceTrack.label);
                      session.data.AudioSourceTrack.enabled = false;
                  }
              }
              //console.log("Muting Audio Track : "+ RTCRtpSender.track.label);
              rTCRtpSender.track.enabled = false;
          }
      });
  
      if(!session.data.mute) session.data.mute = [];
      session.data.mute.push({ event: "mute", eventTime: this.utilsService.utcDateNow() });
      session.data.ismute = true;

    //   console.log("Setting browser phone title");
      this.headerVariableService.browserPhoneTitle = lang.call_on_mute;
  }
  
  unmuteSession(lang:any,sessionData:any){
      console.log("I am in unmuteSession");
      let session = sessionData;
      let pc = session.sessionDescriptionHandler.peerConnection;

    //   console.log("Unmute track for all senders");
      pc.getSenders().forEach((rTCRtpSender) =>{
          if(rTCRtpSender.track && rTCRtpSender.track.kind == "audio") {
              if(rTCRtpSender.track.IsMixedTrack == true){
                  if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
                    //   console.log("Unmuting Mixed Audio Track : "+ session.data.AudioSourceTrack.label);
                      session.data.AudioSourceTrack.enabled = true;
                  }
              }
              //console.log("Unmuting Audio Track : "+ RTCRtpSender.track.label);
              rTCRtpSender.track.enabled = true;
          }
      });
  
      if(!session.data.mute) session.data.mute = [];
      session.data.mute.push({ event: "unmute", eventTime: this.utilsService.utcDateNow() });
      session.data.ismute = false;

    //   console.log("Setting browser phone title");
      this.headerVariableService.browserPhoneTitle = lang.call_off_mute;
  }



      // General end of Session
 teardownSession(lang:any,sessionData:any) {
        console.log("I am in teardownSession");
        // console.log("Tearing all variables");
        this.headerVariableService.callRinging = false;
        this.headerVariableService.callInProgress = false;
        this.headerVariableService.callStatus = 'danger';
        this.currentConnectedSessionData = null;
        this.outgoingCallSessionData  = null;
        this.incommingCallSessionData = null;
        this.headerVariableService.isVideoCall = false;
        this.headerVariableService.isConference = false;
        this.videoDialogDataService.allConferenceMembers = [];
        this.videoDialogDataService.allChatHistory = [];
        this.videoDialogDataService.allChatCurrent = [];
        this.videoDialogDataService.allChatData = [];
        this.videoDialogDataService.mainVideoData = "";
        this.videoDialogDataService.initialChatHistory = "";

        if(sessionData == null) return;

        let session = sessionData;
        if(session.data.teardownComplete == true) return;
        session.data.teardownComplete = true; // Run this code only once

        // // Call UI
        // if(session.data.earlyReject != true){
        //     //HidePopup();
        // }

        // console.log("Ending child sessions");
        // End any child calls
        if(session.data.childsession){
            console.log("Child Conference call detected:", session.data.childsession.state);
            session.data.childsession.dispose().then(()=>{
                session.data.childsession = null;
            }).catch((error:any)=>{
                session.data.childsession = null;
                // Suppress message
            });
        }

        // console.log("Stopping audio tracks");
        // Mixed Tracks
        if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
            session.data.AudioSourceTrack.stop();
            session.data.AudioSourceTrack = null;
        }

        console.log("Stopping early media");
        // Stop any Early Media
        if(session.data.earlyMedia){
            session.data.earlyMedia.pause();
            session.data.earlyMedia.removeAttribute('src');
            session.data.earlyMedia.load();
            session.data.earlyMedia = null;
        }

        console.log("Pausing ringer object");
        // Stop any ringing calls
        if(session.data.ringerObj){
            session.data.ringerObj.pause();
            session.data.ringerObj.removeAttribute('src');
            session.data.ringerObj.load();
            session.data.ringerObj = null;
        }
        
        // console.log("Stopping recording if started");
        // Stop Recording if we are
        this.recordingService.stopRecording(session);

        // console.log("Releasing microphone, stoping sender tracks");
        // Make sure you have released the microphone
        if(session && session.sessionDescriptionHandler && session.sessionDescriptionHandler.peerConnection){
            let pc = session.sessionDescriptionHandler.peerConnection;
            pc.getSenders().forEach( (RTCRtpSender:any) =>{
                if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                    RTCRtpSender.track.stop();
                }
            });
        }

        // console.log("Clearing timer");
        // End timers
        window.clearInterval(session.data.videoResampleInterval);
        window.clearInterval(session.data.callTimer);


        // console.log("Checking if call was missed");
        // Check if this call was missed
        if (session.data.calldirection == "inbound"){
            if(session.data.earlyReject){
                // Call was rejected without even ringing
            } else if (session.data.terminateby == "them" && session.data.startTime == null){
                // Call Terminated by them during ringing
                if(session.data.reasonCode == 0){
                    // Call was canceled, and not answered elsewhere 
                }
            }
        }
      }


  onTrackAddedEvent(sessionData:any, includeVideo:any){
          console.log("I am in onTrackAddedEvent");
        //   Gets remote tracks
          let session = sessionData;
          // TODO: look at detecting video, so that UI switches to audio/video automatically.

          console.log("Session in onTrackAddedEvent: ",session);

        //   console.log("Peer connection from session : ",session);
          console.log("session.sessionDescriptionHandler : ",session.sessionDescriptionHandler);
          let pc = session.sessionDescriptionHandler.peerConnection;
          console.log("session.sessionDescriptionHandler.peerConnection : ",pc);
          console.log("session.data.ConfbridgeChannels : " ,session.data.ConfbridgeChannels);
          console.log("session.data.videoChannelNames : ",session.data.videoChannelNames );
          this.headerVariableService.remoteAudioStream = new MediaStream();
        //   console.log("Ceated audio stream");        
          this.headerVariableService.remoteVideoStream = new MediaStream();
        //   console.log("Ceated video stream");  
  
           console.log("pc.getTransceivers() : ",pc.getTransceivers());  

          pc.getTransceivers().forEach( (transceiver:any, i:number)=> {
  
            //   console.log("Transceiver at index : ",i); 
            //   console.log("transceiver : ",transceiver);  
              // Add Media
              let receiver = transceiver.receiver;
            //   console.log("Attaching media"); 
            //   console.log("transceiver.receiver : ",receiver); 
            //   console.log("transceiver.mid : ",transceiver.mid); 
              if(receiver.track){
  
                //   console.log("receiver.track : ",receiver.track); 
                //   console.log("receiver.track.kind: ",receiver.track.kind);
                  if(receiver.track.kind == "audio"){
                    //   console.log("Adding Remote Audio Track");
                      this.headerVariableService.remoteAudioStream.addTrack(receiver.track);
                  }
                  else if(includeVideo && receiver.track.kind == "video"){
                      if(transceiver.mid){
                          receiver.track.mid = transceiver.mid;
                        //   console.log("Adding Remote Video Track - ", receiver.track.readyState , "MID:", receiver.track.mid);
                          this.headerVariableService.remoteVideoStream.addTrack(receiver.track);
                      }
                  }
                  else{
                    // Do Nothing
                  }
              }
          });
  
          // Attach Audio
        //   console.log("Attaching audio media to speaker"); 
        //   console.log("this.headerVariableService.remoteAudi : ",this.headerVariableService.remoteAudio); 
        //   console.log("remoteAudioStream.getAudioTracks()", this.headerVariableService.remoteAudioStream.getAudioTracks()); 
          
          if(this.headerVariableService.remoteAudioStream.getAudioTracks().length >= 1){
            //   console.log("Setting tracks audio html tag");
              let remoteAudio = this.headerVariableService.remoteAudio;
            //   console.log("Attaching stream to object");
              remoteAudio.srcObject = this.headerVariableService.remoteAudioStream;
              remoteAudio.onloadedmetadata = (e:any)=> {
                //   console.log("Media loaded to remote audio");
                //   console.log("remoteAudio.sinkId", remoteAudio.sinkId);
                  if (typeof remoteAudio.sinkId !== 'undefined') {
                    //   console.log("Setting new Sink ID");
                      remoteAudio.setSinkId(this.headerVariableService.currentSpeakerStringValue).then(()=>{
                        //   console.log("sinkId applied: "+ this.headerVariableService.currentSpeakerStringValue);
                      }).catch((e:any)=>{
                          console.warn("Error using setSinkId: ", e);
                      });
                  }
                //   console.log("Playing Audio");
                  remoteAudio.play();
              }
          }
  
          // if(includeVideo){
             //This code is written in video Dialog Coponent
          // }
        }

}
