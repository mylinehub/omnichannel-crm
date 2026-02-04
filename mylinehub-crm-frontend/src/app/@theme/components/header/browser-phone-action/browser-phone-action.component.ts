import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { NbIconLibraries, NbThemeService } from '@nebular/theme';
import { BrowserPhoneService } from '../../../../service/browser-phone/browser-phone.service';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { Subject } from 'rxjs';
import { HeaderVariableService } from '../../../../service/header-variable/header-variable.service';
import { SessionService } from '../../../../service/browser-phone/session/session.service';
import { RecordingService } from '../../../../service/browser-phone/recording/recording.service';
import { TransferService } from '../../../../service/browser-phone/transfer/transfer.service';
import { VideoService } from '../../../../service/browser-phone/video/video.service';

@Component({
  selector: 'ngx-browser-phone-action',
  templateUrl: './browser-phone-action.component.html',
  styleUrls: ['./browser-phone-action.component.scss']
})
export class BrowserPhoneActionComponent implements OnInit,OnChanges,OnDestroy {

  private destroy$: Subject<void> = new Subject<void>();
  redirectDelay: number = 0;
  themeSubscription: any;
  currentTheme: any;

  //Need to think how below variables will be used
  streamBuffer :any= 50;                 // The amount of rows to buffer in the Buddy Stream
 maxDataStoreDays:any = 0;          // Defines the maximum amount of days worth of data (calls, recordings, messages, etc) to store locally. 0=Stores all data always. >0 Trims n days back worth of data at various events where. 
posterJpegQuality:any = 0.6;    // The image quality of the Video Poster images
  videoResampleSize:any = "HD";               // The resample size (height) to re-render video that gets presented (sent). (SD = ???x360 | HD = ???x720 | FHD = ???x1080)
  recordingVideoSize:any = 'HD';             // The size/quality of the video track in the recordings (SD = 640x360 | HD = 1280x720 | FHD = 1920x1080)
 recordingVideoFps:any = 12;       // The Frame Per Second of the Video Track recording
  recordingLayout:any = "them-pnp";             // The Layout of the Recording Video Track (side-by-side | them-pnp | us-only | them-only)


  constructor(private iconLibraries: NbIconLibraries,
    private browserPhoneService:BrowserPhoneService,
    private constantService : ConstantsService,
    private themeService: NbThemeService,
    protected headerVariableService:HeaderVariableService,
    protected sessionService:SessionService,
    protected recordingService:RecordingService,
    protected transferService:TransferService,
    protected videoService:VideoService) { 
     
//   console.log("browser-phone-action-constructor");  
    this.iconLibraries.registerFontPack('awesome', { packClass: 'fab', iconClassPrefix: 'fa' });
    this.iconLibraries.registerFontPack('font-awesome', { packClass: 'fa', iconClassPrefix: 'fa' });
    this.iconLibraries.registerFontPack('fa-brands', { packClass: 'fa-brands', iconClassPrefix: 'fa' });

    this.themeSubscription = this.themeService.getJsTheme().subscribe(theme => {
      this.currentTheme = theme.name;
      });
  }


  ngOnInit(): void {
//   console.log("browser-phone-action-ng-onIt"); 
  }

  ngOnDestroy(): void {
//   console.log("browser-phone-action-ng-onDestoy");
    this.destroy$.next();
    this.destroy$.complete()
  }
  ngOnChanges(changes: SimpleChanges): void {
//   console.log("browser-phone-ng-onChanges");
  }

  mutession($event:any)
  {
     console.log($event);
     let value : boolean = false;

     if($event==='true')
     {
      value = true;
      this.sessionService.muteSession(this.browserPhoneService.lang,this.sessionService.currentConnectedSessionData);
     }
     else if ($event==='false')
     {
      value = false;
      this.sessionService.unmuteSession(this.browserPhoneService.lang,this.sessionService.currentConnectedSessionData);
     }
  }

  holdSession($event:any)
  {
     console.log($event);
     let value : boolean = false;

     if($event==='true')
     {
      value = true;
      this.sessionService.holdSession(this.browserPhoneService.lang,this.sessionService.currentConnectedSessionData);
     }
     else if ($event==='false')
     {
      value = false;
      this.sessionService.unholdSession(this.browserPhoneService.lang,this.sessionService.currentConnectedSessionData);
     }
  }

  endSession()
  {
      this.sessionService.endSession(this.browserPhoneService.lang,this.sessionService.currentConnectedSessionData);
  }

  attendedTransferSession($event:any)
  {
     console.log($event);
     let value : boolean = false;

     if($event==='true')
     {
      value = true;
      // this.browserPhoneService.userAgent,this.browserPhoneService.lang,
      this.transferService.attendedTransfer(this.browserPhoneService.userAgent,this.browserPhoneService.lang,this.sessionService.currentConnectedSessionData);
     }
     else if ($event==='false')
     {
      value = false;
      this.transferService.cancelTransferSession(this.browserPhoneService.lang,this.sessionService.currentConnectedSessionData);
     }
  }

  startUnAttendedTransferSession()
  {
     this.transferService.startTransferSession(this.browserPhoneService.lang,
      this.sessionService.currentConnectedSessionData);
  }

  recordingFunc($event:any)
  {
     console.log($event);
     let value : boolean = false;

     if($event==='true')
     {
      value = true;
      this.recordingService.startRecording(this.sessionService.currentConnectedSessionData);
     }
     else if ($event==='false')
     {
      value = false;
      this.recordingService.stopRecording(this.sessionService.currentConnectedSessionData);
     }
  }

  conferenceCall($event:any)
  {
     console.log($event);
     let value : boolean = false;

     if($event==='true')
     {
      value = true;
     }
     else if ($event==='false')
     {
      value = false;
     }
  }

  videoFunc($event:any)
  {
     console.log($event);
     let value : boolean = false;

     if($event==='true')
     {
      value = true;
      this.videoService.switchVideoSource(this.browserPhoneService.lang,this.sessionService.currentConnectedSessionData,this.headerVariableService.currentVideoStringValue);
     }
     else if ($event==='false')
     {
      value = false;
      this.videoService.disableVideoStream(this.browserPhoneService.lang,this.sessionService.currentConnectedSessionData);
     }
  }

  connectYouTubeStream()
  {
    
  }

//   shareFile()
//   {

//   }

//   shareScreen()
//   {

//   }
}
