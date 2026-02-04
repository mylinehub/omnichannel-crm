import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { AbsenteeismComponent } from './absenteeism.component';
import { MyAbsenteeismComponent } from './my-absenteeism/my-absenteeism.component';
import { AllAbsenteeismComponent } from './all-absenteeism/all-absenteeism.component';

const routes: Routes = [{
  path: '',
  component: AbsenteeismComponent,
  children: [
    {
      path: 'my-absenteeism',
      component: MyAbsenteeismComponent,
    },
    {
      path: 'all-absenteeism',
      component: AllAbsenteeismComponent,
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
export class AbsenteeismRoutingModule {
}