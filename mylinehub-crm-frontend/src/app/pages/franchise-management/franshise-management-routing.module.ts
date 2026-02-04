import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FranchiseCallListingComponent } from './franchise-call-listing/franchise-call-listing.component';
import { NotFoundComponent } from '../miscellaneous/not-found/not-found.component';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'franchise-call-listing',
        component: FranchiseCallListingComponent,
      },
      {
        path: '**',
        component: NotFoundComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class FranchiseManagementRoutingModule {}
