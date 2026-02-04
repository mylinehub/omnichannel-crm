import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AmiConnectionComponent } from './ami-connection.component';
import { RouterModule } from '@angular/router';
import { AmiConnectionRoutingModule } from './ami-connection-routing.module';
import { RegistriesComponent } from './registries/registries.component';
import { AmiConnectionService } from './service/ami-connection.service';
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
import { CardComponent } from './registries/card/card.component';


@NgModule({
  declarations: [
    AmiConnectionComponent,
    RegistriesComponent,
    CardComponent,
  ],
  imports: [
    NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule, CommonModule,AmiConnectionRoutingModule,RouterModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule
  ],
  providers: [AmiConnectionService]
})
export class AmiConnectionModule { }
