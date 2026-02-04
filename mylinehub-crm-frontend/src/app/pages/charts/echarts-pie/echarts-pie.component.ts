import { AfterViewInit, Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { NbThemeService } from '@nebular/theme';

@Component({
  selector: 'ngx-echarts-pie',
  template: `<div echarts [options]="options" class="echart"></div>`,
})
export class EchartsPieComponent implements OnChanges, AfterViewInit, OnDestroy {

  options: any = {};
  @Input() ledgendData: any;
  @Input() data: any;

  /**
   * BACKWARD COMPATIBLE:
   * - If parent DOES NOT pass seriesName => defaults to "Employee" (old behavior)
   * - If parent passes seriesName => uses that (Engagement/Inbound/Outbound)
   */
  @Input() seriesName?: string;

  themeSubscription: any;
  color: any = [];
  colorAll: any;
  setLastEventNumberID: any = null;
  eCharts: any = null;
  colors: any = null;

  constructor(private theme: NbThemeService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (this.setLastEventNumberID == null) {
      this.setLastEventNumberID = setTimeout(() => this.resetChart(), 1000);
    } else {
      clearTimeout(this.setLastEventNumberID);
      this.setLastEventNumberID = setTimeout(() => this.resetChart(), 1000);
    }
  }

  ngAfterViewInit() {
    this.themeSubscription = this.theme.getJsTheme().subscribe(config => {
      const colors = config.variables;
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
      const echarts: any = config.variables.echarts;
      this.eCharts = echarts;
      this.colors = colors;
    });
  }

  resetChart() {
    if (!this.eCharts) return;

    this.color = [];

    if (this.ledgendData && this.ledgendData.length > 0) {
      let i = 0;
      this.ledgendData.forEach(() => {
        this.color.unshift(this.colorAll[i] ?? this.colorAll[this.colorAll.length - 1]);
        i = i + 1;
      });
    }

    // ✅ DEFAULT BACK TO OLD: "Employee"
    const safeSeriesName =
      (this.seriesName && String(this.seriesName).trim().length > 0)
        ? String(this.seriesName).trim()
        : 'Employee';

    this.options = {
      backgroundColor: this.eCharts.bg,
      color: [...this.color],
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b} : {c} ({d}%)',
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: [...(this.ledgendData ?? [])],
        textStyle: { color: this.eCharts.textColor },
      },
      series: [
        {
          name: safeSeriesName,
          type: 'pie',
          radius: '80%',
          center: ['50%', '50%'],
          data: this.data ?? [],
          itemStyle: {
            emphasis: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: this.eCharts.itemHoverShadowColor,
            },
          },
          label: {
            normal: { textStyle: { color: this.eCharts.textColor } },
          },
          labelLine: {
            normal: { lineStyle: { color: this.eCharts.axisLineColor } },
          },
        },
      ],
    };
  }

  ngOnDestroy(): void {
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }
}
