import { Component, OnDestroy, OnInit } from '@angular/core';
import { HeaderVariableService } from '../../../../service/header-variable/header-variable.service';
import { NotificationListDataService } from './notification-list-data-service/notification-list-data.service';
import { NbDialogService } from '@nebular/theme';
import { EmployeeService } from '../../../../pages/employee/service/employee.service';
import { DialogComponent } from '../../../../pages/employee/all-employees/dialog/dialog.component';
import { Subject, takeUntil } from 'rxjs';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { ScaleLayoutDirective } from '../../../../directives/scale-layout.directive';

@Component({
  selector: 'ngx-notification-list',
  templateUrl: './notification-list.component.html',
  styleUrls: ['./notification-list.component.scss']
})
export class NotificationListComponent  implements OnInit,OnDestroy  {
  private destroy$: Subject<void> = new Subject<void>();
  // notificationContainer: any;
  // notificationContainerCard: any;
  screenWidth: number;
  screenHeight: number;

  constructor(protected headerVariableService:HeaderVariableService,
              protected notificationListDataService:NotificationListDataService,
              private dialogService: NbDialogService,
              private employeeService : EmployeeService,)
               {

                }

  ngOnDestroy() {
      this.destroy$.next();
      this.destroy$.complete();
    }

  ngOnInit(): void {

    if(this.headerVariableService.notificationBadgeDot != false)
    {
          this.employeeService.updateNotificationDotStatusByOrganization( {email: ConstantsService.user.email,organization: ConstantsService.user.organization,value:false})
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: result => {
              if(String(result) == 'true')
              {
                //console.log("Result is true, setting values to show on icon"); 
                
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


    this.headerVariableService.notificationBadgeDot = false;

    console.log("Finding notification container")
    // this.notificationContainer = document.getElementsByClassName("custom-notification-popover");
    // this.notificationContainerCard = document.getElementById("notificationCard");
    // this.getScreenSize();
  }

  deleteNotification(i){
      this.notificationListDataService.deleteNotification(i);
  }

  clearAllNotifications(){
    this.notificationListDataService.clearAllNotifications();
  }

  //Not used, css manage in css files. Below was used previously. This is just kept for reference
  // getScreenSize() {
  //   this.screenWidth = window.innerWidth;
  //   this.screenHeight = window.innerHeight;

  //   console.log("this.screenHeight : "+this.screenHeight);

  //   if(this.screenWidth<=750){
  //     // Iterate through the collection of elements
  //     console.log("Adding left margin to notificatin container.")
  //     for (let i = 0; i < this.notificationContainer.length; i++) {
  //       // Access each element and modify its style.marginLeft property
  //       this.notificationContainer[i].style.marginLeft = "2.5rem"; // Set the desired margin-left value
  //     }

  //     // this.notificationContainerCard.style.maxHeight  = "24rem";
  //     // this.notificationContainerCard.style.margin = "0.2rem";

  //   }
  //   else{
  //     //Do nothing
  //   }
  // }
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
