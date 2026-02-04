import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { NbDialogRef, NbDialogService } from '@nebular/theme';
import { CampaignService } from './../../../service/campaign.service';
import { EmployeeService } from './../../../../employee/service/employee.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ConstantsService } from '../../../../../service/constants/constants.service';
import { DialogComponent } from '../../../../employee/all-employees/dialog/dialog.component';

@Component({
  selector: 'ngx-fixed-date-dialog-stop',
  templateUrl: './fixed-date-dialog-stop.component.html',
  styleUrls: ['./fixed-date-dialog-stop.component.scss']
})
export class FixedDateDialogStopComponent  implements OnInit, OnDestroy {
 
  heading = 'Schedule At Fixed Date';
  inputDate : any;

  @Input() scheduleTypes:any;
  @Input() scheduleType: string;
  @Input() organization:string;
  @Input() currentRecord:string;
  @Input() isAlreadyScheduled : string;
  
  private destroy$: Subject<void> = new Subject<void>();

  constructor(protected ref: NbDialogRef<FixedDateDialogStopComponent>,
              private dialogService: NbDialogService,
              private campaignService: CampaignService,
              private constantsService: ConstantsService) {}

  submit() {

    this.campaignService.scheduleAFixedDateStopCampaign({campaignId : JSON.parse(JSON.stringify(this.currentRecord)).id,date:this.inputDate,organization:this.organization,fromExtension:JSON.parse(JSON.stringify(this.currentRecord)).extension,domain:JSON.parse(JSON.stringify(this.currentRecord)).domain,data:"data",actionType:"actionType"})
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: data => {
        console.log(data);
        if(String(data) == 'true')
        {
          this.showDialoge('Success','done-all-outline','success', `Schedule Done`);
          this.isAlreadyScheduled = '{Already Scheduled (Schedule again to overwrite)}';

          var index = this.scheduleTypes.indexOf(this.scheduleType);
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
