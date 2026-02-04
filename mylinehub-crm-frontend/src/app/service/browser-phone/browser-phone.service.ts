import { Injectable, OnDestroy } from '@angular/core';
import { UserAgent,Registerer,RegistererState} from 'sip.js';
import { Subject } from 'rxjs';
import { ConstantsService } from '../constants/constants.service';
import { HeaderVariableService } from '../header-variable/header-variable.service';
import { DialogComponent } from '../../pages/employee/all-employees/dialog/dialog.component';
import { NbDialogService } from '@nebular/theme';
import { TransportService } from './transport/transport.service';
import { MessageService } from './message/message.service';
import { RegisterService } from './register/register.service';
import { AnswerService } from './answer/answer.service';

@Injectable({
  providedIn: 'root'
})
 export class BrowserPhoneService implements OnDestroy {

  appversion:any = "0.0.1";
  sipjsversion:any = "0.20.0";
  userAgent:any="";
  registerer:any="";
  userAgentStr:any ;
  
  //This letiable is used to do async tasks
  sendAync:any;
  sendAyncObserver: any;

  enableRingtone:any = true;
  //createSipUserAgent Method letiables
  // Time in seconds before automatic Busy Here sent
  noAnswerTimeout: any = 120;
  // The timeout in seconds for the initial connection to make on the web socket port
  transportConnectionTimeout: any = 1500;
  // SDP Media Bundle: max-bundle | max-compat | balanced https://webrtcstandards.info/sdp-bundle/
  bundlePolicy: any = 'balanced'; 
  //Need to check below                 
  registerContactParams: any="";
  // Registration expiry time (in seconds)
  registerExpires: any = 3000;
  registerExtraHeaders: any="";
  registerExtraContactParams: any="";
  // Sets the JSON string for ice Server. Default: [{ "urls": "stun:stun.l.google.com:19302" }] Must be https://developer.mozilla.org/en-US/docs/Web/API/RTCConfiguration/iceServers
  iceStunServerJson:any = "";
  iceStunCheckTimeout:any = 500;   
  ipInContact:any = 1; 
    // Set the transport parameter to wss when used in SIP URIs. (Required for Asterisk as it doesn't support Path)
  wssInTransport:any = 1;    
  
   // The number of times to attempt to reconnect to a WebSocket when the connection drops.
   transportReconnectionAttempts: any = 999;
   // The time in seconds to wait between WebSocket reconnection attempts.
   transportReconnectionTimeout: number = 300;

  // Asterisk Phone DID length (Our extensions are of length 3. This has to be changed accordingly)
  didLength: any = 3;


  //Enlish language is loaded initially, if we need support for others then first we may need to get jason and define current language accordingly. This will have to make changes in constructor
  lang: any = {};

  streamBuffer :any=  50; 
  setExtensionSubscriptionTimeut: NodeJS.Timeout;


  constructor(protected constService : ConstantsService,
              protected headerVariableService : HeaderVariableService,
              private dialogService: NbDialogService,
              protected transportService:TransportService,
              protected registerService: RegisterService,
              protected messageService:MessageService,
              protected answerService : AnswerService
            ) {
                this.startSendAync();
                //By Default we have choosen language as english although this could have been changed to other languages from here for browser phone information
                this.lang = this.constService.en;
                this.userAgentStr = "Mylinehub Phone "+ this.appversion +" (SIPJS - "+ this.sipjsversion +") "+ this.headerVariableService.navuserAgent;   // Set this to whatever you want.
            }

    startSendAync()  
    {
          //console.log("creating obserable first time ...")
          this.sendAync = new Subject();
          this.sendAyncObserver = this.sendAync.subscribe((data:any)=>{
            if(data)
            {
                  switch(data)
                {
                  case 'createSipUserAgent':
                 //console.log("createUserAgent aync request initilated ...")
                  this.createSipUserAgent();
                  break;
                  default:
                 //console.log("deafult called, may be initializing first time ...")
                  //console.log(data)
                  break;
                }
            }
          });

          //console.log("sending default string to verify ...")
          this.sendAync.next('default data');
        
    }  

    //SIP Methods 
    createSipUserAgent() {
      //console.log("Creating User Agent...");
      let sipUserAgentoptions:any = {};
        if(!(ConstantsService.user.domain==null || ConstantsService.user.domain=="" ||ConstantsService.user.secondDomain=="" || ConstantsService.user.domain=="null" || ConstantsService.user.domain=="undefined")) //ConstantsService.user.domain = ConstantsService.user.domain; // Sets globally
        { 

            if(ConstantsService.user.useSecondaryAllotedLine)
            {
                sipUserAgentoptions = {
                    uri: UserAgent.makeURI("sip:"+ ConstantsService.user.extension + "@" + ConstantsService.user.domain),
                    transportOptions: {
                        server: "wss://" + ConstantsService.user.secondDomain + ":"+ ConstantsService.user.sipPort +""+ ConstantsService.user.sipPath,
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
                   // contactName: ConstantsService.user.extension,
                    displayName: ConstantsService.user.firstName + " " + ConstantsService.user.lastName,
                    authorizationUsername: ConstantsService.user.extension,
                    authorizationPassword: ConstantsService.user.extensionpassword,
                    userAgentString: this.userAgentStr,
                    autoStart: false,
                    autoStop: true,
                    logBuiltinEnabled:false,
                    logLevel:false,
                    // traceSip: false,
                    register: false,
                    noAnswerTimeout: this.noAnswerTimeout,
                    // sipExtension100rel: // UNSUPPORTED | SUPPORTED | REQUIRED NOTE: rel100 is not supported
                    delegate: {
                        onInvite: (sip:any)=>{
                        //   console.log("Invite received");
                        //   console.log(typeof sip);
                        //   console.log(JSON.stringify(sip));
        
                          this.answerService.receiveCall(this.userAgent, this.lang, sip, this.didLength, this.enableRingtone);
                        },
                        onMessage: (sip:any)=>{
                            console.log("Message received");
                            this.messageService.receiveOutOfDialogMessage(this.userAgent,sip,this.lang,this.didLength);
                        }
                    }
                    }
            }
            else
            {
                sipUserAgentoptions = {
                    uri: UserAgent.makeURI("sip:"+ ConstantsService.user.extension + "@" + ConstantsService.user.domain),
                    transportOptions: {
                        server: "wss://" + ConstantsService.user.domain + ":"+ ConstantsService.user.sipPort +""+ ConstantsService.user.sipPath,
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
                   // contactName: ConstantsService.user.extension,
                    displayName: ConstantsService.user.firstName + " " + ConstantsService.user.lastName,
                    authorizationUsername: ConstantsService.user.extension,
                    authorizationPassword: ConstantsService.user.extensionpassword,
                    userAgentString: this.userAgentStr,
                    autoStart: false,
                    autoStop: true,
                    logBuiltinEnabled:false,
                    logLevel:false,
                    // traceSip: false,
                    register: false,
                    noAnswerTimeout: this.noAnswerTimeout,
                    // sipExtension100rel: // UNSUPPORTED | SUPPORTED | REQUIRED NOTE: rel100 is not supported
                    delegate: {
                        onInvite: (sip:any)=>{
                        //   console.log("Invite received");
                        //   console.log(typeof sip);
                        //   console.log(JSON.stringify(sip));
        
                          this.answerService.receiveCall(this.userAgent, this.lang, sip, this.didLength, this.enableRingtone);
                        },
                        onMessage: (sip:any)=>{
                            console.log("Message received");
                            this.messageService.receiveOutOfDialogMessage(this.userAgent,sip,this.lang,this.didLength);
                        }
                    }
                    }
            }

        }
        else
        {
            //Dialod showing domain ijs null
            this.showDialoge('Error','activity-outline','danger', "Doamin in database for user is null. Kindly report to admin."); 
          
        }

        if(this.iceStunServerJson != ""){
            sipUserAgentoptions.sessionDescriptionHandlerFactoryOptions.peerConnectionConfiguration.iceServers = JSON.parse(this.iceStunServerJson);
        }

        // Added to the contact BEFORE the '>' (permanent)
        if(this.registerContactParams && this.registerContactParams != "" && this.registerContactParams != "{}"){
            try{
                sipUserAgentoptions.contactParams = JSON.parse(this.registerContactParams);
            } catch(e){}
        }
        if(this.wssInTransport){
            try{
                sipUserAgentoptions.contactParams["transport"] = "wss";
            } catch(e){}
        }

        // Add (Hardcode) other RTCPeerConnection({ rtcConfiguration }) config dictionary options here
        // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/RTCPeerConnection
        // Example: 
        // options.sessionDescriptionHandlerFactoryOptions.peerConnectionConfiguration.rtcpMuxPolicy = "require";
        
        this.userAgent = new UserAgent(sipUserAgentoptions);
        this.userAgent.isRegistered = ()=>{
            return (this.userAgent && this.userAgent.registerer && this.userAgent.registerer.state == RegistererState.Registered);
        }
        // For some reason this is marked as private... not sure why
        this.userAgent.sessions = this.userAgent._sessions;
        this.userAgent.registrationCompleted = false;
        this.userAgent.registering = false;
        this.userAgent.transport.reconnectionAttempts = this.transportReconnectionAttempts;
        this.userAgent.transport.attemptingReconnection = false;
        this.userAgent.BlfSubs = [];
        this.userAgent.lastVoicemailCount = 0;

      //console.log("Creating User Agent... Done");

        this.userAgent.transport.onConnect = ()=>{
            this.transportService.onTransportConnected(this.userAgent,this.lang,this.transportReconnectionAttempts,this.transportConnectionTimeout);
        }
        this.userAgent.transport.onDisconnect = (error:any)=>{
            if(error){
                this.transportService.onTransportConnectError(this.userAgent,this.lang,this.transportReconnectionAttempts,this.transportConnectionTimeout,error);
            }
            else {
                this.transportService.onTransportDisconnected(this.userAgent,this.lang,this.transportReconnectionAttempts,this.transportConnectionTimeout);
            }
        }

        let RegistererOptions = { 
            expires: this.registerExpires,
            extraHeaders: [],
            extraContactHeaderParams: []
        }

        // Added to the SIP Headers
        if(this.registerExtraHeaders && this.registerExtraHeaders != "" && this.registerExtraHeaders != "{}"){
            try{
                let registerExtraHeaders = JSON.parse(this.registerExtraHeaders);
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
                let registerExtraContactParams = JSON.parse(this.registerExtraContactParams);
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
      //console.log("Creating Registerer... Done");

        this.userAgent.registerer.stateChange.addListener((newState:any)=>{
          //console.log("User Agent Registration State:", newState);
            switch (newState) {
                case RegistererState.Initial:
                    // Nothing to do, just printing lof inside below function
                    this.registerService.onInitial(this.userAgent, this.lang);
                    break;
                case RegistererState.Registered:
                    this.registerService.onRegistered(this.userAgent, this.lang);
                    break;
                case RegistererState.Unregistered:
                    this.registerService.onUnregistered(this.userAgent, this.lang);
                    break;
                case RegistererState.Terminated:
                    // Nothing to do, just printing lof inside below function
                    this.registerService.onTerminated(this.userAgent, this.lang);
                    break;
            }
        });

      //console.log("User Agent Connecting to WebSocket...");
        this.headerVariableService.browserPhoneTitle = this.lang.connecting_to_web_socket;
        this.userAgent.start().catch((error:any)=>{
            this.transportService.onTransportConnectError(this.userAgent,this.lang,this.transportReconnectionAttempts,this.transportConnectionTimeout,error);
        });
    }
   
    ngOnDestroy() {
      //console.log("header-OnDestroy");
        this.sendAync.next();
        this.sendAync.complete();
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