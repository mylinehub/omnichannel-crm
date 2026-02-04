import { Component, Input, OnDestroy } from '@angular/core';
import { NbThemeService } from '@nebular/theme';

@Component({
  selector: 'ngx-d3-polar',
  template: `
    <ngx-charts-polar-chart
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
    </ngx-charts-polar-chart>
  `,
})
export class D3PolarComponent implements OnDestroy {
  @Input() multi = [
    {
      name: 'Employee A',
      series: [
        {
          name: '1990',
          value: 31476,
        },
        {
          name: '2000',
          value: 36953,
        },
        {
          name: '2010',
          value: 40632,
        },
      ],
    },
    {
      name: 'Employee B',
      series: [
        {
          name: '1990',
          value: 37060,
        },
        {
          name: '2000',
          value: 45986,
        },
        {
          name: '2010',
          value: 49737,
        },
      ],
    },
    {
      name: 'Employee C',
      series: [
        {
          name: '1990',
          value: 29476,
        },
        {
          name: '2000',
          value: 34774,
        },
        {
          name: '2010',
          value: 36240,
        },
      ],
    },
  ];
  @Input() showLegend = true;
  @Input() autoScale = true;
  @Input() showXAxis = true;
  @Input() showYAxis = true;
  @Input() showXAxisLabel = true;
  @Input() showYAxisLabel = true;
  @Input() xAxisLabel = 'Country';
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
