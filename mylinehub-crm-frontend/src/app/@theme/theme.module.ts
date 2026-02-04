import { ModuleWithProviders, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {ScrollingModule} from '@angular/cdk/scrolling';
import {CdkStepperModule} from '@angular/cdk/stepper';
import {CdkTableModule} from '@angular/cdk/table';
import {CdkTreeModule} from '@angular/cdk/tree';
import {
  NbPopoverModule,
  NbActionsModule,
  NbLayoutModule,
  NbMenuModule,
  NbSearchModule,
  NbSidebarModule,
  NbUserModule,
  NbContextMenuModule,
  NbButtonModule,
  NbSelectModule,
  NbIconModule,
  NbCardModule,
  NbThemeModule,
  NbListModule,
  NbTabsetModule,
  NbTagModule,
  NbTooltipModule,
  NbToggleModule,
  NbButtonGroupModule,
  NbDialogModule,
  NbAutocompleteModule,
  NbAlertModule,
  NbBadgeModule
} from '@nebular/theme';
import { NbEvaIconsModule } from '@nebular/eva-icons';
import { NbSecurityModule } from '@nebular/security';
import { FormsModule } from '@angular/forms';

import {
  FooterComponent,
  HeaderComponent,
  SearchInputComponent,
  TinyMCEComponent,
} from './components';
import {
  CapitalizePipe,
  PluralPipe,
  RoundPipe,
  TimingPipe,
  NumberWithCommasPipe,
} from './pipes';
import {
  OneColumnLayoutComponent,
  ThreeColumnsLayoutComponent,
  TwoColumnsLayoutComponent,
} from './layouts';
import { DEFAULT_THEME } from './styles/theme.default';
import { COSMIC_THEME } from './styles/theme.cosmic';
import { CORPORATE_THEME } from './styles/theme.corporate';
import { DARK_THEME } from './styles/theme.dark';
import { BrowserPhoneComponent } from './components/header/browser-phone/browser-phone.component';
import { BrowserPhoneActionComponent } from './components/header/browser-phone-action/browser-phone-action.component';
import { VideoDialogComponent } from './components/header/video-dialog/video-dialog.component';
import { ReceiveDialControlComponent } from './components/header/receive-dial-control/receive-dial-control.component';
import { NotificationListComponent } from './components/header/notification-list/notification-list.component';
import { MessageListComponent } from './components/header/message-list/message-list.component';
import { ReplacePipe } from './pipes/replace.pipe';
import { AskDeleteComponent } from './components/header/message-list/ask-delete/ask-delete.component';
import { AskSendFileComponent } from './components/header/message-list/ask-send-file/ask-send-file.component';
import { AddCallComponent } from './components/header/video-dialog/add-call/add-call.component';
import { ZeroColumnComponent } from './layouts/zero-column/zero-column.component';
import { HideStringFromEmployeePipe } from './pipes/hide-string-from-employee.pipe';
import { ScaleLayoutModule } from '../directives/scale-layout.module';
// import { SpellCheckerModule } from 'ngx-spellchecker';

const NB_MODULES = [
  // SpellCheckerModule,
  ScaleLayoutModule,
  NbDialogModule,
  DragDropModule,
  ScrollingModule,
  CdkStepperModule,
  CdkTableModule,
  CdkTreeModule,
  NbPopoverModule,
  NbButtonGroupModule,
  NbCardModule,
  NbListModule,
  NbLayoutModule,
  NbMenuModule,
  NbUserModule,
  NbActionsModule,
  NbSearchModule,
  NbSidebarModule,
  NbContextMenuModule,
  NbSecurityModule,
  NbButtonModule,
  NbSelectModule,
  NbIconModule,
  NbEvaIconsModule,
  NbTabsetModule,
  NbTagModule,
  NbTooltipModule,
  NbToggleModule,
  NbAutocompleteModule,
  NbAlertModule,
  NbBadgeModule
];
const COMPONENTS = [
  HeaderComponent,
  FooterComponent,
  SearchInputComponent,
  TinyMCEComponent,
  OneColumnLayoutComponent,
  ThreeColumnsLayoutComponent,
  TwoColumnsLayoutComponent,
  ZeroColumnComponent
];
const PIPES = [
  HideStringFromEmployeePipe,
  CapitalizePipe,
  PluralPipe,
  RoundPipe,
  TimingPipe,
  NumberWithCommasPipe,
  ReplacePipe
];

@NgModule({
  imports: [FormsModule,CommonModule, ...NB_MODULES],
  exports: [CommonModule, ...PIPES, ...COMPONENTS],
  declarations: [...COMPONENTS, ...PIPES, BrowserPhoneComponent, BrowserPhoneActionComponent, VideoDialogComponent, ReceiveDialControlComponent, NotificationListComponent, MessageListComponent, AskDeleteComponent, AskSendFileComponent, AddCallComponent, ZeroColumnComponent],
})
export class ThemeModule {
  static forRoot(): ModuleWithProviders<ThemeModule> {
    return {
      ngModule: ThemeModule,
      providers: [
        ...NbThemeModule.forRoot(
          {
            name: 'default',
          },
          [ DEFAULT_THEME, COSMIC_THEME, CORPORATE_THEME, DARK_THEME ],
        ).providers,
      ],
    };
  }
}
