import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { NbDialogRef, NbDialogService, NbThemeService } from '@nebular/theme';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { BrowserPhoneService } from '../../../../service/browser-phone/browser-phone.service';
import { Subject, of } from 'rxjs';
import { HeaderVariableService } from '../../../../service/header-variable/header-variable.service';
import { DialogComponent } from '../../../../pages/employee/all-employees/dialog/dialog.component';
import { VideoDialogDataService } from './video-dialog-data-service/video-dialog-data.service';
import { MessageListDataService } from '../message-list/message-list-data-service/message-list-data.service';
import { AddCallComponent } from './add-call/add-call.component';
import { StompService } from '../../../../service/stomp/stomp.service';

@Component({
  selector: 'ngx-video-dialog',
  templateUrl: './video-dialog.component.html',
  styleUrls: ['./video-dialog.component.scss']
})
export class VideoDialogComponent implements OnInit,OnChanges,OnDestroy {
  
  @ViewChild('addConferenceMember') input;
  private destroy$: Subject<void> = new Subject<void>();
  redirectDelay: number = 0;
  themeSubscription: any;
  currentTheme: any;
  liveChat: any = "";

  isMessageContentWrapped: boolean = true;
  isAllParticipantsWrapped: boolean = true;

  videoBlockContainerWidth: number = 100;
  videoBlockContainerHeight: number = 100;

  mainVideoWrapperWidth: number = 100;
  mainVideoWrapperHeight: number = 100;

  messageContentWrapperWidth: number = 0;
  messageContentWrapperHeight: number = 100;

  allParticipantsWrapperWidth: number = 100;
  allParticipantsWrapperHeight: number = 0;

  speakerVideoWidth: number = 100;
  speakerVideoHeight: number = 100;



  //seeAll:any = true;

  constructor(protected ref: NbDialogRef<VideoDialogComponent>,
    private browserPhoneService:BrowserPhoneService,
    private constantService : ConstantsService,
    private themeService: NbThemeService,
    protected headerVariableService:HeaderVariableService,
    private dialogService: NbDialogService,
    protected videoDialogDataService:VideoDialogDataService,
    protected messageListDataService:MessageListDataService,
    protected stompService:StompService,) {
      //console.log("video-dialog-constructor");
      this.videoDialogDataService.myExtension = ConstantsService.user.extension;
      this.themeSubscription = this.themeService.getJsTheme().subscribe(theme => {
        this.currentTheme = theme.name;
        });
  }
  
  ngOnInit(): void { 

    console.log("video-dialog-ng-onIt");
    makeResizableDiv('.video');
    const element:any = document.querySelector('.video');
    this.headerVariableService.boundingClientRect = element.getBoundingClientRect();
    
    console.log("Setting current user values");
    this.videoDialogDataService.currentUser = ConstantsService.user;
    this.videoDialogDataService.myExtension = ConstantsService.user.extension;
    this.videoDialogDataService.fullScreenEnabled = false;

    // this.headerVariableService.remoteVideo = document.getElementById('remoteVideo');

    if(this.headerVariableService.startVideoFullScreen)
      {
        console.log("Start video full screen is enabled");
        this.videoDialogDataService.fullScreenEnabled = true;
        this.fullScreen();
      }
    
    console.log("Setting all conference members variable. Even if its conference it is set. This is initial value");
    this.videoDialogDataService.filteredOptions$ = of(this.messageListDataService.allEmployeesData);

    if(!this.headerVariableService.isConference)
      {
        console.log("It is not conference call. So it is video call. Setting video stage");
        this.setUpVideosContainers();
        this.setUpVideoSources();
      }
    else if (this.headerVariableService.isConference)
      {
        console.log("It is conference call. Setting conference stage");
        this.setUpConference();
      }

  }

  setUpVideosContainers()
  {
    console.log("setUpVideosContainers");
    if(this.videoDialogDataService.mainVideoData.isVideoOn)
      {
        console.log("Setting main video as its on");
        this.videoDialogDataService.mainVideoData.videoContainer = document.getElementById('mainVideo');
      }
    else{
        console.log("Main video is off");
        this.videoDialogDataService.mainVideoData.videoContainer = null;
    }
   
    console.log("Setting participants video if its on");
    this.videoDialogDataService.allConferenceMembers.forEach((element:any,i:number)=>{

      if(element.isVideoOn)
        {
          this.videoDialogDataService.allConferenceMembers[i].videoContainer = document.getElementById('video-'+element.extension);
        }
      else{
        this.videoDialogDataService.allConferenceMembers[i].videoContainer = null;
      }
    });
  }
 

  //id="video-{{ data.extension }}"
  //id="mainVideo"
  setUpVideoSources()
  {
      console.log("setUpVideos");
      if(this.headerVariableService.remoteVideoStream.getVideoTracks().length >= 1){
        console.log("this.headerVariableService.remoteVideoStream.getVideoTracks().length >= 1");
              let remoteVideoStreamTracks = this.headerVariableService.remoteVideoStream.getVideoTracks();
              remoteVideoStreamTracks.forEach((remoteVideoStreamTrack:any,i)=> {
                  let thisRemoteVideoStream:any = new MediaStream();
                  thisRemoteVideoStream.trackID = remoteVideoStreamTrack.id;
                  thisRemoteVideoStream.mid = remoteVideoStreamTrack.mid;
                  remoteVideoStreamTrack.onended = ()=> {
                      console.log("Video Track Ended: ", remoteVideoStreamTrack.mid);
                      // RedrawStage(lineObj.LineNumber, true);
                      // Add allConferenceMembers varuable inside redraw Stage
                  }
                   thisRemoteVideoStream.addTrack(remoteVideoStreamTrack);

                   // Add video element here for center

                    let videoEl:any = "";

                    let videoObj = videoEl.get(0);
                    videoObj.srcObject = thisRemoteVideoStream;
                    videoObj.onloadedmetadata = (e:any) =>{
                    videoObj.play();
                    videoEl.show();
                    videoEl.parent().show();
                    console.log("Playing Video Stream MID:", thisRemoteVideoStream.mid);
                    // RedrawStage(lineObj.LineNumber, true);
                   }

                   console.log("Added Video Element MID:", thisRemoteVideoStream.mid);
            });
    }
    else {
        console.log("No Video Streams while setting up videos");
        //Close all video tracks
        console.log("Close all video tracks");
        this.videoDialogDataService.allConferenceMembers.forEach((element:any,i:number)=>{
          this.videoDialogDataService.allConferenceMembers[i].videoContainer = null;
          this.videoDialogDataService.allConferenceMembers[i].isVideoOn = false;
        });
    }
  }

  setUpConference()
  {

  }

   //RedrawVideoStage
   redrawStage(sessionData:any, videoChanged:any){
    //console.log("I am in redrawStage");
    let videoContainer:any = "this.headerVariableService.videoContainer";
    let session = sessionData;
    if(session == null) return;

    let isVideoPinned = false;
    let pinnedVideoID = "";

    // Count and Tag Videos
    let videoCount = 0;
    videoContainer.find('video').each((video:any,i:any)=> {
        let thisRemoteVideoStream = video.srcObject;
        let videoTrack = thisRemoteVideoStream.getVideoTracks()[0];
        let videoTrackSettings = videoTrack.getSettings();
        let srcVideoWidth = (videoTrackSettings.width)? videoTrackSettings.width : video.videoWidth;
        let srcVideoHeight = (videoTrackSettings.height)? videoTrackSettings.height : video.videoHeight;

        if(thisRemoteVideoStream.mid) {
            thisRemoteVideoStream.channel = "unknown"; // Asterisk Channel
            thisRemoteVideoStream.CallerIdName = "";
            thisRemoteVideoStream.CallerIdNumber = "";
            thisRemoteVideoStream.isAdminMuted = false;
            thisRemoteVideoStream.isAdministrator = false;
            if(session && session.data && session.data.videoChannelNames){
                session.data.videoChannelNames.forEach((videoChannelName:any)=>{
                    if(thisRemoteVideoStream.mid == videoChannelName.mid){
                        thisRemoteVideoStream.channel = videoChannelName.channel;
                    }
                });
            }
            if(session && session.data && session.data.ConfbridgeChannels){
                session.data.ConfbridgeChannels.forEach((ConfbridgeChannel:any)=>{
                    if(ConfbridgeChannel.id == thisRemoteVideoStream.channel){
                        thisRemoteVideoStream.CallerIdName = ConfbridgeChannel.caller.name;
                        thisRemoteVideoStream.CallerIdNumber = ConfbridgeChannel.caller.number;
                        thisRemoteVideoStream.isAdminMuted = ConfbridgeChannel.muted;
                        thisRemoteVideoStream.isAdministrator = ConfbridgeChannel.admin;
                    }
                });
            }
            // //console.log("Track MID :", thisRemoteVideoStream.mid, thisRemoteVideoStream.channel);
        }

        // Remove any in the preview area
        // if(videoChanged){
        //     $("#line-" + lineNum + "-preview-container").find('video').each((video:any, i:any)=> {
        //         if(video.id.indexOf("copy-") == 0){
        //             video.remove();
        //         }
        //     });
        // }

        // Count Videos
        if(videoTrack.readyState == "live" && srcVideoWidth > 10 && srcVideoHeight >= 10){
            // A valid and live video is pinned
            isVideoPinned = true;
            pinnedVideoID = thisRemoteVideoStream.trackID;
        }
        // Count All the videos
        if(videoTrack.readyState == "live" && srcVideoWidth > 10 && srcVideoHeight >= 10) {
            videoCount ++;
            //console.log("Display Video - ", videoTrack.readyState, "MID:", thisRemoteVideoStream.mid, "channel:", thisRemoteVideoStream.channel, "src width:", srcVideoWidth, "src height", srcVideoHeight);
        }
        else{
            //console.log("Hide Video - ", videoTrack.readyState ,"MID:", thisRemoteVideoStream.mid);
        }


    });
    
    if(isVideoPinned) videoCount = 1;


    // videoAspectRatio (1|1.33|1.77) is for the peer video, so can technically be used here
    // default ia 4:3
    let Margin = 3;
    let videoRatio = 0.750; // 0.5625 = 9/16 (16:9) | 0.75   = 3/4 (4:3)
    if(this.headerVariableService.videoAspectRatio == "" || this.headerVariableService.videoAspectRatio == "1.33") videoRatio = 0.750;  
    if(this.headerVariableService.videoAspectRatio == "1.77") videoRatio = 0.5625;
    if(this.headerVariableService.videoAspectRatio == "1") videoRatio = 1;
    let stageWidth = videoContainer.outerWidth() - (Margin * 2);
    let stageHeight = videoContainer.outerHeight() - (Margin * 2);
    let maxWidth = 0;
    let i = 1;
    while (i < 5000) {
        let w:any = 'StageArea(i, videoCount, stageWidth, stageHeight, Margin, videoRatio);'
        if (w === false) {
            maxWidth =  i - 1;
            break;
        }
        i++;
    }
    maxWidth = maxWidth - (Margin * 2);

    // Layout Videos
    videoContainer.find('video').each((video:any,i:any)=> {
        let thisRemoteVideoStream = video.srcObject;
        let videoTrack = thisRemoteVideoStream.getVideoTracks()[0];
        let videoTrackSettings = videoTrack.getSettings();
        let srcVideoWidth = (videoTrackSettings.width)? videoTrackSettings.width : video.videoWidth;
        let srcVideoHeight = (videoTrackSettings.height)? videoTrackSettings.height : video.videoHeight;

        let videoWidth = maxWidth;
        let videoHeight = maxWidth * videoRatio;

        // Set & Show
        if(isVideoPinned){
            // One of the videos are pinned
            if(pinnedVideoID == video.srcObject.trackID){
                
            } else {
                // Put the videos in the preview area
                if(videoTrack.readyState == "live" && srcVideoWidth > 10 && srcVideoHeight >= 10) {
                    if(videoChanged){
                        // let videoEl = $("<video />", {
                        //     id: "copy-"+ thisRemoteVideoStream.id,
                        //     muted: true,
                        //     autoplay: true,
                        //     playsinline: true,
                        //     controls: false
                        // });
                        // let videoObj = videoEl.get(0);
                        // videoObj.srcObject = thisRemoteVideoStream;
                        // $("#line-" + lineNum + "-preview-container").append(videoEl);
                    }
                }
            }
        }
        else {
            // None of the videos are pinned
            if(videoTrack.readyState == "live" && srcVideoWidth > 10 && srcVideoHeight >= 10) {
                // Unpinned 
                // Unpinned Actions
            }
        }

        // Populate Caller ID
        let adminMuteIndicator = "";
        let administratorIndicator = "";
        if(thisRemoteVideoStream.isAdminMuted == true){
            adminMuteIndicator = "<i class=\"fa fa-microphone-slash\" style=\"color:red\"></i>&nbsp;"
        }
        if(thisRemoteVideoStream.isAdministrator == true){
            administratorIndicator = "<i class=\"fa fa-user\" style=\"color:orange\"></i>&nbsp;"
        }
        if(thisRemoteVideoStream.CallerIdName == ""){
            thisRemoteVideoStream.CallerIdName = 'FindBuddyByIdentity(session.data.buddyId).CallerIDName';
        }
     });
  }

  
  changeParticipantAudio(j){
    console.log("changeParticipantAudio",j);
  }

  changeParticipantVideo(j){
    console.log("changeParticipantVideo",j);
  }

  
  onInternalParticipantAdded($event) {
    console.log('onSelectionChange');
    console.log($event);
    //Call to member
    //Add member to conference, if user accepts call let him be in call or else remove record if call is not picked
  }

  
  sendMessage()
  {

  }

  changeShare()
  {
    if(this.videoDialogDataService.screenShare)
      {
        this.videoDialogDataService.screenShare = false;
      }
    else{
      this.videoDialogDataService.screenShare = true;   
    }
  }

  ngOnDestroy(): void {
    //console.log("video-dialog-ng-onDestoy");
    this.destroy$.next();
    this.destroy$.complete()
  }

  ngOnChanges(changes: SimpleChanges): void {
    //console.log("video-dialog-ng-onChanges");
  }

  downloadMessageFile(i,j){
    console.log("downloadMessageFile i : ",i);
    console.log("downloadMessageFile j : ",j);
  }

  dismiss() {
    this.ref.close();
  }



  messageContentWrapperFunc()
  {
    if(this.isMessageContentWrapped)
    {
      this.isMessageContentWrapped = false;
      

      if(this.videoDialogDataService.fullScreenEnabled)
      {
        this.mainVideoWrapperWidth = 70;
        this.mainVideoWrapperHeight = 100;
        this.messageContentWrapperWidth = 30;
        this.messageContentWrapperHeight = 100;
      }
      else{
        this.mainVideoWrapperWidth = 50;
        this.mainVideoWrapperHeight = 100;
        this.messageContentWrapperWidth = 50;
        this.messageContentWrapperHeight = 100;
      }
      this.liveChat = "Chat";
    }
    else{

      this.isMessageContentWrapped = true;
      this.mainVideoWrapperWidth = 100;
      this.mainVideoWrapperHeight = 100;
      this.messageContentWrapperWidth = 0;
      this.messageContentWrapperHeight = 100;
      this.liveChat = "";
    }

  }

  allParticipantsWrapperFunc()
  {

    if(this.isAllParticipantsWrapped)
    {
      this.isAllParticipantsWrapped = false;
      this.videoBlockContainerWidth = 100;
      this.videoBlockContainerHeight = 70;
      this.allParticipantsWrapperWidth = 100;
      this.allParticipantsWrapperHeight = 30;
    }
    else{
      this.isAllParticipantsWrapped = true;
      this.videoBlockContainerWidth = 100;
      this.videoBlockContainerHeight = 100;
      this.allParticipantsWrapperWidth = 100;
      this.allParticipantsWrapperHeight = 0;

    }

  }


  fullScreen(){

    // console.log(this.headerVariableService.fullScreenEnabled);
    const element =  Array.from(document.getElementsByClassName('video') as HTMLCollectionOf<HTMLElement>)[0];
    if(this.videoDialogDataService.fullScreenEnabled)
    {
      this.headerVariableService.dragPosition = this.headerVariableService.previousDragPosition;
      // console.log('this.headerVariableService.dragPosition after enabled',this.headerVariableService.dragPosition);
      // console.log('this.headerVariableService.previousDragPosition after enabled',this.headerVariableService.previousDragPosition);
      // console.log('this.headerVariableService.width after enabled',this.headerVariableService.width);
      // console.log('this.headerVariableService.height after enabled',this.headerVariableService.height);
      element.style.width =this.headerVariableService.participantWidth + 'px';
      element.style.height =this.headerVariableService.participantHeight + 'px';
      this.videoDialogDataService.fullScreenEnabled = false;

      let h = this.headerVariableService.participantHeight;
      let textElement = document.getElementById('speaker-center');
      textElement.style.fontSize = h/14 + 'px';
    }
    else
    {
      // console.log('window.innerHeight',window.innerHeight);
      // console.log('window.innerWidth',window.innerWidth);
      // console.log('window.outerHeight',window.outerHeight);
      // console.log('window.outerWidth',window.outerWidth);

      
      //element.style.x = 0;
      //element.style.y = 0;
      //element.closest('.cdk-global-overlay-wrapper').addClass('stick-right');

       if(Number(String(element.style.width).split('.')[0].trim()) != 0  && !Number.isNaN(Number(String(element.style.width).split('.')[0].trim())))
       {
          this.headerVariableService.participantWidth = Number(String(element.style.width).split('.')[0].trim());
          this.headerVariableService.participantHeight = Number(String(element.style.height).split('.')[0].trim());
          this.headerVariableService.previousDragPosition = this.headerVariableService.dragPosition;
       }
     
   //  console.log('this.headerVariableService.width',this.headerVariableService.width);
    //  console.log('this.headerVariableService.dragPosition after enabled',this.headerVariableService.dragPosition);
    //  console.log('this.headerVariableService.previousDragPosition after enabled',this.headerVariableService.previousDragPosition);

      this.headerVariableService.dragPosition = {x: 0, y: 0};

      element.style.width = window.innerWidth + 'px';
      element.style.height = window.innerHeight + 'px';

      let h = window.innerHeight;
      let textElement = document.getElementById('speaker-center');
      textElement.style.fontSize = h/14 + 'px';
      
     // element.style.backgroundPositionX = '0.0000rem';
      //element.style.backgroundPositionY = '0.0000rem';
      this.videoDialogDataService.fullScreenEnabled = true;

    }
    // this.remoteVideoWidth = 100;
    // this. remoteVideoHeight = 100;

    if(!this.isMessageContentWrapped)
    document.getElementById('messageContentButton').click();

    if(!this.isAllParticipantsWrapped)
    document.getElementById('allParticipantsButton').click();
    //document.getElementById('messageContentButton').click();
  }

  onChange() {
    // console.log('onChange');
    if(this.input.nativeElement.value=='' || this.input.nativeElement.value==' ')
    {
      this.videoDialogDataService.filteredOptions$ = of(this.messageListDataService.allEmployeesData);
    }
    else{
      this.videoDialogDataService.filteredOptions$ = this.messageListDataService.getFilteredOptions(this.input.nativeElement.value);
    }
  }


  addPstnCall()
  {
    this.showAddCallDialoge(); 
  }

  showAddCallDialoge() {
      this.dialogService.open(AddCallComponent, {
        context: {
          // title: status,
          // data: message,
          // header: header,
          // icon: icon
        },
      }).onClose.subscribe((type) => {
          console.log(type);
          if(type =="tryingCall")
            {
              
            }
      })      ;
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


function makeResizableDiv(div) {
  var previousX:any;
  var set = false;

  const element =  Array.from(document.getElementsByClassName('video') as HTMLCollectionOf<HTMLElement>)[0];
  const resizers = document.querySelectorAll(div + ' .resizer')
  for (let i = 0;i < resizers.length; i++) {
    const currentResizer = resizers[i];
    currentResizer.addEventListener('mousedown', (e)=> {
     // console.log("Inside currentResizer : mouse down");
      e.preventDefault()
      window.addEventListener('mousemove', resize)
      window.addEventListener('mouseup', stopDesktopResize)
    })

    currentResizer.addEventListener('touchstart', (e)=> {
      // console.log("Inside currentResizer : mouse down");
       e.preventDefault()
       window.addEventListener('touchstart', resize)
       window.addEventListener('touchend', stopMouseResize)
     })
    
    function resize(e) {
      //console.log(e);
      // console.log(element.getBoundingClientRect());
      // console.log("currentResizer",currentResizer);
      var currentObject = element.getBoundingClientRect();
      var textElement = document.getElementById('speaker-center');

      if (currentResizer.classList.contains('bottom-right')) {
       
       // console.log("Going inside current resizer buttom right");
        
        if((Number(currentObject.top) <= 2 || Number(currentObject.right) <= 2 ||Number(currentObject.bottom) <= 150 ||Number(currentObject.left) <= 2))
        {
            //console.log("Going nagative bottom-right");

            if(set&&(previousX>Number(e.pageX)))
            {
              //console.log("Inside conditon");
              // console.log('e.pageX',e.pageX);
              // console.log('currentObject.left',currentObject.left);
              let w = e.pageX - currentObject.left;
              let h = e.pageX - currentObject.left;
              element.style.width = w + 'px';
              element.style.height = h + 'px';
              console.log('Height : ',h);
              // console.log('this.videoDialogDataService.speakerTextSize : ',this.videoDialogDataService.speakerTextSize);
              textElement.style.fontSize = h/14 + 'px';
            }
           
        }
        else{
              //console.log("Not in nagative bottom-right");
              previousX = Number(e.pageX);
              set = true;
              let w = e.pageX - currentObject.left;
              let h = e.pageX - currentObject.left;
              element.style.width = w + 'px';
              element.style.height = h + 'px';
              console.log('Height : ',h);
              textElement.style.fontSize = h/14 + 'px';
        }

      }
      else if (currentResizer.classList.contains('bottom-left')) {
        if(Number(currentObject.top) <= 2 || Number(currentObject.right) <= 2 ||Number(currentObject.bottom) <= 150 ||Number(currentObject.left) <= 2)
        {
         // console.log("Going nagative bottom-left");
          if(set&&(previousX<Number(e.pageX)))
          {
            let w = currentObject.right - e.pageX;
            let h = currentObject.right - e.pageX;
            element.style.width = w + 'px';
            element.style.height = h + 'px';
            console.log('Height : ',h);
            textElement.style.fontSize = h/14 + 'px';
          }
        }
        else{
             // console.log("Not in nagative bottom-left");
              previousX = Number(e.pageX);
              set = true;
              let w = currentObject.right - e.pageX;
              let h = currentObject.right - e.pageX;
              element.style.width = w + 'px';
              element.style.height = h + 'px';
              console.log('Height : ',h);
              textElement.style.fontSize = h/14 + 'px';
        }
      }
      // else if (currentResizer.classList.contains('top-right')) {
      //   element.style.width = element.style.width - (e.pageX - currentObject.right)  + 'px'
      // }
      // else if (currentResizer.classList.contains('top-left')) {
      //   element.style.width = element.style.width - (e.pageX - currentObject.left)  + 'px'
      // }

      //       bottom-right:
      //   new_width = element_original_width + (mouseX - original_mouseX)
      //   new_height = element_original_height + (mouseY - original_mouseY)
      // bottom-left:
      //   new_width = element_original_width - (mouseX - original_mouseX)
      //   new_height = element_original_height + (mouseY - original_mouseY)
      //   new_x = element_original_x - (mouseX - original_mouseX)
      // top-right:
      //   new_width = element_original_width + (mouseX - original_mouseX)
      //   new_height = element_original_height - (mouseY - original_mouseY)
      //   new_y = element_original_y + (mouseY - original_mouseY)
      // top-left:
      //   new_width = element_original_width - (mouseX - original_mouseX)
      //   new_height = element_original_height - (mouseY - original_mouseY)
      //   new_x = element_original_x + (mouseX - original_mouseX)
      //   new_y = element_original_y + (mouseY - original_mouseY)

    }
    
    function stopDesktopResize() {
      window.removeEventListener('mousemove', resize);
    }

    function stopMouseResize() {
      window.removeEventListener('touchend', resize);
    }
  }
}

