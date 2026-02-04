import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScaleLayoutDirective } from '../directives/scale-layout.directive';

@NgModule({
  declarations: [ScaleLayoutDirective],
  imports: [CommonModule],
  exports: [ScaleLayoutDirective]
})
export class ScaleLayoutModule {}