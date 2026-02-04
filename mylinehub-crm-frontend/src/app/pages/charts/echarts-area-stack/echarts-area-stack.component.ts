import { AfterViewInit, Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { NbThemeService } from '@nebular/theme';

@Component({
  selector: 'ngx-echarts-area-stack',
  template: `<div echarts [options]="options" class="echart"></div>`,
})
export class EchartsAreaStackComponent implements OnChanges, AfterViewInit, OnDestroy {

  options: any = {};

  @Input() ledgendData: any;
  @Input() xAxisData: any;
  @Input() data: any;

  themeSubscription: any;

  color: any[] = [];
  colorAll: any[] = [];
  setLastEventNumberID: any = null;

  eCharts: any = null;
  colors: any = null;

  constructor(private theme: NbThemeService) {}

  ngOnChanges(changes: SimpleChanges) {
    // debounce resetChart() to allow inputs to settle
    if (this.setLastEventNumberID) clearTimeout(this.setLastEventNumberID);
    this.setLastEventNumberID = setTimeout(() => this.resetChart(), 300);
  }

  ngAfterViewInit() {
    this.themeSubscription = this.theme.getJsTheme().subscribe(config => {
      const colors = config.variables;
      this.colors = colors;

      this.colorAll = [
        colors.warningLight,
        colors.infoLight,
        colors.dangerLight,
        colors.successLight,
        colors.primaryLight,
        '#ffcc99',
        '#ffb3ff',
        '#99bfd9'
      ];

      this.eCharts = config.variables.echarts;

      // Theme arrived — if inputs already exist, render now
      this.resetChart();
    });
  }

  resetChart() {
    // guard: theme not ready yet
    if (!this.eCharts) return;

    const legend = Array.isArray(this.ledgendData) ? this.ledgendData : [];
    const xAxis = Array.isArray(this.xAxisData) ? this.xAxisData : [];
    const series = Array.isArray(this.data) ? this.data : [];

    // build colors safely
    this.color = [];
    for (let i = 0; i < legend.length; i++) {
      const c = this.colorAll[i] ?? this.colorAll[this.colorAll.length - 1] ?? '#999999';
      this.color.unshift(c);
    }

    this.options = {
      backgroundColor: this.eCharts.bg,
      color: [...this.color],
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          label: { backgroundColor: this.eCharts.tooltipBackgroundColor },
        },
      },
      legend: {
        data: [...legend],
        textStyle: { color: this.eCharts.textColor },
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
          boundaryGap: false,
          data: [...xAxis],
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
      series: [...series],
    };

    // remove noisy log in production (keep if you want)
    // console.log("AreaStack options:", this.options);
  }

  ngOnDestroy(): void {
    if (this.setLastEventNumberID) clearTimeout(this.setLastEventNumberID);
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }
}
