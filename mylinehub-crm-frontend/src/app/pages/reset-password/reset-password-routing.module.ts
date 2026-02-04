import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { ResetPasswordComponent } from './reset-password.component';

const routes: Routes = [{
  path: '',
  component: ResetPasswordComponent,
  children: [
    {
      path: 'reset',
      component: ResetPasswordComponent,
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
export class ResetPasswordRoutingModule {
}