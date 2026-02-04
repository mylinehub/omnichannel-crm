import { Component, OnDestroy, OnInit } from '@angular/core';
import { EmployeeService } from './../service/employee.service';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { ConstantsService } from '../../../service/constants/constants.service';
import { DialogComponent } from '../all-employees/dialog/dialog.component';
import { ResetExtensionPasswordComponent } from '../profile/reset-extension-password/reset-extension-password.component';
import { NbDialogService, NbThemeService } from '@nebular/theme';

interface CardSettings {
  title: string;
  iconClass: string;
  type: string;
}

@Component({
  selector: 'ngx-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit, OnDestroy {

  private destroy$: Subject<void> = new Subject<void>();
  redirectDelay: number = 0;
  // profilePic = 'cool';
  // docOne = 'cool';
  // docTwo = 'cool';
  currentRecord : any;
  organization : string;
  currentTheme: string;
  themeSubscription: any;
  imageURL = 'youthWithPhone';
  addtionalInfoImageURL = 'addtionalInfo';

  parkedValues:string[] = ['','','','','','','','','',''];
  parkedStatus:string[] = ['danger','danger','danger','danger','danger','danger','danger','danger','danger','danger'];
  currentByteImageData:any=[];
  base64ImageData:any = [];

  enableDisableCard: CardSettings = {
    title: 'Call On Mobile',
    iconClass: 'nb-lightbulb',
    type: 'primary',
  };

  statusCards: string;

  commonStatusCardsSet: CardSettings[] = [
    this.enableDisableCard
  ];

  statusCardsByThemes: {
    default: CardSettings[];
    cosmic: CardSettings[];
    corporate: CardSettings[];
    dark: CardSettings[];
  } = {
    default: this.commonStatusCardsSet,
    cosmic: this.commonStatusCardsSet,
    corporate: [
      {
        ...this.enableDisableCard,
        type: 'warning',
      },
      
    ],
    dark: this.commonStatusCardsSet,
  };

  showAction : boolean = true;
  colorScheme: { domain: any[]; };

  constructor(private employeeService : EmployeeService,
              private constantService : ConstantsService,
              private themeService: NbThemeService,
              private dialogService: NbDialogService) { 

                this.themeSubscription = this.themeService.getJsTheme().subscribe(theme => {
                  this.currentTheme = theme.name;
                  this.statusCards = this.statusCardsByThemes[theme.name];
                  const colors: any = theme.variables;
                  this.colorScheme = {
                  domain: [colors.primaryLight, colors.infoLight, colors.successLight, colors.warningLight, colors.dangerLight],
                };
                });

              }
  
  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }
  
  ngOnInit(): void {
    //console.log("ngOnIt profile");
    //console.log(ConstantsService.user);
    if(ConstantsService.user.firstName !== undefined){
                   console.log("Loading empoloyee data");
                   this.setupNgOnInitData();
              }
    else{
                    setTimeout(() => {
                      console.log("Delay employee load due to unavailability of data");
                      this.setupNgOnInitData();
                    }, ConstantsService.DIRECT_REFRESH_PAGE_TIME_MS); // 2000 milliseconds = 2 seconds
    }
  }

  setupNgOnInitData(){
    //console.log("constructor profile");
    //console.log(ConstantsService.user);
    this.organization = localStorage.getItem("organization");
    this.employeeService.getEmployeeByEmailAndOrganization(localStorage.getItem("email"),localStorage.getItem("organization"))
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (data:any) => {
                
      // console.log(JSON.parse(JSON.stringify(data)));
      // console.log(JSON.parse(JSON.stringify(data)).parkedchannel2);
        if(data == null)
        {
          // console.log("data is null");
          this.showDialoge('Error','activity-outline','danger', "Employee not found for this email"); 
        }
        else
        {
          // console.log("data is not null");
          // console.log("Data",data);
          this.currentRecord = JSON.parse(JSON.stringify(data));
          // console.log("Before Setting Image",JSON.stringify(this.currentRecord));

          // let file = new Blob([data], {type: 'application/json'});
          // file.text()
          //         .then(value => {
                  

          //         })
          //         .catch(error => {
          //           console.log("Something went wrong" + error);
          //         });

          
          // this.profilePic = "data:image/png;base64,"+ JSON.parse(JSON.stringify(data)).imageData;
          // this.docOne = "data:image/png;base64,"+ JSON.parse(JSON.stringify(data)).governmentDocument1Data;
          // this.docTwo = "data:image/png;base64,"+ JSON.parse(JSON.stringify(data)).governmentDocument2Data;
          this.setImageData();
          this.parkedValues[0] = JSON.parse(JSON.stringify(data)).parkedchannel1;
          this.parkedValues[1] = JSON.parse(JSON.stringify(data)).parkedchannel2;
          this.parkedValues[2] = JSON.parse(JSON.stringify(data)).parkedchannel3;
          this.parkedValues[3] = JSON.parse(JSON.stringify(data)).parkedchannel4;
          this.parkedValues[4] = JSON.parse(JSON.stringify(data)).parkedchannel5;
          this.parkedValues[5] = JSON.parse(JSON.stringify(data)).parkedchannel6;
          this.parkedValues[6] = JSON.parse(JSON.stringify(data)).parkedchannel7;
          this.parkedValues[7] = JSON.parse(JSON.stringify(data)).parkedchannel8;
          this.parkedValues[8] = JSON.parse(JSON.stringify(data)).parkedchannel9;
          this.parkedValues[9] = JSON.parse(JSON.stringify(data)).parkedchannel10;

          if(String(JSON.parse(JSON.stringify(data)).parkedchannel1) != 'null'){
            this.parkedStatus[0] = "success"
          }
          else
          {
            this.parkedValues[0] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel2) != 'null')
          {
            this.parkedStatus[1] = "success"
          }
          else
          {
            this.parkedValues[1] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel3) != 'null')
          {
            this.parkedStatus[2] = "success"
          }
          else
          {
            this.parkedValues[2] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel4) != 'null')
          {
            this.parkedStatus[3] = "success"
          }
          else
          {
            this.parkedValues[3] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel5) != 'null')
          {
            this.parkedStatus[4] = "success"
          }
          else
          {
            this.parkedValues[4] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel6) != 'null')
          {
            this.parkedStatus[5] = "success"
          }
          else
          {
            this.parkedValues[5] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel7) != 'null')
          {
            this.parkedStatus[6] = "success"
          }
          else
          {
            this.parkedValues[6] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel8) != 'null')
          {
            this.parkedStatus[7] = "success"
          }
          else
          {
            this.parkedValues[7] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel9) != 'null')
          {
            this.parkedStatus[8] = "success"
          }
          else
          {
            this.parkedValues[8] = 'No value';
          }
          if(String(JSON.parse(JSON.stringify(data)).parkedchannel10) != 'null')
          {
            this.parkedStatus[9] = "success"
          }
          else
          {
            this.parkedValues[9] = 'No value';
          }

        }
      },
      error: err => {
      console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
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

    showResetPasswordDialoge() {

      this.dialogService.open(ResetExtensionPasswordComponent, {
        context: {
        },
      });
      }

    updateExtensionPassword()
    {
      console.log("updateExtensionPassword");
      this.showResetPasswordDialoge(); 
    }


    setImageData()
  {
    this.employeeService.getEmployeeImages(this.currentRecord.email,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {

        if(allData == null)
        {
          //console.log("I am null data");
          //console.log("Inside Data : "+ JSON.stringify(allData));
            this.currentByteImageData = []; 
        }
        else
        {
            //console.log("Inside Data : "+ JSON.stringify(allData));
            this.currentByteImageData = allData;    
            this.currentByteImageData.forEach((current:any,i:number)=>{
              
              try{
                    if(current != null)
                    {
                      try{
                          //  console.log("this.currentByteImageData is not null");
                          //  console.log("current");
                          //  console.log(current);
                           if(i == 0)
                           {
                            let url = 'data:image/'+this.currentRecord.imageType+';base64,'+current;
                            this.base64ImageData.push(url);
                           }
    
                           if(i == 1)
                           {
                            let url = 'data:image/'+this.currentRecord.imageType+';base64,'+current;
                            this.base64ImageData.push(url);
                           }
    
                           if(i == 2)
                           {
                            let url = 'data:image/'+this.currentRecord.imageType+';base64,'+current;
                            this.base64ImageData.push(url);
                           }
                           
                           this.base64ImageData[i] = this.base64ImageData[i].replace("/image","");
                      }  
                      catch(e)
                      {
                        // console.log(e);
                      }
                    }  
                    
                  }
              catch(e)
              {
                  if((this.base64ImageData.length-1)<i)
                  {
                    this.base64ImageData.push(null);
                  }   
              }
            });
        }
      },
      error: err => {
       console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

}
