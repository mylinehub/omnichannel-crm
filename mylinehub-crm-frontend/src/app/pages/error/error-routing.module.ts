import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { AllErrorsComponent } from './all-errors/all-errors.component';
import { ErrorComponent } from './error.component';

const routes: Routes = [{
  path: '',
  component: ErrorComponent,
  children: [
    {
      path: 'all-errors',
      component: AllErrorsComponent,
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
export class ErrorRoutingModule {
}