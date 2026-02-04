import { Component, OnInit, HostBinding } from '@angular/core';

@Component({
  selector: 'ngx-search-post-placeholder',
  templateUrl: './search-post-placeholder.component.html',
  styleUrls: ['./search-post-placeholder.component.scss']
})
export class SearchPostPlaceholderComponent implements OnInit {

  @HostBinding('attr.aria-label')
  label = 'Loading';

  constructor() { }

  ngOnInit(): void {
  }

}