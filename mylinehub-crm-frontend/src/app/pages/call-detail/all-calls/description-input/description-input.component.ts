import { ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { DialogComponent } from '../../../employee/all-employees/dialog/dialog.component';
import { NbDialogService } from '@nebular/theme';
import { Subject, takeUntil } from 'rxjs';
import { CallDetailService } from '../../service/call-detail.service';

@Component({
  selector: 'ngx-description-input',
  templateUrl: './description-input.component.html',
  styleUrls: ['./description-input.component.scss']
})
export class DescriptionInputComponent implements OnInit, OnDestroy {

  @Input() rowData; // data from table
  value:any = 'No remark for this customer. Edit Now.'; 
  private destroy$: Subject<void> = new Subject<void>();
  setTextChangeId:any = null;
  data :any = '';

  constructor(private dialogService: NbDialogService,
              private callDetailService : CallDetailService,
              private cd: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.data = JSON.parse(JSON.stringify(this.rowData));
    this.value = this.data.description;
  }

  changed($event){
    if(this.setTextChangeId == null)
      {
        this.setTextChangeId = setTimeout(()=>{
          this.setNewDescription();
          this.cd.detectChanges();
        },3000);
      }
      else{
        clearTimeout(this.setTextChangeId);
        this.setTextChangeId = setTimeout(()=>{
            this.setNewDescription();
            this.cd.detectChanges();
        },3000);
      }
  }

  setNewDescription()
  {
// console.log("setNewDescription");
    let data = {
      description: this.value,
      phoneNumber:this.data.customerid,
      organization: this.data.organization
    }

    this.callDetailService.addCustomerIfRequiredAndUpdateRemark(data)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: result => {

                if(String(result) == 'true')
                {
                  this.showDialoge('Success','activity-outline','success', "Customer remark updated");
                }
                else{
                   // console.log("Result is not true");
                    this.showDialoge('Error','activity-outline','danger', "Customer remark was not updated"); 
                }
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

    ngOnDestroy() {
      this.destroy$.next();
      this.destroy$.complete();
    }
    
}
