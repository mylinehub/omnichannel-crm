import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PurchasesComponent } from './purchases.component';
import { RouterModule } from '@angular/router';
import { PurchasesRoutingModule} from './purchases-routing.module';
import { AllPurchasesComponent } from './all-purchases/all-purchases.component';
import { PurchaseService } from './service/purchase.service';
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
import { CardComponent } from './all-purchases/card/card.component';
@NgModule({
  declarations: [
    PurchasesComponent,
    AllPurchasesComponent,
    CardComponent
  ],
  imports: [
    NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule, CommonModule,PurchasesRoutingModule,RouterModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule
  ],
  providers:[PurchaseService]
})
export class PurchasesModule { }
