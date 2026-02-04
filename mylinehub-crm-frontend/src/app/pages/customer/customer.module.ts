import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CustomerComponent } from './customer.component';
import { RouterModule } from '@angular/router';
import { CustomerRoutingModule } from './customer-routing.module';
import { AllCustomersComponent } from './all-customers/all-customers.component';
import { CustomerService } from './service/customer.service';
import {
  NbAutocompleteModule,
  NbAccordionModule,
  NbButtonModule,
  NbCardModule,
  NbListModule,
  NbRouteTabsetModule,
  NbStepperModule,
  NbTabsetModule, NbUserModule,NbTimepickerModule,NbDatepickerModule,NbToggleModule,
  NbIconModule, NbInputModule, NbTreeGridModule ,NbRadioModule,NbOptionModule,NbSelectModule,NbFormFieldModule,NbTagModule, NbDialogModule
} from '@nebular/theme';


import { CardComponent } from './all-customers/card/card.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { FormsModule } from '@angular/forms';
import { Ng2SmartTableModule } from 'ng2-smart-table';
import { PreviewCustomerComponent } from './preview-customer/preview-customer.component';
import { ScheduleCustomerCardComponent } from './all-customers/schedule-customer-card/schedule-customer-card.component';
import { CronCustomerDialogComponent } from './all-customers/schedule-customer-card/cron-customer-dialog/cron-customer-dialog.component';
import { FixDateCustomerDialogComponent } from './all-customers/schedule-customer-card/fix-date-customer-dialog/fix-date-customer-dialog.component';
import { NSecCustomerDialogComponent } from './all-customers/schedule-customer-card/n-sec-customer-dialog/n-sec-customer-dialog.component';
import { CronEditorModule } from 'cron-editor';
import { ProductInterestDialogComponent } from './preview-customer/product-interest-dialog/product-interest-dialog.component';
import { ImageCorouselComponent } from './image-corousel/image-corousel.component';
import { PreviewScheduleCustomersComponent } from './preview-schedule-customers/preview-schedule-customers.component';

@NgModule({
  declarations: [
    CustomerComponent,
    AllCustomersComponent,
    CardComponent,
    PreviewCustomerComponent,
    ScheduleCustomerCardComponent,
    CronCustomerDialogComponent,
    FixDateCustomerDialogComponent,
    NSecCustomerDialogComponent,
    ProductInterestDialogComponent,
    ImageCorouselComponent,
    PreviewScheduleCustomersComponent
  ],
  imports: [
    NbAutocompleteModule,CronEditorModule,NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule, CommonModule,CustomerRoutingModule,RouterModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule
  ],
  providers:[CustomerService]
})
export class CustomerModule { }
