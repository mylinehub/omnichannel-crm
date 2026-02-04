import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CallDetailComponent } from './call-detail.component';
import { RouterModule } from '@angular/router';
import { CallDetailRoutingModule } from './call-detail-routing.module';
import { AllCallsComponent } from './all-calls/all-calls.component';
import { CallDetailService } from './service/call-detail.service';
import {
  NbAccordionModule,
  NbButtonModule,
  NbCardModule,
  NbListModule,
  NbRouteTabsetModule,
  NbStepperModule,
  NbTabsetModule, NbUserModule,NbTimepickerModule,NbDatepickerModule,NbToggleModule,
  NbIconModule, NbInputModule, NbTreeGridModule ,NbRadioModule,NbOptionModule,NbSelectModule,NbFormFieldModule,NbTagModule, NbDialogModule,
  NbAutocompleteModule,
  NbSpinnerModule,
  NbButtonGroupModule
} from '@nebular/theme';
import { CellScrollComponent } from './all-calls/cell-scroll/cell-scroll.component';
import { CardComponent } from './all-calls/card/card.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { FormsModule } from '@angular/forms';
import { Ng2SmartTableModule } from 'ng2-smart-table';
import { ReplacePipe } from './pipes/replace.pipe';
import { ChartsModule } from '../charts/charts.module';
import { CallDashboardComponent } from './call-dashboard/call-dashboard.component';
import { CallHistoryComponent } from './call-history/call-history.component';
import { ConvertedButtonComponent } from './all-calls/converted-button/converted-button.component';
import { DescriptionInputComponent } from './all-calls/description-input/description-input.component';
// import { ChartjsBarHorizontalComponent } from '../charts/chartjs-bar-horizontal/chartjs-bar-horizontal.component';
// import { ChartjsBarComponent } from '../charts/chartjs-bar/chartjs-bar.component';
// import { ChartjsLineComponent } from '../charts/chartjs-line/chartjs-line.component';
// import { ChartjsMultipleXaxisComponent } from '../charts/chartjs-multiple-xaxis/chartjs-multiple-xaxis.component';
// import { ChartjsRadarComponent } from '../charts/chartjs-radar/chartjs-radar.component';
// import { EchartsAreaStackComponent } from '../charts/echarts-area-stack/echarts-area-stack.component';
// import { EchartsBarAnimationComponent } from '../charts/echarts-bar-animation/echarts-bar-animation.component';
// import { EchartsBarComponent } from '../charts/echarts-bar/echarts-bar.component';
// import { EchartsLineComponent } from '../charts/echarts-line/echarts-line.component';
// import { EchartsMultipleXaxisComponent } from '../charts/echarts-multiple-xaxis/echarts-multiple-xaxis.component';
// import { EchartsPieComponent } from '../charts/echarts-pie/echarts-pie.component';
// import { EchartsRadarComponent } from '../charts/echarts-radar/echarts-radar.component';
// import { D3AdvancedPieComponent } from '../charts/d3-advanced-pie/d3-advanced-pie.component';
// import { D3AreaStackComponent } from '../charts/d3-area-stack/d3-area-stack.component';
// import { D3BarComponent } from '../charts/d3-bar/d3-bar.component';
// import { D3LineComponent } from '../charts/d3-line/d3-line.component';
// import { D3PipeComponent } from '../charts/d3-pipe/d3-pipe.component';
// import { D3PolarComponent } from '../charts/d3-polar/d3-polar.component';
// import { ChartjsPieComponent } from '../charts/chartjs-pie/chartjs-pie.component';


@NgModule({
  declarations: [
    CallDetailComponent,
    AllCallsComponent,
    CellScrollComponent,
    CardComponent,
    ReplacePipe,
    CallDashboardComponent,
    CallHistoryComponent,
    ConvertedButtonComponent,
    DescriptionInputComponent,
  ],
  imports: [
    ChartsModule,NbAutocompleteModule,NbButtonGroupModule,NbSpinnerModule,NbListModule,NbDialogModule.forChild(),NbToggleModule,NbDatepickerModule,NbTimepickerModule,NbTagModule, NbFormFieldModule,NbOptionModule,NbSelectModule,NbRadioModule,NgxChartsModule,FormsModule, CommonModule,CallDetailRoutingModule,RouterModule, NbAccordionModule, NbButtonModule, NbCardModule ,  NbListModule, NbRouteTabsetModule, NbStepperModule,  NbTabsetModule, NbUserModule, NbIconModule, NbInputModule, NbTreeGridModule , Ng2SmartTableModule
  ],
  providers :[CallDetailService],
})
export class CallDetailModule { }
