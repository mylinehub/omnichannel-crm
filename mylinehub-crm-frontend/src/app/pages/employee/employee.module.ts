import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProfileComponent } from './profile/profile.component';
import { EmployeeComponent } from './employee.component';
import { RouterModule } from '@angular/router';
import { EmployeeRoutingModule } from './employee-routing.module';
import { EmployeeService } from './service/employee.service';
import {
    NbPopoverModule,
    NbBadgeModule,NbTooltipModule,
    NbAccordionModule,
    NbButtonModule,
    NbCardModule,
    NbSpinnerModule,
    NbListModule,
    NbRouteTabsetModule,
    NbStepperModule,
    NbTabsetModule, NbUserModule,NbTimepickerModule,NbDatepickerModule,NbToggleModule,
    NbIconModule, NbInputModule, NbTreeGridModule ,NbRadioModule,NbOptionModule,NbSelectModule,NbFormFieldModule,NbTagModule, NbDialogModule,
    NbListPageTrackerDirective,
    NbButtonGroupModule
  } from '@nebular/theme';
import { AllEmployeesComponent } from './all-employees/all-employees.component';
import { CardComponent } from './profile/card/card.component';
import { DialogComponent } from './all-employees/dialog/dialog.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { FormsModule } from '@angular/forms';
import { Ng2SmartTableModule } from 'ng2-smart-table';
import { ResetExtensionPasswordComponent } from './profile/reset-extension-password/reset-extension-password.component';
import { AllCardsComponent } from './all-employees/all-cards/all-cards.component';
import { MonitorEmployeesComponent } from './monitor-employees/monitor-employees.component';
import { ResetWebPasswordComponent } from './all-employees/reset-web-password/reset-web-password.component';
import { AdminResetExtensionPasswordComponent } from './all-employees/admin-reset-extension-password/admin-reset-extension-password.component';
import { PlayAudioComponent } from './all-employees/play-audio/play-audio.component';
import { MonitorBodyComponent } from './monitor-employees/monitor-body/monitor-body.component';
import { FileStorageModule } from "../file-storage/file-storage.module";
import { EmployeeCallHistoryComponent } from './employee-call-history/employee-call-history.component';
import { CallPostComponent } from './employee-call-history/call-post/call-post.component';
import { CallPostPlaceholderComponent } from './employee-call-history/call-post-placeholder/call-post-placeholder.component';
import { CustomInputTableComponent } from './all-employees/custom-input-table/custom-input-table.component';
@NgModule({
    declarations: [
        ProfileComponent,
        EmployeeComponent,
        AllEmployeesComponent,
        CardComponent,
        DialogComponent,
        ResetExtensionPasswordComponent,
        AllCardsComponent,
        MonitorEmployeesComponent,
        ResetWebPasswordComponent,
        AdminResetExtensionPasswordComponent,
        PlayAudioComponent,
        MonitorBodyComponent,
        EmployeeCallHistoryComponent,
        CallPostComponent,
        CallPostPlaceholderComponent,
        CustomInputTableComponent
    ],
    providers: [EmployeeService],
    imports: [
        NbPopoverModule,NbSpinnerModule,NbButtonGroupModule,NbListModule, NbTooltipModule, NbDialogModule.forChild(), NbToggleModule, NbDatepickerModule, NbTimepickerModule, NbTagModule, NbFormFieldModule, NbOptionModule, NbSelectModule, NbRadioModule, NgxChartsModule, FormsModule, CommonModule, EmployeeRoutingModule, RouterModule, NbAccordionModule, NbButtonModule, NbCardModule, NbListModule, NbRouteTabsetModule, NbStepperModule, NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule, Ng2SmartTableModule, NbBadgeModule,
        FileStorageModule
    ]
})
export class EmployeeModule { }
