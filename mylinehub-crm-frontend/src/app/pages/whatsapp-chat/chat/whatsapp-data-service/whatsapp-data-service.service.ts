import { Injectable } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { Observable, Subject, map, of, takeUntil } from 'rxjs';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { DialogComponent } from '../../../employee/all-employees/dialog/dialog.component';
import { WhatsappChatHistoryService } from '../../../../service/whatsapp-chat-history/whatsapp-chat-history.service';


@Injectable({
  providedIn: 'root'
})
export class WhatsappDataServiceService {

  //Step-1 (Setup in header component)
  //Variables whose value is filled in header component
  //All phone Number Main data for employee
  allNumbersData: any   = [];

  //Step-2 (Setup during initialization in chat component & later)
  //All below are variables filled in chat component while initialization itself
  //Options for phone number with drop down
  filteredOptions$: Observable<string[]>;
  //Phone number main selected as per drop down of maked via filterd options (set via allNumbersData)
  phoneNumberMain:any;
  //Current Participant Index
  currentSelectedPhoneNumber = -1;

  //Disable send button for chat. This is enabled only when someone selects a user
  showTextDictate: boolean = true;
  sendMessageDisabled: boolean = true;
  allowCallDisabled:boolean = true;
  clearChatDisabled: boolean = true;
  clearAllChatsDisabled: boolean = true;

  //All phone Number With data
  participants: any = [];
  //Current Participant Index
  currentSelectedParticipantNumber = -1;
  currentCustomerRecord : any;

  //Before receiving new messahe
  allChatHistory:any = [];
  //total of history and new message
  allChatData:any = [];
  //New Message, below variable is kept as is in initialization. But it is kept here instead step-3
  allChatCurrent:any = [];

  //Step-3, other variables used for different clicks
  previousMessageId:any = -1;

  //MessageDetails
  messageType:any;
  
  //Current Selected FileDetails
  file: any = null;
  whatsAppMediaId:any;
  blobType:any;
  fileName:any;
  fileSizeInMB:any;
  blobStringText;

  singleChatDeleteIndex: any;

  //Below is used in chat component to avoid double trigger of api
  sendChatHistoryDataId: any = null;


  clearAllChatHistoryInProgress: boolean = false;
  clearSingleChatHistoryInProgress: boolean = false;
  previousChatLength: any = 0;
  selectedAction = 'Support Numbers';

  constructor(private dialogService: NbDialogService,
              private whatsappChatHistoryService : WhatsappChatHistoryService,
              protected constantService : ConstantsService,) { }


  // updateChatHistoryNow(){
  //   //Chat history is not updated from api. When STOMP message is send to backend, there itself history is picked.
  //   //This caontainer is just created and is kept here to inform above.
  //   console.log("updateChatHistoryNow, this function is not doing anything here");
  // }

  getChatHistory(phoneWith:string)
  {

    console.log("getChatHistory from whats app data service");
    let destroy$ = new Subject<void>();
    let postData = {
      phoneMain:this.phoneNumberMain,
      phoneWith:phoneWith,
      lastReadIndex:null,
      organization:localStorage.getItem("organization")
    };

    this.sendChatHistoryDataId = this.whatsappChatHistoryService.getAllChatHistoryByTwoPhoneNumbersAndOrganization(postData)
    .pipe(takeUntil(destroy$))
    .subscribe({
      next: (result:any) => {
              if (result != null)
              {

                this.allChatHistory = [... JSON.parse(JSON.stringify(result))];
                this.allChatData = [... JSON.parse(JSON.stringify(result))];
                this.updateLastUpdateIndexForPhone(phoneWith);
                this.previousChatLength = this.allChatData.length;
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

  
  updateLastUpdateIndexForPhone(phoneWith:string)
  {
    console.log("updateLastUpdateIndexForPhone from whats app data service");
    let destroy$ = new Subject<void>();
    let postData = {
      phoneMain:this.phoneNumberMain,
      phoneWith:phoneWith,
      lastReadIndex:null,
      organization:localStorage.getItem("organization")
    };

    this.whatsappChatHistoryService.updateLastReadIndexByTwoPhoneNumbersAndOrganization(postData)
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
    console.log('Clear All Chats in service');
    this.clearAllChatHistoryInProgress = true;
    let destroy$ = new Subject<void>();
    let postData = {
      phoneMain:this.phoneNumberMain,
      phoneWith:null,
      lastReadIndex:null,
      organization:localStorage.getItem("organization")
    };

    this.whatsappChatHistoryService.deleteAllChatHistoryByPhoneNumberMainAndOrganization(postData)
    .pipe(takeUntil(destroy$))
    .subscribe({
      next: (result) => {

              if(result)
              {
                this.allChatData = [];
                this.allChatHistory = [];
                this.participants = [];
                this.sendMessageDisabled = true;
                this.clearChatDisabled = true;
                this.clearAllChatsDisabled = true;
              }

              destroy$.next();
              destroy$.complete();
              this.clearAllChatHistoryInProgress = false;
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.clearAllChatHistoryInProgress = false;
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
      });
  }

  clearSingleChat(phoneWith:string,phoneWithIndex:any){
    console.log("clearSingleChats in service");
    this.clearSingleChatHistoryInProgress = true;

    let destroy$ = new Subject<void>();
     let postData = {
      phoneMain:this.phoneNumberMain,
      phoneWith:phoneWith,
      lastReadIndex:null,
      organization:localStorage.getItem("organization")
    };

    this.whatsappChatHistoryService.deleteAllChatHistoryByTwoPhoneNumbersAndOrganization(postData)
    .pipe(takeUntil(destroy$))
    .subscribe({
      next: (result) => {

              if(phoneWith == this.phoneNumberMain)
              {
                this.allChatData = [];
                this.allChatHistory = [];
              }

              this.participants.splice(phoneWithIndex, 1);

              //disabling chat button if list of participant becomes zero
              if(this.participants.length ==0)
              {
                 this.sendMessageDisabled = true;
              }

              this.clearSingleChatHistoryInProgress = false;
              destroy$.next();
              destroy$.complete();
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.clearSingleChatHistoryInProgress = false;
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
      });
  }



  private filter(value: string): string[] {
    console.log("Filter from whats app data service");
    const filterValue = value.toLowerCase();
    return this.allNumbersData.filter(optionValue => (optionValue.firstName+' '+optionValue.lastName+' '+ optionValue.extension).toLowerCase().includes(filterValue));
  }

  getFilteredOptions(value: string): Observable<string[]> {
    console.log("getFilteredOptions from whats app data service");
    return of(value).pipe(
      map(filterString => this.filter(filterString)),
    );
  }

  noDataToShowSoSetToDefault(){
    console.log("noDataToShowSoSetToDefault from whats app data service");
     this.phoneNumberMain = null;
    this.currentCustomerRecord = null;
    this.allChatCurrent = [];
    this.sendMessageDisabled = true;
    this.allowCallDisabled = true;
    this.clearChatDisabled = true;
    this.clearAllChatsDisabled = true;
    //All phone Number With data
    this.participants = [];
    //Current Participant Index
    this.currentSelectedParticipantNumber = -1;
    //Before receiving new messahe
    this.allChatHistory = [];
    //New Message
    this.allChatCurrent = [];
    //total of history and new message
    this.allChatData = [];
    //Below is used in chat component to avoid double trigger of api
    this.sendChatHistoryDataId = null;
    this.currentSelectedPhoneNumber = -1;
    this.previousMessageId = -1;
    //MessageDetails
    this.messageType = null;
    //Current Selected FileDetails
    this.file = null;
    this.whatsAppMediaId = null;
    this.blobType = null;
    this.fileName = null;
    this.fileSizeInMB = null;
    this.blobStringText = null;
    this.singleChatDeleteIndex = -1;
  }

  showDialoge(header: string,icon: string,status: string, message:string) {
   console.log("showDialoge from whats app data service");
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
