import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { AllQueuesComponent } from './all-queues/all-queues.component';
import { QueueComponent } from './queue.component';

const routes: Routes = [{
  path: '',
  component: QueueComponent,
  children: [
    {
      path: 'all-queues',
      component: AllQueuesComponent,
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
export class QueueRoutingModule {
}