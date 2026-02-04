import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output } from '@angular/core';
import { NbComponentStatus, NbDialogService, NbToastrService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { Router } from '@angular/router';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { BrowserPhoneService } from '../../../../service/browser-phone/browser-phone.service';
import { DialLineService } from '../../../../service/browser-phone/dial-line/dial-line.service';
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


  constructor(
    private dialogService: NbDialogService,
    protected router: Router,
    protected constantService : ConstantsService,
    protected browserPhoneService : BrowserPhoneService,
    protected dialLineService:DialLineService,) {

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
    //console.log(this.currentRecord.active);

    if(this.title == 'Make-A-Call')
    {
      this.on = true;
      this.show = 'ON';
    }
    
    if(this.title == 'Enable/Disable')
    {
      //console.log("Enable / Disable card intial value")
      if(!this.currentRecord.active)
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

    if(this.title == 'Make-A-Call')
    {
      this.on = true;
      this.show = 'ON';
    }

   // console.log("ngOnChanges")
    if(this.title == 'Enable/Disable')
    {
      //console.log("Enable / Disable card intial value")
      if(!this.currentRecord.active)
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
   if(this.title == 'Make-A-Call')
    {
      //Make a call to customer
      console.log("Make a call to customer");
      this.on = false;
      this.show = 'Making-Call';

      try{
           this.dialLineService.audioCall(this.browserPhoneService.userAgent,this.browserPhoneService.lang,this.currentRecord.supplierPhoneNumber,"",this.browserPhoneService.didLength);
      }
      catch(e)
      {
         console.log(e);
      }

      console.log("Turning back");
      setTimeout(()=>{
        this.on = true;
        this.show = 'Make-A-Call';
      },1500);

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
