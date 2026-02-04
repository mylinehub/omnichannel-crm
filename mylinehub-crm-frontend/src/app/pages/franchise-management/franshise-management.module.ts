import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FranchiseCallListingComponent } from './franchise-call-listing/franchise-call-listing.component';
import { CronEditorModule } from 'cron-editor';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { FormsModule } from '@angular/forms';
import { Ng2SmartTableModule } from 'ng2-smart-table';
import { RouterModule } from '@angular/router';
import { FranchiseManagementRoutingModule } from './franshise-management-routing.module';
import { NbAccordionModule, NbAutocompleteModule, NbButtonModule, NbCardModule, NbDatepickerModule, NbDialogModule, NbFormFieldModule, NbIconModule, NbInputModule, NbListModule, NbOptionModule, NbRadioModule, NbRouteTabsetModule, NbSelectModule, NbStepperModule, NbTabsetModule, NbTagModule, NbTimepickerModule, NbToggleModule, NbTreeGridModule, NbUserModule } from '@nebular/theme';


@NgModule({
  declarations: [FranchiseCallListingComponent],
  imports: [
    CommonModule,
    NbCardModule,
    FranchiseManagementRoutingModule,NbAutocompleteModule,CronEditorModule,NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule,RouterModule, NbAccordionModule, NbButtonModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule
  ],
})
export class FranshiseManagementModule { }
