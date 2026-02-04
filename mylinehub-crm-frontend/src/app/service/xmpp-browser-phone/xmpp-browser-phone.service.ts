import { Injectable } from '@angular/core';
import { UserAgent,Registerer,RegistererState,SubscriptionState,Subscriber,Inviter,SessionState,Messager } from 'sip.js';
import {
    Strophe,
    $msg,
    $pres,
    $iq
} from '../../../assets/data/strophe.js';

import * as moment from 'moment';
import { Canvas } from 'leaflet';
import { ConstantsService } from '../constants/constants.service';

@Injectable({
  providedIn: 'root'
})
export class XmppBrowserPhoneService {

  // // Global Settings
  // // ===============
  appversion:any = "0.0.01";
  sipjsversion:any = "0.20.0";
  loadAlternateLang:any = (getDbItem("loadAlternateLang", "0") == "1"); // Enables searching and loading for the additional language packs other thAan /en.json
  availableLang:any = ["ja", "zh-hans", "zh", "ru", "tr", "nl", "es", "de", "pl", "pt-br"]; // Defines the language packs (.json) available in /lang/ folder
  
  navuserAgent:any = window.navigator.userAgent;  // TODO: change to Navigator.this.userAgentData
  userAgent:any;
  wssServer:any = getDbItem("wssServer", null);           // eg: raspberrypi.local
  webSocketPort:any = getDbItem("webSocketPort", null);   // eg: 444 | 4443
  serverPath:any = getDbItem("serverPath", null);         // eg: /ws
  sipDomain:any = getDbItem("sipDomain", null);           // eg: raspberrypi.local
  sipUsername:any = getDbItem("sipUsername", null);       // eg: webrtc
  sipPassword:any = getDbItem("sipPassword", null);       // eg: webrtc
  lang:any
  
  //Timeout
  transportConnectionTimeout:any = parseInt(getDbItem("transportConnectionTimeout", 15));          // The timeout in seconds for the initial connection to make on the web socket port
  transportReconnectionAttempts:any = parseInt(getDbItem("transportReconnectionAttempts", 999));   // The number of times to attempt to reconnect to a WebSocket when the connection drops.
  transportReconnectionTimeout:any = parseInt(getDbItem("transportReconnectionTimeout", 3));       // The time in seconds to wait between WebSocket reconnection attempts.
  noAnswerTimeout:any = parseInt(getDbItem("noAnswerTimeout", 120));          // Time in seconds before automatic Busy Here sent
  
  
  bundlePolicy:any = getDbItem("bundlePolicy", "balanced");                              // SDP Media Bundle: max-bundle | max-compat | balanced https://webrtcstandards.info/sdp-bundle/
  iceStunServerJson:any = getDbItem("iceStunServerJson", "");                            // Sets the JSON string for ice Server. Default: [{ "urls": "stun:stun.l.google.com:19302" }] Must be https://developer.mozilla.org/en-US/docs/Web/API/RTCConfiguration/iceServers
  iceStunCheckTimeout:any = parseInt(getDbItem("iceStunCheckTimeout", 500));        
  
  userAgentStr:any = getDbItem("UserAgentStr", "Browser Phone "+ this.appversion +" (SIPJS - "+ this.sipjsversion +") "+ window.navigator.userAgent);   // Set this to whatever you want.
  hostingPrefix:any = getDbItem("HostingPrefix", "");  
  profileDisplayPrefix:any = getDbItem("profileDisplayPrefix", "");                      // Can display an item from you vCard before you name. Options: Number1 | Number2
  profileDisplayPrefixSeparator:any = getDbItem("profileDisplayPrefixSeparator", "");    // Used with profileDisplayPrefix, adds a separating character (string). eg: - ~ * or even 💥
  registerExpires:any = parseInt(getDbItem("registerExpires", 300));                     // Registration expiry time (in seconds)
  registerExtraHeaders:any = getDbItem("registerExtraHeaders", "{}");                    // Parsable Json string of headers to include in register process. eg: '{"foo":"bar"}'
  registerExtraContactParams:any = getDbItem("registerExtraContactParams", "{}");        // Parsable Json string of extra parameters add to the end (after >) of contact header during register. eg: '{"foo":"bar"}'
  registerContactParams:any = getDbItem("registerContactParams", "{}");                  // Parsable Json string of extra parameters added to contact URI during register. eg: '{"foo":"bar"}'
  wssInTransport:any = (getDbItem("wssInTransport", "1") == "1");                        // Set the transport parameter to wss when used in SIP URIs. (Required for Asterisk as it doesn't support Path)
  ipInContact:any = (getDbItem("ipInContact", "1") == "1"); 
  subscribeToYourself:any = (getDbItem("subscribeToYourself", "0") == "1");              // Enable Subscribe to your own uri. (Useful to understand how other buddies see you.)
  voiceMailSubscribe:any = (getDbItem("voiceMailSubscribe", "1") == "1");                // Enable Subscribe to voicemail
  voicemailDid:any = getDbItem("voicemailDid", "");                                      // Number to dial for VoicemialMain()
  subscribeVoicemailExpires:any = parseInt(getDbItem("subscribeVoicemailExpires", 300)); // Voceimail Subscription expiry time (in seconds)
  contactUserName:any = getDbItem("contactUserName", "");  
  profileUserID:any = getDbItem("profileUserID", null);   // Internal reference ID. (DON'T CHANGE THIS!)
  profileName:any = getDbItem("profileName", null);       // eg: Keyla James
  
  
  autoAnswerEnabled:any = (getDbItem("autoAnswerEnabled", "0") == "1");       // Automatically answers the phone when the call comes in, if you are not on a call already
  doNotDisturbEnabled :any= (getDbItem("doNotDisturbEnabled", "0") == "1");   // Rejects any inbound call, while allowing outbound calls
  callWaitingEnabled:any = (getDbItem("callWaitingEnabled", "1") == "1");     // Rejects any inbound call if you are on a call already.
  recordAllCalls:any = (getDbItem("recordAllCalls", "0") == "1");             // Starts Call Recording when a call is established.
  startVideoFullScreen:any = (getDbItem("startVideoFullScreen", "1") == "1"); // Starts a video call in the full screen (browser screen, not desktop)
  selectRingingLine:any = (getDbItem("selectRingingLine", "1") == "1");       // Selects the ringing line if you are not on another call ()
  
  // Permission Settings
  enableTextMessaging:any = (getDbItem("enableTextMessaging", "1") == "1");               // Enables the Text Messaging
  disableFreeDial:any = (getDbItem("disableFreeDial", "0") == "1");                       // Removes the Dial icon in the profile area, users will need to add buddies in order to dial.
  disableBuddies:any = (getDbItem("disableBuddies", "0") == "1");                         // Removes the Add Someone menu item and icon from the profile area. Buddies will still be created automatically. Please also use maxBuddies or maxBuddyAge
  enableTransfer:any = (getDbItem("enableTransfer", "1") == "1");                         // Controls Transferring during a call
  enableConference:any = (getDbItem("enableConference", "1") == "1");                     // Controls Conference during a call
  autoAnswerPolicy:any = getDbItem("autoAnswerPolicy", "allow");                          // allow = user can choose | disabled = feature is disabled | enabled = feature is always on
  doNotDisturbPolicy:any = getDbItem("doNotDisturbPolicy", "allow");                      // allow = user can choose | disabled = feature is disabled | enabled = feature is always on
  callWaitingPolicy:any = getDbItem("callWaitingPolicy", "allow");                        // allow = user can choose | disabled = feature is disabled | enabled = feature is always on
  callRecordingPolicy:any = getDbItem("callRecordingPolicy", "allow");                    // allow = user can choose | disabled = feature is disabled | enabled = feature is always on
  intercomPolicy:any = getDbItem("intercomPolicy", "enabled");                            // disabled = feature is disabled | enabled = feature is always on
  enableAccountSettings:any = (getDbItem("enableAccountSettings", "1") == "1");           // Controls the Account tab in Settings
  enableAppearanceSettings:any = (getDbItem("enableAppearanceSettings", "1") == "1");     // Controls the Appearance tab in Settings
  enableNotificationSettings:any = (getDbItem("enableNotificationSettings", "1") == "1"); // Controls the Notifications tab in Settings
  enableAlphanumericDial:any = (getDbItem("enableAlphanumericDial", "0") == "1");         // Allows calling /[^\da-zA-Z\*\#\+\-\_\.\!\~\'\(\)]/g default is /[^\d\*\#\+]/g 
  enableVideoCalling:any = (getDbItem("enableVideoCalling", "1") == "1");                 // Enables Video during a call
  enableTextExpressions:any = (getDbItem("enableTextExpressions", "1") == "1");           // Enables Expressions (Emoji) glyphs when texting
  enableTextDictate :any= (getDbItem("enableTextDictate", "1") == "1");                   // Enables Dictate (speech-to-text) when texting
  enableRingtone:any = (getDbItem("enableRingtone", "1") == "1");                         // Enables a ring tone when an inbound call comes in.  (media/Ringtone_1.mp3)
  maxBuddies :any= parseInt(getDbItem("maxBuddies", 999));                                // Sets the Maximum number of buddies the system will accept. Older ones get deleted. (Considered when(after) adding buddies)
  maxBuddyAge :any= parseInt(getDbItem("maxBuddyAge", 365));                              // Sets the Maximum age in days (by latest activity). Older ones get deleted. (Considered when(after) adding buddies)
  autoDeleteDefault :any= (getDbItem("autoDeleteDefault", "1") == "1");                   // For automatically created buddies (inbound and outbound), should the buddy be set to AutoDelete.
  
  chatEngine:any = getDbItem("chatEngine", "SIMPLE");    // Select the chat engine XMPP | SIMPLE
  
  autoGainControl :any= (getDbItem("autoGainControl", "1") == "1");        // Attempts to adjust the microphone volume to a good audio level. (OS may be better at this)
  echoCancellation :any= (getDbItem("echoCancellation", "1") == "1");      // Attempts to remove echo over the line.
  noiseSuppression:any = (getDbItem("noiseSuppression", "1") == "1");      // Attempts to clear the call quality of noise.
  mirrorVideo:any = getDbItem("VideoOrientation", "rotateY(180deg)");      // Displays the self-preview in normal or mirror view, to better present the preview. 
  maxFrameRate:any = getDbItem("FrameRate", "");                           // Suggests a frame rate to your webcam if possible.
  videoHeight:any = getDbItem("VideoHeight", "");                          // Suggests a video height (and therefor picture quality) to your webcam.
  maxVideoBandwidth:any = parseInt(getDbItem("maxVideoBandwidth", "2048")); // Specifies the maximum bandwidth (in Kb/s) for your outgoing video stream. e.g: 32 | 64 | 128 | 256 | 512 | 1024 | 2048 | -1 to disable
  videoAspectRatio:any = getDbItem("AspectRatio", "1.33");                  // Suggests an aspect ratio (1:1 = 1 | 4:3 = 0.75 | 16:9 = 0.5625) to your webcam.
  notificationsActive:any = (getDbItem("Notifications", "0") == "1");
  
  streamBuffer :any= parseInt(getDbItem("streamBuffer", 50));                 // The amount of rows to buffer in the Buddy Stream
  maxDataStoreDays:any = parseInt(getDbItem("maxDataStoreDays", 0));          // Defines the maximum amount of days worth of data (calls, recordings, messages, etc) to store locally. 0=Stores all data always. >0 Trims n days back worth of data at various events where. 
  posterJpegQuality:any = parseFloat(getDbItem("posterJpegQuality", 0.6));    // The image quality of the Video Poster images
  videoResampleSize:any = getDbItem("videoResampleSize", "HD");               // The resample size (height) to re-render video that gets presented (sent). (SD = ???x360 | HD = ???x720 | FHD = ???x1080)
  recordingVideoSize:any = getDbItem("recordingVideoSize", "HD");             // The size/quality of the video track in the recordings (SD = 640x360 | HD = 1280x720 | FHD = 1920x1080)
  recordingVideoFps:any = parseInt(getDbItem("recordingVideoFps", 12));       // The Frame Per Second of the Video Track recording
  recordingLayout:any = getDbItem("recordingLayout", "them-pnp");             // The Layout of the Recording Video Track (side-by-side | them-pnp | us-only | them-only)
  
  didLength :any= parseInt(getDbItem("didLength", 6));                 // DID length from which to decide if an incoming caller is a "contact" or an "extension".
  maxdidLength :any= parseInt(getDbItem("maxdidLength", 16));          // Maximum length of any DID number including international dialled numbers.
  displayDateFormat:any = getDbItem("DateFormat", "YYYY-MM-DD");       // The display format for all dates. https://momentjs.com/docs/#/displaying/
  displayTimeFormat:any = getDbItem("TimeFormat", "h:mm:ss A");        // The display format for all times. https://momentjs.com/docs/#/displaying/
  language:any = getDbItem("language", "auto");                        // Overrides the language selector or "automatic". Must be one of availableLang[]. If not defaults to en.
  
  //BUDDY
  subscribeBuddyAccept:any = getDbItem("subscribeBuddyAccept", "application/pidf+xml");  // Normally only application/dialog-info+xml and application/pidf+xml
  subscribeBuddyEvent:any = getDbItem("subscribeBuddyEvent", "presence");                // For application/pidf+xml use presence. For application/dialog-info+xml use dialog 
  subscribeBuddyExpires = parseInt(getDbItem("subscribeBuddyExpires", 300)); 
  
  //XMPP
  XMPP:any;
  
  // XMPP Settings
  xmppServer:any = getDbItem("xmppServer", "");                // FQDN of XMPP server HTTP service";
  xmppwebSocketPort:any = getDbItem("xmppwebSocketPort", "");  // OpenFire Default : 7443
  xmppWebsocketPath:any = getDbItem("xmppWebsocketPath", "");  // OpenFire Default : /ws
  xmppDomain:any = getDbItem("xmppDomain", "");                // The domain of the XMPP server
  
  // XMPP Tenanting
  xmppRealm:any = getDbItem("xmppRealm", "");                    // To create a tenant like partition in XMPP server all users and buddies will have this realm prepended to their details.
  xmppRealmSeparator:any = getDbItem("xmppRealmSeparator", "-"); // Separates the realm from the profileUser eg: abc123-100@xmppDomain
  // TODO
  xmppChatGroupService:any = getDbItem("xmppChatGroupService", "conference");
  
  profileUser:any = getDbItem("profileUser", null);            // Username for auth with XMPP Server eg: 100
  
  
  //HTML
  windowObj:any = null;
  alertObj:any = null;
  confirmObj:any = null;
  promptObj:any = null;
  menuObj:any = null;
  
  // Settings
  hasVideoDevice:any = false;
  hasAudioDevice:any = false;
  hasSpeakerDevice:any = false;
  audioinputDevices:any = [];
  videoinputDevices:any = [];
  speakerDevices:any = [];
  lines:any = [];
  audioBlobs : any = {}
  newLineNumber:any = 1;
  telNumericRegEx:any = /[^\d\*\#\+]/g
  telAlphanumericRegEx:any = /[^\da-zA-Z\*\#\+\-\_\.\!\~\'\(\)]/g
  settingsMicrophoneStream:any = null;
  settingsMicrophoneStreamTrack:any = null;
  settingsMicrophoneSoundMeter:any = null;
  settingsVideoStream:any = null;
  settingsVideoStreamTrack:any = null;
  localDB:any;
  
  phoneOptions:any = {
    loadAlternateLang: true
  }
  
  callDuration: moment.Duration;
  ringTime: moment.Duration;
  totalDuration: moment.Duration;
  
  
    constructor(protected constService : ConstantsService,) {
  
      console.log("starting browser service");
  
      // window.setInterval(function(){
          
      //  }, 10000);
  
       console.log("setting devices");
       this.detectDevices();
  
  
       console.log("setting language");
       this.lang = this.constService.en;
  
       console.log("en_json", JSON.stringify(this.lang));
  
       console.log("setting local storage");
       this.localDB = window.localStorage;
      
     }
  
  alert(messageStr:any, TitleStr:any, onOk:any) {
      if (this.confirmObj != null) {
          this.confirmObj.dialog("close");
          this.confirmObj = null;
      }
      if (this.promptObj != null) {
          this.promptObj.dialog("close");
          this.promptObj = null;
      }
      if (this.alertObj != null) {
          console.error("Alert not null, while Alert called: " + TitleStr + ", saying:" + messageStr);
          return;
      }
      else {
          console.log("Alert called with Title: " + TitleStr + ", saying: " + messageStr);
      }
  
      var html = "<div class=NoSelect>";
      html += "<div class=UiText style=\"padding: 10px\" id=AllertMessageText>" + messageStr + "</div>";
      html += "</div>"
  
      // this.alertObj = $('<div>').html(html).dialog({
      //     autoOpen: false,
      //     title: TitleStr,
      //     modal: true,
      //     width: 300,
      //     height: "auto",
      //     resizable: false,
      //     closeOnEscape : false,
      //     close: function(event, ui) {
      //         $(this).dialog("destroy");
      //         alertObj = null;
      //     }
      // });
  
      var buttons = [];
      buttons.push({
          text: this.lang.ok,
          click: function(){
              console.log("Alert OK clicked");
              if (onOk) onOk();
             // $(this).dialog("close");
              this.alertObj = null;
          }
      });
      this.alertObj.dialog( "option", "buttons", buttons);
  
      // Open the Window
      this.alertObj.dialog("open");
  
      this.alertObj.dialog({ dialogClass: 'no-close' });
  
       // Call UpdateUI to perform all the nesesary UI updates.
       //UpdateUI();
  
  }
  findLineByNumber(lineNum:any) {
      for(var l = 0; l < this.lines.length; l++) {
          if(this.lines[l].LineNumber == lineNum) return this.lines[l];
      }
      return null;
  }
  
  uID(){
      return Date.now()+Math.floor(Math.random()*10000).toString(16).toUpperCase();
  }
  
  utcDateNow(){
      return moment.utc().format("YYYY-MM-DD HH:mm:ss UTC");
  }
  
  getAudioSrcID(){
      var id = this.localDB.getItem("AudioSrcId");
      return (id != null)? id : "default";
  }
  getAudioOutputID(){
      var id = this.localDB.getItem("AudioOutputId");
      return (id != null)? id : "default";
  }
  getVideoSrcID(){
      var id = this.localDB.getItem("VideoSrcId");
      return (id != null)? id : "default";
  }
  getRingerOutputID(){
      var id = this.localDB.getItem("RingOutputId");
      return (id != null)? id : "default";
  }
  formatDuration(seconds:any){
      var sec = Math.floor(parseFloat(seconds));
      if(sec < 0){
          return sec;
      } 
      else if(sec >= 0 && sec < 60){
          return sec + " " + ((sec > 1) ? this.lang.seconds_plural : this.lang.second_single);
      } 
      else if(sec >= 60 && sec < 60 * 60){ // greater then a minute and less then an hour
          var duration = moment.duration(sec, 'seconds');
          return duration.minutes() + " "+ ((duration.minutes() > 1) ? this.lang.minutes_plural: this.lang.minute_single) +" " + duration.seconds() +" "+ ((duration.seconds() > 1) ? this.lang.seconds_plural : this.lang.second_single);
      } 
      else if(sec >= 60 * 60 && sec < 24 * 60 * 60){ // greater than an hour and less then a day
          var duration = moment.duration(sec, 'seconds');
          return duration.hours() + " "+ ((duration.hours() > 1) ? this.lang.hours_plural : this.lang.hour_single) +" " + duration.minutes() + " "+ ((duration.minutes() > 1) ? this.lang.minutes_plural: this.lang.minute_single) +" " + duration.seconds() +" "+ ((duration.seconds() > 1) ? this.lang.seconds_plural : this.lang.second_single);
      } 
      //  Otherwise.. this is just too long
  }
  
  
  formatShortDuration(seconds:any){
      var sec = Math.floor(parseFloat(seconds));
      if(sec < 0){
          return sec;
      } 
      else if(sec >= 0 && sec < 60){
          return "00:"+ ((sec > 9)? sec : "0"+sec );
      } 
      else if(sec >= 60 && sec < 60 * 60){ // greater then a minute and less then an hour
          var duration = moment.duration(sec, 'seconds');
          return ((duration.minutes() > 9)? duration.minutes() : "0"+duration.minutes()) + ":" + ((duration.seconds() > 9)? duration.seconds() : "0"+duration.seconds());
      } 
      else if(sec >= 60 * 60 && sec < 24 * 60 * 60){ // greater than an hour and less then a day
          var duration = moment.duration(sec, 'seconds');
          return ((duration.hours() > 9)? duration.hours() : "0"+duration.hours())  + ":" + ((duration.minutes() > 9)? duration.minutes() : "0"+duration.minutes())  + ":" + ((duration.seconds() > 9)? duration.seconds() : "0"+duration.seconds());
      } 
      //  Otherwise.. this is just too long
  }
  formatBytes(bytes:any, decimals:any) {
      if (bytes === 0) return "0 "+ this.lang.bytes;
      var k = 1024;
      var dm = (decimals && decimals >= 0)? decimals : 2;
      var sizes = [this.lang.bytes, this.lang.kb, this.lang.mb, this.lang.gb, this.lang.tb, this.lang.pb, this.lang.eb, this.lang.zb, this.lang.yb];
      var i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + " " + sizes[i];
  }
  
  userLocale(){
      var language = window.navigator.language || window.navigator.language; // "en", "en-US", "fr", "fr-FR", "es-ES", etc.
      // langtag = language["-"script]["-" region] *("-" variant) *("-" extension) ["-" privateuse]
      // TODO Needs work
      let langtag = language.split('-');
      if(langtag.length == 1){
          return ""; 
      } 
      else if(langtag.length == 2) {
          return langtag[1].toLowerCase();  // en-US => us
      }
      else if(langtag.length >= 3) {
          return langtag[1].toLowerCase();  // en-US => us
      }
  }
  
  getAlternatelanguage(){
      var userlanguage = window.navigator.language; // "en", "en-US", "fr", "fr-FR", "es-ES", etc.
      // langtag = language["-"script]["-" region] *("-" variant) *("-" extension) ["-" privateuse]
      if(this.language != "auto") userlanguage = this.language;
      userlanguage = userlanguage.toLowerCase();
      if(userlanguage == "en" || userlanguage.indexOf("en-") == 0) return "";  // English is already loaded
  
      for(let l = 0; l < this.availableLang.length; l++){
          if(userlanguage.indexOf(this.availableLang[l].toLowerCase()) == 0){
              console.log("Alternate language detected: ", userlanguage);
              // Set up Moment with the same language settings
              moment.locale(userlanguage);
              return this.availableLang[l].toLowerCase();
          }
      }
      return "";
  }
  
  getFilter(filter:any, keyword:any){
      if(filter.indexOf(",", filter.indexOf(keyword +": ") + keyword.length + 2) != -1){
          return filter.substring(filter.indexOf(keyword +": ") + keyword.length + 2, filter.indexOf(",", filter.indexOf(keyword +": ") + keyword.length + 2));
      }
      else {
          return filter.substring(filter.indexOf(keyword +": ") + keyword.length + 2);
      }
  }
  
  base64toBlob(base64Data:any, contentType:any) {
      if((base64Data.indexOf(",") != -1)) base64Data = base64Data.split(",")[1]; // [data:image/png;base64] , [xxx...]
      var byteCharacters = atob(base64Data);
      var slicesCount = Math.ceil(byteCharacters.length / 1024);
      var byteArrays = new Array(slicesCount);
      for (var s = 0; s < slicesCount; ++s) {
          var begin = s * 1024;
          var end = Math.min(begin + 1024, byteCharacters.length);
          var bytes = new Array(end - begin);
          for (var offset = begin, i = 0; offset < end; ++i, ++offset) {
              bytes[i] = byteCharacters[offset].charCodeAt(0);
          }
          byteArrays[s] = new Uint8Array(bytes);
      }
      return new Blob(byteArrays, { type: contentType });
  }
  
  makeDataArray(defaultValue:any, count:any){
      var rtnArray = new Array(count);
      for(var i=0; i< rtnArray.length; i++) {
          rtnArray[i] = defaultValue;
      }
      return rtnArray;
  }
  
    //SIP Methods 
  createuserAgent() {
      console.log("Creating User Agent...");
      if(this.sipDomain==null || this.sipDomain=="" || this.sipDomain=="null" || this.sipDomain=="undefined") this.sipDomain = this.wssServer; // Sets globally
      var options = {
          uri: UserAgent.makeURI("sip:"+ this.sipUsername + "@" + this.sipDomain),
          transportOptions: {
              server: "wss://" + this.wssServer + ":"+ this.webSocketPort +""+ this.serverPath,
              traceSip: false,
              connectionTimeout: this.transportConnectionTimeout
              // keepAliveInterval: 30 // Uncomment this and make this any number greater then 0 for keep alive... 
              // NB, adding a keep alive will NOT fix bad internet, if your connection cannot stay open (permanent WebSocket Connection) you probably 
              // have a router or ISP issue, and if your internet is so poor that you need to some how keep it alive with empty packets
              // upgrade you internet connection. This is voip we are talking about here.
          },
          sessionDescriptionHandlerFactoryOptions: {
              peerConnectionConfiguration :{
                  bundlePolicy: this.bundlePolicy,
                  // certificates: undefined,
                  // iceCandidatePoolSize: 10,
                  iceServers: [{ urls: "stun:stun.l.google.com:19302" }],
                  // iceTransportPolicy: "all",
                  // peerIdentity: undefined,
                  // rtcpMuxPolicy: "require",
              },
              iceGatheringTimeout: this.iceStunCheckTimeout
          },
          contactName: this.contactUserName,
          displayName: this.profileName,
          authorizationUsername: this.sipUsername,
          authorizationPassword: this.sipPassword,
          hackipInContact: this.ipInContact,           // Asterisk should also be set to rewrite contact
          userAgentString: this.userAgentStr,
          autoStart: false,
          autoStop: true,
          register: false,
          noAnswerTimeout: this.noAnswerTimeout,
          // sipExtension100rel: // UNSUPPORTED | SUPPORTED | REQUIRED NOTE: rel100 is not supported
          contactParams: {},
          delegate: {
              onInvite: (sip)=>{
                  this.receiveCall(sip);
              },
              onMessage: (sip)=>{
                  this.receiveOutOfDialogMessage(sip);
              }
          }
      }
      if(this.iceStunServerJson != ""){
          options.sessionDescriptionHandlerFactoryOptions.peerConnectionConfiguration.iceServers = JSON.parse(this.iceStunServerJson);
      }
  
      // Added to the contact BEFORE the '>' (permanent)
      if(this.registerContactParams && this.registerContactParams != "" && this.registerContactParams != "{}"){
          try{
              options.contactParams = JSON.parse(this.registerContactParams);
          } catch(e){}
      }
      if(this.wssInTransport){
          try{
              options.contactParams["transport"] = "wss";
          } catch(e){}
      }
  
      // Add (Hardcode) other RTCPeerConnection({ rtcConfiguration }) config dictionary options here
      // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/RTCPeerConnection
      // Example: 
      // options.sessionDescriptionHandlerFactoryOptions.peerConnectionConfiguration.rtcpMuxPolicy = "require";
      
      this.userAgent = new UserAgent(options);
      this.userAgent.isRegistered = function(){
          return (this.userAgent && this.userAgent.registerer && this.userAgent.registerer.state == RegistererState.Registered);
      }
      // For some reason this is marked as private... not sure why
      this.userAgent.sessions = this.userAgent._sessions;
      this.userAgent.registrationCompleted = false;
      this.userAgent.registering = false;
      this.userAgent.transport.ReconnectionAttempts = this.transportReconnectionAttempts;
      this.userAgent.transport.attemptingReconnection = false;
      this.userAgent.BlfSubs = [];
      this.userAgent.lastVoicemailCount = 0;
  
      console.log("Creating User Agent... Done");
  
      this.userAgent.transport.onConnect = function(){
          this.onTransportConnected();
      }
      this.userAgent.transport.onDisconnect = function(error){
          if(error){
              this.onTransportConnectError(error);
          }
          else {
              this.onTransportDisconnected();
          }
      }
  
      var RegistererOptions = { 
          expires: this.registerExpires,
          extraHeaders: [],
          extraContactHeaderParams: []
      }
  
      // Added to the SIP Headers
      if(this.registerExtraHeaders && this.registerExtraHeaders != "" && this.registerExtraHeaders != "{}"){
          try{
              var registerExtraHeaders = JSON.parse(this.registerExtraHeaders);
              for (const [key, value] of Object.entries(registerExtraHeaders)) {
                  if(value != ""){
                      RegistererOptions.extraHeaders.push(key + ": "+  value);
                  }
              }
          } catch(e){}
      }
  
      // Added to the contact AFTER the '>' (not permanent)
      if(this.registerExtraContactParams && this.registerExtraContactParams != "" && this.registerExtraContactParams != "{}"){
          try{
              var registerExtraContactParams = JSON.parse(this.registerExtraContactParams);
              for (const [key, value] of Object.entries(registerExtraContactParams)) {
                  if(value == ""){
                      RegistererOptions.extraContactHeaderParams.push(key);
                  } else {
                      RegistererOptions.extraContactHeaderParams.push(key + ":"+  value);
                  }
              }
          } catch(e){}
      }
  
      this.userAgent.registerer = new Registerer(this.userAgent, RegistererOptions);
      console.log("Creating Registerer... Done");
  
      this.userAgent.registerer.stateChange.addListener(function(newState){
          console.log("User Agent Registration State:", newState);
          switch (newState) {
              case RegistererState.Initial:
                  // Nothing to do
                  break;
              case RegistererState.Registered:
                  this.onRegistered();
                  break;
              case RegistererState.Unregistered:
                  this.onUnregistered();
                  break;
              case RegistererState.Terminated:
                  // Nothing to do
                  break;
          }
      });
  
      console.log("User Agent Connecting to WebSocket...");
      //$("#regStatus").html(this.lang.connecting_to_web_socket);
      this.userAgent.start().catch(function(error){
          this.onTransportConnectError(error);
      });
  }
    
  //Calling Functions
  
  // Sessions & During Call Activity
  // ===============================
  
  getSession(buddy:any) {
    if(this.userAgent == null) {
        console.warn("userAgent is null");
        return null;
    }
    if(this.userAgent.isRegistered() == false) {
        console.warn("userAgent is not registered");
        return null;
    }
  
    var rtnSession = null;
  //   $.each(this.userAgent.sessions, (i, session)=> {
  //       if(session.data.buddyId == buddy) {
  //           rtnSession = session;
  //           return false;
  //       }
  //   });
    return rtnSession;
  }
  
  countSessions(id:any){
    var rtn = 0;
    if(this.userAgent == null) {
        console.warn("userAgent is null");
        return 0;
    }
  //   $.each(this.userAgent.sessions, (i, session)=> {
  //       if(id != session.id) rtn ++;
  //   });
    return rtn;
  }
  
  
  startRecording(lineNum:any){
    if(this.callRecordingPolicy == "disabled") {
        console.warn("Policy Disabled: Call Recording");
        return;
    }
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null) return;
  
  //   $("#line-"+ lineObj.LineNumber +"-btn-start-recording").hide();
  //   $("#line-"+ lineObj.LineNumber +"-btn-stop-recording").show();
  
    var session = lineObj.SipSession;
    if(session == null){
        console.warn("Could not find session");
        return;
    }
  
    var id = this.uID();
  
    if(!session.data.recordings) session.data.recordings = [];
    session.data.recordings.push({
        uID: id,
        startTime: this.utcDateNow(),
        stopTime: this.utcDateNow(),
    });
  
    if(session.data.mediaRecorder && session.data.mediaRecorder.state == "recording"){
        console.warn("Call Recording was somehow on... stopping call recording");
        this.stopRecording(lineNum, true);
        // State should be inactive now, but the data available event will fire
        // Note: potential race condition here if someone hits the stop, and start quite quickly.
    }
    console.log("Creating call recorder...");
  
    session.data.recordingAudioStreams = new MediaStream();
    var pc = session.sessionDescriptionHandler.peerConnection;
    pc.getSenders().forEach((RTCRtpSender) =>{
        if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
            console.log("Adding sender audio track to record:", RTCRtpSender.track.label);
            session.data.recordingAudioStreams.addTrack(RTCRtpSender.track);
        }
    });
    pc.getReceivers().forEach((RTCRtpReceiver)=> {
        if(RTCRtpReceiver.track && RTCRtpReceiver.track.kind == "audio") {
            console.log("Adding receiver audio track to record:", RTCRtpReceiver.track.label);
            session.data.recordingAudioStreams.addTrack(RTCRtpReceiver.track);
        }
    });
  
    // Resample the Video Recording
    if(session.data.withvideo){
        var recordingWidth = 640;
        var recordingHeight = 360;
        var pnpVideSize = 100;
        if(this.recordingVideoSize == "HD"){
            recordingWidth = 1280;
            recordingHeight = 720;
            pnpVideSize = 144;
        }
        if(this.recordingVideoSize == "FHD"){
            recordingWidth = 1920;
            recordingHeight = 1080;
            pnpVideSize = 240;
        }
        // Create Canvas
        session.data.recordingCanvas =" $('<canvas/>').get(0)";
        session.data.recordingCanvas.width = (this.recordingLayout == "side-by-side")? (recordingWidth * 2) + 5: recordingWidth;
        session.data.recordingCanvas.height = recordingHeight;
        session.data.recordingContext = session.data.recordingCanvas.getContext("2d");
  
        // Capture Interval
        window.clearInterval(session.data.recordingRedrawInterval);
        session.data.recordingRedrawInterval = window.setInterval(function(){
  
            // Video Source
            var pnpVideo = "";//$("#line-" + lineObj.LineNumber + "-localVideo").get(0);
  
            var mainVideo = null;
            var validVideos = [];
            var talkingVideos = [];
            var videoContainer:any = "";//$("#line-" + lineObj.LineNumber + "-remote-videos");
            var potentialVideos =  videoContainer.find('video').length;
            if(potentialVideos == 0){
                // Nothing to render
                // console.log("Nothing to render in this frame")
            }
            else if (potentialVideos == 1){
                mainVideo = videoContainer.find('video')[0];
                // console.log("Only one video element", mainVideo);
            }
            else if (potentialVideos > 1){
                // Decide what video to record
              //   videoContainer.find('video').each(function(i, video) {
              //       var videoTrack = video.srcObject.getVideoTracks()[0];
              //       if(videoTrack.readyState == "live" && video.videoWidth > 10 && video.videoHeight >= 10) {
              //           if(video.srcObject.isPinned == true){
              //               mainVideo = video;
              //               // console.log("Multiple Videos using last PINNED frame");
              //           }
              //           if(video.srcObject.isTalking == true){
              //               talkingVideos.push(video);
              //           }
              //           validVideos.push(video);
              //       }
              //   });
  
                // Check if we found something
                if(mainVideo == null && talkingVideos.length >= 1){
                    // Nothing pinned use talking
                    mainVideo = talkingVideos[0];
                    // console.log("Multiple Videos using first talking frame");
                }
                if(mainVideo == null && validVideos.length >= 1){
                    // Nothing pinned or talking use valid
                    mainVideo = validVideos[0];
                    // console.log("Multiple Videos using first VALID frame");
                }
            }
  
            // Main Video
            var videoWidth = (mainVideo && mainVideo.videoWidth > 0)? mainVideo.videoWidth : recordingWidth ;
            var videoHeight = (mainVideo && mainVideo.videoHeight > 0)? mainVideo.videoHeight : recordingHeight ;
            if(videoWidth >= videoHeight){
                // Landscape / Square
                var scale = recordingWidth / videoWidth;
                videoWidth = recordingWidth;
                videoHeight = videoHeight * scale;
                if(videoHeight > recordingHeight){
                    var scale = recordingHeight / videoHeight;
                    videoHeight = recordingHeight;
                    videoWidth = videoWidth * scale;
                }
            } 
            else {
                // Portrait
                var scale = recordingHeight / videoHeight;
                videoHeight = recordingHeight;
                videoWidth = videoWidth * scale;
            }
            var offsetX = (videoWidth < recordingWidth)? (recordingWidth - videoWidth) / 2 : 0;
            var offsetY = (videoHeight < recordingHeight)? (recordingHeight - videoHeight) / 2 : 0;
            if(this.recordingLayout == "side-by-side") offsetX = recordingWidth + 5 + offsetX;
  
            // Picture-in-Picture Video
          //   var pnpVideoHeight = pnpVideo.videoHeight;
          //   var pnpVideoWidth = pnpVideo.videoWidth;
            var pnpVideoHeight = 50;
            var pnpVideoWidth = 70;
            if(pnpVideoHeight > 0){
                if(pnpVideoWidth >= pnpVideoHeight){
                    var scale = pnpVideSize / pnpVideoHeight;
                    pnpVideoHeight = pnpVideSize;
                    pnpVideoWidth = pnpVideoWidth * scale;
                } 
                else{
                    var scale = pnpVideSize / pnpVideoWidth;
                    pnpVideoWidth = pnpVideSize;
                    pnpVideoHeight = pnpVideoHeight * scale;
                }
            }
            var pnpOffsetX = 10;
            var pnpOffsetY = 10;
            if(this.recordingLayout == "side-by-side"){
              //   pnpVideoWidth = pnpVideo.videoWidth;
              //   pnpVideoHeight = pnpVideo.videoHeight;
              var pnpVideoHeight = 50;
              var pnpVideoWidth = 70;
                if(pnpVideoWidth >= pnpVideoHeight){
                    // Landscape / Square
                    var scale = recordingWidth / pnpVideoWidth;
                    pnpVideoWidth = recordingWidth;
                    pnpVideoHeight = pnpVideoHeight * scale;
                    if(pnpVideoHeight > recordingHeight){
                        var scale = recordingHeight / pnpVideoHeight;
                        pnpVideoHeight = recordingHeight;
                        pnpVideoWidth = pnpVideoWidth * scale;
                    }
                } 
                else {
                    // Portrait
                    var scale = recordingHeight / pnpVideoHeight;
                    pnpVideoHeight = recordingHeight;
                    pnpVideoWidth = pnpVideoWidth * scale;
                }
                pnpOffsetX = (pnpVideoWidth < recordingWidth)? (recordingWidth - pnpVideoWidth) / 2 : 0;
                pnpOffsetY = (pnpVideoHeight < recordingHeight)? (recordingHeight - pnpVideoHeight) / 2 : 0;
            }
  
            // Draw Background
            session.data.recordingContext.fillRect(0, 0, session.data.recordingCanvas.width, session.data.recordingCanvas.height);
  
            // Draw Main Video
            if(mainVideo && mainVideo.videoHeight > 0){
                session.data.recordingContext.drawImage(mainVideo, offsetX, offsetY, videoWidth, videoHeight);
            }
  
          //   // Draw PnP
          //   if(pnpVideo.videoHeight > 0 && (this.recordingLayout == "side-by-side" || this.recordingLayout == "them-pnp")){
          //       // Only Draw the Pnp Video when needed
          //       session.data.recordingContext.drawImage(pnpVideo, pnpOffsetX, pnpOffsetY, pnpVideoWidth, pnpVideoHeight);
          //   }
        }, Math.floor(1000/this.recordingVideoFps));
  
        // Start Video Capture
        session.data.recordingVideoMediaStream = session.data.recordingCanvas.captureStream(this.recordingVideoFps);
    }
  
    session.data.recordingMixedAudioVideoRecordStream = new MediaStream();
    session.data.recordingMixedAudioVideoRecordStream.addTrack(this.mixAudioStreams(session.data.recordingAudioStreams).getAudioTracks()[0]);
    if(session.data.withvideo){
        session.data.recordingMixedAudioVideoRecordStream.addTrack(session.data.recordingVideoMediaStream.getVideoTracks()[0]);
    }
  
    var mediaType = "audio/webm"; // audio/mp4 | audio/webm;
    if(session.data.withvideo) mediaType = "video/webm";
    var options = {
        mimeType : mediaType
    }
    // Note: It appears that mimeType is optional, but... Safari is truly dreadful at recording in mp4, and doesn't have webm yet
    // You you can leave this as default, or force webm, however know that Safari will be no good at this either way.
    // session.data.mediaRecorder = new MediaRecorder(session.data.recordingMixedAudioVideoRecordStream, options);
    session.data.mediaRecorder = new MediaRecorder(session.data.recordingMixedAudioVideoRecordStream);
    session.data.mediaRecorder.data = {}
    session.data.mediaRecorder.data.id = ""+ id;
    session.data.mediaRecorder.data.sessionId = ""+ session.id;
    session.data.mediaRecorder.data.buddyId = ""+ lineObj.BuddyObj.identity;
    session.data.mediaRecorder.ondataavailable = function(event) {
        console.log("Got Call Recording Data: ", event.data.size +"Bytes", this.data.id, this.data.buddyId, this.data.sessionId);
        // Save the Audio/Video file
        this.saveCallRecording(event.data, this.data.id, this.data.buddyId, this.data.sessionId);
    }
  
    console.log("Starting Call Recording", id);
    session.data.mediaRecorder.start(); // Safari does not support time slice
    session.data.recordings[session.data.recordings.length-1].startTime = this.utcDateNow();
  
  //   $("#line-" + lineObj.LineNumber + "-msg").html(this.lang.call_recording_started);
  
   // this.updatelinescroll(lineNum);
  }
  
  saveCallRecording(blob:any, id:any, buddy:any, sessionid:any){
    var indexedDB = window.indexedDB;
    var request = indexedDB.open("CallRecordings", 1);
    request.onerror = function(event) {
        console.error("IndexDB Request Error:", event);
    }
    request.onupgradeneeded = function(event) {
        console.warn("Upgrade Required for IndexDB... probably because of first time use.");
      //   var IDB = request.result;
  
      //   // Create Object Store
      //   if(IDB.objectStoreNames.contains("Recordings") == false){
      //       var objectStore = IDB.createObjectStore("Recordings", { keyPath: "uID" });
      //       objectStore.createIndex("sessionid", "sessionid", { unique: false });
      //       objectStore.createIndex("bytes", "bytes", { unique: false });
      //       objectStore.createIndex("type", "type", { unique: false });
      //       objectStore.createIndex("mediaBlob", "mediaBlob", { unique: false });
      //   }
      //   else {
      //       console.warn("IndexDB requested upgrade, but object store was in place.");
      //   }
    }
    request.onsuccess = function(event) {
        console.log("IndexDB connected to CallRecordings");
  
      //   var IDB = request.result;
      //   if(IDB.objectStoreNames.contains("Recordings") == false){
      //       console.warn("IndexDB CallRecordings.Recordings does not exists, this call recoding will not be saved.");
      //       IDB.close();
      //       window.indexedDB.deleteDatabase("CallRecordings"); // This should help if the table structure has not been created.
      //       return;
      //   }
      //   IDB.onerror = function(event) {
      //       console.error("IndexDB Error:", event);
      //   }
    
        // Prepare data to write
        var data = {
            uID: id,
            sessionid: sessionid,
            bytes: blob.size,
            type: blob.type,
            mediaBlob: blob
        }
        // Commit Transaction
        //var transaction = IDB.transaction(["Recordings"], "readwrite");
      //   var objectStoreAdd = transaction.objectStore("Recordings").add(data);
      //   objectStoreAdd.onsuccess = function(event) {
      //       console.log("Call Recording Success: ", id, blob.size, blob.type, buddy, sessionid);
      //   }
    }
  }
  
  
  stopRecording(lineNum:any, noConfirm:any){
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null) return;
  
    var session = lineObj.SipSession;
    if(noConfirm == true){
        // Called at the end of a call
      //   $("#line-"+ lineObj.LineNumber +"-btn-start-recording").show();
      //   $("#line-"+ lineObj.LineNumber +"-btn-stop-recording").hide();
  
        if(session.data.mediaRecorder){
            if(session.data.mediaRecorder.state == "recording"){
                console.log("Stopping Call Recording");
                session.data.mediaRecorder.stop();
                session.data.recordings[session.data.recordings.length-1].stopTime = this.utcDateNow();
                window.clearInterval(session.data.recordingRedrawInterval);
  
              //   $("#line-" + lineObj.LineNumber + "-msg").html(this.lang.call_recording_stopped);
  
               // this.updatelinescroll(lineNum);
            } 
            else{
                console.warn("Recorder is in an unknown state");
            }
        }
        return;
    }
    else {
        // User attempts to end call recording
        if(this.callRecordingPolicy == "enabled"){
            console.warn("Policy Enabled: Call Recording");
            return;
        }
  
      //   this.Confirm(this.lang.confirm_stop_recording, this.lang.stop_recording, function(){
      //     this.stopRecording(lineNum, true);
      //   });
    }
  }
  
  
  playAudioCallRecording(obj:any, cdrId:any, uID:any){
    var container:any = "$(obj).parent()";
    container.empty();
  
    var audioObj = new Audio();
    audioObj.autoplay = false;
    audioObj.controls = true;
  
    // Make sure you are playing out via the correct device
    var sinkId = this.getAudioOutputID();
  //   if (typeof audioObj.sinkId !== 'undefined') {
  //       audioObj.setSinkId(sinkId).then(function(){
  //           console.log("sinkId applied: "+ sinkId);
  //       }).catch(function(e){
  //           console.warn("Error using setSinkId: ", e);
  //       });
  //   } else {
  //       console.warn("setSinkId() is not possible using this browser.")
  //   }
  
    container.append(audioObj);
  
    // Get Call Recording
    var indexedDB = window.indexedDB;
    var request = indexedDB.open("CallRecordings", 1);
    request.onerror = function(event) {
        console.error("IndexDB Request Error:", event);
    }
    request.onupgradeneeded = function(event) {
        console.warn("Upgrade Required for IndexDB... probably because of first time use.");
    }
    request.onsuccess = function(event) {
        console.log("IndexDB connected to CallRecordings");
  
      //   var IDB = request.result;
      //   if(IDB.objectStoreNames.contains("Recordings") == false){
      //       console.warn("IndexDB CallRecordings.Recordings does not exists");
      //       return;
      //   } 
  
      //   var transaction = IDB.transaction(["Recordings"]);
      //   var objectStoreGet = transaction.objectStore("Recordings").get(uID);
      //   objectStoreGet.onerror = function(event) {
      //       console.error("IndexDB Get Error:", event);
      //   }
      //   objectStoreGet.onsuccess = function(event) {
      //       $("#cdr-media-meta-size-"+ cdrId +"-"+ uID).html(" Size: "+ this.formatBytes(event.target.result.bytes));
      //       $("#cdr-media-meta-codec-"+ cdrId +"-"+ uID).html(" Codec: "+ event.target.result.type);
  
      //       // Play
      //       audioObj.src = window.URL.createObjectURL(event.target.result.mediaBlob);
      //       audioObj.oncanplaythrough = function(){
      //           audioObj.play().then(function(){
      //               console.log("Playback started");
      //           }).catch(function(e){
      //               console.error("Error playing back file: ", e);
      //           });
      //       }
      //   }
    }
    
  }
  
  playVideoCallRecording(obj:any, cdrId:any, uID:any, buddy:any){
    var container:any = "$(obj).parent()";
    container.empty();
  
    var videoObj:any = "$(\"<video>\").get(0)";
    videoObj.id = "callrecording-video-"+ cdrId;
  //   videoObj.autoplay = false;
  //   videoObj.controls = true;
  //   videoObj.playsinline = true;
  //   videoObj.ontimeupdate = function(event){
  //       $("#cdr-video-meta-width-"+ cdrId +"-"+ uID).html(this.lang.width + " : "+ event.target.videoWidth +"px");
  //       $("#cdr-video-meta-height-"+ cdrId +"-"+ uID).html(this.lang.height +" : "+ event.target.videoHeight +"px");
  //   }
  
  //   var sinkId = this.getAudioOutputID();
  //   if (typeof videoObj.sinkId !== 'undefined') {
  //       videoObj.setSinkId(sinkId).then(function(){
  //           console.log("sinkId applied: "+ sinkId);
  //       }).catch(function(e){
  //           console.warn("Error using setSinkId: ", e);
  //       });
  //   } else {
  //       console.warn("setSinkId() is not possible using this browser.")
  //   }
  
  //   container.append(videoObj);
  
  //   // Get Call Recording
  //   var indexedDB = window.indexedDB;
  //   var request = indexedDB.open("CallRecordings", 1);
  //   request.onerror = function(event) {
  //       console.error("IndexDB Request Error:", event);
  //   }
  //   request.onupgradeneeded = function(event) {
  //       console.warn("Upgrade Required for IndexDB... probably because of first time use.");
  //   }
  //   request.onsuccess = function(event) {
  //       console.log("IndexDB connected to CallRecordings");
  
  //       var IDB = request.result;
  //       if(IDB.objectStoreNames.contains("Recordings") == false){
  //           console.warn("IndexDB CallRecordings.Recordings does not exists");
  //           return;
  //       } 
  
  //       var transaction = IDB.transaction(["Recordings"]);
  //       var objectStoreGet = transaction.objectStore("Recordings").get(uID);
  //       objectStoreGet.onerror = function(event) {
  //           console.error("IndexDB Get Error:", event);
  //       }
  //       objectStoreGet.onsuccess = function(event) {
  //           $("#cdr-media-meta-size-"+ cdrId +"-"+ uID).html(" Size: "+ this.formatBytes(event.target.result.bytes));
  //           $("#cdr-media-meta-codec-"+ cdrId +"-"+ uID).html(" Codec: "+ event.target.result.type);
  
  //           // Play
  //           videoObj.src = window.URL.createObjectURL(event.target.result.mediaBlob);
  //           videoObj.oncanplaythrough = function(){
  //               try{
  //                   videoObj.scrollIntoViewIfNeeded(false);
  //               } catch(e){}
  //               videoObj.play().then(function(){
  //                   console.log("Playback started");
  //               }).catch(function(e){
  //                   console.error("Error playing back file: ", e);
  //               });
  
  //               // Create a Post Image after a second
  //               if(buddy){
  //                   window.setTimeout(function(){
  //                       var canvas = $("<canvas>").get(0);
  //                       var videoWidth = videoObj.videoWidth;
  //                       var videoHeight = videoObj.videoHeight;
  //                       if(videoWidth > videoHeight){
  //                           // Landscape
  //                           if(videoHeight > 225){
  //                               var p = 225 / videoHeight;
  //                               videoHeight = 225;
  //                               videoWidth = videoWidth * p;
  //                           }
  //                       }
  //                       else {
  //                           // Portrait
  //                           if(videoHeight > 225){
  //                               var p = 225 / videoWidth;
  //                               videoWidth = 225;
  //                               videoHeight = videoHeight * p;
  //                           }
  //                       }
  //                       canvas.width = videoWidth;
  //                       canvas.height = videoHeight;
  //                       canvas.getContext('2d').drawImage(videoObj, 0, 0, videoWidth, videoHeight);  
  //                       canvas.toBlob(function(blob) {
  //                           var reader = new FileReader();
  //                           reader.readAsDataURL(blob);
  //                           reader.onloadend = function() {
  //                               var Poster = { width: videoWidth, height: videoHeight, posterBase64: reader.result }
  //                               console.log("Capturing Video Poster...");
    
  //                               // Update DB
  //                               var currentStream = JSON.parse(this.localDB.getItem(buddy + "-stream"));
  //                               if(currentStream != null || currentStream.DataCollection != null){
  //                                   $.each(currentStream.DataCollection, function(i, item) {
  //                                       if (item.ItemType == "CDR" && item.CdrId == cdrId) {
  //                                           // Found
  //                                           if(item.Recordings && item.Recordings.length >= 1){
  //                                               $.each(item.Recordings, function(r, recording) {
  //                                                   if(recording.uID == uID) recording.Poster = Poster;
  //                                               });
  //                                           }
  //                                           return false;
  //                                       }
  //                                   });
  //                                   this.localDB.setItem(buddy + "-stream", JSON.stringify(currentStream));
  //                                   console.log("Capturing Video Poster, Done");
  //                               }
  //                           }
  //                       }, 'image/jpeg', this.posterJpegQuality);
  //                   }, 1000);
  //               }
  //           }
  //       }
    }
  
  
  // Stream Manipulations
  // ====================
  mixAudioStreams(MultiAudioTackStream:any){
    // Takes in a MediaStream with any number of audio tracks and mixes them together
  
    var audioContext = null;
    try {
        window.AudioContext = window.AudioContext;
        audioContext = new AudioContext();
    }
    catch(e){
        console.warn("AudioContext() not available, cannot record");
        return MultiAudioTackStream;
    }
    var mixedAudioStream = audioContext.createMediaStreamDestination();
    MultiAudioTackStream.getAudioTracks().forEach(function(audioTrack){
        var srcStream = new MediaStream();
        srcStream.addTrack(audioTrack);
        var streamSourceNode = audioContext.createMediaStreamSource(srcStream);
        streamSourceNode.connect(mixedAudioStream);
    });
  
    return mixedAudioStream.stream;
  }
  
  // Call Transfer & Conference
  // ============================
  quickFindBuddy(obj:any){
    var filter = obj.value;
    if(filter == "") {
        //HidePopup();
        return;
    }
  
    console.log("Find Buddy: ", filter);
  
    // Buddies.sort(function(a, b){
    //     if(a.CallerIDName < b.CallerIDName) return -1;
    //     if(a.CallerIDName > b.CallerIDName) return 1;
    //     return 0;
    // });
  
    var items = [];
    var visibleItems = 0;
    // for(var b = 0; b < Buddies.length; b++){
    //     var buddyObj = Buddies[b];
  
    //     // Perform Filter Display
    //     var display = false;
    //     if(buddyObj.CallerIDName && buddyObj.CallerIDName.toLowerCase().indexOf(filter.toLowerCase()) > -1) display = true;
    //     if(buddyObj.ExtNo && buddyObj.ExtNo.toLowerCase().indexOf(filter.toLowerCase()) > -1) display = true;
    //     if(buddyObj.Desc && buddyObj.Desc.toLowerCase().indexOf(filter.toLowerCase()) > -1) display = true;
    //     if(buddyObj.MobileNumber && buddyObj.MobileNumber.toLowerCase().indexOf(filter.toLowerCase()) > -1) display = true;
    //     if(buddyObj.ContactNumber1 && buddyObj.ContactNumber1.toLowerCase().indexOf(filter.toLowerCase()) > -1) display = true;
    //     if(buddyObj.ContactNumber2 && buddyObj.ContactNumber2.toLowerCase().indexOf(filter.toLowerCase()) > -1) display = true;
    //     if(display) {
    //         // Filtered Results
    //         var iconClass = "dotDefault";
    //         if(buddyObj.type == "extension" && buddyObj.EnableSubscribe == true) {
    //             iconClass = buddyObj.devState;
    //         } else if(buddyObj.type == "xmpp" && buddyObj.EnableSubscribe == true) {
    //             iconClass = buddyObj.devState;
    //         }
    //         if(visibleItems > 0) items.push({ value: null, text: "-"});
    //         items.push({ value: null, text: buddyObj.CallerIDName, isHeader: true });
    //         if(buddyObj.ExtNo != "") {
    //             items.push({ icon : "fa fa-phone-square "+ iconClass, text: lang.extension +" ("+ buddyObj.presence +"): "+ buddyObj.ExtNo, value: buddyObj.ExtNo });
    //         }
    //         if(buddyObj.MobileNumber != "") {
    //             items.push({ icon : "fa fa-mobile", text: lang.mobile +": "+ buddyObj.MobileNumber, value: buddyObj.MobileNumber });
    //         }
    //         if(buddyObj.ContactNumber1 != "") {
    //             items.push({ icon : "fa fa-phone", text: lang.call +": "+ buddyObj.ContactNumber1, value: buddyObj.ContactNumber1 });
    //         }
    //         if(buddyObj.ContactNumber2 != "") {
    //             items.push({ icon : "fa fa-phone", text: lang.call +": "+ buddyObj.ContactNumber2, value: buddyObj.ContactNumber2 });
    //         }
    //         visibleItems++;
    //     }
    //     if(visibleItems >= 5) break;
    // }
  
    if(items.length > 1){
        var menu = {
            selectEvent : function( event, ui ) {
                var number = ui.item.attr("value");
               // if(number == null) HidePopup();
                if(number != "null" && number != "" && number != undefined) {
                  //  HidePopup();
                    obj.value = number;
                }
            },
            createEvent : null,
            autoFocus : false,
            items : items
        }
        //this.PopupMenu(obj, menu);
    } 
    else {
     // this.HidePopup();
    }
  }
  
  // Call Transfer
  // =============
  startTransferSession(lineNum:any){
    if('$("#line-"+ lineNum +"-btn-CancelConference").is(":visible")'){
      this.cancelConference(lineNum);
        return;
    }
  
  //   $("#line-"+ lineNum +"-btn-Transfer").hide();
  //   $("#line-"+ lineNum +"-btn-CancelTransfer").show();
  
    this.holdSession(lineNum);
  //   $("#line-"+ lineNum +"-txt-FindTransferBuddy").val("");
  //   $("#line-"+ lineNum +"-txt-FindTransferBuddy").parent().show();
  
  //   $("#line-"+ lineNum +"-session-avatar").css("width", "50px");
  //   $("#line-"+ lineNum +"-session-avatar").css("height", "50px");
    this.restoreCallControls(lineNum)
  
  //   $("#line-"+ lineNum +"-btn-blind-transfer").show();
  //   $("#line-"+ lineNum +"-btn-attended-transfer").show();
  //   $("#line-"+ lineNum +"-btn-complete-transfer").hide();
  //   $("#line-"+ lineNum +"-btn-cancel-transfer").hide();
  
  //   $("#line-"+ lineNum +"-btn-complete-attended-transfer").hide();
  //   $("#line-"+ lineNum +"-btn-cancel-attended-transfer").hide();
  //   $("#line-"+ lineNum +"-btn-terminate-attended-transfer").hide();
  
  //   $("#line-"+ lineNum +"-transfer-status").hide();
  
  //   $("#line-"+ lineNum +"-Transfer").show();
  
   // this.updatelinescroll(lineNum);
  }
  
  
  cancelTransferSession(lineNum:any){
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null){
        console.warn("Null line or session");
        return;
    }
    var session = lineObj.SipSession;
    if(session.data.childsession){
        console.log("Child Transfer call detected:", session.data.childsession.state);
        session.data.childsession.dispose().then(function(){
            session.data.childsession = null;
        }).catch(function(error){
            session.data.childsession = null;
            // Suppress message
        });
    }
  
  //   $("#line-"+ lineNum +"-session-avatar").css("width", "");
  //   $("#line-"+ lineNum +"-session-avatar").css("height", "");
  
  //   $("#line-"+ lineNum +"-btn-Transfer").show();
  //   $("#line-"+ lineNum +"-btn-CancelTransfer").hide();
  
    this.unholdSession(lineNum);
  //   $("#line-"+ lineNum +"-Transfer").hide();
  
    //this.updatelinescroll(lineNum);
  }
  
  transferOnkeydown(event:any, obj:any, lineNum:any) {
    var keycode = (event.keyCode ? event.keyCode : event.which);
    if (keycode == '13'){
        event.preventDefault();
        if(event.ctrlKey){
            this.attendedTransfer(lineNum);
        }
        else {
            this.blindTransfer(lineNum);
        }
  
        return false;
    }
  }

  blindTransfer(lineNum:any) {
    var dstNo:any = '$("#line-"+ lineNum +"-txt-FindTransferBuddy").val()';
    if(this.enableAlphanumericDial){
        dstNo = String(dstNo).replace(this.telAlphanumericRegEx, "").substring(0,this.maxdidLength);
    }
    else {
        dstNo = String(dstNo).replace(this.telNumericRegEx, "").substring(0,this.maxdidLength);
    }
    if(dstNo == ""){
        console.warn("Cannot transfer, no number");
        return;
    }
  
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null){
        console.warn("Null line or session");
        return;
    }
    var session = lineObj.SipSession;
  
    if(!session.data.transfer) session.data.transfer = [];
    session.data.transfer.push({ 
        type: "Blind", 
        to: dstNo, 
        transferTime: this.utcDateNow(), 
        disposition: "refer",
        dispositionTime: this.utcDateNow(), 
        accept : {
            complete: null,
            eventTime: null,
            disposition: ""
        }
    });
    var transferId = session.data.transfer.length-1;
  
    var transferOptions  = { 
        requestDelegate: {
            onAccept: function(sip){
                console.log("Blind transfer Accepted");
  
                session.data.terminateby = "us";
                session.data.reasonCode = 202;
                session.data.reasonText = "Transfer";
            
                session.data.transfer[transferId].accept.complete = true;
                session.data.transfer[transferId].accept.disposition = sip.message.reasonPhrase;
                session.data.transfer[transferId].accept.eventTime = this.utcDateNow();
  
                // TODO: use lang pack
              //   $("#line-" + lineNum + "-msg").html("Call Blind Transferred (Accepted)");
  
                this.updatelinescroll(lineNum);
  
                session.bye().catch(function(error){
                    console.warn("Could not BYE after blind transfer:", error);
                });
                this.teardownSession(lineObj);
            },
            onReject:function(sip){
                console.warn("REFER rejected:", sip);
  
                session.data.transfer[transferId].accept.complete = false;
                session.data.transfer[transferId].accept.disposition = sip.message.reasonPhrase;
                session.data.transfer[transferId].accept.eventTime = this.utcDateNow();
  
              //   $("#line-" + lineNum + "-msg").html("Call Blind Failed!");
  
                this.updatelinescroll(lineNum);
  
                // Session should still be up, so just allow them to try again
            }
        }
    }
    console.log("REFER: ", dstNo + "@" + this.sipDomain);
    var referTo = UserAgent.makeURI("sip:"+ dstNo.replace(/#/g, "%23") + "@" + this.sipDomain);
    session.refer(referTo, transferOptions).catch(function(error){
        console.warn("Failed to REFER", error);
    });;
  
  //   $("#line-" + lineNum + "-msg").html(this.lang.call_blind_transfered);
  
    //this.updatelinescroll(lineNum);
  }

  attendedTransfer(lineNum:any){
    var dstNo :any= '$("#line-"+ lineNum +"-txt-FindTransferBuddy").val()';
    if(this.enableAlphanumericDial){
        dstNo = String(dstNo).replace(this.telAlphanumericRegEx, "").substring(0,this.maxdidLength);
    }
    else {
        dstNo = String(dstNo).replace(this.telNumericRegEx, "").substring(0,this.maxdidLength);
    }
    if(dstNo == ""){
        console.warn("Cannot transfer, no number");
        return;
    }
    
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null){
        console.warn("Null line or session");
        return;
    }
    var session = lineObj.SipSession;
  
   // this.HidePopup();
  
  //   $("#line-"+ lineNum +"-txt-FindTransferBuddy").parent().hide();
  //   $("#line-"+ lineNum +"-btn-blind-transfer").hide();
  //   $("#line-"+ lineNum +"-btn-attended-transfer").hide();
  
  //   $("#line-"+ lineNum +"-btn-complete-attended-transfer").hide();
  //   $("#line-"+ lineNum +"-btn-cancel-attended-transfer").hide();
  //   $("#line-"+ lineNum +"-btn-terminate-attended-transfer").hide();
  
  
    var newCallStatus:any =' $("#line-"+ lineNum +"-transfer-status")';
    newCallStatus.html(this.lang.connecting);
    newCallStatus.show();
  
    if(!session.data.transfer) session.data.transfer = [];
    session.data.transfer.push({ 
        type: "Attended", 
        to: dstNo, 
        transferTime: this.utcDateNow(), 
        disposition: "invite",
        dispositionTime: this.utcDateNow(), 
        accept : {
            complete: null,
            eventTime: null,
            disposition: ""
        }
    });
    var transferId = session.data.transfer.length-1;
  
   // this.updatelinescroll(lineNum);
  
    // SDP options
    var supportedConstraints = navigator.mediaDevices.getSupportedConstraints();
    var spdOptions = {
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
        spdOptions.sessionDescriptionHandlerOptions.constraints.audio["autoGainControl"] = this.autoGainControl;
    }
    if(supportedConstraints.echoCancellation) {
        spdOptions.sessionDescriptionHandlerOptions.constraints.audio["echoCancellation"] = this.echoCancellation;
    }
    if(supportedConstraints.noiseSuppression) {
        spdOptions.sessionDescriptionHandlerOptions.constraints.audio["noiseSuppression"] = this.noiseSuppression;
    }
  
    // Not sure if its possible to transfer a Video call???
    if(session.data.withvideo){
        spdOptions.sessionDescriptionHandlerOptions.constraints.video = true;
        if(session.data.VideoSourceDevice != "default"){
            spdOptions.sessionDescriptionHandlerOptions.constraints.video["deviceId"] = { exact: session.data.VideoSourceDevice }
        }
        // Add additional Constraints
        if(supportedConstraints.frameRate && this.maxFrameRate != "") {
            spdOptions.sessionDescriptionHandlerOptions.constraints.video["frameRate"] = this.maxFrameRate;
        }
        if(supportedConstraints.height && this.videoHeight != "") {
            spdOptions.sessionDescriptionHandlerOptions.constraints.video["height"] = this.videoHeight;
        }
        if(supportedConstraints.aspectRatio && this.videoAspectRatio != "") {
            spdOptions.sessionDescriptionHandlerOptions.constraints.video["aspectRatio"] = this.videoAspectRatio;
        }
    }
  
    // Create new call session
    console.log("TRANSFER INVITE: ", "sip:" + dstNo + "@" + this.sipDomain);
    var targetURI = UserAgent.makeURI("sip:"+ dstNo.replace(/#/g, "%23") + "@" + this.sipDomain);
    var newSession = new Inviter(this.userAgent, targetURI, spdOptions);
    newSession.data = {}
    newSession.delegate = {
        onBye: function(sip){
            console.log("New call session ended with BYE");
            newCallStatus.html(this.lang.call_ended);
            session.data.transfer[transferId].disposition = "bye";
            session.data.transfer[transferId].dispositionTime = this.utcDateNow();
  
          //   $("#line-"+ lineNum +"-txt-FindTransferBuddy").parent().show();
          //   $("#line-"+ lineNum +"-btn-blind-transfer").show();
          //   $("#line-"+ lineNum +"-btn-attended-transfer").show();
    
          //   $("#line-"+ lineNum +"-btn-complete-attended-transfer").hide();
          //   $("#line-"+ lineNum +"-btn-cancel-attended-transfer").hide();
          //   $("#line-"+ lineNum +"-btn-terminate-attended-transfer").hide();
    
          //   $("#line-"+ lineNum +"-msg").html(this.lang.attended_transfer_call_terminated);
    
            //this.updatelinescroll(lineNum);
    
            window.setTimeout(function(){
                newCallStatus.hide();
                //this.updatelinescroll(lineNum);
            }, 1000);
        },
        onSessionDescriptionHandler: function(sdh:any, provisional){
            if (sdh) {
                if(sdh.peerConnection){
                    sdh.peerConnection.ontrack = function(event){
                        var pc = sdh.peerConnection;
  
                        // Gets Remote Audio Track (Local audio is setup via initial GUM)
                        var remoteStream = new MediaStream();
                        pc.getReceivers().forEach((receiver) => {
                            if(receiver.track && receiver.track.kind == "audio"){
                                remoteStream.addTrack(receiver.track);
                            }
                        });
                      //  var remoteAudio = $("#line-" + lineNum + "-transfer-remoteAudio").get(0);
                      //   remoteAudio.srcObject = remoteStream;
                      //   remoteAudio.onloadedmetadata = function(e) {
                      //       if (typeof remoteAudio.sinkId !== 'undefined') {
                      //           remoteAudio.setSinkId(session.data.AudioOutputDevice).then(function(){
                      //               console.log("sinkId applied: "+ session.data.AudioOutputDevice);
                      //           }).catch(function(e){
                      //               console.warn("Error using setSinkId: ", e);
                      //           });
                      //       }
                      //       remoteAudio.play();
                      //   }
  
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
    var inviterOptions = {
        requestDelegate: {
            onTrying: function(sip){
                newCallStatus.html(this.lang.trying);
                session.data.transfer[transferId].disposition = "trying";
                session.data.transfer[transferId].dispositionTime = this.utcDateNow();
  
              //   $("#line-" + lineNum + "-msg").html(this.lang.attended_transfer_call_started);
            },
            onProgress:function(sip){
                newCallStatus.html(this.lang.ringing);
                session.data.transfer[transferId].disposition = "progress";
                session.data.transfer[transferId].dispositionTime = this.utcDateNow();
  
              //   $("#line-" + lineNum + "-msg").html(this.lang.attended_transfer_call_started);
  
                var CancelAttendedTransferBtn:any = '$("#line-"+ lineNum +"-btn-cancel-attended-transfer")';
                CancelAttendedTransferBtn.off('click');
                CancelAttendedTransferBtn.on('click', function(){
                    newSession.cancel().catch(function(error){
                        console.warn("Failed to CANCEL", error);
                    });
                    //newCallStatus.html(this.lang.call_cancelled);
                    console.log("New call session canceled");
        
                    session.data.transfer[transferId].accept.complete = false;
                    session.data.transfer[transferId].accept.disposition = "cancel";
                    //session.data.transfer[transferId].accept.eventTime = this.utcDateNow();
        
                   // $("#line-" + lineNum + "-msg").html(this.lang.attended_transfer_call_cancelled);
        
                    //this.updatelinescroll(lineNum);
                });
                CancelAttendedTransferBtn.show();
        
                //this.updatelinescroll(lineNum);
            },
            onRedirect:function(sip){
                console.log("Redirect received:", sip);
            },
            onAccept:function(sip){
                newCallStatus.html(this.lang.call_in_progress);
              //   $("#line-"+ lineNum +"-btn-cancel-attended-transfer").hide();
                session.data.transfer[transferId].disposition = "accepted";
                session.data.transfer[transferId].dispositionTime = this.utcDateNow();
        
                var CompleteTransferBtn:any = '$("#line-"+ lineNum +"-btn-complete-attended-transfer")';
                CompleteTransferBtn.off('click');
                CompleteTransferBtn.on('click', function(){
                    var transferOptions  = { 
                        requestDelegate: {
                            onAccept: function(sip){
                                console.log("Attended transfer Accepted");
  
                                session.data.terminateby = "us";
                                session.data.reasonCode = 202;
                                session.data.reasonText = "Attended Transfer";
  
                                session.data.transfer[transferId].accept.complete = true;
                                session.data.transfer[transferId].accept.disposition = sip.message.reasonPhrase;
                                session.data.transfer[transferId].accept.eventTime = this.utcDateNow();
  
                              //   $("#line-" + lineNum + "-msg").html(this.lang.attended_transfer_complete_accepted);
  
                                this.updatelinescroll(lineNum);
  
                                // We must end this session manually
                                session.bye().catch(function(error){
                                    console.warn("Could not BYE after blind transfer:", error);
                                });
  
                                this.teardownSession(lineObj);
                            },
                            onReject: function(sip){
                                console.warn("Attended transfer rejected:", sip);
  
                                session.data.transfer[transferId].accept.complete = false;
                                session.data.transfer[transferId].accept.disposition = sip.message.reasonPhrase;
                                session.data.transfer[transferId].accept.eventTime = this.utcDateNow();
  
                              //   $("#line-" + lineNum + "-msg").html("Attended Transfer Failed!");
  
                                this.updatelinescroll(lineNum);
                            }
                        }
                    }
        
                    // Send REFER
                    session.refer(newSession, transferOptions).catch(function(error){
                        console.warn("Failed to REFER", error);
                    });
        
                   // newCallStatus.html(this.lang.attended_transfer_complete);
  
                    //this.updatelinescroll(lineNum);
                });
                CompleteTransferBtn.show();
        
                this.updatelinescroll(lineNum);
        
                var TerminateAttendedTransferBtn:any = '$("#line-"+ lineNum +"-btn-terminate-attended-transfer")';
                TerminateAttendedTransferBtn.off('click');
                TerminateAttendedTransferBtn.on('click', function(){
                    newSession.bye().catch(function(error){
                        console.warn("Failed to BYE", error);
                    });
                   // newCallStatus.html(this.lang.call_ended);
                    console.log("New call session end");
        
                    session.data.transfer[transferId].accept.complete = false;
                    session.data.transfer[transferId].accept.disposition = "bye";
                    //session.data.transfer[transferId].accept.eventTime = this.utcDateNow();
        
                  //   $("#line-"+ lineNum +"-btn-complete-attended-transfer").hide();
                  //   $("#line-"+ lineNum +"-btn-cancel-attended-transfer").hide();
                  //   $("#line-"+ lineNum +"-btn-terminate-attended-transfer").hide();
  
                    //$("#line-" + lineNum + "-msg").html(this.lang.attended_transfer_call_ended);
  
                    //this.updatelinescroll(lineNum);
  
                    window.setTimeout(function(){
                        newCallStatus.hide();
                        this.cancelTransferSession(lineNum);
                        this.updatelinescroll(lineNum);
                    }, 1000);
                });
                TerminateAttendedTransferBtn.show();
  
                this.updatelinescroll(lineNum);
            },
            onReject:function(sip){
                console.log("New call session rejected: ", sip.message.reasonPhrase);
                newCallStatus.html(this.lang.call_rejected);
                session.data.transfer[transferId].disposition = sip.message.reasonPhrase;
                session.data.transfer[transferId].dispositionTime = this.utcDateNow();
        
              //   $("#line-"+ lineNum +"-txt-FindTransferBuddy").parent().show();
              //   $("#line-"+ lineNum +"-btn-blind-transfer").show();
              //   $("#line-"+ lineNum +"-btn-attended-transfer").show();
        
              //   $("#line-"+ lineNum +"-btn-complete-attended-transfer").hide();
              //   $("#line-"+ lineNum +"-btn-cancel-attended-transfer").hide();
              //   $("#line-"+ lineNum +"-btn-terminate-attended-transfer").hide();
        
              //   $("#line-"+ lineNum +"-msg").html(this.lang.attended_transfer_call_rejected);
        
                this.updatelinescroll(lineNum);
        
                window.setTimeout(function(){
                    newCallStatus.hide();
                    this.updatelinescroll(lineNum);
                }, 1000);
            }
        }
    }
    newSession.invite(inviterOptions).catch(function(e){
        console.warn("Failed to send INVITE:", e);
    });
  }
  
  // Conference Calls
  // ================
  startConferenceCall(lineNum:any){
    if('$("#line-"+ lineNum +"-btn-CancelTransfer").is(":visible")'){
      this.cancelTransferSession(lineNum);
        return;
    }
  
  //   $("#line-"+ lineNum +"-btn-Conference").hide();
  //   $("#line-"+ lineNum +"-btn-CancelConference").show();
  
    this.holdSession(lineNum);
  //   $("#line-"+ lineNum +"-txt-FindConferenceBuddy").val("");
  //   $("#line-"+ lineNum +"-txt-FindConferenceBuddy").parent().show();
  
  //   $("#line-"+ lineNum +"-session-avatar").css("width", "50px");
  //   $("#line-"+ lineNum +"-session-avatar").css("height", "50px");
    this.restoreCallControls(lineNum)
  
  //   $("#line-"+ lineNum +"-btn-conference-dial").show();
  //   $("#line-"+ lineNum +"-btn-cancel-conference-dial").hide();
  //   $("#line-"+ lineNum +"-btn-join-conference-call").hide();
  //   $("#line-"+ lineNum +"-btn-terminate-conference-call").hide();
  
  //   $("#line-"+ lineNum +"-conference-status").hide();
  
  //   $("#line-"+ lineNum +"-Conference").show();
  
    //this.updatelinescroll(lineNum);
  }
  
  cancelConference(lineNum:any){
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null){
        console.warn("Null line or session");
        return;
    }
    var session = lineObj.SipSession;
    if(session.data.childsession){
        console.log("Child Conference call detected:", session.data.childsession.state);
        session.data.childsession.dispose().then(function(){
            session.data.childsession = null;
        }).catch(function(error){
            session.data.childsession = null;
            // Suppress message
        });
    }
  
  //   $("#line-"+ lineNum +"-session-avatar").css("width", "");
  //   $("#line-"+ lineNum +"-session-avatar").css("height", "");
  
  //   $("#line-"+ lineNum +"-btn-Conference").show();
  //   $("#line-"+ lineNum +"-btn-CancelConference").hide();
  
    this.unholdSession(lineNum);
  //   $("#line-"+ lineNum +"-Conference").hide();
  
    //this.updatelinescroll(lineNum);
  }
  conferenceOnkeydown(event:any, obj:any, lineNum:any) {
    var keycode = (event.keyCode ? event.keyCode : event.which);
    if (keycode == '13'){
        event.preventDefault();
  
        this.ConferenceDial(lineNum);
        return false;
    }
  }
  ConferenceDial(lineNum:any){
    var dstNo:any = '$("#line-"+ lineNum +"-txt-FindConferenceBuddy").val()';
    if(this.enableAlphanumericDial){
        dstNo = String(dstNo).replace(this.telAlphanumericRegEx, "").substring(0,this.maxdidLength);
    }
    else {
        dstNo = String(dstNo).replace(this.telNumericRegEx, "").substring(0,this.maxdidLength);
    }
    if(dstNo == ""){
        console.warn("Cannot transfer, must be [0-9*+#]");
        return;
    }
    
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null){
        console.warn("Null line or session");
        return;
    }
    var session = lineObj.SipSession;
  
    //this.HidePopup();
  
  //   $("#line-"+ lineNum +"-txt-FindConferenceBuddy").parent().hide();
  
  //   $("#line-"+ lineNum +"-btn-conference-dial").hide();
  //   $("#line-"+ lineNum +"-btn-cancel-conference-dial")
  //   $("#line-"+ lineNum +"-btn-join-conference-call").hide();
  //   $("#line-"+ lineNum +"-btn-terminate-conference-call").hide();
  
    var newCallStatus :any = '$("#line-"+ lineNum +"-conference-status")';
    newCallStatus.html(this.lang.connecting);
    newCallStatus.show();
  
    if(!session.data.confcalls) session.data.confcalls = [];
    session.data.confcalls.push({ 
        to: dstNo, 
        startTime: this.utcDateNow(), 
        disposition: "invite",
        dispositionTime: this.utcDateNow(), 
        accept : {
            complete: null,
            eventTime: null,
            disposition: ""
        }
    });
    var confCallId = session.data.confcalls.length-1;
  
    //this.updatelinescroll(lineNum);
  
    // SDP options
    var supportedConstraints = navigator.mediaDevices.getSupportedConstraints();
    var spdOptions = {
        sessionDescriptionHandlerOptions: {
            earlyMedia: true,
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
        spdOptions.sessionDescriptionHandlerOptions.constraints.audio["autoGainControl"] = this.autoGainControl;
    }
    if(supportedConstraints.echoCancellation) {
        spdOptions.sessionDescriptionHandlerOptions.constraints.audio["echoCancellation"] = this.echoCancellation;
    }
    if(supportedConstraints.noiseSuppression) {
        spdOptions.sessionDescriptionHandlerOptions.constraints.audio["noiseSuppression"] = this.noiseSuppression;
    }
  
    // Unlikely this will work
    if(session.data.withvideo){
        spdOptions.sessionDescriptionHandlerOptions.constraints.video = true;
        if(session.data.VideoSourceDevice != "default"){
            spdOptions.sessionDescriptionHandlerOptions.constraints.video["deviceId"] = { exact: session.data.VideoSourceDevice }
        }
        // Add additional Constraints
        if(supportedConstraints.frameRate && this.maxFrameRate != "") {
            spdOptions.sessionDescriptionHandlerOptions.constraints.video["frameRate"] = this.maxFrameRate;
        }
        if(supportedConstraints.height && this.videoHeight != "") {
            spdOptions.sessionDescriptionHandlerOptions.constraints.video["height"] = this.videoHeight;
        }
        if(supportedConstraints.aspectRatio && this.videoAspectRatio != "") {
            spdOptions.sessionDescriptionHandlerOptions.constraints.video["aspectRatio"] =this.videoAspectRatio;
        }
    }
  
    // Create new call session
    console.log("CONFERENCE INVITE: ", "sip:" + dstNo + "@" + this.sipDomain);
  
    var targetURI = UserAgent.makeURI("sip:"+ dstNo.replace(/#/g, "%23") + "@" + this.sipDomain);
    var newSession = new Inviter(this.userAgent, targetURI, spdOptions);
    newSession.data = {}
    newSession.delegate = {
        onBye: function(sip){
            console.log("New call session ended with BYE");
            newCallStatus.html(this.lang.call_ended);
            session.data.confcalls[confCallId].disposition = "bye";
            session.data.confcalls[confCallId].dispositionTime = this.utcDateNow();
    
          //   $("#line-"+ lineNum +"-txt-FindConferenceBuddy").parent().show();
          //   $("#line-"+ lineNum +"-btn-conference-dial").show();
    
          //   $("#line-"+ lineNum +"-btn-cancel-conference-dial").hide();
          //   $("#line-"+ lineNum +"-btn-join-conference-call").hide();
          //   $("#line-"+ lineNum +"-btn-terminate-conference-call").hide();
    
          //   $("#line-"+ lineNum +"-msg").html(this.lang.conference_call_terminated);
    
            this.updatelinescroll(lineNum);
    
            window.setTimeout(function(){
                newCallStatus.hide();
                this.updatelinescroll(lineNum);
            }, 1000);
        },
        onSessionDescriptionHandler: function(sdh:any, provisional){
            if (sdh) {
                if(sdh.peerConnection){
                    sdh.peerConnection.ontrack = function(event){
                        var pc = sdh.peerConnection;
  
                        // Gets Remote Audio Track (Local audio is setup via initial GUM)
                        var remoteStream = new MediaStream();
                        pc.getReceivers().forEach((receiver) =>{
                            if(receiver.track && receiver.track.kind == "audio"){
                                remoteStream.addTrack(receiver.track);
                            }
                        });
                      //  var remoteAudio = $("#line-" + lineNum + "-conference-remoteAudio").get(0);
                      //   remoteAudio.srcObject = remoteStream;
                      //   remoteAudio.onloadedmetadata = function(e) {
                      //       if (typeof remoteAudio.sinkId !== 'undefined') {
                      //           remoteAudio.setSinkId(session.data.AudioOutputDevice).then(function(){
                      //               console.log("sinkId applied: "+ session.data.AudioOutputDevice);
                      //           }).catch(function(e){
                      //               console.warn("Error using setSinkId: ", e);
                      //           });
                      //       }
                      //       remoteAudio.play();
                      //   }
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
    // Make sure we always restore audio paths
    newSession.stateChange.addListener(function(newState){
        if (newState == SessionState.Terminated) {
            // Ends the mixed audio, and releases the mic
            if(session.data.childsession.data.AudioSourceTrack && session.data.childsession.data.AudioSourceTrack.kind == "audio"){
                session.data.childsession.data.AudioSourceTrack.stop();
            }
            // Restore Audio Stream as it was changed
            if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
                var pc = session.sessionDescriptionHandler.peerConnection;
                pc.getSenders().forEach((RTCRtpSender) => {
                    if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                        RTCRtpSender.replaceTrack(session.data.AudioSourceTrack).then(function(){
                            if(session.data.ismute){
                                RTCRtpSender.track.enabled = false;
                            }
                            else {
                                RTCRtpSender.track.enabled = true;
                            }
                        }).catch(function(){
                            //console.error(e);
                        });
                        session.data.AudioSourceTrack = null;
                    }
                });
            }
        }
    });
    session.data.childsession = newSession;
    var inviterOptions = {
        requestDelegate: {
            onTrying: function(sip){
                newCallStatus.html(this.lang.ringing);
                session.data.confcalls[confCallId].disposition = "trying";
                session.data.confcalls[confCallId].dispositionTime = this.utcDateNow();
  
              //   $("#line-" + lineNum + "-msg").html(this.lang.conference_call_started);
            },
            onProgress:function(sip){
                newCallStatus.html(this.lang.ringing);
                session.data.confcalls[confCallId].disposition = "progress";
                session.data.confcalls[confCallId].dispositionTime = this.utcDateNow();
        
              //   $("#line-" + lineNum + "-msg").html(this.lang.conference_call_started);
  
                var CancelConferenceDialBtn:any =' $("#line-"+ lineNum +"-btn-cancel-conference-dial")';
                CancelConferenceDialBtn.off('click');
                CancelConferenceDialBtn.on('click', function(){
                    newSession.cancel().catch(function(error){
                        console.warn("Failed to CANCEL", error);
                    });
                   // newCallStatus.html(this.lang.call_cancelled);
                    console.log("New call session canceled");
        
                    session.data.confcalls[confCallId].accept.complete = false;
                    session.data.confcalls[confCallId].accept.disposition = "cancel";
                    //session.data.confcalls[confCallId].accept.eventTime = this.utcDateNow();
        
                   // $("#line-" + lineNum + "-msg").html(this.lang.conference_call_cancelled);
        
                    //this.updatelinescroll(lineNum);
                });
                CancelConferenceDialBtn.show();
  
                this.updatelinescroll(lineNum);
            },
            onRedirect:function(sip){
                console.log("Redirect received:", sip);
            },
            onAccept:function(sip){
                newCallStatus.html(this.lang.call_in_progress);
              //   $("#line-"+ lineNum +"-btn-cancel-conference-dial").hide();
                session.data.confcalls[confCallId].complete = true;
                session.data.confcalls[confCallId].disposition = "accepted";
                session.data.confcalls[confCallId].dispositionTime = this.utcDateNow();
  
                // Join Call
                var JoinCallBtn :any= '$("#line-"+ lineNum +"-btn-join-conference-call")';
                JoinCallBtn.off('click');
                JoinCallBtn.on('click', function(){
                    // Merge Call Audio
                    if(!session.data.childsession){
                        console.warn("Conference session lost");
                        return;
                    }
  
                    var outputStreamForSession = new MediaStream();
                    var outputStreamForConfSession = new MediaStream();
  
                    var pc = session.sessionDescriptionHandler.peerConnection;
                    var confPc = session.data.childsession.sessionDescriptionHandler.peerConnection;
  
                    // Get conf call input channel
                    confPc.getReceivers().forEach((RTCRtpReceiver) =>{
                        if(RTCRtpReceiver.track && RTCRtpReceiver.track.kind == "audio") {
                            console.log("Adding conference session:", RTCRtpReceiver.track.label);
                            outputStreamForSession.addTrack(RTCRtpReceiver.track);
                        }
                    });
  
                    // Get session input channel
                    pc.getReceivers().forEach((RTCRtpReceiver)=> {
                        if(RTCRtpReceiver.track && RTCRtpReceiver.track.kind == "audio") {
                            console.log("Adding conference session:", RTCRtpReceiver.track.label);
                            outputStreamForConfSession.addTrack(RTCRtpReceiver.track);
                        }
                    });
  
                    // Replace tracks of Parent Call
                    pc.getSenders().forEach((RTCRtpSender) =>{
                        if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                            console.log("Switching to mixed Audio track on session");
        
                            session.data.AudioSourceTrack = RTCRtpSender.track;
                            outputStreamForSession.addTrack(RTCRtpSender.track);
                            //var mixedAudioTrack = this.mixAudioStreams(outputStreamForSession).getAudioTracks()[0];
                            //mixedAudioTrack.IsMixedTrack = true;
  
                            //RTCRtpSender.replaceTrack(mixedAudioTrack);
                        }
                    });
                    // Replace tracks of Child Call
                    confPc.getSenders().forEach((RTCRtpSender)=> {
                      //   if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                      //       console.log("Switching to mixed Audio track on conf call");
        
                      //       session.data.childsession.data.AudioSourceTrack = RTCRtpSender.track;
                      //       outputStreamForConfSession.addTrack(RTCRtpSender.track);
                      //       //var mixedAudioTrackForConf = this.mixAudioStreams(outputStreamForConfSession).getAudioTracks()[0];
                      //       //mixedAudioTrackForConf.IsMixedTrack = true;
  
                      //       //RTCRtpSender.replaceTrack(mixedAudioTrackForConf);
                      //   }
                    });
        
                   // newCallStatus.html(this.lang.call_in_progress);
                    console.log("Conference Call In Progress");
        
                    session.data.confcalls[confCallId].accept.complete = true;
                    session.data.confcalls[confCallId].accept.disposition = "join";
                    //session.data.confcalls[confCallId].accept.eventTime = this.utcDateNow();
        
                  //   $("#line-"+ lineNum +"-btn-terminate-conference-call").show();
        
                   // $("#line-" + lineNum + "-msg").html(this.lang.conference_call_in_progress);
        
                    JoinCallBtn.hide();
                    //this.updatelinescroll(lineNum);
  
                    // Take the parent call off hold after a second
                    window.setTimeout(function(){
                      this.unholdSession(lineNum);
                      this.updatelinescroll(lineNum);
                    }, 1000);
                });
                JoinCallBtn.show();
  
                this.updatelinescroll(lineNum);
  
                // End Call
                var TerminateConfCallBtn:any = '$("#line-"+ lineNum +"-btn-terminate-conference-call")';
                TerminateConfCallBtn.off('click');
                TerminateConfCallBtn.on('click', function(){
                    newSession.bye().catch(function(e){
                        console.warn("Failed to BYE", e);
                    });
                    //newCallStatus.html(this.lang.call_ended);
                    console.log("New call session end");
  
                    // session.data.confcalls[confCallId].accept.complete = false;
                    session.data.confcalls[confCallId].accept.disposition = "bye";
                    //session.data.confcalls[confCallId].accept.eventTime = this.utcDateNow();
  
                   // $("#line-" + lineNum + "-msg").html(this.lang.conference_call_ended);
  
                    //this.updatelinescroll(lineNum);
  
                    window.setTimeout(function(){
                        newCallStatus.hide();
                        this.cancelConference(lineNum);
                        this.updatelinescroll(lineNum);
                    }, 1000);
                });
                TerminateConfCallBtn.show();
        
                this.updatelinescroll(lineNum);
            },
            onReject:function(sip){
                console.log("New call session rejected: ", sip.message.reasonPhrase);
                newCallStatus.html(this.lang.call_rejected);
                session.data.confcalls[confCallId].disposition = sip.message.reasonPhrase;
                session.data.confcalls[confCallId].dispositionTime = this.utcDateNow();
        
              //   $("#line-"+ lineNum +"-txt-FindConferenceBuddy").parent().show();
              //   $("#line-"+ lineNum +"-btn-conference-dial").show();
        
              //   $("#line-"+ lineNum +"-btn-cancel-conference-dial").hide();
              //   $("#line-"+ lineNum +"-btn-join-conference-call").hide();
              //   $("#line-"+ lineNum +"-btn-terminate-conference-call").hide();
        
              //   $("#line-"+ lineNum +"-msg").html(this.lang.conference_call_rejected);
        
                this.updatelinescroll(lineNum);
        
                window.setTimeout(function(){
                    newCallStatus.hide();
                    this.updatelinescroll(lineNum);
                }, 1000);
            }
        }
    }
    newSession.invite(inviterOptions).catch(function(e){
        console.warn("Failed to send INVITE:", e);
    });
  }
  
  // In-Session Call Functionality
  // ============================= 
  cancelSession(lineNum:any) {
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null) return;
  
    lineObj.SipSession.data.terminateby = "us";
    lineObj.SipSession.data.reasonCode = 0;
    lineObj.SipSession.data.reasonText = "Call Cancelled";
  
    console.log("Cancelling session : "+ lineNum);
    if(lineObj.SipSession.state == SessionState.Initial || lineObj.SipSession.state == SessionState.Establishing){
        lineObj.SipSession.cancel();
    }
    else {
        console.warn("Session not in correct state for cancel.", lineObj.SipSession.state);
        console.log("Attempting teardown : "+ lineNum);
        //this.teardownSession(lineObj);
    }
  
  //   $("#line-" + lineNum + "-msg").html(this.lang.call_cancelled);
  }
  holdSession(lineNum:any) {
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null) return;
    var session = lineObj.SipSession;
    if(session.isOnHold == true) {
        console.log("Call is is already on hold:", lineNum);
        return;
    }
    console.log("Putting Call on hold:", lineNum);
    session.isOnHold = true;
  
    var sessionDescriptionHandlerOptions = session.sessionDescriptionHandlerOptionsReInvite;
    sessionDescriptionHandlerOptions.hold = true;
    session.sessionDescriptionHandlerOptionsReInvite = sessionDescriptionHandlerOptions;
  
    var options = {
        requestDelegate: {
            onAccept: function(){
                if(session && session.sessionDescriptionHandler && session.sessionDescriptionHandler.peerConnection){
                    var pc = session.sessionDescriptionHandler.peerConnection;
                    // Stop all the inbound streams
                    pc.getReceivers().forEach(function(RTCRtpReceiver){
                        if (RTCRtpReceiver.track) RTCRtpReceiver.track.enabled = false;
                    });
                    // Stop all the outbound streams (especially useful for Conference Calls!!)
                    pc.getSenders().forEach(function(RTCRtpSender){
                        // Mute Audio
                        if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                            if(RTCRtpSender.track.IsMixedTrack == true){
                                if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
                                    console.log("Muting Mixed Audio Track : "+ session.data.AudioSourceTrack.label);
                                    session.data.AudioSourceTrack.enabled = false;
                                }
                            }
                            console.log("Muting Audio Track : "+ RTCRtpSender.track.label);
                            RTCRtpSender.track.enabled = false;
                        }
                        // Stop Video
                        else if(RTCRtpSender.track && RTCRtpSender.track.kind == "video"){
                            RTCRtpSender.track.enabled = false;
                        }
                    });
                }
                session.isOnHold = true;
                console.log("Call is is on hold:", lineNum);
  
              //   $("#line-" + lineNum + "-btn-Hold").hide();
              //   $("#line-" + lineNum + "-btn-Unhold").show();
              //   $("#line-" + lineNum + "-msg").html(this.lang.call_on_hold);
  
                // Log Hold
                if(!session.data.hold) session.data.hold = [];
                session.data.hold.push({ event: "hold", eventTime: this.utcDateNow() });
  
                this.updatelinescroll(lineNum);
  
           
            },
            onReject: function(){
                session.isOnHold = false;
                console.warn("Failed to put the call on hold:", lineNum);
            }
        }
    };
    session.invite(options).catch(function(error){
        session.isOnHold = false;
        console.warn("Error attempting to put the call on hold:", error);
    });
  }
  unholdSession(lineNum:any) {
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null) return;
    var session = lineObj.SipSession;
    if(session.isOnHold == false) {
        console.log("Call is already off hold:", lineNum);
        return;
    }
    console.log("Taking call off hold:", lineNum);
    session.isOnHold = false;
  
    var sessionDescriptionHandlerOptions = session.sessionDescriptionHandlerOptionsReInvite;
    sessionDescriptionHandlerOptions.hold = false;
    session.sessionDescriptionHandlerOptionsReInvite = sessionDescriptionHandlerOptions;
  
    var options = {
        requestDelegate: {
            onAccept: function(){
                if(session && session.sessionDescriptionHandler && session.sessionDescriptionHandler.peerConnection){
                    var pc = session.sessionDescriptionHandler.peerConnection;
                    // Restore all the inbound streams
                    pc.getReceivers().forEach(function(RTCRtpReceiver){
                        if (RTCRtpReceiver.track) RTCRtpReceiver.track.enabled = true;
                    });
                    // Restore all the outbound streams
                    pc.getSenders().forEach(function(RTCRtpSender){
                        // Unmute Audio
                        if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                            if(RTCRtpSender.track.IsMixedTrack == true){
                                if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
                                    console.log("Unmuting Mixed Audio Track : "+ session.data.AudioSourceTrack.label);
                                    session.data.AudioSourceTrack.enabled = true;
                                }
                            }
                            console.log("Unmuting Audio Track : "+ RTCRtpSender.track.label);
                            RTCRtpSender.track.enabled = true;
                        }
                        else if(RTCRtpSender.track && RTCRtpSender.track.kind == "video") {
                            RTCRtpSender.track.enabled = true;
                        }
                    });
                }
                session.isOnHold = false;
                console.log("Call is off hold:", lineNum);
  
              //   $("#line-" + lineNum + "-btn-Hold").show();
              //   $("#line-" + lineNum + "-btn-Unhold").hide();
              //   $("#line-" + lineNum + "-msg").html(this.lang.call_in_progress);
  
                // Log Hold
                if(!session.data.hold) session.data.hold = [];
                session.data.hold.push({ event: "unhold", eventTime: this.utcDateNow() });
  
                this.updatelinescroll(lineNum);
  
         
            },
            onReject: function(){
                session.isOnHold = true;
                console.warn("Failed to put the call on hold", lineNum);
            }
        }
    };
    session.invite(options).catch(function(error){
        session.isOnHold = true;
        console.warn("Error attempting to take to call off hold", error);
    });
  }
  
  muteSession(lineNum:any){
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null) return;
  
  //   $("#line-"+ lineNum +"-btn-Unmute").show();
  //   $("#line-"+ lineNum +"-btn-Mute").hide();
  
    var session = lineObj.SipSession;
    var pc = session.sessionDescriptionHandler.peerConnection;
    pc.getSenders().forEach((RTCRtpSender) => {
        if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
            if(RTCRtpSender.track.IsMixedTrack == true){
                if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
                    console.log("Muting Mixed Audio Track : "+ session.data.AudioSourceTrack.label);
                    session.data.AudioSourceTrack.enabled = false;
                }
            }
            console.log("Muting Audio Track : "+ RTCRtpSender.track.label);
            RTCRtpSender.track.enabled = false;
        }
    });
  
    if(!session.data.mute) session.data.mute = [];
    session.data.mute.push({ event: "mute", eventTime: this.utcDateNow() });
    session.data.ismute = true;
  
  //   $("#line-" + lineNum + "-msg").html(this.lang.call_on_mute);
  
    //this.updatelinescroll(lineNum);
  
  }
  
  unmuteSession(lineNum:any){
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null) return;
  
  //   $("#line-"+ lineNum +"-btn-Unmute").hide();
  //   $("#line-"+ lineNum +"-btn-Mute").show();
  
    var session = lineObj.SipSession;
    var pc = session.sessionDescriptionHandler.peerConnection;
    pc.getSenders().forEach((RTCRtpSender) =>{
        if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
            if(RTCRtpSender.track.IsMixedTrack == true){
                if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
                    console.log("Unmuting Mixed Audio Track : "+ session.data.AudioSourceTrack.label);
                    session.data.AudioSourceTrack.enabled = true;
                }
            }
            console.log("Unmuting Audio Track : "+ RTCRtpSender.track.label);
            RTCRtpSender.track.enabled = true;
        }
    });
  
    if(!session.data.mute) session.data.mute = [];
    session.data.mute.push({ event: "unmute", eventTime: this.utcDateNow() });
    session.data.ismute = false;
  
  //   $("#line-" + lineNum + "-msg").html(this.lang.call_off_mute);
  
    //this.updatelinescroll(lineNum);
  
  }
  
  
  endSession(lineNum:any) {
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null) return;
  
    console.log("Ending call with: "+ lineNum);
    lineObj.SipSession.data.terminateby = "us";
    lineObj.SipSession.data.reasonCode = 16;
    lineObj.SipSession.data.reasonText = "Normal Call clearing";
  
    lineObj.SipSession.bye().catch(function(e){
        console.warn("Failed to bye the session!", e);
    });
  
  //   $("#line-" + lineNum + "-msg").html(this.lang.call_ended);
  //   $("#line-" + lineNum + "-ActiveCall").hide();
  
    //this.teardownSession(lineObj);
  
    //this.updatelinescroll(lineNum);
  }
  
  sendDTMF(lineNum:any, itemStr:any) {
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null) return;
  
    // https://developer.mozilla.org/en-US/docs/Web/API/RTCDTMFSender/insertDTMF
    var options = {
        duration: 100,
        interToneGap: 70
    }
    
    if(lineObj.SipSession.isOnHold == true){
        if(lineObj.SipSession.data.childsession){
            if(lineObj.SipSession.data.childsession.state == SessionState.Established){
                console.log("Sending DTMF ("+ itemStr +"): "+ lineObj.LineNumber + " child session");
  
                var result = lineObj.SipSession.data.childsession.sessionDescriptionHandler.sendDtmf(itemStr, options);
                if(result){
                    console.log("Sent DTMF ("+ itemStr +") child session");
                }
                else{
                    console.log("Failed to send DTMF ("+ itemStr +") child session");
                }
            }
            else {
                console.warn("Cannot Send DTMF ("+ itemStr +"): "+ lineObj.LineNumber + " is on hold, and the child session is not established");
            }
        } 
        else {
            console.warn("Cannot Send DTMF ("+ itemStr +"): "+ lineObj.LineNumber + " is on hold, and there is no child session");
        }
    } 
    else {
        if(lineObj.SipSession.state == SessionState.Established || lineObj.SipSession.state == SessionState.Establishing){
            console.log("Sending DTMF ("+ itemStr +"): "+ lineObj.LineNumber);
  
            var result = lineObj.SipSession.sessionDescriptionHandler.sendDtmf(itemStr, options);
            if(result){
                console.log("Sent DTMF ("+ itemStr +")");
            }
            else{
                console.log("Failed to send DTMF ("+ itemStr +")");
            }
        
          //   $("#line-" + lineNum + "-msg").html(this.lang.send_dtmf + ": "+ itemStr);
        
           // this.updatelinescroll(lineNum);
  
        } 
        else {
            console.warn("Cannot Send DTMF ("+ itemStr +"): "+ lineObj.LineNumber + " session is not establishing or established");
        }
    }
  }
  
  switchVideoSource(lineNum:any, srcId:any){
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null){
        console.warn("Line or Session is Null");
        return;
    }
    var session = lineObj.SipSession;
  
  //   $("#line-" + lineNum + "-msg").html(this.lang.switching_video_source);
  
    var supportedConstraints = navigator.mediaDevices.getSupportedConstraints();
    var constraints = { 
        audio: false, 
        video: { deviceId: {} }
    }
    if(srcId != "default"){
        constraints.video.deviceId = { exact: srcId }
    }
  
    // Add additional Constraints
    if(supportedConstraints.frameRate && this.maxFrameRate != "") {
        constraints.video["frameRate"] = this.maxFrameRate;
    }
    if(supportedConstraints.height && this.videoHeight != "") {
        constraints.video["height"] = this.videoHeight;
    }
    if(supportedConstraints.aspectRatio && this.videoAspectRatio != "") {
        constraints.video["aspectRatio"] = this.videoAspectRatio;
    }
  
    session.data.VideoSourceDevice = srcId;
  
    var pc = session.sessionDescriptionHandler.peerConnection;
  
    var localStream = new MediaStream();
    navigator.mediaDevices.getUserMedia(constraints).then(function(newStream){
        var newMediaTrack = newStream.getVideoTracks()[0];
        // var pc = session.sessionDescriptionHandler.peerConnection;
        pc.getSenders().forEach((RTCRtpSender) =>{
            if(RTCRtpSender.track && RTCRtpSender.track.kind == "video") {
                console.log("Switching Video Track : "+ RTCRtpSender.track.label + " to "+ newMediaTrack.label);
                RTCRtpSender.track.stop();
                RTCRtpSender.replaceTrack(newMediaTrack);
                localStream.addTrack(newMediaTrack);
            }
        });
    }).catch(function(e){
        console.error("Error on getUserMedia", e, constraints);
    });
  
    // Restore Audio Stream is it was changed
    if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
        pc.getSenders().forEach((RTCRtpSender) =>{
            if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                RTCRtpSender.replaceTrack(session.data.AudioSourceTrack).then(function(){
                    if(session.data.ismute){
                        RTCRtpSender.track.enabled = false;
                    }
                    else {
                        RTCRtpSender.track.enabled = true;
                    }
                }).catch(function(){
                    //console.error();
                });
                session.data.AudioSourceTrack = null;
            }
        });
    }
  
    // Set Preview
  //   console.log("Showing as preview...");
  //   var localVideo = $("#line-" + lineNum + "-localVideo").get(0);
  //   localVideo.srcObject = localStream;
  //   localVideo.onloadedmetadata = function(e) {
  //       localVideo.play();
  //  }
  }

  sendCanvas(lineNum:any){
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null){
        console.warn("Line or Session is Null");
        return;
    }
    var session = lineObj.SipSession;
    
  //   $("#line-" + lineNum + "-msg").html(this.lang.switching_to_canvas);
  
    // Create scratch Pad
    //this.RemoveScratchpad(lineNum);
  
    // TODO: This needs work!
    var newCanvas:any = "$('<canvas/>')";
    newCanvas.prop("id", "line-" + lineNum + "-scratchpad");
  //   $("#line-" + lineNum + "-scratchpad-container").append(newCanvas);
  //   $("#line-" + lineNum + "-scratchpad").css("display", "inline-block");
  //   $("#line-" + lineNum + "-scratchpad").css("width", "100%"); // SD
  //   $("#line-" + lineNum + "-scratchpad").css("height", "100%"); // SD
  //   $("#line-" + lineNum + "-scratchpad").prop("width", 640); // SD
  //   $("#line-" + lineNum + "-scratchpad").prop("height", 360); // SD
  //   $("#line-" + lineNum + "-scratchpad-container").show();
  
    console.log("Canvas for Scratchpad created...");
  
  //   let scratchpad = new Canvas("line-" + lineNum + "-scratchpad");
  //   scratchpad.id = "line-" + lineNum + "-scratchpad";
  //   scratchpad.backgroundColor = "#FFFFFF";
  //   scratchpad.isDrawingMode = true;
  //   scratchpad.renderAll();
  //   scratchpad.redrawIntrtval = window.setInterval(function(){
  //       scratchpad.renderAll();
  //   }, 1000);
  
  //   this.CanvasCollection.push(scratchpad);
  
    // Get The Canvas Stream
    //var canvasMediaStream = $("#line-"+ lineNum +"-scratchpad").get(0).captureStream(25);
    //var canvasMediaTrack = canvasMediaStream.getVideoTracks()[0];
  
    // Switch Tracks
    var pc = session.sessionDescriptionHandler.peerConnection;
    pc.getSenders().forEach((RTCRtpSender)=> {
        if(RTCRtpSender.track && RTCRtpSender.track.kind == "video") {
            console.log("Switching Track : "+ RTCRtpSender.track.label + " to Scratchpad Canvas");
            RTCRtpSender.track.stop();
            //RTCRtpSender.replaceTrack(canvasMediaTrack);
        }
    });
  
    // Restore Audio Stream is it was changed
    if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
        pc.getSenders().forEach((RTCRtpSender)=> {
            if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                RTCRtpSender.replaceTrack(session.data.AudioSourceTrack).then(function(){
                    if(session.data.ismute){
                        RTCRtpSender.track.enabled = false;
                    }
                    else {
                        RTCRtpSender.track.enabled = true;
                    }
                }).catch(function(){
              //      console.error(e);
                });
                session.data.AudioSourceTrack = null;
            }
        });
    }
  
    // Set Preview
    // ===========
  //   console.log("Showing as preview...");
  //   var localVideo = $("#line-" + lineNum + "-localVideo").get(0);
  //   localVideo.srcObject = canvasMediaStream;
  //   localVideo.onloadedmetadata = function(e) {
  //       localVideo.play();
  //  }
  }
  sendVideo(lineNum:any, src:any){
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null){
        console.warn("Line or Session is Null");
        return;
    }
  
    var session = lineObj.SipSession;
  
  //   $("#line-"+ lineNum +"-src-camera").prop("disabled", false);
  //   $("#line-"+ lineNum +"-src-canvas").prop("disabled", false);
  //   $("#line-"+ lineNum +"-src-desktop").prop("disabled", false);
  //   $("#line-"+ lineNum +"-src-video").prop("disabled", true);
  //   $("#line-"+ lineNum +"-src-blank").prop("disabled", false);
  
  //   $("#line-" + lineNum + "-msg").html(this.lang.switching_to_shared_video);
  
  //   $("#line-" + lineNum + "-scratchpad-container").hide();
  //   ///this.RemoveScratchpad(lineNum);
  //   $("#line-"+ lineNum +"-sharevideo").hide();
  // //$("#line-"+ lineNum +"-sharevideo").get(0).pause();
  //   $("#line-"+ lineNum +"-sharevideo").get(0).removeAttribute('src');
  //   $("#line-"+ lineNum +"-sharevideo").get(0).load();
  
  //   $("#line-"+ lineNum +"-localVideo").hide();
  //   $("#line-"+ lineNum +"-remote-videos").hide();
    // $("#line-"+ lineNum +"-remoteVideo").appendTo("#line-" + lineNum + "-preview-container");
  
    // Create Video Object
    var newVideo:any =' $("#line-" + lineNum + "-sharevideo")';
    newVideo.prop("src", src);
    newVideo.off("loadedmetadata");
    newVideo.on("loadedmetadata", ()=> {
        console.log("Video can play now... ");
  
        // Resample Video
        var ResampleSize = 360;
        if(this.videoResampleSize == "HD") ResampleSize = 720;
        if(this.videoResampleSize == "FHD") ResampleSize = 1080;
  
        var videoObj = newVideo.get(0);
        var resampleCanvas:any = "$('<canvas/>').get(0)";
  
      //   var videoWidth = videoObj.videoWidth;
      //   var videoHeight = videoObj.videoHeight;
  
        var videoWidth = 50;
        var videoHeight = 70;
  
        if(videoWidth >= videoHeight){
            // Landscape / Square
            if(videoHeight > ResampleSize){
                var p = ResampleSize / videoHeight;
                videoHeight = ResampleSize;
                videoWidth = videoWidth * p;
            }
        }
        else {
            // Portrait... (phone turned on its side)
            if(videoWidth > ResampleSize){
                var p = ResampleSize / videoWidth;
                videoWidth = ResampleSize;
                videoHeight = videoHeight * p;
            }
        }
  
      //   resampleCanvas.width = videoWidth;
      //   resampleCanvas.height = videoHeight;
      //   var resampleContext = resampleCanvas.getContext("2d");
  
        window.clearInterval(session.data.videoResampleInterval);
        session.data.videoResampleInterval = window.setInterval(function(){
           // resampleContext.drawImage(videoObj, 0, 0, videoWidth, videoHeight);
        }, 40); // 25frames per second
  
        // Capture the streams
        var videoMediaStream = null;
        if('captureStream' in videoObj) {
            //videoMediaStream = videoObj.captureStream();
        }
        else if('mozCaptureStream' in videoObj) {
            // This doesn't really work?
            // see: https://developer.mozilla.org/en-US/docs/Web/API/HTMLMediaElement/captureStream
            //videoMediaStream = videoObj.mozCaptureStream();
        }
        else {
            // This is not supported??.
            // videoMediaStream = videoObj.webkitCaptureStream();
            console.warn("Cannot capture stream from video, this will result in no audio being transmitted.")
        }
        //var resampleVideoMediaStream = resampleCanvas.captureStream(25);
  
        // Get the Tracks
        //var videoMediaTrack = resampleVideoMediaStream.getVideoTracks()[0];
        //var audioTrackFromVideo = (videoMediaStream != null )? videoMediaStream.getAudioTracks()[0] : null;
  
        // Switch & Merge Tracks
        var pc = session.sessionDescriptionHandler.peerConnection;
        pc.getSenders().forEach((RTCRtpSender)=> {
            if(RTCRtpSender.track && RTCRtpSender.track.kind == "video") {
                console.log("Switching Track : "+ RTCRtpSender.track.label);
                RTCRtpSender.track.stop();
          //      RTCRtpSender.replaceTrack(videoMediaTrack);
            }
            if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                console.log("Switching to mixed Audio track on session");
                
                session.data.AudioSourceTrack = RTCRtpSender.track;
  
                var mixedAudioStream = new MediaStream();
            //    if(audioTrackFromVideo) mixedAudioStream.addTrack(audioTrackFromVideo);
                mixedAudioStream.addTrack(RTCRtpSender.track);
                var mixedAudioTrack = this.mixAudioStreams(mixedAudioStream).getAudioTracks()[0];
                mixedAudioTrack.IsMixedTrack = true;
  
                RTCRtpSender.replaceTrack(mixedAudioTrack);
            }
        });
  
        // Set Preview
        console.log("Showing as preview...");
        var localVideo:any = '$("#line-" + lineNum + "-localVideo").get(0)';
       // localVideo.srcObject = videoMediaStream;
        //localVideo.onloadedmetadata = function(e) {
          //  localVideo.play().then(function(){
          //       console.log("Playing Preview Video File");
          //   }).catch(function(e){
          //       console.error("Cannot play back video", e);
          //   });
        //}
        // Play the video
        console.log("Starting Video...");
       // $("#line-"+ lineNum +"-sharevideo").get(0).play();
    });
  
  //   $("#line-"+ lineNum +"-sharevideo").show();
    console.log("Video for Sharing created...");
  }
  
  shareScreen(lineNum:any){
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null){
        console.warn("Line or Session is Null");
        return;
    }
    var session = lineObj.SipSession;
  
  //   $("#line-" + lineNum + "-msg").html(this.lang.switching_to_shared_screen);
  
    var localStream = new MediaStream();
    var pc = session.sessionDescriptionHandler.peerConnection;
  
    // TODO: Remove legacy ones
    if (navigator.mediaDevices.getDisplayMedia) {
        // EDGE, legacy support
        var screenShareConstraints = { video: true, audio: false }
        navigator.mediaDevices.getDisplayMedia(screenShareConstraints).then(function(newStream) {
            console.log("navigator.getDisplayMedia")
            var newMediaTrack = newStream.getVideoTracks()[0];
            pc.getSenders().forEach((RTCRtpSender)=> {
                if(RTCRtpSender.track && RTCRtpSender.track.kind == "video") {
                    console.log("Switching Video Track : "+ RTCRtpSender.track.label + " to Screen");
                    RTCRtpSender.track.stop();
                    RTCRtpSender.replaceTrack(newMediaTrack);
                    localStream.addTrack(newMediaTrack);
                }
            });
  
            // Set Preview
            // ===========
            console.log("Showing as preview...");
            var localVideo :any = '$("#line-" + lineNum + "-localVideo").get(0)';
          //   localVideo.srcObject = localStream;
          //   localVideo.onloadedmetadata = function(e) {
          //       localVideo.play();
          //  }
        }).catch((err) =>{
            console.error("Error on getUserMedia");
        });
    } 
    else if (navigator.mediaDevices.getDisplayMedia) {
        // New standard
        var screenShareConstraints = { video: true, audio: false }
        navigator.mediaDevices.getDisplayMedia(screenShareConstraints).then(function(newStream) {
            console.log("navigator.mediaDevices.getDisplayMedia")
            var newMediaTrack = newStream.getVideoTracks()[0];
            pc.getSenders().forEach((RTCRtpSender) =>{
                if(RTCRtpSender.track && RTCRtpSender.track.kind == "video") {
                    console.log("Switching Video Track : "+ RTCRtpSender.track.label + " to Screen");
                    RTCRtpSender.track.stop();
                    RTCRtpSender.replaceTrack(newMediaTrack);
                    localStream.addTrack(newMediaTrack);
                }
            });
  
            // Set Preview
            // ===========
            console.log("Showing as preview...");
            var localVideo :any=' $("#line-" + lineNum + "-localVideo").get(0)';
          //   localVideo.srcObject = localStream;
          //   localVideo.onloadedmetadata = function(e) {
          //       localVideo.play();
          //   }
        }).catch((err) =>{
            console.error("Error on getUserMedia");
        });
    } 
    else {
        // Firefox, apparently
        let screenShareConstraints:any = { video: { mediaSource: 'screen' }, audio: false }
        navigator.mediaDevices.getUserMedia(screenShareConstraints).then(function(newStream) {
            console.log("navigator.mediaDevices.getUserMedia")
            var newMediaTrack = newStream.getVideoTracks()[0];
            pc.getSenders().forEach((RTCRtpSender)=> {
                if(RTCRtpSender.track && RTCRtpSender.track.kind == "video") {
                    console.log("Switching Video Track : "+ RTCRtpSender.track.label + " to Screen");
                    RTCRtpSender.track.stop();
                    RTCRtpSender.replaceTrack(newMediaTrack);
                    localStream.addTrack(newMediaTrack);
                }
            });
  
            // Set Preview
            console.log("Showing as preview...");
            var localVideo:any = '$("#line-" + lineNum + "-localVideo").get(0)';
          //   localVideo.srcObject = localStream;
          //   localVideo.onloadedmetadata = function(e) {
          //       localVideo.play();
          //   }
        }).catch((err)=> {
            console.error("Error on getUserMedia");
        });
    }
  
    // Restore Audio Stream is it was changed
    if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
        pc.getSenders().forEach((RTCRtpSender)=> {
            if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                RTCRtpSender.replaceTrack(session.data.AudioSourceTrack).then(function(){
                    if(session.data.ismute){
                        RTCRtpSender.track.enabled = false;
                    }
                    else {
                        RTCRtpSender.track.enabled = true;
                    }
                }).catch(function(){
                   // console.error(e);
                });
                session.data.AudioSourceTrack = null;
            }
        });
    }
  
  }

  disableVideoStream(lineNum:any){
    var lineObj = this.findLineByNumber(lineNum);
    if(lineObj == null || lineObj.SipSession == null){
        console.warn("Line or Session is Null");
        return;
    }
    var session = lineObj.SipSession;
  
    var pc = session.sessionDescriptionHandler.peerConnection;
    pc.getSenders().forEach((RTCRtpSender) =>{
        if(RTCRtpSender.track && RTCRtpSender.track.kind == "video") {
            console.log("Disable Video Track : "+ RTCRtpSender.track.label + "");
            RTCRtpSender.track.enabled = false; //stop();
        }
        if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
            if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
                RTCRtpSender.replaceTrack(session.data.AudioSourceTrack).then(function(){
                    if(session.data.ismute){
                        RTCRtpSender.track.enabled = false;
                    }
                    else {
                        RTCRtpSender.track.enabled = true;
                    }
                }).catch(function(){
                    //console.error(e);
                });
                session.data.AudioSourceTrack = null;
            }
        }
    });
  
    // Set Preview
    console.log("Showing as preview...");
    var localVideo :any= '$("#line-" + lineNum + "-localVideo").get(0)';
  //   localVideo.pause();
  //   localVideo.removeAttribute('src');
  //   localVideo.load();
  
  //   $("#line-" + lineNum + "-msg").html(this.lang.video_disabled);
  }

  showDtmfMenu(lineNum:any){
    console.log("Show DTMF");
    //this.HidePopup();
  
    this.restoreCallControls(lineNum)
  
    // DTMF
    var html = ""
    html += "<div>";
    html += "<table cellspacing=10 cellpadding=0 style=\"margin-left:auto; margin-right: auto\">";
    html += "<tr><td><button class=dialButtons onclick=\"sendDTMF('"+ lineNum +"', '1')\"><div>1</div><span>&nbsp;</span></button></td>"
    html += "<td><button class=dialButtons onclick=\"sendDTMF('"+ lineNum +"', '2')\"><div>2</div><span>ABC</span></button></td>"
    html += "<td><button class=dialButtons onclick=\"sendDTMF('"+ lineNum +"', '3')\"><div>3</div><span>DEF</span></button></td></tr>";
    html += "<tr><td><button class=dialButtons onclick=\"sendDTMF('"+ lineNum +"', '4')\"><div>4</div><span>GHI</span></button></td>"
    html += "<td><button class=dialButtons onclick=\"sendDTMF('"+ lineNum +"', '5')\"><div>5</div><span>JKL</span></button></td>"
    html += "<td><button class=dialButtons onclick=\"sendDTMF('"+ lineNum +"', '6')\"><div>6</div><span>MNO</span></button></td></tr>";
    html += "<tr><td><button class=dialButtons onclick=\"sendDTMF('"+ lineNum +"', '7')\"><div>7</div><span>PQRS</span></button></td>"
    html += "<td><button class=dialButtons onclick=\"sendDTMF('"+ lineNum +"', '8')\"><div>8</div><span>TUV</span></button></td>"
    html += "<td><button class=dialButtons onclick=\"sendDTMF('"+ lineNum +"', '9')\"><div>9</div><span>WXYZ</span></button></td></tr>";
    html += "<tr><td><button class=dialButtons onclick=\"sendDTMF('"+ lineNum +"', '*')\">*</button></td>"
    html += "<td><button class=dialButtons onclick=\"sendDTMF('"+ lineNum +"', '0')\">0</button></td>"
    html += "<td><button class=dialButtons onclick=\"sendDTMF('"+ lineNum +"', '#')\">#</button></td></tr>";
    html += "</table>";
    html += "</div>";
  
    var h = 400;
    var w = 240
  //   this.OpenWindow(html, lang.send_dtmf, h, w, false, false, lang.cancel, function(){
  //     this.CloseWindow()
  //   });
  }

  showPresentMenu(obj:any, lineNum:any){
    var items = [];
    items.push({value: "src-camera", icon : "fa fa-video-camera", text: this.lang.camera, isHeader: false }); // Camera
    items.push({value: "src-canvas", icon : "fa fa-pencil-square", text: this.lang.scratchpad, isHeader: false }); // Canvas
    items.push({value: "src-desktop", icon : "fa fa-desktop", text: this.lang.screen, isHeader: false }); // Screens
    items.push({value: "src-video", icon : "fa fa-file-video-o", text: this.lang.video, isHeader: false }); // Video
    items.push({value: "src-blank", icon : "fa fa-ban", text: this.lang.blank, isHeader: false }); // None
  
    var menu = {
        selectEvent : function( event, ui ) {
            var id = ui.item.attr("value");
            if(id != null) {
                if(id == "src-camera") this.PresentCamera(lineNum);
                if(id == "src-canvas") this.PresentScratchpad(lineNum);
                if(id == "src-desktop") this.PresentScreen(lineNum);
                if(id == "src-video") this.PresentVideo(lineNum);
                if(id == "src-blank") this.PresentBlank(lineNum);
                this.HidePopup();
            }
            else {
              this.HidePopup();
            }
        },
        createEvent : null,
        autoFocus : true,
        items : items
    }
    //this.PopupMenu(obj, menu);
  }
  
  showCallTimeline(lineNum:any){
    console.log("Show Timeline");
    //this.HidePopup();
    this.restoreCallControls(lineNum)
  
    if('$("#line-"+ lineNum +"-AudioStats").is(":visible")'){
        // The AudioStats is open, they can't take the same space
        this.hideCallStats(lineNum)
    }
  
  //   $("#line-"+ lineNum +"-AudioOrVideoCall").hide();
  //   $("#line-"+ lineNum +"-CallDetails").show();
    
  //   $("#line-"+ lineNum +"-btn-ShowTimeline").hide();
  //   $("#line-"+ lineNum +"-btn-HideTimeline").show();
  }

  hideCallTimeline(lineNum:any){
    console.log("Hide Timeline");
   // this.HidePopup();
  
  //   $("#line-"+ lineNum +"-CallDetails").hide();
  //   $("#line-"+ lineNum +"-AudioOrVideoCall").show();
  
  //   $("#line-"+ lineNum +"-btn-ShowTimeline").show();
  //   $("#line-"+ lineNum +"-btn-HideTimeline").hide();
  }
  
  showCallStats(lineNum:any){
    console.log("Show Call Stats");
    //this.HidePopup();
    this.restoreCallControls(lineNum)
  
    if('$("#line-"+ lineNum +"-CallDetails").is(":visible")'){
        // The Timeline is open, they can't take the same space
        this.hideCallTimeline(lineNum)
    }
  
  //   $("#line-"+ lineNum +"-AudioOrVideoCall").hide();
  //   $("#line-"+ lineNum +"-AudioStats").show();
  
  //   $("#line-"+ lineNum +"-btn-ShowCallStats").hide();
  //   $("#line-"+ lineNum +"-btn-HideCallStats").show();
  }
  hideCallStats(lineNum:any){
    console.log("Hide Call Stats");
  
    //this.HidePopup();
  //   $("#line-"+ lineNum +"-AudioOrVideoCall").show();
  //   $("#line-"+ lineNum +"-AudioStats").hide();
  
  //   $("#line-"+ lineNum +"-btn-ShowCallStats").show();
  //   $("#line-"+ lineNum +"-btn-HideCallStats").hide();
  }
  toggleMoreButtons(lineNum:any){
    if('$("#line-"+ lineNum +"-btn-more").is(":visible")'){
        // The more buttons are showing, drop them down
        this.restoreCallControls(lineNum);
    } else {
      this.expandCallControls(lineNum);
    }
  }
  expandCallControls(lineNum:any){
  //   $("#line-"+ lineNum +"-btn-more").show(200);
  //   $("#line-"+ lineNum +"-btn-ControlToggle").html('<i class=\"fa fa-chevron-down\"></i>');
  }
  restoreCallControls(lineNum:any){
  //   $("#line-"+ lineNum +"-btn-more").hide(200);
  //   $("#line-"+ lineNum +"-btn-ControlToggle").html('<i class=\"fa fa-chevron-up\"></i>');
  }
  
  
  // Transport Events
  // ================
  onTransportConnected(){
      console.log("Connected to Web Socket!");
      // $("#regStatus").html(this.lang.connected_to_web_socket);
  
      // $("#WebRtcFailed").hide();
  
      // Reset the ReconnectionAttempts
      this.userAgent.isReRegister = false;
      this.userAgent.transport.attemptingReconnection = false;
      this.userAgent.transport.ReconnectionAttempts = this.transportReconnectionAttempts;
  
      // Auto start register
      if(this.userAgent.transport.attemptingReconnection == false && this.userAgent.registering == false){
          window.setTimeout(()=>{
              this.register();
          }, 500);
      } else{
          console.warn("onTransportConnected: register() called, but attemptingReconnection is true or registering is true")
      }
  }
  
  onTransportConnectError(error:any){
      console.warn("WebSocket Connection Failed:", error);
  
      // We set this flag here so that the re-register attempts are fully completed.
      this.userAgent.isReRegister = false;
  
      // If there is an issue with the WS connection
      // We unregister, so that we register again once its up
      console.log("Unregister...");
      try{
          this.userAgent.registerer.unregister();
      } catch(e){
          // I know!!!
      }
  
      // $("#regStatus").html(this.lang.web_socket_error);
      // $("#WebRtcFailed").show();
  
      this.ReconnectTransport();
  
   }
  
  onTransportDisconnected(){
      console.log("Disconnected from Web Socket!");
      // $("#regStatus").html(this.lang.disconnected_from_web_socket);
  
      this.userAgent.isReRegister = false;
  }
  
  
  ReconnectTransport(){
      if(this.userAgent == null) return;
  
      this.userAgent.registering = false; // if the transport was down, you will not be registered
      if(this.userAgent.transport && this.userAgent.transport.isConnected()){
          // Asked to re-connect, but ws is connected
          this.onTransportConnected();
          return;
      }
      console.log("Reconnect Transport...");
  
      window.setTimeout(function(){
          // $("#regStatus").html(this.lang.connecting_to_web_socket);
          console.log("ReConnecting to WebSocket...");
  
          if(this.userAgent.transport && this.userAgent.transport.isConnected()){
              // Already Connected
              this.onTransportConnected();
              return;
          } else {
              this.userAgent.transport.attemptingReconnection = true
              this.userAgent.reconnect().catch(function(error){
                  this.userAgent.transport.attemptingReconnection = false
                  console.warn("Failed to reconnect", error);
  
                  // Try Again
                  this.ReconnectTransport();
              });
          }
      }, this.transportReconnectionTimeout * 1000);
  
      // $("#regStatus").html(this.lang.connecting_to_web_socket);
      console.log("Waiting to Re-connect...", this.transportReconnectionTimeout, "Attempt remaining", this.userAgent.transport.ReconnectionAttempts);
      this.userAgent.transport.ReconnectionAttempts = this.userAgent.transport.ReconnectionAttempts - 1;
  }
  
  // Registration
  // ============
  register() {
      if (this.userAgent == null) return;
      if (this.userAgent.registering == true) return;
      if (this.userAgent.isRegistered()) return;
  
      var RegistererRegisterOptions = {
          requestDelegate: {
              onReject: function(sip){
                  this.onRegisterFailed(sip.message.reasonPhrase, sip.message.statusCode);
                  
              }
          }
      }
  
      console.log("Sending Registration...");
      // $("#regStatus").html(this.lang.sending_registration);
      this.userAgent.registering = true
      this.userAgent.registerer.register(RegistererRegisterOptions);
  }
  
  unregister(skipUnsubscribe:any) {
      if (this.userAgent == null || !this.userAgent.isRegistered()) return;
  
      if(skipUnsubscribe == true){
          console.log("Skipping Unsubscribe");
      } else {
          console.log("Unsubscribing...");
          // $("#regStatus").html(this.lang.unsubscribing);
          try {
             //this.UnsubscribeAll();
          } catch (e) { }
      }
  
      console.log("Unregister...");
      // $("#regStatus").html(this.lang.disconnecting);
      this.userAgent.registerer.unregister();
  
      this.userAgent.transport.attemptingReconnection = false;
      this.userAgent.registering = false;
      this.userAgent.isReRegister = false;
  }
  
  // Registration Events
  // ===================
  /**
   * Called when account is registered
   */
  onRegistered(){
      // This code fires on re-register after session timeout
      // to ensure that events are not fired multiple times
      // a isReRegister state is kept.
      // TODO: This check appears obsolete
  
      this.userAgent.registrationCompleted = true;
      if(!this.userAgent.isReRegister) {
          console.log("Registered!");
  
          // $("#reglink").hide();
          // $("#dereglink").show();
          if(this.doNotDisturbEnabled || this.doNotDisturbPolicy == "enabled") {
              // $("#dereglink").attr("class", "dotDoNotDisturb");
              // $("#dndStatus").html("(DND)");
          }
  
          // Start Subscribe Loop
          window.setTimeout(()=>{
              this.subscribeAll();
          }, 500);
  
          // Output to status
          // $("#regStatus").html(this.lang.registered);
  
          // Start XMPP
          if(this.chatEngine == "XMPP") this.reconnectXmpp();
  
          this.userAgent.registering = false;
  
          // // Close possible Alerts that may be open. (Can be from failed registers)
          // if (alertObj != null) {
          //     alertObj.dialog("close");
          //     alertObj = null;
          // }
  
      }
      else {
          this.userAgent.registering = false;
  
          console.log("ReRegistered!");
      }
      this.userAgent.isReRegister = true;
  }
  
  
  /**
   * Called if this.userAgent can connect, but not register.
   * @param {string} response Incoming request message
   * @param {string} cause Cause message. Unused
  **/
  onRegisterFailed(response:any, cause:any){
      console.log("Registration Failed: " + response);
      // $("#regStatus").html(this.lang.registration_failed);
  
      // $("#reglink").show();
      // $("#dereglink").hide();
  
      //alert(this.lang.registration_failed +":"+ response, this.lang.registration_failed);
  
      this.userAgent.registering = false;
  
  }
  /**
   * Called when Unregister is requested
   */
  onUnregistered(){
      if(this.userAgent.registrationCompleted){
          console.log("Unregistered, bye!");
          // $("#regStatus").html(this.lang.unregistered);
  
          // $("#reglink").show();
          // $("#dereglink").hide();
  
      }
      else {
          // Was never really registered, so cant really say unregistered
      }
  
      // We set this flag here so that the re-register attempts are fully completed.
      this.userAgent.isReRegister = false;
  }
  
  
  // Presence / Subscribe
  // ====================
  subscribeAll() {
    if(!this.userAgent.isRegistered()) return;
  
    if(this.voiceMailSubscribe){
      this.subscribeVoicemail();
    }
    if(this.subscribeToYourself){
      this.selfSubscribe();
    }
  
    // Start subscribe all
    if(this.userAgent.BlfSubs && this.userAgent.BlfSubs.length > 0){
      //this.UnsubscribeAll();
    }
    this.userAgent.BlfSubs = [];
    // if(this.Buddies.length >= 1){
    //     console.log("Starting Subscribe of all ("+ this.Buddies.length +") Extension Buddies...");
    //     for(var b=0; b<Buddies.length; b++) {
    //       this.SubscribeBuddy(Buddies[b]);
    //     }
    // }
  }
  selfSubscribe(){
    if(!this.userAgent.isRegistered()) return;
  
    if(this.userAgent.selfSub){
        console.log("Unsubscribe from old self subscribe...");
        this.selfUnsubscribe();
    }
  
    var targetURI = UserAgent.makeURI("sip:" + this.sipUsername + "@" + this.sipDomain);
  
    var options = { 
        expires: this.subscribeBuddyExpires, 
        extraHeaders: ['Accept: '+ this.subscribeBuddyAccept]
    }
  
    this.userAgent.selfSub = new Subscriber(this.userAgent, targetURI, this.subscribeBuddyEvent, options);
    this.userAgent.selfSub.delegate = {
        onNotify: function(sip) {
          this.receiveNotify(sip, true);
        }
    }
    console.log("SUBSCRIBE Self: "+ this.sipUsername +"@" + this.sipDomain);
    this.userAgent.selfSub.subscribe().catch(function(error){
        console.warn("Error subscribing to yourself:", error);
    });
  }
  
  subscribeVoicemail(){
    if(!this.userAgent.isRegistered()) return;
  
    if(this.userAgent.voicemailSub){
        console.log("Unsubscribe from old voicemail Messages...");
        this.unsubscribeVoicemail();
    }
  
    var vmOptions = { expires : this.subscribeVoicemailExpires }
    var targetURI = UserAgent.makeURI("sip:" + this.sipUsername + "@" + this.sipDomain);
    this.userAgent.voicemailSub = new Subscriber(this.userAgent, targetURI, "message-summary", vmOptions);
    this.userAgent.voicemailSub.delegate = {
        onNotify: function(sip) {
          this.voicemailNotify(sip);
        }
    }
    console.log("SUBSCRIBE VOICEMAIL: "+ this.sipUsername +"@" + this.sipDomain);
    this.userAgent.voicemailSub.subscribe().catch(function(error){
        console.warn("Error subscribing to voicemail notifications:", error);
    });
  }
  
  selfUnsubscribe(){
    if(!this.userAgent.isRegistered()) return;
  
    if(this.userAgent.selfSub){
        console.log("Unsubscribe from yourself...", this.userAgent.selfSub.state);
        if(this.userAgent.selfSub.state == SubscriptionState.Subscribed){
          this.userAgent.selfSub.unsubscribe().catch(function(error){
                console.warn("Error self subscription:", error);
            });
        }
        this.userAgent.selfSub.dispose().catch(function(error){
            console.warn("Error disposing self subscription:", error);
        });
    } else {
        console.log("Not subscribed to Yourself");
    }
    this.userAgent.selfSub = null;
  }
  
  
  // Subscription Events
  // ===================
  voicemailNotify(notification:any){
    // Messages-Waiting: yes        <-- yes/no
    // Voice-Message: 1/0           <-- new/old
    // Voice-Message: 1/0 (0/0)     <-- new/old (ugent new/old)
    if(notification.request.body.indexOf("Messages-Waiting:") > -1){
        notification.accept();
  
        var messagesWaiting = (notification.request.body.indexOf("Messages-Waiting: yes") > -1)
        var newVoiceMessages = 0;
        var oldVoiceMessages = 0;
        var ugentNewVoiceMessage = 0;
        var ugentOldVoiceMessage = 0;
  
        if(messagesWaiting){
            console.log("Messages Waiting!");
            var lines = notification.request.body.split("\r\n");
            for(var l=0; l<lines.length; l++){
                if(lines[l].indexOf("Voice-Message: ") > -1){
                    var value = lines[l].replace("Voice-Message: ", ""); // 1/0 (0/0)
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
            console.log("Voicemail: ", newVoiceMessages, oldVoiceMessages, ugentNewVoiceMessage, ugentOldVoiceMessage);
  
            // Show the messages waiting bubble
          //   $("#TxtVoiceMessages").html(""+ newVoiceMessages)
          //   $("#TxtVoiceMessages").show();
  
            // Show a system notification
            if(newVoiceMessages > this.userAgent.lastVoicemailCount){
              this.userAgent.lastVoicemailCount = newVoiceMessages;
  
                if ("Notification" in window) {
                    if (Notification.permission === "granted") {
  
                        var noticeOptions = { 
                            body: this.lang.you_have_new_voice_mail.replace("{0}", newVoiceMessages)
                        }
  
                        var vmNotification = new Notification(this.lang.new_voice_mail, noticeOptions);
                        vmNotification.onclick = (event)=> {
                            // if(this.voicemailDid != ""){
                            //   this.DialByLine("audio", null, this.voicemailDid, this.lang.voice_mail);
                            // }
                        }
                    }
                }
  
            }
  
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
  receiveNotify(notification:any, selfSubscribe:any) {
    if (this.userAgent == null || !this.userAgent.isRegistered()) return;
  
    notification.accept();
  
    var buddy = "";
    var dotClass = "dotOffline";
    var Presence = "Unknown";
  
    var ContentType = notification.request.headers["Content-Type"][0].parsed;
    if (ContentType == "application/pidf+xml") {
        // Handle Presence
        /*
        // Asterisk chan_sip
        <?xml version="1.0" encoding="ISO-8859-1"?>
        <presence
            xmlns="urn:ietf:params:xml:ns:pidf" 
            xmlns:pp="urn:ietf:params:xml:ns:pidf:person" 
            xmlns:es="urn:ietf:params:xml:ns:pidf:rid:status:rid-status"
            xmlns:ep="urn:ietf:params:xml:ns:pidf:rid:rid-person"
            entity="sip:webrtc@192.168.88.98">
  
            <pp:person>
                <status>
                    <ep:activities>
                        <ep:away/>
                    </ep:activities>
                </status>
            </pp:person>
  
            <note>Not online</note>
            <tuple id="300">
                <contact priority="1">sip:300@192.168.88.98</contact>
                <status>
                    <basic>open | closed</basic>
                </status>
            </tuple>
        </presence>
  
        // Asterisk chan_pj-sip
        <?xml version="1.0" encoding="UTF-8"?>
        <presence 
            entity="sip:300@192.168.88.40:443;transport=ws" 
            xmlns="urn:ietf:params:xml:ns:pidf" 
            xmlns:dm="urn:ietf:params:xml:ns:pidf:data-model" 
            xmlns:rid="urn:ietf:params:xml:ns:pidf:rid">
            <note>Ready</note>
            <tuple id="300">
                <status>
                    <basic>open</basic>
                </status>
                <contact priority="1">sip:User1@raspberrypi.local</contact>
            </tuple>
            <dm:person />
        </presence>
  
        // OpenSIPS 
        <?xml version="1.0"?>
        <presence 
            xmlns="urn:ietf:params:xml:ns:pidf" 
            entity="sip:200@ws-eu-west-1.innovateasterisk.com">
            <tuple xmlns="urn:ietf:params:xml:ns:pidf" id="tuple_mixing-id">
                <status>
                    <basic>closed</basic>
                </status>
            </tuple>
        </presence>
  
        <?xml version="1.0"?>
        <presence 
            xmlns="urn:ietf:params:xml:ns:pidf" 
            entity="sip:TTbXG7XMO@ws-eu-west-1.innovateasterisk.com">
            <tuple 
                xmlns="urn:ietf:params:xml:ns:pidf" 
                id="0x7ffe17f496c0">
                <status>
                    <basic>open</basic>
                </status>
            </tuple>
        </presence>
  
  
        <?xml version="1.0"?>
        <presence 
            xmlns="urn:ietf:params:xml:ns:pidf" 
            entity="sip:TTbXG7XMO@ws-eu-west-1.innovateasterisk.com">
            <tuple 
                xmlns="urn:ietf:params:xml:ns:pidf" 
                id="tuple_mixing-id">
                <status>
                    <basic>open</basic>
                </status>
            </tuple>
            <note xmlns="urn:ietf:params:xml:ns:pidf">On the phone</note>
            <dm:person 
                xmlns:dm="urn:ietf:params:xml:ns:pidf:data-model" 
                xmlns:rid="urn:ietf:params:xml:ns:pidf:rid" 
                id="peers_mixing-id">
                <rid:activities>
                    <rid:on-the-phone/>
                </rid:activities>
                <dm:note>On the phone</dm:note>
            </dm:person>
        </presence>
  
        // There can be more than one tuple
        <?xml version="1.0"?>
        <presence 
            xmlns="urn:ietf:params:xml:ns:pidf" 
            entity="sip:TTbXG7XMO@ws-eu-west-1.innovateasterisk.com">
            <tuple 
                xmlns="urn:ietf:params:xml:ns:pidf" 
                id="0x7ffce2b4b1a0">
                <status>
                    <basic>open</basic>
                </status>
            </tuple>
            <tuple 
                xmlns="urn:ietf:params:xml:ns:pidf"
                id="0x7ffd6abd4a40">
                <status>
                    <basic>open</basic>
                </status>
            </tuple>
        </presence>
  "
  
  
  open: In the context of INSTANT MESSAGES, this value means that the
    associated <contact> element, if any, corresponds to an INSTANT
    INBOX that is ready to accept an INSTANT MESSAGE.
  
  closed: In the context of INSTANT MESSAGES, this value means that
    the associated <contact> element, if any, corresponds to an
    INSTANT INBOX that is unable to accept an INSTANT MESSAGE.
  
        */
  
        var xml:any = '$($.parseXML(notification.request.body))';
  
        // The value of the 'entity' attribute is the 'pres' URL of the PRESENT publishing this presence document.
        // (In some cases this can present as the user... what if using DIDs)
        var ObservedUser = xml.find("presence").attr("entity");
        buddy = ObservedUser.split("@")[0].split(":")[1];
        // buddy = xml.find("presence").find("tuple").attr("id"); // Asterisk does this, but its not correct.
        // buddy = notification.request.from.uri.user; // Unreliable 
  
        var availability = "closed"
        // availability = xml.find("presence").find("tuple").find("status").find("basic").text();
        var tuples = xml.find("presence").find("tuple");
        if(tuples){
          //   $.each(tuples, function(i, obj){
          //       // So if any of the contacts are open, then say open
          //       if($(obj).find("status").find("basic").text() == "open") {
          //           availability = "open";
          //       }
          //   });
        }
  
        Presence = xml.find("presence").find("note").text(); 
        if(Presence == ""){
            if (availability == "open") Presence = "Ready";
            if (availability == "closed") Presence = "Not online";
        }
    }
    else if (ContentType == "application/dialog-info+xml") {
        // Handle "Dialog" State
  
        var xml:any = '$($.parseXML(notification.request.body))';
  
        /*
        Asterisk:
        <?xml version="1.0"?>
        <dialog-info 
            xmlns="urn:ietf:params:xml:ns:dialog-info" 
            version="0-99999" 
            state="full|partial" 
            entity="sip:xxxx@XXX.XX.XX.XX">
            <dialog id="xxxx">
                <state>trying | proceeding | early | terminated | confirmed</state>
            </dialog>
        </dialog-info>
  
        OpenSIPS:
        <?xml version="1.0"?>
        <dialog-info 
            xmlns="urn:ietf:params:xml:ns:dialog-info" 
            version="18" 
            state="full" 
            entity="sip:TTbXG7XMO@ws-eu-west-1.innovateasterisk.com"
        />
  
        <?xml version="1.0"?>
        <dialog-info 
            xmlns="urn:ietf:params:xml:ns:dialog-info" 
            version="17" 
            entity="sip:TTbXG7XMO@ws-eu-west-1.innovateasterisk.com" 
            state="partial">
            <dialog 
                id="soe2vr886cbn1ccj3h.0" 
    *           local-tag="ceq735vrh" 
    *           remote-tag="a1d22259-28ea-434f-9680-b925218b7418" 
                direction="initiator">
                <state>terminated</state>
    *           <remote>
                    <identity display="Bob">sip:*65@ws-eu-west-1.innovateasterisk.com</identity>
                    <target uri="sip:*65@ws-eu-west-1.innovateasterisk.com"/>
    *           </remote>
    *           <local>
                    <identity display="Conrad De Wet">sip:TTbXG7XMO@ws-eu-west-1.innovateasterisk.com</identity>
                    <target uri="sip:TTbXG7XMO@ws-eu-west-1.innovateasterisk.com"/>
                </local>
            </dialog>
        </dialog-info>
        */
  
        var ObservedUser = xml.find("dialog-info").attr("entity");
        buddy = ObservedUser.split("@")[0].split(":")[1];
  
        var version = xml.find("dialog-info").attr("version"); // 1|2|etc
        var DialogState = xml.find("dialog-info").attr("state"); // full|partial
        var extId = xml.find("dialog-info").find("dialog").attr("id"); // qoe2vr886cbn1ccj3h.0
  
        var state = xml.find("dialog-info").find("dialog").find("state").text();
        if (state == "terminated") Presence = "Ready";
        if (state == "trying") Presence = "On the phone";
        if (state == "proceeding") Presence = "On the phone";
        if (state == "early") Presence = "Ringing";
        if (state == "confirmed") Presence = "On the phone";
  
        // The dialog states only report devices states, and cant say online or offline.
    }
  
    if(selfSubscribe){
        if(buddy == this.sipUsername){
            console.log("Self Notify:", Presence);
  
        }
        else {
            console.warn("Self Subscribe Notify, but wrong user returned.", buddy, this.sipUsername);
        }
        return;
    }
  
    // dotOnline | dotOffline | dotRinging | dotInUse | dotReady | dotOnHold
    if (Presence == "Not online") dotClass = "dotOffline";
    if (Presence == "Unavailable") dotClass = "dotOffline";
    if (Presence == "Ready") dotClass = "dotOnline";
    if (Presence == "On the phone") dotClass = "dotInUse";
    if (Presence == "Proceeding") dotClass = "dotInUse";
    if (Presence == "Ringing") dotClass = "dotRinging";
    if (Presence == "On hold") dotClass = "dotOnHold";
  }
  
  
  unsubscribeBlf(blfSubscribe:any){
    if(!this.userAgent.isRegistered()) return;
  
    if(blfSubscribe.state == SubscriptionState.Subscribed){
        console.log("Unsubscribe to BLF Messages...", blfSubscribe.data.buddyId);
        blfSubscribe.unsubscribe().catch(function(error){
            console.warn("Error removing BLF notifications:", error);
        });
    } 
    else {
        console.log("Incorrect buddy subscribe state", blfSubscribe.data.buddyId, blfSubscribe.state);
    }
    blfSubscribe.dispose().catch(function(error){
        console.warn("Error disposing BLF notifications:", error);
    });
    blfSubscribe = null;
  }
  
  unsubscribeVoicemail(){
    if(!this.userAgent.isRegistered()) return;
  
    if(this.userAgent.voicemailSub){
        console.log("Unsubscribe to voicemail Messages...", this.userAgent.voicemailSub.state);
        if(this.userAgent.voicemailSub.state == SubscriptionState.Subscribed){
          this.userAgent.voicemailSub.unsubscribe().catch(function(error){
                console.warn("Error removing voicemail notifications:", error);
            });
        }
        this.userAgent.voicemailSub.dispose().catch(function(error){
            console.warn("Error disposing voicemail notifications:", error);
        });
    } else {
        console.log("Not subscribed to MWI");
    }
    this.userAgent.voicemailSub = null;
  }
  
  
  // Buddy: Chat / Instant Message / XMPP
  // ====================================
  initialiseStream(buddy:any){
      var template = { TotalRows:0, DataCollection:[] }
      this.localDB.setItem(buddy + "-stream", JSON.stringify(template));
      return JSON.parse(this.localDB.getItem(buddy + "-stream"));
  }
  sendChatMessage(buddy:any) {
      if (this.userAgent == null) return;
      if (!this.userAgent.isRegistered()) return;
  
      // $("#contact-" + buddy + "-ChatMessage").focus(); // refocus on the textarea
  
      var message = String('$("#contact-" + buddy + "-ChatMessage").val()');
     // message = $.trim(message);
      if(message == "") {
          this.alert(this.lang.alert_empty_text_message, this.lang.no_message,"");
          return;
      }
      // Note: AMI has this limit, but only if you use AMI to transmit
      // if(message.length > 755){
      //     alert("Asterisk has a limit on the message size (755). This message is too long, and cannot be delivered.", "Message Too Long");
      //     return;
      // }
  
      var messageId = this.uID();
      // var buddyObj = this.FindBuddyByIdentity(buddy);
      var buddyObj :any = "";
  
      // Update Stream
      var DateTime = moment.utc().format("YYYY-MM-DD HH:mm:ss UTC");
      var currentStream = JSON.parse(this.localDB.getItem(buddy + "-stream"));
      if(currentStream == null) currentStream = this.initialiseStream(buddy);
  
      // Add New Message
      var newMessageJson = {
          ItemId: messageId,
          ItemType: "MSG",
          ItemDate: DateTime,
          SrcUserId: this.profileUserID,
          Src: "\""+ this.profileName +"\"",
          DstUserId: buddyObj.identity,
          Dst: "",
          MessageData: message
      }
  
      currentStream.DataCollection.push(newMessageJson);
      currentStream.TotalRows = currentStream.DataCollection.length;
      this.localDB.setItem(buddy + "-stream", JSON.stringify(currentStream));
  
      // SIP Messages (Note, this may not work as required)
      // ============
      if(buddyObj.type == "extension") {
          var chatBuddy = UserAgent.makeURI("sip:"+ buddyObj.ExtNo.replace(/#/g, "%23") + "@" + this.sipDomain);
          console.log("MESSAGE: "+ chatBuddy + " (extension)");
  
  
          var MessagerMessageOptions = {
              requestDelegate : {
                  onAccept: function(sip){
                      console.log("Message Accepted:", messageId);
                      this.markMessageSent(buddyObj, messageId, true);
                  },
                  onReject: function(sip){
                      console.warn("Message Error", sip.message.reasonPhrase);
                      this.markMessageNotSent(buddyObj, messageId, true);
                  }
              },
              requestOptions : {
                  extraHeaders: [],
              }
          }
          var messageObj = new Messager(this.userAgent, chatBuddy, message, "text/plain");
      }
  
      // XMPP Messages
      // =============
      if(buddyObj.type == "xmpp"){
          console.log("MESSAGE: "+ buddyObj.jid + " (xmpp)");
          this.XmppSendMessage(buddyObj, message, messageId,"","","");
      }
  
      // Group Chat
      // ==========
      if(buddyObj.type == "group"){
          // TODO
      }
  
      // // Post Add Activity
      // $("#contact-" + buddy + "-ChatMessage").val("");
      // $("#contact-" + buddy + "-dictate-message").hide();
      // $("#contact-" + buddy + "-emoji-menu").hide();
      // $("#contact-" + buddy + "-ChatMessage").focus();
  
      if(buddyObj.recognition != null){
          buddyObj.recognition.abort();
          buddyObj.recognition = null;
      }
  
      //this.UpdateBuddyActivity(buddy);
      this.refreshStream(buddyObj,"");
  }
  markMessageSent(buddyObj:any, messageId:any, refresh:any){
      var currentStream = JSON.parse(this.localDB.getItem(buddyObj.identity + "-stream"));
      if(currentStream != null || currentStream.DataCollection != null){
          // $.each(currentStream.DataCollection, function (i, item) {
          //     if (item.ItemType == "MSG" && item.ItemId == messageId) {
          //         // Found
          //         item.Sent = true;
          //         return false;
          //     }
          // });
          this.localDB.setItem(buddyObj.identity + "-stream", JSON.stringify(currentStream));
  
          if(refresh) this.refreshStream(buddyObj,"");
      }
  }
  markMessageNotSent(buddyObj:any, messageId:any, refresh:any){
      var currentStream = JSON.parse(this.localDB.getItem(buddyObj.identity + "-stream"));
      if(currentStream != null || currentStream.DataCollection != null){
          // $.each(currentStream.DataCollection, function (i, item) {
          //     if (item.ItemType == "MSG" && item.ItemId == messageId) {
          //         // Found
          //         item.Sent = false;
          //         return false;
          //     }
          // });
          this.localDB.setItem(buddyObj.identity + "-stream", JSON.stringify(currentStream));
  
          if(refresh) this.refreshStream(buddyObj,"");
      }
  }
  markDeliveryReceipt(buddyObj:any, messageId:any, refresh:any){
      var currentStream = JSON.parse(this.localDB.getItem(buddyObj.identity + "-stream"));
      if(currentStream != null || currentStream.DataCollection != null){
          // $.each(currentStream.DataCollection, function (i, item) {
          //     if (item.ItemType == "MSG" && item.ItemId == messageId) {
          //         // Found
          //         item.Delivered = { state : true, eventTime: this.utcDateNow()};
          //         return false;
          //     }
          // });
          this.localDB.setItem(buddyObj.identity + "-stream", JSON.stringify(currentStream));
  
          if(refresh) this.refreshStream(buddyObj,"");
      }
  }
  markDisplayReceipt(buddyObj:any, messageId:any, refresh:any){
      var currentStream = JSON.parse(this.localDB.getItem(buddyObj.identity + "-stream"));
      if(currentStream != null || currentStream.DataCollection != null){
          // $.each(currentStream.DataCollection, function (i, item) {
          //     if (item.ItemType == "MSG" && item.ItemId == messageId) {
          //         // Found
          //         item.Displayed = { state : true, eventTime: this.utcDateNow()};
          //         return false;
          //     }
          // });
          this.localDB.setItem(buddyObj.identity + "-stream", JSON.stringify(currentStream));
  
          if(refresh) this.refreshStream(buddyObj,"");
      }
  }
  markMessageRead(buddyObj:any, messageId:any){
      var currentStream = JSON.parse(this.localDB.getItem(buddyObj.identity + "-stream"));
      if(currentStream != null || currentStream.DataCollection != null){
          // $.each(currentStream.DataCollection, function (i, item) {
          //     if (item.ItemType == "MSG" && item.ItemId == messageId) {
          //         // Found
          //         item.Read = { state : true, eventTime: this.utcDateNow()};
          //         // return false; /// Mark all messages matching that id to avoid 
          //         // duplicate id issue
          //     }
          // });
          this.localDB.setItem(buddyObj.identity + "-stream", JSON.stringify(currentStream));
          console.log("Set message ("+ messageId +") as Read");
      }
  }
  
  
  addMessageToStream(buddyObj:any, messageId:any, type:any, message:any, DateTime:any){
      var currentStream = JSON.parse(this.localDB.getItem(buddyObj.identity + "-stream"));
      if(currentStream == null) currentStream = this.initialiseStream(buddyObj.identity);
  
      // Add New Message
      var newMessageJson = {
          ItemId: messageId,
          ItemType: type,
          ItemDate: DateTime,
          SrcUserId: buddyObj.identity,
          Src: "\""+ buddyObj.CallerIDName +"\"",
          DstUserId: this.profileUserID,
          Dst: "",
          MessageData: message
      }
  
      currentStream.DataCollection.push(newMessageJson);
      currentStream.TotalRows = currentStream.DataCollection.length;
      this.localDB.setItem(buddyObj.identity + "-stream", JSON.stringify(currentStream));
  
      // Data Cleanup
      if(this.maxDataStoreDays && this.maxDataStoreDays > 0){
          console.log("Cleaning up data: ", this.maxDataStoreDays);
          //this.removeBuddyMessageStream(this.FindBuddyByIdentity("buddy"), this.maxDataStoreDays);
      }
  }
  
  removeBuddyMessageStream(buddyObj:any, days:any){
      // use days to specify how many days back must the records be cleared
      // eg: 30, will only remove records older than 30 day from now
      // and leave the buddy in place.
      // Must be greater then 0 or the entire buddy will be removed.
      if(buddyObj == null) return;
  
      // Grab a copy of the stream
      var stream = JSON.parse(this.localDB.getItem(buddyObj.identity + "-stream"));
      if(days && days > 0){
          if(stream && stream.DataCollection && stream.DataCollection.length >= 1){
  
              // Create Trim Stream 
              var trimmedStream = {
                  TotalRows : 0,
                  DataCollection : []
              }
              trimmedStream.DataCollection = stream.DataCollection.filter(function(item){
                  // Apply Date Filter
                  var itemDate = moment.utc(item.ItemDate.replace(" UTC", ""));
                  var expiredDate = moment.utc().subtract(days, 'days');
                  // Condition
                  if(itemDate.isSameOrAfter(expiredDate, "second")){
                      return true // return true to include;
                  }
                  else {
                      return false; // return false to exclude;
                  }
              });
              trimmedStream.TotalRows = trimmedStream.DataCollection.length;
              this.localDB.setItem(buddyObj.identity + "-stream", JSON.stringify(trimmedStream));
  
              // Create Delete Stream
              var deleteStream = {
                  TotalRows : 0,
                  DataCollection : []
              }
              deleteStream.DataCollection = stream.DataCollection.filter(function(item){
                  // Apply Date Filter
                  var itemDate = moment.utc(item.ItemDate.replace(" UTC", ""));
                  var expiredDate = moment.utc().subtract(days, 'days');
                  // Condition
                  if(itemDate.isSameOrAfter(expiredDate, "second")){
                      return false; // return false to exclude;
                  }
                  else {
                      return true // return true to include;
                  }
              });
              deleteStream.TotalRows = deleteStream.DataCollection.length;
  
              // Re-assign stream so that the normal delete action can apply
              stream = deleteStream;
  
              this.refreshStream(buddyObj,"");
          }
      }
      else {
          this.closeBuddy(buddyObj.identity);
  
          // Remove From UI
          // $("#stream-"+ buddyObj.identity).remove();
  
          // Remove Stream (CDRs & Messages etc)
          this.localDB.removeItem(buddyObj.identity + "-stream");
  
          // Remove Buddy
          var json = JSON.parse(this.localDB.getItem(this.profileUserID + "-Buddies"));
          var x = 0;
          // $.each(json.DataCollection, function (i:number, item) {
          //     if(item.uID == buddyObj.identity || item.cID == buddyObj.identity || item.gID == buddyObj.identity){
          //         x = i;
          //         return false;
          //     }
          // });
          json.DataCollection.splice(x,1);
          json.TotalRows = json.DataCollection.length;
          this.localDB.setItem(this.profileUserID + "-Buddies", JSON.stringify(json));
  
          // Remove Images
          this.localDB.removeItem("img-"+ buddyObj.identity +"-extension");
          this.localDB.removeItem("img-"+ buddyObj.identity +"-contact");
          this.localDB.removeItem("img-"+ buddyObj.identity +"-group");
      }
      //this.UpdateBuddyList();
  
      // Remove Call Recordings
      if(stream && stream.DataCollection && stream.DataCollection.length >= 1){
          //this.DeleteCallRecordings(buddyObj.identity, stream);
      }
      
      // Remove QOS Data
      if(stream && stream.DataCollection && stream.DataCollection.length >= 1){
          //this.DeleteQosData(buddyObj.identity, stream);
      }
  }
  
  closeBuddy(buddy){
      // lines and Buddies (Left)
      // $(".buddySelected").each(function () {
      //     $(this).prop('class', 'buddy');
      // });
      // Streams (Right)
      // $(".streamSelected").each(function () {
      //     $(this).prop('class', 'stream');
      // });
      // Save Selected
      this.localDB.setItem("SelectedBuddy", null);
  
      // Change to Stream if in Narrow view
  }
  
  activateStream(buddyObj:any, message:any){
      // Handle Stream Not visible
      // =========================
      var streamVisible :any= '$("#stream-"+ buddyObj.identity).is(":visible")';
      if (!streamVisible) {
          // Add or Increase the Badge
          this.increaseMissedBadge(buddyObj.identity);
          if ("Notification" in window) {
              if (Notification.permission === "granted") {
                  //var imageUrl = this.getPicture(buddyObj.identity);
                  var noticeOptions = { body: message.substring(0, 250), icon: "imageUrl" }
                  var inComingChatNotification = new Notification(this.lang.message_from + " : " + buddyObj.CallerIDName, noticeOptions);
                  inComingChatNotification.onclick = function (event) {
                      // Show Message
                      //this.SelectBuddy(buddyObj.identity);
                  }
              }
          }
          // Play Alert
          console.log("Audio:", this.audioBlobs.Alert.url);
          var ringer = new Audio(this.audioBlobs.Alert.blob);
          ringer.preload = "auto";
          ringer.loop = false;
          ringer.oncanplaythrough = function(e) {
              // if (typeof ringer.sinkId !== 'undefined' && this.getRingerOutputID() != "default") {
              //     ringer.setSinkId(this.getRingerOutputID()).then(function() {
              //         console.log("Set sinkId to:", this.getRingerOutputID());
              //     }).catch(function(e){
              //         console.warn("Failed not apply setSinkId.", e);
              //     });
              // }
              // If there has been no interaction with the page at all... this page will not work
              ringer.play().then(function(){
                  // Audio Is Playing
              }).catch(function(e){
                  console.warn("Unable to play audio file.", e);
              });
          }
          // message.data.ringerObj = ringer;
      } else {
          // Message window is active.
      }
  }
  
  addCallMessage(buddy:any, session:any) {
  
      var currentStream = JSON.parse(this.localDB.getItem(buddy + "-stream"));
      if(currentStream == null) currentStream = this.initialiseStream(buddy);
  
      var CallEnd = moment.utc(); // Take Now as the Hangup Time
      var callDuration :any;
      var totalDuration :any;
      var ringTime :any;
  
      var CallStart = moment.utc(session.data.callstart.replace(" UTC", "")); // Actual start (both inbound and outbound)
      var CallAnswer = null; // On Accept when inbound, Remote Side when Outbound
      if(session.data.startTime){
          // The time when WE answered the call (May be null - no answer)
          // or
          // The time when THEY answered the call (May be null - no answer)
          CallAnswer = moment.utc(session.data.startTime);  // Local Time gets converted to UTC 
  
          this.callDuration = moment.duration(CallEnd.diff(CallAnswer));
          this.ringTime = moment.duration(CallAnswer.diff(CallStart));
      } 
      else {
          // There was no start time, but on inbound/outbound calls, this would indicate the ring time
          this.ringTime = moment.duration(CallEnd.diff(CallStart));
      }
      this.totalDuration = moment.duration(CallEnd.diff(CallStart));
  
      var srcId = "";
      var srcCallerID = "";
      var dstId = ""
      var dstCallerID = "";
      if(session.data.calldirection == "inbound") {
          srcId = buddy;
          dstId = this.profileUserID;
          srcCallerID = session.remoteIdentity.displayName;
          dstCallerID = this.profileName;
      } else if(session.data.calldirection == "outbound") {
          srcId = this.profileUserID;
          dstId = buddy;
          srcCallerID = this.profileName;
          dstCallerID = session.data.dst;
      }
  
      var callDirection = session.data.calldirection;
      var withVideo = session.data.withvideo;
      var sessionId = session.id;
      var hangupBy = session.data.terminateby;
  
      var newMessageJson = {
          CdrId: this.uID(),
          ItemType: "CDR",
          ItemDate: CallStart.format("YYYY-MM-DD HH:mm:ss UTC"),
          CallAnswer: (CallAnswer)? CallAnswer.format("YYYY-MM-DD HH:mm:ss UTC") : null,
          CallEnd: CallEnd.format("YYYY-MM-DD HH:mm:ss UTC"),
          SrcUserId: srcId,
          Src: srcCallerID,
          DstUserId: dstId,
          Dst: dstCallerID,
          RingTime: (ringTime != 0)? ringTime.asSeconds() : 0,
          Billsec: (callDuration != 0)? callDuration.asSeconds() : 0,
          TotalDuration: (totalDuration != 0)? totalDuration.asSeconds() : 0,
          ReasonCode: session.data.reasonCode,
          ReasonText: session.data.reasonText,
          WithVideo: withVideo,
          SessionId: sessionId,
          CallDirection: callDirection,
          Terminate: hangupBy,
          // CRM
          MessageData: null,
          Tags: [],
          //Reporting
          Transfers: (session.data.transfer)? session.data.transfer : [],
          Mutes: (session.data.mute)? session.data.mute : [],
          Holds: (session.data.hold)? session.data.hold : [],
          Recordings: (session.data.recordings)? session.data.recordings : [],
          ConfCalls: (session.data.confcalls)? session.data.confcalls : [],
          ConfbridgeEvents: (session.data.ConfbridgeEvents)? session.data.ConfbridgeEvents : [],
          QOS: []
      }
  
      console.log("New CDR", newMessageJson);
  
      currentStream.DataCollection.push(newMessageJson);
      currentStream.TotalRows = currentStream.DataCollection.length;
      this.localDB.setItem(buddy + "-stream", JSON.stringify(currentStream));
  
     // UpdateBuddyActivity(buddy);
  
      // Data Cleanup
      if(this.maxDataStoreDays && this.maxDataStoreDays > 0){
          console.log("Cleaning up data: ", this.maxDataStoreDays);
          //this.removeBuddyMessageStream(this.FindBuddyByIdentity(buddy), this.maxDataStoreDays);
      }
  
  }
  
  
  searchStream(obj:any, buddy:any){
      var q = obj.value;
  
      var buddyObj = "this.FindBuddyByIdentity(buddy)";
     // var buddyObj = this.FindBuddyByIdentity(buddy);
      if(q == ""){
          console.log("Restore Stream");
          this.refreshStream(buddyObj,"");
      }
      else{
          this.refreshStream(buddyObj, q);
      }
  }
  
  refreshStream(buddyObj:any, filter:any) {
      // $("#contact-" + buddyObj.identity + "-ChatHistory").empty();
  
      var json = JSON.parse(this.localDB.getItem(buddyObj.identity +"-stream"));
      if(json == null || json.DataCollection == null) return;
  
      // Sort DataCollection (Newest items first)
      json.DataCollection.sort(function(a, b){
          var aMo = moment.utc(a.ItemDate.replace(" UTC", ""));
          var bMo = moment.utc(b.ItemDate.replace(" UTC", ""));
          if (aMo.isSameOrAfter(bMo, "second")) {
              return -1;
          } else return 1;
          return 0;
      });
  
      // Filter
      if(filter && filter != ""){
          // TODO: Maybe some room for improvement here
          console.log("Rows without filter ("+ filter +"): ", json.DataCollection.length);
          json.DataCollection = json.DataCollection.filter(function(item){
              if(filter.indexOf("date: ") != -1){
                  // Apply Date Filter
                  var dateFilter = this.getFilter(filter, "date");
                  if(dateFilter != "" && item.ItemDate.indexOf(dateFilter) != -1) return true;
              }
              if(item.MessageData && item.MessageData.length > 1){
                  if(item.MessageData.toLowerCase().indexOf(filter.toLowerCase()) != -1) return true;
                  if(filter.toLowerCase().indexOf(item.MessageData.toLowerCase()) != -1) return true;
              }
              if (item.ItemType == "MSG") {
                  // Special search??
              } 
              else if (item.ItemType == "CDR") {
                  // Tag Search
                  if(item.Tags && item.Tags.length > 1){
                      var tagFilter = this.getFilter(filter, "tag");
                      if(tagFilter != "") {
                          if(item.Tags.some(function(i){
                              if(tagFilter.toLowerCase().indexOf(i.value.toLowerCase()) != -1) return true;
                              if(i.value.toLowerCase().indexOf(tagFilter.toLowerCase()) != -1) return true;
                              return false;
                          }) == true) return true;
                      }
                  }
              }
              else if(item.ItemType == "FILE"){
                  // Not yest implemented
              } 
              else if(item.ItemType == "SMS"){
                  // Not yest implemented
              }
              // return true to keep;
              return false;
          });
          console.log("Rows After Filter: ", json.DataCollection.length);
      }
  
      // Create Buffer
      if(json.DataCollection.length > this.streamBuffer){
          console.log("Rows:", json.DataCollection.length, " (will be trimmed to "+ this.streamBuffer +")");
          // Always limit the Stream to {streamBuffer}, users much search for messages further back
          json.DataCollection.splice(this.streamBuffer);
      }
  
      // $.each(json.DataCollection, function (i, item) {
  
      //     var IsToday = moment.utc(item.ItemDate.replace(" UTC", "")).isSame(moment.utc(), "day");
      //     var DateTime = moment.utc(item.ItemDate.replace(" UTC", "")).local().calendar(null, { sameElse: this.displayDateFormat });
      //     if(IsToday) DateTime = moment.utc(item.ItemDate.replace(" UTC", "")).local().format(this.displayTimeFormat);
  
      //     if (item.ItemType == "MSG") {
      //         // Add Chat Message
      //         // ===================
  
      //         //Billsec: "0"
      //         //Dst: "sip:800"
      //         //DstUserId: "8D68C1D442A96B4"
      //         //ItemDate: "2019-05-14 09:42:15"
      //         //ItemId: "89"
      //         //ItemType: "MSG"
      //         //MessageData: "........."
      //         //Src: ""Keyla James" <100>"
      //         //SrcUserId: "8D68B3EFEC8D0F5"
  
      //         var deliveryStatus = "<i class=\"fa fa-question-circle-o SendingMessage\"></i>"
      //         if(item.Sent == true) deliveryStatus = "<i class=\"fa fa-check SentMessage\"></i>";
      //         if(item.Sent == false) deliveryStatus = "<i class=\"fa fa-exclamation-circle FailedMessage\"></i>";
      //         if(item.Delivered && item.Delivered.state == true) {
      //             deliveryStatus += " <i class=\"fa fa-check DeliveredMessage\"></i>";
      //         }
      //         if(item.Displayed && item.Displayed.state == true){
      //             deliveryStatus = "<i class=\"fa fa-check CompletedMessage\"></i>";
      //         }
  
      //         var formattedMessage = this.ReformatMessage(item.MessageData);
      //         var longMessage = (formattedMessage.length > 1000);
  
      //         if (item.SrcUserId == this.profileUserID) {
      //             // You are the source (sending)
      //             var messageString = "<table class=ourChatMessage cellspacing=0 cellpadding=0><tr>"
      //             messageString += "<td class=ourChatMessageText onmouseenter=\"ShowChatMenu(this)\" onmouseleave=\"HideChatMenu(this)\">"
      //             messageString += "<span onclick=\"ShowMessageMenu(this,'MSG','"+  item.ItemId +"', '"+ buddyObj.identity +"')\" class=chatMessageDropdown style=\"display:none\"><i class=\"fa fa-chevron-down\"></i></span>";
      //             messageString += "<div id=msg-text-"+ item.ItemId +" class=messageText style=\""+ ((longMessage)? "max-height:190px; overflow:hidden" : "") +"\">" + formattedMessage + "</div>"
      //             if(longMessage){
      //                 messageString += "<div id=msg-readmore-"+  item.ItemId +" class=messageReadMore><span onclick=\"ExpandMessage(this,'"+ item.ItemId +"', '"+ buddyObj.identity +"')\">"+ this.lang.read_more +"</span></div>"
      //             }
      //             messageString += "<div class=messageDate>" + DateTime + " " + deliveryStatus +"</div>"
      //             messageString += "</td>"
      //             messageString += "</tr></table>";
      //         } 
      //         else {
      //             // You are the destination (receiving)
      //             var ActualSender = ""; //TODO
      //             var messageString = "<table class=theirChatMessage cellspacing=0 cellpadding=0><tr>"
      //             messageString += "<td class=theirChatMessageText onmouseenter=\"ShowChatMenu(this)\" onmouseleave=\"HideChatMenu(this)\">";
      //             messageString += "<span onclick=\"ShowMessageMenu(this,'MSG','"+  item.ItemId +"', '"+ buddyObj.identity +"')\" class=chatMessageDropdown style=\"display:none\"><i class=\"fa fa-chevron-down\"></i></span>";
      //             if(buddyObj.type == "group"){
      //                 messageString += "<div class=messageDate>" + ActualSender + "</div>";
      //             }
      //             messageString += "<div id=msg-text-"+ item.ItemId +" class=messageText style=\""+ ((longMessage)? "max-height:190px; overflow:hidden" : "") +"\">" + formattedMessage + "</div>";
      //             if(longMessage){
      //                 messageString += "<div id=msg-readmore-"+  item.ItemId +" class=messageReadMore><span onclick=\"ExpandMessage(this,'"+ item.ItemId +"', '"+ buddyObj.identity +"')\">"+ this.lang.read_more +"</span></div>"
      //             }
      //             messageString += "<div class=messageDate>"+ DateTime + "</div>";
      //             messageString += "</td>";
      //             messageString += "</tr></table>";
  
      //             // Update any received messages
      //             if(buddyObj.type == "xmpp") {
      //                 var streamVisible = $("#stream-"+ buddyObj.identity).is(":visible");
      //                 if (streamVisible && !item.Read) {
      //                     console.log("Buddy stream is now visible, marking XMPP message("+ item.ItemId +") as read")
      //                     this.markMessageRead(buddyObj, item.ItemId);
      //                     this.XmppSendDisplayReceipt(buddyObj, item.ItemId);
      //                 }
      //             }
  
      //         }
      //         $("#contact-" + buddyObj.identity + "-ChatHistory").prepend(messageString);
      //     } 
      //     else if (item.ItemType == "CDR") {
      //         // Add CDR 
      //         // =======
  
      //         // CdrId = uID(),
      //         // ItemType: "CDR",
      //         // ItemDate: "...",
      //         // SrcUserId: srcId,
      //         // Src: srcCallerID,
      //         // DstUserId: dstId,
      //         // Dst: dstCallerID,
      //         // Billsec: duration.asSeconds(),
      //         // MessageData: ""
      //         // ReasonText: 
      //         // ReasonCode: 
      //         // Flagged
      //         // Tags: [""", "", "", ""]
      //         // Transfers: [{}],
      //         // Mutes: [{}],
      //         // Holds: [{}],
      //         // Recordings: [{ uID, startTime, mediaType, stopTime: utcDateNow, size}],
      //         // QOS: [{}]
      
      //         var iconColor = (item.Billsec > 0)? "green" : "red";
      //         var formattedMessage:any = "";
  
      //         // Flagged
      //         var flag = "<span id=cdr-flagged-"+  item.CdrId +" style=\""+ ((item.Flagged)? "" : "display:none") +"\">";
      //         flag += "<i class=\"fa fa-flag FlagCall\"></i> ";
      //         flag += "</span>";
  
      //         // Comment
      //         var callComment = "";
      //         if(item.MessageData) callComment = item.MessageData;
  
      //         // Tags
      //         if(!item.Tags) item.Tags = [];
      //         var CallTags = "<ul id=cdr-tags-"+  item.CdrId +" class=tags style=\""+ ((item.Tags && item.Tags.length > 0)? "" : "display:none" ) +"\">"
      //         $.each(item.Tags, function (i, tag) {
      //             CallTags += "<li onclick=\"TagClick(this, '"+ item.CdrId +"', '"+ buddyObj.identity +"')\">"+ tag.value +"</li>";
      //         });
      //         CallTags += "<li class=tagText><input maxlength=24 type=text onkeypress=\"TagKeyPress(event, this, '"+ item.CdrId +"', '"+ buddyObj.identity +"')\" onfocus=\"TagFocus(this)\"></li>";
      //         CallTags += "</ul>";
  
      //         // Call Type
      //         var callIcon = (item.WithVideo)? "fa-video-camera" :  "fa-phone";
      //         formattedMessage += "<i class=\"fa "+ callIcon +"\" style=\"color:"+ iconColor +"\"></i>";
      //         var audioVideo = (item.WithVideo)? this.lang.a_video_call :  this.lang.an_audio_call;
  
      //         // Recordings
      //         var recordingsHtml = "";
      //         if(item.Recordings && item.Recordings.length >= 1){
      //             $.each(item.Recordings, function (i, recording) {
      //                 if(recording.uID){
      //                     var StartTime = moment.utc(recording.startTime.replace(" UTC", "")).local();
      //                     var StopTime = moment.utc(recording.stopTime.replace(" UTC", "")).local();
      //                     var recordingDuration = moment.duration(StopTime.diff(StartTime));
      //                     recordingsHtml += "<div class=callRecording>";
      //                     if(item.WithVideo){
      //                         if(recording.Poster){
      //                             var posterWidth = recording.Poster.width;
      //                             var posterHeight = recording.Poster.height;
      //                             var posterImage = recording.Poster.posterBase64;
      //                             recordingsHtml += "<div><IMG src=\""+ posterImage +"\"><button onclick=\"playVideoCallRecording(this, '"+ item.CdrId +"', '"+ recording.uID +"')\" class=videoPoster><i class=\"fa fa-play\"></i></button></div>";
      //                         }
      //                         else {
      //                             recordingsHtml += "<div><button class=roundButtons onclick=\"playVideoCallRecording(this, '"+ item.CdrId +"', '"+ recording.uID +"', '"+ buddyObj.identity +"')\"><i class=\"fa fa-video-camera\"></i></button></div>";
      //                         }
      //                     } 
      //                     else {
      //                         recordingsHtml += "<div><button class=roundButtons onclick=\"playAudioCallRecording(this, '"+ item.CdrId +"', '"+ recording.uID +"', '"+ buddyObj.identity +"')\"><i class=\"fa fa-play\"></i></button></div>";
      //                     } 
      //                     recordingsHtml += "<div>"+ this.lang.started +": "+ StartTime.format(this.displayTimeFormat) +" <i class=\"fa fa-long-arrow-right\"></i> "+ this.lang.stopped +": "+ StopTime.format(this.displayTimeFormat) +"</div>";
      //                     recordingsHtml += "<div>"+ this.lang.recording_duration +": "+ this.formatShortDuration(recordingDuration.asSeconds()) +"</div>";
      //                     recordingsHtml += "<div>";
      //                     recordingsHtml += "<span id=\"cdr-video-meta-width-"+ item.CdrId +"-"+ recording.uID +"\"></span>";
      //                     recordingsHtml += "<span id=\"cdr-video-meta-height-"+ item.CdrId +"-"+ recording.uID +"\"></span>";
      //                     recordingsHtml += "<span id=\"cdr-media-meta-size-"+ item.CdrId +"-"+ recording.uID +"\"></span>";
      //                     recordingsHtml += "<span id=\"cdr-media-meta-codec-"+ item.CdrId +"-"+ recording.uID +"\"></span>";
      //                     recordingsHtml += "</div>";
      //                     recordingsHtml += "</div>";
      //                 }
      //             });
      //         }
  
      //         if (item.SrcUserId == this.profileUserID) {
      //             // (Outbound) You(profileUserID) initiated a call
      //             if(item.Billsec == "0") {
      //                 formattedMessage += " "+ this.lang.you_tried_to_make +" "+ audioVideo +" ("+ item.ReasonText +").";
      //             } 
      //             else {
      //                 formattedMessage += " "+ this.lang.you_made + " "+ audioVideo +", "+ this.lang.and_spoke_for +" " + this.formatDuration(item.Billsec) + ".";
      //             }
      //             var messageString = "<table class=ourChatMessage cellspacing=0 cellpadding=0><tr>"
      //             messageString += "<td style=\"padding-right:4px;\">" + flag + "</td>"
      //             messageString += "<td class=ourChatMessageText onmouseenter=\"ShowChatMenu(this)\" onmouseleave=\"HideChatMenu(this)\">";
      //             messageString += "<span onClick=\"ShowMessageMenu(this,'CDR','"+  item.CdrId +"', '"+ buddyObj.identity +"')\" class=chatMessageDropdown style=\"display:none\"><i class=\"fa fa-chevron-down\"></i></span>";
      //             messageString += "<div>" + formattedMessage + "</div>";
      //             messageString += "<div>" + CallTags + "</div>";
      //             messageString += "<div id=cdr-comment-"+  item.CdrId +" class=cdrComment>" + callComment + "</div>";
      //             messageString += "<div class=callRecordings>" + recordingsHtml + "</div>";
      //             messageString += "<div class=messageDate>" + DateTime  + "</div>";
      //             messageString += "</td>"
      //             messageString += "</tr></table>";
      //         } 
      //         else {
      //             // (Inbound) you(profileUserID) received a call
      //             if(item.Billsec == "0"){
      //                 formattedMessage += " "+ this.lang.you_missed_a_call + " ("+ item.ReasonText +").";
      //             } 
      //             else {
      //                 formattedMessage += " "+ this.lang.you_received + " "+ audioVideo +", "+ this.lang.and_spoke_for +" " + this.formatDuration(item.Billsec) + ".";
      //             }
      //             var messageString = "<table class=theirChatMessage cellspacing=0 cellpadding=0><tr>";
      //             messageString += "<td class=theirChatMessageText onmouseenter=\"ShowChatMenu(this)\" onmouseleave=\"HideChatMenu(this)\">";
      //             messageString += "<span onClick=\"ShowMessageMenu(this,'CDR','"+  item.CdrId +"', '"+ buddyObj.identity +"')\" class=chatMessageDropdown style=\"display:none\"><i class=\"fa fa-chevron-down\"></i></span>";
      //             messageString += "<div style=\"text-align:left\">" + formattedMessage + "</div>";
      //             messageString += "<div>" + CallTags + "</div>";
      //             messageString += "<div id=cdr-comment-"+  item.CdrId +" class=cdrComment>" + callComment + "</div>";
      //             messageString += "<div class=callRecordings>" + recordingsHtml + "</div>";
      //             messageString += "<div class=messageDate> " + DateTime + "</div>";
      //             messageString += "</td>";
      //             messageString += "<td style=\"padding-left:4px\">" + flag + "</td>";
      //             messageString += "</tr></table>";
      //         }
      //         // Messages are prepended here, and appended when logging
      //         $("#contact-" + buddyObj.identity + "-ChatHistory").prepend(messageString);
      //     } 
      //     else if(item.ItemType == "FILE"){
      //         // TODO
      //     } 
      //     else if(item.ItemType == "SMS"){
      //         // TODO
      //     }
      // });
  
      // For some reason, the first time this fires, it doesn't always work
     // this.updateScroll(buddyObj.identity);
      window.setTimeout(function(){
        //  this.updateScroll(buddyObj.identity);
      }, 300);
  }
  
  // Missed Item Notification
  // ========================
  increaseMissedBadge(buddy:any) {
      var buddyObj:any = "this.FindBuddyByIdentity(buddy)";
      if(buddyObj == null) return;
  
      // Up the Missed Count
      // ===================
      buddyObj.missed += 1;
  
      // Take Out
      var json = JSON.parse(this.localDB.getItem(this.profileUserID + "-Buddies"));
      if(json != null) {
          // $.each(json.DataCollection, function (i, item) {
          //     if(item.uID == buddy || item.cID == buddy || item.gID == buddy){
          //         item.missed = item.missed +1;
          //         return false;
          //     }
          // });
          // Put Back
          this.localDB.setItem(this.profileUserID + "-Buddies", JSON.stringify(json));
      }
  
      // Update Badge
      // ============
      // $("#contact-" + buddy + "-missed").text(buddyObj.missed);
      // $("#contact-" + buddy + "-missed").show();
  
      console.log("Set Missed badge for "+ buddyObj.CallerIDName +" to: "+ buddyObj.missed);
  }
  
  clearMissedBadge(buddy) {
      var buddyObj:any = "FindBuddyByIdentity(buddy)";
      if(buddyObj == null) return;
  
      buddyObj.missed = 0;
  
      // Take Out
      var json = JSON.parse(this.localDB.getItem(this.profileUserID + "-Buddies"));
      if(json != null) {
          // $.each(json.DataCollection, function (i, item) {
          //     if(item.uID == buddy || item.cID == buddy || item.gID == buddy){
          //         item.missed = 0;
          //         return false;
          //     }
          // });
          // Put Back
          this.localDB.setItem(this.profileUserID + "-Buddies", JSON.stringify(json));
      }
  
      // $("#contact-" + buddy + "-missed").text(buddyObj.missed);
      // $("#contact-" + buddy + "-missed").hide(400);
  
  }
  
  // Outbound Calling
  // ================
  videoCall(lineObj:any, dialledNumber:any, extraHeaders:any) {
      if(this.userAgent == null) return;
      if(!this.userAgent.isRegistered()) return;
      if(lineObj == null) return;
  
      if(this.hasAudioDevice == false){
          this.alert(this.lang.alert_no_microphone,"","");
          return;
      }
  
      if(this.hasVideoDevice == false){
          console.warn("No video devices (webcam) found, switching to audio call.");
          this.audioCall(lineObj, dialledNumber,"");
          return;
      }
  
      var supportedConstraints = navigator.mediaDevices.getSupportedConstraints();
      var spdOptions = {
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
      var currentAudioDevice = this.getAudioSrcID();
      if(currentAudioDevice != "default"){
          var confirmedAudioDevice = false;
          for (var i = 0; i < this.audioinputDevices.length; ++i) {
              if(currentAudioDevice == this.audioinputDevices[i].deviceId) {
                  confirmedAudioDevice = true;
                  break;
              }
          }
          if(confirmedAudioDevice) {
              spdOptions.sessionDescriptionHandlerOptions.constraints.audio.deviceId = { exact: currentAudioDevice }
          }
          else {
              console.warn("The audio device you used before is no longer available, default settings applied.");
              this.localDB.setItem("AudioSrcId", "default");
          }
      }
      // Add additional Constraints
      if(supportedConstraints.autoGainControl) {
          spdOptions.sessionDescriptionHandlerOptions.constraints.audio["autoGainControl"] = this.autoGainControl;
      }
      if(supportedConstraints.echoCancellation) {
          spdOptions.sessionDescriptionHandlerOptions.constraints.audio["echoCancellation"] = this.echoCancellation;
      }
      if(supportedConstraints.noiseSuppression) {
          spdOptions.sessionDescriptionHandlerOptions.constraints.audio["noiseSuppression"] = this.noiseSuppression;
      }
  
      // Configure Video
      var currentVideoDevice = this.getVideoSrcID();
      if(currentVideoDevice != "default"){
          var confirmedVideoDevice = false;
          for (var i = 0; i < this.videoinputDevices.length; ++i) {
              if(currentVideoDevice == this.videoinputDevices[i].deviceId) {
                  confirmedVideoDevice = true;
                  break;
              }
          }
          if(confirmedVideoDevice){
              spdOptions.sessionDescriptionHandlerOptions.constraints.video.deviceId = { exact: currentVideoDevice }
          }
          else {
              console.warn("The video device you used before is no longer available, default settings applied.");
              this.localDB.setItem("VideoSrcId", "default"); // resets for later and subsequent calls
          }
      }
      // Add additional Constraints
      if(supportedConstraints.frameRate && this.maxFrameRate != "") {
          spdOptions.sessionDescriptionHandlerOptions.constraints.video["frameRate"] = this.maxFrameRate;
      }
      if(supportedConstraints.height && this.videoHeight != "") {
          spdOptions.sessionDescriptionHandlerOptions.constraints.video["height"] = this.videoHeight;
      }
      if(supportedConstraints.aspectRatio && this.videoAspectRatio != "") {
          spdOptions.sessionDescriptionHandlerOptions.constraints.video["aspectRatio"] = this.videoAspectRatio;
      }
      // Extra Headers
      if(extraHeaders) {
          spdOptions["extraHeaders"] = extraHeaders;
      }
  
      // $("#line-" + lineObj.LineNumber + "-msg").html(this.lang.starting_video_call);
      // $("#line-" + lineObj.LineNumber + "-timer").show();
  
      var startTime = moment.utc();
  
      // Invite
      console.log("INVITE (video): " + dialledNumber + "@" + this.sipDomain); 
  
      var targetURI = UserAgent.makeURI("sip:" + dialledNumber.replace(/#/g, "%23") + "@" + this.sipDomain);
      lineObj.SipSession = new Inviter(this.userAgent, targetURI, spdOptions);
      lineObj.SipSession.data = {}
      lineObj.SipSession.data.line = lineObj.LineNumber;
      lineObj.SipSession.data.buddyId = lineObj.BuddyObj.identity;
      lineObj.SipSession.data.calldirection = "outbound";
      lineObj.SipSession.data.dst = dialledNumber;
      lineObj.SipSession.data.callstart = startTime.format("YYYY-MM-DD HH:mm:ss UTC");
      lineObj.SipSession.data.callTimer = window.setInterval(function(){
          var now = moment.utc();
          var duration = moment.duration(now.diff(startTime)); 
          var timeStr = this.formatShortDuration(duration.asSeconds());
          // $("#line-" + lineObj.LineNumber + "-timer").html(timeStr);
          // $("#line-" + lineObj.LineNumber + "-datetime").html(timeStr);
      }, 1000);
      lineObj.SipSession.data.VideoSourceDevice = this.getVideoSrcID();
      lineObj.SipSession.data.AudioSourceDevice = this.getAudioSrcID();
      lineObj.SipSession.data.AudioOutputDevice = this.getAudioOutputID();
      lineObj.SipSession.data.terminateby = "them";
      lineObj.SipSession.data.withvideo = true;
      lineObj.SipSession.data.earlyReject = false;
      lineObj.SipSession.isOnHold = false;
      lineObj.SipSession.delegate = {
          onBye: function(sip){
              this.onSessionReceivedBye(lineObj, sip);
          },
          onMessage: function(sip){
              this.onSessionReceivedMessage(lineObj, sip);
          },
          onInvite: function(sip){
              this.onSessionReinvited(lineObj, sip);
          },
          onSessionDescriptionHandler: function(sdh, provisional){
              this.onSessionDescriptionHandlerCreated(lineObj, sdh, provisional, true);
          }
      }
      var inviterOptions = {
          requestDelegate: { // OutgoingRequestDelegate
              onTrying: function(sip){
                  this.onInviteTrying(lineObj, sip);
              },
              onProgress:function(sip){
                  this.onInviteProgress(lineObj, sip);
              },
              onRedirect:function(sip){
                  this.onInviteRedirected(lineObj, sip);
              },
              onAccept:function(sip){
                  this.onInviteAccepted(lineObj, true, sip);
              },
              onReject:function(sip){
                  this.onInviteRejected(lineObj, sip);
              }
          }
      }
      lineObj.SipSession.invite(inviterOptions).catch(function(e){
          console.warn("Failed to send INVITE:", e);
      });
  
  //     $("#line-" + lineObj.LineNumber + "-btn-settings").removeAttr('disabled');
  //     $("#line-" + lineObj.LineNumber + "-btn-audioCall").prop('disabled','disabled');
  //     $("#line-" + lineObj.LineNumber + "-btn-videoCall").prop('disabled','disabled');
  //     $("#line-" + lineObj.LineNumber + "-btn-search").removeAttr('disabled');
  
  //     $("#line-" + lineObj.LineNumber + "-progress").show();
  //     $("#line-" + lineObj.LineNumber + "-msg").show();
  
  }
  
  
  audioCall(lineObj, dialledNumber, extraHeaders) {
      if(this.userAgent == null) return;
      if(this.userAgent.isRegistered() == false) return;
      if(lineObj == null) return;
  
      if(this.hasAudioDevice == false){
          this.alert(this.lang.alert_no_microphone,"","");
          return;
      }
  
      var supportedConstraints = navigator.mediaDevices.getSupportedConstraints();
  
      var spdOptions = {
          earlyMedia: true,
          sessionDescriptionHandlerOptions: {
              constraints: {
                  audio: { deviceId : {} },
                  video: false
              }
          }
      }
      // Configure Audio
      var currentAudioDevice = this.getAudioSrcID();
      if(currentAudioDevice != "default"){
          var confirmedAudioDevice = false;
          for (var i = 0; i < this.audioinputDevices.length; ++i) {
              if(currentAudioDevice == this.audioinputDevices[i].deviceId) {
                  confirmedAudioDevice = true;
                  break;
              }
          }
          if(confirmedAudioDevice) {
              spdOptions.sessionDescriptionHandlerOptions.constraints.audio.deviceId = { exact: currentAudioDevice }
          }
          else {
              console.warn("The audio device you used before is no longer available, default settings applied.");
              this.localDB.setItem("AudioSrcId", "default");
          }
      }
      // Add additional Constraints
      if(supportedConstraints.autoGainControl) {
          spdOptions.sessionDescriptionHandlerOptions.constraints.audio["autoGainControl"] = this.autoGainControl;
      }
      if(supportedConstraints.echoCancellation) {
          spdOptions.sessionDescriptionHandlerOptions.constraints.audio["echoCancellation"] = this.echoCancellation;
      }
      if(supportedConstraints.noiseSuppression) {
          spdOptions.sessionDescriptionHandlerOptions.constraints.audio["noiseSuppression"] = this.noiseSuppression;
      }
      // Extra Headers
      if(extraHeaders) {
          spdOptions["extraHeaders"] = extraHeaders;
      }
  
      // $("#line-" + lineObj.LineNumber + "-msg").html(this.lang.starting_audio_call);
      // $("#line-" + lineObj.LineNumber + "-timer").show();
  
      var startTime = moment.utc();
  
      // Invite
      console.log("INVITE (audio): " + dialledNumber + "@" + this.sipDomain);
  
      var targetURI = UserAgent.makeURI("sip:" + dialledNumber.replace(/#/g, "%23") + "@" + this.sipDomain);
      lineObj.SipSession = new Inviter(this.userAgent, targetURI, spdOptions);
      lineObj.SipSession.data = {}
      lineObj.SipSession.data.line = lineObj.LineNumber;
      lineObj.SipSession.data.buddyId = lineObj.BuddyObj.identity;
      lineObj.SipSession.data.calldirection = "outbound";
      lineObj.SipSession.data.dst = dialledNumber;
      lineObj.SipSession.data.callstart = startTime.format("YYYY-MM-DD HH:mm:ss UTC");
      lineObj.SipSession.data.callTimer = window.setInterval(function(){
          var now = moment.utc();
          var duration = moment.duration(now.diff(startTime)); 
          var timeStr = this.formatShortDuration(duration.asSeconds());
          // $("#line-" + lineObj.LineNumber + "-timer").html(timeStr);
          // $("#line-" + lineObj.LineNumber + "-datetime").html(timeStr);
      }, 1000);
      lineObj.SipSession.data.VideoSourceDevice = null;
      lineObj.SipSession.data.AudioSourceDevice = this.getAudioSrcID();
      lineObj.SipSession.data.AudioOutputDevice = this.getAudioOutputID();
      lineObj.SipSession.data.terminateby = "them";
      lineObj.SipSession.data.withvideo = false;
      lineObj.SipSession.data.earlyReject = false;
      lineObj.SipSession.isOnHold = false;
      lineObj.SipSession.delegate = {
          onBye: function(sip){
              this.onSessionReceivedBye(lineObj, sip);
          },
          onMessage: function(sip){
              this.onSessionReceivedMessage(lineObj, sip);
          },
          onInvite: function(sip){
              this.onSessionReinvited(lineObj, sip);
          },
          onSessionDescriptionHandler: function(sdh, provisional){
              this.onSessionDescriptionHandlerCreated(lineObj, sdh, provisional, false);
          }
      }
      var inviterOptions = {
          requestDelegate: { // OutgoingRequestDelegate
              onTrying: function(sip){
                  this.onInviteTrying(lineObj, sip);
              },
              onProgress:function(sip){
                  this.onInviteProgress(lineObj, sip);
              },
              onRedirect:function(sip){
                  this.onInviteRedirected(lineObj, sip);
              },
              onAccept:function(sip){
                  this.onInviteAccepted(lineObj, false, sip);
              },
              onReject:function(sip){
                  this.onInviteRejected(lineObj, sip);
              }
          }
      }
      lineObj.SipSession.invite(inviterOptions).catch(function(e){
          console.warn("Failed to send INVITE:", e);
      });
  
      // $("#line-" + lineObj.LineNumber + "-btn-settings").removeAttr('disabled');
      // $("#line-" + lineObj.LineNumber + "-btn-audioCall").prop('disabled','disabled');
      // $("#line-" + lineObj.LineNumber + "-btn-videoCall").prop('disabled','disabled');
      // $("#line-" + lineObj.LineNumber + "-btn-search").removeAttr('disabled');
  
      // $("#line-" + lineObj.LineNumber + "-progress").show();
      // $("#line-" + lineObj.LineNumber + "-msg").show();    
  }
  
  
  
  
  // Inbound Calls
  // =============
  receiveOutOfDialogMessage(message:any) {
    var callerID = message.request.from.displayName;
    var did = message.request.from.uri.normal.user;
  
    // Out of dialog Message Receiver
    var messageType = (message.request.headers["Content-Type"].length >=1)? message.request.headers["Content-Type"][0].parsed : "Unknown" ;
    // Text Messages
    if(messageType.indexOf("text/plain") > -1){
        // Plain Text Messages SIP SIMPLE
        console.log("New Incoming Message!", "\""+ callerID +"\" <"+ did +">");
  
        if(did.length > this.didLength) {
            // Contacts cannot receive Test Messages, because they cannot reply
            // This may change with FAX, Email, WhatsApp etc
            console.warn("DID length greater then extensions length")
            return;
        }
  
        var CurrentCalls = this.countSessions("0");
  
      //   var buddyObj = this.FindBuddyByDid(did);
      var buddyObj:any = "";
      // Make new contact of its not there
        if(buddyObj == null) {
            var json = JSON.parse(this.localDB.getItem(this.profileUserID + "-Buddies"));
          //  if(json == null) json = this.InitUserBuddies();
  
            // Add Extension
            var id = this.uID();
            var dateNow = this.utcDateNow();
            json.DataCollection.push({
                Type: "extension",
                LastActivity: dateNow,
                ExtensionNumber: did,
                MobileNumber: "",
                ContactNumber1: "",
                ContactNumber2: "",
                uID: id,
                cID: null,
                gID: null,
                jid: null,
                DisplayName: callerID,
                Description: "", 
                Email: "",
                MemberCount: 0,
                EnableDuringDnd: false,
                Subscribe: false
            });
            //buddyObj = new Buddy("extension", id, callerID, did, "", "", "", dateNow, "", "", jid, false, false);
            
            // Add memory object
            //AddBuddy(buddyObj, true, (CurrentCalls==0), false, tue);
  
            // Update Size: 
            json.TotalRows = json.DataCollection.length;
  
            // Save To DB
            this.localDB.setItem(this.profileUserID + "-Buddies", JSON.stringify(json));
        }
  
        var originalMessage = message.request.body;
        var messageId = this.uID();
        var DateTime = this.utcDateNow();
  
        message.accept();
  
        this.addMessageToStream(buddyObj, messageId, "MSG", originalMessage, DateTime)
        //this.UpdateBuddyActivity(buddyObj.identity);
        this.refreshStream(buddyObj,"");
        this.activateStream(buddyObj, originalMessage);
    }
    // Message Summary
    else if(messageType.indexOf("application/simple-message-summary") > -1){
        console.warn("This message-summary is unsolicited (out-of-dialog). Consider using the SUBSCRIBE method.")
        this.voicemailNotify(message);
    }
    else{
        console.warn("Unknown Out Of Dialog Message Type: ", messageType);
        message.reject();
    }
  
  }
  
  receiveCall(session:any) {
    var callerID = session.remoteIdentity.displayName;
    var did = session.remoteIdentity.uri.user;
    if (typeof callerID === 'undefined') callerID = did;
  
    console.log("New Incoming Call!", callerID +" <"+ did +">");
  
    var CurrentCalls = this.countSessions(session.id);
    console.log("Current Call Count:", CurrentCalls);
  
    //   var buddyObj = this.FindBuddyByDid(did);
    var buddyObj:any = "";
    // Make new contact of its not there
    if(buddyObj == null) {
  
        // Check if Privacy DND is enabled
  
        var buddyType = (did.length > this.didLength)? "contact" : "extension";
        var focusOnBuddy = (CurrentCalls==0);
    //    buddyObj = this.MakeBuddy(buddyType, true, focusOnBuddy, false, callerID, did, null, false, null, this.autoDeleteDefault);
    }
    else {
        // Double check that the buddy has the same caller ID as the incoming call
        // With Buddies that are contacts, eg +441234567890 <+441234567890> leave as as
        if(buddyObj.type == "extension" && buddyObj.CallerIDName != callerID){
      //    this.UpdateBuddyCallerID(buddyObj, callerID);
        }
        else if(buddyObj.type == "contact" && callerID != did && buddyObj.CallerIDName != callerID){
        //  this.UpdateBuddyCallerID(buddyObj, callerID);
        }
    }
  
    var startTime = moment.utc();
  
    // Create the line and add the session so we can answer or reject it.
    let newLineNumber = 0;
    newLineNumber = newLineNumber + 1;
    //var lineObj = new Line(newLineNumber, callerID, did, buddyObj);
    var lineObj:any = "";
    lineObj.SipSession = session;
    lineObj.SipSession.data = {}
    lineObj.SipSession.data.line = lineObj.LineNumber;
    lineObj.SipSession.data.calldirection = "inbound";
    lineObj.SipSession.data.terminateby = "";
    lineObj.SipSession.data.src = did;
    lineObj.SipSession.data.buddyId = lineObj.BuddyObj.identity;
    lineObj.SipSession.data.callstart = startTime.format("YYYY-MM-DD HH:mm:ss UTC");
    lineObj.SipSession.data.callTimer = window.setInterval(function(){
        var now = moment.utc();
        var duration = moment.duration(now.diff(startTime));
        var timeStr = this.formatShortDuration(duration.asSeconds());
      //   $("#line-" + lineObj.LineNumber + "-timer").html(timeStr);
      //   $("#line-" + lineObj.LineNumber + "-datetime").html(timeStr);
    }, 1000);
    lineObj.SipSession.data.earlyReject = false;
    this.lines.push(lineObj);
    // Detect Video
    lineObj.SipSession.data.withvideo = false;
    if(this.enableVideoCalling == true && lineObj.SipSession.request.body){
        // Asterisk 13 PJ_SIP always sends m=video if endpoint has video codec,
        // even if original invite does not specify video.
        if(lineObj.SipSession.request.body.indexOf("m=video") > -1) {
            lineObj.SipSession.data.withvideo = true;
            // The invite may have video, but the buddy may be a contact
            if(buddyObj.type == "contact"){
                // videoInvite = false;
                // TODO: Is this limitation necessary?
            }
        }
    }
  
    // Session Delegates
    lineObj.SipSession.delegate = {
        onBye: function(sip){
          this.onSessionReceivedBye(lineObj, sip)
        },
        onMessage: function(sip){
          this.onSessionReceivedMessage(lineObj, sip);
        },
        onInvite: function(sip){
          this.onSessionReinvited(lineObj, sip);
        },
        onSessionDescriptionHandler: function(sdh, provisional){
          this.onSessionDescriptionHandlerCreated(lineObj, sdh, provisional, lineObj.SipSession.data.withvideo);
        }
    }
    // incomingInviteRequestDelegate
    lineObj.SipSession.incomingInviteRequest.delegate = {
        onCancel: function(sip){
          this.onInviteCancel(lineObj, sip)
        }
    }
  
    // Possible Early Rejection options
    if(this.doNotDisturbEnabled == true || this.doNotDisturbPolicy == "enabled") {
        if(this.doNotDisturbEnabled == true && buddyObj.EnableDuringDnd == true){
            // This buddy has been allowed 
            console.log("Buddy is allowed to call while you are on DND")
        }
        else {
            console.log("Do Not Disturb Enabled, rejecting call.");
            lineObj.SipSession.data.earlyReject = true;
            this.rejectCall(lineObj.LineNumber);
            return;
        }
    }
    if(CurrentCalls >= 1){
        if(this.callWaitingEnabled == false || String(this.callWaitingEnabled) == "disabled"){
            console.log("Call Waiting Disabled, rejecting call.");
            lineObj.SipSession.data.earlyReject = true;
            this.rejectCall(lineObj.LineNumber);
            return;
        }
    }
  
    // Create the call HTML 
    //this.AddLineHtml(lineObj, "inbound");
  //   $("#line-" + lineObj.LineNumber + "-msg").html(this.lang.incoming_call);
  //   $("#line-" + lineObj.LineNumber + "-msg").show();
  //   $("#line-" + lineObj.LineNumber + "-timer").show();
    if(lineObj.SipSession.data.withvideo){
      //   $("#line-"+ lineObj.LineNumber +"-answer-video").show();
    }
    else {
      //   $("#line-"+ lineObj.LineNumber +"-answer-video").hide();
    }
  //   $("#line-" + lineObj.LineNumber + "-AnswerCall").show();
  
    // Update the buddy list now so that any early rejected calls don't flash on
    //this.UpdateBuddyList();
  
    // Auto Answer options
    var autoAnswerRequested = false;
    var answerTimeout = 1000;
    if (!this.autoAnswerEnabled  && this.intercomPolicy == "enabled"){ // Check headers only if policy is allow
  
        // https://github.com/InnovateAsterisk/Browser-Phone/issues/126
        // Alert-Info: info=alert-autoanswer
        // Alert-Info: answer-after=0
        // Call-info: answer-after=0; x=y
        // Call-Info: Answer-After=0
        // Alert-Info: ;info=alert-autoanswer
        // Alert-Info: <sip:>;info=alert-autoanswer
        // Alert-Info: <sip:domain>;info=alert-autoanswer
  
        var ci = session.request.headers["Call-Info"];
        if (ci !== undefined && ci.length > 0){
            for (var i = 0; i < ci.length; i++){
                var raw_ci = ci[i].raw.toLowerCase();
                if (raw_ci.indexOf("answer-after=") > 0){
                    var temp_seconds_autoanswer = parseInt(raw_ci.substring(raw_ci.indexOf("answer-after=") +"answer-after=".length).split(';')[0]);
                    if (Number.isInteger(temp_seconds_autoanswer) && temp_seconds_autoanswer >= 0){
                        autoAnswerRequested = true;
                        if(temp_seconds_autoanswer > 1) answerTimeout = temp_seconds_autoanswer * 1000;
                        break;
                    }
                }
            }
        }
        var ai = session.request.headers["Alert-Info"];
        if (autoAnswerRequested === false && ai !== undefined && ai.length > 0){
            for (var i=0; i < ai.length ; i++){
                var raw_ai = ai[i].raw.toLowerCase();
                if (raw_ai.indexOf("auto answer") > 0 || raw_ai.indexOf("alert-autoanswer") > 0){
                    var autoAnswerRequested = true;
                    break;
                }
                if (raw_ai.indexOf("answer-after=") > 0){
                    var temp_seconds_autoanswer = parseInt(raw_ai.substring(raw_ai.indexOf("answer-after=") +"answer-after=".length).split(';')[0]);
                    if (Number.isInteger(temp_seconds_autoanswer) && temp_seconds_autoanswer >= 0){
                        autoAnswerRequested = true;
                        if(temp_seconds_autoanswer > 1) answerTimeout = temp_seconds_autoanswer * 1000;
                        break;
                    }
                }
            }
        }
    }
  
    if(this.autoAnswerEnabled || this.autoAnswerPolicy == "enabled" || autoAnswerRequested){
        if(CurrentCalls == 0){ // There are no other calls, so you can answer
            console.log("Going to Auto Answer this call...");
            window.setTimeout(function(){
                // If the call is with video, assume the auto answer is also
                // In order for this to work nicely, the recipient maut be "ready" to accept video calls
                // In order to ensure video call compatibility (i.e. the recipient must have their web cam in, and working)
                // The NULL video should be configured
                // https://github.com/InnovateAsterisk/Browser-Phone/issues/26
                if(lineObj.SipSession.data.withvideo) {
                  this.answervideoCall(lineObj.LineNumber);
                }
                else {
                  this.answeraudioCall(lineObj.LineNumber);
                }
            }, answerTimeout);
  
            // Select Buddy
            //this.SelectLine(lineObj.LineNumber);
            return;
        }
        else {
            console.warn("Could not auto answer call, already on a call.");
        }
    }
  
    // Check if that buddy is not already on a call??
    var streamVisible:any = '$("#stream-"+ buddyObj.identity).is(":visible")';
    if (streamVisible || CurrentCalls == 0) {
        // If you are already on the selected buddy who is now calling you, switch to his call.
        // NOTE: This will put other calls on hold
        if(CurrentCalls == 0) //this.SelectLine(lineObj.LineNumber);
        {}
    }
  
    // Show notification / Ring / Windows Etc
    // ======================================
  
    // Browser Window Notification
    if ("Notification" in window) {
        if (Notification.permission === "granted") {
            var noticeOptions = { body: this.lang.incoming_call_from +" " + callerID +" <"+ did +">", icon: "this.getPicture(buddyObj.identity)" }
            var inComingCallNotification = new Notification(this.lang.incoming_call, noticeOptions);
            inComingCallNotification.onclick = (event) =>{
  
                var lineNo = lineObj.LineNumber;
                var videoInvite = lineObj.SipSession.data.withvideo
                window.setTimeout(function(){
                    // https://github.com/InnovateAsterisk/Browser-Phone/issues/26
                    if(videoInvite) {
                      this.answervideoCall(lineNo)
                    }
                    else {
                      this.answeraudioCall(lineNo);
                    }
                }, 1000);
  
                // Select Buddy
               // this.SelectLine(lineNo);
                return;
            }
        }
    }
  
    // Play Ring Tone if not on the phone
    if(this.enableRingtone == true){
        if(CurrentCalls >= 1){
            // Play Alert
            console.log("Audio:", this.audioBlobs.CallWaiting.url);
            var ringer = new Audio(this.audioBlobs.CallWaiting.blob);
            ringer.preload = "auto";
            ringer.loop = false;
            ringer.oncanplaythrough = function(e) {
              //   if (typeof ringer.sinkId !== 'undefined' && this.getRingerOutputID() != "default") {
              //       ringer.setSinkId(this.getRingerOutputID()).then(function() {
              //           console.log("Set sinkId to:", this.getRingerOutputID());
              //       }).catch(function(e){
              //           console.warn("Failed not apply setSinkId.", e);
              //       });
              //  }
                // If there has been no interaction with the page at all... this page will not work
                ringer.play().then(function(){
                    // Audio Is Playing
                }).catch(function(e){
                    console.warn("Unable to play audio file.", e);
                }); 
            }
            lineObj.SipSession.data.ringerObj = ringer;
        } else {
            // Play Ring Tone
            console.log("Audio:", this.audioBlobs.Ringtone.url);
            var ringer = new Audio(this.audioBlobs.Ringtone.blob);
            ringer.preload = "auto";
            ringer.loop = true;
            ringer.oncanplaythrough = function(e) {
              //   if (typeof ringer.sinkId !== 'undefined' && this.getRingerOutputID() != "default") {
              //       ringer.setSinkId(this.getRingerOutputID()).then(function() {
              //           console.log("Set sinkId to:", this.getRingerOutputID());
              //       }).catch(function(e){
              //           console.warn("Failed not apply setSinkId.", e);
              //       });
               // }
                // If there has been no interaction with the page at all... this page will not work
                ringer.play().then(function(){
                    // Audio Is Playing
                }).catch(function(e){
                    console.warn("Unable to play audio file.", e);
                }); 
            }
            lineObj.SipSession.data.ringerObj = ringer;
        }
    
    }
  
  }
  
  answeraudioCall(lineNumber:any) {
    // CloseWindow();
  
    var lineObj = this.findLineByNumber(lineNumber);
    if(lineObj == null){
        console.warn("Failed to get line ("+ lineNumber +")");
        return;
    }
    var session = lineObj.SipSession;
    // Stop the ringtone
    if(session.data.ringerObj){
        session.data.ringerObj.pause();
        session.data.ringerObj.removeAttribute('src');
        session.data.ringerObj.load();
        session.data.ringerObj = null;
    }
    // Check vitals
    if(this.hasAudioDevice == false){
      //this.alert(this.lang.alert_no_microphone);
      //   $("#line-" + lineObj.LineNumber + "-msg").html(this.lang.call_failed);
      //   $("#line-" + lineObj.LineNumber + "-AnswerCall").hide();
        return;
    }
  
    // Update UI
  //   $("#line-" + lineObj.LineNumber + "-AnswerCall").hide();
  
    // Start SIP handling
    var supportedConstraints = navigator.mediaDevices.getSupportedConstraints();
    var spdOptions = {
        sessionDescriptionHandlerOptions: {
            constraints: {
                audio: { deviceId : {} },
                video: false
            }
        }
    }
  
    // Configure Audio
    var currentAudioDevice = this.getAudioSrcID();
    if(currentAudioDevice != "default"){
        var confirmedAudioDevice = false;
        for (var i = 0; i < this.audioinputDevices.length; ++i) {
            if(currentAudioDevice == this.audioinputDevices[i].deviceId) {
                confirmedAudioDevice = true;
                break;
            }
        }
        if(confirmedAudioDevice) {
            spdOptions.sessionDescriptionHandlerOptions.constraints.audio.deviceId = { exact: currentAudioDevice }
        }
        else {
            console.warn("The audio device you used before is no longer available, default settings applied.");
            this.localDB.setItem("AudioSrcId", "default");
        }
    }
    // Add additional Constraints
    if(supportedConstraints.autoGainControl) {
        spdOptions.sessionDescriptionHandlerOptions.constraints.audio["autoGainControl"] = this.autoGainControl;
    }
    if(supportedConstraints.echoCancellation) {
        spdOptions.sessionDescriptionHandlerOptions.constraints.audio["echoCancellation"] = this.echoCancellation;
    }
    if(supportedConstraints.noiseSuppression) {
        spdOptions.sessionDescriptionHandlerOptions.constraints.audio["noiseSuppression"] = this.noiseSuppression;
    }
  
    // Save Devices
    lineObj.SipSession.data.withvideo = false;
    lineObj.SipSession.data.VideoSourceDevice = null;
    lineObj.SipSession.data.AudioSourceDevice = this.getAudioSrcID();
    lineObj.SipSession.data.AudioOutputDevice = this.getAudioOutputID();
  
    // Send Answer
    lineObj.SipSession.accept(spdOptions).then(function(){
      this.onInviteAccepted(lineObj,false);
    }).catch(function(error){
        console.warn("Failed to answer call", error, lineObj.SipSession);
        lineObj.SipSession.data.reasonCode = 500;
        lineObj.SipSession.data.reasonText = "Client Error";
        this.teardownSession(lineObj);
    });
  }
  
  answervideoCall(lineNumber:any) {
    // CloseWindow();
  
    var lineObj = this.findLineByNumber(lineNumber);
    if(lineObj == null){
        console.warn("Failed to get line ("+ lineNumber +")");
        return;
    }
    var session = lineObj.SipSession;
    // Stop the ringtone
    if(session.data.ringerObj){
        session.data.ringerObj.pause();
        session.data.ringerObj.removeAttribute('src');
        session.data.ringerObj.load();
        session.data.ringerObj = null;
    }
    // Check vitals
    if(this.hasAudioDevice == false){
      //this.alert(lang.alert_no_microphone);
      //   $("#line-" + lineObj.LineNumber + "-msg").html(this.lang.call_failed);
      //   $("#line-" + lineObj.LineNumber + "-AnswerCall").hide();
        return;
    }
  
    // Update UI
  //   $("#line-" + lineObj.LineNumber + "-AnswerCall").hide();
  
    // Start SIP handling
    var supportedConstraints = navigator.mediaDevices.getSupportedConstraints();
    var spdOptions = {
        sessionDescriptionHandlerOptions: {
            constraints: {
                audio: { deviceId : {} },
                video: { deviceId : {} }
            }
        }
    }
  
    // Configure Audio
    var currentAudioDevice = this.getAudioSrcID();
    if(currentAudioDevice != "default"){
        var confirmedAudioDevice = false;
        for (var i = 0; i < this.audioinputDevices.length; ++i) {
            if(currentAudioDevice == this.audioinputDevices[i].deviceId) {
                confirmedAudioDevice = true;
                break;
            }
        }
        if(confirmedAudioDevice) {
            spdOptions.sessionDescriptionHandlerOptions.constraints.audio.deviceId = { exact: currentAudioDevice }
        }
        else {
            console.warn("The audio device you used before is no longer available, default settings applied.");
            this.localDB.setItem("AudioSrcId", "default");
        }
    }
    // Add additional Constraints
    if(supportedConstraints.autoGainControl) {
        spdOptions.sessionDescriptionHandlerOptions.constraints.audio["autoGainControl"] = this.autoGainControl;
    }
    if(supportedConstraints.echoCancellation) {
        spdOptions.sessionDescriptionHandlerOptions.constraints.audio["echoCancellation"] = this.echoCancellation;
    }
    if(supportedConstraints.noiseSuppression) {
        spdOptions.sessionDescriptionHandlerOptions.constraints.audio["noiseSuppression"] = this.noiseSuppression;
    }
  
    // Configure Video
    var currentVideoDevice = this.getVideoSrcID();
    if(currentVideoDevice != "default"){
        var confirmedVideoDevice = false;
        for (var i = 0; i < this.videoinputDevices.length; ++i) {
            if(currentVideoDevice == this.videoinputDevices[i].deviceId) {
                confirmedVideoDevice = true;
                break;
            }
        }
        if(confirmedVideoDevice){
            spdOptions.sessionDescriptionHandlerOptions.constraints.video["deviceId"] = { exact: currentVideoDevice }
        }
        else {
            console.warn("The video device you used before is no longer available, default settings applied.");
            this.localDB.setItem("VideoSrcId", "default"); // resets for later and subsequent calls
        }
    }
    // Add additional Constraints
    if(supportedConstraints.frameRate && this.maxFrameRate != "") {
        spdOptions.sessionDescriptionHandlerOptions.constraints.video["frameRate"] = this.maxFrameRate;
    }
    if(supportedConstraints.height && this.videoHeight != "") {
        spdOptions.sessionDescriptionHandlerOptions.constraints.video["height"] = this.videoHeight;
    }
    if(supportedConstraints.aspectRatio && this.videoAspectRatio != "") {
        spdOptions.sessionDescriptionHandlerOptions.constraints.video["aspectRatio"] = this.videoAspectRatio;
    }
  
    // Save Devices
    lineObj.SipSession.data.withvideo = true;
    lineObj.SipSession.data.VideoSourceDevice = this.getVideoSrcID();
    lineObj.SipSession.data.AudioSourceDevice = this.getAudioSrcID();
    lineObj.SipSession.data.AudioOutputDevice = this.getAudioOutputID();
  
   // if(this.startVideoFullScreen) this.expandVideoArea(lineObj.LineNumber);
  
    // Send Answer
    lineObj.SipSession.accept(spdOptions).then(function(){
      this.onInviteAccepted(lineObj,true);
    }).catch(function(error){
        console.warn("Failed to answer call", error, lineObj.SipSession);
        lineObj.SipSession.data.reasonCode = 500;
        lineObj.SipSession.data.reasonText = "Client Error";
        this.teardownSession(lineObj);
    });
  }
  
  rejectCall(lineNumber:any) {
    var lineObj = this.findLineByNumber(lineNumber);
    if (lineObj == null) {
        console.warn("Unable to find line ("+ lineNumber +")");
        return;
    }
    var session = lineObj.SipSession;
    if (session == null) {
        console.warn("Reject failed, null session");
      //   $("#line-" + lineObj.LineNumber + "-msg").html(this.lang.call_failed);
      //   $("#line-" + lineObj.LineNumber + "-AnswerCall").hide();
    }
    if(session.state == SessionState.Established){
        session.bye().catch(function(e){
            console.warn("Problem in rejectCall(), could not bye() call", e, session);
        });
    }
    else {
        session.reject({ 
            statusCode: 486, 
            reasonPhrase: "Busy Here" 
        }).catch(function(e){
            console.warn("Problem in rejectCall(), could not reject() call", e, session);
        });
    }
  //   $("#line-" + lineObj.LineNumber + "-msg").html(this.lang.call_rejected);
  
    session.data.terminateby = "us";
    session.data.reasonCode = 486;
    session.data.reasonText = "Busy Here";
    this.teardownSession(lineObj);
  }
  
  // Session Events
  // ==============
  
  // Incoming INVITE
  onInviteCancel(lineObj:any, response:any){
        // Remote Party Canceled while ringing...
  
        // Check to see if this call has been completed elsewhere
        // https://github.com/InnovateAsterisk/Browser-Phone/issues/405
        var temp_cause = 0;
        var reason = response.headers["Reason"];
        if (reason !== undefined && reason.length > 0){
            for (var i = 0; i < reason.length; i++){
                var cause = reason[i].raw.toLowerCase().trim(); // Reason: Q.850 ;cause=16 ;text="Terminated"
                var items = cause.split(';');
                if (items.length >= 2 && (items[0].trim() == "sip" || items[0].trim() == "q.850") && items[1].includes("cause") && cause.includes("call completed elsewhere")){
                    temp_cause = parseInt(items[1].substring(items[1].indexOf("=")+1).trim());
                    // No sample provided for "token"
                    break;
                }
            }
        }
  
        lineObj.SipSession.data.terminateby = "them";
        lineObj.SipSession.data.reasonCode = temp_cause;
        if(temp_cause == 0){
            lineObj.SipSession.data.reasonText = "Call Cancelled";
            console.log("Call canceled by remote party before answer");
        } else {
            lineObj.SipSession.data.reasonText = "Call completed elsewhere";
            console.log("Call completed elsewhere before answer");
        }
  
        lineObj.SipSession.dispose().catch(function(error){
            console.log("Failed to dispose the cancel dialog", error);
        })
  
        this.teardownSession(lineObj);
  }
  // Both Incoming an outgoing INVITE
  onInviteAccepted(lineObj:any, includeVideo:any, response:any){
    // Call in progress
    var session = lineObj.SipSession;
  
    if(session.data.earlyMedia){
        session.data.earlyMedia.pause();
        session.data.earlyMedia.removeAttribute('src');
        session.data.earlyMedia.load();
        session.data.earlyMedia = null;
    }
  
    window.clearInterval(session.data.callTimer);
  //   $("#line-" + lineObj.LineNumber + "-timer").show();
    var startTime = moment.utc();
    session.data.startTime = startTime;
    session.data.callTimer = window.setInterval(function(){
        var now = moment.utc();
        var duration = moment.duration(now.diff(startTime));
        var timeStr = this.formatShortDuration(duration.asSeconds());
      //   $("#line-" + lineObj.LineNumber + "-timer").html(timeStr);
      //   $("#line-" + lineObj.LineNumber + "-datetime").html(timeStr);
    }, 1000);
    session.isOnHold = false;
    session.data.started = true;
  
    if(includeVideo){
        // Preview our stream from peer connection
        var localVideoStream = new MediaStream();
        var pc = session.sessionDescriptionHandler.peerConnection;
        pc.getSenders().forEach((sender) =>{
            if(sender.track && sender.track.kind == "video"){
                localVideoStream.addTrack(sender.track);
            }
        });
      //  var localVideo = $("#line-" + lineObj.LineNumber + "-localVideo").get(0);
      //   localVideo.srcObject = localVideoStream;
      //   localVideo.onloadedmetadata = function(e) {
      //       localVideo.play();
      //   }
  
        // Apply Call Bandwidth Limits
        if(this.maxVideoBandwidth > -1){
            pc.getSenders().forEach((sender)=> {
                if(sender.track && sender.track.kind == "video"){
  
                    var parameters = sender.getParameters();
                    if(!parameters.encodings) parameters.encodings = [{}];
                    parameters.encodings[0].maxBitrate = this.maxVideoBandwidth * 1000;
  
                    console.log("Applying limit for Bandwidth to: ", this.maxVideoBandwidth + "kb per second")
  
                    // Only going to try without re-negotiations
                    sender.setParameters(parameters).catch(function(e){
                        console.warn("Cannot apply Bandwidth Limits", e);
                    });
  
                }
            });
        }
  
    }
  
    // Start Call Recording
    if(this.recordAllCalls || this.callRecordingPolicy == "enabled") {
      this.startRecording(lineObj.LineNumber);
    }
  
    if(includeVideo){
      // Layout for Video Call
      //   $("#line-"+ lineObj.LineNumber +"-progress").hide();
      //   $("#line-"+ lineObj.LineNumber +"-VideoCall").show();
      //   $("#line-"+ lineObj.LineNumber +"-ActiveCall").show();
  
      //   $("#line-"+ lineObj.LineNumber +"-btn-Conference").hide(); // Cannot conference a Video Call (Yet...)
      //   $("#line-"+ lineObj.LineNumber +"-btn-CancelConference").hide();
      //   $("#line-"+ lineObj.LineNumber +"-Conference").hide();
  
      //   $("#line-"+ lineObj.LineNumber +"-btn-Transfer").hide(); // Cannot transfer a Video Call (Yet...)
      //   $("#line-"+ lineObj.LineNumber +"-btn-CancelTransfer").hide();
      //   $("#line-"+ lineObj.LineNumber +"-Transfer").hide();
  
        // Default to use Camera
      //   $("#line-"+ lineObj.LineNumber +"-src-camera").prop("disabled", true);
      //   $("#line-"+ lineObj.LineNumber +"-src-canvas").prop("disabled", false);
      //   $("#line-"+ lineObj.LineNumber +"-src-desktop").prop("disabled", false);
      //   $("#line-"+ lineObj.LineNumber +"-src-video").prop("disabled", false);
    }
    else {
        // Layout for Audio Call
      //   $("#line-" + lineObj.LineNumber + "-progress").hide();
      //   $("#line-" + lineObj.LineNumber + "-VideoCall").hide();
      //   $("#line-" + lineObj.LineNumber + "-AudioCall").show();
        // Call Control
      //   $("#line-"+ lineObj.LineNumber +"-btn-Mute").show();
      //   $("#line-"+ lineObj.LineNumber +"-btn-Unmute").hide();
      //   $("#line-"+ lineObj.LineNumber +"-btn-start-recording").show();
      //   $("#line-"+ lineObj.LineNumber +"-btn-stop-recording").hide();
      //   $("#line-"+ lineObj.LineNumber +"-btn-Hold").show();
      //   $("#line-"+ lineObj.LineNumber +"-btn-Unhold").hide();
      //   $("#line-"+ lineObj.LineNumber +"-btn-Transfer").show();
      //   $("#line-"+ lineObj.LineNumber +"-btn-CancelTransfer").hide();
      //   $("#line-"+ lineObj.LineNumber +"-btn-Conference").show();
      //   $("#line-"+ lineObj.LineNumber +"-btn-CancelConference").hide();
      //   $("#line-"+ lineObj.LineNumber +"-btn-ShowDtmf").show();
      //   $("#line-"+ lineObj.LineNumber +"-btn-settings").show();
      //   $("#line-"+ lineObj.LineNumber +"-btn-ShowCallStats").show();
      //   $("#line-"+ lineObj.LineNumber +"-btn-HideCallStats").hide();
      //   $("#line-"+ lineObj.LineNumber +"-btn-ShowTimeline").show();
      //   $("#line-"+ lineObj.LineNumber +"-btn-HideTimeline").hide();
      //   $("#line-"+ lineObj.LineNumber +"-btn-present-src").hide();
      //   $("#line-"+ lineObj.LineNumber +"-btn-expand").hide();
      //   $("#line-"+ lineObj.LineNumber +"-btn-restore").hide();
      //   $("#line-"+ lineObj.LineNumber +"-btn-End").show();
        // Show the Call
      //   $("#line-" + lineObj.LineNumber + "-ActiveCall").show();
    }
  
    // Start Audio Monitoring
    lineObj.LocalSoundMeter = this.startLocalAudioMediaMonitoring(lineObj.LineNumber, session);
    lineObj.RemoteSoundMeter = this.startRemoteAudioMediaMonitoring(lineObj.LineNumber, session);
  
  //   $("#line-" + lineObj.LineNumber + "-msg").html(this.lang.call_in_progress);
  
    //if(includeVideo && this.startVideoFullScreen) this.expandVideoArea(lineObj.LineNumber);
  
  }
  
  // General end of Session
  teardownSession(lineObj:any) {
      if(lineObj == null || lineObj.SipSession == null) return;
  
      var session = lineObj.SipSession;
      if(session.data.teardownComplete == true) return;
      session.data.teardownComplete = true; // Run this code only once
  
      // Call UI
      if(session.data.earlyReject != true){
          //HidePopup();
      }
  
      // End any child calls
      if(session.data.childsession){
          session.data.childsession.dispose().then(function(){
              session.data.childsession = null;
          }).catch(function(error){
              session.data.childsession = null;
              // Suppress message
          });
      }
  
      // Mixed Tracks
      if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
          session.data.AudioSourceTrack.stop();
          session.data.AudioSourceTrack = null;
      }
      // Stop any Early Media
      if(session.data.earlyMedia){
          session.data.earlyMedia.pause();
          session.data.earlyMedia.removeAttribute('src');
          session.data.earlyMedia.load();
          session.data.earlyMedia = null;
      }
      // Stop any ringing calls
      if(session.data.ringerObj){
          session.data.ringerObj.pause();
          session.data.ringerObj.removeAttribute('src');
          session.data.ringerObj.load();
          session.data.ringerObj = null;
      }
      
      // Stop Recording if we are
      this.stopRecording(lineObj.LineNumber,true);
  
      // Audio Meters
      if(lineObj.LocalSoundMeter != null){
          lineObj.LocalSoundMeter.stop();
          lineObj.LocalSoundMeter = null;
      }
      if(lineObj.RemoteSoundMeter != null){
          lineObj.RemoteSoundMeter.stop();
          lineObj.RemoteSoundMeter = null;
      }
  
      // Make sure you have released the microphone
      if(session && session.sessionDescriptionHandler && session.sessionDescriptionHandler.peerConnection){
          var pc = session.sessionDescriptionHandler.peerConnection;
          pc.getSenders().forEach(function (RTCRtpSender) {
              if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                  RTCRtpSender.track.stop();
              }
          });
      }
  
      // End timers
      window.clearInterval(session.data.videoResampleInterval);
      window.clearInterval(session.data.callTimer);
  
      // Add to stream
      this.addCallMessage(lineObj.BuddyObj.identity, session);
  
      // Check if this call was missed
      if (session.data.calldirection == "inbound"){
          if(session.data.earlyReject){
              // Call was rejected without even ringing
              this.increaseMissedBadge(session.data.buddyId);
          } else if (session.data.terminateby == "them" && session.data.startTime == null){
              // Call Terminated by them during ringing
              if(session.data.reasonCode == 0){
                  // Call was canceled, and not answered elsewhere 
                  this.increaseMissedBadge(session.data.buddyId);
              }
          }
      }
      
      // Close up the UI
      window.setTimeout(function () {
          this.RemoveLine(lineObj);
      }, 1000);
  
  }
  
  // Mic and Speaker Levels
  // ======================
  startRemoteAudioMediaMonitoring(lineNum, session) {
      console.log("Creating RemoteAudio AudioContext on Line:" + lineNum);
  
      // Create local SoundMeter
      var soundMeter = new SoundMeter(session.id, lineNum);
      if(soundMeter == null){
          console.warn("AudioContext() RemoteAudio not available... it fine.");
          return null;
      }
  
      // Ready the getStats request
      var remoteAudioStream = new MediaStream();
      var audioReceiver = null;
      var pc = session.sessionDescriptionHandler.peerConnection;
      pc.getReceivers().forEach(function (RTCRtpReceiver) {
          if(RTCRtpReceiver.track && RTCRtpReceiver.track.kind == "audio"){
              if(audioReceiver == null) {
                  remoteAudioStream.addTrack(RTCRtpReceiver.track);
                  audioReceiver = RTCRtpReceiver;
              }
              else {
                  console.log("Found another Track, but audioReceiver not null");
                  console.log(RTCRtpReceiver);
                  console.log(RTCRtpReceiver.track);
              }
          }
      });
  
  
      // Setup Charts
      var maxDataLength = 100;
      soundMeter.startTime = Date.now();
      //Chart.defaults.global.defaultFontSize = 12;
  
      var ChatHistoryOptions = { 
          responsive: true,
          maintainAspectRatio: false,
          // devicePixelRatio: 1,
          animation: false,
          scales: {
              yAxes: [{
                  ticks: { beginAtZero: true } //, min: 0, max: 100
              }],
              xAxes: [{
                  display: false
              }]
          }, 
      }
  
      //Receive Kilobits per second
      // soundMeter.ReceiveBitRateChart = new Chart($("#line-"+ lineNum +"-AudioReceiveBitRate"), {
      //     type: 'line',
      //     data: {
      //         labels: makeDataArray("", maxDataLength),
      //         datasets: [{
      //             label: lang.receive_kilobits_per_second,
      //             data: makeDataArray(0, maxDataLength),
      //             backgroundColor: 'rgba(168, 0, 0, 0.5)',
      //             borderColor: 'rgba(168, 0, 0, 1)',
      //             borderWidth: 1,
      //             pointRadius: 1
      //         }]
      //     },
      //     options: ChatHistoryOptions
      // });
      soundMeter.ReceiveBitRateChart.lastValueBytesReceived = 0;
      soundMeter.ReceiveBitRateChart.lastValueTimestamp = 0;
  
      // Receive Packets per second
      // soundMeter.ReceivePacketRateChart = new Chart($("#line-"+ lineNum +"-AudioReceivePacketRate"), {
      //     type: 'line',
      //     data: {
      //         labels: makeDataArray("", maxDataLength),
      //         datasets: [{
      //             label: lang.receive_packets_per_second,
      //             data: makeDataArray(0, maxDataLength),
      //             backgroundColor: 'rgba(168, 0, 0, 0.5)',
      //             borderColor: 'rgba(168, 0, 0, 1)',
      //             borderWidth: 1,
      //             pointRadius: 1
      //         }]
      //     },
      //     options: ChatHistoryOptions
      // });
      soundMeter.ReceivePacketRateChart.lastValuePacketReceived = 0;
      soundMeter.ReceivePacketRateChart.lastValueTimestamp = 0;
  
      // Receive Packet Loss
      // soundMeter.ReceivePacketLossChart = new Chart($("#line-"+ lineNum +"-AudioReceivePacketLoss"), {
      //     type: 'line',
      //     data: {
      //         labels: makeDataArray("", maxDataLength),
      //         datasets: [{
      //             label: lang.receive_packet_loss,
      //             data: makeDataArray(0, maxDataLength),
      //             backgroundColor: 'rgba(168, 99, 0, 0.5)',
      //             borderColor: 'rgba(168, 99, 0, 1)',
      //             borderWidth: 1,
      //             pointRadius: 1
      //         }]
      //     },
      //     options: ChatHistoryOptions
      // });
      soundMeter.ReceivePacketLossChart.lastValuePacketLoss = 0;
      soundMeter.ReceivePacketLossChart.lastValueTimestamp = 0;
  
      // Receive Jitter
      // soundMeter.ReceiveJitterChart = new Chart($("#line-"+ lineNum +"-AudioReceiveJitter"), {
      //     type: 'line',
      //     data: {
      //         labels: makeDataArray("", maxDataLength),
      //         datasets: [{
      //             label: lang.receive_jitter,
      //             data: makeDataArray(0, maxDataLength),
      //             backgroundColor: 'rgba(0, 38, 168, 0.5)',
      //             borderColor: 'rgba(0, 38, 168, 1)',
      //             borderWidth: 1,
      //             pointRadius: 1
      //         }]
      //     },
      //     options: ChatHistoryOptions
      // });
  
      // Receive Audio Levels
      // soundMeter.ReceiveLevelsChart = new Chart($("#line-"+ lineNum +"-AudioReceiveLevels"), {
      //     type: 'line',
      //     data: {
      //         labels: makeDataArray("", maxDataLength),
      //         datasets: [{
      //             label: lang.receive_audio_levels,
      //             data: makeDataArray(0, maxDataLength),
      //             backgroundColor: 'rgba(140, 0, 168, 0.5)',
      //             borderColor: 'rgba(140, 0, 168, 1)',
      //             borderWidth: 1,
      //             pointRadius: 1
      //         }]
      //     },
      //     options: ChatHistoryOptions
      // });
  
      // Connect to Source
      soundMeter.connectToSource(remoteAudioStream, function (e) {
          if (e != null) return;
  
          // Create remote SoundMeter
          console.log("SoundMeter for RemoteAudio Connected, displaying levels for Line: " + lineNum);
          soundMeter.levelsInterval = window.setInterval(function () {
              // Calculate Levels (0 - 255)
              var instPercent = (soundMeter.instant/255) * 100;
              // $("#line-" + lineNum + "-Speaker").css("height", instPercent.toFixed(2) +"%");
          }, 50);
          soundMeter.networkInterval = window.setInterval(function (){
              // Calculate Network Conditions
              if(audioReceiver != null) {
                  audioReceiver.getStats().then(function(stats) {
                      stats.forEach(function(report){
  
                          var theMoment = this.utcDateNow();
                          var ReceiveBitRateChart = soundMeter.ReceiveBitRateChart;
                          var ReceivePacketRateChart = soundMeter.ReceivePacketRateChart;
                          var ReceivePacketLossChart = soundMeter.ReceivePacketLossChart;
                          var ReceiveJitterChart = soundMeter.ReceiveJitterChart;
                          var ReceiveLevelsChart = soundMeter.ReceiveLevelsChart;
                          var elapsedSec = Math.floor((Date.now() - soundMeter.startTime)/1000);
  
                          if(report.type == "inbound-rtp"){
  
                              if(ReceiveBitRateChart.lastValueTimestamp == 0) {
                                  ReceiveBitRateChart.lastValueTimestamp = report.timestamp;
                                  ReceiveBitRateChart.lastValueBytesReceived = report.bytesReceived;
  
                                  ReceivePacketRateChart.lastValueTimestamp = report.timestamp;
                                  ReceivePacketRateChart.lastValuePacketReceived = report.packetsReceived;
  
                                  ReceivePacketLossChart.lastValueTimestamp = report.timestamp;
                                  ReceivePacketLossChart.lastValuePacketLoss = report.packetsLost;
  
                                  return;
                              }
                              // Receive Kilobits Per second
                              var kbitsPerSec = (8 * (report.bytesReceived - ReceiveBitRateChart.lastValueBytesReceived))/1000;
  
                              ReceiveBitRateChart.lastValueTimestamp = report.timestamp;
                              ReceiveBitRateChart.lastValueBytesReceived = report.bytesReceived;
  
                              soundMeter.ReceiveBitRate.push({ value: kbitsPerSec, timestamp : theMoment});
                              ReceiveBitRateChart.data.datasets[0].data.push(kbitsPerSec);
                              ReceiveBitRateChart.data.labels.push("");
                              if(ReceiveBitRateChart.data.datasets[0].data.length > maxDataLength) {
                                  ReceiveBitRateChart.data.datasets[0].data.splice(0,1);
                                  ReceiveBitRateChart.data.labels.splice(0,1);
                              }
                              ReceiveBitRateChart.update();
  
                              // Receive Packets Per Second
                              var PacketsPerSec = (report.packetsReceived - ReceivePacketRateChart.lastValuePacketReceived);
  
                              ReceivePacketRateChart.lastValueTimestamp = report.timestamp;
                              ReceivePacketRateChart.lastValuePacketReceived = report.packetsReceived;
  
                              soundMeter.ReceivePacketRate.push({ value: PacketsPerSec, timestamp : theMoment});
                              ReceivePacketRateChart.data.datasets[0].data.push(PacketsPerSec);
                              ReceivePacketRateChart.data.labels.push("");
                              if(ReceivePacketRateChart.data.datasets[0].data.length > maxDataLength) {
                                  ReceivePacketRateChart.data.datasets[0].data.splice(0,1);
                                  ReceivePacketRateChart.data.labels.splice(0,1);
                              }
                              ReceivePacketRateChart.update();
  
                              // Receive Packet Loss
                              var PacketsLost = (report.packetsLost - ReceivePacketLossChart.lastValuePacketLoss);
  
                              ReceivePacketLossChart.lastValueTimestamp = report.timestamp;
                              ReceivePacketLossChart.lastValuePacketLoss = report.packetsLost;
  
                              soundMeter.ReceivePacketLoss.push({ value: PacketsLost, timestamp : theMoment});
                              ReceivePacketLossChart.data.datasets[0].data.push(PacketsLost);
                              ReceivePacketLossChart.data.labels.push("");
                              if(ReceivePacketLossChart.data.datasets[0].data.length > maxDataLength) {
                                  ReceivePacketLossChart.data.datasets[0].data.splice(0,1);
                                  ReceivePacketLossChart.data.labels.splice(0,1);
                              }
                              ReceivePacketLossChart.update();
  
                              // Receive Jitter
                              soundMeter.ReceiveJitter.push({ value: report.jitter, timestamp : theMoment});
                              ReceiveJitterChart.data.datasets[0].data.push(report.jitter);
                              ReceiveJitterChart.data.labels.push("");
                              if(ReceiveJitterChart.data.datasets[0].data.length > maxDataLength) {
                                  ReceiveJitterChart.data.datasets[0].data.splice(0,1);
                                  ReceiveJitterChart.data.labels.splice(0,1);
                              }
                              ReceiveJitterChart.update();
                          }
                          if(report.type == "track") {
  
                              // Receive Audio Levels
                              var levelPercent = (report.audioLevel * 100);
                              soundMeter.ReceiveLevels.push({ value: levelPercent, timestamp : theMoment});
                              ReceiveLevelsChart.data.datasets[0].data.push(levelPercent);
                              ReceiveLevelsChart.data.labels.push("");
                              if(ReceiveLevelsChart.data.datasets[0].data.length > maxDataLength)
                              {
                                  ReceiveLevelsChart.data.datasets[0].data.splice(0,1);
                                  ReceiveLevelsChart.data.labels.splice(0,1);
                              }
                              ReceiveLevelsChart.update();
                          }
                      });
                  });
              }
          } ,1000);
      });
  
      return soundMeter;
  }
  startLocalAudioMediaMonitoring(lineNum, session) {
      console.log("Creating LocalAudio AudioContext on line " + lineNum);
  
      // Create local SoundMeter
      var soundMeter = new SoundMeter(session.id, lineNum);
      if(soundMeter == null){
          console.warn("AudioContext() LocalAudio not available... its fine.")
          return null;
      }
  
      // Ready the getStats request
      var localAudioStream = new MediaStream();
      var audioSender = null;
      var pc = session.sessionDescriptionHandler.peerConnection;
      pc.getSenders().forEach(function (RTCRtpSender) {
          if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio"){
              if(audioSender == null){
                  console.log("Adding Track to Monitor: ", RTCRtpSender.track.label);
                  localAudioStream.addTrack(RTCRtpSender.track);
                  audioSender = RTCRtpSender;
              }
              else {
                  console.log("Found another Track, but audioSender not null");
                  console.log(RTCRtpSender);
                  console.log(RTCRtpSender.track);
              }
          }
      });
  
      // Setup Charts
      var maxDataLength = 100;
      soundMeter.startTime = Date.now();
     // Chart.defaults.global.defaultFontSize = 12;
      var ChatHistoryOptions = { 
          responsive: true,    
          maintainAspectRatio: false,
          // devicePixelRatio: 1,
          animation: false,
          scales: {
              yAxes: [{
                  ticks: { beginAtZero: true }
              }],
              xAxes: [{
                  display: false
              }]
          }, 
      }
  
      // Send Kilobits Per Second
      // soundMeter.SendBitRateChart = new Chart($("#line-"+ lineNum +"-AudioSendBitRate"), {
      //     type: 'line',
      //     data: {
      //         labels: makeDataArray("", maxDataLength),
      //         datasets: [{
      //             label: lang.send_kilobits_per_second,
      //             data: makeDataArray(0, maxDataLength),
      //             backgroundColor: 'rgba(0, 121, 19, 0.5)',
      //             borderColor: 'rgba(0, 121, 19, 1)',
      //             borderWidth: 1,
      //             pointRadius: 1
      //         }]
      //     },
      //     options: ChatHistoryOptions
      // });
      soundMeter.SendBitRateChart.lastValueBytesSent = 0;
      soundMeter.SendBitRateChart.lastValueTimestamp = 0;
  
      // // Send Packets Per Second
      // soundMeter.SendPacketRateChart = new Chart($("#line-"+ lineNum +"-AudioSendPacketRate"), {
      //     type: 'line',
      //     data: {
      //         labels: makeDataArray("", maxDataLength),
      //         datasets: [{
      //             label: lang.send_packets_per_second,
      //             data: makeDataArray(0, maxDataLength),
      //             backgroundColor: 'rgba(0, 121, 19, 0.5)',
      //             borderColor: 'rgba(0, 121, 19, 1)',
      //             borderWidth: 1,
      //             pointRadius: 1
      //         }]
      //     },
      //     options: ChatHistoryOptions
      // });
      soundMeter.SendPacketRateChart.lastValuePacketSent = 0;
      soundMeter.SendPacketRateChart.lastValueTimestamp = 0;    
  
      // Connect to Source
      soundMeter.connectToSource(localAudioStream, function (e) {
          if (e != null) return;
  
          console.log("SoundMeter for LocalAudio Connected, displaying levels for Line: " + lineNum);
          soundMeter.levelsInterval = window.setInterval(function () {
              // Calculate Levels (0 - 255)
              var instPercent = (soundMeter.instant/255) * 100;
              // $("#line-" + lineNum + "-Mic").css("height", instPercent.toFixed(2) +"%");
          }, 50);
          soundMeter.networkInterval = window.setInterval(function (){
              // Calculate Network Conditions
              // Sending Audio Track
              if(audioSender != null) {
                  audioSender.getStats().then(function(stats) {
                      stats.forEach(function(report){
  
                          var theMoment = this.utcDateNow();
                          var SendBitRateChart = soundMeter.SendBitRateChart;
                          var SendPacketRateChart = soundMeter.SendPacketRateChart;
                          var elapsedSec = Math.floor((Date.now() - soundMeter.startTime)/1000);
  
                          if(report.type == "outbound-rtp"){
                              if(SendBitRateChart.lastValueTimestamp == 0) {
                                  SendBitRateChart.lastValueTimestamp = report.timestamp;
                                  SendBitRateChart.lastValueBytesSent = report.bytesSent;
  
                                  SendPacketRateChart.lastValueTimestamp = report.timestamp;
                                  SendPacketRateChart.lastValuePacketSent = report.packetsSent;
                                  return;
                              }
  
                              // Send Kilobits Per second
                              var kbitsPerSec = (8 * (report.bytesSent - SendBitRateChart.lastValueBytesSent))/1000;
  
                              SendBitRateChart.lastValueTimestamp = report.timestamp;
                              SendBitRateChart.lastValueBytesSent = report.bytesSent;
  
                              soundMeter.SendBitRate.push({ value: kbitsPerSec, timestamp : theMoment});
                              SendBitRateChart.data.datasets[0].data.push(kbitsPerSec);
                              SendBitRateChart.data.labels.push("");
                              if(SendBitRateChart.data.datasets[0].data.length > maxDataLength) {
                                  SendBitRateChart.data.datasets[0].data.splice(0,1);
                                  SendBitRateChart.data.labels.splice(0,1);
                              }
                              SendBitRateChart.update();
  
                              // Send Packets Per Second
                              var PacketsPerSec = report.packetsSent - SendPacketRateChart.lastValuePacketSent;
  
                              SendPacketRateChart.lastValueTimestamp = report.timestamp;
                              SendPacketRateChart.lastValuePacketSent = report.packetsSent;
  
                              soundMeter.SendPacketRate.push({ value: PacketsPerSec, timestamp : theMoment});
                              SendPacketRateChart.data.datasets[0].data.push(PacketsPerSec);
                              SendPacketRateChart.data.labels.push("");
                              if(SendPacketRateChart.data.datasets[0].data.length > maxDataLength) {
                                  SendPacketRateChart.data.datasets[0].data.splice(0,1);
                                  SendPacketRateChart.data.labels.splice(0,1);
                              }
                              SendPacketRateChart.update();
                          }
                          if(report.type == "track") {
                              // Bug/security concern... this seems always to report "0"
                              // Possible reason: When applied to isolated streams, media metrics may allow an application to infer some characteristics of the isolated stream, such as if anyone is speaking (by watching the audioLevel statistic).
                              // console.log("Audio Sender: " + report.audioLevel);
                          }
                      });
                  });
              }
          } ,1000);
      });
  
      return soundMeter;
  }
  
  
  meterSettingsOutput(audioStream, objectId, direction, interval){
      var soundMeter = new SoundMeter(null, null);
      soundMeter.startTime = Date.now();
      soundMeter.connectToSource(audioStream, function (e) {
          if (e != null) return;
  
          console.log("SoundMeter Connected, displaying levels to:"+ objectId);
          soundMeter.levelsInterval = window.setInterval(function () {
              // Calculate Levels (0 - 255)
              var instPercent = (soundMeter.instant/255) * 100;
              // $("#"+ objectId).css(direction, instPercent.toFixed(2) +"%");
          }, interval);
      });
  
      return soundMeter;
  }
  
  
  
  // Outgoing INVITE
  onInviteTrying(lineObj:any, response:any){
  //   $("#line-" + lineObj.LineNumber + "-msg").html(this.lang.trying);
  }
  onInviteProgress(lineObj:any, response:any){
    console.log("Call Progress:", response.message.statusCode);
    
    // Provisional 1xx
    // response.message.reasonPhrase
    if(response.message.statusCode == 180){
      //   $("#line-" + lineObj.LineNumber + "-msg").html(this.lang.ringing);
        
        var soundFile = this.audioBlobs.EarlyMedia_European;
        if(this.userLocale().indexOf("us") > -1) soundFile = this.audioBlobs.EarlyMedia_US;
        if(this.userLocale().indexOf("gb") > -1) soundFile = this.audioBlobs.EarlyMedia_UK;
        if(this.userLocale().indexOf("au") > -1) soundFile = this.audioBlobs.EarlyMedia_Australia;
        if(this.userLocale().indexOf("jp") > -1) soundFile = this.audioBlobs.EarlyMedia_Japan;
  
        // Play Early Media
        console.log("Audio:", soundFile.url);
        if(lineObj.SipSession.data.earlyMedia){
            // There is already early media playing
            // onProgress can be called multiple times
            // Don't add it again
            console.log("Early Media already playing");
        }
        else {
            var earlyMedia = new Audio(soundFile.blob);
            earlyMedia.preload = "auto";
            earlyMedia.loop = true;
            earlyMedia.oncanplaythrough = function(e) {
              //   if (typeof earlyMedia.sinkId !== 'undefined' && this.getAudioOutputID() != "default") {
              //       earlyMedia.setSinkId(this.getAudioOutputID()).then(function() {
              //           console.log("Set sinkId to:", this.getAudioOutputID());
              //       }).catch(function(e){
              //           console.warn("Failed not apply setSinkId.", e);
              //       });
              //   }
              //   earlyMedia.play().then(function(){
              //       // Audio Is Playing
              //   }).catch(function(e){
              //       console.warn("Unable to play audio file.", e);
              //   }); 
            }
            lineObj.SipSession.data.earlyMedia = earlyMedia;
        }
    }
    else if(response.message.statusCode === 183){
      //   $("#line-" + lineObj.LineNumber + "-msg").html(response.message.reasonPhrase + "...");
  
        // Add UI to allow DTMF
      //   $("#line-" + lineObj.LineNumber + "-early-dtmf").show();
    }
    else {
        // 181 = Call is Being Forwarded
        // 182 = Call is queued (Busy server!)
        // 199 = Call is Terminated (Early Dialog)
  
      //   $("#line-" + lineObj.LineNumber + "-msg").html(response.message.reasonPhrase + "...");
    }
  }
  onInviteRejected(lineObj:any, response:any){
    console.log("INVITE Rejected:", response.message.reasonPhrase);
  
    lineObj.SipSession.data.terminateby = "them";
    lineObj.SipSession.data.reasonCode = response.message.statusCode;
    lineObj.SipSession.data.reasonText = response.message.reasonPhrase;
  
    this.teardownSession(lineObj);
  }
  
  onInviteRedirected(response:any){
    console.log("onInviteRedirected", response);
    // Follow???
  }
  
  
  //XMPP
  reconnectXmpp = function(){
    console.log("Connect/Reconnect XMPP connection...");
  
    if(this.XMPP) this.XMPP.disconnect("");
    if(this.XMPP) this.XMPP.reset();
  
    var xmpp_websocket_uri = "wss://"+ this.xmppServer +":"+ this.xmppwebSocketPort +""+ this.xmppWebsocketPath; 
    var xmpp_username = this.profileUser +"@"+ this.xmppDomain;
    if(this.xmppRealm != "" && this.xmppRealmSeparator) xmpp_username = this.xmppRealm + this.xmppRealmSeparator + xmpp_username;
    var xmpp_password = this.sipPassword;
  
    this.XMPP = null;
    if(this.xmppDomain == "" || this.xmppServer == "" || this.xmppwebSocketPort == "" || this.xmppWebsocketPath == ""){
        console.log("Cannot connect to XMPP: ", this.xmppDomain, this.xmppServer, this.xmppwebSocketPort, this.xmppWebsocketPath);
        return;
    }
    this.XMPP = new Strophe.Connection(xmpp_websocket_uri);
  
    // XMPP.rawInput = function(data){
    //     console.log('RECV:', data);
    // }
    // XMPP.rawOutput = function(data){
    //     console.log('SENT:', data);
    // }
  
    // Information Query
    this.XMPP.addHandler(this.onPingRequest, "urn:xmpp:ping", "iq", "get");
    this.XMPP.addHandler(this.onVersionRequest, "jabber:iq:version", "iq", "get");
  
    // Presence
    this.XMPP.addHandler(this.onPresenceChange, null, "presence", null);
    // Message
    this.XMPP.addHandler(this.onMessage, null, "message", null);
  
    console.log("XMPP connect...");
  
    this.XMPP.connect(xmpp_username, xmpp_password, this.onStatusChange);
  }
  
  // XMPP Messaging
  // ==============
  onMessage(message:any){
    // console.log('onMessage', message);
  
    var from = message.getAttribute("from");
    var fromJid = Strophe.getBareJidFromJid(from);
    var to = message.getAttribute("to");
    var messageId = message.getAttribute("id");
  
    // Determin Buddy
    var buddyObj:any = "this.FindBuddyByJid(fromJid)";
    if(buddyObj == null) {
        // You don't appear to be a buddy of mine
  
        // TODO: Handle this
        console.warn("Spam!"); // LOL :)
        return true;
    }
  
    var isDelayed = false;
    var DateTime = this.utcDateNow();
    Strophe.forEachChild(message, "delay", function(elem) {
        // Delay message received
        if(elem.getAttribute("xmlns") == "urn:xmpp:delay"){
            isDelayed = true;
            DateTime = moment.utc().format("YYYY-MM-DD HH:mm:ss UTC");
        }
    });
     var originalMessage = "";
    Strophe.forEachChild(message, "body", function(elem) {
        // For simplicity, this code is assumed to take the last body
        originalMessage = elem.textContent;
    });
  
  
    // chatstate
    var chatstate = "";
    Strophe.forEachChild(message, "composing", function(elem) {
        if(elem.getAttribute("xmlns") == "http://jabber.org/protocol/chatstates"){
            chatstate = "composing";
        }
    });
    Strophe.forEachChild(message, "paused", function(elem) {
        if(elem.getAttribute("xmlns") == "http://jabber.org/protocol/chatstates"){
            chatstate = "paused";
        }
    });
    Strophe.forEachChild(message, "active", function(elem) {
        if(elem.getAttribute("xmlns") == "http://jabber.org/protocol/chatstates"){
            chatstate = "active";
        }
    });
    if(chatstate == "composing"){
        if(!isDelayed) this.XmppShowComposing(buddyObj);
        return true;
    }
    else {
        this.XmppHideComposing(buddyObj);
    }
  
    // Message Correction
    var isCorrection = false;
    var targetCorrectionMsg = "";
    Strophe.forEachChild(message, "replace", function(elem) {
        if(elem.getAttribute("xmlns") == "urn:xmpp:message-correct:0"){
            isCorrection = true;
            Strophe.forEachChild(elem, "id", function(idElem) {
                targetCorrectionMsg = idElem.textContent;
            });
        }
    });
    if(isCorrection && targetCorrectionMsg != "") {
        console.log("Message "+ targetCorrectionMsg +" for "+ buddyObj.CallerIDName +" was corrected");
        //this.CorrectMessage(buddyObj, targetCorrectionMsg, originalMessage);
    }
  
    // Delivery Events
    var eventStr = "";
    var targetDeliveryMsg = "";
    Strophe.forEachChild(message, "x", function(elem) {
        if(elem.getAttribute("xmlns") == "jabber:x:event"){
            // One of the delivery events occured
            Strophe.forEachChild(elem, "delivered", function(delElem) {
                eventStr = "delivered";
            });
            Strophe.forEachChild(elem, "displayed", function(delElem) {
                eventStr = "displayed";
            });
            Strophe.forEachChild(elem, "id", function(idElem) {
                targetDeliveryMsg = idElem.textContent;
            });
        }
    });
    if(eventStr == "delivered" && targetDeliveryMsg != "") {
        console.log("Message "+ targetDeliveryMsg +" for "+ buddyObj.CallerIDName +" was delivered");
        this.markDeliveryReceipt(buddyObj, targetDeliveryMsg, true);
  
        return true;
    }
    if(eventStr == "displayed" && targetDeliveryMsg != "") {
        console.log("Message "+ targetDeliveryMsg +" for "+ buddyObj.CallerIDName +" was displayed");
        this.markDisplayReceipt(buddyObj, targetDeliveryMsg, true);
  
        return true;
    }
  
    // Messages
    if(originalMessage == ""){
        // Not a full message
    }
    else {
        if(messageId) {
            // Although XMPP does not require message ID's, this application does
            this.XmppSendDeliveryReceipt(buddyObj, messageId);
  
            this.addMessageToStream(buddyObj, messageId, "MSG", originalMessage, DateTime)
            //this.UpdateBuddyActivity(buddyObj.identity);
            var streamVisible:any =' $("#stream-"+ buddyObj.identity).is(":visible")';
            if (streamVisible) {
              this.markMessageRead(buddyObj, messageId);
              this.XmppSendDisplayReceipt(buddyObj, messageId);
            }
            this.refreshStream(buddyObj,"");
            this.activateStream(buddyObj, originalMessage);
        }
        else {
            console.warn("Sorry, messages must have an id ", message)
        }
    }
  
    return true;
  }
  
  XmppShowComposing(buddyObj:any){
    console.log("Buddy is composing a message...");
  //   $("#contact-"+ buddyObj.identity +"-chatstate").show();
  //   $("#contact-"+ buddyObj.identity +"-presence").hide();
  //   $("#contact-"+ buddyObj.identity +"-presence-main").hide();
  //   $("#contact-"+ buddyObj.identity +"-chatstate-menu").show();
  //   $("#contact-"+ buddyObj.identity +"-chatstate-main").show();
  
    //this.updateScroll(buddyObj.identity);
  }
  
  
  XmppHideComposing(buddyObj:any){
    console.log("Buddy composing is done...");
  //   $("#contact-"+ buddyObj.identity +"-chatstate").hide();
  //   $("#contact-"+ buddyObj.identity +"-chatstate-menu").hide();
  //   $("#contact-"+ buddyObj.identity +"-chatstate-main").hide();
  //   $("#contact-"+ buddyObj.identity +"-presence").show();
  //   $("#contact-"+ buddyObj.identity +"-presence-main").show();
  
    //this.updateScroll(buddyObj.identity);
  }
  
  
  XmppSendMessage(buddyObj:any,message:any, messageId:any, thread:any, markable:any, type:any){
    if(!this.XMPP || this.XMPP.connected == false) {
        console.warn("XMPP not connected");
        return;
    }
  
    if(!type) type = "normal"; // chat | error | normal | groupchat | headline
    var msg = $msg({"to": buddyObj.jid, "type": type, "id" : messageId, "from" : this.XMPP.jid})
    if(thread && thread != ""){
        msg.c("thread").t(thread);
        msg.up();
    }
    msg.c("body").t(message); 
    // XHTML-IM
    msg.up();
    msg.c("active", {"xmlns": "http://jabber.org/protocol/chatstates"});
    msg.up();
    msg.c("x", {"xmlns": "jabber:x:event"});
    msg.c("delivered");
    msg.up();
    msg.c("displayed");
  
    console.log("sending message...");
    buddyObj.chatstate = "active";
    if(buddyObj.chatstateTimeout){
        window.clearTimeout(buddyObj.chatstateTimeout);
    }
    buddyObj.chatstateTimeout = null;
  
    try{
      this.XMPP.send(msg);
      this.markMessageSent(buddyObj, messageId, false);
    }
    catch(e){
      this.markMessageNotSent(buddyObj, messageId, false);
    }
  }
  
  XmppStartComposing(buddyObj:any, thread:any){
    if(!this.XMPP || this.XMPP.connected == false) {
        console.warn("XMPP not connected");
        return;
    }
  
    if(buddyObj.jid == null || buddyObj.jid == "") return;
  
    if(buddyObj.chatstateTimeout){
        window.clearTimeout(buddyObj.chatstateTimeout);
    }
    buddyObj.chatstateTimeout = window.setTimeout(function(){
      this.XmppPauseComposing(buddyObj, thread);
    }, 10 * 1000);
  
    if(buddyObj.chatstate && buddyObj.chatstate == "composing") return;
  
    var msg = $msg({"to": buddyObj.jid, "from" : this.XMPP.jid})
    if(thread && thread != ""){
        msg.c("thread").t(thread);
        msg.up();
    }
    msg.c("composing", {"xmlns": "http://jabber.org/protocol/chatstates"});
  
    console.log("you are composing a message...")
    buddyObj.chatstate = "composing";
  
    this.XMPP.send(msg);
  }
  
  XmppPauseComposing(buddyObj:any, thread:any){
    if(!this.XMPP || this.XMPP.connected == false) {
        console.warn("XMPP not connected");
        return;
    }
  
    if(buddyObj.jid == null || buddyObj.jid == "") return;
  
    if(buddyObj.chatstate && buddyObj.chatstate == "paused") return;
  
    var msg = $msg({"to": buddyObj.jid, "from" : this.XMPP.jid})
    if(thread && thread != ""){
        msg.c("thread").t(thread);
        msg.up();
    }
    msg.c("paused", {"xmlns": "http://jabber.org/protocol/chatstates"});
  
    console.log("You have paused your message...");
    buddyObj.chatstate = "paused";
    if(buddyObj.chatstateTimeout){
        window.clearTimeout(buddyObj.chatstateTimeout);
    }
    buddyObj.chatstateTimeout = null;
  
    this.XMPP.send(msg);
  }
  
  XmppSendDeliveryReceipt(buddyObj:any, id:any){
    if(!this.XMPP || this.XMPP.connected == false) {
        console.warn("XMPP not connected");
        return;
    }
  
    var msg = $msg({"to": buddyObj.jid, "from" : this.XMPP.jid});
    msg.c("x", {"xmlns": "jabber:x:event"});
    msg.c("delivered");
    msg.up();
    msg.c("id").t(id);
  
    console.log("sending delivery notice for "+ id +"...");
  
    this.XMPP.send(msg);
  }
  
  XmppSendDisplayReceipt(buddyObj:any, id:any){
    if(!this.XMPP || this.XMPP.connected == false) {
        console.warn("XMPP not connected");
        return;
    }
  
    var msg = $msg({"to": buddyObj.jid, "from" : this.XMPP.jid});
    msg.c("x", {"xmlns": "jabber:x:event"});
    msg.c("displayed");
    msg.up();
    msg.c("id").t(id);
  
    console.log("sending display notice for "+ id +"...");
  
    this.XMPP.send(msg);
  }
  
  // XMPP Other
  // ==========
  onPingRequest(iq:any){
    // Handle Ping Pong
    // <iq type="get" id="86-14" from="localhost" to="websocketuser@localhost/cc9fd219" >
    //     <ping xmlns="urn:xmpp:ping"/>
    // </iq>
    var id = iq.getAttribute("id");
    var to = iq.getAttribute("to");
    var from = iq.getAttribute("from");
  
    var iq_response = $iq({'type':'result', 'id':id, 'to':from, 'from':to});
    this.XMPP.send(iq_response);
  
    return true;
  }
  
  onVersionRequest(iq){
    // Handle Request for our version etc
    // <iq xmlns="jabber:client" type="get" id="419-24" to=".../..." from="innovateasterisk.com">
    //     <query xmlns="jabber:iq:version"/>
    // </iq>
    var id = iq.getAttribute("id");
    var to = iq.getAttribute("to");
    var from = iq.getAttribute("from");
  
    var iq_response = $iq({'type':'result', 'id':id, 'to':from, 'from':to});
    iq_response.c('query', {'xmlns':'jabber:iq:version'});
    iq_response.c('name', null, 'Browser Phone');
    iq_response.c('version', null, '0.0.1');
    iq_response.c('os', null, 'Browser');
    this.XMPP.send(iq_response);
  
    return true;
  }
  
  
  onInfoQuery(iq:any){
    console.log('onInfoQuery', iq);
  
    // Probably a result
    return true;
  }
  
  onInfoQueryRequest(iq:any){
    console.log('onInfoQueryRequest', iq);
  
    var query = ""; // xml.find("iq").find("query").attr("xmlns");
    Strophe.forEachChild(iq, "query", function(elem) {
        query = elem.getAttribute("xmlns");
    });
    console.log(query);
  
    // ??
    return true;
  }
  
  onInfoQueryCommand(iq:any){
    console.log('onInfoQueryCommand', iq);
  
    var query = ""; // xml.find("iq").find("query").attr("xmlns");
    Strophe.forEachChild(iq, "query", function(elem) {
        query = elem.getAttribute("xmlns");
    });
    console.log(query);
  
    // ??
    return true;
  }
  
  XMPP_GetGroups(){
    var iq_request = $iq({"type" : "get", "id" : this.XMPP.getUniqueId(), "to" : this.xmppChatGroupService +"."+ this.xmppDomain, "from" : this.XMPP.jid});
    iq_request.c("query", {"xmlns" : "http://jabber.org/protocol/disco#items", "node" : "http://jabber.org/protocol/muc#rooms"});
  
    this.XMPP.sendIQ(iq_request, (result)=>{
        console.log("GetGroups Response: ", result);
    }, function(e){
        console.warn("Error in GetGroups", e);
    }, 30 * 1000);
  }
  
  XMPP_GetGroupMembers(){
    var iq_request = $iq({"type" : "get", "id" : this.XMPP.getUniqueId(), "to" : "directors@"+ this.xmppChatGroupService +"."+ this.xmppDomain, "from" : this.XMPP.jid});
    iq_request.c("query", {"xmlns":"http://jabber.org/protocol/disco#items"});
  
    this.XMPP.sendIQ(iq_request, (result)=>{
        console.log("GetGroupMembers Response: ", result);
    }, function(e){
        console.warn("Error in GetGroupMembers", e);
    }, 30 * 1000);
  }
  
  XMPP_JoinGroup(){
    var pres_request = $pres({"id" : this.XMPP.getUniqueId(), "from" : this.XMPP.jid, "to" : "directors@"+ this.xmppChatGroupService +"."+ this.xmppDomain +"/nickname" });
    pres_request.c("x", {"xmlns" : "http://jabber.org/protocol/muc" });
  
    this.XMPP.sendPresence(pres_request, (result)=>{
        console.log("JoinGroup Response: ", result);
    }, function(e){
        console.warn("Error in Set Presence", e);
    }, 30 * 1000);
  }
  
  XMPP_QueryMix(){
    var iq_request = $iq({"type" : "get", "id" : this.XMPP.getUniqueId(), "from" : this.XMPP.jid});
    iq_request.c("query", {"xmlns" : "http://jabber.org/protocol/disco#info"});
  
    this.XMPP.sendIQ(iq_request, (result)=>{
        console.log("XMPP_QueryMix Response: ", result);
    }, function(e){
        console.warn("Error in XMPP_QueryMix", e);
    }, 30 * 1000);
  }
  
  // Device Detection
  // ================
  detectDevices() {
      console.log('detectDevices');
      navigator.mediaDevices.enumerateDevices().then((deviceInfos:any)=>{
        // deviceInfos will not have a populated lable unless to accept the permission
        // during getUserMedia. This normally happens at startup/setup
        // so from then on these devices will be with lables.
        console.log('inside navigator');
        this.hasVideoDevice = false;
        this.hasAudioDevice = false;
        this.hasSpeakerDevice = false; // Safari and Firefox don't have these
        this.audioinputDevices = [];
        this.videoinputDevices = [];
        this.speakerDevices = [];
        console.log('deviceInfos',deviceInfos);
        for (var i = 0; i < deviceInfos.length; ++i) {
            if (deviceInfos[i].kind === "audioinput") {
              this.hasAudioDevice = true;
              this.audioinputDevices.push(deviceInfos[i]);
            } 
            else if (deviceInfos[i].kind === "audiooutput") {
              this.hasSpeakerDevice = true;
              this.speakerDevices.push(deviceInfos[i]);
            }
            else if (deviceInfos[i].kind === "videoinput") {
                if(this.enableVideoCalling == true){
                  this.hasVideoDevice = true;
                  this.videoinputDevices.push(deviceInfos[i]);
                }
            }
        }
        // console.log(audioinputDevices, videoinputDevices);
    }).catch(function(e){
        console.error("Error enumerating devices", e);
    });
  }
  
  
  // =================================================================================
  
  onStatusChange(status:any) {
    Strophe.ConnectionStatus = status;
    if (status == Strophe.Status.CONNECTING) {
        console.log('XMPP is connecting...');
    } 
    else if (status == Strophe.Status.CONNFAIL) {
        console.warn('XMPP failed to connect.');
    } 
    else if (status == Strophe.Status.DISCONNECTING) {
        console.log('XMPP is disconnecting.');
    } 
    else if (status == Strophe.Status.DISCONNECTED) {
        console.log('XMPP is disconnected.');
        
        // Keep connected
        window.setTimeout(function(){
            // reconnectXmpp();
        }, 5 * 1000);
    } 
    else if (status == Strophe.Status.CONNECTED) {
        console.log('XMPP is connected!');
  
        // Re-publish my vCard
        this.XmppSetMyVcard();
  
        // Get buddies
        this.XmppGetBuddies();
  
        this.XMPP.ping = window.setTimeout(function(){
          this.XmppSendPing();
        }, 45 * 1000);
    }
    else {
        console.log('XMPP is: ', Strophe.Status);
    }
  }
  
  XmppSendPing(){
    // return;
  
    if(!this.XMPP || this.XMPP.connected == false) this.reconnectXmpp();
  
    var iq_request = $iq({"type":"get", "id":this.XMPP.getUniqueId(), "to":this.xmppDomain, "from":this.XMPP.jid});
    iq_request.c("ping", {"xmlns":"urn:xmpp:ping"});
  
    this.XMPP.sendIQ(iq_request, (result)=>{
        // console.log("XmppSendPing Response: ", result);
    }, function(e){
        console.warn("Error in Ping", e);
    }, 30 * 1000);
  
    this.XMPP.ping = window.setTimeout(function(){
      this.XmppSendPing();
    }, 45 * 1000);
    // TODO: Make this is a setting
  }
  
  // XMPP Presence
  // =============
  XmppSetMyPresence(str:any, desc:any, updateVcard:any){
    if(!this.XMPP || this.XMPP.connected == false) {
        console.warn("XMPP not connected");
        return;
    }
  
    // ["away", "chat", "dnd", "xa"] => ["Away", "Available", "Busy", "Gone"]
  
    console.log("Setting My Own Presence to: "+ str + "("+ desc +")");
  
    if(desc == "") desc = this.lang.default_status;
  //   $("#regStatus").html("<i class=\"fa fa-comments\"></i> "+ desc);
  
    var pres_request = $pres({"id": this.XMPP.getUniqueId(), "from": this.XMPP.jid });
    pres_request.c("show").t(str);
    // if(desc && desc != ""){
    //     pres_request.root();
    //     pres_request.c("status").t(desc);
    // }
    // if(updateVcard == true){
    //     var base64 = this.getPicture("profilePicture");
    //     var imgBase64 = base64.split(",")[1];
    //     var photoHash = $.md5(imgBase64);
  
    //     pres_request.root();
    //     pres_request.c("x", {"xmlns": "vcard-temp:x:update"});
    //     if(photoHash){
    //         pres_request.c("photo", {}, photoHash);
    //     }
    //}
  
    this.XMPP.sendPresence(pres_request, (result)=>{
        // console.log("XmppSetMyPresence Response: ", result);
    }, function(e){
        console.warn("Error in XmppSetMyPresence", e);
    }, 30 * 1000);
  }
  
  onPresenceChange(presence:any) {
    // console.log('onPresenceChange', presence);
  
    var from = presence.getAttribute("from");
    var to = presence.getAttribute("to");
  
    var subscription = presence.getAttribute("subscription");
    var type = (presence.getAttribute("type"))? presence.getAttribute("type") : "presence"; // subscribe | subscribed | unavailable
    var pres = "";
    var status = "";
    var xmlns = "";
    Strophe.forEachChild(presence, "show", function(elem) {
        pres = elem.textContent;
    });
    Strophe.forEachChild(presence, "status", function(elem) {
        status = elem.textContent;
    });
    Strophe.forEachChild(presence, "x", function(elem) {
        xmlns = elem.getAttribute("xmlns");
    });
  
    var fromJid = Strophe.getBareJidFromJid(from);
  
    // Presence notification from me to me
    if(from == to){
        // Either my vCard updated, or my Presence updated
        return true;
    }
  
    // Find the buddy this message is coming from
   // var buddyObj = this.FindBuddyByJid(fromJid);
    // if(buddyObj == null) {
  
    //     // TODO: What to do here?
  
    //     console.warn("Buddy Not Found: ", fromJid);
    //     return true;
    // }
  
    // if(type == "subscribe"){
    //     // <presence xmlns="jabber:client" type="subscribe" from="58347g3721h~800@...com" id="1" subscription="both" to="58347g3721h~100@...com"/>
    //     // <presence xmlns="jabber:client" type="subscribe" from="58347g3721h~800@...com" id="1" subscription="both" to="58347g3721h~100@...com"/>
        
    //     // One of your buddies is requestion subscription
    //     console.log("Presence: "+ buddyObj.CallerIDName +" requesting subscrption");
  
    //     this.XmppConfirmSubscription(buddyObj);
  
    //     // Also Subscribe to them
    //     this.XmppSendSubscriptionRequest(buddyObj);
  
    //     //UpdateBuddyList();
    //     return true;
    // }
    // if(type == "subscribed"){
    //     // One of your buddies has confimed subscription
    //     console.log("Presence: "+ buddyObj.CallerIDName +" confimed subscrption");
  
    //     //UpdateBuddyList();
    //     return true;
    // }
    // if(type == "unavailable"){
    //     // <presence xmlns="jabber:client" type="unavailable" from="58347g3721h~800@...com/63zy33arw5" to="yas43lag8l@...com"/>
    //     console.log("Presence: "+ buddyObj.CallerIDName +" unavailable");
  
    //     //UpdateBuddyList();
    //     return true;
    // }
  
    // if(xmlns == "vcard-temp:x:update"){
    //     // This is a presence update for the picture change
    //     console.log("Presence: "+ buddyObj.ExtNo +" - "+ buddyObj.CallerIDName +" vCard change");
  
    //     // Should check if the hash is different, could have been a non-picture change..
    //     // However, either way you would need to update the vCard, as there isnt a awy to just get the picture
    //     //this.XmppGetBuddyVcard(buddyObj);
        
    //     //UpdateBuddyList();
    // }
  
    // if(pres != "") {
    //     // This is a regulare 
    //     console.log("Presence: "+ buddyObj.ExtNo +" - "+ buddyObj.CallerIDName +" is now: "+ pres +"("+ status +")");
  
    //     buddyObj.presence = pres;
    //     buddyObj.presenceText = (status == "")? this.lang.default_status : status;
  
    //     //UpdateBuddyList();
    // }
  
    return true;
  }
  
  XmppConfirmSubscription(buddyObj:any){
    if(!this.XMPP || this.XMPP.connected == false) {
        console.warn("XMPP not connected");
        return;
    }
  
    var pres_request = $pres({"to": buddyObj.jid, "from": this.XMPP.jid, "type": "subscribed"});
    this.XMPP.sendPresence(pres_request);
    // Responses are handled in the main handler
  }
  
  XmppSendSubscriptionRequest(buddyObj:any){
    if(!this.XMPP || this.XMPP.connected == false) {
        console.warn("XMPP not connected");
        return;
    }
  
    var pres_request = $pres({"to": buddyObj.jid, "from":this.XMPP.jid, "type": "subscribe" });
    this.XMPP.sendPresence(pres_request);
    // Responses are handled in the main handler
  }
  
  // XMPP Roster
  // ===========
  XmppRemoveBuddyFromRoster(buddyObj:any){
    if(!this.XMPP || this.XMPP.connected == false) {
        console.warn("XMPP not connected");
        return;
    }
  
    var iq_request = $iq({"type":"set", "id":this.XMPP.getUniqueId(), "from":this.XMPP.jid});
    iq_request.c("query", {"xmlns": "jabber:iq:roster"});
    iq_request.c("item", {"jid": buddyObj.jid, "subscription":"remove"});
    if(buddyObj.jid == null){
        console.warn("Missing JID", buddyObj);
        return;
    }
    console.log("Removing "+ buddyObj.CallerIDName +"  from roster...")
  
    this.XMPP.sendIQ(iq_request, (result)=>{
        // console.log(result);
    });
  }
  
  XmppAddBuddyToRoster(buddyObj:any){
    if(!this.XMPP || this.XMPP.connected == false) {
        console.warn("XMPP not connected");
        return;
    }
  
    var iq_request = $iq({"type":"set", "id":this.XMPP.getUniqueId(), "from":this.XMPP.jid});
    iq_request.c("query", {"xmlns": "jabber:iq:roster"});
    iq_request.c("item", {"jid": buddyObj.jid, "name": buddyObj.CallerIDName});
    if(buddyObj.jid == null){
        console.warn("Missing JID", buddyObj);
        return;
    }
    console.log("Adding "+ buddyObj.CallerIDName +"  to roster...")
  
    this.XMPP.sendIQ(iq_request, (result)=>{
        // console.log(result);
        //this.XmppGetBuddyVcard(buddyObj);
  
        this.XmppSendSubscriptionRequest(buddyObj);
    });
  }
  
  XmppGetBuddies(){
    if(!this.XMPP || this.XMPP.connected == false) {
        console.warn("XMPP not connected");
        return;
    }
  
    var iq_request = $iq({"type":"get", "id":this.XMPP.getUniqueId(), "from":this.XMPP.jid});
    iq_request.c("query", {"xmlns":"jabber:iq:roster"});
    console.log("Getting Buddy List (roster)...")
  
    this.XMPP.sendIQ(iq_request, (result)=>{
        // console.log("XmppGetBuddies Response: ", result);
  
        // Clear out only XMPP
  
        Strophe.forEachChild(result, "query", function(query) {
            Strophe.forEachChild(query, "item", function(buddyItem) {
  
                // console.log("Register Buddy", buddyItem);
  
                // <item xmlns="jabber:iq:roster" jid="58347g3721h~800@xmpp-eu-west-1.innovateasterisk.com" name="Alfredo Dixon" subscription="both"/>
                // <item xmlns="jabber:iq:roster" jid="58347g3721h~123456@conference.xmpp-eu-west-1.innovateasterisk.com" name="Some Group Name" subscription="both"/>
  
                var jid = buddyItem.getAttribute("jid");
                var displayName = buddyItem.getAttribute("name");
                var node = Strophe.getNodeFromJid(jid);
                var buddyDid = node;
                if(this.xmppRealm != "" && this.xmppRealmSeparator !="") {
                    buddyDid = node.split(this.xmppRealmSeparator,2)[1];
                }
                var ask = (buddyItem.getAttribute("ask"))? buddyItem.getAttribute("ask") : "none";
                var sub = (buddyItem.getAttribute("subscription"))? buddyItem.getAttribute("subscription") : "none";
                var isGroup = (jid.indexOf("@"+ this.xmppChatGroupService +".") > -1);
  
                var buddyObj = this.FindBuddyByJid(jid);
                if(buddyObj == null){
                    // Create Cache
                    if(isGroup == true){
                        console.log("Adding roster (group):", buddyDid, "-", displayName);
                        buddyObj = this.MakeBuddy("group", false, false, false, displayName, buddyDid, jid, false, buddyDid, false);
                    }
                    else {
                        console.log("Adding roster (xmpp):", buddyDid, "-", displayName);
                        buddyObj = this.MakeBuddy("xmpp", false, false, true, displayName, buddyDid, jid, false, buddyDid, false);
                    }
  
                    // RefreshBuddyData(buddyObj);
                    this.XmppGetBuddyVcard(buddyObj);
                }
                else {
                    // Buddy cache exists
                    console.log("Existing roster item:", buddyDid, "-", displayName);
  
                    // RefreshBuddyData(buddyObj);
                    this.XmppGetBuddyVcard(buddyObj);
                }
  
            });
        });
  
        // Update your own status, and get the status of others
        this.XmppSetMyPresence(getDbItem("XmppLastPresence", "chat"), getDbItem("XmppLastStatus", ""), true);
  
        // Populate the buddy list
        //UpdateBuddyList();
  
    }, function(e){
        console.warn("Error Getting Roster", e);
    }, 30 * 1000);
  }
  
  
  // Profile (vCard)
  // ===============
  XmppGetMyVcard(){
    if(!this.XMPP || this.XMPP.connected == false) {
        console.warn("XMPP not connected");
        return;
    }
  
    var iq_request = $iq({"type" : "get", "id" : this.XMPP.getUniqueId(), "from" : this.XMPP.jid});
    iq_request.c("vCard", {"xmlns" : "vcard-temp"});
  
    this.XMPP.sendIQ(iq_request, (result)=>{
        console.log("XmppGetMyVcard Response: ", result);
  
  
  
    }, function(e){
        console.warn("Error in XmppGetMyVcard", e);
    }, 30 * 1000);
  }
  
  
  XmppSetMyVcard(){
    if(!this.XMPP || this.XMPP.connected == false) {
        console.warn("XMPP not connected");
        return;
    }
  
    var profileVcard:any = getDbItem("profileVcard", null);
    if(profileVcard == null || profileVcard == ""){
        console.warn("No vCard created yet");
        return;
    }
    profileVcard = JSON.parse(profileVcard);
  
    //var base64 = this.getPicture("profilePicture");
    //var imgBase64 = base64.split(",")[1];
  
    var iq_request = $iq({"type" : "set", "id" : this.XMPP.getUniqueId(), "from" : this.XMPP.jid});
    iq_request.c("vCard", {"xmlns" : "vcard-temp"});
    iq_request.c("FN", {}, this.profileName);
    iq_request.c("TITLE", {}, profileVcard.TitleDesc);
    iq_request.c("TEL");
    iq_request.c("NUMBER", {}, this.profileUser);
    iq_request.up();
    iq_request.c("TEL");
    iq_request.c("CELL", {}, profileVcard.Mobile);
    iq_request.up();
    iq_request.c("TEL");
    iq_request.c("VOICE", {}, profileVcard.Number1);
    iq_request.up();
    iq_request.c("TEL");
    iq_request.c("FAX", {}, profileVcard.Number2);
    iq_request.up();
    iq_request.c("EMAIL");
    iq_request.c("USERID", {}, profileVcard.Email);
    iq_request.up();
    iq_request.c("PHOTO");
    iq_request.c("TYPE", {}, "image/webp"); // image/png
    //iq_request.c("BINVAL", {}, imgBase64);
    iq_request.up();
    iq_request.c("JABBERID", {}, Strophe.getBareJidFromJid(this.XMPP.jid));
  
    console.log("Sending vCard update");
    this.XMPP.sendIQ(iq_request, (result)=>{
        // console.log("XmppSetMyVcard Response: ", result);
    }, function(e){
        console.warn("Error in XmppSetMyVcard", e);
    }, 30 * 1000);
  }
  
  }
  
  function getDbItem(itemIndex, defaultValue){
    if(window.localStorage.getItem(itemIndex) != null) return window.localStorage.getItem(itemIndex);
    return defaultValue;
  }
  
  // Sounds Meter Class
  // ==================
  class SoundMeter {
      startTime: any;
      context: any;
      source: any;
      lineNum: any;
      sessionId: any;
      captureInterval: any;
      levelsInterval: any;
      networkInterval: any;
      ReceiveBitRateChart: any;
      ReceiveBitRate: any[];
      ReceivePacketRate: any[];
      ReceivePacketLossChart: any;
      ReceivePacketLoss:any;
      ReceiveJitterChart:any;
      ReceiveJitter:any;
      ReceiveLevelsChart:any;
      ReceiveLevels:any = [];
      SendBitRateChart:any;
      SendBitRate:any = [];
      SendPacketRateChart:any;
      SendPacketRate:any = [];
  
      instant:any; // Primary Output indicator
      AnalyserNode:any;
      ReceivePacketRateChart: any;
      //ReceivePacketRateChart: any;
  
      constructor(sessionId, lineNum) {
          var audioContext = null;
          try {
              window.AudioContext = window.AudioContext;
              audioContext = new AudioContext();
          }
          catch(e) {
              console.warn("AudioContext() LocalAudio not available... its fine.");
          }
          if (audioContext == null) return null;
          this.context = audioContext;
          this.source = null;
  
          this.lineNum = lineNum;
          this.sessionId = sessionId;
  
          this.captureInterval = null;
          this.levelsInterval = null;
          this.networkInterval = null;
          this.startTime = 0;
  
          this.ReceiveBitRateChart = null;
          this.ReceiveBitRate = [];
          this.ReceiveBitRateChart = null;
          this.ReceivePacketRate = [];
          this.ReceivePacketLossChart = null;
          this.ReceivePacketLoss = [];
          this.ReceiveJitterChart = null;
          this.ReceiveJitter = [];
          this.ReceiveLevelsChart = null;
          this.ReceiveLevels = [];
          this.SendBitRateChart = null;
          this.SendBitRate = [];
          this.SendPacketRateChart = null;
          this.SendPacketRate = [];
  
          this.instant = 0; // Primary Output indicator
  
          this.AnalyserNode = this.context.createAnalyser();
          this.AnalyserNode.minDecibels = -90;
          this.AnalyserNode.maxDecibels = -10;
          this.AnalyserNode.smoothingTimeConstant = 0.85;
      }
      connectToSource(stream, callback) {
          console.log("SoundMeter connecting...");
          try {
              this.source = this.context.createMediaStreamSource(stream);
              this.source.connect(this.AnalyserNode);
              // this.AnalyserNode.connect(this.context.destination); // Can be left unconnected
              this._start();
  
              callback(null);
          }
          catch(e) {
              console.error(e); // Probably not audio track
              callback(e);
          }
      }
      _start(){
          var self:any = this;
          self.instant = 0;
          self.AnalyserNode.fftSize = 32; // 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, and 32768. Defaults to 2048
          self.dataArray = new Uint8Array(self.AnalyserNode.frequencyBinCount);
  
          this.captureInterval = window.setInterval(function(){
              self.AnalyserNode.getByteFrequencyData(self.dataArray); // Populate array with data from 0-255
  
              // Just take the maximum value of this data
              self.instant = 0;
              for(var d = 0; d < self.dataArray.length; d++) {
                  if(self.dataArray[d] > self.instant) self.instant = self.dataArray[d];
              }
  
          }, 1);
      }
      stop() {
          console.log("Disconnecting SoundMeter...");
          window.clearInterval(this.captureInterval);
          this.captureInterval = null;
          window.clearInterval(this.levelsInterval);
          this.levelsInterval = null;
          window.clearInterval(this.networkInterval);
          this.networkInterval = null;
          try {
              this.source.disconnect();
          }
          catch(e) { }
          this.source = null;
          try {
              this.AnalyserNode.disconnect();
          }
          catch(e) { }
          this.AnalyserNode = null;
          try {
              this.context.close();
          }
          catch(e) { }
          this.context = null;
  
          // Save to IndexDb
          var lineObj:any = "findLineByNumber(this.lineNum)";
          var QosData = {
              ReceiveBitRate: this.ReceiveBitRate,
              ReceivePacketRate: this.ReceivePacketRate,
              ReceivePacketLoss: this.ReceivePacketLoss,
              ReceiveJitter: this.ReceiveJitter,
              ReceiveLevels: this.ReceiveLevels,
              SendBitRate: this.SendBitRate,
              SendPacketRate: this.SendPacketRate,
          }
          if(this.sessionId != null){
              SaveQosData(QosData, this.sessionId, lineObj.BuddyObj.identity);
          }
      }
  }
  
  function meterSettingsOutput(audioStream, objectId, direction, interval){
      var soundMeter = new SoundMeter(null, null);
      soundMeter.startTime = Date.now();
      soundMeter.connectToSource(audioStream, function (e) {
          if (e != null) return;
  
          console.log("SoundMeter Connected, displaying levels to:"+ objectId);
          soundMeter.levelsInterval = window.setInterval(function () {
              // Calculate Levels (0 - 255)
              var instPercent = (soundMeter.instant/255) * 100;
              // $("#"+ objectId).css(direction, instPercent.toFixed(2) +"%");
          }, interval);
      });
  
      return soundMeter;
  }
  
  // QOS
  // ===
  function SaveQosData(QosData, sessionId, buddy){
      var indexedDB = window.indexedDB;
      var request = indexedDB.open("CallQosData", 1);
      request.onerror = function(event) {
          console.error("IndexDB Request Error:", event);
      }
      request.onupgradeneeded = function(event) {
          console.warn("Upgrade Required for IndexDB... probably because of first time use.");
          var IDB = request.result;
  
          // Create Object Store
          if(IDB.objectStoreNames.contains("CallQos") == false){
              var objectStore = IDB.createObjectStore("CallQos", { keyPath: "uID" });
              objectStore.createIndex("sessionid", "sessionid", { unique: false });
              objectStore.createIndex("buddy", "buddy", { unique: false });
              objectStore.createIndex("QosData", "QosData", { unique: false });
          }
          else {
              console.warn("IndexDB requested upgrade, but object store was in place");
          }
      }
      request.onsuccess = function(event) {
          console.log("IndexDB connected to CallQosData");
  
          var IDB = request.result;
          if(IDB.objectStoreNames.contains("CallQos") == false){
              console.warn("IndexDB CallQosData.CallQos does not exists");
              IDB.close();
              window.indexedDB.deleteDatabase("CallQosData"); // This should help if the table structure has not been created.
              return;
          }
          IDB.onerror = function(event) {
              console.error("IndexDB Error:", event);
          }
  
          // Prepare data to write
          var data = {
              uID: uID(),
              sessionid: sessionId,
              buddy: buddy,
              QosData: QosData
          }
          // Commit Transaction
          var transaction = IDB.transaction(["CallQos"], "readwrite");
          var objectStoreAdd = transaction.objectStore("CallQos").add(data);
          objectStoreAdd.onsuccess = function(event) {
              console.log("Call CallQos Success: ", sessionId);
          }
      }
  }
  
  function DisplayQosData(sessionId){
      // var indexedDB = window.indexedDB;
      // var request = indexedDB.open("CallQosData", 1);
      // request.onerror = function(event) {
      //     console.error("IndexDB Request Error:", event);
      // }
      // request.onupgradeneeded = function(event) {
      //     console.warn("Upgrade Required for IndexDB... probably because of first time use.");
      // }
      // request.onsuccess = function(event) {
      //     console.log("IndexDB connected to CallQosData");
  
      //     var IDB = request.result;
      //     if(IDB.objectStoreNames.contains("CallQos") == false){
      //         console.warn("IndexDB CallQosData.CallQos does not exists");
      //         return;
      //     } 
  
      //     var transaction = IDB.transaction(["CallQos"]);
      //     var objectStoreGet = transaction.objectStore("CallQos").index('sessionid').getAll(sessionId);
      //     objectStoreGet.onerror = function(event) {
      //         console.error("IndexDB Get Error:", event);
      //     }
      //     objectStoreGet.onsuccess = function(event) {
      //         if(event.target.result && event.target.result.length == 2){
      //             // This is the correct data
  
      //             var QosData0 = event.target.result[0].QosData;
      //             // ReceiveBitRate: (8) [{…}, {…}, {…}, {…}, {…}, {…}, {…}, {…}]
      //             // ReceiveJitter: (8) [{…}, {…}, {…}, {…}, {…}, {…}, {…}, {…}]
      //             // ReceiveLevels: (9) [{…}, {…}, {…}, {…}, {…}, {…}, {…}, {…}, {…}]
      //             // ReceivePacketLoss: (8) [{…}, {…}, {…}, {…}, {…}, {…}, {…}, {…}]
      //             // ReceivePacketRate: (8) [{…}, {…}, {…}, {…}, {…}, {…}, {…}, {…}]
      //             // SendBitRate: []
      //             // SendPacketRate: []
      //             var QosData1 = event.target.result[1].QosData;
      //             // ReceiveBitRate: []
      //             // ReceiveJitter: []
      //             // ReceiveLevels: []
      //             // ReceivePacketLoss: []
      //             // ReceivePacketRate: []
      //             // SendBitRate: (9) [{…}, {…}, {…}, {…}, {…}, {…}, {…}, {…}, {…}]
      //             // SendPacketRate: (9) [{…}, {…}, {…}, {…}, {…}, {…}, {…}, {…}, {…}]
  
      //             Chart.defaults.global.defaultFontSize = 12;
  
      //             var ChatHistoryOptions = { 
      //                 responsive: true,
      //                 maintainAspectRatio: false,
      //                 animation: false,
      //                 scales: {
      //                     yAxes: [{
      //                         ticks: { beginAtZero: true } //, min: 0, max: 100
      //                     }],
      //                     xAxes: [{
      //                         display: false
      //                     }]
      //                 }, 
      //             }
  
  
      //             // ReceiveBitRateChart
      //             var labelSet = [];
      //             var dataset = [];
      //             var data = (QosData0.ReceiveBitRate.length > 0)? QosData0.ReceiveBitRate : QosData1.ReceiveBitRate;
      //             $.each(data, function(i,item){
      //                 labelSet.push(moment.utc(item.timestamp.replace(" UTC", "")).local().format(displayDateFormat +" "+ displayTimeFormat));
      //                 dataset.push(item.value);
      //             });
      //             var ReceiveBitRateChart = new Chart($("#cdr-AudioReceiveBitRate"), {
      //                 type: 'line',
      //                 data: {
      //                     labels: labelSet,
      //                     datasets: [{
      //                         label: lang.receive_kilobits_per_second,
      //                         data: dataset,
      //                         backgroundColor: 'rgba(168, 0, 0, 0.5)',
      //                         borderColor: 'rgba(168, 0, 0, 1)',
      //                         borderWidth: 1,
      //                         pointRadius: 1
      //                     }]
      //                 },
      //                 options: ChatHistoryOptions
      //             });
  
      //             // ReceivePacketRateChart
      //             var labelSet = [];
      //             var dataset = [];
      //             var data = (QosData0.ReceivePacketRate.length > 0)? QosData0.ReceivePacketRate : QosData1.ReceivePacketRate;
      //             $.each(data, function(i,item){
      //                 labelSet.push(moment.utc(item.timestamp.replace(" UTC", "")).local().format(displayDateFormat +" "+ displayTimeFormat));
      //                 dataset.push(item.value);
      //             });
      //             var ReceivePacketRateChart = new Chart($("#cdr-AudioReceivePacketRate"), {
      //                 type: 'line',
      //                 data: {
      //                     labels: labelSet,
      //                     datasets: [{
      //                         label: lang.receive_packets_per_second,
      //                         data: dataset,
      //                         backgroundColor: 'rgba(168, 0, 0, 0.5)',
      //                         borderColor: 'rgba(168, 0, 0, 1)',
      //                         borderWidth: 1,
      //                         pointRadius: 1
      //                     }]
      //                 },
      //                 options: ChatHistoryOptions
      //             });
  
      //             // AudioReceivePacketLossChart
      //             var labelSet = [];
      //             var dataset = [];
      //             var data = (QosData0.ReceivePacketLoss.length > 0)? QosData0.ReceivePacketLoss : QosData1.ReceivePacketLoss;
      //             $.each(data, function(i,item){
      //                 labelSet.push(moment.utc(item.timestamp.replace(" UTC", "")).local().format(displayDateFormat +" "+ displayTimeFormat));
      //                 dataset.push(item.value);
      //             });
      //             var AudioReceivePacketLossChart = new Chart($("#cdr-AudioReceivePacketLoss"), {
      //                 type: 'line',
      //                 data: {
      //                     labels: labelSet,
      //                     datasets: [{
      //                         label: lang.receive_packet_loss,
      //                         data: dataset,
      //                         backgroundColor: 'rgba(168, 99, 0, 0.5)',
      //                         borderColor: 'rgba(168, 99, 0, 1)',
      //                         borderWidth: 1,
      //                         pointRadius: 1
      //                     }]
      //                 },
      //                 options: ChatHistoryOptions
      //             });
  
      //             // AudioReceiveJitterChart
      //             var labelSet = [];
      //             var dataset = [];
      //             var data = (QosData0.ReceiveJitter.length > 0)? QosData0.ReceiveJitter : QosData1.ReceiveJitter;
      //             $.each(data, function(i,item){
      //                 labelSet.push(moment.utc(item.timestamp.replace(" UTC", "")).local().format(displayDateFormat +" "+ displayTimeFormat));
      //                 dataset.push(item.value);
      //             });
      //             var AudioReceiveJitterChart = new Chart($("#cdr-AudioReceiveJitter"), {
      //                 type: 'line',
      //                 data: {
      //                     labels: labelSet,
      //                     datasets: [{
      //                         label: lang.receive_jitter,
      //                         data: dataset,
      //                         backgroundColor: 'rgba(0, 38, 168, 0.5)',
      //                         borderColor: 'rgba(0, 38, 168, 1)',
      //                         borderWidth: 1,
      //                         pointRadius: 1
      //                     }]
      //                 },
      //                 options: ChatHistoryOptions
      //             });
                  
      //             // AudioReceiveLevelsChart
      //             var labelSet = [];
      //             var dataset = [];
      //             var data = (QosData0.ReceiveLevels.length > 0)? QosData0.ReceiveLevels : QosData1.ReceiveLevels;
      //             $.each(data, function(i,item){
      //                 labelSet.push(moment.utc(item.timestamp.replace(" UTC", "")).local().format(displayDateFormat +" "+ displayTimeFormat));
      //                 dataset.push(item.value);
      //             });
      //             var AudioReceiveLevelsChart = new Chart($("#cdr-AudioReceiveLevels"), {
      //                 type: 'line',
      //                 data: {
      //                     labels: labelSet,
      //                     datasets: [{
      //                         label: lang.receive_audio_levels,
      //                         data: dataset,
      //                         backgroundColor: 'rgba(140, 0, 168, 0.5)',
      //                         borderColor: 'rgba(140, 0, 168, 1)',
      //                         borderWidth: 1,
      //                         pointRadius: 1
      //                     }]
      //                 },
      //                 options: ChatHistoryOptions
      //             });
                  
      //             // SendPacketRateChart
      //             var labelSet = [];
      //             var dataset = [];
      //             var data = (QosData0.SendPacketRate.length > 0)? QosData0.SendPacketRate : QosData1.SendPacketRate;
      //             $.each(data, function(i,item){
      //                 labelSet.push(moment.utc(item.timestamp.replace(" UTC", "")).local().format(displayDateFormat +" "+ displayTimeFormat));
      //                 dataset.push(item.value);
      //             });
      //             var SendPacketRateChart = new Chart($("#cdr-AudioSendPacketRate"), {
      //                 type: 'line',
      //                 data: {
      //                     labels: labelSet,
      //                     datasets: [{
      //                         label: lang.send_packets_per_second,
      //                         data: dataset,
      //                         backgroundColor: 'rgba(0, 121, 19, 0.5)',
      //                         borderColor: 'rgba(0, 121, 19, 1)',
      //                         borderWidth: 1,
      //                         pointRadius: 1
      //                     }]
      //                 },
      //                 options: ChatHistoryOptions
      //             });
  
      //             // AudioSendBitRateChart
      //             var labelSet = [];
      //             var dataset = [];
      //             var data = (QosData0.SendBitRate.length > 0)? QosData0.SendBitRate : QosData1.SendBitRate;
      //             $.each(data, function(i,item){
      //                 labelSet.push(moment.utc(item.timestamp.replace(" UTC", "")).local().format(displayDateFormat +" "+ displayTimeFormat));
      //                 dataset.push(item.value);
      //             });
      //             var AudioSendBitRateChart = new Chart($("#cdr-AudioSendBitRate"), {
      //                 type: 'line',
      //                 data: {
      //                     labels: labelSet,
      //                     datasets: [{
      //                         label: lang.send_kilobits_per_second,
      //                         data: dataset,
      //                         backgroundColor: 'rgba(0, 121, 19, 0.5)',
      //                         borderColor: 'rgba(0, 121, 19, 1)',
      //                         borderWidth: 1,
      //                         pointRadius: 1
      //                     }]
      //                 },
      //                 options: ChatHistoryOptions
      //             });
  
      //         } else{
      //             console.warn("Result not expected", result.result);
      //         }
      //     }
      // }
  }
  function DeleteQosData(buddy, stream){
      var indexedDB = window.indexedDB;
      var request = indexedDB.open("CallQosData", 1);
      request.onerror = function(event) {
          console.error("IndexDB Request Error:", event);
      }
      request.onupgradeneeded = function(event) {
          console.warn("Upgrade Required for IndexDB... probably because of first time use.");
          // If this is the case, there will be no call recordings
      }
      request.onsuccess = function(event) {
          console.log("IndexDB connected to CallQosData");
  
          var IDB = request.result;
          if(IDB.objectStoreNames.contains("CallQos") == false){
              console.warn("IndexDB CallQosData.CallQos does not exists");
              return;
          }
          IDB.onerror = function(event) {
              console.error("IndexDB Error:", event);
          }
  
          // Loop and Delete
          // Note:  This database can only delete based on Primary Key
          // The The Primary Key is arbitrary, so you must get all the rows based
          // on a lookup, and delete from there.
          // $.each(stream.DataCollection, function (i, item) {
          //     if (item.ItemType == "CDR" && item.SessionId && item.SessionId != "") {
          //         console.log("Deleting CallQosData: ", item.SessionId);
          //         var objectStore = IDB.transaction(["CallQos"], "readwrite").objectStore("CallQos");
          //         var objectStoreGet = objectStore.index('sessionid').getAll(item.SessionId);
          //         objectStoreGet.onerror = function(event) {
          //             console.error("IndexDB Get Error:", event);
          //         }
          //         objectStoreGet.onsuccess = function(event) {
          //             if(request.result){
          //                 // There sre some rows to delete
          //                 // $.each(request.result, function(i:any, item:any){
          //                 //     // console.log("Delete: ", item.uID);
          //                 //     try{
          //                 //         objectStore.delete(item.uID);
          //                 //     } catch(e){
          //                 //         console.log("Call CallQosData Delete failed: ", e);
          //                 //     }
          //                 // });
          //             }
          //         }
          //     }
          // });
  
  
      }
  }
  function uID(){
      return Date.now()+Math.floor(Math.random()*10000).toString(16).toUpperCase();
  }
