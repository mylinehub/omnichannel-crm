import { Component, Input, OnDestroy } from '@angular/core';
import { NbThemeService, NbColorHelper } from '@nebular/theme';

@Component({
  selector: 'ngx-chartjs-radar',
  template: `
    <chart type="radar" [data]="data" [options]="options"></chart>
  `,
})
export class ChartjsRadarComponent implements OnDestroy {
  @Input() data: any;
  options: any;
  themeSubscription: any;

  constructor(private theme: NbThemeService) {
    this.themeSubscription = this.theme.getJsTheme().subscribe(config => {

      const colors: any = config.variables;
      const chartjs: any = config.variables.chartjs;

      this.data = {
        labels: ['Calls', 'Conversions', 'Leave', 'OverWork', 'Innovation', 'Team Building', 'Certifications'],
        datasets: [{
          data: [65, 59, 90, 81, 56, 55, 40],
          label: 'Employee A',
          borderColor: colors.danger,
          backgroundColor: NbColorHelper.hexToRgbA(colors.dangerLight, 0.5),
        }, {
          data: [28, 48, 40, 19, 96, 27, 100],
          label: 'Employee B',
          borderColor: colors.warning,
          backgroundColor: NbColorHelper.hexToRgbA(colors.warningLight, 0.5),
        }],
      };

      this.options = {
        responsive: true,
        maintainAspectRatio: false,
        scaleFontColor: 'white',
        legend: {
          labels: {
            fontColor: chartjs.textColor,
          },
        },
        scale: {
          pointLabels: {
            fontSize: 14,
            fontColor: chartjs.textColor,
          },
          gridLines: {
            color: chartjs.axisLineColor,
          },
          angleLines: {
            color: chartjs.axisLineColor,
          },
        },
      };
    });
  }

  ngOnDestroy(): void {
    this.themeSubscription.unsubscribe();
  }
}
