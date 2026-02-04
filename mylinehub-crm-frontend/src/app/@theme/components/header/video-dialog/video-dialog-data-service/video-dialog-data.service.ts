import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ConstantsService } from '../../../../../service/constants/constants.service';
import { MessageListDataService } from '../../message-list/message-list-data-service/message-list-data.service';
import { DialogComponent } from '../../../../../pages/employee/all-employees/dialog/dialog.component';
import { NbDialogService } from '@nebular/theme';

@Injectable({
  providedIn: 'root'
})
export class VideoDialogDataService {
  
  currentUser:any;
  myExtension:any;
  animationDuration:any ="100ms";
  animationIterationCount:any ="infinite";
  animationTimingFunction:any ="ease";
  filteredOptions$: Observable<string[]>;
  fullScreenEnabled:any = false;
  screenShare:any = false;
  // mainVideoCenterBlur:any ="blur(0.00vmin)";
  // mainVideoMiddleBlur:any ="blur(0.00vmin)";
  // mainVideoMiddleAnimationName:any ="";
  // mainVideoCenterBlur:any ="blur(0.05vmin)";
  // mainVideoMiddleBlur:any ="blur(0.05vmin)";
  // mainVideoMiddleAnimationName:any ="inside";

  //videoContainer
  //isVideoOn
  //isMute
  //centerBlur
  //middleBlur
  //middleAnimationName
  mainVideoData:any = { firstName: 'Anand',lastName:'Goel', role: 'Admin',extension:'201',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" };
 

  // allOtherVideoCenterBlur:any =["blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)"];
  // allOtherVideoMiddleBlur:any =["blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)","blur(0.05vmin)"];
  // allOtherVideoMiddleAnimationName:any =["inside","inside","inside","inside","inside","inside","inside","inside","inside","inside","inside","inside","inside","inside","inside"];
  
  //************* See these values to find what values have to be placed **********/
  // allOtherVideoCenterBlur:any =["","","blur(0.05vmin)","","blur(0.05vmin)","","","","","","","","","",""];
  // allOtherVideoMiddleBlur:any =["","","blur(0.05vmin)","","blur(0.05vmin)","","","","","","","","","",""];
  // allOtherVideoMiddleAnimationName:any =["","","inside","","inside","","","","","","","","","",""];
  // allButtonMute:any =["true","true","false","true","false","true","true","true","true","true","true","true","true","true","true"];
  // allButtonCameraOff:any =["true","true","false","true","false","true","true","true","true","true","true","true","true","true","true"];


  //**********In actual we have shifted all variables to just one Value *******/
  //videoContainer
  //isVideoOn
  //isMute
  //centerBlur
  //middleBlur
  //middleAnimationName
  allConferenceMembers:any= [
    { firstName: 'Carla',lastName:'Espinosa', role: 'Nurse',extension:'216',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
    { firstName: 'Bob',lastName:'Kelso', role: 'Doctor of Medicine',extension:'202',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
    { firstName: 'Janitor',lastName:'', role: 'Janitor',extension:'203',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
    { firstName: 'Perry',lastName:'Cox', role: 'Doctor of Medicine',extension:'204',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
    { firstName: 'Ben',lastName:'Sullivan', role: 'Carpenter and photographer',extension:'205',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
    { firstName: 'Shipra',lastName:'Goel', role: 'Nurse',extension:'206',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
    { firstName: 'Mohan',lastName:'Kelso', role: 'Doctor of Medicine',extension:'207',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
    { firstName: 'Vinay',lastName:'Garg', role: 'Janitor',extension:'208',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
    { firstName: 'Kusum',lastName:'Cox', role: 'Doctor of Medicine',extension:'209',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
    { firstName: 'Alkhos',lastName:'Sullivan', role: 'Carpenter and photographer',extension:'210',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
    { firstName: 'Vural',lastName:'Espinosa', role: 'Nurse',extension:'211',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
    { firstName: 'Pneep',lastName:'Kelso', role: 'Doctor of Medicine',extension:'212',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
    { firstName: 'Clog',lastName:'Mehta', role: 'Janitor',extension:'213',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
    { firstName: 'Falling',lastName:'Cox', role: 'Doctor of Medicine',extension:'214',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
    { firstName: 'Sanitorachy',lastName:'Sullivan', role: 'Carpenter and photographer',extension:'215',videoContainer:"",isVideoOn:false,isMute:true,centerBlur:"",middleBlur:"",middleAnimationName:"" },
  ];

  initialChatHistory:any
  allChatHistory:any = [];
  allChatCurrent:any = [];
  allChatData:any = [
                      { 
                        fromExtension:'203',
                        fromName:'Shipra Goel',
                        fromTitle:'Employee',
                        toExtension:'conferenceId',
                        messages:  [
                                      {
                                        messageType:'string',
                                        lines : [
                                                      {
                                                        messageSubType:'anchor',
                                                        stringMessage:null,
                                                        anchorMessage:['@all','!😍 Download below file.'],
                                                        dateTime:'2024-01-25T09:16:17.086Z',
                                                      }
                                                ],
                                        blobMessage:null,
                                        fileName:null,
                                        fileSizeInMB:null,
                                        dateTime:'2024-01-25T09:16:17.086Z',
                                      },
                                      {
                                        messageType:'blob',
                                        lines:[],
                                        blobMessage:'huhinkn hjvcuytfgujvj',
                                        fileName:'NewYear.sketch',
                                        fileSizeInMB:120,
                                        dateTime:'2024-01-25T09:17:17.086Z',
                                      }
                                   ]
                      },
                      { 
                        fromExtension:'201',
                        fromName:'Anand Goel',
                        fromTitle:'Admin',
                        toExtension:'conferenceId',
                        messages:  [
                                      {
                                        messageType:'string',
                                        lines:[
                                                    {
                                                      messageSubType:'string',
                                                      stringMessage:'Good morning!🌈',
                                                      anchorMessage:null,
                                                      dateTime:'2024-01-25T09:16:31.086Z',
                                                    },
                                                    {
                                                      messageSubType:'anchor',
                                                      stringMessage:null,
                                                      anchorMessage:['I downloaded the file','@shipra'],
                                                      dateTime:'2024-01-25T09:16:33.086Z',
                                                    },
                                              ],
                                        blobMessage:null,
                                        fileName:null,
                                        fileSizeInMB:null,
                                        dateTime:'2024-01-25T09:16:30.086Z',
                                      },
                                   ]
                      },
                      { 
                        fromExtension:'203',
                        fromName:'Raju Srivastava',
                        fromTitle:'Admin',
                        toExtension:'conferenceId',
                        messages:  [
                                      {
                                        messageType:'string',
                                        lines:[
                                                    {
                                                      messageSubType:'string',
                                                      stringMessage:'Good morning!🌈',
                                                      anchorMessage:null,
                                                      dateTime:'2024-01-25T09:16:31.086Z',
                                                    },
                                                    {
                                                      messageSubType:'anchor',
                                                      stringMessage:null,
                                                      anchorMessage:['I downloaded the file','@shipra'],
                                                      dateTime:'2024-01-25T09:16:33.086Z',
                                                    },
                                              ],
                                        blobMessage:null,
                                        fileName:null,
                                        fileSizeInMB:null,
                                        dateTime:'2024-01-25T09:16:30.086Z',
                                      },
                                   ]
                      },
                      { 
                        fromExtension:'204',
                        fromName:'Amit Mehta',
                        fromTitle:'Admin',
                        toExtension:'conferenceId',
                        messages:  [
                                      {
                                        messageType:'string',
                                        lines:[
                                                    {
                                                      messageSubType:'string',
                                                      stringMessage:'Good morning!🌈',
                                                      anchorMessage:null,
                                                      dateTime:'2024-01-25T09:16:31.086Z',
                                                    },
                                                    {
                                                      messageSubType:'anchor',
                                                      stringMessage:null,
                                                      anchorMessage:['I downloaded the file','@shipra'],
                                                      dateTime:'2024-01-25T09:16:33.086Z',
                                                    },
                                              ],
                                        blobMessage:null,
                                        fileName:null,
                                        fileSizeInMB:null,
                                        dateTime:'2024-01-25T09:16:30.086Z',
                                      },
                                   ]
                      },
                      { 
                        fromExtension:'205',
                        fromName:'Virendra Jha',
                        fromTitle:'Admin',
                        toExtension:'conferenceId',
                        messages:  [
                                      {
                                        messageType:'string',
                                        lines:[
                                                    {
                                                      messageSubType:'string',
                                                      stringMessage:'Good morning!🌈',
                                                      anchorMessage:null,
                                                      dateTime:'2024-01-25T09:16:31.086Z',
                                                    },
                                                    {
                                                      messageSubType:'anchor',
                                                      stringMessage:null,
                                                      anchorMessage:['I downloaded the file','@shipra'],
                                                      dateTime:'2024-01-25T09:16:33.086Z',
                                                    },
                                              ],
                                        blobMessage:null,
                                        fileName:null,
                                        fileSizeInMB:null,
                                        dateTime:'2024-01-25T09:16:30.086Z',
                                      },
                                   ]
                      },
                      { 
                        fromExtension:'203',
                        fromName:'Shipra Goel',
                        fromTitle:'Employee',
                        toExtension:'conferenceId',
                        messages:  [
                                      {
                                        messageType:'string',
                                        lines : [
                                                      {
                                                        messageSubType:'anchor',
                                                        stringMessage:null,
                                                        anchorMessage:['@all,','!Good that you downloaded file. Anand Can you explain team what is to be done further?'],
                                                        dateTime:'2024-01-25T09:16:17.086Z',
                                                      }
                                                ],
                                        blobMessage:null,
                                        fileName:null,
                                        fileSizeInMB:null,
                                        dateTime:'2024-01-25T09:16:17.086Z',
                                      }
                                   ]
                      },
                      { 
                        fromExtension:'201',
                        fromName:'Anand Goel',
                        fromTitle:'Admin',
                        toExtension:'conferenceId',
                        messages:  [
                                      {
                                        messageType:'string',
                                        lines:[
                                                    {
                                                      messageSubType:'string',
                                                      stringMessage:'Sure mam!',
                                                      anchorMessage:null,
                                                      dateTime:'2024-01-25T09:16:31.086Z',
                                                    },
                                                    {
                                                      messageSubType:'anchor',
                                                      stringMessage:null,
                                                      anchorMessage:['@all','Let us connect on zoom call to discuss further onto this. 445599876 is my zoom Id , 34563 is the pasword. Let us connect at 4 PM.'],
                                                      dateTime:'2024-01-25T09:16:33.086Z',
                                                    },
                                              ],
                                        blobMessage:null,
                                        fileName:null,
                                        fileSizeInMB:null,
                                        dateTime:'2024-01-25T09:16:30.086Z',
                                      },
                                   ]
                      },
                      { 
                        fromExtension:'203',
                        fromName:'Raju Srivastava',
                        fromTitle:'Admin',
                        toExtension:'conferenceId',
                        messages:  [
                                      {
                                        messageType:'string',
                                        lines:[
                                                    {
                                                      messageSubType:'anchor',
                                                      stringMessage:null,
                                                      anchorMessage:['Yes','@Anand'],
                                                      dateTime:'2024-01-25T09:16:31.086Z',
                                                    }
                                              ],
                                        blobMessage:null,
                                        fileName:null,
                                        fileSizeInMB:null,
                                        dateTime:'2024-01-25T09:16:30.086Z',
                                      },
                                   ]
                      },
                      { 
                        fromExtension:'204',
                        fromName:'Amit Mehta',
                        fromTitle:'Admin',
                        toExtension:'conferenceId',
                        messages:  [
                                      {
                                        messageType:'string',
                                        lines:[
                                                    {
                                                      messageSubType:'string',
                                                      stringMessage:'Sure!',
                                                      anchorMessage:null,
                                                      dateTime:'2024-01-25T09:16:31.086Z',
                                                    }
                                              ],
                                        blobMessage:null,
                                        fileName:null,
                                        fileSizeInMB:null,
                                        dateTime:'2024-01-25T09:16:30.086Z',
                                      },
                                   ]
                      },
                      { 
                        fromExtension:'205',
                        fromName:'Virendra Jha',
                        fromTitle:'Admin',
                        toExtension:'conferenceId',
                        messages:  [
                                      {
                                        messageType:'string',
                                        lines:[
                                                    {
                                                      messageSubType:'string',
                                                      stringMessage:'okay sir !',
                                                      anchorMessage:null,
                                                      dateTime:'2024-01-25T09:16:31.086Z',
                                                    }
                                              ],
                                        blobMessage:null,
                                        fileName:null,
                                        fileSizeInMB:null,
                                        dateTime:'2024-01-25T09:16:30.086Z',
                                      },
                                   ]
                      },
                    ];

  constructor(protected constService : ConstantsService,
              protected messageListDataService:MessageListDataService,
              private dialogService: NbDialogService,) 
              {

              }

  setInitialTwoMembers(isVideo:boolean,external:boolean,number:any)
  {
      this.initialChatHistory = "";
      this.allChatHistory = [];
      this.allChatCurrent = [];
      this.allChatData = [];
      this.allConferenceMembers = [];
      this.mainVideoData = "";

      this.mainVideoData = {
            firstName: ConstantsService.user.firstName,
            lastName:ConstantsService.user.lastName, 
            role: ConstantsService.user.role, 
            extension:ConstantsService.user.extension,
            videoContainer:"",
            isVideoOn:isVideo,
            isMute:false,
            centerBlur:"blur(0.05vmin)",
            middleBlur:"blur(0.05vmin)",
            middleAnimationName:"inside" 
          };
 
     this.allConferenceMembers.push(this.mainVideoData);

     let talkWith:any;

     if (external)
      {
        talkWith = {
          firstName: number,
          lastName:"", 
          role: "external", 
          extension:number,
          videoContainer:"",
          isVideoOn:isVideo,
          isMute:false,
          centerBlur:"blur(0.05vmin)",
          middleBlur:"blur(0.05vmin)",
          middleAnimationName:"inside" 
        };

        this.allConferenceMembers.push(talkWith);

      }
    else
      {

        let allEmployeeIndex = -1;
        allEmployeeIndex = this.messageListDataService.allEmployeesData.findIndex(obj => obj.extension == number);

        if(allEmployeeIndex != -1)
          {
              talkWith = {
              firstName: this.messageListDataService.allEmployeesData[allEmployeeIndex].firstName,
              lastName:this.messageListDataService.allEmployeesData[allEmployeeIndex].lastName, 
              role: this.messageListDataService.allEmployeesData[allEmployeeIndex].role, 
              extension:this.messageListDataService.allEmployeesData[allEmployeeIndex].extension,
              videoContainer:"",
              isVideoOn:isVideo,
              isMute:false,
              centerBlur:"blur(0.05vmin)",
              middleBlur:"blur(0.05vmin)",
              middleAnimationName:"inside" 
              };

              this.allConferenceMembers.push(talkWith);
              
          }
        else
         {
           this.allConferenceMembers.push(null);
           this.showDialoge('Error','activity-outline','danger', "Extension "+number+" dialed is not found in organization"); 
         }
      } 
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
