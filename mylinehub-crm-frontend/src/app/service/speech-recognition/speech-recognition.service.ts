import { Injectable, OnDestroy } from '@angular/core';


declare var webkitSpeechRecognition: any;

@Injectable({
  providedIn: 'root'
})
export class SpeechRecognitionService {

   recognition =  new webkitSpeechRecognition();

  listeningNow:any = false;
  listeningToolTip="start-listening";
  messageValue="";
  
  // public text = '';
  transcriptWords:string='';
  setSendMessageId:any = null;
  eventListnerObject: any;

  constructor() { }

  init() {

    this.recognition.interimResults = true;
    this.recognition.lang = 'en-US';

    this.recognition.addEventListener('result', (e:any) => {
      console.log("I am in init for speech recognition class");
      const transcript = Array.from(e.results)
        .map((result) => result[0])
        .map((result) => result.transcript)
        .join('');
      this.transcriptWords = transcript;
      console.log(transcript);
    });
  }

  start() {
    this.listeningNow = true;
    this.recognition.start();
    console.log("Speech recognition started")
    this.eventListnerObject  = this.recognition.addEventListener('end', (condition:any) => {
      console.log("I am end");
      if (!this.listeningNow) {
        this.recognition.stop();
        console.log("End speech recognition")
      } else {
        this.wordConcat()
        this.recognition.start();
      }
    });
    this.listeningToolTip="stop-listening";
  }


  stop() {
    this.listeningNow = false;
    this.wordConcat()
    this.recognition.stop();
    this.listeningToolTip="start-listening";
    console.log("End speech recognition")
  }

  wordConcat() {
    if(this.transcriptWords != '')
    {
      this.messageValue = this.messageValue + ' ' + this.capitalizeFirstLetter(this.transcriptWords) + '.';
    }
    this.transcriptWords = '';

    if(this.messageValue == "" || this.messageValue == ' '  || this.messageValue == "   " || this.messageValue == "    ")
    {
      if(this.setSendMessageId != null)
      {
        clearTimeout(this.setSendMessageId);
        this.setSendMessageId = null;
      }      
    }
    else
    {
      if(this.setSendMessageId == null)
      {
        
        console.log("setSendMessageId is set now");
        this.setSendMessageId = setTimeout(()=>{ 
                                                 this.setSendMessageId = null;
                                                 document.getElementById("sendMessage").click();
                                                },4000);
      }
      else{
        console.log("setSendMessageId is cleared");
        clearTimeout(this.setSendMessageId);
        console.log("setSendMessageId is set again");
        this.setSendMessageId = setTimeout(()=>{ 
                                                this.setSendMessageId = null;
                                                document.getElementById("sendMessage").click();
                                               },4000);
      }  
    }
  }

  capitalizeFirstLetter(value:string) {
    return value[0].toUpperCase() + value.slice(1);
  }

}
