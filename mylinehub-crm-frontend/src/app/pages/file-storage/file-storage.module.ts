import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AllFileStorageComponent } from './all-file-storage/all-file-storage.component';
import { FileStorageService } from './service/file-storage.service';
import { FileStorageComponent } from './file-storage.component';
import { RouterModule } from '@angular/router';
import { FileStorageRoutingModule } from './file-storage-routing.module';
import {
  NbActionsModule,
  NbPopoverModule,
  NbCheckboxModule,
  NbBadgeModule,NbTooltipModule,
  NbAccordionModule,
  NbButtonModule,
  NbCardModule,
  NbListModule,
  NbRouteTabsetModule,
  NbProgressBarModule,
  NbStepperModule,
  NbTabsetModule, NbUserModule,NbTimepickerModule,NbDatepickerModule,NbToggleModule,
  NbIconModule, NbInputModule, NbTreeGridModule ,NbRadioModule,NbOptionModule,NbSelectModule,NbFormFieldModule,NbTagModule, NbDialogModule
} from '@nebular/theme';
import { CreateUpdateCategoryComponent } from './create-update-category/create-update-category.component';
import { FormsModule } from '@angular/forms';
import { ReplacePipe } from './pipes/replace.pipe';
import { AskDeleteComponent } from './ask-delete/ask-delete.component';
import { UploadStatusComponent } from './upload-status/upload-status.component';
import { DownloadStatusComponent } from './download-status/download-status.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';

const PIPES = [
  ReplacePipe
];

@NgModule({
  declarations: [
    FileStorageComponent,
    AllFileStorageComponent,
    CreateUpdateCategoryComponent,
    ReplacePipe,
    AskDeleteComponent,
    UploadStatusComponent,
    DownloadStatusComponent
  ],
  imports: [
    CommonModule,
    NgxChartsModule,
    RouterModule,
    FileStorageRoutingModule,
    NbActionsModule,
    NbCheckboxModule,
    NbProgressBarModule,
    NbPopoverModule,
    NbBadgeModule,NbTooltipModule,
    NbAccordionModule,
    NbButtonModule,
    NbCardModule,
    NbListModule,
    NbRouteTabsetModule,
    NbStepperModule,
    FormsModule,
    NbTabsetModule, NbUserModule,NbTimepickerModule,NbDatepickerModule,NbToggleModule,
    NbIconModule, NbInputModule, NbTreeGridModule ,NbRadioModule,NbOptionModule,NbSelectModule,NbFormFieldModule,NbTagModule, NbDialogModule
  ],
  exports: [...PIPES],
  providers:[FileStorageService]
})
export class FileStorageModule { }
