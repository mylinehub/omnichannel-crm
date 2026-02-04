import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { NbDialogRef, NbDialogService, NbSidebarService, NbThemeService } from '@nebular/theme';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { Observable, Subject, map, of, takeUntil } from 'rxjs';
import { HeaderVariableService } from '../../../../service/header-variable/header-variable.service';
import { DialogComponent } from '../../../../pages/employee/all-employees/dialog/dialog.component';
import { MessageListDataService } from './message-list-data-service/message-list-data.service';
import { EmployeeService } from '../../../../pages/employee/service/employee.service';
import { ChatHistoryService } from '../../../../service/chat-history/chat-history.service';
import { StompService } from '../../../../service/stomp/stomp.service';
import { BrowserPhoneService } from '../../../../service/browser-phone/browser-phone.service';
import { AskDeleteComponent } from './ask-delete/ask-delete.component';
import { AskSendFileComponent } from './ask-send-file/ask-send-file.component';
import { DialLineService } from '../../../../service/browser-phone/dial-line/dial-line.service';
import { SpeechRecognitionService } from '../../../../service/speech-recognition/speech-recognition.service';

@Component({
  selector: 'ngx-message-list',
  templateUrl: './message-list.component.html',
  styleUrls: ['./message-list.component.scss']
})
export class MessageListComponent implements OnInit,OnChanges,OnDestroy {

  private destroy$: Subject<void> = new Subject<void>();
  redirectDelay: number = 0;
  themeSubscription: any;
  currentTheme: any;
  chat: any = "Chat With";
  clearAll = "";
  clearAllButton: any;
  participantdataDiv: any;
  searchAllBox:any;

  isParticipantMessageDataWrapped: boolean = true;
  isAllParticipantsWrapped: boolean = true;
  spellcheck:any = true;
  messageContentWrapperWidth: number = 100;
  messageContentWrapperHeight: number = 100;

  mainMessageWrapperWidth: number = 100;
  mainMessageWrapperHeight: number = 100;

  allParticipantsWrapperWidth: number = 0;
  allParticipantsWrapperHeight: number = 100;
  
  participantDataWidth: number = 100;
  participantDataHeight: number = 0;
  // currentRecord:any= { firstName : "No", lastName : "One"};
  currentPic:any;
  participantImageHeight = 80;
  participantImageWidth = 55;

  @ViewChild('autoInput') input;
  file: any = null;
  chatInput:any;
  showTextDictate = true;
  downloadCursor:any = "pointer";
  singleChatDeleteIndex: any;
  constructor(protected ref: NbDialogRef<MessageListComponent>,
              private stompService:StompService,
              private browserPhoneService: BrowserPhoneService,
              private constantService : ConstantsService,
              private themeService: NbThemeService,
              protected headerVariableService:HeaderVariableService,
              private dialogService: NbDialogService,
              private sidebarService: NbSidebarService,
              protected messageListDataService:MessageListDataService,
              private employeeService : EmployeeService,
              private chatHistoryService : ChatHistoryService,
              protected voiceRecognitionService : SpeechRecognitionService,
              protected dialLineService:DialLineService,) {
      // console.log("message-dialog-constructor");
      this.messageListDataService.myExtension = ConstantsService.user.extension;
      this.themeSubscription = this.themeService.getJsTheme().subscribe(theme => {
        this.currentTheme = theme.name;
        });
        this.showTextDictate = ConstantsService.user.textDictateOption;
  }
  
  ngOnInit(): void { 
    // console.log("message-dialog-ng-onIt");
    makeMessageResizableDiv('.allMessages');
    const element:any = document.querySelector('.allMessages');
    // this.headerVariableService.messageBadgeDot = false;
    this.messageListDataService.currentUser = ConstantsService.user;
    this.headerVariableService.messageBoundingClientRect = element.getBoundingClientRect();
    this.headerVariableService.messagefullScreenEnabled = false;
    this.clearAllButton = document.getElementById('clearAllButton');
    this.searchAllBox = document.getElementById('searchAllParticipantsBox');
    this.participantdataDiv = document.getElementById('participantDataWrapper');
    this.messageListDataService.filteredOptions$ = of(this.messageListDataService.allEmployeesData);
    this.loadAllParticipantsData();
    // Get the input field
    this.chatInput = document.getElementById("chatInput");

    // Execute a function when the user presses a key on the keyboard
    this.chatInput.addEventListener("keypress", (event:any)=>{
      // If the user presses the "Enter" key on the keyboard
      if (event.key === "Enter") {
        // Cancel the default action, if needed
        event.preventDefault();
        // Trigger the button element with a click
        // console.log("Enter is pressed");
        if(!this.messageListDataService.disableChatButton)
        {
          // console.log("Current selected candidate is not null hence we are clicking button after enter");
          document.getElementById("sendMessage").click();
        }
      }
    });

    this.voiceRecognitionService.init();
    
  }
  
  ngOnDestroy(): void {
    //console.log("message-dialog-ng-onDestoy");
    this.destroy$.next();
    this.destroy$.complete()
  }
  ngOnChanges(changes: SimpleChanges): void {
    //console.log("message-dialog-ng-onChanges");
  }

  dismiss() {
    this.ref.close();
  }

  sendAttachedMessage()
  {
    if(ConstantsService.user.textMessagingOption)
    {     
        const val = document.getElementById('onSendMessageFile');
        val.click();
    }
    else{
          this.showDialoge('Error','activity-outline','danger', "Messaging is not allowed for this user. Kindly contact your manager."); 
    }
  }

  onSendMessageFile(event)
  {
    // console.log("onSendMessageFile");
    this.file = event.target.files[0];
    this.showSendFileDialoge("If the participant is avaiable you may send file directly, hence below there may be two options. Otherwise you can send file only via server.",this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].dotClass); 
    event.target.value = "";
  }

  sendFileViaServer()
  { 
    try
    {
      let lastIndex = this.file.name.lastIndexOf('.');
      let name = this.file.name.slice(0, lastIndex);
      let type = this.file.name.slice(lastIndex + 1);
      let size = this.file.size;

      // console.log("Name : ",name);
      // console.log("Type : ",type);
      // console.log("Size : ",size);

      if(size >= 12000)
      {
        size = (((size)/1024)/1024).toFixed(2);
        console.log("Size In MB : ",size);
      }
      else
      {
        size = 0.001;
      }

      // Encode the file using the FileReader API
      const reader = new FileReader();
      const now = new Date(); 
      reader.readAsDataURL(this.file);

       reader.onloadend = () => {
          // Use a regex to remove data url part
          const base64String = String(reader.result)
              // .replace('data:', '')
              // .replace(/^.+,/, '');

          // console.log("base64String : ",base64String);
          // console.log("Time : ",now);
          // Logs wL2dvYWwgbW9yZ...

          if(base64String != null)
          {
              //sending message directly to user
              this.stompService.sendAttachedFileToExtension(this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].extension,String(base64String),name,type,size);

              let currentChatLength = this.messageListDataService.allChatCurrent.length;
              let allChatLength = this.messageListDataService.allChatData.length;
                
              //updating local current variable
              if(currentChatLength !=0)
                    {
                        if(this.messageListDataService.allChatCurrent[currentChatLength-1].fromExtension == ConstantsService.user.extension)
                        {
                          this.messageListDataService.attachFileToPreviousRecordCurrentMessagesPerType(String(base64String),name,type,size);
                        }
                        else{
                          this.messageListDataService.attachFileToNewRecordCurrentMessagesPerType(String(base64String),name,type,size,ConstantsService.user.extension,ConstantsService.user.firstName.trim()+" "+ConstantsService.user.lastName.trim(),ConstantsService.user.role);
                        }
                    }
              else{
                      this.messageListDataService.attachFileToNewRecordCurrentMessagesPerType(String(base64String),name,type,size,ConstantsService.user.extension,ConstantsService.user.firstName.trim()+" "+ConstantsService.user.lastName.trim(),ConstantsService.user.role);
                    }
                
              //updating local all chat variable
              if(allChatLength !=0)
                    {
                        if(this.messageListDataService.allChatData[allChatLength-1].fromExtension == ConstantsService.user.extension)
                        {
                          this.messageListDataService.attachFileToPreviousRecordAllMessagesPerType(String(base64String),name,type,size);
                        }
                        else{
                          this.messageListDataService.attachFileToNewRecordAllMessagesPerType(String(base64String),name,type,size,ConstantsService.user.extension,ConstantsService.user.firstName.trim()+" "+ConstantsService.user.lastName.trim(),ConstantsService.user.role);
                        }
                    }
              else{
                      this.messageListDataService.attachFileToNewRecordAllMessagesPerType(String(base64String),name,type,size,ConstantsService.user.extension,ConstantsService.user.firstName.trim()+" "+ConstantsService.user.lastName.trim(),ConstantsService.user.role);
                    }

              //sending chat history data to back-end after 3 seconds
              this.messageListDataService.updateChatHistory(this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].extension,this.messageListDataService.allChatCurrent,3000);
          }
          else
          {
            this.showDialoge('Error','activity-outline','danger', "Cannot send null message !!!"); 
          }

      };
    }
    catch(e)
    {
      //console.log("error");
        this.file = null;
        this.showDialoge('Error','activity-outline','danger', e); 
    }
  }

  sendFileToPeer()
  {
    try
    {
      let lastIndex = this.file.name.lastIndexOf('.');
      let name = this.file.name.slice(0, lastIndex);
      let type = this.file.name.slice(lastIndex + 1);
      let size = this.file.size;

      // console.log("Name : ",name);
      // console.log("Type : ",type);
      // console.log("Size : ",size);

      if(size >= 12000)
      {
        size = (((size)/1024)/1024).toFixed(2);
        // console.log("Size In MB : ",size);
      }
      else
      {
        size = 0.001;
      }

      // Encode the file using the FileReader API
      const reader = new FileReader();
      const now = new Date(); 
      reader.readAsDataURL(this.file);

       reader.onloadend = () => {
          // Use a regex to remove data url part
          const base64String = String(reader.result)
              // .replace('data:', '')
              // .replace(/^.+,/, '');

          // console.log("base64String : ",base64String);
          // console.log("Time : ",now);
          // Logs wL2dvYWwgbW9yZ...

          if(base64String != null)
          {
              //sending message directly to user
              this.stompService.sendAttachedFileToExtension(this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].extension,String(base64String),name,type,size);

              let currentChatLength = this.messageListDataService.allChatCurrent.length;
              let allChatLength = this.messageListDataService.allChatData.length;
                
              //updating local current variable
              if(currentChatLength !=0)
                    {
                        if(this.messageListDataService.allChatCurrent[currentChatLength-1].fromExtension == ConstantsService.user.extension)
                        {
                          this.messageListDataService.attachFileToPreviousRecordCurrentMessagesPerType(String(base64String),name,type,size);
                        }
                        else{
                          this.messageListDataService.attachFileToNewRecordCurrentMessagesPerType(String(base64String),name,type,size,ConstantsService.user.extension,ConstantsService.user.firstName.trim()+" "+ConstantsService.user.lastName.trim(),ConstantsService.user.role);
                        }
                    }
              else{
                      this.messageListDataService.attachFileToNewRecordCurrentMessagesPerType(String(base64String),name,type,size,ConstantsService.user.extension,ConstantsService.user.firstName.trim()+" "+ConstantsService.user.lastName.trim(),ConstantsService.user.role);
                    }
                
              //updating local all chat variable
              if(allChatLength !=0)
                    {
                        if(this.messageListDataService.allChatData[allChatLength-1].fromExtension == ConstantsService.user.extension)
                        {
                          this.messageListDataService.attachFileToPreviousRecordAllMessagesPerType(String(base64String),name,type,size);
                        }
                        else{
                          this.messageListDataService.attachFileToNewRecordAllMessagesPerType(String(base64String),name,type,size,ConstantsService.user.extension,ConstantsService.user.firstName.trim()+" "+ConstantsService.user.lastName.trim(),ConstantsService.user.role);
                        }
                    }
              else{
                      this.messageListDataService.attachFileToNewRecordAllMessagesPerType(String(base64String),name,type,size,ConstantsService.user.extension,ConstantsService.user.firstName.trim()+" "+ConstantsService.user.lastName.trim(),ConstantsService.user.role);
                    }

              //sending chat history data to back-end after 3 seconds
              // this.messageListDataService.updateChatHistory(this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].extension,this.messageListDataService.allChatCurrent,3000);
          }
          else
          {
            this.showDialoge('Error','activity-outline','danger', "Cannot send null message !!!"); 
          }

      };
    }
    catch(e)
    {
      //console.log("error");
        this.file = null;
        this.showDialoge('Error','activity-outline','danger', e); 
    }
  }


  

  downloadMessageFile(i:any,j:any)
  {
    // console.log("downloadMessageFile : i : ",i," j: "+j);
    try
    {
      // console.log("Creating anchor tag");
      const downloadLink = document.createElement("a");
      // console.log("Defining href");
      downloadLink.href = this.messageListDataService.allChatData[i].messages[j].blobMessage;
      // console.log("Defining filename");
      downloadLink.download = this.messageListDataService.allChatData[i].messages[j].fileName+"."+this.messageListDataService.allChatData[i].messages[j].blobType;
      // console.log("Click Anchor");
      downloadLink.click();
    }
     catch(e)
    {
      //console.log("error");
        this.showDialoge('Error','activity-outline','danger', e); 
    }
  }

  sendMessage()
  {

    if(this.voiceRecognitionService.setSendMessageId != null)
    {
      clearTimeout(this.voiceRecognitionService.setSendMessageId);
      this.voiceRecognitionService.setSendMessageId = null;
    }

    if(ConstantsService.user.textMessagingOption)
    {
          //  console.log("sendMessageButtonClicked");
          // console.log("this.voiceRecognitionService.messageValue");
          // console.log(this.voiceRecognitionService.messageValue);
          let messageSubType = "";
          //creating message
          if(this.voiceRecognitionService.messageValue == "" || this.voiceRecognitionService.messageValue == ' '  || this.voiceRecognitionService.messageValue == "   " || this.voiceRecognitionService.messageValue == "    ")
          {
            this.showDialoge('Error','activity-outline','danger', "Cannot send null message !!!"); 
          }
          else{
              const now = new Date(); 
              let message:any = {};
              if(this.voiceRecognitionService.messageValue.includes("@"))
                  {
                    messageSubType = 'anchor';
                    message={
                      messageSubType:'anchor',
                      stringMessage:null,
                      anchorMessage:this.voiceRecognitionService.messageValue,
                      dateTime:now,
                    };
                  }
              else{
                    messageSubType = 'string';
                    message={
                      messageSubType:'string',
                      stringMessage:this.voiceRecognitionService.messageValue,
                      anchorMessage:null,
                      dateTime:now,
                    };
              }

              //sending message directly to user
              this.stompService.sendMessageToExtension(this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].extension,ConstantsService.user.organization,ConstantsService.user.domain,this.voiceRecognitionService.messageValue,messageSubType);
                    
              //Setting current chat variable as "". When we send message input box should become emoty
              this.voiceRecognitionService.messageValue = "";

              let currentChatLength = this.messageListDataService.allChatCurrent.length;
              let allChatLength = this.messageListDataService.allChatData.length;
          
              //updating local current variable
              if(currentChatLength !=0)
              {
                  // console.log("Setting current data");
                  if(this.messageListDataService.allChatCurrent[currentChatLength-1].fromExtension == ConstantsService.user.extension)
                  {
                      this.messageListDataService.setPreviousRecordCurrentMessageAsPerType(message);
                  }
                  else{
                    this.messageListDataService.setNewCurrentMessageAsPerType(message,ConstantsService.user.extension,ConstantsService.user.firstName.trim()+" "+ConstantsService.user.lastName.trim(),ConstantsService.user.role);
                  }
              }
              else{
                this.messageListDataService.setNewCurrentMessageAsPerType(message,ConstantsService.user.extension,ConstantsService.user.firstName.trim()+" "+ConstantsService.user.lastName.trim(),ConstantsService.user.role);
              }
          
              //updating local all chat variable
              if(allChatLength !=0)
              {
                  console.log("Setting all data");
                  if(this.messageListDataService.allChatData[allChatLength-1].fromExtension == ConstantsService.user.extension)
                  {
                      this.messageListDataService.setPreviousRecordAllMessageAsPerType(message);
                  }
                  else{
                    this.messageListDataService.setNewAllMessageAsPerType(message,ConstantsService.user.extension,ConstantsService.user.firstName.trim()+" "+ConstantsService.user.lastName.trim(),ConstantsService.user.role);
                  }
              }
              else{
                this.messageListDataService.setNewAllMessageAsPerType(message,ConstantsService.user.extension,ConstantsService.user.firstName.trim()+" "+ConstantsService.user.lastName.trim(),ConstantsService.user.role);
              }
          
              //sending chat history data to back-end after 3 seconds
              this.messageListDataService.updateChatHistory(this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].extension,this.messageListDataService.allChatCurrent,3000);
            }
    }
    else{
          this.showDialoge('Error','activity-outline','danger', "Sending messaging is not allowed for this user. Kindly contact your manager."); 
    }
 }

 
  clearAllChats()
  {
    // console.log("clearallChats");
    this.headerVariableService.messageBadgeDot = false;
    this.showDeleteDialoge("All current chats will be deleted ?","allChats"); 
    
  }

  deleteAllChats()
  {
    this.messageListDataService.clearAllChats();
  }


  clearSingleChat(i:any){
    // console.log("clearSingleChats : ",i);
    this.headerVariableService.messageBadgeDot = false;
    this.singleChatDeleteIndex = i;
    this.showDeleteDialoge("Current chat with "+this.messageListDataService.participants[this.singleChatDeleteIndex].firstName+" will be deleted ?","singleChat"); 
    
  }

  deleteSingleChat()
  {
    this.messageListDataService.clearSingleChat(this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].extension,this.messageListDataService.participants[this.singleChatDeleteIndex].extension,this.messageListDataService.participants[this.singleChatDeleteIndex].lastReadIndex,this.singleChatDeleteIndex);
  }

  makeACallToParticipant(i){
    // console.log("makeACallToParticipant : ",i);
    try{
      this.dialLineService.audioCall(this.browserPhoneService.userAgent,this.browserPhoneService.lang,this.messageListDataService.participants[i].extension,"",this.browserPhoneService.didLength);
      }
    catch(e)
      {
          console.log(e);
          // console.log("Error : "+ JSON.stringify(err));
          this.showDialoge('Error','activity-outline','danger', JSON.stringify(e)); 
      }
  }

  allParticipantWrapperFunc()
  {

    if(this.isAllParticipantsWrapped)
    {
      this.clearAll = "Clear All";
      this.clearAllButton.style.display = 'inline';
      this.searchAllBox.style.display = 'inline-block';
      this.isAllParticipantsWrapped = false;

      if(this.headerVariableService.messagefullScreenEnabled)

      {
        this.mainMessageWrapperWidth = 70;
        this.mainMessageWrapperHeight = 100;
        this.allParticipantsWrapperWidth = 30;
        this.allParticipantsWrapperHeight = 100;
      }
      else{
        this.mainMessageWrapperWidth = 50;
        this.mainMessageWrapperHeight = 100;
        this.allParticipantsWrapperWidth = 50;
        this.allParticipantsWrapperHeight = 100;
      }
    }
    else{

      this.clearAll = "";
      this.clearAllButton.style.display = 'none';
      this.searchAllBox.style.display = 'none';
      this.isAllParticipantsWrapped = true;
      this.mainMessageWrapperWidth = 100;
      this.mainMessageWrapperHeight = 100;
      this.allParticipantsWrapperWidth = 0;
      this.allParticipantsWrapperHeight = 100;

    }

  }

  participantDataFunc()
  {

    if(this.isParticipantMessageDataWrapped)
    {
      this.isParticipantMessageDataWrapped = false;
      this.participantdataDiv.style.display = 'inline-block';
      this.messageContentWrapperWidth =100;
      this.messageContentWrapperHeight = 70;
      this.participantDataWidth = 100;
      this.participantDataHeight = 30;
      
    }
    else{
      this.isParticipantMessageDataWrapped = true;
      this.participantdataDiv.style.display = 'none';
      this.messageContentWrapperWidth =100;
      this.messageContentWrapperHeight = 100;
      this.participantDataWidth = 100;
      this.participantDataHeight = 0;

    }

  }

  messageParticipantClicked(i:any){
    // console.log ("messageParticipantClicked  : ",i);
    this.headerVariableService.messageBadgeDot = false;

    if(this.messageListDataService.sendChatHistoryDataId !=null)
    {
      clearTimeout(this.messageListDataService.sendChatHistoryDataId);
      this.messageListDataService.updateChatHistoryNow(this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].extension,this.messageListDataService.allChatCurrent);
    }
    this.currentPic=null;
    this.messageListDataService.currentSelectedParticipantNumber = i;
    this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].badgeText = 0;
    this.setCurrentChatData(this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].email);
    this.messageListDataService.getChatHistory(this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].extension,this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].lastReadIndex);
  }

  onChange() {
    // console.log('onChange');
    if(this.input.nativeElement.value=='' || this.input.nativeElement.value==' ')
    {
      this.messageListDataService.filteredOptions$ = of(this.messageListDataService.allEmployeesData);
    }
    else{
      this.messageListDataService.filteredOptions$ = this.messageListDataService.getFilteredOptions(this.input.nativeElement.value);
    }
  }

  onSelectionChange($event) {
    // console.log('onSelectionChange');
    // console.log($event);
    this.headerVariableService.messageBadgeDot = false;
    if(this.messageListDataService.sendChatHistoryDataId !=null)
    {
      clearTimeout(this.messageListDataService.sendChatHistoryDataId);
      this.messageListDataService.updateChatHistoryNow(this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].extension,this.messageListDataService.allChatCurrent);
    }

    let allEmployeeIndex = -1;
    allEmployeeIndex = this.messageListDataService.allEmployeesData.findIndex(obj => obj.extension == $event);
    // console.log("Index : ",allEmployeeIndex);
    this.messageListDataService.currentSelectedParticipantNumber = 0;
    //Set input string
    this.input.nativeElement.value = this.messageListDataService.allEmployeesData[allEmployeeIndex].firstName + " "+this.messageListDataService.allEmployeesData[allEmployeeIndex].lastName;

    //Reset filtered Options
    this.messageListDataService.filteredOptions$ = of(this.messageListDataService.allEmployeesData);

    let participantIndex = -1;
    participantIndex = this.messageListDataService.participants.findIndex(obj => obj.extension == $event);

    if(participantIndex == -1)
    {
        let record:any = this.messageListDataService.allEmployeesData[allEmployeeIndex];
            record.lastReadIndex=0;
            record.badgeText=0;
        this.messageListDataService.participants.unshift(record);
    }
    else if (participantIndex > -1){
        let record:any = this.messageListDataService.participants[participantIndex];
        this.messageListDataService.participants.splice(participantIndex, 1);
        this.messageListDataService.participants.unshift(record);
    }
    this.messageListDataService.currentSelectedParticipantNumber = 0;
    this.currentPic=null;
    
    this.setCurrentChatData(this.messageListDataService.allEmployeesData[allEmployeeIndex].email);
    this.messageListDataService.getChatHistory(this.messageListDataService.allEmployeesData[allEmployeeIndex].extension,this.messageListDataService.allEmployeesData[allEmployeeIndex].lastReadIndex);
  }

 

  fullScreen(){

    // console.log(this.headerVariableService.messagefullScreenEnabled);
    const element =  Array.from(document.getElementsByClassName('allMessages') as HTMLCollectionOf<HTMLElement>)[0];
    if(this.headerVariableService.messagefullScreenEnabled)
    {
      this.headerVariableService.messageDragPosition = this.headerVariableService.messagePreviousDragPosition;
      // console.log('this.headerVariableService.messageDragPosition after enabled',this.headerVariableService.messageDragPosition);
      // console.log('this.headerVariableService.messagePreviousDragPosition after enabled',this.headerVariableService.messagePreviousDragPosition);
      // console.log('this.headerVariableService.width after enabled',this.headerVariableService.width);
      // console.log('this.headerVariableService.height after enabled',this.headerVariableService.height);
      element.style.width =this.headerVariableService.messageParticipantWidth + 'px';
      element.style.height =this.headerVariableService.messageParticipantHeight + 'px';
      this.headerVariableService.messagefullScreenEnabled = false;
      this.participantImageHeight = 80;
      this.participantImageWidth = 55;
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
          this.headerVariableService.messageParticipantWidth = Number(String(element.style.width).split('.')[0].trim());
          this.headerVariableService.messageParticipantHeight = Number(String(element.style.height).split('.')[0].trim());
          this.headerVariableService.messagePreviousDragPosition = this.headerVariableService.messageDragPosition;
       }
     
   //  console.log('this.headerVariableService.width',this.headerVariableService.width);
    //  console.log('this.headerVariableService.messageDragPosition after enabled',this.headerVariableService.messageDragPosition);
    //  console.log('this.headerVariableService.messagePreviousDragPosition after enabled',this.headerVariableService.messagePreviousDragPosition);

      this.headerVariableService.messageDragPosition = {x: 0, y: 0};

      element.style.width = window.innerWidth + 'px';
      element.style.height = window.innerHeight + 'px';

      
     // element.style.backgroundPositionX = '0.0000rem';
      //element.style.backgroundPositionY = '0.0000rem';
      this.headerVariableService.messagefullScreenEnabled = true;
      this.participantImageHeight = 80;
      this.participantImageWidth = 30;

    }


    if(!this.isParticipantMessageDataWrapped)
    document.getElementById('participantDataButton').click();

    if(!this.isAllParticipantsWrapped)
    document.getElementById('allParticipantsButton').click();
    //document.getElementById('messageContentButton').click();
  }

  toggle() {
    this.sidebarService.toggle(false, 'down');
  }

  toggleCompact() {
    this.sidebarService.toggle(true, 'right');
  }

  loadAllParticipantsData()
  {

    let destroy$ = new Subject<void>();
    // console.log("loadAllParticipantsData");

    let data = {
        id:0,
        extension:ConstantsService.user.extension,
        organization:localStorage.getItem("organization")
    };

    this.chatHistoryService.getAllChatHistoryCandidatesByExtensionAndOrganization(data)
    .pipe(takeUntil(destroy$))
    .subscribe({
      next: (result) => {
               //What should happen when it is saved
               if (result != null)
               {
                this.messageListDataService.participants = [... JSON.parse(JSON.stringify(result))];
                if (this.messageListDataService.participants.length != 0)
                {
                    this.setCurrentChatData(this.messageListDataService.participants[0].email);
                    this.messageListDataService.currentSelectedParticipantNumber = 0;
                    this.messageListDataService.getChatHistory(this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].extension,this.messageListDataService.participants[this.messageListDataService.currentSelectedParticipantNumber].lastReadIndex);
                }

                this.messageListDataService.participants.forEach((participant : any)=>{

                  let allEmployeeIndex = -1;
                  allEmployeeIndex = this.messageListDataService.allEmployeesData.findIndex(obj => obj.extension == participant.extension);

                  if(allEmployeeIndex != null)
                    {
                        participant.presence = this.messageListDataService.allEmployeesData[allEmployeeIndex].presence;
                        participant.state = this.messageListDataService.allEmployeesData[allEmployeeIndex].state;
                        participant.dotClass = this.messageListDataService.allEmployeesData[allEmployeeIndex].dotClass;
                        participant.channel = this.messageListDataService.allEmployeesData[allEmployeeIndex].channel;
                    }

              });

               }
               else{
                this.messageListDataService.participants = [];
                this.messageListDataService.disableChatButton = true;
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

  setCurrentChatData(email:any)
  {
    this.employeeService.getEmployeeByEmailAndOrganization(email,localStorage.getItem("organization"))
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: data => {

       //console.log(JSON.parse(JSON.stringify(data)));
       //console.log(JSON.parse(JSON.stringify(data)).parkedchannel2);
        if(data == null)
        {
          //console.log("data is null");
          this.showDialoge('Error','activity-outline','danger', "Employee not found for this email"); 
        }
        else
        {
          //console.log("data is not null");
          this.messageListDataService.currentRecord = data;
          
          let allEmployeeIndex = -1;
          allEmployeeIndex = this.messageListDataService.allEmployeesData.findIndex(obj => obj.extension == this.messageListDataService.currentRecord.extension);

          if(allEmployeeIndex != null)
            {
              this.messageListDataService.currentRecord.presence = this.messageListDataService.allEmployeesData[allEmployeeIndex].presence;
              this.messageListDataService.currentRecord.state = this.messageListDataService.allEmployeesData[allEmployeeIndex].state;
              this.messageListDataService.currentRecord.dotClass = this.messageListDataService.allEmployeesData[allEmployeeIndex].dotClass;
              this.messageListDataService.currentRecord.channel = this.messageListDataService.allEmployeesData[allEmployeeIndex].channel;
            }

          this.messageListDataService.disableChatButton = false;
          this.setImageData();
        }
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.messageListDataService.disableChatButton = true;
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  setImageData()
  {
    this.employeeService.getEmployeeImages(this.messageListDataService.currentRecord.email,this.messageListDataService.currentRecord.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (allData:any) => {

        if(allData == null)
        {
          //console.log("I am null data");
          //console.log("Inside Data : "+ JSON.stringify(allData));
            this.currentPic = null; 
        }
        else
        {
            //console.log("Inside Data : "+ JSON.stringify(allData));  
            allData.forEach((current:any,i:number)=>{
              
              try{
                    if(current != null)
                    {
                      try{
                          //  console.log("this.currentByteImageData is not null");
                          //  console.log("current");
                          //  console.log(current);
                           if(i == 0)
                           {
                            let url = 'data:image/'+this.messageListDataService.currentRecord.imageType+';base64,'+current;
                            this.currentPic = url;
                            this.currentPic = this.currentPic.replace("/image","");
                           }
                      }  
                      catch(e)
                      {
                        // console.log(e);
                      }
                    }  
                    
                  }
              catch(e)
              {
                  this.currentPic = null;
              }
            });
        }
      },
      error: err => {
       // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  get_blob_from_string (string, type) {
        let array = new Uint8Array(string.length);
        for (let i = 0; i < string.length; i++){
            array[i] = string.charCodeAt(i);
        }
        let end_file = new Blob([array], {type: type});
        return end_file;
   }

   startListening()
   {

        // check for support (webkit only)
	      if (!('webkitSpeechRecognition' in window))
        {
          console.log("Browser does not supports webkitSpeechRecognition");
          this.showDialoge('Error','activity-outline','danger', "Speech Recognition is not supported. Kindly update your browser"); 
        }
        else
        {
          console.log("Browser supports webkitSpeechRecognition");
          if(this.voiceRecognitionService.listeningNow)
          {
            this.voiceRecognitionService.stop();
          }
          else{
            this.voiceRecognitionService.start();
          }
        }
   }

 

   showSendFileDialoge(message:string,type:string) {

    this.dialogService.open(AskSendFileComponent, {
      context: {
        message: message,
        type: type
      },
    }).onClose.subscribe((type) => {
      // console.log("Delete dialog is closed");
      // console.log("Something of type is delete : ",type);
      if(type != undefined && type == "peer")
        {   
           this.sendFileToPeer();    
        }
      else if(type != undefined && type == "server")
        {
           this.sendFileViaServer();
        }
    });
}

  showDeleteDialoge(message:string,type:string) {

        this.dialogService.open(AskDeleteComponent, {
          context: {
            message: message,
            type: type
          },
        }).onClose.subscribe((type) => {
          // console.log("Delete dialog is closed");
          // console.log("Something of type is delete : ",type);
          if(type != undefined && type == "singleChat")
            {   
               this. deleteSingleChat();    
            }
          else if(type != undefined && type == "allChats")
            {
               this.deleteAllChats();
            }
        });
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


function makeMessageResizableDiv(div:any) {
  var previousX:any;
  var set = false;

  const element =  Array.from(document.getElementsByClassName('allMessages') as HTMLCollectionOf<HTMLElement>)[0];
  const resizers = document.querySelectorAll(div + ' .message-resizer')
  for (let i = 0;i < resizers.length; i++) {
    const currentResizer = resizers[i];
    currentResizer.addEventListener('mousedown', function(e) {

     // console.log("Inside currentResizer : mouse down");

      e.preventDefault()
      window.addEventListener('mousemove', resize)
      window.addEventListener('mouseup', stopDesktopResize)
    })

    currentResizer.addEventListener('touchstart', function(e) {

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


      if (currentResizer.classList.contains('message-bottom-right')) {
       
       // console.log("Going inside current resizer buttom right");
        
        if((Number(currentObject.top) <= 2 || Number(currentObject.right) <= 2 ||Number(currentObject.bottom) <= 150 ||Number(currentObject.left) <= 2))
        {
            //console.log("Going nagative bottom-right");

            if(set&&(previousX>Number(e.pageX)))
            {
              //console.log("Inside conditon");
              // console.log('e.pageX',e.pageX);
              // console.log('currentObject.left',currentObject.left);
              element.style.width = e.pageX - currentObject.left + 'px';
              element.style.height = e.pageX - currentObject.left + 'px';
            }
           
        }
        else{
              //console.log("Not in nagative bottom-right");
              previousX = Number(e.pageX);
              set = true;
              element.style.width = e.pageX - currentObject.left + 'px';
              element.style.height = e.pageX - currentObject.left + 'px';
        }

      }
      else if (currentResizer.classList.contains('message-bottom-left')) {
        if(Number(currentObject.top) <= 2 || Number(currentObject.right) <= 2 ||Number(currentObject.bottom) <= 150 ||Number(currentObject.left) <= 2)
        {
         // console.log("Going nagative bottom-left");
          if(set&&(previousX<Number(e.pageX)))
          {
            element.style.width = currentObject.right - e.pageX + 'px'
            element.style.height = currentObject.right - e.pageX + 'px'
          }
        }
        else{
             // console.log("Not in nagative bottom-left");
              previousX = Number(e.pageX);
              set = true;
              element.style.width = currentObject.right - e.pageX + 'px'
              element.style.height = currentObject.right - e.pageX + 'px'
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


// export interface IWindow extends Window {
//   webkitSpeechRecognition: any;
// }

// const {webkitSpeechRecognition} : IWindow = <IWindow><unknown>window;
// const recognition = new webkitSpeechRecognition();

// /*global webkitSpeechRecognition */
// (function() {
// 	'use strict';

// 	// check for support (webkit only)
// 	if (!('webkitSpeechRecognition' in window)) return;

// 	var talkMsg = 'Speak now';
// 	// seconds to wait for more input after last
//   	var defaultPatienceThreshold = 6;

// 	function capitalize(str) {
// 		return str.charAt(0).toUpperCase() + str.slice(1);
// 	}

// 	var inputEls = document.getElementsByClassName('speech-input');

// 	[].forEach.call(inputEls, function(inputEl) {
// 		var patience = parseInt(inputEl.dataset.patience, 10) || defaultPatienceThreshold;
// 		var micBtn, micIcon, holderIcon, newWrapper;
// 		var shouldCapitalize = true;

// 		// gather inputEl data
// 		var nextNode = inputEl.nextSibling;
// 		var parent = inputEl.parentNode;
// 		var inputRightBorder = parseInt(getComputedStyle(inputEl).borderRightWidth, 10);
// 		var buttonSize = 0.8 * (inputEl.dataset.buttonsize || inputEl.offsetHeight);

// 		// default max size for textareas
// 		if (!inputEl.dataset.buttonsize && inputEl.tagName === 'TEXTAREA' && buttonSize > 26) {
// 			buttonSize = 26;
// 		}

// 		// create wrapper if not present
// 		var wrapper = inputEl.parentNode;
// 		if (!wrapper.classList.contains('si-wrapper')) {
// 			wrapper = document.createElement('div');
// 			wrapper.classList.add('si-wrapper');
// 			wrapper.appendChild(parent.removeChild(inputEl));
// 			newWrapper = true;
// 		}

// 		// create mic button if not present
// 		micBtn = wrapper.querySelector('.si-btn');
// 		if (!micBtn) {
// 			micBtn = document.createElement('button');
// 			micBtn.type = 'button';
// 			micBtn.classList.add('si-btn');
// 			micBtn.textContent = 'speech input';
// 			micIcon = document.createElement('span');
// 			holderIcon = document.createElement('span');
// 			micIcon.classList.add('si-mic');
// 			holderIcon.classList.add('si-holder');
// 			micBtn.appendChild(micIcon);
// 			micBtn.appendChild(holderIcon);
// 			wrapper.appendChild(micBtn);

// 			// size and position mic and input
// 			micBtn.style.cursor = 'pointer';
// 			micBtn.style.top = 0.125 * buttonSize + 'px';
// 			micBtn.style.height = micBtn.style.width = buttonSize + 'px';
// 			inputEl.style.paddingRight = buttonSize - inputRightBorder + 'px';
// 		}

// 		// append wrapper where input was
// 		if (newWrapper) parent.insertBefore(wrapper, nextNode);

// 		// setup recognition
// 		var prefix = '';
// 		var isSentence;
// 		var recognizing = false;
// 		var timeout;
// 		var oldPlaceholder = null;
// 		var recognition = new webkitSpeechRecognition();
// 		recognition.continuous = true;
// 		recognition.interimResults = true;

// 		// if lang attribute is set on field use that
// 		// (defaults to use the lang of the root element)
// 		if (inputEl.lang) recognition.lang = inputEl.lang;

// 		function restartTimer() {
// 			timeout = setTimeout(function() {
// 				recognition.stop();
// 			}, patience * 1000);
// 		}

// 		recognition.onstart = function() {
// 			oldPlaceholder = inputEl.placeholder;
// 			inputEl.placeholder = inputEl.dataset.ready || talkMsg;
// 			recognizing = true;
// 			micBtn.classList.add('listening');
// 			restartTimer();
// 		};

// 		recognition.onend = function() {
// 			recognizing = false;
// 			clearTimeout(timeout);
// 			micBtn.classList.remove('listening');
// 			if (oldPlaceholder !== null) inputEl.placeholder = oldPlaceholder;

// 			// If the <input> has data-instant-submit and a value,
// 			if (inputEl.dataset.instantSubmit !== undefined && inputEl.value) {
// 				// submit the form it's in (if it is in one).
// 				if (inputEl.form) inputEl.form.submit();
// 			}
// 		};

// 		recognition.onresult = function(event) {
// 			clearTimeout(timeout);

// 			// get SpeechRecognitionResultList object
// 			var resultList = event.results;

// 			// go through each SpeechRecognitionResult object in the list
// 			var finalTranscript = '';
// 			var interimTranscript = '';
// 			for (var i = event.resultIndex; i < resultList.length; ++i) {
// 				var result = resultList[i];

// 				// get this result's first SpeechRecognitionAlternative object
// 				var firstAlternative = result[0];

// 				if (result.isFinal) {
// 					finalTranscript = firstAlternative.transcript;
// 				} else {
// 					interimTranscript += firstAlternative.transcript;
// 				}
// 			}

// 			// capitalize transcript if start of new sentence
// 			var transcript = finalTranscript || interimTranscript;
// 			transcript = !prefix || isSentence ? capitalize(transcript) : transcript;

// 			// append transcript to cached input value
// 			inputEl.value = prefix + transcript;

// 			// set cursur and scroll to end
// 			inputEl.focus();
// 			if (inputEl.tagName === 'INPUT') {
// 				inputEl.scrollLeft = inputEl.scrollWidth;
// 			} else {
// 				inputEl.scrollTop = inputEl.scrollHeight;
// 			}

// 			restartTimer();
// 		};

// 		micBtn.addEventListener('click', function(event) {
// 			event.preventDefault();

// 			// stop and exit if already going
// 			if (recognizing) {
// 				recognition.stop();
// 				return;
// 			}

// 			// Cache current input value which the new transcript will be appended to
// 			var endsWithWhitespace = inputEl.value.slice(-1).match(/\s/);
// 			prefix = !inputEl.value || endsWithWhitespace ? inputEl.value : inputEl.value + ' ';

// 			// check if value ends with a sentence
// 			isSentence = prefix.trim().slice(-1).match(/[\.\?\!]/);

// 			// restart recognition
// 			recognition.start();
// 		}, false);
// 	});
// })();

