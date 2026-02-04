import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'ngx-cell-scroll',
  templateUrl: './cell-scroll.component.html',
  styleUrls: ['./cell-scroll.component.scss']
})
export class CellScrollComponent implements OnInit {

  @Input() value: string;

  constructor() { }

  ngOnInit(): void {
  }

}
