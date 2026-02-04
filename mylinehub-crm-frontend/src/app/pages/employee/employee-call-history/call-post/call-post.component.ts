import { Component, Input } from '@angular/core';
import { NewsPost } from '../../../../service/news-service/news-service.service';

@Component({
  selector: 'ngx-call-post',
  template: `
    <article>
      <h2 class="h5">{{post.title}}</h2>
      <p>{{post.text}}</p>
      <a [attr.href]="post.link">Read full article</a>
    </article>
  `,
})
export class CallPostComponent {
  @Input()
  post: NewsPost;
}
