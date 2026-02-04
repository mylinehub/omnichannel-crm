import { Injectable } from '@angular/core';
import { HeaderVariableService } from '../header-variable/header-variable.service';
import { UtilsService } from '../browser-phone/utils/utils.service';

@Injectable({
  providedIn: 'root'
})
export class PhoneMusicService {

      audioBlobs : any = {}

      constructor(protected headerVariableService : HeaderVariableService,
        protected utilsService : UtilsService,
      ) { 
        this.preloadAudioFiles();
      }


      preloadAudioFiles(){
        this.audioBlobs.Alert = { file : "Alert.mp3", url : this.headerVariableService.hostingPrefix +"assets/media/Alert.mp3" }
        this.audioBlobs.Ringtone = { file : "Ringtone_1.mp3", url : this.headerVariableService.hostingPrefix +"assets/media/Ringtone_1.mp3" }
        this.audioBlobs.speech_orig = { file : "speech_orig.mp3", url : this.headerVariableService.hostingPrefix +"assets/media/speech_orig.mp3" }
        this.audioBlobs.Busy_UK = { file : "Tone_Busy-UK.mp3", url : this.headerVariableService.hostingPrefix +"assets/media/Tone_Busy-UK.mp3" }
        this.audioBlobs.Busy_US = { file : "Tone_Busy-US.mp3", url : this.headerVariableService.hostingPrefix +"assets/media/Tone_Busy-US.mp3" }
        this.audioBlobs.CallWaiting = { file : "Tone_CallWaiting.mp3", url : this.headerVariableService.hostingPrefix +"assets/media/Tone_CallWaiting.mp3" }
        this.audioBlobs.Congestion_UK = { file : "Tone_Congestion-UK.mp3", url : this.headerVariableService.hostingPrefix +"assets/media/Tone_Congestion-UK.mp3" }
        this.audioBlobs.Congestion_US = { file : "Tone_Congestion-US.mp3", url : this.headerVariableService.hostingPrefix +"assets/media/Tone_Congestion-US.mp3" }
        this.audioBlobs.EarlyMedia_Australia = { file : "Tone_EarlyMedia-Australia.mp3", url : this.headerVariableService.hostingPrefix +"assets/media/Tone_EarlyMedia-Australia.mp3" }
        this.audioBlobs.EarlyMedia_European = { file : "Tone_EarlyMedia-European.mp3", url : this.headerVariableService.hostingPrefix +"assets/media/Tone_EarlyMedia-European.mp3" }
        this.audioBlobs.EarlyMedia_Japan = { file : "Tone_EarlyMedia-Japan.mp3", url : this.headerVariableService.hostingPrefix +"assets/media/Tone_EarlyMedia-Japan.mp3" }
        this.audioBlobs.EarlyMedia_UK = { file : "Tone_EarlyMedia-UK.mp3", url : this.headerVariableService.hostingPrefix +"assets/media/Tone_EarlyMedia-UK.mp3" }
        this.audioBlobs.EarlyMedia_US = { file : "Tone_EarlyMedia-US.mp3", url : this.headerVariableService.hostingPrefix +"assets/media/Tone_EarlyMedia-US.mp3" }
        
        Object.keys(this.audioBlobs).forEach((item:any,i:any) =>{
            var oReq = new XMLHttpRequest();
            oReq.open("GET", this.audioBlobs[item].url, true);
            //console.log(oReq);
            oReq.responseType = "blob";
            oReq.onload = (oEvent)=> {
                //console.log('oReq',oReq);
                //console.log('oEvent',oEvent);
                var reader = new FileReader();
                reader.readAsDataURL(oReq.response);
                //console.log('oReq.response',oReq.response);
                reader.onload = ()=> {
                  //console.log('reader',reader);
                  //console.log('reader.result',reader.result);
                  this.audioBlobs[item].blob = reader.result;
                }
            }
            oReq.send();
        });
        
        //console.log('this.audioBlobs',this.audioBlobs);
    }

    

      ringMessageMusic(message:any){
        //console.log("I am in ringMessageMusic");
        // Handle Stream Not visible
        // =========================
      // Play Alert
      //console.log("Audio:", this.phoneMusicService.audioBlobs.Alert.url);
      let ringer:any = new Audio(this.audioBlobs.Alert.blob);
      ringer.preload = "auto";
      ringer.loop = false;
      ringer.oncanplaythrough = (e:any)=> {
          if (typeof ringer.sinkId !== 'undefined' && this.headerVariableService.currentSpeakerStringValue != "default") {
              ringer.setSinkId(this.headerVariableService.currentSpeakerStringValue).then(() =>{
                  //console.log("Set sinkId to:", this.headerVariableService.currentSpeakerStringValue);
              }).catch((e:any)=>{
                  console.warn("Failed not apply setSinkId.", e);
              });
          }
          //If there has been no interaction with the page at all... this page will not work
          ringer.play().then(()=>{
              // Audio Is Playing
          }).catch((e)=>{
              console.warn("Unable to play audio file.", e);
          });
      }
      message.data.ringerObj = ringer;
    }


    ringAlertMusic(){
          // console.log("I am in ringMessageMusic");
          // Handle Stream Not visible
          // =========================
        // Play Alert
        // console.log("Audio:", this.audioBlobs.Alert.url);
        let ringer:any = new Audio(this.audioBlobs.Alert.blob);
        ringer.preload = "auto";
        ringer.loop = false;
        ringer.oncanplaythrough = (e:any)=> {
            if (typeof ringer.sinkId !== 'undefined' && this.headerVariableService.currentSpeakerStringValue != "default") {
                ringer.setSinkId(this.headerVariableService.currentSpeakerStringValue).then(() =>{
                    // console.log("Set sinkId to:", this.headerVariableService.currentSpeakerStringValue);
                }).catch((e:any)=>{
                    console.warn("Failed not apply setSinkId.", e);
                });
            }
            //If there has been no interaction with the page at all... this page will not work
            ringer.play().then(()=>{
                // Audio Is Playing
            }).catch((e)=>{
                console.warn("Unable to play audio file.", e);
            });
        }
      }

    ringIcomingCallMusic()
    {
             // Play Ring Tone
             console.log("Audio:", this.audioBlobs.Ringtone.url);
             let ringer:any = new Audio(this.audioBlobs.Ringtone.blob);
             ringer.preload = "auto";
             ringer.loop = true;
             ringer.oncanplaythrough = (e:any)=> {
                 if (typeof ringer.sinkId !== 'undefined' && this.headerVariableService.currentMicStringValue != "default") {
                     ringer.setSinkId(this.headerVariableService.currentMicStringValue).then(()=> {
                         console.log("Set sinkId to:", this.headerVariableService.currentMicStringValue);
                     }).catch((e:any)=>{
                         console.warn("Failed not apply setSinkId.", e);
                     });
                 }
                 // If there has been no interaction with the page at all... this page will not work
                 ringer.play().then(()=>{
                     // Audio Is Playing
                 }).catch((e:any)=>{
                     console.warn("Unable to play audio file.", e);
                 }); 
             }

             return ringer;
    }

    ringCallWaitingMusic()
    {
        console.log("Audio:", this.audioBlobs.CallWaiting.url);
        let ringer:any = new Audio(this.audioBlobs.CallWaiting.blob); 
        ringer.preload = "auto";
        ringer.loop = false;
        ringer.oncanplaythrough = (e:any)=> {
            if (typeof ringer.sinkId !== 'undefined' && this.headerVariableService.currentMicStringValue != "default") {
                ringer.setSinkId(this.headerVariableService.currentMicStringValue).then(()=> {
                    console.log("Set sinkId to:", this.headerVariableService.currentMicStringValue);
                }).catch((e:any)=>{
                    console.warn("Failed not apply setSinkId.", e);
                });
            }
            // If there has been no interaction with the page at all... this page will not work
            ringer.play().then(()=>{
                // Audio Is Playing
            }).catch((e:any)=>{
                console.warn("Unable to play audio file.", e);
            }); 
        }

        return ringer;
    }

    ringEarlyMedia(sessionData:any){
        let soundFile = this.audioBlobs.EarlyMedia_European;
        if(this.utilsService.userLocale().indexOf("us") > -1) soundFile = this.audioBlobs.EarlyMedia_US;
        if(this.utilsService.userLocale().indexOf("gb") > -1) soundFile = this.audioBlobs.EarlyMedia_UK;
        if(this.utilsService.userLocale().indexOf("au") > -1) soundFile = this.audioBlobs.EarlyMedia_Australia;
        if(this.utilsService.userLocale().indexOf("jp") > -1) soundFile = this.audioBlobs.EarlyMedia_Japan;
  
        // Play Early Media
        console.log("Audio:", soundFile.url);
        if(sessionData.data.earlyMedia){
            // There is already early media playing
            // onProgress can be called multiple times
            // Don't add it again
            console.log("Early Media already playing");
        }
        else {
            let earlyMedia:any = new Audio(soundFile.blob);
            earlyMedia.preload = "auto";
            earlyMedia.loop = true;
            earlyMedia.oncanplaythrough = (e:any)=> {
                if (typeof earlyMedia.sinkId !== 'undefined' && this.headerVariableService.currentSpeakerStringValue != "default") {
                    earlyMedia.setSinkId(this.headerVariableService.currentSpeakerStringValue).then(()=> {
                        console.log("Set sinkId to:", this.headerVariableService.currentSpeakerStringValue);
                    }).catch((e:any)=>{
                        console.warn("Failed not apply setSinkId.", e);
                    });
                }
                earlyMedia.play().then(()=>{
                    // Audio Is Playing
                }).catch((e:any)=>{
                    console.warn("Unable to play audio file.", e);
                }); 
            }
            sessionData.data.earlyMedia = earlyMedia;
        }
    }
}
