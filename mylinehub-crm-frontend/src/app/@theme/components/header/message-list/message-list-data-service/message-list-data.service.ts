import { Injectable } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { EmployeeService } from '../../../../../pages/employee/service/employee.service';
import { DialogComponent } from '../../../../../pages/employee/all-employees/dialog/dialog.component';
import { Observable, Subject, map, of, takeUntil } from 'rxjs';
import { ChatHistoryService } from '../../../../../service/chat-history/chat-history.service';
import { ConstantsService } from '../../../../../service/constants/constants.service';

@Injectable({
  providedIn: 'root'
})
export class MessageListDataService {

  disableChatButton :any = true;
  allEmployeesData: any   = [];
  filteredOptions$: Observable<string[]>;
  filteredCostOptions$: Observable<string[]>;
  filteredCallOptions$: Observable<string[]>;
  multipleFilteredCallOptions$: Observable<string[]>;
  
  participants: any = [];
  sendChatHistoryDataId=null;
  currentSelectedParticipantNumber = 0;
  currentUser:any; //This user was created because ConstantsService class static variable had no scope within html file
  currentRecord:any= { firstName : "No", lastName : "One"};
  myExtension:any;
  initialChatHistory:any
  allChatHistory:any = [];
  allChatCurrent:any = [];
  // allChatData:any = [
  //                     { 
  //                       fromExtension:'203',
  //                       fromName:'Shipra Goel',
  //                       fromTitle:'Employee',
  //                       toExtension:'conferenceId',
  //                       messages:  [
  //                                     {
  //                                       messageType:'string',
  //                                       lines : [
  //                                                     {
  //                                                       messageSubType:'anchor',
  //                                                       stringMessage:null,
  //                                                       anchorMessage:['@all','!😍 Download below file.'],
  //                                                       dateTime:'2024-01-25T09:16:17.086Z',
  //                                                     }
  //                                               ],
  //                                       blobMessage:null,
  //                                       fileName:null,
  //                                       blobType:null,
  //                                       fileSizeInMB:null,
  //                                       dateTime:'2024-01-25T09:16:17.086Z',
  //                                     },
  //                                     {
  //                                       messageType:'blob',
  //                                       lines:[],
  //                                       blobMessage:'huhinkn hjvcuytfgujvj',
  //                                       fileName:'NewYear',
  //                                       blobType:'sketch',
  //                                       fileSizeInMB:120,
  //                                       dateTime:'2024-01-25T09:17:17.086Z',
  //                                     }
  //                                  ]
  //                     },
  //                     { 
  //                       fromExtension:'201',
  //                       fromName:'Anand Goel',
  //                       fromTitle:'Admin',
  //                       toExtension:'conferenceId',
  //                       messages:  [
  //                                     {
  //                                       messageType:'string',
  //                                       lines:[
  //                                                   {
  //                                                     messageSubType:'string',
  //                                                     stringMessage:'Good morning!🌈',
  //                                                     anchorMessage:null,
  //                                                     dateTime:'2024-01-25T09:16:31.086Z',
  //                                                   },
  //                                                   {
  //                                                     messageSubType:'anchor',
  //                                                     stringMessage:null,
  //                                                     anchorMessage:['I downloaded the file','@shipra'],
  //                                                     dateTime:'2024-01-25T09:16:33.086Z',
  //                                                   },
  //                                             ],
  //                                       blobMessage:null,
  //                                       fileName:null,
  //                                       blobType:null,
  //                                       fileSizeInMB:null,
  //                                       dateTime:'2024-01-25T09:16:30.086Z',
  //                                     },
  //                                  ]
  //                     }
  //                   ];

  allChatData:any = [];
  
  constructor(private dialogService: NbDialogService,
              private employeeService : EmployeeService,
              private chatHistoryService : ChatHistoryService,
              protected constantService : ConstantsService,) { }



  getChatHistory(extensionWith:string,lastReadIndex:any)
  {
    let destroy$ = new Subject<void>();
    let postData = {
      lastReadIndex:lastReadIndex,
      extensionMain:ConstantsService.user.extension,
      extensionWith:extensionWith,
      organization:localStorage.getItem("organization")
    };

    this.chatHistoryService.getAllChatHistoryByTwoExtensionsAndOrganization(postData)
    .pipe(takeUntil(destroy$))
    .subscribe({
      next: (result:any) => {
              if (result != null)
              {
                this.initialChatHistory = JSON.parse(JSON.stringify(result));
                // this.allChatHistory = [... JSON.parse(JSON.parse(JSON.stringify(JSON.parse(JSON.stringify(result)).chats)).allChats)];
                this.allChatHistory = [... JSON.parse(JSON.stringify(result.chats.allChats))];
                this.allChatData = [... JSON.parse(JSON.stringify(result.chats.allChats))];
                // console.log("********************************result****************************");
                // console.log(JSON.stringify(result));
                // console.log("********************************chats****************************");
                // console.log(JSON.stringify(JSON.parse(JSON.stringify(JSON.parse(JSON.stringify(result)).chats))));
                this.updateLastUpdateIndexForExtension(this.participants[this.currentSelectedParticipantNumber].extension,this.allChatData.length);

              }
              else{
                this.allChatHistory = [];
                this.allChatData = [];
              }
              destroy$.next();
              destroy$.complete();
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
      });
  }

  
  updateChatHistory(extensionWith:string,chat:any,timeout:number)
  {
    // console.log("updateLastUpdateIndexForExtension");

    if(this.sendChatHistoryDataId == null)
    {
      this.sendChatHistoryDataId = setTimeout(()=>{this.updateChatHistoryNow(extensionWith,chat)},timeout);
    }
    else{
      clearTimeout(this.sendChatHistoryDataId);
      this.sendChatHistoryDataId = setTimeout(()=>{this.updateChatHistoryNow(extensionWith,chat)},timeout);
    }

  }

  updateChatHistoryNow(extensionWith:string,chat:any)
  {
    console.log("*******************updateChatHistoryNow****************************");
    
    let destroy$ = new Subject<void>();
    let postData = {
      chat:chat,
      extensionMain:ConstantsService.user.extension,
      extensionWith:extensionWith,
      organization:localStorage.getItem("organization")
    };

    console.log("*****************************postData***************************************");
    console.log(postData);

    this.chatHistoryService.appendChatHistoryByTwoExtensionsAndOrganization(postData)
    .pipe(takeUntil(destroy$))
    .subscribe({
      next: (result) => {
              // if (result != null)
              // {
              //   this.allChatHistory = [... JSON.parse(JSON.stringify(result))];
              //   this.allChatData = [... JSON.parse(JSON.stringify(result))];
              // }
              // else{
              //   this.allChatHistory = [];
              //   this.allChatData = [];
              // }

              //As all chat is saved into database hence current chat is made zero after saving to database. 
              this.allChatCurrent = [];
              this.sendChatHistoryDataId = null;
              destroy$.next();
              destroy$.complete();
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
      });
  }

  updateLastUpdateIndexForExtension(extensionWith:string,lastReadIndex:any)
  {

    // console.log("updateLastUpdateIndexForExtension");
    let destroy$ = new Subject<void>();
    let postData = {
      lastReadIndex:lastReadIndex,
      extensionMain:ConstantsService.user.extension,
      extensionWith:extensionWith,
      organization:localStorage.getItem("organization")
    };

    this.chatHistoryService.updateLastReadIndexByTwoExtensionsAndOrganization(postData)
    .pipe(takeUntil(destroy$))
    .subscribe({
      next: (result) => {
              destroy$.next();
              destroy$.complete();
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
      });

  }

  clearAllChats()
  {
    // console.log("clearallChats in service");
    let destroy$ = new Subject<void>();
    let postData = {
      id:0,
      extension:ConstantsService.user.extension,
      organization:localStorage.getItem("organization")
    };

    this.chatHistoryService.deleteAllChatHistoryByExtensionAndOrganization(postData)
    .pipe(takeUntil(destroy$))
    .subscribe({
      next: (result) => {

              if(result)
              {
                this.allChatData = [];
                this.allChatHistory = [];
                this.participants = [];
                this.disableChatButton=true;
                this.currentRecord = { firstName : "No", lastName : "One"};
              }

              destroy$.next();
              destroy$.complete();
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
      });
  }

  clearSingleChat(extensionCurrent:string,extensionWith:string,lastReadIndex:any,extensionWithIndex:any){
    // console.log("clearSingleChats in service");
    let destroy$ = new Subject<void>();
    let postData = {
      lastReadIndex:lastReadIndex,
      extensionMain:ConstantsService.user.extension,
      extensionWith:extensionWith,
      organization:localStorage.getItem("organization")
    };

    this.chatHistoryService.deleteAllChatHistoryByTwoExtensionsAndOrganization(postData)
    .pipe(takeUntil(destroy$))
    .subscribe({
      next: (result) => {

              if(extensionWith == extensionCurrent)
              {
                this.allChatData = [];
                this.allChatHistory = [];
                this.currentRecord = { firstName : "No", lastName : "One"};
              }

              this.participants.splice(extensionWithIndex, 1);

              //disabling chat button if list of participant becomes zero
              if(this.participants.length ==0)
              {
                 this.disableChatButton=true;
              }

              destroy$.next();
              destroy$.complete();
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
      });
  }

  attachFileToPreviousRecordAllMessagesPerType(file:any,name:any,type:any,size:any)
  {
    console.log("Pushing attachFileToPreviousRecordAllMessagesPerType");
    const now = new Date(); 
    let allChatLength = this.allChatData.length;
    
    this.allChatData[allChatLength-1].messages.push({
      messageType:'blob',
      lines:[],
      blobMessage:file,
      fileName:name,
      blobType:type,
      fileSizeInMB:size,
      dateTime:now,
    });

  }

  attachFileToNewRecordAllMessagesPerType(file:any,name:any,type:any,size:any,fromExtension:string,fromName:string,fromTitle:string)
  {
    console.log("Pushing attachFileToNewRecordAllMessagesPerType");
    const now = new Date();    
    let record = { 
              fromExtension:fromExtension,
              fromName:fromName,
              fromTitle:fromTitle,
              toExtension:this.participants[this.currentSelectedParticipantNumber].extension,
              messages:  [
                            {
                              messageType:'blob',
                              lines:[],
                              blobMessage:file,
                              fileName:name,
                              blobType:type,
                              fileSizeInMB:size,
                              dateTime:now,
                            },
                        ]
            };
    this.allChatData.push(record);
  }

  attachFileToPreviousRecordCurrentMessagesPerType(file:any,name:any,type:any,size:any)
  {
    const now = new Date(); 
    let currentChatLength = this.allChatCurrent.length;
    this.allChatCurrent[currentChatLength-1].messages.push({
      messageType:'blob',
      lines:[],
      blobMessage:file,
      fileName:name,
      blobType:type,
      fileSizeInMB:size,
      dateTime:now,
    });
      
  }

  attachFileToNewRecordCurrentMessagesPerType(file:any,name:any,type:any,size:any,fromExtension:string,fromName:string,fromTitle:string)
  {
    const now = new Date();    
    let record = { 
              fromExtension:fromExtension,
              fromName:fromName,
              fromTitle:fromTitle,
              toExtension:this.participants[this.currentSelectedParticipantNumber].extension,
              messages:  [
                            {
                              messageType:'blob',
                              lines:[],
                              blobMessage:file,
                              fileName:name,
                              blobType:type,
                              fileSizeInMB:size,
                              dateTime:now,
                            },
                        ]
            };
    this.allChatCurrent.push(record);
  }

  setPreviousRecordAllMessageAsPerType(sendMessage:any)
  {
    const now = new Date(); 
    let allChatLength = this.allChatData.length;
    let messagesLength = 0;
    // console.log("setPreviousRecordAllMessageAsPerType : allChatLength");
    // console.log(allChatLength);

    if (allChatLength !=0)
    {
      // console.log("setPreviousRecordAllMessageAsPerType : this.allChatData[allChatLength-1]");
      // console.log(this.allChatData[allChatLength-1]);
      messagesLength = this.allChatData[allChatLength-1].messages.length;
    }

    if(allChatLength!=0)
    {
      if(messagesLength!=0 && this.allChatData[allChatLength-1].messages[messagesLength-1].messageType == 'string') 
        {
          this.allChatData[allChatLength-1].messages[messagesLength-1].lines.push(sendMessage); 
        }
        else{
          this.allChatData[allChatLength-1].messages.push({
            messageType:'string',
            lines:[],
            blobMessage:null,
            fileName:null,
            blobType:null,
            fileSizeInMB:null,
            dateTime:now,
          });
          this.allChatData[allChatLength-1].messages[0].lines.push(sendMessage); 
        }
    }
  }

  setNewAllMessageAsPerType(sendMessage:any,fromExtension:string,fromName:string,fromTitle:string)
  {
    const now = new Date();    
    let record = { 
              fromExtension:fromExtension,
              fromName:fromName,
              fromTitle:fromTitle,
              toExtension:this.participants[this.currentSelectedParticipantNumber].extension,
              messages:  [
                            {
                              messageType:'string',
                              lines:[],
                              blobMessage:null,
                              fileName:null,
                              blobType:null,
                              fileSizeInMB:null,
                              dateTime:now,
                            },
                        ]
            };
    record.messages[0].lines.push(sendMessage); 
    this.allChatData.push(record);
  }


  setPreviousRecordCurrentMessageAsPerType(sendMessage:any)
  {
    const now = new Date(); 
    let currentChatLength = this.allChatCurrent.length;
    let messagesLength = 0;

    // console.log("setPreviousRecordAllMessageAsPerType : currentChatLength");
    // console.log(currentChatLength);

    if (currentChatLength !=0)
    {
      // console.log("setPreviousRecordAllMessageAsPerType : this.allChatCurrent[currentChatLength-1]");
      // console.log(this.allChatCurrent[currentChatLength-1]);
      messagesLength = this.allChatCurrent[currentChatLength-1].messages.length;
    }

    if(currentChatLength!=0)
    {
        if(messagesLength!=0 && this.allChatCurrent[currentChatLength-1].messages[messagesLength-1].messageType == 'string') 
          {
            this.allChatCurrent[currentChatLength-1].messages[messagesLength-1].lines.push(sendMessage); 
          }
          else{

            this.allChatCurrent[currentChatLength-1].messages.push({
              messageType:'string',
              lines:[],
              blobMessage:null,
              fileName:null,
              blobType:null,
              fileSizeInMB:null,
              dateTime:now,
            });

            this.allChatCurrent[currentChatLength-1].messages[0].lines.push(sendMessage); 
          }
    }
  }

  setNewCurrentMessageAsPerType(sendMessage:any,fromExtension:string,fromName:string,fromTitle:string)
  {
    const now = new Date();    
    let record = { 
              fromExtension:fromExtension,
              fromName:fromName,
              fromTitle:fromTitle,
              toExtension:this.participants[this.currentSelectedParticipantNumber].extension,
              messages:  [
                            {
                              messageType:'string',
                              lines:[],
                              blobMessage:null,
                              fileName:null,
                              blobType:null,
                              fileSizeInMB:null,
                              dateTime:now,
                            },
                        ]
            };
    record.messages[0].lines.push(sendMessage); 
    this.allChatCurrent.push(record);
  }


  private filter(value: string): string[] {
    const filterValue = value.toLowerCase();
    return this.allEmployeesData.filter(optionValue => (optionValue.firstName+' '+optionValue.lastName+' '+ optionValue.extension).toLowerCase().includes(filterValue));
  }

  getFilteredOptions(value: string): Observable<string[]> {
    return of(value).pipe(
      map(filterString => this.filter(filterString)),
    );
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
