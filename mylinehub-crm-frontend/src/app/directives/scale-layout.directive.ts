import { Directive, ElementRef, Input, OnChanges, Renderer2, SimpleChanges } from '@angular/core';

@Directive({
  selector: '[appScaleLayout]'
})
export class ScaleLayoutDirective implements OnChanges {
  @Input('appScaleLayout') scale = 1;  // default 1 = no scale

  constructor(private el: ElementRef, private renderer: Renderer2) {}

  ngOnChanges(changes: SimpleChanges): void {
    if ('scale' in changes) {
      const scaleValue = this.scale;

      // Apply transform scale
      this.renderer.setStyle(this.el.nativeElement, 'transform', `scale(${scaleValue})`);

      // Set transform origin to top left (adjust if needed)
      this.renderer.setStyle(this.el.nativeElement, 'transform-origin', 'top left');

      // Adjust width and height to inverse scale for layout shrink
      this.renderer.setStyle(this.el.nativeElement, 'width', `${100 / scaleValue}%`);
      this.renderer.setStyle(this.el.nativeElement, 'height', `${100 / scaleValue}%`);

      // Optional: adjust display for layout consistency
      this.renderer.setStyle(this.el.nativeElement, 'display', 'inline-block');
    }
  }
}
