import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { NbDialogRef, NbDialogService } from '@nebular/theme';
import { DialogComponent } from '../../all-employees/dialog/dialog.component';
import { EmployeeService } from './../../service/employee.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'ngx-reset-web-password',
  templateUrl: './reset-web-password.component.html',
  styleUrls: ['./reset-web-password.component.scss']
})
export class ResetWebPasswordComponent implements OnInit, OnDestroy {
 
  @Input() currentRecord: any;
  private destroy$: Subject<void> = new Subject<void>();

  constructor(protected ref: NbDialogRef<ResetWebPasswordComponent>,
              private dialogService: NbDialogService,
              private employeeService: EmployeeService) {}

  cancel() {
    this.ref.close();
  }

  submit(password,confirmPassword) {

    if(String(password)==String(confirmPassword))
    {

      this.employeeService.updateWebPassword({email : this.currentRecord.email,organization:localStorage.getItem("organization"),password:password})
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: data => {
          console.log(data);
          if(String(data) == 'true')
          {
            this.showDialoge('Success','done-all-outline','success', `Password updated`);
            this.ref.close();
               
                }
          else{
            this.showDialoge('Error','activity-outline','danger', "Please try again."); 
            this.ref.close();
          }

        },
        error: err => {
        // console.log("Error : "+ JSON.stringify(err));
          this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
          this.ref.close();
        }
      });

    }
    else
    {
      this.showDialoge('Error','activity-outline','danger', "password and confirmPassword should be same"); 
    }
  }

  ngOnInit(): void {
  }

  ngOnDestroy() {
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

}
