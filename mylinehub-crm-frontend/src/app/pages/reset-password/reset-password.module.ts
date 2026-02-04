import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ResetPasswordComponent } from './reset-password.component';
import { RouterModule } from '@angular/router';
import { ResetPasswordRoutingModule} from './reset-password-routing.module';


@NgModule({
  declarations: [
    ResetPasswordComponent
  ],
  imports: [
    CommonModule,ResetPasswordRoutingModule, RouterModule
  ]
})
export class ResetPasswordModule { }
