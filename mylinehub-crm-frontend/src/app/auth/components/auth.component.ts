/**
 * @license
 * Copyright Akveo. All Rights Reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
import { Component,HostListener, OnDestroy, OnInit } from '@angular/core';
import { DatePipe, Location } from '@angular/common';

import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { NbAuthService } from '@nebular/auth';
import { LoginService } from '../service/login/login.service';
import { RegistrationService } from '../service/registration/registration.service';
import { EncrDecrService } from '../../service/encr-decr/encr-decr.service';
import { ConstantsService } from '../../service/constants/constants.service';
import { Router } from '@angular/router';
import jwt_decode, { JwtPayload }  from 'jwt-decode'
import { JwtHelperService } from '@auth0/angular-jwt'
import { NbThemeService } from '@nebular/theme';

@Component({
  selector: 'nb-auth',
  styleUrls: ['./auth.component.scss'],
  templateUrl: './auth.component.html',
})
export class NgxAuthComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  subscription: any;

  authenticated: boolean = false;
  token: string = '';
  showMessages: any = {};
  registrationStarted = false;
  registrationProcessErrorBoolean = false;
  registrationProcessErrorString:string = 'Error While Doing Business Verification. Connect With Our Team Via WhatsApp : +919625048379';
  registrationError = 'Type Your Indian Identification Number.';
  setRegiaterOrgId: any;
  dot = 'one';
  setProgressingDotId: NodeJS.Timeout;

  screenWidth: number;
  screenHeight: number;
  orientation = 'horizontal';
  backButtonSize="medium";

  notIndianWhileRegistration = false;
  loginNow:boolean = false;
  user:any = null;
  // showcase of how to use the onAuthenticationChange method
  showBackButton:boolean = false;
  
  constructor(  protected router: Router,
                protected loginService : LoginService,
                protected constantService : ConstantsService,
                protected jwtHelperService:JwtHelperService,
                protected datepipe: DatePipe,
                protected auth: NbAuthService,
                protected location: Location,
                protected registrationService:RegistrationService,
                protected encrDecrService:EncrDecrService,
                private themeService: NbThemeService) {

    this.themeService.changeTheme('default');

    this.subscription = auth.onAuthenticationChange()
      .pipe(takeUntil(this.destroy$))
      .subscribe((authenticated: boolean) => {
        this.authenticated = authenticated;
      });

       if(this.location.path().includes('login'))
      {
         this.loginService.register = false;
         if(this.setRegiaterOrgId != null)
          {
            clearTimeout(this.setRegiaterOrgId);
            this.setRegiaterOrgId = null;
          }  
          else{
            this.setRegiaterOrgId = setTimeout(()=>{ 
                                                    this.loginService.register = true;
                                                },2000);
          }
      }
      else{
            this.loginService.register = false;
      }
      
  }
  ngOnInit(): void {
    
    console.log(this.router.url);
    if(this.router.url.includes("reset-password")){
      this.showBackButton = true;
    }
    else{
      this.showBackButton = false;
    }
                            
    const body = document.getElementsByTagName('body')[0];
    body.style.overflow = "scroll";

    console.log("this.showBackButton : "+this.showBackButton);
    this.getScreenSize();

    //False Registration page simulation statements
    // this.registrationStarted = true;
    //   this.loginService.register = false;

  }

  back() {
    this.location.back();
    return false;
  }

  cancel() {
    this.loginService.register = false;
    this.registrationError = 'Type Your Indian Identification Number.';
  }

  registerNow(businessIdentification:any){
    console.log("registerNow : ",businessIdentification);

    if(businessIdentification == '' || businessIdentification == null || businessIdentification == undefined)
    {
        this.registrationError = "Indian Business Identity Is Mandatory**";
        document.getElementById("registrationHint").style.color = '#8B0000';
    }
    else if(!this.isValidGSTNo(businessIdentification)){
        this.registrationError = "**Invalid Indian Business Identity (GSTIN)";
        document.getElementById("registrationHint").style.color = '#8B0000';
    }
    else{
      //proceed with business verification check
      this.registrationStarted = true;
      this.loginService.register = false;
      const today = new Date();
      const token = this.encrDecrService.encrypt(ConstantsService.SECRET_KEY,ConstantsService.INIT_VECTOR,today.getTime()+ConstantsService.DELIMITER+ConstantsService.VERIFYTOKEN);
      
      console.log("businessId : ",businessIdentification);
      console.log("token : ",token);
      
      let registrationInput = {
                    "businessId": businessIdentification,
                    "menuDto": {                   
                      "whatsAppModule": {
                        "whatsappdeliveryreport": [
                          "whatsappdeliveryreportdetails"
                        ],
                        "whatsappnumber": [
                          "whatsappnumberdetails"
                        ],
                        "whatsappchat": [
                          "whatsappchatdetails"
                        ],
                        "mediastorage": [
                          "mediastoragedetails"
                        ]
                      },
                      "campaignModule":{
                        "campaign": [
                          "campaigndetails"
                        ],
                      },
                      "callingModule": {
                        "calldetail": [
                          "calldetaildasboard","calldetailall"
                        ]
                      },
                      "organizationModule": {
                        "employee": [
                          "employeeprofile","employeecallhistory","employeedetails","employeemonitor"
                        ],
                        "absenteeism": [
                          "myabsenteeism","absenteeism"
                        ],
                        "department": [
                          "departmentdetails"
                        ],
                        "filestore": [
                          "filestoredetails"
                        ],
                        "customer": [
                          "customerautodialpreview","customerdetails"
                        ],
                        "product": [
                          "productdetails"
                        ],
                        "purchase": [
                          "purchasedetails"
                        ],
                        "supplier": [
                          "supplierdetails"
                        ]
                      },
                      "settingModule": {
                        "auth": [
                          "authresetpassword"
                        ]
                      },
                      // "issueTrackingModule": {
                      //   "error": [
                      //     "errordetails"
                      //   ],
                      //   "log": [
                      //     "logdetails"
                      //   ]
                      // },
                    },
                    "token": token
                  };

      this.registrationService.registerUser(registrationInput)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: employee => {
                console.log("Registration Response Data : ", employee);
                this.user = JSON.parse(JSON.stringify(employee));

                //Login Now
                      this.loginService.login({ username: this.user.email, password:"mylinehub@123" },'')
                      .pipe(takeUntil(this.destroy$))
                      .subscribe({
                        next: data => {
                
                          //console.log("SuccessfulLogin");
                
                          //https://florimond.dev/en/posts/2018/09/consuming-apis-in-angular-the-model-adapter-pattern/
                          //str = JSON.stringify(obj);
                          //obj = JSON.parse(str);
                          
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
                
                          //console.log('Remember Me : ' + this.user.rememberMe);
                
                          if(this.user.rememberMe)
                          {
                            //console.log('Setting zunu');
                            localStorage.setItem("zunu",this.encrDecrService.encrypt('123456$#@$^@1ERF','123456$#@$^@1ERF', this.user.organization));
                          }
                
                         
                          setTimeout(() => {
                         //   console.log('Routing to dashboard page');
                            return this.router.navigateByUrl(this.constantService.DASHBOARD_ENDPOINT);
                          }, 1000);
                
                         // this.cd.detectChanges();
                          
                        },
                        error: err => {
                          console.log("Error : "+ JSON.stringify(err));
                          this.registrationProcessErrorString = err.message;
                          this.registrationProcessErrorBoolean = true;
                        }
                    });
              },
              error: err => {
                console.log("Error : ",err);
                this.registrationProcessErrorString = err.error;

                if((err.error == null) || (!String(this.registrationProcessErrorString).includes("9625048"))){
                    this.registrationProcessErrorString = 'GST Server Seems Down. For Manual Registration, WhatsApp Support : +919625048379';
  
                }

                this.registrationProcessErrorBoolean = true;
              }
          });

      //Hit registration API
      setTimeout(()=>{
              let element = document.getElementById("stepOneNextButton");
              element.click();
              setTimeout(()=>{
                    let element = document.getElementById("stepTwoNextButton");
                    element.click();
                    setTimeout(()=>{
                            let element = document.getElementById("stepThreeNextButton");
                              element.click();
                              setTimeout(()=>{
                                    let element = document.getElementById("stepFourNextButton");
                                      element.click();
                            },3000);
                     },3000);
              },3000);
      },3000);
      
    }
  }


  startProgressingDot(){
    this.setProgressingDotId = setTimeout(()=>{ 
                                                if(this.dot == 'one'){
                                                    this.dot = 'two';
                                                }
                                                else if(this.dot == 'two'){
                                                    this.dot = 'Three';
                                                }
                                                else{
                                                    this.dot = 'one';
                                                }

                                                this.startProgressingDot();
                                             },1000);
  }

  @HostListener('window:resize', ['$event'])
  onResize(event?: any) {
    // console.log("screensize canged")
    this.getScreenSize(); // Update screen size on window resize
  }

  getScreenSize() {
    console.log("getScreenSize");

    this.screenWidth = window.innerWidth;
    this.screenHeight = window.innerHeight;

    if(this.screenWidth<650){

      console.log("on mobile");

      this.orientation = 'vertical';
      this.backButtonSize="tiny";

      try{
          setTimeout(()=>{ 
             console.log("fetching back button");
            const backButton = document.getElementById('backButton') as HTMLElement;
              
              console.log(backButton);

              if (backButton) {
                console.log("changing back botton top")
                backButton.style.top = '3.5%';
              }
          },50);
      }
      catch(e){
        console.log(e);
      }

    }
    else{
      this.orientation = 'horizontal';
      this.backButtonSize="small";
    }
  }

  goToNotIndian(){
    this.notIndianWhileRegistration = true;
  }

  backToRegistration(){
    this.notIndianWhileRegistration = false;
  }

  // GST (Goods and Services Tax) number
  // using Regular Expression
  // Function to validate the
  // GST Number  
  isValidGSTNo(str:any) {
      // Regex to check valid
      // GST CODE
      let regex = new RegExp(/^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$/);

      // GST CODE
      // is empty return false
      if (str == null) {
          return false;
      }

      // Return true if the GST_CODE
      // matched the ReGex
      if (regex.test(str) == true) {
          return true;
      }
      else {
          return false;
      }
  }

  goToDashboard(){
    this.router.navigateByUrl(this.constantService.DASHBOARD_ENDPOINT);
  }

  ngOnDestroy(): void {
    const body = document.getElementsByTagName('body')[0];
    body.style.overflow = "hidden";
    this.destroy$.next();
    this.destroy$.complete();
  }

}