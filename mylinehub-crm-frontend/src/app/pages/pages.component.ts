import { Component, OnInit } from '@angular/core';
import { NbMenuItem } from '@nebular/theme';
import { ConstantsService } from '../service/constants/constants.service';
import { MenuDataService } from '../service/menu-data/menu-data.service';
import { MENU_ITEMS } from './pages-menu';

@Component({
  selector: 'ngx-pages',
  styleUrls: ['pages.component.scss'],
  template: `
    <ngx-one-column-layout>
      <nb-menu [items]="menuDataService.menu"></nb-menu>
      <router-outlet></router-outlet>
    </ngx-one-column-layout>
  `,
})
export class PagesComponent implements OnInit {

  menu:NbMenuItem[]=[];

  constructor( protected menuDataService: MenuDataService)
  {
  }

  ngOnInit(){
    // console.log("I am in ngOnIt of pages");
    // console.log(JSON.stringify(MENU_ITEMS));
    // this.menu = MENU_ITEMS;
    // this.verifyUserDataBeforeLoadingAllPages();

  }

}


