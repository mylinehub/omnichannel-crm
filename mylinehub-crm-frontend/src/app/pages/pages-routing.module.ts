import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';

import { PagesComponent } from './pages.component';
import { NotFoundComponent } from './miscellaneous/not-found/not-found.component';

const routes: Routes = [{
  path: '',
  component: PagesComponent,
  children: [
    {
      path: 'absenteeism',
      loadChildren: () => import('./absenteeism/absenteeism.module')
        .then(m => m.AbsenteeismModule),
    },
    {
      path: 'ami-connection',
      loadChildren: () => import('./ami-connection/ami-connection.module')
        .then(m => m.AmiConnectionModule),
    },
    {
      path: 'call-detail',
      loadChildren: () => import('./call-detail/call-detail.module')
        .then(m => m.CallDetailModule),
    },
    {
      path: 'calling-cost',
      loadChildren: () => import('./calling-cost/calling-cost.module')
        .then(m => m.CallingCostModule),
    },
    {
      path: 'campaign',
      loadChildren: () => import('./campaign/campaign.module')
        .then(m => m.CampaignModule),
    },
    {
      path: 'conference',
      loadChildren: () => import('./conference/conference.module')
        .then(m => m.ConferenceModule),
    },
    {
      path: 'customer',
      loadChildren: () => import('./customer/customer.module')
        .then(m => m.CustomerModule),
    },
    {
      path: 'department',
      loadChildren: () => import('./department/department.module')
        .then(m => m.DepartmentModule),
    },
    {
      path: 'employee',
      loadChildren: () => import('./employee/employee.module')
        .then(m => m.EmployeeModule),
    },
    {
      path: 'file-storage',
      loadChildren: () => import('./file-storage/file-storage.module')
        .then(m => m.FileStorageModule),
    },
    {
      path: 'error',
      loadChildren: () => import('./error/error.module')
        .then(m => m.ErrorModule),
    },
    {
      path: 'ivr',
      loadChildren: () => import('./ivr/ivr.module')
        .then(m => m.IvrModule),
    },
    {
      path: 'log',
      loadChildren: () => import('./logs/logs.module')
        .then(m => m.LogsModule),
    },
    {
      path: 'product',
      loadChildren: () => import('./product/product.module')
        .then(m => m.ProductModule),
    },
    {
      path: 'purchase',
      loadChildren: () => import('./purchases/purchases.module')
        .then(m => m.PurchasesModule),
    },
    {
      path: 'queue',
      loadChildren: () => import('./queue/queue.module')
        .then(m => m.QueueModule),
    },
    {
      path: 'search',
      loadChildren: () => import('./search/search.module')
        .then(m => m.SearchModule),
    },
    {
      path: 'sip-provider',
      loadChildren: () => import('./sip-provider/sip-provider.module')
        .then(m => m.SipProviderModule),
    },
    {
      path: 'ssh-connection',
      loadChildren: () => import('./ssh-connection/ssh-connection.module')
        .then(m => m.SshConnectionModule),
    },
    {
      path: 'supplier',
      loadChildren: () => import('./supplier/supplier.module')
        .then(m => m.SupplierModule),
    },
    {
      path: 'reset-password',
      loadChildren: () => import('./reset-password/reset-password.module')
        .then(m => m.ResetPasswordModule),
    },
    {
      path: 'whatsapp-report',
      loadChildren: () => import('./whatsapp-report/whatsapp-report.module')
        .then(m => m.WhatsappReportModule),
    },
    {
      path: 'whatsapp-project',
      loadChildren: () => import('./whatsapp-project/whatsapp-project.module')
        .then(m => m.WhatsappProjectModule),
    },
    {
      path: 'whatsapp-number',
      loadChildren: () => import('./whatsapp-number/whatsapp-number.module')
        .then(m => m.WhatsappNumberModule),
    },
    {
      path: 'whatsapp-chat',
      loadChildren: () => import('./whatsapp-chat/whatsapp-chat.module')
        .then(m => m.WhatsappChatModule),
    },
    {
      path: 'property-management',
      loadChildren: () =>
        import('./property-management/property-management.module')
          .then(m => m.PropertyManagementModule),
    },

    {
      path: 'franchise-management',
      loadChildren: () =>
        import('./franchise-management/franshise-management.module')
          .then(m => m.FranshiseManagementModule),
    },
   /* {
      path: 'dashboard',
      component: ECommerceComponent,
    },
   
    {
      path: 'iot-dashboard',
      component: DashboardComponent,
    },
    {
      path: 'layout',
      loadChildren: () => import('./layout/layout.module')
        .then(m => m.LayoutModule),
    },
    {
      path: 'forms',
      loadChildren: () => import('./forms/forms.module')
        .then(m => m.FormsModule),
    },
    {
      path: 'ui-features',
      loadChildren: () => import('./ui-features/ui-features.module')
        .then(m => m.UiFeaturesModule),
    },
    {
      path: 'modal-overlays',
      loadChildren: () => import('./modal-overlays/modal-overlays.module')
        .then(m => m.ModalOverlaysModule),
    },
    {
      path: 'extra-components',
      loadChildren: () => import('./extra-components/extra-components.module')
        .then(m => m.ExtraComponentsModule),
    },
    {
      path: 'maps',
      loadChildren: () => import('./maps/maps.module')
        .then(m => m.MapsModule),
    },
    {
      path: 'charts',
      loadChildren: () => import('./charts/charts.module')
        .then(m => m.ChartsModule),
    },
    {
      path: 'editors',
      loadChildren: () => import('./editors/editors.module')
        .then(m => m.EditorsModule),
    },
    {
      path: 'tables',
      loadChildren: () => import('./tables/tables.module')
        .then(m => m.TablesModule),
    },*/
    {
      path: 'miscellaneous',
      loadChildren: () => import('./miscellaneous/miscellaneous.module')
        .then(m => m.MiscellaneousModule),
    },
    {
      path: '',
      redirectTo: '/pages/employee/profile',
      pathMatch: 'full',
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
export class PagesRoutingModule {
}
