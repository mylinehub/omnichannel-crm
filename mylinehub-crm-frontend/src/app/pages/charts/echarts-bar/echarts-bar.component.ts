import { AfterViewInit, Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { NbThemeService } from '@nebular/theme';

@Component({
  selector: 'ngx-echarts-bar',
  template: `<div echarts [options]="options" class="echart"></div>`,
})
export class EchartsBarComponent implements OnChanges, AfterViewInit, OnDestroy {

  options: any = {};

  @Input() ledgendData: any;
  @Input() data: any;

  // ✅ NEW: optional, backward compatible
  // If not provided -> defaults to "Total Calls" (your current behavior)
  @Input() seriesName?: string;

  themeSubscription: any;
  setLastEventNumberID: any = null;

  eCharts: any = null;
  colors: any = null;

  constructor(private theme: NbThemeService) {}

  ngOnChanges(changes: SimpleChanges) {
    // debounce resetChart by 1s (same as your original)
    if (this.setLastEventNumberID) {
      clearTimeout(this.setLastEventNumberID);
    }
    this.setLastEventNumberID = setTimeout(() => this.resetChart(), 1000);
  }

  ngAfterViewInit() {
    this.themeSubscription = this.theme.getJsTheme().subscribe(config => {
      const colors = config.variables;
      const echarts: any = config.variables.echarts;
      this.eCharts = echarts;
      this.colors = colors;

      // If inputs were already present before theme loaded, render now.
      this.resetChart();
    });
  }

  resetChart() {
    // ✅ Prevent crashes when theme isn't loaded yet
    if (!this.eCharts || !this.colors) return;

    const xData = Array.isArray(this.ledgendData) ? this.ledgendData : [];
    const yData = Array.isArray(this.data) ? this.data : [];

    // ✅ Backward compatible default
    const safeSeriesName =
      (this.seriesName && String(this.seriesName).trim().length > 0)
        ? String(this.seriesName).trim()
        : 'Total Calls';

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
          data: [...xData],
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
      series: [
        {
          name: safeSeriesName,     // ✅ FIXED
          type: 'bar',
          barWidth: '60%',
          data: yData,              // ✅ safe
        },
      ],
    };
  }

  ngOnDestroy(): void {
    if (this.setLastEventNumberID) clearTimeout(this.setLastEventNumberID);
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }
}
