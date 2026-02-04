import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { WhatsappReportComponent } from './whatsapp-report.component';
import { ReportComponent } from './report/report.component';

const routes: Routes = [{
  path: '',
  component: WhatsappReportComponent,
  children: [
    {
      path: 'report',
      component: ReportComponent,
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
export class WhatsAppReportRoutingModule {
}