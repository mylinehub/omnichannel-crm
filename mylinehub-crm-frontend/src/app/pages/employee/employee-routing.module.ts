import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { ProfileComponent } from './profile/profile.component';
import { AllEmployeesComponent } from './all-employees/all-employees.component';
import { MonitorEmployeesComponent } from './monitor-employees/monitor-employees.component';
import { EmployeeComponent } from './employee.component';
import { EmployeeCallHistoryComponent } from './employee-call-history/employee-call-history.component';

const routes: Routes = [{
  path: '',
  component: EmployeeComponent,
  children: [
    {
      path: 'profile',
      component: ProfileComponent,
    },
    {
      path: 'employee-call-history',
      component: EmployeeCallHistoryComponent,
    },
    {
      path: 'all-employees',
      component: AllEmployeesComponent,
    },
    {
      path: 'monitor-employees',
      component: MonitorEmployeesComponent,
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
export class EmployeeRoutingModule {
}