import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartjsBarHorizontalComponent } from '../charts/chartjs-bar-horizontal/chartjs-bar-horizontal.component';
import { ChartjsBarComponent } from '../charts/chartjs-bar/chartjs-bar.component';
import { ChartjsLineComponent } from '../charts/chartjs-line/chartjs-line.component';
import { ChartjsMultipleXaxisComponent } from '../charts/chartjs-multiple-xaxis/chartjs-multiple-xaxis.component';
import { ChartjsRadarComponent } from '../charts/chartjs-radar/chartjs-radar.component';
import { EchartsAreaStackComponent } from '../charts/echarts-area-stack/echarts-area-stack.component';
import { EchartsBarAnimationComponent } from '../charts/echarts-bar-animation/echarts-bar-animation.component';
import { EchartsBarComponent } from '../charts/echarts-bar/echarts-bar.component';
import { EchartsLineComponent } from '../charts/echarts-line/echarts-line.component';
import { EchartsMultipleXaxisComponent } from '../charts/echarts-multiple-xaxis/echarts-multiple-xaxis.component';
import { EchartsPieComponent } from '../charts/echarts-pie/echarts-pie.component';
import { EchartsRadarComponent } from '../charts/echarts-radar/echarts-radar.component';
import { D3AdvancedPieComponent } from '../charts/d3-advanced-pie/d3-advanced-pie.component';
import { D3AreaStackComponent } from '../charts/d3-area-stack/d3-area-stack.component';
import { D3BarComponent } from '../charts/d3-bar/d3-bar.component';
import { D3LineComponent } from '../charts/d3-line/d3-line.component';
import { D3PipeComponent } from '../charts/d3-pipe/d3-pipe.component';
import { D3PolarComponent } from '../charts/d3-polar/d3-polar.component';
import { ChartjsPieComponent } from '../charts/chartjs-pie/chartjs-pie.component';
import { NgxEchartsModule } from 'ngx-echarts';
import { ChartModule } from 'angular2-chartjs';
import { NgxChartsModule } from '@swimlane/ngx-charts';

@NgModule({
  declarations: [ChartjsBarHorizontalComponent, ChartjsBarComponent, ChartjsLineComponent, ChartjsMultipleXaxisComponent, ChartjsRadarComponent, EchartsAreaStackComponent, EchartsBarAnimationComponent, EchartsBarComponent, EchartsLineComponent, EchartsMultipleXaxisComponent, EchartsPieComponent, EchartsRadarComponent, D3AdvancedPieComponent, D3AreaStackComponent, D3BarComponent, D3LineComponent, D3PipeComponent, D3PolarComponent, ChartjsPieComponent],
  imports: [
    CommonModule,
    NgxEchartsModule,
    ChartModule,
    NgxChartsModule,
  ],
  exports:[ChartjsBarHorizontalComponent, ChartjsBarComponent, ChartjsLineComponent, ChartjsMultipleXaxisComponent, ChartjsRadarComponent, EchartsAreaStackComponent, EchartsBarAnimationComponent, EchartsBarComponent, EchartsLineComponent, EchartsMultipleXaxisComponent, EchartsPieComponent, EchartsRadarComponent, D3AdvancedPieComponent, D3AreaStackComponent, D3BarComponent, D3LineComponent, D3PipeComponent, D3PolarComponent, ChartjsPieComponent]
})
export class ChartsModule { }
