import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConferenceComponent } from './conference.component';
import { RouterModule } from '@angular/router';
import { ConferenceRoutingModule } from './conference-routing.module';
import { AllConferencesComponent } from './all-conferences/all-conferences.component';
import { ConferenceService } from './service/conference.service';
import {
  NbAccordionModule,
  NbButtonModule,
  NbCardModule,
  NbListModule,
  NbRouteTabsetModule,
  NbStepperModule,
  NbTabsetModule, NbUserModule,NbTimepickerModule,NbDatepickerModule,NbToggleModule,
  NbIconModule, NbInputModule, NbTreeGridModule ,NbRadioModule,NbOptionModule,NbSelectModule,NbFormFieldModule,NbTagModule, NbDialogModule
} from '@nebular/theme';
import { CardComponent } from './all-conferences/card/card.component';
import { Ng2SmartTableModule } from 'ng2-smart-table';
import { FormsModule } from '@angular/forms';
import { NgxChartsModule } from '@swimlane/ngx-charts';

@NgModule({
  declarations: [
    ConferenceComponent,
    AllConferencesComponent,
    CardComponent
  ],
  imports: [
    NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule, CommonModule,ConferenceRoutingModule,RouterModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule
  ],
  providers :[ConferenceService],
})
export class ConferenceModule { }
