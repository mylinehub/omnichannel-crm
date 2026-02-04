import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CallDetailService } from './../service/call-detail.service';
import { delay, map, takeUntil } from 'rxjs/operators';
import { Observable, Subject, of } from 'rxjs';
import { LocalDataSource } from 'ng2-smart-table';
import {
  ExportAsService,
  ExportAsConfig,
  SupportedExtensions,
} from 'ngx-export-as';
import { NbToastrService,NbThemeService,NbStepChangeEvent,NbComponentStatus, NbToastRef, NbDialogService, NbTagComponent } from '@nebular/theme';
import { takeWhile } from 'rxjs/operators' ;
import { Router } from '@angular/router';
import { ConstantsService } from './../../../service/constants/constants.service';
import { CellScrollComponent } from './cell-scroll/cell-scroll.component';
import { MessageListDataService } from '../../../@theme/components/header/message-list/message-list-data-service/message-list-data.service';
import { ConvertedButtonComponent } from './converted-button/converted-button.component';
import { DescriptionInputComponent } from './description-input/description-input.component';
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';
import { CustomInputTableComponent } from '../../employee/all-employees/custom-input-table/custom-input-table.component';

interface CardSettings {
  title: string;
  iconClass: string;
  type: string;
}


@Component({
  selector: 'ngx-all-calls',
  templateUrl: './all-calls.component.html',
  styleUrls: ['./all-calls.component.scss']
})
export class AllCallsComponent implements OnInit, OnDestroy {
  
  @ViewChild('searchName') searchTextInput;
  @ViewChild('autoEmployeeInput') input;
  @ViewChild('autoMultipleEmployeeInput') inputMultiple;

  tableHeading = 'Call Details';
  inMemoryTableHeading = 'In Memory Call History';
  private destroy$: Subject<void> = new Subject<void>();
  downloadAs: SupportedExtensions = 'png';


  file: File = null;

  
  stepTwoNextButton = true;
  stepThreeNextButton = true;

  currentRecord:any = ' ';
  previousRecord:any = ' ';
  allRecords:any = [];
  allGraphRecords:any = [];

  //Step2
  keys: any = [];
  values: any = [];
  selectedOption;

  //Step3
  inputs: any = [];
  types: any = [];
  inputValues: any = ['','',''];
  organization = '';

  colorScheme: any;
  themeSubscription: any;

  changeEvent: NbStepChangeEvent;
  private alive = true;
  

  view :any = [600, 200];
  gradient: boolean = false;
  on = true;
  solarValue: number;
  

  statusCards: string;

  commonStatusCardsSet: CardSettings[] = [
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
    ],
    dark: this.commonStatusCardsSet,
  };


  redirectDelay: number = 0;
  showDetail : boolean = false;
  showAction : boolean = false;

  settings = {

    hideSubHeader: true,
    pager: { display: false },
    actions: {
      add: false,      //  if you want to remove add button
      edit: false,     //  if you want to remove edit button
      delete: false //  if you want to remove delete button
    },

    columns: {
      id: {
        title: 'ID',
        type: 'number',
        editable: false,
        addable: false,
        hide: true,
      },
      employeeName: {
        title: 'Employee Name',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      callerid: {
        title: 'Extension',
        type: 'string',
      },
      customerName: {
        title: 'Customer Name',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      customerid: {
        title: 'Customer Number',
        type: 'string',
      },
      coverted: {
        title: 'Converted',
        type: 'custom',
        valuePrepareFunction: (row) => {
          // DATA FROM HERE GOES TO renderComponent
          return row;
        },
        renderComponent: ConvertedButtonComponent,
      },
      description: {
        title: 'Remarks',
        type: 'custom',
        valuePrepareFunction: (row) => {
          // DATA FROM HERE GOES TO renderComponent
          return row;
        },
        renderComponent: DescriptionInputComponent,
      },
      ivr: {
        title: 'IVR',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide: true,
      },
      queue: {
        title: 'Queue',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide: true,
      },
      pridictive: {
        title: 'Predictive',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide: true,
      },
      progressive: {
        title: 'Progressive',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide: true,
      },
      organization: {
        title: 'Organization',
        type: 'string',
        hide: true,
      },
      phoneContext: {
        title: 'Phone Context',
        type: 'string',
        hide: true,
      },
      calldurationseconds: {
        title: 'Duration(s)',
        type: 'number',
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
         hide: true,
      },
      timezone: {
        title: 'Time Zone',
        type: 'string',
        hide: true,
      },
	  starttime: {
        title: 'Start Time',
        type: 'string',
        hide: true,
      },
	  endtime: {
        title: 'End Time',
        type: 'string',
        hide: true,
      },
      callonmobile: {
        title: 'Call On Mobile',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide: true,
      },
	  isconference: {
        title: 'Is Conference',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide: true,
      },
	  isconnected: {
        title: 'Is Connected',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide: true,
      },
      maximumchannels: {
        title: 'Maximum Channels',
        type: 'number',
        hide: true,
      },
      country: {
        title: 'Country',
        type: 'string',
        hide: true,
      },
      callType: {
        title: 'Call Type',
        type: 'string',
      },
      startdate: {
        title: 'Start Date',
        type: 'date',
        valuePrepareFunction: (cell: any) => this.formatEventAt(cell),
        hide: true,
      },
	  enddate: {
        title: 'Date',
        type: 'date',
        valuePrepareFunction: (cell: any) => this.formatEventAt(cell),
      },
    callSessionId: {
        title: 'Session Id',
        type: 'string',
        hide: true,
      },
      linkId: {
        title: 'Link Id',
        type: 'string',
        hide: true,
      },
      campaignID: {
        title: 'Campaign Id',
        type: 'string',
        hide: true,
      },
      callCost: {
        title: 'Cost',
        type: 'string',
      },
      callCostMode: {
        title: 'Cost Mode',
        type: 'string',
        hide: true,
      },
      campaignRunDetailsId: {
        title: 'Run ID',
        type: 'string',
        hide: true,
      },
      campaignRunCallLogId: {
        title: 'Call Leg Id',
        type: 'string',
        hide: true,
      }
	}
  };


  inMemorySettings = {

    actions: {
      add: false,      //  if you want to remove add button
      edit: false,     //  if you want to remove edit button
      delete: false //  if you want to remove delete button
    },
    pager: {
      display: true,
      perPage: 5
    },

    columns: {
      id: {
        title: 'ID',
        type: 'number',
        editable: false,
        addable: false,
        hide: true,
      },
      employeeName: {
        title: 'Employee Name',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      callerid: {
        title: 'Extension',
        type: 'string',
      },
      customerName: {
        title: 'Customer Name',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      customerid: {
        title: 'Customer Number',
        type: 'string',
      },
      coverted: {
        title: 'Converted',
        type: 'custom',
        valuePrepareFunction: (row) => {
          // DATA FROM HERE GOES TO renderComponent
          return row;
        },
        renderComponent: ConvertedButtonComponent,
      },
      description: {
        title: 'Remarks',
        type: 'custom',
        valuePrepareFunction: (row) => {
          // DATA FROM HERE GOES TO renderComponent
          return row;
        },
        renderComponent: DescriptionInputComponent,
      },
      ivr: {
        title: 'IVR',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide: true,
      },
      queue: {
        title: 'Queue',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide: true,
      },
      pridictive: {
        title: 'Predictive',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide: true,
      },
      progressive: {
        title: 'Progressive',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide: true,
      },
      organization: {
        title: 'Organization',
        type: 'string',
        hide: true,
      },
      phoneContext: {
        title: 'Phone Context',
        type: 'string',
        hide: true,
      },
      calldurationseconds: {
        title: 'Duration(s)',
        type: 'number',
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
         hide: true,
      },
      timezone: {
        title: 'Time Zone',
        type: 'string',
        hide: true,
      },
	  starttime: {
        title: 'Start Time',
        type: 'string',
        hide: true,
      },
	  endtime: {
        title: 'End Time',
        type: 'string',
        hide: true,
      },
      callonmobile: {
        title: 'Call On Mobile',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide: true,
      },
	  isconference: {
        title: 'Is Conference',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide: true,
      },
	  isconnected: {
        title: 'Is Connected',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
         hide: true,
      },
      maximumchannels: {
        title: 'Maximum Channels',
        type: 'number',
        hide: true,
      },
      country: {
        title: 'Country',
        type: 'string',
        hide: true,
      },
      callType: {
        title: 'Call Type',
        type: 'string',
      },
      startdate: {
        title: 'Start Date',
        type: 'date',
        valuePrepareFunction: (cell: any) => this.formatEventAt(cell),
        hide: true,
      },
	  enddate: {
        title: 'Date',
        type: 'date',
        valuePrepareFunction: (cell: any) => this.formatEventAt(cell),
      },
      callSessionId: {
        title: 'Session Id',
        type: 'string',
        hide: true,
      },
      linkId: {
        title: 'Link Id',
        type: 'string',
        hide: true,
      },
      campaignID: {
        title: 'Campaign Id',
        type: 'string',
        hide: true,
      },
      callCost: {
        title: 'Cost',
        type: 'string',
      },
      callCostMode: {
        title: 'Cost Mode',
        type: 'string',
        hide: true,
      },
      campaignRunDetailsId: {
        title: 'Run ID',
        type: 'string',
        hide: true,
      },
      campaignRunCallLogId: {
        title: 'Call Leg Id',
        type: 'string',
        hide: true,
      }
	 }
  };


  radioOptions = [
    { value: '1-Column Search', label: '1-Property Search', checked: true },
    { value: '2-Column Search', label: '2-Property Search', disabled: false  },
    { value: '3-Column Search', label: '3-Property Search', disabled: true },
  ];

  radioOption;

  allDropDownOptions = [
    { column: 1, label: 'Phone Context', properties:['phoneContext']},
    { column: 1, label: 'TimeZone', properties:['timezone']},
    { column: 1, label: 'Is Conference', properties:['isconference']},
    { column: 1, label: 'Is Ivr', properties:['ivr']},
    { column: 1, label: 'Is Queue', properties:['queue']},
    { column: 1, label: 'Is Pridictive', properties:['pridictive']},
    { column: 1, label: 'Is Progressive', properties:['progressive']},
    { column: 1, label: 'Dialed Number', properties:['customerid']},
    { column: 1, label: 'Country', properties:['country']},
    { column: 1, label: 'Call On Mobile', properties:['callonmobile']},
    { column: 1, label: 'Extension', properties:['callerid']},
    { column: 1, label: 'Start Date Greator', properties:['startdate']},
    { column: 1, label: 'Call Duration (s) Less', properties:['calldurationseconds']},
    { column: 1, label: 'Call Duration (s) Greator', properties:['calldurationseconds']},
    { column: 2, label: 'Call Duration (s) Greator, Extension', properties:['calldurationseconds','callerid']},
    { column: 2, label: 'Call Duration (s) Greator, Dialed Number', properties:['calldurationseconds','customerid']},
    { column: 2, label: 'Call Duration (s) Greator, Is Conference', properties:['calldurationseconds','isconference']},
    { column: 2, label: 'Call Duration (s) Greator, Phone Context', properties:['calldurationseconds','phoneContext']},
    { column: 2, label: 'Call Duration (s) Greator, Timezone', properties:['calldurationseconds','timezone']},
    { column: 2, label: 'Call Duration (s) Less, Extension', properties:['calldurationseconds','callerid']},
    { column: 2, label: 'Call Duration (s) Less, Timezone', properties:['calldurationseconds','timezone']},
    { column: 2, label: 'Call Duration (s) Less, Phone Context', properties:['calldurationseconds','phoneContext']},
    { column: 2, label: 'Call Duration (s) Less, Is Conference', properties:['calldurationseconds','isconference']},
    { column: 2, label: 'Call On Mobile, Is Conference', properties:['callonmobile','isconference']},
    { column: 2, label: 'Call Duration (s) Less, Dialed Number', properties:['calldurationseconds','customerid']},
  ];

  dropDownOptions = [
    { column: 1, label: 'Phone Context', properties:['phoneContext']},
    { column: 1, label: 'TimeZone', properties:['timezone']},
    { column: 1, label: 'Is Conference', properties:['isconference']},
    { column: 1, label: 'Is Ivr', properties:['ivr']},
    { column: 1, label: 'Is Queue', properties:['queue']},
    { column: 1, label: 'Is Pridictive', properties:['pridictive']},
    { column: 1, label: 'Is Progressive', properties:['progressive']},
    { column: 1, label: 'Dialed Number', properties:['customerid']},
    { column: 1, label: 'Country', properties:['country']},
    { column: 1, label: 'Call On Mobile', properties:['callonmobile']},
    { column: 1, label: 'Extension', properties:['callerid']},
    { column: 1, label: 'Start Date Greator', properties:['startDate']},
    { column: 1, label: 'Call Duration (s) Less', properties:['calldurationseconds']},
    { column: 1, label: 'Call Duration (s) Greator', properties:['calldurationseconds']},
  ];


  dropDownOption;

  source: LocalDataSource = new LocalDataSource();
  inMemorySource: LocalDataSource = new LocalDataSource();

  // trees:any = ['Employee A','Employee B'];

  searchString:any = "";
  currentDropDownOption: any = null;
  isSearchDone:boolean = false;
  previousDropDownOption:any = null;
  currentPageNumber:number = 1;
  currentPageSize:number = 6;
  setSearchTextChangeId: any;

  single = [
    {
      name: 'Calls',
      value: 0,
    },
  ];
  totalPages:any = 50;
  pageOptionsAll = [];
  pageOptions$: Observable<number[]>;
  setPageNumberId: NodeJS.Timeout;
  // pageNumberInput: HTMLElement;
  // pageNumberInputListner: void;
  // pageNumberKeyDownListner: void;
  
   exportAsConfig: ExportAsConfig = {
    type: 'xlsx', // the type you want to download
    elementIdOrContent: 'lastTable', // the id of html/table element
  };
  whatsAppSupportlink: string;
  
  constructor(private callDetailService : CallDetailService,
              private exportAsService: ExportAsService,
              private themeService: NbThemeService,
              private nbToastrService:NbToastrService,
              protected router: Router,
              protected constantService : ConstantsService,
              private dialogService: NbDialogService,
              protected messageListDataService:MessageListDataService,) {

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
                  this.statusCards = this.statusCardsByThemes[theme.name];
                  const colors: any = theme.variables;
                  this.colorScheme = {
                  domain: [colors.primaryLight, colors.infoLight, colors.successLight, colors.warningLight, colors.dangerLight],
                };
              });
            }
   
    
      verifyAlphaNumericConstraintforSize(event:any)
      {
        // console.log("event : ",event);
          let k:number;  
          k = event.keyCode; 
          let character = event.key;
          let pageSize = this.currentPageSize;

          // console.log("Current Page Number : ",pageNumber)
          // console.log("Input character char : ",character);
          // console.log("Input character char code : ",k);

          // console.log("String(pageNumber).length : ",String(pageNumber).length);

          if(((String(pageSize).length == 1) || (String(pageSize).length == 0)) && (k == 8))

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

                    if(ConstantsService.user.role === ConstantsService.employee)
                      {
                        this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
                      }
                    else
                      {
                          if(this.setSearchTextChangeId == null)
                            {
                              this.setSearchTextChangeId = setTimeout(()=>{
                                this.setTableAsPerSearch();
                              },1500);
                            }
                            else{
                              clearTimeout(this.setSearchTextChangeId);
                              this.setSearchTextChangeId = setTimeout(()=>{
                                  this.setTableAsPerSearch();
                              },1500);
                            }
                    }
                }
              else{
                // console.log("Setting back this.currentPageNumber");
                this.currentPageSize = pageSize;
                // console.log("this.currentPageNumber : ", this.currentPageNumber);
                this.showDialoge('Error','activity-outline','danger', "Only numbers are allowed"); 
              }  
            }

    }


    verifyAlphaNumericConstraint(event:any)
    {
      // console.log("event : ",event);
        let k:number;  
        k = event.keyCode; 
        let character = event.key;
        let pageNumber = this.currentPageNumber;

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

                  if(ConstantsService.user.role === ConstantsService.employee)
                    {
                      this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
                    }
                  else
                    {
                        if(this.setSearchTextChangeId == null)
                          {
                            this.setSearchTextChangeId = setTimeout(()=>{
                              this.setTableAsPerSearch();
                            },1500);
                          }
                          else{
                            clearTimeout(this.setSearchTextChangeId);
                            this.setSearchTextChangeId = setTimeout(()=>{
                                this.setTableAsPerSearch();
                            },1500);
                          }
                  }
              }
            else{
              // console.log("Setting back this.currentPageNumber");
              this.currentPageNumber = pageNumber;
              // console.log("this.currentPageNumber : ", this.currentPageNumber);
              this.showDialoge('Error','activity-outline','danger', "Only numbers are allowed"); 
            }  
          }

  }


  onSearchTextChange()
  {

    // console.log('onSearchTextChange');
    if(ConstantsService.user.role === ConstantsService.employee)
      {
        this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
      }
    else
      {
        if(this.setSearchTextChangeId == null)
          {
            this.setSearchTextChangeId = setTimeout(()=>{
              this.currentPageNumber = 1;
              this.setTableAsPerSearch();
            },1500);
          }
          else{
            clearTimeout(this.setSearchTextChangeId);
            this.setSearchTextChangeId = setTimeout(()=>{
                this.currentPageNumber = 1;
                this.setTableAsPerSearch();
            },1500);
          }
    }
  }

  setTableAsPerSearch()
  {
    if(!this.isSearchDone)
      {
            this.setTable();
      }
    else
      {
            this.onSearch();
      }
  }

  setTotalRecords(output:any)
  {
    this.single=[...output];
  }


  // setPageNumber(totalPages:number)
  // {

  //   if(this.setPageNumberId == null)
  //     {
  //       this.setPageNumberId = setTimeout(()=>{
  //         this.pageOptionsAll = [];
  //         for(let i=0;i<totalPages;i++)
  //           {
  //             this.pageOptionsAll.push(i+1);
  //             if(i==500)
  //               {
  //                 this.pageOptions$ = of(this.pageOptionsAll);
  //               }
  //           }
  //           this.pageOptions$ = of(this.pageOptionsAll);

  //           this.setPageNumberId = null;

  //       },2500);
  //     }
  // }

  setAnyKindTableData(allData:any)
  {

    if(this.currentPageNumber == -1)
      {
        this.currentPageNumber = 1;
      }

    if(this.currentPageNumber == 1)
      {
          this.totalPages = allData.numberOfPages;
          let output = [
            {
              name: 'AllCall Records',
              value: 0,
            },
          ];

          output[0].value = allData.totalRecords;
          this.setTotalRecords(output);
          // this.setPageNumber(this.totalPages);
      }

       this.source.load(<any[]>allData.data); 
       let arr = JSON.parse(JSON.stringify(allData.data));
       this.allRecords = [...arr];
  }

  setTable()
  {
    this.callDetailService.getAllCallDetailsOnOrganization(this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {
            this.setAnyKindTableData(allData);
      },
      error: err => {
       console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  search(){

    if(ConstantsService.user.role === ConstantsService.employee)
      {
        this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
      }
    else
      {
        this.currentPageNumber = 1;
        this.searchString = "";
        this.onSearch();
      }
  }
   
  onSearch()
  {
    // console.log("this.inputValues[0] : ",this.inputValues[0]);
    // console.log("this.inputValues[1] : ",this.inputValues[1]);
    // console.log("this.inputValues[2] : ",this.inputValues[2]);

    this.showAction = false;
    this.showDetail = false;
    
    if(!this.isSearchDone)
      {
        this.isSearchDone = true;
      }

   console.log("On Search is clicked");

  if(this.currentDropDownOption.label == 'Phone Context')
      {
        this.callDetailService.findAllByPhoneContextAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'TimeZone')
      {
        this.callDetailService.findAllByTimezoneAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });

      }
  else if(this.currentDropDownOption.label == 'Is Conference')
      {
        this.callDetailService.findAllByIsconferenceAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Is Ivr')
      {
        this.callDetailService.findAllByIsivrAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Is Queue')
      {
        this.callDetailService.findAllByIsqueueAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Is Pridictive')
      {
        this.callDetailService.findAllByIspridictiveAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Is Progressive')
      {
        this.callDetailService.findAllByIsprogressiveAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Dialed Number')
      {
        this.callDetailService.findAllByCustomeridAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Country')
      {
        this.callDetailService.findAllByCountryAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Call On Mobile')
      {
        this.callDetailService.findAllByCallonmobileAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Extension')
      {
        this.callDetailService.findAllByCalleridAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Start Date Greator')
      {
        var inputDate: Date = new Date(this.inputValues[0]);
        var result = inputDate.toLocaleString();

        this.callDetailService.findAllByStartdateGreaterThanEqualAndOrganization(result,this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Call Duration (s) Less')
      {
        this.callDetailService.findAllByCalldurationsecondsLessThanEqualAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Call Duration (s) Greator')
      {
        this.callDetailService.findAllByCalldurationsecondsGreaterThanEqualAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Call Duration (s) Greator, Extension')
      {
        this.callDetailService.findAllByCalldurationsecondsGreaterThanEqualAndCalleridAndOrganization(this.inputValues[0],this.inputValues[1],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Call Duration (s) Greator, Dialed Number')
      {
        this.callDetailService.findAllByCalldurationsecondsGreaterThanEqualAndCustomeridAndOrganization(this.inputValues[0],this.inputValues[1],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Call Duration (s) Greator, Is Conference')
      {
        this.callDetailService.findAllByCalldurationsecondsGreaterThanEqualAndIsconferenceAndOrganization(this.inputValues[0],this.inputValues[1],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Call Duration (s) Greator, Phone Context')
      {
        this.callDetailService.findAllByCalldurationsecondsGreaterThanEqualAndPhoneContextAndOrganization(this.inputValues[0],this.inputValues[1],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Call Duration (s) Greator, Timezone')
      {
        this.callDetailService.findAllByCalldurationsecondsGreaterThanEqualAndTimezoneAndOrganization(this.inputValues[0],this.inputValues[1],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Call Duration (s) Less, Extension')
      {
        this.callDetailService.findAllByCalldurationsecondsLessThanEqualAndCalleridAndOrganization(this.inputValues[0],this.inputValues[1],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Call Duration (s) Less, Timezone')
      {
        this.callDetailService.findAllByCalldurationsecondsLessThanEqualAndTimezoneAndOrganization(this.inputValues[0],this.inputValues[1],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Call Duration (s) Less, Phone Context')
      {
        this.callDetailService.findAllByCalldurationsecondsLessThanEqualAndPhoneContextAndOrganization(this.inputValues[0],this.inputValues[1],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Call Duration (s) Less, Is Conference')
      {
        this.callDetailService.findAllByCalldurationsecondsLessThanEqualAndIsconferenceAndOrganization(this.inputValues[0],this.inputValues[1],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });    
      }
  else if(this.currentDropDownOption.label == 'Call On Mobile, Is Conference')
      {
        this.callDetailService.findAllByCallonmobileAndIsconferenceAndOrganization(this.inputValues[0],this.inputValues[1],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  else if(this.currentDropDownOption.label == 'Call Duration (s) Less, Dialed Number')
      {
        this.callDetailService.findAllByCalldurationsecondsLessThanEqualAndCustomeridAndOrganization(this.inputValues[0],this.inputValues[1],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  }
  
  dropDownChange(event)
  {
    //console.log("Drop Down Button changed");

    if(this.currentDropDownOption != null)
      {
        this.previousDropDownOption = this.currentDropDownOption;
      }

    this.currentDropDownOption = this.dropDownOptions[event];
    this.stepTwoNextButton = false;
  }

  // onTagRemove(tagToRemove: NbTagComponent): void {
  //       this.trees = this.trees.filter(t => t !== tagToRemove.text);
  //     }


  onDeleteConfirm(event): void {
    console.log("User Deleted a row. Row data is  : ");
    //console.log(event);
  }

  onSaveConfirm(event): void {

    //console.log("User Edited a row. Row data is  : ");
    //console.log(event);
    //console.log(event);

  }

  onCreateConfirm(event): void {
    //console.log("User Created a row. Row data is  : ");
    //console.log(event);
  }

 

  onUserRowSelect(event): void {
   // console.log("User Selected a row. Row data is  : ");
    //console.log(event);
    this.currentRecord = JSON.parse(JSON.stringify(event.data));
    var columns = JSON.parse(JSON.stringify(this.settings.columns));
    
    //console.log(JSON.stringify(this.settings.columns));
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

  }


  getChildData(event){
    //console.log("Event from child");
    // if(event == 'Convertedbuttonchanged')
    //   {
    //     this.setTable();
    //     this.setInMemoryData();
    //   }
  }

  export() {
  this.exportAsConfig.type = this.downloadAs;
    this.exportAsService
      .save(this.exportAsConfig, 'CallDetails')
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

 stepInputChange(event)
  {
    console.log('stepInputChange');
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
    console.log('date value changed');
    var track = 0;

    for (let i = 0; i < this.inputs.length; i++) {
      console.log(this.types[i]);
      console.log(this.inputValues[i]);
      if(this.types[i] == 'boolean')
      {
        console.log('valueChanged : boolean');
      }
      else{
        if(this.inputValues[i] == '' || this.inputValues[i] == ' ' || this.inputValues[i] == null)
        {
          console.log('valueChanged : Not boolean');
          if(track==0)
          { // Do Nothing
            console.log('track0');
          }
          else{
            console.log('valueChanged : Track is on2');
            track = 1;
            this.stepThreeNextButton = true;
            break;
          } 
        }
      }

      console.log('Setting to false');
      this.stepThreeNextButton = false;
    }
  }


  
  onChange() {
    // console.log('onChange');
    if(this.input.nativeElement.value=='' || this.input.nativeElement.value==' ')
    {
      this.messageListDataService.filteredCallOptions$ = of(this.messageListDataService.allEmployeesData);
    }
    else{
      this.messageListDataService.filteredCallOptions$ = this.messageListDataService.getFilteredOptions(this.input.nativeElement.value);
    }
  }

onMultipleChange() {
    // console.log('onChange');
    if(this.input.nativeElement.value=='' || this.input.nativeElement.value==' ')
    {
      this.messageListDataService.multipleFilteredCallOptions$ = of(this.messageListDataService.allEmployeesData);
    }
    else{
      this.messageListDataService.multipleFilteredCallOptions$ = this.messageListDataService.getFilteredOptions(this.input.nativeElement.value);
    }
  }

  
onSelectionChange($event) {
  console.log('onSelectionChange');
  console.log($event);

  // this.callDetailService.findAllByCalleridAndOrganization($event,this.organization)
  // .pipe(takeUntil(this.destroy$))
  // .subscribe({
  //   next: allData => {
  //     if(allData == null)
  //     {
  //         this.allGraphRecords = [];
  //     }
  //     else
  //     {
  //         var arr = JSON.parse(JSON.stringify(allData));
  //         //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
  //         this.allGraphRecords = [...arr];
  //     }
  //     this.input.nativeElement.value = '';
  //   },
  //   error: err => {
  //    // console.log("Error : "+ JSON.stringify(err));
  //    this.input.nativeElement.value = '';
  //     this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
  //   }
  // });

}

onMultipleSelectionChange($event) {
  console.log('onSelectionChange');
  console.log($event);

  // this.callDetailService.findAllByCalleridAndOrganization($event,this.organization)
  // .pipe(takeUntil(this.destroy$))
  // .subscribe({
  //   next: allData => {
  //     if(allData == null)
  //     {
  //         this.allGraphRecords = [];
  //     }
  //     else
  //     {
  //         var arr = JSON.parse(JSON.stringify(allData));
  //         //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
  //         this.allGraphRecords = [...arr];
  //     }
  //     this.input.nativeElement.value = '';
  //   },
  //   error: err => {
  //    // console.log("Error : "+ JSON.stringify(err));
  //    this.input.nativeElement.value = '';
  //     this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
  //   }
  // });

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
    // setTimeout(()=>{
    //   this.messageListDataService.filteredCallOptions$ = of(this.messageListDataService.allEmployeesData);
    // },500);
  }

  setupNgOnInitData(){
 
    if(ConstantsService.user.role === ConstantsService.employee)
      {
        this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
      }
    else
      {
        this.setTable(); 
        this.setInMemoryData();
      } 

  }

  setInMemoryData(){
    this.callDetailService.findAllInMemoryDataByOrganization(ConstantsService.user.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (allData:any) => {
        if(allData != null)
        {
          this.inMemorySource.load(<any[]>allData); 
        }
      },
      error: err => {
       console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  private formatEventAt(v: any): string {
    if (v === null || v === undefined) return '';

    // LocalDate-like: { year, monthValue, dayOfMonth }
    if (typeof v === 'object' && v.year !== undefined && v.monthValue !== undefined && v.dayOfMonth !== undefined) {
      const pad = (n: number) => String(n).padStart(2, '0');
      const dd = pad(Number(v.dayOfMonth));
      const MM = pad(Number(v.monthValue));
      const yy = String(Number(v.year)).slice(-2);
      return `${dd}-${MM}-${yy}`; // no time available
    }

    let ms: number | null = null;

    // Java Instant-like: { epochSecond, nano }
    if (typeof v === 'object' && v.epochSecond !== undefined) {
      const sec = Number(v.epochSecond);
      const nano = Number(v.nano ?? 0);
      if (Number.isFinite(sec)) ms = sec * 1000 + Math.floor(nano / 1_000_000);
    }
    // raw number (epoch seconds or milliseconds)
    else if (typeof v === 'number' && Number.isFinite(v)) {
      ms = v < 10_000_000_000 ? v * 1000 : v;
    }

    if (ms === null) return '';

    const d = new Date(ms);
    const pad = (n: number) => String(n).padStart(2, '0');

    const dd = pad(d.getDate());
    const MM = pad(d.getMonth() + 1);
    const yy = String(d.getFullYear()).slice(-2);
    const HH = pad(d.getHours());
    const mm = pad(d.getMinutes());
    const ss = pad(d.getSeconds());

    return `${dd}-${MM}-${yy} ${HH}:${mm}:${ss}`;
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }
}
