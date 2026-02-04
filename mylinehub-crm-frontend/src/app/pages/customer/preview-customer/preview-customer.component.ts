import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { NbDialogService, NbThemeService } from '@nebular/theme';
import { PreviewCustomerDataService } from './preview-customer-data-service/preview-customer-data.service';
import { CustomerService } from '../service/customer.service';
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';

interface CardSettings {
  title: string;
  iconClass: string;
  type: string;
}

@Component({
  selector: 'ngx-preview-customer',
  templateUrl: './preview-customer.component.html',
  styleUrls: ['./preview-customer.component.scss']
})
export class PreviewCustomerComponent implements OnInit, OnDestroy {

  private destroy$: Subject<void> = new Subject<void>();
  redirectDelay: number = 0;
  // profilePic = 'cool';
 
  organization : string;
  currentTheme: string;
  themeSubscription: any;
  imageURL = 'customers';
  addtionalInfoImageURL = 'addtionalInfo';


  updateProductInterestListCard: CardSettings = {
    title: 'Interested Products',
    iconClass: 'nb-e-commerce',
    type: 'primary',
  };
 
  convertedDivertedCard: CardSettings = {
    title: 'Converted/Diverted',
    iconClass: 'nb-lightbulb',
    type: 'primary',
  };

  statusCards: string;

  commonStatusCardsSet: CardSettings[] = [
    this.convertedDivertedCard,
    this.updateProductInterestListCard,
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
        ...this.convertedDivertedCard,
        type: 'warning',
      },
      {
        ...this.updateProductInterestListCard,
        type: 'warning',
      },
      
    ],
    dark: this.commonStatusCardsSet,
  };

  showAction : boolean = true;
  colorScheme: { domain: any[]; };

  constructor(protected previewCustomerDataService:PreviewCustomerDataService,
              private themeService: NbThemeService,
              private dialogService: NbDialogService,
              private customerService:CustomerService,) { 

                this.themeSubscription = this.themeService.getJsTheme().subscribe(theme => {
                  this.currentTheme = theme.name;
                  this.statusCards = this.statusCardsByThemes[theme.name];
                  const colors: any = theme.variables;
                  this.colorScheme = {
                  domain: [colors.primaryLight, colors.infoLight, colors.successLight, colors.warningLight, colors.dangerLight],
                };
                });


                //console.log("constructor profile");
                //console.log(ConstantsService.user);
                this.organization = localStorage.getItem("organization");

              }
  

  getChildData(event){

    console.log(event);

    if(event == 'Converted/Diverted'){
      
    }

    if(event == 'Interested Products'){}
  }              

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }
  
  ngOnInit(): void {
  }


  submitDescription()
  {
      console.log('submit description button pressed')

      if(this.previewCustomerDataService.currentRecord != null)
      {
         this.customerService.updateCustomerDescription( {description: this.previewCustomerDataService.currentRecord.description, id: this.previewCustomerDataService.currentRecord.id,organization: this.previewCustomerDataService.currentRecord.organization})
                 .pipe(takeUntil(this.destroy$))
                 .subscribe({
                   next: result => {
                     if(String(result) == 'true')
                     {
                       console.log("Result is true, setting values to show on icon"); 
                       this.showDialoge('Success','activity-outline','success', "Customer remark updated."); 
                     }
                     else{
                        // console.log("Result is not true");
                         this.showDialoge('Error','activity-outline','danger', "Customer remarks was not updated."); 
                     }
                   },
                   error: err => {
                   console.log("Error : "+ JSON.stringify(err));
                    //  this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
                   }
                 });
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
