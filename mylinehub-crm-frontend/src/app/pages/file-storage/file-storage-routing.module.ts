import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { AllFileStorageComponent } from './all-file-storage/all-file-storage.component';
import { FileStorageComponent } from './file-storage.component';

const routes: Routes = [{
  path: '',
  component: FileStorageComponent,
  children: [
    {
      path: 'all-files',
      component: AllFileStorageComponent,
    },
    {
      path: 'all-whatsapp-files',
      component: AllFileStorageComponent,
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
export class FileStorageRoutingModule {
}