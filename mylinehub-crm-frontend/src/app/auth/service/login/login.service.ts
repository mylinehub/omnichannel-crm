import { Injectable } from '@angular/core';
import { ConstantsService } from './../../../service/constants/constants.service';
import { ApiHttpService } from './../../../service/http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  public register = false;

  constructor(protected constService : ConstantsService,
              protected httpService : ApiHttpService) { }


  /*

  const headers = { 'Authorization': 'Bearer my-token', 'My-Custom-Header': 'foobar' };
  const body = { title: 'Angular POST Request Example' };
  this.http.post<any>('https://reqres.in/api/posts', body, { headers }).subscribe(data => {
  this.postId = data.id;
              
  */

  public login(data: any, options?: any) { 
                return this.httpService.post(this.constService.API_BASE_ENDPOINT + this.constService.API_LOGIN_ENDPOINT , data, options); 
  } 

  
  public getEmployeeData(organization: string, email: string,options?: any) { 

    var endpoint: string = this.constService.API_EMPLOYEE_DETAIL_ENDPOINT;

    endpoint = endpoint.replace("{{email}}",email);
    endpoint = endpoint.replace("{{organization}}",organization);

    return this.httpService.get(this.constService.API_BASE_ENDPOINT + endpoint, options); 
} 


public googleLogin(googleToken: string) { 
  var endpoint: string = this.constService.GOOGLE_LOGIN_ENDPOINT;
  endpoint = endpoint.replace("{{googleToken}}",googleToken);
  return this.httpService.get(this.constService.API_BASE_ENDPOINT + endpoint); 
}

}
