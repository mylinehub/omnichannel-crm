import { Component, Input } from '@angular/core';

import { SearchPost } from '../../service/search.service';

@Component({
  selector: 'ngx-search-post',
  templateUrl: './search-post.component.html',
})
export class SearchPostComponent {

  @Input() post: SearchPost;
}

