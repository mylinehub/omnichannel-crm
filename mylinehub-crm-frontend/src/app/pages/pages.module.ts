import { NgModule } from '@angular/core';
import { NbCardModule, NbFormFieldModule, NbMenuModule,NbTabsetModule,NbProgressBarModule, NbSpinnerModule, NbListPageTrackerDirective, NbListModule } from '@nebular/theme';
import { ThemeModule } from '../@theme/theme.module';
import { PagesComponent } from './pages.component';
import { PagesRoutingModule } from './pages-routing.module';
import { PropertyManagementModule } from './property-management/property-management.module';
import { FranshiseManagementModule } from './franchise-management/franshise-management.module';

@NgModule({
  imports: [
    
    PagesRoutingModule,
    ThemeModule,
    NbMenuModule,
    NbTabsetModule,
    NbCardModule,
    NbSpinnerModule,
    NbListModule,
    NbFormFieldModule,
    NbProgressBarModule,
    PropertyManagementModule,
    FranshiseManagementModule
  ],
  declarations: [
    PagesComponent
  ]
})
export class PagesModule {
}
