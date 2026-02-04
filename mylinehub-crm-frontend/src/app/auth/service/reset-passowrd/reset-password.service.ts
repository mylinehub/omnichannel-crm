import { Injectable } from '@angular/core';
import { ConstantsService } from './../../../service/constants/constants.service';
import { ApiHttpService } from './../../../service/http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class ResetPasswordService {

  constructor(protected constService : ConstantsService,
    protected httpService : ApiHttpService) { }


/*

const headers = { 'Authorization': 'Bearer my-token', 'My-Custom-Header': 'foobar' };
const body = { title: 'Angular POST Request Example' };
this.http.post<any>('https://reqres.in/api/posts', body, { headers }).subscribe(data => {
this.postId = data.id;
    
*/

      public reset_password(data: any,options?: any) { 
    
      return this.httpService.post(this.constService.API_BASE_ENDPOINT + this.constService.API_SELF_RESET_PASSWORD_ENDPOINT , data, options); 
    }  

}