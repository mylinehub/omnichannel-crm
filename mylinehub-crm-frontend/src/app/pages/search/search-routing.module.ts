import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { SearchResultComponent } from './search-result/search-result.component';

const routes: Routes = [{
  path: '',
  component: SearchResultComponent,
  children: [
    {
      path: 'search-result',
      component: SearchResultComponent,
    },
    {
      path: '**',
      component: NotFoundComponent,
    },
  ],
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SearchRoutingModule {
}