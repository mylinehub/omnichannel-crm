import { AfterViewChecked, ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { LocalDataSource, Ng2SmartTableComponent } from 'ng2-smart-table';
import { CustomerService } from '../../../customer/service/customer.service';
import { CampaignService } from '../../../campaign/service/campaign.service';
import { NbDialogRef, NbDialogService, NbThemeService } from '@nebular/theme';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { Router } from '@angular/router';
import { takeUntil, takeWhile } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { CheckBoxComponent } from './check-box/check-box.component';
import { DialogComponent } from '../../../employee/all-employees/dialog/dialog.component';

@Component({
  selector: 'ngx-submit-bulk-customer',
  templateUrl: './submit-bulk-customer.component.html',
  styleUrls: ['./submit-bulk-customer.component.scss']
})
export class SubmitBulkCustomerComponent implements OnInit, AfterViewChecked {


  @Input() campaignid:any;
  @Output() parent:EventEmitter<any> = new EventEmitter();
  
  private destroy$: Subject<void> = new Subject<void>();
  tableHeading = "Upload Customer To Campaign Criteria";

  organization: string;
  redirectDelay: number = 0;
  colorScheme: { domain: any[]; };
  alive: boolean = true;

  showTotalRecordsPannel:any = false;
  totalRecords:number = 0;

  country:any = "";
  city:any = "";
  zip:any = "";

  isAndOperator:any = true;

  business:any = "";
  dataType:any = "";
  description:any = "";

  start:number = 0;
  limit:number = 0;

    constructor(private themeService: NbThemeService,
              protected router: Router,
              protected constantService : ConstantsService,
              private dialogService: NbDialogService,
              protected dialogRef: NbDialogRef<SubmitBulkCustomerComponent>,
              private cdr: ChangeDetectorRef,
              private campaignService:CampaignService){

                 console.log("I am in constructor");

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


  changeIsAndOperator()
  {
        this.isAndOperator = !(this.isAndOperator);
  }


  onSubmit()
  {

    let data = {
      campaignId: this.campaignid,
      zipCode: this.zip,
      city: this.city,
      country: this.country,
      organization: this.organization,
      datatype: this.dataType,
      business: this.business,
      description: this.description,
      start: this.start,
      limit: this.limit,
      isAndOperator: this.isAndOperator
    };

    this.campaignService.createCustomerToCampaignByOrganization(data,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: data => {
       // console.log("getAllEmployeesByOrganization");
        //console.log(JSON.stringify(allData));
        if(data != 0)
          {
            this.showTotalRecordsPannel=true;
            this.totalRecords = Number(data);
            this.dialogRef.close();
          }
          else
          {
            this.showTotalRecordsPannel=false;
            this.totalRecords = 0;
            this.dialogRef.close();
            this.showDialoge('Error','activity-outline','danger', JSON.stringify("Zero records found for this criteria. Try to change it."));
          }
      },
      error: err => {
       // console.log("Error : "+ JSON.stringify(err));
       this.dialogRef.close();
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

  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngAfterViewChecked(): void {
    //console.log("ngAfterViewChecked")
    this.syncTable();

  }

  getRecords(){

    let data = {
      campaignId: this.campaignid,
      zipCode: this.zip,
      city: this.city,
      country: this.country,
      organization: this.organization,
      datatype: this.dataType,
      business: this.business,
      description: this.description,
      start: this.start,
      limit: this.limit,
      isAndOperator: this.isAndOperator
    };

    this.campaignService.getCountForCustomerToCampaignByOrganization(data,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: data => {
       // console.log("getAllEmployeesByOrganization");
        //console.log(JSON.stringify(allData));
       if(data != null)
        {
          this.totalRecords = Number(data);
          this.showTotalRecordsPannel = true;
        }
        else
        {
          this.totalRecords =  0;
          this.showTotalRecordsPannel = false;
          this.showDialoge('Error','activity-outline','danger', JSON.stringify("Records not updated. Contact admin."));
        }
      },
      error: err => {
       // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  syncTable (): void {
    this.cdr.detectChanges();
  }

}
