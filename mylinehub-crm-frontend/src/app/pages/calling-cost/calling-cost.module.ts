import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CallingCostComponent } from './calling-cost.component';
import { RouterModule } from '@angular/router';
import { CallingCostRoutingModule } from './calling-cost-routing.module';
import { AllCostsComponent } from './all-costs/all-costs.component';
import { CallingCostService } from './service/calling-cost.service';
import {
  NbAccordionModule,
  NbButtonModule,
  NbCardModule,
  NbListModule,
  NbRouteTabsetModule,
  NbStepperModule,
  NbTabsetModule, NbUserModule,NbTimepickerModule,NbDatepickerModule,NbToggleModule,
  NbIconModule, NbInputModule, NbTreeGridModule ,NbRadioModule,NbOptionModule,NbSelectModule,NbFormFieldModule,NbTagModule, NbDialogModule,
  NbAutocompleteModule
} from '@nebular/theme';
import { CardComponent } from './all-costs/card/card.component';
import { CellScrollComponent } from './all-costs/cell-scroll/cell-scroll.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { FormsModule } from '@angular/forms';
import { Ng2SmartTableModule } from 'ng2-smart-table';
import { ReplacePipe } from './pipes/replace.pipe';


@NgModule({
  declarations: [
    CallingCostComponent,
    AllCostsComponent,
    CardComponent,
    CellScrollComponent,
    ReplacePipe
  ],
  imports: [
    NbAutocompleteModule, NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule, CommonModule,CallingCostRoutingModule,RouterModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule
  ],
  providers :[CallingCostService],
})
export class CallingCostModule { }
