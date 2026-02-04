import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { EmployeeService } from './../service/employee.service';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { LocalDataSource } from 'ng2-smart-table';
import {
  ExportAsService,
  ExportAsConfig,
  SupportedExtensions,
} from 'ngx-export-as';
import { NbToastrService,NbThemeService,NbStepChangeEvent,NbDialogService } from '@nebular/theme';
import { takeWhile } from 'rxjs/operators' ;
import { Router } from '@angular/router';
import { ConstantsService } from './../../../service/constants/constants.service';
import { DialogComponent } from './dialog/dialog.component';
import { AdminResetExtensionPasswordComponent } from './admin-reset-extension-password/admin-reset-extension-password.component';
import { ResetWebPasswordComponent } from './reset-web-password/reset-web-password.component';
import { CampaignService } from '../../campaign/service/campaign.service';
import { NbPopoverDirective, NbPosition, NbTrigger } from '@nebular/theme';
import { PlayAudioComponent } from './play-audio/play-audio.component';
import { CustomInputTableComponent } from './custom-input-table/custom-input-table.component';

interface CardSettings {
  title: string;
  iconClass: string;
  type: string;
}


@Component({
  selector: 'ngx-all-employees',
  templateUrl: './all-employees.component.html',
  styleUrls: ['./all-employees.component.scss']
})
export class AllEmployeesComponent implements OnInit, OnDestroy {
  
  tableHeading = 'All Employees';
  campaignHeading = "Associated Campaigns";

  private destroy$: Subject<void> = new Subject<void>();
  downloadAs: SupportedExtensions = 'xlsx';
  downloadAsCampaign: SupportedExtensions = 'xlsx';
  campaignSource: LocalDataSource = new LocalDataSource();

  @ViewChild(NbPopoverDirective) popover: NbPopoverDirective;
  component: any = PlayAudioComponent;
  trigger = NbTrigger.CLICK;
  //recordingContext: any;


  exportAsConfigCampaign: ExportAsConfig = {
    type: 'xlsx', // the type you want to download
    elementIdOrContent: 'campaignTable', // the id of html/table element
  };

  exportAsConfigEmployee: ExportAsConfig = {
    type: 'xlsx', // the type you want to download
    elementIdOrContent: 'lastTable', // the id of html/table element
  };

  file: File = null;

  
  stepTwoNextButton = true;
  stepThreeNextButton = true;

  // profilePic = 'cool';
  // docOne = 'cool';
  // docTwo = 'cool';
  imageURL = 'youthWithPhone';
  addtionalInfoImageURL = 'addtionalInfo';

  parkedValues:string[] = ['','','','','','','','','',''];
  parkedStatus:string[] = ['danger','danger','danger','danger','danger','danger','danger','danger','danger','danger'];
  allRecordings:any = [];
  
  inputRecordingDate: Date;
  inputRecordingDateSubmitted: Date;

  currentRecord:any = ' ';
  previousRecord:any = ' ';
  allRecords:any = [];

  //Step2
  keys: any = [];
  values: any = [];
  selectedOption;

  //Step3
  inputs: any = [];
  types: any = [];
  inputValues: any = ['','',''];
  organization = '';

  single = [
    {
      name: 'Enabled',
      value: 0,
    },
    {
      name: 'Disabled',
      value: 0,
    },
  ];
  colorScheme: any;
  themeSubscription: any;

  changeEvent: NbStepChangeEvent;
  private alive = true;
  

  view :any = [600, 200];
  gradient: boolean = false;
  on = true;
  solarValue: number;

  callingCard: CardSettings = {
    title: 'Make-A-Call',
    iconClass: 'nb-phone',
    type: 'primary',
  };

  enableDisableCard: CardSettings = {
    title: 'Enable/Disable',
    iconClass: 'nb-locked',
    type: 'primary',
  };
  callOnMobile: CardSettings = {
    title: 'Call On Mobile',
    iconClass: 'nb-layout-default',
    type: 'primary',
  };
  useSecondLine: CardSettings = {
    title: 'Use Second Line',
    iconClass: 'nb-lightbulb',
    type: 'primary',
  };

  recordAllCalls: CardSettings = {
    title: 'Record Calls',
    iconClass: 'nb-snowy-circled',
    type: 'primary',
  };
  intercomPolicy: CardSettings = {
    title: 'Internal Calls',
    iconClass: 'nb-arrow-thin-down',
    type: 'primary',
  };
  freeDial: CardSettings = {
    title: 'Free Dial',
    iconClass: 'nb-paper-plane',
    type: 'primary',
  };
  textDictate: CardSettings = {
    title: 'Text Dictate',
    iconClass: 'nb-title',
    type: 'primary',
  };
  textMessaging: CardSettings = {
    title: 'Text Messaging',
    iconClass: 'nb-compose',
    type: 'primary',
  };
  allowDisableWhatsAppAutoAI: CardSettings = {
    title: 'Controls AI',
    iconClass: 'nb-loop-circled',
    type: 'primary',
  };
  
  /*coffeeMakerCard: CardSettings = {
    title: 'Coffee Maker',
    iconClass: 'nb-coffee-maker',
    type: 'warning',
  };*/

  statusCards: string;

  commonStatusCardsSet: CardSettings[] = [
    this.callingCard,
    this.enableDisableCard,
    this.callOnMobile,
    this.useSecondLine,
    this.recordAllCalls,
    this.intercomPolicy,
    this.freeDial,
    this.textDictate,
    this.textMessaging,
    this.allowDisableWhatsAppAutoAI
   // this.coffeeMakerCard,
  ];

  statusCardsByThemes: {
    default: CardSettings[];
    cosmic: CardSettings[];
    corporate: CardSettings[];
    dark: CardSettings[];
  } = {
    default: this.commonStatusCardsSet,
    cosmic: this.commonStatusCardsSet,
    corporate: [
      {
        ...this.callingCard,
        type: 'warning',
      },
      {
        ...this.enableDisableCard,
        type: 'warning',
      },
      {
        ...this.callOnMobile,
        type: 'warning',
      },
      {
        ...this.useSecondLine,
        type: 'warning',
      },
      {
        ...this.recordAllCalls,
        type: 'warning',
      },
      {
          ...this.intercomPolicy,
          type: 'warning',
      },
      {
          ...this.freeDial,
          type: 'warning',
      },
      {
          ...this.textDictate,
          type: 'warning',
      },
      {
          ...this.textMessaging,
          type: 'warning',
      },
      {
          ...this.allowDisableWhatsAppAutoAI,
          type: 'warning',
      },
      /*{
        ...this.coffeeMakerCard,
        type: 'info',
      },*/
    ],
    dark: this.commonStatusCardsSet,
  };


  redirectDelay: number = 0;
  showDetail : boolean = false;
  showAction : boolean = false;

  settings = {

    actions: {
      add: false,      //  if you want to remove add button
      edit: true,     //  if you want to remove edit button
      delete: false,
    },
    pager: {
      display: true,
      perPage: 5
    },

    add: {
      addButtonContent: '<i class="nb-plus"></i>',
      createButtonContent: '<i class="nb-checkmark"></i>',
      cancelButtonContent: '<i class="nb-close"></i>',
      confirmCreate: true
    },
    edit: {
      editButtonContent: '<i class="nb-edit"></i>',
      saveButtonContent: '<i class="nb-checkmark"></i>',
      cancelButtonContent: '<i class="nb-close"></i>',
      confirmSave: true
    },
    delete: {
      deleteButtonContent: '<i class="nb-trash"></i>',
      confirmDelete: true,
    },


    columns: {
      sizeMediaUploadInMB:{
        title: 'Size (MB)',
        type: 'string',
        hide: true,
      },
      id: {
        title: 'ID',
        type: 'number',
        editable: false,
        addable: false,
      },
      firstName: {
        title: 'First Name',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      lastName: {
        title: 'Last Name',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      sex: {
        title: 'Sex',
        type: 'string',
      },
      phonenumber: {
        title: 'Phone  Number',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      allotednumber1: {
        title: 'Alloted Number 1',
        type: 'string',
        hide: true,
      },
      allotednumber2: {
        title: 'Alloted Number 2',
        type: 'string',
        hide: true,
      },
      costCalculation: {
        title: 'Cost Calculation',
       /* valuePrepareFunction: (value) => { 

         console.log("*******************************************");
         console.log("*******************************************");
         console.log("*******************************************");
         console.log(value);
          
          return value 
        },*/
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          }
         },
         hide: true,
      },
      amount: {
        title: 'Amount',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          }
         },
         hide: true,
      },
      departmentName: {
        title: 'Department Name',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      domain: {
        title: 'Domain',
        type: 'string',
        hide: true,
      },
      secondDomain: {
        title: 'Second Domain',
        type: 'string',
        hide: true,
      },
      email: {
        title: 'Email',
        type: 'string',
      },
      extensionPrefix: {
        title: 'Extension Prefix',
        type: 'string',
        hide: true,
      },
      extension: {
        title: 'Extension',
        type: 'string',
      },
      lastConnectedCustomerPhone: {
        title: 'Last Connected Customer',
        type: 'string',
      },
      confExtensionPrefix: {
        title: 'Conference Prefix',
        type: 'string',
        hide: true,
      },
      confExtension: {
        title: 'Conference Extension',
        type: 'string',
        hide: true,
      },
      extensionpassword: {
        title: 'Extension Password',
        type: 'string',
        hide:true,
      },
      governmentDocument1Data: {
        title: 'Doc One Data',
        type: 'string',
        hide: true,
      },
      governmentDocument2Data: {
        title: 'Doc Two Data',
        type: 'string',
        hide: true,
      },
      governmentDocumentID1: {
        title: 'Doc One ID',
        type: 'string',
        hide: true,
      },
      governmentDocumentID2: {
        title: 'Doc Two ID',
        type: 'string',
        hide: true,
      },
      imageData: {
        title: 'Image Data',
        type: 'string',
        hide: true,
      },
      iconImageData: {
        title: 'Image Icon Data',
        type: 'string',
        hide: true,
      },
      iconImageByteData: {
        title: 'Image Icon Data',
        type: 'string',
        hide: true,
      },
      imageName: {
        title: 'Image Name',
        type: 'string',
        hide: true,
      },
      imageType: {
        title: 'Image Type',
        type: 'string',
        hide: true,
      },
      doc1ImageType: {
        title: 'Doc1 Image Type',
        type: 'string',
        hide: true,
      },
      doc2ImageType: {
        title: 'Doc2 Image Type',
        type: 'string',
        hide: true,
      },
      parkedchannel1: {
        title: 'Parked Channel 1',
        type: 'string',
        hide:true,
      },
      parkedchannel2: {
        title: 'Parked Channel 2',
        type: 'string',
        hide:true,
      },
      parkedchannel3: {
        title: 'Parked Channel 3',
        type: 'string',
        hide:true,
      },
      parkedchannel4: {
        title: 'Parked Channel 4',
        type: 'string',
        hide:true,
      },
      parkedchannel5: {
        title: 'Parked Channel 5',
        type: 'string',
        hide:true,
      },
      parkedchannel6: {
        title: 'Parked Channel 6',
        type: 'string',
        hide:true,
      },
      parkedchannel7: {
        title: 'Parked Channel 7',
        type: 'string',
        hide:true,
      },
      parkedchannel8: {
        title: 'Parked Channel 8',
        type: 'string',
        hide:true,
      },
      parkedchannel9: {
        title: 'Parked Channel 9',
        type: 'string',
        hide:true,
      },
      parkedchannel10: {
        title: 'Parked Channel 10',
        type: 'string',
        hide:true,
      },
      password: {
        title: 'Password',
        type: 'string',
        hide:true,
      },
      pesel: {
        title: 'Pesel',
        type: 'string',
      },
      phoneContext: {
        title: 'Phone Context',
        type: 'string',
        hide: true,
      },
      phoneTrunk: {
        title: 'Phone Trunk',
        type: 'string',
        hide: true,
      },
      protocol: {
        title: 'Protocol',
        type: 'string',
        hide: true,
      },
      provider1: {
        title: 'Provider 1',
        type: 'string',
        hide: true,
      },
      provider2: {
        title: 'Provider 2',
        type: 'string',
        hide: true,
      },
      role: {
        title: 'Role',
        type: 'string',
      },
      timezone: {
        title: 'Timezone',
        type: 'string',
      },
      transfer_phone_1: {
        title: 'Manager Extension',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      transfer_phone_2: {
        title: 'Head Extension',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      type: {
        title: 'Employee type',
        type: 'string',
      },
      departmentId: {
        title: 'Department ID',
        type: 'number',
        hide:true
      },
      salary: {
        title: 'Salary',
        type: 'number',
      },
      totalparkedchannels: {
        title: 'Total Parked Channels',
        type: 'number',
        hide:true,
      },
      birthdate: {
        title: 'Birth Date',
        type: 'date',
      },
      isEnabled: {
        title: 'Is Active',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide:true,
      },
      useSecondaryAllotedLine: {
        title: 'Use Second Line',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide:true,
      },
      callonnumber: {
        title: 'Call On Mobile',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide:true,
      },
      isLocked: {
        title: 'Is Locked',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
      },
      recordAllCalls: {
        title: 'Record Calls',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide:true,
      },
      intercomPolicy: {
        title: 'Intercom Policy',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide:true,
      },
      freeDialOption: {
        title: 'Free Dial',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide:true,
      },
      textDictateOption: {
        title: 'Text Dictation',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide:true,
      },
      textMessagingOption: {
        title: 'Text Messaging',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide:true,
      },
      organization: {
        title: 'Organization',
        type: 'string',
        hide:true,
      },
      uiTheme: {
        title: 'UI Theme',
        type: 'string',
        hide:true,
      },
      autoAnswer: {
        title: 'Auto Answer',
        type: 'string',
        hide:true,
      },
      autoConference: {
        title: 'Auto Conference',
        type: 'string',
        hide:true,
      },
      autoVideo: {
        title: 'Auto Video',
        type: 'string',
        hide:true,
      },
      micDevice: {
        title: 'Mic Device',
        type: 'string',
        hide:true,
      },
      speakerDevice: {
        title: 'Speaker Device',
        type: 'string',
        hide:true,
      },
      videoDevice: {
        title: 'Video Device',
        type: 'string',
        hide:true,
      },
      videoOrientation: {
        title: 'Video Orientation',
        type: 'string',
        hide:true,
      },
      videoQuality: {
        title: 'Video Quality',
        type: 'string',
        hide:true,
      },
      videoFrameRate: {
        title: 'Video Frame Rate',
        type: 'string',
        hide:true,
      },
      autoGainControl: {
        title: 'Auto Gain Control',
        type: 'string',
        hide:true,
      },
      echoCancellation: {
        title: 'Echo Cancellation',
        type: 'string',
        hide:true,
      },
      noiseSupression: {
        title: 'Noise Supression',
        type: 'string',
        hide:true,
      },
      sipPort: {
        title: 'Sip Port',
        type: 'string',
        hide:true,
      },
      sipPath: {
        title: 'Sip Path',
        type: 'string',
        hide:true,
      },
      doNotDisturb: {
        title: 'Do Not Disturb',
        type: 'string',
        hide:true,
      },
      startVideoFullScreen: {
        title: 'Start Video Full Screen',
        type: 'string',
        hide:true,
      },
      callWaiting: {
        title: 'Start Video Full Screen',
        type: 'string',
        hide:true,
      },
      notificationDot: {
        title: 'Start Video Full Screen',
        type: 'string',
        hide:true,
      },
      allowedToSwitchOffWhatsAppAI: {
        title: 'allowedToSwitchOffWhatsAppAI',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide:true,
      },
    },
  };


  campaignSettings = {
    actions: {
      add: false,      //  if you want to remove add button
      edit: false,     //  if you want to remove edit button
      delete: false //  if you want to remove delete button
    },

    pager: {
      display: true,
      perPage: 5
    },

    add: {
      addButtonContent: '<i class="nb-plus"></i>',
      createButtonContent: '<i class="nb-checkmark"></i>',
      cancelButtonContent: '<i class="nb-close"></i>',
      confirmCreate: true
    },
    edit: {
      editButtonContent: '<i class="nb-edit"></i>',
      saveButtonContent: '<i class="nb-checkmark"></i>',
      cancelButtonContent: '<i class="nb-close"></i>',
      confirmSave: true
    },
    delete: {
      deleteButtonContent: '<i class="nb-trash"></i>',
      confirmDelete: true,
    },


    columns: {
      id: {
        title: 'Record ID',
        type: 'number',
        editable: false,
        addable: false,
      },
      employeeid: {
        title: 'Employee ID',
        type:'string',
        hide:true
      },
      campaignid: {
        title: 'Campaign ID',
        type:'string'
      },
      campaignName: {
        title: 'Campaign Name',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      firstName: {
        title: 'Name',
        type: 'string',
        editable: false,
        addable: false,
        hide:true
      },
      phonenumber: {
        title: 'Phone',
        type: 'string',
        editable: false,
        addable: false,
        hide:true
      },
      email: {
        title: 'Email',
        type: 'string',
        editable: false,
        addable: false,
        hide:true
      },
      lastConnectedCustomerPhone: {
        title: 'Last Connected Customer',
        type: 'string',
        hide:true
      },
      lastCustomerNumber: {
        title: '# Connected Calls ',
        type: 'string',
        editable: false,
        hide:true
      },
      organization: {
        title: 'Organization',
        type: 'string',
        hide:true,
      },
    }
  };

  radioOptions = [
    { value: '1-Column Search', label: '1-Property Search', checked: true },
    { value: '2-Column Search', label: '2-Property Search', disabled: true  },
    { value: '3-Column Search', label: '3-Property Search', disabled: true },
  ];

  radioOption;

  allDropDownOptions = [
    { column: 1, label: 'Email', properties:['email']},
    { column: 1, label: 'Phone Number', properties:['phonenumber']},
    { column: 1, label: 'Extension', properties:['extension']},
    { column: 1, label: 'User Role', properties:['role']},
    { column: 1, label: 'Sex', properties:['sex']},
    { column: 1, label: 'Phone Contetxt', properties:['phoneContext']},
    { column: 1, label: 'Is Active', properties:['isEnabled']},
    { column: 1, label: 'Cost Calculation', properties:['costCalculation']},
  ];

  dropDownOptions = [
    { column: 1, label: 'Email', properties:['email']},
    { column: 1, label: 'Phone Number', properties:['phonenumber']},
    { column: 1, label: 'Extension', properties:['extension']},
    { column: 1, label: 'User Role', properties:['role']},
    { column: 1, label: 'Sex', properties:['sex']},
    { column: 1, label: 'Phone Contetxt', properties:['phoneContext']},
    { column: 1, label: 'Is Active', properties:['isEnabled']},
    { column: 1, label: 'Cost Calculation', properties:['costCalculation']},
  ];
  currentDropDownOption: any;
  dropDownOption;

  source: LocalDataSource = new LocalDataSource();
  currentByteImageData:any=[];
  base64ImageData:any = [];
  whatsAppSupportlink: string;

  constructor(private employeeService : EmployeeService,
              private exportAsService: ExportAsService,
              private themeService: NbThemeService,
              private nbToastrService:NbToastrService,
              protected router: Router,
              protected constantService : ConstantsService,
              private dialogService: NbDialogService,
              private campaignService: CampaignService) {

               // console.log("I am in constructor");

               this.whatsAppSupportlink = ConstantsService.whatsAppSupportlink;
               
                if(localStorage.getItem("organization")!=null)
                 {
                  this.organization = localStorage.getItem("organization");
                 }
                 else{
                  
                  setTimeout(() => {
                    //   console.log('Routing to dashboard page');
                       return this.router.navigateByUrl(this.constantService.LOGIN_ENDPOINT);
                     }, this.redirectDelay);
                }
                
                this.themeService.getJsTheme()
                .pipe(takeWhile(() => this.alive))
                .subscribe(theme => {
                  this.statusCards = this.statusCardsByThemes[theme.name];
                  const colors: any = theme.variables;
                  this.colorScheme = {
                  domain: [colors.primaryLight, colors.infoLight, colors.successLight, colors.warningLight, colors.dangerLight],
                };
              });
            }

 
  setTableDropDownValues()
  {
    //console.log("setTableDropDownValues");
    this.employeeService.getAllCostCalcultationType(this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {
        
        //console.log(JSON.stringify(allData));
        //this.allCostCalculation = [... JSON.parse(JSON.stringify(allData))];
        var list= [];
        var singleObject = {value: '', title:''};
        if(allData == null)
        {
          //console.log("all data is null");
          list= [];
        }
        else{
          var arr = JSON.parse(JSON.stringify(allData));
          arr.forEach((element) => {
           // console.log(JSON.stringify(element));
            singleObject.value = element.replace('"','');
            singleObject.title = element.replace('"','');
            list.push( JSON.parse(JSON.stringify(singleObject)));
          });
        }

       
        this.settings.columns.costCalculation.editor.config.list = [... list];
        this.settings = Object.assign({}, this.settings);

      },
      error: err => {
       console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });

    this.callValuePrepareFunction();

  }


  callValuePrepareFunction ()
  {
    // console.log("valuePrepareFunction");
     //console.log(cell);
     //console.log(row);
 
     
     this.employeeService.getAllMeteredPlanAmount(this.organization)
     .pipe(takeUntil(this.destroy$))
     .subscribe({
       next: allData => {
        // console.log("getAllEmployeesByOrganization");
         //console.log(JSON.stringify(allData));
         //this.allCostCalculation = [... JSON.parse(JSON.stringify(allData))];
         var list= [];
 
         var singleObject = {value: '', title:''};
         if(allData == null)
         {
           //console.log("all data is null");
           list= [];
         }
         else{
           var arr = JSON.parse(JSON.stringify(allData));
           arr.forEach((element) => {
             //console.log(JSON.stringify(element));
             singleObject.value = element;
             singleObject.title = element;
             list.push( JSON.parse(JSON.stringify(singleObject)));
           });
         }
 
        
         this.settings.columns.amount.editor.config.list = [... list];
         this.settings = Object.assign({}, this.settings);
 
       },
       error: err => {
        console.log("Error : "+ JSON.stringify(err));
        //  this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
       }
     });
     
     this.employeeService.getAllUnlimitedPlanAmount(this.organization)
     .pipe(takeUntil(this.destroy$))
     .subscribe({
       next: allData => {
        // console.log("getAllEmployeesByOrganization");
         //console.log(JSON.stringify(allData));
         //this.allCostCalculation = [... JSON.parse(JSON.stringify(allData))];
         var list=  this.settings.columns.amount.editor.config.list;
         var singleObject = {value: '', title:''};
         if(allData == null)
         {
           //console.log("all data is null");
           list= [];
         }
         else{
           var arr = JSON.parse(JSON.stringify(allData));
           arr.forEach((element) => {
             //console.log(JSON.stringify(element));
             singleObject.value = element;
             singleObject.title = element;
             list.push( JSON.parse(JSON.stringify(singleObject)));
           });
         }
 
        
         this.settings.columns.amount.editor.config.list = [... list];
         this.settings = Object.assign({}, this.settings);
 
       },
       error: err => {
        console.log("Error : "+ JSON.stringify(err));
        //  this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
       }
     });
 
 
  }

  
  setTable()
  {

   // console.log("setTable");
    this.employeeService.getAllEmployeesByOrganization(this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {

        if(allData == null)
        {
          //console.log("I am null data");
          //console.log("Inside Data : "+ JSON.stringify(allData));
            this.source.load([]); 
                   
            this.allRecords = [];

            var output = [
              {
                name: 'Enabled',
                value: 0,
              },
              {
                name: 'Disabled',
                value: 0,
              },
            ];

            //console.log("After http");    
            this.single=[...output];
        }
        else
        {
            //console.log("Inside Data : "+ JSON.stringify(allData));
              this.source.load(<any[]>allData); 
                   
              var arr = JSON.parse(JSON.stringify(allData));
              //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
              this.allRecords = [...arr];

              var output = [
                {
                  name: 'Enabled',
                  value: 0,
                },
                {
                  name: 'Disabled',
                  value: 0,
                },
              ];

              arr.forEach((element) => {

                //console.log("Element"+ JSON.stringify(element));
          
                if (element.isEnabled == true) {
                  //console.log("Its Enabled");
                  output[0].value = output[0].value +1;
                }
                else{
                  //console.log("Its Disabled");
                  output[1].value = output[1].value +1;
                }
              });
              
              //console.log("After http");    
              this.single=[...output];
        }
      },
      error: err => {
       console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }
  
  onFilechange(event: any) {
   // console.log("onFilechange");
    //console.log(event);
    //console.log(event.target.files[0].name.endsWith(".xlsx"));
    //console.log(event.target.files[0].name.endsWith(".xls"));

    if(event.target.files[0].name.endsWith(".xlsm") || event.target.files[0].name.endsWith(".xlsx") || event.target.files[0].name.endsWith(".xls")) 
    {
      this.file = event.target.files[0];
      
      let formParams = new FormData();
      formParams.append('file', event.target.files[0])

      this.showDialoge('Success','done-all-outline','success', `Upload process is started. It will take some time.`);
      

      event.target.value = "";

      this.employeeService.upload(formParams,this.organization)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
          // console.log("Result : "+ JSON.stringify(result));
           this.setTable();
          },
          error: err => {
          console.log("Error : "+ JSON.stringify(err));
            // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
            event.target.value = "";
          }
        });
    }
    else
    {
        this.file = null;
        this.showDialoge('Error','activity-outline','danger', `Only excel can be uploaded`); 
        event.target.value = "";
    }
    
  }

  
  onDeleteConfirm(event): void {
    // console.log("User Deleted a row. Row data is  : ");
    //console.log(event);
    
    this.previousRecord =  this.currentRecord;
    this.currentRecord = JSON.parse(JSON.stringify(event.data));

    this.showDetail = false;
    this.showAction = false;
    this.showDialoge('Error','activity-outline','danger', "This application does not allow to delete employess. Please connect with mylinehub support."); 
    

  }

  onSaveConfirm(event): void {

    //console.log("User Edited a row. Row data is  : ");
    //console.log(event);
    //console.log(event);

    this.previousRecord =  JSON.parse(JSON.stringify(event.data));
    this.currentRecord = JSON.parse(JSON.stringify(event.newData));

    // this.profilePic = "data:image/png;base64,"+ this.previousRecord.imageData;
    this.currentRecord.imageData = this.previousRecord.imageData;

    // this.docOne = "data:image/png;base64,"+ this.previousRecord.governmentDocument1Data;
    this.currentRecord.governmentDocument1Data = this.previousRecord.governmentDocument1Data;

    // this.docTwo = "data:image/png;base64,"+ this.previousRecord.governmentDocument2Data;
    this.currentRecord.governmentDocument2Data = this.previousRecord.governmentDocument2Data;

    this.setCampaignTable();
    
    var columns = JSON.parse(JSON.stringify(this.settings.columns));
    var keys = [];
    Object.keys(this.currentRecord).map(function (key) { //console.log(key);
      //console.log(key);
      //console.log(columns[key].title);
      keys.push(columns[key].title);

    });
    this.keys = keys;
    this.values = Object.values(this.currentRecord);

    this.showDetail = true;
    this.showAction = true;


    if (window.confirm('Are you sure you want to edit?')) {
    
     // console.log("Starting Update API");
      this.employeeService.updateEmployeeByOrganization(JSON.stringify(this.currentRecord),this.organization,this.previousRecord.email)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {

        //console.log("API returned result");

        if(String(result) == 'true')
        {
            // console.log("Result is true");
             event.confirm.resolve();
             var output = JSON.parse(JSON.stringify(this.single));


             if(JSON.stringify(this.currentRecord.isEnabled) == JSON.stringify(this.previousRecord.isEnabled)){   
              //console.log("Both are same");
            }
            else{
              if(JSON.stringify(this.currentRecord.isEnabled) == "\"true\""||JSON.stringify(this.currentRecord.isEnabled) == "true")
              {
                //console.log("current record is true");
                output[0].value = output[0].value + 1;
                output[1].value = output[1].value - 1;
              }
              else if(JSON.stringify(this.currentRecord.isEnabled) == "\"false\""||JSON.stringify(this.currentRecord.isEnabled) == "false")
                {
                  //console.log("current record is false");
                  output[0].value = output[0].value - 1;
                  output[1].value = output[1].value + 1;
                
              }
              else
              {
                //console.log("I am something else");
                //console.log(this.currentRecord.isEnabled);
              }

            this.single = [...output];

            //console.log("Moving out of update");
              }
            }
        else{
          //console.log("Result is not true");
          event.confirm.reject();
        }
      },
      error: err => {
       console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        event.confirm.reject();
      }
    });
    } 
    else {
      //console.log("User rejected delete");
      event.confirm.reject();
    }
  }

  onCreateConfirm(event): void {
    //console.log("User Created a row. Row data is  : ");
    //console.log(event);

    this.currentRecord = JSON.parse(JSON.stringify(event.newData));
    this.currentRecord.organization = this.organization;
    
    this.setCampaignTable();
    var columns = JSON.parse(JSON.stringify(this.settings.columns));
    var keys = [];
    Object.keys(this.currentRecord).map(function (key) { //console.log(key);
      //console.log(key);
      //console.log(columns[key].title);
      keys.push(columns[key].title);

    });
    this.keys = keys;
    this.values = Object.values(this.currentRecord);

    this.showDetail = false;
    this.showAction = false;

    if (window.confirm('Are you sure you want to create?')) {

      //console.log("Starting Create API");
   
      this.employeeService.createEmployeeByOrganization(JSON.stringify(this.currentRecord),this.organization)
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
   
           //console.log("API returned result");
   
           if(String(result) == 'true')
           {
                //console.log("Result is true");
                this.setTable();
           }
           else{
             //console.log("Result is not true");
             this.showDialoge('Error','activity-outline','danger', "You should create records for your organization. Record not created."); 
             event.confirm.reject();
           }
         },
         error: err => {
           console.log("Error : "+ JSON.stringify(err));
          //  this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
           event.confirm.reject();
         }
       });
       } 
       else {
         //console.log("User rejected delete");
         event.confirm.reject();
       }
  }

  search()
  {
    if(ConstantsService.user.role === ConstantsService.employee)
      {
        this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
      }
    else
      {
        this.onSearch();
      }
  }
  
  onSearch()
  {
    this.showAction = false;
    this.showDetail = false;

   //console.log("On Search is clicked");

    if(this.currentDropDownOption.label == 'Email')
    {
    // console.log("Ami User Search Started");
      this.employeeService.getEmployeeByEmailAndOrganization(this.inputValues[0],this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
  
          if(allData == null)
          {
            //console.log("I am null data");
            this.source.load([]); 
                   
            this.allRecords = [];

            var output = [
              {
                name: 'Enabled',
                value: 0,
              },
              {
                name: 'Disabled',
                value: 0,
              },
            ];

            //console.log("After http");    
            this.single=[...output];
          }
          else
          {
            //console.log("Inside Data : "+ +"["+JSON.stringify(allData)+"]");
            this.source.load(<any[]>JSON.parse("["+JSON.stringify(allData)+"]")); 
            var arr = JSON.parse("["+JSON.stringify(allData)+"]");
            //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
            this.allRecords = [...arr];
  
            var output = [
              {
                name: 'Enabled',
                value: 0,
              },
              {
                name: 'Disabled',
                value: 0,
              },
            ];
    
            arr.forEach((element) => {
    
            // console.log("Element"+ JSON.stringify(element));
        
              if (element.isEnabled == true) {
                //console.log("Its Enabled");
                output[0].value = output[0].value +1;
              }
              else{
                //console.log("Its Disabled");
                output[1].value = output[1].value +1;
              }
            });
            
           // console.log("After http");    
            this.single=[...output];
          }
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }
    else if(this.currentDropDownOption.label == 'Phone Number')
    {
    // console.log("Ami User Search Started");
      this.employeeService.getEmployeeByPhonenumberAndOrganization(this.inputValues[0],this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
  
          if(allData == null)
          {
            //console.log("I am null data");
            this.source.load([]); 
                   
            this.allRecords = [];

            var output = [
              {
                name: 'Enabled',
                value: 0,
              },
              {
                name: 'Disabled',
                value: 0,
              },
            ];

            //console.log("After http");    
            this.single=[...output];
          }
          else
          {
            //console.log("Inside Data : "+ +"["+JSON.stringify(allData)+"]");
            this.source.load(<any[]>JSON.parse("["+JSON.stringify(allData)+"]")); 
            var arr = JSON.parse("["+JSON.stringify(allData)+"]");
            //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
            this.allRecords = [...arr];
  
            var output = [
              {
                name: 'Enabled',
                value: 0,
              },
              {
                name: 'Disabled',
                value: 0,
              },
            ];
    
            arr.forEach((element) => {
    
            // console.log("Element"+ JSON.stringify(element));
        
              if (element.isEnabled == true) {
                //console.log("Its Enabled");
                output[0].value = output[0].value +1;
              }
              else{
                //console.log("Its Disabled");
                output[1].value = output[1].value +1;
              }
            });
            
           // console.log("After http");    
            this.single=[...output];
          }
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }

    else if(this.currentDropDownOption.label == 'Extension')
    {
    // console.log("Ami User Search Started");
      this.employeeService.getEmployeeByExtensionAndOrganization(this.inputValues[0],this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
  
          if(allData == null)
          {
            //console.log("I am null data");
            this.source.load([]); 
                   
            this.allRecords = [];

            var output = [
              {
                name: 'Enabled',
                value: 0,
              },
              {
                name: 'Disabled',
                value: 0,
              },
            ];

            //console.log("After http");    
            this.single=[...output];
          }
          else
          {
            //console.log("Inside Data : "+ +"["+JSON.stringify(allData)+"]");
            this.source.load(<any[]>JSON.parse("["+JSON.stringify(allData)+"]")); 
            var arr = JSON.parse("["+JSON.stringify(allData)+"]");
            //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
            this.allRecords = [...arr];
  
            var output = [
              {
                name: 'Enabled',
                value: 0,
              },
              {
                name: 'Disabled',
                value: 0,
              },
            ];
    
            arr.forEach((element) => {
    
            // console.log("Element"+ JSON.stringify(element));
        
              if (element.isEnabled == true) {
                //console.log("Its Enabled");
                output[0].value = output[0].value +1;
              }
              else{
                //console.log("Its Disabled");
                output[1].value = output[1].value +1;
              }
            });
            
           // console.log("After http");    
            this.single=[...output];
          }
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }

    else if(this.currentDropDownOption.label == 'User Role')
    {
      //console.log("Phone Context Search Started");
      this.employeeService.getAllEmployeesOnUserRoleAndOrganization(this.inputValues[0],this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
  
          if(allData == null)
          {
           // console.log("I am null data");
            this.source.load([]); 
                   
            this.allRecords = [];

            var output = [
              {
                name: 'Enabled',
                value: 0,
              },
              {
                name: 'Disabled',
                value: 0,
              },
            ];

            //console.log("After http");    
            this.single=[...output];
          }
          else
          {
              //console.log("Inside Data : "+ JSON.stringify(allData));
          this.source.load(<any[]>allData); 
                   
          var arr = JSON.parse(JSON.stringify(allData));
          //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
          this.allRecords = [...arr];
          var output = [
            {
              name: 'Enabled',
              value: 0,
            },
            {
              name: 'Disabled',
              value: 0,
            },
          ];
  
          arr.forEach((element) => {
  
            //console.log("Element"+ JSON.stringify(element));
      
            if (element.isEnabled == true) {
              //console.log("Its Enabled");
              output[0].value = output[0].value +1;
            }
            else{
              //console.log("Its Disabled");
              output[1].value = output[1].value +1;
            }
          });
          
          //console.log("After http");    
          this.single=[...output];
          }
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }

    else if(this.currentDropDownOption.label == 'Sex')
    {
      //console.log("Phone Context Search Started");
      this.employeeService.getAllEmployeesOnSexAndOrganization(this.inputValues[0],this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
  
          if(allData == null)
          {
           // console.log("I am null data");
            this.source.load([]); 
                   
            this.allRecords = [];

            var output = [
              {
                name: 'Enabled',
                value: 0,
              },
              {
                name: 'Disabled',
                value: 0,
              },
            ];

            //console.log("After http");    
            this.single=[...output];
          }
          else
          {
              //console.log("Inside Data : "+ JSON.stringify(allData));
          this.source.load(<any[]>allData); 
                   
          var arr = JSON.parse(JSON.stringify(allData));
          //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
          this.allRecords = [...arr];
          var output = [
            {
              name: 'Enabled',
              value: 0,
            },
            {
              name: 'Disabled',
              value: 0,
            },
          ];
  
          arr.forEach((element) => {
  
            //console.log("Element"+ JSON.stringify(element));
      
            if (element.isEnabled == true) {
              //console.log("Its Enabled");
              output[0].value = output[0].value +1;
            }
            else{
              //console.log("Its Disabled");
              output[1].value = output[1].value +1;
            }
          });
          
          //console.log("After http");    
          this.single=[...output];
          }
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }
    else if(this.currentDropDownOption.label == 'Phone Contetxt')
    {
      //console.log("Phone Context Search Started");
      this.employeeService.getAllEmployeesOnPhoneContextAndOrganization(this.inputValues[0],this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
  
          if(allData == null)
          {
           // console.log("I am null data");
            this.source.load([]); 
                   
            this.allRecords = [];

            var output = [
              {
                name: 'Enabled',
                value: 0,
              },
              {
                name: 'Disabled',
                value: 0,
              },
            ];

            //console.log("After http");    
            this.single=[...output];
          }
          else
          {
              //console.log("Inside Data : "+ JSON.stringify(allData));
          this.source.load(<any[]>allData); 
                   
          var arr = JSON.parse(JSON.stringify(allData));
          //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
          this.allRecords = [...arr];
          var output = [
            {
              name: 'Enabled',
              value: 0,
            },
            {
              name: 'Disabled',
              value: 0,
            },
          ];
  
          arr.forEach((element) => {
  
            //console.log("Element"+ JSON.stringify(element));
      
            if (element.isEnabled == true) {
              //console.log("Its Enabled");
              output[0].value = output[0].value +1;
            }
            else{
              //console.log("Its Disabled");
              output[1].value = output[1].value +1;
            }
          });
          
          //console.log("After http");    
          this.single=[...output];
          }
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }

    else if(this.currentDropDownOption.label == 'Is Active')
    {
      //console.log("Phone Context Search Started");
      this.employeeService.getAllEmployeesOnIsEnabledAndOrganization(this.inputValues[0],this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
  
          if(allData == null)
          {
           // console.log("I am null data");
            this.source.load([]); 
                   
            this.allRecords = [];

            var output = [
              {
                name: 'Enabled',
                value: 0,
              },
              {
                name: 'Disabled',
                value: 0,
              },
            ];

            //console.log("After http");    
            this.single=[...output];
          }
          else
          {
              //console.log("Inside Data : "+ JSON.stringify(allData));
          this.source.load(<any[]>allData); 
                   
          var arr = JSON.parse(JSON.stringify(allData));
          //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
          this.allRecords = [...arr];
          var output = [
            {
              name: 'Enabled',
              value: 0,
            },
            {
              name: 'Disabled',
              value: 0,
            },
          ];
  
          arr.forEach((element) => {
  
            //console.log("Element"+ JSON.stringify(element));
      
            if (element.isEnabled == true) {
              //console.log("Its Enabled");
              output[0].value = output[0].value +1;
            }
            else{
              //console.log("Its Disabled");
              output[1].value = output[1].value +1;
            }
          });
          
          //console.log("After http");    
          this.single=[...output];
          }
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }

    else if(this.currentDropDownOption.label == 'Cost Calculation')
    {
      //console.log("Phone Context Search Started");
      this.employeeService.findAllBycostCalculationAndOrganization(this.inputValues[0],this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
  
          if(allData == null)
          {
           // console.log("I am null data");
            this.source.load([]); 
                   
            this.allRecords = [];

            var output = [
              {
                name: 'Enabled',
                value: 0,
              },
              {
                name: 'Disabled',
                value: 0,
              },
            ];

            //console.log("After http");    
            this.single=[...output];
          }
          else
          {
              //console.log("Inside Data : "+ JSON.stringify(allData));
          this.source.load(<any[]>allData); 
                   
          var arr = JSON.parse(JSON.stringify(allData));
          //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
          this.allRecords = [...arr];
          var output = [
            {
              name: 'Enabled',
              value: 0,
            },
            {
              name: 'Disabled',
              value: 0,
            },
          ];
  
          arr.forEach((element) => {
  
            //console.log("Element"+ JSON.stringify(element));
      
            if (element.isEnabled == true) {
              //console.log("Its Enabled");
              output[0].value = output[0].value +1;
            }
            else{
              //console.log("Its Disabled");
              output[1].value = output[1].value +1;
            }
          });
          
          //console.log("After http");    
          this.single=[...output];
          }
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }

  }
  
  getChildData(event){

    if(event == 'Record Calls')
    {
      let newRecords: any = [];

      this.allRecords.forEach( (element) => {
        //console.log("Element");
        //console.log(element);
        if(element.id == this.currentRecord.id)
        {
          newRecords.push(this.currentRecord);
        }
        else{
          newRecords.push(element);
        }
      });

      this.allRecords = [...newRecords];
      this.source.load(<any[]>this.allRecords);


      //Update all keys and Values
      let i : number = 0;
      Object.keys(this.currentRecord).forEach((element) => { //console.log(key);
        if(element == "recordAllCalls")
        {
          this.values[i] = this.currentRecord.recordAllCalls;
        }
        i = i +1;
      });

    }

    if(event == 'Internal Calls')
    {
      let newRecords: any = [];

      this.allRecords.forEach( (element) => {
        //console.log("Element");
        //console.log(element);
        if(element.id == this.currentRecord.id)
        {
          newRecords.push(this.currentRecord);
        }
        else{
          newRecords.push(element);
        }
      });

      this.allRecords = [...newRecords];
      this.source.load(<any[]>this.allRecords);


      //Update all keys and Values
      let i : number = 0;
      Object.keys(this.currentRecord).forEach((element) => { //console.log(key);
        if(element == "intercomPolicy")
        {
          this.values[i] = this.currentRecord.intercomPolicy;
        }
        i = i +1;
      });
      
    }

    if(event == 'Free Dial')
    {
      let newRecords: any = [];

      this.allRecords.forEach( (element) => {
        //console.log("Element");
        //console.log(element);
        if(element.id == this.currentRecord.id)
        {
          newRecords.push(this.currentRecord);
        }
        else{
          newRecords.push(element);
        }
      });

      this.allRecords = [...newRecords];
      this.source.load(<any[]>this.allRecords);


      //Update all keys and Values
      let i : number = 0;
      Object.keys(this.currentRecord).forEach((element) => { //console.log(key);
        if(element == "freeDialOption")
        {
          this.values[i] = this.currentRecord.freeDialOption;
        }
        i = i +1;
      });
      
    }

    if(event == 'Text Dictate')
    {
      let newRecords: any = [];

      this.allRecords.forEach( (element) => {
        //console.log("Element");
        //console.log(element);
        if(element.id == this.currentRecord.id)
        {
          newRecords.push(this.currentRecord);
        }
        else{
          newRecords.push(element);
        }
      });

      this.allRecords = [...newRecords];
      this.source.load(<any[]>this.allRecords);


      //Update all keys and Values
      let i : number = 0;
      Object.keys(this.currentRecord).forEach((element) => { //console.log(key);
        if(element == "textDictateOption")
        {
          this.values[i] = this.currentRecord.textDictateOption;
        }
        i = i +1;
      });
      
    }

    if(event == 'Text Messaging')
    {
      let newRecords: any = [];

      this.allRecords.forEach( (element) => {
        //console.log("Element");
        //console.log(element);
        if(element.id == this.currentRecord.id)
        {
          newRecords.push(this.currentRecord);
        }
        else{
          newRecords.push(element);
        }
      });

      this.allRecords = [...newRecords];
      this.source.load(<any[]>this.allRecords);


      //Update all keys and Values
      let i : number = 0;
      Object.keys(this.currentRecord).forEach((element) => { //console.log(key);
        if(element == "textMessagingOption")
        {
          this.values[i] = this.currentRecord.textMessagingOption;
        }
        i = i +1;
      });
      
    }


    if(event == 'Controls AI')
    {
      let newRecords: any = [];

      this.allRecords.forEach( (element) => {
        //console.log("Element");
        //console.log(element);
        if(element.id == this.currentRecord.id)
        {
          newRecords.push(this.currentRecord);
        }
        else{
          newRecords.push(element);
        }
      });

      this.allRecords = [...newRecords];
      this.source.load(<any[]>this.allRecords);


      //Update all keys and Values
      let i : number = 0;
      Object.keys(this.currentRecord).forEach((element) => { //console.log(key);
        if(element == "allowDisableWhatsAppAutoAI")
        {
          this.values[i] = this.currentRecord.allowDisableWhatsAppAutoAI;
        }
        i = i +1;
      });
      
    }

    

    if(event == 'Enable/Disable')
    {
      //console.log("this.currentRecord");
        //console.log(JSON.stringify(this.currentRecord));
        //console.log("all records");
        //console.log(JSON.stringify(this.allRecords));
        var newRecords: any = [];

        this.allRecords.forEach( (element) => {
          //console.log("Element");
          //console.log(element);
          if(element.id == this.currentRecord.id)
          {
            newRecords.push(this.currentRecord);
          }
          else{
            newRecords.push(element);
          }
        });

        this.allRecords = [...newRecords];
        this.source.load(<any[]>this.allRecords);


        //Update all keys and Values
        var i : number = 0;
        Object.keys(this.currentRecord).forEach((element) => { //console.log(key);
          if(element == "isEnabled")
          {
            this.values[i] = this.currentRecord.isEnabled;
          }
          i = i +1;
        });

       

        //Update upper chart
        var output = JSON.parse(JSON.stringify(this.single));
        if(JSON.stringify(this.currentRecord.isEnabled) == "\"true\""||JSON.stringify(this.currentRecord.isEnabled) == "true")
                     {
                       //console.log("current record is true");
                       output[0].value = output[0].value + 1;
                       output[1].value = output[1].value - 1;
                     }
                     else if(JSON.stringify(this.currentRecord.isEnabled) == "\"false\""||JSON.stringify(this.currentRecord.isEnabled) == "false")
                       {
                        // console.log("current record is false");
                         output[0].value = output[0].value - 1;
                         output[1].value = output[1].value + 1;
                       
                     }
                     else
                     {
                      // console.log("I am something else");
                       //console.log(this.currentRecord.isEnabled);
                     }
       
                   this.single = [...output];
                   
        //this.setTable();
    }


    if(event == 'Call On Mobile')
    {
      //console.log("this.currentRecord");
        //console.log(JSON.stringify(this.currentRecord));
        //console.log("all records");
        //console.log(JSON.stringify(this.allRecords));
        var newRecords: any = [];

        this.allRecords.forEach( (element) => {
          //console.log("Element");
          //console.log(element);
          if(element.id == this.currentRecord.id)
          {
            newRecords.push(this.currentRecord);
          }
          else{
            newRecords.push(element);
          }
        });

        this.allRecords = [...newRecords];
        this.source.load(<any[]>this.allRecords);


        //Update all keys and Values
        var i : number = 0;
        Object.keys(this.currentRecord).forEach((element) => { //console.log(key);
          if(element == "callonnumber")
          {
            this.values[i] = this.currentRecord.callonnumber;
          }
          i = i +1;
        });
    }

    if(event == 'Use Second Line')
    {
      //console.log("this.currentRecord");
        //console.log(JSON.stringify(this.currentRecord));
        //console.log("all records");
        //console.log(JSON.stringify(this.allRecords));
        var newRecords: any = [];

        this.allRecords.forEach( (element) => {
          //console.log("Element");
          //console.log(element);
          if(element.id == this.currentRecord.id)
          {
            newRecords.push(this.currentRecord);
          }
          else{
            newRecords.push(element);
          }
        });

        this.allRecords = [...newRecords];
        this.source.load(<any[]>this.allRecords);


        //Update all keys and Values
        var i : number = 0;
        Object.keys(this.currentRecord).forEach((element) => { //console.log(key);
          if(element == "useSecondaryAllotedLine")
          {
            this.values[i] = this.currentRecord.useSecondaryAllotedLine;
          }
          i = i +1;
        });
    }
  }

  onUserRowSelect(event): void {
    // console.log("User Selected a row. Row data is  : ");
    // console.log(event);
    this.currentRecord = JSON.parse(JSON.stringify(event.data));
    this.setCampaignTable();
    var columns = JSON.parse(JSON.stringify(this.settings.columns));
    var keys = [];
    Object.keys(this.currentRecord).map(function (key) { //console.log(key);
      // console.log(key);
      // console.log(columns[key].title);
      keys.push(columns[key].title);

    });
    this.keys = keys;
    this.values = Object.values(this.currentRecord);
    this.showDetail = true;
    this.showAction = true;

    this.organization = localStorage.getItem("organization");
    this.employeeService.getEmployeeByEmailAndOrganization(this.currentRecord.email,localStorage.getItem("organization"))
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
          this.currentRecord = data;
          // this.profilePic = "data:image/png;base64,"+ JSON.parse(JSON.stringify(data)).imageData;
          // this.docOne = "data:image/png;base64,"+ JSON.parse(JSON.stringify(data)).governmentDocument1Data;
          // this.docTwo = "data:image/png;base64,"+ JSON.parse(JSON.stringify(data)).governmentDocument2Data;
          this.setImageData();
          this.parkedValues[0] = JSON.parse(JSON.stringify(data)).parkedchannel1;
          this.parkedValues[1] = JSON.parse(JSON.stringify(data)).parkedchannel2;
          this.parkedValues[2] = JSON.parse(JSON.stringify(data)).parkedchannel3;
          this.parkedValues[3] = JSON.parse(JSON.stringify(data)).parkedchannel4;
          this.parkedValues[4] = JSON.parse(JSON.stringify(data)).parkedchannel5;
          this.parkedValues[5] = JSON.parse(JSON.stringify(data)).parkedchannel6;
          this.parkedValues[6] = JSON.parse(JSON.stringify(data)).parkedchannel7;
          this.parkedValues[7] = JSON.parse(JSON.stringify(data)).parkedchannel8;
          this.parkedValues[8] = JSON.parse(JSON.stringify(data)).parkedchannel9;
          this.parkedValues[9] = JSON.parse(JSON.stringify(data)).parkedchannel10;

          if(String(JSON.parse(JSON.stringify(data)).parkedchannel1) != 'null'){
            this.parkedStatus[0] = "success"
          }
          else
          {
            this.parkedValues[0] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel2) != 'null')
          {
            this.parkedStatus[1] = "success"
          }
          else
          {
            this.parkedValues[1] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel3) != 'null')
          {
            this.parkedStatus[2] = "success"
          }
          else
          {
            this.parkedValues[2] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel4) != 'null')
          {
            this.parkedStatus[3] = "success"
          }
          else
          {
            this.parkedValues[3] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel5) != 'null')
          {
            this.parkedStatus[4] = "success"
          }
          else
          {
            this.parkedValues[4] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel6) != 'null')
          {
            this.parkedStatus[5] = "success"
          }
          else
          {
            this.parkedValues[5] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel7) != 'null')
          {
            this.parkedStatus[6] = "success"
          }
          else
          {
            this.parkedValues[6] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel8) != 'null')
          {
            this.parkedStatus[7] = "success"
          }
          else
          {
            this.parkedValues[7] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel9) != 'null')
          {
            this.parkedStatus[8] = "success"
          }
          else
          {
            this.parkedValues[8] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel10) != 'null')
          {
            this.parkedStatus[9] = "success"
          }
          else
          {
            this.parkedValues[9] = 'No value';
          }

        }
      },
      error: err => {
      console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });


  }

  
  export() {
  this.exportAsConfigEmployee.type = this.downloadAs;
    this.exportAsService
      .save(this.exportAsConfigEmployee, 'Employees')
      .subscribe(() => {
        // save started
      });
  }


  handleStepChange(e: NbStepChangeEvent): void {
    //console.log('Handle event');
    this.changeEvent = e;
    if(e.index == 2)
    {
      var currentSearch = this.allDropDownOptions.find( record => record.label === this.currentDropDownOption.label);
      var currentProperties = currentSearch.properties;
      var columns = JSON.parse(JSON.stringify(this.settings.columns));
      var inputs = [];
      var types = [];
      var count =0;
      //console.log(currentSearch)
      //console.log(currentProperties)
      //console.log(columns)

      currentProperties.forEach( (element) => {
        inputs.push(columns[element].title);


        if(columns[element].hasOwnProperty("editor") && (columns[element].editor.type == 'list' && columns[element].editor.config.list[0].hasOwnProperty("value") && columns[element].editor.config.list[0].value == 'true'))
        {
          types.push('boolean');
          this.inputValues[count] = true;
        }
        else if(columns[element].hasOwnProperty("editor") && columns[element].editor.type == 'list'){
            types.push('string');
            this.inputValues[count] = '';
        }
        else{
          types.push(columns[element].type);
          if(columns[element].type == 'boolean' || (columns[element].type == 'list' && columns[element].config.list[0].value == 'true'))
            {
              this.inputValues[count] = true;
            }
            else{
              this.inputValues[count] = '';
            }
        }
        count = count+ 1;
     });

     this.inputs = inputs;
     this.types = types;

     for (let i = 0; i < this.inputs.length; i++) {

      if(this.types[i] == 'boolean')
      {
        //console.log('I am boolean');
      }
      else{
        if(this.inputValues[i] == '' || this.inputValues[i] == ' ' || this.inputValues[i] == null)
        {
          this.stepThreeNextButton = true;
          break;
        }
      }

      this.stepThreeNextButton = false;
     
    }
   }
   if(e.index == 3)
   {
    //console.log(JSON.stringify(this.inputValues));
   }
  }

  bulkUpload(){
    //console.log("Bulk Upload AMI");
    const val = document.getElementById('file-input');

   /* val.addEventListener("change", function() {

    });*/

    val.click();
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

  /*showToast(status: NbComponentStatus, message:string) {
    const toastRef: NbToastRef =  this.nbToastrService.show(status,message, { status });
   // toastRef.close();
  }*/

  onRadioChange(event)
  {
    this.selectedOption = '';
    this.stepTwoNextButton = true;
    var allValues: any = this.allDropDownOptions;
    var requiredValues: any = [];

    if(JSON.stringify(event) == "\"1-Column Search\"")
    {
      allValues.forEach( (element) => {
        if(element.column == 1)
        {
          requiredValues.push(element);
        }
      });
      this.dropDownOptions = requiredValues;
    }
    else if(JSON.stringify(event) == "\"2-Column Search\""){
      allValues.forEach( (element) => {
        if(element.column == 2)
        {
          requiredValues.push(element);
        }
      });
      this.dropDownOptions = requiredValues;
    }
    else if(JSON.stringify(event) == "\"3-Column Search\""){
      allValues.forEach( (element) => {
        if(element.column == 3)
        {
          requiredValues.push(element);
        }
      });
      this.dropDownOptions = requiredValues;
    }

  }

  dropDownChange(event)
  {
    //console.log("Drop Down Button changed");
    this.currentDropDownOption = this.dropDownOptions[event];
    this.stepTwoNextButton = false;
  }

 stepInputChange(event)
  {
    //console.log('stepInputChange');
    for (let i = 0; i < this.inputs.length; i++) {
      //console.log(this.inputValues[i]);

      if(this.types[i] == 'boolean')
      {

      }
      else{
        if(this.inputValues[i] == '' || this.inputValues[i] == ' ' || this.inputValues[i] == null)
        {
         // console.log('break');
          this.stepThreeNextButton = true;
          break;
        }  
      }
     
     // console.log('passed');
      this.stepThreeNextButton = false;
    }
  }

  valueChanged(event){
    //console.log('date value changed');
    var track = 0;

    for (let i = 0; i < this.inputs.length; i++) {

      if(this.types[i] == 'boolean')
      {
       // console.log('valueChanged : boolean');
      }
      else{
        if(this.inputValues[i] == '' || this.inputValues[i] == ' ' || this.inputValues[i] == null)
        {
          if(track==0)
          { // Do Nothing
           // console.log('track0');
          }
          else{
            track = 1;
            this.stepThreeNextButton = true;
            break;
          } 
        }
      }
      this.stepThreeNextButton = false;
    }
  }

  ngOnInit(){
   //console.log("I am in ngOnIt");
  //  console.log('ConstantsService.user' + ConstantsService.user);
  //  console.log('ConstantsService.user.role' + ConstantsService.user.role);
  //  console.log('ConstantsService.user.employee' + ConstantsService.employee);

  if(ConstantsService.user.firstName !== undefined){
               console.log("Loading empoloyee data");
               this.setupNgOnInitData();
          }
      else{
                setTimeout(() => {
                  console.log("Delay employee load due to unavailability of data");
                  this.setupNgOnInitData();
                }, ConstantsService.DIRECT_REFRESH_PAGE_TIME_MS); // 2000 milliseconds = 2 seconds
          }
  }

  setupNgOnInitData(){

   if(ConstantsService.user.role === ConstantsService.employee)
        {
          this.settings.actions.add = false;
          this.settings.actions.edit = false;
          this.settings.actions.delete = false;
          document.getElementById("bulkUploadButton").hidden = true;
          document.getElementById("downloadMainTableButton").hidden = true;
          this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
        }
   else
        {
          this.setTable();  
          this.setTableDropDownValues();     
        }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }

  showExtensionResetPasswordDialoge() {

    this.dialogService.open(AdminResetExtensionPasswordComponent, {
      context: {
        currentRecord: this.currentRecord,
      },
    });
    }


  showWebResetPasswordDialoge() {

      this.dialogService.open(ResetWebPasswordComponent, {
        context: {
          currentRecord: this.currentRecord,
        },
      });
      }

  updateExtensionPassword()
  {
    //console.log("updateExtensionPassword");
    this.showExtensionResetPasswordDialoge(); 
  }

  updateWebPassword()
  {
    //console.log("updateExtensionPassword");
    this.showWebResetPasswordDialoge(); 
  }

  onProfilePicUpload(event)
  {
    // console.log("onProfilePicUpload");
    let lastIndex = event.target.files[0].name.lastIndexOf('.');
    let name = event.target.files[0].name.slice(0, lastIndex);
    let type = event.target.files[0].name.slice(lastIndex + 1);
    let size = event.target.files[0].size;

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

    if(size < 520) 
    {
      if(event.target.files[0].name.endsWith(".png") ||event.target.files[0].name.endsWith(".PNG") || event.target.files[0].name.endsWith(".JPG") ||event.target.files[0].name.endsWith(".jpg") || event.target.files[0].name.endsWith(".jpeg")|| event.target.files[0].name.endsWith(".JPG") ||event.target.files[0].name.endsWith(".jpg") || event.target.files[0].name.endsWith(".JPEG")) 
        {
    
          this.file = event.target.files[0];
          
          let formParams = new FormData();
          formParams.append('image', event.target.files[0])
    
          this.employeeService.uploadProfilePicByEmailAndOrganization(formParams,this.organization,this.currentRecord.email)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: result => {
              if(String(result) == 'true')
                  {
                       // this.currentRecord.imageData;
                       event.target.value = "";
                      this.showDialoge('Success','done-all-outline','success', `Picture upload process is successful.`);
                      // event.confirm.resolve();
                       this.setImageData();
                      }
                  else{
                    //console.log("Result is not true");
                   // event.confirm.reject();
                   this.showDialoge('Unsuccess','done-all-outline','danger', `Picture upload process is unsuccessful.`);
                  }
                  event.target.value = "";
            },
            error: err => {
            console.log("Error : "+ JSON.stringify(err));
              // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
              event.target.value = "";
            }
          });
        }
        else
        {
          //console.log("error");
            this.file = null;
            this.showDialoge('Error','activity-outline','danger', `PNG/JPEG/JPG are supported formats.`); 
            event.target.value = "";
        }    
    }
    else
    {
        //console.log("error");
        this.showDialoge('Error','activity-outline','danger', `Max size supported is 500KB.`); 
        event.target.value = "";
    }
  }

  onDocOneUpload(event)
  {
      let lastIndex = event.target.files[0].name.lastIndexOf('.');
      let name = event.target.files[0].name.slice(0, lastIndex);
      let type = event.target.files[0].name.slice(lastIndex + 1);
      let size = event.target.files[0].size;

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

   if(size < 520) 
    {
        // console.log("onProductPicturechange");
     if(event.target.files[0].name.endsWith(".png") ||event.target.files[0].name.endsWith(".PNG") || event.target.files[0].name.endsWith(".JPG") ||event.target.files[0].name.endsWith(".jpg") || event.target.files[0].name.endsWith(".jpeg")|| event.target.files[0].name.endsWith(".JPG") ||event.target.files[0].name.endsWith(".jpg") || event.target.files[0].name.endsWith(".JPEG")) 
      {
   
        this.file = event.target.files[0];
        
        let formParams = new FormData();
        formParams.append('image', event.target.files[0])
  
        this.employeeService.uploadDocOneByEmailAndOrganization(formParams,this.organization,this.currentRecord.email)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
                {
                     // this.currentRecord.imageData;
                     event.target.value = "";
                    this.showDialoge('Success','done-all-outline','success', `Doc-1 upload process is successful.`);
                    // event.confirm.resolve();
                    this.setImageData();
                    }
                else{
                  //console.log("Result is not true");
                 // event.confirm.reject();
                 this.showDialoge('Unsuccess','done-all-outline','danger', `Doc-1 upload process is unsuccessful.`);
                }
                event.target.value = "";
          },
          error: err => {
          console.log("Error : "+ JSON.stringify(err));
            // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
            event.target.value = "";
          }
        });
      }
      else
      {
        //console.log("error");
          this.file = null;
          this.showDialoge('Error','activity-outline','danger', `PNG/JPEG/JPG are supported formats.`); 
          event.target.value = "";
      } 
    }
    else
    {
        //console.log("error");
        this.showDialoge('Error','activity-outline','danger', `Max size supported is 500KB.`); 
        event.target.value = "";
    }
  }

  onDocTwoUpload(event)
  {
      let lastIndex = event.target.files[0].name.lastIndexOf('.');
      let name = event.target.files[0].name.slice(0, lastIndex);
      let type = event.target.files[0].name.slice(lastIndex + 1);
      let size = event.target.files[0].size;

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
   if(size < 520) 
    {
          // console.log("onProductPicturechange");
     if(event.target.files[0].name.endsWith(".png") ||event.target.files[0].name.endsWith(".PNG") || event.target.files[0].name.endsWith(".JPG") ||event.target.files[0].name.endsWith(".jpg") || event.target.files[0].name.endsWith(".jpeg")|| event.target.files[0].name.endsWith(".JPG") ||event.target.files[0].name.endsWith(".jpg") || event.target.files[0].name.endsWith(".JPEG")) 
      { 
  
        this.file = event.target.files[0];
        
        let formParams = new FormData();
        formParams.append('image', event.target.files[0])
  
        this.employeeService.uploadDocTwoByEmailAndOrganization(formParams,this.organization,this.currentRecord.email)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
                {
                     // this.currentRecord.imageData;
                     event.target.value = "";
                    this.showDialoge('Success','done-all-outline','success', `Doc-2 upload process is successful.`);
                    this.setImageData();
                   // event.confirm.resolve();
                    }
                else{
                  //console.log("Result is not true");
                 // event.confirm.reject();
                 this.showDialoge('Unsuccess','done-all-outline','danger', `Doc-2 upload process is unsuccessful.`);
                }
                event.target.value = "";
          },
          error: err => {
          console.log("Error : "+ JSON.stringify(err));
            // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
            event.target.value = "";
          }
        });
      }
      else
      {
        //console.log("error");
          this.file = null;
          this.showDialoge('Error','activity-outline','danger', `PNG/JPEG/JPG are supported formats.`); 
          event.target.value = "";
      }
    }
    else
    {
        //console.log("error");
        this.showDialoge('Error','activity-outline','danger', `Max size supported is 500KB.`); 
        event.target.value = "";
    }
  }

  onProfilePicButton()
  {
    const val = document.getElementById('onProfilePicUpload');
    val.click();
  }

  onDocOneButton()
  {
    const val = document.getElementById('onDocOneUpload');
    val.click();
  }

  onDocTwoButton()
  {
    const val = document.getElementById('onDocTwoUpload');
    val.click();
  }
  campaignExport() {
    this.exportAsConfigCampaign.type = this.downloadAsCampaign;
    this.exportAsService
      .save(this.exportAsConfigCampaign, 'Campaigns_To_Employee')
      .subscribe(() => {
        // save started
      });
  }

  setCampaignTable()
  {
    this.campaignService.findAllByEmployeeAndOrganization(this.currentRecord.extension,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {

        if(allData == null)
        {
          //console.log("I am null data");
          //console.log("Inside Data : "+ JSON.stringify(allData));
            this.campaignSource.load([]);   
        }
        else
        {
            //console.log("Inside Data : "+ JSON.stringify(allData));
              this.campaignSource.load(<any[]>allData);   
        }
      },
      error: err => {
       console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  setImageData()
  {
    this.employeeService.getEmployeeImages(this.currentRecord.email,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {

        if(allData == null)
        {
          //console.log("I am null data");
          //console.log("Inside Data : "+ JSON.stringify(allData));
            this.currentByteImageData = []; 
            this.base64ImageData = [];
        }
        else
        {
            //console.log("Inside Data : "+ JSON.stringify(allData));
            this.currentByteImageData = allData;    
            this.base64ImageData = [];
            this.currentByteImageData.forEach((current:any,i:number)=>{
              
              try{
                    if(current != null)
                    {
                      try{
                          //  console.log("this.currentByteImageData is not null");
                          //  console.log("current");
                          //  console.log(current);
                           if(i == 0)
                           {
                            let url = 'data:image/'+this.currentRecord.imageType+';base64,'+current;
                            this.base64ImageData.push(url);
                           }
    
                           if(i == 1)
                           {
                            let url = 'data:image/'+this.currentRecord.imageType+';base64,'+current;
                            this.base64ImageData.push(url);
                           }
    
                           if(i == 2)
                           {
                            let url = 'data:image/'+this.currentRecord.imageType+';base64,'+current;
                            this.base64ImageData.push(url);
                           }
                           
                           this.base64ImageData[i] = this.base64ImageData[i].replace("/image","");
                      }  
                      catch(e)
                      {
                        // console.log(e);
                      }
                    }  
                    
                  }
              catch(e)
              {
                  if((this.base64ImageData.length-1)<i)
                  {
                    this.base64ImageData.push(null);
                  }   
              }
            });
        }
      },
      error: err => {
       // console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  getAllRecordingForDay()
  {
    //console.log("getAllRecordingForDay");
    // Test data 11-07-2023
    const year = this.inputRecordingDate.getFullYear();
    const month = this.inputRecordingDate.getMonth() + 1;
    const day = this.inputRecordingDate.getDate();
    this.inputRecordingDateSubmitted = this.inputRecordingDate;

   // console.log(year);
    //console.log(month);
    //console.log(day);
    
    this.employeeService.getAllRecordingDataForEmployee({year:year,month:month,day:day,domain:this.currentRecord.domain,extension:this.currentRecord.extension,organization:this.organization,fileName:null})
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {
        
        var list= [];
 
         var singleObject =  {title: '', icon:'play-circle-outline',audioSource:''};
         if(allData == null)
         {
           //console.log("all data is null");
           list= [];
         }
         else{
          // console.log("All Data : "+ JSON.stringify(allData));

           var arr = JSON.parse(JSON.stringify(allData));
           arr.forEach((element) => {
             //console.log(JSON.stringify(element));
             singleObject.title = element.filename;
             list.push( JSON.parse(JSON.stringify(singleObject)));
           });

           this.allRecordings = [... list];

         }
            

      },
      error: err => {
      console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
   
  }
}


function getBase64(file) {

  //console.log("inside base 64");
  return new Promise((resolve, reject) => {
    //console.log("creating promise");
    const reader = new FileReader();
    //console.log("reading file");
    reader.readAsDataURL(file);
   // console.log("on load if resolved");
    reader.onload = () => resolve(reader.result);

   // console.log("reject on error");
    reader.onerror = error => reject(error);
  });
}