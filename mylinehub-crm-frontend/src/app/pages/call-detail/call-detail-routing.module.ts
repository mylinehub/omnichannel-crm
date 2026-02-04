import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { AllCallsComponent } from './all-calls/all-calls.component';
import { CallDetailComponent } from './call-detail.component';
import { CallDashboardComponent } from './call-dashboard/call-dashboard.component';
import { CallHistoryComponent } from './call-history/call-history.component';

const routes: Routes = [{
  path: '',
  component: CallDetailComponent,
  children: [
    {
      path: 'call-dashboard',
      component: CallDashboardComponent,
    },
    {
      path: 'all-calls',
      component: AllCallsComponent,
    },
    {
      path: 'call-history',
      component: CallHistoryComponent,
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
export class CallDetailRoutingModule {
}