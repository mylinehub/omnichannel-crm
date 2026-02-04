import { Injectable, OnDestroy, OnInit } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { NotificationDataService } from '../../../../../service/notification/notification-data.service';
import { DialogComponent } from '../../../../../pages/employee/all-employees/dialog/dialog.component';
import { NbDialogService } from '@nebular/theme';
import { ConstantsService } from '../../../../../service/constants/constants.service';
import { HeaderVariableService } from '../../../../../service/header-variable/header-variable.service';
import { PhoneMusicService } from '../../../../../service/phone-music/phone-music.service';
import { EmployeeService } from '../../../../../pages/employee/service/employee.service';

@Injectable({
  providedIn: 'root'
})
export class NotificationListDataService implements OnInit, OnDestroy {
  
 emptyNotifications = { alertType: 'alert-info', title: 'Oops!', message: 'Empty! Notification box.' };

 generalNotifications: any = [
  { alertType: 'alert-success', title: 'Well done!' , message: 'You successfullyread this important.'},
  { alertType: 'alert-info', title: 'Heads up!', message: 'This alert needs your attention, but it\'s not super important.' },
  { alertType: 'alert-warning', title: 'Warning!', message: 'Better check yourself, you\'re not looking too good.' },
  { alertType: 'alert-danger', title: 'Oh snap!', message: 'Change a few things up and try submitting again.' },
  { alertType: 'alert-primary', title: 'Good Work!', message: 'You completed the training number 23450 well.' },
];

setNotificationDeleteId:any = null;
deletedItems: any = [];
private destroy$: Subject<void> = new Subject<void>();

  constructor(protected notificationDataService : NotificationDataService,
              private dialogService: NbDialogService,
              protected headerVariableService:HeaderVariableService,
              protected phoneMusicService:PhoneMusicService,
              private employeeService : EmployeeService,) {
                
               }
  ngOnInit(): void {
    
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadNotifications()
  {
    // console.log("loadNotifications");

    let data : any = {id:null,extension:ConstantsService.user.extension,organization:localStorage.getItem("organization")};

    this.notificationDataService.getAllNotificationsByExtensionAndOrganization(data)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (result) => {
               //What should happen when it is saved
               if (result != null)
               {
                this.generalNotifications = [... JSON.parse(JSON.stringify(result))];
               }
               else{
                this.generalNotifications = [];
               }
               this.deletedItems = [];
               this.setNotificationDeleteId = null;
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
      });
  }

  ingestNotification(notification)
  {
    this.generalNotifications.splice(0, 0, notification);

    if(this.headerVariableService.notificationBadgeDot != true)
    {
          this.employeeService.updateNotificationDotStatusByOrganization( {email: ConstantsService.user.email,organization: ConstantsService.user.organization,value:true})
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

    this.headerVariableService.notificationBadgeDot = true;

    if(!this.headerVariableService.callInProgress)
    {
        this.phoneMusicService.ringAlertMusic();
    }

  }

  deleteNotification(i)
  {

    console.log("deleteNotification",i);

    this.deletedItems.push(this.generalNotifications[i].id);
    this.generalNotifications.splice(i, 1);

    if(this.setNotificationDeleteId == null)
    {
      this.setNotificationDeleteId = setTimeout(()=>{this.deleteNow(i)},2000);
    }
    else{
      clearTimeout(this.setNotificationDeleteId);
      this.setNotificationDeleteId = setTimeout(()=>{this.deleteNow(i)},2000);
    }
  }

  deleteNow(i)
  {
    
    console.log("deleteNow",i);
    this.setNotificationDeleteId = null;
    let data : any = {extension:ConstantsService.user.extension,ids:this.deletedItems,organization:localStorage.getItem("organization")};
    
    this.notificationDataService.deleteNotificationByIdsAndExtensionsAndOrganization(data)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
               //What should happen when it is saved
               this.deletedItems = [];
               this.setNotificationDeleteId = null;
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
      });
 
  }

  
  clearAllNotifications(){   

    console.log("clearAllNotifications");

    let data : any = {id:null,extension:ConstantsService.user.extension,organization:localStorage.getItem("organization")};

    this.notificationDataService.deleteAllNotificationsByExtensionAndOrganization(data)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
               //What should happen when it is saved
               this.generalNotifications = [];
               this.deletedItems = [];
               this.setNotificationDeleteId = null;
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
      });
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
