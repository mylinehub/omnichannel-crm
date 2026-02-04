import { Injectable } from '@angular/core';
import { NbMenuItem } from '@nebular/theme';

@Injectable({
  providedIn: 'root'
})
export class MenuDataService {

  menu:NbMenuItem[]=[];
  
  constructor() { }
}
