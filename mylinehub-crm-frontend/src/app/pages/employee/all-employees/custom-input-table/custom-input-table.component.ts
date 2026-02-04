import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { DefaultEditor, ViewCell } from 'ng2-smart-table';

@Component({
  selector: 'ngx-custom-input-table',
  template: `
    <input class="tableInputForDialog" nbInput readonly fullWidth
      [(ngModel)]="value"
      class="form-control"
      [name]="value"
      style="padding:0px; font-size:88%;"
    />
  `,
})
export class CustomInputTableComponent implements OnInit{

  @Input() value; // data from table
  @Input() rowData; // data from table
  
  constructor() {
  }

  ngOnInit(): void {
    console.log('value : '+this.value);
    console.log('CustomInputTableComponent');
  }

  inputClicked(){
    console.log('inputClicked');
  }
}

