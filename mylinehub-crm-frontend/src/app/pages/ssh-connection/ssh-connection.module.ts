import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SshConnectionComponent } from './ssh-connection.component';
import { RouterModule } from '@angular/router';
import { SshConnectionRoutingModule } from './ssh-connection-routing.module';
import { RegistriesComponent } from './registries/registries.component';
import { SshConnectionService } from './service/ssh-connection.service';
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
import { CellScrollComponent } from './registries/cell-scroll/cell-scroll.component';
@NgModule({
  declarations: [
    SshConnectionComponent,
    RegistriesComponent,
    CardComponent,
    CellScrollComponent
  ],
  imports: [
    NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule, CommonModule,SshConnectionRoutingModule,RouterModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule
  ],
  providers:[SshConnectionService]
})
export class SshConnectionModule { }
