import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CampaignComponent } from './campaign.component';
import { RouterModule } from '@angular/router';
import { CampaignRoutingModule } from './campaign-routing.module';
import { AllCampaignsComponent } from './all-campaigns/all-campaigns.component';
import { CampaignService } from './service//campaign.service';
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
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { FormsModule } from '@angular/forms';
import { Ng2SmartTableModule } from 'ng2-smart-table';
import { CardComponent } from './all-campaigns/card/card.component';
import { SubmitBulkCustomerComponent } from './all-campaigns/submit-bulk-customer/submit-bulk-customer.component';
import { SubmitBulkEmployeeComponent } from './all-campaigns/submit-bulk-employee/submit-bulk-employee.component';
import { CheckBoxComponent } from './all-campaigns/submit-bulk-customer/check-box/check-box.component';
import { ScheduleCardComponent } from './all-campaigns/schedule-card/schedule-card.component';
import { NSecDialogComponent } from './all-campaigns/schedule-card/n-sec-dialog/n-sec-dialog.component';
import { FixedDateDialogComponent } from './all-campaigns/schedule-card/fixed-date-dialog/fixed-date-dialog.component';
import { CronDialogComponent } from './all-campaigns/schedule-card/cron-dialog/cron-dialog.component';
import { CronEditorModule } from 'cron-editor';
import { AskDeleteComponent } from './ask-delete/ask-delete.component';
import { ScheduleStopCardComponent } from './all-campaigns/schedule-stop-card/schedule-stop-card.component';
import { NSecDialogStopComponent } from './all-campaigns/schedule-stop-card/n-sec-dialog-stop/n-sec-dialog-stop.component';
import { FixedDateDialogStopComponent } from './all-campaigns/schedule-stop-card/fixed-date-dialog-stop/fixed-date-dialog-stop.component';
import { CronDialogStopComponent } from './all-campaigns/schedule-stop-card/cron-dialog-stop/cron-dialog-stop.component';
import { RunCampaignViewComponent } from './run-campaign-view/run-campaign-view.component';
//import { CronEditorModule } from 'ngx-cron-editor';

@NgModule({
  declarations: [
    CampaignComponent,
    AllCampaignsComponent,
    CardComponent,
    SubmitBulkCustomerComponent,
    SubmitBulkEmployeeComponent,
    CheckBoxComponent,
    ScheduleCardComponent,
    NSecDialogStopComponent,
    FixedDateDialogStopComponent,
    CronDialogStopComponent,
    NSecDialogComponent,
    FixedDateDialogComponent,
    CronDialogComponent,
    AskDeleteComponent,
    ScheduleStopCardComponent,
    RunCampaignViewComponent
  ],
  imports: [
    NbAutocompleteModule,CronEditorModule,NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule, CommonModule,CampaignRoutingModule,RouterModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule
  ],
  providers :[CampaignService],
})
export class CampaignModule { }
