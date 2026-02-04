import { AfterViewChecked, ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { LocalDataSource, Ng2SmartTableComponent } from 'ng2-smart-table';
import { NbDialogRef, NbDialogService, NbThemeService } from '@nebular/theme';
import { Router } from '@angular/router';
import { takeUntil, takeWhile } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { ConstantsService } from '../../../service/constants/constants.service';
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';
import { WhatsappNumberService } from '../../../service/whatsapp-number/whatsapp-number.service';
import { CustomInputTableComponent } from '../../employee/all-employees/custom-input-table/custom-input-table.component';

@Component({
  selector: 'ngx-assign-employees',
  templateUrl: './assign-employees.component.html',
  styleUrls: ['./assign-employees.component.scss']
})
export class AssignEmployeesComponent implements OnInit {
// export class AssignEmployeesComponent implements OnInit, AfterViewChecked {

  @ViewChild('table') table: Ng2SmartTableComponent;
  
  @Input() allEmployees:any = [];
  @Input() employeeExtensionAccessList:any = [];
  @Input() id:any;
  @Output() parent:EventEmitter<any> = new EventEmitter();
  
  private destroy$: Subject<void> = new Subject<void>();
  tableHeading = "Assign-Employees";
  currentRecord:any = ' ';
  previousRecord:any = ' ';
  allSelectedRecords = [];
  currentPage:number = 1;
  syncAllowed: boolean = true;


  
  settings = {
    selectMode: 'multi',
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
    pager: {
      display: true,
      perPage: 4
    },
    actions: {
      add: false,      //  if you want to remove add button
      edit: false,     //  if you want to remove edit button
      delete: false //  if you want to remove delete button
    },

    columns: {
          sizeMediaUploadInMB:{
            title: 'Size (MB)',
            type: 'string',
            hide: true,
          },
          id: {
            title: 'ID',
            type: 'number',
            editable: false,
            addable: false,
             hide:true,
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
          },
          sex: {
            title: 'Sex',
            type: 'string',
             hide:true,
          },
          phonenumber: {
            title: 'Phone  Number',
            type: 'custom',
            valuePrepareFunction: (cell,row) => {
                    // DATA FROM HERE GOES TO renderComponent
                    console.log('valuePrepareFunction');
                    console.log(cell);
                    return cell;
            },
            renderComponent: CustomInputTableComponent,
             hide:true,
          },
          allotednumber1: {
            title: 'Alloted Number 1',
            type: 'string',
            hide: true,
          },
          allotednumber2: {
            title: 'Alloted Number 2',
            type: 'string',
            hide: true,
          },
          costCalculation: {
            title: 'Cost Calculation',
           /* valuePrepareFunction: (value) => { 
    
             console.log("*******************************************");
             console.log("*******************************************");
             console.log("*******************************************");
             console.log(value);
              
              return value 
            },*/
            editor: {
              type: 'list',
              config: {
                selectText: 'Select',
                list: []
              }
             },
             hide: true,
          },
          amount: {
            title: 'Amount',
            editor: {
              type: 'list',
              config: {
                selectText: 'Select',
                list: []
              }
             },
             hide: true,
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
          },
          domain: {
            title: 'Domain',
            type: 'string',
            hide: true,
          },
          secondDomain: {
            title: 'Second Domain',
            type: 'string',
            hide: true,
          },
          email: {
            title: 'Email',
            type: 'string',
             hide:true,
          },
          extensionPrefix: {
            title: 'Extension Prefix',
            type: 'string',
            hide: true,
          },
          extension: {
            title: 'Extension',
            type: 'string',
             hide:true,
          },
          lastConnectedCustomerPhone: {
            title: 'Last Connected Customer',
            type: 'string',
             hide:true,
          },
          confExtensionPrefix: {
            title: 'Conference Prefix',
            type: 'string',
            hide: true,
          },
          confExtension: {
            title: 'Conference Extension',
            type: 'string',
            hide: true,
          },
          extensionpassword: {
            title: 'Extension Password',
            type: 'string',
            hide:true,
          },
          governmentDocument1Data: {
            title: 'Doc One Data',
            type: 'string',
            hide: true,
          },
          governmentDocument2Data: {
            title: 'Doc Two Data',
            type: 'string',
            hide: true,
          },
          governmentDocumentID1: {
            title: 'Doc One ID',
            type: 'string',
            hide: true,
          },
          governmentDocumentID2: {
            title: 'Doc Two ID',
            type: 'string',
            hide: true,
          },
          imageData: {
            title: 'Image Data',
            type: 'string',
            hide: true,
          },
          iconImageData: {
            title: 'Image Icon Data',
            type: 'string',
            hide: true,
          },
          iconImageByteData: {
            title: 'Image Icon Data',
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
          doc1ImageType: {
            title: 'Doc1 Image Type',
            type: 'string',
            hide: true,
          },
          doc2ImageType: {
            title: 'Doc2 Image Type',
            type: 'string',
            hide: true,
          },
          parkedchannel1: {
            title: 'Parked Channel 1',
            type: 'string',
            hide:true,
          },
          parkedchannel2: {
            title: 'Parked Channel 2',
            type: 'string',
            hide:true,
          },
          parkedchannel3: {
            title: 'Parked Channel 3',
            type: 'string',
            hide:true,
          },
          parkedchannel4: {
            title: 'Parked Channel 4',
            type: 'string',
            hide:true,
          },
          parkedchannel5: {
            title: 'Parked Channel 5',
            type: 'string',
            hide:true,
          },
          parkedchannel6: {
            title: 'Parked Channel 6',
            type: 'string',
            hide:true,
          },
          parkedchannel7: {
            title: 'Parked Channel 7',
            type: 'string',
            hide:true,
          },
          parkedchannel8: {
            title: 'Parked Channel 8',
            type: 'string',
            hide:true,
          },
          parkedchannel9: {
            title: 'Parked Channel 9',
            type: 'string',
            hide:true,
          },
          parkedchannel10: {
            title: 'Parked Channel 10',
            type: 'string',
            hide:true,
          },
          password: {
            title: 'Password',
            type: 'string',
            hide:true,
          },
          pesel: {
            title: 'Pesel',
            type: 'string',
             hide:true,
          },
          phoneContext: {
            title: 'Phone Context',
            type: 'string',
            hide: true,
          },
          phoneTrunk: {
            title: 'Phone Trunk',
            type: 'string',
            hide: true,
          },
          protocol: {
            title: 'Protocol',
            type: 'string',
            hide: true,
          },
          provider1: {
            title: 'Provider 1',
            type: 'string',
            hide: true,
          },
          provider2: {
            title: 'Provider 2',
            type: 'string',
            hide: true,
          },
          role: {
            title: 'Role',
            type: 'string',
             hide:true,
          },
          timezone: {
            title: 'Timezone',
            type: 'string',
             hide:true,
          },
          transfer_phone_1: {
            title: 'Manager Extension',
            type: 'custom',
            valuePrepareFunction: (cell,row) => {
                    // DATA FROM HERE GOES TO renderComponent
                    console.log('valuePrepareFunction');
                    console.log(cell);
                    return cell;
            },
            renderComponent: CustomInputTableComponent,
             hide:true,
          },
          transfer_phone_2: {
            title: 'Head Extension',
            type: 'custom',
            valuePrepareFunction: (cell,row) => {
                    // DATA FROM HERE GOES TO renderComponent
                    console.log('valuePrepareFunction');
                    console.log(cell);
                    return cell;
            },
            renderComponent: CustomInputTableComponent,
             hide:true,
          },
          type: {
            title: 'Employee type',
            type: 'string',
             hide:true,
          },
          departmentId: {
            title: 'Department ID',
            type: 'number',
            hide:true
          },
          salary: {
            title: 'Salary',
            type: 'number',
             hide:true,
          },
          totalparkedchannels: {
            title: 'Total Parked Channels',
            type: 'number',
            hide:true,
          },
          birthdate: {
            title: 'Birth Date',
            type: 'date',
             hide:true,
          },
          isEnabled: {
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
          useSecondaryAllotedLine: {
            title: 'Use Second Line',
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
          callonnumber: {
            title: 'Call On Mobile',
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
          isLocked: {
            title: 'Is Locked',
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
          recordAllCalls: {
            title: 'Record Calls',
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
          intercomPolicy: {
            title: 'Intercom Policy',
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
          freeDialOption: {
            title: 'Free Dial',
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
          textDictateOption: {
            title: 'Text Dictation',
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
          textMessagingOption: {
            title: 'Text Messaging',
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
          organization: {
            title: 'Organization',
            type: 'string',
            hide:true,
          },
          uiTheme: {
            title: 'UI Theme',
            type: 'string',
            hide:true,
          },
          autoAnswer: {
            title: 'Auto Answer',
            type: 'string',
            hide:true,
          },
          autoConference: {
            title: 'Auto Conference',
            type: 'string',
            hide:true,
          },
          autoVideo: {
            title: 'Auto Video',
            type: 'string',
            hide:true,
          },
          micDevice: {
            title: 'Mic Device',
            type: 'string',
            hide:true,
          },
          speakerDevice: {
            title: 'Speaker Device',
            type: 'string',
            hide:true,
          },
          videoDevice: {
            title: 'Video Device',
            type: 'string',
            hide:true,
          },
          videoOrientation: {
            title: 'Video Orientation',
            type: 'string',
            hide:true,
          },
          videoQuality: {
            title: 'Video Quality',
            type: 'string',
            hide:true,
          },
          videoFrameRate: {
            title: 'Video Frame Rate',
            type: 'string',
            hide:true,
          },
          autoGainControl: {
            title: 'Auto Gain Control',
            type: 'string',
            hide:true,
          },
          echoCancellation: {
            title: 'Echo Cancellation',
            type: 'string',
            hide:true,
          },
          noiseSupression: {
            title: 'Noise Supression',
            type: 'string',
            hide:true,
          },
          sipPort: {
            title: 'Sip Port',
            type: 'string',
            hide:true,
          },
          sipPath: {
            title: 'Sip Path',
            type: 'string',
            hide:true,
          },
          doNotDisturb: {
            title: 'Do Not Disturb',
            type: 'string',
            hide:true,
          },
          startVideoFullScreen: {
            title: 'Start Video Full Screen',
            type: 'string',
            hide:true,
          },
          callWaiting: {
            title: 'Start Video Full Screen',
            type: 'string',
            hide:true,
          },
          notificationDot: {
            title: 'Start Video Full Screen',
            type: 'string',
            hide:true,
          },
          allowedToSwitchOffWhatsAppAI: {
            title: 'allowedToSwitchOffWhatsAppAI',
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
        },
  };


  source: LocalDataSource = new LocalDataSource();
  organization: string;
  redirectDelay: number = 0;
  colorScheme: { domain: any[]; };
  alive: boolean = true;

    constructor(private themeService: NbThemeService,
              protected router: Router,
              protected constantService : ConstantsService,
              private dialogService: NbDialogService,
              protected dialogRef: NbDialogRef<AssignEmployeesComponent>,
              private cdr: ChangeDetectorRef,
              private whatsappNumberService:WhatsappNumberService,){

                 console.log("I am in constructor of AssignEmployeesComponent");

                 this.table =<any> document.getElementById('table');

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
  }

  ngOnInit(){
    console.log("I am in ngOnIt");

    console.log("syncTable allowed is made true");
    this.syncAllowed = true;

    console.log("Input Variable allRecords :"+JSON.stringify(this.allEmployees));
    console.log("Input Variable allEmployeeAssignedToNumber :"+JSON.parse(this.employeeExtensionAccessList));
    console.log("Input Variable numberId :"+this.id);

    if(this.employeeExtensionAccessList == null || this.employeeExtensionAccessList == 'null'  || this.employeeExtensionAccessList === '' || this.employeeExtensionAccessList === ' '){
        console.log('Employee Access Extension List was null . Making it empty')
        this.employeeExtensionAccessList = [];
        this.allSelectedRecords = [];
    }
    else{
      this.allSelectedRecords = [... JSON.parse(this.employeeExtensionAccessList)];
    }
 
    if(this.allEmployees == null || this.allEmployees == 'null'  || this.allEmployees === '' || this.allEmployees === ' '){
        console.log('All Employee List was null . Making it empty')
        this.allEmployees = [];
    } 

    console.log("Input Variable allSelectedRecords :"+JSON.stringify(this.allSelectedRecords));

    this.source.onChanged().subscribe(change =>
        {
          console.log("source.onChanged()");

           switch(change.action)
           {
            case 'page': 
                console.log(change);
                console.log("Source Page Changed");
                this.currentPage = change.paging.page;
                this.syncAllowed = true;
            break;
            case 'sort':
                console.log("Source Sorted");
                this.syncAllowed = true;          
            break;
            case 'load': 
                console.log("Source Loaded");
                this.syncAllowed = true;
            break;
            case 'refresh': 
                console.log("Source refreshed");
                this.syncAllowed = true;
            break;
            case 'filter':
              console.log("Source filtered");
              this.syncAllowed = true;
            break;
            default:
                console.log("Action");
                console.log(change.action);
            break;
           }
        }
      );
        
    this.setTable();  
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }


  setTable()
  {
    var allToLoad = [...this.allEmployees];
    this.source.load(allToLoad);
  }

  onUserRowSelect(event)
  {
    console.log("User Selected a row.");
    console.log("Current page :" +this.currentPage );
    console.log(event);
    console.log("event.data");
    console.log(event.data);
    console.log("event.selected");
    console.log(event.selected);

    this.currentRecord = event.data;
    console.log("this.currentRecord :" + this.currentRecord);
    this.organization = localStorage.getItem("organization");

    if((event.data === null) && (this.table.isAllSelected)){
        console.log("this.table.isAllSelected = true , adding all values");
        this.allSelectedRecords = [... this.allEmployees];
       }
    else{
        console.log("this.table.isAllSelected = false or else data is null");

        if(event.data === null){
              console.log("Header row checkbox was clicked as data is null");
              this.allSelectedRecords = [];
              this.employeeExtensionAccessList = [];
        }else{
            
              console.log("Record row checkbox was clicked");
              console.log("event.isSelected : "+ event.isSelected)
               if(String(event.isSelected) === 'true'){
                console.log("Finding element index");
                const index = this.allSelectedRecords.findIndex(employee => employee.id === event.data.id); // index will be 1
                if(index === -1){
                    console.log("Adding element to list");
                    this.allSelectedRecords.push(this.currentRecord);
                }
              }
              else{

                console.log("If select all is true, make it false");
                console.log("Finding element index");
                const index = this.allSelectedRecords.findIndex(employee => employee.id === event.data.id); // index will be 1
                if(index !== -1){
                    console.log("Removing element at index :"+index);
                    this.allSelectedRecords.splice(index, 1);

                    if(String(this.table.isAllSelected) === 'true'){
                    console.log("Select all was true, as we deselected row, making false now");
                    this.syncAllowed = true;
                    this.cdr.detectChanges();
                }
                }
              }
        }
    }

  }

  onSubmit()
  {
      console.log("onSubmit");
      var input=[];

      console.log("After merging both lists");
      console.log("this.allSelectedRecords : "+ this.allSelectedRecords);

      this.allSelectedRecords.forEach((element) => {
        let currentValue =JSON.parse(JSON.stringify(element));

          let oneValue = {
            id: 0,
            firstName: 0,
            lastName: "no data",
            role: "no data",
            departmentName: "no data",
            phonenumber: "no data",
            extension: "no data",
            email: "no data"
          };


          oneValue.id = currentValue.id;
          oneValue.firstName=currentValue.firstName;
          oneValue.lastName=currentValue.lastName;
          oneValue.role=currentValue.role;
          oneValue.departmentName=currentValue.departmentName;
          oneValue.phonenumber=currentValue.phonenumber;
          oneValue.extension=currentValue.extension;
          oneValue.email=currentValue.email;

          input.push(oneValue);
      });

          
      console.log("Prepared data");
      let data={
            id:this.id,
            employeeExtensionAccessList:input,
            organization:this.organization
          }

      console.log("Sending request");
      this.whatsappNumberService.updateEmployeeAccessListByOrganization(data)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
          // this.parent.emit('assign-employee-to-whatsapp-number-complete');
          
          if(String(allData) == 'true')
          {
              this.dialogRef.close('assign-employee-to-whatsapp-number-complete');
          }
          else{
                this.dialogRef.close('assign-employee-to-whatsapp-number-not-complete');
          }
        },
        error: err => {
        // console.log("Error : "+ JSON.stringify(err));
          this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });

  }


   
  ngAfterViewChecked(): void {
    console.log("ngAfterViewChecked")
    this.syncTableUI();
  }

  syncTableUI (): void {

    console.log("syncTable");
    // console.log("this.table.grid.getRows() : "+JSON.stringify(this.table.grid.getRows()));
    if(this.syncAllowed){
        console.log("syncTable is allowed");
        this.syncAllowed = false;

        
        console.log("this.allSelectedRecords.length : "+this.allSelectedRecords.length);
        console.log("this.allEmployees.length : "+this.allEmployees.length);

        if(this.allSelectedRecords.length === this.allEmployees.length){
             //mark upper checkbox
             console.log("Marking select all in sync UI");
             this.table.isAllSelected = true;
        }
        else{
              //Make sure upper check box is not checked
              console.log("Demarking select all in sync UI");
              this.table.isAllSelected = false;
        }

        this.table.grid.getRows().forEach((row) => {
          console.log("Element added to list");
          const index = this.allSelectedRecords.findIndex(employee => {
            console.log("employee :"+JSON.stringify(employee) );
            // console.log("row.getData() :"+row.getData());
            console.log("employee.id :"+employee.id );
            console.log("row.getData().id :"+row.getData().id );
            return employee.id === row.getData().id
          }); // index will be 1
          if(index !== -1){
              console.log('Making row true having id : '+row.getData().id);
              row.isSelected = true;
          }
        });

        this.cdr.detectChanges();

    }
    else{
        console.log("syncTable is not allowed");
    }
  }


  onClose()
  {
     this.dialogRef.close('closed');
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

