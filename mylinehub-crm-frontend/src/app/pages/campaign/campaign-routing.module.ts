import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { NotFoundComponent } from './../miscellaneous/not-found/not-found.component';
import { CampaignComponent } from './campaign.component';
import { AllCampaignsComponent } from './all-campaigns/all-campaigns.component';
import { RunCampaignViewComponent } from './run-campaign-view/run-campaign-view.component';

const routes: Routes = [{
  path: '',
  component: CampaignComponent,
  children: [
    {
      path: 'all-campaigns',
      component: AllCampaignsComponent,
    },
    {
      path: 'run-history',
      component: RunCampaignViewComponent,
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
export class CampaignRoutingModule {
}