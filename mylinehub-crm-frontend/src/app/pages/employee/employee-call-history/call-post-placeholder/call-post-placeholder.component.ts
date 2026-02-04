import { Component, HostBinding } from '@angular/core';

@Component({
  selector: 'ngx-call-post-placeholder',
  template: `
    <div class="title-placeholder"></div>
    <div class="text-placeholder"></div>
    <div class="link-placeholder"></div>
  `,
  styleUrls: ['./call-post-placeholder.component.scss']
})
export class CallPostPlaceholderComponent {
  @HostBinding('attr.aria-label')
  label = 'Loading';
}
