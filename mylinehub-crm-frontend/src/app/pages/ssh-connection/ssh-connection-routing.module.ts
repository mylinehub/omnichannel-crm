import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { RegistriesComponent } from './registries/registries.component';
import { SshConnectionComponent } from './ssh-connection.component';

const routes: Routes = [{
  path: '',
  component: SshConnectionComponent,
  children: [
    {
      path: 'registries',
      component: RegistriesComponent,
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
export class SshConnectionRoutingModule {
}