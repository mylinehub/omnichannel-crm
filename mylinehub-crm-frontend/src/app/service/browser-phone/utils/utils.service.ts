import { Injectable } from '@angular/core';
import { DialogComponent } from '../../../pages/employee/all-employees/dialog/dialog.component';
import { HeaderVariableService } from '../../header-variable/header-variable.service';
import { NbDialogService } from '@nebular/theme';
import * as moment from 'moment';

@Injectable({
  providedIn: 'root'
})
export class UtilsService {

  constructor(protected headerVariableService : HeaderVariableService,
    private dialogService: NbDialogService,
  ) { }


  userLocale(){
    //console.log("I am in userLocale");
    let language = window.navigator.language || window.navigator.language; // "en", "en-US", "fr", "fr-FR", "es-ES", etc.
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

uID(){
    //console.log("I am in uID");
    return Date.now()+Math.floor(Math.random()*10000).toString(16).toUpperCase();
}

utcDateNow(){
    //console.log("I am in utcDateNow");
    return moment.utc().format("YYYY-MM-DD HH:mm:ss UTC");
}

formatShortDuration(seconds:any){
  //console.log("I am in formatShortDuration");
  let sec = Math.floor(parseFloat(seconds));
  if(sec < 0){
      return sec;
  } 
  else if(sec >= 0 && sec < 60){
      return "00:"+ ((sec > 9)? sec : "0"+sec );
  } 
  else if(sec >= 60 && sec < 60 * 60){ // greater then a minute and less then an hour
      let duration = moment.duration(sec, 'seconds');
      return ((duration.minutes() > 9)? duration.minutes() : "0"+duration.minutes()) + ":" + ((duration.seconds() > 9)? duration.seconds() : "0"+duration.seconds());
  } 
  else if(sec >= 60 * 60 && sec < 24 * 60 * 60){ // greater than an hour and less then a day
      let duration = moment.duration(sec, 'seconds');
      return ((duration.hours() > 9)? duration.hours() : "0"+duration.hours())  + ":" + ((duration.minutes() > 9)? duration.minutes() : "0"+duration.minutes())  + ":" + ((duration.seconds() > 9)? duration.seconds() : "0"+duration.seconds());
  } 
  //  Otherwise.. this is just too long
}

countSessions(userAgent:any,id:any){
//   //console.log("I am in countSessions");
//   //console.log("My Id : ",id);

  let rtn = 0;
  if(userAgent == null) {
    console.warn("userAgent is null");
      return 0;
  }

//   //console.log("User Agent within count session : ")
//  //console.log(JSON.stringify(userAgent._sessions));
 // //console.log(userAgent._sessions);

  userAgent.sessions=userAgent._sessions;
//   //console.log(Object.keys(userAgent.sessions));

  Object.keys(userAgent.sessions).forEach((session:any,i:any)=> {
      if(String(id) != session) rtn ++;
  });


 //console.log("Current call count: ",rtn);

  return rtn;
}

// Stream Manipulations
// ====================
mixAudioStreams(MultiAudioTackStream:any){
  //console.log("I am in mixAudioStreams");
  // Takes in a MediaStream with any number of audio tracks and mixes them together
  let audioContext = null;
  try {
      window.AudioContext = window.AudioContext;
      audioContext = new AudioContext();
  }
  catch(e){
    console.warn("AudioContext() not available, cannot record");
      return MultiAudioTackStream;
  }
  let mixedAudioStream = audioContext.createMediaStreamDestination();
  MultiAudioTackStream.getAudioTracks().forEach((audioTrack:any)=>{
      let srcStream = new MediaStream();
      srcStream.addTrack(audioTrack);
      let streamSourceNode = audioContext.createMediaStreamSource(srcStream);
      streamSourceNode.connect(mixedAudioStream);
  });

  return mixedAudioStream.stream;
}

// Device Detection
// ================
detectDevices() {

  //console.log("I am in detectDevices");
  //console.log('verify permissions');

  const microphone = "microphone" as PermissionName;
  const camera = "camera" as PermissionName;

  navigator.permissions.query({name: microphone})
  .then((permissionObj) => {
    //console.log(permissionObj.state);
    let now = Date.now();
    switch(permissionObj.state)
    {
          case 'granted': 
                              navigator.permissions.query({name: camera})
                              .then((permissionObj) => {
                                //console.log(permissionObj.state);
                                switch(permissionObj.state)
                                {
                                      case 'granted':
                                                          //console.log('detectDevices');
                                                          navigator.mediaDevices.enumerateDevices().then((deviceInfos:any)=>{
                                                            // deviceInfos will not have a populated lable unless to accept the permission
                                                            // during getUserMedia. This normally happens at startup/setup
                                                            // so from then on these devices will be with lables.
                                                          //console.log('inside navigator');
                                                            this.headerVariableService.hasVideoDevice = false;
                                                            this.headerVariableService.hasAudioDevice = false;
                                                            this.headerVariableService.hasSpeakerDevice = false; // Safari and Firefox don't have these
                                                            this.headerVariableService.micList = [];
                                                            this.headerVariableService.videoList = [];
                                                            this.headerVariableService.speakerList = [];
                                                            //console.log('deviceInfos',deviceInfos);
                                                            for (let i = 0; i < deviceInfos.length; ++i) {
                                                                if (deviceInfos[i].kind === "audioinput") {
                                                                  this.headerVariableService.hasAudioDevice = true;
                                                                  this.headerVariableService.micList.push(deviceInfos[i].deviceId);
                                                                } 
                                                                else if (deviceInfos[i].kind === "audiooutput") {
                                                                  this.headerVariableService.hasSpeakerDevice = true;
                                                                  this.headerVariableService.speakerList.push(deviceInfos[i].deviceId);
                                                                }
                                                                else if (deviceInfos[i].kind === "videoinput") {
                                                                      this.headerVariableService.hasVideoDevice = true;
                                                                      this.headerVariableService.videoList.push(deviceInfos[i].deviceId);
                                                                }
                                                            }
                                                            //console.log(this.headerVariableService.micList, this.headerVariableService.videoList);
                                                            //Setting up letible values aysn. This is an example on how observers are created to do parallel job
                                                            this.headerVariableService.setMicDeviceVariable();
                                                            this.headerVariableService.setVideoDeviceVariable();
                                                            this.headerVariableService.setSpeakerDeviceVariable();
                                                        }).catch((e:any)=>{
                                                          console.error("Error enumerating devices", e);
                                                        });
                                            
                                            break;
                        
                                            case 'denied': 
                                            //console.log('case denied: asking permissions for video');
                                            this.showDialoge('Error','activity-outline','danger', "Camera permission is required to use this web-app. Contact admin."); 
                                            break;
                              
                                            case 'prompt': 
                                                                      
                                                                      this.showDialoge('Information','activity-outline','success', "Camera permission is required to use this web-app."); 
                                                                      //console.log('case prompt: asking permissions for video');
                                                                      navigator.mediaDevices.getUserMedia({audio: false, video: true})
                                                                      .then((stream) =>{
                                                                        //console.log('case prompt: Got stream, time diff :', Date.now() - now);
                                                                        //console.log(stream);
                                                                        stream.getTracks().forEach((track)=> {
                                                                          //console.log(track);
                                                                          track.stop();
                                                                        });
                                                                      })
                                                                      .catch((err) =>{
                                                                        //console.log('case prompt: GUM failed with error, time diff: ', Date.now() - now);
                                                                        //DOMException: Permission denied
                                                                        if(String(err).includes('Permission denied'))
                                                                        {
                                                                          this.showDialoge('Error','activity-outline','danger', "Camera permission is required to use this web-app. Contact admin."); 
                                                                           //this.detectDevices();
                                                                        }
                                                                        //console.log(err);
                                                                      });
                                                  break;
                        
                                      default: break;
                                }
                              })
                              .catch((error) => {
                                //console.log('Got error :', error);
                              })
                break;

          case 'denied': 
                                //console.log('case denied: asking permissions for audio');
                                this.showDialoge('Error','activity-outline','danger', "Microphone permission is required to use this web-app. Contact admin."); 
                 break;

          case 'prompt': 
                                    
                                    this.showDialoge('Information','activity-outline','success', "Microphone permission is required to use this web-app."); 
                                    //console.log('case prompt: asking permissions for audio');
                                    navigator.mediaDevices.getUserMedia({audio: true, video: false})
                                    .then((stream) =>{
                                      //console.log('case prompt: Got stream, time diff :', Date.now() - now);
                                      //console.log(stream);
                                      stream.getTracks().forEach((track)=> {
                                        //console.log(track);
                                        track.stop();
                                      });
                                      this.detectDevices();
                                    })
                                    .catch((err) =>{
                                      //console.log('case prompt: GUM failed with error, time diff: ', Date.now() - now);
                                      //DOMException: Permission denied
                                      if(String(err).includes('Permission denied'))
                                      {
                                        this.showDialoge('Error','activity-outline','danger', "Microphone permission is required to use this web-app. Contact admin."); 
                                         //this.detectDevices();
                                      }
                                      //console.log(err);
                                    });
                break;
          default: break;
    }

  })
  .catch((error) => {
    //console.log('Got error :', error);
  })
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
