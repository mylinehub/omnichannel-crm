import { AfterViewInit, Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { NbThemeService } from '@nebular/theme';

@Component({
  selector: 'ngx-echarts-radar',
  template: `<div echarts [options]="options" class="echart"></div>`,
})
export class EchartsRadarComponent implements AfterViewInit, OnDestroy, OnChanges {

  options: any = {};

  @Input() ledgendData: any;     // legend labels (optional)
  @Input() indicator: any;       // radar indicator list (optional)
  @Input() series: any;          // echarts series list (optional)

  themeSubscription: any;
  eCharts: any = null;
  colors: any = null;

  constructor(private theme: NbThemeService) {}

  ngAfterViewInit() {
    this.themeSubscription = this.theme.getJsTheme().subscribe(config => {
      this.colors = config.variables;
      this.eCharts = config.variables.echarts;
      this.resetChart();
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    this.resetChart();
  }

  private resetChart() {
    if (!this.eCharts || !this.colors) return;

    // ✅ backward-safe default indicator
    const safeIndicator = Array.isArray(this.indicator) && this.indicator.length > 0
      ? [...this.indicator]
      : [
          { name: 'Calls', max: 6500 },
          { name: 'Holidays', max: 16000 },
          { name: 'Conversions', max: 30000 },
          { name: 'Knowledge Transfer', max: 38000 },
          { name: 'Innovation', max: 52000 },
          { name: 'Deadbeat', max: 25000 },
        ];

    // ✅ backward-safe default series
    const safeSeries = Array.isArray(this.series) && this.series.length > 0
      ? [...this.series]
      : [
          {
            name: 'Compare Employees',
            type: 'radar',
            data: [
              { value: [4300, 10000, 28000, 35000, 50000, 19000], name: 'Employee A' },
              { value: [5000, 14000, 28000, 31000, 42000, 21000], name: 'Employee B' },
            ],
          },
        ];

    // ✅ legend priority:
    // 1) ledgendData input
    // 2) collect names from radar data
    // 3) fallback Employee A/B (already covered by default series)
    const legendFromSeriesData: string[] = [];
    safeSeries.forEach(s => {
      (s?.data ?? []).forEach(d => {
        if (d?.name) legendFromSeriesData.push(String(d.name));
      });
    });

    const safeLegend =
      Array.isArray(this.ledgendData) && this.ledgendData.length > 0
        ? [...this.ledgendData]
        : legendFromSeriesData;

    this.options = {
      backgroundColor: this.eCharts.bg,
      color: [this.colors.danger, this.colors.warning, this.colors.info, this.colors.success, this.colors.primary],

      tooltip: {},

      legend: {
        data: safeLegend,
        textStyle: { color: this.eCharts.textColor },
      },

      radar: {
        name: { textStyle: { color: this.eCharts.textColor } },
        indicator: safeIndicator,
        splitArea: { areaStyle: { color: 'transparent' } },
      },

      series: safeSeries,
    };
  }

  ngOnDestroy(): void {
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }
}
