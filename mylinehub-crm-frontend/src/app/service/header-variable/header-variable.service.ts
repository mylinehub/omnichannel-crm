import { Injectable } from '@angular/core';
import { Observable, Observer } from 'rxjs';
import { UserAgent,Registerer,RegistererState,SubscriptionState,Subscriber,Inviter,SessionState,Messager } from 'sip.js';


@Injectable({
  providedIn: 'root'
})
export class HeaderVariableService {
  dotClass = "dotOffline";
  selfState = "terminated";
  selfPresence:any="danger";
  notificationBadgeDot:any = true;
  messageBadgeDot:any = false;
  userBackgroundColor = "#efefef";
  
  hostingPrefix:any = '';  
  loadAlternateLang:any = 1; // Enables searching and loading for the additional language packs other thAan /en.json
  availableLang:any = ["ja", "zh-hans", "zh", "ru", "tr", "nl", "es", "de", "pl", "pt-br"]; // Defines the language packs (.json) available in /lang/ folder
  navuserAgent:any = window.navigator.userAgent;  // TODO: change to Navigator.this.userAgentData
  voiceMailSubscribe:any = 1                // Enable Subscribe to voicemail
  voicemailDid:any = "";    
  subscribeToYourself: any;
  subscribeBuddyAccept = "application/pidf+xml";
  subscribeBuddyDialog = "application/dialog-info+xml";  // Normally only application/dialog-info+xml and application/pidf+xml
  subscribeBuddyEvent = "presence";                // For application/pidf+xml use presence. For application/dialog-info+xml use dialog 
  subscribeBuddyDialogEvent = "dialog";     
  subscribeBuddyExpires = 3000; 
  subscribeVoicemailExpires: any;
  
  //Header Own Browser Phone Varibles
  hasVideoDevice:any = false;
  hasAudioDevice:any = false;
  hasSpeakerDevice:any = false;
  maxVideoBandwidth : any = 2048;
  videoAspectRatio : any = 1.33;
  phoneOptions:any = {
    loadAlternateLang: true
  }
  
  browserphoneToolTip:any = 'browser-phone-online';
  videoToolTip:any = 'shared-screen-video';
  videoIcon:any = 'monitor-outline';
  browserPhoneActionToolTip:any = 'actions';
  incomingToolTip:any = 'incoming-call'
  outgoingToolTip:any = 'outgoing-call'
  incomingIcon:any = 'arrowhead-down-outline'
  outgoingIcon:any = 'arrowhead-up-outline'
  //incomingOutgoingIcon = 'arrowhead-up-outline'
  callRinging:any = false;
  callInProgress:any = false;
  recordAllCalls:any = false;
  intercomPolicy:any = true;
  freeDial:any = true;
  textDictate:any = false;
  textMessaging:any = false;

  //ToImplement
  callWaiting:any = false;
  selectRingingLine:any = [];

   //Headphone Button Data
   callStatus:any = "danger";
   isOnlineStatus:any = "danger";
   dialValue:any = '';
   isBrowserPhoneOnline:any = false;
   timer:any = "00:00:00";
   micList : any = ['default','audio1','audio2'];
   videoList : any = ['default','video1','video2','video3'];
   speakerList: any = ['default','speaker1','speaker2','spekaer3'];
   videoOrientation : any =  ['Normal','Mirror'];
   videoQuality : any =  ['HQVGA','QVGA','VGA','HD'];
   videoFrameRate : any = ['5','15','25','30','60'];
   autoGainControl : any = ['true','flase'];
   echoCancellation : any = ['true','flase'];
   noiseSeperation : any = ['true','flase'];
   currentMic:any = [false,false,true];
   currentVideo:any = [true,false,false,false];
   currentSpeaker:any = [false,true,false,false];
   currentVideoOrientation : any = [true,false];
   currentVideoQuality : any = [true,false,false,false];
   currentVideoFrameRate : any = [true,false,false,false,false];
   currentAutoGainControl:any = [true,false];
   currentEchoCancellation:any = [true,false];
   currentNoiseSeperation:any = [true,false];
   
   currentMicStringValue:any = "";
   currentVideoStringValue:any = "";
   currentSpeakerStringValue:any = "";
   currentVideoOrientationStringValue:any = "";
   currentVideoQualityStringValue: any = "";
   currentVideoFrameRateStringValue: any  = "";
   currentAutoGainControlStringValue:any  = "";
   currentEchoCancellationStringValue:any  = "";
   currentNoiseSeperationStringValue:any  = "";

   browserPhoneTitle:any ='Browser Phone Activated';
   autoAnswer :any = true;
   autoConference :any = false;
   autoVideo :any = true;
   doNotDisturb:any = false;
   startVideoFullScreen:any = false;

 //Browser action button input data
 isMute:any =false;
 isHold:any =false;
 isTransfer: any = false;
 isRecording: any = false;
 isConference : any = false;
 isVideoStream : any = false;

  //ReceiveDial button input data

 callType= "Incoming-Call";
 callerName : any = 'Vijay Kumar';
 phoneNumber : any = '9876756465';
 isVideoCall : any = false;
 isInternal : any = false;

 //Video Component Variables
 videoComponentTitle:any= "Video-Dialog"
 messagefullScreenEnabled = false;
 participantHeight:any = 450;
 messageParticipantWidth:any = 450;
 participantWidth:any = 450;
 messageParticipantHeight:any = 450;
//  xPosition:any = 0;
//  yPosition:any = 0;
 boundingClientRect: any={};
 messageBoundingClientRect: any={};
 dragPosition:any = {x: 0, y: 0};
 messageDragPosition:any = {x: 0, y: 0};
 previousDragPosition:any = {x: 0, y: 0};
 messagePreviousDragPosition:any = {x: 0, y: 0};

 toolTip:any = 'preview-video'

  maxFrameRate: string = "30";
  videoHeight: string = "300";

  recordingVideoFps: number = 12;
  recordingLayout: string = 'them-pnp';
  recordingVideoSize: string = 'HD';

  remoteAudio: any;

  remoteAudioStream:MediaStream;
  remoteVideoStream:MediaStream;

  mid: any;
  localVideo: any;
  recordingCanvas: any;
  maxDidLength: any;
  telAlphanumericRegEx: any;
  telNumericRegEx: any;
  enableAlphanumericDial: any = false;
  attendedTransferId: number;
  confCallId: number;
  videoResampleSize: string;
  sharedVideo: any;
  resampleCanvas: any;

  constructor() { 
  }

//Browser Phone Component Methonds. Below are used to prepare above varibale lists containing true/false
  setMicDeviceVariable()
  {
    var micDeviceValue = new Observable((observer: Observer<String>) => {

      try{
        observer.next(this.currentMicStringValue);
      }
      catch(e:any){
          //console.log(e);
      }
      finally{
        observer.complete();
      }
      
     });
   
     micDeviceValue.subscribe((data:any)=>{
        //console.log('micDeviceValue',data);    // output - ‘Hi Observable’
        
        let setDefault = true;
        this.currentMic = [];
         //setting theme as per user prefrence
         //console.log('this.micList',this.micList)
         this.micList.forEach( (element:any,index:number) => {
         //console.log(element);
           if(element === data)
           {
             //console.log('above value is equal');
             setDefault = false;
             this.currentMic.push(true);
           }
           else{
            //console.log('above value is unequal');
            this.currentMic.push(false);
           }
         });

         if (setDefault)
         {
          //console.log('setting default');
          this.currentMic[0] = true;
         }
    });
  }

  setVideoDeviceVariable()
  {
    var videoDeviceValue = new Observable((observer: Observer<String>) => {

      try{
        observer.next(this.currentVideoStringValue);
      }
      catch(e:any){
          //console.log(e);
      }
      finally{
        observer.complete();
      }
      
     });
   
     videoDeviceValue.subscribe((data:any)=>{
        //console.log(data);    // output - ‘Hi Observable’
        
        let setDefault = true;
        this.currentVideo = [];
         //setting theme as per user prefrence
         this.videoList.forEach( (element:any,index:number) => {
         //console.log(element.value);
         //console.log(ConstantsService.user.uiTheme);
        //console.log(element); 
           if(element === data)
           {
            //console.log("true"); 
             setDefault = false;
             this.currentVideo.push(true);
           }
           else{
            this.currentVideo.push(false);
           }
         });

         if (setDefault)
         {
          this.currentVideo[0] = true;
         }
    });
  }

  setSpeakerDeviceVariable()
  {
    var setSpeakerDeviceValue = new Observable((observer: Observer<String>) => {

      try{
        observer.next(this.currentSpeakerStringValue);
      }
      catch(e:any){
          //console.log(e);
      }
      finally{
        observer.complete();
      }
      
     });
   
     setSpeakerDeviceValue.subscribe((data:any)=>{
      //console.log(data);    // output - ‘Hi Observable’
        
        let setDefault = true;
        this.currentSpeaker = [];
         //setting theme as per user prefrence
         this.speakerList.forEach( (element:any,index:number) => {
         //console.log(element.value);
         //console.log(ConstantsService.user.uiTheme);
           if(element === data)
           {
             setDefault = false;
             this.currentSpeaker.push(true);
           }
           else{
            this.currentSpeaker.push(false);
           }
         });

         if (setDefault)
         {
          this.currentSpeaker[0] = true;
         }
    });
  }

  setVideoOrientationVariable()
  {
    var videoOrientationValue = new Observable((observer: Observer<String>) => {

      try{
        observer.next(this.currentVideoOrientationStringValue);
      }
      catch(e:any){
          //console.log(e);
      }
      finally{
        observer.complete();
      }
      
     });
   
     videoOrientationValue.subscribe((data:any)=>{
       //console.log(data);    // output - ‘Hi Observable’
        
        let setDefault = true;
        this.currentVideoOrientation = [];
         //setting theme as per user prefrence
         this.videoOrientation.forEach( (element:any,index:number) => {
         //console.log(element.value);
         //console.log(ConstantsService.user.uiTheme);
           if(element === data)
           {
             setDefault = false;
             this.currentVideoOrientation.push(true);
           }
           else{
            this.currentVideoOrientation.push(false);
           }
         });

         if (setDefault)
         {
          this.currentVideoOrientation[0] = true;
         }
    });
  }

  setVideoQualityVariable()
  {
    var videoQualityValue = new Observable((observer: Observer<String>) => {

      try{
        observer.next(this.currentVideoQualityStringValue);
      }
      catch(e:any){
          //console.log(e);
      }
      finally{
        observer.complete();
      }
      
     });
   
     videoQualityValue.subscribe((data:any)=>{
        //console.log(data);    // output - ‘Hi Observable’
        
        let setDefault = true;
        this.currentVideoQuality = [];
         //setting theme as per user prefrence
         this.videoQuality.forEach( (element:any,index:number) => {
         //console.log(element.value);
         //console.log(ConstantsService.user.uiTheme);
           if(element === data)
           {
             setDefault = false;
             this.currentVideoQuality.push(true);
           }
           else{
            this.currentVideoQuality.push(false);
           }
         });

         if (setDefault)
         {
          this.currentVideoQuality[0] = true;
         }
    });
  }

  setVideoFrameRateVariable()
  {
    var videoFrameRateValue = new Observable((observer: Observer<String>) => {

      try{
        observer.next(this.currentVideoFrameRateStringValue);
      }
      catch(e:any){
          //console.log(e);
      }
      finally{
        observer.complete();
      }
      
     });
   
     videoFrameRateValue.subscribe((data:any)=>{
       //console.log(data);    // output - ‘Hi Observable’
        
        let setDefault = true;
        this.currentVideoFrameRate = [];
         //setting theme as per user prefrence
         this.videoFrameRate.forEach( (element:any,index:number) => {
         //console.log(element.value);
         //console.log(ConstantsService.user.uiTheme);
           if(element === data)
           {
             setDefault = false;
             this.currentVideoFrameRate.push(true);
           }
           else{
            this.currentVideoFrameRate.push(false);
           }
         });

         if (setDefault)
         {
          this.currentVideoFrameRate[0] = true;
         }
    });
  }

  setAutoGainControlVariable()
  {
    var autoGainControlValue = new Observable((observer: Observer<String>) => {

      try{
        observer.next(this.currentAutoGainControlStringValue);
      }
      catch(e:any){
          //console.log(e);
      }
      finally{
        observer.complete();
      }
      
     });
   
     autoGainControlValue.subscribe((data:any)=>{
       //console.log(data);    // output - ‘Hi Observable’
        
        let setDefault = true;
        this.currentAutoGainControl = [];
         //setting theme as per user prefrence
         this.autoGainControl.forEach( (element:any,index:number) => {
         //console.log(element.value);
         //console.log(ConstantsService.user.uiTheme);
           if(element === data)
           {
             setDefault = false;
             this.currentAutoGainControl.push(true);
           }
           else{
            this.currentAutoGainControl.push(false);
           }
         });

         if (setDefault)
         {
          this.currentAutoGainControl[0] = true;
         }
    });
  }

  setEchoCancellationVariable()
  {
    var echoCancellationValue = new Observable((observer: Observer<String>) => {

      try{
        observer.next(this.currentEchoCancellationStringValue);
      }
      catch(e:any){
          //console.log(e);
      }
      finally{
        observer.complete();
      }
      
     });
   
     echoCancellationValue.subscribe((data:any)=>{
        //console.log(data);    // output - ‘Hi Observable’
        
        let setDefault = true;
        this.currentEchoCancellation = [];
         //setting theme as per user prefrence
         this.echoCancellation.forEach( (element:any,index:number) => {
         //console.log(element.value);
         //console.log(ConstantsService.user.uiTheme);
           if(element === data)
           {
             setDefault = false;
             this.currentEchoCancellation.push(true);
           }
           else{
            this.currentEchoCancellation.push(false);
           }
         });

         if (setDefault)
         {
          this.currentEchoCancellation[0] = true;
         }
    });
  }

  setNoiseSeperationVariable()
  {
    var noiseSeperationValue = new Observable((observer: Observer<String>) => {

      try{
        observer.next(this.currentNoiseSeperationStringValue);
      }
      catch(e:any){
          //console.log(e);
      }
      finally{
        observer.complete();
      }
      
     });
   
     noiseSeperationValue.subscribe((data:any)=>{
       //console.log(data);    // output - ‘Hi Observable’
        
        let setDefault = true;
        this.currentNoiseSeperation = [];
         //setting theme as per user prefrence
         this.noiseSeperation.forEach( (element:any,index:number) => {
         //console.log(element.value);
         //console.log(ConstantsService.user.uiTheme);
           if(element === data)
           {
             setDefault = false;
             this.currentNoiseSeperation.push(true);
           }
           else{
            this.currentNoiseSeperation.push(false);
           }
         });

         if (setDefault)
         {
          this.currentNoiseSeperation[0] = true;
         }
    });
  }

}
