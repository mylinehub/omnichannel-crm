import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { BrowserPhoneService } from '../../../../service/browser-phone/browser-phone.service';
import { Subject } from 'rxjs';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { NbDialogService, NbThemeService } from '@nebular/theme';
import { EmployeeService } from '../../../../pages/employee/service/employee.service';
import { DialogComponent } from '../../../../pages/employee/all-employees/dialog/dialog.component';
import { takeUntil } from 'rxjs/operators';
import { HeaderVariableService } from '../../../../service/header-variable/header-variable.service';
import { RegisterService } from '../../../../service/browser-phone/register/register.service';
import { DialLineService } from '../../../../service/browser-phone/dial-line/dial-line.service';
import { CallDetailRoutingModule } from '../../../../pages/call-detail/call-detail-routing.module';
import { CallDetailService } from '../../../../pages/call-detail/service/call-detail.service';

@Component({
  selector: 'ngx-browser-phone',
  templateUrl: './browser-phone.component.html',
  styleUrls: ['./browser-phone.component.scss']
})
export class BrowserPhoneComponent implements OnInit,OnChanges,OnDestroy {

  private destroy$: Subject<void> = new Subject<void>();
  redirectDelay: number = 0;
  themeSubscription: any;
  currentTheme: any;
  
  email : any = "";
  organization : any = "";
  lastEvent : any = ''
  lastEventNumber : number = 1;
  setLastEventNumberID:any= null;

  runMicChanged:any = false;
  runSpeakerChanged:any = false;
  runVideoChanged:any = false;
  runVideoOrientationChanged:any = false;
  runVideoQualityChanged:any = false;
  runVideoFrameRateChanged:any = false;
  runautoGainControlChanged:any = false;
  runEchoCancellationChanged:any = false;
  runNoiseSeperationChanged:any = false;
  dialledPhoneInput: HTMLElement;
  dialledPhoneInputListner: void;

  // All other variable are managed by header-veriable service as these varibles are global and UI changes as per them

  constructor(private browserPhoneService:BrowserPhoneService,
              private constantService : ConstantsService,
              private themeService: NbThemeService,
              private employeeService : EmployeeService,
              private callDetailService : CallDetailService,
              private dialogService: NbDialogService,
              protected headerVariableService: HeaderVariableService,
              protected registerService: RegisterService,
              protected dialLineService:DialLineService,) {

  //console.log("browser-phone-constructor");
    this.email = ConstantsService.user.email;
    this.organization = ConstantsService.user.organization;
    this.themeSubscription = this.themeService.getJsTheme().subscribe(theme => {
      this.currentTheme = theme.name;
    });
    
    this.runMicChanged = false;
    this.runSpeakerChanged = false;
    this.runVideoChanged = false;
    this.runVideoOrientationChanged = false;
    this.runVideoQualityChanged = false;
    this.runVideoFrameRateChanged = false;
    this.runautoGainControlChanged = false;
    this.runEchoCancellationChanged = false;
    this.runNoiseSeperationChanged = false;

  }
  
  ngOnDestroy(): void {
  // console.log("browser-phone-ng-onDestoy");
    this.dialledPhoneInput.removeEventListener("keypress",()=>{
    console.log("Removing event listner");
    });

    this.destroy$.next();
    this.destroy$.complete()
  }
  ngOnChanges(changes: SimpleChanges): void {
  // console.log("browser-phone-ng-onChanges");
  }

  ngOnInit(): void {
  // console.log("browser-phone-ng-onIt");

  // Get the input field

  setTimeout(()=>{
    this.dialledPhoneInput = document.getElementById("dialText");

    // Execute a function when the user presses a key on the keyboard
    this.dialledPhoneInputListner = this.dialledPhoneInput.addEventListener("keypress", (event:any)=>{
       console.log("event : ",event.key);
       console.log("Dialed Number : ",this.headerVariableService.dialValue);
       this.headerVariableService.dialValue = this.headerVariableService.dialValue + event.key;
       this.verifyAlphaNumericConstraint(event);
    });
  },1000);

  //console.log("browser-phone-ng-onIt this.headerVariableService.autoAnswer",this.headerVariableService.autoAnswer);
  //console.log("browser-phone-ng-onIt this.headerVariableService.autoConference",this.headerVariableService.autoConference);
  //console.log("browser-phone-ng-onIt this.headerVariableService.autoVideo",this.headerVariableService.autoVideo);
  }


  verifyAlphaNumericConstraint(event:any)
  {
        let k;  
        k = event.charCode;  //         k = event.keyCode;  (Both can be used)
        let current = this.headerVariableService.dialValue;
        // console.log("Input character : ", event);
        // console.log("Input character char code : ",k)
        // if((k > 64 && k < 91) || (k > 96 && k < 123) || k == 95 || k == 32 || (k >= 48 && k <= 57))
       
       if(((String(this.headerVariableService.dialValue).length == 1) || (String(this.headerVariableService.dialValue).length == 0)) && (k == 8))
         {
            //Do nothing
            console.log("Dial value is empty or just one and backspace is pressed. Hence doing nothing.")
         }

        else if((k > 64 && k < 91) || k == 43 || k == 8 || k == 42 || k == 45 || k == 35 || (k >= 48 && k <= 57))
          {
              //Its fine. Ng Model will take care of everything
          }
        else{
          this.headerVariableService.dialValue = current;
          this.showDialoge('Error','activity-outline','danger', "Only big alphabet, number,-,+, *, # are allowed via typing"); 
        }  
  }

  toggleBrowserPhone($event:any)
  {
    console.log("Browser Phone", $event);

    if($event)
    {
        this.registerService.register(this.browserPhoneService.userAgent,this.browserPhoneService.lang);
    }
    else
    {
       //boolean false passed as argument decides we want to skip unsubcrive event are this registered event. By default it should be false
       this.registerService.unregister(this.browserPhoneService.userAgent,this.browserPhoneService.lang,false);
    }
    
  }

   dialByLine($event:any)
    {
     //console.log($event);
     if(this.headerVariableService.freeDial)
     {

          this.callDetailService.refreshConnectionsOnOrganization(this.organization,this.headerVariableService.dialValue)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: allData => {
            },
            error: err => {
            // console.log("Error : "+ JSON.stringify(err));
              this.showDialoge('Refresh Failed','activity-outline','danger', 'CDR will not record. Contact support team. Although making call'); 
            }
          });
          
          if($event==='audio')
          {
              this.dialLineService.dialLine(this.browserPhoneService.userAgent,this.browserPhoneService.lang,$event,null,this.browserPhoneService.didLength);
          }
          else if ($event==='video')
          {
          this.dialLineService.dialLine(this.browserPhoneService.userAgent,this.browserPhoneService.lang,$event,null,this.browserPhoneService.didLength);
          }
     }
     else
     {
         this.showDialoge('Error','activity-outline','danger', "This user does not have permission for free dial. Contact admin to get"); 
     }
    }

    autoAnswerCall($event:any)
    {
     //console.log($event);
       let value : boolean = false;
       
       if($event==='true')
       {
        value = true;
       }
       else if ($event==='false')
       {
        value = false;
       }

        
    this.employeeService.updateSelfAutoAnswerByOrganization({email:this.email,organization:this.organization,value:value})
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
        
      //console.log('result',result)
        if($event==='true')
        {
       //console.log("I am autoAnswer true");
         this.headerVariableService.autoAnswer = true;
        }
        else if ($event==='false')
        {
       //console.log("I am autoAnswer false");
         this.headerVariableService.autoAnswer = false;
        }

      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });


    }

    autoConferenceCall($event:any)
    {
     //console.log($event);
       let value : boolean = false;

       if($event==='true')
       {
        value = true;
       }
       else if ($event==='false')
       {
        value = false;
       }

       this.employeeService.updateSelfAutoConferenceByOrganization({email:this.email,organization:this.organization,value:value})
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
           
         //console.log('result',result)
           if($event==='true')
           {
          //console.log("I am autoAnswer true");
            this.headerVariableService.autoConference = true;
           }
           else if ($event==='false')
           {
          //console.log("I am autoAnswer false");
            this.headerVariableService.autoConference = false;
           }
   
         },
         error: err => {
         // console.log("Error : "+ JSON.stringify(err));
           this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
         }
       });


    }

    autoVideoCall($event:any)
    {
     //console.log($event);
       let value : boolean = false;

       if($event==='true')
       {
        value = true;
       }
       else if ($event==='false')
       {
        value = false;
       }

       this.employeeService.updateSelfAutoVideoByOrganization({email:this.email,organization:this.organization,value:value})
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
           
         //console.log('result',result)
           if($event==='true')
           {
          //console.log("I am autoAnswer true");
            this.headerVariableService.autoVideo = true;
           }
           else if ($event==='false')
           {
          //console.log("I am autoAnswer false");
            this.headerVariableService.autoVideo = false;
           }
   
         },
         error: err => {
         // console.log("Error : "+ JSON.stringify(err));
           this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
         }
       });
    }

    doNotDisturbCall($event:any)
    {
      //console.log($event);
      let value : boolean = false;

      if($event==='true')
      {
       value = true;
      }
      else if ($event==='false')
      {
       value = false;
      }

      this.employeeService.updateSelfDoNotDisturbByOrganization({email:this.email,organization:this.organization,value:value})
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: result => {
          
        //console.log('result',result)
          if($event==='true')
          {
         //console.log("I am autoAnswer true");
           this.headerVariableService.doNotDisturb = true;
          }
          else if ($event==='false')
          {
         //console.log("I am autoAnswer false");
           this.headerVariableService.doNotDisturb = false;
          }
  
        },
        error: err => {
        // console.log("Error : "+ JSON.stringify(err));
          this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });

    }

    startFullScreenVideoCall($event:any)
    {
       console.log($event);
       let value : boolean = false;

       if($event==='true')
       {
        value = true;
       }
       else if ($event==='false')
       {
        value = false;
       }
 
       this.employeeService.updateSelfStartVideoFullScreenByOrganization({email:this.email,organization:this.organization,value:value})
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
           
         //console.log('result',result)
           if($event==='true')
           {
          //console.log("I am autoAnswer true");
            this.headerVariableService.startVideoFullScreen = true;
           }
           else if ($event==='false')
           {
          //console.log("I am autoAnswer false");
            this.headerVariableService.startVideoFullScreen = false;
           }
   
         },
         error: err => {
         // console.log("Error : "+ JSON.stringify(err));
           this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
         }
       });

    }


    changeCallWaiting($event:any){
      console.log($event);
       let value : boolean = false;

       if($event==='true')
       {
        value = true;
       }
       else if ($event==='false')
       {
        value = false;
       }
 
       this.employeeService.updateSelfCallWaitingByOrganization({email:this.email,organization:this.organization,value:value})
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
           
         //console.log('result',result)
           if($event==='true')
           {
          //console.log("I am autoAnswer true");
            this.headerVariableService.callWaiting = true;
           }
           else if ($event==='false')
           {
          //console.log("I am autoAnswer false");
            this.headerVariableService.callWaiting = false;
           }
   
         },
         error: err => {
         // console.log("Error : "+ JSON.stringify(err));
           this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
         }
       });

    }

    micChanged($event:any)
    {
      if (!(this.runMicChanged))
      {
        this.runMicChanged = true;
      }
      else
      {
      //console.log("event",$event);
        this.headerVariableService.currentMicStringValue = $event[0];
        this.employeeService.updateSelfMicDeviceByOrganization({email:this.email,organization:this.organization,value:$event[0]})
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
                  //What should happen when it is saved
         },
         error: err => {
         // console.log("Error : "+ JSON.stringify(err));
           this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
         }
         });
      }
      // console.log("this.browserPhoneService.HasVideoDevice",this.browserPhoneService.hasVideoDevice);
      // console.log("this.browserPhoneService.HasAudioDevice",this.browserPhoneService.hasAudioDevice);
      // console.log("this.browserPhoneService.HasSpeakerDevice",this.browserPhoneService.hasSpeakerDevice);
      // console.log("this.browserPhoneService.AudioinputDevices",this.browserPhoneService.audioinputDevices);
      // console.log("this.browserPhoneService.VideoinputDevices",this.browserPhoneService.videoinputDevices);
      // console.log("this.browserPhoneService.SpeakerDevices",this.browserPhoneService.speakerDevices);
    }

    
    speakerChanged($event)
    {
      if (!(this.runSpeakerChanged))
      {
        this.runSpeakerChanged = true;
      }
      else
      {
      //console.log("event",$event);
        this.headerVariableService.currentSpeakerStringValue = $event[0];
        this.employeeService.updateSelfSpeakerDeviceByOrganization({email:this.email,organization:this.organization,value:$event[0]})
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
                  //What should happen when it is saved
         },
         error: err => {
         // console.log("Error : "+ JSON.stringify(err));
           this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
         }
         });
      }
    }

    videoChanged($event)
    {
      if (!(this.runVideoChanged))
      {
        this.runVideoChanged = true;
      }
      else
      {
      //console.log("event",$event);
        this.headerVariableService.currentVideoStringValue = $event[0];
        this.employeeService.updateSelfVideoDeviceByOrganization({email:this.email,organization:this.organization,value:$event[0]})
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
                  //What should happen when it is saved
         },
         error: err => {
         // console.log("Error : "+ JSON.stringify(err));
           this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
         }
         });
      }
    }

    videoOrientationChanged($event)
    {
      if (!(this.runVideoOrientationChanged))
      {
        this.runVideoOrientationChanged = true;
      }
      else
      {
      //console.log("event",$event);
        this.headerVariableService.currentVideoOrientationStringValue = $event[0];
        this.employeeService.updateSelfVideoOrientationByOrganization({email:this.email,organization:this.organization,value:$event[0]})
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
                  //What should happen when it is saved
         },
         error: err => {
         // console.log("Error : "+ JSON.stringify(err));
           this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
         }
         });
      }
    }

    videoQualityChanged($event)
    {
      if (!(this.runVideoQualityChanged))
      {
        this.runVideoQualityChanged = true;
      }
      else
      {
      //console.log("event",$event);
        this.headerVariableService.currentVideoQualityStringValue = $event[0];
        this.employeeService.updateSelfVideoQualityByOrganization({email:this.email,organization:this.organization,value:$event[0]})
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
                  //What should happen when it is saved
         },
         error: err => {
         // console.log("Error : "+ JSON.stringify(err));
           this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
         }
         });
      }
    }

    videoFrameRateChanged($event)
    {
      if (!(this.runVideoFrameRateChanged))
      {
        this.runVideoFrameRateChanged = true;
      }
      else
      {
      //console.log("event",$event);
        this.headerVariableService.currentVideoFrameRateStringValue = $event[0];
        this.employeeService.updateSelfVideoFrameRateByOrganization({email:this.email,organization:this.organization,value:$event[0]})
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
                  //What should happen when it is saved
         },
         error: err => {
         // console.log("Error : "+ JSON.stringify(err));
           this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
         }
         });
      }
    }

    autoGainControlChanged($event){
      if (!(this.runautoGainControlChanged))
      {
        this.runautoGainControlChanged = true;
      }
      else
      {
      //console.log("event",$event);
        this.headerVariableService.currentAutoGainControlStringValue = $event[0];
        this.employeeService.updateSelfAutoGainControlByOrganization({email:this.email,organization:this.organization,value:$event[0]})
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
                  //What should happen when it is saved
         },
         error: err => {
         // console.log("Error : "+ JSON.stringify(err));
           this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
         }
         });
      }
    }

    echoCancellationChanged($event){
      if (!(this.runEchoCancellationChanged))
      {
        this.runEchoCancellationChanged = true;
      }
      else
      {
      //console.log("event",$event);
        this.headerVariableService.currentEchoCancellationStringValue = $event[0];
        this.employeeService.updateSelfEchoCancellationByOrganization({email:this.email,organization:this.organization,value:$event[0]})
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
                  //What should happen when it is saved
         },
         error: err => {
         // console.log("Error : "+ JSON.stringify(err));
           this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
         }
         });
      }
    }

    noiseSeperationChanged($event)
    {
      if (!(this.runNoiseSeperationChanged))
      {
        this.runNoiseSeperationChanged = true;
      }
      else
      {
      //console.log("event",$event);
        this.headerVariableService.currentNoiseSeperationStringValue = $event[0];
        this.employeeService.updateSelfNoiseSupressionByOrganization({email:this.email,organization:this.organization,value:$event[0]})
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
                  //What should happen when it is saved
         },
         error: err => {
         // console.log("Error : "+ JSON.stringify(err));
           this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
         }
         });
      }
    }


    keyPress($event)
    {
      //console.log('Key Pressed event: ',$event);
      //console.log('Key Pressed last Event: ',this.lastEvent);

      switch($event)
      {
        case 'del':
          //console.log('switch: ','del');

          if (this.headerVariableService.dialValue.length >0)
          this.headerVariableService.dialValue = this.headerVariableService.dialValue.substring(0, this.headerVariableService.dialValue.length - 1);
          this.lastEventNumber = 1;

          //console.log('new event number : ',this.lastEventNumber);

          break;
        case '1':
          //console.log('switch: ','1');
          this.changeOutOfFiveValue('1','(','-',')','+');
          break; 
        case '2':
          //console.log('switch: ','2');
          this.changeOutOfFourValue('2','A','B','C');
          break; 
        case '3':
          //console.log('switch: ','3');
          this.changeOutOfFourValue('3','D','E','F');
          break;
        case '4':
          //console.log('switch: ','4');
          this.changeOutOfFourValue('4','G','H','I');
          break; 
        case '5':
          //console.log('switch: ','5');
          this.changeOutOfFourValue('5','J','K','L');
          break; 
        case '6':
          //console.log('switch: ','6');
          this.changeOutOfFourValue('6','M','N','O');
          break;
        case '7':
          //console.log('switch: ','7');
          //S is pending
          this.changeOutOfFiveValue('7','P','Q','R','S');
          break; 
        case '8':
          //console.log('switch: ','8');
          this.changeOutOfFourValue('8','T','U','V');
          break;   
        case '9':
          //console.log('switch: ','9');
          this.changeOutOfFiveValue('9','W','X','Y','Z');
          break;
        case '*':
          //console.log('switch: ','*');
          this.changeOutOfOneValue('*');
          break; 
        case '0':
          //console.log('switch: ','0');
          this.changeOutOfOneValue('0');
          break;
        case '#':
          //console.log('switch: ','#');
          this.changeOutOfOneValue('#');
          break;    
        default:
        //console.log('switch: ','default');
          break;  
      }

      this.lastEvent = $event;
      
    }

    changeOutOfOneValue(value:string)
    {
      this.headerVariableService.dialValue = this.headerVariableService.dialValue + value;
      this.lastEventNumber = 1;
      //console.log('new event number : ',this.lastEventNumber);
    }

    changeOutOfFourValue(value:string,first:string,second:string,third:string)
    {

      if(this.lastEvent === value)
      {
        //console.log('last value was: ',value);
        //console.log('last value event number : ',this.lastEventNumber);

        this.headerVariableService.dialValue = this.headerVariableService.dialValue.substring(0, this.headerVariableService.dialValue.length - 1);

        if(this.lastEventNumber >= 4)
        {
          this.lastEventNumber =  1;
            this.headerVariableService.dialValue = this.headerVariableService.dialValue + value;
        }
        else
        this.lastEventNumber =  this.lastEventNumber + 1;

        if(this.lastEventNumber === 2)
        this.headerVariableService.dialValue = this.headerVariableService.dialValue + first;

        if(this.lastEventNumber === 3)
        this.headerVariableService.dialValue = this.headerVariableService.dialValue + second;

        if(this.lastEventNumber === 4)
        this.headerVariableService.dialValue = this.headerVariableService.dialValue + third;

        this.resetLastEvent();
        //console.log('new event number : ',this.lastEventNumber);
      }
      else
      {

        //console.log('last value was not: ',value);

        this.headerVariableService.dialValue = this.headerVariableService.dialValue + value;
        this.lastEventNumber = 1;
        this.resetLastEvent();
        //console.log('new event number : ',this.lastEventNumber);
      }

    }


    changeOutOfFiveValue(value:string,first:string,second:string,third:string,fourth:string)
    {

      if(this.lastEvent === value)
      {
        // console.log('last value was: ',value);
        // console.log('last value event number : ',this.lastEventNumber);
        // console.log('last value event : ',this.lastEvent);

        this.headerVariableService.dialValue = this.headerVariableService.dialValue.substring(0, this.headerVariableService.dialValue.length - 1);

        if(this.lastEventNumber >= 5)
        {
          this.lastEventNumber =  1;
            this.headerVariableService.dialValue = this.headerVariableService.dialValue + value;
        }
        else
        this.lastEventNumber =  this.lastEventNumber + 1;

        if(this.lastEventNumber === 2)
        this.headerVariableService.dialValue = this.headerVariableService.dialValue + first;

        if(this.lastEventNumber === 3)
        this.headerVariableService.dialValue = this.headerVariableService.dialValue + second;

        if(this.lastEventNumber === 4)
        this.headerVariableService.dialValue = this.headerVariableService.dialValue + third;

        if(this.lastEventNumber === 5)
        this.headerVariableService.dialValue = this.headerVariableService.dialValue + fourth;

        this.resetLastEvent();
        //console.log('new event number : ',this.lastEventNumber);
      }
      else
      {

        //console.log('last value was not: ',value);

        this.headerVariableService.dialValue = this.headerVariableService.dialValue + value;
        this.lastEventNumber = 1;
        this.resetLastEvent();
        //console.log('new event number : ',this.lastEventNumber);
      }

    }

    resetLastEvent()
    {
      if(this.setLastEventNumberID == null)
        {
          this.setLastEventNumberID = setTimeout(()=>{
            this.lastEvent =  null;
          },1000);
        }
        else{
          clearTimeout(this.setLastEventNumberID);
          this.setLastEventNumberID = setTimeout(()=>{
            this.lastEvent =  null;
          },1000);
        }
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
