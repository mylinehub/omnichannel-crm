import { Component, Input, OnInit } from '@angular/core';
import { NbDialogService, NbThemeService } from '@nebular/theme';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { CampaignService } from '../../../campaign/service/campaign.service';
import { Subject } from 'rxjs';
import { DialogComponent } from '../dialog/dialog.component';
import { takeUntil, takeWhile } from 'rxjs/operators';
import { EmployeeService } from '../../service/employee.service';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'ngx-play-audio',
  templateUrl: './play-audio.component.html',
  styleUrls: ['./play-audio.component.scss']
})
export class PlayAudioComponent implements OnInit {

  @Input() recording : any;
  @Input() inputRecordingDateSubmitted:Date;
  @Input() organization: string;
  @Input() domain: string;
  @Input() extension: string;

  private destroy$: Subject<void> = new Subject<void>();
  colorScheme: { domain: any[]; };
  alive: boolean = true;
  
  constructor(
    private themeService: NbThemeService,
    protected constantService : ConstantsService,
    private dialogService: NbDialogService,
    private employeeService: EmployeeService,
    private domSanitizer: DomSanitizer) { 

      
      this.themeService.getJsTheme()
      .pipe(takeWhile(() => this.alive))
      .subscribe(theme => {
        const colors: any = theme.variables;
        this.colorScheme = {
        domain: [colors.primaryLight, colors.infoLight, colors.successLight, colors.warningLight, colors.dangerLight],
      };
    });
    }

  ngOnInit(): void {
    this.getCurrentRecording();
  }

  
  ngOnDestroy() {

    this.recording.icon = 'play-circle-outline';
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
  

  getCurrentRecording()
  {
   // console.log("getCurrentRecording");

    //console.log(this.recording.title);
    //console.log(this.recording.icon);
    //console.log(this.i);
    const title = this.recording.title;
    const year = this.inputRecordingDateSubmitted.getFullYear();
    const month = this.inputRecordingDateSubmitted.getMonth() + 1;
    const day = this.inputRecordingDateSubmitted.getDate();

    this.employeeService.downloadRecordingForEmployee({year:year,month:month,day:day,domain:this.domain,extension:this.extension,organization:this.organization,fileName:title})
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {
       
       // console.log("all data is not null");

        if(allData == null)
         {
           // console.log("All Data is null");
         }
         else{
           //console.log("All Data is not null");
           //console.log(allData);
           this.recording.icon = 'pause-circle-outline';
           var obj = <any>allData;
          // console.log("obj");
           //console.log(obj);

           var blob = new Blob([obj], { 'type' : 'audio/wav' });

         //  console.log("blob");
           //console.log(blob);


           var audioURL = window.URL.createObjectURL(blob);
          // console.log("audioURL");
           //console.log(audioURL);

           this.recording.audioSource = this.domSanitizer.bypassSecurityTrustUrl(audioURL);
         }
            

      },
      error: err => {
      //console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

}
