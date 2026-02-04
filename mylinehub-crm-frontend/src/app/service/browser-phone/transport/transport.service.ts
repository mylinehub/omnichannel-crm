import { Injectable } from '@angular/core';
import { HeaderVariableService } from '../../header-variable/header-variable.service';
import { RegisterService } from '../register/register.service';
import { NbDialogService } from '@nebular/theme';

@Injectable({
  providedIn: 'root'
})
export class TransportService {

  constructor(protected headerVariableService : HeaderVariableService,
              private dialogService: NbDialogService,
              protected registerService: RegisterService,
            ) { }

        // Transport Events
      // ================
      onTransportConnected(userAgent:any, lang:any,transportReconnectionAttempts:any, transportReconnectionTimeout:any){
        //console.log("I am in onTransportConnected");
        //console.log("Connected to Web Socket!");
        this.headerVariableService.browserPhoneTitle=lang.connected_to_web_socket;
  
  
          // Reset the ReconnectionAttempts
          userAgent.isReRegister = false;
          userAgent.transport.attemptingReconnection = false;
          userAgent.transport.ReconnectionAttempts = transportReconnectionAttempts;
  
          // Auto start register
          if(userAgent.transport.attemptingReconnection == false && userAgent.registering == false){
              window.setTimeout(()=>{
                  this.registerService.register(userAgent,lang);
              }, 500);
          } else{
            console.warn("onTransportConnected: register() called, but attemptingReconnection is true or registering is true")
          }
      }
  
    onTransportConnectError(userAgent:any,lang:any,transportReconnectionAttempts:any, transportReconnectionTimeout:any,error:any){

        //console.log("I am in onTransportConnectError");

        console.warn("WebSocket Connection Failed:", error);
  
          // We set this flag here so that the re-register attempts are fully completed.
          userAgent.isReRegister = false;
  
          // If there is an issue with the WS connection
          // We unregister, so that we register again once its up
        //console.log("Unregister...");
          try{
              userAgent.registerer.unregister();
          } catch(e){
              // I know!!!
          }
  
          this.headerVariableService.browserPhoneTitle=lang.web_socket_error;
          this.headerVariableService.browserPhoneTitle=lang.unregistered;
          this.headerVariableService.isBrowserPhoneOnline = false;
          this.headerVariableService.isOnlineStatus = "danger";

          this.reconnectTransport(userAgent,lang,transportReconnectionAttempts, transportReconnectionTimeout);
  
      }
  
    onTransportDisconnected(userAgent:any,lang:any,transportReconnectionAttempts:any, transportReconnectionTimeout:any){
        //console.log("I am in onTransportDisconnected");

        //console.log("Disconnected from Web Socket!");
        this.headerVariableService.browserPhoneTitle=lang.disconnected_from_web_socket;
  
          userAgent.isReRegister = false;
          this.headerVariableService.browserPhoneTitle=lang.unregistered;
          this.headerVariableService.isBrowserPhoneOnline = false;
          this.headerVariableService.isOnlineStatus = "danger";
      }
  
  
    reconnectTransport(userAgent:any,lang:any,transportReconnectionAttempts:any, transportReconnectionTimeout:any){

        //console.log("I am in reconnectTransport");

          if(userAgent == null) return;
      
          userAgent.registering = false; // if the transport was down, you will not be registered
          if(userAgent.transport && userAgent.transport.isConnected()){
              // Asked to re-connect, but ws is connected
              this.onTransportConnected(userAgent,lang,transportReconnectionAttempts, transportReconnectionTimeout);
              return;
          }
          //console.log("Reconnect Transport...");
      
          window.setTimeout(()=>{
              // $("#regStatus").html(lang.connecting_to_web_socket);
              //console.log("ReConnecting to WebSocket...");
      
              if(userAgent.transport && userAgent.transport.isConnected()){
                  // Already Connected
                  this.onTransportConnected(userAgent,lang,transportReconnectionAttempts, transportReconnectionTimeout);
                  return;
              } else {
                  userAgent.transport.attemptingReconnection = true
                  userAgent.reconnect().catch((error:any)=>{
                      userAgent.transport.attemptingReconnection = false
                      console.warn("Failed to reconnect", error);
      
                      // Try Again
                      this.reconnectTransport(userAgent,lang,transportReconnectionAttempts, transportReconnectionTimeout);
                  });
              }
          }, transportReconnectionTimeout * 1000);
      
          this.headerVariableService.browserPhoneTitle = lang.connecting_to_web_socket;
          //console.log("Waiting to Re-connect...", transportReconnectionTimeout, "Attempt remaining", userAgent.transport.reconnectionAttempts);
          userAgent.transport.reconnectionAttempts = userAgent.transport.reconnectionAttempts - 1;
      }


}
