import { AfterViewInit, Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { NbThemeService } from '@nebular/theme';

@Component({
  selector: 'ngx-echarts-bar-animation',
  template: `<div echarts [options]="options" class="echart"></div>`,
})
export class EchartsBarAnimationComponent implements OnChanges, AfterViewInit, OnDestroy {

  options: any = {};
  @Input() ledgendData: any;   // x-axis labels
  @Input() data: any;          // bar values

  // optional: if not provided -> "Employee" (backward compatible)
  @Input() seriesName: string = 'Employee';

  themeSubscription: any;
  eCharts: any = null;
  colors: any = null;
  setLastEventNumberID: any = null;

  constructor(private theme: NbThemeService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (this.setLastEventNumberID == null) {
      this.setLastEventNumberID = setTimeout(() => this.resetChart(), 300);
    } else {
      clearTimeout(this.setLastEventNumberID);
      this.setLastEventNumberID = setTimeout(() => this.resetChart(), 300);
    }
  }

  ngAfterViewInit() {
    this.themeSubscription = this.theme.getJsTheme().subscribe(config => {
      this.colors = config.variables;
      this.eCharts = config.variables.echarts;
      this.resetChart();
    });
  }

  resetChart() {
    if (!this.eCharts || !this.colors) return;

    const xAxisData = Array.isArray(this.ledgendData) ? [...this.ledgendData] : [];
    const seriesData = Array.isArray(this.data) ? [...this.data] : [];

    const safeSeriesName =
      (this.seriesName && String(this.seriesName).trim().length > 0)
        ? String(this.seriesName).trim()
        : 'Employee';

    this.options = {
      backgroundColor: this.eCharts.bg,
      color: [this.colors.primaryLight],

      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
      },

      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },

      xAxis: [
        {
          type: 'category',
          data: xAxisData,
          axisTick: { alignWithLabel: true },
          axisLine: { lineStyle: { color: this.eCharts.axisLineColor } },
          axisLabel: { textStyle: { color: this.eCharts.textColor } },
        },
      ],

      yAxis: [
        {
          type: 'value',
          axisLine: { lineStyle: { color: this.eCharts.axisLineColor } },
          splitLine: { lineStyle: { color: this.eCharts.splitLineColor } },
          axisLabel: { textStyle: { color: this.eCharts.textColor } },
        },
      ],

      legend: {
        data: [safeSeriesName],
        align: 'left',
        textStyle: { color: this.eCharts.textColor },
      },

      series: [
        {
          name: safeSeriesName,
          type: 'bar',
          barWidth: '60%',
          data: seriesData,
          animationDelay: (idx: number) => idx * 20,
        },
      ],

      animationEasing: 'elasticOut',
      animationDelayUpdate: (idx: number) => idx * 5,
    };
  }

  ngOnDestroy(): void {
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }
}
