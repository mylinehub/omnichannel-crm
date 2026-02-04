import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbsenteeismComponent } from './absenteeism.component';
import { RouterModule } from '@angular/router';
import { AbsenteeismRoutingModule } from './absenteeism-routing.module';
import { AbsenteeismService } from './service/absenteeism.service';
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
import { AllAbsenteeismComponent } from './all-absenteeism/all-absenteeism.component';
import { MyAbsenteeismComponent } from './my-absenteeism/my-absenteeism.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { FormsModule } from '@angular/forms';
import { Ng2SmartTableModule } from 'ng2-smart-table';
import { CardComponent } from './all-absenteeism/card/card.component';

@NgModule({
  declarations: [
    AbsenteeismComponent,
    AllAbsenteeismComponent,
    MyAbsenteeismComponent,
    CardComponent,
  ],
  imports: [
    NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule, CommonModule,AbsenteeismRoutingModule,RouterModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule
  ],
  providers: [
    AbsenteeismService,
  ]
})
export class AbsenteeismModule { }
