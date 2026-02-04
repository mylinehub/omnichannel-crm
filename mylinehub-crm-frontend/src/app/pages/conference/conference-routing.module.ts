import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { AllConferencesComponent } from './all-conferences/all-conferences.component';
import { ConferenceComponent } from './conference.component';

const routes: Routes = [{
  path: '',
  component: ConferenceComponent,
  children: [
    {
      path: 'all-conferences',
      component: AllConferencesComponent,
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
export class ConferenceRoutingModule {
}