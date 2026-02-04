import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { ChatComponent } from './chat/chat.component';
import { WhatsappChatComponent } from './whatsapp-chat.component';

const routes: Routes = [{
  path: '',
  component: WhatsappChatComponent,
  children: [
    {
      path: 'chat',
      component: ChatComponent,
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
export class WhatsAppChatRoutingModule {
}