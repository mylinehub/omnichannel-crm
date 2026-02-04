import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InventoryListingComponent } from './inventory-listing/inventory-listing.component';
import { NotFoundComponent } from '../miscellaneous/not-found/not-found.component';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'inventory-listing',
        component: InventoryListingComponent,
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
export class PropertyManagementRoutingModule {}
