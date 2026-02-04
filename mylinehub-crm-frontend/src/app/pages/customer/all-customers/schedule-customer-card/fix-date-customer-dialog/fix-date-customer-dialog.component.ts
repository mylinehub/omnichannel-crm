import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { NbDialogRef, NbDialogService } from '@nebular/theme';
import { CustomerService } from './../../../service/customer.service';
import { EmployeeService } from './../../../../employee/service/employee.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ConstantsService } from '../../../../../service/constants/constants.service';
import { DialogComponent } from '../../../../employee/all-employees/dialog/dialog.component';

@Component({
  selector: 'ngx-fix-date-customer-dialog',
  templateUrl: './fix-date-customer-dialog.component.html',
  styleUrls: ['./fix-date-customer-dialog.component.scss']
})
export class FixDateCustomerDialogComponent implements OnInit, OnDestroy {
 
  heading = 'Schedule At Fixed Date';
  inputDate : any;
  @Input() scheduleTypes:any;
  @Input() scheduleType:any;
  @Input() organization:string;
  @Input() currentRecord:string;
  @Input() isAlreadyScheduled : string;
  @Input() selectedCallTypeModel : any;

  private destroy$: Subject<void> = new Subject<void>();

  constructor(protected ref: NbDialogRef<FixDateCustomerDialogComponent>,
              private dialogService: NbDialogService,
              private customerService: CustomerService,
              private constantsService: ConstantsService) {}

  submit() {

    let data : any = {
      campaignId : "",
      date:this.inputDate,
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

    this.customerService.scheduleAFixedDateCall(data)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: data => {
        console.log(data);
        if(String(data) == 'true')
        {
          this.showDialoge('Success','done-all-outline','success', `Schedule Done`);
          this.isAlreadyScheduled = '{Already Scheduled (Schedule again to overwrite)}';

          var index = this.scheduleTypes.indexOf(this.scheduleType);
          // console.log("index :" ,index);
                if (index != -1) {
                  this.scheduleTypes.splice(index, 1);
                }
                
          this.scheduleTypes.push(this.constantsService.fixeddate);

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

  
  cancel() {
    this.ref.close();
  }

  ngOnInit(): void {
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
