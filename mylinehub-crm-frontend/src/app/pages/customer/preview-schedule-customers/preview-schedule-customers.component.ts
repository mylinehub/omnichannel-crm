import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { NbDialogService, NbThemeService } from '@nebular/theme';
import { PreviewScheduleDataServiceService } from './preview-schedule-customer-data-service/preview-schedule-data-service.service';
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';

interface CardSettings {
  title: string;
  iconClass: string;
  type: string;
}

@Component({
  selector: 'ngx-preview-schedule-customers',
  templateUrl: './preview-schedule-customers.component.html',
  styleUrls: ['./preview-schedule-customers.component.scss']
})
export class PreviewScheduleCustomersComponent implements OnInit, OnDestroy {

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

  constructor(protected previewScheduleDataServiceService:PreviewScheduleDataServiceService,
              private themeService: NbThemeService,
              private dialogService: NbDialogService,) { 

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
    if(event == 'Converted/Diverted'){}

    if(event == 'Interested Products'){}
  }              

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }
  
  ngOnInit(): void {
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
