import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { DetailsComponent } from './details/details.component';
import { WhatsappProjectComponent } from './whatsapp-project.component';
import { AiAccountComponent } from './ai-account/ai-account.component';
const routes: Routes = [{
  path: '',
  component: WhatsappProjectComponent,
  children: [
    {
      path: 'details',
      component: DetailsComponent,
    },
    {
      path: 'ai-accounts',
      component: AiAccountComponent,
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
export class WhatsAppProjectRoutingModule {
}