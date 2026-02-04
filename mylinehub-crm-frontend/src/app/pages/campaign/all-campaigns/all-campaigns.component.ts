import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CampaignService } from './../service/campaign.service';
import { takeUntil } from 'rxjs/operators';
import { Observable, Subject, of } from 'rxjs';
import { LocalDataSource } from 'ng2-smart-table';
import { EmployeeService } from './../../employee/service/employee.service';
import { IvrService } from './../../ivr/service/ivr.service';
import { ConferenceService } from './../../conference/service/conference.service';
import { QueueService } from './../../queue/service/queue.service';
import {
  ExportAsService,
  ExportAsConfig,
  SupportedExtensions,
} from 'ngx-export-as';
import { NbThemeService,NbStepChangeEvent, NbDialogService } from '@nebular/theme';
import { takeWhile } from 'rxjs/operators' ;
import { Router } from '@angular/router';
import { ConstantsService } from './../../../service/constants/constants.service';
import { SubmitBulkCustomerComponent } from './submit-bulk-customer/submit-bulk-customer.component';
import { SubmitBulkEmployeeComponent } from './submit-bulk-employee/submit-bulk-employee.component';
import { AskDeleteComponent } from '../ask-delete/ask-delete.component';
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';
import { CustomInputTableComponent } from '../../employee/all-employees/custom-input-table/custom-input-table.component';

interface CardSettings {
  title: string;
  iconClass: string;
  type: string;
}


@Component({
  selector: 'ngx-all-campaigns',
  templateUrl: './all-campaigns.component.html',
  styleUrls: ['./all-campaigns.component.scss']
})
export class AllCampaignsComponent implements OnInit, OnDestroy {
  
  @ViewChild('autoPageNumber') pageNumberInput;
  @ViewChild('searchName') searchTextInput;

  pageOptionsAll = [1,2,3,4,5,6,7,8,9,10,11,12,13];
  pageOptions$: Observable<number[]>;
  currentCustomerToCampaignPageSize:number = 20;

  searchCustomerToCampaignString:any = "";
  customerToCampaignTotalPages:any = 1;
  customerToCampaignTotalRecords:any = 3;

  tableHeading = 'All Campaigns';
  isAlreadyScheduledStart = 'Already Scheduled (Overwrite it)';
  isAlreadyScheduledStop = 'Already Scheduled (Overwrite it)';
  customerHeading = 'Customers To This Campaign';
  employeeHeading = 'Employees To This Campaign';


  private destroy$: Subject<void> = new Subject<void>();
  downloadAs: SupportedExtensions = 'png';
  downloadAsCustomer: SupportedExtensions = 'png';
  downloadAsEmployee: SupportedExtensions = 'png';

  exportAsConfigCampaign: ExportAsConfig = {
    type: 'xlsx', // the type you want to download
    elementIdOrContent: 'lastTable', // the id of html/table element
  };

  exportAsConfigCustomer: ExportAsConfig = {
    type: 'xlsx', // the type you want to download
    elementIdOrContent: 'customerTable', // the id of html/table element
  };

  exportAsConfigEmployee: ExportAsConfig = {
    type: 'xlsx', // the type you want to download
    elementIdOrContent: 'employeeTable', // the id of html/table element
  };
  file: File = null;

  
  stepTwoNextButton = true;
  stepThreeNextButton = true;

  currentRecord:any = ' ';
  previousRecord:any = ' ';
  allRecords:any = [];
  allCustomerToCampaign:any = [];
  allEmployeeToCampaign:any = [];
  allEmployees:any = [];
  allQueues:any = [];
  allConference:any = [];
  allIVR:any = [];

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

  removeStartScheduledCard: CardSettings = {
    title: 'Remove Job',
    iconClass: 'nb-close',
    type: 'primary',
  };

  scheduleStartAFixedDateCard: CardSettings = {
    title: 'At Fix Date',
    iconClass: 'nb-locked',
    type: 'primary',
  };

  scheduleStartAfterNSecCard: CardSettings = {
    title: 'After N Seconds',
    iconClass: 'nb-maximize',
    type: 'primary',
  };

  scheduleStartCronCard: CardSettings = {
    title: 'Cron',
    iconClass: 'nb-loop-circled',
    type: 'primary',
  };


  removeStopScheduledCard: CardSettings = {
    title: 'Remove Job',
    iconClass: 'nb-close',
    type: 'primary',
  };

  scheduleStopAFixedDateCard: CardSettings = {
    title: 'At Fix Date',
    iconClass: 'nb-locked',
    type: 'primary',
  };

  scheduleStopAfterNSecCard: CardSettings = {
    title: 'After N Seconds',
    iconClass: 'nb-maximize',
    type: 'primary',
  };

  scheduleStopCronCard: CardSettings = {
    title: 'Cron',
    iconClass: 'nb-loop-circled',
    type: 'primary',
  };

  startStopCard: CardSettings = {
    title: 'Start/Stop',
    iconClass: 'nb-lightbulb',
    type: 'primary',
  };
  pauseCard: CardSettings = {
    title: 'Pause',
    iconClass: 'nb-pause',
    type: 'primary',
  };
  unpauseCard: CardSettings = {
    title: 'Unpause',
    iconClass: 'nb-play',
    type: 'primary',
  };
  resetCard: CardSettings = {
    title: 'Reset',
    iconClass: 'nb-roller-shades',
    type: 'info',
  };
  
  /*coffeeMakerCard: CardSettings = {
    title: 'Coffee Maker',
    iconClass: 'nb-coffee-maker',
    type: 'warning',
  };*/

  statusCards: string;
  scheduleStartCards: string;
  scheduleStopCards: string;
  scheduleTypeStart: string;
  scheduleTypeStop: string;
  allScheduleTypeStart:string[]= [];
  allScheduleTypeStop:string[]= [];

  commonStatusCardsSet: CardSettings[] = [
    this.startStopCard,
    this.pauseCard,
    this.unpauseCard,
    this.resetCard,
   // this.coffeeMakerCard,
  ];

  scheduleStatusCardsSetToStart: CardSettings[] = [
    this.removeStartScheduledCard,
    this.scheduleStartAfterNSecCard,
    this.scheduleStartAFixedDateCard,
    this.scheduleStartCronCard,
   // this.coffeeMakerCard,
  ];

  scheduleStatusCardsSetToStop: CardSettings[] = [
    this.removeStopScheduledCard,
    this.scheduleStopAfterNSecCard,
    this.scheduleStopAFixedDateCard,
    this.scheduleStopCronCard,
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
        ...this.startStopCard,
        type: 'warning',
      },
      {
        ...this.pauseCard,
        type: 'primary',
      },
      {
        ...this.unpauseCard,
        type: 'primary',
      },
      {
        ...this.resetCard,
        type: 'danger',
      },
      /*{
        ...this.coffeeMakerCard,
        type: 'info',
      },*/
    ],
    dark: this.commonStatusCardsSet,
  };


  scheduleStartCardsByThemes: {
    default: CardSettings[];
    cosmic: CardSettings[];
    corporate: CardSettings[];
    dark: CardSettings[];
  } = {
    default: this.scheduleStatusCardsSetToStart,
    cosmic: this.scheduleStatusCardsSetToStart,
    corporate: [
      {
        ...this.removeStartScheduledCard,
        type: 'warning',
      },
      {
        ...this.scheduleStartAFixedDateCard,
        type: 'primary',
      },
      {
        ...this.scheduleStartAfterNSecCard,
        type: 'primary',
      },
      {
        ...this.scheduleStartCronCard,
        type: 'danger',
      },
      /*{
        ...this.coffeeMakerCard,
        type: 'info',
      },*/
    ],
    dark: this.scheduleStatusCardsSetToStart,
  };


  scheduleStopCardsByThemes: {
    default: CardSettings[];
    cosmic: CardSettings[];
    corporate: CardSettings[];
    dark: CardSettings[];
  } = {
    default: this.scheduleStatusCardsSetToStop,
    cosmic: this.scheduleStatusCardsSetToStop,
    corporate: [
      {
        ...this.removeStopScheduledCard,
        type: 'warning',
      },
      {
        ...this.scheduleStopAFixedDateCard,
        type: 'primary',
      },
      {
        ...this.scheduleStopAfterNSecCard,
        type: 'primary',
      },
      {
        ...this.scheduleStopCronCard,
        type: 'danger',
      },
      /*{
        ...this.coffeeMakerCard,
        type: 'info',
      },*/
    ],
    dark: this.scheduleStatusCardsSetToStop,
  };

  redirectDelay: number = 0;
  showDetail : boolean = false;
  showAction : boolean = false;

  settings = {

    actions: {
      add: true,      //  if you want to remove add button
      edit: true,     //  if you want to remove edit button
      delete: true,
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
        title: 'ID',
        type: 'number',
        editable: false,
        addable: false,
      },
      name: {
        title: 'Name',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      autodialertype: {
        title: 'Campaign Type',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      managerId: {
        title: 'Manager ID',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      phonecontext: {
        title: 'Campaign Context',
        type: 'string',
      },
      breathingSeconds: {
        title: 'Breathing Seconds',
        type: 'number',
      },
      aiApplicationName: {
        title: 'AI Application',
        type: 'string'
      },
      aiApplicationDomain: {
        title: 'Domain',
        type: 'string',
        hide:true,
      },
      confExtension: {
        title: 'Conference Extension',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      ivrExtension: {
        title: 'IVR Extension',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      whatsAppNumber: {
        title: 'Whatsapp Number',
        type: 'string'
      },
      template: {
        title: 'Template',
        type: 'string'
      },
      queueExtension: {
        title: 'Queue Extension',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      isonmobile: {
        title: 'Call On Mobile',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
      },
      startdate: {
        title: 'Start Date',
        type: 'date',
        hide:true,
      },
      enddate: {
        title: 'End Date',
        type: 'date',
        hide:true,
      },
      cronremindercalling: {
        title: 'Reminder Calling Cron',
        type: 'string',
      },
      domain: {
        title: 'Domain',
        type: 'string',
        hide:true,
      },
      lastCustomerNumber: {
        title: 'All Customer Called',
        type: 'number',
        editable:false,
        hide:true,
      },
      totalCallsMade: {
        title: 'Customer Picked Called',
        type: 'number',
        editable:false,
        hide:true,
      },
      callLimit: {
        title: 'Call Limit',
        type: 'number',

      },
      parallelLines: {
        title: 'Parallel Lines',
        type: 'number',
      },
      business: {
        title: 'Business',
        type: 'string',
        hide:true,
      },
      country: {
        title: 'Country',
        type: 'string',
        hide:true,
      },
      description: {
        title: 'Description',
        type: 'string',
        hide:true,
      },
      remindercalling: {
        title: 'Reminder Calling',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
      },
      organization: {
        title: 'Organization',
        type: 'string',
        hide:true,
      },
      isactive: {
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
      isenabled: {
        title: 'Is Enabled',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
      },
      callCost: {
        title: 'Cost',
        type: 'string',
        hide:true,
      },
      callCostMode: {
        title: 'Cost Mode',
        type: 'string',
        hide:true,
      }
    },
  };




  customerSettings = {

    hideSubHeader: true,
    pager: { display: false },

    actions: {
      add: false,      //  if you want to remove add button
      edit: false,     //  if you want to remove edit button
      delete: true,
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
      campaignid: {
        title: 'Campaign ID',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      customerid: {
        title: 'Customer ID',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      firstname: {
        title: 'Name',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
        editable: false,
        addable: false,
      },
      phoneNumber: {
        title: 'Phone',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
        editable: false,
        addable: false,
      },
      email: {
        title: 'Email',
        type: 'string',
        editable: false,
        addable: false,
      },
      organization: {
        title: 'Organization',
        type: 'string',
        hide: true,
      },
      isCalledOnce: {
        title: 'Called Once',
        type: 'string',
      },
      lastConnectedExtension: {
        title: 'Last Connected Extension',
        type: 'string',
      },
    }
  };

  employeeSettings = {

    actions: {
      add: false,      //  if you want to remove add button
      edit: false,     //  if you want to remove edit button
      delete: true,
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
      campaignid: {
        title: 'Campaign ID',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      employeeid: {
        title: 'Employee ID',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      firstName: {
        title: 'Name',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
        editable: false,
        addable: false,
      },
      phonenumber: {
        title: 'Phone',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
        editable: false,
        addable: false,
      },
      email: {
        title: 'Email',
        type: 'string',
        editable: false,
        addable: false,
      },
      lastConnectedCustomerPhone: {
        title: 'Last Connected Customer',
        type: 'string',
      },
      lastCustomerNumber: {
        title: '# Connected Calls ',
        type: 'string',
        editable: false,
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
    { column: 1, label: 'Name', properties:['name']},
    { column: 1, label: 'Phone Context', properties:['phonecontext']},
    { column: 1, label: 'Call On Mobile', properties:['isonmobile']},
    { column: 1, label: 'Country', properties:['country']},
    { column: 1, label: 'Business', properties:['business']},
    { column: 1, label: 'AutoDialer type', properties:['autodialertype']},
    { column: 1, label: 'Manager ID', properties:['managerId']},
    { column: 1, label: 'Start Date Greator', properties:['startdate']},
  ];

  dropDownOptions = [
    { column: 1, label: 'Name', properties:['name']},
    { column: 1, label: 'Phone Context', properties:['phonecontext']},
    { column: 1, label: 'Call On Mobile', properties:['isonmobile']},
    { column: 1, label: 'Country', properties:['country']},
    { column: 1, label: 'Business', properties:['business']},
    { column: 1, label: 'AutoDialer type', properties:['autodialertype']},
    { column: 1, label: 'Manager ID', properties:['managerId']},
    { column: 1, label: 'Start Date Greator', properties:['startdate']},
  ];
  currentDropDownOption: any;
  dropDownOption;

  source: LocalDataSource = new LocalDataSource();
  customerSource: LocalDataSource = new LocalDataSource();
  employeeSource: LocalDataSource = new LocalDataSource();
  currentCustomerCampaignPageNumber: number = 1;
  setCustomerToCampaignChangeId: NodeJS.Timeout;
  setPageNumberId: NodeJS.Timeout;
  whatsAppSupportlink: string;

  constructor(private campaignService : CampaignService,
              private exportAsService: ExportAsService,
              private themeService: NbThemeService,
              protected router: Router,
              protected constantService : ConstantsService,
              private dialogService: NbDialogService,
              private employeeService:EmployeeService,
              private ivrService:IvrService,
              private queueService:QueueService,
              private conferenceService:ConferenceService,) {

                //console.log("I am in constructor");

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
                  this.scheduleStartCards = this.scheduleStartCardsByThemes[theme.name];
                  this.scheduleStopCards = this.scheduleStopCardsByThemes[theme.name];
                  this.statusCards = this.statusCardsByThemes[theme.name];
                  const colors: any = theme.variables;
                  this.colorScheme = {
                  domain: [colors.primaryLight, colors.infoLight, colors.successLight, colors.warningLight, colors.dangerLight],
                };
              });

            }

  
  verifyAlphaNumericConstraint(event:any)
    {
      // console.log("event : ",event);
        let k:number;  
        k = event.keyCode; 
        let character = event.key;
        let pageNumber = this.currentCustomerCampaignPageNumber;

        // console.log("Current Page Number : ",pageNumber)
        // console.log("Input character char : ",character);
        // console.log("Input character char code : ",k);

        // console.log("String(pageNumber).length : ",String(pageNumber).length);

        if(((String(pageNumber).length == 1) || (String(pageNumber).length == 0)) && (k == 8))

          {
                //Do nothing
          }
          else
          {
            if((k >= 48 && k <= 57) || (k == 8))
              {
                // console.log("Arranging this.currentPageNumber");
                // console.log(String(pageNumber)+String(character));
                // console.log(Number(String(pageNumber)+String(character)));
                // this.currentPageNumber = Number(String(pageNumber)+String(character));
                // console.log("this.currentPageNumber : ", this.currentPageNumber);
    
                  // Its fine
                  if(this.setCustomerToCampaignChangeId == null)
                    {
                      this.setCustomerToCampaignChangeId = setTimeout(()=>{
                        this.setTableCustomerToCampaign();
                      },2500);
                    }
                    else{
                      clearTimeout(this.setCustomerToCampaignChangeId);
                      this.setCustomerToCampaignChangeId = setTimeout(()=>{
                           this.setTableCustomerToCampaign();
                      },2500);
                    }
              }
            else{
              // console.log("Setting back this.currentPageNumber");
              this.currentCustomerCampaignPageNumber = pageNumber;
              // console.log("this.currentPageNumber : ", this.currentPageNumber);
              this.showDialoge('Error','activity-outline','danger', "Only numbers are allowed"); 
            }  
          }

  }

  setTableDropDownValues()
  {
    //autodialertype

    this.campaignService.getAllAutodialerTypes(this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {
       // console.log("getAllEmployeesByOrganization");
        //console.log(JSON.stringify(allData));
        //this.allConference = [... JSON.parse(JSON.stringify(allData))];
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

       
        this.settings.columns.autodialertype.editor.config.list = [... list];
        this.settings = Object.assign({}, this.settings);

      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
       // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });


    
    this.employeeService.getAllEmployeesByOrganization(this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {
       // console.log("getAllEmployeesByOrganization");
        //console.log(JSON.stringify(allData));
        this.allEmployees = [... JSON.parse(JSON.stringify(allData))];
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
            singleObject.value = element.id;
            singleObject.title = element.id+", "+element.firstName + " " + element.lastName;;
            list.push( JSON.parse(JSON.stringify(singleObject)));
          });
        }

       
        this.settings.columns.managerId.editor.config.list = [... list];
        this.employeeSettings.columns.employeeid.editor.config.list = [... list];

        this.employeeSettings = Object.assign({}, this.employeeSettings);
        this.settings = Object.assign({}, this.settings);

      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
       // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });


    this.conferenceService.getAllConferenceByOrganization(this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {
       // console.log("getAllEmployeesByOrganization");
        //console.log(JSON.stringify(allData));
        this.allConference = [... JSON.parse(JSON.stringify(allData))];
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
            singleObject.value = element.confextension;
            singleObject.title = element.confextension;
            list.push( JSON.parse(JSON.stringify(singleObject)));
          });
        }

       
        this.settings.columns.confExtension.editor.config.list = [... list];
        this.settings = Object.assign({}, this.settings);

      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
       // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });

    this.queueService.getAllQueuesByOrganization(this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {
       // console.log("getAllEmployeesByOrganization");
        //console.log(JSON.stringify(allData));
        this.allQueues = [... JSON.parse(JSON.stringify(allData))];
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
            singleObject.value = element.extension;
            singleObject.title = element.extension;
            list.push( JSON.parse(JSON.stringify(singleObject)));
          });
        }

       
        this.settings.columns.queueExtension.editor.config.list = [... list];
        this.settings = Object.assign({}, this.settings);

      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
       // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });


    this.ivrService.getAllIvrsByOrganization(this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {
       // console.log("getAllEmployeesByOrganization");
        //console.log(JSON.stringify(allData));
        this.allIVR = [... JSON.parse(JSON.stringify(allData))];
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
            singleObject.value = element.extension;
            singleObject.title = element.extension;
            list.push( JSON.parse(JSON.stringify(singleObject)));
          });
        }

       
        this.settings.columns.ivrExtension.editor.config.list = [... list];
        this.settings = Object.assign({}, this.settings);

      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
       // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
    
  }

  setTable()
  {
    this.campaignService.getAllCampaignsOnOrganization(this.organization)
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

                if (element.isenabled == true) {
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
  }

  
  onDeleteConfirm(event): void {
    // console.log("User Deleted a row. Row data is  : ");
    //console.log(event);

    this.currentRecord = JSON.parse(JSON.stringify(event.data));

    this.showDetail = false;
    this.showAction = false;

    var list= [];
    var singleObject = {value: this.currentRecord.id, title:this.currentRecord.id};
    list.push(singleObject);
    this.customerSettings.columns.campaignid.editor.config.list = [... list];
    this.employeeSettings.columns.campaignid.editor.config.list = [... list];

    this.customerSettings = Object.assign({}, this.customerSettings);
    this.employeeSettings = Object.assign({}, this.employeeSettings);


    if (window.confirm('Are you sure you want to delete?')) {

   //console.log("Starting Delete API");

   this.campaignService.deleteCampaignByIdAndOrganization(this.currentRecord.id,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {

        //console.log("API returned result");

        if(String(result) == 'true')
        {
              //console.log("Result is true");
              event.confirm.resolve();
              var output = JSON.parse(JSON.stringify(this.single));
              //console.log("Chart intial value");
             //console.log(output);

             //console.log("this.currentRecord.isactive");
             //console.log(JSON.stringify(this.currentRecord.isactive));


              if(JSON.stringify(this.currentRecord.isactive) == "\"true\""||JSON.stringify(this.currentRecord.isactive) == "true"){
                //console.log("Subtracting enabled value");
              output[0].value = output[0].value - 1;
              }
              else{
                //console.log("Subtracting disabled value");
                output[1].value = output[1].value - 1;
              }
              
              this.single = [...output];
              //console.log("I am coming out from delete")
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
     // console.log("User rejected delete");
      event.confirm.reject();
    }
  }


  onSaveConfirm(event): void {

    //console.log("User Edited a row. Row data is  : ");
    //console.log(event);
    //console.log(event);

    this.previousRecord =  JSON.parse(JSON.stringify(event.data));
    this.currentRecord = JSON.parse(JSON.stringify(event.newData));
    this.currentCustomerCampaignPageNumber = 1;
    this.searchCustomerToCampaignString = "";
    this.setCustomerToCampaign();
    this.setEmployeeToCampaign();
    this.setIfScheduledStart();
    this.setIfScheduledStop();

    var list= [];
    var singleObject = {value: this.currentRecord.id, title:this.currentRecord.id};
    list.push(singleObject);
    this.customerSettings.columns.campaignid.editor.config.list = [... list];
    this.employeeSettings.columns.campaignid.editor.config.list = [... list];

    this.customerSettings = Object.assign({}, this.customerSettings);
    this.employeeSettings = Object.assign({}, this.employeeSettings);

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
    if(ConstantsService.user.role === ConstantsService.employee)
      {
      }
      else{
        this.showAction = true;
      }
    

    if (window.confirm('Are you sure you want to edit?')) {
    
     // console.log("Starting Update API");
      this.campaignService.updateCampaignByOrganization(JSON.stringify(this.currentRecord),this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {

        //console.log("API returned result");

        if(String(result) == 'true')
        {
            // console.log("Result is true");
             event.confirm.resolve();
             var output = JSON.parse(JSON.stringify(this.single));


             if(JSON.stringify(this.currentRecord.isactive) == JSON.stringify(this.previousRecord.isactive)){   
              //console.log("Both are same");
            }
            else{
              if(JSON.stringify(this.currentRecord.isactive) == "\"true\""||JSON.stringify(this.currentRecord.isactive) == "true")
              {
                //console.log("current record is true");
                output[0].value = output[0].value + 1;
                output[1].value = output[1].value - 1;
              }
              else if(JSON.stringify(this.currentRecord.isactive) == "\"false\""||JSON.stringify(this.currentRecord.isactive) == "false")
                {
                  //console.log("current record is false");
                  output[0].value = output[0].value - 1;
                  output[1].value = output[1].value + 1;
                
              }
              else
              {
                //console.log("I am something else");
                //console.log(this.currentRecord.isactive);
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
    this.currentRecord.isactive = false;
    
    //console.log(JSON.stringify(this.currentRecord));
    var id = this.currentRecord.managerId.split(',')[0];
    //console.log("Id : " + id);
    this.currentRecord.managerId = id;

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
    if(ConstantsService.user.role === ConstantsService.employee)
      {
      }
      else{
        this.showAction = false;
      }
    this.currentCustomerCampaignPageNumber = 1;
    this.searchCustomerToCampaignString = "";

    if (window.confirm('Are you sure you want to create?')) {

      //console.log("Starting Create API");
   
      this.campaignService.createCampaignByOrganization(JSON.stringify(this.currentRecord),this.organization)
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
   
           //console.log("API returned result");
   
           if(String(result) == 'true')
           {
                this.setTable();
                event.confirm.resolve();

                 this.campaignService.getCampaignByNameAndOrganization(this.currentRecord.name,this.organization)
                  .pipe(takeUntil(this.destroy$))
                  .subscribe({
                    next: result => {
                      this.currentRecord = JSON.parse(JSON.stringify(result));
                      //console.log("Result is true");
                      this.setCustomerToCampaign();
                      this.setEmployeeToCampaign();
                      this.setIfScheduledStart();
                      this.setIfScheduledStop();

                      var list= [];
                      var singleObject = {value: this.currentRecord.id, title:this.currentRecord.id};
                      list.push(singleObject);
                      this.customerSettings.columns.campaignid.editor.config.list = [... list];
                      this.employeeSettings.columns.campaignid.editor.config.list = [... list];

                      this.customerSettings = Object.assign({}, this.customerSettings);
                      this.employeeSettings = Object.assign({}, this.employeeSettings);
                    },
                    error: err => {
                      console.log("Error : "+ JSON.stringify(err));
                      // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
                    }
                  });

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

  
  onSearch()
  {
    this.showAction = false;
    this.showDetail = false;

   //console.log("On Search is clicked");

    if(this.currentDropDownOption.label == 'Name')
    {
    // console.log("Ami User Search Started");
      this.campaignService.getCampaignByNameAndOrganization(this.inputValues[0],this.organization)
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
        
              if (element.isenabled == true) {
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
   
    else if(this.currentDropDownOption.label == 'Phone Context')
    {
      //console.log("Phone Context Search Started");
      this.campaignService.findAllByPhonecontextAndOrganization(this.inputValues[0],this.organization)
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
      
            if (element.isenabled == true) {
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
    else if(this.currentDropDownOption.label == 'Call On Mobile')
    {
      //console.log("Is Active Search Started");
      this.campaignService.findAllByIsonmobileAndOrganization(this.inputValues[0],this.organization)
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
            
          }
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
      
            if (element.isenabled == true) {
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
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }

    else if(this.currentDropDownOption.label == 'Country')
    {
      //console.log("Phone Context Search Started");
      this.campaignService.findAllByCountryAndOrganization(this.inputValues[0],this.organization)
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
      
            if (element.isenabled == true) {
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

    else if(this.currentDropDownOption.label == 'Business')
    {
      //console.log("Phone Context Search Started");
      this.campaignService.findAllByBusinessAndOrganization(this.inputValues[0],this.organization)
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
      
            if (element.isenabled == true) {
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
    else if(this.currentDropDownOption.label == 'AutoDialer type')
    {
      //console.log("Phone Context Search Started");
      this.campaignService.findAllByAutodialertypeAndOrganization(this.inputValues[0],this.organization)
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
      
            if (element.isenabled == true) {
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

    else if(this.currentDropDownOption.label == 'Manager ID')
    {
      //console.log("Phone Context Search Started");
      this.campaignService.findAllByManagerAndOrganization(this.inputValues[0],this.organization)
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
      
            if (element.isenabled == true) {
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

    else if(this.currentDropDownOption.label == 'Start Date Greator')
    {
      //console.log("Phone Context Search Started");
      var inputDate1: Date = new Date(this.inputValues[0]);
      var result1 = inputDate1.toLocaleString();

      this.campaignService.findAllByStartdateGreaterThanEqualAndOrganization(result1,this.organization)
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
      
            if (element.isenabled == true) {
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
  
  getScheduleStartData(event){

    if(event == 'remove')
    {

    }

    if(event == 'n-seconds')
    {

    }

    if(event == 'fixed-date')
    {

    }

    if(event == 'cron')
    {

    }

  }


  getScheduleStopData(event){

    if(event == 'remove')
    {

    }

    if(event == 'n-seconds')
    {

    }

    if(event == 'fixed-date')
    {

    }

    if(event == 'cron')
    {

    }

  }
  
  getChildData(event){
    
    if(event == 'refreshCustomerToCampaignTable')
      {
        this.setTableCustomerToCampaign();
      }

    if(event == 'Start/Stop')
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
          if(element == "isactive")
          {
            this.values[i] = this.currentRecord.isactive;
          }
          i = i +1;
        });

       

        //Update upper chart
        var output = JSON.parse(JSON.stringify(this.single));
        if(JSON.stringify(this.currentRecord.isactive) == "\"true\""||JSON.stringify(this.currentRecord.isactive) == "true")
                     {
                       //console.log("current record is true");
                       output[0].value = output[0].value + 1;
                       output[1].value = output[1].value - 1;
                     }
                     else if(JSON.stringify(this.currentRecord.isactive) == "\"false\""||JSON.stringify(this.currentRecord.isactive) == "false")
                       {
                        // console.log("current record is false");
                         output[0].value = output[0].value - 1;
                         output[1].value = output[1].value + 1;
                       
                     }
                     else
                     {
                      // console.log("I am something else");
                       //console.log(this.currentRecord.isactive);
                     }
       
                   this.single = [...output];
                   
        //this.setTable();
    }
  }

  onUserRowSelect(event): void {
    //console.log("User Selected a row. Row data is  : ");
    //console.log(event);
    this.currentRecord = JSON.parse(JSON.stringify(event.data));
    

    var list= [];
    var singleObject = {value: this.currentRecord.id, title:this.currentRecord.id};
    list.push(singleObject);
    this.customerSettings.columns.campaignid.editor.config.list = [... list];
    this.employeeSettings.columns.campaignid.editor.config.list = [... list];

    this.customerSettings = Object.assign({}, this.customerSettings);
    this.employeeSettings = Object.assign({}, this.employeeSettings);

    var columns = JSON.parse(JSON.stringify(this.settings.columns));
    var keys = [];
    Object.keys(this.currentRecord).map(function (key) { //console.log(key);
      // console.log(key);
      // console.log(columns[key].title);
      keys.push(columns[key].title);

    });
    this.keys = keys;
    this.values = Object.values(this.currentRecord);
    //console.log("setCustomerToCampaign ");
    this.currentCustomerCampaignPageNumber = 1;
    this.searchCustomerToCampaignString = "";
    this.setCustomerToCampaign();
    //console.log("setEmployeeToCampaign ");
    this.setEmployeeToCampaign();
    this.setIfScheduledStart();
    this.setIfScheduledStop();
    this.showDetail = true;
    if(ConstantsService.user.role === ConstantsService.employee)
      {
      }
      else{
        this.showAction = true;
      }
    
  }

  
  export() {
  this.exportAsConfigCampaign.type = this.downloadAs;
    this.exportAsService
      .save(this.exportAsConfigCampaign, 'Campaigns')
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
      //console.log(JSON.stringify(currentSearch));
      //console.log(JSON.stringify(currentProperties));
      //console.log(JSON.stringify(columns));

      currentProperties.forEach( (element) => {
        //console.log(JSON.stringify(element));
        //console.log(JSON.stringify(columns[element]));

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
    // console.log("I am in ngOnIt");

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
        //Do nothing here as user is allowed to view but not allowed to do anything else
        // this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
        this.settings.actions.add = false;
        this.settings.actions.edit = false;
        this.settings.actions.delete = false;
        this.customerSettings.actions.delete = false;
        this.employeeSettings.actions.delete = false;
        document.getElementById("downloadMainTableButton").hidden = true;

      }

    this.setTable(); 
    if(ConstantsService.user.role === ConstantsService.employee)
      {
        //Do nothing here as user is allowed to view but not allowed to do anything else
        // this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
      }
    else
      {
        this.setTableDropDownValues();
      }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }

  setEmployeeToCampaign()
  {
    this.campaignService.findAllEmployeesByCampaignAndOrganization(this.currentRecord.id,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {
        if(allData == null)
        {
          //console.log("I am null data");
          //console.log("Inside Data : "+ JSON.stringify(allData));
            this.allEmployeeToCampaign = [];
            this.employeeSource.load([]);   
        }
        else
        {
            //console.log("Inside Data : "+ JSON.stringify(allData));
             this.allEmployeeToCampaign = [...JSON.parse(JSON.stringify(allData))];
              var  arr= JSON.parse(JSON.stringify(allData));
              this.employeeSource.load(arr);   
        }

      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
      //  this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  setIfScheduledStart()
  {
    this.campaignService.findIfStartScheduledJobsOrganization(this.constantService.cron,this.currentRecord.id,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
        if(String(result) == 'true')
        {
             this.isAlreadyScheduledStart = '{Already Scheduled (Schedule again to overwrite)}';
             this.allScheduleTypeStart.push(this.constantService.cron);
        }
        else{
                 this.isAlreadyScheduledStart = '{Nothing Scheduled (Schedule Now)}';
        }

      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
      //  this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });

    this.campaignService.findIfStartScheduledJobsOrganization(this.constantService.fixeddate,this.currentRecord.id,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
        if(String(result) == 'true')
        {
             this.isAlreadyScheduledStart = '{Already Scheduled (Schedule again to overwrite)}';
             this.allScheduleTypeStart.push(this.constantService.fixeddate);
        }
        else{
                 this.isAlreadyScheduledStart = '{Nothing Scheduled (Schedule Now)}';
        }

      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
      //  this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });


    this.campaignService.findIfStartScheduledJobsOrganization(this.constantService.afternseconds,this.currentRecord.id,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
        if(String(result) == 'true')
        {
             this.isAlreadyScheduledStart = '{Already Scheduled (Schedule again to overwrite)}';
             this.allScheduleTypeStart.push(this.constantService.afternseconds);
        }
        else{
                 this.isAlreadyScheduledStart = '{Nothing Scheduled (Schedule Now)}';
        }

      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
      //  this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }


  setIfScheduledStop()
  {
    this.campaignService.findIfStopScheduledJobsOrganization(this.constantService.cron,this.currentRecord.id,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
        if(String(result) == 'true')
        {
             this.isAlreadyScheduledStop = '{Already Scheduled (Schedule again to overwrite)}';
             this.allScheduleTypeStop.push(this.constantService.cron);
        }
        else{
                 this.isAlreadyScheduledStop = '{Nothing Scheduled (Schedule Now)}';
        }

      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
      //  this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });

    this.campaignService.findIfStopScheduledJobsOrganization(this.constantService.fixeddate,this.currentRecord.id,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
        if(String(result) == 'true')
        {
             this.isAlreadyScheduledStop = '{Already Scheduled (Schedule again to overwrite)}';
             this.allScheduleTypeStop.push(this.constantService.fixeddate);
        }
        else{
                 this.isAlreadyScheduledStop = '{Nothing Scheduled (Schedule Now)}';
        }

      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
      //  this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });


    this.campaignService.findIfStopScheduledJobsOrganization(this.constantService.afternseconds,this.currentRecord.id,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
        if(String(result) == 'true')
        {
             this.isAlreadyScheduledStop = '{Already Scheduled (Schedule again to overwrite)}';
             this.allScheduleTypeStop.push(this.constantService.afternseconds);
        }
        else{
                 this.isAlreadyScheduledStop = '{Nothing Scheduled (Schedule Now)}';
        }

      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
      //  this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }


  onCustomerToCampaignSearchTextChange()
  {
    if(this.setCustomerToCampaignChangeId == null)
      {
        this.setCustomerToCampaignChangeId = setTimeout(()=>{
          this.setTableCustomerToCampaign();
          this.currentCustomerCampaignPageNumber = 1;
        },1500);
      }
      else{
        clearTimeout(this.setCustomerToCampaignChangeId);
        this.setCustomerToCampaignChangeId = setTimeout(()=>{
             this.setTableCustomerToCampaign();
             this.currentCustomerCampaignPageNumber = 1;
        },1500);
      }
  }

  setTableCustomerToCampaign()
  {
    this.setCustomerToCampaign();
  }

  setTotalRecords(records:number)
  {
    this.customerToCampaignTotalRecords = records;
  }


  setPageNumber(totalPages:number)
  {

    if(this.setPageNumberId == null)
      {
        this.setPageNumberId = setTimeout(()=>{
          this.pageOptionsAll = [];
          for(let i=0;i<totalPages;i++)
            {
              this.pageOptionsAll.push(i+1);
              if(i==500)
                {
                  this.pageOptions$ = of(this.pageOptionsAll);
                }
            }
            this.pageOptions$ = of(this.pageOptionsAll);

            this.setPageNumberId = null;

        },2500);
      }
  }

  setTableDataCustomersByCampaign(allData:any)
  {

    if(this.currentCustomerCampaignPageNumber == 1)
      {
          this.customerToCampaignTotalPages = allData.numberOfPages;
          this.setTotalRecords(allData.totalRecords);
          // this.setPageNumber(this.customerToCampaignTotalPages);
      }

       this.customerSource.load(<any[]>allData.data); 
       let arr = JSON.parse(JSON.stringify(allData.data));
       this.allCustomerToCampaign = [...arr];
  }


  setCustomerToCampaign()
  {
    this.campaignService.findAllCustomersByCampaignAndOrganization(this.currentRecord.id,this.organization,this.searchCustomerToCampaignString,(this.currentCustomerCampaignPageNumber-1),this.currentCustomerToCampaignPageSize)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {
        this.setTableDataCustomersByCampaign(allData);
      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
      //  this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }


  onCustomerDeleteConfirm(event)
  {
    
    let currentRecord = JSON.parse(JSON.stringify(event.data));

    if (window.confirm('Are you sure you want to delete?')) {
      this.campaignService.deleteCustomerToCampaignByOrganization("["+JSON.stringify(currentRecord)+"]",this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
             //console.log("Result is true");
            this.setCustomerToCampaign();
            event.confirm.resolve();
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          event.confirm.reject();
        }
      });
    }    
  }

  onEmployeeDeleteConfirm(event)
  {
    
    let currentRecord = JSON.parse(JSON.stringify(event.data));

    if (window.confirm('Are you sure you want to delete?')) {
      this.campaignService.deleteEmployeeToCampaignByOrganization("["+JSON.stringify(currentRecord)+"]",this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
             //console.log("Result is true");
             this.setEmployeeToCampaign();
             event.confirm.resolve();
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          event.confirm.reject();
        }
      });
    }

   
  }

  deleteAllCampaignEmployees(){
    this.showDeleteDialoge("Delete all employees associated with this campaign?","deleteEmployees"); 
  }

  actualDeleteAllCampaignEmployees()
  {
    this.campaignService.deleteAllEmployeesByCampaignIdAndOrganization(this.currentRecord.id,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {

        //console.log("API returned result");

        if(String(result) == 'true')
        {
          this.allEmployeeToCampaign = [];
          this.employeeSource.load([]); 
        }  
        else{
      
        }
      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
       // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  deleteAllCampaignCustomers(){
    this.showDeleteDialoge("Delete all Customers associated with this campaign?","deleteCustomers"); 
  }

  actualDeleteAllCampaignCustomers()
  {
    this.campaignService.deleteAllCustomersByCampaignIdAndOrganization(this.currentRecord.id,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {

        //console.log("API returned result");

        if(String(result) == 'true')
        {
          this.allCustomerToCampaign = [];
          this.customerSource.load([]); 
          this.searchCustomerToCampaignString = "";
          this.customerToCampaignTotalRecords = 0;
          this.customerToCampaignTotalPages = 0;
          this.currentCustomerCampaignPageNumber = 1;
        }  
        else{
      
        }
      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
       // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
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
      if(type != undefined && type == "deleteCustomers")
        {   
           this. actualDeleteAllCampaignCustomers();    
        }
      else if(type != undefined && type == "deleteEmployees")
        {
           this.actualDeleteAllCampaignEmployees();
        }
    });
}

  bulkUplodCustomer(){
    //console.log("bulkUplodCustomer");
    this.showUplodCustomerDialoge(); 
  }

  bulkUplodEmployee(){

    //console.log("bulkUplodEmployee");
    this.showUplodEmployeeDialoge(); 

  }

  showUplodCustomerDialoge() {

    var closeOnBackdropClick: boolean = false;
    var closeOnEsc: boolean = true;
    this.dialogService.open(SubmitBulkCustomerComponent, { closeOnBackdropClick,closeOnEsc,
      context: {
        // allRecords: this.allCustomers,
        campaignid: this.currentRecord.id,
        // allCustomerToCampaign : this.allCustomerToCampaign,
      },
    }).onClose.subscribe({
      next: (res) => {
          this.setCustomerToCampaign();
      },
      error: (err) => console.error(`Observer got an error: ${err}`),
    });


    //this.dialogService.open(SubmitBulkCustomerComponent, { closeOnBackdropClick });

    }

    showUplodEmployeeDialoge() {
    var closeOnBackdropClick: boolean = false;
    var closeOnEsc: boolean = true;
    this.dialogService.open(SubmitBulkEmployeeComponent, {closeOnBackdropClick,closeOnEsc,
      context: {
        allRecords: this.allEmployees,
        campaignid: this.currentRecord.id,
        allEmployeeToCampaign : this.allEmployeeToCampaign,
      },
    }).onClose.subscribe({
      next: (res) => {
          this.setEmployeeToCampaign();
      },
      error: (err) => console.error(`Observer got an error: ${err}`),
    });

    }
    
  customerExport() {
    this.exportAsConfigCustomer.type = this.downloadAsCustomer;
    this.exportAsService
      .save(this.exportAsConfigCustomer, 'Customers_Of_Campaign')
      .subscribe(() => {
        // save started
      });
  }

  employeeExport() {
    this.exportAsConfigEmployee.type = this.downloadAsEmployee;
    this.exportAsService
      .save(this.exportAsConfigEmployee, 'Employees_Of_Campaign')
      .subscribe(() => {
        // save started
      });
  }
}