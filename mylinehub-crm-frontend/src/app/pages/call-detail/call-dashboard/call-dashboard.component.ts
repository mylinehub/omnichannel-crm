import { ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MessageListDataService } from '../../../@theme/components/header/message-list/message-list-data-service/message-list-data.service';
import { of, Subject, takeUntil } from 'rxjs';
import { NbDialogService, NbTagComponent, NbThemeService } from '@nebular/theme';
import { ConstantsService } from '../../../service/constants/constants.service';
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';
import { CallDetailService } from '../service/call-detail.service';

@Component({
  selector: 'ngx-call-dashboard',
  templateUrl: './call-dashboard.component.html',
  styleUrls: ['./call-dashboard.component.scss']
})
export class CallDashboardComponent implements OnInit,OnDestroy {

  private destroy$: Subject<void> = new Subject<void>();
  @ViewChild('autoMultipleEmployeeInput') inputMultiple;

  extensions:any = [];
  trees:any = [];
  selectedRange:any = 'Today';
  totalAmount:number = 20;
  totalSpend:number = 20;
  totalCalls:number = 20;
  incomingCalls:number = 5;
  outgoingCalls:number = 15;
  connectedCalls:number = 15;
  converted:number = 15;
  showDashboard:boolean = false;
  allEmployeeData:any;

  themeSubscription: any;
  color:any=[];
  colorAll:any;
  setLastEventNumberID:any= null;
  eCharts:any = null;
  colors:any = null;

  ngxPieLegend:any = [];
  ngxPieData:any = [];
  ngxBarLegend:any = [];
  ngxBarData:any = [];
  ngxLineLegend:any = [];
  ngxLineData:any = [];
  ngxLineXAxisData:any = [];
  ngxAreaStackLegend:any = [];
  ngxAreaStackData:any = [];
  ngxAreaStackXAxisData:any = [];

  screenWidth: number;
  screenHeight: number;
  dateSelectionSize = "large";
  tagSetSize = "large";
  
  constructor(protected messageListDataService:MessageListDataService,
    private cd: ChangeDetectorRef,
    private constantsService: ConstantsService,
    private dialogService: NbDialogService,
    private callDetailService : CallDetailService,
    private theme: NbThemeService,
  ) { 
    // console.log('constructor');
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }

  ngOnInit(): void {
    console.log('ngOnInit');
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
    // As update Selected Range Value is called everytime initially when page is refreshed
    // So below is not required in ngOnIt.
    // this.refreshDashboard();
  }


  setupNgOnInitData(){

        if(ConstantsService.user.role === ConstantsService.employee)
      {
        this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
       }
    else
      {

        this.themeSubscription = this.theme.getJsTheme().subscribe(config => {

          const colors = config.variables;
          // console.log("Print colors from variable");
          // console.log("colors : " + JSON.stringify(colors));
          this.colorAll = [colors.warningLight, colors.infoLight, colors.dangerLight, colors.successLight, colors.primaryLight,'#ffcc99','#ffb3ff','#99bfd9'];
          const echarts: any = config.variables.echarts;
          this.eCharts = echarts;
          this.colors = colors;
        
        });

        this.showDashboard = true;
        this.allEmployeeData = [...(this.messageListDataService.allEmployeesData ?? [])];
        this.allEmployeeData.unshift(
                  {
                    id: ConstantsService.user.id,
                    firstName: ConstantsService.user.firstName,
                    lastName: ConstantsService.user.lastName,
                    role: ConstantsService.user.role,
                    departmentName: ConstantsService.user.departmentName,
                    phonenumber: ConstantsService.user.phonenumber,
                    extension: ConstantsService.user.extension,
                    email: ConstantsService.user.email,
                    pesel: ConstantsService.user.pesel,
                    birthdate: ConstantsService.user.birthdate,
                    iconImageData: ConstantsService.user.iconImageData,
                    iconImageByteData: ConstantsService.user.iconImageByteData,
                    imageType: ConstantsService.user.imageType,
                    state: ConstantsService.user.state,
                    presence: ConstantsService.user.presence,
                    dotClass: ConstantsService.user.dotClass,
                    channel: ConstantsService.user.channel,
                  }
        );

        this.messageListDataService.multipleFilteredCallOptions$ = of(this.allEmployeeData);

        
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
                  this.totalAmount = allData.totalAmount;
                  this.totalSpend = allData.totalSpend;
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


    this.callDetailService.getCallCountForDashboardForEmployee(data)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
                // console.log(allData);
                
                // console.log("cleaning chart previous data");
                this.ngxPieLegend = [];
                this.ngxPieData = [];
                this.ngxBarLegend = [];
                this.ngxBarData = [];

                if(JSON.parse(JSON.stringify(allData)).length > 0)
                {
                  // console.log("setting new pie chart data");
                  let i = 0;
                  JSON.parse(JSON.stringify(allData)).forEach((element) => {
                    // console.log(JSON.stringify(element));
                    if(i<8)
                    {
                      this.ngxPieLegend.unshift(String(element.firstName+"-"+element.extension));
                      this.ngxPieData.unshift({ value: element.totalCalls, name: String(element.firstName+"-"+element.extension) })
                      this.ngxBarLegend.unshift(String(element.firstName+"-"+element.extension));
                      this.ngxBarData.unshift(Number(element.totalCalls));
                    }
                    i=i+1;
                  });
                }
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });

    
    this.callDetailService.getCallCountForDashboardForEmployeeByTime(data)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
                // console.log(allData);

                let data = JSON.parse(JSON.stringify(allData));
                let monthYear = [];
                let i = 0;

                this.ngxLineLegend = [];
                this.ngxAreaStackLegend = [];
                this.ngxLineXAxisData = [];
                this.ngxAreaStackXAxisData = [];

                if(data.length > 0)
                {
                    data.forEach((element) => {
                      // console.log("First loop");
                      // console.log(JSON.stringify(element));
                      this.ngxLineLegend.unshift(element.firstName+"-"+element.extension);
                      this.ngxAreaStackLegend.unshift(element.firstName+"-"+element.extension);

                      if(monthYear.length < element.callDetails.length)
                      {
                        monthYear = [];
                        element.callDetails.forEach((element) => {
                            monthYear.unshift(element.month+","+element.year);
                        });
                      }
                      i=i+1;
                    });

                  // console.log('this.ngxLineLegend : '+JSON.stringify(this.ngxLineLegend));
                  // console.log('this.ngxAreaStackLegend : '+JSON.stringify(this.ngxAreaStackLegend));
                  // console.log('monthYear'+JSON.stringify(monthYear));

                  monthYear = [... monthYear.reverse()];

                  this.ngxLineXAxisData = [... monthYear];
                  this.ngxAreaStackXAxisData = [... monthYear];
                  // console.log('ngxLineXAxisData'+JSON.stringify(this.ngxLineXAxisData));
                  // console.log('ngxAreaStackXAxisData'+JSON.stringify(this.ngxAreaStackXAxisData));

                  this.ngxLineData = [];
                  this.ngxAreaStackData = [];

                  i = 0;
                  data.forEach((element) => {
                    // console.log("Second loop");

                    let lineData = JSON.parse(JSON.stringify({
                      name: element.firstName+"-"+element.extension,
                      type: 'line',
                      data: [],
                    }))

                    const areaOpacity = this.eCharts?.areaOpacity ?? 0.35;
                    let areaStackData:any = JSON.parse(JSON.stringify({
                      name: element.firstName+"-"+element.extension,
                      type: 'line',
                      stack: 'Total Calls',
                      areaStyle: { normal: { opacity: areaOpacity } },
                      data: [],
                    }));
                    
                    // if(i+1 == data.length)
                    // {
                    //   areaStackData =  JSON.parse(JSON.stringify({
                    //     name: element.firstName+"-"+element.extension,
                    //     type: 'line',
                    //     stack: 'Total calls',
                    //     label: {
                    //       normal: {
                    //         show: true,
                    //         position: 'top',
                    //         textStyle: {
                    //           color: this.eCharts.textColor,
                    //         },
                    //       },
                    //     },
                    //     areaStyle: { normal: { opacity: this.eCharts.areaOpacity } },
                    //     data: [],
                    //   }));
                    // }       
                    
                    // console.log('********* Before unshift one *********');
                    // console.log('lineData'+JSON.stringify(lineData));
                    // console.log('areaStackData'+JSON.stringify(areaStackData));

                    this.ngxLineXAxisData.forEach((value:any)=>{
                      // console.log('Inside monthyear loop to set data value 1');
                      lineData.data.unshift(1);
                      areaStackData.data.unshift(0);
                    });

                    // console.log('********* After unshift one *********');
                    // console.log('lineData'+JSON.stringify(lineData));
                    // console.log('areaStackData'+JSON.stringify(areaStackData));
                    // console.log('********* Before lst load *********');

                    if(element.callDetails.length > 0)
                    {
                      element.callDetails.forEach((value)=>{
                        let dataIndex = -1;
                        dataIndex = this.ngxLineXAxisData.findIndex(obj => {
                          // console.log("obj : ",JSON.stringify(obj));
                          // console.log("value : ",JSON.stringify(value));
                          let result = (obj === value.month+","+value.year);
                          // console.log("result : ",result);
                          return result;
                        });

                        // console.log("dataIndex : ",dataIndex);
                        
                        if(dataIndex != -1)
                        {
                          // console.log("dataIndex is not -1");
                          // console.log("lineData.data[dataIndex] : ",lineData.data[dataIndex]);
                          // console.log("areaStackData.data[dataIndex] : ",areaStackData.data[dataIndex]);
                          lineData.data[dataIndex] = value.totalCalls;
                          areaStackData.data[dataIndex] = value.totalCalls;

                          // console.log("after setting new value");
                          // console.log("lineData.data[dataIndex] : ",lineData.data[dataIndex]);
                          // console.log("areaStackData.data[dataIndex] : ",areaStackData.data[dataIndex]);
                        }
                      });
                    }

                    this.ngxLineData.unshift(lineData);
                    this.ngxAreaStackData.unshift(areaStackData);

                    i=i+1;
                  });

                  // console.log('ngxLineData'+JSON.stringify(this.ngxLineData));
                  // console.log('ngxAreaStackData'+JSON.stringify(this.ngxAreaStackData));
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
  }
  
  onTagRemove(tagToRemove: NbTagComponent): void {
        // console.log('onTagRemove');
        // console.log('tagToRemove : '+tagToRemove.text);
        let currentValueIndex = this.trees.findIndex(obj => obj == tagToRemove.text);
        // console.log('currentValueIndex : '+currentValueIndex);
        if(currentValueIndex  != -1)
        {
          this.extensions.splice(currentValueIndex, 1);
          this.trees.splice(currentValueIndex, 1);
          this.refreshDashboard();
        }
        // console.log('tree : '+this.trees);
        // console.log('extensions : '+this.extensions);

        let filteredData = this.allEmployeeData;

        if(this.trees.length !=0 )
          {
            this.trees.forEach((element) => {
              console.log(JSON.stringify(element));
              let valueToSearch = String(element).split('-')[1].trim();
              filteredData = this.filterSelectedExtensions(valueToSearch, filteredData);
            });
          }

        this.messageListDataService.multipleFilteredCallOptions$ = of(filteredData);  
      }
  
  onMultipleChange() {
    // console.log('onMultipleChange');
    if(this.inputMultiple.nativeElement.value=='' || this.inputMultiple.nativeElement.value==' ')
    {
      let filteredData = this.allEmployeeData;

      if(this.trees.length !=0 )
      {
        this.trees.forEach((element) => {
          //console.log(JSON.stringify(element));
          let valueToSearch = String(element).split('-')[1].trim();
          filteredData = this.filterSelectedExtensions(valueToSearch, filteredData);
        });
      }

      this.messageListDataService.multipleFilteredCallOptions$ = of(filteredData);
        
    }
    else{

      let filteredData = this.allEmployeeData;

      if(this.trees.length !=0 )
      {
        this.trees.forEach((element) => {
          //console.log(JSON.stringify(element));
          let valueToSearch = String(element).split('-')[1].trim();
          filteredData = this.filterSelectedExtensions(valueToSearch, filteredData);
        });
      }

      filteredData = this.filterSearch(this.inputMultiple.nativeElement.value,filteredData);
      this.messageListDataService.multipleFilteredCallOptions$ = of(filteredData);
    }
  }

  private filterSearch(value: string, data: any[]): any[] {
    const filterValue = (value ?? '').toLowerCase();
    return data.filter(opt =>
      (`${opt.firstName ?? ''} ${opt.lastName ?? ''} ${opt.extension ?? ''}`).toLowerCase().includes(filterValue)
    );
  }

  private filterSelectedExtensions(selectedExt: string, data: any[]): any[] {
    const ext = String(selectedExt ?? '').trim();
    return data.filter(opt => String(opt.extension ?? '').trim() !== ext);
  }

  private filter(value: string, data:any): string[] {
    const filterValue = value.toLowerCase();
    return data.filter(optionValue => !(optionValue.firstName+' '+optionValue.lastName+' '+ optionValue.extension).toLowerCase().includes(filterValue));
  }

  
  onMultipleSelectionChange(value:any) {
    // console.log('onMultipleSelectionChange');
    console.log(value);
    // console.log('selectedValue : '+value);

    if(this.trees.length < 8)
    {
      let allEmployeeIndex = -1;
      allEmployeeIndex = this.messageListDataService.allEmployeesData.findIndex(obj => obj.extension == value);
      // console.log("Index : ",allEmployeeIndex);
      
      if(allEmployeeIndex != -1)
      {
        let employee = this.messageListDataService.allEmployeesData[allEmployeeIndex];
        let currentValue = `${employee.firstName}-${value}`;
        this.extensions.unshift(value);
        this.trees.unshift(currentValue);
  
        // console.log('tree : '+this.trees);
        // console.log('extensions : '+this.extensions);
        this.refreshDashboard();
      }
  
      //Reset filtered Options
      let filteredData = this.allEmployeeData;
      
      // console.log('this.trees.length : '+this.trees.length);
  
      if(this.trees.length !=0 )
        {
          // console.log('starting tree loop');
            let i =0;
            this.trees.forEach((element) => {
              // console.log("element :"+JSON.stringify(element));
              let valueToSearch = String(element).split('-')[1].trim();
              // console.log("valueToSearch :"+JSON.stringify(valueToSearch))
              filteredData = this.filterSelectedExtensions(valueToSearch, filteredData);
            });
        }
  
      // console.log('filteredData : '+JSON.stringify(filteredData));
      this.messageListDataService.multipleFilteredCallOptions$ = of(filteredData);
      // console.log(JSON.stringify(this.messageListDataService.multipleFilteredCallOptions$));
  
    }
    else{
      this.showDialoge('No-More','activity-outline','danger', "You can compare maximum 8 employees");
    }
    
    this.inputMultiple.nativeElement.value='';
    
  }

  getScreenSize() {
    this.screenWidth = window.innerWidth;
    this.screenHeight = window.innerHeight;

    console.log("this.screenWidth : "+this.screenWidth);

    if(this.screenWidth<700){
      console.log("changing values");
       this.dateSelectionSize = "small";
       this.tagSetSize = "small";
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

  //   async waitForLoggedInUserCondition(): Promise<void> {
  //   while (ConstantsService.user.firstName === undefined) {
  //     console.log('First name of logged in user not found. Would need to wait for 2000ms...');
  //     // Simulate some asynchronous operation that eventually sets the condition
  //     await sleep(2000); // Wait for 1 second before rechecking
  //   }
  //   console.log('Found Constant User Now');
  // }

}

// async function sleep(ms: number): Promise<void> {
//   console.log("Sleeping for ms : "+ms);
//   return new Promise((resolve) => setTimeout(resolve, ms));
// }
