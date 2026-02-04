import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output } from '@angular/core';
import { NbComponentStatus, NbDialogService, NbToastrService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { EmployeeService } from '../../service/employee.service';
import { Router } from '@angular/router';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { takeUntil } from 'rxjs/operators';
import { DialogComponent } from '../../all-employees/dialog/dialog.component';

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


  constructor(private employeeService : EmployeeService,
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
    //console.log(this.currentRecord.callonnumber);
    if(this.title == 'Call On Mobile')
    {
      //console.log("Enable / Disable card intial value")
      if(!this.currentRecord.callonnumber)
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
    if(this.title == 'Call On Mobile')
    {
      //console.log("Enable / Disable card intial value")
      if(!this.currentRecord?.callonnumber)
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

    if(this.title == 'Call On Mobile')
    {
      if(this.currentRecord.callonnumber)
      {
        this.employeeService.disableSelfCallOnMobile( {email: this.currentRecord.email,organization: this.organization})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
              //console.log("Result is true, setting values to show on icon"); 
              this.on = false;
              this.show = 'OFF';
              this.currentRecord.callonnumber = false;
              this.parent.emit('Call On Mobile');
            }
            else{
               // console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "Connection was not disabled"); 
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
      }
      else
      {
        this.employeeService.enableSelfCallOnMobile( {email: this.currentRecord.email,organization: this.organization})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
             // console.log("Result is true, setting values to show on icon");
              this.on = true;
              this.show = 'ON';
              this.currentRecord.callonnumber = true;
              this.parent.emit('Call On Mobile');
            }
            else{
              //  console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "Connection was not disabled"); 
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });

      }
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
