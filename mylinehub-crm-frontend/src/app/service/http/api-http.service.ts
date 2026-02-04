// Angular Modules 
import { Injectable, OnDestroy } from '@angular/core'; 
import { Router } from '@angular/router';
import { HttpClient, HttpEvent, HttpHeaders } from '@angular/common/http'; 
import { ConstantsService } from './../../service/constants/constants.service';
import { takeUntil } from 'rxjs/operators';
import { JwtHelperService } from '@auth0/angular-jwt'
import { DatePipe } from '@angular/common';
import {ObservedValueOf,of, from, Subject, Observable } from 'rxjs';
import { EncrDecrService } from '../encr-decr/encr-decr.service';
import { BrowserPhoneService } from '../browser-phone/browser-phone.service';
import { StompService } from '../stomp/stomp.service';

@Injectable() 
export class ApiHttpService implements OnDestroy { 
  private destroy$: Subject<void> = new Subject<void>();
  
  constructor( 
// Angular Modules 
private http: HttpClient,
protected constantService:ConstantsService, 
protected jwtHelperService:JwtHelperService,
protected datepipe: DatePipe,
private router: Router,
private EncrDecr: EncrDecrService,
protected constService : ConstantsService,
) { } 


public get(url: string, options?: any) { 
return this.http.get(url, options); 
} 

public post(url: string, data: any, options?: any) { 
return this.http.post(url, data, options); 
} 

public put(url: string, data: any, options?: any) { 
return this.http.put(url, data, options); 
} 

public delete(url: string, options?: any) { 
return this.http.delete(url, options); 
}


public getWithTokenHandleded(url: string) { 

  var isExpired : boolean  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));

   if(isExpired && !(url.includes('organization-app')))
   {
    console.log("Token was expired");
    this.refreshToken(url,"","getWithTokenHandleded");
    setTimeout(()=>{
      console.log("Setting up time out to execute API after 3 seconds so as new token could be saved in local db");
      return this.executeGetWithTokenHandleded(url);
     },3000);
   }
   else
   {
      return this.executeGetWithTokenHandleded(url);
   }
  
  } 
  public executeGetWithTokenHandleded(url: string)
  {     
    const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem("token")}`
      });

    const requestOptions = { headers: headers };

    return this.http.get(url, requestOptions); 
  }
  


  public postWithTokenHandleded(url: string, data: any) { 

   var isExpired : boolean  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));
   //console.log(isExpired);

   if(isExpired && !(url.includes('organization-app')))
   {
    console.log("Token was expired");
    this.refreshToken(url,data,"postWithTokenHandleded");
    setTimeout(()=>{
      console.log("Setting up time out to execute API after 3 seconds so as new token could be saved in local db");
      return this.executePostWithTokenHandleded(url,data);
    },3000);
   }
   else
   {
      return this.executePostWithTokenHandleded(url,data);
   }
  
  } 
  public executePostWithTokenHandleded(url: string, data: any)
  { 
      const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem("token")}`
      });

      const requestOptions = { headers: headers };

      return this.http.post(url, data, requestOptions); 
  }



public getWithTokenHandlededAndEvent(url: string): Observable<HttpEvent<Blob>> { 

  var isExpired : boolean  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));

   if(isExpired && !(url.includes('organization-app')))
   {
    console.log("Token was expired");
    this.refreshToken(url,"","getWithTokenHandlededAndEvent");
    setTimeout(()=>{
      console.log("Setting up time out to execute API after 3 seconds so as new token could be saved in local db");
      return this.executeGetWithTokenHandlededAndEvent(url);
      },3000);
   }
   else
   {
      return this.executeGetWithTokenHandlededAndEvent(url);
   }
  
  } 
  public executeGetWithTokenHandlededAndEvent(url: string)
  {   
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${localStorage.getItem("token")}`
    });

    return this.http.get(url, {  reportProgress: true,
                                observe: 'events',
                                responseType: 'blob',
                                headers: headers }); 
  }


  

  public postWithTokenHandlededAndEvent(url: string, data: FormData): Observable<HttpEvent<string[]>> { 

   var isExpired : boolean  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));
   //console.log(isExpired);

   if(isExpired && !(url.includes('organization-app')))
   {
    console.log("Token was expired");
    this.refreshToken(url,data,"postWithTokenHandlededAndEvent");
    setTimeout(()=>{
      console.log("Setting up time out to execute API after 3 seconds so as new token could be saved in local db");
      return this.executePostWithTokenHandlededAndEvent(url,data);
     },3000);
   }
   else
   {
      return this.executePostWithTokenHandlededAndEvent(url,data);
   }

  } 
  public executePostWithTokenHandlededAndEvent(url: string, data: any)
  { 
      const headers = new HttpHeaders({
        'Authorization': `Bearer ${localStorage.getItem("token")}`
      });

      //  const requestOptions = { headers: headers };

      return this.http.post<string[]>(url, data, {  reportProgress: true,
                                                  observe: 'events',
                                                  headers: headers }); 
  }



  public postWithTokenAndMultipartHandleded(url: string, data: FormData) { 

    var isExpired : boolean  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));

   if(isExpired && !(url.includes('organization-app')))
   {
    console.log("Token was expired");
    this.refreshToken(url,data,"postWithTokenAndMultipartHandleded");
    setTimeout(()=>{
      console.log("Setting up time out to execute API after 3 seconds so as new token could be saved in local db");
      return this.executePostWithTokenAndMultipartHandleded(url,data);
    },3000);
   }
   else
   {
        return this.executePostWithTokenAndMultipartHandleded(url,data);
   }
   
  } 
  public executePostWithTokenAndMultipartHandleded(url: string, data: any)
  { 
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${localStorage.getItem("token")}`
    });

    const requestOptions = { headers: headers };

    return this.http.post(url, data, requestOptions); 
  }



  public getWithTokenAndText(url: string) { 

    var isExpired : boolean  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));

   if(isExpired && !(url.includes('organization-app')))
   {
    console.log("Token was expired");
    this.refreshToken(url,"","getWithTokenAndText");
    setTimeout(()=>{
      console.log("Setting up time out to execute API after 3 seconds so as new token could be saved in local db");
      return this.executeGetWithTokenAndText(url);
    },3000);
   }
   else
   {
        return this.executeGetWithTokenAndText(url);
   }
  }
  public executeGetWithTokenAndText(url: string)
  {   
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${localStorage.getItem("token")}`
    });

    // const requestOptions = { headers: headers, responseType: 'text' };
    const requestOptions = { headers: headers, responseType: 'text' };

    return this.http.get(url, {headers: headers,responseType: 'text'}); 
  }



  public getWithTokenAndArrayBufferAsJson(url: string) { 

    var isExpired : boolean  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));

   if(isExpired && !(url.includes('organization-app')))
   {
    console.log("Token was expired");
    this.refreshToken(url,"","getWithTokenAndArrayBufferAsJson");
    setTimeout(()=>{
      console.log("Setting up time out to execute API after 3 seconds so as new token could be saved in local db");
      return this.executeGetWithTokenAndArrayBufferAsJson(url);
    },3000);
   }
   else
   {
     return this.executeGetWithTokenAndArrayBufferAsJson(url);
   }
  
  }
  public executeGetWithTokenAndArrayBufferAsJson(url: string)
  {   
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${localStorage.getItem("token")}`
    });

    const requestOptions = { headers: headers, responseType: 'arraybuffer' as 'json' };

    return this.http.get(url, requestOptions); 
  }



  public getWithTokenAndBlobAsJson(url: string) { 

    var isExpired : boolean  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));

   if(isExpired && !(url.includes('organization-app')))
   {
    console.log("Token was expired");
    this.refreshToken(url,"","getWithTokenAndBlobAsJson");

    setTimeout(()=>{
      console.log("Setting up time out to execute API after 3 seconds so as new token could be saved in local db");
      return this.executeGetWithTokenAndBlobAsJson(url);
    },3000);

   }
   else
   {
      return this.executeGetWithTokenAndBlobAsJson(url);
   }
  } 
  public executeGetWithTokenAndBlobAsJson(url: string)
  {   
       
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${localStorage.getItem("token")}`
    });

    const requestOptions = { headers: headers, responseType: 'blob' as 'json' };

    return this.http.get(url, requestOptions); 
  }



  public postWithTokenAndBlobAsJson(url: string, data: any) { 

    var isExpired : boolean  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));

   if(isExpired && !(url.includes('organization-app')))
   {
    console.log("Token was expired");
    this.refreshToken(url,data,"postWithTokenAndBlobAsJson");

    setTimeout(()=>{
      console.log("Setting up time out to execute API after 3 seconds so as new token could be saved in local db");
        return this.executePostWithTokenAndBlobAsJson(url,data);
    },3000);
   }
   else
   {
      return this.executePostWithTokenAndBlobAsJson(url,data);
   }
 
  } 
  public executePostWithTokenAndBlobAsJson(url: string, data: any)
  {    
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${localStorage.getItem("token")}`
    });

    const requestOptions = { headers: headers, responseType: 'blob' as 'json' };

    return this.http.post(url,data, requestOptions); 
  }



  public putWithTokenHandleded(url: string, data: any) { 
  
    var isExpired : boolean  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));

   if(isExpired && !(url.includes('organization-app')))
   {
    console.log("Token was expired");
    this.refreshToken(url,data,"putWithTokenHandleded");

    setTimeout(()=>{
      console.log("Setting up time out to execute API after 3 seconds so as new token could be saved in local db");
      return this.executePutWithTokenHandleded(url,data);
    },3000);

   }
   else
   {
      return this.executePutWithTokenHandleded(url,data);
   }
  
  }
  public executePutWithTokenHandleded(url: string, data: any)
  {  
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${localStorage.getItem("token")}`
    });

    const requestOptions = { headers: headers };
    return this.http.put(url, data, requestOptions); 
  }

  

  public deleteWithTokenHandleded(url: string) { 
  
    var isExpired : boolean  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));

    if(isExpired && !(url.includes('organization-app')))
    {
      console.log("Token was expired");
      this.refreshToken(url,"","deleteWithTokenHandleded");

      setTimeout(()=>{
        console.log("Setting up time out to execute API after 3 seconds so as new token could be saved in local db");
        return this.executeDeleteWithTokenHandleded(url);
      },3000);

    }
    else
   {
      return this.executeDeleteWithTokenHandleded(url);
   }

  } 
  public executeDeleteWithTokenHandleded(url: string)
  {   
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${localStorage.getItem("token")}`
    });

    const requestOptions = { headers: headers };
    return this.http.delete(url, requestOptions); 
  }



  public postWithTokenHandlededAndAudio(url: string, data: any) { 

    var isExpired : boolean  = this.jwtHelperService.isTokenExpired(localStorage.getItem('token'));

   if(isExpired && !(url.includes('organization-app')))
   {
    console.log("Token was expired");
    this.refreshToken(url,data,"postWithTokenHandlededAndAudio");

    setTimeout(()=>{
      console.log("Setting up time out to execute API after 3 seconds so as new token could be saved in local db");
        return this.executePostWithTokenHandlededAndAudio(url,data);
    },3000);

   }
   else
   {
       return this.executePostWithTokenHandlededAndAudio(url,data);
   }
  
  } 
  public executePostWithTokenHandlededAndAudio(url: string, data: any)
  {
       
      const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem("token")}`
        });

      const requestOptions = { headers: headers };

      return this.http.post(url, data, { ...requestOptions, responseType: 'blob' }); 
  }

  refreshToken(apiUrl:any ,apiData:any ,apiType:String)
  {
    console.log("Inside refresh token");
   if(localStorage.getItem("token")!= null)
   {

    let endpoint: string = this.constService.REFRESH_TOKEN_ENDPOINT;
    endpoint = endpoint.replace("{{oldToken}}",localStorage.getItem("token"));

     this.get(this.constService.API_BASE_ENDPOINT + endpoint)
     .pipe(takeUntil(this.destroy$))
     .subscribe({
       next: data => {
        console.log('Setting Up Local Storage');
        console.log('Received data : ', data);
        console.log('Type of data : ', typeof (data));
        console.log('String data : ', String(data));
        localStorage.setItem("token",String(data));
        console.log('After setting refresh token from local db : ', localStorage.getItem("token"));
        //  switch(type)
        //  {
        //         case 'getWithTokenHandleded': return this.executeGetWithTokenHandleded(url);break;
        //         case 'postWithTokenHandleded': return this.executePostWithTokenHandleded(url,data); break;
        //         case 'getWithTokenHandlededAndEvent': return this.executeGetWithTokenHandlededAndEvent(url); break;
        //         case 'postWithTokenHandlededAndEvent': return this.executePostWithTokenHandlededAndEvent(url,data); break;
        //         case 'postWithTokenAndMultipartHandleded': return this.executePostWithTokenAndMultipartHandleded(url,data);break;
        //         case 'getWithTokenAndText': return this.executeGetWithTokenAndText(url);break;
        //         case 'getWithTokenAndArrayBufferAsJson':   return this.executeGetWithTokenAndArrayBufferAsJson(url);break;
        //         case 'getWithTokenAndBlobAsJson':  return this.executeGetWithTokenAndBlobAsJson(url);  break;
        //         case 'postWithTokenAndBlobAsJson': return this.executePostWithTokenAndBlobAsJson(url,data);break;
        //         case 'putWithTokenHandleded': return this.executePutWithTokenHandleded(url,data); break;
        //         case 'deleteWithTokenHandleded': return this.executeDeleteWithTokenHandleded(url);break;
        //         case 'postWithTokenHandlededAndAudio': return this.executePostWithTokenHandlededAndAudio(url,data); break;
        //         default: return null; break;
        //  }
        

       },
       error: err => {
                   console.log("Error while refreshing token : "+ JSON.stringify(err));
                   console.log('Routing To Login Page');
                  
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

                 console.log('Setting Up Local Storage To Empty It');
                  localStorage.removeItem("token");
                  localStorage.removeItem("organization");
                  localStorage.removeItem("role");
                  localStorage.removeItem("extension");
                  localStorage.removeItem("fetchTime");
                  localStorage.removeItem("email");
        
                  console.log('Remove zunu if not null');
                  if(localStorage.getItem("zunu")!= null)
                    {
                      console.log('Zunu is not null');
                      localStorage.removeItem("zunu");
                    }

                  setTimeout(() => {
                    console.log('Navgating to login page');
                    return this.router.navigateByUrl(this.constantService.LOGIN_ENDPOINT);
                  }, 0);
          }
        });  
   }
   else{
    this.router.navigate([this.constantService.LOGIN_ENDPOINT]);
   }
 
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

}


