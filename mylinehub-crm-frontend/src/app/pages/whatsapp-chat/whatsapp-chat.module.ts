import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WhatsappChatComponent } from './whatsapp-chat.component';
import { ChatComponent } from './chat/chat.component';
import { RouterModule } from '@angular/router';
import {
  NbAccordionModule,
  NbButtonModule,
  NbCardModule,
  NbListModule,
  NbRouteTabsetModule,
  NbStepperModule,
  NbTabsetModule, NbUserModule,NbTimepickerModule,NbDatepickerModule,NbToggleModule,
  NbIconModule, NbInputModule, NbTreeGridModule ,NbRadioModule,NbOptionModule,NbSelectModule,NbFormFieldModule,NbTagModule, NbDialogModule,
  NbAutocompleteModule,
  NbButtonGroupModule
} from '@nebular/theme';

import { NgxChartsModule } from '@swimlane/ngx-charts';
import { FormsModule } from '@angular/forms';
import { Ng2SmartTableModule } from 'ng2-smart-table';
import { WhatsAppChatRoutingModule } from './whatsapp-chat-routing.module';
import { AskDeleteComponent } from './chat/ask-delete/ask-delete.component';
import { AskSendFileComponent } from './chat/ask-send-file/ask-send-file.component';
import { ThemeModule } from '../../@theme/theme.module';
import { ChatShowcaseServiceService } from './chat/chat-showcase-service/chat-showcase-service.service';
import { MessageStatusPipe } from './pipes/message-status.pipe';
import { MessageStatusClassPipe } from './pipes/message-status-class.pipe';


@NgModule({
  declarations: [
    WhatsappChatComponent,
    ChatComponent,
    AskDeleteComponent,
    AskSendFileComponent,
    MessageStatusPipe,
    MessageStatusClassPipe
  ],
  imports: [
      ThemeModule,
      NbAutocompleteModule,
      NbButtonModule,
      NbListModule,
      NbUserModule,
      NbIconModule,
      NbDialogModule,
      NbInputModule,
      NbButtonGroupModule,
      NbAutocompleteModule,
      WhatsAppChatRoutingModule,
      CommonModule,
      RouterModule,
      NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule
    ],
    providers:[ChatShowcaseServiceService],
    exports:[]
})
export class WhatsappChatModule { }
