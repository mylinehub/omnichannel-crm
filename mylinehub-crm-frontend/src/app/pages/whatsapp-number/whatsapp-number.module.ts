import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WhatsappNumberComponent } from './whatsapp-number.component';
import { DetailsComponent } from './details/details.component';
import { WhatsAppNumberRoutingModule } from './whatsapp-number-routing.module';
import { RouterModule } from '@angular/router';
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

import { NgxChartsModule } from '@swimlane/ngx-charts';
import { FormsModule } from '@angular/forms';
import { Ng2SmartTableModule } from 'ng2-smart-table';
import { AssignEmployeesComponent } from './assign-employees/assign-employees.component';

@NgModule({
  declarations: [
    WhatsappNumberComponent,
    DetailsComponent,
    AssignEmployeesComponent
  ],
  imports: [
      NbAccordionModule,
      WhatsAppNumberRoutingModule,
      CommonModule,
      RouterModule,
      NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule
    ],
  providers:[]
})
export class WhatsappNumberModule { }
