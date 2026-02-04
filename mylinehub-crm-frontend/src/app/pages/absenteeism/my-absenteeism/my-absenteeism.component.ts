import { Component, OnDestroy, OnInit } from '@angular/core';
import { AbsenteeismService } from './../service/absenteeism.service';
import { delay, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { LocalDataSource } from 'ng2-smart-table';
import { EmployeeService } from './../../employee/service/employee.service';
import { DepartmentService } from './../../department/service/department.service';
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
  selector: 'ngx-my-absenteeism',
  templateUrl: './my-absenteeism.component.html',
  styleUrls: ['./my-absenteeism.component.scss']
})
export class MyAbsenteeismComponent  implements OnInit, OnDestroy {
  
  tableHeading = 'My Absenteeism';
  private destroy$: Subject<void> = new Subject<void>();
  downloadAs: SupportedExtensions = 'png';

  file: File = null;

  
  stepTwoNextButton = true;
  stepThreeNextButton = true;

  currentRecord:any = ' ';
  previousRecord:any = ' ';
  allRecords:any = [];
  allEmployees:any = [];

  //Step2
  keys: any = [];
  values: any = [];
  selectedOption;

  //Step3
  inputs: any = [];
  types: any = [];
  inputValues: any = ['','',''];
  organization = '';
  email = '';

  single = [
    {
      name: 'Absenteeisms',
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

  exportAsConfig: ExportAsConfig = {
    type: 'xlsx', // the type you want to download
    elementIdOrContent: 'lastTable', // the id of html/table element
  };

  settings = {

    add: {
      addButtonContent: '<i class="nb-plus"></i>',
      createButtonContent: '<i class="nb-checkmark"></i>',
      cancelButtonContent: '<i class="nb-close"></i>',
      confirmCreate: true
    },
    pager: {
      display: true,
      perPage: 5
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
      absenteeismName: {
        title: 'Type Of Absense',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      employeeID: {
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
        title: 'First Name',
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
        editable: false,
        addable: false,
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
        editable: false,
        addable: false,
      },
      reasonForAbsense: {
        title: 'Reason For Absense',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      dateFrom: {
        title: 'Date From',
        type: 'date',
      },
      dateTo: {
        title: 'Date To',
        type: 'date',
      },
      organization: {
        title: 'Organization',
        type: 'string',
        hide: true
      }
    },
  };



  radioOptions = [
    { value: '1-Column Search', label: '1-Property Search', disabled: true },
    { value: '2-Column Search', label: '2-Property Search', disabled: true  },
    { value: '3-Column Search', label: '3-Property Search', disabled: true },
  ];

  radioOption;

  allDropDownOptions = [
  ];

  dropDownOptions = [
  ];
  currentDropDownOption: any;
  dropDownOption;

  source: LocalDataSource = new LocalDataSource();

  constructor(private absenteeismService : AbsenteeismService,
              private employeeService : EmployeeService,
              private exportAsService: ExportAsService,
              private themeService: NbThemeService,
              private nbToastrService:NbToastrService,
              protected router: Router,
              protected constantService : ConstantsService,
              private dialogService: NbDialogService,
              private departmentService: DepartmentService) {

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

                if(localStorage.getItem("email")!=null)
                 {
                  this.email = localStorage.getItem("email");
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
    this.employeeService.getEmployeeByEmailAndOrganization(this.email,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {
        //console.log("getEmployeeByEmailAndOrganization");
        //console.log(JSON.stringify(allData));

        this.allEmployees = [... JSON.parse('['+JSON.stringify(allData)+']')];
        this.setTable(); 
        var list= [];
        var singleObject = {value: '', title:''};
        if(allData == null)
        {
          //console.log("all data is null");
          list= [];
        }
        else{

          //console.log("all data is not null");

          var arr = JSON.parse('['+JSON.stringify(allData)+']');
          arr.forEach((element) => {
            //console.log("element");
            //console.log(element);
            //console.log(JSON.stringify(element));
            singleObject.value = element.id;
            singleObject.title = element.id+", "+element.firstName + " " + element.lastName;;
            list.push( JSON.parse(JSON.stringify(singleObject)));
          });
        }

       
        this.settings.columns.employeeID.editor.config.list = [... list];
        this.settings = Object.assign({}, this.settings);

      },
      error: err => {
       // console.log("Error : "+ JSON.stringify(err));
        //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  setTable()
  {
    this.absenteeismService.findAllByEmployeeAndOrganization( this.allEmployees[0].id,this.organization)
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
                name: 'Absenteeisms',
                value: 0,
              },
            ];

            //console.log("After http");    
            this.single=[...output];
        }
        else
        {
           // console.log("Inside Data : "+ JSON.stringify(allData));
              this.source.load(<any[]>allData); 
                 
              var arr = JSON.parse(JSON.stringify(allData));
             // console.log("Inside Data (arr) : "+ JSON.stringify(arr));
              this.allRecords = [...arr];

              var output = [
                {
                  name: 'Absenteeisms',
                  value: 0,
                },
              ];

              arr.forEach((element) => {
              //  console.log("Element"+ JSON.stringify(element));
                output[0].value = output[0].value +1;
              });
              
             // console.log("Assigning output");    
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
  }

  
  onDeleteConfirm(event): void {
    //console.log("User Deleted a row. Row data is  : ");
    //console.log(event);
    
    this.previousRecord =  this.currentRecord;
    this.currentRecord = JSON.parse(JSON.stringify(event.data));

    this.showDetail = false;
    this.showAction = false;

    if (window.confirm('Are you sure you want to delete?')) {

   //console.log("Starting Delete API");

   this.absenteeismService.deleteAbsenteeismByIdAndOrganization(this.currentRecord.id,this.organization)
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


             output[0].value = output[0].value - 1;
              
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
      this.absenteeismService.updateAbsenteeismByOrganization(JSON.stringify(this.currentRecord),this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {

        //console.log("API returned result");

        if(String(result) == 'true')
        {
            // console.log("Result is true");
             event.confirm.resolve();
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
    
    //console.log(JSON.stringify(this.currentRecord));
    var id = this.currentRecord.employeeID.split(',')[0];
    //console.log("Id : " + id);

    var arr = JSON.parse(JSON.stringify(this.allEmployees));
    arr.forEach((element) => {
            //console.log(JSON.stringify(element));
            if(element.id == id)
            {
              //console.log("Inside Create loop and found manager");
              this.currentRecord.employeeID = id;
              this.currentRecord.firstName = element.firstName;
              this.currentRecord.lastName = element.lastName;
              this.currentRecord.departmentName = element.departmentName;
            }
          });


    var columns = JSON.parse(JSON.stringify(this.settings.columns));
    var keys = [];
    Object.keys(this.currentRecord).map(function (key) { //console.log(key);
      console.log(key);
      //console.log(columns[key].title);
      keys.push(columns[key].title);

    });
    this.keys = keys;
    this.values = Object.values(this.currentRecord);

    this.showDetail = false;
    this.showAction = false;

    if (window.confirm('Are you sure you want to create?')) {

    //console.log("Starting Create API");
    //console.log(JSON.stringify(this.currentRecord));
      this.absenteeismService.createAbsenteeismByOrganization(JSON.stringify(this.currentRecord),this.organization)
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

  
  onSearch()
  {
    this.showAction = false;
    this.showDetail = false;

   //console.log("On Search is clicked");
   if(this.currentDropDownOption.label == 'Employee')
   {
     //console.log("Phone Context Search Started");
     this.absenteeismService.findAllByEmployeeAndOrganization(this.inputValues[0],this.organization)
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
              name: 'Absenteeisms',
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
            name: 'Absenteeisms',
            value: 0,
          },
        ];
 
         arr.forEach((element) => {
 
           //console.log("Element"+ JSON.stringify(element));
           output[0].value = output[0].value +1;
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

   if(this.currentDropDownOption.label == 'Reason For Absenteeism')
   {
     //console.log("Phone Context Search Started");
     this.absenteeismService.findAllByReasonForAbsenseAndOrganization(this.inputValues[0],this.organization)
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
              name: 'Absenteeisms',
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
            name: 'Absenteeisms',
            value: 0,
          },
        ];
 
         arr.forEach((element) => {
 
           //console.log("Element"+ JSON.stringify(element));
           output[0].value = output[0].value +1;
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

   if(this.currentDropDownOption.label == 'B/W From,To Date')
   {
        var inputDate1: Date = new Date(this.inputValues[0]);
        var result1 = inputDate1.toLocaleString();


        var inputDate2: Date = new Date(this.inputValues[1]);
        var result2 = inputDate2.toLocaleString();
     //console.log("Phone Context Search Started");
     this.absenteeismService.findAllByDateFromGreaterThanEqualAndDateToLessThanEqualOrganization(result1,result2,this.organization)
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
              name: 'Absenteeisms',
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
            name: 'Absenteeisms',
            value: 0,
          },
        ];
 
         arr.forEach((element) => {
 
           //console.log("Element"+ JSON.stringify(element));
           output[0].value = output[0].value +1;
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
  }
  
  getChildData(event){
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
      .save(this.exportAsConfig, 'MyAbsenteeism')
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
    this.setTableDropDownValues();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }
}

