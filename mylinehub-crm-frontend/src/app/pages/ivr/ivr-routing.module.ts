import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { AllIvrsComponent } from './all-ivrs/all-ivrs.component';
import { IvrComponent } from './ivr.component';

const routes: Routes = [{
  path: '',
  component: IvrComponent,
  children: [
    {
      path: 'all-ivrs',
      component: AllIvrsComponent,
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
export class IvrRoutingModule {
}