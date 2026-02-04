
declare var google:any;

import {  ChangeDetectorRef, Component, Inject, OnInit,OnDestroy, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { NB_AUTH_OPTIONS, NbAuthSocialLink } from '@nebular/auth';
import { getDeepFromObject } from '@nebular/auth';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NbAuthService } from '@nebular/auth';
import { LoginService } from './../../service/login/login.service';
import { ConstantsService } from './../../../service/constants/constants.service';
import jwt_decode, { JwtPayload }  from 'jwt-decode'
import { JwtHelperService } from '@auth0/angular-jwt'
import { DatePipe } from '@angular/common';
import { EncrDecrService } from './../../../service/encr-decr/encr-decr.service';

@Component({
  selector: 'ngx-login',
  //changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrls: ['./login.component.scss'],
  templateUrl: './login.component.html',
//  styleUrls: ['./login.component.scss']
})
export class NgxLoginComponent implements OnInit, OnDestroy {

  private destroy$: Subject<void> = new Subject<void>();
  
  redirectDelay: number = 0;
  showMessages: any = {};
  strategy: string = '';

  errors: string[] = [];
  messages: string[] = [];
  user: any = {};
  submitted: boolean = false;
  socialLinks: NbAuthSocialLink[] = [];
  rememberMe = false;
  passwordInputType: string = 'password';
  passwordVisibilityIcon: string = 'eye-off-outline'; // Or 'eye-outline'

  screenWidth: number;
  screenHeight: number;
  orientation = 'horizontal';
  paddingTop:number = 0;

  constructor(protected service: NbAuthService,
              @Inject(NB_AUTH_OPTIONS) protected options = {},
              protected cd: ChangeDetectorRef,
              protected router: Router,
              protected loginService : LoginService,
              protected constantService : ConstantsService,
              protected jwtHelperService:JwtHelperService,
              protected datepipe: DatePipe,
              private EncrDecr: EncrDecrService,
              private ngZone: NgZone) {
    // console.log("Login constructor");
    this.redirectDelay = this.getConfigValue('forms.login.redirectDelay');
    this.showMessages = this.getConfigValue('forms.login.showMessages');
    this.strategy = this.getConfigValue('forms.login.strategy');
    this.socialLinks = this.getConfigValue('forms.login.socialLinks');
    this.rememberMe = this.getConfigValue('forms.login.rememberMe');
  }

  ngOnInit(): void { 
  //  console.log("Login ngOnInit : Setting isTokenExpired");

  this.getScreenSize();

   /* const isTokenExpired = (token: string): boolean => {
      try {

        console.log("Login ngOnInit : Decoding Token");
          const { exp } = jwt.decode(token) as {
              exp: number;
          };

          console.log("Login ngOnInit : exp : "+exp);

          const expirationDatetimeInSeconds = exp * 1000;
          
          console.log("Login ngOnInit : expirationDatetimeInSeconds : "+expirationDatetimeInSeconds);

          return Date.now() >= expirationDatetimeInSeconds;
      } catch {
        console.log("Login ngOnInit : Token is expired as per check");
          return true;
      }
  };
*/
   
   var isExpired : boolean = false;

   if(localStorage.getItem('token') === null)
   {
  //  console.log("Login ngOnInit : Token is not in local storage");
   }
   else{

    //  console.log("Login ngOnInit : Token is in local storage");
    //  console.log("Login ngOnInit Token : "+ localStorage.getItem('token'));

     // isValid = isTokenExpired(localStorage.getItem('token'));
     // console.log("Login ngOnInit Token Expiry Date : "+  this.jwtHelperService.getTokenExpirationDate(localStorage.getItem('token')));
      
      isExpired  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));

     console.log("Login ngOnInit Token validity : " + isExpired);

      if(!isExpired)
      {
         console.log("Login ngOnInit : Token is valid");
          setTimeout(() => {
            return this.router.navigateByUrl(this.constantService.DASHBOARD_ENDPOINT);
          }, this.redirectDelay);
      }
      else{
       console.log("Login ngOnInit : Token is expired");
        if(localStorage.getItem("zunu")!= null && localStorage.getItem("email")!= null)
          {
            console.log("We have email and passcode in memory");
            this.user.email = localStorage.getItem("email")
            this.user.password  = this.EncrDecr.decrypt('123456$#@$^@1ERF','123456$#@$^@1ERF', localStorage.getItem("zunu"));
            this.user.rememberMe = true;
            this.login();
          }

      }
   }

  //  google.accounts.id.initialize({
  //   client_id: '525538250273-3u3j0edt81k5lqmv31349fk043gc1asn.apps.googleusercontent.com',
  //   callback: (resp:any)=>{
  //               this. googleCallBack(resp);
  //               }
  //   });

  //  google.accounts.id.prompt();

//    google.accounts.id.renderButton(
//     document.getElementById("signinButton"),
//     { type: "icon",
//       theme: "outline",
//       size: "large",
//       shape: "rectangular" } // customization attributes
//  );

 google.accounts.id.initialize({
  client_id: "525538250273-3u3j0edt81k5lqmv31349fk043gc1asn.apps.googleusercontent.com",
  callback: (resp:any)=>{
    this.googleCallBack(resp);
    },
});

// google.accounts.id.prompt(); // also display the One Tap dialog

  google.accounts.id.renderButton(document.getElementById("google-btn"), {
    theme: 'outline',
    size: 'medium',
    shape:'rectangular',
    width:'100',
    click_listener: (resp:any)=>{
      this.googleButtonListner(resp);
    }
  }); 

  }

  // googleSignIn()
  // {
  //   console.log("googleSignIn");
  //   document.getElementById("google-btn").click();
  // }

  login(): void {

    //https://www.knowledgehut.com/blog/web-development/make-api-calls-angular
    console.log("Login is initiated");    
    //this.submitted = true;
    
    console.log("Username : "+ this.user.email);  
    console.log("Password : "+ this.user.password);  

      this.loginService.login({ username: this.user.email, password: this.user.password },'')
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: data => {

          //console.log("SuccessfulLogin");

          //https://florimond.dev/en/posts/2018/09/consuming-apis-in-angular-the-model-adapter-pattern/
          //str = JSON.stringify(obj);
          //obj = JSON.parse(str);

          this.messages = ['Login Successful'];
          
          ConstantsService.token = JSON.parse(JSON.stringify(data)).data.token;
          ConstantsService.organization = JSON.parse(JSON.stringify(data)).data.user.organization;
          ConstantsService.user = JSON.parse(JSON.stringify(data)).data.user;
          ConstantsService.role = JSON.parse(JSON.stringify(data)).data.user.role;
          ConstantsService.extension = JSON.parse(JSON.stringify(data)).data.user.extension;
          ConstantsService.isAuthenticated = true;
          ConstantsService.decodedToken = JSON.stringify(jwt_decode<JwtPayload>(ConstantsService.token));
          ConstantsService.email = this.user.email;
          ConstantsService.password = this.user.password;
          ConstantsService.fetchTime = this.datepipe.transform((new Date), 'MM/dd/yyyy h:mm:ss');

         // console.log('Setting Up Local Storage');
          localStorage.setItem("token",JSON.parse(JSON.stringify(data)).data.token);
          localStorage.setItem("organization",ConstantsService.organization);
          localStorage.setItem("role",ConstantsService.role);
          localStorage.setItem("email",ConstantsService.email);
          localStorage.setItem("extension",ConstantsService.extension);
          localStorage.setItem("fetchTime",ConstantsService.fetchTime);

          console.log('Remember Me : ' + this.user.rememberMe);

          if(this.user.rememberMe)
          {
            console.log('Setting zunu');
            localStorage.setItem("zunu",this.EncrDecr.encrypt('123456$#@$^@1ERF','123456$#@$^@1ERF', this.user.password));
          }

         
          setTimeout(() => {
         //   console.log('Routing to dashboard page');
            return this.router.navigateByUrl(this.constantService.DASHBOARD_ENDPOINT);
          }, this.redirectDelay);

         // this.cd.detectChanges();
          
        },
        error: err => {
          this.errors = [err.error.message];
          //console.log("Error : "+ JSON.stringify(err));
        }
    });

    /*
    this.errors = [];
    this.messages = [];
    this.submitted = true;

    this.service.authenticate(this.strategy, this.user).subscribe((result: NbAuthResult) => {
      this.submitted = false;

      if (result.isSuccess()) {
        this.messages = result.getMessages();
      } else {
        this.errors = result.getErrors();
      }

      const redirect = result.getRedirect();
      if (redirect) {
        setTimeout(() => {
          return this.router.navigateByUrl(redirect);
        }, this.redirectDelay);
      }
      this.cd.detectChanges();
    });

    */
  }

  googleCallBack(resp:any)
  {
      // console.log("googleCallBack");
      // console.log(resp);
      //clientId: '525538250273-3u3j0edt81k5lqmv31349fk043gc1asn.apps.googleusercontent.com', client_id: '525538250273-3u3j0edt81k5lqmv31349fk043gc1asn.apps.googleusercontent.com', credential: 'eyJhbGciOiJSUzI1NiIsImtpZCI6IjZjZTExYWVjZjllYjE0MD…nnICveQj0hHCPqPpR3xtSS8UKOqxtMTGpoeXUjfQX6fcHUihw', select_by: 'btn_confirm'

      // localStorage.setItem("goClient",resp.clientId);
      localStorage.setItem("goToken",resp.credential);

      this.ngZone.run(() => {
                  this.loginService.googleLogin(resp.credential)
                  .pipe(takeUntil(this.destroy$))
                  .subscribe({
                    next: data => {
            
                      // console.log("SuccessfulLogin");
            
                      //https://florimond.dev/en/posts/2018/09/consuming-apis-in-angular-the-model-adapter-pattern/
                      //str = JSON.stringify(obj);
                      //obj = JSON.parse(str);
            
                      this.messages = ['Google Login Successful'];
                      
                      ConstantsService.token = JSON.parse(JSON.stringify(data)).data.token;
                      ConstantsService.organization = JSON.parse(JSON.stringify(data)).data.user.organization;
                      ConstantsService.user = JSON.parse(JSON.stringify(data)).data.user;
                      ConstantsService.role = JSON.parse(JSON.stringify(data)).data.user.role;
                      ConstantsService.extension = JSON.parse(JSON.stringify(data)).data.user.extension;
                      ConstantsService.isAuthenticated = true;
                      ConstantsService.decodedToken = JSON.stringify(jwt_decode<JwtPayload>(ConstantsService.token));
                      ConstantsService.email = JSON.parse(JSON.stringify(data)).data.user.email;
                      // ConstantsService.password = this.user.password;
                      ConstantsService.fetchTime = this.datepipe.transform((new Date), 'MM/dd/yyyy h:mm:ss');
            
                    // console.log('Setting Up Local Storage');
                      localStorage.setItem("token",JSON.parse(JSON.stringify(data)).data.token);
                      localStorage.setItem("organization",ConstantsService.organization);
                      localStorage.setItem("role",ConstantsService.role);
                      localStorage.setItem("email",ConstantsService.email);
                      localStorage.setItem("extension",ConstantsService.extension);
                      localStorage.setItem("fetchTime",ConstantsService.fetchTime);
            
                      // console.log('Remember Me : ' + this.user.rememberMe);
            
                    
                      setTimeout(() => {
                        //   console.log('Routing to dashboard page');
                          return this.router.navigateByUrl(this.constantService.DASHBOARD_ENDPOINT);
                        }, this.redirectDelay);
            
                    // this.cd.detectChanges();
                      
                    },
                    error: err => {
                      this.errors = [err.error.message];
                      console.log("Error : "+ JSON.stringify(err));
                    }
                });
      });
  }

  googleButtonListner(resp:any)
  {
    // console.log("googleButtonListner");
    // console.log(resp);
  }

  getConfigValue(key: string): any {
    return getDeepFromObject(this.options, key, null);
  }

  onRegisterButtonClick(){
    console.log("onRegisterButtonClick");
    this.loginService.register = true;
  }
  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  togglePasswordVisibility() {
        console.log("togglePasswordVisibility");
        if (this.passwordInputType === 'password') {
          this.passwordInputType = 'text';
          this.passwordVisibilityIcon = 'eye-outline';
        } else {
          this.passwordInputType = 'password';
          this.passwordVisibilityIcon = 'eye-off-outline';
        }
      }

  getScreenSize() {
    this.screenWidth = window.innerWidth;
    this.screenHeight = window.innerHeight;

    console.log("this.screenHeight : "+this.screenHeight);

    if(this.screenHeight>=850){
      this.paddingTop = 20;
    }
    else if(this.screenHeight>700 && this.screenHeight<850){
      this.paddingTop = 12;
    }
    else{
      this.paddingTop = 0;
    }
  }

}