import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { DetailsComponent } from './details/details.component';
import { WhatsappNumberComponent } from './whatsapp-number.component';

const routes: Routes = [{
  path: '',
  component: WhatsappNumberComponent,
  children: [
    {
      path: 'details',
      component: DetailsComponent,
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
export class WhatsAppNumberRoutingModule {
}