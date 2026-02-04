import { Component, OnDestroy, OnInit } from '@angular/core';
import { AmiConnectionService } from './../service/ami-connection.service';
import { delay, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
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
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';
import { CustomInputTableComponent } from '../../employee/all-employees/custom-input-table/custom-input-table.component';

interface CardSettings {
  title: string;
  iconClass: string;
  type: string;
}


@Component({
  selector: 'ngx-registries',
  templateUrl: './registries.component.html',
  styleUrls: ['./registries.component.scss']
})
export class RegistriesComponent implements OnInit, OnDestroy {
  
  tableHeading = 'AMI Connections';
  private destroy$: Subject<void> = new Subject<void>();
  downloadAs: SupportedExtensions = 'png';

  
  file: File = null;

  
  stepTwoNextButton = true;
  stepThreeNextButton = true;

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
  enableDisableCard: CardSettings = {
    title: 'Enable/Disable',
    iconClass: 'nb-lightbulb',
    type: 'primary',
  };
  refreshCard: CardSettings = {
    title: 'Refresh',
    iconClass: 'nb-roller-shades',
    type: 'success',
  };
  connectCard: CardSettings = {
    title: 'Connect',
    iconClass: 'nb-audio',
    type: 'info',
  };
  
  /*coffeeMakerCard: CardSettings = {
    title: 'Coffee Maker',
    iconClass: 'nb-coffee-maker',
    type: 'warning',
  };*/

  statusCards: string;

  commonStatusCardsSet: CardSettings[] = [
    this.enableDisableCard,
    this.refreshCard,
    this.connectCard,
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
        ...this.enableDisableCard,
        type: 'warning',
      },
      {
        ...this.refreshCard,
        type: 'primary',
      },
      {
        ...this.connectCard,
        type: 'danger',
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
      domain: {
        title: 'Domain',
        type: 'string',
      },
      port: {
        title: 'Port',
        type: 'number',
      },
      amiuser: {
        title: 'AMI User',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                  // DATA FROM HERE GOES TO renderComponent
                  console.log('valuePrepareFunction');
                  console.log(cell);
                  return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      password: {
        title: 'Password',
        type: 'string',
      },
      phonecontext: {
        title: 'Phone Context',
        type: 'string',
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
    { column: 1, label: 'AmiUser', properties:['amiuser']},
    { column: 1, label: 'PhoneContext', properties:['phonecontext']},
    { column: 1, label: 'IsActive', properties:['isactive']},
  ];

  dropDownOptions = [
    { column: 1, label: 'AmiUser', properties:['amiuser']},
    { column: 1, label: 'PhoneContext', properties:['phonecontext']},
    { column: 1, label: 'IsActive', properties:['isactive']},
  ];
  currentDropDownOption: any;
  dropDownOption;

  source: LocalDataSource = new LocalDataSource();

  exportAsConfig: ExportAsConfig = {
      type: 'xlsx', // the type you want to download
      elementIdOrContent: 'lastTable', // the id of html/table element
    };
  constructor(private amiConnectionService : AmiConnectionService,
              private exportAsService: ExportAsService,
              private themeService: NbThemeService,
              private nbToastrService:NbToastrService,
              protected router: Router,
              protected constantService : ConstantsService,
              private dialogService: NbDialogService) {

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


  setTable()
  {
    this.amiConnectionService.getAllAmiConnectionsByOrganization(this.organization)
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
          
                if (element.isactive == true) {
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
       // console.log("Error : "+ JSON.stringify(err));
        //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
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

      this.amiConnectionService.upload(formParams,this.organization)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
          // console.log("Result : "+ JSON.stringify(result));
           this.setTable();
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
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
    console.log("User Deleted a row. Row data is  : ");
    //console.log(event);
    
    this.previousRecord =  this.currentRecord;
    this.currentRecord = JSON.parse(JSON.stringify(event.data));

    this.showDetail = false;
    this.showAction = false;

    if (window.confirm('Are you sure you want to delete?')) {

   //console.log("Starting Delete API");

   this.amiConnectionService.deleteAmiConnectionByAmiUserAndOrganization(this.currentRecord.amiuser,this.organization)
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
        //console.log("Error : "+ JSON.stringify(err));
        //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
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
      this.amiConnectionService.updateAmiConnectionByOrganization(JSON.stringify(this.currentRecord),this.organization)
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
       // console.log("Error : "+ JSON.stringify(err));
        //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
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
   
      this.amiConnectionService.createAmiConnectionByOrganization(JSON.stringify(this.currentRecord),this.organization)
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
   
           //console.log("API returned result");
   
           if(String(result) == 'true')
           {
                //console.log("Result is true");
                event.confirm.resolve();
                this.setTable();
           }
           else{
             //console.log("Result is not true");
             this.showDialoge('Error','activity-outline','danger', "You should create records for your organization. Record not created."); 
             event.confirm.reject();
           }
         },
         error: err => {
           //console.log("Error : "+ JSON.stringify(err));
           //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
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

    if(this.currentDropDownOption.label == 'AmiUser')
    {
    // console.log("Ami User Search Started");
      this.amiConnectionService.getAmiConnectionByAmiuserAndOrganization(this.inputValues[0],this.organization)
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
        
              if (element.isactive == true) {
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
        // console.log("Error : "+ JSON.stringify(err));
          //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }
    else if(this.currentDropDownOption.label == 'PhoneContext')
    {
      //console.log("Phone Context Search Started");
      this.amiConnectionService.getAllAmiConnectionsOnPhoneContextAndOrganization(this.inputValues[0],this.organization)
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
      
            if (element.isactive == true) {
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
        // console.log("Error : "+ JSON.stringify(err));
          //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }
    else if(this.currentDropDownOption.label == 'IsActive')
    {
      //console.log("Is Active Search Started");
      this.amiConnectionService.getAllAmiConnectionOnIsEnabledAndOrganization(this.inputValues[0],this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
  
          if(allData == null)
          {
            
            console.log("I am null data");
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
      
            if (element.isactive == true) {
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
        // console.log("Error : "+ JSON.stringify(err));
          //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }

  }
  
  getChildData(event){
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
          if(element.amiuser == this.currentRecord.amiuser)
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
      .save(this.exportAsConfig, 'AMIConnections')
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
        this.settings.actions.add = false;
        this.settings.actions.edit = false;
        this.settings.actions.delete = false;
        document.getElementById("downloadMainTableButton").hidden = true;
        document.getElementById("bulkUploadButton").hidden = true;
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