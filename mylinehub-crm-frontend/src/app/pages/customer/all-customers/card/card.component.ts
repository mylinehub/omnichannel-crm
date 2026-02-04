import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output } from '@angular/core';
import { NbComponentStatus, NbDialogService, NbToastrService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { Router } from '@angular/router';
import { CustomerService } from '../../service/customer.service';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { takeUntil } from 'rxjs/operators';
import { BrowserPhoneService } from '../../../../service/browser-phone/browser-phone.service';
import { ProductInterestDialogComponent } from '../../preview-customer/product-interest-dialog/product-interest-dialog.component';
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
    private customerService:CustomerService,
    protected browserPhoneService : BrowserPhoneService,
    protected dialLineService:DialLineService,) {

      // console.log("I am in constructor");

      if(localStorage.getItem("organization")!=null)
       {
        this.organization = localStorage.getItem("organization");
       }
       else{
        
        setTimeout(() => {
            // console.log('Routing to dashboard page');
             return this.router.navigateByUrl(this.constantService.LOGIN_ENDPOINT);
           }, this.redirectDelay);
      }
  }
  
 ngOnInit(): void {
  //  console.log(JSON.stringify(this.currentRecord));
  //   console.log(this.currentRecord.coverted);
  //   console.log(this.title);

    if(this.title == 'Interested Products')
    {
      this.on = true;
      this.show = 'ON';
    }

    if(this.title == 'Make-A-Call')
    {
      this.on = true;
      this.show = 'ON';
    }

    if(this.title == 'Convert/Divert')
    {
      // console.log("Enable / Disable card intial value")
      if(!this.currentRecord.coverted)
      {
        // console.log("Value is false");
        this.on = false;
        this.show = 'OFF';
      }
      else
      {
      //  console.log("Value is true");
        this.on = true;
        this.show = 'ON';
      }
    }
  }

  ngOnChanges() {

    //  console.log("ngOnChanges")
    // console.log(this.title)
    if(this.title == 'Interested Products')
    {
      this.on = true;
      this.show = 'ON';
    }

    if(this.title == 'Make-A-Call')
    {
      this.on = true;
      this.show = 'ON';
    }

    if(this.title == 'Convert/Divert')
    {
    //  console.log("Convert/Divert card intial value")
      if(!this.currentRecord.coverted)
      {
      // console.log("Value is false");
        this.on = false;
        this.show = 'OFF';
      }
      else
      {
      //  console.log("Value is true");
        this.on = true;
        this.show = 'ON';
      }
    }
}   


  async buttonClicked()
  {

    if(this.title == 'Interested Products')
    {

      this.on = false;
      this.show = 'Change Now';
        
      this.showProductInterestDialoge();

    }
    
    if(this.title == 'Make-A-Call')
    {
      //Make a call to customer
      // console.log("Make a call to customer");
      this.on = false;
      this.show = 'Making-Call';

      try{
        this.dialLineService.audioCall(this.browserPhoneService.userAgent,this.browserPhoneService.lang,this.currentRecord.phoneNumber,"",this.browserPhoneService.didLength);
        }
        catch(e)
        {
            console.log(e);
        }

      setTimeout(()=>{
        // console.log("Make a call to customer");
        this.on = true;
        this.show = 'Make-A-Call';
      },1500);

    }
    
  //  console.log("actionButtonClicked");
   if(this.title == 'Convert/Divert')
    {
      if(this.currentRecord.coverted)
      {
        this.customerService.customerGotDiverted( {id: this.currentRecord.id,organization: this.organization})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
              // console.log("Result is true, setting values to show on icon"); 
              this.on = false;
              this.show = 'OFF';
              this.currentRecord.coverted = false;
              this.parent.emit('Convert/Divert');
            }
            else{
               // console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "Customer was not diverted"); 
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
        this.customerService.customerGotConverted( {id: this.currentRecord.id,organization: this.organization})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
             // console.log("Result is true, setting values to show on icon");
              this.on = true;
              this.show = 'ON';
              this.currentRecord.coverted = true;
              this.parent.emit('Convert/Divert');
            }
            else{
              //  console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "Customer was not converted"); 
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

  showProductInterestDialoge()
  {
    // console.log("this.currentRecord In Customer",this.currentRecord);
    this.dialogService.open(ProductInterestDialogComponent, {
      context: {
        currentRecord: this.currentRecord,
        organization: this.organization,
      },
    }).onClose.subscribe({
      next: (res) => {
              this.on = true;
              this.show = 'ON';
      },
      error: (err) => console.error(`Observer got an error: ${err}`),
    });
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
