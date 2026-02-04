import { Component, OnInit } from '@angular/core';
import { ConstantsService } from '../../../service/constants/constants.service';
import { NbDialogService, NbThemeService } from '@nebular/theme';
import { Router } from '@angular/router';
import { takeWhile } from 'rxjs/operators';
import { DialogComponent } from '../all-employees/dialog/dialog.component';
import { Subject } from 'rxjs';
import { MessageListDataService } from '../../../@theme/components/header/message-list/message-list-data-service/message-list-data.service';

@Component({
  selector: 'ngx-monitor-employees',
  templateUrl: './monitor-employees.component.html',
  styleUrls: ['./monitor-employees.component.scss']
})
export class MonitorEmployeesComponent implements OnInit {

  private destroy$: Subject<void> = new Subject<void>();
  allRecords:any = [];
  organization;any;
  redirectDelay = 0;
  colorScheme: any;
  private alive = true;
  searchString:any="";
  currentOptionsSize: any;
  isSearched: boolean;
  whatsAppSupportlink: string;
  themeSubscription: any;

  constructor(private themeService: NbThemeService,
              protected router: Router,
              protected constantService : ConstantsService,
              private dialogService: NbDialogService,
              protected messageListDataService:MessageListDataService) {

                //console.log("I am in constructor");

                this.whatsAppSupportlink = ConstantsService.whatsAppSupportlink;
                
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
                
               this.themeSubscription = this.themeService.getJsTheme()
                .pipe(takeWhile(() => this.alive))
                .subscribe(theme => {
                 
                  const colors: any = theme.variables;
                  this.colorScheme = {
                  domain: [colors.primaryLight, colors.infoLight, colors.successLight, colors.warningLight, colors.dangerLight],
                };
              });
            }

  ngOnInit(): void {

    setTimeout(() => {
      //   console.log('Routing to dashboard page');
    console.log(this.messageListDataService.allEmployeesData);
       }, ConstantsService.DIRECT_REFRESH_PAGE_TIME_MS);

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
    
   ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }

}
