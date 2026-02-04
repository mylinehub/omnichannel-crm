import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NbDialogService, NbThemeService } from '@nebular/theme';
import { ConstantsService } from '../../../service/constants/constants.service';
import { EmployeeService } from '../../employee/service/employee.service';
import { Subject, takeUntil, takeWhile } from 'rxjs';
import { ExportAsConfig, ExportAsService, SupportedExtensions } from 'ngx-export-as';
import { LocalDataSource } from 'ng2-smart-table';
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';
import { ProductService } from '../../product/service/product.service';
import { WhatsappdictService } from '../../../service/whatsapp-dict/whatsappdict.service';
import { WhatsappNumberService } from '../../../service/whatsapp-number/whatsapp-number.service';
import { WhatsappTemplateService } from '../../../service/whatsapp-template/whatsapp-template.service';
import { WhatsappTemplateVariableService } from '../../../service/whatsapp-template-variable/whatsapp-template-variable.service';
import { WhatsappProjectService } from '../../../service/whatsapp-project/whatsapp-project.service';
import { AssignEmployeesComponent } from '../assign-employees/assign-employees.component';
import { CustomInputTableComponent } from '../../employee/all-employees/custom-input-table/custom-input-table.component';
import { EmbeddedSignupResult } from './EmbeddedSignup';

@Component({
  selector: 'ngx-details',
  templateUrl: './details.component.html',
  styleUrls: ['./details.component.scss']
})
export class DetailsComponent implements OnInit,OnDestroy {

  embeddedSignupBusy: boolean = false;
  embeddedSignupResult: EmbeddedSignupResult | null = null;
  embeddedSignupError: string | null = null;

  private embeddedSessionInfo: any = null;
  private embeddedPostMessageRaw: any = null;

  whatsAppSupportlink = "+919625048379";
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
  view :any = [600, 200];
  gradient: boolean = false;
  private alive = true;
  themeSubscription: any;
  redirectDelay: number = 0;
  organization: string;
  currentNumberRecord:any = ' ';
  previousNumberRecord:any = ' ';
  currentTemplateRecord:any = ' ';
  previousTemplateRecord:any = ' ';
  currentTemplateVariableRecord:any = ' ';
  previousTemplateVariableRecord:any = ' ';

  tableHeading = 'Phone Numbers';
  templateHeading = 'Phone Number Templates';
  templateVariableHeading = 'Template Variables';

  private destroy$: Subject<void> = new Subject<void>();

  downloadAs: SupportedExtensions = 'png';
  downloadAsTemplate: SupportedExtensions = 'png';
  downloadAsTemplateVariable: SupportedExtensions = 'png';
  
  exportAsConfigNumber: ExportAsConfig = {
      type: 'xlsx', // the type you want to download
      elementIdOrContent: 'lastTable', // the id of html/table element
  };
  
  exportAsConfigTemplate: ExportAsConfig = {
      type: 'xlsx', // the type you want to download
      elementIdOrContent: 'templateTable', // the id of html/table element
  };
  
  exportAsConfigTemplateVariable: ExportAsConfig = {
      type: 'xlsx', // the type you want to download
      elementIdOrContent: 'templateVariableTable', // the id of html/table element
  };

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
        hide:true,
      },
      whatsAppProjectId: {
        title: 'Project Id',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
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
      phoneNumberID: {
        title: 'Phone Id',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      verifyToken: {
        title: 'Verify Token',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      autoAiMessageAllowed: {
        title: 'AI Reply',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
      },
      autoAiMessageLimit: {
        title: 'AI Message Limit',
        type: 'string',
        hide:true,
      },
      whatsAppAccountID: {
        title: 'Account Id',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      aiModel: {
        title: 'Ai Model',
        type: 'string',
        hide:true,
      },
      callBackURL: {
        title: 'Call-Back Url',
        type: 'string',
        hide:true,
      },
      callBackSecret: {
        title: 'Call-Back Secret',
        type: 'string',
        hide:true,
      },
      country: {
        title: 'Country',
        type: 'string',
        hide:true,
      },
      currency: {
        title: 'Currency',
        type: 'string',
        hide:true,
      },
      aiCallExtension: {
        title: 'AI Call Extension',
        type: 'string'
      },
      adminEmployeeId: {
        title: 'Admin-I',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      secondAdminEmployeeId: {
        title: 'Admin-II',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      employeeExtensionAccessList: {
        title: 'Access List',
        type: 'string',
        hide:true,
      },
      costPerInboundMessage: {
        title: 'costPerInboundMessage',
        type: 'string',
        hide:true,
      },
      costPerOutboundMessage: {
        title: 'costPerOutboundMessage',
        type: 'string',
        hide:true,
      },
      costPerInboundAIMessageToken: {
        title: 'costPerInboundAIMessageToken',
        type: 'string',
        hide:true,
      },
      costPerOutboundAIMessageToken: {
        title: 'costPerOutboundAIMessageToken',
        type: 'string',
        hide:true,
      },
      active: {
        title: 'Active',
        type: 'string',
        hide:true,
      },
      organization: {
        title: 'Organization',
        type: 'string',
        hide:true,
      },
      storeVerifyCustomerPropertyInventory: {
        title: 'Collect Property Inventory',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
      },
    aiOutputClassName: {
        title: 'AI Output Class Name',
        type: 'string',
      },

      
    },
  };


  templateSettings = {

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
        hide:true,
      },
      whatsAppPhoneNumberId: {
        title: 'Phone Number Id',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
         hide:true,
      },
      templateName: {
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
      conversationType: {
        title: 'Type',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      currency: {
        title: 'Currency',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      productId: {
        title: 'Product Id',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      organization: {
        title: 'Organization',
        type: 'string',
        hide:true,
      },
      followOrder: {
        title: 'Follow Order',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: [{value: 'true', title:'true'},
                   {value: 'false', title:'false'}]
          },
         },
      },
      languageCode: {
        title: 'Language Code',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
      },
      mediaPath: {
        title: 'Media Path',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      mediaType: {
          title: 'Media Type',
          type: 'string',
      },
      mediaId: {
          title: 'Media Id',
          type: 'string',
          hide:true,
      },
    },
  };


   templateVariableSettings = {
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
        title: 'Id',
        type: 'number',
        editable: false,
        addable: false,
        hide:true,
      },
      whatsAppPhoneNumberTemplateId: {
        title: 'Template Id',
        editor: {
          type: 'list',
          config: {
            selectText: 'Select',
            list: []
          },
         },
         hide:true,
      },
     orderNumber: {
        title: 'Order',
        type: 'string',
      },
     variableName: {
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
     variableHeaderType: {
        title: 'Placement',
        type: 'string',
      },
     variableType: {
        title: 'Type',
        type: 'string',
      },
     mediaSelectionType: {
        title: 'Media Type',
        type: 'string',
      },
     mediaID: {
        title: 'Media Id',
        type: 'string',
      },
     mediaUrl: {
        title: 'Media Url',
        type: 'string',
         hide:true,
      },
     fileName: {
        title: 'File Name',
        type: 'string',
         hide:true,
      },
     caption: {
        title: 'Caption',
        type: 'string',
         hide:true,
      },
      organization: {
        title: 'Organization',
        type: 'string',
        hide:true,
      },
    },
  };
  

  source: LocalDataSource = new LocalDataSource();
  templateSource: LocalDataSource = new LocalDataSource();
  templateVariableSource: LocalDataSource = new LocalDataSource();
  showAction: boolean = false;
  showTemplateVariable : boolean = false;
  totalTemplateRecords = 10;
  totalTemplateVariableRecords  = 10;

  allEmployeeToNumber:any = [];
  allEmployees:any = [];
  allProducts:any = [];
  allNumberRecords:any = [];
  allTemplateRecords:any = [];
  allTemplateVariableRecords:any = [];
  embeddedFinishEventReceived = false;
  embeddedFinishEventPayload: any = null;

  constructor(  private themeService: NbThemeService,
                protected router: Router,
                protected constantService : ConstantsService,
                private dialogService: NbDialogService,
                private employeeService:EmployeeService,
                private productService:ProductService,
                private exportAsService: ExportAsService,
                private whatsappProjectService:WhatsappProjectService,
                private whatsappdictService:WhatsappdictService,
                private whatsappNumberService:WhatsappNumberService,
                private whatsappTemplateService:WhatsappTemplateService,
                private whatsappTemplateVariableService:WhatsappTemplateVariableService
            ) {
  
                  //Set support number 
                  this.whatsAppSupportlink = ConstantsService.whatsAppSupportlink;
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
                    const colors: any = theme.variables;
                    this.colorScheme = {
                    domain: [colors.primaryLight, colors.infoLight, colors.successLight, colors.warningLight, colors.dangerLight],
                    };
                  });

                  window.addEventListener('message', this.embeddedSignupMessageHandler);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    window.removeEventListener('message', this.embeddedSignupMessageHandler);
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }

  ngOnInit(): void {
    console.log("I am in ngOnIt");

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
        this.templateSettings.actions.add = false;
        this.templateSettings.actions.edit = false;
        this.templateSettings.actions.delete = false;
        this.templateVariableSettings.actions.add = false;
        this.templateVariableSettings.actions.edit = false;
        this.templateVariableSettings.actions.delete = false;
        document.getElementById("downloadMainTableButton").hidden = true;
      }

    if(ConstantsService.user.role === ConstantsService.employee)
      {
        //Do nothing here as user is allowed to view but not allowed to do anything else
        this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
      }
    else
      {
        this.setTableDropDownValues();
        this.setTable(); 
      }
  }

    onDeleteConfirm(event:any): void {
    // console.log("User Deleted a row. Row data is  : ");
    //console.log(event);
    this.currentNumberRecord = JSON.parse(JSON.stringify(event.data));

    this.showTemplateVariable = false;
    this.showAction = false;


    if (window.confirm('Are you sure you want to delete?')) {

   //console.log("Starting Delete API");

   this.whatsappNumberService.delete(this.organization,this.currentNumberRecord.id)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {

        //console.log("API returned result");

        if(String(result) == 'true')
        {
              //console.log("Result is true");
              event.confirm.resolve();
              var output = JSON.parse(JSON.stringify(this.single));
              if(JSON.stringify(this.currentNumberRecord.active) == "\"true\""||JSON.stringify(this.currentNumberRecord.active) == "true"){
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
          this.showDialoge('Error','activity-outline','danger', 'Cannot delete until it has associated template.'); 
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


  onSaveConfirm(event:any): void {

    //console.log("User Edited a row. Row data is  : ");
    //console.log(event);
    //console.log(event);

    this.previousNumberRecord =  JSON.parse(JSON.stringify(event.data));
    this.currentNumberRecord = JSON.parse(JSON.stringify(event.newData));

    this.showTemplateVariable = false;
    this.showAction = true;

    var list= [];
    var singleObject = {value: this.currentNumberRecord.id, title:this.currentNumberRecord.id};
    list.push(singleObject);
    this.templateSettings.columns.whatsAppPhoneNumberId.editor.config.list = [... list];
    this.templateSettings = Object.assign({}, this.templateSettings);

    if (window.confirm('Are you sure you want to edit?')) {
    
     // console.log("Starting Update API");
    this.whatsappNumberService.update(JSON.stringify(this.currentNumberRecord),this.previousNumberRecord.phoneNumber)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {

        //console.log("API returned result");

        if(String(result) == 'true')
        {
            // console.log("Result is true");
             this.setTemplateTable();
             event.confirm.resolve();
             var output = JSON.parse(JSON.stringify(this.single));


             if(JSON.stringify(this.currentNumberRecord.active) == JSON.stringify(this.previousNumberRecord.active)){   
              //console.log("Both are same");
            }
            else{
              if(JSON.stringify(this.currentNumberRecord.active) == "\"true\""||JSON.stringify(this.currentNumberRecord.active) == "true")
              {
                //console.log("current record is true");
                output[0].value = output[0].value + 1;
                output[1].value = output[1].value - 1;
              }
              else if(JSON.stringify(this.currentNumberRecord.active) == "\"false\""||JSON.stringify(this.currentNumberRecord.active) == "false")
                {
                  //console.log("current record is false");
                  output[0].value = output[0].value - 1;
                  output[1].value = output[1].value + 1;
                
              }
              else
              {
                //console.log("I am something else");
                //console.log(this.currentNumberRecord.isactive);
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

  onCreateConfirm(event:any): void {
    console.log("User Created a row. Row data is  : ");
    this.currentNumberRecord = JSON.parse(JSON.stringify(event.newData));
    this.currentNumberRecord.organization = this.organization;

    this.showTemplateVariable = false;
    this.showAction = false;

    var list= [];
    var singleObject = {value: this.currentNumberRecord.id, title:this.currentNumberRecord.id};
    list.push(singleObject);
    this.templateSettings.columns.whatsAppPhoneNumberId.editor.config.list = [... list];
    this.templateSettings = Object.assign({}, this.templateSettings);

    this.currentNumberRecord.organization = ConstantsService.user.organization;
    this.currentNumberRecord.employeeExtensionAccessList = null;

    if (window.confirm('Are you sure you want to create?')) {

      console.log("Starting Create API with data :");
      console.log(this.currentNumberRecord);
      this.whatsappNumberService.create(JSON.stringify(this.currentNumberRecord))
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
   
           //console.log("API returned result");
   
           if(String(result) == 'true')
           {
                event.confirm.resolve();
                this.setTable();
                this.setTemplateTable();

           }
           else{
             //console.log("Result is not true");
             this.showDialoge('Error','activity-outline','danger', "You should create records for your organization. Record not created."); 
             event.confirm.reject();
           }
         },
         error: err => {
           console.log("Error : "+ JSON.stringify(err));
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

   onUserRowSelect(event:any): void {
    //console.log("User Selected a row. Row data is  : ");
    //console.log(event);
    this.currentNumberRecord = JSON.parse(JSON.stringify(event.data));
    
    this.showTemplateVariable = false;
    this.showAction = true;

    var list= [];
    var singleObject = {value: this.currentNumberRecord.id, title:this.currentNumberRecord.id};
    list.push(singleObject);
    this.templateSettings.columns.whatsAppPhoneNumberId.editor.config.list = [... list];
    this.templateSettings = Object.assign({}, this.templateSettings);
    
    //Load Template table data here
    this.setTemplateTable();
  }

  onTemplateDeleteConfirm(event:any){
    console.log("User Deleted a template row.");
    //console.log(event);
    this.currentTemplateRecord = JSON.parse(JSON.stringify(event.data));
    
    this.showTemplateVariable = false;
    this.showAction = true;

    if (window.confirm('Are you sure you want to delete?')) {
   //console.log("Starting Delete API");
   this.whatsappTemplateService.delete(this.organization,this.currentTemplateRecord.id)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {

        //console.log("API returned result");

        if(String(result) == 'true')
        {
              //console.log("Result is true");
              event.confirm.resolve();
              this.totalTemplateRecords = this.totalTemplateRecords - 1;
        }
        else{
          //console.log("Result is not true");
          event.confirm.reject();
          this.showDialoge('Error','activity-outline','danger', 'Cannot delete until it has associated template variables.'); 
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
  onTemplateSaveConfirm(event:any){
    console.log("User Updated a template row.");
    //console.log(event);

    this.previousTemplateRecord =  JSON.parse(JSON.stringify(event.data));
    this.currentTemplateRecord = JSON.parse(JSON.stringify(event.newData));

    this.showTemplateVariable = false;
    this.showAction = true;

    var list= [];
    var singleObject = {value: this.currentTemplateRecord.id, title:this.currentTemplateRecord.id};
    list.push(singleObject);
    this.templateVariableSettings.columns.whatsAppPhoneNumberTemplateId.editor.config.list = [... list];
    this.templateVariableSettings = Object.assign({}, this.templateVariableSettings);
    
    if (window.confirm('Are you sure you want to edit?')) {
    
     // console.log("Starting Update API");
    this.whatsappTemplateService.update(JSON.stringify(this.currentTemplateRecord))
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {

        //console.log("API returned result");

        if(String(result) == 'true')
        {
            // console.log("Result is true");
             event.confirm.resolve();
             this.setTemplateTable();
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

  onTemplateCreateConfirm(event){
    console.log("User Created a template row.");
    //console.log(event);
    this.currentTemplateRecord = JSON.parse(JSON.stringify(event.newData));
    
    this.showTemplateVariable = false;
    this.showAction = true;

    var list= [];
    var singleObject = {value: this.currentTemplateRecord.id, title:this.currentTemplateRecord.id};
    list.push(singleObject);
    this.templateVariableSettings.columns.whatsAppPhoneNumberTemplateId.editor.config.list = [... list];
    this.templateVariableSettings = Object.assign({}, this.templateVariableSettings);

     if (window.confirm('Are you sure you want to create?')) {

      //console.log("Starting Create API");
   
      this.currentTemplateRecord.whatsAppPhoneNumberId = this.currentNumberRecord.id;
      this.currentTemplateRecord.organization = ConstantsService.user.organization;
    
      this.whatsappTemplateService.create(JSON.stringify(this.currentTemplateRecord))
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
   
           console.log("API returned result");
           console.log("result");
           console.log(result);
           console.log("this.currentTemplateRecord");
           console.log(this.currentTemplateRecord);
   
           if(String(result) == 'true')
           {
                event.confirm.resolve();
                this.totalTemplateRecords = this.totalTemplateRecords + 1;
                this.setTemplateTable();
           }
           else{
             //console.log("Result is not true");
             this.showDialoge('Error','activity-outline','danger', "You should create records for your organization. Record not created."); 
             event.confirm.reject();
           }
         },
         error: err => {
           console.log("Error : "+ JSON.stringify(err));
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

  onTemplateUserRowSelect(event)
  {
    console.log("User Selected a template row.");
    //console.log(event);
    this.currentTemplateRecord = JSON.parse(JSON.stringify(event.data));
    
    this.showTemplateVariable = true;
    this.showAction = true;

    var list= [];
    var singleObject = {value: this.currentTemplateRecord.id, title:this.currentTemplateRecord.id};
    list.push(singleObject);
    this.templateVariableSettings.columns.whatsAppPhoneNumberTemplateId.editor.config.list = [... list];
    this.templateVariableSettings = Object.assign({}, this.templateVariableSettings);
    
    //Load Template table data here
    this.setTemplateVariableTable();
  }
  
  onTemplateVariableDeleteConfirm(event:any){
    console.log("User created a template variable row.");
    //console.log(event);
    this.currentTemplateVariableRecord = JSON.parse(JSON.stringify(event.data));
    
    if (window.confirm('Are you sure you want to delete?')) {

          //console.log("Starting Create API");
          let variableIndex = -1;
          variableIndex = this.allTemplateVariableRecords.findIndex(obj => obj.id == this.currentTemplateVariableRecord.id);

            if(variableIndex != -1){
                  this.allTemplateVariableRecords.splice(variableIndex,1);

                  //Send API now
                  this.whatsappTemplateVariableService.update(this.currentTemplateRecord.id,this.currentNumberRecord.organization,JSON.stringify(this.allTemplateVariableRecords))
                  .pipe(takeUntil(this.destroy$))
                  .subscribe({
                    next: result => {
              
                      //console.log("API returned result");
              
                      if(String(result) == 'true')
                      {
                            event.confirm.resolve();
                            this.setTemplateVariableTable();

                      }
                      else{
                        //console.log("Result is not true");
                        this.showDialoge('Error','activity-outline','danger', "You should create records for your organization. Record not created."); 
                        event.confirm.reject();
                      }
                    },
                    error: err => {
                      console.log("Error : "+ JSON.stringify(err));
                      //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
                      event.confirm.reject();
                    }
                  });
            }
            else{
              this.showDialoge('Error','activity-outline','danger', 'Template variable not found. Connect support team.'); 
              event.confirm.reject();
            }
       } 
       else {
         //console.log("User rejected delete");
         event.confirm.reject();
       }

  }


  onTemplateVariableSaveConfirm(event:any){
    console.log("User updated a template variable row.");
    //console.log(event);
    this.previousTemplateVariableRecord =  JSON.parse(JSON.stringify(event.data));
    this.currentTemplateVariableRecord = JSON.parse(JSON.stringify(event.newData)); 

    if (window.confirm('Are you sure you want to update?')) {

          //console.log("Starting Create API");
          let variableIndex = -1;
          variableIndex = this.allTemplateVariableRecords.findIndex(obj => obj.id == this.currentTemplateVariableRecord.id);

            if(variableIndex != -1){
                  this.allTemplateVariableRecords[variableIndex] = this.currentTemplateVariableRecord;

                  //Send API now
                  this.whatsappTemplateVariableService.update(this.currentTemplateRecord.id,this.currentNumberRecord.organization,JSON.stringify(this.allTemplateVariableRecords))
                  .pipe(takeUntil(this.destroy$))
                  .subscribe({
                    next: result => {
              
                      //console.log("API returned result");
              
                      if(String(result) == 'true')
                      {
                            event.confirm.resolve();
                            this.setTemplateVariableTable();

                      }
                      else{
                        //console.log("Result is not true");
                        this.showDialoge('Error','activity-outline','danger', "You should create records for your organization. Record not created."); 
                        event.confirm.reject();
                      }
                    },
                    error: err => {
                      console.log("Error : "+ JSON.stringify(err));
                      //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
                      event.confirm.reject();
                    }
                  });
            }
            else{
              this.showDialoge('Error','activity-outline','danger', 'Template variable not found. Connect support team.'); 
              event.confirm.reject();
            }
       } 
       else {
         //console.log("User rejected delete");
         event.confirm.reject();
       }

  }


  onTemplateVariableCreateConfirm(event){
  console.log("User created a template variable row.");
    //console.log(event);
    this.currentTemplateVariableRecord = JSON.parse(JSON.stringify(event.newData)); 

    this.currentTemplateVariableRecord.whatsAppPhoneNumberTemplateId = this.currentTemplateRecord.id;
    this.currentTemplateVariableRecord.organization = ConstantsService.user.organization;
    if (window.confirm('Are you sure you want to create?')) {

          //console.log("Starting Create API");
          this.allTemplateVariableRecords.push(this.currentTemplateVariableRecord);
          
          //Send API now
           this.whatsappTemplateVariableService.update(this.currentTemplateRecord.id,this.currentNumberRecord.organization,JSON.stringify(this.allTemplateVariableRecords))
                  .pipe(takeUntil(this.destroy$))
                  .subscribe({
                    next: result => {
                      //console.log("API returned result");
                      if(String(result) == 'true')
                      {
                            event.confirm.resolve();
                            this.setTemplateVariableTable();
                      }
                      else{
                        //console.log("Result is not true");
                        this.showDialoge('Error','activity-outline','danger', "You should create records for your organization. Record not created."); 
                        event.confirm.reject();
                      }
                    },
                    error: err => {
                      console.log("Error : "+ JSON.stringify(err));
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

  onTemplateVariableUserRowSelect(event)
  {
    console.log("User Selected a template variable row.");
    //console.log(event);
    this.currentTemplateVariableRecord = JSON.parse(JSON.stringify(event.data));  
  }

  
  assignEmployees(){
      this.showUplodEmployeeDialoge();
  }
  

  export(){
  this.exportAsConfigNumber.type = this.downloadAs;
    this.exportAsService
      .save(this.exportAsConfigNumber, 'WhatsAppNumbers')
      .subscribe(() => {
        // save started
      });
  }


  setTemplateVariableTable(){

     this.whatsappTemplateVariableService.findAllByWhatsAppNumberTemplateAndOrganization(this.currentTemplateRecord.id,this.currentNumberRecord.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {

        if(allData == null)
        {
          //console.log("I am null data");
          //console.log("Inside Data : "+ JSON.stringify(allData));
            this.templateVariableSource.load([]); 
            this.allTemplateVariableRecords = [];
            this.totalTemplateVariableRecords = 0;
        }
        else
        {
            //console.log("Inside Data : "+ JSON.stringify(allData));
              this.templateVariableSource.load(<any[]>allData); 
                   
              var arr = JSON.parse(JSON.stringify(allData));
              //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
              this.allTemplateVariableRecords = [...arr];
              this.totalTemplateVariableRecords = this.allTemplateVariableRecords.length;
        }
      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
       // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }


  setTemplateTable(){

    let data={
      phoneNumber: this.currentNumberRecord.phoneNumber,
	    organization:this.currentNumberRecord.organization
    }

     this.whatsappTemplateService.getAllByOrganizationAndWhatsAppPhoneNumber(data)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {

        if(allData == null)
        {
          //console.log("I am null data");
          //console.log("Inside Data : "+ JSON.stringify(allData));
            this.templateSource.load([]); 
            this.allTemplateRecords = [];
            this.totalTemplateRecords = 0;
        }
        else
        {
            //console.log("Inside Data : "+ JSON.stringify(allData));
              this.templateSource.load(<any[]>allData); 
                   
              var arr = JSON.parse(JSON.stringify(allData));
              //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
              this.allTemplateRecords = [...arr];
              this.totalTemplateRecords = this.allTemplateRecords.length;
        }
      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
       // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  setTable(){
    this.whatsappNumberService.getAllByOrganization(this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {

        if(allData == null)
        {
          //console.log("I am null data");
          //console.log("Inside Data : "+ JSON.stringify(allData));
            this.source.load([]); 
                
            this.allNumberRecords = [];

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
              this.allNumberRecords = [...arr];

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

                if (element.active == true) {
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


  setTableDropDownValues()
  {
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

              this.settings.columns.adminEmployeeId.editor.config.list = [... list];
              this.settings.columns.secondAdminEmployeeId.editor.config.list = [... list];
              this.settings = Object.assign({}, this.settings);
      
            },
            error: err => {
             console.log("Error : "+ JSON.stringify(err));
              // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
            }
          });


     this.productService.getAllproductsByOrganization(this.organization)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: allData => {
             // console.log("getAllproductsByOrganization");
              //console.log(JSON.stringify(allData));
              this.allProducts = [... JSON.parse(JSON.stringify(allData))];
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
                  singleObject.title = element.id+", "+element.name;
                  list.push( JSON.parse(JSON.stringify(singleObject)));
                });
              }

              this.templateSettings.columns.productId.editor.config.list = [... list];
              this.templateSettings = Object.assign({}, this.templateSettings);
      
            },
            error: err => {
             console.log("Error : "+ JSON.stringify(err));
              // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
            }
          });

     this.whatsappdictService.getAllWhatsAppCurrencyCodes(this.organization)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: allData => {
             // console.log("getAllWhatsAppCurrencyCodes");
              //console.log(JSON.stringify(allData));
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

              this.templateSettings.columns.currency.editor.config.list = [... list];
              this.templateSettings = Object.assign({}, this.templateSettings);
      
            },
            error: err => {
             console.log("Error : "+ JSON.stringify(err));
              // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
            }
          });

     this.whatsappdictService.getAllWhatsAppConversationTypes(this.organization)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: allData => {
             // console.log("getAllWhatsAppConversationTypes");
              //console.log(JSON.stringify(allData));
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

              this.templateSettings.columns.conversationType.editor.config.list = [... list];
              this.templateSettings = Object.assign({}, this.templateSettings);
      
            },
            error: err => {
             console.log("Error : "+ JSON.stringify(err));
              // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
            }
          });


      this.whatsappProjectService.getAllProjectByOrganization(this.organization)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: allData => {
            //  console.log("getAllProjectByOrganization");
            //   console.log(JSON.stringify(allData));
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
                  singleObject.title = element.id+'-'+element.appName;
                  list.push( JSON.parse(JSON.stringify(singleObject)));
                });
              }

              this.settings.columns.whatsAppProjectId.editor.config.list = [... list];
              this.settings = Object.assign({}, this.settings);
      
            },
            error: err => {
             console.log("Error : "+ JSON.stringify(err));
              // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
            }
          });

    this.whatsappdictService.getAllWhatsAppLanguageCode(this.organization)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: allData => {
             // console.log("getAllWhatsAppCurrencyCodes");
              //console.log(JSON.stringify(allData));
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

              this.templateSettings.columns.languageCode.editor.config.list = [... list];
              this.templateSettings = Object.assign({}, this.templateSettings);
      
            },
            error: err => {
             console.log("Error : "+ JSON.stringify(err));
              // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
            }
          });
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

  showUplodEmployeeDialoge() {
      var closeOnBackdropClick: boolean = false;
      var closeOnEsc: boolean = true;

      // console.log("Whats App Phone Number this.currentNumberRecord : "+JSON.stringify(this.currentNumberRecord));
      // console.log("Whats App Phone Number All Employees : "+JSON.stringify(this.allEmployees));
      // console.log("Whats App Phone Number this.currentNumberRecord.id : "+this.currentNumberRecord.id);
      // console.log("Whats App Phone Number All Employees : "+this.currentNumberRecord.employeeExtensionAccessList);

      this.dialogService.open(AssignEmployeesComponent, {closeOnBackdropClick,closeOnEsc,
        context: {
          allEmployees: this.allEmployees,
          id: this.currentNumberRecord.id,
          employeeExtensionAccessList : this.currentNumberRecord.employeeExtensionAccessList,
        },
      }).onClose.subscribe({
        next: (res) => {
             console.log('Dialog close having response : '+res);
            if(res === 'assign-employee-to-whatsapp-number-complete')
            {
               this.showAction = false;
               this.showTemplateVariable = false;
               this.setTable();
               this.showDialoge('Employees-Assigned','activity-outline','success', "Selected employees are assigned to number : "+this.currentNumberRecord.phoneNumber);
            } 
            else if(res === 'assign-employee-to-whatsapp-number-not-complete'){
              this.showAction = false;
               this.showTemplateVariable = false;
               this.setTable();
               this.showDialoge('Employees-Not-Assigned','activity-outline','danger', "Selected employees were not assigned to number : "+this.currentNumberRecord.phoneNumber);
            }
        },
        error: (err) => console.error(`Observer got an error: ${err}`),
      });
      }

    startEmbeddedSignup(): void {
      const org = this.organization;

      this.embeddedSignupBusy = true;
      this.embeddedSignupError = null;
      this.embeddedSignupResult = null;
      this.embeddedSessionInfo = null;
      this.embeddedPostMessageRaw = null;
      this.embeddedFinishEventReceived = false;
      this.embeddedFinishEventPayload = null;

      this.ensureFacebookSdkLoadedAndInit()
        .then(() => {

          // IMPORTANT: clear old session info right before login
          window.FB.login(async (loginResp: any) => {
            try {

              console.log('FB.login');
              console.log(loginResp);
              const auth = loginResp?.authResponse;

              // If user closed popup / cancelled, authResponse can be null
              if (!auth) {
                this.embeddedSignupError = 'User cancelled or login did not return authResponse.';
                this.embeddedSignupBusy = false;
                return;
              }

              const finished = await this.waitForFinishEvent(30000);
                if (!finished) {
                  this.embeddedSignupError =
                    'Coexistence flow did not finish (FINISH_WHATSAPP_BUSINESS_APP_ONBOARDING not received). ' +
                    'User likely didn’t complete the WhatsApp app “Connect to Business Platform” step.';
                  this.embeddedSignupBusy = false;
                  return;
                }

              // wait AFTER login window is opened and user completes flow
              const ok = await this.waitForEmbeddedFields(20000);
              if (!ok) {
                this.embeddedSignupError =
                  'Meta did not send sessionInfo (waba_id/phone_number_id/business_id). ' +
                  'Usually user did not finish WhatsApp Business app "Connect to Business Platform" step, or postMessage blocked.';
                this.embeddedSignupBusy = false;
                return;
              }

              const code = auth?.code;
              const state = auth?.state;

              const waba_id = this.embeddedSessionInfo?.waba_id ?? this.embeddedSessionInfo?.wabaId;
              const phone_number_id = this.embeddedSessionInfo?.phone_number_id ?? this.embeddedSessionInfo?.phoneId;
              const business_id = this.embeddedSessionInfo?.business_id ?? this.embeddedSessionInfo?.businessId;

              if (!code) {
                this.embeddedSignupError = 'Meta did not return code. Check config_id / app settings.';
                this.embeddedSignupBusy = false;
                return;
              }

              if (!waba_id || !phone_number_id || !business_id) {
                this.embeddedSignupError =
                  'Meta sessionInfo missing (waba_id / phone_number_id / business_id).';
                this.embeddedSignupBusy = false;
                return;
              }

              const payload: EmbeddedSignupResult = {
                organization: org,
                code,
                waba_id,
                phone_number_id,
                business_id,
                ...(state ? { state } : {})
              };

              this.embeddedSignupResult = payload;

              this.whatsappNumberService
                .embeddedSignupComplete(org, payload)
                .pipe(takeUntil(this.destroy$))
                .subscribe({
                  next: (_res) => {
                    this.showDialoge(
                      'Onboarding Complete',
                      'activity-outline',
                      'success',
                      'Embedded signup completed and records created.'
                    );

                    this.showAction = false;
                    this.showTemplateVariable = false;
                    this.setTable();
                    this.embeddedSignupBusy = false;
                  },
                  error: (err) => {
                    this.embeddedSignupError = JSON.stringify(err);
                    this.embeddedSignupBusy = false;
                  }
                });

            } catch (e: any) {
              this.embeddedSignupError = e?.message ?? String(e);
              this.embeddedSignupBusy = false;
              this.showDialoge('Onboarding Failure', 'activity-outline', 'danger', this.embeddedSignupError);
            }
          }, {
            config_id: ConstantsService.META_LOGIN_FOR_BUSINESS_CONFIG_ID,
            response_type: 'code',
            override_default_response_type: true,
            extras: {
              setup: {},
              featureType: 'whatsapp_business_app_onboarding',
              sessionInfoVersion: '3',
            }
          });

        })
        .catch((e) => {
          this.embeddedSignupError = e?.message ?? String(e);
          this.embeddedSignupBusy = false;
        });
    }

    private embeddedSignupMessageHandler = (event: MessageEvent) => {

      console.log('embeddedSignupMessageHandler');
      console.log(event);

      const origin = event.origin || '';

      // Keep this simple; Meta often uses facebook.com
      if (!origin.includes('facebook.com') && !origin.includes('meta.com')) return;


      let data: any = event.data;
      if (typeof data === 'string') {
        try { data = JSON.parse(data); } catch { return; }
      }

      
      // Keep raw for debugging
      this.embeddedPostMessageRaw = data;

      // Coexistence + embedded signup can send different shapes.
      // 1) sessionInfo is what you want for ids
      if (data?.sessionInfo) {
        this.embeddedSessionInfo = data.sessionInfo;
      }

      // 2) Some v3 events send { type:'WA_EMBEDDED_SIGNUP', event:'...', data:{...} }
      // If needed, you can also merge IDs from there:
      if (data?.type === 'WA_EMBEDDED_SIGNUP' && data?.data) {
        // best effort: some events only send waba_id here
        this.embeddedSessionInfo = {
          ...(this.embeddedSessionInfo || {}),
          ...(data.data || {}),
        };
      }

      // v3 finish event for coexistence
      if (data?.type === 'WA_EMBEDDED_SIGNUP' && data?.event === 'FINISH_WHATSAPP_BUSINESS_APP_ONBOARDING') {
        this.embeddedFinishEventReceived = true;
        this.embeddedFinishEventPayload = data; // for debugging if needed
      }

    };


      private ensureFacebookSdkLoadedAndInit(): Promise<void> {
        return new Promise((resolve, reject) => {
          if (window.FB) return resolve();

          window.fbAsyncInit = () => {
            try {
              window.FB.init({
                appId: ConstantsService.META_APP_ID,
                cookie: true,
                xfbml: false,
                version: ConstantsService.META_GRAPH_VERSION,
              });
              resolve();
            } catch (e) {
              reject(e);
            }
          };

          const id = 'facebook-jssdk';
          if (document.getElementById(id)) return;

          const js = document.createElement('script');
          js.id = id;
          js.async = true;
          js.defer = true;
          js.crossOrigin = 'anonymous';
          js.src = 'https://connect.facebook.net/en_US/sdk.js';
          js.onerror = () => reject(new Error('Failed to load Facebook SDK'));
          document.body.appendChild(js);
        });
      }

      private waitForFinishEvent(maxMs = 20000): Promise<boolean> {
        const start = Date.now();
        return new Promise((resolve) => {
          const tick = () => {
            if (this.embeddedFinishEventReceived) return resolve(true);
            if ((Date.now() - start) >= maxMs) return resolve(false);
            setTimeout(tick, 50);
          };
          tick();
        });
      }

   private waitForEmbeddedFields(maxMs = 20000): Promise<boolean> {
      const start = Date.now();

      const hasAll = () => {
        const s = this.embeddedSessionInfo || {};
        const waba = s.waba_id ?? s.wabaId ?? s.data?.waba_id;
        const phone = s.phone_number_id ?? s.phoneId ?? s.data?.phone_number_id;
        const biz = s.business_id ?? s.businessId ?? s.data?.business_id;
        return !!(waba && phone && biz);
      };

      return new Promise((resolve) => {
        const tick = () => {
          if (hasAll()) return resolve(true);
          if ((Date.now() - start) >= maxMs) return resolve(false);
          setTimeout(tick, 50);
        };
        tick();
      });
    }
}

declare global {
  interface Window {
    fbAsyncInit?: () => void;
    FB?: any;
  }
}


