import { Component, AfterViewInit, OnDestroy, Input, OnChanges, SimpleChanges } from '@angular/core';
import { NbThemeService } from '@nebular/theme';

@Component({
  selector: 'ngx-echarts-multiple-xaxis',
  template: `<div echarts [options]="options" class="echart"></div>`,
})
export class EchartsMultipleXaxisComponent implements AfterViewInit, OnDestroy, OnChanges {
  options: any = {};

  @Input() ledgendData: any;        // array of legend labels
  @Input() xAxisData: any;          // array OR array-of-arrays
  @Input() xAxisType: any;          // string OR array of strings
  @Input() xAxisBoundaryGap: any;   // boolean OR array of booleans
  @Input() yAxisType: any;          // string (usually "value")
  @Input() series: any;             // array of echarts series objects

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

    const legend = Array.isArray(this.ledgendData) ? [...this.ledgendData] : [];

    // normalize xAxisData -> always array of arrays (one per xAxis)
    const rawXAxisData = this.xAxisData;
    const xAxisDataList: any[] =
      Array.isArray(rawXAxisData)
        ? (Array.isArray(rawXAxisData[0]) ? rawXAxisData : [rawXAxisData])
        : [[]];

    // normalize xAxisType -> one per xAxis
    const rawXAxisType = this.xAxisType;
    const xAxisTypeList: string[] =
      Array.isArray(rawXAxisType)
        ? rawXAxisType.map(x => String(x))
        : xAxisDataList.map(() => (rawXAxisType ? String(rawXAxisType) : 'category'));

    // normalize boundaryGap -> one per xAxis
    const rawGap = this.xAxisBoundaryGap;
    const gapList: any[] =
      Array.isArray(rawGap)
        ? rawGap
        : xAxisDataList.map(() => (rawGap !== undefined ? rawGap : false));

    const yType = this.yAxisType ? String(this.yAxisType) : 'value';

    const seriesList = Array.isArray(this.series) ? [...this.series] : [];

    // backward-safe fallback (only if nothing passed)
    const safeLegend = legend.length > 0 ? legend : ['Employee A', 'Employee B'];
    const safeXAxisDataList = (xAxisDataList.length > 0 && xAxisDataList[0].length > 0)
      ? xAxisDataList
      : [[ '2016-1','2016-2','2016-3','2016-4','2016-5','2016-6','2016-7','2016-8','2016-9','2016-10','2016-11','2016-12' ]];

    const safeSeriesList = seriesList.length > 0 ? seriesList : [
      { name: 'Employee A', type: 'line', smooth: true, data: [2.6,5.9,9.0,26.4,28.7,70.7,175.6,182.2,48.7,18.8,6.0,2.3] },
      { name: 'Employee B', type: 'line', smooth: true, data: [3.9,5.9,11.1,18.7,48.3,69.2,231.6,46.6,55.4,18.4,10.3,0.7] },
    ];

    this.options = {
      backgroundColor: this.eCharts.bg,
      color: [this.colors.success, this.colors.info, this.colors.warning, this.colors.primary, this.colors.danger],

      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross' },
      },

      legend: {
        data: safeLegend,
        textStyle: { color: this.eCharts.textColor },
      },

      grid: { top: 70, bottom: 50, left: '3%', right: '4%', containLabel: true },

      xAxis: safeXAxisDataList.map((dataArr: any[], idx: number) => ({
        type: xAxisTypeList[idx] ?? 'category',
        boundaryGap: gapList[idx] ?? false,
        axisTick: { alignWithLabel: true },
        axisLine: {
          onZero: false,
          lineStyle: { color: this.eCharts.axisLineColor },
        },
        axisLabel: { textStyle: { color: this.eCharts.textColor } },
        data: [...dataArr],
      })),

      yAxis: [
        {
          type: yType,
          axisLine: { lineStyle: { color: this.eCharts.axisLineColor } },
          splitLine: { lineStyle: { color: this.eCharts.splitLineColor } },
          axisLabel: { textStyle: { color: this.eCharts.textColor } },
        },
      ],

      series: safeSeriesList,
    };
  }

  ngOnDestroy(): void {
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }
}
