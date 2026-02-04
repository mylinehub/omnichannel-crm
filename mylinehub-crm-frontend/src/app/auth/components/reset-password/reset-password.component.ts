import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Inject, OnInit,OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { NB_AUTH_OPTIONS, NbAuthSocialLink } from '@nebular/auth';
import { getDeepFromObject } from '@nebular/auth';
import { Subject } from 'rxjs';
import { NbAuthService } from '@nebular/auth';
import { ResetPasswordService } from './../../service/reset-passowrd/reset-password.service';
import { ConstantsService } from './../../../service/constants/constants.service';
import { HttpHeaders } from '@angular/common/http';
import { takeUntil } from 'rxjs/operators';
import { JwtHelperService } from '@auth0/angular-jwt'
import { DatePipe } from '@angular/common';

@Component({
  selector: 'ngx-reset-password',
  templateUrl: './reset-password.component.html',
  //styleUrls: ['./reset-password.component.scss']
})
export class NgxResetPasswordComponent implements OnInit, OnDestroy{

  private static doWeNeedRestart : boolean = true;
  private destroy$: Subject<void> = new Subject<void>();

  redirectDelay: number = 0;
  showMessages: any = {};
  strategy: string = '';

  submitted = false;
  errors: string[] = [];
  messages: string[] = [];
  user: any = {};

  paddingTop:number = 20;

  screenWidth: number;
  screenHeight: number;
  orientation = 'horizontal';

  passwordInputTypeForPassword: string = 'password';
  passwordVisibilityIconForPassword: string = 'eye-off-outline'; // Or 'eye-outline'
  passwordInputTypeForConfirm: string = 'password';
  passwordVisibilityIconForConfirm: string = 'eye-off-outline'; // Or 'eye-outline'

  constructor(protected service: NbAuthService,
              @Inject(NB_AUTH_OPTIONS) protected options = {},
              protected cd: ChangeDetectorRef,
              protected router: Router,
              protected constantService : ConstantsService,
              protected resetPasswordService : ResetPasswordService,
              protected jwtHelperService:JwtHelperService,
              protected datepipe: DatePipe) {

    this.redirectDelay = this.getConfigValue('forms.resetPassword.redirectDelay');
    this.showMessages = this.getConfigValue('forms.resetPassword.showMessages');
    this.strategy = this.getConfigValue('forms.resetPassword.strategy');
  }
  ngOnInit(): void {
    //console.log ("Verifying if we have already logged in");
    // isValid = isTokenExpired(localStorage.getItem('token'));
    //console.log("App Module Auth Gaurd: canActivate : "+  this.jwtHelperService.getTokenExpirationDate(localStorage.getItem('token')));
      
    var isExpired : boolean  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));

   if(isExpired)
   {
     this.router.navigate([this.constantService.LOGIN_ENDPOINT]);
   }
    
   this.getScreenSize();

  }

  resetPass(): void {
   
    //console.log("Reset password button is clicked in this custom function");   

    if(localStorage.getItem("token") === null)
    {
     // console.log('Routing to login page');
      setTimeout(() => {
        return this.router.navigateByUrl(this.constantService.LOGIN_ENDPOINT);
      }, this.redirectDelay);
    }
    else
    {

      const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem("token")}`
      });

      const requestOptions = { headers: headers };

      this.resetPasswordService.reset_password({ email: localStorage.getItem("email"), password: this.user.password , organization: localStorage.getItem("organization") },requestOptions)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: data => {

           // console.log("SuccessfulPassword Reset");

            //https://florimond.dev/en/posts/2018/09/consuming-apis-in-angular-the-model-adapter-pattern/
            //str = JSON.stringify(obj);
            //obj = JSON.parse(str);

          this.messages = ['Reset Password Successful'];
          //console.log("Logout : Reset Password Success : Remiving store values for re login");
          

          ConstantsService.token = '';
          ConstantsService.organization = '';
          ConstantsService.user = '';
          ConstantsService.role = '';
          ConstantsService.extension = '';
          ConstantsService.isAuthenticated = false;
          ConstantsService.decodedToken = '';
          ConstantsService.email = '';
          ConstantsService.password = '';
          ConstantsService.fetchTime = '';

         // console.log('Setting Up Local Storage To Empty It');
          localStorage.removeItem("token");
          localStorage.removeItem("organization");
          localStorage.removeItem("role");
          localStorage.removeItem("extension");
          localStorage.removeItem("email");
          localStorage.removeItem("fetchTime");

          if(localStorage.getItem("zunu")!= null)
            {
              localStorage.removeItem("zunu");
            }

         // console.log('Routing To Login Page');
          
          setTimeout(() => {
              return this.router.navigateByUrl(this.constantService.LOGIN_ENDPOINT);
            }, this.redirectDelay);

          // this.cd.detectChanges();
            
          },
          error: err => {
            this.errors = [err.error.message];
            //console.log("Error : "+ JSON.stringify(err));
          }
      });

    }
  }

  getConfigValue(key: string): any {
    return getDeepFromObject(this.options, key, null);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  getScreenSize() {
    this.screenWidth = window.innerWidth;
    this.screenHeight = window.innerHeight;
    // console.log(`Screen width: ${this.screenWidth}, Screen height: ${this.screenHeight}`);
    if(this.screenWidth<650){
      this.paddingTop = 40;
    }
    else{
      this.paddingTop = 20;
    }
  }

  togglePasswordVisibilityForPassword() {
        console.log("togglePasswordVisibility");
        if (this.passwordInputTypeForPassword === 'password') {
          this.passwordInputTypeForPassword = 'text';
          this.passwordVisibilityIconForPassword = 'eye-outline';
        } else {
          this.passwordInputTypeForPassword = 'password';
          this.passwordVisibilityIconForPassword = 'eye-off-outline';
        }
      }


  togglePasswordVisibilityForConfirm() {
        console.log("togglePasswordVisibility");
        if (this.passwordInputTypeForConfirm === 'password') {
          this.passwordInputTypeForConfirm = 'text';
          this.passwordVisibilityIconForConfirm = 'eye-outline';
        } else {
          this.passwordInputTypeForConfirm = 'password';
          this.passwordVisibilityIconForConfirm = 'eye-off-outline';
        }
      }

}
