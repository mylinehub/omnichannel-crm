import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IvrComponent } from './ivr.component';
import { RouterModule } from '@angular/router';
import { IvrRoutingModule } from './ivr-routing.module';
import { AllIvrsComponent } from './all-ivrs/all-ivrs.component';
import { IvrService } from './service/ivr.service';
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
import { CardComponent } from './all-ivrs/card/card.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { FormsModule } from '@angular/forms';
import { Ng2SmartTableModule } from 'ng2-smart-table';
@NgModule({
  declarations: [
    IvrComponent,
    AllIvrsComponent,
    CardComponent
  ],
  imports: [
    NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule, CommonModule,IvrRoutingModule,RouterModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule
  ],
  providers:[IvrService]
})
export class IvrModule { }
