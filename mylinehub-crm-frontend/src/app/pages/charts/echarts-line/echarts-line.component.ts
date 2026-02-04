import { AfterViewInit, Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { NbThemeService } from '@nebular/theme';

@Component({
  selector: 'ngx-echarts-line',
  template: `<div echarts [options]="options" class="echart"></div>`,
})
export class EchartsLineComponent implements OnChanges, AfterViewInit, OnDestroy {

  options: any = {};

  @Input() ledgendData: any;
  @Input() xAxisData: any;

  // IMPORTANT: in your current system "data" is actually "series list"
  @Input() data: any;

  // ✅ optional (backward safe)
  @Input() yAxisType: string = 'log';   // keep your old behavior by default
  @Input() tooltipTrigger: string = 'item'; // keep old behavior
  @Input() seriesNameFallback: string = 'Employee'; // for old tooltips

  themeSubscription: any;
  color: any[] = [];
  colorAll: any[] = [];
  setLastEventNumberID: any = null;

  eCharts: any = null;
  colors: any = null;

  constructor(private theme: NbThemeService) {}

  ngOnChanges(changes: SimpleChanges) {
    // debounce chart rebuild
    if (this.setLastEventNumberID == null) {
      this.setLastEventNumberID = setTimeout(() => this.resetChart(), 300);
    } else {
      clearTimeout(this.setLastEventNumberID);
      this.setLastEventNumberID = setTimeout(() => this.resetChart(), 300);
    }
  }

  ngAfterViewInit() {
    this.themeSubscription = this.theme.getJsTheme().subscribe(config => {
      const colors = config.variables;
      const echarts: any = config.variables.echarts;

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

      this.eCharts = echarts;
      this.colors = colors;

      // ✅ theme is ready → we can build now
      this.resetChart();
    });
  }

  private resetChart() {
    // ✅ do nothing until theme is ready
    if (!this.eCharts) return;

    const legendList = Array.isArray(this.ledgendData) ? [...this.ledgendData] : [];
    const xAxisList = Array.isArray(this.xAxisData) ? [...this.xAxisData] : [];
    const seriesList = Array.isArray(this.data) ? [...this.data] : [];

    // colors for legend count (safe)
    this.color = [];
    if (legendList.length > 0) {
      let i = 0;
      legendList.forEach(() => {
        this.color.unshift(this.colorAll[i] ?? this.colorAll[this.colorAll.length - 1]);
        i++;
      });
    }

    // ✅ backward-safe fallback: if caller forgot series.name, add one from legend
    // (does NOT affect your ReportComponent because you already set name)
    const normalizedSeries = seriesList.map((s: any, idx: number) => {
      if (s && !s.name && legendList[idx]) return { ...s, name: String(legendList[idx]) };
      if (s && !s.name) return { ...s, name: this.seriesNameFallback };
      return s;
    });

    this.options = {
      backgroundColor: this.eCharts.bg,
      color: [...this.color],

      tooltip: {
        trigger: this.tooltipTrigger, // 'item' (old) or 'axis'
        formatter: '{a} <br/>{b} : {c}',
      },

      legend: {
        left: 'left',
        data: legendList,
        textStyle: { color: this.eCharts.textColor },
      },

      xAxis: [
        {
          type: 'category',
          data: xAxisList,
          axisTick: { alignWithLabel: true },
          axisLine: { lineStyle: { color: this.eCharts.axisLineColor } },
          axisLabel: { textStyle: { color: this.eCharts.textColor } },
        },
      ],

      yAxis: [
        {
          type: this.yAxisType || 'value', // default keeps old behavior = 'log'
          axisLine: { lineStyle: { color: this.eCharts.axisLineColor } },
          splitLine: { lineStyle: { color: this.eCharts.splitLineColor } },
          axisLabel: { textStyle: { color: this.eCharts.textColor } },
        },
      ],

      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },

      // ✅ your caller already passes real echarts series objects
      series: normalizedSeries,
    };
  }

  ngOnDestroy(): void {
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
    if (this.setLastEventNumberID) clearTimeout(this.setLastEventNumberID);
  }
}
