import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WhatsappReportComponent } from './whatsapp-report.component';
import { ReportComponent } from './report/report.component';
import { RouterModule } from '@angular/router';
import {
  NbAccordionModule,
  NbButtonModule,
  NbCardModule,
  NbListModule,
  NbRouteTabsetModule,
  NbStepperModule,
  NbTabsetModule, NbUserModule,NbTimepickerModule,NbDatepickerModule,NbToggleModule,
  NbIconModule, NbInputModule, NbTreeGridModule ,NbRadioModule,NbOptionModule,NbSelectModule,NbFormFieldModule,NbTagModule, NbDialogModule,
  NbAutocompleteModule,
  NbButtonGroupModule,
  NbSpinnerModule
} from '@nebular/theme';

import { NgxChartsModule } from '@swimlane/ngx-charts';
import { FormsModule } from '@angular/forms';
import { Ng2SmartTableModule } from 'ng2-smart-table';
import { WhatsAppReportRoutingModule } from './whatsapp-report-routing.module';
import { ChartsModule } from '../charts/charts.module';

@NgModule({
  declarations: [
    WhatsappReportComponent,
    ReportComponent
  ],
  imports: [
    WhatsAppReportRoutingModule,
    CommonModule,
    RouterModule,
    NbAutocompleteModule,
    NbButtonGroupModule,
    NbSpinnerModule,
    NbListModule,
    ChartsModule,
    NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule, NbInputModule, NbTreeGridModule,
  ],
  providers:[]
})
export class WhatsappReportModule { }
