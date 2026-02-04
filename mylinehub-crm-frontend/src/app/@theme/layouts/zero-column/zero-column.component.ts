import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'ngx-zero-column',
  styleUrls: ['./zero-column.component.scss'],
  template: `
    <nb-layout windowMode>
        <ng-content select="router-outlet"></ng-content>
    </nb-layout>
  `,
})
export class ZeroColumnComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
