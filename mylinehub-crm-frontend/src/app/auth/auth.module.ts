import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { LoginService } from './service/login/login.service';
import { ResetPasswordService } from './service/reset-passowrd/reset-password.service';
import { NgxAuthRoutingModule } from './auth-routing.module';
import { NbAuthModule } from '@nebular/auth';
import { 
  NbAlertModule,
  NbButtonModule,
  NbCheckboxModule,
  NbInputModule,
  NbStepperModule
} from '@nebular/theme';
import { NgxLoginComponent } from './components/login/login.component';
import { NgxResetPasswordComponent } from './components/reset-password/reset-password.component';
import { NgxAuthComponent } from './components/auth.component';
import {
  NbLayoutModule,
  NbCardModule,
  NbIconModule,
} from '@nebular/theme';
import { ThemeModule } from '../@theme/theme.module';

@NgModule({
  imports: [
    NbIconModule,
    NbCardModule,
    CommonModule,
    FormsModule,
    RouterModule,
    NbAlertModule,
    NbInputModule,
    NbButtonModule,
    NbCheckboxModule,
    NgxAuthRoutingModule,
    NbLayoutModule,
    NbAuthModule,
    NbStepperModule,
    ThemeModule
  ],
  declarations: [
    // ... here goes our new components
    NgxLoginComponent,
    NgxResetPasswordComponent,
    NgxAuthComponent
  ],
  providers: [LoginService,ResetPasswordService,],
})
export class NgxAuthModule {
}