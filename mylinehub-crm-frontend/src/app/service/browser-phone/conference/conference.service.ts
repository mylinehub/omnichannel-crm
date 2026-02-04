import { Injectable } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { HeaderVariableService } from '../../header-variable/header-variable.service';
import { SessionService } from '../session/session.service';
import { DialogComponent } from '../../../pages/employee/all-employees/dialog/dialog.component';
import { UtilsService } from '../utils/utils.service';
import { TransferService } from '../transfer/transfer.service';
import { ConstantsService } from '../../constants/constants.service';
import { Inviter, SessionState, UserAgent } from 'sip.js';

@Injectable({
  providedIn: 'root'
})
export class ConferenceService {

  constructor(private dialogService: NbDialogService,
    protected headerVariableService : HeaderVariableService,
    protected sessionService:SessionService,
    protected utilsService : UtilsService,
    protected transferService:TransferService,
  ) { }

  
    // Conference Calls
    // ================
  startConferenceCall(lang:any,sessionData:any){
      console.log("I am in startConferenceCall");
      this.headerVariableService.isConference = true;
      if(this.headerVariableService.isTransfer){
        console.log("Call is currently transferred. Hence call is disconnected. Make a new call first for start conference");
        this.transferService.cancelTransferSession(lang,sessionData);
          return;
      }
      console.log("Current call is put on hold. Now a member should be added. Although we can decide later if call is to be put on hold.");
      this.sessionService.holdSession(lang,sessionData);
  }

  
  cancelConference(lang:any,sessionData:any,confId:any){
      console.log("I am in cancelConference");
      let session = sessionData;
     
     this.headerVariableService.browserPhoneTitle = lang.call_ended;
          //console.log("New call session end");

     console.log("Changing session data variable");
     session.data.confcalls[confId].accept.complete = false;
     session.data.confcalls[confId].accept.disposition = "bye";
     session.data.confcalls[confId].accept.eventTime = this.utilsService.utcDateNow();
     
     console.log("Setting header varible");
     this.headerVariableService.browserPhoneTitle = lang.conference_call_ended;

     console.log("Disposing child session while tearing down session");
     console.log("session.data : ",session.data);
     console.log("session.data.childsession : ",session.data.childsession);    
     this.sessionService.teardownSession(lang,session);
     
      console.log("Unhold session");
      this.sessionService.unholdSession(lang,session);  

    }

    cancelNewSession(lang:any,confCallId:any,session:any,newSession:any)
    {
           console.log("I am in cancelNewSession");
            //Code to cancel conference session
            newSession.cancel().catch((error:any)=>{
                console.warn("Failed to CANCEL", error);
            });
            this.headerVariableService.browserPhoneTitle = lang.call_cancelled;
            console.log("New call session canceled");
        
            session.data.confcalls[confCallId].accept.complete = false;
            session.data.confcalls[confCallId].accept.disposition = "cancel";
            session.data.confcalls[confCallId].accept.eventTime = this.utilsService.utcDateNow();
            this.headerVariableService.browserPhoneTitle = lang.conference_call_cancelled;
    }

    terminateConferenceCall(lang:any,confCallId:any,session:any,newSession:any)
    {
        console.log("I am in terminateConferenceCall");
        console.log("Doing bye to call");
        newSession.bye().catch((e)=>{
            console.warn("Failed to BYE", e);
        });

        this.headerVariableService.browserPhoneTitle = lang.call_ended;
        console.log("New call session end");

        console.log("Setting terminate veriable");
        // session.data.confcalls[confCallId].accept.complete = false;
        session.data.confcalls[confCallId].accept.disposition = "bye";
        session.data.confcalls[confCallId].accept.eventTime = this.utilsService.utcDateNow();

        this.headerVariableService.browserPhoneTitle = lang.conference_call_ended;

        console.log("Starting to cancel conference");
        window.setTimeout(()=>{
            this.cancelConference(lang,session,confCallId);
        }, 1000);
    }

    joinConferenceCall(lang:any,confCallId:any,session:any)
    {
        console.log("I am in joinConferenceCall");
         // Merge Call Audio
         if(!session.data.childsession){
            console.warn("Conference session lost");
            return;
        }

        let outputStreamForSession = new MediaStream();
        let outputStreamForConfSession = new MediaStream();

        let pc = session.sessionDescriptionHandler.peerConnection;
        let confPc = session.data.childsession.sessionDescriptionHandler.peerConnection;

        console.log("Get conf call input channel");
        // Get conf call input channel
        confPc.getReceivers().forEach((RTCRtpReceiver:any)=> {
            if(RTCRtpReceiver.track && RTCRtpReceiver.track.kind == "audio") {
                console.log("Adding conference session:", RTCRtpReceiver.track.label);
                outputStreamForSession.addTrack(RTCRtpReceiver.track);
            }
        });

        console.log("Get session input channel");
        // Get session input channel
        pc.getReceivers().forEach((RTCRtpReceiver:any)=> {
            if(RTCRtpReceiver.track && RTCRtpReceiver.track.kind == "audio") {
                console.log("Adding conference session:", RTCRtpReceiver.track.label);
                outputStreamForConfSession.addTrack(RTCRtpReceiver.track);
            }
        });

        console.log("Replace tracks of Parent Call");
        // Replace tracks of Parent Call
        pc.getSenders().forEach((RTCRtpSender:any)=> {
            if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                console.log("Switching to mixed Audio track on session");

                session.data.AudioSourceTrack = RTCRtpSender.track;
                outputStreamForSession.addTrack(RTCRtpSender.track);
                var mixedAudioTrack = this.utilsService.mixAudioStreams(outputStreamForSession).getAudioTracks()[0];
                mixedAudioTrack.IsMixedTrack = true;

                RTCRtpSender.replaceTrack(mixedAudioTrack);
            }
        });

        console.log("Replace tracks of Child Call");
        // Replace tracks of Child Call
        confPc.getSenders().forEach((RTCRtpSender:any)=> {
            if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                console.log("Switching to mixed Audio track on conf call");

                session.data.childsession.data.AudioSourceTrack = RTCRtpSender.track;
                outputStreamForConfSession.addTrack(RTCRtpSender.track);
                var mixedAudioTrackForConf = this.utilsService.mixAudioStreams(outputStreamForConfSession).getAudioTracks()[0];
                mixedAudioTrackForConf.IsMixedTrack = true;

                RTCRtpSender.replaceTrack(mixedAudioTrackForConf);
            }
        });

        this.headerVariableService.browserPhoneTitle = lang.call_in_progress;
        console.log("Conference Call In Progress");
        console.log("Setting join conference data");
        session.data.confcalls[confCallId].accept.complete = true;
        session.data.confcalls[confCallId].accept.disposition = "join";
        session.data.confcalls[confCallId].accept.eventTime = this.utilsService.utcDateNow();


        this.headerVariableService.browserPhoneTitle = lang.conference_call_in_progress;

        console.log("Take the parent call off hold after a second");
        // Take the parent call off hold after a second
        window.setTimeout(()=>{
            this.sessionService.unholdSession(lang,session);
        }, 1000);
    }

    conferenceDial(userAgent:any,lang:any,sessionData:any, dialNumber:any){

        console.log("I am in ConferenceDial");
        let dstNo:any = dialNumber;

        if(this.headerVariableService.enableAlphanumericDial){
            dstNo = String(dstNo).replace(this.headerVariableService.telAlphanumericRegEx, "").substring(0,this.headerVariableService.maxDidLength);
        }
        else {
            dstNo = String(dstNo).replace(this.headerVariableService.telNumericRegEx, "").substring(0,this.headerVariableService.maxDidLength);
        }

        console.log("dialNumber : ",dstNo);

        if(dstNo == ""){
            console.warn("Cannot transfer, must be [0-9*+#]");
            return;
        }
        
        let session = sessionData;
    
        this.headerVariableService.browserPhoneTitle = lang.connecting;

        if(!session.data.confcalls) session.data.confcalls = [];

        console.log("Pushing conf calls to session data");
        session.data.confcalls.push({ 
            to: dstNo, 
            startTime: this.utilsService.utcDateNow(), 
            disposition: "invite",
            dispositionTime: this.utilsService.utcDateNow(), 
            accept : {
                complete: null,
                eventTime: null,
                disposition: ""
            }
        });

        console.log("Setting confID");
        let confCallId = session.data.confcalls.length-1;
        console.log("confCallId : ",confCallId);
        this.headerVariableService.confCallId = confCallId;

        console.log("Setting SDP options");
        // SDP options
        let supportedConstraints = navigator.mediaDevices.getSupportedConstraints();
        let spdOptions = {
            sessionDescriptionHandlerOptions: {
                earlyMedia: true,
                constraints: {
                    audio: { deviceId : {} },
                    video: false
                }
            }
        }

        console.log("Setting audio device");
        if(session.data.AudioSourceDevice != "default"){
            spdOptions.sessionDescriptionHandlerOptions.constraints.audio.deviceId = { exact: session.data.AudioSourceDevice }
        }

        console.log("Add additional Constraints");
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


        // Unlikely this will work
        if(session.data.withvideo){
            spdOptions.sessionDescriptionHandlerOptions.constraints.video = true;

            console.log("Setting videoSource");
            if(session.data.VideoSourceDevice != "default"){
                spdOptions.sessionDescriptionHandlerOptions.constraints.video["deviceId"] = { exact: session.data.VideoSourceDevice }
            }

            console.log("Setting frame rate , height, aspectRatio");
            // Add additional Constraints
            if(supportedConstraints.frameRate && this.headerVariableService.maxFrameRate != "") {
                spdOptions.sessionDescriptionHandlerOptions.constraints.video["frameRate"] = this.headerVariableService.maxFrameRate;
            }
            if(supportedConstraints.height && this.headerVariableService.videoHeight != "") {
                spdOptions.sessionDescriptionHandlerOptions.constraints.video["height"] = this.headerVariableService.videoHeight;
            }
            if(supportedConstraints.aspectRatio && this.headerVariableService.videoAspectRatio != "") {
                spdOptions.sessionDescriptionHandlerOptions.constraints.video["aspectRatio"] =this.headerVariableService.videoAspectRatio;
            }
        }

    
        // Create new call session
        console.log("CONFERENCE INVITE: ", "sip:" + dstNo + "@" + ConstantsService.user.domain);
    
        let targetURI = UserAgent.makeURI("sip:"+ dstNo.replace(/#/g, "%23") + "@" + ConstantsService.user.domain);
        let newSession = new Inviter(userAgent, targetURI, spdOptions);

        console.log("targetURI: ", targetURI);
        console.log("newSession: ", newSession);

        newSession.data = {};

        newSession.delegate = {
            onBye: (sip:any)=>{
                console.log("Conference Dial Delegate onBye: New call session ended with BYE");
                this.headerVariableService.browserPhoneTitle = lang.call_ended;
                session.data.confcalls[confCallId].disposition = "bye";
                session.data.confcalls[confCallId].dispositionTime = this.utilsService.utcDateNow();
                this.headerVariableService.browserPhoneTitle = lang.conference_call_terminated;
        
                //Remove call UI as call has ended
                this.headerVariableService.callInProgress = false;
                this.headerVariableService.callStatus = "danger";
  
            },
            onSessionDescriptionHandler: (sdh:any, provisional:any)=>{

                console.log("Conference Dial Delegate onSessionDescriptionHandler");

                if (sdh) {
                    if(sdh.peerConnection){
                        sdh.peerConnection.ontrack = (event:any)=>{
                            let pc = sdh.peerConnection;
    
                            // Gets Remote Audio Track (Local audio is setup via initial GUM)
                            let remoteStream = new MediaStream();
                            pc.getReceivers().forEach((receiver:any) =>{
                                if(receiver.track && receiver.track.kind == "audio"){
                                    remoteStream.addTrack(receiver.track);
                                }
                            });
                           let remoteAudio = this.headerVariableService.remoteAudio;
                            remoteAudio.srcObject = remoteStream;
                            remoteAudio.onloadedmetadata = (e:any)=> {
                                if (typeof remoteAudio.sinkId !== 'undefined') {
                                    remoteAudio.setSinkId(session.data.AudioOutputDevice).then(()=>{
                                        //console.log("sinkId applied: "+ session.data.AudioOutputDevice);
                                    }).catch((e:any)=>{
                                        console.warn("Error using setSinkId: ", e);
                                    });
                                }
                                remoteAudio.play();
                            }
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
        }

        console.log("newSession.stateChange.addListener");
        // Make sure we always restore audio paths
        newSession.stateChange.addListener((newState:any)=>{
        if (newState == SessionState.Terminated) {

                console.log("Ends the mixed audio, and releases the mic");
                // Ends the mixed audio, and releases the mic
                if(session.data.childsession.data.AudioSourceTrack && session.data.childsession.data.AudioSourceTrack.kind == "audio"){
                    session.data.childsession.data.AudioSourceTrack.stop();
                }

                console.log("Restore Audio Stream as it was changed");
                // Restore Audio Stream as it was changed
                if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
                    var pc = session.sessionDescriptionHandler.peerConnection;
                    pc.getSenders().forEach((RTCRtpSender) =>{
                        if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                            RTCRtpSender.replaceTrack(session.data.AudioSourceTrack).then(()=>{
                                if(session.data.ismute){
                                    RTCRtpSender.track.enabled = false;
                                }
                                else {
                                    RTCRtpSender.track.enabled = true;
                                }
                            }).catch((e)=>{
                                console.error(e);
                            });
                            session.data.AudioSourceTrack = null;
                        }
                    });
                }
            }
        });


        console.log("Putting new session in childsession of original session");
        session.data.childsession = newSession;

        var inviterOptions = {
            requestDelegate: {
                onTrying: (sip:any)=>{
                  console.log("Conference Dial inviterOption onTrying");
                  this.headerVariableService.browserPhoneTitle = lang.ringing;
                  session.data.confcalls[confCallId].disposition = "trying";
                  session.data.confcalls[confCallId].dispositionTime = this.utilsService.utcDateNow();
                  this.headerVariableService.browserPhoneTitle = lang.conference_call_started;
                },
                onProgress:(sip:any)=>{
                    console.log("Conference Dial inviterOption onProgress");
                    this.headerVariableService.browserPhoneTitle = lang.ringing;
                    session.data.confcalls[confCallId].disposition = "progress";
                    session.data.confcalls[confCallId].dispositionTime = this.utilsService.utcDateNow();
                    this.headerVariableService.browserPhoneTitle = lang.conference_call_started;
                    this.sessionService.conferenceNewSession = newSession;
                    //cancelNewSession function code was written here for canelling when user was added to conference.
                },
                onRedirect:(sip:any)=>{
                    console.log("Conference Dial inviterOption onRedirect");
                    console.log("Redirect received:", sip);
                },
                onAccept:(sip:any)=>{
                    console.log("Conference Dial inviterOption onAccept");
                    this.headerVariableService.browserPhoneTitle =lang.call_in_progress;
                    session.data.confcalls[confCallId].complete = true;
                    session.data.confcalls[confCallId].disposition = "accepted";
                    session.data.confcalls[confCallId].dispositionTime = this.utilsService.utcDateNow();
    
                    // Join Call
                    // joinConferenceCall function data was here
    
                    // End Call
                    // terminateConferenceCall function code was here
                },
                onReject:(sip:any)=>{
                    console.log("Conference Dial inviterOption onReject");
                    console.log("New call session rejected: ", sip.message.reasonPhrase);
                    this.headerVariableService.browserPhoneTitle =lang.call_rejected;
                    session.data.confcalls[confCallId].disposition = sip.message.reasonPhrase;
                    session.data.confcalls[confCallId].dispositionTime = this.utilsService.utcDateNow();
                    this.headerVariableService.browserPhoneTitle =lang.conference_call_rejected;
                }
            }
        }

        console.log("Starting new conf session invite")
        newSession.invite(inviterOptions).catch((e)=>{
            console.warn("Failed to send INVITE:", e);
        });
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

