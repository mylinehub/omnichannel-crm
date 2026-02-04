import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output } from '@angular/core';
import { NbComponentStatus, NbDialogService, NbToastrService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { CampaignService } from '../../service/campaign.service';
import { Router } from '@angular/router';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { takeUntil } from 'rxjs/operators';
import { NSecDialogStopComponent } from './n-sec-dialog-stop/n-sec-dialog-stop.component';
import { FixedDateDialogStopComponent } from './fixed-date-dialog-stop/fixed-date-dialog-stop.component';
import { CronDialogStopComponent } from './cron-dialog-stop/cron-dialog-stop.component';
import { DialogComponent } from '../../../employee/all-employees/dialog/dialog.component';

@Component({
  selector: 'ngx-schedule-stop-card',
  styleUrls: ['./schedule-stop-card.component.scss'],
  template: `
    <!--<nb-card (click)="on = !on" [ngClass]="{'off': !on}">
      <div class="icon-container">
        <div class="icon status-{{ type }}">
          <ng-content></ng-content>
        </div>
      </div>

      <div class="details">
        <div class="title h5">{{ title }}</div>
        <div class="status paragraph-2">{{ on ? 'ON' : 'OFF' }}</div>
      </div>
    </nb-card>-->

    <nb-card (click)="buttonClicked()" [ngClass]="{'off': !on}">
      <div class="icon-container">
        <div class="icon status-{{ type }}">
          <ng-content></ng-content>
        </div>
      </div>

      <div class="details">
        <div class="title h5">{{ title }}</div>
        <div class="status paragraph-2">{{show}}</div>
      </div>
    </nb-card>

  `,
})
export class ScheduleStopCardComponent implements OnInit, OnDestroy, OnChanges{
  
  @Input() scheduleTypes:any;
  @Input() scheduleType: string;
  @Input() title: string;
  @Input() isAlreadyScheduled : string;
  @Input() type: string;
  @Input() on = true;
  @Input() show = 'Action';
  @Input() currentRecord:any = ' ';
  @Output() parent:EventEmitter<any> = new EventEmitter();

  redirectDelay: number = 0;
  organization: any;
  private destroy$: Subject<void> = new Subject<void>();


  constructor(private campaignService : CampaignService,
    private dialogService: NbDialogService,
    protected router: Router,
    protected constantService : ConstantsService,) {

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
  }
  
 ngOnInit(): void {
   // console.log(JSON.stringify(this.currentRecord));
    //console.log(this.currentRecord.isactive);
    
  }

  ngOnChanges() {

   // console.log("ngOnChanges")
   
}   


  async buttonClicked()
  {
   // console.log("actionButtonClicked");

      if(this.title == 'Remove Job')
      {
        this.on = false;
        this.show = 'Removing';
        
        this.campaignService.removeStopScheduledCampaign(this.scheduleType,this.currentRecord.id,this.organization)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: result => {
              if(String(result) == 'true')
              {
                //console.log("Result is true, setting values to show on icon");
                this.on = true;
                this.show = 'Removed';
                
                var index = this.scheduleTypes.indexOf(this.scheduleType);
                if (index != -1) {
                  this.scheduleTypes.splice(index, 1);
                }

                if(this.scheduleTypes.length == 0)
                {
                  this.isAlreadyScheduled = '{Nothing Scheduled (Schedule Now)}';
                }

                this.scheduleType = "";
                
              }
              else{
                  //console.log("Result is not true");
                  this.showDialoge('Error','activity-outline','danger', "Refresh was not successful"); 
                  this.on = true;
                  this.show = 'Removal Unsuccessful';
              }
            },
            error: err => {
            // console.log("Error : "+ JSON.stringify(err));
              this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
            }
          });
      }
      if(this.title == 'At Fix Date')
      {
        this.on = false;
        this.show = 'Scheduling';
        
        this.showFixedDateDialoge();
      }
      if(this.title == 'After N Seconds')
      {
        this.on = false;
        this.show = 'Scheduling';
        
        this.showNSecondsDialoge();
      }
      if(this.title == 'Cron')
      {
        this.on = false;
        this.show = 'Scheduling';
        
        this.showCronDialoge();
      }
  }


  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /*showToast(status: NbComponentStatus, message:string) {
      this.nbToastrService.show(status,message, { status });
    }*/

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

      showNSecondsDialoge() {

        this.dialogService.open(NSecDialogStopComponent, {
          context: {
            currentRecord: this.currentRecord,
            organization: this.organization,
            isAlreadyScheduled: this.isAlreadyScheduled,
            scheduleTypes:this.scheduleTypes,
            scheduleType:this.constantService.afternseconds,
          },
        }).onClose.subscribe({
          next: (res) => {
                  this.on = true;
                  this.show = 'Schedule Overwrite';
          },
          error: (err) => console.error(`Observer got an error: ${err}`),
        });
        }

       showFixedDateDialoge() {

      this.dialogService.open(FixedDateDialogStopComponent, {
        context: {
          currentRecord: this.currentRecord,
          organization: this.organization,
          isAlreadyScheduled: this.isAlreadyScheduled,
          scheduleTypes:this.scheduleTypes,
          scheduleType:this.constantService.fixeddate,
        },
      }).onClose.subscribe({
        next: (res) => {
                this.on = true;
                this.show = 'Schedule Overwrite';
        },
        error: (err) => console.error(`Observer got an error: ${err}`),
      });
      }

    showCronDialoge() {

          this.dialogService.open(CronDialogStopComponent, {
            context: {
              currentRecord: this.currentRecord,
              organization: this.organization,
              isAlreadyScheduled: this.isAlreadyScheduled,
              scheduleTypes:this.scheduleTypes,
              scheduleType:this.constantService.cron,
            },
          }).onClose.subscribe({
            next: (res) => {
                    this.on = true;
                    this.show = 'Schedule Overwrite';
            },
            error: (err) => console.error(`Observer got an error: ${err}`),
          });
          }
}

