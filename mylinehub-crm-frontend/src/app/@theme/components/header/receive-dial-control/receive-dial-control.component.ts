import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { BrowserPhoneService } from '../../../../service/browser-phone/browser-phone.service';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { NbThemeService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { HeaderVariableService } from '../../../../service/header-variable/header-variable.service';
import { SessionService } from '../../../../service/browser-phone/session/session.service';
import { AnswerService } from '../../../../service/browser-phone/answer/answer.service';

@Component({
  selector: 'ngx-receive-dial-control',
  templateUrl: './receive-dial-control.component.html',
  styleUrls: ['./receive-dial-control.component.scss']
})
export class ReceiveDialControlComponent implements OnInit,OnChanges,OnDestroy {

  private destroy$: Subject<void> = new Subject<void>();
  redirectDelay: number = 0;
  themeSubscription: any;
  currentTheme: any;

  receiveStatus = "success";
  rejectStatus = "danger";
  receiveCallToolTip = 'Receive Call';
  rejectCallToolTip = 'Reject Call';
  
  constructor(private browserPhoneService:BrowserPhoneService,
    private constantService : ConstantsService,
    private themeService: NbThemeService,
    protected headerVariableService:HeaderVariableService,
    protected sessionService:SessionService,
    protected answerService : AnswerService) { 
    //console.log("receive-dial-constructor"); 
      this.themeSubscription = this.themeService.getJsTheme().subscribe(theme => {
        this.currentTheme = theme.name;
        });
    }

    ngOnInit(): void {
    //console.log("receive-dial-ng-onIt"); 
    }
  
    ngOnDestroy(): void {
    //console.log("receive-dial-ng-onDestoy");
      this.destroy$.next();
      this.destroy$.complete()
    }
    ngOnChanges(changes: SimpleChanges): void {
    //console.log("receive-dial-ng-onChanges");
    }

  receiveCall()
  {
    console.log("I am in receive call");
    console.log("this.sessionService.incommingCallSessionData",this.sessionService.incommingCallSessionData);
    if(this.sessionService.incommingCallSessionData != null && this.sessionService.incommingCallSessionData.data.withvideo) {
      console.log("Video , withVideo is : " + this.sessionService.incommingCallSessionData.data.withvideo);
        this.answerService.answervideoCall(this.browserPhoneService.lang,this.sessionService.incommingCallSessionData);
    }
    else if (this.sessionService.incommingCallSessionData != null){
      console.log("Audio , withVideo is : " + this.sessionService.incommingCallSessionData.data.withvideo);
        this.answerService.answeraudioCall(this.browserPhoneService.lang,this.sessionService.incommingCallSessionData);
    }
                      
  }

  rejectCall()
  {
    console.log("I am in reject Call");
    if (this.sessionService.incommingCallSessionData != null){

      console.log("incommingCallSessionData is not null");
      this.answerService.rejectCall(this.browserPhoneService.lang,this.sessionService.incommingCallSessionData);
    }
    else if (this.sessionService.outgoingCallSessionData != null)
      {
        console.log("outgoingCallSessionData is not null");
        this.sessionService.cancelSession(this.browserPhoneService.lang,this.sessionService.outgoingCallSessionData);
      }
    else{
      console.log("both incoming and outgoing session data is null");
    }
  }

}
