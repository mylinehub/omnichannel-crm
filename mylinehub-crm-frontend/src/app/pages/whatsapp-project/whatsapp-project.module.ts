import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WhatsappProjectComponent } from './whatsapp-project.component';
import { DetailsComponent } from './details/details.component';
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
import { WhatsAppProjectRoutingModule } from './whatsapp-project-routing.module';
import { AiAccountComponent } from './ai-account/ai-account.component';


@NgModule({
  declarations: [
    WhatsappProjectComponent,
    DetailsComponent,
    AiAccountComponent
  ],
  imports: [
      WhatsAppProjectRoutingModule,
      CommonModule,
      RouterModule,
      NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule
    ],
    providers:[]
})
export class WhatsappProjectModule { }
