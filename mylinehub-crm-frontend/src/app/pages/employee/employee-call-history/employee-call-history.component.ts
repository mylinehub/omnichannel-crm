
import { ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MessageListDataService } from '../../../@theme/components/header/message-list/message-list-data-service/message-list-data.service';
import { of, Subject, takeUntil } from 'rxjs';
import { NbDialogService, NbTagComponent, NbThemeService } from '@nebular/theme';
import { ConstantsService } from '../../../service/constants/constants.service';
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';
import { CallDetailService } from '../../call-detail/service/call-detail.service';
import { LocalDataSource } from 'ng2-smart-table';
import { ConvertedButtonComponent } from '../../call-detail/all-calls/converted-button/converted-button.component';
import { DescriptionInputComponent } from '../../call-detail/all-calls/description-input/description-input.component';
import { CustomInputTableComponent } from '../all-employees/custom-input-table/custom-input-table.component';

@Component({
  selector: 'ngx-employee-call-history',
  templateUrl: './employee-call-history.component.html',
  styleUrls: ['./employee-call-history.component.scss'],
})
export class EmployeeCallHistoryComponent implements OnInit {

  private destroy$: Subject<void> = new Subject<void>();
 
  extensions:any = [];
  selectedRange:any = 'Today';
  totalCalls:number = 20;
  incomingCalls:number = 5;
  outgoingCalls:number = 15;
  connectedCalls:number = 15;
  converted:number = 15;
  showDashboard:boolean = true;
 
  tableHeading = 'Call History';
  inMemoryTableHeading = 'In Memory Call History';
  searchString:any = "";
  currentPageNumber:number = 1;
  currentPageSize:number = 6;
  setSearchTextChangeId: any;
  totalPages:any = 50;
  allRecords:any = [];
  source: LocalDataSource = new LocalDataSource();
  inMemorySource: LocalDataSource = new LocalDataSource();

  screenWidth: number;
  screenHeight: number;
  dateSelectionSize = "large";

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
        title: 'Date',
        type: 'date',
      },
	  enddate: {
        title: 'End Date',
        type: 'date',
        hide: true,
      },
    callSessionId: {
        title: 'Session Id',
        type: 'date',
        hide: true,
      },
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
        title: 'Date',
        type: 'date',
      },
	  enddate: {
        title: 'End Date',
        type: 'date',
        hide: true,
      },
    callSessionId: {
        title: 'Session Id',
        type: 'date',
        hide: true,
      },
	}
  };
  
  constructor(protected messageListDataService:MessageListDataService,
    private cd: ChangeDetectorRef,
    private dialogService: NbDialogService,
    private callDetailService : CallDetailService,
  ) { 
    // console.log('constructor');
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnInit(): void {
    // console.log('ngOnInit');
    this.getScreenSize();

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
    this.extensions.unshift(ConstantsService.user.extension);
    this.setInMemoryData();
  }

  setInMemoryData()
  {
    this.callDetailService.findAllInMemoryDataByOrganizationAndExtension(ConstantsService.user.organization,ConstantsService.user.extension)
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
    this.setTable();
  }

  setTable()
  {
    this.callDetailService.findAllForEmployeeHistory(this.selectedRange,ConstantsService.user.extension,ConstantsService.user.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
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

  setAnyKindTableData(allData:any)
  {

    if(this.currentPageNumber == -1)
      {
        this.currentPageNumber = 1;
      }

    if(this.currentPageNumber == 1)
      {
          this.totalPages = allData.numberOfPages;
      }

       this.source.load(<any[]>allData.data); 
       let arr = JSON.parse(JSON.stringify(allData.data));
       this.allRecords = [...arr];
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

  refreshDashboard()
  {
    console.log('refreshDashboard');

    let data = {
                 dateRange: this.selectedRange,
                 extensions: this.extensions,
                 organization: ConstantsService.user.organization
              };

    this.callDetailService.getCallCountForDashboard(data)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (allData:any) => {
                // console.log(allData);

                if(allData != null)
                {
                  this.totalCalls = allData.totalCalls;
                  this.incomingCalls = allData.incomingCalls;
                  this.outgoingCalls = allData.outgoingCalls;
                  this.converted = allData.converted;
                  this.connectedCalls = allData.callsConnected;
                }
                
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
  }

  updateSelectedRangeValue(value): void {
    // console.log('updateSelectedRangeValue');
    // console.log('value : '+value);
    this.selectedRange = String(value);
    // console.log('this.selectedRange : '+this.selectedRange);
    this.cd.markForCheck();
    this.refreshDashboard();
    this.setTableAsPerSearch();
  }

  // getChildData(event){
  //   console.log('getChildData event : '+event);
  //   if(event == 'Convertedbuttonchanged')
  //   {
  //     this.setTableAsPerSearch();
  //     this.setInMemoryData();
  //   }
  // }

  getScreenSize() {
    this.screenWidth = window.innerWidth;
    this.screenHeight = window.innerHeight;

    console.log("this.screenWidth : "+this.screenWidth);

    if(this.screenWidth<700){
      console.log("changing values");
       this.dateSelectionSize = "small";
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
