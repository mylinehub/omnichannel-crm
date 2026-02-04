import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { AllPurchasesComponent } from './all-purchases/all-purchases.component';
import { PurchasesComponent } from './purchases.component';

const routes: Routes = [{
  path: '',
  component: PurchasesComponent,
  children: [
    {
      path: 'all-purchases',
      component: AllPurchasesComponent,
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
export class PurchasesRoutingModule {
}