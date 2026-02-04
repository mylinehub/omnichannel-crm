import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NgxLoginComponent } from './components/login/login.component';
import { NgxResetPasswordComponent } from './components/reset-password/reset-password.component';
import { NgxAuthComponent } from './components/auth.component';

import {
    NbAuthComponent,
    NbResetPasswordComponent,
  } from '@nebular/auth';

  
export const routes: Routes = [
  // .. here goes our components routes
  {
    path: '',
    component: NgxAuthComponent,
    children: [
      {
        path: '',
        component: NgxLoginComponent,
      },
      {
        path: 'login',
        component: NgxLoginComponent,
      },
      /*{
        path: 'register',
        component: NbRegisterComponent,
      },
      {
        path: 'logout',
        component: NbLogoutComponent,
      },
      {
        path: 'request-password',
        component: NbRequestPasswordComponent,
      },*/
      {
        path: 'reset-password',
        component: NgxResetPasswordComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class NgxAuthRoutingModule {
}