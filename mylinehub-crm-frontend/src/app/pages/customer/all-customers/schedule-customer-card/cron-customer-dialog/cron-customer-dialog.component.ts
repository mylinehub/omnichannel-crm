import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { NbDialogRef, NbDialogService } from '@nebular/theme';
import { CustomerService } from './../../../service/customer.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { CronOptions } from 'cron-editor';
import { ConstantsService } from '../../../../../service/constants/constants.service';
import { DialogComponent } from '../../../../employee/all-employees/dialog/dialog.component';

@Component({
  selector: 'ngx-cron-customer-dialog',
  templateUrl: './cron-customer-dialog.component.html',
  styleUrls: ['./cron-customer-dialog.component.scss']
})
export class CronCustomerDialogComponent  implements OnInit, OnDestroy {
 
  heading = 'Schedule At Recurring Interval';
  @Input() scheduleTypes:any;
  @Input() scheduleType:any;
  @Input() organization:string;
  @Input() currentRecord:string;
  @Input() isAlreadyScheduled : string;
  @Input() selectedCallTypeModel : any;

  private destroy$: Subject<void> = new Subject<void>();

  public cronExpression = '0 12 1W 1/1 ?';
  public isCronDisabled = false;
  public cronOptions: CronOptions = {
    formInputClass: 'form-control cron-editor-input',
    formSelectClass: 'form-control cron-editor-select',
    formRadioClass: 'cron-editor-radio',
    formCheckboxClass: 'cron-editor-checkbox',

    defaultTime: '10:00:00',
    use24HourTime: true,

    hideMinutesTab: false,
    hideHourlyTab: false,
    hideDailyTab: false,
    hideWeeklyTab: false,
    hideMonthlyTab: false,
    hideYearlyTab: false,
    hideAdvancedTab: false,

    hideSeconds: true,
    removeSeconds: true,
    removeYears: true
  };

  constructor(protected ref: NbDialogRef<CronCustomerDialogComponent>,
              private dialogService: NbDialogService,
              private customerService: CustomerService,
              private constantsService: ConstantsService) {}

  cancel() {
    this.ref.close();
  }

  submit() {
    var submitCron  = "0 "+ this.cronExpression;

   // console.log("cron : "+ submitCron);

      let data : any = {
        campaignId : "",
        cronExpression:submitCron,
        organization:this.organization,
        domain:ConstantsService.user.domain,
        data:"",
        actionType:"actionType",
        phoneNumber:JSON.parse(JSON.stringify(this.currentRecord)).phoneNumber,
        callType:this.selectedCallTypeModel,
        fromExtension:ConstantsService.user.extension,
        context:ConstantsService.user.phoneContext,
        priority: 1,
        timeOut: 30000,
        firstName:ConstantsService.user.firstName,
        protocol:ConstantsService.user.protocol,
	      phoneTrunk:ConstantsService.user.phoneTrunk
      };

    this.customerService.scheduleCronCall(data)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: data => {
       // console.log(data);
        if(String(data) == 'true')
        {
          this.showDialoge('Success','done-all-outline','success', `Schedule Done`);
          this.isAlreadyScheduled = '{Already Scheduled (Schedule again to overwrite)}';

          var index = this.scheduleTypes.indexOf(this.scheduleType);
          // console.log("index :" ,index);
                if (index != -1) {
                  this.scheduleTypes.splice(index, 1);
                }
                
          this.scheduleTypes.push(this.constantsService.cron)


          this.ref.close();
             
              }
        else{
          this.showDialoge('Error','activity-outline','danger', "Please try again."); 
          this.ref.close();
        }

      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        this.ref.close();
      }
    });
  
  }

  ngOnInit(): void {
    //this.cronForm = new FormControl('0 0 1/1 * *');
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
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


/*



A Spring Scheduled tasks is like this:
1 2 3 4 5 6 Index
- - - - - -
* * * * * * command to be executed
- - - - - -
| | | | | | 
| | | | | ------- Day of week (MON - SUN)
| | | | --------- Month (1 - 12)
| | | ----------- Day of month (1 - 31)
| |-------------- Hour (0 - 23)
| --------------- Minute (0 - 59)
----------------- Seconds (0 - 59)
From: https://www.cyberciti.biz/faq/how-do-i-add-jobs-to-cron-under-linux-or-unix-oses/

A Linux Cron job is like this:
1 2 3 4 5 Index
- - - - -
* * * * * command to be executed
- - - - -
| | | | |
| | | | ----- Day of week (0 - 7) (Sunday=0 or 7)
| | | ------- Month (1 - 12)
| | --------- Day of month (1 - 31)
| ----------- Hour (0 - 23)
------------- Minute (0 - 59)

Side note:

Some article said it is possible to have a 7 optional param which is year , I have tried using latest spring and it show error, so I don't think it is working.
If your Linux cron job expression is simple enough, seems like it is possible to just put an 0 in front and it will convert to the spring scheduled tasks expression
E.g. Every 5 minutes

*/
