import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { AllCostsComponent } from './all-costs/all-costs.component';
import { CallingCostComponent } from './calling-cost.component';

const routes: Routes = [{
  path: '',
  component: CallingCostComponent,
  children: [
    {
      path: 'all-costs',
      component: AllCostsComponent,
    },
    {
      path: '**',
      component: NotFoundComponent,
    },
  ],
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CallingCostRoutingModule {
}