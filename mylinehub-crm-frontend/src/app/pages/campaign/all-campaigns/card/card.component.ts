import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output } from '@angular/core';
import { NbComponentStatus, NbDialogService, NbToastrService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { CampaignService } from '../../service/campaign.service';
import { Router } from '@angular/router';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { takeUntil } from 'rxjs/operators';
import { DialogComponent } from '../../../employee/all-employees/dialog/dialog.component';

@Component({
  selector: 'ngx-card',
  styleUrls: ['./card.component.scss'],
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
export class CardComponent implements OnInit, OnDestroy, OnChanges{
  
  
  @Input() title: string;
  @Input() type: string;
  @Input() on = true;
  @Input() show = 'Action';
  @Input() currentRecord:any = ' ';
  @Output() parent:EventEmitter<any> = new EventEmitter();

  redirectDelay: number = 0;
  organization: any;
  private destroy$: Subject<void> = new Subject<void>();
  private startStopInFlight = false;   // guards Start/Stop double click


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
    if(this.title == 'Start/Stop')
    {
      //console.log("Enable / Disable card intial value")
      if(!this.currentRecord.isactive)
      {
        //console.log("Value is false");
        this.on = false;
        this.show = 'OFF';
      }
      else
      {
       // console.log("Value is true");
        this.on = true;
        this.show = 'ON';
      }
    }
  }

  ngOnChanges() {

   // console.log("ngOnChanges")
    if(this.title == 'Start/Stop')
    {
      //console.log("Enable / Disable card intial value")
      if(!this.currentRecord.isactive)
      {
       // console.log("Value is false");
        this.on = false;
        this.show = 'OFF';
      }
      else
      {
        //console.log("Value is true");
        this.on = true;
        this.show = 'ON';
      }
    }
}   


  async buttonClicked()
  {
   // console.log("actionButtonClicked");

    if(this.title == 'Start/Stop')
    {
      if (this.startStopInFlight) {
        this.showDialoge(
          'Please wait',
          'activity-outline',
          'danger',
          'Button is already pressed. Please wait...'
        );
        return;
      }

      this.startStopInFlight = true;
      if(this.currentRecord.isactive)
      {
        this.campaignService.stopCampaignByOrganization(this.currentRecord,this.organization)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            this.startStopInFlight = false;
            if(String(result) == 'true')
            {
              //console.log("Result is true, setting values to show on icon"); 
              this.on = false;
              this.show = 'OFF';
              this.currentRecord.isactive = false;
              this.parent.emit('Start/Stop');
            }
            else{
               // console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "Campaign was not stopped"); 
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.startStopInFlight = false;
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err));
          }
        });
      }
      else
      {
        this.campaignService.startCampaignByOrganization(this.currentRecord,this.organization)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
             this.startStopInFlight = false;
            if(String(result) == 'true')
            {
             // console.log("Result is true, setting values to show on icon");
              this.on = true;
              this.show = 'ON';
              this.currentRecord.isactive = true;
              this.parent.emit('Start/Stop');
            }
            else{
              //  console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "Campaign was not started. Make sure it's unpaused, reset & within user limit."); 
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.startStopInFlight = false;
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });

      }
    }
    else if(this.title == 'Pause')
    {
      this.on = false;
      this.show = 'Pausing';
      
      this.campaignService.pauseCampaignByOrganization(this.currentRecord,this.organization)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
              //console.log("Result is true, setting values to show on icon");
              this.on = true;
              this.show = 'Paused';
            }
            else{
                //console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "Pause was not successful"); 
                this.on = true;
                this.show = 'Pause Unsuccessful';
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
    }
    else if(this.title == 'Unpause')
    {
      this.on = false;
      this.show = 'Unpausing';
      
      this.campaignService.unpauseCampaignByOrganization(this.currentRecord,this.organization)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
              //console.log("Result is true, setting values to show on icon");
              this.on = true;
              this.show = 'Unpaused';
            }
            else{
                //console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "Unpause was not successful"); 
                this.on = true;
                this.show = 'Refresh Unsuccessful';
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
    }
    else if(this.title == 'Reset')
    {
      this.on = false;
      this.show = 'Reseting';

      this.campaignService.resetCampaignByOrganization(this.currentRecord,this.organization)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
              //console.log("Result is true, setting values to show on icon");
              this.on = true;
              this.show = 'Reset Done';
            }
            else{
                //console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "Reset was not successful"); 
                this.on = true;
                this.show = 'Connection Unsuccessful';
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
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

}
