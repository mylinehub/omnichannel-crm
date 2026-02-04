import { AfterViewChecked, ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { LocalDataSource, Ng2SmartTableComponent } from 'ng2-smart-table';
import { CustomerService } from '../../../customer/service/customer.service';
import { CampaignService } from '../../../campaign/service/campaign.service';
import { NbDialogRef, NbDialogService, NbThemeService } from '@nebular/theme';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { Router } from '@angular/router';
import { takeUntil, takeWhile } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { DialogComponent } from '../../../employee/all-employees/dialog/dialog.component';
import { CustomInputTableComponent } from '../../../employee/all-employees/custom-input-table/custom-input-table.component';

@Component({
  selector: 'ngx-submit-bulk-employee',
  templateUrl: './submit-bulk-employee.component.html',
  styleUrls: ['./submit-bulk-employee.component.scss']
})
export class SubmitBulkEmployeeComponent implements OnInit, AfterViewChecked {

  @ViewChild('table') table: Ng2SmartTableComponent;
  
  @Input() allRecords:any = [];
  @Input() allEmployeeToCampaign:any = [];
  @Input() campaignid:any;
  @Output() parent:EventEmitter<any> = new EventEmitter();
  
  private destroy$: Subject<void> = new Subject<void>();
  tableHeading = "Upload Employee To Campaign Multi-Select";
  currentRecord:any = ' ';
  previousRecord:any = ' ';
  allSelectedRecords = new Map<number, []>();
  currentPage:number = 1;
  allSelectedRecordsToCheck:any = [];
  processStarted: boolean = false;


  
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

    constructor(private customerService : CustomerService,
              private themeService: NbThemeService,
              protected router: Router,
              protected constantService : ConstantsService,
              private dialogService: NbDialogService,
              protected dialogRef: NbDialogRef<SubmitBulkEmployeeComponent>,
              private cdr: ChangeDetectorRef,
              private campaignService:CampaignService){

                 console.log("I am in constructor");
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

  setTable()
  {
    var allToLoad = [...this.allRecords];


    this.allEmployeeToCampaign.forEach((element) => {

      
      const requiredIndex = allToLoad.findIndex(el => {
        return el.id === element.employeeid;
     });

     if(requiredIndex === -1){
      //return false;
      }
      else{
        !!allToLoad.splice(requiredIndex, 1); 
      }
      //element.id // customer ID
      //element.customerid
      //element.employeeid
    });


    this.source.load(allToLoad);
  }

  onUserRowSelect(event)
  {
    //console.log("User Selected a row. Row data is  : ");
    //console.log(event);
    this.currentRecord = JSON.parse(JSON.stringify(event.data));
    this.organization = localStorage.getItem("organization");
    //console.log("Current page :" +this.currentPage );
    
    this.allSelectedRecords.set(this.currentPage,JSON.parse(JSON.stringify(event.selected)));
   /* if(this.allSelectedRecords.has(this.currentPage))
    {
      this.allSelectedRecords.set(this.currentPage,JSON.parse(JSON.stringify(event.selected)));
    }
    else{
      this.allSelectedRecords.set(this.currentPage,JSON.parse(JSON.stringify(event.selected)));
    }*/
    
  }

  onSubmit()
  {
      //console.log("onSubmit");
      var input=[];

      this.allSelectedRecords.forEach((value: [], key: number) => {
        //console.log("key/value");
        //console.log(key, value);
        value.forEach((element) => {

          let currentValue =JSON.parse(JSON.stringify(element));
          
          let oneValue = {
            campaignid: 0,
            employeeid: 0,
            email: "no data",
            firstName: "no data",
            organization: "no data",
            phonenumber: "no data"
          };

          oneValue.campaignid = this.campaignid;
          oneValue.employeeid=currentValue.id;
          oneValue.email=currentValue.email;
          oneValue.firstName=currentValue.firstName;
          oneValue.organization=currentValue.organization;
          oneValue.phonenumber=currentValue.phonenumber;

          input.push(oneValue);

          });
      });

      //console.log("Phone Context Search Started");
      this.campaignService.createEmployeeToCampaignByOrganization(input,this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
          this.dialogRef.close();
          this.parent.emit('bulk-employee-campaign');
          /*if(allData == null)
          {
              this.dialogRef.close();
          }
          else{
                this.dialogRef.close();
          }*/
        },
        error: err => {
        // console.log("Error : "+ JSON.stringify(err));
          this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });

  }

  onClose()
  {
     this.dialogRef.close();
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

   ngOnInit(){
    // console.log("I am in ngOnIt");
    this.setTable();  
    this.source.onChanged().subscribe(change =>
        {
          // console.log("source.onChanged()");

           switch(change.action)
           {
            case 'page': 
           // console.log(change);
           // console.log("Looping row");
            this.currentPage = change.paging.page;
           
            break;

            case 'sort': break;
            case 'load': break;
            case '"refresh"': break;
           }
        }
      )
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngAfterViewChecked(): void {
    //console.log("ngAfterViewChecked")
    this.syncTable();

  }

  syncTable (): void {
    this.table.grid.getRows().forEach((row) => {
      this.allSelectedRecordsToCheck=this.allSelectedRecords.get(this.currentPage);
      if(this.allSelectedRecordsToCheck != undefined)
      {
        if(this.allSelectedRecordsToCheck.find( r => r.id == row.getData().id)) {
          row.isSelected = true;
        }
      } 
    });

    this.cdr.detectChanges();
  }

}
