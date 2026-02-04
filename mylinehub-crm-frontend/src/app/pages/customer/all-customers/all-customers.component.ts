import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CustomerService } from './../service/customer.service';
import { CampaignService } from './../../campaign/service/campaign.service';
import { takeUntil } from 'rxjs/operators';
import { Observable, Subject, of } from 'rxjs';
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
import { ProductService } from '../../product/service/product.service';
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';
import { CustomInputTableComponent } from '../../employee/all-employees/custom-input-table/custom-input-table.component';

interface CardSettings {
  title: string;
  iconClass: string;
  type: string;
}


@Component({
  selector: 'ngx-all-customers',
  templateUrl: './all-customers.component.html',
  styleUrls: ['./all-customers.component.scss']
})
export class AllCustomersComponent implements OnInit, OnDestroy {
  
  @ViewChild('autoPageNumber') pageNumberInput;
  @ViewChild('searchName') searchTextInput;

  searchString:any = "";
  currentDropDownOption: any = null;
  currentPageNumber:number = 1;
  currentPageSize:number = 6;
  isSearchDone:boolean = false;
  previousDropDownOption:any = null;
  setSearchTextChangeId: any;

  allScheduleType:string[] = [];
  scheduleType:string ="";

  single = [
    {
      name: 'Converted',
      value: 0,
    },
    {
       name: 'Diverted',
      value: 0,
    },
  ];

  totalPages:any = 50;
  pageOptionsAll = [];
  pageOptions$: Observable<number[]>;
  setPageNumberId: NodeJS.Timeout;


  tableHeading = 'All Customers';
  campaignHeading = "Associated Campaigns";
  private destroy$: Subject<void> = new Subject<void>();
  downloadAs: SupportedExtensions = 'png';
  downloadAsCampaign: SupportedExtensions = 'png';
  campaignSource: LocalDataSource = new LocalDataSource();
  isAlreadyScheduled = 'Already Scheduled (Overwrite it)';
  

  exportAsConfigCampaign: ExportAsConfig = {
    type: 'xlsx', // the type you want to download
    elementIdOrContent: 'campaignTable', // the id of html/table element
  };

  file: File = null;

  
  stepTwoNextButton = true;
  stepThreeNextButton = true;

  currentRecord:any = ' ';
  previousRecord:any = ' ';
  allRecords:any = [];
  // currentImage : any;

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

  removeScheduledCard: CardSettings = {
    title: 'Remove Job',
    iconClass: 'nb-close',
    type: 'primary',
  };

  scheduleAFixedDateCard: CardSettings = {
    title: 'At Fix Date',
    iconClass: 'nb-locked',
    type: 'primary',
  };

  scheduleAfterNSecCard: CardSettings = {
    title: 'After N Seconds',
    iconClass: 'nb-maximize',
    type: 'primary',
  };

  scheduleCronCard: CardSettings = {
    title: 'Cron',
    iconClass: 'nb-loop-circled',
    type: 'primary',
  };

  scheduleStatusCardsSet: CardSettings[] = [
    this.removeScheduledCard,
    this.scheduleAfterNSecCard,
    this.scheduleAFixedDateCard,
    this.scheduleCronCard,
   // this.coffeeMakerCard,
  ];

  scheduleCards: string;
  // selectedCallTypeModel:any= "";
  allAutodialerTypes:any=[];

  scheduleCardsByThemes: {
    default: CardSettings[];
    cosmic: CardSettings[];
    corporate: CardSettings[];
    dark: CardSettings[];
  } = {
    default: this.scheduleStatusCardsSet,
    cosmic: this.scheduleStatusCardsSet,
    corporate: [
      {
        ...this.removeScheduledCard,
        type: 'warning',
      },
      {
        ...this.scheduleAFixedDateCard,
        type: 'primary',
      },
      {
        ...this.scheduleAfterNSecCard,
        type: 'primary',
      },
      {
        ...this.scheduleCronCard,
        type: 'danger',
      },
      /*{
        ...this.coffeeMakerCard,
        type: 'info',
      },*/
    ],
    dark: this.scheduleStatusCardsSet,
  };


  callingCard: CardSettings = {
      title: 'Make-A-Call',
      iconClass: 'nb-phone',
      type: 'primary',
    };

  convertedDivertedCard: CardSettings = {
    title: 'Convert/Divert',
    iconClass: 'nb-lightbulb',
    type: 'primary',
  };
  
  updateProductInterestListCard: CardSettings = {
    title: 'Interested Products',
    iconClass: 'nb-e-commerce',
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
    this.convertedDivertedCard,
    this.updateProductInterestListCard,
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
        ...this.convertedDivertedCard,
        type: 'warning',
      },
      {
        ...this.updateProductInterestListCard,
        type: 'warning',
      },
    ],
    dark: this.commonStatusCardsSet,
  };


  redirectDelay: number = 0;
  showDetail : boolean = false;
  showAction : boolean = false;

  campaignSettings = {

    actions: {
      add: false,      //  if you want to remove add button
      edit: false,     //  if you want to remove edit button
      delete: false //  if you want to remove delete button
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
        hide: true,
      },
      customerid: {
        title: 'Customer ID',
        type: 'string',
        hide: true,
      },
      campaignid: {
        title: 'Campaign ID',
        type:'string',
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
      firstname: {
        title: 'Name',
        type: 'string',
        editable: false,
        addable: false,
        hide: true,
      },
      phoneNumber: {
        title: 'Phone',
        type: 'string',
        editable: false,
        addable: false,
        hide: true,
      },
      email: {
        title: 'Email',
        type: 'string',
        editable: false,
        addable: false,
        hide: true,
      },
      lastConnectedExtension: {
        title: 'Last Connected Extension',
        type: 'string',
        hide: true,
      },
      isCalledOnce: {
        title: 'Is Called Once',
        type: 'string',
        hide: true,
      },
      organization: {
        title: 'Organization',
        type: 'string',
        hide:true,
      },
    }
  };

  settings = {

    actions: {
      add: false,      //  if you want to remove add button
      edit: false,     //  if you want to remove edit button
      delete: false //  if you want to remove delete button
    },
    
    hideSubHeader: true,
    pager: { display: false },

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
      firstname: {
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
      lastname: {
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
      phoneNumber: {
        title: 'Phone Number',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      email: {
        title: 'Email',
        type: 'string',
      },
      coverted: {
        title: 'Is Coverted',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
      },
      domain: {
        title: 'Domain',
        type: 'string',
        hide: true,
      },
      phoneContext: {
        title: 'Phone Context',
        type: 'string',
        hide: true,
      },
      country: {
        title: 'Country',
        type: 'string',
      },
      city: {
        title: 'City',
        type: 'string',
        hide: true,
      },
      pesel: {
        title: 'Pesel',
        type: 'string',
        hide: true,
      },
      business: {
        title: 'Business',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      datatype: {
        title: 'Data Type',
        type: 'string',
        hide: true,
      },
      zipCode: {
        title: 'Zip Code',
        type: 'string',
        hide: true,
      },
      cronremindercalling: {
        title: 'Reminder Calling Cron',
        type: 'string',
        hide: true,
      },
      description: {
        title: 'Remark',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      imageData: {
        title: 'Image Data',
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
      lastConnectedExtension: {
        title: 'Last Connected Extension',
        type: 'string',
      },
      iscalledonce: {
        title: 'Call Once',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
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
         hide: true,
      },
      organization: {
        title: 'Organization',
        type: 'string',
        hide: true,
      },
      interestedProducts:{
        title: 'Interested Products',
        type: 'string',
        hide: true,
      },
      autoWhatsAppAIReply:{
        title: 'WhatsApp Auto Reply',
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
      firstWhatsAppMessageIsSend:{
        title: 'WhatsApp First Message',
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
      preferredLanguage:{
        title: 'Language',
         type: 'string',
         hide: true,
      },
      secondPreferredLanguage:{
        title: 'Second Language',
         type: 'string',
         hide: true,
      },
      updatedByAI:{
        title: 'Updated By AI',
         type: 'string',
         hide: true,
      },
      propertyInventory:{
        title: 'Property Inventory',
         type: 'string',
         hide: true,
      }
    },
  };


  radioOptions = [
    { value: '1-Column Search', label: '1-Property Search', checked: true },
    { value: '2-Column Search', label: '2-Property Search', disabled: true  },
    { value: '3-Column Search', label: '3-Property Search', disabled: true },
  ];

  radioOption;

  allDropDownOptions = [
    { column: 1, label: 'Email', properties:['email']},
    { column: 1, label: 'Phone Number', properties:['phoneNumber']},
    { column: 1, label: 'Pesel', properties:['pesel']},
    { column: 1, label: 'Zip Code', properties:['zipCode']},
    { column: 1, label: 'Phone Context', properties:['phoneContext']},
    { column: 1, label: 'Is Converted', properties:['coverted']},
    { column: 1, label: 'Country', properties:['country']},
    { column: 1, label: 'City', properties:['city']},
    { column: 1, label: 'Business', properties:['business']},
  ];

  dropDownOptions = [
    { column: 1, label: 'Email', properties:['email']},
    { column: 1, label: 'Phone Number', properties:['phoneNumber']},
    { column: 1, label: 'Pesel', properties:['pesel']},
    { column: 1, label: 'Zip Code', properties:['zipCode']},
    { column: 1, label: 'Phone Context', properties:['phoneContext']},
    { column: 1, label: 'Is Converted', properties:['coverted']},
    { column: 1, label: 'Country', properties:['country']},
    { column: 1, label: 'City', properties:['city']},
    { column: 1, label: 'Business', properties:['business']},
  ];

  dropDownOption;

  source: LocalDataSource = new LocalDataSource();
  interestedProducts :any = [];
  showInterestedProducts: any = false;
  interestedProductsLength = 0;
  base64InterestedProductImageData:any=[];
  currentByteImageData:any=null;
  base64ImageData:any=null;
  
  constructor(private customerService : CustomerService,
              private productService : ProductService,
              private exportAsService: ExportAsService,
              private themeService: NbThemeService,
              private nbToastrService:NbToastrService,
              protected router: Router,
              protected constantService : ConstantsService,
              private dialogService: NbDialogService,
              private campaignService: CampaignService) {

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
                  this.scheduleCards = this.scheduleCardsByThemes[theme.name];
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
              name: 'Converted',
              value: 0,
            },
            {
               name: 'Diverted',
              value: 0,
            },
          ];

          output[0].value = allData.converted;
          output[1].value = allData.diverted;
          this.setTotalRecords(output);
          // this.setPageNumber(this.totalPages);
      }

       this.source.load(<any[]>allData.data); 
       let arr = JSON.parse(JSON.stringify(allData.data));
       this.allRecords = [...arr];
  }


  setTable()
  {
    this.customerService.getAllCustomersOnOrganization(this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
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

      this.customerService.upload(formParams,this.organization)
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

    if (window.confirm('Are you sure you want to delete?')) {

   //console.log("Starting Delete API");

   this.customerService.deleteCustomerByIdAndOrganization(this.currentRecord.id,this.organization)
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

             //console.log("this.currentRecord.coverted");
             //console.log(JSON.stringify(this.currentRecord.coverted));


              if(JSON.stringify(this.currentRecord.coverted) == "\"true\""||JSON.stringify(this.currentRecord.coverted) == "true"){
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


    // this.currentImage = "data:image/png;base64,"+ this.previousRecord.imageData;
    this.currentRecord.imageData = this.previousRecord.imageData;

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
      this.customerService.updateCustomerByOrganization(JSON.stringify(this.currentRecord),this.organization,this.previousRecord.phoneNumber)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {

        //console.log("API returned result");

        if(String(result) == 'true')
        {

            try
            {
              this.setCampaignTable();
              this.setIfScheduledCall();
              this.setProductInterestType();
              this.setImageData();
            }
            catch(e){
              console.log(e)
            }

          
            // console.log("Result is true");
             event.confirm.resolve();
             var output = JSON.parse(JSON.stringify(this.single));


             if(JSON.stringify(this.currentRecord.coverted) == JSON.stringify(this.previousRecord.coverted)){   
              //console.log("Both are same");
            }
            else{
              if(JSON.stringify(this.currentRecord.coverted) == "\"true\""||JSON.stringify(this.currentRecord.coverted) == "true")
              {
                //console.log("current record is true");
                output[0].value = output[0].value + 1;
                output[1].value = output[1].value - 1;
              }
              else if(JSON.stringify(this.currentRecord.coverted) == "\"false\""||JSON.stringify(this.currentRecord.coverted) == "false")
                {
                  //console.log("current record is false");
                  output[0].value = output[0].value - 1;
                  output[1].value = output[1].value + 1;
                
              }
              else
              {
                //console.log("I am something else");
                //console.log(this.currentRecord.coverted);
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
   
      this.customerService.createCustomerByOrganization(JSON.stringify(this.currentRecord),this.organization)
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
   
           //console.log("API returned result");
   
           if(String(result) == 'true')
           {
                //console.log("Result is true");
                this.setTable();
                this.setIfScheduledCall();
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

  search(){
    this.currentPageNumber = 1;
    this.searchString = "";
    this.onSearch();
  }

  onSearch()
  {
    this.showAction = false;
    this.showDetail = false;

   //console.log("On Search is clicked");

    // console.log("On Search is clicked");

    if(!this.isSearchDone)
      {
        this.isSearchDone = true;
      }
      
    if(this.currentDropDownOption.label == 'Email')
    {
    // console.log("Ami User Search Started");
      this.customerService.getCustomerByEmailAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
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
    if(this.currentDropDownOption.label == 'Phone Number')
    {
    // console.log("Ami User Search Started");
      this.customerService.getByPhoneNumberAndOrganization(this.inputValues[0],this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (allData:any) => {

          let output = [
            {
              name: 'Converted',
              value: 0,
            },
            {
               name: 'Diverted',
              value: 0,
            },
          ];

          if(allData !=null)
          {
            this.totalPages = 1;
            this.currentPageNumber = 1;
            this.pageOptionsAll=[1];
            this.pageOptions$ = of(this.pageOptionsAll);
            if(allData.coverted)
              {
                output[0].value = 1;
                output[1].value = 0;
              }
            else
              {
                output[0].value = 0;
                output[1].value = 1;
              }
            
            this.single=[...output];
            var arr = JSON.parse('['+JSON.stringify(allData)+']');
            this.source.load(arr); 
            //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
            this.allRecords = [...arr];  
          }
          else
          {
            this.totalPages = 0;
            this.currentPageNumber = 0;
            this.pageOptionsAll=[];
            this.pageOptions$ = of(this.pageOptionsAll);
            this.single=[...output];
            this.source.load(<any[]>[]); 
            this.allRecords = [];
          }
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }
    if(this.currentDropDownOption.label == 'Pesel')
    {
    // console.log("Ami User Search Started");
      this.customerService.getCustomerByPeselAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
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
    else if(this.currentDropDownOption.label == 'Zip Code')
    {
      //console.log("Phone Context Search Started");
      this.customerService.findAllByZipCodeAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
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
    else if(this.currentDropDownOption.label == 'Is Converted')
    {
      //console.log("Is Active Search Started");
      this.customerService.findAllByCovertedAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
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
    else if(this.currentDropDownOption.label == 'Phone Context')
    {
      //console.log("Phone Context Search Started");
      this.customerService.findAllByPhoneContextAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
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
      //console.log("Phone Context Search Started");
      this.customerService.findAllByCountryAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
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
    else if(this.currentDropDownOption.label == 'City')
    {
      //console.log("Phone Context Search Started");
      this.customerService.findAllByCityAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
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
    else if(this.currentDropDownOption.label == 'Business')
    {
      //console.log("Phone Context Search Started");
      this.customerService.findAllByBusinessAndOrganization(this.inputValues[0],this.organization,this.searchString,(this.currentPageNumber-1),this.currentPageSize)
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
  
  submitDescription()
  {
      console.log('submit description button pressed')

      if(this.currentRecord != null)
      {
         this.customerService.updateCustomerDescription( {description: this.currentRecord.description, id: this.currentRecord.id,organization: this.currentRecord.organization})
                 .pipe(takeUntil(this.destroy$))
                 .subscribe({
                   next: result => {
                     if(String(result) == 'true')
                     {
                       console.log("Result is true, setting values to show on icon"); 
                       this.showDialoge('Success','activity-outline','success', "Customer remark updated."); 
                     }
                     else{
                        // console.log("Result is not true");
                         this.showDialoge('Error','activity-outline','danger', "Customer remarks was not updated."); 
                     }
                   },
                   error: err => {
                   console.log("Error : "+ JSON.stringify(err));
                    //  this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
                   }
                 });
      }
  }


  getChildData(event){

    if(event == 'Convert/Divert')
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
          if(element == "coverted")
          {
            this.values[i] = this.currentRecord.coverted;
          }
          i = i +1;
        });

       

        //Update upper chart
        var output = JSON.parse(JSON.stringify(this.single));
        if(JSON.stringify(this.currentRecord.coverted) == "\"true\""||JSON.stringify(this.currentRecord.coverted) == "true")
                     {
                       //console.log("current record is true");
                       output[0].value = output[0].value + 1;
                       output[1].value = output[1].value - 1;
                     }
                     else if(JSON.stringify(this.currentRecord.coverted) == "\"false\""||JSON.stringify(this.currentRecord.coverted) == "false")
                       {
                        // console.log("current record is false");
                         output[0].value = output[0].value - 1;
                         output[1].value = output[1].value + 1;
                       
                     }
                     else
                     {
                      // console.log("I am something else");
                       //console.log(this.currentRecord.coverted);
                     }
       
                   this.single = [...output];
                   
        //this.setTable();
    }
  }

  onUserRowSelect(event): void {
    //console.log("User Selected a row. Row data is  : ");
    //console.log(event);
    this.currentRecord = JSON.parse(JSON.stringify(event.data));

    // console.log("onUserRowSelect this.currentRecord ", this.currentRecord);


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

    this.organization = localStorage.getItem("organization");
    this.customerService.getByPhoneNumberAndOrganization(this.currentRecord.phoneNumber,localStorage.getItem("organization"))
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: data => {

       //console.log(JSON.parse(JSON.stringify(data)));
       //console.log(JSON.parse(JSON.stringify(data)).parkedchannel2);
        if(data == null)
        {
          //console.log("data is null");
          this.showDialoge('Error','activity-outline','danger', "Customer not found for this phone on server."); 
        }
        else
        {
          //console.log("data is not null");
          this.currentRecord = data;
          // this.currentImage = "data:image/png;base64,"+ JSON.parse(JSON.stringify(data)).imageData;

          try{
            this.setCampaignTable();
            this.setIfScheduledCall();
            this.setProductInterestType();
            this.setImageData();

            if(ConstantsService.user.role === ConstantsService.employee)
              {
                 document.getElementById("downloadCustomerCampaign").hidden = true;
              }
            
          }
          catch(e)
          {
            console.log(e);
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

  picUpload(){
    //console.log("Bulk Upload AMI");
    const val = document.getElementById('pic-input');
    val.click();
  }

  onCustomerPicturechange (event: any)
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
        // console.log("onProductPicturechange");
      if(event.target.files[0].name.endsWith(".png") ||event.target.files[0].name.endsWith(".PNG") || event.target.files[0].name.endsWith(".JPG") ||event.target.files[0].name.endsWith(".jpg") || event.target.files[0].name.endsWith(".jpeg")|| event.target.files[0].name.endsWith(".JPG") ||event.target.files[0].name.endsWith(".jpg") || event.target.files[0].name.endsWith(".JPEG")) 
        {

        this.file = event.target.files[0];
        
        let formParams = new FormData();
        formParams.append('image', event.target.files[0])

        this.customerService.uploadPicByIdAndOrganization(formParams,this.currentRecord.id,this.organization)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
                {
                      // this.currentRecord.imageData;
                      event.target.value = "";
                    this.showDialoge('Success','done-all-outline','success', `Customer pic upload process is successful.`);
                    // event.confirm.resolve();
                    this.setImageData();
                    }
                else{
                  //console.log("Result is not true");
                  // event.confirm.reject();
                  this.showDialoge('Unsuccess','done-all-outline','danger', `Customer pic upload process is unsuccessful.`);
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

  campaignExport() {
    this.exportAsConfigCampaign.type = this.downloadAsCampaign;
    this.exportAsService
      .save(this.exportAsConfigCampaign, 'Campaigns_To_Customer')
      .subscribe(() => {
        // save started
      });
  }

  setImageData()
  {
    this.customerService.getCustomerImage(this.currentRecord.phoneNumber,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (allData:any) => {

        if(allData == null)
        {
          //  console.log("I am null data");
            this.currentByteImageData = null; 
            this.base64ImageData=null
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

        }
      },
      error: err => {
       // console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  setCampaignTable()
  {
    this.campaignService.findAllByCustomerAndOrganization(this.currentRecord.phoneNumber,this.organization)
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
  
  setIfScheduledCall()
  {
    this.customerService.findIfScheduledCallJobByOrganization(this.constantService.cron,this.currentRecord.phoneNumber,ConstantsService.user.extension,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
        if(String(result) == 'true')
        {
             this.allScheduleType.push(this.constantService.cron);
             this.isAlreadyScheduled = '{Already Scheduled (Schedule again to overwrite)}';
        }
        else{
                 this.isAlreadyScheduled = '{Nothing Scheduled (Schedule Now)}';
        }

      },
      error: err => {
       console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });


    this.customerService.findIfScheduledCallJobByOrganization(this.constantService.fixeddate,this.currentRecord.phoneNumber,ConstantsService.user.extension,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
        if(String(result) == 'true')
        {
             this.allScheduleType.push(this.constantService.fixeddate);
             this.isAlreadyScheduled = '{Already Scheduled (Schedule again to overwrite)}';
        }
        else{
                 this.isAlreadyScheduled = '{Nothing Scheduled (Schedule Now)}';
        }

      },
      error: err => {
       console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });


    this.customerService.findIfScheduledCallJobByOrganization(this.constantService.afternseconds,this.currentRecord.phoneNumber,ConstantsService.user.extension,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
        if(String(result) == 'true')
        {
             this.allScheduleType.push(this.constantService.afternseconds);
             this.isAlreadyScheduled = '{Already Scheduled (Schedule again to overwrite)}';
        }
        else{
                 this.isAlreadyScheduled = '{Nothing Scheduled (Schedule Now)}';
        }

      },
      error: err => {
       console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });

  }

  getScheduleData(event){

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

  setProductInterestType()
  {

    // console.log("setProductInterestType this.currentRecord ", this.currentRecord);

    let data:any;
    if(JSON.parse(JSON.stringify(this.currentRecord)).interestedProducts != null )
    {
      data = {
        ids: JSON.parse(JSON.stringify(this.currentRecord)).interestedProducts.split(',').map(list => Number(list)),
        extension:this.currentRecord.extension,
        organization:this.organization
      }

      this.productService.getAllProductsByIdIn(data)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
          // console.log("getAllProductsByIdIn");
          // console.log(JSON.stringify(allData));
          if(allData == null || JSON.parse(JSON.stringify(allData)).length ==0)
          {
            //console.log("all data is null");
            // console.log("Does not have products interested in")
            this.interestedProducts = [];
            this.showInterestedProducts = false;
          }
          else{
            // console.log("Has product interests")
            this.interestedProducts = [... JSON.parse(JSON.stringify(allData))];
            this.showInterestedProducts = true;
            this. interestedProductsLength = this.interestedProducts.length;

            this.interestedProducts.forEach((current:any,i:number)=>
            {
                try{
                      if(JSON.parse(JSON.stringify(current)).imageByteData != null)
                      {
                        let url = 'data:image/'+current.imageType+';base64,'+current.imageByteData;
                        this.base64InterestedProductImageData.push(url);
                      }
                      this.base64InterestedProductImageData[i] = this.base64InterestedProductImageData[i].replace("/image","");
                }
                catch(e)
                {
                  if((this.base64InterestedProductImageData.length-1)<i)
                  {
                    this.base64InterestedProductImageData.push(null);
                  }   
                }    
            });
  
          }
  
        },
        error: err => {
            console.log("Error : "+ JSON.stringify(err));
          //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });

    }
    else{
      this.showInterestedProducts = false;
    }
  }
  
  setAutodialerType()
  {
    this.campaignService.getAllAutodialerTypes(this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {
        // console.log("setAutodialerType");
        // console.log(JSON.stringify(allData));
        //this.allConference = [... JSON.parse(JSON.stringify(allData))];
        let list= [];
        let singleObject = {value: '', title:''};
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

       
        this.allAutodialerTypes = [... list];
        // console.log(JSON.stringify(this.allAutodialerTypes));

      },
      error: err => {
       console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
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
        // this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
        this.settings.actions.add = false;
        this.settings.actions.edit = false;
        this.settings.actions.delete = false;
        document.getElementById("downloadMainTableButton").hidden = true;
        document.getElementById("bulkUploadButton").hidden = true;
      }
      else{
          this.setAutodialerType();  
      }

      this.setTable();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
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