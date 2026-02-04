import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { AllLogsComponent } from './all-logs/all-logs.component';
import { LogsComponent } from './logs.component';

const routes: Routes = [{
  path: '',
  component: LogsComponent,
  children: [
    {
      path: 'all-logs',
      component: AllLogsComponent,
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
export class LogsRoutingModule {
}