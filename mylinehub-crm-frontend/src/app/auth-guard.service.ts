import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { NbAuthService } from '@nebular/auth';
import { tap } from 'rxjs/operators';
import { JwtHelperService } from '@auth0/angular-jwt'
import { DatePipe } from '@angular/common';
import {ObservedValueOf,of, from } from 'rxjs';
import { ConstantsService } from './service/constants/constants.service';

@Injectable()
export class AuthGuard implements CanActivate {

  constructor(private authService: NbAuthService, 
              private router: Router,
              protected jwtHelperService:JwtHelperService,
              protected datepipe: DatePipe,
              protected constantService:ConstantsService,
              ) {
  }

  
  canActivate() {
    // isValid = isTokenExpired(localStorage.getItem('token'));
    // console.log("App Module Auth Gaurd: canActivate : ");
    var isExpired : boolean = true;

    if(localStorage.getItem("token") != null)
      {
        isExpired  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));
      }

    // console.log("App Module Auth Gaurd: canActivate : Token : " + localStorage.getItem('token'));

    //console.log("App Module Auth Gaurd: canActivate : Expired: " + isExpired);

   return of(!isExpired)
      .pipe(
        tap(authenticated => {
          if (!authenticated) {
            this.router.navigate([this.constantService.LOGIN_ENDPOINT]);
          }
        }),
      );
    
    //return true;
  }
 
}