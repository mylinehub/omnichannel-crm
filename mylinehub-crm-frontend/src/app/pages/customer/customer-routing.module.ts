import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { CustomerComponent } from './customer.component';
import { AllCustomersComponent } from './all-customers/all-customers.component';
import { PreviewCustomerComponent } from './preview-customer/preview-customer.component';
import { PreviewScheduleCustomersComponent } from './preview-schedule-customers/preview-schedule-customers.component';

const routes: Routes = [{
  path: '',
  component: CustomerComponent,
  children: [
    {
      path: 'all-customers',
      component: AllCustomersComponent,
    },
    {
      path: 'preview-customers',
      component: PreviewCustomerComponent,
    },
    {
      path: 'preview-schedule-customers',
      component: PreviewScheduleCustomersComponent,
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
export class CustomerRoutingModule {
}