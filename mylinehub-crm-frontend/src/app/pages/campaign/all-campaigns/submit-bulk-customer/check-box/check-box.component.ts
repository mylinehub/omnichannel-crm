import { Component, Input, OnInit } from '@angular/core';
import { ViewCell } from 'ng2-smart-table';

@Component({
  selector: 'ngx-check-box',
  template: `
    <input 
      type="checkbox"
      (click)="changeBoolean()"
      [checked]="this.checked">
      
  `
})
export class CheckBoxComponent implements ViewCell, OnInit {
 
  @Input() value: any;
  @Input() rowData: any;

  checked: boolean;

  constructor() { }

  ngOnInit() {
    this.checked = this.value;
  }

  changeBoolean() {
    this.checked = !this.checked;
  }

}