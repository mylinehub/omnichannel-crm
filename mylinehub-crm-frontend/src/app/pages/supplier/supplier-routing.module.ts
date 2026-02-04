import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { AllSuppliersComponent } from './all-suppliers/all-suppliers.component';
import { SupplierComponent } from './supplier.component';

const routes: Routes = [{
  path: '',
  component: SupplierComponent,
  children: [
    {
      path: 'all-suppliers',
      component: AllSuppliersComponent,
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
export class SupplierRoutingModule {
}