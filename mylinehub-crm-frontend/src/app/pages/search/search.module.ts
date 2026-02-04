import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchComponent } from './search.component';
import { RouterModule } from '@angular/router';
import { SearchRoutingModule} from './search-routing.module';
import { SearchResultComponent } from './search-result/search-result.component';
import { SearchService } from './service/search.service';

import {
  NbAccordionModule,
  NbButtonModule,
  NbCardModule,
  NbListModule,
  NbRouteTabsetModule,
  NbStepperModule,
  NbTabsetModule, NbUserModule,
} from '@nebular/theme';
import { SearchPostComponent } from './search-result/search-post/search-post.component';
import { SearchPostPlaceholderComponent } from './search-result/search-post-placeholder/search-post-placeholder.component';

@NgModule({
  declarations: [
    SearchComponent,
    SearchResultComponent,
    SearchPostComponent,
    SearchPostPlaceholderComponent
  ],
  imports: [
    CommonModule , SearchRoutingModule , RouterModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule
  ],
  providers:[SearchService]
})
export class SearchModule { }
