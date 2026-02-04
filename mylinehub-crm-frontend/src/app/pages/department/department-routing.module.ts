import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { AllDepartmentsComponent } from './all-departments/all-departments.component';
import { DepartmentComponent } from './department.component';


const routes: Routes = [{
  path: '',
  component: DepartmentComponent,
  children: [
    {
      path: 'all-departments',
      component: AllDepartmentsComponent,
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
export class DepartmentRoutingModule {
}