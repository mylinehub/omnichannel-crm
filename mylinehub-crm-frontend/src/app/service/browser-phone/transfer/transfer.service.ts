import { Injectable } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { HeaderVariableService } from '../../header-variable/header-variable.service';
import { SessionService } from '../session/session.service';
import { DialogComponent } from '../../../pages/employee/all-employees/dialog/dialog.component';
import { ConstantsService } from '../../constants/constants.service';
import { Inviter, UserAgent } from 'sip.js';
import { UtilsService } from '../utils/utils.service';

@Injectable({
  providedIn: 'root'
})
export class TransferService {

  constructor(private dialogService: NbDialogService,
    protected headerVariableService : HeaderVariableService,
    protected sessionService:SessionService,
    protected utilsService : UtilsService,
  ) { }

      //OnGoing Call Func

    // Call Transfer
    // =============
    startTransferSession(lang:any,sessionData:any){
      //console.log("I am in startTransferSession");
      if(this.headerVariableService.isConference){
          this.cancelConference(lang,sessionData);
          return;
      }
      this.sessionService.holdSession(lang,sessionData);
  }
  
  
  cancelTransferSession(lang:any,sessionData:any){
      //console.log("I am in cancelTransferSession");
      let session = sessionData;
      if(session.data.childsession){
          //console.log("Child Transfer call detected:", session.data.childsession.state);
          session.data.childsession.dispose().then(()=>{
              session.data.childsession = null;
          }).catch((error:any)=>{
              session.data.childsession = null;
              // Suppress message
          });
      }    
      this.sessionService.unholdSession(lang,session);
  }

  cancelAttendedTransferSession(lang:any,sessionData:any,transferId:any){
      //console.log("I am in cancelAttendedTransferSession");
      let session = sessionData;
      if(session.data.childsession){
          //console.log("Child Transfer call detected:", session.data.childsession.state);
          session.data.childsession.cancel().catch((error:any)=>{
              console.warn("Failed to CANCEL", error);
          });
          //newCallStatus.html(lang.call_cancelled);
          //console.log("New call session canceled");

          session.data.transfer[transferId].accept.complete = false;
          session.data.transfer[transferId].accept.disposition = "cancel";
          //session.data.transfer[transferId].accept.eventTime = this.utilsService.utcDateNow();

         this.headerVariableService.browserPhoneTitle = lang.attended_transfer_call_cancelled;
      }    
         
      this.sessionService.unholdSession(lang,session);
  }

  terminateAttendedTransferSession(lang:any,sessionData:any,transferId:any){
      //console.log("I am in terminateAttendedTransferSession");
      let session = sessionData;
      if(session.data.childsession){
          //console.log("Child Transfer call detected:", session.data.childsession.state);
          session.data.childsession.bye().catch((error:any)=>{
              console.warn("Failed to BYE", error);
          });
      }

      this.headerVariableService.browserPhoneTitle = lang.call_ended;
      //console.log("New call session end");

      session.data.transfer[transferId].accept.complete = false;
      session.data.transfer[transferId].accept.disposition = "bye";
      session.data.transfer[transferId].accept.eventTime = this.utilsService.utcDateNow();

      this.headerVariableService.browserPhoneTitle = lang.attended_transfer_call_ended;

      window.setTimeout(()=>{
          this.cancelTransferSession(lang,session);
      }, 1000);
  }

  blindTransfer(lang:any,sessionData:any) {
      //console.log("I am in blindTransfer");
      this.startTransferSession(lang,sessionData);

      let dstNo:any = ConstantsService.user.transfer_phone_2;
      if(this.headerVariableService.enableAlphanumericDial){
          dstNo = String(dstNo).replace(this.headerVariableService.telAlphanumericRegEx, "").substring(0,this.headerVariableService.maxDidLength);
      }
      else {
          dstNo = String(dstNo).replace(this.headerVariableService.telNumericRegEx, "").substring(0,this.headerVariableService.maxDidLength);
      }
      if(dstNo == ""){
          console.warn("Cannot transfer, no number");
          return;
      }
  
      let session = sessionData;
  
      if(!session.data.transfer) session.data.transfer = [];
      session.data.transfer.push({ 
          type: "Blind", 
          to: dstNo, 
          transferTime: this.utilsService.utcDateNow(), 
          disposition: "refer",
          dispositionTime: this.utilsService.utcDateNow(), 
          accept : {
              complete: null,
              eventTime: null,
              disposition: ""
          }
      });
      let transferId = session.data.transfer.length-1;
  
      let transferOptions  = { 
          requestDelegate: {
              onAccept: (sip:any)=>{
                  //console.log("Blind transfer Accepted");
  
                  session.data.terminateby = "us";
                  session.data.reasonCode = 202;
                  session.data.reasonText = "Transfer";
              
                  session.data.transfer[transferId].accept.complete = true;
                  session.data.transfer[transferId].accept.disposition = sip.message.reasonPhrase;
                  session.data.transfer[transferId].accept.eventTime = this.utilsService.utcDateNow();
  
                  // TODO: use lang pack
                this.showDialoge('Information','activity-outline','success', "Call Blind Transferred (Accepted)"); 
                                             
                  session.bye().catch((error:any)=>{
                      console.warn("Could not BYE after blind transfer:", error);
                  });
                  this.sessionService.teardownSession(lang,session);
              },
              onReject:(sip:any)=>{
                  console.warn("REFER rejected:", sip);
  
                  session.data.transfer[transferId].accept.complete = false;
                  session.data.transfer[transferId].accept.disposition = sip.message.reasonPhrase;
                  session.data.transfer[transferId].accept.eventTime = this.utilsService.utcDateNow();
                  this.showDialoge('Information','activity-outline','danger', "Call Blind Transferred (Rejected)"); 
                
                  // Session should still be up, so just allow them to try again
              }
          }
      }
      //console.log("REFER: ", dstNo + "@" + ConstantsService.user.domain);
      let referTo = UserAgent.makeURI("sip:"+ dstNo.replace(/#/g, "%23") + "@" + ConstantsService.user.domain);
      session.refer(referTo, transferOptions).catch((error:any)=>{
          console.warn("Failed to REFER", error);
      });;

  }

  attendedTransfer(userAgent:any,lang:any,sessionData:any){
      //console.log("I am in attendedTransfer");
      this.startTransferSession(lang,sessionData);

      let dstNo:any = ConstantsService.user.transfer_phone_1;

      if(this.headerVariableService.enableAlphanumericDial){
          dstNo = String(dstNo).replace(this.headerVariableService.telAlphanumericRegEx, "").substring(0,this.headerVariableService.maxDidLength);
      }
      else {
          dstNo = String(dstNo).replace(this.headerVariableService.telNumericRegEx, "").substring(0,this.headerVariableService.maxDidLength);
      }
      if(dstNo == ""){
          console.warn("Cannot transfer, no number");
          return;
      }
      

      let session = sessionData;
      this.headerVariableService.browserPhoneTitle = lang.connecting;

      if(!session.data.transfer) session.data.transfer = [];
      session.data.transfer.push({ 
          type: "Attended", 
          to: dstNo, 
          transferTime: this.utilsService.utcDateNow(), 
          disposition: "invite",
          dispositionTime: this.utilsService.utcDateNow(), 
          accept : {
              complete: null,
              eventTime: null,
              disposition: ""
          }
      });
      let transferId = session.data.transfer.length-1;
      this.headerVariableService.attendedTransferId = transferId;

      // SDP options
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
      if(session.data.AudioSourceDevice != "default"){
          spdOptions.sessionDescriptionHandlerOptions.constraints.audio.deviceId = { exact: session.data.AudioSourceDevice }
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
  
      // Not sure if its possible to transfer a Video call???
      if(session.data.withvideo){
          spdOptions.sessionDescriptionHandlerOptions.constraints.video = true;
          if(session.data.VideoSourceDevice != "default"){
              spdOptions.sessionDescriptionHandlerOptions.constraints.video["deviceId"] = { exact: session.data.VideoSourceDevice }
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
      }
  
      // Create new call session
      //console.log("TRANSFER INVITE: ", "sip:" + dstNo + "@" + ConstantsService.user.domain);
      let targetURI = UserAgent.makeURI("sip:"+ dstNo.replace(/#/g, "%23") + "@" + ConstantsService.user.domain);
      let newSession = new Inviter(userAgent, targetURI, spdOptions);
      newSession.data = {}
      newSession.delegate = {
          onBye: (sip:any)=>{
              //console.log("New call session ended with BYE");
              this.headerVariableService.browserPhoneTitle = lang.call_ended;
              session.data.transfer[transferId].disposition = "bye";
              session.data.transfer[transferId].dispositionTime = this.utilsService.utcDateNow();
              //Calls gets disconnected

          },
          onSessionDescriptionHandler: (sdh:any, provisional:any)=>{
              if (sdh) {
                  if(sdh.peerConnection){
                      sdh.peerConnection.ontrack = (event:any)=>{
                          let pc = sdh.peerConnection;
  
                          // Gets Remote Audio Track (Local audio is setup via initial GUM)
                          let remoteStream = new MediaStream();
                          pc.getReceivers().forEach((receiver:any) => {
                              if(receiver.track && receiver.track.kind == "audio"){
                                  remoteStream.addTrack(receiver.track);
                              }
                          });
                         let remoteAudio:any = this.headerVariableService.remoteAudio;
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
      session.data.childsession = newSession;
      let inviterOptions = {
          requestDelegate: {
              onTrying: (sip:any)=>{
                  this.headerVariableService.browserPhoneTitle = lang.trying;
                  session.data.transfer[transferId].disposition = "trying";
                  session.data.transfer[transferId].dispositionTime = this.utilsService.utcDateNow();
                  this.headerVariableService.browserPhoneTitle = lang.attended_transfer_call_started;
              },
              onProgress:(sip:any)=>{
                  this.headerVariableService.browserPhoneTitle = lang.ringing;
                  session.data.transfer[transferId].disposition = "progress";
                  session.data.transfer[transferId].dispositionTime = this.utilsService.utcDateNow();
                  this.cancelAttendedTransferSession(lang,session,transferId);
                  this.headerVariableService.browserPhoneTitle = lang.attended_transfer_call_started;
              },
              onRedirect:(sip:any)=>{
                  //console.log("Redirect received:", sip);
              },
              onAccept:(sip:any)=>{
                  this.headerVariableService.browserPhoneTitle = lang.call_in_progress;

                  session.data.transfer[transferId].disposition = "accepted";
                  session.data.transfer[transferId].dispositionTime = this.utilsService.utcDateNow();
                  
                  let transferOptions  = { 
                      requestDelegate: {
                          onAccept: (sip:any)=>{
                              //console.log("Attended transfer Accepted");

                              session.data.terminateby = "us";
                              session.data.reasonCode = 202;
                              session.data.reasonText = "Attended Transfer";

                              session.data.transfer[transferId].accept.complete = true;
                              session.data.transfer[transferId].accept.disposition = sip.message.reasonPhrase;
                              session.data.transfer[transferId].accept.eventTime = this.utilsService.utcDateNow();

                            this.showDialoge('Information','activity-outline','success', "Call Attended Transferred (Accepted)"); 
                              // We must end this session manually
                              session.bye().catch((error:any)=>{
                                  console.warn("Could not BYE after blind transfer:", error);
                              });

                              this.sessionService.teardownSession(lang,session);
                          },
                          onReject: (sip:any)=>{
                              console.warn("Attended transfer rejected:", sip);

                              session.data.transfer[transferId].accept.complete = false;
                              session.data.transfer[transferId].accept.disposition = sip.message.reasonPhrase;
                              session.data.transfer[transferId].accept.eventTime = this.utilsService.utcDateNow();

                              this.showDialoge('Information','activity-outline','danger', "Call Attended Transferred (Rejected)"); 
            
                          }
                      }
                  }
      
                  // Send REFER
                  session.refer(newSession, transferOptions).catch((error:any)=>{
                      console.warn("Failed to REFER", error);
                  });
      
                 this.headerVariableService.browserPhoneTitle = lang.attended_transfer_complete;
              //    this.terminateAttendedTransferSession(session,transferId);
              //    this.headerVariableService.browserPhoneTitle = lang.attended_transfer_call_ended;


              },
              onReject:(sip:any)=>{
                  //console.log("New call session rejected: ", sip.message.reasonPhrase);
                  this.headerVariableService.browserPhoneTitle = lang.call_rejected;
                  session.data.transfer[transferId].disposition = sip.message.reasonPhrase;
                  session.data.transfer[transferId].dispositionTime = this.utilsService.utcDateNow();
                  this.headerVariableService.browserPhoneTitle = lang.attended_transfer_call_rejected;
              }
          }
      }
      newSession.invite(inviterOptions).catch((e:any)=>{
          console.warn("Failed to send INVITE:", e);
      });
  }

  // This method had to be created here as well to avoid circular dependency between transfer and conference browser phone service
  cancelConference(lang:any,sessionData:any){
    //console.log("I am in cancelConference");
    let session = sessionData;
    if(session.data.childsession){
        //console.log("Child Conference call detected:", session.data.childsession.state);
        session.data.childsession.dispose().then(()=>{
            session.data.childsession = null;
        }).catch((error:any)=>{
            session.data.childsession = null;
            // Suppress message
        });
    }
    this.sessionService.unholdSession(lang,session);
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
