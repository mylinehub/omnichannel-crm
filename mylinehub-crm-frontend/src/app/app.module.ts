/**
 * @license
 * Copyright Akveo. All Rights Reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { NgxEchartsModule } from 'ngx-echarts';
import { ChartModule } from 'angular2-chartjs';

import { HttpClientModule } from '@angular/common/http';
import { CoreModule } from './@core/core.module';
import { ThemeModule } from './@theme/theme.module';
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {ScrollingModule} from '@angular/cdk/scrolling';
import {CdkStepperModule} from '@angular/cdk/stepper';
import {CdkTableModule} from '@angular/cdk/table';
import {CdkTreeModule} from '@angular/cdk/tree';

import {
  NbProgressBarModule,
  NbTabsetModule,
  NbTooltipModule,
  NbBadgeModule,
  NbPopoverModule,
  NbSearchModule,
  NbChatModule,
  NbDatepickerModule,
  NbDialogModule,
  NbMenuModule,
  NbSidebarModule,
  NbToastrModule,
  NbWindowModule,
  NbLayoutModule,
  NbCardModule,
  NbIconModule,
  NbListModule,
  NbButtonModule,
  NbRadioModule,
  NbTagModule,
  NbTimepickerModule,
  NbToggleModule,
  NbButtonGroupModule,
  NbAutocompleteModule,
  NbAlertModule,
  NbFormFieldModule,
  NbCheckboxModule
} from '@nebular/theme';
import { NbPasswordAuthStrategy, NbAuthModule } from '@nebular/auth';
import { ConstantsService } from './service/constants/constants.service';
import { ApiHttpService } from './service/http/api-http.service';
import { AuthGuard } from './auth-guard.service';
import { JwtTokenService } from './service/JWTToken/jwt-token.service';
import { EncrDecrService } from './service/encr-decr/encr-decr.service';
import { JwtHelperService, JWT_OPTIONS  } from '@auth0/angular-jwt';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { ExportAsModule } from 'ngx-export-as';
import { CronEditorModule } from 'cron-editor';
import { FormsModule } from '@angular/forms';
import { ScaleLayoutModule } from './directives/scale-layout.module';
import { HammertimeDirective } from './directives/hammertime.directive';
import * as Hammer from 'hammerjs';
import { HammerGestureConfig, HAMMER_GESTURE_CONFIG } from '@angular/platform-browser';

export class MyHammerConfig extends HammerGestureConfig  {
  overrides = <any>{
      // override hammerjs default configuration
      'swipe': { direction: Hammer.DIRECTION_ALL  }
  }
}

// import { SpellCheckerModule } from 'ngx-spellchecker';

const formSetting: any = {
  redirectDelay: 0,
  showMessages: {
    success: true,
  },
};


@NgModule({
  declarations: [AppComponent,HammertimeDirective],
  imports: [
    // SpellCheckerModule,
    ScaleLayoutModule,
    NgxEchartsModule,
    NgxChartsModule,
    ChartModule,
    FormsModule,
    NbProgressBarModule,
    NbCheckboxModule,
    NbFormFieldModule,
    NbAlertModule,
    DragDropModule,
    ScrollingModule,
    CdkStepperModule,
    CdkTableModule,
    CdkTreeModule,
    NbTabsetModule,
    CronEditorModule,
    NbTooltipModule,
    NbBadgeModule,
    NbToggleModule,
    NbTimepickerModule.forRoot(),
    NbTagModule,
    NbRadioModule,
    NgxChartsModule,
    ExportAsModule,
    NbPopoverModule,
    NbSearchModule,
    NbButtonModule,
    NbListModule,
    NbIconModule,
    NbCardModule,
    NbLayoutModule,
    NbButtonGroupModule,
    BrowserModule,
    BrowserAnimationsModule,
    NbAutocompleteModule,
    HttpClientModule,
    AppRoutingModule,
    NbSidebarModule.forRoot(),
    NbMenuModule.forRoot(),
    NbDatepickerModule.forRoot(),
    NbDialogModule.forRoot(),
    NbWindowModule.forRoot(),
    NbToastrModule.forRoot(),
    NbChatModule.forRoot({
      messageGoogleMapKey: 'AIzaSyA_wNuCzia92MAmdLRzmqitRGvCF7wCZPY',
    }),
    CoreModule.forRoot(),
    ThemeModule.forRoot(),
    NbAuthModule.forRoot({
      strategies: [
        NbPasswordAuthStrategy.setup({
          name: 'email',
         
          baseEndpoint: 'http://localhost:8080/',
           login: {
             // ...
             endpoint: 'login',
           },
          /* logout: {
            endpoint: 'logout',
          },*/
          resetPass: {
            endpoint: 'api/v1/employees/updateSelfWebPassword',
          },

        }),
      ],
      forms: {
        login: formSetting,
        resetPassword: formSetting,
        },
    }),

    
  ],
  providers: [ConstantsService,ApiHttpService,AuthGuard,{ provide: JWT_OPTIONS, useValue: JWT_OPTIONS },
    JwtHelperService, JwtTokenService,EncrDecrService],
  bootstrap: [AppComponent],
  exports:[]
})
export class AppModule {
}
