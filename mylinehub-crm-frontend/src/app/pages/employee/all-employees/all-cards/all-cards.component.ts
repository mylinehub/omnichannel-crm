import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output } from '@angular/core';
import { NbComponentStatus, NbDialogService, NbToastrService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { EmployeeService } from '../../service/employee.service';
import { Router } from '@angular/router';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { takeUntil } from 'rxjs/operators';
import { DialogComponent } from '../dialog/dialog.component';
import { HeaderVariableService } from '../../../../service/header-variable/header-variable.service';
import { BrowserPhoneService } from '../../../../service/browser-phone/browser-phone.service';
import { DialLineService } from '../../../../service/browser-phone/dial-line/dial-line.service';


@Component({
  selector: 'ngx-all-cards',
  styleUrls: ['./all-cards.component.scss'],
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
export class AllCardsComponent implements OnInit, OnDestroy, OnChanges{
  
  
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
    protected headerVariableService : HeaderVariableService,
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
    //console.log(this.currentRecord.isEnabled);
    this.setButtonVariables();

  }

  ngOnChanges() {
      this.setButtonVariables();
  }   

 setButtonVariables()
 {

  if(this.title == 'Make-A-Call')
    {
      //Make a call to customer
      this.on = true;
      this.show = 'Make-A-Call';
    }

  if(this.title == 'Record Calls')
  {
     if(!this.currentRecord.recordAllCalls)
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

  if(this.title == 'Internal Calls')
  {
     if(!this.currentRecord.intercomPolicy)
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

  if(this.title == 'Free Dial')
  {
     if(!this.currentRecord.freeDialOption)
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

  if(this.title == 'Text Dictate')
  {
     if(!this.currentRecord.textDictateOption)
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

  if(this.title == 'Text Messaging')
  {
     if(!this.currentRecord.textMessagingOption)
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

  // console.log("ngOnChanges")
   if(this.title == 'Enable/Disable')
   {
     //console.log("Enable / Disable card intial value")
     if(!this.currentRecord.isEnabled)
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


   if(this.title == 'Use Second Line')
   {
     //console.log("Enable / Disable card intial value")
     if(!this.currentRecord.useSecondaryAllotedLine)
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
            this.dialLineService.audioCall(this.browserPhoneService.userAgent,this.browserPhoneService.lang,this.currentRecord.phonenumber,"",this.browserPhoneService.didLength);
        }
        catch(e)
        {
            console.log(e);
        }

      setTimeout(()=>{
        this.on = true;
        this.show = 'Make-A-Call';
      },1500);
    }



   if(this.title == 'Record Calls')
   {
      let value:any;
      if(this.currentRecord.recordAllCalls)
      {
        value = false;
      }
      else{
        value = true;
      }

      this.employeeService.updateEmployeeRecordAllCallsByOrganization( {email: this.currentRecord.email,organization: this.organization,value:value})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
              //console.log("Result is true, setting values to show on icon"); 
              this.on = value;
              if (!value)
              this.show = 'OFF';
              else
              this.show = 'ON';

              this.currentRecord.recordAllCalls = value;
              this.headerVariableService.recordAllCalls = value;
              this.parent.emit('Record Calls');
            }
            else{
               // console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "User value not changed, try again ..."); 
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
   }

   if(this.title == 'Internal Calls')
   {
      let value:any;
      if(this.currentRecord.intercomPolicy)
      {
        value = false;
      }
      else{
        value = true;
      }
      this.employeeService.updateEmployeeIntercomPolicyByOrganization( {email: this.currentRecord.email,organization: this.organization,value:value})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
              //console.log("Result is true, setting values to show on icon"); 
              this.on = value;
              if (!value)
              this.show = 'OFF';
              else
              this.show = 'ON';

              this.currentRecord.intercomPolicy = value;
              this.headerVariableService.intercomPolicy = value;
              this.parent.emit('Internal Calls');
            }
            else{
               // console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "User value not changed, try again ..."); 
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
   }

   if(this.title == 'Free Dial')
   {
      let value:any;
      if(this.currentRecord.freeDialOption)
      {
        value = false;
      }
      else{
        value = true;
      }
      this.employeeService.updateEmployeeFreeDialOptionByOrganization( {email: this.currentRecord.email,organization: this.organization,value:value})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
              //console.log("Result is true, setting values to show on icon"); 
              this.on = value;
              if (!value)
              this.show = 'OFF';
              else
              this.show = 'ON';

              this.currentRecord.freeDialOption = value;
              this.headerVariableService.freeDial = value;
              this.parent.emit('Free Dial');
            }
            else{
               // console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "User value not changed, try again ..."); 
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
   }

   if(this.title == 'Text Dictate')
   {
      let value:any;
      if(this.currentRecord.textDictateOption)
      {
        value = false;
      }
      else{
        value = true;
      }
      this.employeeService.updateEmployeeTextDictationByOrganization( {email: this.currentRecord.email,organization: this.organization,value:value})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
              //console.log("Result is true, setting values to show on icon"); 
              this.on = value;
              if (!value)
              this.show = 'OFF';
              else
              this.show = 'ON';

              this.currentRecord.textDictateOption = value;
              this.headerVariableService.textDictate = value;
              this.parent.emit('Text Dictate');
            }
            else{
               // console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "User value not changed, try again ..."); 
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
   }

   if(this.title == 'Text Messaging')
   {
      let value:any;
      if(this.currentRecord.textMessagingOption)
      {
        value = false;
      }
      else{
        value = true;
      }
      this.employeeService.updateEmployeeTextMessagingByOrganization( {email: this.currentRecord.email,organization: this.organization,value:value})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
              //console.log("Result is true, setting values to show on icon"); 
              this.on = value;
              if (!value)
              this.show = 'OFF';
              else
              this.show = 'ON';

              this.currentRecord.textMessagingOption = value;
              this.headerVariableService.textMessaging = value;
              this.parent.emit('Text Messaging');
            }
            else{
               // console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "User value not changed, try again ..."); 
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });
   }


    if(this.title == 'Enable/Disable')
    {
      if(this.currentRecord.isEnabled)
      {
        this.employeeService.disableUserOnEmailAndOrganization( {email: this.currentRecord.email,organization: this.organization})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
              //console.log("Result is true, setting values to show on icon"); 
              this.on = false;
              this.show = 'OFF';
              this.currentRecord.isEnabled = false;
              this.parent.emit('Enable/Disable');
            }
            else{
               // console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "User was not disabled"); 
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
        this.employeeService.enableUserOnEmailAndOrganization( {email: this.currentRecord.email,organization: this.organization})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
             // console.log("Result is true, setting values to show on icon");
              this.on = true;
              this.show = 'ON';
              this.currentRecord.isEnabled = true;
              this.parent.emit('Enable/Disable');
            }
            else{
              //  console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "User was not enabled"); 
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });

      }
    }

    if(this.title == 'Call On Mobile')
    {
      if(this.currentRecord.callonnumber)
      {
        this.employeeService.disableEmployeeCallOnMobile( {email: this.currentRecord.email,organization: this.organization})
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
                this.showDialoge('Error','activity-outline','danger', "Call on mobile was not disabled"); 
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
        this.employeeService.enableEmployeeCallOnMobile( {email: this.currentRecord.email,organization: this.organization})
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
                this.showDialoge('Error','activity-outline','danger', "Call on mobile was not enabled"); 
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });

      }
    }

    if(this.title == 'Use Second Line')
    {
      if(this.currentRecord.useSecondaryAllotedLine)
      {
        this.employeeService.disableUseAllotedSecondLineByOrganization( {email: this.currentRecord.email,organization: this.organization})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
              //console.log("Result is true, setting values to show on icon"); 
              this.on = false;
              this.show = 'OFF';
              this.currentRecord.useSecondaryAllotedLine = false;
              this.parent.emit('Use Second Line');
            }
            else{
               // console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "Use second line was not disabled"); 
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
        this.employeeService.enableUseAllotedSecondLineByOrganization( {email: this.currentRecord.email,organization: this.organization})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
             // console.log("Result is true, setting values to show on icon");
              this.on = true;
              this.show = 'ON';
              this.currentRecord.useSecondaryAllotedLine = true;
              this.parent.emit('Use Second Line');
            }
            else{
              //  console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "Use second line was not enabled"); 
            }
          },
          error: err => {
          // console.log("Error : "+ JSON.stringify(err));
            this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          }
        });

      }
    }

    if(this.title == 'Controls AI')
    {
      let value:any;
      if(this.currentRecord.allowDisableWhatsAppAutoAI)
      {
        value = false;
      }
      else{
        value = true;
      }
      this.employeeService.updateUserAllowedToSwitchOffWhatsAppAIByOrganization( {email: this.currentRecord.email,organization: this.organization,value:value})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
            if(String(result) == 'true')
            {
              //console.log("Result is true, setting values to show on icon"); 
              this.on = value;
              if (!value)
              this.show = 'OFF';
              else
              this.show = 'ON';

              this.currentRecord.allowDisableWhatsAppAutoAI = value;
              this.headerVariableService.textMessaging = value;
              this.parent.emit('Controls AI');
            }
            else{
               // console.log("Result is not true");
                this.showDialoge('Error','activity-outline','danger', "User value not changed, try again ..."); 
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
