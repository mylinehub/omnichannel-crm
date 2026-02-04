import { Component, Input, OnDestroy } from '@angular/core';
import { NbThemeService } from '@nebular/theme';

@Component({
  selector: 'ngx-d3-area-stack',
  template: `
    <ngx-charts-area-chart
      [scheme]="colorScheme"
      [results]="multi"
      [xAxis]="showXAxis"
      [yAxis]="showYAxis"
      [legend]="showLegend"
      [showXAxisLabel]="showXAxisLabel"
      [showYAxisLabel]="showYAxisLabel"
      [xAxisLabel]="xAxisLabel"
      [yAxisLabel]="yAxisLabel"
      [autoScale]="autoScale">
    </ngx-charts-area-chart>
  `,
})
export class D3AreaStackComponent implements OnDestroy {
  @Input() multi = [{
    name: 'Employee A',
    series: [{
        name: '2010',
        value: 7300000,
      }, {
        name: '2011',
        value: 8940000,
      }],
  }, {
    name: 'Employee B',
    series: [{
        name: '2010',
        value: 7870000,
      }, {
        name: '2011',
        value: 8270000,
      }],
  }, {
    name: 'Employee B',
    series: [{
        name: '2010',
        value: 5000002,
      }, {
        name: '2011',
        value: 5800000,
      }],
  }];
  @Input() showLegend = true;
  @Input() autoScale = true;
  @Input() showXAxis = true;
  @Input() showYAxis = true;
  @Input() showXAxisLabel = true;
  @Input() showYAxisLabel = true;
  @Input() xAxisLabel = 'Employee';
  @Input() yAxisLabel = 'Calls';
  colorScheme: any;
  themeSubscription: any;

  constructor(private theme: NbThemeService) {
    this.themeSubscription = this.theme.getJsTheme().subscribe(config => {
      const colors: any = config.variables;
      this.colorScheme = {
        domain: [colors.primaryLight, colors.infoLight, colors.successLight, colors.warningLight, colors.dangerLight],
      };
    });
  }

  ngOnDestroy(): void {
    this.themeSubscription.unsubscribe();
  }
}
