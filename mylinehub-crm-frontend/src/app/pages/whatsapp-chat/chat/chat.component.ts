import { ChangeDetectorRef, Component, DoCheck, ElementRef, OnChanges, OnDestroy, OnInit, QueryList, SimpleChanges, ViewChild, ViewChildren } from "@angular/core";
import { NbDialogService, NbThemeService } from "@nebular/theme";
import { trigger, keyframes, animate, transition } from '@angular/animations';
import * as kf from '../../../../keyframes';
import { WhatsappDataServiceService } from "./whatsapp-data-service/whatsapp-data-service.service";
import { ConstantsService } from "../../../service/constants/constants.service";
import { FileStorageService } from "../../file-storage/service/file-storage.service";
import { WhatsappMessageService } from "../../../service/send-to-whatsapp/whatsapp-message.service";
import { BrowserPhoneService } from "../../../service/browser-phone/browser-phone.service";
import { StompService } from "../../../service/stomp/stomp.service";
import { CustomerService } from "../../customer/service/customer.service";
import { WhatsappChatHistoryService } from "../../../service/whatsapp-chat-history/whatsapp-chat-history.service";
import { WhatsappSpeechRecognitionService } from "../../../service/whats-app-speech-recognition/whatsapp-speech-recognition.service";
import { DialLineService } from "../../../service/browser-phone/dial-line/dial-line.service";
import { of, Subject, takeUntil } from "rxjs";
import { DialogComponent } from "../../employee/all-employees/dialog/dialog.component";
import { AskSendFileComponent } from "./ask-send-file/ask-send-file.component";
import { AskDeleteComponent } from "./ask-delete/ask-delete.component";
import { UploadStatusComponent } from "../../file-storage/upload-status/upload-status.component";
import { HttpEvent, HttpEventType } from "@angular/common/http";

@Component({
  selector: 'ngx-message-list',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss'],
  animations: [
    trigger('chatDisplayAnimator', [
      transition('* => wobble', animate(1000, keyframes(kf.wobble))),
      transition('* => swing', animate(1000, keyframes(kf.swing))),
      transition('* => jello', animate(1000, keyframes(kf.jello))),
      transition('* => zoomOutRight', animate(1000, keyframes(kf.zoomOutRight))),
      transition('* => slideOutLeft', animate(1000, keyframes(kf.slideOutLeft))),
      transition('* => rotateOutUpRight', animate(1000, keyframes(kf.rotateOutUpRight))),
      transition('* => flip', animate(1000, keyframes(kf.flip))),
      transition('* => flipInX', animate(1000, keyframes(kf.flipInX))),
      transition('* => flipInY', animate(1000, keyframes(kf.flipInY))),
      transition('* => flipOutX', animate(1000, keyframes(kf.flipOutX))),
      transition('* => flipOutY', animate(1000, keyframes(kf.flipOutY))),
      transition('* => rotateOutY', animate(1300, keyframes(kf.rotateOutY))),
    ]),
    trigger('fixedChatDisplayAnimator', [
      transition('* => bounce', animate(1300, keyframes(kf.bounce))),
    ])
  ]
})
export class ChatComponent implements OnInit,OnDestroy,DoCheck {

  private destroy$: Subject<void> = new Subject<void>();
  clearAll = "Clear All Chats";

  maxNameLength = 16;
  currentByteImageData:any = null; //Used Internally, raw image data object received from backend
  base64ImageData:any = null; //Using currentByteImageData, this variable is created

  //Used in getScreensize to find out if its mobile or not
  screenWidth: number;
  screenHeight: number;
  itsMobile:boolean = false;

  //Movable Parts Variables
  
  startedSelectedActionFirstTime:boolean = false;
  buttonGroupSize = "medium";
  displayBlockAnimationState: string = '';
  fixedChatDisplayAnimationString :string = '';

  //Theme vairables
  themeIsReady = false;
  currentTheme: any;
  redirectDelay: number = 0;
  themeSubscription: any;

  isWhite:boolean = false;

  //Piping Variable
  setMessageTypeAsPerAttachment: boolean = false;
  
  startOffset:number = 0;
	endOffset:number = 3;
  showCustomeCloseAI : boolean = false;
  
  @ViewChild('autoWhatsAppParticipantListInput') input;
  @ViewChildren('toggleBtn') toggleButtons!: QueryList<ElementRef<HTMLButtonElement>>;
  uploadStatusRef: import("@nebular/theme").NbDialogRef<UploadStatusComponent>;
 
  searhToggleEnabled: boolean = false;


  constructor(private themeService: NbThemeService,
              private cdr: ChangeDetectorRef,
              protected whatsappDataServiceService: WhatsappDataServiceService,
              private stompService:StompService,
              private browserPhoneService: BrowserPhoneService,
              // protected headerVariableService:HeaderVariableService,
              private dialogService: NbDialogService,
              private customerService : CustomerService,
              private whatsappChatHistoryService : WhatsappChatHistoryService,
              protected voiceRecognitionService : WhatsappSpeechRecognitionService,
              protected dialLineService:DialLineService,
              private whatsappMessageService:WhatsappMessageService,
              private fileStorageService:FileStorageService,
              private constantsService: ConstantsService) {

      this.themeSubscription = this.themeService.getJsTheme().subscribe(theme => {
          this.currentTheme = theme.name;
          this.themeIsReady = true;

           if(this.currentTheme.toLowerCase() === 'dark'){
            console.log("Changing logo to dark");
            this.isWhite= false;
          }
          else if(this.currentTheme.toLowerCase() === 'cosmic'){
            console.log("Changing logo to cosmic");
            this.isWhite= false;
          }
          else{
            console.log("Changing logo to white");
            this.isWhite= true;
          }

      });
      
    }

    handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault(); // Prevent newline
        this.whatsAppSendMessage(); // Send message
      }
    }

    formatMentions(message: string): string {
      if (!message) return '';
      // Replace any @word (letters, numbers, underscore allowed) with <a> tag
      return message.replace(/(@\w+)/g, '<a class="whats-app-mention">$1</a>');
    }

    isValidName(name: string | null | undefined): boolean {
      if (!name) return false;
      const trimmed = name.trim();
      return !/^[\d+]/.test(trimmed); // false if starts with digit OR +
    }

    selectButton(value: string): void {
      const matchingBtn = this.toggleButtons.find(
        (btn) => btn.nativeElement.getAttribute('value') === value
      );

      if (matchingBtn) {
        matchingBtn.nativeElement.click(); // Simulates user click
      } else {
        console.warn(`Button with value '${value}' not found`);
      }
      this.cdr.detectChanges();
    }
  
   public isWhatsAppMessageText(type: string): boolean {
      return ConstantsService.whatsAppText.includes(type);
   }

   toggleAutoReply() {
      const record = this.whatsappDataServiceService.currentCustomerRecord;
      record.autoWhatsAppAIReply = !record.autoWhatsAppAIReply;
      this.onAutoReplyToggle(record.autoWhatsAppAIReply); // existing handler
    }

    getToggleStyle() {
      const isOn = this.whatsappDataServiceService.currentCustomerRecord?.autoWhatsAppAIReply;
      return {
        backgroundColor: isOn ? '#4CAF50' : '#f44336', // green / red
      };
    }

    getCircleStyle() {
      const isOn = this.whatsappDataServiceService.currentCustomerRecord?.autoWhatsAppAIReply;
      return {
        backgroundColor: '#fff',
        transform: isOn ? 'translateX(18px)' : 'translateX(0)',
      };
    }

    onSearhToggleChange($event){
      console.log('onSearhToggleChange');
      console.log($event);
      this.searhToggleEnabled = $event;
    }

   onAutoReplyToggle($event){
    console.log('onAutoReplyToggle');
    console.log($event);

    let data = {
      id: this.whatsappDataServiceService.currentCustomerRecord.id,
      organization: this.whatsappDataServiceService.currentCustomerRecord.organization,
      value: $event,
      email: this.whatsappDataServiceService.currentCustomerRecord.email
    };

    this.customerService.updateWhatsAppAIAutoMessage(data)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
              // console.log("Result is true, setting values to show on icon"); 
            }
            else{
               // console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "Custome auto reply was not updated"); 
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
    });
   }

   ngAfterViewInit() {
      // ✅ Trigger a new, clean detection cycle to sync DOM and data
      this.cdr.detectChanges();
   }


   ngDoCheck() {
    const currentLength = this.whatsappDataServiceService.allChatData?.length || 0;
    if (currentLength !== this.whatsappDataServiceService.previousChatLength) {
      console.log("ngDoCheck value added to message list");
      this.whatsappDataServiceService.previousChatLength = currentLength;
      // Wait a tick so Angular finishes rendering the new messages
      setTimeout(() => this.scrollToBottom(), 0);
      this.cdr.detectChanges();
    }
  }

  private scrollToBottom() {
    setTimeout(() => {
          console.log('Scroll started');
         // dynamically get the element every time
          const el = document.getElementById('whatsAppChatArea');

          if (el) {
            console.log('✅ Before scroll :', el.scrollTop, el.scrollHeight);
            el.scrollTop = el.scrollHeight;
            console.log('✅ Scrolled to bottom:', el.scrollTop, el.scrollHeight);
          } else {
            console.warn('⚠️ Chat area not found (maybe *ngIf hidden or not rendered yet)');
          }      
    }, 1800);

  }


  ngOnInit() {

    console.log("ngOnIt");

     if(ConstantsService.user.firstName !== undefined){
          console.log("Loading chat data");
          this.setupNgOnInitData();
     }
     else{
         setTimeout(() => {
            console.log("Delay Chat load due to unavailability of data");
            this.setupNgOnInitData();
          }, ConstantsService.DIRECT_REFRESH_PAGE_TIME_MS); // 2000 milliseconds = 2 seconds
     }  
  }

  setupNgOnInitData(){

    this.getScreenSize();

    console.log('setupNgOnInitData');

    const scrollContainers = document.getElementsByClassName('scrollable-container');
    if (scrollContainers.length > 0) {
        const container = scrollContainers[0] as HTMLElement;
        container.style.overflow = 'hidden';
         this.cdr.detectChanges();
    }

    setTimeout(() => {
      const header = document.querySelector('nb-layout-header') as HTMLElement;
      const targetBlock = document.getElementById('whatsAppChatFixedOuterBlock') as HTMLElement;

      if (header && targetBlock) {
        const headerHeight = header.offsetHeight;
        let availableHeight;

        if(this.screenWidth<=500){
              availableHeight = ((window.innerHeight - headerHeight)-32);
        }
        else if(this.screenWidth>500 && this.screenWidth <=900){
              availableHeight = ((window.innerHeight - headerHeight)-47);
        }

         else if(this.screenWidth >900){
              availableHeight = ((window.innerHeight - headerHeight)-64);
        }

        targetBlock.style.height = `${availableHeight}px`;
        // Disable Safari momentum scrolling
        targetBlock.style['-webkit-overflow-scrolling'] = 'auto';
        console.log('Header height:', headerHeight);
        console.log('Target height set to:', availableHeight);
        this.cdr.detectChanges();

      }
    }, 50); // Ensures DOM is fully rendered before accessing elements

    setTimeout(() => {
        console.log("Loading Support data");
        console.log("Setting filter data");
        this.whatsappDataServiceService.filteredOptions$ = of(this.whatsappDataServiceService.allNumbersData);
        console.log("Initializing voice command");
        this.voiceRecognitionService.init();
        console.log("Initializing voice button");
        this.showCustomeCloseAI = ConstantsService.user.allowedToSwitchOffWhatsAppAI;
        this.whatsappDataServiceService.showTextDictate = ConstantsService.user.textDictateOption;
        console.log("this.showCustomeCloseAI : "+this.showCustomeCloseAI);
        console.log("this.whatsappDataServiceService.showTextDictate : "+this.whatsappDataServiceService.showTextDictate);

        if(this.whatsappDataServiceService.allNumbersData.length >0){
          console.log("Fetching first phoneNumberMain");
          this.whatsappDataServiceService.phoneNumberMain = this.whatsappDataServiceService.allNumbersData[0].phoneNumber;
          this.whatsappDataServiceService.currentSelectedPhoneNumber = 0;
          console.log("this.whatsappDataServiceService.currentSelectedPhoneNumber : "+this.whatsappDataServiceService.currentSelectedPhoneNumber);
          console.log("this.whatsappDataServiceService.phoneNumberMain : "+this.whatsappDataServiceService.phoneNumberMain);
          this.loadAllParticipantsDataAndConfigureFirstCustomer();
        }
        else{
          console.log("No support numbers presents to fetch participant data. All buttons still disabled.");
        }
         this.cdr.detectChanges();

    }, 1500); 

  }
  
  loadAllParticipantsDataAndConfigureFirstCustomer()
    {
  
      let destroy$ = new Subject<void>();
      console.log("loadAllParticipantsData for mobile : "+this.whatsappDataServiceService.phoneNumberMain);
  
      let data = {
        phoneMain:this.whatsappDataServiceService.phoneNumberMain,
        phoneWith:null,
        lastReadIndex:null,
        organization:localStorage.getItem("organization"),
        startOffset:this.startOffset,
	      endOffset:this.endOffset
      };
  
      this.whatsappChatHistoryService.getAllChatHistoryForPhoneNumberMain(data)
      .pipe(takeUntil(destroy$))
      .subscribe({
        next: (result) => {
                 //What should happen when it is saved
                 console.log("result generated ...");
                 if (result != null)
                 {
                  console.log("setting participants");
                  this.whatsappDataServiceService.participants = [... JSON.parse(JSON.stringify(result))];
                  console.log(this.whatsappDataServiceService.participants);
                  
                  if (this.whatsappDataServiceService.participants.length != 0)
                  {
                      console.log("Participant length greator than zero ...");
                      this.whatsappDataServiceService.sendMessageDisabled = false;
                      this.whatsappDataServiceService.allowCallDisabled = false;

                      if(!(ConstantsService.user.role === ConstantsService.employee)){
                        console.log("Enabling clear buttons as its not employee");
                        this.whatsappDataServiceService.clearChatDisabled = false;
                        this.whatsappDataServiceService.clearAllChatsDisabled = false;
                      }
                      else{
                        console.log("Employee will not get clear all chat button enabled");
                      }

                      console.log("Setting Participant index to zero ...");
                      this.whatsappDataServiceService.currentSelectedParticipantNumber = 0;

                      console.log("Get Customer information"); 
                      this.setCurrentCustomerInformation(this.whatsappDataServiceService.participants[0].phoneNumber);
                      
                      console.log("Get Customer image"); 
                      this.setCustomerImageData(this.whatsappDataServiceService.participants[0].phoneNumber);
                      
                      console.log("Get Chat history with customer"); 
                      this.whatsappDataServiceService.getChatHistory(this.whatsappDataServiceService.participants[0].phoneNumber);
                  }
                  else{
                    console.log("No participants (length is zero) for phone number : "+this.whatsappDataServiceService.phoneNumberMain);
                    this.whatsappDataServiceService.sendMessageDisabled = true;
                    this.whatsappDataServiceService.allowCallDisabled = true;
                    this.whatsappDataServiceService.clearChatDisabled = true;
                    this.whatsappDataServiceService.clearAllChatsDisabled = true;
                  }
                 }
                 else{
                    console.log("Result null for participants via phone number : "+this.whatsappDataServiceService.phoneNumberMain);
                    this.whatsappDataServiceService.noDataToShowSoSetToDefault();
                 }

                 destroy$.next();
                 destroy$.complete();
                this.cdr.detectChanges();
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
        });
  }

  setCurrentCustomerInformation(phoneNumberWith:any)
  {
    console.log('setCurrentCustomerInformation');
    this.customerService.getByPhoneNumberAndOrganization(phoneNumberWith,localStorage.getItem("organization"))
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: data => {

       //console.log(JSON.parse(JSON.stringify(data)));
       //console.log(JSON.parse(JSON.stringify(data)).parkedchannel2);
        if(data == null)
        {
          //console.log("data is null");
          this.showDialoge('Error','activity-outline','danger', "Customer not found for this phone number. Contact support."); 
        }
        else
        {
          //console.log("data is not null");
          this.whatsappDataServiceService.currentCustomerRecord = JSON.parse(JSON.stringify(data));
           this.cdr.detectChanges();
        }
      },
      error: err => {
      console.log("Error : "+ JSON.stringify(err));
        this.whatsappDataServiceService.noDataToShowSoSetToDefault();
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  setCustomerImageData(phoneNumberWith)
  {
    console.log('setCustomerImageData');
    this.customerService.getCustomerImage(phoneNumberWith,localStorage.getItem("organization"))
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (allData:any) => {
    
            if(allData == null)
            {  
                this.base64ImageData=null ;
                this.currentByteImageData=null;
            }
            else
            {
                this.currentByteImageData = allData; 
                try{
                  if(this.currentByteImageData != null)
                  {
                    // console.log("this.currentByteImageData is not null");
                    //  let uints = new Uint8Array(bytes);
                    //  let base64 = btoa(String.fromCharCode(null,... uints));
    
                     let url = 'data:image/'+this.currentByteImageData.type+';base64,'+this.currentByteImageData.byteData;
                     this.base64ImageData = url;
                     this.base64ImageData = this.base64ImageData.replace("/image","");
                  }
                  else{
                  }
                }
                catch(e)
                {
                  // console.log(e);
                  this.base64ImageData = null;  
                }
                 this.cdr.detectChanges();
            }
          },
          error: err => {
            console.log("Error while fetching cutomer Image");
           console.log("Error : "+ JSON.stringify(err));
            // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
  }
  
  onSupportPhoneInputValueChanged() {
      console.log('onSupportPhoneInputValueChanged');
      console.log('this.input.nativeElement.value : '+this.input.nativeElement.value);
      console.log(this.input);

      if(this.input != undefined){
        console.log('setting drop down value to select support number as per search parameter');
        if(this.input.nativeElement.value=='' || this.input.nativeElement.value==' ')
        {
          console.log('fetching all');
          this.whatsappDataServiceService.filteredOptions$ = of(this.whatsappDataServiceService.allNumbersData);
        }
        else{
          console.log('fetching as per search');
          this.whatsappDataServiceService.filteredOptions$ = this.whatsappDataServiceService.getFilteredOptions(this.input.nativeElement.value);
        } 
      }
      else{
        console.log("this.input was undefined");
      }
      this.cdr.detectChanges();
  }

  onWhatsAppParticipantSelectionChange($event) {
    console.log('onWhatsAppParticipantSelectionChange');
    console.log($event);

    console.log('Freeing sendChatHistoryDataId');
    if(this.whatsappDataServiceService.sendChatHistoryDataId !=null)
    {
      clearTimeout(this.whatsappDataServiceService.sendChatHistoryDataId);
    }

    let allNumberIndex = -1;
    allNumberIndex = this.whatsappDataServiceService.allNumbersData.findIndex(obj => obj.phoneNumber == $event);
    console.log("Index : ",allNumberIndex);

    if(allNumberIndex !== -1){

      console.log('Phone Number Main : '+this.whatsappDataServiceService.allNumbersData[allNumberIndex]+" has index : "+allNumberIndex);
      this.whatsappDataServiceService.currentSelectedPhoneNumber = allNumberIndex;
      this.whatsappDataServiceService.phoneNumberMain = this.whatsappDataServiceService.allNumbersData[allNumberIndex].phoneNumber;
      
      console.log('this.whatsappDataServiceService.currentSelectedPhoneNumber '+this.whatsappDataServiceService.currentSelectedPhoneNumber);
      console.log('Participant '+JSON.stringify(this.whatsappDataServiceService.participants[this.whatsappDataServiceService.currentSelectedPhoneNumber]));

      console.log('Set input string');
      //Set input string
      this.input.nativeElement.value = this.whatsappDataServiceService.phoneNumberMain;

      console.log('Reset filtered Options');
      //Reset filtered Options
      this.whatsappDataServiceService.filteredOptions$ = of(this.whatsappDataServiceService.allNumbersData);

      console.log('Load & first chat for that whats app number');
      //Load & first chat for that whats app number
      this.loadAllParticipantsDataAndConfigureFirstCustomer();
      this.cdr.detectChanges();
    }
    else{
      this.showDialoge('Error','activity-outline','danger', "Selected phone from drop down was not found. Kindly refresh page."); 
    }
  }

  startListening()
   {
        console.log("Start Listening Pressed");
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
 
  downloadMessageFile(i:any)
    {
      console.log("downloadMessageFile : i : ",i);
      console.log("this.whatsappDataServiceService.allChatData[i] ",this.whatsappDataServiceService.allChatData[i]);
      try
      {
  
       let destroy$ = new Subject<void>();
  
      let data = {
        phoneNumberMain:this.whatsappDataServiceService.phoneNumberMain,
        id:this.whatsappDataServiceService.allChatData[i].whatsAppMediaId,  //mediaID
        organization:localStorage.getItem("organization")
      };
  
      console.log("Calling URL API from whats app");
      this.whatsappMessageService.getWhatsAppMediaUrl(data)
      .pipe(takeUntil(destroy$))
      .subscribe({
        next: (result) => {
                 //What should happen when it is saved
                 if (result != null)
                 {
                        console.log("Creating anchor tag");
                        const downloadLink = document.createElement("a");
                        console.log("Defining href");
                        downloadLink.href = String(result);
                        console.log("Defining filename");
                        downloadLink.download = this.whatsappDataServiceService.allChatData[i].fileName+"."+this.whatsappDataServiceService.allChatData[i].blobType;
                        console.log("Click Anchor");
                        downloadLink.click();
                 }
                 else{
                  console.log("Download result was null. Contact support.")
                  this.showDialoge('Error','activity-outline','danger', "File was not found to download."); 
                 }
                 destroy$.next();
                 destroy$.complete();
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
        });
      }
       catch(e)
      {
        console.log("error : "+e);
          // this.showDialoge('Error','activity-outline','danger', e); 
      }
  }

  sendWhatsAppAttachedMessage()
  {
      const val = document.getElementById('setWhatsAppMediaMessageToDataService');
      val.click();
  }

  public reportProgress(httpEvent: HttpEvent<string[] | Blob>): void {
        console.log("httpEvent : ");
        console.log(httpEvent);
    
        switch(httpEvent.type) {
  
          case HttpEventType.UploadProgress:
             console.log("Upload in progress ...");
             if(this.fileStorageService.fileUploadStatus.status == 'done')
               {
                 this.showUploadStatusDialoge();
               }
                
             this.fileStorageService.updateUploadStatus(httpEvent.loaded, httpEvent.total!, 'Uploading... ');
            break;
  
          case HttpEventType.DownloadProgress:
            console.log("Download in progress ...");
            break;
  
          case HttpEventType.ResponseHeader:
            console.log("Response Header ...");
            console.log("httpEvent.headers : ",httpEvent.headers);
            break;
  
          case HttpEventType.Response:
            console.log("Response Body");
            console.log("httpEvent.body instanceof Array : ",httpEvent.body instanceof Array);
            console.log("Closing upload dialog box ...");
            try{
                 if(this.uploadStatusRef)
                  {
                    console.log("this.uploadStatusRef is open, now closing it");
                    this.uploadStatusRef.close();
                  }
            }
            catch(e)
            {
                console.log(e);
            }
  
            if (httpEvent.body instanceof Array) {
               console.log("Resonse body is instance of array");

                let element = JSON.parse(JSON.stringify(httpEvent.body))[0];
                console.log("element : ",element);
                console.log("Setting whats app media upload ID");
                this.whatsappDataServiceService.whatsAppMediaId = element.whatsAppMediaId;

                console.log("Sending media message to whats app with tet if avaiable");
                //sending message to whats app
                this.whatsAppSendMessage();
                this.cdr.detectChanges();
            } 
            else
            {
              console.log("Response body was not JSON Array");
              console.log("Body : "+httpEvent.body);
            }
            break;
  
            default:
            console.log("Inside Default For Switch having http event type : "+httpEvent.type);
            console.log(httpEvent);
            break;
          
        }
  }
    
  showUploadStatusDialoge() {
      
          if(!this.fileStorageService.isFileUploadDialogOpen)
            {
                this. uploadStatusRef = this.dialogService.open(UploadStatusComponent, {
                    context: {
                      fileStorageService:this.fileStorageService,
                    },
                  });
          
                this.uploadStatusRef.onClose.subscribe((value) => {
                  console.log("Upload dialog is closed");
                  console.log("Value : ",value);
                  this.fileStorageService.isFileUploadDialogOpen = false;
                });
            }
  }

    
  setWhatsAppMediaMessageToDataService($event)
    {
      console.log("setMediaMessageToDataService");
      console.log("Added file to service data");
      this.whatsappDataServiceService.file = $event.target.files[0];
      console.log("Starting dialoge");
      this.showSendFileDialoge('Kindly confirm if you want to transfer file to '+ this.whatsappDataServiceService.currentCustomerRecord.firstname+". Adding below text is not mandatory.");
      console.log("Making value nothing");
      $event.target.value = "";
  }
  

  sendMediaToWhatsApp()
    { 
      console.log("sendMediaToWhatsApp");
      try
      {
        let lastIndex = this.whatsappDataServiceService.file.name.lastIndexOf('.');
        console.log("lastIndex : ",lastIndex);

        let name = this.whatsappDataServiceService.file.name;
        let type = this.whatsappDataServiceService.file.name.slice(lastIndex + 1);
        let size = this.whatsappDataServiceService.file.size;
        console.log("Name : ",name);
        console.log("Type : ",type);
        console.log("Size : ",size);
  
        if(size >= 12000)
        {
          size = (((size)/1024)/1024).toFixed(2);
          console.log("Size > 12000");
        }
        else
        {
          size = 0.001;
        }

        console.log("Size In MB : ",size);
  
        console.log("Setting data service variables");
        this.whatsappDataServiceService.fileName = name;
        this.whatsappDataServiceService.blobType = type;
        this.whatsappDataServiceService.fileSizeInMB = size;
        this.setMessageTypeAsPerAttachment = true;
  
        console.log("Type of file : "+type);
        if(this.containsAny(type,ConstantsService.whatsAppAudio)){
          console.log("It is audio");
          this.whatsappDataServiceService.messageType = ConstantsService.audio;
        }
        else if(this.containsAny(type,ConstantsService.whatsAppVideo)){
          console.log("It is video");
          this.whatsappDataServiceService.messageType = ConstantsService.video;
        }
        else if(this.containsAny(type,ConstantsService.whatsAppSticker)){
          console.log("It is sticker");
          this.whatsappDataServiceService.messageType = ConstantsService.sticker;
        }
        else if(this.containsAny(type,ConstantsService.whatsAppImage)){
          console.log("It is image");
          this.whatsappDataServiceService.messageType = ConstantsService.image;
        }
        else  if(this.containsAny(type,ConstantsService.whatsAppDocument))
        {
          console.log("It is document");
          this.whatsappDataServiceService.messageType = ConstantsService.document;
        }
        else{
          this.showDialoge('Error','activity-outline','danger', "Only formats allowed are : { \"aac\",\"mp4\",\"mpeg\",\"amr\",\"ogg\",\"mp4\",\"3sp\",\"webp\",\"png\",\"jpeg\",\"plain\",\"pdf\",\"vnd.ms-powerpoint\",\"msword\",\"vnd.ms-excel\",\"vnd.openxmlformats-officedocument.wordprocessingml.document\",\"vnd.openxmlformats-officedocument.presentationml.presentation\",\"vnd.openxmlformats-officedocument.spreadsheetml.sheet\" }");
        }
  
        // Encode the file using the FileReader API
        const reader = new FileReader();
        const now = new Date(); 
        reader.readAsDataURL(this.whatsappDataServiceService.file);
  
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
                //upload whats app media
                const formData = new FormData();
                formData.append('files', this.whatsappDataServiceService.file, this.whatsappDataServiceService.file.name); 
               
               console.log("Form Data created, sending it to whats app store");

                this.fileStorageService.uploadSubscription = this.fileStorageService.uploadUserFilesByOrganizationAndCategory(ConstantsService.whatsAppStore,formData)
                .pipe(takeUntil(this.destroy$))
                .subscribe({
                  next: event => {
                        console.log(event);
                        console.log("reporting progress")
                        this.reportProgress(event);
                  },
                  error: err => {
                  // console.log("Error : "+ JSON.stringify(err));
                    this.showDialoge('Refresh Page','activity-outline','danger', JSON.stringify(err)); 
                  }
                });
            }
            else
            {
              this.showDialoge('Error','activity-outline','danger', "Cannot send null message !!!"); 
            }
  
        };
      }
      catch(e)
      {
        console.log("error :"+e);
          this.whatsappDataServiceService.file = null;
          // this.showDialoge('Error','activity-outline','danger', e); 
      }
  }

   
  containsAny(mainString, searchArray) {
        console.log("****** containsAny***********");
        console.log("main String : "+mainString+" , Does it contain : "+searchArray);
        // The some() method checks if at least one element in the array
        // satisfies the condition provided by the callback function.
        return searchArray.some((element) =>{
          // The includes() method determines whether a string contains
          // another string, returning true or false as appropriate.
          console.log(element);
          return mainString.includes(element);
        });
  }
   
    
  whatsAppSendMessage()
    {
      //Message Type for Whats App are mentioned in constants service for reference.
      console.log("whatsAppSendMessage");

      if(this.setMessageTypeAsPerAttachment)
      {
          //Do nothing its already set
          console.log("this.setMessageTypeAsPerAttachment is true");
      }
      else{
        console.log("this.setMessageTypeAsPerAttachment is false, hence it is text");
          this.whatsappDataServiceService.messageType = ConstantsService.text;
      }
  
      if(this.voiceRecognitionService.setSendMessageId != null)
      {
        clearTimeout(this.voiceRecognitionService.setSendMessageId);
        this.voiceRecognitionService.setSendMessageId = null;
      }
  
      console.log("this.whatsappDataServiceService.messageType : "+this.whatsappDataServiceService.messageType);

      //creating message
      if((!this.setMessageTypeAsPerAttachment) && (this.voiceRecognitionService.messageValue == "" || this.voiceRecognitionService.messageValue == ' '  || this.voiceRecognitionService.messageValue == "   " || this.voiceRecognitionService.messageValue == "    "))
        {
          console.log("Cannot send null message as its emplty and this.setMessageTypeAsPerAttachment is : "+this.setMessageTypeAsPerAttachment);
          this.showDialoge('Error','activity-outline','danger', "Cannot send null message !!!"); 
        }
      else{
                const now = new Date(); 
  
                let messageString = '';
                if(this.setMessageTypeAsPerAttachment)
                {
                    console.log("Setting Message String from blob");
                    messageString = this.whatsappDataServiceService.blobStringText;
                }
                else{
                    console.log("Setting Message String from messageVaue");
                    messageString = this.voiceRecognitionService.messageValue;
                }
  
                console.log("Setting Message Type From Attachment to False");
                this.setMessageTypeAsPerAttachment = false;

                console.log("Sending message to use via Stomp Client associated with Java");
                //sending message directly to user
                this.stompService.sendMessageToWhatsAppPhone(this.whatsappDataServiceService.phoneNumberMain,this.whatsappDataServiceService.participants[this.whatsappDataServiceService.currentSelectedParticipantNumber].phoneNumber,
                  this.whatsappDataServiceService.previousMessageId,messageString,this.whatsappDataServiceService.messageType,
                  this.whatsappDataServiceService.whatsAppMediaId,this.whatsappDataServiceService.blobType,
                  this.whatsappDataServiceService.fileName,this.whatsappDataServiceService.fileSizeInMB);
  
                this.setAfterSendMessageDataBackToNull();
        }
  }

  setAfterSendMessageDataBackToNull(){
              console.log("setAfterSendMessageDataBackToNull");
              this.voiceRecognitionService.messageValue = null;
              this.whatsappDataServiceService.previousMessageId = null;
              this.whatsappDataServiceService.messageType = null;
              this.whatsappDataServiceService.whatsAppMediaId = null;
              this.whatsappDataServiceService.blobType = null;
              this.whatsappDataServiceService.fileName = null;
              this.whatsappDataServiceService.fileSizeInMB = 0;
              this.whatsappDataServiceService.blobStringText = null;
  }
 

  messageParticipantClicked(i:any){
    console.log ("messageParticipantClicked  : ",i);
    if(this.whatsappDataServiceService.sendChatHistoryDataId !=null)
    {
      clearTimeout(this.whatsappDataServiceService.sendChatHistoryDataId);
      //As od now update now is not implemented here
    }

    console.log ("Setting image data null");
    this.base64ImageData=null;
    this.currentByteImageData = null
    console.log ("Setting customer data null");
    this.whatsappDataServiceService.currentCustomerRecord = null;

    console.log ("Changing current participant number");
    this.whatsappDataServiceService.currentSelectedParticipantNumber = i;
    console.log ("Setting current participant badge to zero");
    this.whatsappDataServiceService.participants[this.whatsappDataServiceService.currentSelectedParticipantNumber].badgeText = 0;
    console.log ("Fetch call history");
    this.whatsappDataServiceService.getChatHistory(this.whatsappDataServiceService.participants[this.whatsappDataServiceService.currentSelectedParticipantNumber].phoneNumber);
    console.log ("Fetch customer info");
    this.setCurrentCustomerInformation(this.whatsappDataServiceService.participants[this.whatsappDataServiceService.currentSelectedParticipantNumber].phoneNumber);
    console.log ("Fetch customer image");
    this.setCustomerImageData(this.whatsappDataServiceService.participants[this.whatsappDataServiceService.currentSelectedParticipantNumber].phoneNumber);
    if (this.itsMobile) {
      this.selectButton('Chat'); // or 'Customer', etc.
    }
    setTimeout(() => this.scrollToBottom(), 500);
  }


  openDialer(number: string) {
    console.log("Open Dialer Link set to number : "+number);
    if (this.itsMobile) {
        window.location.href = `tel:${number}`;
      }
  }

 makeACallToParticipant(i){
    console.log("makeACallToParticipant : ",i);
    try{
      if(!this.itsMobile){
          this.dialLineService.audioCall(this.browserPhoneService.userAgent,this.browserPhoneService.lang,this.whatsappDataServiceService.participants[i].phoneNumber,"",this.browserPhoneService.didLength);
     
      }
       else{
          this.openDialer(this.whatsappDataServiceService.participants[i].phoneNumber);
      }
    }
    catch(e)
      {
          console.log(e);
          console.log("Error : "+ JSON.stringify(e));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(e)); 
      }
  }

  clearAllChats()
  {
    console.log("clearallChats");

    if(this.whatsappDataServiceService.clearAllChatHistoryInProgress || this.whatsappDataServiceService.clearSingleChatHistoryInProgress){
        this.showDialoge('Error','activity-outline','danger', "Previous deletion in progress. Kindly wait until it completes."); 
    }
    else{
        this.showDeleteDialoge("All current chats will be deleted ?","allChats"); 
    }
    
  }

  deleteAllChats()
  {
    this.whatsappDataServiceService.clearAllChats();
  }

  clearSingleChat(i:any){
    console.log("clearSingleChats : ",i);
    if(this.whatsappDataServiceService.clearAllChatHistoryInProgress || this.whatsappDataServiceService.clearSingleChatHistoryInProgress){
        this.showDialoge('Error','activity-outline','danger', "Previous deletion in progress. Kindly wait until it completes."); 
    }
    else{
      this.whatsappDataServiceService.singleChatDeleteIndex = i;
      this.showDeleteDialoge("Current chat with "+this.whatsappDataServiceService.participants[this.whatsappDataServiceService.singleChatDeleteIndex].firstName+" will be deleted ?","singleChat");   
    }
  }

  deleteSingleChat()
  {
    this.whatsappDataServiceService.clearSingleChat(this.whatsappDataServiceService.participants[this.whatsappDataServiceService.singleChatDeleteIndex].phoneNumber,this.whatsappDataServiceService.singleChatDeleteIndex);
  }

  showSendFileDialoge(message:string) {
    console.log("showSendFileDialoge");
      this.dialogService.open(AskSendFileComponent, {
        context: {
          message: message
        },
      }).onClose.subscribe((result) => {
        console.log("File transfer dialog is closed");
        if(result === 'yes'){
          console.log("File transfer started");
          this.sendMediaToWhatsApp();
        }
        else {
            // Do nothing
            console.log("File transfer will not start");
        }
      });
  }
  
  showDeleteDialoge(message:string,type:string) {
  console.log("showDeleteDialoge");
          this.dialogService.open(AskDeleteComponent, {
            context: {
              message: message,
              type: type
            },
          }).onClose.subscribe((type) => {
            console.log("Delete dialog is closed");
            console.log("Something of type is delete : ",type);
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

  ngOnDestroy() {

    console.log("ngOnDestroy");
    this.destroy$.next();
    this.destroy$.complete();
    this.whatsappDataServiceService.noDataToShowSoSetToDefault();
    const scrollContainers = document.getElementsByClassName('scrollable-container');
    if (scrollContainers.length > 0) {
      const container = scrollContainers[0] as HTMLElement;
      container.style.overflow = 'auto'; // Restore default scroll
    }
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }

  //Change status of changable area. This function is not used, rather group func updateSingleSelectGroupValue used
  onDisplayChange($event: any) {
    console.log("onDisplayChanges");
    console.log($event)
  }

  updateSingleSelectGroupValue($event: any) {
      console.log('updateSingleSelectGroupValue : ' + $event);
      //Initial value os started selected Action i false.
      console.log('this.startedSelectedActionFirstTime : ' + this.startedSelectedActionFirstTime);
      if(this.startedSelectedActionFirstTime){
        this.startDisplayBlockAnimationStateAnimation('rotateOutY');
        // this.startFixedChatDisplayAnimationStateAnimation('bounce');
        this.whatsappDataServiceService.selectedAction = String($event).trim();
      }else{
        this.startedSelectedActionFirstTime = true;
      }
    }

  startDisplayBlockAnimationStateAnimation(state) {
    console.log("startDisplayBlockAnimationStateAnimation");
    console.log(this.displayBlockAnimationState);
    console.log("themeIsReady : "+this.themeIsReady);

    console.log(state)
    if (!this.displayBlockAnimationState) {
      this.displayBlockAnimationState = state;
    }
  }

  resetDisplayBlockAnimationStateAnimationState() {
    console.log("resetDisplayBlockAnimationStateAnimationState");
    this.displayBlockAnimationState = '';
  }

  //Not in use
  startFixedChatDisplayAnimationStateAnimation(state) {
    console.log("startFixedChatDisplayAnimationStateAnimation");
    console.log(state)
    if (!this.fixedChatDisplayAnimationString) {
      this.fixedChatDisplayAnimationString = state;
    }
  }

  //Not in use
  resetFixedChatDisplayAnimationStateAnimation() {
    console.log("resetFixedChatDisplayAnimationStateAnimation");
    this.fixedChatDisplayAnimationString = '';
  }

  getScreenSize() {

    console.log("getScreenSize");
    this.screenWidth = (window.innerWidth);
    this.screenHeight = (window.innerHeight);

    if(this.screenWidth<550){
      this.buttonGroupSize = "small";
      this.itsMobile = true;
      this.maxNameLength = 16;
    }
    else{
      this.buttonGroupSize = "medium";
      this.maxNameLength = 25;
    }
  }


 private formatDateYYYYMMDD_IST(d: Date): string {
    // Convert to IST without moment libs
    const utc = d.getTime() + (d.getTimezoneOffset() * 60000);
    const ist = new Date(utc + (5.5 * 60 * 60000));

    const yyyy = ist.getFullYear();
    const mm = String(ist.getMonth() + 1).padStart(2, '0');
    const dd = String(ist.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  public downloadChatHistoryExcelDbOnly() {
    console.log("downloadChatHistoryExcelDbOnly clicked");

    try {
      const now = new Date();

      // last 3 days (as you said fixed for now)
      const endDate = this.formatDateYYYYMMDD_IST(now);
      const start = new Date(now.getTime() - (3 * 24 * 60 * 60 * 1000));
      const startDate = this.formatDateYYYYMMDD_IST(start);

      const data = {
        organization: localStorage.getItem("organization"),
        phoneMain: this.whatsappDataServiceService.phoneNumberMain, // keep; remove if you want full org
        startDate: startDate,
        endDate: endDate
      };

      console.log("Excel Download payload:", data);

      let destroy$ = new Subject<void>();

      this.whatsappChatHistoryService.downloadChatHistoryExcelDbOnly(data)
        .pipe(takeUntil(destroy$))
        .subscribe({
          next: (blob: any) => {

            if (blob == null) {
              this.showDialoge('Error','activity-outline','danger', "Excel was empty / null.");
              destroy$.next(); destroy$.complete();
              return;
            }

            const org = String(localStorage.getItem("organization") || "ORG");
            const ts = new Date().getTime();
            const filename = `whatsapp_chat_history_${org}_${startDate}_to_${endDate}_${ts}.xlsx`;

            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = filename;
            a.click();

            window.URL.revokeObjectURL(url);

            destroy$.next();
            destroy$.complete();
            this.cdr.detectChanges();
          },
          error: err => {
            console.log("Error downloading excel:", err);
            this.showDialoge('Error','activity-outline','danger', "Unable to download excel. Kindly try again.");
            destroy$.next();
            destroy$.complete();
          }
        });

    } catch (e) {
      console.log("Exception in downloadChatHistoryExcelDbOnly", e);
      this.showDialoge('Error','activity-outline','danger', "Unable to download excel.");
    }
  }


}