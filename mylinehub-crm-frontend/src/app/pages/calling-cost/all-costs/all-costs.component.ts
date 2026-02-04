  import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
  import { CallingCostService } from './../service/calling-cost.service';
  import { delay, map, takeUntil } from 'rxjs/operators';
  import { Observable, Subject, of } from 'rxjs';
  import { LocalDataSource } from 'ng2-smart-table';
  import {
    ExportAsService,
    ExportAsConfig,
    SupportedExtensions,
  } from 'ngx-export-as';
  import { NbToastrService,NbThemeService,NbStepChangeEvent,NbComponentStatus, NbToastRef, NbDialogService } from '@nebular/theme';
  import { takeWhile } from 'rxjs/operators' ;
  import { Router } from '@angular/router';
  import { ConstantsService } from './../../../service/constants/constants.service';
import { MessageListDataService } from '../../../@theme/components/header/message-list/message-list-data-service/message-list-data.service';
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';
import { CustomInputTableComponent } from '../../employee/all-employees/custom-input-table/custom-input-table.component';
  
  interface CardSettings {
    title: string;
    iconClass: string;
    type: string;
  }
  
  
  @Component({
    selector: 'ngx-all-costs',
    templateUrl: './all-costs.component.html',
    styleUrls: ['./all-costs.component.scss']
  })
  export class AllCostsComponent implements OnInit, OnDestroy {
    
    @ViewChild('autoEmployeeInput') input;
    @ViewChild('autoPageNumber') pageNumberInput;
    @ViewChild('searchName') searchTextInput;

    searchString:any = "";
    currentDropDownOption: any = null;
    currentPageNumber:number = 1;
    currentPageSize:number = 6;
    isSearchDone:boolean = false;
    previousDropDownOption:any = null;
    setSearchTextChangeId: any;
  
    single = [
      {
        name: 'Cost Records',
        value: 0,
      },
    ];
    totalPages:any = 50;
    pageOptionsAll = [];
    pageOptions$: Observable<number[]>;
    setPageNumberId: NodeJS.Timeout;


    tableHeading = 'All Costs';
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
        callID: {
          title: 'ID',
          type: 'number',
          editable: false,
          addable: false,
        },
        amount: {
          title: 'Amount',
          type: 'string',
        },
        extension: {
          title: 'Extension',
          type: 'string',
        },
        callcalculation: {
          title: 'Call Calculation',
          type: 'string',
        },
        remarks: {
          title: 'Remarks',
          type: 'custom',
          valuePrepareFunction: (cell,row) => {
                  // DATA FROM HERE GOES TO renderComponent
                  console.log('valuePrepareFunction');
                  console.log(cell);
                  return cell;
          },
          renderComponent: CustomInputTableComponent,
        },
        date: {
          title: 'Date',
          type: 'date',
        },
        organization: {
          title: 'Organization',
          type: 'string',
          hide:true,
        },
      },
    };

    
    radioOptions = [
      { value: '1-Column Search', label: '1-Property Search', checked: true },
      { value: '2-Column Search', label: '2-Property Search', disabled: false  },
      { value: '3-Column Search', label: '3-Property Search', disabled: true },
    ];
  
    radioOption;
  
    allDropDownOptions = [
      { column: 1, label: 'Call Calculation', properties:['callcalculation']},
      { column: 1, label: 'Extension', properties:['extension']},
      { column: 1, label: 'Amount Greator Equal', properties:['amount']},
      { column: 1, label: 'Amount Less Equal', properties:['amount']},
      { column: 2, label: 'Amount Less Equal , Call Calculation', properties:['amount','callcalculation']},
      { column: 2, label: 'Amount Greator Equal, Call Calculation', properties:['amount','callcalculation']},
    ];
  
    dropDownOptions = [
       { column: 1, label: 'Call Calculation', properties:['callcalculation']},
       { column: 1, label: 'Extension', properties:['extension']},
      { column: 1, label: 'Amount Greator Equal', properties:['amount']},
      { column: 1, label: 'Amount Less Equal', properties:['amount']},
    ];

    dropDownOption;
  
    source: LocalDataSource = new LocalDataSource();

   exportAsConfig: ExportAsConfig = {
      type: 'xlsx', // the type you want to download
      elementIdOrContent: 'lastTable', // the id of html/table element
    };

    constructor(private callingCostService : CallingCostService,
                private exportAsService: ExportAsService,
                private themeService: NbThemeService,
                private nbToastrService:NbToastrService,
                protected router: Router,
                protected constantService : ConstantsService,
                private dialogService: NbDialogService,
                protected messageListDataService:MessageListDataService) {
  
                  //console.log("I am in constructor");
  
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
            },2500);
          }
        else{
            clearTimeout(this.setSearchTextChangeId);
            this.setSearchTextChangeId = setTimeout(()=>{
                this.currentPageNumber = 1;
                 this.setTableAsPerSearch();
            },2500);
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

  setAnyKindTableData(allData:any)
  {

    if(this.currentPageNumber == 1)
      {
          this.totalPages = allData.numberOfPages;
          let output = [
            {
              name: 'All Cost Records',
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
  


    onSelectionChange($event) {
        console.log('onSelectionChange');
        console.log($event);

        // this.callingCostService.getAllCallingCostOnExtensionAndOrganization($event,this.organization)
        //   .pipe(takeUntil(this.destroy$))
        //   .subscribe({
        //     next: allData => {
        //       if(allData == null)
        //       {
        //           this.allGraphRecords = [];
        //       }
        //       else
        //       {
        //           var arr = JSON.parse(JSON.stringify(allData));
        //           //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
        //           this.allGraphRecords = [...arr];
        //       }
        //        this.input.nativeElement.value = '';

        //     },
        //     error: err => {
        //      // console.log("Error : "+ JSON.stringify(err));
        //      this.input.nativeElement.value = '';
        //       //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        //     }
        //   });
      }
 

    onChange() {
        // console.log('onChange');
        if(this.input.nativeElement.value=='' || this.input.nativeElement.value==' ')
        {
          this.messageListDataService.filteredCostOptions$ = of(this.messageListDataService.allEmployeesData);
        }
        else{
          this.messageListDataService.filteredCostOptions$ = this.messageListDataService.getFilteredOptions(this.input.nativeElement.value);
        }
      }


    setTable()
    {
      this.callingCostService.getAllCallingCostOnOrganization(this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
            this.setAnyKindTableData(allData);
        },
        error: err => {
         // console.log("Error : "+ JSON.stringify(err));
          //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }
  
    
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
      this.showAction = false;
      this.showDetail = false;
  
    // console.log("On Search is clicked");
    //console.log("On Search is clicked");

      if(!this.isSearchDone)
        {
          this.isSearchDone = true;
        }

      console.log("On Search is clicked");

      if(this.currentDropDownOption.label == 'Call Calculation')
      {
        this.callingCostService.findAllByCallcalculationAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           // console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
      else if(this.currentDropDownOption.label == 'Extension')
        {
          this.callingCostService.getAllCallingCostOnExtensionAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: allData => {
              this.setAnyKindTableData(allData);
            },
            error: err => {
             // console.log("Error : "+ JSON.stringify(err));
              //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
            }
          });
        }
      else if(this.currentDropDownOption.label == 'Amount Greator Equal')
      {
        this.callingCostService.findAllByAmountGreaterThanEqualAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           // console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
      else if(this.currentDropDownOption.label == 'Amount Less Equal')
      {
        this.callingCostService.findAllByAmountLessThanEqualAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           // console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
      else if(this.currentDropDownOption.label == 'Amount Less Equal , Call Calculation')
      {
        this.callingCostService.findAllByAmountLessThanEqualAndCallcalculationAndOrganization(this.inputValues[0],this.inputValues[1],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           // console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
      else if(this.currentDropDownOption.label == 'Amount Greator Equal, Call Calculation')
      {
        this.callingCostService.findAllByAmountGreaterThanEqualAndCallcalculationAndOrganization(this.inputValues[0],this.inputValues[1],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            this.setAnyKindTableData(allData);
          },
          error: err => {
           // console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
        
      }
    }
    
    getChildData(event){
      //console.log("Event from child");
      //console.log(event);
    }
  
    onUserRowSelect(event): void {
      //console.log("User Selected a row. Row data is  : ");
      //console.log(event);
      this.currentRecord = JSON.parse(JSON.stringify(event.data));
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
  
    }
  
    
    export() {
    this.exportAsConfig.type = this.downloadAs;
    this.exportAsService
      .save(this.exportAsConfig, 'CallCost')
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
      // setTimeout(()=>{
      //   this.messageListDataService.filteredCostOptions$ = of(this.messageListDataService.allEmployeesData);
      // },3000);
    }

    setupNgOnInitData(){

      if(ConstantsService.user.role === ConstantsService.employee)
        {
          this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
        }
      else
        {
          this.setTable();  
        }
    }
  
    ngOnDestroy() {
      this.destroy$.next();
      this.destroy$.complete();
      if (this.themeSubscription) this.themeSubscription.unsubscribe();
    }
  }
  