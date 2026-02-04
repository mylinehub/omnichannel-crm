import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { DialogComponent } from '../../../employee/all-employees/dialog/dialog.component';
import { NbDialogService } from '@nebular/theme';
import { Subject, takeUntil } from 'rxjs';
import { CallDetailService } from '../../service/call-detail.service';

@Component({
  selector: 'ngx-converted-button',
  templateUrl: './converted-button.component.html',
  styleUrls: ['./converted-button.component.scss']
})
export class ConvertedButtonComponent implements OnInit, OnDestroy {

  @Input() rowData; // data from table
  value:any = 'false'; 
  status="danger";
  data:any='';
  changeTo:boolean = false;
  buttonSize:string = "medium";

  private destroy$: Subject<void> = new Subject<void>();
  screenWidth: number;
  screenHeight: number;
  itsMobile: boolean;

  constructor(private dialogService: NbDialogService,
              private callDetailService : CallDetailService,
  ) { }

  getScreenSize() {
      this.screenWidth = window.innerWidth;
      this.screenHeight = window.innerHeight;
      console.log(`Screen width: ${this.screenWidth}, Screen height: ${this.screenHeight}`);
      if(this.screenWidth<555){
        this.itsMobile = true;
        this.buttonSize = "tiny";
      }
    }

  ngOnInit(): void {

    this.getScreenSize();

    this.data = JSON.parse(JSON.stringify(this.rowData));

    this.value = this.data.coverted;

    if(this.data.coverted == false)
    {
      this.status = "danger";
      this.changeTo = true;
    }
    else{
      this.status = "success";
      this.changeTo = false;
    }
  }

  buttonClicked(){

    // console.log("button clicked");
    // console.log("this.status : "+this.status);
    // console.log("this.changeTo : "+this.changeTo);
    // console.log("this.value : "+this.value);

    let change = this.changeTo;

    if(String(this.changeTo) == 'true')
      {
        // console.log("this.changeTo is true");
          this.status = "success";
          this.value = 'true';
          this.changeTo = false;
      }
    else{
        // console.log("this.changeTo is false");
        this.status = "danger";
        this.value = 'false';
        this.changeTo = true;
    }

    let data = {
      description: this.data.description,
	    converted: change,
	    phoneNumber:this.data.customerid,
	    organization: this.data.organization
    }

    this.callDetailService.addCustomerIfRequiredAndConvert(data)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: result => {
                if(String(result) == 'true')
                {
                  // console.log("Inside api");
                  // console.log("Showing message");
                  // console.log("this.status : "+this.status);
                  // console.log("this.changeTo : "+this.changeTo);
                  // console.log("this.value : "+this.value);
                  this.showDialoge('Success','activity-outline','success', "Customer was converted");
                }
                else{
                   // console.log("Result is not true");
                    this.showDialoge('Error','activity-outline','danger', "Customer was not converted"); 
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
