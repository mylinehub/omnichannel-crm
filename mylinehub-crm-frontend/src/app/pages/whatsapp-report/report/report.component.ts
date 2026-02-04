import { ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { of, Subject, takeUntil } from 'rxjs';
import { NbDialogService, NbTagComponent, NbThemeService } from '@nebular/theme';
import { ConstantsService } from '../../../service/constants/constants.service';
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';
import { OrganizationService } from '../../../service/organization/organization.service';
import { Router } from '@angular/router';
import { WhatsappReportDataServiceService } from '../data-service/whatsapp-report-data-service.service';
import { WhatsappNumberService } from '../../../service/whatsapp-number/whatsapp-number.service';
import { WhatsappReportService } from '../../../service/whatsapp-report/whatsapp-report.service';

@Component({
  selector: 'ngx-report',
  templateUrl: './report.component.html',
  styleUrls: ['./report.component.scss']
})
export class ReportComponent implements OnInit,OnDestroy {

  private destroy$: Subject<void> = new Subject<void>();
  @ViewChild('autoMultipleNumberInput') inputMultiple;

  //Data Variables
  whatsAppSupportlink = "https://wa.me/919625048379";
  phoneNumbers:any = [];
  trees:any = [];
  selectedRange:any = 'Today';
  selectedStatsType:any = 'Engagement';
  showDashboard:boolean = false;
  allPhoneData:any =[];
  organizationString:any ='';
  organizationObject:any = null;
  reportCountForDashboardForNumberByTimeData:any = null;
  reportCountForDashboardForNumberData: any = null;

  //Theme Variables
  colorScheme: any;
  view :any = [600, 200];
  gradient: boolean = false;
  private alive = true;
  redirectDelay: number = 0;
  themeSubscription: any;
  color:any=[];
  colorAll:any;
  setLastEventNumberID:any= null;
  eCharts:any = null;
  colors:any = null;

  //Responsiveness Variables
  screenWidth: number;
  screenHeight: number;
  dateSelectionSize = "large";
  statsTypeSelectionSize = "small";
  tagSetSize = "large";

  //Stats Variables
  totalAmount:number = 5875476456757;
  totalSpend:number = 685767858678;
  lastRecharged:string = "25-Nov-2025";

  totalMessagesReceived:number = 345345345315;
  totalMediaSizeSendMB:number = 56456;
  totalAmountSpend:number = 23423;
  totalTokenReceived:number = 364356;
  totalTokenSend:number = 234234;


  totalMessagesSend:number = 23453453453450;
  aiMessagesSend:number = 5345345345;
  campaignMessagesSend:number = 135345;
  manualMessagesSend:number = 15345345;


  totalMessagesDelivered:number = 2344333;
  aiMessagesDelivered:number = 4433332;
  campaignMessagesDelivered:number = 44343534;
  manualMessagesDelivered:number = 346363456;

  totalMessagesRead:number = 322344;
  aiMessagesRead:number = 234322;
  campaignMessagesRead:number = 23322;
  manualMessagesRead:number = 112345;

  totalMessagesFailed:number = 20;
  aiMessagesFailed:number = 10000;
  campaignMessagesFailed:number = 500000;
  manualMessagesFailed:number = 333322;

  totalMessagesDeleted:number = 9;
  aiMessagesDeleted:number = 1000;
  campaignMessagesDeleted:number = 99;
  manualMessagesDeleted:number = 1000;

  //Graph Variables
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


  constructor(protected whatsappReportDataServiceService:WhatsappReportDataServiceService,
    private cd: ChangeDetectorRef,
    private constantService: ConstantsService,
    private dialogService: NbDialogService,
    private whatsappNumberService:WhatsappNumberService,
    private organizationService:OrganizationService,
    private themeService: NbThemeService,
    protected router: Router,
    private whatsappReportService:WhatsappReportService
  ) { 
    // console.log('constructor');

    //Set support number 
    this.whatsAppSupportlink = ConstantsService.whatsAppSupportlink;

    if(localStorage.getItem("organization")!=null)
                   {
                    this.organizationString = localStorage.getItem("organization");
                   }
                   else{
                    
                    setTimeout(() => {
                      //   console.log('Routing to dashboard page');
                         return this.router.navigateByUrl(this.constantService.LOGIN_ENDPOINT);
                       }, this.redirectDelay);
                  }
  }

  ngOnDestroy() {
  this.destroy$.next();
  this.destroy$.complete();
  if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }
 

  ngOnInit(): void {
    // console.log('ngOnInit');

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

  //Sets all phone dropdown
  setupNgOnInitData(){

    this.getScreenSize();

    if(ConstantsService.user.role === ConstantsService.employee)
      {
        this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
       }
    else
      {

        this.themeSubscription = this.themeService.getJsTheme().subscribe(config => {

          const colors = config.variables;
          // console.log("Print colors from variable");
          // console.log("colors : " + JSON.stringify(colors));
          this.colorAll = [colors.warningLight, colors.infoLight, colors.dangerLight, colors.successLight, colors.primaryLight,'#ffcc99','#ffb3ff','#99bfd9'];
          const echarts: any = config.variables.echarts;
          this.eCharts = echarts;
          this.colors = colors;
        
        });

        this.showDashboard = true;

        this.whatsappNumberService.getAllByOrganization(this.organizationString)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (allData:any) => {
                // console.log(allData);

                if(allData != null)
                {
                    console.log('Setting initial value of this.allPhoneData');
                    this.allPhoneData = JSON.parse(JSON.stringify(allData));
                    this.whatsappReportDataServiceService.filteredOptions$ = of(this.allPhoneData);
                }
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
  }

  private formatEpochMillis(ms: any): string {
    if (ms === null || ms === undefined) return '-';
    const n = Number(ms);
    if (!Number.isFinite(n) || n <= 0) return '-';
    const d = new Date(n);
    const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    const dd = String(d.getDate()).padStart(2, '0');
    return `${dd}-${months[d.getMonth()]}-${d.getFullYear()}`;
  }


  refreshDashboard()
  {
    console.log('refreshDashboard');

     // Get first 3 variables for stats
     //Intakes only organization and is only dependent on it
    this.organizationService.getOrganizationalData(this.organizationString)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (allData:any) => {
                // console.log(allData);

                if(allData != null)
                {
                  this.organizationObject = allData;
                  this.totalAmount = allData.totalWhatsAppMessagesAmount;
                  this.totalSpend = allData.totalWhatsAppMessagesAmountSpend;
                  this.lastRecharged = this.formatEpochMillis(allData.lastRechargedOn);
                }
                
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });


    let data = {
                 dateRange: this.selectedRange,
                 whatsAppPhoneNumbers: this.phoneNumbers,
                 organization: ConstantsService.user.organization
              };

    // Get remaining 35 variables for stats
    // Intakes whats app support numbers list and selected Range String
    this.whatsappReportService.getReportCountForDashboard(data)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (allData:any) => {
                // console.log(allData);

                if(allData != null)
                {
                  this.totalMessagesReceived = allData.totalMessagesReceived;
                  this.totalMediaSizeSendMB = allData.totalMediaSizeSendMB;
                  this.totalAmountSpend = allData.totalAmountSpend;
                  this.totalTokenReceived = allData.totalTokenReceived;
                  this.totalTokenSend = allData.totalTokenSend;


                  this.totalMessagesSend = allData.totalMessagesSend;
                  this.aiMessagesSend = allData.aiMessagesSend;
                  this.campaignMessagesSend = allData.campaignMessagesSend;
                  this.manualMessagesSend = allData.manualMessagesSend;


                  this.totalMessagesDelivered = allData.totalMessagesDelivered;
                  this.aiMessagesDelivered = allData.aiMessagesDelivered;
                  this.campaignMessagesDelivered = allData.campaignMessagesDelivered;
                  this.manualMessagesDelivered = allData.manualMessagesDelivered;

                  this.totalMessagesRead = allData.totalMessagesRead;
                  this.aiMessagesRead = allData.aiMessagesRead;
                  this.campaignMessagesRead = allData.campaignMessagesRead;
                  this.manualMessagesRead = allData.manualMessagesRead;

                  this.totalMessagesFailed = allData.totalMessagesFailed;
                  this.aiMessagesFailed = allData.aiMessagesFailed;
                  this.campaignMessagesFailed = allData.campaignMessagesFailed;
                  this.manualMessagesFailed = allData.manualMessagesFailed;

                  this.totalMessagesDeleted = allData.totalMessagesDeleted;
                  this.aiMessagesDeleted = allData.aiMessagesDeleted;
                  this.campaignMessagesDeleted = allData.campaignMessagesDeleted;
                  this.manualMessagesDeleted = allData.manualMessagesDeleted;
                }
                
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });

    this.refreshDashboardGraphs();
    
  }

  refreshDashboardGraphs(){

    let data = {
                 dateRange: this.selectedRange,
                 whatsAppPhoneNumbers: this.phoneNumbers,
                 organization: ConstantsService.user.organization
              };

     this.whatsappReportService.getReportCountForDashboardForNumber(data)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {

                console.log("Initial data for getReportCountForDashboardForNumber");
                console.log(allData);
                
                console.log("this.selectedStatsType : "+this.selectedStatsType);

                this.reportCountForDashboardForNumberData = JSON.parse(JSON.stringify(allData));
                console.log("cleaning pie & bar chart previous data")
                this.ngxPieLegend = [];
                this.ngxPieData = [];
                this.ngxBarLegend= [];
                this.ngxBarData = [];

                if(this.reportCountForDashboardForNumberData.length > 0)
                {
                  // console.log("setting new pie chart data");
                  let i = 0;
                  this.reportCountForDashboardForNumberData.forEach((element) => {
                    // console.log(JSON.stringify(element));
                    if(i<5)
                    {

                      this.ngxPieLegend.unshift(String(element.phoneNumberMain));
                      this.ngxBarLegend.unshift(String(element.phoneNumberMain));

                      if(this.selectedStatsType === "Engagement"){
                        this.ngxPieData.unshift({ value: element.totalPhoneNumberWith, name: String(element.phoneNumberMain) })
                        this.ngxBarData.unshift(Number(element.totalPhoneNumberWith));
                      }
                      else if(this.selectedStatsType === "Inbound"){
                        this.ngxPieData.unshift({ value: element.totalMessagesReceived, name: String(element.phoneNumberMain) })
                        this.ngxBarData.unshift(Number(element.totalMessagesReceived));
                      }
                      else if(this.selectedStatsType === "Outbound"){
                        this.ngxPieData.unshift({ value: element.totalMessagesDelivered, name: String(element.phoneNumberMain) })
                        this.ngxBarData.unshift(Number(element.totalMessagesDelivered));
                      }
                      else{
                        console.log("system should never reach here")
                        this.showDialoge('Error','activity-outline','danger', "Connect admin. Stats type not supported : "+this.selectedStatsType); 
                      }
                    }
                    i=i+1;
                  });

                  console.log(this.ngxPieLegend);
                  console.log(this.ngxPieData);
                  console.log(this.ngxBarLegend);
                  console.log(this.ngxBarData);
                }
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });

    
    this.whatsappReportService.getReportCountForDashboardForNumberByTime(data)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
                console.log("getReportCountForDashboardForNumberByTime");
                console.log(allData);

                this.reportCountForDashboardForNumberByTimeData = JSON.parse(JSON.stringify(allData));
                let monthYear = [];
                let i = 0;

                console.log("cleaning line & area chart previous data")
                this.ngxLineLegend = [];
                this.ngxLineXAxisData = [];
                this.ngxLineData = [];
                this.ngxAreaStackLegend = [];
                this.ngxAreaStackXAxisData = [];
                this.ngxAreaStackData = [];

                if(this.reportCountForDashboardForNumberByTimeData.length > 0)
                {
                    this.reportCountForDashboardForNumberByTimeData.forEach((element) => {
                      console.log("First loop");
                      console.log(JSON.stringify(element));

                      this.ngxLineLegend.unshift(String(element.phoneNumberMain));
                      this.ngxAreaStackLegend.unshift(String(element.phoneNumberMain));

                      

                      if(monthYear.length < element.messageDetails.length)
                      {
                        monthYear = [];
                        element.messageDetails.forEach((element) => {
                            monthYear.unshift(element.month+","+element.year);
                        });
                      }
                      i=i+1;
                    });

                  console.log('this.ngxLineLegend : '+JSON.stringify(this.ngxLineLegend));
                  console.log('this.ngxAreaStackLegend : '+JSON.stringify(this.ngxAreaStackLegend));
                  console.log('monthYear'+JSON.stringify(monthYear));

                  monthYear = [... monthYear.reverse()];

                  this.ngxLineXAxisData = [... monthYear];
                  this.ngxAreaStackXAxisData = [... monthYear];
                  this.ngxLineData = [];
                  this.ngxAreaStackData = [];
                  

                  i = 0;
                  this.reportCountForDashboardForNumberByTimeData.forEach((element) => {
                    // console.log("Second loop");

                    let lineData = JSON.parse(JSON.stringify({
                      name: String(element.phoneNumberMain),
                      type: 'line',
                      data: [],
                    }))

                    let stackType = "Engagement";
                    if(this.selectedStatsType === "Engagement"){
                      stackType = "Engagement";
                    }
                    else if(this.selectedStatsType === "Inbound"){
                      stackType = "Inbound";
                    }
                    else if(this.selectedStatsType === "Outbound"){
                      stackType = "Outbound";
                    }

                    const areaOpacity = this.eCharts?.areaOpacity ?? 0.15;
                    let areaStackData:any = JSON.parse(JSON.stringify({
                      name: String(element.phoneNumberMain),
                      type: 'line',
                      stack: stackType,
                      areaStyle: { normal: { opacity: areaOpacity } },
                      data: [],
                    }));


                    this.ngxLineXAxisData.forEach((value:any)=>{
                      // console.log('Inside monthyear loop to set data value 1');
                      lineData.data.unshift(1);
                      areaStackData.data.unshift(0);
                    });
                    console.log('********* After unshift one *********');
                    console.log('lineData'+JSON.stringify(lineData));
                    console.log('areaStackData'+JSON.stringify(areaStackData));
                    console.log('********* Before lst load *********');

                    if(element.messageDetails.length > 0)
                    {
                      element.messageDetails.forEach((value)=>{
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
                          console.log("dataIndex is not -1");
                          console.log("lineData.data[dataIndex] : ",lineData.data[dataIndex]);
                          console.log("areaStackData.data[dataIndex] : ",areaStackData.data[dataIndex]);
                          
                          if(this.selectedStatsType === "Engagement"){
                             lineData.data[dataIndex] = value.totalPhoneNumberWith;
                            areaStackData.data[dataIndex] = value.totalPhoneNumberWith;
                          }
                          else if(this.selectedStatsType === "Inbound"){
                             lineData.data[dataIndex] = value.totalMessagesReceived;
                             areaStackData.data[dataIndex] = value.totalMessagesReceived;
                          }
                          else if(this.selectedStatsType === "Outbound"){
                            lineData.data[dataIndex] = value.totalMessagesDelivered;
                            areaStackData.data[dataIndex] = value.totalMessagesDelivered;
                          }


                          console.log("after setting new value");
                          console.log("lineData.data[dataIndex] : ",lineData.data[dataIndex]);
                          console.log("areaStackData.data[dataIndex] : ",areaStackData.data[dataIndex]);
                        }
                      });
                    }

                    this.ngxLineData.unshift(lineData);
                    this.ngxAreaStackData.unshift(areaStackData);

                    i=i+1;
                  });

                  console.log('ngxLineData'+JSON.stringify(this.ngxLineData));
                  console.log('ngxAreaStackData'+JSON.stringify(this.ngxAreaStackData));
                }
          },
          error: err => {
           console.log("Error : "+ JSON.stringify(err));
            // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });

  }

  //When search of input changes
  onMultipleChange() {
    console.log('Input search text changed : onMultipleChange');
    console.log('Reset filter to all');
      let filteredData = this.allPhoneData;

      if(this.trees.length !=0 )
      {
        this.trees.forEach((element) => {
          //console.log(JSON.stringify(element));
          let valueToSearch = String(element).trim();
          filteredData = this.filter(valueToSearch,filteredData);
        });
      }

    console.log('already added tree value filtered');

    if(this.inputMultiple.nativeElement.value=='' || this.inputMultiple.nativeElement.value==' ')
    {  
      //Do nothing
    }
    else{
      filteredData = this.filter(this.inputMultiple.nativeElement.value,filteredData);
    }

    console.log('search text value filtered');
    this.whatsappReportDataServiceService.filteredOptions$ = of(filteredData);  
  }

  //New value selected from drop down
  onMultipleSelectionChange(value:any) {
    console.log('onMultipleSelectionChange');
    console.log(value);
    console.log('selectedValue : '+value);

    if(this.trees.length < 5)
    {
      console.log("Tree length less than 5");

      let allPhoneNumberIndex = -1;
      allPhoneNumberIndex = this.allPhoneData.findIndex(obj => obj.phoneNumber == value);
      console.log("allPhoneNumberIndex : ",allPhoneNumberIndex);
      
      console.log('Adding value to tree and phone numbers');
      if(allPhoneNumberIndex != -1)
      {
        this.phoneNumbers.unshift(value);
        this.trees.unshift(value);
  
        console.log('tree : '+this.trees);
        console.log('phoneNumbers : '+this.phoneNumbers);
        this.refreshDashboard();
      }
  
      this.resetSupportPhoneFilterValue();
  
    }
    else{
      this.showDialoge('No-More','activity-outline','danger', "You can compare maximum 5 support numbers");
    }
    
    console.log("clearing input search value");
    this.inputMultiple.nativeElement.value='';
    
  }

  // One value removed from tree
  onTagRemove(tagToRemove: NbTagComponent): void {
        console.log('onTagRemove');
        console.log('tagToRemove : '+tagToRemove.text);
        let currentValueIndex = this.trees.findIndex(obj => obj == tagToRemove.text);
        console.log('currentValueIndex : '+currentValueIndex);
        if(currentValueIndex  != -1)
        {
          this.phoneNumbers.splice(currentValueIndex, 1);
          this.trees.splice(currentValueIndex, 1);
          this.refreshDashboard();
        }
        console.log('tree : '+this.trees);
        console.log('phoneNumbers : '+this.phoneNumbers);

        this.resetSupportPhoneFilterValue();
  }
  
  // this function is used in above both tag remove and multiple selection changed
  resetSupportPhoneFilterValue(){
      //Reset filtered Options
      console.log('resetSupportPhoneFilterValue: First reset filter to all');
      let filteredData = this.allPhoneData;
      
      console.log('this.trees.length : '+this.trees.length);
  
      if(this.trees.length !=0 )
        {
          // console.log('starting tree loop');
            this.trees.forEach((element) => {
              // console.log("element :"+JSON.stringify(element));
              let valueToSearch = String(element);
              // console.log("valueToSearch :"+JSON.stringify(valueToSearch))
              filteredData = this.filter(valueToSearch,filteredData);
            });
        }
  
      console.log('Tree Values filtered');
      console.log('filteredData : '+JSON.stringify(filteredData));
      this.whatsappReportDataServiceService.filteredOptions$ = of(filteredData);
  }

  //Today,Yesterday,Week,Month,Quater,Year
  //Resfresh dashboards
  updateSelectedRangeValue(value): void {
    console.log('updateSelectedRangeValue');
    console.log('value : '+value);
    this.selectedRange = String(value);
    // console.log('this.selectedRange : '+this.selectedRange);
    this.cd.markForCheck();
    this.refreshDashboard();
  }

   //Engagement,Inbound,Outbound
   //Ony refresh graphs
  updateSelectedStatsValue(value): void{

    console.log('updateSelectedStatsValue');
    console.log('value : '+value);
    this.selectedStatsType = String(value);

    // console.log('this.selectedRange : '+this.selectedRange);
    this.cd.markForCheck();
    this.refreshDashboardGraphs();

  }
  
  //Used in functionals where from all phone we have to filter and remove search values
  private filter(value: string, data: any[]): any[] {
    const filterValue = value.toLowerCase();
    return data.filter(optionValue => !(optionValue.phoneNumber).toLowerCase().includes(filterValue));
  }


  //Set mobile responsivenss required using typescript using this function
  getScreenSize() {
    this.screenWidth = window.innerWidth;
    this.screenHeight = window.innerHeight;

    console.log("this.screenWidth : "+this.screenWidth);

    if(this.screenWidth<700){
       this.dateSelectionSize = "small";
       this.statsTypeSelectionSize = "tiny";
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

}

